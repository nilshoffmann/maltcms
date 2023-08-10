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
package maltcms.commands.distances.dtw;

import cross.Factory;
import cross.annotations.Configurable;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import lombok.Data;

import maltcms.commands.distances.DtwRecurrence;
import maltcms.commands.distances.PairwiseFeatureSimilarity;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.datastructures.alignment.AnchorPairSet;
import maltcms.datastructures.array.ArrayFactory;
import maltcms.datastructures.array.IArrayD2Double;
import maltcms.datastructures.fragments.PairwiseAlignment;
import maltcms.datastructures.ms.IAnchor;
import maltcms.tools.ImageTools;
import maltcms.tools.MaltcmsTools;
import org.apache.commons.configuration.Configuration;
import org.jdom2.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.MAMath;

/**
 * Base class for dynamic time warping implementations.
 *
 * @author Nils Hoffmann
 * 
 */

@Data
public abstract class ADynamicTimeWarp implements IDynamicTimeWarp {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ADynamicTimeWarp.class);

//    @Configurable(name = "alignment.algorithm.windowsize")
//    protected transient double maxdeviation = 1.0d;
    /**
     *
     */
    protected transient IArrayD2Double alignment = null;
    /**
     *
     */
    protected transient IArrayD2Double distance = null;
    /**
     *
     */
    protected transient ArrayByte.D2 predecessors = null;
    /**
     *
     */
    protected transient IFileFragment resF = null;
    /**
     *
     */
    protected transient IFileFragment refF = null;
    /**
     *
     */
    protected transient IFileFragment queryF = null;
    /**
     *
     */
    protected transient ArrayDouble.D0 result = null;
    /**
     *
     */
    protected transient ArrayDouble.D1 resultVector = null;
    /**
     *
     */
    protected transient int ref_num_scans = 0;
    /**
     *
     */
    protected transient int query_num_scans = 0;
//    @Configurable(name = "alignment.cumulative.distance")
    /**
     *
     */
    protected DtwRecurrence recurrence = new DtwRecurrence();
    /**
     *
     */
    @Configurable(name = "var.scan_acquisition_time")
    protected String scan_acquisition_time = "scan_acquisition_time";
//    @Configurable(name = "alignment.pairwise.distance")
    /**
     *
     */
    protected PairwiseFeatureSimilarity pairwiseFeatureSimilarity = new PairwiseFeatureSimilarity();
    private StatsMap statsMap;
    private boolean useAnchors = true;
    private int anchorRadius = 10;
    private int minScansBetweenAnchors = 10;
    private IWorkflow workflow;
    @Configurable(name = "alignment.precalculatePairwiseDistances",
            value = "true")
    private boolean precalculatePairwiseDistances = true;
    private PairwiseAlignment pa;
    @Configurable(name = "alignment.globalBand", value = "true")
    private boolean globalBand = true;
    private double bandWidthPercentage = 1.0d;
    private String extension = "";
    @Configurable(name = "alignment.saveLayoutImage", value = "false")
    private boolean saveLayoutImage = false;
    private boolean sample = false;
    private boolean saveDtwMatrix = false;
    private boolean savePairwiseSimilarityMatrix = false;
    private boolean normalizeAlignmentValue = false;

    /**
     * <p>Constructor for ADynamicTimeWarp.</p>
     */
    public ADynamicTimeWarp() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.commands.distances.dtw.IDynamicTimeWarp#align(cross.datastructures
     * .tuple.Tuple2D, maltcms.datastructures.AnchorPairSet, double)
     */
    /** {@inheritDoc} */
    @Override
    public IArrayD2Double align(final Tuple2D<List<Array>, List<Array>> tuple,
            final AnchorPairSet ris, final double maxdev1,
            final ArrayDouble.D1 sat_ref, final ArrayDouble.D1 sat_query) {
        EvalTools.notNull(tuple, this);
        // log.info("Starting pairwise alignment!");
        final List<Array> ref = tuple.getFirst();
        final List<Array> query = tuple.getSecond();
        EvalTools.neqI(0, ref.size(), this);
        EvalTools.neqI(0, query.size(), this);
        final int rows = ref.size();
        final int cols = query.size();

        if (this.pairwiseFeatureSimilarity.getSimilarityFunction().minimize()) {
            log.debug("Initializing matrices with {}",
                    Double.POSITIVE_INFINITY);
            initMatrices(rows, cols, Double.POSITIVE_INFINITY, ris, maxdev1);
        } else {
            log.debug("Initializing matrices with {}",
                    Double.NEGATIVE_INFINITY);
            initMatrices(rows, cols, Double.NEGATIVE_INFINITY, ris, maxdev1);
        }

        //saveLayoutImage(ris,"test");
        EvalTools.notNull(new Object[]{this.distance, this.alignment}, this);
        final long start = System.currentTimeMillis();
        if (this.precalculatePairwiseDistances) {
            log.info("Precalculating pairwise distances/similarities");
            this.pairwiseFeatureSimilarity.calculatePairwiseDistances(
                    this.distance, sat_ref, sat_query, ref, query);
        }
        calculateCumulativeDistances(this.distance, this.alignment, rows, cols,
                ref, query, sat_ref, sat_query, this.predecessors);
        // log.info(alignment.toString());
        final long time = System.currentTimeMillis() - start;
        if (getStatsMap() != null) {
            getStatsMap().put("totalMatrixCalculationTime", (double) time);
        }
        log.info("Time to calculate alignment matrices: {} milliseconds",
                time);
        log.debug("Shape of alignment: {} X {}, sx {}, sy {}",
                new Object[]{this.alignment.rows(), this.alignment.columns(),
                    rows, cols});
        log.info(
                "Value of alignment[{}][{}]={}",
                new Object[]{
                    this.alignment.rows() - 1,
                    this.alignment.columns() - 1,
                    this.alignment.get(this.alignment.rows() - 1,
                            this.alignment.columns() - 1)});
        return this.alignment;
    }

    private void samplePWDistance(
            final Tuple2D<List<Array>, List<Array>> tuple,
            final ArrayDouble.D1 sat_ref, final ArrayDouble.D1 sat_query,
            int samples, int bins, String name) {
        Random r1 = new Random();
        Random r2 = new Random();
        HistogramDataset hd = new HistogramDataset();
        double[] values = new double[samples];
        for (int i = 0; i < samples; i++) {
            int i1 = r1.nextInt(tuple.getFirst().size());
            int i2 = r2.nextInt(tuple.getSecond().size());
            values[i] = this.pairwiseFeatureSimilarity.getDistance(i1, i2,
                    sat_ref.get(i1), sat_query.get(i2), tuple.getFirst().get(i1),
                    tuple.getSecond().get(i2));
            log.debug("Sampling {},{} = {}", new Object[]{i1, i2, values[i]});
        }
        hd.addSeries(name, values, bins);
        JFreeChart jfc = ChartFactory.createHistogram(
                "Histogram of "
                + name
                + " using "
                + this.pairwiseFeatureSimilarity.getSimilarityFunction().getClass().getName(),
                "value", "count", hd,
                PlotOrientation.VERTICAL, true, true, true);
        ImageTools.writeImage(jfc,
                new File(getWorkflow().getOutputDirectory(this), name
                        + "-pw-histogram.png"), 1024, 768);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.io.misc.IXMLSerializable#appendXML(org.jdom.Element)
     */
    /** {@inheritDoc} */
    @Override
    public void appendXML(final Element e) {
        
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.commands.distances.dtw.IDynamicTimeWarp#apply(cross.datastructures
     * .fragments.FileFragment, cross.datastructures.fragments.FileFragment)
     */
    /** {@inheritDoc} */
    @Override
    public IFileFragment apply(final IFileFragment a, final IFileFragment b) {
        log.info(
                "#############################################################################");
        final String s = this.getClass().getName();
        log.info("# {} running", s);
        log.info(
                "#############################################################################");
        log.info("LHS: {}", a.getName());
        log.info("RHS: {}", b.getName());
        EvalTools.notNull(new Object[]{a, b}, this);
        IFileFragment alignmentFragment = calcAlignment(new Tuple2D<>(
                a, b));
        return alignmentFragment;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.commands.distances.dtw.IDynamicTimeWarp#apply(cross.datastructures
     * .tuple.Tuple2D)
     */
    /** {@inheritDoc} */
    @Override
    public Array[] apply(final Tuple2D<Array[], Array[]> t) {
        EvalTools.notNull(t, this);
        EvalTools.notNull(new Object[]{this.refF, this.queryF}, this);
        this.resF = apply(this.refF, this.queryF);
        EvalTools.notNull(this.result, this);
        return new Array[]{this.result};
    }

    private AnchorPairSet buildAnchorPairSet(
            final Tuple2D<IFileFragment, IFileFragment> t) {
        log.debug("Loading anchors for {} and {}",
                t.getFirst().getUri(), t.getSecond().getUri());
        Tuple2D<List<IAnchor>, List<IAnchor>> ris = null;
        AnchorPairSet aps;
        if (useAnchors) {
            ris = MaltcmsTools.getAnchors(t.getFirst(), t.getSecond());

            if ((ris.getFirst().size() > 0) && (ris.getSecond().size() > 0)) {
                log.info("Using {} alignment anchors", ris.getFirst().size());
                aps = new AnchorPairSet(ris.getFirst(),
                        ris.getSecond(), this.ref_num_scans,
                        this.query_num_scans, minScansBetweenAnchors);
                for (final Tuple2D<Integer, Integer> ta : aps.getCorrespondingScans()) {
                    log.debug("{}<->{}", ta.getFirst(), ta.getSecond());
                }
                return aps;
            }
        }
        //return default anchors at 0,0 and N-1,M-1
        aps = new AnchorPairSet(new LinkedList<IAnchor>(),
                new LinkedList<IAnchor>(), ref_num_scans, query_num_scans,
                minScansBetweenAnchors);

        return aps;
    }

    /**
     * @param t
     * @return
     */
    private IFileFragment calcAlignment(
            final Tuple2D<IFileFragment, IFileFragment> t) {
        this.pa = new PairwiseAlignment();
        this.pa.setWorkflow(getWorkflow());
        this.pa.setSaveCDM(saveDtwMatrix);
        this.pa.setSavePWDM(savePairwiseSimilarityMatrix);
        this.pa.setNormalizeByMapLength(normalizeAlignmentValue);
        this.pa.setFileFragments(t.getFirst(), t.getSecond(), this.getClass());

        final Tuple2D<List<Array>, List<Array>> tuple = createTuple(t);
        final AnchorPairSet aps = buildAnchorPairSet(t);
        final ArrayDouble.D1 sat_ref = getScanAcquisitionTime(t.getFirst());
        final ArrayDouble.D1 sat_query = getScanAcquisitionTime(t.getSecond());
        if (sample) {
            samplePWDistance(
                    tuple,
                    sat_ref,
                    sat_query,
                    (int) Math.ceil(0.01 * tuple.getFirst().size()
                            * tuple.getSecond().size()), 10,
                    StringTools.removeFileExt(t.getFirst().getName()) + " "
                    + StringTools.removeFileExt(t.getSecond().getName()));
        }
        // calculate the alignment matrix
        this.alignment = align(tuple, aps, this.bandWidthPercentage, sat_ref,
                sat_query);
        EvalTools.notNull(this.alignment, this);

        // capture state in pairwise alignment
        this.pa.setTraceMatrix(this.predecessors);
        this.pa.setAlignment(this.alignment);
        this.pa.setPairwiseDistances(this.distance);

        // Possibly take this out
        final ArrayStatsScanner ass = Factory.getInstance().getObjectFactory().
                instantiate(ArrayStatsScanner.class);
        final StatsMap sm = ass.apply(new Array[]{this.distance.flatten().
            getSecond()})[0];
        this.pa.setIsMinimizing(minimize());
        this.pa.setCumulativeDistance(this.recurrence);
        this.pa.setPairwiseDistance(getPairwiseFeatureSimilarity());
        this.pa.setNumberOfScansReference(this.ref_num_scans);
        this.pa.setNumberOfScansQuery(this.query_num_scans);
        this.pa.setAnchors(aps);
        if (getStatsMap() != null) {
            getStatsMap().put("avgPWValue", sm.get(Vars.Mean.toString()));
            getStatsMap().put("minPWValue", sm.get(Vars.Min.toString()));
            getStatsMap().put("maxPWValue", sm.get(Vars.Max.toString()));
            getStatsMap().put("nanchors", (double) (aps.getSize() - 2));
            getStatsMap().put("lhsNscans", (double) this.ref_num_scans);
            getStatsMap().put("rhsNscans", (double) this.query_num_scans);
            getStatsMap().put("longestPath",
                    (double) (this.ref_num_scans + this.query_num_scans - 1));
            getStatsMap().put("w_exp",
                    this.recurrence.getExpansionWeight());
            getStatsMap().put("w_comp",
                    this.recurrence.getCompressionWeight());
            getStatsMap().put("w_diag",
                    this.recurrence.getDiagonalWeight());
            getStatsMap().put("gap_global",
                    this.recurrence.getGlobalGapPenalty());
            getStatsMap().put("matrixElements", (double) this.alignment.getNumberOfStoredElements());
        }
        // calculate traceback, store path etc.
        this.resF = this.pa.provideFileFragment();
        this.result = this.pa.getResult();
        this.resultVector = this.pa.getResultVector();
        if (getStatsMap() != null) {
            getStatsMap().put("pathLength", (double) pa.getPath().size());
        }
        // add the pairwise alignment to bookkeeping
        // MaltcmsTools.addPairwiseAlignment(t.getFirst(), t.getSecond(),
        // this.resF, this.extension);
        // save the path to csv
        // pt.savePathCSV(this.resF, this.alignment, pa.getPath(),
        // getWorkflow());
        final IFileFragment forwardAlignment = saveState(this.resF);
        if (this.saveLayoutImage) {
            saveLayoutImage(aps, StringTools.removeFileExt(forwardAlignment.getName()));
        }
//        this.alignment = null;
//        this.distance = null;
//        this.predecessors = null;
        t.getFirst().clearArrays();
        t.getSecond().clearArrays();
        return forwardAlignment;
    }

    /**
     * @param aps
     * @param alignmentName
     */
    private void saveLayoutImage(final AnchorPairSet aps,
            final String alignmentName) {
        final ArrayFactory f = Factory.getInstance().getObjectFactory().
                instantiate(ArrayFactory.class);
        final BufferedImage bi = f.createLayoutImage(this.distance);
        final Color c = Color.white;
        final Graphics2D g2 = (Graphics2D) bi.getGraphics();
        if (this.pa != null) {
            final List<Tuple2DI> l = this.pa.getPath();
            final GeneralPath gp = new GeneralPath();
            gp.moveTo(0, 0);
            for (final Tuple2DI pair : l) {
                gp.lineTo(pair.getSecond(), pair.getFirst());
            }
            g2.setColor(c);
            g2.draw(gp);
        }

        // final Color d = new Color(0, 0, 255, 128);
        // final List<Tuple2DI> l2 = this.pa.getInterppath();
        // final GeneralPath gp2 = new GeneralPath();
        // gp2.moveTo(0, 0);
        // for (final Tuple2DI pair : l2) {
        // gp2.lineTo(pair.getSecond(), pair.getFirst());
        // }
        // g2.setColor(d);
        // g2.draw(gp2);
        g2.setColor(Color.RED);
        for (final Tuple2D<Integer, Integer> pair : aps.getCorrespondingScans()) {
            g2.fillRect(pair.getSecond(), pair.getFirst(), 1, 1);
        }
        try {
            ImageIO.write(bi, "PNG", new File(getWorkflow().getOutputDirectory(
                    this), alignmentName
                    + "_matrixLayout.png"));
        } catch (final IOException e) {
            
            log.warn(e.getLocalizedMessage());
        }
    }

    /**
     * <p>calculateCumulativeDistances.</p>
     *
     * @param distance2 a {@link maltcms.datastructures.array.IArrayD2Double} object.
     * @param alignment2 a {@link maltcms.datastructures.array.IArrayD2Double} object.
     * @param vrows a int.
     * @param vcols a int.
     * @param ref a {@link java.util.List} object.
     * @param query a {@link java.util.List} object.
     * @param sat_ref a {@link ucar.ma2.ArrayDouble.D1} object.
     * @param sat_query a {@link ucar.ma2.ArrayDouble.D1} object.
     * @param predecessors a {@link ucar.ma2.ArrayByte.D2} object.
     */
    protected void calculateCumulativeDistances(final IArrayD2Double distance2,
            final IArrayD2Double alignment2, final int vrows, final int vcols,
            final List<Array> ref, final List<Array> query,
            final ArrayDouble.D1 sat_ref, final ArrayDouble.D1 sat_query,
            final ArrayByte.D2 predecessors) {
        final long start = System.currentTimeMillis();
        log.debug("Calculating the alignment matrix!");
        EvalTools.notNull(new Object[]{distance2, alignment2}, this);
        log.debug("Number of query scans {}", query.size());
        log.debug("Number of ref scans {}", ref.size());
        // cumulativeDistance.set(compression_penalty, expansion_penalty,
        // diagonal_penalty);

        log.debug("Set up anchors!");
        final long elements = this.alignment.getNumberOfStoredElements();
        log.info("Calculating {} pairwise scores/costs", elements);
        if (precalculatePairwiseDistances) {
            for (int i = 0; i < this.alignment.rows(); i++) {
                final int[] bounds = this.alignment.getColumnBounds(i);
                for (int j = bounds[0]; j < bounds[0] + bounds[1]; j++) {
                    final double sat_r = sat_ref == null ? -1 : sat_ref.get(i);
                    final double sat_q = sat_query == null ? -1 : sat_query.get(j);
                    this.recurrence.eval(i, j,
                            alignment2, this.pairwiseFeatureSimilarity.getDistance(i, j,
                                    sat_r, sat_q, ref.get(i), query.get(j)), predecessors);
                }
            }
        } else {
            for (int i = 0; i < this.alignment.rows(); i++) {
                final int[] bounds = this.alignment.getColumnBounds(i);
                for (int j = bounds[0]; j < bounds[0] + bounds[1]; j++) {
                    final double sat_r = sat_ref == null ? -1 : sat_ref.get(i);
                    final double sat_q = sat_query == null ? -1 : sat_query.get(j);
                    distance2.set(i, j, this.pairwiseFeatureSimilarity.getDistance(i, j,
                            sat_r, sat_q, ref.get(i), query.get(j)));
                    this.recurrence.eval(i, j,
                            alignment2, distance2.get(i, j), predecessors);
                }
            }
        }

        // log.info("{}%", (int) (Math.ceil(percentDone * 100.0d)));
        final long time = System.currentTimeMillis() - start;
        log.debug("Calculated cumulative distance in {} milliseconds",
                time);
        if (getStatsMap() != null) {
            getStatsMap().put("alignmentMatrixCalculationTime", (double) time);
        }

    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        log.debug("Configure called on {}", this.getClass().getName());
        this.scan_acquisition_time = cfg.getString("var.scan_acquisition_time",
                "scan_acquisition_time");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.commands.distances.dtw.IDynamicTimeWarp#getCumulativeDistance()
     */
    /**
     * <p>getCumulativeDistance.</p>
     *
     * @return a {@link maltcms.commands.distances.DtwRecurrence} object.
     */
    public DtwRecurrence getCumulativeDistance() {
        return this.recurrence;
    }

    /**
     * <p>Getter for the field <code>extension</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getExtension() {
        return this.extension;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflow()
     */
    /** {@inheritDoc} */
    @Override
    public IWorkflow getWorkflow() {
        return this.workflow;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.commands.distances.dtw.IDynamicTimeWarp#getResultFileFragment()
     */
    /** {@inheritDoc} */
    @Override
    public IFileFragment getResultFileFragment() {
        EvalTools.notNull(this.resF, this);
        return this.resF;
    }

    private ArrayDouble.D1 getScanAcquisitionTime(final IFileFragment f) {
        Array sat_ref = null;
        try {
            sat_ref = f.getChild(this.scan_acquisition_time).getArray();
            EvalTools.eqI(sat_ref.getRank(), 1, this);
            final ArrayDouble.D1 ret = new ArrayDouble.D1(sat_ref.getShape()[0]);
            MAMath.copyDouble(ret, sat_ref);
            return ret;
        } catch (final ResourceNotAvailableException e) {
            log.debug("Could not load " + this.scan_acquisition_time
                    + " for " + f.getName());
        }
        // }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /** {@inheritDoc} */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.ALIGNMENT;
    }

    /**
     * <p>initMatrices.</p>
     *
     * @param rows a int.
     * @param cols a int.
     * @param value a {@link java.lang.Double} object.
     * @param aps a {@link maltcms.datastructures.alignment.AnchorPairSet} object.
     * @param band a double.
     */
    protected void initMatrices(final int rows, final int cols,
            final Double value, final AnchorPairSet aps, final double band) {
        final long start = System.currentTimeMillis();
        final ArrayFactory f = Factory.getInstance().getObjectFactory().
                instantiate(ArrayFactory.class);
        final IArrayD2Double parts1 = f.create(rows, cols, aps,
                this.anchorRadius, band, value.doubleValue(),
                this.globalBand);
        this.alignment = parts1;
        final IArrayD2Double parts2 = f.createSharedLayout(parts1);
        this.distance = parts2;
        this.predecessors = new ArrayByte.D2(rows, cols);
        log.debug("Alignment matrix has {} rows and {} columns",
                parts1.rows(), parts2.columns());
        final long time = System.currentTimeMillis() - start;
        if (getStatsMap() != null) {
            getStatsMap().put("alignmentMatrixInitTime", (double) time);
        }
        log.debug("Initialized matrices in {} milliseconds", time);
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.commands.distances.dtw.IDynamicTimeWarp#minimize()
     */
    /** {@inheritDoc} */
    @Override
    public boolean minimize() {
        final boolean b = (this.pairwiseFeatureSimilarity != null) ? this.pairwiseFeatureSimilarity.getSimilarityFunction().minimize() : true;
        return b;
    }

    /**
     * <p>saveState.</p>
     *
     * @param pa a {@link cross.datastructures.fragments.IFileFragment} object.
     * @return a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    protected IFileFragment saveState(final IFileFragment pa) {
        final long start = System.currentTimeMillis();
        pa.save();
        System.gc();
        final long time = System.currentTimeMillis() - start;
        log.debug("Saved state in {} milliseconds", time);
        return pa;
    }

    /**
     * <p>Setter for the field <code>extension</code>.</p>
     *
     * @param newExtension a {@link java.lang.String} object.
     */
    public void setExtension(final String newExtension) {
        if (newExtension != null) {
            this.extension = newExtension;
        } else {
            this.extension = "";
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.commands.distances.dtw.IDynamicTimeWarp#setFileFragments(cross
     * .datastructures.fragments.FileFragment,
     * cross.datastructures.fragments.FileFragment)
     */
    /** {@inheritDoc} */
    @Override
    public void setFileFragments(final IFileFragment a, final IFileFragment b) {
        EvalTools.notNull(new Object[]{a, b}, this);
        this.refF = a;
        this.queryF = b;
    }
}
