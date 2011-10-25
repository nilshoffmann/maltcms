/*
 * $license$
 *
 * $Id$
 */
package maltcms.commands.fragments2d.peakfinding;

import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class MockFragmentCommand extends AFragmentCommand {

    @Override
    public String getDescription() {
        return "This is just a mock command for testing!";
    }

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
        System.out.println("Running fragment command "+getClass().getName());
        return in;
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }
    
}
