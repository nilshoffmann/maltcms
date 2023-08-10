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
package cross.datastructures.workflow;

import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import org.slf4j.LoggerFactory;



@RequiresVariables(names = {"nil.variable2", "nil.variable5"})
@RequiresOptionalVariables(names = {"nil.variable4"})
public class FragmentCommandMockC extends AFragmentCommand {
        
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FragmentCommandMockC.class);

    /**
     *
     */
    private static final long serialVersionUID = 7454449407054696377L;

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
        log.info("Running {}", getClass().getName());
        TupleND<IFileFragment> out = createWorkFragments(in);
        for (IFileFragment frag : out) {
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
        return "This is a mock c command";
    }
}
