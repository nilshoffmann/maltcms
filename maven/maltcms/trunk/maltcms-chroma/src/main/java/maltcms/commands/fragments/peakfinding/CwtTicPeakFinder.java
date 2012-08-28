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
package maltcms.commands.fragments.peakfinding;

import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.awt.image.BufferedImage;
import java.io.File;
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
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
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
        ICompletionService<File> ics = createCompletionService(File.class);
        initProgress(t.size() * 2);
        for (IFileFragment f : t) {
            CwtTicPeakFinderCallable cwt = new CwtTicPeakFinderCallable();
            cwt.setInput(new File(f.getAbsolutePath()));
            cwt.setOutput(new File(createWorkFragment(f).getAbsolutePath()));
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
            List<File> results = ics.call();
            TupleND<IFileFragment> resultFragments = new TupleND<IFileFragment>();
            for (File file : results) {
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
