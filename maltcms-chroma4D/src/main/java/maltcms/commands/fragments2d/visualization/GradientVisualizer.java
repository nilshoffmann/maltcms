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

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import maltcms.math.functions.IArraySimilarity;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.io.csv.ColorRampReader;
import maltcms.tools.ImageTools;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.math.functions.similarities.ArrayCos;
import maltcms.math.functions.similarities.ArrayDotMap;
import org.openide.util.lookup.ServiceProvider;

/**
 * Creates a gradient image of an chromatogramm using a specific distance
 * function.
 *
 * @author Mathias Wilhelm
 */
@Slf4j
@Data
@RequiresVariables(names = {"var.total_intensity", "var.scan_rate",
    "var.modulation_time", "var.second_column_scan_index",
    "var.mass_values", "var.intensity_values", "var.scan_index",
    "var.mass_range_min", "var.mass_range_max", "var.modulation_time",
    "var.scan_rate"})
@ServiceProvider(service = AFragmentCommand.class)
public class GradientVisualizer extends AFragmentCommand {

    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String totalIntensityVar = "total_intensity";
    @Configurable(name = "var.second_column_scan_index",
    value = "second_column_scan_index")
    private String secondScanIndexVar = "second_column_scan_index";
    @Configurable(name = "var.scan_rate", value = "scan_rate")
    private String scanRateVar = "scan_rate";
    @Configurable(name = "var.modulation_time", value = "modulation_time")
    private String modulationTimeVar = "modulation_time";
    @Configurable(name = "images.colorramp", value = "res/colorRamps/bcgyr.csv")
    private String colorrampLocation = "res/colorRamps/bcgyr.csv";
    @Configurable(name = "maltcms.ui.charts.PlotRunner.filetype", value = "png")
    private final String format = "png";
    @Configurable(name = "ucar.nc2.NetcdfFile.fillValueDouble",
    value = "9.9692099683868690e+36d")
    private double doubleFillValue;
    @Configurable(name = "images.thresholdLow", value = "0")
    private double threshold = 0;
    private IArraySimilarity similarity = new ArrayCos();
    @Configurable(value = "false")
    private boolean absolut = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        final ColorRampReader crr = new ColorRampReader();
        final int[][] colorRamp = crr.readColorRamp(this.colorrampLocation);

        for (final IFileFragment ff : t) {
            log.info("Creating image for {}", ff.getName());
            final int scanRate = ff.getChild(this.scanRateVar).getArray().getInt(
                    Index.scalarIndexImmutable);
            final int modulationTime = ff.getChild(this.modulationTimeVar).
                    getArray().getInt(Index.scalarIndexImmutable);
            final int scansPerModulation = scanRate * modulationTime;
            IVariableFragment ticVar = ff.getChild(this.totalIntensityVar);
			IVariableFragment ssiv = ff.getChild(this.secondScanIndexVar);
			ticVar.setIndex(ssiv);
            if (this.similarity instanceof ArrayDotMap) {
                log.info("Setting std");
                ((ArrayDotMap) this.similarity).setStdArray((ArrayDouble.D1) ff.
                        getChild("sd_intensity_values").getArray());
            }

            final List<Array> gradient = new ArrayList<Array>();
            final IScanLine slc = ScanLineCacheFactory.getScanLineCache(ff);
            slc.setCacheModulations(false);
            Array old = null, actual = null;
            final Point start = new Point(410, 274);
            if (this.absolut) {
                old = slc.getScanlineMS(start.x).get(start.y);
            }
            double d = 0;
            double sum = 0, max = Double.MIN_VALUE, min = Double.MAX_VALUE;
            int c = 0;
            for (int i = 0; i < slc.getScanLineCount() - 1; i++) {
                if (i % 100 == 0) {
                    log.info("Modulation {} - {}", i, sum);
                }
                final List<Array> scanline = slc.getScanlineMS(i);
                final ArrayDouble.D1 g = new ArrayDouble.D1(scansPerModulation);
                int counter = 0;
                for (final Array ms : scanline) {
                    actual = ms;
                    if ((old != null) && (actual != null)) {
                        d = Math.abs(this.similarity.apply(old,
                                actual));
                        sum += d;
                        c++;
                        if (max < d) {
                            max = d;
                        }
                        if (min > d) {
                            min = d;
                        }
                        g.set(counter, d);
                    } else {
                        g.set(counter, 0);
                    }
                    if (!this.absolut) {
                        old = ms;
                    }
                    counter++;
                }
                gradient.add(g);
            }

            log.info("sum: {}, c: {}", sum, c);
            log.info("min: {}, max: {}, mean: " + (sum / c), min, max);

            final BufferedImage bi = ImageTools.create2DImage(ff.getName(),
                    gradient, scansPerModulation, this.doubleFillValue,
                    this.threshold, colorRamp, this.getClass());
            if (this.absolut) {
                log.info("Using abolute MS as reference");
                bi.getRaster().setPixel(start.x, start.y, new int[]{0, 0, 0});
            }
            final String filename = StringTools.removeFileExt(ff.getName())
                    + "-" + this.similarity.getClass().getSimpleName();
            ImageTools.saveImage(bi, filename, this.format, getWorkflow().
                    getOutputDirectory(this), this);
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
        this.threshold = cfg.getDouble("images.thresholdLow", 0.0d);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Will visualize the distance gradient in y direction for all modulations.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.VISUALIZATION;
    }
}