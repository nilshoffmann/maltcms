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
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.peakfinding.cwtEicPeakFinder.CwtTicPeakFinderCallable;
import maltcms.commands.fragments2d.peakfinding.CwtChartFactory;
import maltcms.tools.ImageTools;
import net.sf.mpaxs.api.ICompletionService;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.ArrayDouble;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class CwtTicPeakFinder extends AFragmentCommand {

    private final String description = "Finds TIC peaks using Continuous Wavelet Transform.";
    private final WorkflowSlot workflowSlot = WorkflowSlot.PEAKFINDING;
    @Configurable
    private int minScale = 10;
    @Configurable
    private int maxScale = 100;
    @Configurable
    private double minPercentile = 5.0d;
    @Configurable
    private boolean integratePeaks = true;
    @Configurable
    private boolean integrateRawTic = true;
    @Configurable
    private boolean saveGraphics = false;

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        ICompletionService<URI> ics = createCompletionService(URI.class);
        initProgress(t.size() * 2);
        for (IFileFragment f : t) {
            CwtTicPeakFinderCallable cwt = new CwtTicPeakFinderCallable();
            cwt.setInput(f.getUri());
            cwt.setOutput(createWorkFragment(f).getUri());
            cwt.setMinScale(minScale);
            cwt.setMaxScale(maxScale);
            cwt.setMinPercentile(minPercentile);
            cwt.setIntegratePeaks(integratePeaks);
            cwt.setIntegrateRawTic(integrateRawTic);
            cwt.setStoreScaleogram(saveGraphics);
            ics.submit(cwt);
            getProgress().nextStep();
        }
        try {
            List<URI> results = ics.call();
            TupleND<IFileFragment> resultFragments = new TupleND<>();
            for (URI file : results) {
                FileFragment f = new FileFragment(file);
                if (saveGraphics) {
                    BufferedImage bi = CwtChartFactory.createColorHeatmap((ArrayDouble.D2) f.getChild("cwt_scaleogram").getArray());
                    ImageTools.saveImage(bi, "scaleogram-" + f.getName(), "png", getWorkflow().getOutputDirectory(), this, f);
                }
                resultFragments.add(f);
                getProgress().nextStep();
            }
            return resultFragments;
        } catch (Exception ex) {
            log.error("Caught exception while executing workers: ", ex);
            throw new RuntimeException(ex);
        }
    }
}
