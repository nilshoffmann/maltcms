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
package maltcms.commands.fragments.peakfinding;

import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ConstraintViolationException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.BatchFilter;
import maltcms.commands.filters.array.MultiplicationFilter;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.IBaselineEstimator;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.LoessMinimaBaselineEstimator;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.PeakFinderWorkerResult;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.TICPeakFinderWorker;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.TICPeakFinderWorker.TICPeakFinderWorkerBuilder;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.WorkflowResult;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import net.sf.mpaxs.api.ICompletionService;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.openide.util.lookup.ServiceProvider;

/**
 * Find Peaks based on TIC, estimates a local baseline and, based on a given
 * signal-to-noise ratio, decides whether a maximum is a peak candidate or not.
 *
 * @author Nils Hoffmann
 * 
 */
@RequiresVariables(names = {"var.total_intensity"})
@RequiresOptionalVariables(names = {"var.scan_acquisition_time"})
@ProvidesVariables(names = {"var.tic_peaks", "var.tic_filtered", "var.peak_name",
    "var.peak_retention_time", "var.peak_start_time",
    "var.peak_end_time", "var.peak_area", "var.baseline_start_time",
    "var.baseline_stop_time", "var.baseline_start_value", "var.baseline_stop_value"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class TICPeakFinder extends AFragmentCommand {

    @Configurable(description="The minimal local signal-to-noise threshold "
            + "required for a peak to be reported.")
    private double peakThreshold = 0.01d;
    @Configurable(description="If true, a plot of the local estimated "
            + "signal-to-noise and of peak locations will be created.")
    private boolean saveGraphics = false;
    @Configurable(description="If true, peak areas will be integrated.")
    private boolean integratePeaks = false;
    @Configurable(description="If true, the tic will be used to integrate peaks.")
    private boolean integrateTICPeaks = true;
    @Configurable(description="Width of the sliding window for local snr"
            + " estimation. ")
    private int snrWindow = 50;
    @Configurable(name = "var.total_intensity")
    private String ticVarName = "total_intensity";
    @Configurable(name = "var.scan_acquisition_time")
    private String satVarName = "scan_acquisition_time";
    @Configurable
    private String ticPeakVarName = "tic_peaks";
    @Configurable
    private String ticFilteredVarName = "tic_filtered";
    @Configurable(description="If true, the raw tic will be used for "
            + "integration. If false, the smoothed and filtered "
            + "tic will be used.")
    private boolean integrateRawTic = true;
    @Configurable(description="The minimum number of scans between two peak apices."
            + "The second peak will be omitted, if it is closer to the first"
            + " peak than allowed by the parameter.")
    private int peakSeparationWindow = 10;
    @Deprecated
    @Configurable(description="The removal of overlapping peaks is currently unavailable.")
    private boolean removeOverlappingPeaks = true;
    @Configurable(description="If true, the estimated baseline is subtracted"
            + " from the smoothed and filtered tic.")
    private boolean subtractBaseline = false;
    @Configurable(description="The baseline estimator to use.")
    private IBaselineEstimator baselineEstimator = new LoessMinimaBaselineEstimator();
    @Configurable(description="The filters to use for smoothing and filtering"
            + " of the tic.")
    private List<AArrayFilter> filter = Arrays.asList(
            (AArrayFilter) new MultiplicationFilter());
    @Configurable(description = "A list of peak normalizers. Each normalizer is "
            + "invoked and its result multiplied to the intermediate result "
            + "with the original peak's area.")
    private List<IPeakNormalizer> peakNormalizers = Collections.emptyList();

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getClass().getName();
    }

    /** {@inheritDoc} */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        EvalTools.notNull(t, this);
        log.info("Searching for peaks");
        initProgress(2*t.size());
        ICompletionService<PeakFinderWorkerResult> completionService = createCompletionService(PeakFinderWorkerResult.class);
        for (final IFileFragment f : t) {
            List<IPeakNormalizer> peakNormalizerCopy = new ArrayList<>();
            for (IPeakNormalizer normalizer : peakNormalizers) {
                peakNormalizerCopy.add((IPeakNormalizer) normalizer.copy());
            }
            TICPeakFinderWorkerBuilder builder = TICPeakFinderWorker.builder();
            TICPeakFinderWorker tpf = builder
                .outputDirectory(getWorkflow().getOutputDirectory(this))
               .inputUri(f.getUri())
               .integratePeaks(integratePeaks)
               .integrateTICPeaks(integrateTICPeaks)
               .integrateRawTic(integrateRawTic)
               .saveGraphics(saveGraphics)
               .removeOverlappingPeaks(removeOverlappingPeaks)
               .subtractBaseline(subtractBaseline)
               .peakThreshold(peakThreshold)
               .peakSeparationWindow(peakSeparationWindow)
               .snrWindow(snrWindow)
               .baselineEstimator((IBaselineEstimator)baselineEstimator.copy())
               .filter(BatchFilter.copy(filter))
               .peakNormalizers(peakNormalizerCopy)
               .ticVarName(ticVarName)
               .satVarName(satVarName)
               .ticPeakVarName(ticPeakVarName)
               .ticFilteredVarName(ticFilteredVarName)
               .properties(ConfigurationConverter.getProperties(Factory.getInstance().getConfiguration()))
               .build();
            completionService.submit(tpf);
            getWorkflow().append(getProgress().nextStep());
        }
        try {
            List<URI> resultsUris = new ArrayList<>();
            List<PeakFinderWorkerResult> results = completionService.call();
            for (PeakFinderWorkerResult res : results) {
                resultsUris.add(res.getResultUri());
                for (WorkflowResult wf : res.getWorkflowResults()) {
                    getWorkflow().append(new DefaultWorkflowResult(wf.getResource(), this, wf.getWorkflowSlot(), wf.getResources()));
                }
                getWorkflow().append(getProgress().nextStep());
            }
            TupleND<IFileFragment> resultFragments = mapToInputUri(resultsUris, t);
            addWorkflowResults(resultFragments);
            return resultFragments;
        } catch (Exception e) {
            log.warn("Caught exception while waiting for results: ", e);
        }
        throw new ConstraintViolationException("Could not process any files!");
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        log.debug("Configure called on TICPeakFinder");
        this.ticPeakVarName = cfg.getString("var.tic_peaks", "tic_peaks");
        this.ticVarName = cfg.getString("var.total_intensity",
                "total_intensity");
        this.satVarName = cfg.getString("var.scan_acquisition_time",
                "scan_acquisition_time");
        this.ticPeakVarName = cfg.getString("var.tic_peaks", "tic_peaks");
        this.ticFilteredVarName = cfg.getString("var.tic_filtered",
                "tic_filtered");
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Finds peaks based on total ion current (TIC), using a simple extremum search within a window, combined with a signal-to-noise parameter to select peaks.";
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /** {@inheritDoc} */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.PEAKFINDING;
    }
}
