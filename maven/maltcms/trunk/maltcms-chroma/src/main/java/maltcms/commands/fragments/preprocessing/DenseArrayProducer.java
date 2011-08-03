/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
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
 * $Id: DenseArrayProducer.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.commands.fragments.preprocessing;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.MAVector;
import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.threads.ExecutorsManager;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowProgressResult;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.datastructures.tools.EvalTools;

/**
 * Creates bins of fixed size (currently 1) from a given set of spectra with
 * masses and intensities. Can filter mass channels, whose intensity is then
 * removed from the chromatogram.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@ProvidesVariables(names = {"var.binned_mass_values",
    "var.binned_intensity_values", "var.binned_scan_index"})
@RequiresVariables(names = {"var.mass_values", "var.intensity_values",
    "var.scan_index", "var.scan_acquisition_time", "var.total_intensity"})
public class DenseArrayProducer extends AFragmentCommand {

    private int nthreads = 5;
    @Configurable
    private boolean normalizeScans = false;
    private String mass_values = "mass_values";
    private String intensity_values = "intensity_values";
    private String scan_index = "scan_index";
    private String total_intensity = "total_intensity";
    private String binned_intensity_values = "binned_intensity_values";
    private String binned_mass_values = "binned_mass_values";
    private String binned_scan_index = "binned_scan_index";
    private String mapping_file = "input_to_tmp_files.cdf",
            mapping_file_var = "file_map";
    private final Logger log = Logging.getLogger(this.getClass());
    private String minvarname = "mass_range_min";
    private String maxvarname = "mass_range_max";
    @Configurable
    private List<Double> maskedMasses = null;
    @Configurable
    private boolean invertMaskedMasses = false;
    @Configurable
    private boolean normalizeMeanVariance = false;
    private double massBinResolution = 1.0d;

    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        // create new ProgressResult
        final DefaultWorkflowProgressResult dwpr = new DefaultWorkflowProgressResult(
                t.getSize(), this, getWorkflowSlot());
        ExecutorsManager em = new ExecutorsManager(this.nthreads);
        final ArrayList<IFileFragment> al = new ArrayList<IFileFragment>();
        this.log.debug("Creating dense arrays!");
        this.log.debug("Looking for minimum and maximum values!");
        final Tuple2D<Double, Double> minmax = MaltcmsTools.findGlobalMinMax(t,
                this.minvarname, this.maxvarname, this.mass_values);
        EvalTools.notNull(minmax, this);
        this.log.info("Minimum mass: {}; Maximum mass; {}", minmax.getFirst(),
                minmax.getSecond());
        final IWorkflowElement iwe = this;
        for (final IFileFragment ff : t) {
            // Runnable r = new Runnable() {
            // @Override
            // public void run() {
            log.info("Loading scans for file {}", ff.getName());
            final IFileFragment f = MaltcmsTools.prepareDenseArraysMZI(ff,
                    scan_index, mass_values, intensity_values,
                    binned_scan_index, binned_mass_values,
                    binned_intensity_values, minmax.getFirst(),
                    minmax.getSecond(), getWorkflow().getOutputDirectory(
                    this));
            // f.save();
            log.debug("Loaded scans for file {}, stored in {}", ff, f);
            log.debug("Source Files of f={} : {}", f, f.getSourceFiles());
            final int bins = MaltcmsTools.getNumberOfIntegerMassBins(minmax.
                    getFirst(), minmax.getSecond(), massBinResolution);
            maskMasses(minmax, f, bins);
            normalizeScans(f);
            log.debug("Created IFileFragment {}", f);
            log.debug("Adding pair {},{}", ff, f);
            log.debug("{}", FileFragment.printFragment(f));
            f.save();
            ff.clearArrays();
            al.add(f);
            final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
                    new File(f.getAbsolutePath()), iwe,
                    WorkflowSlot.GENERAL_PREPROCESSING, f);
            getWorkflow().append(dwr);
            // notify workflow
            getWorkflow().append(dwpr.nextStep());
            // }
            // };
            // em.submit(r);
        }
        // em.shutdown();
        // try {
        // em.awaitTermination(30, TimeUnit.MINUTES);
        // } catch (InterruptedException e) {
        // throw new ConstraintViolationException(
        // "Executor terminated abnormally: \n"
        // + e.getLocalizedMessage());
        // }
        final TupleND<IFileFragment> res = new TupleND<IFileFragment>(al);
        EvalTools.notNull(res, this);
        return res;
    }

    /**
     * @param minmax
     * @param f
     * @param bins
     */
    private void maskMasses(final Tuple2D<Double, Double> minmax,
            final IFileFragment f, final int bins) {
        // set masked masschannels to zero intensity
        if ((this.maskedMasses != null) && !this.maskedMasses.isEmpty()) {
            this.log.info("Filtering masked masses!");
            final ArrayDouble.D1 selector = new ArrayDouble.D1(bins);
            if (this.invertMaskedMasses) {
                ArrayTools.fill(selector, 1.0d);
                for (final Double integ : this.maskedMasses) {
                    this.log.info("Retaining mass {} at index {}", integ,
                            MaltcmsTools.binMZ(integ, minmax.getFirst(), minmax.
                            getSecond(), this.massBinResolution));

                    selector.set(MaltcmsTools.binMZ(integ, minmax.getFirst(),
                            minmax.getSecond(), this.massBinResolution), 0.0d);
                    // - (int) (Math.floor(minmax.getFirst())), 0.0d);
                }
            } else {
                for (final Double integ : this.maskedMasses) {
                    this.log.info("Filtering mass {} at index {}", integ,
                            MaltcmsTools.binMZ(integ, minmax.getFirst(), minmax.
                            getSecond(), this.massBinResolution));
                    selector.set(MaltcmsTools.binMZ(integ, minmax.getFirst(),
                            minmax.getSecond(), this.massBinResolution), 1.0d);
                    // - (int) (Math.floor(minmax.getFirst())), 1.0d);
                }
            }
            final IVariableFragment ivf = f.getChild(
                    this.binned_intensity_values);
            final IVariableFragment sidx = f.getChild(this.binned_scan_index);
            // since we remove intensities in bins, we should also adjust the
            // TIC
            // in this case, we shadow previous definitions
            final IVariableFragment total_intens = f.hasChild(
                    this.total_intensity) ? f.getChild(this.total_intensity) : new VariableFragment(
                    f,
                    this.total_intensity);
            final Array tan = Array.factory(DataType.DOUBLE, sidx.getArray().
                    getShape());
            final Index tanidx = tan.getIndex();
            ivf.setIndex(sidx);
            final List<Array> intens = ivf.getIndexedArray();
            // Over all scans
            int scan = 0;
            final ArrayList<Array> filtered = new ArrayList<Array>(intens.size());
            for (final Array a : intens) {
                // System.out.println("Before: " + a.toString());
                final Index aidx = a.getIndex();
                EvalTools.eqI(1, a.getRank(), this);
                // double accum = 0;
                for (int i = 0; i < a.getShape()[0]; i++) {
                    if (selector.get(i) == 1.0d) {
                        // log.info("Selector index {} = 1.0",i);
                        aidx.set(i);
                        a.setDouble(aidx, 0);
                    }

                }
                tanidx.set(scan);
                tan.setDouble(tanidx, ArrayTools.integrate(a));
                filtered.add(a);
                // System.out.println("After: " + a.toString());
                scan++;
            }
            ivf.setIndexedArray(filtered);
            total_intens.setArray(tan);
        }
    }

    /**
     * @param f
     */
    private void normalizeScans(final IFileFragment f) {
        if (this.normalizeScans || this.normalizeMeanVariance) {
            final IVariableFragment ivf = f.getChild(
                    this.binned_intensity_values);
            final IVariableFragment sidx = f.getChild(this.binned_scan_index);
            ivf.setIndex(sidx);
            final List<Array> intens = ivf.getIndexedArray();

            final List<Array> normIntens = new ArrayList<Array>();
            if (this.normalizeMeanVariance) {
                this.log.info(
                        "Normalizing by subtracting mean and dividing by variance!");
                final List<Array> tilted = ArrayTools.tilt(intens);
                final ArrayStatsScanner ass = Factory.getInstance().
                        getObjectFactory().instantiate(ArrayStatsScanner.class);
                StatsMap[] sm = ass.apply(tilted.toArray(new Array[]{}));
                ArrayDouble.D1 mean = new ArrayDouble.D1(tilted.size());
                ArrayDouble.D1 var = new ArrayDouble.D1(tilted.size());
                for (int i = 0; i < tilted.size(); i++) {
                    mean.set(i, -sm[i].get(Vars.Mean.name()));
                    var.set(i, sm[i].get(Vars.Variance.name()));
                }
                for (final Array a : intens) {
                    final MAVector ma = new MAVector(a);
                    final double norm = ma.norm();
                    if (norm == 0.0) {
                        normIntens.add(a);
                    } else {
                        normIntens.add(ArrayTools.div(ArrayTools.diff(a, mean),
                                var));
                    }

                }
            } else if (this.normalizeScans) {
                this.log.info("Normalizing scans to length 1");
                for (final Array a : intens) {
                    final MAVector ma = new MAVector(a);
                    final double norm = ma.norm();
                    this.log.debug("Norm: {}", norm);
                    if (norm == 0.0) {
                        normIntens.add(a);
                    } else {
                        normIntens.add(ArrayTools.mult(a, 1.0d / norm));
                    }
                }
            }
            ivf.setIndexedArray(normIntens);
        }
    }

    @Override
    public void configure(final Configuration cfg) {
        this.mass_values = cfg.getString("var.mass_values", "mass_values");
        this.intensity_values = cfg.getString("var.intensity_values",
                "intensity_values");
        this.total_intensity = cfg.getString("var.total_intensity",
                "total_intensity");
        this.minvarname = cfg.getString("var.mass_range_min", "mass_range_min");
        this.maxvarname = cfg.getString("var.mass_range_max", "mass_range_max");
        this.scan_index = cfg.getString("var.scan_index", "scan_index");
        this.binned_intensity_values = cfg.getString(
                "var.binned_intensity_values", "binned_intensity_values");
        this.binned_mass_values = cfg.getString("var.binned_mass_values",
                "binned_mass_values");
        this.binned_scan_index = cfg.getString("var.binned_scan_index",
                "binned_scan_index");
        this.mapping_file = cfg.getString("input_to_tmp_files_file_name",
                "input_to_tmp_files.cdf");
        this.mapping_file_var = cfg.getString("var.file_map", "file_map");
        this.normalizeScans = cfg.getBoolean(this.getClass().getName()
                + ".normalizeScans", false);
        this.normalizeMeanVariance = cfg.getBoolean(this.getClass().getName()
                + ".normalizeMeanVariance", false);
        this.nthreads = cfg.getInt("cross.Factory.maxthreads", 5);
        this.maskedMasses = MaltcmsTools.parseMaskedMassesList(cfg.getList(this.
                getClass().getName()
                + ".maskMasses", Collections.emptyList()));
        this.invertMaskedMasses = cfg.getBoolean(this.getClass().getName()
                + ".invertMaskedMasses", false);
        this.massBinResolution = cfg.getDouble(
                "dense_arrays.massBinResolution", 1.0d);
    }

    @Override
    public String getDescription() {
        return "Creates a binned representation of a chromatogram.";
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }
}
