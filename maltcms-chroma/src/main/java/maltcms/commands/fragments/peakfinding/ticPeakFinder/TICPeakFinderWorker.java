/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2013, The authors of Maltcms. All rights reserved.
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
package maltcms.commands.fragments.peakfinding.ticPeakFinder;

import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.experimental.Builder;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.BatchFilter;
import maltcms.commands.filters.array.FirstDerivativeFilter;
import maltcms.commands.fragments.peakfinding.TICPeakFinder;
import maltcms.datastructures.caches.RingBuffer;
import maltcms.datastructures.peak.MaltcmsAnnotationFactory;
import maltcms.datastructures.peak.Peak1D;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import maltcms.io.csv.CSVWriter;
import maltcms.io.xml.bindings.annotation.MaltcmsAnnotation;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.CombinedDomainXYChart;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYChart;
import net.sf.mpaxs.api.ICompletionService;
import net.sf.mpaxs.spi.concurrent.CompletionServiceFactory;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;

/**
 *
 * @author Nils Hoffmann
 */
@Builder
@Data
@Slf4j
public class TICPeakFinderWorker implements Callable<TICPeakFinderWorkerResult>, Serializable {

    private final File outputDirectory;
    private final URI inputUri;
    private final boolean integratePeaks;
    private final boolean integrateTICPeaks;
    private final boolean integrateRawTic;
    private final boolean saveGraphics;
    private final boolean removeOverlappingPeaks;
    private final boolean subtractBaseline;

    private final double peakThreshold;
    private final int peakSeparationWindow;
    private final int snrWindow;

    private final IBaselineEstimator baselineEstimator;
    private final List<AArrayFilter> filter;
    private final List<IPeakNormalizer> peakNormalizers;

    //variables
    private final String ticVarName;
    private final String satVarName;
    private final String ticPeakVarName;
    private final String ticFilteredVarName;

    private final Properties properties;

    @Override
    public TICPeakFinderWorkerResult call() {
        return findPeaks(new FileFragment(inputUri));
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

    private void addResults(final IFileFragment ff, final PeakPositionsResultSet pprs,
            final List<Peak1D> peaklist) {

        List<Peak1D> peaks;
        if (peaklist.isEmpty()) {
            peaks = new ArrayList<>(pprs.getTs().size());
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

    public List<Peak1D> findPeakAreas(final IFileFragment chromatogram,
            final List<Integer> ts, String filename, final Array sat, final Array rawTIC,
            final Array filteredTIC, final double[] snr, final PolynomialSplineFunction baseline) {
        final ArrayList<Peak1D> pbs = new ArrayList<>();
        Array scanAcquisitionTime = chromatogram.getChild(satVarName).getArray();
        if (integrateTICPeaks) {
            log.info("Using TIC based peak integration");
            FirstDerivativeFilter fdf = new FirstDerivativeFilter();
            Array fdTIC = fdf.apply(filteredTIC);
            Array sdTIC = fdf.apply(fdTIC);
            Array tdTIC = fdf.apply(sdTIC);
            XYChart xyc = new XYChart(filename + "-TIC", new String[]{
                "TIC", "FIRST DERIVATIVE", "SECOND DERIVATIVE",
                "THIRD DERIVATIVE"}, new Array[]{filteredTIC,
                fdTIC, sdTIC, tdTIC}, "scan", "value");
            xyc.configure(ConfigurationConverter.getConfiguration(properties));
            final PlotRunner pr = new PlotRunner(xyc.create(),
                    "TIC and TIC derivatives " + filename,
                    "tic-derivatives-"
                    + StringTools.removeFileExt(filename),
                    outputDirectory);
            pr.configure(ConfigurationConverter.getConfiguration(properties));
            CompletionServiceFactory<JFreeChart> csf = new CompletionServiceFactory<>();
            ICompletionService<JFreeChart> ics = csf.newLocalCompletionService();
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
                final Peak1D pb = getPeakBoundsByTIC(chromatogram, scanApex, sat,
                        rawTIC,
                        filteredTIC, fdTIC, sdTIC, tdTIC, baseline);
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
            List<Peak1D> overlaps = new ArrayList<>();
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
            if (removeOverlappingPeaks && overlaps.size() > 0) {
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
     * @param sat
     * @return
     */
    public PeakPositionsResultSet findPeakPositions(Array tic, Array sat) {
        EvalTools.notNull(tic, this);
        Array filteredTic = null;
        final ArrayList<Integer> ts = new ArrayList<>();
        log.debug("Value\tLow\tMedian\tHigh\tDev\tGTMedian\tSNR");
        double[] ticValues = (double[]) tic.get1DJavaArray(double.class);
        filteredTic = applyFilters(tic.copy());
        double[] snrValues = new double[ticValues.length];
        double[] satValues = (double[]) sat.get1DJavaArray(double.class);
        double[] cticValues = (double[]) filteredTic.get1DJavaArray(
                double.class);
        PolynomialSplineFunction baselineEstimatorFunction = baselineEstimator.findBaseline(satValues, cticValues);
        for (int i = 0; i < snrValues.length; i++) {
            double snr = Double.NEGATIVE_INFINITY;
            try {
                double baselineValue = baselineEstimatorFunction.value(sat.getDouble(i));
                double ratio = cticValues[i] / baselineValue;
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
        log.debug("Corrected tic value: {}", filteredTic);
        PeakPositionsResultSet pprs = new PeakPositionsResultSet(filteredTic,
                createPeakCandidatesArray(tic, ts), snrValues, ts, baselineEstimatorFunction);
        return pprs;
    }

    private TICPeakFinderWorkerResult findPeaks(final IFileFragment f) {
        final Array tic = f.getChild(this.ticVarName).getArray();
        final Array sat = f.getChild(this.satVarName).getArray();
        final PeakPositionsResultSet pprs = findPeakPositions(tic, sat);
        final ArrayInt.D1 extr = pprs.getPeakPositions();
        final double[] snrValues = pprs.getSnrValues();
        log.info("Found {} peaks for file {}", pprs.getTs().size(), f.getName());
        List<Peak1D> peaks = Collections.emptyList();
        if (this.integratePeaks) {
            peaks = findPeakAreas(f, pprs.getTs(), f.getName(), sat, tic, pprs.getCorrectedTIC(), snrValues, pprs.getBaselineEstimator());
        }
        List<WorkflowResult> workflowResults = new ArrayList<>();
        if (this.saveGraphics) {
            workflowResults.addAll(visualize(f, sat, tic, pprs.getCorrectedTIC(), snrValues, extr,
                    this.peakThreshold, pprs.getBaselineEstimator()));
//            workflowResults.addAll(visualize(f, sat, tic, pprs.getCorrectedTIC(), snrValues, extr,
//                    this.peakThreshold, pprs.getBaselineEstimator()));
        }
        final String filename = f.getName();
        final IFileFragment ff = new FileFragment(
                new File(outputDirectory,
                        filename));

        ff.addSourceFile(f);
        if (this.integratePeaks) {
            log.info("Using peak normalizers: {}", peakNormalizers);
        }
        addResults(ff, pprs, peaks);
        if (this.integratePeaks) {
            workflowResults.addAll(savePeakTable(peaks, f));
        }
        ff.save();
        f.clearArrays();
        return new TICPeakFinderWorkerResult(ff.getUri(), workflowResults);
    }

    /**
     * // * @param f //
     */
    private void findEICPeaks(final IFileFragment f) {
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
    }

    /**
     * @param correctedtic
     * @return
     */
    private Array applyFilters(final Array correctedtic) {
        final Array filteredtic = BatchFilter.applyFilters(correctedtic,
                this.filter);
        return filteredtic;
    }

    private Peak1D getPeakBoundsByTIC(final IFileFragment chromatogram,
            final int scanIndex, final Array sat, final Array rawTIC,
            final Array baselineCorrectedTIC,
            final Array fdTIC, final Array sdTIC, final Array tdTIC, final PolynomialSplineFunction baseline) {

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
        RingBuffer<Double> rb = new RingBuffer<>(3);
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
        RingBuffer<Double> rb2 = new RingBuffer<>(3);
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
        pb.setFile(chromatogram.getUri().toString());
        if (integrateRawTic) {
            integratePeak(pb, null, rawTIC);
        } else {
            Array baselineSubtractedTic = baselineCorrectedTIC.copy();
            if (subtractBaseline) {
                Index satIndex = sat.getIndex();
                Index baselineSubtractedTicIndex = baselineSubtractedTic.getIndex();
                for (int i = 0; i < baselineSubtractedTic.getShape()[0]; i++, baselineSubtractedTicIndex.incr(), satIndex.incr()) {
                    double baselineValue;
                    try {
                        baselineValue = baseline.value(sat.getDouble(satIndex));
                        baselineSubtractedTic.setDouble(baselineSubtractedTicIndex, Math.max(0, baselineCorrectedTIC.getDouble(i) - baselineValue));
                    } catch (ArgumentOutsideDomainException ex) {
                        Logger.getLogger(TICPeakFinderWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            integratePeak(pb, null, baselineSubtractedTic);
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
        final SortedMap<Integer, List<Integer>> al = new TreeMap<>();
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
        final List<int[]> mwIndices = new ArrayList<>();// int[al.size()][];
        for (final Integer key : al.keySet()) {
            final int[] arr = new int[al.get(key).size()];
            int i = 0;
            for (final Integer itg : al.get(key)) {
                arr[i++] = itg;
            }
            mwIndices.add(arr);
        }
        log.debug("start: {}, stop: {}", (l + 1), r - 1);
        final Peak1D pb = new Peak1D(l + 1, apexIndex, r - 1);//PeakFactory.createPeak1DTic();
        pb.setFile(f.getUri().toString());
        integratePeak(pb, mwIndices, f.getChild(this.ticVarName).getArray());
        return pb;
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
    private Collection<WorkflowResult> savePeakTable(final List<Peak1D> l, final IFileFragment iff) {
        final List<List<String>> rows = new ArrayList<>(l.size());
        List<String> headers = null;
        final String[] headerLine = new String[]{"APEX", "START", "STOP",
            "RT_APEX", "RT_START", "RT_STOP", "AREA", "AREA_NORMALIZED", "AREA_NORMALIZED_PERCENT", "NORMALIZATION_METHODS", "MW", "INTENSITY", "SNR"};
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
                (pb.getNormalizationMethods().length == 0) ? "" : pb.getNormalizedArea() * 100.0 + "",
                Arrays.toString(pb.getNormalizationMethods()),
                "" + pb.getMw(), "" + pb.getApexIntensity(), "" + pb.getSnr()};
            final List<String> v = Arrays.asList(line);
            rows.add(v);
            log.debug("Adding row {}", v);
        }

        final CSVWriter csvw = new CSVWriter();
        File peakAreasFile = csvw.writeTableByRows(outputDirectory.
                getAbsolutePath(), StringTools.removeFileExt(iff.getName())
                + "_peakAreas.csv", rows, WorkflowSlot.ALIGNMENT);
        WorkflowResult peakAreas = new WorkflowResult(peakAreasFile.toURI(), TICPeakFinder.class.getCanonicalName(), WorkflowSlot.PEAKFINDING, new URI[]{iff.getUri()});
        WorkflowResult annotations = savePeakAnnotations(l, iff);
        return Arrays.asList(peakAreas, annotations);
    }

    /**
     *
     * @param l
     * @param iff
     * @return
     */
    public WorkflowResult savePeakAnnotations(final List<Peak1D> l,
            final IFileFragment iff) {
        MaltcmsAnnotationFactory maf = new MaltcmsAnnotationFactory();
        File matFile = new File(outputDirectory,
                StringTools.removeFileExt(iff.getName())
                + ".maltcmsAnnotation.xml");
        MaltcmsAnnotation ma = maf.createNewMaltcmsAnnotationType(iff.getUri());
        for (Peak1D p : l) {
            maf.addPeakAnnotation(ma, this.getClass().getName(), p);
        }
        maf.save(ma, matFile);
        WorkflowResult result = new WorkflowResult(matFile.toURI(), TICPeakFinder.class.getCanonicalName(), WorkflowSlot.PEAKFINDING, new URI[]{iff.getUri()});
        return result;
    }

    /**
     *
     * @param f
     * @param sat
     * @param intensities
     * @param filteredIntensities
     * @param snr
     * @param peaks
     * @param peakThreshold
     * @param baselineEstimator
     * @return
     */
    public Collection<WorkflowResult> visualize(final IFileFragment f, final Array sat, final Array intensities,
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
                baseline.setDouble(i, baselineEstimator.value(sat.getDouble(i)));
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
        tc1.setSeriesColors(new Color[]{Color.RED, Color.BLUE});
        final AChart<XYPlot> tc2 = new XYChart("TICPeakFinder results for "
                + f.getName(), new String[]{"Total Ion Count (TIC)", "Filtered TIC",
                    "Estimated Baseline"},
                new Array[]{intensities, filteredIntensities, baseline}, new Array[]{
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
        final ArrayList<XYPlot> al = new ArrayList<>();
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
        final PlotRunner pr = new PlotRunner(cdt.create(),
                "TIC and Peak information for " + f.getName(),
                "combinedTICandPeakChart-" + StringTools.removeFileExt(f.getName()) + ".png", outputDirectory);
        pr.configure(ConfigurationConverter.getConfiguration(properties));
        ICompletionService<JFreeChart> ics = new CompletionServiceFactory<JFreeChart>().newLocalCompletionService();
        ics.submit(pr);
        Collection<WorkflowResult> results = new ArrayList<>();
        try {
            ics.call();
            results.add(new WorkflowResult(pr.getFile().toURI(), TICPeakFinder.class.getCanonicalName(), WorkflowSlot.VISUALIZATION, new URI[]{f.getUri()}));
        } catch (Exception ex) {
            log.warn("{}", ex);
        }
        return results;
    }

}
