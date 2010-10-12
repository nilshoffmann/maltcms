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

package maltcms.commands.fragments;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.AdditionFilter;
import maltcms.commands.filters.array.MovingAverageFilter;
import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.CombinedDomainXYChart;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYChart;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import ucar.nc2.Dimension;
import annotations.ProvidesVariables;
import annotations.RequiresOptionalVariables;
import annotations.RequiresVariables;
import cross.Factory;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.io.csv.CSVWriter;
import cross.tools.ArrayTools;
import cross.tools.EvalTools;
import cross.tools.FileTools;
import cross.tools.MathTools;
import cross.tools.StringTools;

/**
 * Find Peaks based on TIC, estimates a local baseline and, based on a given
 * signal-to-noise ratio, decides whether a maximum is a peak candidate or not.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@RequiresVariables(names = { "var.total_intensity" })
@RequiresOptionalVariables(names = { "var.scan_acquisition_time" })
@ProvidesVariables(names = { "var.tic_peaks", "var.tic_sorted_intensities",
        "var.tic_baseline_deviations", "var.tic_median_adj_intensities" })
public class TICPeakFinder extends AFragmentCommand {

	private final Logger log = Logging.getLogger(this.getClass());

	private double peak_threshold = 1.0d;

	private double epsilon = 0.1d;

	private boolean saveGraphics = false;

	private int filter_window = 20;

	private String ticVarName = "total_intensity";

	private String satVarName = "scan_acquisition_time";

	private String ticPeakVarName = "tic_peaks";

	private String ticSortedIntensVarName = "tic_sorted_intensities";

	private String ticDeviationsVarName = "tic_deviations";

	private String ticMedianAdjIntensVarName = "tic_median_adj_intensities";

	private void addResults(final IFileFragment ff, final Array sortedtic,
	        final Array correctedtic, final Array dev, final ArrayInt.D1 extr) {
		final IVariableFragment peaks = new VariableFragment(ff,
		        this.ticPeakVarName);
		final Dimension peak_number = new Dimension("peak_number", extr
		        .getShape()[0], true, false, false);
		peaks.setDimensions(new Dimension[] { peak_number });
		final IVariableFragment sortedIntensities = new VariableFragment(ff,
		        this.ticSortedIntensVarName);
		final IVariableFragment devs = new VariableFragment(ff,
		        this.ticDeviationsVarName);
		final IVariableFragment mai = new VariableFragment(ff,
		        this.ticMedianAdjIntensVarName);
		devs.setArray(dev);
		peaks.setArray(extr);
		sortedIntensities.setArray(sortedtic);
		mai.setArray(correctedtic);

	}

	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		EvalTools.notNull(t, this);
		final ArrayList<IFileFragment> peaks = new ArrayList<IFileFragment>();
		this.log.info("Searching for peaks");
		for (final IFileFragment f : t) {
			peaks.add(findPeaks(f));
		}
		return new TupleND<IFileFragment>(peaks);
	}

	@Override
	public void configure(final Configuration cfg) {
		this.log.debug("Configure called on TICPeakFinder");
		this.ticPeakVarName = cfg.getString("var.tic_peaks", "tic_peaks");
		this.peak_threshold = cfg.getDouble(this.getClass().getName()
		        + ".peak_threshold", 1.0d);
		this.filter_window = cfg.getInt(this.getClass().getName()
		        + ".median_window", 10);
		this.epsilon = cfg.getDouble(this.getClass().getName()
		        + ".mass_epsilon", 0.1d);
		this.ticVarName = cfg.getString("var.total_intensity",
		        "total_intensity");
		this.satVarName = cfg.getString("var.scan_acquisition_time",
		        "scan_acquisition_time");
		this.ticPeakVarName = cfg.getString("var.tic_peaks", "tic_peaks");
		this.ticSortedIntensVarName = cfg.getString(
		        "var.tic_sorted_intensities", "tic_sorted_intensities");
		this.ticDeviationsVarName = cfg.getString("var.tic_deviations",
		        "tic_deviations");
		this.ticMedianAdjIntensVarName = cfg.getString(
		        "var.tic_median_adj_intensities", "tic_median_adj_intensities");
		this.saveGraphics = cfg.getBoolean(this.getClass().getName()
		        + ".saveGraphics", false);
	}

	private ArrayInt.D1 createPeakCandidatesArray(final Array tic,
	        final ArrayList<Integer> ts) {
		EvalTools.notNull(ts, this);
		final ArrayInt.D1 extr = new ArrayInt.D1(ts.size());
		// checkUniformDistribution(tic.getShape()[0], ts);
		for (int i = 0; i < ts.size(); i++) {
			extr.set(i, ts.get(i));
		}
		return extr;
	}

	/**
	 * @param columnMap
	 * @param ll
	 */
	private void savePeakTable(final List<PeakBounds> l, IFileFragment iff) {
		final Vector<Vector<String>> rows = new Vector<Vector<String>>(l.size());
		Vector<String> headers = null;
		final String[] headerLine = new String[] { "APEX", "START", "STOP",
		        "RT_APEX", "RT_START", "RT_STOP", "AREA", "MW", "INTENSITY" };
		headers = new Vector<String>(Arrays.asList(headerLine));
		this.log.debug("Adding row {}", headers);
		rows.add(headers);
		for (final PeakBounds pb : l) {
			final DecimalFormat df = (DecimalFormat) NumberFormat
			        .getInstance(Locale.US);
			df.applyPattern("0.0000");
			this.log.debug("Adding {} peaks", l.size());
			String[] line = new String[] { pb.apexIndex + "",
			        pb.startIndex + "", pb.stopIndex + "",
			        df.format(pb.apexTime), df.format(pb.startTime),
			        df.format(pb.stopTime), pb.area + "", "" + pb.mw,
			        "" + pb.intensity };
			final Vector<String> v = new Vector<String>(Arrays.asList(line));
			rows.add(v);
			this.log.debug("Adding row {}", v);
		}

		final CSVWriter csvw = new CSVWriter();
		csvw.setIWorkflow(getIWorkflow());
		csvw.writeTableByRows(FileTools.prependDefaultDirs(this.getClass(),
		        getIWorkflow().getStartupDate()).getAbsolutePath(), StringTools
		        .removeFileExt(iff.getName())
		        + "_peakAreas.csv", rows, WorkflowSlot.ALIGNMENT);
	}

	private List<PeakBounds> findPeakAreas(final ArrayList<Integer> ts,
	        IFileFragment iff, double epsilon) {
		ArrayList<PeakBounds> pbs = new ArrayList<PeakBounds>();
		for (Integer scanApex : ts) {
                    PeakBounds pb = getPeakBoundsByUniqueMass(scanApex, iff, epsilon);
                    if(pb!=null){
                        pbs.add(pb);
                    }
		}
		return pbs;
	}

	/**
	 * @param epsilon
	 * @return
	 */
	private double getMaxMassIntensity(final Array intens) {
		final int[] ranksByIntensity = ranksByIntensity(intens);
		final Index idx = intens.getIndex();
		final double maxIntens = intens.getDouble(idx.set(ranksByIntensity[0]));
		return maxIntens;
	}

	/**
	 * @param masses
	 * @param intens
	 * @return
	 */
	private double getMaxMass(final Array masses, final Array intens) {
		final int[] ranksByIntensity = ranksByIntensity(intens);
                if(ranksByIntensity.length>0) {
                    final Index idx = masses.getIndex();
                    this.log.debug("Rank 0: {}",ranksByIntensity[0]);
                    final double maxMass = masses.getDouble(idx.set(ranksByIntensity[0]));
                    this.log.debug("Max mass {} at index {}", maxMass, ranksByIntensity[0]);
                    double maxIntens = MAMath.getMaximum(intens);
                    this.log.debug("Max intens {}={}", maxIntens, intens.getDouble(intens
                            .getIndex().set(ranksByIntensity[0])));
                    EvalTools.eqD(maxIntens, intens.getDouble(intens.getIndex().set(
                            ranksByIntensity[0])), this);
                    // return new Tuple2D<List<Integer>, List<Double>>(Arrays.asList(Integer
                    // .valueOf(maxMassIdx)), Arrays.asList(Double.valueOf(maxMass)));
                    return maxMass;
                }
		return Double.NaN;
	}

	/**
	 * Expects intensities to be sorted in ascending order of masses. Returns
	 * intensities sorted ascending by intensity, so the index of the mass
	 * channel with highest intensity is at intensities.getShape()[0]-1.
	 * 
	 * @param intensities
	 * @return
	 */
	private int[] ranksByIntensity(Array intensities) {
		Index mint = intensities.getIndex();
		List<Tuple2D<Integer, Double>> l = new ArrayList<Tuple2D<Integer, Double>>(
		        intensities.getShape()[0]);
		int[] ranks = new int[intensities.getShape()[0]];
		// identity
		for (int i = 0; i < ranks.length; i++) {
			l.add(new Tuple2D<Integer, Double>(i, Double.valueOf(intensities
			        .getDouble(mint.set(i)))));
			ranks[i] = i;
		}
		// reverse comparator
		Collections.sort(l, Collections
		        .reverseOrder(new Comparator<Tuple2D<Integer, Double>>() {

			        @Override
			        public int compare(Tuple2D<Integer, Double> o1,
			                Tuple2D<Integer, Double> o2) {
				        if (o1.getSecond() < o2.getSecond()) {
					        return -1;
				        } else if (o1.getSecond() > o2.getSecond()) {
					        return 1;
				        }
				        return 0;
			        }
		        }));
		for (int i = 0; i < ranks.length; i++) {
			ranks[i] = l.get(i).getFirst();
		}
		return ranks;
	}

	/**
	 * Will explore the area around scanIndex, in order to find those scans,
	 * which still belong to this peak, by tracking the unique mass and
	 * (maximum) intensity.
	 * 
	 * TODO implement method for fragment mass correlation
	 * 
	 * @param scanIndex
	 * @param f
	 * @param epsilon
	 */
	private PeakBounds getPeakBoundsByUniqueMass(final int scanIndex,
	        final IFileFragment f, final double epsilon) {
		this.log.debug("Checking peak {}", scanIndex);
		final Tuple2D<List<Array>, List<Array>> t = MaltcmsTools.getMZIs(f);
		final Array intens = t.getSecond().get(scanIndex);
		final Array masses = t.getFirst().get(scanIndex);
		final double maxMass = getMaxMass(masses, intens);
                if(Double.isNaN(maxMass)) {
                    this.log.warn("Could not determine max mass for peak {}, skipping!",scanIndex);
                    return null;
                }
		List<Integer> peakMaxMasses = isMaxMass(masses, intens, maxMass,
		        epsilon);
		final double mwIntensity = getMaxMassIntensity(intens);
		int startIndex = -1;
		int stopIndex = -1;
		int apexIndex = scanIndex;
		int r = scanIndex + 1;
		List<Integer> midx = java.util.Collections.emptyList();
		// increase scan index
		SortedMap<Integer, List<Integer>> al = new TreeMap<Integer, List<Integer>>();
		al.put(scanIndex, peakMaxMasses);
		while ((r < t.getFirst().size())) {
			// this.log.info("Checking scan {}", r);
			midx = isMaxMass(t.getFirst().get(r), t.getSecond().get(r),
			        maxMass, epsilon);
			if (midx.size() == 0) {
				break;
			} else {
				Array intensa = t.getSecond().get(r);
				int mIdx = ranksByIntensity(intensa)[0];
				double max = MAMath.getMaximum(intensa);
				if (max == intensa.getDouble(intensa.getIndex().set(mIdx))) {
					// this.log.info("Adding scan {}", r);
					al.put(r, midx);
					r++;
				} else {
					this.log
					        .info("Mass in window, but intensity was not maximal!");
					break;
				}
			}
		}
		// capture post increment with -1
		// startIndex = r;
		if (stopIndex > masses.getShape()[0]) {
			stopIndex = masses.getShape()[0];
		}
		int l = scanIndex - 1;
		// decrease scan index
		while ((l >= 0)) {
			// this.log.info("Checking scan {}", l);
			midx = isMaxMass(t.getFirst().get(l), t.getSecond().get(l),
			        maxMass, epsilon);
			if (midx.size() == 0) {
				break;
			} else {
				Array intensa = t.getSecond().get(l);
				int mIdx = ranksByIntensity(intensa)[0];
				double max = MAMath.getMaximum(intensa);
				if (max == intensa.getDouble(intensa.getIndex().set(mIdx))) {
					// this.log.info("Adding scan {}", l);
					al.put(l, midx);
					l--;
				} else {
					this.log
					        .info("Mass in window, but intensity was not maximal!");
					break;
				}
			}
		}
		// capture post-decrement with +1
		// stopIndex = l;
		this.log.debug("Found {} signals for peak: {}", al.size(), al);
		List<int[]> mwIndices = new ArrayList<int[]>();// int[al.size()][];
		for (Integer key : al.keySet()) {
			int[] arr = new int[al.get(key).size()];
			int i = 0;
			for (Integer itg : al.get(key)) {
				arr[i++] = itg.intValue();
			}
			mwIndices.add(arr);
		}
		this.log.debug("start: {}, stop: {}", (l + 1), r - 1);
		PeakBounds pb = new PeakBounds(l + 1, apexIndex, r - 1, 0, 0);
		integratePeak(pb, f, mwIndices);
		Array sat = f.getChild(this.satVarName).getArray();
		Index sati = sat.getIndex();
		final double startRT = sat.getDouble(sati.set(l + 1));
		final double stopRT = sat.getDouble(sati.set(r - 1));
		final double apexRT = sat.getDouble(sati.set(apexIndex));
		pb.apexTime = apexRT;
		pb.stopTime = stopRT;
		pb.startTime = startRT;
		pb.mw = maxMass;
		pb.intensity = mwIntensity;
		return pb;
	}

	/**
	 * Performs peak area integration on the mass indices given by mwIndices,
	 * within the area defined by PeakBounds, tracking the unique mass with a
	 * maximum error of epsilon.
	 * 
	 * @param pb
	 * @param iff
	 * @param mwIndices
	 * @return
	 */
	private double integratePeak(PeakBounds pb, IFileFragment iff,
	        List<int[]> mwIndices) {
		Tuple2D<List<Array>, List<Array>> t = MaltcmsTools.getMZIs(iff);
		List<Array> masses = t.getFirst().subList(pb.startIndex,
		        pb.stopIndex + 1);
		List<Array> intens = t.getSecond().subList(pb.startIndex,
		        pb.stopIndex + 1);
		EvalTools.eqI(mwIndices.size(), masses.size(), this);
		double s = 0;
		Array a = null;
		Index aidx = null;
		for (int i = 0; i < mwIndices.size(); i++) {
			for (int j = 0; j < mwIndices.get(i).length; j++) {
				int idx = mwIndices.get(i)[j];
				a = intens.get(i);
				aidx = a.getIndex();
				s += (intens.get(i).getDouble(aidx.set(idx)));
			}
		}
		pb.area = s;
		this.log.debug("Raw peak area: {}", s);
		return s;
	}

	private class PeakBounds {
		int startIndex = -1;
		int apexIndex = -1;
		int stopIndex = -1;
		double intensity = 0;
		double startTime = 0;
		double stopTime = 0;
		double apexTime = 0;
		double area = 0;
		double mw = 0;

		PeakBounds(int startIndex, int apexIndex, int stopIndex, double area,
		        double intensity) {
			this.startIndex = startIndex;
			this.apexIndex = apexIndex;
			this.stopIndex = stopIndex;
			this.area = 0;
			this.intensity = intensity;
		}
	}

	/**
	 * Determine, whether the mass with maximum intensity within this scan is
	 * the same (within epsilon) of maxMass. Returns -1 if no mass was found
	 * within epsilon and otherwise the index of the mass within the scan.
	 * 
	 * @param masses
	 * @param intens
	 * @param maxMass
	 * @param epsilon
	 * @return
	 */
	private List<Integer> isMaxMass(Array masses, Array intens, double maxMass,
	        double epsilon) {
		double[] m = (double[]) masses.copyTo1DJavaArray();
		int i = Arrays.binarySearch(m, maxMass);
		// if left neighbor is > epsilon away, proceed to neighbor on right
		// if right neighbor is > epsilon away -> no matching mass found
		// else while next neighbor to right is <= epsilon away, continue

		// exact match
		if (i >= 0) {
			this.log.debug("Exact mass match at {}: {}", m[i], i);
			return Arrays.asList(Integer.valueOf(i));
		} else {// check for insertion position
			this.log.debug("Insertion at {}", i);
			int idx = (-i) - 1;
			int lcnt = idx - 1;
			ArrayList<Integer> al = new ArrayList<Integer>();
			// extend to left
			while (lcnt >= 0) {
				if (Math.abs(m[lcnt] - maxMass) <= epsilon) {
					this.log.debug("mass at index {} within epsilon of {}",
					        lcnt, maxMass);
					lcnt--;
				} else {
					break;
				}
			}
			int rcnt = idx;
			// extend to right
			while (rcnt < m.length) {
				if (Math.abs(m[rcnt] - maxMass) <= epsilon) {
					this.log.debug("mass at index {} within epsilon of {}",
					        rcnt, maxMass);
					rcnt++;
				} else {
					break;
				}
			}
			for (int j = lcnt + 1; j < rcnt; j++) {
				this.log.debug("Adding mass at index {}", j);
				al.add(j);
			}
			return al;
		}
	}

	private IFileFragment findPeaks(final IFileFragment f) {
		// if (f.hasChild("total_intensity")) {
		final Array tic = f.getChild(this.ticVarName).getArray();
		EvalTools.notNull(tic, this);
		double prev = -Double.MAX_VALUE, current = 0.0d, next = 0.0d;
		final Index ind = tic.getIndex();
		final MinMax mm = MAMath.getMinMax(tic);
		// a.getShape()
		final Array sortedtic = Array.factory(tic.getElementType(), tic
		        .getShape());
		final Array correctedtic = Array.factory(tic.getElementType(), tic
		        .getShape());
		final Index cind = correctedtic.getIndex();
		MAMath.copy(sortedtic, tic);
		final double globalmean = MAMath.sumDouble(tic)
		        / (tic.getShape()[0] - 1);
		final double globalvar = (mm.max - mm.min) * (mm.max - mm.min);
		this.log.debug("Squared difference between median and mean: {}",
		        globalvar);
		final Array dev = Array.factory(tic.getElementType(), tic.getShape());
		final Index dind = dev.getIndex();
		final ArrayList<Integer> ts = new ArrayList<Integer>();
		int lastExtrm = -1;
		double baselineEstimate = globalmean, lstddev = 0.0d;
		double lmedian = Math.sqrt(globalvar);
		this.log.debug("Value\tLow\tMedian\tHigh\tDev\tGTMedian\tSNR");
		double[] ticValues = (double[]) tic.get1DJavaArray(double.class);
		final ArrayList<Integer> diffs = new ArrayList<Integer>();
		for (int i = 1; i < tic.getShape()[0] - 1; i++) {
			this.log.debug("i=" + i);
			prev = tic.getDouble(ind.set(i - 1));
			current = tic.getDouble(ind.set(i));
			next = tic.getDouble(ind.set(i + 1));
			// a-1 < a < a+1 -> median = a
			this.log.debug("Checking for extremum!");
			EvalTools.notNull(new Object[] { current, prev, next }, this);
			final int lmedian_low = Math.max(0, i - this.filter_window);
			final int lmedian_high = Math.min(tic.getShape()[0] - 1, i
			        + this.filter_window);
			// this.log.debug("Median low: " + lmedian_low + " high: "
			// + lmedian_high);
			double[] vals;// = new int[lmedian_high-lmedian_low];
			try {
				vals = (double[]) tic.section(new int[] { lmedian_low },
				        new int[] { lmedian_high - lmedian_low },
				        new int[] { 1 }).get1DJavaArray(double.class);
				lmedian = MathTools.median(vals);

				lstddev = Math.abs(vals[vals.length - 1] - vals[0]);
			} catch (final InvalidRangeException e) {
				System.err.println(e.getLocalizedMessage());
			}

			baselineEstimate = MathTools.min(ticValues, i - this.filter_window,
			        i + this.filter_window);

			// lstddev = Math.abs(vals[vals.length - 1] - vals[0]);
			// this.log.info("local rel dev={}", lstddev);
			dind.set(i);
			cind.set(i);
			dev.setDouble(dind, baselineEstimate);// current -
			final double signalEstimate = current;// Math.max(current -
			// baselineEstimate,
			// 0);
			correctedtic.setDouble(cind, signalEstimate - baselineEstimate);

			// lastExtrm = isExtremum(ticValues, ts, diffs, lastExtrm,
			// signalEstimate, lmedian, i, this.median_window);
		}
		lastExtrm = -1;
		ticValues = (double[]) correctedtic.get1DJavaArray(double.class);
		AArrayFilter maf = new MovingAverageFilter();
		maf.configure(Factory.getInstance().getConfiguration());
		Array averagedtic = maf.apply(new Array[] { correctedtic })[0];
		Index aind = averagedtic.getIndex();
		ticValues = (double[]) averagedtic.get1DJavaArray(double.class);
		double maxCorrectedIntensity = MAMath.getMaximum(correctedtic);
		double threshold = this.peak_threshold * maxCorrectedIntensity;
		for (int i = 1; i < averagedtic.getShape()[0] - 1; i++) {
			this.log.debug("i=" + i);
			prev = averagedtic.getDouble(aind.set(i - 1));
			current = averagedtic.getDouble(aind.set(i));
			next = averagedtic.getDouble(aind.set(i + 1));
			// a-1 < a < a+1 -> median = a
			this.log.debug("Checking for extremum!");
			EvalTools.notNull(new Object[] { current, prev, next }, this);
			final int lmedian_low = Math.max(0, i - this.filter_window);
			final int lmedian_high = Math.min(averagedtic.getShape()[0] - 1, i
			        + this.filter_window);
			// this.log.debug("Median low: " + lmedian_low + " high: "
			// + lmedian_high);
			double[] vals;// = new int[lmedian_high-lmedian_low];
			try {
				vals = (double[]) averagedtic.section(
				        new int[] { lmedian_low },
				        new int[] { lmedian_high - lmedian_low },
				        new int[] { 1 }).get1DJavaArray(double.class);
				lmedian = MathTools.median(vals);

				lstddev = Math.abs(vals[vals.length - 1] - vals[0]);
			} catch (final InvalidRangeException e) {
				System.err.println(e.getLocalizedMessage());
			}

			lastExtrm = isExtremum(ticValues, ts, diffs, lastExtrm, current,
			        threshold, i, this.filter_window);
		}
		this.log.info("Found {} peaks for file {}", ts.size(), f.getName());
		final ArrayInt.D1 extr = createPeakCandidatesArray(tic, ts);
		savePeakTable(findPeakAreas(ts, f, epsilon), f);
		if (this.saveGraphics) {
			visualize(f, tic, extr, averagedtic, dev, threshold);
		}
		final String filename = f.getName();
		final IFileFragment ff = cross.datastructures.fragments.FileFragmentFactory
		        .getInstance().create(
		                FileTools.prependDefaultDirs(filename, this.getClass(),
		                        getIWorkflow().getStartupDate()),
		                this.getClass());
		ff.addSourceFile(f);
		addResults(ff, sortedtic, correctedtic, dev, extr);
		ff.save();
		return ff;
		// }
	}

	@Override
	public String getDescription() {
		return "Finds peaks based on total ion current (TIC), using a simple extremum search within a window, combined with a signal-to-noise parameter to select peaks.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.PEAKFINDING;
	}

	private boolean isAboveThreshold(final double snrdb, final double threshold) {
		return (snrdb > threshold);
	}

	private boolean isCandidate(final double prev, final double current,
	        final double next) {
		final boolean b = (prev < current) && (current > next);
		this.log.debug("Found candidate, checking additional constraints!");
		return b;
	}

	private boolean isCandidate(final int index, double[] values, int window) {
		double max = MathTools.max(values, index - window, index + window);
		double indxVal = values[index];
		if (max == indxVal) {
			return true;
		}
		return false;
	}

	private int isExtremum(final double[] values, final ArrayList<Integer> ts,
	        final ArrayList<Integer> diffs, final int lastExtrm1,
	        final double signal, final double threshold, final int i, int window) {
		EvalTools.notNull(new Object[] { values, signal, i, lastExtrm1,
		        threshold }, this);
		int lastExtrm = lastExtrm1;
		this.log.debug("Distance to last extremum: {}", Math
		        .abs(i - lastExtrm1));
		if (signal > 0 && isCandidate(i, values, window)
		        && isAboveThreshold(signal, threshold)) {
			ts.add(i);
			this.log
			        .debug(
			                "Found extremum above minimum threshold {} with value {} at scan: {}",
			                new Object[] { threshold, values[i], i });
			lastExtrm = i;
		}
		return lastExtrm;
	}

	public void visualize(final IFileFragment f, final Array intensities,
	        final ArrayInt.D1 peaks, final Array median_intensities,
	        final Array deviation, final double peakThreshold) {
		Array domain = null;
		final boolean subtract_start_time = true;
		String x_label = "scan number";
		if (f.hasChild(this.satVarName)) {
			domain = f.getChild(this.satVarName).getArray();
			final double min = MAMath.getMinimum(domain);
			x_label = "time [s]";
			if (subtract_start_time) {
				final AdditionFilter af = new AdditionFilter(-min);
				domain = af.apply(new Array[] { domain })[0];
			}
		}
		final ArrayDouble.D1 posx = new ArrayDouble.D1(peaks.getShape()[0]);
		final ArrayDouble.D1 posy = new ArrayDouble.D1(peaks.getShape()[0]);
		final ArrayDouble.D1 threshold = new ArrayDouble.D1(intensities
		        .getShape()[0]);
		ArrayTools.fill(threshold, peakThreshold);
		final Index intensIdx = median_intensities.getIndex();
		final Index satIdx = domain.getIndex();
		for (int i = 0; i < peaks.getShape()[0]; i++) {
			posx.set(i, domain.getDouble(satIdx.set(peaks.get(i))));
			posy.set(i, median_intensities.getInt(intensIdx.set(peaks.get(i))));
		}
		// final AChart<XYPlot> tc1 = new XYChart("Total intensity plot",
		// new String[] { "Total Ion Count (TIC)" },
		// new Array[] { intensities }, new Array[] { domain }, posx,
		// posy, new String[] {}, x_label, "counts");
		final AChart<XYPlot> tc2 = new XYChart("TICPeakFinder results for "
		        + f.getName(), new String[] {
		        "Total Ion Count (TIC)",
		        "TIC with baseline removed, window size=" + this.filter_window
		                + " peak threshold=" + peakThreshold, "threshold" },
		        new Array[] { intensities, median_intensities, threshold },
		        new Array[] { domain }, posx, posy, new String[] {}, x_label,
		        "counts");
		// final AChart<XYPlot> tc3 = new XYChart("Peak candidates",
		// new String[] { "Peak candidates" }, new Array[] { peaks },
		// new Array[] { domain }, x_label, "peak");
		// final AChart<XYPlot> tc4 = new
		// XYChart("Value of median within window",
		// new String[] { "Value of median within window" },
		// new Array[] { deviation }, new Array[] { domain }, x_label,
		// "counts");
		final ArrayList<XYPlot> al = new ArrayList<XYPlot>();
		// al.add(tc1.create());
		// final XYPlot pk = tc3.create();
		final XYBarRenderer xyb = new XYBarRenderer();
		xyb.setShadowVisible(false);
		// pk.setRenderer(xyb);
		// al.add(pk);
		al.add(tc2.create());
		// al.add(tc4.create());
		final CombinedDomainXYChart cdt = new CombinedDomainXYChart("TIC-Peak",
		        x_label, false, al);
		cdt.configure(Factory.getInstance().getConfiguration());
		final PlotRunner pr = new PlotRunner(cdt.create(),
		        "TIC and Peak information for " + f.getName(),
		        "combinedTICandPeakChart-" + f.getName(), getIWorkflow()
		                .getStartupDate());
		pr.configure(Factory.getInstance().getConfiguration());
		Factory.getInstance().submitJob(pr);
	}
}
