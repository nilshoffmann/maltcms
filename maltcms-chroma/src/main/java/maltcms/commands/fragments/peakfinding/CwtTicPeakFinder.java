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

import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ConstraintViolationException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.peakfinding.cwtPeakFinder.CwtTicPeakFinderCallable;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.PeakFinderWorkerResult;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.WorkflowResult;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import net.sf.mpaxs.api.ICompletionService;
import org.openide.util.lookup.ServiceProvider;

/**
 * <p>
 * CwtTicPeakFinder class.</p>
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
@RequiresVariables(names = {"var.total_intensity"})
public class CwtTicPeakFinder extends AFragmentCommand {

    private final String description = "Finds TIC peaks using Continuous Wavelet Transform.";
    private final WorkflowSlot workflowSlot = WorkflowSlot.PEAKFINDING;
    @Configurable(description = "Minimum cwt scale to require a peak to reach.")
    private int minScale = 10;
    @Configurable(description = "Maximum cwt scale to calculate.")
    private int maxScale = 100;
    @Deprecated
    @Configurable(description = "Deprecated. Is not used internally.")
    private double minPercentile = 5.0d;
    @Configurable(description = "If true, peaks will be integrated in the original"
            + " signal domain within the bounds as determined by the cwt.")
    private boolean integratePeaks = true;
    @Configurable(description = "If true, save scaleogram details.")
    private boolean saveGraphics = false;
    @Configurable(description = "A list of peak normalizers. Each normalizer is "
            + "invoked and its result multiplied to the intermediate result "
            + "with the original peak's area.")
    private List<IPeakNormalizer> peakNormalizers = Collections.emptyList();
    @Configurable(name = "var.total_intensity")
    private String totalIntensityVar = "total_intensity";

    /**
     * {@inheritDoc}
     */
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        ICompletionService<PeakFinderWorkerResult> completionService
                = createCompletionService(PeakFinderWorkerResult.class);
        initProgress(2 * t.size());
        for (IFileFragment f : t) {
            CwtTicPeakFinderCallable cwt = new CwtTicPeakFinderCallable();
            cwt.setInput(f.getUri());
            cwt.setOutput(createWorkFragment(f).getUri());
            cwt.setMinScale(minScale);
            cwt.setMaxScale(maxScale);
            cwt.setMinPercentile(minPercentile);
            cwt.setIntegratePeaks(integratePeaks);
            cwt.setStoreScaleogram(saveGraphics);
            cwt.setPeakNormalizers(peakNormalizers);
            cwt.setTotalIntensityVar(totalIntensityVar);
            completionService.submit(cwt);
            getWorkflow().append(getProgress().nextStep());
        }
        try {
            List<URI> resultsUris = new ArrayList<>();
            List<PeakFinderWorkerResult> results = completionService.call();
            for (PeakFinderWorkerResult res : results) {
                resultsUris.add(res.getResultUri());
                for (WorkflowResult wf : res.getWorkflowResults()) {
                    getWorkflow().append(
                            new DefaultWorkflowResult(
                                    wf.getResource(),
                                    this,
                                    wf.getWorkflowSlot(),
                                    wf.getResources()
                            )
                    );
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
}
