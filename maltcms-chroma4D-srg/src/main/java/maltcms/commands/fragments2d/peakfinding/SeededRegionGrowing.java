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

import maltcms.commands.fragments2d.peakfinding.srg.PeakSeparator;
import maltcms.commands.fragments2d.peakfinding.comparator.PeakComparator;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments2d.peakfinding.output.IPeakExporter;
import maltcms.commands.fragments2d.peakfinding.output.IPeakIntegration;
import maltcms.commands.fragments2d.peakfinding.picking.IPeakPicking;
import maltcms.commands.fragments2d.peakfinding.srg.IRegionGrowing;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.caches.ScanLineCacheFactory;
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
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;

/**
 * Peakpicking + integration + identification + normalization + evaluation...
 *
 * @author Mathias Wilhelm
 * 
 */
@Slf4j
@Data
@RequiresVariables(names = {"var.total_intensity", "var.scan_rate",
    "var.modulation_time", "var.second_column_scan_index",
    "var.scan_acquisition_time_1d"})
@RequiresOptionalVariables(names = {"var.v_total_intensity", "var.tic_peaks"})
@ProvidesVariables(names = {"var.peak_index_list", "var.region_index_list",
    "var.region_peak_index", "var.boundary_index_list",
    "var.boundary_peak_index", "var.peak_mass_intensity"})
@ServiceProvider(service=AFragmentCommand.class)
public class SeededRegionGrowing extends AFragmentCommand {

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
    @Configurable(name = "var.scan_acquisition_time_1d",
            value = "scan_acquisition_time_1d", type = String.class)
    private String scanAcquTime1DVar = "scan_acquisition_time_1d";
    @Configurable(name = "var.second_column_time", value = "second_column_time",
            type = String.class)
    private final String secondColumnTimeVar = "second_column_time";
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
            type = String.class)
    private final String format = "png";
    @Configurable(name = "images.colorramp", value = "res/colorRamps/bcgyr.csv",
            type = String.class)
    private String colorrampLocation = "res/colorRamps/bcgyr.csv";
    @Configurable(name = "ucar.nc2.NetcdfFile.fillValueDouble",
            value = "9.9692099683868690e+36d", type = double.class)
    private double doubleFillValue;
    @Configurable(name = "images.thresholdLow", value = "0", type = double.class)
    private double threshold = 0;
    @Configurable(value = "true", type = boolean.class)
    private boolean separate = true;
    @Configurable(value = "false", type = boolean.class)
    private boolean doNormalization = false;
    @Configurable(value = "false", type = boolean.class)
    private boolean doIntegration = false;
    private int scansPerModulation = 0;
    @Configurable
    private IPeakPicking peakPicking;
    @Configurable
    private IRegionGrowing regionGrowing;
    @Configurable
    private IPeakIntegration integration;
    @Configurable
    private IPeakExporter peakExporter;
    @Configurable
    private PeakSeparator peakSeparator = new PeakSeparator();
    private List<List<Peak2D>> peakLists = new ArrayList<>();

    /** {@inheritDoc} */
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
            peaklist = runSRG(ff, null);
            Collections.sort(peaklist, new PeakComparator());
            for (int j = 0; j < peaklist.size(); j++) {
                peaklist.get(j).setIndex(j);
            }
            this.peakLists.add(peaklist);
        }
        for (int i = 0; i < t.size(); i++) {
            addAdditionalInformation(this.peakLists.get(i), t.get(i));
        }
        log.info("Saving all Peaks");
        // exporting peak lists
        for (int i = 0; i < t.size(); i++) {
            final IFileFragment fret = new FileFragment(
                    new File(getWorkflow().getOutputDirectory(this), t.get(i).
                            getName()));
            fret.addSourceFile(t.get(i));
            savePeaks(t.get(i), fret, this.peakLists.get(i), colorRamp);

            final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
                    fret.getUri(), this, getWorkflowSlot(),
                    t.get(i));
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
     * @param ff file fragement to generate the {@link IScanLine}
     * @param seeds initial seeds. If this parameter is <code>null</code> then
     * the {@link IPeakPicking} class will be used to determine seeds.
     * @return List of resulting peaks
     */
    private List<Peak2D> runSRG(IFileFragment ff, List<Point> seeds) {
        final double scanRate = ff.getChild(this.scanRateVar).getArray().getDouble(
                Index.scalarIndexImmutable);
        final double modulationTime = ff.getChild(this.modulationTimeVar).getArray().
                getDouble(Index.scalarIndexImmutable);
        this.scansPerModulation = (int) (scanRate * modulationTime);

        if (seeds == null) {
            log.info("== starting peak finding for " + ff.getName());
            seeds = this.peakPicking.findPeaks(ff);
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
        } else {
            log.info("== restarting peak finding for " + ff.getName());
        }
        log.info("	Found {} potential peaks in {}", seeds.size(), ff.getName());

        final IScanLine slc = ScanLineCacheFactory.getSparseScanLineCache(ff);
        log.info("Computing areas with {}", slc.getClass().getSimpleName());
//		this.scanLineCount = slc.getScanLineCount();
        long start = System.currentTimeMillis();
        final List<PeakArea2D> peakAreaList = this.regionGrowing.getAreasFor(
                seeds, ff, slc);

        log.info("Integration: {} ms", System.currentTimeMillis()
                - start);

        if (this.separate) {
            this.peakSeparator.startSeparationFor(peakAreaList, slc, getRetentionTime(ff));
        }

        slc.clear();

        final List<Peak2D> peaklist = createPeaklist(peakAreaList,
                getRetentionTime(ff));
        return peaklist;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        this.totalIntensityVar = cfg.getString(this.getClass().getName()
                + ".totalIntensityVar", "total_intensity");
        this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
        this.modulationTimeVar = cfg.getString("var.modulation_time",
                "modulation_time");
        this.secondScanIndexVar = cfg.getString("var.second_column_scan_index",
                "second_column_scan_index");
        this.scanAcquTime1DVar = cfg.getString("var.scan_acquisition_time_1d",
                "scan_acquisition_time_1d");
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
            final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> times) {
        ImageTools.saveImage(image, name, this.format, getWorkflow().
                getOutputDirectory(this), this);
        // ImageTools.saveImage(image, name + "_emtpy", this.format,
        // getIWorkflow().getOutputDirectory(this), this);

        final File d = getWorkflow().getOutputDirectory(this);
        final File out = new File(d, name + "." + this.format);
        log.info("Using file {} for AChart", out.getAbsolutePath());
        final AChart<XYBPlot> chart = new BHeatMapChart(out.getAbsolutePath(),
                "first retention time[min]", "second retention time[s]", times,
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
        final PlotRunner pl = new PlotRunner(plot, title, name + "_plot", d);
        pl.configure(Factory.getInstance().getConfiguration());
        Factory.getInstance().submitJob(pl);
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
            final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> times) {
        final List<Peak2D> peaklist = new ArrayList<>();
        PeakArea2D s;
        Peak2D peak;
        double x, y;
        for (int i = 0; i < pas.size(); i++) {
            if (i % 10 == 0) {
                log.info("	Did " + i);
            }
            s = pas.get(i);
            peak = new Peak2D();
            x = times.getFirst().get(s.getSeedPoint().x);
            y = times.getSecond().get(s.getSeedPoint().y);
            peak.setPeakArea(s);
            peak.setFirstRetTime(x);
            peak.setSecondRetTime(y);
            peak.setScanIndex(s.getIndex());

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
        for (int i = 0; i < ps.size(); i++) {
            if (i % 10 == 0) {
                log.info("	Did " + i);
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

    /** {@inheritDoc} */
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
    private Tuple2D<ArrayDouble.D1, ArrayDouble.D1> getRetentionTime(
            final IFileFragment ff) {
        final ArrayDouble.D1 firstRetTime = (ArrayDouble.D1) ff.getChild(
                this.scanAcquTime1DVar).getArray();
        IVariableFragment sctv = ff.getChild(this.secondColumnTimeVar);
        final ArrayDouble.D1 secondRetTime;
        try {
            secondRetTime = (ArrayDouble.D1) sctv.getArray().section(new int[]{0}, new int[]{this.scansPerModulation});
        } catch (InvalidRangeException ex) {
            throw new ResourceNotAvailableException("Invalid range while subsetting variable " + this.secondColumnTimeVar + " on file " + ff.getName(), ex);
        }
        final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> times = new Tuple2D<>(
                firstRetTime, secondRetTime);
        return times;
    }

    /** {@inheritDoc} */
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
     * @param ff file fragment
     * @param fret returning file fragment
     * @param peakAreaList peak area list
     * @param colorRamp color ramp
     * @return peak list
     */
    private List<Peak2D> savePeaks(final IFileFragment ff,
            final IFileFragment fret, final List<Peak2D> peaklist,
            final int[][] colorRamp) {
        log.info("Saving areas");
        final ArrayInt.D1 peakindex = new ArrayInt.D1(peaklist.size());
        final IndexIterator iter = peakindex.getIndexIterator();
        for (final Peak2D pa : peaklist) {
            iter.setIntNext(idx(pa.getPeakArea().getSeedPoint().x, pa.getPeakArea().getSeedPoint().y));
        }

        final IVariableFragment var = new VariableFragment(fret,
                this.peakListVar);
        var.setArray(peakindex);

        log.info("Saving peaks");
        this.peakExporter.exportPeakInformation(StringTools.removeFileExt(ff.getName()), peaklist);
        this.peakExporter.exportPeakNames(peaklist,
                StringTools.removeFileExt(ff.getName()));
        if (this.doIntegration) {
            this.peakExporter.exportDetailedPeakInformation(StringTools.removeFileExt(ff.getName()), peaklist);
        }
        IScanLine isl = ScanLineCacheFactory.getSparseScanLineCache(ff);
        this.peakExporter.exportPeaksToMSP(StringTools.removeFileExt(
                ff.getName())
                + "-peaks.msp", peaklist, isl);
        isl.clear();
        createImage(ff, peaklist, colorRamp, getRetentionTime(ff));
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
            final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> times) {

        List<Array> intensities = getIntensities(ff);
        // FIXME: should not be static!
        if (!intensities.isEmpty()) {
            intensities = intensities.subList(0, intensities.size() - 2);
            final BufferedImage biBoundary = ImageTools.create2DImage(ff.getName(),
                    intensities, this.scansPerModulation, this.doubleFillValue,
                    this.threshold, colorRamp, this.getClass());
            // log.info("PEAK AREA SIZE: {}", peakAreaList.size());
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
