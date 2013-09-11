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
package maltcms.commands.fragments.visualization;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import maltcms.commands.filters.array.AdditionFilter;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.io.csv.ColorRampReader;
import maltcms.tools.ArrayTools;
import maltcms.tools.ImageTools;
import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.HeatMapChart;
import maltcms.ui.charts.PlotRunner;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.tools.MathTools;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * Visualizes a chromatogram as a heat map, based on the empirical distribution
 * of intensity values, using a user defined sample model and color model.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@RequiresVariables(names = {"var.scan_acquisition_time", "var.mass_values",
    "var.binned_intensity_values", "var.binned_scan_index"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class ChromatogramVisualizer extends AFragmentCommand {

    private final String description = "Creates two-dimensional heat map plots of chromatograms.";
    private final WorkflowSlot workflowSlot = WorkflowSlot.VISUALIZATION;
    @Configurable(name = "var.mass_values")
    private String mzVariableName = "mass_values";
    @Configurable(name = "var.scan_acquisition_time")
    private String scanAcquisitionTimeVariableName = "scan_acquisition_time";
    @Configurable
    private String format = "png";
    @Configurable
    private String colorrampLocation = "res/colorRamps/bcgyr.csv";
    @Configurable
    private int sampleSize = 1024;
    @Configurable
    private double lowThreshold = 0.0d;
    @Configurable
    private boolean substractStartTime = false;
    @Configurable
    private String timeUnit = "min";

    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        final ColorRampReader crr = new ColorRampReader();
        final int[][] colorRamp = crr.readColorRamp(this.colorrampLocation);
        for (final IFileFragment ff : t) {
            final IFileFragment iff = ff;
            List<Array> aa = null;
            // try {// try loading of binned arrays intensities
            final IVariableFragment bints = ff.getChild(
                    "binned_intensity_values");
            final IVariableFragment bsi = ff.getChild("binned_scan_index");
            bints.setIndex(bsi);
            aa = bints.getIndexedArray();
            final List<Array> eics = ArrayTools.tilt(aa);
            final List<Array> feics = new ArrayList<Array>();
            // for (Array a : eics) {
            // feics.add(filterBaseline(a, 20));
            // }
            aa = ArrayTools.tilt(eics);
            log.info("aa: {}", aa);

            // } catch (final ResourceNotAvailableException rnae) {
            // Tuple2D<Double, Double> tple = MaltcmsTools
            // .getMinMaxMassRange(ff);
            // iff = MaltcmsTools.prepareDenseArraysMZI(ff,
            // this.scanIndexVariableName, this.mzVariableName,
            // this.intenVariableName, "binned_scan_index",
            // "binned_mass_values", "binned_intensity_values", tple
            // .getFirst(), tple.getSecond(), getWorkflow()
            // .getStartupDate());
            // final IVariableFragment bints = iff
            // .getChild("binned_intensity_values");
            // final IVariableFragment bsi = iff.getChild("binned_scan_index");
            // bints.setIndex(bsi);
            // aa = bints.getIndexedArray();
            // }

            final ArrayStatsScanner ass = new ArrayStatsScanner();
            ass.apply(aa.toArray(new Array[]{}));
            final StatsMap sm = ass.getGlobalStatsMap();
            final double var = sm.get(Vars.Variance.toString());
            final double sdev = Math.sqrt(var);
            final double si = ArrayTools.integrate(ArrayTools.integrate(aa));
            final double sdevrel = sdev / si;
            this.lowThreshold = 0.0;
            log.info("SDev of intensities: {}, relative: {}", sdev,
                    sdevrel);

            // ArrayList<Array>
            final Array masses = iff.getChild(this.mzVariableName).getArray();
            ArrayDouble.D1 sat = null;
            // boolean drawTIC = true;
            String x_label = "scan number";
            final Array[] domains = new Array[1];
            try {
                domains[0] = ff.getChild(this.scanAcquisitionTimeVariableName).
                        getArray().copy();
                log.debug("Using scan acquisition time0 {}", domains[0]);
                if (this.timeUnit.equals("min")) {
                    log.info("Converting seconds to minutes");
                    domains[0] = ArrayTools.divBy60(domains[0]);
                } else if (this.timeUnit.equals("h")) {
                    log.info("Converting seconds to hours");
                    domains[0] = ArrayTools.divBy60(ArrayTools.divBy60(
                            domains[0]));
                }
                final double min = MAMath.getMinimum(domains[0]);
                x_label = "time [" + this.timeUnit + "]";
                if (this.substractStartTime) {
                    final AdditionFilter af = new AdditionFilter(-min);
                    domains[0] = af.apply(new Array[]{domains[0].copy()})[0];
                }
            } catch (final ResourceNotAvailableException re) {
                log.info(
                        "Could not load resource {} for domain axis, falling back to scan index domain!",
                        this.scanAcquisitionTimeVariableName);
                domains[0] = ArrayTools.indexArray(aa.size(), 0);
            }
            sat = (ArrayDouble.D1) domains[0];
            final MinMax mm = MAMath.getMinMax(masses);
            final int bins = MaltcmsTools.getNumberOfIntegerMassBins(mm.min,
                    mm.max, Factory.getInstance().getConfiguration().getDouble(
                    "dense_arrays.binResolution", 1.0d));
            final ArrayDouble.D1 massAxis = new ArrayDouble.D1(bins);
            for (int i = 0; i < bins; i++) {
                massAxis.set(i, mm.min + i);
            }
            if (!aa.isEmpty()) {
                final BufferedImage bi = ImageTools.fullSpectrum(ff.getName(),
                        aa, bins, colorRamp, this.sampleSize, true,
                        this.lowThreshold);
                ImageTools.saveImage(bi, ff.getName() + "-chromatogram",
                        this.format, getWorkflow().getOutputDirectory(this),
                        this);

                final HeatMapChart hmc = new HeatMapChart(bi, x_label, "m/z",
                        new Tuple2D<ArrayDouble.D1, ArrayDouble.D1>(sat,
                        massAxis), ff.getUri().toString());
                final PlotRunner pl = new PlotRunner(hmc.create(),
                        "Chromatogram of " + ff.getName(), StringTools.
                        removeFileExt(ff.getName())
                        + "-chromatogram-chart", getWorkflow().
                        getOutputDirectory(this));
                pl.configure(Factory.getInstance().getConfiguration());
                final File f = pl.getFile();
                final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f,
                        this, WorkflowSlot.VISUALIZATION, ff);
                getWorkflow().append(dwr);
                Factory.getInstance().submitJob(pl);
            } else {
                log.warn("Could not load required variables");
            }
        }
        return t;
    }

    @Override
    public void configure(final Configuration cfg) {
        this.scanAcquisitionTimeVariableName = cfg.getString(
                "var.scan_acquisition_time", "scan_acquisition_time");
        this.mzVariableName = cfg.getString("var.mass_values", "mass_values");
    }

    private Array filterBaseline(final Array tic, final int median_window) {
        double current = 0;
        final Index ind = tic.getIndex();
        final MinMax mm = MAMath.getMinMax(tic);
        // a.getShape()
        final Array sortedtic = Array.factory(tic.getElementType(),
                tic.getShape());
        final Array correctedtic = Array.factory(tic.getElementType(), tic.
                getShape());
        final Index cind = correctedtic.getIndex();
        MAMath.copy(sortedtic, tic);
        final double globalmean = MAMath.sumDouble(tic)
                / (tic.getShape()[0] - 1);
        final double globalvar = (mm.max - mm.min) * (mm.max - mm.min);
        log.debug("Squared difference between median and mean: {}",
                globalvar);
        double lmedian = globalmean, lstddev = 0.0d;
        log.debug("Value\tLow\tMedian\tHigh\tDev\tGTMedian\tSNR");
        for (int i = 1; i < tic.getShape()[0] - 1; i++) {
            log.debug("i=" + i);
            current = tic.getDouble(ind.set(i));
            // a-1 < a < a+1 -> median = a
            log.debug("Checking for extremum!");
            final int lmedian_low = Math.max(0, i - median_window);
            final int lmedian_high = Math.min(tic.getShape()[0] - 1, i
                    + median_window);
            log.debug("Median low: " + lmedian_low + " high: "
                    + lmedian_high);
            int[] vals;// = new int[lmedian_high-lmedian_low];
            try {
                vals = (int[]) tic.section(new int[]{lmedian_low},
                        new int[]{lmedian_high - lmedian_low},
                        new int[]{1}).get1DJavaArray(int.class);
                lmedian = MathTools.median(vals);

                lstddev = Math.abs(vals[vals.length - 1] - vals[0]);
                log.debug("local rel dev={}", lstddev);
                cind.set(i);
                final double corrected_value = Math.max(current - lmedian, 0);

                correctedtic.setDouble(cind, corrected_value);

            } catch (final InvalidRangeException e) {
                System.err.println(e.getLocalizedMessage());
            }

        }
        return correctedtic;
    }
}
