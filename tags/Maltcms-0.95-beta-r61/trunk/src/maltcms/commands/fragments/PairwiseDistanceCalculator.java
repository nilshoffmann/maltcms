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
 * $Id$
 */

/**
 * 
 */
package maltcms.commands.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import maltcms.commands.distances.ListDistanceFunction;
import maltcms.datastructures.fragments.PairwiseDistances;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import apps.Maltcms;
import cross.Factory;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.threads.CallableCompletionService;
import cross.datastructures.threads.ExecutorsManager;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.event.EventSource;
import cross.io.csv.CSVWriter;
import cross.io.misc.StatsWriter;
import cross.tools.EvalTools;
import cross.tools.FileTools;
import cross.tools.StringTools;

/**
 * Calculates pariwise scores or costs between time series of possibly different
 * lengths.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class PairwiseDistanceCalculator extends AFragmentCommand {

	public static void main(final String[] args) {
		final Maltcms m = Maltcms.getInstance();
		System.out.println("Starting Maltcms");
		Factory.getInstance().configure(m.parseCommandLine(args));
		System.out.println("Configured ArrayFactory");
		final TupleND<IFileFragment> t = Factory.prepareInputData();
		final PairwiseDistanceCalculator pdc = new PairwiseDistanceCalculator();
		pdc.apply(t);
		System.exit(0);
	}

	boolean minimizingLocalDistance = false;

	Logger log = Logging.getLogger(this.getClass());

	private boolean pairsWithFirstElement = false;

	// private ExecutorService es = null;

	private String minArrayComp;

	private int nthreads = 1;

	private final EventSource<PairwiseDistances> evSrc = new EventSource<PairwiseDistances>();

	private boolean normalizeToDistances = true;

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
		EvalTools.inRangeI(1, Integer.MAX_VALUE, this.nthreads, this);
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
		this.nthreads = cfg.getInt("maltcms.pipelinethreads", 1);
		this.normalizeToDistances = cfg.getBoolean(this.getClass().getName()
		        + ".normalizeToDistances", true);
		this.log.info("PairwiseDistanceCalculator running with {} threads",
		        this.nthreads);
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

	protected TupleND<IFileFragment> pairwiseDistances(
	        final TupleND<IFileFragment> t, final PairwiseDistanceCalculator pdc) {
		EvalTools.notNull(t, this);
		this.log.info("Received " + t.getSize() + " elements!");
		final List<Tuple2D<IFileFragment, IFileFragment>> list = checkInput(t);
		this.log.info("Calculating " + list.size() + " pairwise distances!");

		// this.es = Executors.newFixedThreadPool(Math.min(list.size(),
		// this.nthreads));
		final HashMap<IFileFragment, Integer> filenameToIndex = new HashMap<IFileFragment, Integer>();
		final int nextIndex = 0;
		final ArrayDouble.D2 pairwiseDistances = new ArrayDouble.D2(
		        t.getSize(), t.getSize());
		final Iterator<IFileFragment> ffiter = t.getIterator();
		final int maxlength = initMaxLength(ffiter);
		final ArrayChar.D2 names = initNames(t, filenameToIndex, nextIndex,
		        maxlength);

		final TupleND<IFileFragment> alignments = new TupleND<IFileFragment>();
		final Iterator<Tuple2D<IFileFragment, IFileFragment>> iter = list
		        .iterator();
		final ArrayList<Callable<Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double>>> al = new ArrayList<Callable<Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double>>>(
		        list.size());
		int tcnt = 0;
		final int lsize = list.size();
		final String[] stepnames = new String[t.getSize() * (t.getSize() - 1)
		        / 2];

		ExecutorsManager e = new ExecutorsManager(Factory.getInstance().getConfiguration().getInt("maltcms.pipelinethreads", 1));
		final CallableCompletionService<Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double>> ccs = new CallableCompletionService<Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double>>(
		        e);
		while (iter.hasNext()) {
			final Tuple2D<IFileFragment, IFileFragment> tuple = iter.next();
			stepnames[tcnt] = "Pairwise distance/similarity of "
			        + tuple.getFirst().getName() + " and "
			        + tuple.getSecond().getName();
			this.log.info("Creating job for tuple {}/{}: {} with {}",
			        new Object[] { (tcnt + 1), list.size(),
			                tuple.getFirst().getName(),
			                tuple.getSecond().getName() });
			tcnt++;
			final int cnt = tcnt;

			final Callable<Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double>> c = new Callable<Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double>>() {

				@Override
				public Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double> call()
				        throws Exception {
					ListDistanceFunction sldf = null;
					sldf = Factory.getInstance().instantiate(
					        Factory.getInstance().getConfiguration().getString(
					                "pairwise.distances.distance"),
					        ListDistanceFunction.class);
					sldf.setIWorkflow(getIWorkflow());
					final String filename = "PW_DISTANCE_"
					        + StringTools.removeFileExt(tuple.getFirst()
					                .getName())
					        + "_"
					        + StringTools.removeFileExt(tuple.getSecond()
					                .getName()) + ".csv";
					final IFileFragment iff = new FileFragment(FileTools
					        .prependDefaultDirs(filename,
					                PairwiseDistanceCalculator.class,
					                getIWorkflow().getStartupDate()), null,
					        null);
					final StatsMap sm = new StatsMap(iff);
					PairwiseDistanceCalculator.this.minimizingLocalDistance = sldf
					        .minimize();
					sldf.setStatsMap(sm);
					final long t_start = System.currentTimeMillis();
					Logging.getLogger(this).info("Running job {}/{}", cnt,
					        lsize);
					final IFileFragment ff = sldf.apply(tuple.getFirst(), tuple
					        .getSecond());
					final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
					        new File(ff.getAbsolutePath()), pdc,
					        WorkflowSlot.ALIGNMENT);
					getIWorkflow().append(dwr);
					alignments.add(ff);
					final long t_end = System.currentTimeMillis() - t_start;
					final double d = sldf.getResult().get();
					final int maplength = MaltcmsTools.getWarpPath(ff).size();
					sm.setLabel(tuple.getFirst().getName() + "-"
					        + tuple.getSecond().getName());
					sm.put("time", new Double(t_end));
					sm.put("value", new Double(d));
					sm.put("maplength", new Double(maplength));
					final StatsWriter sw = Factory.getInstance().instantiate(
					        StatsWriter.class);
					sw.setIWorkflow(getIWorkflow());
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
		for (Tuple2D<Tuple2D<IFileFragment, IFileFragment>, Double> tpl : ccs) {
			if (tpl != null) {
				final Integer i1 = filenameToIndex.get(tpl.getFirst()
				        .getFirst());
				final Integer i2 = filenameToIndex.get(tpl.getFirst()
				        .getSecond());
				pairwiseDistances.set(i1, i2, tpl.getSecond());
				pairwiseDistances.set(i2, i1, tpl.getSecond());
			}
		}
		if (e.isShutdown()) {
			try {
				e.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		if (this.normalizeToDistances && (t.size() > 2)) {
			normalizePairwiseDistances(pairwiseDistances,
			        this.minimizingLocalDistance);
		}
		final String name = "pairwise_distances.cdf";
		final PairwiseDistances pd = new PairwiseDistances();
		pd.setName(name);
		pd.setPairwiseDistances(pairwiseDistances);
		pd.setNames(names);
		pd.setIsMinimizing(this.minimizingLocalDistance);
		pd.setMinimizingArrayComp(this.minArrayComp);
		pd.setAlignments(alignments);
		pd.setIWorkflow(getIWorkflow());
		final IFileFragment ret = pd.provideFileFragment();
		ret.save();
		final DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(
		        ret.getAbsolutePath()), this, WorkflowSlot.STATISTICS);
		getIWorkflow().append(dwr);
		Factory.getInstance().getConfiguration().setProperty(
		        "pairwise_distances_location", ret.getAbsolutePath());
		saveToCSV(ret, pairwiseDistances, names);
		EvalTools.notNull(alignments, this);
		TupleND<IFileFragment> tple = new TupleND<IFileFragment>();
		for (IFileFragment iff : t) {
			IFileFragment rf = FileFragmentFactory.getInstance().create(
			        FileTools.prependDefaultDirs(iff.getName(),
			                this.getClass(), getIWorkflow().getStartupDate()));
			rf.addSourceFile(iff);
			rf.addSourceFile(ret);
			tple.add(rf);
			rf.save();
			final DefaultWorkflowResult wr = new DefaultWorkflowResult(
			        new File(rf.getAbsolutePath()), this,
			        WorkflowSlot.STATISTICS);
			getIWorkflow().append(wr);
		}
		return tple;
	}

	private void normalizePairwiseDistances(ArrayDouble.D2 pairwiseDistances,
	        boolean isDistance) {
		MinMax mm = MAMath.getMinMax(pairwiseDistances);
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

	public void saveToCSV(final IFileFragment pwdist,
	        final ArrayDouble.D2 distances, final ArrayChar.D2 names) {
		final CSVWriter csvw = Factory.getInstance().instantiate(
		        CSVWriter.class);
		csvw.setIWorkflow(getIWorkflow());
		csvw.writeArray2DwithLabels("pairwise_distances.csv", distances, names,
		        this.getClass(), WorkflowSlot.STATISTICS, getIWorkflow()
		                .getStartupDate());
	}

}
