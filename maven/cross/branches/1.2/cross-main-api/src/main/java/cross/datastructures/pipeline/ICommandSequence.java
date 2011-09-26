/*
 * $license$
 *
 * $Id$
 */

package cross.datastructures.pipeline;

import java.util.Collection;
import java.util.Iterator;

import cross.IConfigurable;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowResult;
import cross.event.IEvent;
import cross.event.IListener;
import cross.io.xml.IXMLSerializable;

/**
 * Abstract sequence of commands on FileFragment objects.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface ICommandSequence extends Iterator<TupleND<IFileFragment>>,
        IListener<IEvent<IWorkflowResult>>, IXMLSerializable {

    public abstract Collection<IFragmentCommand> getCommands();

    /**
     * Return input to this ICommandSequence.
     *
     * @return
     */
    public abstract TupleND<IFileFragment> getInput();

    public abstract IWorkflow getWorkflow();

    /**
     * Do we have any unprocessed Commands left?
     */
    @Override
    public abstract boolean hasNext();

    public abstract void init();

    /**
     * Apply the next ICommand and return results.
     */
    @Override
    public abstract TupleND<IFileFragment> next();

    @Override
    public abstract void remove();

    public abstract void setCommands(Collection<IFragmentCommand> c);

    public abstract void setInput(TupleND<IFileFragment> t);

    public abstract void setWorkflow(IWorkflow iw);
    
    public abstract boolean isCheckCommandDependencies();
    
    public abstract void setCheckCommandDependencies(boolean checkCommandDependencies);

}
