/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: PairwiseDistanceCalculator.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
/**
 * 
 */
package maltcms.commands.fragments.cluster;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import maltcms.commands.distances.ListDistanceFunction;
import maltcms.datastructures.fragments.PairwiseDistances;
import maltcms.io.csv.CSVWriter;
import maltcms.io.misc.StatsWriter;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.threads.CallableCompletionService;
import cross.datastructures.threads.ExecutorsManager;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowProgressResult;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ConstraintViolationException;
import cross.datastructures.tools.EvalTools;
import cross.tools.StringTools;

/**
 * Calculates pariwise scores or costs between time series of possibly different
 * lengths.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@ProvidesVariables(names = {"var.minimizing_array_comp",
    "var.pairwise_distance_matrix", "var.pairwise_distance_names"})
public class PairwiseDistanceCalculator extends AFragmentCommand {

//	public static void main(final String[] args) {
//		final Maltcms m = Maltcms.getInstance();
//		System.out.println("Starting Maltcms");
//		Factory.getInstance().configure(m.parseCommandLine(args));
//		System.out.println("Configured ArrayFactory");
//		final TupleND<IFileFragment> t = Factory.getInstance()
//		        .getInputDataFactory().prepareInputData();
//		final PairwiseDistanceCalculator pdc = new PairwiseDistanceCalculator();
//		pdc.apply(t);
//		System.exit(0);
//	}
    boolean minimizingLocalDistance = false;
    Logger log = Logging.getLogger(this.getClass());
    @Configurable
    private boolean pairsWithFirstElement = false;
    @Configurable
    private String minArrayComp;
    // @Configurable
    // private boolean normalizeToDistances = true;
    @Configurable
    private String pairwiseDistanceFunction = "maltcms.commands.distances.dtw.MZIDynamicTimeWarp";
    @Configurable(name = "cross.executors.timeout")
    private long timeout = 30;
    @Configurable(name = "cross.executors.timeout.unit")
    private String timeoutUnit;
    @Configurable
    private String pwdExtension = "";

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.commands.ICommand#apply(java.lang.Object)
     */
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
                + ".pairsWithFirstElement", true);
        // this.normalizeToDistances = cfg.getBoolean(this.getClass().getName()
        // + ".normalizeToDistances", true);
        this.pairwiseDistanceFunction = cfg.getString(this.getClass().getName()
                + ".pairwiseDistanceFunction",
                "maltcms.commands.distances.dtw.MZIDynamicTimeWarp");
        this.pwdExtension = cfg.getString(this.getClass().getName()
                + ".pwdExtension", "");
        this.timeout = cfg.getLong("cross.executors.timeout", 30);
        this.timeoutUnit = cfg.getString("cross.executors.timeout.unit",
                "MINUTES");
    }

    @Override
    public String getDescription() {
        return "Calculates pairwise distances/similarities between time series.";
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
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
            final HashMap<IFileFragment, Integer> filenameToIndex,
            final int nextIndex1, final int maxlength) {
        int nextIndex = nextIndex1;
        Iterator<IFileFragment> ffiter;
        final ArrayChar.D2 names = new ArrayChar.D2(t.size(), maxlength);
        ffiter = t.getIterator();
        while (ffiter.hasNext()) {
            final IFileFragment ff = ffiter.next();
            if (!filenameToIndex.containsKey(ff)) {
                filenameToIndex.put(ff, nextIndex);
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

    protected TupleND<IFileFragment> pairwiseDistances(
            final TupleND<IFileFragment> t, final PairwiseDistanceCalculator pdc) {
        EvalTools.notNull(t, this);
        this.log.info("Received " + t.getSize() + " elements!");
        final List<Tuple2D<IFileFragment, IFileFragment>> list = checkInput(t);
        this.log.info("Calculating " + list.size() + " pairwise distances!");
        this.log.info("Using {} as local function.",
                this.pairwiseDistanceFunction);

        final HashMap<IFileFragment, Integer> filenameToIndex = new HashMap<IFileFragment, Integer>();
        final int nextIndex = 0;
        final ArrayDouble.D2 pairwiseDistances = new ArrayDouble.D2(
                t.getSize(), t.getSize());
        final Iterator<IFileFragment> ffiter = t.getIterator();
        final int maxlength = initMaxLength(ffiter);
        final ArrayChar.D2 names = initNames(t, filenameToIndex, nextIndex,
                maxlength);

        final TupleND<IFileFragment> alignments = new TupleND<IFileFragment>();
        final Iterator<Tuple2D<IFileFragment, IFileFragment>> iter = list.
                iterator();
        final ArrayList<Callable<Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double>>> al = new ArrayList<Callable<Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double>>>(
                list.size());
        int tcnt = 0;
        final int lsize = list.size();
        final String[] stepnames = new String[t.getSize() * (t.getSize() - 1)
                / 2];

        final ExecutorsManager e = new ExecutorsManager(Factory.getInstance().
                getConfiguration().getInt("maltcms.pipelinethreads", 1));
        final CallableCompletionService<Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double>> ccs = new CallableCompletionService<Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double>>(
                e);
        final String pairwiseDistFunc = this.pairwiseDistanceFunction;
        final IWorkflowElement iwe = this;
        while (iter.hasNext()) {
            final Tuple2D<IFileFragment, IFileFragment> tuple = iter.next();
            stepnames[tcnt] = "Pairwise distance/similarity of "
                    + tuple.getFirst().getName() + " and "
                    + tuple.getSecond().getName();
            this.log.debug("Creating job for tuple {}/{}: {} with {}",
                    new Object[]{(tcnt + 1), list.size(),
                        tuple.getFirst().getName(),
                        tuple.getSecond().getName()});
            tcnt++;
            final int cnt = tcnt;

            final Callable<Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double>> c = new Callable<Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double>>() {

                @Override
                public Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double> call()
                        throws Exception {
                    ListDistanceFunction sldf = null;
                    sldf = Factory.getInstance().getObjectFactory().instantiate(
                            pairwiseDistFunc,
                            ListDistanceFunction.class);
                    sldf.setWorkflow(getWorkflow());
                    final String filename = "PW_DISTANCE_"
                            + StringTools.removeFileExt(
                            tuple.getFirst().getName())
                            + "_"
                            + StringTools.removeFileExt(tuple.getSecond().
                            getName()) + ".csv";
                    final IFileFragment iff = Factory.getInstance().
                            getFileFragmentFactory().create(
                            new File(getWorkflow().getOutputDirectory(
                            iwe), filename));
                    final StatsMap sm = new StatsMap(iff);
                    PairwiseDistanceCalculator.this.minimizingLocalDistance = sldf.
                            minimize();
                    sldf.setStatsMap(sm);
                    final long t_start = System.currentTimeMillis();
                    Logging.getLogger(this).info(
                            "Running job {}/{}: {} with {}",
                            new Object[]{cnt, lsize,
                                tuple.getFirst().getName(),
                                tuple.getSecond().getName()});
                    IFileFragment ff;
                    ff = sldf.apply(tuple.getFirst(), tuple.getSecond());
                    final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
                            new File(ff.getAbsolutePath()), pdc,
                            WorkflowSlot.ALIGNMENT, ff);
                    getWorkflow().append(dwr);
                    alignments.add(ff);
                    final long t_end = System.currentTimeMillis() - t_start;
                    final double d = sldf.getResult().get();
                    final int maplength = MaltcmsTools.getWarpPath(ff).size();
                    sm.setLabel(tuple.getFirst().getName() + "-"
                            + tuple.getSecond().getName());
                    sm.put("time", new Double(t_end));
                    sm.put("value", new Double(d));
                    sm.put("maplength", new Double(maplength));
                    final StatsWriter sw = Factory.getInstance().
                            getObjectFactory().instantiate(StatsWriter.class);
                    sw.setWorkflow(getWorkflow());
                    sw.write(sm);
                    PairwiseDistanceCalculator.this.log.debug("Distance: " + d);
                    tuple.getFirst().clearArrays();
                    tuple.getSecond().clearArrays();
                    return new Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double>(
                            tuple, d);
                }
            };
            al.add(c);
        }
        ccs.submit(al);
        e.shutdown();
        final DefaultWorkflowProgressResult dwpr = new DefaultWorkflowProgressResult(
                stepnames, this, getWorkflowSlot());
        for (final Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double> tpl : ccs) {
            if (tpl != null) {
                final Integer i1 = filenameToIndex.get(tpl.getFirst().getFirst());
                final Integer i2 = filenameToIndex.get(
                        tpl.getFirst().getSecond());
                pairwiseDistances.set(i1, i2, tpl.getSecond());
                pairwiseDistances.set(i2, i1, tpl.getSecond());
                // notify workflow
                getWorkflow().append(dwpr.nextStep());
            }
        }
        // set diagonal values appropriately
        for (int i = 0; i < pairwiseDistances.getShape()[0]; i++) {
            pairwiseDistances.set(i, i, this.minimizingLocalDistance ? 0.0d
                    : 1.0d);
        }
        if (e.isShutdown()) {
            try {
                e.awaitTermination(this.timeout, TimeUnit.valueOf(
                        this.timeoutUnit));
            } catch (final InterruptedException e1) {
                throw new ConstraintViolationException(
                        "Executor terminated abnormally: \n"
                        + e1.getLocalizedMessage());
            }
        }
        //
        // if (this.normalizeToDistances && (t.size() > 2)) {
        // normalizePairwiseDistances(pairwiseDistances,
        // this.minimizingLocalDistance);
        // }
        final String name = "pairwise_distances" + this.pwdExtension + ".cdf";
        final PairwiseDistances pd = new PairwiseDistances();
        pd.setName(name);
        pd.setPairwiseDistances(pairwiseDistances);
        pd.setNames(names);
        pd.setMinArrayComp(this.minArrayComp);
        pd.setMinimize(this.minimizingLocalDistance);
        pd.setAlignments(alignments);
        pd.setWorkflow(getWorkflow());
        final IFileFragment ret = Factory.getInstance().getFileFragmentFactory().
                create(
                new File(getWorkflow().getOutputDirectory(pd), name));
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
            final IFileFragment rf = Factory.getInstance().
                    getFileFragmentFactory().create(
                    new File(getWorkflow().getOutputDirectory(this),
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

    public void saveToCSV(final IFileFragment pwdist,
            final ArrayDouble.D2 distances, final ArrayChar.D2 names) {
        final CSVWriter csvw = Factory.getInstance().getObjectFactory().
                instantiate(CSVWriter.class);
        csvw.setWorkflow(getWorkflow());
        csvw.writeArray2DwithLabels(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), "pairwise_distances.csv", distances, names,
                this.getClass(), WorkflowSlot.STATISTICS, getWorkflow().
                getStartupDate(), pwdist);
    }
}
