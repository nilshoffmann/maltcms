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
package maltcms.commands.fragments2d.visualization;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import maltcms.io.csv.ColorRampReader;
import maltcms.tools.ImageTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.MassSpectrumPlot;
import maltcms.ui.charts.PlotRunner;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.plot.XYPlot;

import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * Creates an image for a two-dimensional chromatogram.
 *
 * @author Mathias Wilhelm
 */
@Slf4j
@Data
@RequiresVariables(names = {"var.total_intensity", "var.scan_rate",
    "var.modulation_time", "var.second_column_scan_index",
    "var.second_column_time", "var.scan_acquisition_time"})
@RequiresOptionalVariables(names = {"var.v_total_intensity"})
@ProvidesVariables(names = {"var.meanms_1d_vertical",
    "var.meanms_1d_vertical_index"})
@ServiceProvider(service = AFragmentCommand.class)
public class Chromatogram2DVisualizer extends AFragmentCommand {

    private final String description = "2D chromatogram visualization";
    private final WorkflowSlot workflowSlot = WorkflowSlot.VISUALIZATION;
    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String totalIntensityVar = "total_intensity";
    @Configurable(name = "var.second_column_scan_index",
    value = "second_column_scan_index")
    private String secondScanIndexVar = "second_column_scan_index";
    @Configurable(name = "var.scan_rate", value = "scan_rate")
    private String scanRateVar = "scan_rate";
    @Configurable(name = "var.modulation_time", value = "modulation_time")
    private String modulationTimeVar = "modulation_time";
    @Configurable(name = "var.scan_acquisition_time",
    value = "scan_acquisition_time")
    private String scanAcquTime = "scan_acquisition_time";
    @Configurable(name = "var.second_column_scan_index",
    value = "second_column_scan_index")
    private final String secondColumnScanIndexVar = "second_column_scan_index";
    @Configurable(name = "var.second_column_time", value = "second_column_time")
    private String secondColumnTimeVar = "second_column_time";
    @Configurable(name = "images.colorramp", value = "res/colorRamps/bcgyr.csv")
    private String colorrampLocation = "res/colorRamps/bcgyr.csv";
    @Configurable(name = "maltcms.ui.charts.PlotRunner.filetype", value = "png")
    private final String format = "png";
    @Configurable(name = "ucar.nc2.NetcdfFile.fillValueDouble",
    value = "9.9692099683868690e+36d")
    private double doubleFillValue;
    @Configurable(name = "images.thresholdLow", value = "0")
    private double threshold = 0;
    @Configurable(value = "false")
    private boolean substractMean = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        final ColorRampReader crr = new ColorRampReader();
        final int[][] colorRamp = crr.readColorRamp(this.colorrampLocation);

        for (final IFileFragment ff : t) {
            log.info("Creating image for {}", ff.getName());
            log.info("Using {} as data", this.totalIntensityVar);
            final double scanRate = ff.getChild(this.scanRateVar).getArray().getDouble(
                    Index.scalarIndexImmutable);
            final double modulationTime = ff.getChild(this.modulationTimeVar).
                    getArray().getDouble(Index.scalarIndexImmutable);
            final int scansPerModulation = (int) (scanRate * modulationTime);
			IVariableFragment ticVar = ff.getChild(this.totalIntensityVar);
			IVariableFragment tic2DScanIndexVar = ff.getChild(this.secondScanIndexVar);
			ticVar.setIndex(tic2DScanIndexVar);
            List<Array> intensities = ticVar.
                    getIndexedArray();
            boolean truncateLast = false;
            int shapeZero = -1;
            for (Array a : intensities) {
                log.debug("Shape of array: {}", a.getShape()[0]);
                if (shapeZero == -1) {
                    shapeZero = a.getShape()[0];
                }
                if (a.getShape()[0] != shapeZero) {
                    truncateLast = true;
                }
            }

            if (truncateLast) {
                intensities = intensities.subList(0, intensities.size() - 2);
            }

            if (this.substractMean) {
                ArrayInt.D1 means = new ArrayInt.D1(intensities.size());
                IndexIterator iter;
                int sum;
                int i = 0;
                for (Array inten : intensities) {
                    iter = inten.getIndexIterator();
                    sum = 0;
                    while (iter.hasNext()) {
                        sum += iter.getIntNext();
                    }
                    means.set(i++, (int) ((sum / inten.getShape()[0]) / 1.5));
                }

                for (i = 0; i < intensities.size(); i++) {
                    iter = intensities.get(i).getIndexIterator();
                    while (iter.hasNext()) {
                        iter.setIntCurrent(iter.getIntNext() - means.get(i));
                    }
                }

                final AChart<XYPlot> plot = new MassSpectrumPlot(
                        "Scanline TIC mean", StringTools.removeFileExt(ff.
                        getName()), means, false, false);
                final PlotRunner pl1 = new PlotRunner(plot.create(),
                        "Scanline TIC mean", StringTools.removeFileExt(ff.
                        getName())
                        + "_tic-mean", getWorkflow().getOutputDirectory(this));
                pl1.configure(Factory.getInstance().getConfiguration());
                Factory.getInstance().submitJob(pl1);

            }

            final BufferedImage bi = ImageTools.create2DImage(ff.getName(),
                    intensities, scansPerModulation, this.doubleFillValue,
                    this.threshold, colorRamp, this.getClass());
            final String filename = StringTools.removeFileExt(ff.getName())
                    + "_empty";
            final File out = ImageTools.saveImage(bi, filename, this.format,
                    getWorkflow().getOutputDirectory(this), this);

//            final ArrayDouble.D1 firstRetTime = (ArrayDouble.D1) ArrayTools.
//                    divBy60(ff.getChild(this.scanAcquTime).getArray());
//            IVariableFragment sctv = ff.getChild(this.secondColumnTimeVar);
//			sctv.setIndex(tic2DScanIndexVar);
//            final ArrayDouble.D1 secondRetTime = (ArrayDouble.D1) sctv.getIndexedArray().get(0);
//            final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> times = new Tuple2D<ArrayDouble.D1, ArrayDouble.D1>(
//                    firstRetTime, secondRetTime);
//
//            log.info("Using file {} for AChart", out.getAbsolutePath());
//            final AChart<XYBPlot> chart = new BHeatMapChart(
//                    out.getAbsolutePath(), "first retention time[min]",
//                    "second retention time[s]", times, filename);
//            final PlotRunner pl = new PlotRunner(chart.create(),
//                    "Chromatogram", StringTools.removeFileExt(ff.getName()),
//                    getWorkflow().getOutputDirectory(this));
//            pl.configure(Factory.getInstance().getConfiguration());
//            Factory.getInstance().submitJob(pl);

            final ArrayInt.D1 histo = new ArrayInt.D1(256);
            int[] a = new int[3];
            for (int i = 0; i < bi.getWidth(); i++) {
                for (int j = 0; j < bi.getHeight(); j++) {
                    a = bi.getRaster().getPixel(i, j, a);
                    if ((a[2] != 255) && (a[2] != 0)) {
                        histo.set(a[2], histo.get(a[2]) + 1);
                    }
                }
            }

            final AChart<XYPlot> plot = new MassSpectrumPlot(
                    "Verteilung Farbe",
                    StringTools.removeFileExt(ff.getName()), histo, false,
                    false);
            final PlotRunner pl1 = new PlotRunner(plot.create(),
                    "Verteilung Farbe", StringTools.removeFileExt(ff.getName())
                    + "_v", getWorkflow().getOutputDirectory(this));
            pl1.configure(Factory.getInstance().getConfiguration());
            Factory.getInstance().submitJob(pl1);
        }
        return t;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        this.totalIntensityVar = cfg.getString(this.getClass().getName()
                + ".total_intensity", "total_intensity");
        this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
        this.modulationTimeVar = cfg.getString("var.modulation_time",
                "modulation_time");
        this.secondScanIndexVar = cfg.getString("var.second_column_scan_index",
                "second_column_scan_index");
        this.doubleFillValue = cfg.getDouble(
                "ucar.nc2.NetcdfFile.fillValueDouble", 9.9692099683868690e+36);
        this.secondColumnTimeVar = cfg.getString("var.second_column_time",
                "second_column_time");
        this.scanAcquTime = cfg.getString("var.scan_acquisition_time",
                "scan_acquisition_time");
    }
}
