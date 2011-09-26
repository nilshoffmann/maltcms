/*
 * 
 *
 * $Id$
 */

package cross.datastructures.pipeline;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowResult;
import cross.event.IEvent;
import java.util.Collection;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;


/**
 *
 * @author nils
 */
@Slf4j
@Data
public class CommandPipelineBean implements ICommandSequence {

    private Collection<IFragmentCommand> commands;
    
    private TupleND<IFileFragment> input;
    
    private IWorkflow workflow;
    
    private boolean checkCommandDependencies;

    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init() {
        
    }

    @Override
    public TupleND<IFileFragment> next() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void listen(IEvent<IWorkflowResult> v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void appendXML(Element e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
