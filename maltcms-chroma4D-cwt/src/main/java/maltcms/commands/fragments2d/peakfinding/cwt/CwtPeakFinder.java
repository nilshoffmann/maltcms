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
package maltcms.commands.fragments2d.peakfinding.cwt;

import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.io.File;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.mpaxs.api.ICompletionService;
import org.openide.util.lookup.ServiceProvider;

/**
 * Detects peaks within the 1D TIC of a 2D chromatogram and
 * selects peaks that have fewer than <code>maxNeighbors</code> in
 * <code>radius</code>.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class CwtPeakFinder extends AFragmentCommand {

    @Configurable(value = "5", description = "The minimum required scale for a ridge.")
    private int minScale = 5;
    @Configurable(value = "20", description = "The maximum scale to calculate the Continuous Wavelet Transform for.")
    private int maxScale = 20;
    @Configurable(value = "", description = "The output directory. Should be an absolute file path.")
    private String outputDir = "";
    @Configurable(name = "var.modulation_time.default",
        value = "5.0d", description = "The modulation time. Default value is var.modulation_time.default.")
    private double modulationTime = 5.0d;
    @Configurable(name = "var.scan_rate.default", type = double.class,
        value = "100.0d", description = "The scan rate. Default value is var.scan_rate.default.")
    private double scanRate = 100.0d;
    @Configurable(description = "The maximum radius around a peak to search for neighboring peaks.")
    private double radius = 10.0d;
    @Configurable(description = "The maxmimum number of neighbors expected in the given radius.")
    private int maxNeighbors = 15;
    @Configurable(description = "Whether the scaleogram image should be saved.")
    private boolean saveScaleogramImage = false;
    @Configurable(description = "Whether the quad tree image should be saved.")
    private boolean saveQuadTreeImage = false;
    @Configurable(description = "Whether the 2D TIC ridge overlay images should be saved, before and after filtering.")
    private boolean saveRidgeOverlayImages = false;
    @Configurable(description = "The maximum number of ridges to report. Actual number of ridges reported may be lower, depending on the other parameters.")
    private int maxRidges = 5000;
    @Configurable(description = "Percentile of the intensity value distribution to use as minimum intensity for peaks.")
    private int minPercentile = 95;

    @Override
    public String getDescription() {
        return "Finds peak locations in intensity profiles using the continuous wavelet transform.";
    }

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        ICompletionService<File> ics = createCompletionService(File.class);
        for (IFileFragment f : t) {
            CwtRunnable cwt = new CwtRunnable();
            cwt.setMinScale(minScale);
            cwt.setMaxScale(maxScale);
            cwt.setOutputDir(getWorkflow().getOutputDirectory(this).getAbsolutePath());
            cwt.setModulationTime(modulationTime);
            cwt.setScanRate(scanRate);
            cwt.setRadius(radius);
            cwt.setMaxNeighbors(maxNeighbors);
            cwt.setSaveScaleogramImage(saveScaleogramImage);
            cwt.setSaveQuadTreeImage(saveQuadTreeImage);
            cwt.setSaveRidgeOverlayImages(saveRidgeOverlayImages);
            cwt.setMaxRidges(maxRidges);
            cwt.setInputFile(f.getUri());
            log.info("Running {}", cwt);
            ics.submit(cwt);
        }
        return postProcess(ics, t);
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.PEAKFINDING;
    }
}
