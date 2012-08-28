/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.commands.fragments.cluster;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import maltcms.datastructures.fragments.PairwiseDistances;
import maltcms.io.csv.CSVWriter;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowProgressResult;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.datastructures.tools.EvalTools;
import java.util.Collections;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.cluster.pairwiseDistanceCalculator.AWorkerFactory;
import maltcms.commands.fragments.cluster.pairwiseDistanceCalculator.PairwiseDistanceResult;
import maltcms.commands.fragments.cluster.pairwiseDistanceCalculator.PairwiseDistanceWorker;
import net.sf.mpaxs.api.ICompletionService;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates pairwise scores or costs between time series of different lengths.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@ProvidesVariables(names = {"var.minimizing_array_comp",
    "var.pairwise_distance_matrix", "var.pairwise_distance_names"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class PairwiseDistanceCalculator extends AFragmentCommand {

    @Configurable
    private boolean minimizingLocalDistance = false;
    @Configurable
    private boolean pairsWithFirstElement = false;
    @Configurable
    private String minArrayComp = "minimizing_array_comp";
    @Configurable
    private String pwdExtension = "";
    private AWorkerFactory workerFactory;

    /*
     * (non-Javadoc)
     *
     * @see maltcms.commands.ICommand#apply(java.lang.Object)
     */
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
    @Override
    public void configure(final Configuration cfg) {
        this.minArrayComp = cfg.getString("var.minimizing_array_comp",
                "minimizing_array_comp");
        this.pairsWithFirstElement = cfg.getBoolean(this.getClass().getName()
                + ".pairsWithFirstElement", false);
        this.pwdExtension = cfg.getString(this.getClass().getName()
                + ".pwdExtension", "");
    }

    /**
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Calculates pairwise distances/similarities between time series.";
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /**
     *
     * @return
     */
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
            final HashMap<File, Integer> filenameToIndex, final int nextIndex1,
            final int maxlength) {
        int nextIndex = nextIndex1;
        Iterator<IFileFragment> ffiter;
        final ArrayChar.D2 names = new ArrayChar.D2(t.size(), maxlength);
        ffiter = t.getIterator();
        while (ffiter.hasNext()) {
            final IFileFragment ff = ffiter.next();
            if (!filenameToIndex.containsKey(ff)) {
                filenameToIndex.put(new File(ff.getAbsolutePath()), nextIndex);
            }

            names.setString(nextIndex, ff.getAbsolutePath());
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
     *
     * @param t
     * @param pdc
     * @return
     */
    protected TupleND<IFileFragment> pairwiseDistances(
            final TupleND<IFileFragment> t, final PairwiseDistanceCalculator pdc) {
        EvalTools.notNull(t, this);
        log.info("Received " + t.getSize() + " elements!");
        final List<Tuple2D<IFileFragment, IFileFragment>> list = checkInput(t);
        log.info("Calculating " + list.size() + " pairwise distances!");
        // log.info("Using {} as local function.",
        // this.pairwiseDistanceFunction);

        final HashMap<File, Integer> filenameToIndex = new HashMap<File, Integer>();
        final int nextIndex = 0;
        final ArrayDouble.D2 pairwiseDistances = new ArrayDouble.D2(
                t.getSize(), t.getSize());
        final Iterator<IFileFragment> ffiter = t.getIterator();
        final int maxlength = initMaxLength(ffiter);
        final ArrayChar.D2 names = initNames(t, filenameToIndex, nextIndex,
                maxlength);

        final TupleND<IFileFragment> alignments = new TupleND<IFileFragment>();
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
            worker.setInput(new Tuple2D<File, File>(new File(tuple.getFirst().getAbsolutePath()), new File(tuple.getSecond().getAbsolutePath())));
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
                        new File(ff.getAbsolutePath()), this,
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
        final IFileFragment ret = Factory.getInstance().getFileFragmentFactory().create(new File(getWorkflow().getOutputDirectory(pd), name));
        pd.modify(ret);
        ret.save();
        final DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(
                ret.getAbsolutePath()), this, WorkflowSlot.STATISTICS, ret);
        getWorkflow().append(dwr);
        // Factory.getInstance().getConfiguration().setProperty(
        // "pairwise_distances_location", ret.getAbsolutePath());
        saveToCSV(ret, pairwiseDistances, names);
        EvalTools.notNull(alignments, this);
        final TupleND<IFileFragment> tple = new TupleND<IFileFragment>();
        for (final IFileFragment iff : t) {
            final IFileFragment rf = Factory.getInstance().getFileFragmentFactory().create(new File(getWorkflow().getOutputDirectory(this),
                    iff.getName()));
            rf.addSourceFile(iff);
            rf.addSourceFile(ret);
            rf.save();
            tple.add(rf);
            final DefaultWorkflowResult wr = new DefaultWorkflowResult(
                    new File(rf.getAbsolutePath()), this,
                    WorkflowSlot.STATISTICS, rf);
            getWorkflow().append(wr);
        }
        return tple;
    }

    /**
     *
     * @param pwdist
     * @param distances
     * @param names
     */
    public void saveToCSV(final IFileFragment pwdist,
            final ArrayDouble.D2 distances, final ArrayChar.D2 names) {
        final CSVWriter csvw = Factory.getInstance().getObjectFactory().instantiate(CSVWriter.class);
        csvw.setWorkflow(getWorkflow());
        csvw.writeArray2DwithLabels(getWorkflow().getOutputDirectory(this).getAbsolutePath(), "pairwise_distances.csv", distances, names,
                this.getClass(), WorkflowSlot.STATISTICS, getWorkflow().getStartupDate(), pwdist);
    }
}
