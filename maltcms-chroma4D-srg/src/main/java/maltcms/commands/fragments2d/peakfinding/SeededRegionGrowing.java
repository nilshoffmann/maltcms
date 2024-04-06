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
package maltcms.commands.fragments2d.peakfinding;

import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.CachedList;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.FragmentTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import lombok.Data;

import maltcms.commands.fragments2d.peakfinding.comparator.PeakComparator;
import maltcms.commands.fragments2d.peakfinding.output.IPeakExporter;
import maltcms.commands.fragments2d.peakfinding.output.IPeakIntegration;
import maltcms.commands.fragments2d.peakfinding.output.PeakExporter;
import maltcms.commands.fragments2d.peakfinding.output.PeakIntegration;
import maltcms.commands.fragments2d.peakfinding.picking.IPeakPicking;
import maltcms.commands.fragments2d.peakfinding.picking.SimplePeakPicking;
import maltcms.commands.fragments2d.peakfinding.srg.IPeakSeparator;
import maltcms.commands.fragments2d.peakfinding.srg.IRegionGrowing;
import maltcms.commands.fragments2d.peakfinding.srg.OneByOneRegionGrowing;
import maltcms.commands.fragments2d.peakfinding.srg.PeakSeparator;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.datastructures.ms.Chromatogram2D;
import maltcms.datastructures.ms.IChromatogram2D;
import maltcms.datastructures.ms.IScan2D;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.PeakArea2D;
import maltcms.io.csv.ColorRampReader;
import maltcms.tools.ArrayTools2;
import maltcms.tools.ImageTools;
import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.BHeatMapChart;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYBPlot;
import org.apache.commons.configuration.Configuration;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;

/**
 * Peakpicking + integration + identification + normalization + evaluation...
 *
 * @author Mathias Wilhelm
 *
 */

@Data
@RequiresVariables(names = {"var.total_intensity", "var.scan_rate",
    "var.modulation_time", "var.second_column_scan_index",
    "var.first_column_elution_time", "var.second_column_elution_time"})
@RequiresOptionalVariables(names = {"var.v_total_intensity", "var.tic_peaks"})
@ProvidesVariables(names = {"var.peak_index_list", "var.region_index_list",
    "var.region_peak_index", "var.boundary_index_list",
    "var.boundary_peak_index", "var.peak_mass_intensity"})
@ServiceProvider(service = AFragmentCommand.class)
public class SeededRegionGrowing extends AFragmentCommand {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SeededRegionGrowing.class);

    @Configurable(name = "var.total_intensity", value = "total_intensity",
            type = String.class)
    private String totalIntensityVar = "total_intensity";
    @Configurable(name = "var.scan_rate", value = "scan_rate",
            type = String.class)
    private String scanRateVar = "scan_rate";
    @Configurable(name = "var.modulation_time", value = "modulation_time",
            type = String.class)
    private String modulationTimeVar = "modulation_time";
    @Configurable(name = "var.second_column_scan_index",
            value = "second_column_scan_index", type = String.class)
    private String secondScanIndexVar = "second_column_scan_index";
    @Configurable(name = "var.peak_index_list", value = "peak_index_list",
            type = String.class)
    private String peakListVar = "peak_index_list";
    @Configurable(name = "var.first_column_elution_time")
    private String first_column_elution_time = "first_column_elution_time";
    @Configurable(name = "var.second_column_elution_time")
    private String second_column_elution_time = "second_column_elution_time";
    @Configurable(name = "var.region_index_list", value = "region_index_list",
            type = String.class)
    private String regionIndexListVar = "region_index_list";
    @Configurable(name = "var.region_peak_index", value = "region_peak_index",
            type = String.class)
    private String regionPeakIndexVar = "region_peak_index";
    @Configurable(name = "var.boundary_index_list",
            value = "boundary_index_list", type = String.class)
    private String boundaryIndexListVar = "boundary_index_list";
    @Configurable(name = "var.boundary_peak_index",
            value = "boundary_peak_index", type = String.class)
    private String boundaryPeakIndexVar = "boundary_peak_index";
    @Configurable(name = "maltcms.ui.charts.PlotRunner.filetype", value = "png",
            type = String.class, description="The format for saved plots.")
    private final String format = "png";
    @Configurable(name = "images.colorramp", value = "res/colorRamps/bcgyr.csv",
            type = String.class, description="The location of the color ramp to use")
    private String colorrampLocation = "res/colorRamps/bcgyr.csv";
    @Configurable(name = "ucar.nc2.NetcdfFile.fillValueDouble",
            value = "9.9692099683868690e+36d", type = double.class)
    private double doubleFillValue;
    @Configurable(name = "images.thresholdLow", value = "0", type = double.class, 
            description="The minimum intensity value to include in images.")
    private double threshold = 0;
    @Configurable(value = "true", type = boolean.class)
    private boolean separate = true;
    @Deprecated
    @Configurable(value = "false", type = boolean.class, description="Deprecated.")
    private boolean doNormalization = false;
    @Configurable(value = "false", type = boolean.class, description="If true,"
            + "peaks will be integrated.")
    private boolean doIntegration = false;
    @Configurable(description="The peak picking implementation to use. "
            + "Use SimplePeakPicking for standalone operation, or "
            + "TicPeakPicking, if another peak finder, such as CWTPeakFinder "
            + "has already detected peaks.")
    private IPeakPicking peakPicking = new SimplePeakPicking();
    @Configurable(description="The region growing implementation to use.")
    private IRegionGrowing regionGrowing = new OneByOneRegionGrowing();
    @Configurable(description="The peak integration implementation to use.")
    private IPeakIntegration integration = new PeakIntegration();
    @Configurable(description="The peak exporter implementation to use.")
    private IPeakExporter peakExporter = new PeakExporter();
    @Configurable(description="The peak separator implementation to use.")
    private IPeakSeparator peakSeparator = new PeakSeparator();
    
    private int scansPerModulation = 0;
    private List<List<Peak2D>> peakLists = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        Tuple2D<Double, Double> massRange = MaltcmsTools.getMinMaxMassRange(t);
        ScanLineCacheFactory.setMinMass(massRange.getFirst());
        ScanLineCacheFactory.setMaxMass(massRange.getSecond());
        final ColorRampReader crr = new ColorRampReader();
        final int[][] colorRamp = crr.readColorRamp(this.colorrampLocation);
        final ArrayList<IFileFragment> ret = new ArrayList<>();

        this.peakExporter.setWorkflow(getWorkflow());
        this.peakExporter.setCaller(this.getClass());

        // running SRG the first time
        List<Peak2D> peaklist = null;
        for (final IFileFragment ff : t) {
            final IChromatogram2D chrom = new Chromatogram2D(ff);
            peaklist = runSRG(chrom, null);
            Collections.sort(peaklist, new PeakComparator());
            for (int j = 0; j < peaklist.size(); j++) {
                Peak2D peak = peaklist.get(j);
                peak.setIndex(j);
                Point p = chrom.getPointFor(peak.getApexIndex());
                IScan2D scan = chrom.getScan2D(p.x, p.y);
                peak.setFirstRetTime(scan.getFirstColumnScanAcquisitionTime());
                peak.setSecondRetTime(scan.getSecondColumnScanAcquisitionTime());
            }
            addAdditionalInformation(peaklist, ff);
            log.info("Saving peaks for {}", ff.getName());
            final IFileFragment fret = new FileFragment(
                    new File(getWorkflow().getOutputDirectory(this), ff.
                            getName()));
            fret.addSourceFile(ff);
            savePeaks(chrom, fret, peaklist, colorRamp);
            final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
                    fret.getUri(), this, getWorkflowSlot(),
                    ff);
            getWorkflow().append(dwr);
            fret.save();
            ret.add(fret);
        }
        return new TupleND<>(ret);
    }

    /**
     * Returns the truncates names of the input files
     *
     * @param t list of input file fragments
     * @return list of strings
     */
    private List<String> getNamesFor(final TupleND<IFileFragment> t) {
        final List<String> chromatogramNames = new ArrayList<>();
        for (final IFileFragment ff : t) {
            chromatogramNames.add(StringTools.removeFileExt(ff.getName()));
        }
        return chromatogramNames;
    }

    /**
     * Will run the seeded region growing either on the given input seeds or
     * uses the {@link IPeakPicking} class to find seeds. After extending the
     * region towards its maximum, the {@link PeakSeparator} will try to merge
     * or separate the resulting {@link PeakArea2D}s.
     *
     * @param chrom the chromatogram
     * @param seeds initial seeds. If this parameter is <code>null</code> then
     * the {@link IPeakPicking} class will be used to determine seeds.
     * @return List of resulting peaks
     */
    private List<Peak2D> runSRG(IChromatogram2D chrom, List<Point> seeds) {
        final IFileFragment ff = chrom.getParent();
        final double scanRate = ff.getChild(this.scanRateVar).getArray().getDouble(
                Index.scalarIndexImmutable);
        final double modulationTime = ff.getChild(this.modulationTimeVar).getArray().
                getDouble(Index.scalarIndexImmutable);
        this.scansPerModulation = (int) (scanRate * modulationTime);
        log.info("Starting seeded region growing for " + ff.getName());

        if (seeds == null) {
            log.info("Starting peak import for " + ff.getName());
            seeds = this.peakPicking.findPeaks(chrom);
            Collections.sort(seeds, new Comparator<Point>() {

                @Override
                public int compare(Point o1, Point o2) {
                    if (o1.x < o2.x) {
                        return -1;
                    } else if (o1.x > o2.x) {
                        return 1;
                    } else if (o1.x == o2.x) {
                        if (o1.y < o2.y) {
                            return -1;
                        } else if (o1.y > o2.y) {
                            return 1;
                        }
                    }
                    return 0;
                }
            });
            log.info("Imported {} peaks for {}", seeds.size(), ff.getName());
        } else {
            log.info("Restarting seeded region growing for " + ff.getName());
        }

        long start = System.currentTimeMillis();
        final List<PeakArea2D> peakAreaList = this.regionGrowing.getAreasFor(
                seeds, ff, chrom);

        log.info("Region growing took {} ms", System.currentTimeMillis()
                - start);

        if (this.separate) {
            this.peakSeparator.startSeparationFor(peakAreaList, chrom, getRetentionTimes(ff));
        }

        final List<Peak2D> peaklist = createPeaklist(peakAreaList,
                getRetentionTimes(ff));
        return peaklist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        this.totalIntensityVar = cfg.getString(this.getClass().getName()
                + ".totalIntensityVar", "total_intensity");
        this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
        this.modulationTimeVar = cfg.getString("var.modulation_time",
                "modulation_time");
        this.first_column_elution_time = cfg.getString("var.first_column_elution_time",
                "first_column_elution_time");
        this.second_column_elution_time = cfg.getString("var.second_column_elution_time",
                "second_column_elution_time");
        this.peakListVar = cfg.getString("var.peak_index_list",
                "peak_index_list");
        this.boundaryIndexListVar = cfg.getString("var.boundary_index_list",
                "boundary_index_list");
        this.boundaryPeakIndexVar = cfg.getString("var.boundary_peak_list",
                "boundary_peak_list");
        this.regionIndexListVar = cfg.getString("var.region_index_list",
                "region_index_list");
        this.regionPeakIndexVar = cfg.getString("var.region_peak_list",
                "region_peak_list");
        this.colorrampLocation = cfg.getString("images.colorramp",
                "res/colorRamps/bcgyr.csv");
        this.doubleFillValue = cfg.getDouble(
                "ucar.nc2.NetcdfFile.fillValueDouble", 9.9692099683868690e+36);
        this.threshold = cfg.getDouble("images.thresholdLow", 0.0d);
    }

    /**
     * Save the given {@link BufferedImage} and serialize it.
     *
     * @param image image
     * @param name name
     * @param title title of the plot
     * @param peakList peaks
     * @param times first and second retention time
     */
    private void createAndSaveImage(final BufferedImage image,
            final String name, final String title, final List<Peak2D> peakList,
            final Tuple2D<Array, Array> times) {
        File savedImage = ImageTools.saveImage(image, name, this.format, getWorkflow().
                getOutputDirectory(this), this);
        log.info("Using file {} for AChart", savedImage.getAbsolutePath());

        final AChart<XYBPlot> chart = new BHeatMapChart(savedImage.getAbsolutePath(),
                "first retention time[s]", "second retention time[s]", times,
                name);
        final XYPlot plot = chart.create();
        for (final Peak2D p : peakList) {
            final XYPointerAnnotation pointer = new XYPointerAnnotation(
                    // p.getName() + "(" +
                    p.getIndex() + "" // + ")"
                    , p.getFirstRetTime(), p.getSecondRetTime(),
                    7 * Math.PI / 4.0d);
            pointer.setTipRadius(0.0d);
            pointer.setArrowLength(0.0d);
            pointer.setBaseRadius(0.0d);
            pointer.setPaint(Color.WHITE);
            plot.addAnnotation(pointer);
        }
        final File d = getWorkflow().getOutputDirectory(this);
        final PlotRunner pl = new PlotRunner(plot, title, name + "_plot", d);
        pl.configure(Factory.getInstance().getConfiguration());
        try {
            pl.call();
        } catch (Exception ex) {
            log.warn("Caught exception while plotting: ", ex);
        }
    }

    /**
     * Creates and writes a csv containing all needed information about the
     * peak.
     *
     * @param pas list of all snakes
     * @param times first and second retention time
     * @return peakList
     */
    private List<Peak2D> createPeaklist(final List<PeakArea2D> pas,
            final Tuple2D<Array, Array> times) {
        final List<Peak2D> peaklist = new ArrayList<>();
        PeakArea2D s;
        Peak2D peak;
        double x, y;
        int stepSize = pas.size() / 10;
        for (int i = 0; i < pas.size(); i++) {
            if (i % stepSize == 0 || i == pas.size() - 1) {
                if (log.isDebugEnabled()) {
                    log.debug("{}%", (int) (100.0f * ((float) (i + 1) / (float) pas.size())));
                }
            }
            s = pas.get(i);
            x = times.getFirst().getDouble(s.getIndex());
            y = times.getSecond().getDouble(s.getIndex());
            peak = Peak2D.builder2D().
                peakArea(s).
                firstRetTime(x).
                secondRetTime(y).
                startIndex(s.getIndex()).
                apexIndex(s.getIndex()).
                stopIndex(s.getIndex()).
            build();
            peaklist.add(peak);
        }

        return peaklist;
    }

    /**
     * Adds additional information to the peaklist ps such as integration on
     * special mzs and identification by db search.
     *
     * @param ps
     * @param ff
     * @return
     */
    private List<Peak2D> addAdditionalInformation(final List<Peak2D> ps,
            final IFileFragment ff) {
        final List<Array> tic = getIntensities(ff);
        Peak2D peak;
        log.info("Adding additional Information");
        int stepSize = ps.size() / 10;
        for (int i = 0; i < ps.size(); i++) {
            if (log.isDebugEnabled()) {
                log.debug("{}%", (int) (100.0f * ((float) (i + 1) / (float) ps.size())));
            }
            peak = ps.get(i);
            if (this.doIntegration) {
                this.integration.integrate(peak, ff, tic, getWorkflow());
            }
        }
        return ps;
    }

    /**
     * Index map.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return index
     */
    private int idx(final int x, final int y) {
        return x * this.scansPerModulation + y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Will do an initial peak finding and computes the 'snakes'";
    }

    /**
     * Getter.
     *
     * @param ff file fragment
     * @return first and second retentiontime
     * @throws ResourceNotAvailableException
     */
    private Tuple2D<Array, Array> getRetentionTimes(
            final IFileFragment ff) {
        final Array firstRetTime = ff.getChild(
                this.first_column_elution_time).getArray();
        final Array secondRetTime = ff.getChild(this.second_column_elution_time).getArray();
        final Tuple2D<Array, Array> times = new Tuple2D<>(
                firstRetTime, secondRetTime);
        return times;
    }

    /**
     * Getter.
     *
     * @param chrom chromatogram
     * @return first and second retentiontime
     * @throws ResourceNotAvailableException
     */
    private Tuple2D<Array, Array> getImageAxisRetentionTimes(
            final IChromatogram2D chrom) {
        Rectangle2D bounds2D = chrom.getTimeRange2D();
        float[] firstRetTime = new float[chrom.getNumberOfModulations()];
        float[] secondRetTime = new float[chrom.getNumberOfScansPerModulation()];
        firstRetTime[0] = (float)bounds2D.getMinX();
        secondRetTime[0] =(float) bounds2D.getMinY();
        for (int i = 1; i < firstRetTime.length; i++) {
            firstRetTime[i] = firstRetTime[i - 1] + (float)chrom.getModulationDuration();
        }
        for (int i = 1; i < secondRetTime.length; i++) {
            secondRetTime[i] = secondRetTime[i - 1] + (float)(chrom.getModulationDuration() / (float) chrom.getNumberOfScansPerModulation());
        }
        final Tuple2D<Array, Array> times = new Tuple2D<>(
                Array.makeFromJavaArray(firstRetTime), Array.makeFromJavaArray(secondRetTime));
        return times;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.PEAKFINDING;
    }

    /**
     * ATTENTION: NOT FIXED YET. METHOD WILL NOT DO WHAT IT SHOULD
     *
     * @param ps
     * @param ff
     */
    @SuppressWarnings("unused")
    private void saveFragment(final List<Peak2D> ps, final IFileFragment ff) {
        final Vector<Integer> region = new Vector<>();
        final Vector<Integer> boundary = new Vector<>();
        final Vector<Integer> regionIndex = new Vector<>();
        final Vector<Integer> boundaryIndex = new Vector<>();
        final Collection<String> peakNames = new ArrayList<>();

        Peak2D peak;

        for (Peak2D p1 : ps) {
            peak = p1;
            regionIndex.add(region.size());
            for (final Point p : peak.getPeakArea().getRegionPoints()) {
                region.add(idx(p.x, p.y));
            }
            boundaryIndex.add(boundary.size());
            for (final Point p : peak.getPeakArea().getBoundaryPointsCopy()) {
                boundary.add(idx(p.x, p.y));
            }
            peakNames.add(peak.getName());
        }

        final IVariableFragment regionVar = new VariableFragment(ff,
                this.regionIndexListVar);
        regionVar.setArray(ArrayTools2.createIntegerArray(region));
        final IVariableFragment regionIndexVar = new VariableFragment(ff,
                this.regionPeakIndexVar);
        regionIndexVar.setArray(ArrayTools2.createIntegerArray(regionIndex));
        final IVariableFragment boundaryVar = new VariableFragment(ff,
                this.boundaryIndexListVar);
        boundaryVar.setArray(ArrayTools2.createIntegerArray(boundary));
        final IVariableFragment boundaryIndexVar = new VariableFragment(ff,
                this.boundaryPeakIndexVar);
        boundaryIndexVar.setArray(ArrayTools2.createIntegerArray(boundaryIndex));
        FragmentTools.createStringArray(ff, "peak_names", peakNames);
    }

    /**
     * Saves all information about peaks and the peakarea.
     *
     * @param chrom 2D chromatogramg
     * @param fret returning file fragment
     * @param peakAreaList peak area list
     * @param colorRamp color ramp
     * @return peak list
     */
    private List<Peak2D> savePeaks(final IChromatogram2D chrom,
            final IFileFragment fret, final List<Peak2D> peaklist,
            final int[][] colorRamp) {
        log.info("Saving areas");
        final ArrayInt.D1 peakindex = new ArrayInt.D1(peaklist.size(), false);
        final IndexIterator iter = peakindex.getIndexIterator();
        for (final Peak2D pa : peaklist) {
            iter.setIntNext(idx(pa.getPeakArea().getSeedPoint().x, pa.getPeakArea().getSeedPoint().y));
        }
        final IFileFragment ff = chrom.getParent();
        final IVariableFragment var = new VariableFragment(fret,
                this.peakListVar);
        var.setArray(peakindex);

        log.info("Saving peaks");
        this.peakExporter.exportPeakInformation(StringTools.removeFileExt(ff.getName()), peaklist);
        if (this.doIntegration) {
            this.peakExporter.exportDetailedPeakInformation(StringTools.removeFileExt(ff.getName()), peaklist);
        }
        this.peakExporter.exportPeaksToMSP(StringTools.removeFileExt(
                ff.getName())
                + "-peaks.msp", peaklist);
        createImage(ff, peaklist, colorRamp, getImageAxisRetentionTimes(chrom));
        return peaklist;
    }

    private List<Array> getIntensities(IFileFragment ff) {
        IVariableFragment ticVar = ff.getChild(this.totalIntensityVar, true);
        IVariableFragment sciv = ff.getChild(this.secondScanIndexVar);
        ticVar.setIndex(sciv);
        return CachedList.getList(getWorkflow().getFactory(), ticVar);
    }

    private void createImage(final IFileFragment ff,
            final List<Peak2D> peakList, final int[][] colorRamp,
            final Tuple2D<Array, Array> times) {

        List<Array> intensities = getIntensities(ff);
        // FIXME: should not be static!
        if (!intensities.isEmpty()) {
//            intensities = intensities.subList(0, intensities.size() - 2);
            final BufferedImage biBoundary = ImageTools.create2DImage(ff.getName(),
                    intensities, this.scansPerModulation, this.doubleFillValue,
                    this.threshold, colorRamp, this.getClass());
            log.info("Peak boundary image: {}x{}", biBoundary.getWidth(), biBoundary.getHeight());
            createAndSaveImage(ImageTools.addPeakToImage(biBoundary,
                    peakList, new int[]{0, 0, 0, 255}, null, null,
                    this.scansPerModulation), StringTools.removeFileExt(ff.getName())
                    + "_seeds", "chromatogram with peak seeds", peakList, times);
            createAndSaveImage(ImageTools.addPeakToImage(biBoundary, peakList,
                    new int[]{255, 255, 255, 255}, null, new int[]{0, 0, 0,
                        255,}, this.scansPerModulation),
                    StringTools.removeFileExt(ff.getName())
                    + "_boundary", "chromatogram with peak boundaries", peakList,
                    times);
        } else {
            log.warn("Intensity array list was empty for fragment {}", ff.getName());
        }
    }
}
