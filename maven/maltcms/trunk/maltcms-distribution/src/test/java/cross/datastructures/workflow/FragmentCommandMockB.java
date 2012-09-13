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
package cross.datastructures.workflow;

import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiresVariables(names = {"variable1", "variable2"})
@RequiresOptionalVariables(names = {"variable3"})
@ProvidesVariables(names = {"variable2", "variable5"})
public class FragmentCommandMockB extends AFragmentCommand {

    /**
     *
     */
    private static final long serialVersionUID = 7454449407054696377L;

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
        log.info("Running {}", getClass().getName());
        TupleND<IFileFragment> out = createWorkFragments(in);
        for (IFileFragment frag : out) {
            IVariableFragment ivf1 = frag.getChild("variable1");
            VariableFragment v4 = new VariableFragment(frag, "variable4");
            v4.setArray(ivf1.getArray());
            IVariableFragment ivf2 = frag.getChild("variable2");
            ivf2.getArray();
            VariableFragment v5 = new VariableFragment(frag, "variable5");
            List<Array> indexedList = new ArrayList<Array>();
            for (int i = 0; i < 10; i++) {
                indexedList.add(new ArrayDouble.D1(20));
            }
            v5.setIndexedArray(indexedList);
            frag.save();
        }
        return out;
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.FILEIO;
    }

    @Override
    public String getDescription() {
        return "This is a mock b command";
    }
}
