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
package maltcms.commands.distances.dtw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import maltcms.commands.distances.DtwRecurrence;
import maltcms.commands.distances.PairwiseFeatureSimilarity;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.datastructures.alignment.AnchorPairSet;
import maltcms.datastructures.array.ArrayFactory;
import maltcms.datastructures.array.IArrayD2Double;
import maltcms.datastructures.fragments.PairwiseAlignment;
import maltcms.datastructures.ms.IAnchor;
import maltcms.tools.ArrayTools;
import maltcms.tools.ImageTools;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;
import org.jdom.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.MAMath;
import cross.Factory;
import cross.annotations.Configurable;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.datastructures.tools.EvalTools;
import cross.tools.StringTools;
import java.util.LinkedList;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for dynamic time warping implementations.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
@Data
public abstract class ADynamicTimeWarp implements IDynamicTimeWarp {

//    @Configurable(name = "alignment.algorithm.windowsize")
//    protected transient double maxdeviation = 1.0d;
    protected transient IArrayD2Double alignment = null;
    protected transient IArrayD2Double distance = null;
    protected transient byte[][] predecessors = null;
    protected transient IFileFragment resF = null;
    protected transient IFileFragment refF = null;
    protected transient IFileFragment queryF = null;
    protected transient ArrayDouble.D0 result = null;
    protected transient ArrayDouble.D1 resultVector = null;
    protected transient int ref_num_scans = 0;
    protected transient int query_num_scans = 0;
//    @Configurable(name = "alignment.cumulative.distance")
    protected DtwRecurrence recurrence = null;
    @Configurable(name = "var.scan_acquisition_time")
    protected String scan_acquisition_time = "scan_acquisition_time";
//    @Configurable(name = "alignment.pairwise.distance")
    protected PairwiseFeatureSimilarity pairwiseFeatureSimilarity = null;
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
    @Override
    public IArrayD2Double align(final Tuple2D<List<Array>, List<Array>> tuple,
            final AnchorPairSet ris, final double maxdev1,
            final ArrayDouble.D1 sat_ref, final ArrayDouble.D1 sat_query) {
        EvalTools.notNull(tuple, this);
        // log.info("Starting pairwise alignment!");
        final List<Array> ref = tuple.getFirst();
        final List<Array> query = tuple.getSecond();

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
    @Override
    public void appendXML(final Element e) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * maltcms.commands.distances.dtw.IDynamicTimeWarp#apply(cross.datastructures
     * .fragments.FileFragment, cross.datastructures.fragments.FileFragment)
     */
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
        IFileFragment alignmentFragment = calcAlignment(new Tuple2D<IFileFragment, IFileFragment>(
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
                t.getFirst().getAbsolutePath(), t.getSecond().getAbsolutePath());
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
//        Factory.getInstance().getConfiguration().setProperty(
//                "maltcms.datastructures.alignment.DefaultPairSet.minScansBetweenAnchors",
//                10);
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
            getStatsMap().put("matrixElements",(double)this.alignment.getNumberOfStoredElements());
        }
        // calculate traceback, store path etc.
        this.resF = this.pa.provideFileFragment();
        this.result = this.pa.getResult();
        this.resultVector = this.pa.getResultVector();
        if(getStatsMap() != null) {
            getStatsMap().put("pathLength",(double)pa.getPath().size());
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void calculateCumulativeDistances(final IArrayD2Double distance2,
            final IArrayD2Double alignment2, final int vrows, final int vcols,
            final List<Array> ref, final List<Array> query,
            final ArrayDouble.D1 sat_ref, final ArrayDouble.D1 sat_query,
            final byte[][] predecessors) {
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

    @Override
    public void configure(final Configuration cfg) {
        log.debug("Configure called on {}", this.getClass().getName());
        EvalTools.notNull(cfg, this);
//        setPairwiseScanDistance(Factory.getInstance().getObjectFactory().
//                instantiate(
//                cfg.getString("alignment.pairwise.distance",
//                "maltcms.commands.distances.PairwiseDistance"),
//                PairwiseFeatureSimilarity.class));

        this.bandWidthPercentage = cfg.getDouble("alignment.algorithm.windowsize", 1.0);

//        final DtwRecurrence cdist = Factory.getInstance().getObjectFactory().
//                instantiate(
//                Factory.getInstance().getConfiguration().getString(
//                "alignment.cumulative.distance",
//                "maltcms.commands.distances.CumulativeDistance"),
//                DtwRecurrence.class);
//        setCumulativeDistance(cdist);

//        EvalTools.notNull(new Object[]{getCumulativeDistance(),
//                    getPairwiseScanDistance(),
//                    getPairwiseScanDistance().getDistance()}, this);
//        getCumulativeDistance().setMinimizing(
//                getPairwiseScanDistance().getDistance().minimize());
//        final IDtwSimilarityFunction iac = getPairwiseScanDistance().
//                getDistance();
//        getCumulativeDistance().set(iac.getCompressionWeight(),
//                iac.getExpansionWeight(), iac.getDiagonalWeight());


        this.scan_acquisition_time = cfg.getString("var.scan_acquisition_time",
                "scan_acquisition_time");
//        this.anchorRadius = cfg.getInteger(
//                "alignment.anchors.neighborhood", 10);
//        this.precalculatePairwiseDistances = cfg.getBoolean(
//                "alignment.precalculatePairwiseDistances", true);
//        this.globalBand = cfg.getBoolean("alignment.globalBand", true);
//        this.extension = cfg.getString(
//                this.getClass().getName() + ".extension", "");
//        this.saveLayoutImage = cfg.getBoolean("alignment.saveLayoutImage",
//                false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * maltcms.commands.distances.dtw.IDynamicTimeWarp#getCumulativeDistance()
     */
    public DtwRecurrence getCumulativeDistance() {
        return this.recurrence;
    }

    public String getExtension() {
        return this.extension;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflow()
     */
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
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.ALIGNMENT;
    }

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
        this.predecessors = new byte[rows][cols];
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
    @Override
    public boolean minimize() {
        final boolean b = (this.pairwiseFeatureSimilarity != null) ? this.pairwiseFeatureSimilarity.getSimilarityFunction().minimize() : true;
        return b;
    }

    protected IFileFragment saveState(final IFileFragment pa) {
        final long start = System.currentTimeMillis();
        pa.save();
        System.gc();
        final long time = System.currentTimeMillis() - start;
        log.debug("Saved state in {} milliseconds", time);
        return pa;
    }

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
    @Override
    public void setFileFragments(final IFileFragment a, final IFileFragment b) {
        EvalTools.notNull(new Object[]{a, b}, this);
        this.refF = a;
        this.queryF = b;
    }
}
