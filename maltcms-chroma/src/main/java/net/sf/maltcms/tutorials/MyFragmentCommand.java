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
package net.sf.maltcms.tutorials;

import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.BatchFilter;
import maltcms.commands.filters.array.MultiplicationFilter;
import maltcms.commands.filters.array.SavitzkyGolayFilter;

/**
 *
 * @author Nils Hoffmann
 */
@RequiresVariables(names = {"var.total_intensity"})
@ProvidesVariables(names = {"var.total_intensity"})
@Data
public class MyFragmentCommand extends AFragmentCommand {

    @Configurable(description = "List of filters to be applied to var.total_intensity")
    private List<AArrayFilter> filter = new LinkedList<AArrayFilter>(Arrays.asList(new MultiplicationFilter(1.0d)));

    @Override
    public String getDescription() {
        return "Applies a list of filters sequentially to var.total_intensity.";
    }

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
        //initialize progress
        initProgress(in.size());
        //create a savitzky golay filter (local polynomial smoothing) for 2*10 +1 = 21 points
        SavitzkyGolayFilter sgf = new SavitzkyGolayFilter(10);
        filter = new LinkedList<>();
        filter.add(sgf);
        TupleND<IFileFragment> out = createWorkFragments(in);
        for (int i = 0; i < in.size(); i++) {
            //the input fragment
            IFileFragment inputFragment = in.get(i);
            //the output fragment
            IFileFragment outputFragment = out.get(i);
            //updated the progress
            getProgress().nextStep();
            //retrieve the total_intensity variable
            IVariableFragment inTicVar = inputFragment.getChild(resolve("var.total_intensity"));
            //create a compatible output variable with same dimensions and data type
            IVariableFragment outTicVar = VariableFragment.createCompatible(outputFragment, inTicVar);
            //set the array after applying a batch filter
            outTicVar.setArray(BatchFilter.applyFilters(inTicVar.getArray(), filter));
            //save the corresponding output file fragment
            outputFragment.save();
            //add the result to the workflow
            addWorkflowResult(outputFragment);
        }
        return out;
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }

}
