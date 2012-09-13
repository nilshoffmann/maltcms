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
package maltcms.commands.fragments.peakfinding;

import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.io.File;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.peakfinding.cwtEicPeakFinder.CwtEicPeakFinderCallable;
import maltcms.tools.ArrayTools;
import net.sf.mpaxs.api.ICompletionService;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
@RequiresVariables(names = {"var.binned_mass_values", "var.binned_intensity_values", "var.binned_scan_index"})
public class CwtEicPeakFinder extends AFragmentCommand {

    private final String description = "Finds EIC peaks using  Continuous Wavelet Transform.";
    private final WorkflowSlot workflowSlot = WorkflowSlot.PEAKFINDING;
    @Configurable(name = "var.binned_mass_values")
    private String binnedMassValues = "binned_mass_values";
    @Configurable(name = "var.binned_intensity_values")
    private String binnedIntensityValues = "binned_intensity_values";
    @Configurable(name = "var.binned_scan_index")
    private String binnedScanIndex = "binned_scan_index";

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        int cnt = 0;
        for (IFileFragment f : t) {
            IVariableFragment biv = f.getChild(binnedIntensityValues);
            IVariableFragment bmv = f.getChild(binnedMassValues);
            IVariableFragment scanIndex = f.getChild(binnedScanIndex);
            biv.setIndex(scanIndex);
            List<Array> eics = ArrayTools.tilt(biv.getIndexedArray());
            Array mzs = bmv.getIndexedArray().get(0);
            ICompletionService<File> ics = createCompletionService(File.class);
            for (int i = 0; i < eics.size(); i++) {
                Array eic = eics.get(i);
                CwtEicPeakFinderCallable cwt = new CwtEicPeakFinderCallable();
                cwt.setInput(new File(f.getAbsolutePath()));
                cwt.setEic((double[]) eic.get1DJavaArray(double.class));
                cwt.setMz(mzs.getDouble(i));
                cwt.setMinScale(5);
                ics.submit(cwt);
            }
            try {
                ics.call();
            } catch (Exception ex) {
                log.error("Caught exception while executing workers: ", ex);
                throw new RuntimeException(ex);
            }
            cnt++;
        }
        return t;
    }
}
