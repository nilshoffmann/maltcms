/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.commands.fragments.cluster;

import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowProgressResult;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import lombok.Data;

import maltcms.commands.fragments.cluster.pairwiseDistanceCalculator.AWorkerFactory;
import maltcms.commands.fragments.cluster.pairwiseDistanceCalculator.MziDtwWorkerFactory;
import maltcms.commands.fragments.cluster.pairwiseDistanceCalculator.PairwiseDistanceResult;
import maltcms.commands.fragments.cluster.pairwiseDistanceCalculator.PairwiseDistanceWorker;
import maltcms.datastructures.fragments.PairwiseDistances;
import maltcms.io.csv.CSVWriter;
import net.sf.mpaxs.api.ICompletionService;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;

/**
 * Calculates pairwise scores or costs between time series of different lengths.
 *
 * @author Nils Hoffmann
 * 
 */
@ProvidesVariables(names = {"var.minimizing_array_comp",
    "var.pairwise_distance_matrix", "var.pairwise_distance_names"})
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class PairwiseDistanceCalculator extends AFragmentCommand {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PairwiseDistanceCalculator.class);

    @Configurable(description="If true, assumes that the comparison function "
            + "between mass spectra behaves like a cost function "
            + "(smaller is better). If false, assumes that the function "
            + "behaves like a similarity or score function (larger is better).")
    private boolean minimizingLocalDistance = false;
    @Configurable(description="If true, calculates similarities / distances only "
            + "with the first chromatogram. If false, calculates all (n-1)*n/2 alignments.")
    private boolean pairsWithFirstElement = false;
    @Configurable(description="Sets the variable name used to store whether the"
            + " similarity or distance function used required minimization (1)"
            + " or maximization (0)")
    private String minArrayComp = "minimizing_array_comp";
    @Configurable(description="Suffix for the pairwise_distances.cdf files. E.g. for \"_CUST\", the result file will be named \"pairwise_distances_CUST.cdf\" .")
    private String pwdExtension = "";
    @Configurable(description="The worker factory to use for chromatogram / MS comparison. Use either TicDtwWorkerFactory or MziDtwWorkerFactory.")
    private AWorkerFactory workerFactory = new MziDtwWorkerFactory();

    /*
     * (non-Javadoc)
     *
     * @see maltcms.commands.ICommand#apply(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        return pairwiseDistances(t, this);
    }

    private List<Tuple2D<IFileFragment, IFileFragment>> checkInput(
            final TupleND<IFileFragment> t) {
        List<Tuple2D<IFileFragment, IFileFragment>> list;
        if (this.pairsWithFirstElement) {
            list = t.getPairsWithFirstElement();
        } else {
            list = t.getPairs();
        }
        EvalTools.notNull(list, "Input list for " + this.getClass().getName()
                + " is null!", this);
        EvalTools.inRangeI(1, Integer.MAX_VALUE, list.size(), this);
        return list;
    }

    /*
     * (non-Javadoc)
     *
     * @seemaltcms.IConfigurable#configure(org.apache.commons.configuration.
     * Configuration)
     */
    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        this.minArrayComp = cfg.getString("var.minimizing_array_comp",
                "minimizing_array_comp");
        this.pairsWithFirstElement = cfg.getBoolean(this.getClass().getName()
                + ".pairsWithFirstElement", false);
        this.pwdExtension = cfg.getString(this.getClass().getName()
                + ".pwdExtension", "");
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Calculates pairwise distances/similarities between time series.";
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /** {@inheritDoc} */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.STATISTICS;
    }

    private int initMaxLength(final Iterator<IFileFragment> ffiter) {
        int maxlength = 512;
        while (ffiter.hasNext()) {
            final IFileFragment ff = ffiter.next();
            final int len = ff.getName().length();
            if (len > maxlength) {
                maxlength = len;
            }
        }
        return maxlength;
    }

    private ArrayChar.D2 initNames(final TupleND<IFileFragment> t,
            final HashMap<URI, Integer> filenameToIndex, final int nextIndex1,
            final int maxlength) {
        int nextIndex = nextIndex1;
        Iterator<IFileFragment> ffiter;
        final ArrayChar.D2 names = new ArrayChar.D2(t.size(), maxlength);
        ffiter = t.getIterator();
        while (ffiter.hasNext()) {
            final IFileFragment ff = ffiter.next();
            if (!filenameToIndex.containsKey(ff.getUri())) {
                filenameToIndex.put(ff.getUri(), nextIndex);
            }

            names.setString(nextIndex, ff.getUri().toString());
            nextIndex++;
        }
        return names;
    }

    private void normalizePairwiseDistances(
            final ArrayDouble.D2 pairwiseDistances, final boolean isDistance) {
        final MinMax mm = MAMath.getMinMax(pairwiseDistances);
        double val = 0;
        for (int i = 0; i < pairwiseDistances.getShape()[0]; i++) {
            for (int j = 0; j < pairwiseDistances.getShape()[1]; j++) {
                val = (pairwiseDistances.get(i, j) - mm.min)
                        / (mm.max - mm.min);
                if (isDistance) {
                    pairwiseDistances.set(i, j, val);
                } else {
                    // invert similarity to behave like a distance
                    pairwiseDistances.set(i, j, 1.0d - val);
                }
            }
        }
    }

    /**
     * <p>pairwiseDistances.</p>
     *
     * @param t a {@link cross.datastructures.tuple.TupleND} object.
     * @param pdc a {@link maltcms.commands.fragments.cluster.PairwiseDistanceCalculator} object.
     * @return a {@link cross.datastructures.tuple.TupleND} object.
     */
    protected TupleND<IFileFragment> pairwiseDistances(
            final TupleND<IFileFragment> t, final PairwiseDistanceCalculator pdc) {
        EvalTools.notNull(t, this);
        log.info("Received " + t.getSize() + " elements!");
        final List<Tuple2D<IFileFragment, IFileFragment>> list = checkInput(t);
        log.info("Calculating " + list.size() + " pairwise distances!");
        // log.info("Using {} as local function.",
        // this.pairwiseDistanceFunction);

        final HashMap<URI, Integer> filenameToIndex = new HashMap<>();
        final int nextIndex = 0;
        final ArrayDouble.D2 pairwiseDistances = new ArrayDouble.D2(
                t.getSize(), t.getSize());
        final Iterator<IFileFragment> ffiter = t.getIterator();
        final int maxlength = initMaxLength(ffiter);
        final ArrayChar.D2 names = initNames(t, filenameToIndex, nextIndex,
                maxlength);

        final TupleND<IFileFragment> alignments = new TupleND<>();
        final Iterator<Tuple2D<IFileFragment, IFileFragment>> iter = list.iterator();
        int tcnt = 0;
        final int lsize = list.size();
        final String[] stepnames = new String[t.getSize() * (t.getSize() - 1)
                / 2];

        ICompletionService<PairwiseDistanceResult> ccs = createCompletionService(PairwiseDistanceResult.class);
        while (iter.hasNext()) {
            final Tuple2D<IFileFragment, IFileFragment> tuple = iter.next();
            stepnames[tcnt] = "Pairwise distance/similarity of "
                    + tuple.getFirst().getName() + " and "
                    + tuple.getSecond().getName();
            log.debug("Creating job for tuple {}/{}: {} with {}", new Object[]{
                (tcnt + 1), list.size(), tuple.getFirst().getName(),
                tuple.getSecond().getName()});
            final PairwiseDistanceWorker worker = workerFactory.create();
            if (tcnt == 0) {
                log.info("Using {} as pairwise sequence function.", worker.getSimilarity().getClass().getName());
            }
            worker.setInput(new Tuple2D<>(tuple.getFirst().getUri(), tuple.getSecond().getUri()));
            worker.setJobNumber(tcnt);
            worker.setNJobs(lsize);
            worker.setWorkflow(getWorkflow());
            worker.setOutputDirectory(getWorkflow().getOutputDirectory(this));
            ccs.submit(worker);
            tcnt++;
        }
        final DefaultWorkflowProgressResult dwpr = new DefaultWorkflowProgressResult(
                stepnames, this, getWorkflowSlot());
        List<PairwiseDistanceResult> results = Collections.emptyList();
        try {
            results = ccs.call();
        } catch (Exception ex) {
            log.error("Caught exception while executing workers: ", ex);
            throw new RuntimeException(ex);
        }
        log.info("{} workers succeeded, {} failed!", results.size(), ccs.getFailedTasks().size());
        for (final PairwiseDistanceResult tpl : results) {
            if (tpl != null) {
                log.debug("Result: {}", tpl);
                final Integer i1 = filenameToIndex.get(tpl.getInput().getFirst());
                final Integer i2 = filenameToIndex.get(tpl.getInput().getSecond());
                log.debug("Index 1: {}|Index 2: {}", i1, i2);
                pairwiseDistances.set(i1, i2, tpl.getValue());
                pairwiseDistances.set(i2, i1, tpl.getValue());
                IFileFragment ff = new FileFragment(tpl.getAlignment());
                alignments.add(ff);
                final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
                        new File(ff.getUri()), this,
                        WorkflowSlot.ALIGNMENT, ff);
                getWorkflow().append(dwr);
                // notify workflow
                getWorkflow().append(dwpr.nextStep());
            }
        }

        // set diagonal values appropriately
        for (int i = 0; i < pairwiseDistances.getShape()[0]; i++) {
            pairwiseDistances.set(i, i, this.minimizingLocalDistance ? 0.0d
                    : 1.0d);
        }
        final String name = "pairwise_distances" + this.pwdExtension + ".cdf";
        final PairwiseDistances pd = new PairwiseDistances();
        pd.setName(name);
        pd.setPairwiseDistances(pairwiseDistances);
        pd.setNames(names);
        pd.setMinArrayComp(minArrayComp);
        pd.setMinimize(this.minimizingLocalDistance);
        pd.setAlignments(alignments);
        pd.setWorkflow(getWorkflow());
        final IFileFragment ret = new FileFragment(new File(getWorkflow().getOutputDirectory(pd), name));
        pd.modify(ret);
        ret.save();
        final DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(
                ret.getUri()), this, WorkflowSlot.STATISTICS, ret);
        getWorkflow().append(dwr);
        // Factory.getInstance().getConfiguration().setProperty(
        // "pairwise_distances_location", ret.getAbsolutePath());
        saveToCSV(ret, pairwiseDistances, names);
        EvalTools.notNull(alignments, this);
        final TupleND<IFileFragment> tple = new TupleND<>();
        for (final IFileFragment iff : t) {
            final IFileFragment rf = new FileFragment(new File(getWorkflow().getOutputDirectory(this),
                    iff.getName()));
            rf.addSourceFile(iff);
            rf.addSourceFile(ret);
            rf.save();
            tple.add(rf);
            final DefaultWorkflowResult wr = new DefaultWorkflowResult(
                    new File(rf.getUri()), this,
                    WorkflowSlot.STATISTICS, rf);
            getWorkflow().append(wr);
        }
        return tple;
    }

    /**
     * <p>saveToCSV.</p>
     *
     * @param pwdist a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param distances a {@link ucar.ma2.ArrayDouble.D2} object.
     * @param names a {@link ucar.ma2.ArrayChar.D2} object.
     */
    public void saveToCSV(final IFileFragment pwdist,
            final ArrayDouble.D2 distances, final ArrayChar.D2 names) {
        final CSVWriter csvw = Factory.getInstance().getObjectFactory().instantiate(CSVWriter.class);
        csvw.setWorkflow(getWorkflow());
        csvw.writeArray2DwithLabels(getWorkflow().getOutputDirectory(this).getAbsolutePath(), "pairwise_distances.csv", distances, names,
                this.getClass(), WorkflowSlot.STATISTICS, getWorkflow().getStartupDate(), pwdist);
    }
}
