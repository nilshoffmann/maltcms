/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: GradientVisualizer.java 160 2010-08-31 19:55:58Z nilshoffmann $
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
import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.math.functions.similarities.ArrayDotMap;
import org.openide.util.lookup.ServiceProvider;

/**
 * Creates a gradient image of an chromatogramm using a specific distance
 * function.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Slf4j
@Data
@RequiresVariables(names = {"var.total_intensity", "var.scan_rate",
    "var.modulation_time", "var.second_column_scan_index",
    "var.mass_values", "var.intensity_values", "var.scan_index",
    "var.mass_range_min", "var.mass_range_max", "var.modulation_time",
    "var.scan_rate"})
@RequiresOptionalVariables(names = {""})
@ProvidesVariables(names = {""})
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
    @Configurable(value = "maltcms.commands.distances.ArrayCos")
    private String distClass = "maltcms.commands.distances.ArrayCos";
    private IArraySimilarity similarity;
    @Configurable(value = "false")
    private boolean absolut = false;

    @Override
    public String toString() {
        return getClass().getName();
    }

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
            ff.getChild(this.totalIntensityVar).setIndex(
                    ff.getChild(this.secondScanIndexVar));

//			if (this.distance instanceof ArrayVarNormLp) {
//				log.info("Setting variance");
//				((ArrayVarNormLp) this.distance)
//						.setVarianceArray((ArrayDouble.D1) ff.getChild(
//								"var_intensity_values").getArray());
//			}
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
        this.colorrampLocation = cfg.getString("images.colorramp",
                "res/colorRamps/bcgyr.csv");
        this.doubleFillValue = cfg.getDouble(
                "ucar.nc2.NetcdfFile.fillValueDouble", 9.9692099683868690e+36);
        this.threshold = cfg.getDouble("images.thresholdLow", 0.0d);
        distClass = cfg.getString(this.getClass().getName() + ".distance",
                "maltcms.commands.distances.ArrayCos");
        if (distClass != null) {
            this.similarity = Factory.getInstance().getObjectFactory().
                    instantiate(distClass, IArraySimilarity.class);
        }
        this.absolut = cfg.getBoolean(this.getClass().getName() + ".absolut",
                false);
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
