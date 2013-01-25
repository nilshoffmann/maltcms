/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.commands.fragments.peakfinding;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.FirstDerivativeFilter;
import maltcms.datastructures.peak.MaltcmsAnnotationFactory;
import maltcms.datastructures.peak.Peak1D;
import maltcms.datastructures.caches.RingBuffer;
import maltcms.io.csv.CSVWriter;
import maltcms.io.xml.bindings.annotation.MaltcmsAnnotation;
import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.CombinedDomainXYChart;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYChart;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowProgressResult;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.datastructures.tools.EvalTools;
import cross.tools.StringTools;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.*;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.IBaselineEstimator;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.LoessMinimaBaselineEstimator;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.PeakFinderUtils;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.PeakPositionsResultSet;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import maltcms.tools.ArrayTools;
import net.sf.mpaxs.api.ICompletionService;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.jfree.chart.JFreeChart;
import org.openide.util.lookup.ServiceProvider;

/**
 * Find Peaks based on TIC, estimates a local baseline and, based on a given
 * signal-to-noise ratio, decides whether a maximum is a peak candidate or not.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@RequiresVariables(names = {"var.total_intensity"})
@RequiresOptionalVariables(names = {"var.scan_acquisition_time"})
@ProvidesVariables(names = {"var.tic_peaks", "var.tic_filtered", "andichrom.var.peak_name",
    "andichrom.dimension.peak_number", "andichrom.var.peak_retention_time", "andichrom.var.peak_start_time",
    "andichrom.var.peak_end_time", "andichrom.var.peak_area", "andichrom.var.baseline_start_time",
    "andichrom.var.baseline_stop_time", "andichrom.var.baseline_start_value", "andichrom.var.baseline_stop_value"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class TICPeakFinder extends AFragmentCommand {

    @Configurable
    private double peakThreshold = 0.01d;
    @Configurable
    private boolean saveGraphics = false;
    @Configurable
    private boolean integratePeaks = false;
    @Configurable
    private boolean integrateTICPeaks = true;
    @Configurable
    private int snrWindow = 50;
    @Configurable(name = "var.total_intensity")
    private String ticVarName = "total_intensity";
    @Configurable(name = "var.scan_acquisition_time")
    private String satVarName = "scan_acquisition_time";
    @Configurable
    private String ticPeakVarName = "tic_peaks";
    @Configurable
    private String ticFilteredVarName = "tic_filtered";
    @Configurable
    private boolean integrateRawTic = true;
    @Configurable
    private int peakSeparationWindow = 10;
    @Configurable
    private boolean removeOverlappingPeaks = true;
    @Configurable
    private IBaselineEstimator baselineEstimator = new LoessMinimaBaselineEstimator();
    @Configurable
    private List<AArrayFilter> filter = Arrays.asList(
            (AArrayFilter) new MultiplicationFilter());
    @Configurable
    private List<IPeakNormalizer> peakNormalizers = Collections.emptyList();

    @Override
    public String toString() {
        return getClass().getName();
    }

    private void addResults(final IFileFragment ff, final PeakPositionsResultSet pprs,
            final List<Peak1D> peaklist) {

        List<Peak1D> peaks;
        if (peaklist.isEmpty()) {
            peaks = new ArrayList<Peak1D>(pprs.getTs().size());
            for (Integer idx : pprs.getTs()) {
                Peak1D pk = new Peak1D();
                pk.setSnr(pprs.getSnrValues()[idx]);
                pk.setApexIndex(idx);
                pk.setFile(ff.getName());
                peaks.add(pk);
            }
        } else {
            peaks = peaklist;
        }
        Peak1D.append(ff, peakNormalizers, peaks, pprs.getCorrectedTIC(), ticPeakVarName, ticFilteredVarName);
    }

    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        EvalTools.notNull(t, this);
        final ArrayList<IFileFragment> peaks = new ArrayList<IFileFragment>();
        log.info("Searching for peaks");
        // create new ProgressResult
        final DefaultWorkflowProgressResult dwpr = new DefaultWorkflowProgressResult(
                t.getSize(), this, getWorkflowSlot());
        for (final IFileFragment f : t) {
            peaks.add(findPeaks(f));
            // notify workflow
            getWorkflow().append(dwpr.nextStep());
        }
        return new TupleND<IFileFragment>(peaks);
    }

    @Override
    public void configure(final Configuration cfg) {
        log.debug("Configure called on TICPeakFinder");
        this.ticPeakVarName = cfg.getString("var.tic_peaks", "tic_peaks");
        this.ticVarName = cfg.getString("var.total_intensity",
                "total_intensity");
        this.satVarName = cfg.getString("var.scan_acquisition_time",
                "scan_acquisition_time");
        this.ticPeakVarName = cfg.getString("var.tic_peaks", "tic_peaks");
        this.ticFilteredVarName = cfg.getString("var.tic_filtered",
                "tic_filtered");
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

    public List<Peak1D> findPeakAreas(final IFileFragment chromatogram,
            final List<Integer> ts, String filename, final Array rawTIC,
            final Array baselineCorrectedTIC, final double[] snr) {
        final ArrayList<Peak1D> pbs = new ArrayList<Peak1D>();
        Array scanAcquisitionTime = chromatogram.getChild(satVarName).getArray();
        if (integrateTICPeaks) {
            log.info("Using TIC based peak integration");
            FirstDerivativeFilter fdf = new FirstDerivativeFilter();
            Array fdTIC = fdf.apply(baselineCorrectedTIC);
            Array sdTIC = fdf.apply(fdTIC);
            Array tdTIC = fdf.apply(sdTIC);
            XYChart xyc = new XYChart(filename + "-TIC", new String[]{
                        "TIC", "FIRST DERIVATIVE", "SECOND DERIVATIVE",
                        "THIRD DERIVATIVE"}, new Array[]{baselineCorrectedTIC,
                        fdTIC, sdTIC, tdTIC}, "scan", "value");
            xyc.configure(Factory.getInstance().getConfiguration());
            final PlotRunner pr = new PlotRunner(xyc.create(),
                    "TIC and TIC derivatives " + filename,
                    "tic-derivatives-"
                    + StringTools.removeFileExt(filename),
                    getWorkflow().getOutputDirectory(this));
            pr.configure(Factory.getInstance().getConfiguration());
            ICompletionService<JFreeChart> ics = createCompletionService(JFreeChart.class);
            ics.submit(pr);
            try {
                ics.call();
            } catch (Exception ex) {
                log.error("Caught exception while executing workers: ", ex);
                throw new RuntimeException(ex);
            }
            // fall back to TIC
            for (final Integer scanApex : ts) {
                log.debug("Adding peak at scan index {}", scanApex);
                final Peak1D pb = getPeakBoundsByTIC(chromatogram, scanApex,
                        rawTIC,
                        baselineCorrectedTIC, fdTIC, sdTIC, tdTIC);
                if (pb != null && pb.getArea() > 0) {
                    pb.setSnr(snr[pb.getApexIndex()]);
                    pb.setApexTime(scanAcquisitionTime.getDouble(pb.getApexIndex()));
                    pb.setStartTime(scanAcquisitionTime.getDouble(pb.getStartIndex()));
                    pb.setStopTime(scanAcquisitionTime.getDouble(pb.getStopIndex()));
                    pbs.add(pb);
                }
            }
            Rectangle2D.Double l1 = null;
            Peak1D prev = null;
            log.info(
                    "Checking peak areas for overlapping or completely contained peaks!");
            List<Peak1D> overlaps = new ArrayList<Peak1D>();
            for (Peak1D peak : pbs) {
                if (l1 == null) {
                    l1 = new Rectangle2D.Double(peak.getStartIndex(), 0, peak.getStopIndex()
                            - peak.getStartIndex(), 1);
                    prev = peak;
                } else {
                    Rectangle2D.Double l2 = new Rectangle2D.Double(peak.getStartIndex(), 0, peak.getStopIndex()
                            - peak.getStartIndex(), 1);
                    if (l1.intersects(l2) || l1.contains(l2) || l2.contains(l1)) {
                        log.warn("Peak area overlap detected!");
                        overlaps.add(prev);
                        overlaps.add(peak);
                    }
                    l1 = l2;
                    prev = peak;
                }
            }
            if (overlaps.size() > 0) {
                log.info("Overlapping peaks: {}", overlaps);
            }
            if (removeOverlappingPeaks) {
                log.warn("Removal of overlapping peaks currently disabled!");
//                log.info("Removing overlapping peaks");
//                pbs.removeAll(overlaps);
            }
        }
        return pbs;
    }

    /**
     *
     * @param tic
     * @return
     */
    public PeakPositionsResultSet findPeakPositions(Array tic) {
        EvalTools.notNull(tic, this);
        Array correctedtic = null;
        final ArrayList<Integer> ts = new ArrayList<Integer>();
        log.debug("Value\tLow\tMedian\tHigh\tDev\tGTMedian\tSNR");
        double[] ticValues = (double[]) tic.get1DJavaArray(double.class);
        correctedtic = applyFilters(tic.copy());
        double[] snrValues = new double[ticValues.length];
        double[] cticValues = (double[]) correctedtic.get1DJavaArray(
                double.class);
        PolynomialSplineFunction baselineEstimatorFunction = baselineEstimator.findBaseline(cticValues);
        for (int i = 0; i < snrValues.length; i++) {
            double snr = Double.NEGATIVE_INFINITY;
            try {
                double ratio = (cticValues[i])
                        / baselineEstimatorFunction.value(i);
                snr = 20.0d * Math.log10(ratio);
            } catch (ArgumentOutsideDomainException ex) {
                Logger.getLogger(TICPeakFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
            snrValues[i] = Double.isInfinite(snr) ? 0 : snr;
        }
        log.debug("SNR: {}", Arrays.toString(snrValues));
        final double threshold = this.peakThreshold;// * maxCorrectedIntensity;
        for (int i = 0; i < ticValues.length; i++) {
            log.debug("i=" + i);
            PeakFinderUtils.checkExtremum(cticValues, snrValues, ts, threshold, i,
                    this.peakSeparationWindow);
        }
        PeakPositionsResultSet pprs = new PeakPositionsResultSet(correctedtic,
                createPeakCandidatesArray(tic, ts), snrValues, ts, baselineEstimatorFunction);
        return pprs;
    }

    private IFileFragment findPeaks(final IFileFragment f) {
        final Array tic = f.getChild(this.ticVarName).getArray();
        final PeakPositionsResultSet pprs = findPeakPositions(tic);
        final ArrayInt.D1 extr = pprs.getPeakPositions();
        final double[] snrValues = pprs.getSnrValues();
        log.info("Found {} peaks for file {}", pprs.getTs().size(), f.getName());
        List<Peak1D> peaks = Collections.emptyList();
        if (this.integratePeaks) {
            peaks = findPeakAreas(f, pprs.getTs(), f.getName(), tic, pprs.getCorrectedTIC(), snrValues);
        }
        if (this.saveGraphics) {
            visualize(f, tic, pprs.getCorrectedTIC(), snrValues, extr,
                    this.peakThreshold, pprs.getBaselineEstimator());
        }
        final String filename = f.getName();
        final IFileFragment ff = new FileFragment(
                new File(getWorkflow().getOutputDirectory(this),
                filename));

        ff.addSourceFile(f);
        if (this.integratePeaks) {
            log.info("Using peak normalizers: {}", peakNormalizers);
        }
        addResults(ff, pprs, peaks);
        if (this.integratePeaks) {
            savePeakTable(peaks, f);
        }
        ff.save();
        f.clearArrays();
        DefaultWorkflowResult dwr = new DefaultWorkflowResult(ff.getUri(), this, WorkflowSlot.PEAKFINDING, ff);
        getWorkflow().append(dwr);
        return ff;
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
    // // log.debug("Value\tLow\tMedian\tHigh\tDev\tGTMedian\tSNR");
    // double[] eicValues = (double[]) eic.get1DJavaArray(double.class);
    // correctedeic = applyFilters(eic);
    // eicValues = getMinimumBaselineEstimate((double[]) correctedeic
    // .get1DJavaArray(double.class));
    // correctedeic = Array.factory(eicValues);
    // final double maxCorrectedIntensity = MAMath
    // .getMaximum(correctedeic);
    // final double snrEstimate = this.peakThreshold
    // * maxCorrectedIntensity;
    // for (int j = 0; j < eicValues.length; j++) {
    // // log.debug("j=" + j);
    // checkExtremum(eicValues, ts, snrEstimate, j, this.filterWindow);
    // }
    // if (ts.size() > 0) {
    // log.debug("Found {} peaks for file {} at mass {} to {}",
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
    // // log.info("{}", p);
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
    // csvw.setWorkflow(getWorkflow());
    // csvw.writeTableByRows(getWorkflow().getOutputDirectory(this)
    // .getAbsolutePath(), StringTools.removeFileExt(f.getName())
    // + "_eicPeaks.csv", v, WorkflowSlot.PEAKFINDING);
    // log.info("Number of peak signal groups of sizes: {}", hm);
    // log.info("Total number of peak signals: {}", totalPeakSignals);
    // FileFragment ff = new FileFragment(getWorkflow().getOutputDirectory(
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
    // getWorkflow().append(dwr);
    // }
    /**
     * @param correctedtic
     * @return
     */
    private Array applyFilters(final Array correctedtic) {
        final Array filteredtic = BatchFilter.applyFilters(correctedtic,
                this.filter);
        return filteredtic;
    }

    /**
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Finds peaks based on total ion current (TIC), using a simple extremum search within a window, combined with a signal-to-noise parameter to select peaks.";
    }

    private Peak1D getPeakBoundsByTIC(final IFileFragment chromatogram,
            final int scanIndex, final Array rawTIC,
            final Array baselineCorrectedTIC,
            final Array fdTIC, final Array sdTIC, final Array tdTIC) {

        Array fdfTIC = baselineCorrectedTIC;
        // return getPeakBoundsByTIC2(scanIndex, f, baselineCorrectedTIC);
        final Index idx = fdfTIC.getIndex();
        final int size = fdfTIC.getShape()[0];
        int startIndex = -1;
        int stopIndex = -1;
        final int apexIndex = scanIndex;
        int r = scanIndex + 1;
        int l = scanIndex - 1;
        // start at peak apex = scanIndex
        // order: prev, current, next
        RingBuffer<Double> rb = new RingBuffer<Double>(3);
        double oldest = fdfTIC.getDouble(idx.set(apexIndex));
        double previous = fdfTIC.getDouble(idx.set(Math.min(size - 1, r)));
        double current = fdfTIC.getDouble(idx.set(Math.min(size - 1, ++r)));
        rb.push(oldest);
        rb.push(previous);
        rb.push(current);
        while ((r < size)) {
            if (PeakFinderUtils.isMinimum(rb.oldest(), rb.previous(), rb.current())) {
                // System.out.println("Found minimum on right side");
                stopIndex = r - 2;
                break;
            }
            if (tdTIC.getDouble(r) >= 0 && sdTIC.getDouble(r) <= 0
                    && fdTIC.getDouble(r) >= 0) {
                // System.out.println("Found inflection point on right side");
                stopIndex = r - 1;
                break;
            }
            rb.push(fdfTIC.getDouble(idx.set(Math.min(size - 1, r))));
            r++;
        }
        stopIndex = Math.min(stopIndex, size - 1);

        // start at peak apex = scanIndex
        // order: prev, current, next
        RingBuffer<Double> rb2 = new RingBuffer<Double>(3);
        oldest = fdfTIC.getDouble(idx.set(apexIndex));
        previous = fdfTIC.getDouble(idx.set(Math.max(0, l)));
        current = fdfTIC.getDouble(idx.set(Math.max(0, --l)));
        rb2.push(oldest);
        rb2.push(previous);
        rb2.push(current);
        // decrease scan index
        while ((l >= 0)) {
            if (PeakFinderUtils.isMinimum(rb2.current(), rb2.previous(), rb2.oldest())) {
                // System.out.println("Found minimum on left side");
                startIndex = l + 2;
                break;
            }
            if (tdTIC.getDouble(l) < 0 && sdTIC.getDouble(l) > 0
                    && fdTIC.getDouble(l) < 0) {
                // System.out.println("Found inflection point on left side");
                startIndex = l + 1;
                break;
            }
            rb2.push(fdfTIC.getDouble(idx.set(Math.max(0, l))));
            l--;
        }
        startIndex = Math.max(0, startIndex);

        log.debug("start: {}, stop: {}", startIndex, stopIndex);
        final Peak1D pb = new Peak1D(startIndex, apexIndex, stopIndex);
        pb.setFile(chromatogram.getUri().toASCIIString());
        if (integrateRawTic) {
            integratePeak(pb, null, rawTIC);
        } else {
            integratePeak(pb, null, baselineCorrectedTIC);
        }
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
        log.debug("Checking peak {}", scanIndex);
        final Tuple2D<List<Array>, List<Array>> t = MaltcmsTools.getMZIs(f);
        Array intens = t.getSecond().get(scanIndex);
        Array masses = t.getFirst().get(scanIndex);
        final double maxMass = MaltcmsTools.getMaxMass(masses, intens);
        log.debug("Max mass: {}", maxMass);
        if (Double.isNaN(maxMass)) {
            log.warn(
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
        log.debug("Extending peak to the right");
        while ((r < t.getFirst().size())) {
            // log.info("Checking scan {}", r);
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
                    // log.info("Adding scan {}", r);
                    al.put(r, midx);

                    // } else {
                    // log
                    // .warn("Mass in window, but intensity was not maximal!");
                    // break;
                    // }
                }
                r++;
                // old
                // final int mIdx = ranks[0];
                // final double max = MAMath.getMaximum(intensa);
                // if (max == intensa.getDouble(intensa.getIndex().set(mIdx))) {
                // // log.info("Adding scan {}", r);
                // al.put(r, midx);
                // r++;
                // } else {
                // log
                // .warn("Mass in window, but intensity was not maximal!");
                // break;
                // }
            }
        }
        // capture post increment with -1
        // startIndex = r;
        int l = scanIndex - 1;
        // decrease scan index
        log.debug("Extending peak to the left");
        while ((l >= 0)) {
            masses = t.getFirst().get(l);
            intens = t.getSecond().get(l);
            // log.info("Checking scan {}", l);
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
                    // log.info("Adding scan {}", l);
                    al.put(l, midx);

                    // } else {
                    // log
                    // .info("Mass in window, but intensity was not maximal!");
                    // break;
                    // }
                }
                l--;
            }
        }
        log.debug("Found {} signals for peak: {}", al.size(), al);
        final List<int[]> mwIndices = new ArrayList<int[]>();// int[al.size()][];
        for (final Integer key : al.keySet()) {
            final int[] arr = new int[al.get(key).size()];
            int i = 0;
            for (final Integer itg : al.get(key)) {
                arr[i++] = itg.intValue();
            }
            mwIndices.add(arr);
        }
        log.debug("start: {}, stop: {}", (l + 1), r - 1);
        final Peak1D pb = new Peak1D(l + 1, apexIndex, r - 1);//PeakFactory.createPeak1DTic();
        pb.setFile(f.getUri().toASCIIString());
        integratePeak(pb, mwIndices, f.getChild(this.ticVarName).getArray());
        return pb;
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
        return WorkflowSlot.PEAKFINDING;
    }

    /**
     * Performs peak area integration on the mass indices given by mwIndices,
     * within the area defined by Peak1D, tracking the unique mass with a
     * maximum error of epsilon.
     *
     * @param pb
     * @param mwIndices
     * @param tic
     * @return
     */
    private double integratePeak(final Peak1D pb,
            final List<int[]> mwIndices, final Array tic) {
        double s = -1;
        if (integrateTICPeaks) {
            log.debug("Using TIC based integration!");
//            final Array tic = iff.getChild(this.ticVarName).getArray();
            final Index ticIndex = tic.getIndex();
            for (int i = pb.getStartIndex(); i <= pb.getStopIndex(); i++) {
                s += (tic.getDouble(ticIndex.set(i)));
            }
            pb.setArea(s);
            pb.setApexIntensity(tic.getDouble(ticIndex.set(pb.getApexIndex())));
        } else {
            log.warn("EIC based integration not implemented yet!");
        }
        log.debug("Raw peak area: {}", s);
        return s;
    }

    /**
     * @param columnMap
     * @param ll
     */
    private void savePeakTable(final List<Peak1D> l, final IFileFragment iff) {
        final List<List<String>> rows = new ArrayList<List<String>>(l.size());
        List<String> headers = null;
        final String[] headerLine = new String[]{"APEX", "START", "STOP",
            "RT_APEX", "RT_START", "RT_STOP", "AREA", "AREA_NORMALIZED", "NORMALIZATION_METHODS", "MW", "INTENSITY", "SNR"};
        headers = Arrays.asList(headerLine);
        log.debug("Adding row {}", headers);
        rows.add(headers);
        for (final Peak1D pb : l) {
            final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                    Locale.US);
            df.applyPattern("0.0000");
            log.debug("Adding {} peaks", l.size());
            final String[] line = new String[]{pb.getApexIndex() + "",
                pb.getStartIndex() + "", pb.getStopIndex() + "",
                df.format(pb.getApexTime()), df.format(pb.getStartTime()),
                df.format(pb.getStopTime()), pb.getArea() + "", pb.getNormalizedArea() + "",
                Arrays.toString(pb.getNormalizationMethods()),
                "" + pb.getMw(), "" + pb.getApexIntensity(), "" + pb.getSnr()};
            final List<String> v = Arrays.asList(line);
            rows.add(v);
            log.debug("Adding row {}", v);
        }

        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(getWorkflow());
        csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
                getAbsolutePath(), StringTools.removeFileExt(iff.getName())
                + "_peakAreas.csv", rows, WorkflowSlot.ALIGNMENT);

        savePeakAnnotations(l, iff);
    }

    /**
     *
     * @param l
     * @param iff
     */
    public void savePeakAnnotations(final List<Peak1D> l,
            final IFileFragment iff) {
        MaltcmsAnnotationFactory maf = new MaltcmsAnnotationFactory();
        File matFile = new File(getWorkflow().getOutputDirectory(this),
                StringTools.removeFileExt(iff.getName())
                + ".maltcmsAnnotation.xml");
        MaltcmsAnnotation ma = maf.createNewMaltcmsAnnotationType(iff.getUri());
        for (Peak1D p : l) {
            maf.addPeakAnnotation(ma, this.getClass().getName(), p);
        }
        maf.save(ma, matFile);
        DefaultWorkflowResult dwr = new DefaultWorkflowResult(matFile, this,
                WorkflowSlot.PEAKFINDING, iff);
        getWorkflow().append(dwr);
    }

    /**
     *
     * @param f
     * @param intensities
     * @param filteredIntensities
     * @param snr
     * @param peaks
     * @param peakThreshold
     * @param baselineEstimator
     */
    public void visualize(final IFileFragment f, final Array intensities,
            final Array filteredIntensities,
            final double[] snr, final ArrayInt.D1 peaks,
            final double peakThreshold, PolynomialSplineFunction baselineEstimator) {
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
        final Array snrEstimate = Array.factory(snr);
        final Array threshold = new ArrayDouble.D1(snr.length);
        final Array baseline = new ArrayDouble.D1(snr.length);
        for (int i = 0; i < snr.length; i++) {
            try {
                baseline.setDouble(i, baselineEstimator.value(i));
            } catch (ArgumentOutsideDomainException ex) {
                Logger.getLogger(TICPeakFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // new ArrayDouble.D1(intensities
        // .getShape()[0]);
        ArrayTools.fill(threshold, peakThreshold);
        final Index satIdx = domain.getIndex();
        final Index intensIdx = intensities.getIndex();
        for (int i = 0; i < peaks.getShape()[0]; i++) {
            posx.set(i, domain.getDouble(satIdx.set(peaks.get(i))));
            posy.set(i, intensities.getInt(intensIdx.set(peaks.get(i))));
        }
        final AChart<XYPlot> tc1 = new XYChart("SNR plot",
                new String[]{"Signal-to-noise ratio", "Threshold"},
                new Array[]{snrEstimate, threshold}, new Array[]{domain}, posx, posy,
                new String[]{}, x_label, "snr (db)");
        final AChart<XYPlot> tc2 = new XYChart("TICPeakFinder results for "
                + f.getName(), new String[]{"Total Ion Count (TIC)",
                    "Estimated Baseline"},
                new Array[]{intensities, baseline}, new Array[]{
                    domain}, posx,
                posy, new String[]{}, x_label, "counts");
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
                "combinedTICandPeakChart-" + f.getName(), getWorkflow().
                getOutputDirectory(this));
        pr.configure(Factory.getInstance().getConfiguration());
        ICompletionService<JFreeChart> ics = createCompletionService(JFreeChart.class);
        ics.submit(pr);
        try {
            ics.call();
        } catch (Exception ex) {
            log.warn("{}", ex);
        }
    }
}
