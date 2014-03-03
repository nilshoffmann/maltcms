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

import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.BatchFilter;
import maltcms.commands.filters.array.FirstDerivativeFilter;
import maltcms.commands.filters.array.SavitzkyGolayFilter;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.IBaselineEstimator;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.LoessMinimaBaselineEstimator;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.PeakFinderUtils;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.PeakPositionsResultSet;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.WorkflowResult;
import maltcms.datastructures.caches.RingBuffer;
import maltcms.datastructures.peak.MaltcmsAnnotationFactory;
import maltcms.datastructures.peak.Peak1D;
import maltcms.datastructures.peak.PeakType;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import maltcms.io.csv.CSVWriter;
import maltcms.io.xml.bindings.annotation.MaltcmsAnnotation;
import maltcms.tools.MaltcmsTools;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;

/**
 * Work in progress. EIC peak finder, EIC are individual ion channels.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Slf4j
@Data
@RequiresVariables(names = {
    "var.scan_acquisition_time", "var.mass_values", "var.intensity_values",
    "var.scan_index"})
@ProvidesVariables(names = {})
public class EICPeakFinder extends AFragmentCommand {

    private final String description = "Finds peaks within on mass channels.";
    private final WorkflowSlot workflowSlot = WorkflowSlot.PEAKFINDING;
    private List<AArrayFilter> filter = new ArrayList<AArrayFilter>();
    private IBaselineEstimator baselineEstimator = new LoessMinimaBaselineEstimator();
    private int peakSeparationWindow = 20;
    private boolean integrateRawTic = false;
    private List<IPeakNormalizer> peakNormalizers = Collections.emptyList();

    /**
     *
     * @param a
     * @param b
     * @param meana
     * @param meanb
     * @param variancea
     * @param varianceb
     * @return
     */
    protected static double calcEstimatedCrossCorrelation(final Array a,
        final Array b, final double meana, final double meanb,
        final double variancea, final double varianceb) {
        EvalTools.eqI(a.getShape()[0], b.getShape()[0], EICPeakFinder.class);
        double res = 0.0d;
        final int n = a.getShape()[0];
        final Index inda = a.getIndex();
        final Index indb = b.getIndex();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                res += (a.getDouble(inda.set(i)) - meana)
                    * (b.getDouble(indb.set(j)) - meanb)
                    / Math.sqrt(variancea * varianceb);
            }
        }
        final double v = res / (n - 1.0d);
        return v;
        // log.debug("R'({})= {}", lag, v);
        // return v;
    }
    @Configurable(value = "0.01d")
    private double peakThreshold = 20.0d;
    @Configurable(value = "20")
    private int filterWindow = 10;
    @Configurable
    private double massResolution = 1.0d;

    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        EvalTools.notNull(t, this);
        SavitzkyGolayFilter sgf = new SavitzkyGolayFilter(10);
        filter.add(sgf);
        List<IFileFragment> results = new ArrayList<IFileFragment>(t.size());
        for (final IFileFragment f : t) {
            log.info("Retrieving min/max mass range!");
            Tuple2D<Double, Double> mm = MaltcmsTools.getMinMaxMassRange(f);
            double minMass = mm.getFirst(), maxMass = mm.getSecond();
            int massBins = MaltcmsTools.getNumberOfIntegerMassBins(minMass, maxMass, massResolution);
            int totalScans = MaltcmsTools.getNumberOfScans(f);
            log.info("Using " + massBins + " mass bins at resolution " + massResolution + " on " + totalScans + " scans");
            int offset = 0;
//			int nscans = 100000;
//			double mass = minMass;
//			QuantileSnrPeakFinder qspf = new QuantileSnrPeakFinder();
//			qspf.setPeakSnrThreshold(peakThreshold);
//			qspf.setPeakSeparationWindow(filterWindow);
            List<Peak1D> peaks = new ArrayList<Peak1D>();
            boolean[] peakPositions = new boolean[totalScans];
            Array sat = f.getChild("scan_acquisition_time").getArray();
//			ArrayDouble.D2 a = new ArrayDouble.D2(totalScans, massBins);
//			MinMax satBounds = MAMath.getMinMax(sat);
//			QuadTree<Peak1D> qt = new QuadTree<Peak1D>(satBounds.min, minMass, satBounds.max - satBounds.min, maxMass - minMass, 10);
//			for (int j = 0; j < 2 * (totalScans / nscans) + 1; j++) {
            log.info("Loading eics from scan " + offset + " to scan " + (totalScans - 1));
            Tuple2D<Array, List<Array>> eicPairs = MaltcmsTools.getEICs(f, massResolution, offset, totalScans);
            List<Array> eics = eicPairs.getSecond();
            Array binnedMasses = eicPairs.getFirst();
            for (int i = 0; i < eics.size(); i++) {
                double massBin = binnedMasses.getDouble(i);
                try {
                    Array eicArray = eics.get(i);
                    if (eicArray != null) {
                        Array eic = applyFilters(eicArray);
                        //						eic = BatchFilter.applyFilters(eic, filters);
                        if (eic != null) {
                            PeakPositionsResultSet pprs;
                            try {
                                pprs = findPeakPositions(eic, sat.section(new int[]{offset}, new int[]{eic.getShape()[0]}));
//								for (Integer idx : pprs.getTs()) {
//									peakPositions[offset + idx] = true;
////									a.set(idx, i, a.get(idx, i) + 1);
//								}
                                List<Peak1D> p = findPeakAreas(f, pprs.getTs(), eic, pprs.getCorrectedTIC(), pprs.getSnrValues());
                                for (Peak1D peak : p) {
                                    peak.setMw(massBin);
                                    peak.setPeakType(PeakType.EIC_FILTERED);
                                    peak.setBaselineStartValue(eics.get(i).getDouble(peak.getStartIndex()) - eic.getDouble(peak.getStartIndex()));
                                    peak.setBaselineStopValue(eics.get(i).getDouble(peak.getStopIndex()) - eic.getDouble(peak.getStopIndex()));
                                    peak.setBaselineStartTime(sat.getDouble(peak.getStartIndex()));
                                    peak.setBaselineStopTime(sat.getDouble(peak.getStopIndex()));
                                    //									qt.put(new Point2D.Double(peak.getApexTime(), peak.getMw()), peak);
                                }
                                peaks.addAll(p);
                                log.info("Found " + pprs.getTs().size() + " peaks");
                            } catch (InvalidRangeException ex) {
                                Logger.getLogger(EICPeakFinder.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    } else {
                        log.info("Skipping empty eic at mass bin {}", massBin);
                    }
                } catch (org.apache.commons.math.exception.NumberIsTooSmallException nte) {
                }
            }
//			offset += nscans / 2;
//		}
            Collections.sort(peaks, new Comparator<Peak1D>() {

                @Override
                public int compare(Peak1D o1, Peak1D o2) {
                    if (o1.getApexTime() < o2.getApexTime()) {
                        return -1;
                    } else if (o1.getApexTime() > o2.getApexTime()) {
                        return 1;
                    } else {
                        if (o1.getMw() < o2.getMw()) {
                            return -1;
                        } else if (o1.getMw() > o2.getMw()) {
                            return 1;
                        }
                    }
                    return 0;
                }
            });
            IFileFragment target = createWorkFragment(f);
            Peak1D.appendEICs(target, peakNormalizers, peaks, "eic_peaks");
            target.save();
            getWorkflow().append(new DefaultWorkflowResult(target.getUri(), this, workflowSlot, f));
//			Color[] cRamp = new Color[]{Color.BLACK, Color.orange, Color.yellow,
//				Color.GRAY, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.WHITE};
//			int nsamples = 256;
//			double[] sampleTable = createSampleTable(nsamples);
//			BufferedImage crampImg = createColorRampImage(sampleTable,
//				Transparency.TRANSLUCENT, cRamp);
//			BufferedImage sourceImg = makeImage2D(a, nsamples);
//			BufferedImage destImg = applyLut(sourceImg, createLookupTable(crampImg,
//				1.0f, nsamples));
//			ImageTools.saveImage(destImg, StringTools.removeFileExt(f.getName()) + "-eics", "png", getWorkflow().getOutputDirectory(this), this, f);
//			log.info("PeakPositions: {}", Arrays.toString(peakPositions));
            savePeakTable(peaks, target);
            results.add(target);
        }

//		EvalTools.notNull(t,
//			this);
        return new TupleND<>(results);
    }

    /**
     * @param columnMap
     * @param ll
     */
    private Collection<WorkflowResult> savePeakTable(final List<Peak1D> l, final IFileFragment iff) {
        final List<List<String>> rows = new ArrayList<List<String>>(l.size());
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
        File peakAreasFile = csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
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
     */
    public WorkflowResult savePeakAnnotations(final List<Peak1D> l,
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
        WorkflowResult result = new WorkflowResult(matFile.toURI(), TICPeakFinder.class.getCanonicalName(), WorkflowSlot.PEAKFINDING, new URI[]{iff.getUri()});
        return result;
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
        pb.setFile(chromatogram.getUri().toString());
        if (integrateRawTic) {
            integratePeak(pb, null, rawTIC);
        } else {
            integratePeak(pb, null, baselineCorrectedTIC);
        }
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
        log.debug("Using TIC based integration!");
//            final Array tic = iff.getChild(this.ticVarName).getArray();
        final Index ticIndex = tic.getIndex();
        for (int i = pb.getStartIndex(); i <= pb.getStopIndex(); i++) {
            s += (tic.getDouble(ticIndex.set(i)));
        }
        pb.setArea(s);
        pb.setApexIntensity(tic.getDouble(ticIndex.set(pb.getApexIndex())));
        log.debug("Raw peak area: {}", s);
        return s;
    }

    public List<Peak1D> findPeakAreas(final IFileFragment chromatogram,
        final List<Integer> ts, final Array rawTIC,
        final Array baselineCorrectedTIC, final double[] snr) {
        final ArrayList<Peak1D> pbs = new ArrayList<Peak1D>();
        Array scanAcquisitionTime = chromatogram.getChild("scan_acquisition_time").getArray();
        log.info("Using TIC based peak integration");
        FirstDerivativeFilter fdf = new FirstDerivativeFilter();
        Array fdTIC = fdf.apply(baselineCorrectedTIC);
        Array sdTIC = fdf.apply(fdTIC);
        Array tdTIC = fdf.apply(sdTIC);
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

        return pbs;
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

    /**
     *
     * @param tic
     * @param sat
     * @return
     */
    public PeakPositionsResultSet findPeakPositions(Array tic, Array sat) {
        EvalTools.notNull(tic, this);
        Array correctedtic = null;
        final ArrayList<Integer> ts = new ArrayList<Integer>();
        log.debug("Value\tLow\tMedian\tHigh\tDev\tGTMedian\tSNR");

        double[] ticValues = (double[]) tic.get1DJavaArray(double.class
        );
        correctedtic = applyFilters(tic.copy());
        double[] snrValues = new double[ticValues.length];
        double[] satValues = (double[]) sat.get1DJavaArray(double.class);
        double[] cticValues = (double[]) correctedtic.get1DJavaArray(
            double.class);
        PolynomialSplineFunction baselineEstimatorFunction = baselineEstimator.findBaseline(satValues, cticValues);
        for (int i = 0;
            i < snrValues.length;
            i++) {
            double snr = Double.NEGATIVE_INFINITY;
            try {
                double ratio = (cticValues[i])
                    / baselineEstimatorFunction.value(sat.getDouble(i));
                snr = 20.0d * Math.log10(ratio);
            } catch (ArgumentOutsideDomainException ex) {
                Logger.getLogger(TICPeakFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
            snrValues[i] = Double.isInfinite(snr) ? 0 : snr;
        }

        log.debug(
            "SNR: {}", Arrays.toString(snrValues));
        final double threshold = this.peakThreshold;// * maxCorrectedIntensity;
        for (int i = 0;
            i < ticValues.length;
            i++) {
            log.debug("i=" + i);
            PeakFinderUtils.checkExtremum(cticValues, snrValues, ts, threshold, i,
                this.peakSeparationWindow);
        }
        PeakPositionsResultSet pprs = new PeakPositionsResultSet(correctedtic,
            createPeakCandidatesArray(tic, ts), snrValues, ts, baselineEstimatorFunction);
        return pprs;
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

//	private void calcCrossCorr(
//		final Executor e,
//		final Collection<Callable<Tuple2D<Tuple2D<Integer, Integer>, Double>>> solvers,
//		final ArrayDouble.D2 a2d) throws InterruptedException,
//		ExecutionException {
//		final CompletionService<Tuple2D<Tuple2D<Integer, Integer>, Double>> ecs = new ExecutorCompletionService<Tuple2D<Tuple2D<Integer, Integer>, Double>>(
//			e);
//		for (final Callable<Tuple2D<Tuple2D<Integer, Integer>, Double>> s : solvers) {
//			ecs.submit(s);
//		}
//		final int n = solvers.size();
//		for (int i = 0; i < n; i++) {
//			final Tuple2D<Tuple2D<Integer, Integer>, Double> r = ecs.take().get();
//			if (r != null) {
//				final Integer i1 = r.getFirst().getFirst();
//				final Integer i2 = r.getFirst().getSecond();
//				a2d.set(i1, i2, r.getSecond());
//				a2d.set(i2, i1, r.getSecond());
//			}
//		}
//
//	}
    /**
     *
     * @param a
     * @param b
     * @param mean
     * @param variance
     * @param lag
     * @param acr
     */
    protected void calcEstimatedAutoCorrelation(final Array a, final Array b,
        final double mean, final double variance, final int lag,
        final ArrayDouble.D1 acr) {
        EvalTools.eqI(a.getRank(), 1, this);
        final int n = a.getShape()[0];
        final int d = n - lag;
        final double norm = (d) * variance;
        // log.info("Norm={}",1.0d/norm);
        // log.info("d={}",d);
        double res = 0.0d;
        final Index ind = a.getIndex();
        final Index indb = b.getIndex();
        for (int i = 0; i < d; i++) {
            res += (a.getDouble(ind.set(i)) - mean)
                * (b.getDouble(indb.set(i + lag)) - mean);
        }
        final double v = res / norm;
        acr.set(lag, v);
        log.debug("R'({})= {}", lag, v);
        // return v;
    }

//	private Callable<Tuple2D<Tuple2D<Integer, Integer>, Double>> createCallable(
//		final int i, final int j, final ArrayDouble.D1 ad1,
//		final ArrayDouble.D1 ad2, final double meana, final double meanb,
//		final double vara, final double varb) {
//		final Callable<Tuple2D<Tuple2D<Integer, Integer>, Double>> c = new Callable<Tuple2D<Tuple2D<Integer, Integer>, Double>>() {
//			@Override
//			public Tuple2D<Tuple2D<Integer, Integer>, Double> call()
//				throws Exception {
//
//				return new Tuple2D<Tuple2D<Integer, Integer>, Double>(
//					new Tuple2D<Integer, Integer>(i, j), EICPeakFinder.
//					calcEstimatedCrossCorrelation(ad1, ad2, meana,
//						meanb, vara, varb));
//
//			}
//		};
//		return c;
//	}
    /**
     *
     * @param a
     * @param b
     * @return
     */
    protected Tuple2D<Integer, Double> getMaxAutocorrelation(final Array a,
        final Array b) {
        final Index ia = a.getIndex();
        final Index ib = b.getIndex();
        final ArrayDouble.D1 autoCorr = new ArrayDouble.D1(a.getShape()[0]);
        for (int lag = 0; lag < a.getShape()[0]; lag++) {
            calcEstimatedAutoCorrelation(a, b, 0, 1, lag, autoCorr);
        }
        double max = autoCorr.get(0);
        int maxindex = 0;
        for (int i = 0; i < autoCorr.getShape()[0]; i++) {
            if (autoCorr.get(i) > max) {
                max = autoCorr.get(i);
                maxindex = i;
            }
        }
        return new Tuple2D<Integer, Double>(Integer.valueOf(maxindex), Double.
            valueOf(max));
    }
}
