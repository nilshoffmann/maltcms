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
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.io.File;
import lombok.Data;

import net.sf.mpaxs.api.ICompletionService;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.LoggerFactory;

/**
 * Detects peaks within the 1D TIC of a 2D chromatogram and selects peaks that
 * have fewer than <code>maxNeighbors</code> in <code>radius</code>.
 *
 * @author Nils Hoffmann
 *
 */
@RequiresVariables(names = {"var.modulation_time", "var.scan_rate"})
@ProvidesVariables(names = {"var.peak_index_list"})
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class CwtPeakFinder extends AFragmentCommand {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CwtPeakFinder.class);

    @Configurable(value = "5", description = "The minimum required scale for a ridge.")
    private int minScale = 5;
    @Configurable(value = "20", description = "The maximum scale to calculate the Continuous Wavelet Transform for.")
    private int maxScale = 20;
    @Configurable(name = "var.modulation_time.default",
            value = "5.0d", description = "The modulation time. Default value is var.modulation_time.default.")
    private double modulationTime = 5.0d;
    @Configurable(name = "var.scan_rate.default", type = double.class,
            value = "100.0d", description = "The scan rate. Default value is var.scan_rate.default.")
    private double scanRate = 100.0d;
    @Configurable(description = "The maximum radius around a peak to search for neighboring peaks.",
            value = "10.0d")
    private double radius = 10.0d;
    @Configurable(description = "The maxmimum number of neighbors expected in the given radius.")
    private int maxNeighbors = 15;
    @Configurable(description = "If true, the scaleogram image will be saved.")
    private boolean saveScaleogramImage = false;
    @Configurable(description = "If true, the quad tree image will be saved.")
    private boolean saveQuadTreeImage = false;
    @Configurable(description = "If true, the 2D TIC ridge overlay images will be saved, before and after filtering.")
    private boolean saveRidgeOverlayImages = false;
    @Configurable(description = "The maximum number of ridges to report. Actual number of ridges reported may be lower, depending on the other parameters.")
    private int maxRidges = 5000;
    @Configurable(description = "Percentile of the intensity value distribution to use as minimum intensity for peaks.")
    private int minPercentile = 95;
    @Configurable(name = "var.peak_index_list", value = "peak_index_list")
    private String peakListVar = "peak_index_list";

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Configuration config) {
        this.peakListVar = config.getString("var.peak_index_list", "peak_index_list");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Finds peak locations in intensity profiles using the continuous wavelet transform.";
    }

    /**
     * {@inheritDoc}
     */
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
            cwt.setMinPercentile(minPercentile);
            log.info("Running {}", cwt);
            ics.submit(cwt);
        }
        return postProcess(ics, t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.PEAKFINDING;
    }
}
