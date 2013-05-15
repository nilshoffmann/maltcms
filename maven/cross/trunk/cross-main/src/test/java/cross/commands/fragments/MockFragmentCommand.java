/*
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.commands.fragments;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;

/**
 *
 * @author Nils Hoffmann
 */
public class MockFragmentCommand extends AFragmentCommand {

    @Override
    public String getDescription() {
        return "Just a mock fragment command";
    }

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
        TupleND<IFileFragment> out = createWorkFragments(in);
        for (IFileFragment iFileFragment : out) {
//            Random r = new Random(System.nanoTime());
//            VariableFragment vf1 = new VariableFragment(iFileFragment, "vf1");
//            int length = 121;
//            Dimension d = new Dimension("vf1-dimension", length, true);
//            vf1.setDimensions(new Dimension[]{d});
//            vf1.setAttributes(new Attribute("vf1-attribute", 834.22));
//            Array a1 = ArrayTools.random(r, double.class, new int[]{d.getLength()});
//            vf1.setArray(a1);
//            VariableFragment vf2 = new VariableFragment(iFileFragment, "vf2");
//            vf2.setDimensions(new Dimension[]{d});
//            Dimension d2 = new Dimension("vf2-2dimension", 24, false);
//            Array a2 = ArrayTools.random(r, int.class, new int[]{d.getLength(), d2.getLength()});
//            iFileFragment.save();
        }
        return out;
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }
}
