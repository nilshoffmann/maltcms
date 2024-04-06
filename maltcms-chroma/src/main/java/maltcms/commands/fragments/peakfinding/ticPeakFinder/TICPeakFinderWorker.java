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
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.Builder;

import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.BatchFilter;
import maltcms.commands.filters.array.FirstDerivativeFilter;
import maltcms.commands.fragments.peakfinding.TICPeakFinder;
import maltcms.commands.fragments.peakfinding.io.Peak1DUtilities;
import maltcms.datastructures.caches.RingBuffer;
import maltcms.datastructures.peak.Peak1D;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import maltcms.tools.ArrayTools;
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
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;

/**
 * <p>
 * TICPeakFinderWorker class.</p>
 *
 * @author Nils Hoffmann
 *
 * @since 1.3.2
 */
@Data
public class TICPeakFinderWorker implements Callable<PeakFinderWorkerResult>, Serializable {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TICPeakFinderWorker.class);

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

    private final Peak1DUtilities peak1DUtilities = new Peak1DUtilities();

    /**
     * {@inheritDoc}
     */
    @Override
    public PeakFinderWorkerResult call() {
        return findPeaks(new FileFragment(inputUri));
    }

    private ArrayInt.D1 createPeakCandidatesArray(final Array tic,
            final ArrayList<Integer> ts) {
        EvalTools.notNull(ts, this);
        final ArrayInt.D1 extr = new ArrayInt.D1(ts.size(), false);
        // checkUniformDistribution(tic.getShape()[0], ts);
        for (int i = 0; i < ts.size(); i++) {
            extr.set(i, ts.get(i));
        }
        return extr;
    }

    /**
     * <p>
     * findPeakAreas.</p>
     *
     * @param chromatogram a
     * {@link cross.datastructures.fragments.IFileFragment} object.
     * @param ts a {@link java.util.List} object.
     * @param filename a {@link java.lang.String} object.
     * @param sat a {@link ucar.ma2.Array} object.
     * @param rawTIC a {@link ucar.ma2.Array} object.
     * @param pprs a {@link PeakPositionsResultSet} object.
     * @param integratePeaks whether to integrate peaks.
     * @param removeOverlappingPeaks whether to remove overlapping peaks.
     * @param subtractBaseline whether the estimated baseline should be
     * subtracted from the filtered signal.
     * @return a {@link java.util.List} object.
     */
    public List<Peak1D> findPeakAreas(final IFileFragment chromatogram,
            final List<Integer> ts, String filename, final Array sat, final Array rawTIC,
            PeakPositionsResultSet pprs, boolean integratePeaks, boolean removeOverlappingPeaks, boolean subtractBaseline) {
        final ArrayList<Peak1D> pbs = new ArrayList<>();
        Array scanAcquisitionTime = sat;
        if (integratePeaks) {
            log.info("Using TIC based peak integration");
            FirstDerivativeFilter fdf = new FirstDerivativeFilter();
            Array fdTIC = fdf.apply(pprs.getCorrectedTIC());
            Array sdTIC = fdf.apply(fdTIC);
            Array tdTIC = fdf.apply(sdTIC);
            XYChart xyc = new XYChart(filename + "-TIC", new String[]{
                "TIC", "FIRST DERIVATIVE", "SECOND DERIVATIVE",
                "THIRD DERIVATIVE"}, new Array[]{pprs.getCorrectedTIC(),
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
            Array ticToIntegrate = rawTIC;
            if (subtractBaseline) {
                ticToIntegrate = subtractBaseline(pprs.getCorrectedTIC(), sat, pprs.getBaselineEstimator());
            }
            // fall back to TIC
            for (final Integer scanApex : ts) {
                log.debug("Adding peak at scan index {}", scanApex);
                final Peak1D pb = getPeakBounds(chromatogram, scanApex,
                        ticToIntegrate,
                        pprs.getCorrectedTIC(), fdTIC, sdTIC, tdTIC);
                if (pb != null && pb.getArea() > 0) {
                    pb.setSnr(pprs.getSnrValues()[pb.getApexIndex()]);
                    pb.setApexTime(scanAcquisitionTime.getDouble(pb.getApexIndex()));
                    pb.setStartTime(scanAcquisitionTime.getDouble(pb.getStartIndex()));
                    pb.setStopTime(scanAcquisitionTime.getDouble(pb.getStopIndex()));
                    pb.setBaselineStartTime(pb.getStartTime());
                    pb.setBaselineStopTime(pb.getStopTime());
                    if (pb.getStartIndex() >= 0) {
                        try {
                            pb.setBaselineStartValue(pprs.getBaselineEstimator().value(pb.getBaselineStartTime()));
                        } catch (ArgumentOutsideDomainException ex) {
                            log.warn("Argument {} out of bounds, setting baselineStartValue to NaN", pb.getStartTime());
                            pb.setBaselineStartValue(0);
                        }
                    } else {
                        pb.setBaselineStartValue(0);
                    }
                    if (pb.getStopIndex() >= 0) {
                        try{
                            pb.setBaselineStopValue(pprs.getBaselineEstimator().value(pb.getBaselineStopTime()));
                        } catch (ArgumentOutsideDomainException ex) {
                            log.warn("Argument {} out of bounds, setting baselineStopValue to NaN", pb.getStartTime());
                            pb.setBaselineStopValue(0);
                        }
                    } else {
                        pb.setBaselineStopValue(0);
                    }
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
            }
        }
        return pbs;
    }

    /**
     * <p>
     * findPeakPositions.</p>
     *
     * @param tic a {@link ucar.ma2.Array} object.
     * @param sat a {@link ucar.ma2.Array} object.
     * @param baselineEstimator the baseline estimator to use.
     * @param threshold the threshold for peak detection
     * @param peakSeparationWindow the minimum required number of scans between
     * two detected peaks.
     * @return a
     * {@link maltcms.commands.fragments.peakfinding.ticPeakFinder.PeakPositionsResultSet}
     * object.
     */
    public PeakPositionsResultSet findPeakPositions(Array tic, Array sat, IBaselineEstimator baselineEstimator, double threshold, int peakSeparationWindow) {
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
        for (int i = 0; i < ticValues.length; i++) {
            log.debug("i=" + i);
            PeakFinderUtils.checkExtremum(cticValues, snrValues, ts, threshold, i,
                    peakSeparationWindow);
        }
        log.debug("Corrected tic value: {}", filteredTic);
        PeakPositionsResultSet pprs = new PeakPositionsResultSet(filteredTic,
                createPeakCandidatesArray(tic, ts), snrValues, ts, baselineEstimatorFunction);
        return pprs;
    }

    private PeakFinderWorkerResult findPeaks(final IFileFragment f) {
        final Array tic = f.getChild(this.ticVarName).getArray();
        final Array sat = f.getChild(this.satVarName).getArray();
        final PeakPositionsResultSet pprs = findPeakPositions(tic, sat, baselineEstimator, peakThreshold, peakSeparationWindow);
        log.info("Found {} peaks for file {}", pprs.getTs().size(), f.getName());
        List<Peak1D> peaks = Collections.emptyList();
        IFileFragment outputFragment = new FileFragment(
                new File(outputDirectory,
                        f.getName()));
        outputFragment.addSourceFile(f);
        if (this.integratePeaks) {
            peaks = findPeakAreas(f, pprs.getTs(), f.getName(), sat, tic, pprs, integrateTICPeaks, removeOverlappingPeaks, subtractBaseline);
        } else {
            peaks = new ArrayList<>(pprs.getTs().size());
            for (Integer idx : pprs.getTs()) {
                peaks.add(
                    Peak1D.builder1D().
                        snr(pprs.getSnrValues()[idx]).
                        apexIndex(idx).
                        file(outputFragment.getName()).
                    build()
                );
            }
        }
        return saveResults(f, outputFragment, pprs, peaks);
    }

    private PeakFinderWorkerResult saveResults(final IFileFragment f, final IFileFragment outputFragment, final PeakPositionsResultSet pprs, List<Peak1D> peaks) {
        List<WorkflowResult> workflowResults = new LinkedList<>();
        if (this.saveGraphics) {
            final Array tic = f.getChild(this.ticVarName).getArray();
            final Array sat = f.getChild(this.satVarName).getArray();
            final double[] snrValues = pprs.getSnrValues();
            workflowResults.addAll(visualize(f, sat, tic, pprs.getCorrectedTIC(), snrValues, pprs.getPeakPositions(),
                    this.peakThreshold, pprs.getBaselineEstimator()));
        }
        getPeak1DUtilities().addTicResults(outputFragment, peaks, getPeakNormalizers(), pprs.getCorrectedTIC(), this.ticFilteredVarName);
        if (this.integratePeaks) {
            log.info("Using peak normalizers: {}", peakNormalizers);
            workflowResults.addAll(savePeakTable(peaks, f));
        }
        outputFragment.save();
        f.clearArrays();
        return new PeakFinderWorkerResult(outputFragment.getUri(), workflowResults);
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

    private Peak1D getPeakBounds(final IFileFragment chromatogram,
            final int scanIndex, final Array ticToIntegrate,
            final Array filteredTIC,
            final Array fdTIC, final Array sdTIC, final Array tdTIC) {

        Array fdfTIC = filteredTIC;
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
                // log.info("Found minimum on right side");
                stopIndex = r - 2;
                break;
            }
            if (tdTIC.getDouble(r) >= 0 && sdTIC.getDouble(r) <= 0
                    && fdTIC.getDouble(r) >= 0) {
                // log.info("Found inflection point on right side");
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
                // log.info("Found minimum on left side");
                startIndex = l + 2;
                break;
            }
            if (tdTIC.getDouble(l) < 0 && sdTIC.getDouble(l) > 0
                    && fdTIC.getDouble(l) < 0) {
                // log.info("Found inflection point on left side");
                startIndex = l + 1;
                break;
            }
            rb2.push(fdfTIC.getDouble(idx.set(Math.max(0, l))));
            l--;
        }
        startIndex = Math.max(0, startIndex);

        log.debug("start: {}, stop: {}", startIndex, stopIndex);
        final Peak1D pb = 
            Peak1D.builder1D().
                startIndex(startIndex).
                apexIndex(apexIndex).
                stopIndex(stopIndex).
                file(chromatogram.getUri().toString()).
            build();
        integratePeak(pb, ticToIntegrate);
        return pb;
    }

    private Array subtractBaseline(final Array filteredTIC, final Array sat, final PolynomialSplineFunction baseline) {
        Array baselineSubtractedTic = filteredTIC.copy();
        if (subtractBaseline) {
            Index satIndex = sat.getIndex();
            Index baselineSubtractedTicIndex = baselineSubtractedTic.getIndex();
            for (int i = 0; i < baselineSubtractedTic.getShape()[0]; i++, baselineSubtractedTicIndex.incr(), satIndex.incr()) {
                double baselineValue;
                try {
                    baselineValue = baseline.value(sat.getDouble(satIndex));
                    baselineSubtractedTic.setDouble(baselineSubtractedTicIndex, Math.max(0, filteredTIC.getDouble(i) - baselineValue));
                } catch (ArgumentOutsideDomainException ex) {
                    Logger.getLogger(TICPeakFinderWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return baselineSubtractedTic;
    }

    /**
     * Performs peak area integration on the mass indices given by mwIndices,
     * within the area defined by Peak1D, tracking the unique mass with a
     * maximum error of epsilon.
     *
     * @param pb
     * @param tic
     * @return
     */
    private double integratePeak(final Peak1D pb, final Array tic) {
        double s = -1;
        if (integrateTICPeaks) {
            log.debug("Using TIC based integration!");
            final Index ticIndex = tic.getIndex();
            for (int i = pb.getStartIndex(); i <= pb.getStopIndex(); i++) {
                s += (tic.getDouble(ticIndex.set(i)));
            }
            pb.setArea(s);
            pb.setApexIntensity(tic.getDouble(ticIndex.set(pb.getApexIndex())));
        }
        log.debug("Peak area: {}", s);
        return s;
    }

    /**
     * @param l peak list
     * @param iff the source file fragment
     */
    private Collection<WorkflowResult> savePeakTable(final List<Peak1D> l, final IFileFragment iff) {
        WorkflowResult peakAreaCSV = peak1DUtilities.saveCSVPeakAnnotations(outputDirectory, l, iff);
        WorkflowResult peakAreaXML = peak1DUtilities.saveXMLPeakAnnotations(outputDirectory, l, iff);
        return Arrays.asList(peakAreaCSV, peakAreaXML);
    }

    /**
     * <p>
     * visualize.</p>
     *
     * @param f the input file fragment.
     * @param sat a {@link ucar.ma2.Array} object.
     * @param intensities a {@link ucar.ma2.Array} object.
     * @param filteredIntensities a {@link ucar.ma2.Array} object.
     * @param snr an array of double.
     * @param peaks a {@link ucar.ma2.ArrayInt.D1} object.
     * @param peakThreshold a double.
     * @param baselineEstimator a
     * {@link org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction}
     * object.
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
        final Array snrEstimate = Array.makeFromJavaArray(snr);
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
