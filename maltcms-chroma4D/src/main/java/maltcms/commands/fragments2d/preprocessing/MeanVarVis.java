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
package maltcms.commands.fragments2d.preprocessing;

import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments2d.tools.ArrayTools;
import maltcms.io.csv.ColorRampReader;
import maltcms.tools.ArrayTools2;
import maltcms.tools.ImageTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.MassSpectrumPlot;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYChart;
import org.apache.commons.configuration.Configuration;
import org.jfree.chart.plot.XYPlot;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;

/**
 * This class will visualize the computed mean, variance and standard deviation
 * of every chromatogram.
 *
 * @author Mathias Wilhelm
 */
@Slf4j
@Data
@RequiresVariables(names = {"var.mean_ms_intensity", "var.var_ms_intensity",
    "var.sd_ms_intensity", "var.v_total_intensity_1d",
    "var.meanms_1d_horizontal_index", "var.meanms_1d_horizontal",
    "var.meanms_1d_vertical_index", "var.meanms_1d_vertical",
    "var.maxms_1d_horizontal_index", "var.maxms_1d_horizontal",
    "var.used_mass_values", "var.maxms_1d_vertical_index",
    "var.maxms_1d_vertical", "var.total_intensity_1d",
    "var.scan_acquisition_time_1d"})
@ServiceProvider(service = AFragmentCommand.class)
public class MeanVarVis extends AFragmentCommand {

    @Configurable(name = "var.var_intensity_values", value = "var_intensity_values")
    private String varMSIntensityVar = "var_intensity_values";
    @Configurable(name = "var.sd_intensity_values", value = "sd_intensity_values")
    private String sdMSIntensityVar = "sd_intensity_values";
    @Configurable(name = "var.mean_intensity_values", value = "mean_intensity_values")
    private String meanMSIntensityVar = "mean_intensity_values";
    @Configurable(name = "var.total_intensity_1d", value = "total_intensity_1d")
    private String totalIntensity1dVar = "total_intensity_1d";
    @Configurable(name = "var.v_total_intensity_1d", value = "v_total_intensity_1d")
    private String vtotalIntensity1DVar = "v_total_intensity_1d";
    @Configurable(name = "var.scan_acquisition_1d", value = "scan_acquisition_1d")
    private String scanAcquisitionTime1dVar = "scan_acquisition_1d";
    @Configurable(name = "var.meanms_1d_horizontal", value = "meanms_1d_horizontal")
    private String meanMSHorizontalVar = "meanms_1d_horizontal";
    @Configurable(name = "var.meanms_1d_horizontal_index", value = "meanms_1d_horizontal_index")
    private String meanMSHorizontalIndexVar = "meanms_1d_horizontal_index";
    @Configurable(name = "var.meanms_1d_vertical", value = "meanms_1d_vertical")
    private String meanMSVerticalVar = "meanms_1d_vertical";
    @Configurable(name = "var.meanms_1d_vertical_index", value = "meanms_1d_vertical_index")
    private String meanMSVerticalIndexVar = "meanms_1d_vertical_index";
    @Configurable(name = "var.maxms_1d_horizontal", value = "maxms_1d_horizontal")
    private String maxMSHorizontalVar = "maxms_1d_horizontal";
    @Configurable(name = "var.maxms_1d_horizontal_index", value = "maxms_1d_horizontal_index")
    private String maxMSHorizontalIndexVar = "maxms_1d_horizontal_index";
    @Configurable(name = "var.maxms_1d_vertical", value = "maxms_1d_vertical")
    private String maxMSVerticalVar = "maxms_1d_vertical";
    @Configurable(name = "var.maxms_1d_vertical_index", value = "maxms_1d_vertical_index")
    private String maxMSVerticalIndexVar = "maxms_1d_vertical_index";
    @Configurable(name = "var.used_mass_values", value = "used_mass_values")
    private String usedMassValuesVar = "used_mass_values";
    @Configurable(name = "maltcms.ui.charts.PlotRunner.filetype", value = "png")
    private String format = "png";
    @Configurable(name = "images.colorramp", value = "res/colorRamps/bcgyr.csv")
    private String colorrampLocation = "res/colorRamps/bcgyr.csv";
    @Configurable(name = "images.thresholdLow", value = "0")
    private double lowThreshold = 0.0d;
    @Configurable(value = "false")
    private boolean differentVisualizations = false;
    @Configurable(value = "false")
    private boolean useLogScale = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        for (final IFileFragment ff : t) {
            final Array mean = ff.getChild(this.meanMSIntensityVar).getArray();
            final Array var = ff.getChild(this.varMSIntensityVar).getArray();
            final Array sd = ff.getChild(this.sdMSIntensityVar).getArray();

            createChartXY(mean, "Mean mz bin signal", StringTools.removeFileExt(ff.getName())
                + "_mean", this.useLogScale, ff);
            createChartXY(var, "Variance in mz bins", StringTools.removeFileExt(ff.getName())
                + "_variance", this.useLogScale, ff);
            createChartXY(sd, "Standard deviation of mz bins", StringTools.removeFileExt(ff.getName())
                + "_standardDeviation", this.useLogScale, ff);
            boolean visualize = true;
            double[] quantil = ArrayTools.getQuantileValue(ff, sd, new double[]{0.001, 0.002,
                0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.5}, visualize, this);

            // 1D TIC VIS
            final Array tic1do = ff.getChild(this.totalIntensity1dVar).getArray();
            Array tic1dsd = ff.getChild(this.vtotalIntensity1DVar).getArray();
            final Array rettime = ff.getChild(this.scanAcquisitionTime1dVar).getArray();

            if (rettime.getShape()[0] < tic1dsd.getShape()[0]) {
                final Array tmp2 = new ArrayDouble.D1(rettime.getShape()[0]);
                final IndexIterator iterTmp2 = tmp2.getIndexIterator();
                final IndexIterator ret = rettime.getIndexIterator();
                final IndexIterator sditer = tic1dsd.getIndexIterator();
                while (ret.hasNext()) {
                    ret.getDoubleNext();
                    iterTmp2.setDoubleNext(sditer.getDoubleNext());
                }
                tic1dsd = tmp2;
            }

            String logarithmic = "";
            if (this.useLogScale) {
                logarithmic = "(log scale) ";
            }

            final AChart<XYPlot> xyc = new XYChart(
                "1D Visualization of 1D TIC", new String[]{
                    this.totalIntensity1dVar,
                    this.vtotalIntensity1DVar,}, new Array[]{tic1do,
                    tic1dsd,}, new Array[]{rettime},
                "first retention time [s]", "intensity " + logarithmic
                + "[TIC]", this.useLogScale);
            final PlotRunner pr = new PlotRunner(xyc.create(), "Plot of "
                + "1D_TIC", StringTools.removeFileExt(ff.getName()) + "_"
                + "1d_tic", getWorkflow().getOutputDirectory(this));
            pr.configure(Factory.getInstance().getConfiguration());
            final File f = pr.getFile();
            final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f,
                this, getWorkflowSlot(), ff);
            getWorkflow().append(dwr);
            Factory.getInstance().submitJob(pr);

            meanMaxMSVisualization(ff);
        }
        return t;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        this.meanMSIntensityVar = cfg.getString("var.mean_ms_intensity",
            "mean_intensity_values");
        this.varMSIntensityVar = cfg.getString("var.var_ms_intensity",
            "var_intensity_values");
        this.sdMSIntensityVar = cfg.getString("var.sd_ms_intensity",
            "sd_intensity_values");
        this.totalIntensity1dVar = cfg.getString("var.total_intensity_1d",
            "total_intensity_1d");
        this.vtotalIntensity1DVar = cfg.getString("var.v_total_intensity_1d",
            "v_total_intensity_1d");
        this.scanAcquisitionTime1dVar = cfg.getString(
            "var.scan_acquisition_time_1d", "scan_acquisition_time_1d");
        this.meanMSHorizontalVar = cfg.getString("var.meanms_1d_horizontal",
            "meanms_1d_horizontal");
        this.meanMSHorizontalIndexVar = cfg.getString(
            "var.meanms_1d_horizontal_index", "meanms_1d_horizontal_index");
        this.meanMSVerticalVar = cfg.getString("var.meanms_1d_vertical",
            "meanms_1d_vertical");
        this.meanMSVerticalIndexVar = cfg.getString(
            "var.meanms_1d_vertical_index", "meanms_1d_vertical_index");
        this.maxMSHorizontalVar = cfg.getString("var.maxms_1d_horizontal",
            "maxms_1d_horizontal");
        this.maxMSHorizontalIndexVar = cfg.getString(
            "var.maxms_1d_horizontal_index", "maxms_1d_horizontal_index");
        this.maxMSVerticalVar = cfg.getString("var.maxms_1d_vertical",
            "maxms_1d_vertical");
        this.maxMSVerticalIndexVar = cfg.getString(
            "var.maxms_1d_vertical_index", "maxms_1d_vertical_index");
        this.usedMassValuesVar = cfg.getString("var.used_mass_values",
            "used_mass_values");
        this.lowThreshold = cfg.getDouble("images.thresholdLow", 0.0d);
        this.colorrampLocation = cfg.getString("images.colorramp", "res/colorRamps/bcgyr.csv");
        this.format = cfg.getString("maltcms.ui.charts.PlotRunner.filetype", "png");
    }

    /**
     * Visualization of horizontal and vertical mean and max mass spectras.
     *
     * @param ff file fragment
     */
    private void meanMaxMSVisualization(final IFileFragment ff) {
        final List<Integer> sdd = new ArrayList<Integer>();
        final Array sda = ff.getChild(this.usedMassValuesVar).getArray();
        final IndexIterator sdaiter = sda.getIndexIterator();
        while (sdaiter.hasNext()) {
            sdd.add(sdaiter.getIntNext());
        }

        // MEAN MS VIS
        final ColorRampReader crr = new ColorRampReader();
        final int[][] colorRamp = crr.readColorRamp(this.colorrampLocation);

        final IVariableFragment meanH = ff.getChild(this.meanMSHorizontalVar);
        meanH.setIndex(ff.getChild(this.meanMSHorizontalIndexVar));
        final List<Array> aaH = meanH.getIndexedArray();
        // FIXME: not supported with cachedList
        // aaH.remove(aaH.size() - 1);

        final String name = StringTools.removeFileExt(ff.getName());
        createImage(name + "-1D_meanMS_horizontal", ff.getName(), aaH,
            colorRamp, this.lowThreshold, this);
        if (this.differentVisualizations) {
            createImage(name + "-1D_meanMS_horizontal_fsd", ff.getName(),
                ArrayTools2.filterExclude(aaH, sdd), colorRamp,
                this.lowThreshold, this);
            createImage(name + "-1D_meanMS_horizontal_fsdi", ff.getName(),
                ArrayTools2.filterInclude(aaH, sdd), colorRamp,
                this.lowThreshold, this);
            createImage(name + "-1D_meanMS_horizontal_fsdi_sqr", ff.getName(),
                ArrayTools2.filterInclude(ArrayTools2.sqrt(aaH), sdd),
                colorRamp, this.lowThreshold, this);
            createImage(name + "-1D_meanMS_horizontal_fsd_sqr", ff.getName(),
                ArrayTools2.filterExclude(ArrayTools2.sqrt(aaH), sdd),
                colorRamp, this.lowThreshold, this);
            createImage(name + "-1D_meanMS_horizontal_sqr", ff.getName(),
                ArrayTools2.sqrt(aaH), colorRamp, this.lowThreshold, this);
        }

        final IVariableFragment meanV = ff.getChild(this.meanMSVerticalVar);
        meanV.setIndex(ff.getChild(this.meanMSVerticalIndexVar));
        final List<Array> aaV = meanV.getIndexedArray();
        // FIXME: not supported with cachedList
        // aaV.remove(aaV.size() - 1);

        createImage(name + "-1D_meanMS_vertical", ff.getName(), aaV, colorRamp,
            this.lowThreshold, this);
        if (this.differentVisualizations) {
            createImage(name + "-1D_meanMS_vertical_fsd", ff.getName(),
                ArrayTools2.filterExclude(aaV, sdd), colorRamp,
                this.lowThreshold, this);
            createImage(name + "-1D_meanMS_vertical_fsdi", ff.getName(),
                ArrayTools2.filterInclude(aaV, sdd), colorRamp,
                this.lowThreshold, this);
            createImage(name + "-1D_meanMS_vertical_fsdi_sqr", ff.getName(),
                ArrayTools2.filterInclude(ArrayTools2.sqrt(aaV), sdd),
                colorRamp, this.lowThreshold, this);
            createImage(name + "-1D_meanMS_vertical_fsd_sqr", ff.getName(),
                ArrayTools2.filterExclude(ArrayTools2.sqrt(aaV), sdd),
                colorRamp, this.lowThreshold, this);
            createImage(name + "-1D_meanMS_vertical_sqr", ff.getName(),
                ArrayTools2.sqrt(aaV), colorRamp, this.lowThreshold, this);
        }

        // MAX MS VIS
        final IVariableFragment maxH = ff.getChild(this.maxMSHorizontalVar);
        maxH.setIndex(ff.getChild(this.maxMSHorizontalIndexVar));
        final List<Array> maxHA = maxH.getIndexedArray();
        // FIXME: not supported with cachedList
        // maxHA.remove(maxHA.size() - 1);

        createImage(name + "-1D_maxMS_horizontal", ff.getName(), maxHA,
            colorRamp, this.lowThreshold, this);
        if (this.differentVisualizations) {
            createImage(name + "-1D_maxMS_horizontal_sqrt", ff.getName(),
                ArrayTools2.sqrt(maxHA), colorRamp, this.lowThreshold, this);
        }

        final IVariableFragment maxV = ff.getChild(this.maxMSVerticalVar);
        maxV.setIndex(ff.getChild(this.maxMSVerticalIndexVar));
        final List<Array> maxVA = maxV.getIndexedArray();
        // FIXME: not supported with cachedList
        // maxVA.remove(maxVA.size() - 1);

        createImage(name + "-1D_maxMS_vertical", ff.getName(), maxVA,
            colorRamp, this.lowThreshold, this);
        if (this.differentVisualizations) {
            createImage(name + "-1D_maxMS_vertical_sqrt", ff.getName(),
                ArrayTools2.sqrt(maxVA), colorRamp, this.lowThreshold, this);
        }
    }

    /**
     * Creates an chart.
     *
     * @param array    array containing the intensities
     * @param name     titel of this chart
     * @param filename filename
     * @param useLog   use logarithmic scale
     * @param resource IFileFragment as resource for the data
     */
    private void createChartXY(final Array array, final String name,
        final String filename, final boolean useLog, IFileFragment resource) {
        final AChart<XYPlot> plot = new MassSpectrumPlot(name, "", array,
            useLog, false);

        final PlotRunner pl = new PlotRunner(plot.create(), name, filename,
            getWorkflow().getOutputDirectory(this));
        pl.configure(Factory.getInstance().getConfiguration());
        final DefaultWorkflowResult dwr1 = new DefaultWorkflowResult(pl.getFile(), this, this.getWorkflowSlot(), resource);
        this.getWorkflow().append(dwr1);
        Factory.getInstance().submitJob(pl);
    }

    /**
     * Creates an image of a list of arrays.
     *
     * @param filename     filename
     * @param title        title
     * @param aa           list of arrays
     * @param colorRamp    color ramp
     * @param lowThreshold threshold
     * @param elem         workflow element
     */
    private void createImage(final String filename, final String title,
        final List<Array> aa, final int[][] colorRamp,
        final double lowThreshold, final IWorkflowElement elem) {
        final BufferedImage bi2 = maltcms.tools.ImageTools.fullSpectrum(title,
            aa, aa.get(0).getShape()[0], colorRamp, 1024, true,
            lowThreshold);
        ImageTools.saveImage(bi2, filename, this.format, getWorkflow().getOutputDirectory(this), elem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Visualization of mean, variance, standard deviation and more.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.VISUALIZATION;
    }
}
