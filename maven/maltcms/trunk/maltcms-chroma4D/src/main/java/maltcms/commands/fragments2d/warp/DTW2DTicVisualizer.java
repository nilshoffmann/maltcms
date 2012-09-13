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
package maltcms.commands.fragments2d.warp;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import maltcms.commands.fragments2d.testing.Visualization2D;
import maltcms.commands.fragments2d.warp.visualization.IVisualization;
import maltcms.tools.ImageTools;
import maltcms.tools.MaltcmsTools;
import maltcms.tools.PathTools;
import maltcms.ui.charts.EPlotRunner;
import maltcms.ui.charts.PlotRunner;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.JFreeChart;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default visualization pipeline command.
 *
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Slf4j
@Data
@RequiresVariables(names = {"var.scan_acquisition_time_1d",
    "var.modulation_time", "var.scan_rate",
    // "var.warp_path_i", "var.warp_path_j",
    "var.second_column_time", "var.second_column_scan_index",
    "var.total_intensity"})
@RequiresOptionalVariables(names = {"var.v_total_intensity"})
@ServiceProvider(service = AFragmentCommand.class)
public class DTW2DTicVisualizer extends AFragmentCommand {

    @Configurable(name = "var.warp_path_i", value = "warp_path_i")
    private String warpPathi = "warp_path_i";
    @Configurable(name = "var.warp_path_j", value = "warp_path_j")
    private String warpPathj = "warp_path_j";
    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String totalIntensity = "total_intensity";
    @Configurable(name = "var.scan_acquisition_time_1d",
    value = "scan_acquisition_time_1d")
    private String scanAcquTime = "scan_acquisition_time_1d";
    @Configurable(name = "var.modulation_time", value = "modulation_time")
    private String modulationVar = "modulation_time";
    @Configurable(name = "var.scan_rate", value = "scan_rate")
    private String scanRateVar = "scan_rate";
    @Configurable(name = "var.second_column_scan_index",
    value = "second_column_scan_index")
    private String secondColumnScanIndexVar = "second_column_scan_index";
    @Configurable(name = "var.second_column_time", value = "second_column_time")
    private String secondColumnTimeVar = "second_column_time";
    private int scanspermodulation = -1;
    @Configurable(name = "maltcms.ui.charts.PlotRunner.serializeJFreeChart",
    value = "true")
    private boolean createSerialized = true;
    @Configurable(name = "maltcms.ui.charts.PlotRunner.filetype", value = "png")
    private final String format = "png";
    @Configurable(
    value = "maltcms.commands.fragments2d.warp.visualization.Default2DTWVisualizer")
    private String visualizerClass = "maltcms.commands.fragments2d.warp.visualization.Default2DTWVisualizer";
    private IVisualization visualizer;

    /**
     * {@inheritDoc}
     */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        // final TupleND<IFileFragment> alignmenttuple = new
        // TupleND<IFileFragment>();
        IFileFragment pwHorizontalAlignmentFragment = null, pwVerticalAlignmentFragment = null;
        try {
            pwHorizontalAlignmentFragment = MaltcmsTools.
                    getPairwiseDistanceFragment(t, "-horizontal");
            log.info("horizontal pairwise distance file found");
        } catch (final ResourceNotAvailableException e) {
            log.error(e.getMessage());
            pwHorizontalAlignmentFragment = null;
        }
        // FIXME: findet unterschiedliche distance elemente nicht mehr!
        // try {
        // pwVerticalAlignmentFragment = MaltcmsTools
        // .getPairwiseDistanceFragment(t, "-vertical");
        // log.info("vertical pairwise distance file found");
        // } catch (final ResourceNotAvailableException e) {
        // log.error(e.getMessage());
        // pwVerticalAlignmentFragment = null;
        // }

        if (pwHorizontalAlignmentFragment == null
                && pwVerticalAlignmentFragment == null) {
            log.error("Cant find horizontal or vertical pairwise distance"
                    + " fragment. Trying to find normal distance"
                    + " fragment. Assuming horizontal warping.");
            try {
                pwHorizontalAlignmentFragment = MaltcmsTools.
                        getPairwiseDistanceFragment(t);
            } catch (final ResourceNotAvailableException e) {
                log.error(e.getMessage());
                log.error("Aborting visualization.");
                return t;
            }
        }

        IFileFragment alignmentHorizontal, alignmentVertical;
        Array warpiH, warpjH, warpiV, warpjV;
        final Visualization2D vis2d = new Visualization2D();
        List<Point> pathH, pathV;
        BufferedImage image;
        for (int i = 0; i < t.size(); i++) {
            final IFileFragment ref = t.get(i);
            for (int j = i + 1; j < t.size(); j++) {
                final IFileFragment query = t.get(j);

                log.info("Visualization for {},{}", ref.getName(),
                        query.getName());
                // log.info("i: {}({})", ref.getName());
                // log.info("j: {}", query.getName());

                final Index idx = Index.scalarIndexImmutable;
                final Double modulationi = ref.getChild(this.modulationVar).
                        getArray().getDouble(idx);
                final Double modulationj = query.getChild(this.modulationVar).
                        getArray().getDouble(idx);
                final Integer scanRatei = ref.getChild(this.scanRateVar).
                        getArray().getInt(idx);
                final Integer scanRatej = query.getChild(this.scanRateVar).
                        getArray().getInt(idx);

                if ((modulationi.intValue() == modulationj.intValue())
                        && (scanRatei.intValue() == scanRatej.intValue())) {

                    this.scanspermodulation = modulationi.intValue()
                            * scanRatei.intValue();
                    log.info("Using {} for TICs", this.totalIntensity);
                    final List<Array> scanlinesi = getScanlineFor(ref,
                            modulationi.intValue() * scanRatei.intValue());
                    final List<Array> scanlinesj = getScanlineFor(query,
                            modulationj.intValue() * scanRatej.intValue());

                    log.info("scanlines size i: {}", scanlinesi.size());
                    log.info("scanlines size j: {}", scanlinesj.size());

                    if (pwHorizontalAlignmentFragment != null) {
                        alignmentHorizontal = MaltcmsTools.getPairwiseAlignment(
                                pwHorizontalAlignmentFragment, ref,
                                query);
                        // alignmenttuple.add(alignmentHorizontal);
                        log.info("{}", alignmentHorizontal.getAbsolutePath());

                        warpiH = alignmentHorizontal.getChild(this.warpPathi).
                                getArray();
                        warpjH = alignmentHorizontal.getChild(this.warpPathj).
                                getArray();
                        pathH = PathTools.pointListFromArrays(warpiH, warpjH);
                    } else {
                        pathH = null;
                    }

                    if (pwVerticalAlignmentFragment != null) {
                        alignmentVertical = MaltcmsTools.getPairwiseAlignment(
                                pwVerticalAlignmentFragment, ref, query);
                        // alignmenttuple.add(alignmentVertical);
                        log.info("{}", alignmentVertical.getAbsolutePath());

                        warpiV = alignmentVertical.getChild(this.warpPathi).
                                getArray();
                        warpjV = alignmentVertical.getChild(this.warpPathj).
                                getArray();
                        pathV = PathTools.pointListFromArrays(warpiV, warpjV);
                    } else {
                        pathV = null;
                    }

                    log.info("	Creating image");
                    // final BufferedImage image = this.visualizer.createImage(
                    // scanlinesi, scanlinesj, warpi, warpj);
                    image = vis2d.createImage(scanlinesi, scanlinesj, pathH,
                            pathV);

                    final String baseFilename = StringTools.removeFileExt(ref.
                            getName())
                            + "_vs_"
                            + StringTools.removeFileExt(query.getName());
                    final String filename = baseFilename + "_rgb";
                    // final File out =
                    ImageTools.saveImage(image, filename, this.format,
                            getWorkflow().getOutputDirectory(this), this);

                    if (this.createSerialized) {
                        // createChart(ref, query, filename, warpi, warpj,
                        // modulationi.intValue() * scanRatei.intValue(),
                        // out);
                    } else {
                        log.info(
                                "If you want to create a serialized Plot change the "
                                + "maltcms.ui.charts.PlotRunner.serializeJFreeChart option to true.");
                    }
                } else {
                    // FIXME: isnt the visualization independent from modulation
                    // and scan rate?
                    log.error(
                            "Could not visualize time warp for {} vs {}.", ref.
                            getName(), query.getName());
                    log.error("Different scanRates or different modulations.");
                    log.error(ref.getName() + " modulation{} scanRate{}",
                            modulationi, scanRatei);
                    log.error(
                            query.getName() + " modulation{} scanRate{}",
                            modulationj, scanRatej);
                    System.out.println(scanRatei + "-" + scanRatej);
                    System.out.println(modulationi + "-" + modulationj);
                }
            }
        }
        return t;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        this.warpPathi = cfg.getString("var.warp_path_i", "warp_path_i");
        this.warpPathj = cfg.getString("var.warp_path_j", "warp_path_j");
        this.totalIntensity = cfg.getString(this.getClass().getName()
                + ".total_intensity", "total_intensity");
        this.scanAcquTime = cfg.getString("var.scan_acquisition_time_1d",
                "scan_acquisition_time_1d");
        this.createSerialized = cfg.getBoolean(
                "maltcms.ui.charts.PlotRunner.serializeJFreeChart", true);
        this.modulationVar = cfg.getString("var.modulation_time",
                "modulation_time");
        this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
        this.secondColumnTimeVar = cfg.getString("var.second_column_time",
                "second_column_time");
        this.secondColumnScanIndexVar = cfg.getString(
                "var.second_column_scan_index", "second_column_scan_index");

        this.visualizerClass = cfg.getString(
                this.getClass().getName() + ".visualizerClass",
                "maltcms.commands.fragments2d.warp.visualization.Default2DTWVisualizer");
        this.visualizer = Factory.getInstance().getObjectFactory().instantiate(
                this.visualizerClass, IVisualization.class);
    }

    /**
     * Create a serialized chart.
     *
     * @param ref reference file fragment
     * @param query query file fragment
     * @param filename background image
     * @param warpi warp path i
     * @param warpj warp path j
     * @param spm scans per modulation
     * @param in infile for chart background image
     */
    private void createChart(final IFileFragment ref,
            final IFileFragment query, final String filename,
            final Array warpi, final Array warpj, final int spm, final File in) {
        log.info("Creating a serialized Plot.");
        final ArrayDouble.D1 ret1 = (ArrayDouble.D1) ref.getChild(
                this.scanAcquTime).getArray();
        final ArrayDouble.D1 ret2 = (ArrayDouble.D1) query.getChild(
                this.scanAcquTime).getArray();
        log.info("Using file {} for AChart", in.getAbsolutePath());

        ref.getChild(this.secondColumnTimeVar).setIndex(
                ref.getChild(this.secondColumnScanIndexVar));
        final ArrayDouble.D1 secondrettime = (ArrayDouble.D1) ref.getChild(
                this.secondColumnTimeVar).getIndexedArray().get(0);
        final JFreeChart chart = this.visualizer.createChart(
                in.getAbsolutePath(), ref.getName(), query.getName(),
                new Tuple2D<ArrayDouble.D1, ArrayDouble.D1>(ret1,
                secondrettime),
                new Tuple2D<ArrayDouble.D1, ArrayDouble.D1>(ret2,
                secondrettime));
        if (chart != null) {
            this.visualizer.addPeakMarker(chart, new Tuple2D<Array, Array>(
                    warpi, warpj), ref, query,
                    new Tuple2D<ArrayDouble.D1, ArrayDouble.D1>(ret1,
                    secondrettime),
                    new Tuple2D<ArrayDouble.D1, ArrayDouble.D1>(ret2,
                    secondrettime), this.scanspermodulation);
            saveChart(chart, ref, query);
        } else {
            log.info("null chart.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Creates an image containing both chromatograms in different"
                + " color channels of the image using the warp path. Optionaly"
                + " it will create a serialized XYBPlot";
    }

    /**
     * Getter.
     *
     * @param ff file fragment
     * @param spm scans per modulation
     * @return scanlines
     */
    protected List<Array> getScanlineFor(final IFileFragment ff, final int spm) {
        setBinSize(256);
        normalize(true);
        ff.getChild(this.totalIntensity).setIndex(
                ff.getChild(this.secondColumnScanIndexVar));
        final List<Array> scanlines = ff.getChild(this.totalIntensity).
                getIndexedArray();
        return scanlines;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.VISUALIZATION;
    }

    /**
     * Setter.
     *
     * @param normalize normalize data from warppath
     */
    protected void normalize(final boolean normalize) {
        this.visualizer.setNormalize(normalize);
    }

    /**
     * Will the generated Chart.
     *
     * @param chart chart
     * @param ref reference file fragment
     * @param query query file fragment
     */
    protected void saveChart(final JFreeChart chart, final IFileFragment ref,
            final IFileFragment query) {
        final PlotRunner pl = new EPlotRunner(chart,
                StringTools.removeFileExt(ref.getName())
                + "_vs_" + StringTools.removeFileExt(query.getName()) + "-D",
                getWorkflow().getOutputDirectory(this));
        pl.configure(Factory.getInstance().getConfiguration());
        final File f = pl.getFile();
        final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f, this,
                WorkflowSlot.VISUALIZATION, ref, query);
        getWorkflow().append(dwr);
        Factory.getInstance().submitJob(pl);
    }

    /**
     * Setter.
     *
     * @param binSize bin size for the visualizer
     */
    protected void setBinSize(final int binSize) {
        this.visualizer.setBinSize(binSize);
    }
}
