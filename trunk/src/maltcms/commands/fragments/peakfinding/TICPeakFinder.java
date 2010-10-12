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

package maltcms.commands.fragments.peakfinding;

import java.awt.geom.Rectangle2D;
import java.io.File;
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

import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.FirstDerivativeFilter;
import maltcms.datastructures.peak.MaltcmsAnnotationFactory;
import maltcms.datastructures.peak.Peak1D;
import maltcms.experimental.datastructures.RingBuffer;
import maltcms.io.csv.CSVWriter;
import maltcms.io.xml.bindings.annotation.MaltcmsAnnotation;
import maltcms.tools.ArrayTools;
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
import ucar.ma2.MAMath;
import ucar.nc2.Dimension;
import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowProgressResult;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.tools.EvalTools;
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
@ProvidesVariables(names = { "var.tic_peaks", "var.tic_filtered" })
public class TICPeakFinder extends AFragmentCommand {

	private final Logger log = Logging.getLogger(this.getClass());

	@Configurable(value = "0.01d")
	private double peak_threshold = 0.01d;

	@Configurable(value = "0.1d")
	private double epsilon = 0.1d;

	@Configurable(value = "false")
	private boolean saveGraphics = false;

	@Configurable(value = "false")
	private boolean integratePeaks = false;

	@Configurable(value = "true")
	private boolean integrateTICPeaks = true;

	@Configurable(value = "20")
	private int filter_window = 20;

	@Configurable(name = "var.total_intensity", value = "total_intensity")
	private String ticVarName = "total_intensity";

	@Configurable(name = "var.scan_acquisition_time", value = "scan_acquisition_time")
	private String satVarName = "scan_acquisition_time";

	@Configurable(value = "tic_peaks")
	private String ticPeakVarName = "tic_peaks";

	@Configurable(value = "tic_filtered")
	private String ticFilteredVarName = "tic_filtered";

	@Configurable(value = "maltcms.commands.filters.array.MultiplicationFilter")
	private List<String> filter = Arrays
	        .asList("maltcms.commands.filters.array.MultiplicationFilter");

	private void addResults(final IFileFragment ff, final Array correctedtic,
	        final ArrayInt.D1 extr) {
		final IVariableFragment peaks = new VariableFragment(ff,
		        this.ticPeakVarName);
		final Dimension peak_number = new Dimension("peak_number", extr
		        .getShape()[0], true, false, false);
		peaks.setDimensions(new Dimension[] { peak_number });
		final IVariableFragment mai = new VariableFragment(ff,
		        this.ticFilteredVarName);
		peaks.setArray(extr);
		mai.setArray(correctedtic);
	}

	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		EvalTools.notNull(t, this);
		final ArrayList<IFileFragment> peaks = new ArrayList<IFileFragment>();
		this.log.info("Searching for peaks");
		// create new ProgressResult
		final DefaultWorkflowProgressResult dwpr = new DefaultWorkflowProgressResult(
		        t.getSize(), this, getWorkflowSlot());
		for (final IFileFragment f : t) {
			peaks.add(findPeaks(f));
			// notify workflow
			getIWorkflow().append(dwpr.nextStep());
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
		        + ".filter_window", 10);
		this.filter = StringTools
		        .toStringList(cfg
		                .getList(
		                        this.getClass().getName() + ".filter",
		                        Arrays
		                                .asList("maltcms.commands.filters.array.MultiplicationFilter")));
		this.epsilon = cfg.getDouble(this.getClass().getName()
		        + ".mass_epsilon", 0.1d);
		this.ticVarName = cfg.getString("var.total_intensity",
		        "total_intensity");
		this.satVarName = cfg.getString("var.scan_acquisition_time",
		        "scan_acquisition_time");
		this.ticPeakVarName = cfg.getString("var.tic_peaks", "tic_peaks");
		this.ticFilteredVarName = cfg.getString("var.tic_filtered",
		        "tic_filtered");
		this.saveGraphics = cfg.getBoolean(this.getClass().getName()
		        + ".saveGraphics", false);
		this.integratePeaks = cfg.getBoolean(this.getClass().getName()
		        + ".integratePeaks", true);
		this.integrateTICPeaks = cfg.getBoolean(this.getClass().getName()
		        + ".integrateTICPeaks", true);
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

	private List<Peak1D> findPeakAreas(final ArrayList<Integer> ts,
	        final IFileFragment iff, final double epsilon,
	        final Array baselineCorrectedTIC) {
		final ArrayList<Peak1D> pbs = new ArrayList<Peak1D>();
		if (integrateTICPeaks) {
			this.log.info("Using TIC based peak integration");
			// fall back to TIC
			for (final Integer scanApex : ts) {
				this.log.debug("Adding peak at scan index {}", scanApex);
				final Peak1D pb = getPeakBoundsByTIC(scanApex, iff,
				        baselineCorrectedTIC);
				if (pb != null) {
					pbs.add(pb);
				}
			}
			Rectangle2D.Double l1 = null;
			Peak1D prev = null;
			this.log
			        .info("Checking peak areas for overlapping or completely contained peaks!");
			List<Peak1D> overlaps = new ArrayList<Peak1D>();
			for (Peak1D peak : pbs) {
				if (l1 == null) {
					l1 = new Rectangle2D.Double(peak.getStartIndex(), 0, peak
					        .getStopIndex()
					        - peak.getStartIndex(), 1);
					prev = peak;
				} else {
					Rectangle2D.Double l2 = new Rectangle2D.Double(peak
					        .getStartIndex(), 0, peak.getStopIndex()
					        - peak.getStartIndex(), 1);
					if (l1.intersects(l2) || l1.contains(l2) || l2.contains(l1)) {
						this.log.warn("Peak area overlap detected!");
						overlaps.add(prev);
						overlaps.add(peak);
					}
					l1 = l2;
					prev = peak;
				}
			}
			if (overlaps.size() > 0) {
				this.log.info("Overlapping peaks: {}", overlaps);
			}
			pbs.removeAll(overlaps);
		}
		// else {
		// this.log.info("Using MZ characteristic mass integration");
		// try {// try to get MZs first
		// // MaltcmsTools.getMZIs(iff);
		// for (final Integer scanApex : ts) {
		// final Peak1D pb = getPeakBoundsByCharacteristicMasses(
		// scanApex, iff, epsilon);
		// if (pb != null) {
		// pbs.add(pb);
		// }
		// }
		// } catch (final ResourceNotAvailableException re) {
		// // fall back to TIC
		// for (final Integer scanApex : ts) {
		// this.log.debug("Adding peak at scan index {}", scanApex);
		// final Peak1D pb = getPeakBoundsByTIC(scanApex, iff,
		// baselineCorrectedTIC);
		// if (pb != null) {
		// pbs.add(pb);
		// }
		// }
		// }
		// }
		return pbs;
	}

	private double getIntensityForMassRange(Array masses, Array intensity,
	        double minMass, double maxMass) {
		double intensities = 0;
		double[] massesA = (double[]) masses.get1DJavaArray(double.class);
		int lb = Arrays.binarySearch(massesA, minMass);
		int ub = Arrays.binarySearch(massesA, maxMass);
		if (lb < 0) { // insert position is less than mass of interest
			// (-(insertionPoint) - 1)
			lb = Math.max(0, Math.min(massesA.length - 1, (-1) * (lb + 1)));
		} else {
			lb = Math.max(0, Math.min(massesA.length - 1, lb));
		}
		if (ub < 0) { // insert position is greater than mass of interest
			ub = Math.max(0, Math.min(massesA.length, ((-1) * (ub + 1)) + 1));
		} else {
			ub = Math.max(0, Math.min(massesA.length, ub + 1));
		}
		Index intenIdx = intensity.getIndex();
		for (int i = lb; i < ub; i++) {
			intensities += intensity.getDouble(intenIdx.set(i));
		}
		return intensities;
	}

	private double getMaxMassForMassRange(Array masses, Array intensity,
	        double minMass, double maxMass) {
		double[] massesA = (double[]) masses.get1DJavaArray(double.class);
		int lb = Arrays.binarySearch(massesA, minMass);
		int ub = Arrays.binarySearch(massesA, maxMass);
		if (lb < 0) { // insert position is less than mass of interest
			// (-(insertionPoint) - 1)
			lb = Math.max(0, Math.min(massesA.length - 1, (-1) * (lb + 1)));
		} else {
			lb = Math.max(0, Math.min(massesA.length - 1, lb));
		}
		if (ub < 0) { // insert position is greater than mass of interest
			ub = Math.max(0, Math.min(massesA.length, ((-1) * (ub + 1)) + 1));
		} else {
			ub = Math.max(0, Math.min(massesA.length, ub + 1));
		}
		Index massesIdx = masses.getIndex();
		Index intenIdx = intensity.getIndex();
		List<Tuple2D<Double, Double>> miPairs = new ArrayList<Tuple2D<Double, Double>>();
		for (int i = lb; i < ub; i++) {
			miPairs.add(new Tuple2D<Double, Double>(masses.getDouble(massesIdx
			        .set(i)), intensity.getDouble(intenIdx.set(i))));
		}
		Collections.sort(miPairs, new Comparator<Tuple2D<Double, Double>>() {

			@Override
			public int compare(Tuple2D<Double, Double> o1,
			        Tuple2D<Double, Double> o2) {
				double m1 = o1.getFirst();
				double m2 = o2.getFirst();
				double i1 = o1.getSecond();
				double i2 = o2.getSecond();
				if (m1 < m2) {
					return -1;
				} else if (m1 > m2) {
					return 1;
				} else {
					if (i1 < i2) {
						return -1;
					} else if (i1 > i2) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		});
		return miPairs.get(miPairs.size() - 1).getFirst();
	}

	private IFileFragment findPeaks(final IFileFragment f) {
		// this.log.info("Looking for EIC peaks");
		// findEICPeaks(f);
		final Array tic = f.getChild(this.ticVarName).getArray();
		EvalTools.notNull(tic, this);
		Array correctedtic = null;
		final ArrayList<Integer> ts = new ArrayList<Integer>();
		this.log.debug("Value\tLow\tMedian\tHigh\tDev\tGTMedian\tSNR");
		double[] ticValues = (double[]) tic.get1DJavaArray(double.class);
		correctedtic = applyFilters(tic);
		// ticValues = getMinimumBaselineEstimate((double[]) correctedtic
		// .get1DJavaArray(double.class));
		// correctedtic = Array.factory(ticValues);
		double[] snrValues = new double[ticValues.length];
		double[] cticValues = (double[]) correctedtic
		        .get1DJavaArray(double.class);
		double[] baselineValues = new double[ticValues.length];
		for (int i = 0; i < snrValues.length; i++) {
			baselineValues[i] = ticValues[i] - cticValues[i];
		}
		for (int i = 0; i < snrValues.length; i++) {
			double baselineEst = MathTools.averageOfSquares(baselineValues, i
			        - this.filter_window, i + this.filter_window);
			double signalEst = MathTools.averageOfSquares(cticValues, i
			        - this.filter_window, i + this.filter_window);
			double snr = 20.0d * Math.log10(Math.sqrt(signalEst)
			        / Math.sqrt(baselineEst));
			snrValues[i] = Double.isInfinite(snr) ? 0 : snr;
		}
		this.log.debug("SNR: {}", Arrays.toString(snrValues));
		final double maxCorrectedIntensity = MAMath.getMaximum(correctedtic);
		final double threshold = this.peak_threshold;// * maxCorrectedIntensity;
		for (int i = 0; i < ticValues.length; i++) {
			this.log.debug("i=" + i);
			checkExtremum(ticValues, snrValues, ts, threshold, i,
			        this.filter_window);
		}
		this.log.info("Found {} peaks for file {}", ts.size(), f.getName());
		final ArrayInt.D1 extr = createPeakCandidatesArray(tic, ts);
		if (this.integratePeaks) {
			savePeakTable(findPeakAreas(ts, f, this.epsilon, correctedtic), f);
		}
		if (this.saveGraphics) {
			visualize(f, tic, snrValues, extr, threshold);
		}
		final String filename = f.getName();
		final IFileFragment ff = Factory.getInstance().getFileFragmentFactory()
		        .create(
		                new File(getIWorkflow().getOutputDirectory(this),
		                        filename));

		ff.addSourceFile(f);
		addResults(ff, correctedtic, extr);
		ff.save();
		f.clearArrays();
		DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(ff
		        .getAbsolutePath()), this, WorkflowSlot.PEAKFINDING, ff);
		getIWorkflow().append(dwr);
		return ff;
		// }
	}

	// /**
	// * @param f
	// */
	// private void findEICPeaks(final IFileFragment f) {
	// double minMass, maxMass, stepSize;
	// Tuple2D<Double, Double> t = MaltcmsTools.getMinMaxMassRange(f);
	// minMass = t.getFirst();
	// maxMass = t.getSecond();
	// stepSize = 1.0;
	// double range = maxMass - minMass;
	// int steps = (int) Math.floor(range / stepSize) + 1;
	// double start = Math.floor(minMass);
	// TreeMap<Double, TreeSet<Peak1D>> rtToPeakMap = new TreeMap<Double,
	// TreeSet<Peak1D>>();
	// for (int i = 0; i < steps; i++) {
	// Array eic = MaltcmsTools.getEIC(f, start, start + stepSize, true,
	// false);
	//
	// EvalTools.notNull(eic, this);
	// Array correctedeic = null;
	// final ArrayList<Integer> ts = new ArrayList<Integer>();
	// // this.log.debug("Value\tLow\tMedian\tHigh\tDev\tGTMedian\tSNR");
	// double[] eicValues = (double[]) eic.get1DJavaArray(double.class);
	// correctedeic = applyFilters(eic);
	// eicValues = getMinimumBaselineEstimate((double[]) correctedeic
	// .get1DJavaArray(double.class));
	// correctedeic = Array.factory(eicValues);
	// final double maxCorrectedIntensity = MAMath
	// .getMaximum(correctedeic);
	// final double threshold = this.peak_threshold
	// * maxCorrectedIntensity;
	// for (int j = 0; j < eicValues.length; j++) {
	// // this.log.debug("j=" + j);
	// checkExtremum(eicValues, ts, threshold, j, this.filter_window);
	// }
	// if (ts.size() > 0) {
	// this.log.debug("Found {} peaks for file {} at mass {} to {}",
	// new Object[] { ts.size(), f.getName(), start,
	// start + stepSize });
	// }
	// final ArrayInt.D1 extr = createPeakCandidatesArray(eic, ts);
	// for (int k = 0; k < extr.getShape()[0]; k++) {
	// int peak = extr.get(k);
	// Tuple2D<Array, Array> tple = MaltcmsTools.getMS(f, peak);
	// double area = getIntensityForMassRange(tple.getFirst(), tple
	// .getSecond(), start, start + stepSize);
	// Peak1D p = new Peak1D(peak, peak, peak, area, area);
	// p.setMw(getMaxMassForMassRange(tple.getFirst(), tple
	// .getSecond(), start, start + stepSize));
	// p.setApexTime(MaltcmsTools.getScanAcquisitionTime(f, peak));
	// p.setStartTime(p.getApexTime());
	// p.setStopTime(p.getApexTime());
	// p.setFile(f.getName());
	// // this.log.info("{}", p);
	// if (rtToPeakMap.containsKey(p.getApexTime())) {
	// TreeSet<Peak1D> s = rtToPeakMap.get(p.getApexTime());
	// s.add(p);
	// } else {
	// TreeSet<Peak1D> s = new TreeSet<Peak1D>(
	// new Comparator<Peak1D>() {
	//
	// @Override
	// public int compare(Peak1D p1, Peak1D p2) {
	// double m1 = p1.getMw();
	// double m2 = p2.getMw();
	// double i1 = p1.getIntensity();
	// double i2 = p2.getIntensity();
	// if (m1 < m2) {
	// return -1;
	// } else if (m1 > m2) {
	// return 1;
	// } else {
	// if (i1 < i2) {
	// return -1;
	// } else if (i1 > i2) {
	// return 1;
	// } else {
	// return 0;
	// }
	// }
	// }
	// });
	// s.add(p);
	// rtToPeakMap.put(p.getApexTime(), s);
	// }
	//
	// }
	// start += stepSize;
	//
	// }
	// TreeMap<Integer, Integer> hm = new TreeMap<Integer, Integer>();
	// int totalPeakSignals = 0;
	// for (Double d : rtToPeakMap.keySet()) {
	// TreeSet<Peak1D> ts = rtToPeakMap.get(d);
	// totalPeakSignals += ts.size();
	// if (hm.containsKey(ts.size())) {
	// Integer itg = hm.get(ts.size());
	// itg += 1;
	// hm.put(ts.size(), itg);
	// } else {
	// hm.put(ts.size(), 1);
	// }
	// }
	// List<List<String>> v = new ArrayList<List<String>>();
	// List<String> header = new ArrayList<String>(Arrays.asList(new String[] {
	// "ScanIndex", "RT", "MW", "Intensity", "File" }));
	// v.add(header);
	// String label = StringTools.removeFileExt(f.getName());
	// int points = 0;
	// for (Double d : rtToPeakMap.keySet()) {
	// TreeSet<Peak1D> ts = rtToPeakMap.get(d);
	// for (Peak1D p : ts) {
	// ArrayList<String> peak = new ArrayList<String>(Arrays
	// .asList(new String[] { p.getApexIndex() + "",
	// p.getApexTime() + "", p.getMw() + "",
	// p.getIntensity() + "", label }));
	// v.add(peak);
	// points++;
	// }
	// }
	// CSVWriter csvw = new CSVWriter();
	// csvw.setIWorkflow(getIWorkflow());
	// csvw.writeTableByRows(getIWorkflow().getOutputDirectory(this)
	// .getAbsolutePath(), StringTools.removeFileExt(f.getName())
	// + "_eicPeaks.csv", v, WorkflowSlot.PEAKFINDING);
	// this.log.info("Number of peak signal groups of sizes: {}", hm);
	// this.log.info("Total number of peak signals: {}", totalPeakSignals);
	// FileFragment ff = new FileFragment(getIWorkflow().getOutputDirectory(
	// this), StringTools.removeFileExt(f.getName())
	// + "_apexPeaks.cdf");
	// IVariableFragment mv = new VariableFragment(ff, "mass_values");
	// IVariableFragment iv = new VariableFragment(ff, "intensity_values");
	// IVariableFragment satv = new VariableFragment(ff,
	// "scan_acquisition_time");
	// IVariableFragment ticv = new VariableFragment(ff, "total_intensity");
	// IVariableFragment sidxv = new VariableFragment(ff, "scan_index");
	// int scans = rtToPeakMap.size();
	// ArrayDouble.D1 masses = new ArrayDouble.D1(points);
	// ArrayDouble.D1 intensities = new ArrayDouble.D1(points);
	// ArrayDouble.D1 sats = new ArrayDouble.D1(scans);
	// ArrayInt.D1 tics = new ArrayInt.D1(scans);
	// ArrayInt.D1 sidx = new ArrayInt.D1(scans);
	// int scanIndex = 0;
	// points = 0;
	// for (Double d : rtToPeakMap.keySet()) {
	// sidx.set(scanIndex, points);
	// TreeSet<Peak1D> ts = rtToPeakMap.get(d);
	// int ticval = 0;
	// double rt = 0;
	// for (Peak1D p : ts) {
	// masses.set(points, p.getMw());
	// intensities.set(points, p.getIntensity());
	// ticval += p.getIntensity();
	// rt = p.getApexTime();
	// points++;
	// }
	// sats.set(scanIndex, rt);
	// tics.set(scanIndex, ticval);
	// scanIndex++;
	// }
	// mv.setArray(masses);
	// iv.setArray(intensities);
	// satv.setArray(sats);
	// ticv.setArray(tics);
	// sidxv.setArray(sidx);
	// ff.save();
	// DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(ff
	// .getAbsolutePath()), this, WorkflowSlot.PEAKFINDING, ff);
	// getIWorkflow().append(dwr);
	// }

	/**
	 * @param correctedtic
	 * @return
	 */
	private Array applyFilters(final Array correctedtic) {
		final List<AArrayFilter> filters = new ArrayList<AArrayFilter>(
		        this.filter.size());
		for (String s : this.filter) {
			filters.add(Factory.getInstance().getObjectFactory().instantiate(s,
			        AArrayFilter.class));
		}
		final Array filteredtic = ArrayTools
		        .applyFilters(correctedtic, filters);
		return filteredtic;
	}

	/**
	 * @param ticValues
	 */
	private double[] getMinimumBaselineEstimate(final double[] ticValues) {
		double baselineEstimate;
		double[] estimate = new double[ticValues.length];
		for (int i = 0; i < ticValues.length; i++) {
			this.log.debug("i=" + i);

			baselineEstimate = MathTools.min(ticValues, i - this.filter_window,
			        i + this.filter_window);
			final double signalEstimate = ticValues[i];
			estimate[i] = signalEstimate - baselineEstimate;
		}
		return estimate;
	}

	@Override
	public String getDescription() {
		return "Finds peaks based on total ion current (TIC), using a simple extremum search within a window, combined with a signal-to-noise parameter to select peaks.";
	}

	private int findLeftInclusivePeakBound(final int scanIndex,
	        final IFileFragment f, final Array baselineCorrectedTIC,
	        final Array firstDerivative, final Array secondDerivative) {
		final Index idx = baselineCorrectedTIC.getIndex();
		final Index fidx = firstDerivative.getIndex();
		final Index sidx = secondDerivative.getIndex();
		int lbound = scanIndex - 1;
		while (lbound >= -1) {
			// left border, no more elements
			if (lbound == -1) {
				return lbound + 1;
			}
			double val = baselineCorrectedTIC.getDouble(idx.set(lbound));
			double fdv = firstDerivative.getDouble(fidx.set(lbound));
			double sdv = secondDerivative.getDouble(sidx.set(lbound));
			if (val == 0 || (fdv == 0 && sdv >= 0)) {
				return lbound + 1;
			} else {
				lbound--;
			}
		}
		return scanIndex;
	}

	private int findRightInclusivePeakBound(final int scanIndex,
	        final IFileFragment f, final Array baselineCorrectedTIC,
	        final Array firstDerivative, final Array secondDerivative) {
		final Index idx = baselineCorrectedTIC.getIndex();
		final Index fidx = firstDerivative.getIndex();
		final Index sidx = secondDerivative.getIndex();
		final int size = baselineCorrectedTIC.getShape()[0];
		int rbound = scanIndex + 1;
		while (rbound <= size) {
			// right border, no more elements
			if (rbound == size) {
				return rbound - 1;
			}
			double val = baselineCorrectedTIC.getDouble(idx.set(rbound));
			double fdv = firstDerivative.getDouble(fidx.set(rbound));
			double sdv = secondDerivative.getDouble(sidx.set(rbound));
			if (val == 0 || (fdv == 0 && sdv > 0)) {
				return rbound - 1;
			} else {
				rbound++;
			}
		}
		return scanIndex;
	}

	private Peak1D getPeakBoundsByTIC2(final int scanIndex,
	        final IFileFragment f, final Array baselineCorrectedTIC) {
		FirstDerivativeFilter fdf = new FirstDerivativeFilter();
		Array firstDerivative = fdf.apply(baselineCorrectedTIC);
		Array secondDerivative = fdf.apply(firstDerivative);
		int startIndex = findLeftInclusivePeakBound(scanIndex, f,
		        baselineCorrectedTIC, firstDerivative, secondDerivative);
		int stopIndex = findRightInclusivePeakBound(scanIndex, f,
		        baselineCorrectedTIC, firstDerivative, secondDerivative);

		this.log.debug("start: {}, stop: {}", startIndex, stopIndex);
		final Peak1D pb = new Peak1D(startIndex, scanIndex, stopIndex, 0, 0);
		final Array sat = f.getChild(this.satVarName).getArray();
		final Index sati = sat.getIndex();
		final double startRT = sat.getDouble(sati.set(startIndex));
		final double apexRT = sat.getDouble(sati.set(scanIndex));
		final double stopRT = sat.getDouble(sati.set(stopIndex));
		pb.setApexTime(apexRT);
		pb.setStopTime(stopRT);
		pb.setStartTime(startRT);
		integratePeak(pb, f, null);
		return pb;
	}

	private Peak1D getPeakBoundsByTIC(final int scanIndex,
	        final IFileFragment f, final Array baselineCorrectedTIC) {
		// return getPeakBoundsByTIC2(scanIndex, f, baselineCorrectedTIC);
		final Index idx = baselineCorrectedTIC.getIndex();
		final int size = baselineCorrectedTIC.getShape()[0];
		int startIndex = -1;
		int stopIndex = -1;
		final int apexIndex = scanIndex;
		int r = scanIndex + 1;
		int l = scanIndex - 1;
		// start at peak apex = scanIndex
		// order: prev, current, next
		RingBuffer<Double> rb = new RingBuffer<Double>(3);
		double prev = baselineCorrectedTIC.getDouble(idx.set(apexIndex));
		double current = baselineCorrectedTIC.getDouble(idx.set(Math.min(
		        size - 1, r)));
		double next = baselineCorrectedTIC.getDouble(idx.set(Math.min(size - 1,
		        r + 1)));
		rb.push(prev);
		rb.push(current);
		rb.push(next);
		while ((r < size)) {
			if (isMinimum(rb.oldest(), rb.previous(), rb.current())) {
				stopIndex = r;
				break;
			}
			rb.push(baselineCorrectedTIC.getDouble(idx.set(Math.min(size - 1,
			        r + 1))));
			r++;
		}
		stopIndex = Math.min(stopIndex - 1, size - 1);

		// start at peak apex = scanIndex
		// order: prev, current, next
		RingBuffer<Double> rb2 = new RingBuffer<Double>(3);
		prev = baselineCorrectedTIC.getDouble(idx.set(apexIndex));
		current = baselineCorrectedTIC.getDouble(idx.set(Math.max(0, l)));
		next = baselineCorrectedTIC.getDouble(idx.set(Math.max(0, l - 1)));
		rb2.push(prev);
		rb2.push(current);
		rb2.push(next);
		// decrease scan index
		while ((l >= 0)) {
			if (isMinimum(rb2.current(), rb2.previous(), rb2.oldest())) {
				startIndex = l;
				break;
			}
			rb2.push(baselineCorrectedTIC
			        .getDouble(idx.set(Math.max(0, l - 1))));
			l--;
		}
		startIndex = Math.max(0, startIndex + 1);

		this.log.debug("start: {}, stop: {}", startIndex, stopIndex);
		final Peak1D pb = new Peak1D(startIndex, apexIndex, stopIndex, 0, 0);
		final Array sat = f.getChild(this.satVarName).getArray();
		final Index sati = sat.getIndex();
		final double startRT = sat.getDouble(sati.set(l + 1));
		final double stopRT = sat.getDouble(sati.set(r - 1));
		final double apexRT = sat.getDouble(sati.set(apexIndex));
		pb.setApexTime(apexRT);
		pb.setStopTime(stopRT);
		pb.setStartTime(startRT);
		integratePeak(pb, f, null);
		return pb;
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
	private Peak1D getPeakBoundsByCharacteristicMasses(final int scanIndex,
	        final IFileFragment f, final double epsilon) {
		this.log.debug("Checking peak {}", scanIndex);
		final Tuple2D<List<Array>, List<Array>> t = MaltcmsTools.getMZIs(f);
		Array intens = t.getSecond().get(scanIndex);
		Array masses = t.getFirst().get(scanIndex);
		final double maxMass = MaltcmsTools.getMaxMass(masses, intens);
		this.log.debug("Max mass: {}", maxMass);
		if (Double.isNaN(maxMass)) {
			this.log.warn(
			        "Could not determine max mass for peak {}, skipping!",
			        scanIndex);
			return null;
		}
		final List<Integer> peakMaxMasses = MaltcmsTools.isMaxMass(masses,
		        intens, maxMass, epsilon);
		final double mwIntensity = MaltcmsTools.getMaxMassIntensity(intens);
		final int startIndex = -1;
		int stopIndex = -1;
		final int apexIndex = scanIndex;
		int r = scanIndex + 1;
		List<Integer> midx = java.util.Collections.emptyList();
		// increase scan index
		final SortedMap<Integer, List<Integer>> al = new TreeMap<Integer, List<Integer>>();
		al.put(scanIndex, peakMaxMasses);
		this.log.debug("Extending peak to the right");
		while ((r < t.getFirst().size())) {
			// this.log.info("Checking scan {}", r);
			masses = t.getFirst().get(r);
			intens = t.getSecond().get(r);
			midx = MaltcmsTools.isMaxMass(masses, intens, maxMass, epsilon);
			if (midx.size() == 0) {
				break;
			} else {
				final int[] ranks = MaltcmsTools.ranksByIntensity(intens);
				for (int k = 0; k < ranks.length; k++) {
					// final int mIdx = ranks[k];
					// final double max = MAMath.getMaximum(intensa);
					// if (max ==
					// intensa.getDouble(intensa.getIndex().set(mIdx))) {
					// this.log.info("Adding scan {}", r);
					al.put(r, midx);

					// } else {
					// this.log
					// .warn("Mass in window, but intensity was not maximal!");
					// break;
					// }
				}
				r++;
				// old
				// final int mIdx = ranks[0];
				// final double max = MAMath.getMaximum(intensa);
				// if (max == intensa.getDouble(intensa.getIndex().set(mIdx))) {
				// // this.log.info("Adding scan {}", r);
				// al.put(r, midx);
				// r++;
				// } else {
				// this.log
				// .warn("Mass in window, but intensity was not maximal!");
				// break;
				// }
			}
		}
		// capture post increment with -1
		// startIndex = r;
		int l = scanIndex - 1;
		// decrease scan index
		this.log.debug("Extending peak to the left");
		while ((l >= 0)) {
			masses = t.getFirst().get(l);
			intens = t.getSecond().get(l);
			// this.log.info("Checking scan {}", l);
			midx = MaltcmsTools.isMaxMass(masses, intens, maxMass, epsilon);
			if (midx.size() == 0) {
				break;
			} else {
				final int[] ranks = MaltcmsTools.ranksByIntensity(intens);
				// final int mIdx = ranks[0];
				// final double max = MAMath.getMaximum(intensa);
				for (int k = 0; k < ranks.length; k++) {
					// if (max ==
					// intensa.getDouble(intensa.getIndex().set(mIdx))) {
					// this.log.info("Adding scan {}", l);
					al.put(l, midx);

					// } else {
					// this.log
					// .info("Mass in window, but intensity was not maximal!");
					// break;
					// }
				}
				l--;
			}
		}
		this.log.debug("Found {} signals for peak: {}", al.size(), al);
		final List<int[]> mwIndices = new ArrayList<int[]>();// int[al.size()][];
		for (final Integer key : al.keySet()) {
			final int[] arr = new int[al.get(key).size()];
			int i = 0;
			for (final Integer itg : al.get(key)) {
				arr[i++] = itg.intValue();
			}
			mwIndices.add(arr);
		}
		this.log.debug("start: {}, stop: {}", (l + 1), r - 1);
		final Peak1D pb = new Peak1D(l + 1, apexIndex, r - 1, 0, 0);
		integratePeak(pb, f, mwIndices);
		final Array sat = f.getChild(this.satVarName).getArray();
		final Index sati = sat.getIndex();
		final double startRT = sat.getDouble(sati.set(l + 1));
		final double stopRT = sat.getDouble(sati.set(r - 1));
		final double apexRT = sat.getDouble(sati.set(apexIndex));
		pb.setApexTime(apexRT);
		pb.setStopTime(stopRT);
		pb.setStartTime(startRT);
		pb.setMw(maxMass);
		pb.setIntensity(mwIntensity);
		return pb;
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

	/**
	 * Performs peak area integration on the mass indices given by mwIndices,
	 * within the area defined by Peak1D, tracking the unique mass with a
	 * maximum error of epsilon.
	 * 
	 * @param pb
	 * @param iff
	 * @param mwIndices
	 * @return
	 */
	private double integratePeak(final Peak1D pb, final IFileFragment iff,
	        final List<int[]> mwIndices) {
		double s = -1;
		if (integrateTICPeaks) {
			this.log.debug("Using TIC based integration!");
			final Array tic = iff.getChild(this.ticVarName).getArray();
			final Index ticIndex = tic.getIndex();
			for (int i = pb.getStartIndex(); i <= pb.getStopIndex(); i++) {
				s += (tic.getDouble(ticIndex.set(i)));
			}
			pb.setArea(s);
			pb.setIntensity(tic.getDouble(ticIndex.set(pb.getApexIndex())));
		} else {
			try {
				final Tuple2D<List<Array>, List<Array>> t = MaltcmsTools
				        .getMZIs(iff);
				final List<Array> masses = t.getFirst().subList(
				        pb.getStartIndex(), pb.getStopIndex() + 1);
				final List<Array> intens = t.getSecond().subList(
				        pb.getStartIndex(), pb.getStopIndex() + 1);
				// EvalTools.eqI(mwIndices.size(), masses.size(), this);
				// EvalTools.eqI(mwIndices.size(), intens.size(), this);
				s = 0;
				Array a = null;
				Index aidx = null;
				// for all
				for (int i = 0; i < mwIndices.size(); i++) {
					for (int j = 0; j < mwIndices.get(i).length; j++) {
						final int idx = mwIndices.get(i)[j];
						a = intens.get(i);
						aidx = a.getIndex();
						s += (intens.get(i).getDouble(aidx.set(idx)));
					}
				}
				pb.setArea(s);
			} catch (final ResourceNotAvailableException ex) {
				this.log
				        .info("Could not retrieve MS data, falling back to TIC based integration!");
				final Array tic = iff.getChild(this.ticVarName).getArray();
				final Index ticIndex = tic.getIndex();
				for (int i = pb.getStartIndex(); i <= pb.getStopIndex(); i++) {
					s += (tic.getDouble(ticIndex.set(i)));
				}
				pb.setArea(s);
				pb.setIntensity(tic.getDouble(ticIndex.set(pb.getApexIndex())));
			}
		}
		this.log.debug("Raw peak area: {}", s);
		return s;
	}

	private boolean isAboveThreshold(final double snrdb, final double threshold) {
		return (snrdb > threshold);
	}

	private boolean isCandidate(final int index, final double[] values,
	        final int window) {
		final double max = MathTools
		        .max(values, index - window, index + window);
		final double indxVal = values[index];
		if (max == indxVal) {
			return true;
		}
		return false;
	}

	private void checkExtremum(final double[] values, final double[] snr,
	        final ArrayList<Integer> ts, final double threshold, final int i,
	        final int window) {
		EvalTools.notNull(new Object[] { values, i, threshold }, this);
		if ((values[i] > 0) && isAboveThreshold(snr[i], threshold)
		        && isCandidate(i, values, window)) {
			ts.add(i);
			this.log
			        .debug(
			                "Found extremum above snr threshold {} with value {} at scan: {}",
			                new Object[] { threshold, values[i], i });
		}
	}

	private boolean isMinimum(final double prev, final double current,
	        final double next) {
		if ((current < prev) && (current < next)) {
			return true;
		}
		if (current == 0) {
			return true;
		}
		return false;
	}

	/**
	 * @param columnMap
	 * @param ll
	 */
	private void savePeakTable(final List<Peak1D> l, final IFileFragment iff) {
		final List<List<String>> rows = new ArrayList<List<String>>(l.size());
		List<String> headers = null;
		final String[] headerLine = new String[] { "APEX", "START", "STOP",
		        "RT_APEX", "RT_START", "RT_STOP", "AREA", "MW", "INTENSITY" };
		headers = Arrays.asList(headerLine);
		this.log.debug("Adding row {}", headers);
		rows.add(headers);
		for (final Peak1D pb : l) {
			final DecimalFormat df = (DecimalFormat) NumberFormat
			        .getInstance(Locale.US);
			df.applyPattern("0.0000");
			this.log.debug("Adding {} peaks", l.size());
			final String[] line = new String[] { pb.getApexIndex() + "",
			        pb.getStartIndex() + "", pb.getStopIndex() + "",
			        df.format(pb.getApexTime()), df.format(pb.getStartTime()),
			        df.format(pb.getStopTime()), pb.getArea() + "",
			        "" + pb.getMw(), "" + pb.getIntensity() };
			final List<String> v = Arrays.asList(line);
			rows.add(v);
			this.log.debug("Adding row {}", v);
		}

		final CSVWriter csvw = new CSVWriter();
		csvw.setIWorkflow(getIWorkflow());
		csvw.writeTableByRows(getIWorkflow().getOutputDirectory(this)
		        .getAbsolutePath(), StringTools.removeFileExt(iff.getName())
		        + "_peakAreas.csv", rows, WorkflowSlot.ALIGNMENT);

		savePeakAnnotations(l, iff);
	}

	public void savePeakAnnotations(final List<Peak1D> l,
	        final IFileFragment iff) {
		MaltcmsAnnotationFactory maf = new MaltcmsAnnotationFactory();
		File matFile = new File(getIWorkflow().getOutputDirectory(this),
		        StringTools.removeFileExt(iff.getName())
		                + ".maltcmsAnnotation.xml");
		MaltcmsAnnotation ma = maf.createNewMaltcmsAnnotationType(new File(iff
		        .getAbsolutePath()).toURI());
		for (Peak1D p : l) {
			maf.addPeakAnnotation(ma, this.getClass().getName(), p);
		}
		maf.save(ma, matFile);
		DefaultWorkflowResult dwr = new DefaultWorkflowResult(matFile, this,
		        WorkflowSlot.PEAKFINDING, iff);
		getIWorkflow().append(dwr);
	}

	public void visualize(final IFileFragment f, final Array intensities,
	        final double[] snr, final ArrayInt.D1 peaks,
	        final double peakThreshold) {
		Array domain = null;
		String x_label = "scan number";
		try {
			f.getChild(this.satVarName);
			domain = f.getChild(this.satVarName).getArray();
			x_label = "time [s]";
		} catch (ResourceNotAvailableException re) {
			domain = f.getChild("scan_index").getArray();
		}
		final ArrayDouble.D1 posx = new ArrayDouble.D1(peaks.getShape()[0]);
		final ArrayDouble.D1 posy = new ArrayDouble.D1(peaks.getShape()[0]);
		final Array threshold = Array.factory(snr);
		// new ArrayDouble.D1(intensities
		// .getShape()[0]);
		// ArrayTools.fill(threshold, peakThreshold);
		final Index satIdx = domain.getIndex();
		final Index intensIdx = intensities.getIndex();
		for (int i = 0; i < peaks.getShape()[0]; i++) {
			posx.set(i, domain.getDouble(satIdx.set(peaks.get(i))));
			posy.set(i, intensities.getInt(intensIdx.set(peaks.get(i))));
		}
		final AChart<XYPlot> tc1 = new XYChart("SNR plot",
		        new String[] { "Signal-to-noise ratio" },
		        new Array[] { threshold }, new Array[] { domain }, posx, posy,
		        new String[] {}, x_label, "snr (db)");
		final AChart<XYPlot> tc2 = new XYChart("TICPeakFinder results for "
		        + f.getName(), new String[] { "Total Ion Count (TIC)" },
		        new Array[] { intensities }, new Array[] { domain }, posx,
		        posy, new String[] {}, x_label, "counts");
		// final AChart<XYPlot> tc3 = new XYChart("Peak candidates",
		// new String[] { "Peak candidates" }, new Array[] { peaks },
		// new Array[] { domain }, x_label, "peak");
		// final AChart<XYPlot> tc4 = new
		// XYChart("Value of median within window",
		// new String[] { "Value of median within window" },
		// new Array[] { deviation }, new Array[] { domain }, x_label,
		// "counts");
		final ArrayList<XYPlot> al = new ArrayList<XYPlot>();
		al.add(tc1.create());
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
		                .getOutputDirectory(this));
		pr.configure(Factory.getInstance().getConfiguration());
		Factory.getInstance().submitJob(pr);
	}
}
