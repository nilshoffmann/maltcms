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
package maltcms.commands.fragments.preprocessing;

import java.io.File;
import java.util.Collections;
import java.util.List;

import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;

import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.datastructures.tools.EvalTools;
import java.lang.Double;
import java.util.ArrayList;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.preprocessing.denseArrayProducer.DenseArrayProducerWorker;
import maltcms.commands.fragments.preprocessing.denseArrayProducer.MinMaxMassFinderWorker;
import net.sf.mpaxs.api.ICompletionService;
import org.openide.util.lookup.ServiceProvider;

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
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class DenseArrayProducer extends AFragmentCommand {

    @Configurable
    private boolean normalizeScans = false;
    private String massValues = "mass_values";
    private String intensityValues = "intensity_values";
    private String scanIndex = "scan_index";
    private String totalIntensity = "total_intensity";
    private String binnedIntensityValues = "binned_intensity_values";
    private String binnedMassValues = "binned_mass_values";
    private String binnedScanIndex = "binned_scan_index";
    private String massRangeMin = "mass_range_min";
    private String massRangeMax = "mass_range_max";
    @Configurable
    private List<Double> maskedMasses = null;
    @Configurable
    private boolean invertMaskedMasses = false;
    @Configurable
    private boolean ignoreMinMaxMassArrays = false;
    @Configurable
    private boolean normalizeMeanVariance = false;
    private boolean normalizeEicsToUnity = false;
    private double massBinResolution = 1.0d;

    @Override
    public String toString() {
        return getClass().getName();
    }

    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {


        log.debug("Creating dense arrays!");
        log.debug("Looking for minimum and maximum values!");
//        final double[] minmax = null;
        //this needs to be done for all fragments before we can do anything else
        //currently not parallelized
        ICompletionService<double[]> massRangeCompletionService = createCompletionService(
                double[].class);

        for (IFileFragment f : t) {
            MinMaxMassFinderWorker mmmfw = new MinMaxMassFinderWorker();
            mmmfw.setFallbackVariableName(massValues);
            mmmfw.setMinMassVariableName(massRangeMin);
            mmmfw.setMaxMassVariableName(massRangeMax);
            mmmfw.setIgnoreMinMaxMassArrays(ignoreMinMaxMassArrays);
            mmmfw.setFileToLoad(new File(f.getAbsolutePath()));
            massRangeCompletionService.submit(mmmfw);
        }
        List<double[]> massRangeResults = new ArrayList<double[]>();
        double[] massRange = new double[]{Double.MAX_VALUE, Double.MIN_VALUE};
        try {
            massRangeResults = massRangeCompletionService.call();
            EvalTools.geq(1, massRangeResults.size(),
                    DenseArrayProducer.class);
            for (double[] result : massRangeResults) {
                massRange[0] = Math.min(massRange[0], result[0]);
                massRange[1] = Math.max(massRange[1], result[1]);
            }
        } catch (Exception e) {
            log.error("{}", e);
        }

        EvalTools.notNull(massRange, this);
        log.info("Minimum mass: {}; Maximum mass; {}", massRange[0],
                massRange[1]);
        ICompletionService<File> ics = createCompletionService(File.class);
        for (final IFileFragment ff : t) {
            DenseArrayProducerWorker dapw = new DenseArrayProducerWorker();
            dapw.setBinnedIntensityValues(binnedIntensityValues);
            dapw.setBinnedMassValues(binnedMassValues);
            dapw.setBinnedScanIndex(binnedScanIndex);
            dapw.setIntensityValues(intensityValues);
            dapw.setInvertMaskedMasses(invertMaskedMasses);
            dapw.setMaskedMasses(maskedMasses);
            dapw.setMassBinResolution(massBinResolution);
            dapw.setMassValues(massValues);
            dapw.setMinMass(massRange[0]);
            dapw.setMaxMass(massRange[1]);
            dapw.setNormalizeEicsToZeroMeanUnitVariance(normalizeMeanVariance);
            dapw.setNormalizeEicsToUnity(normalizeEicsToUnity);
            dapw.setNormalizeScans(normalizeScans);
            dapw.setScanIndex(scanIndex);
            dapw.setTotalIntensity(totalIntensity);
            dapw.setFileToLoad(new File(ff.getAbsolutePath()));
            dapw.setFileToSave(new File(createWorkFragment(ff).
                    getAbsolutePath()));
            ics.submit(dapw);
        }

        //wait and retrieve results
        TupleND<IFileFragment> ret = postProcess(ics, t);
        log.debug("Returning {} FileFragments!", ret.size());
        return ret;
    }

    @Override
    public void configure(final Configuration cfg) {
        this.massValues = cfg.getString("var.mass_values", "mass_values");
        this.intensityValues = cfg.getString("var.intensity_values",
                "intensity_values");
        this.totalIntensity = cfg.getString("var.total_intensity",
                "total_intensity");
        this.massRangeMin = cfg.getString("var.mass_range_min",
                "mass_range_min");
        this.massRangeMax = cfg.getString("var.mass_range_max",
                "mass_range_max");
        this.scanIndex = cfg.getString("var.scan_index", "scan_index");
        this.binnedIntensityValues = cfg.getString(
                "var.binned_intensity_values", "binned_intensity_values");
        this.binnedMassValues = cfg.getString("var.binned_mass_values",
                "binned_mass_values");
        this.binnedScanIndex = cfg.getString("var.binned_scan_index",
                "binned_scan_index");
        this.normalizeScans = cfg.getBoolean(this.getClass().getName()
                + ".normalizeScans", false);
        this.normalizeMeanVariance = cfg.getBoolean(this.getClass().getName()
                + ".normalizeMeanVariance", false);
//		this.nthreads = cfg.getInt("cross.Factory.maxthreads", 5);
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
