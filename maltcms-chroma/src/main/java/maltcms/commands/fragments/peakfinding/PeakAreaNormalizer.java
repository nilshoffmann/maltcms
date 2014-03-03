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
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;

/**
 * Separate class for peak normalization of already annotated peaks. The {@link  TICPeakFinder}
 * class performs the same normalization if configured with the same <code>peakNormalizers</code>.
 *
 * @author Nils Hoffmann
 */
@RequiresVariables(names = {"var.peak_area"})
@ProvidesVariables(names = {"var.peak_area_normalized"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class PeakAreaNormalizer extends AFragmentCommand {

    @Configurable
    private List<IPeakNormalizer> peakNormalizers = Collections.emptyList();

    @Override
    public String getDescription() {
        return "Normalizes peak areas using user-defineable normalization methods.";
    }

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
        TupleND<IFileFragment> results = new TupleND<>();
        for (IFileFragment f : in) {
            //init work data
            IFileFragment result = createWorkFragment(f);
            log.info("Source file of result: {}", result.getSourceFiles());
            Array area = f.getChild(resolve("var.peak_area")).getArray();
            final Dimension peak_number = new Dimension("peak_number",
                area.getShape()[0], true, false, false);
            final Dimension _1024_byte_string = new Dimension("_1024_byte_string", 1024, true, false, false);
            final Dimension peak_normalizers = new Dimension("peak_normalizer_count", peakNormalizers.isEmpty() ? 1 : peakNormalizers.size(), true, false, false);
            //set names of normalization methods
            Array normalizedAreaArray = Array.factory(DataType.getType(area.getElementType()), area.getShape());
            ArrayChar.D2 normalizationMethodArray = cross.datastructures.tools.ArrayTools.createStringArray(
                Math.max(1, peakNormalizers.size()), 1024);
            if (peakNormalizers.isEmpty()) {
                normalizationMethodArray.setString(0, "None");
            } else {
                for (int i = 0; i < peakNormalizers.size(); i++) {
                    normalizationMethodArray.setString(i, peakNormalizers.get(i).getNormalizationName());
                }
            }
            //normalize area values
            for (int i = 0; i < area.getShape()[0]; i++) {
                double normalizedArea = area.getDouble(i);
                for (IPeakNormalizer normalizer : peakNormalizers) {
                    normalizedArea *= normalizer.getNormalizationFactor(f, i);
                }
                normalizedAreaArray.setDouble(i, normalizedArea);
            }
            //normalized peak area (may be not normalized if peakNormalizers is empty
            IVariableFragment peakAreaNormalized = result.addChild(resolve("var.peak_area_normalized"));
            peakAreaNormalized.setDimensions(new Dimension[]{peak_number});
            peakAreaNormalized.setArray(normalizedAreaArray);
            //names of used methods
            IVariableFragment peakNormalizationMethod = result.addChild("peak_area_normalization_methods");
            peakNormalizationMethod.setDimensions(new Dimension[]{peak_normalizers, _1024_byte_string});
            peakNormalizationMethod.setArray(normalizationMethodArray);
            //save
            result.save();
            //add result
            results.add(result);
        }
        return results;
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.PEAKFINDING;
    }
}
