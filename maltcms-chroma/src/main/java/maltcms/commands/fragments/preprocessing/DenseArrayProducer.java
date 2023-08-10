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
package maltcms.commands.fragments.preprocessing;

import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.io.File;
import java.util.List;
import lombok.Data;

import maltcms.commands.fragments.preprocessing.denseArrayProducer.DenseArrayProducerWorker;
import maltcms.commands.fragments.preprocessing.denseArrayProducer.MinMaxMassFinderWorker;
import net.sf.mpaxs.api.ICompletionService;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.LoggerFactory;

/**
 * Creates bins of fixed size (currently 1) from a given set of spectra with
 * masses and intensities. Can filter mass channels, whose intensity is then
 * removed from the chromatogram.
 *
 * @author Nils Hoffmann
 * 
 */
@ProvidesVariables(names = {"var.binned_mass_values",
    "var.binned_intensity_values", "var.binned_scan_index"})
@RequiresVariables(names = {"var.mass_values", "var.intensity_values",
    "var.scan_index", "var.scan_acquisition_time", "var.total_intensity"})
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class DenseArrayProducer extends AFragmentCommand {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DenseArrayProducer.class);

    private final String description = "Creates a binned representation of a chromatogram.";
    private final WorkflowSlot workflowSlot = WorkflowSlot.GENERAL_PREPROCESSING;
    @Configurable(name = "var.mass_values")
    private String massValues = "mass_values";
    @Configurable(name = "var.intensity_values")
    private String intensityValues = "intensity_values";
    @Configurable(name = "var.scan_index")
    private String scanIndex = "scan_index";
    @Configurable(name = "var.total_intensity")
    private String totalIntensity = "total_intensity";
    @Configurable(name = "var.binned_intensity_values")
    private String binnedIntensityValues = "binned_intensity_values";
    @Configurable(name = "var.binned_mass_values")
    private String binnedMassValues = "binned_mass_values";
    @Configurable(name = "var.binned_scan_index")
    private String binnedScanIndex = "binned_scan_index";
    @Configurable(name = "var.mass_range_min")
    private String massRangeMin = "mass_range_min";
    @Configurable(name = "var.mass_range_max")
    private String massRangeMax = "mass_range_max";
    @Configurable(description="If true, scans will be normalized.")
    private boolean normalizeScans = false;
    @Configurable(description="List of masses that should be masked, "
            + "removing their associated intensities from each "
            + "scan they occur in.")
    private List<Double> maskedMasses = null;
    @Configurable(description="If true, invert the logic of masked masses, "
            + "effectively only selecting intensities associated to the"
            + " masked masses.")
    private boolean invertMaskedMasses = false;
    @Configurable(description="If true, ignore values in variables "
            + "mass_range_min and mass_range_max. Will then determine mass "
            + "ranges from the actual mass_values.")
    private boolean ignoreMinMaxMassArrays = false;
    @Configurable(description="If true, normalize intensities per EIC, "
            + "based on that EIC's mean intensity and variance. If false,"
            + "normalized intensities to length one for each scan. Mutually "
            + "exclusive with normalizeEicsToUnity.")
    private boolean normalizeMeanVariance = false;
    @Configurable(description="If true, normalizes all EIC intensities between 0 and 1,"
            + "before other normalizations are applied. Mutually exclusive "
            + "with normalizeMeanVariance.")
    private boolean normalizeEicsToUnity = false;
    @Configurable(description="Mass resolution to use for generation of profile "
            + "EICs. 1.0 means nominal mass accuracy. 10.0 results in ten times"
            + " higher resolution, up to the first decimal point. High values "
            + "may significantly increase both memory usage and runtime.")
    private double massBinResolution = 1.0d;

    /** {@inheritDoc} */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        log.debug("Creating dense arrays!");
        log.debug("Looking for minimum and maximum values!");
        //this needs to be done for all fragments before we can do anything else
        ICompletionService<double[]> massRangeCompletionService = createCompletionService(
                double[].class);

        for (IFileFragment f : t) {
            MinMaxMassFinderWorker mmmfw = new MinMaxMassFinderWorker();
            mmmfw.setFallbackVariableName(massValues);
            mmmfw.setMinMassVariableName(massRangeMin);
            mmmfw.setMaxMassVariableName(massRangeMax);
            mmmfw.setIgnoreMinMaxMassArrays(ignoreMinMaxMassArrays);
            mmmfw.setFileToLoad(f.getUri());
            massRangeCompletionService.submit(mmmfw);
        }
        double[] massRange = new double[]{Double.MAX_VALUE, Double.MIN_VALUE};
        try {
            List<double[]> massRangeResults = massRangeCompletionService.call();
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
            dapw.setFileToLoad(ff.getUri());
            dapw.setFileToSave(createWorkFragment(ff).getUri());
            ics.submit(dapw);
        }

        //wait and retrieve results
        TupleND<IFileFragment> ret = postProcess(ics, t);
        log.debug("Returning {} FileFragments!", ret.size());
        return ret;
    }

    /** {@inheritDoc} */
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
    }
}
