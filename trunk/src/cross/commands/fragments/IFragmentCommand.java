/**
 * 
 */
package cross.commands.fragments;

import cross.commands.ICommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.IWorkflowResult;
import cross.event.IEventSource;

/**
 * Cover interface (c++: typedef) for AFragmentCommand.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
public interface IFragmentCommand extends
        ICommand<TupleND<IFileFragment>, TupleND<IFileFragment>>,
        IEventSource<IWorkflowResult>, IWorkflowElement {

}
