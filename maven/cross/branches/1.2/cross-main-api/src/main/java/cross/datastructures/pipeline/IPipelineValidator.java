/*
 * $license$
 *
 * $Id$
 */

package cross.datastructures.pipeline;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.exception.ConstraintViolationException;
import java.util.List;

/**
 *
 * @author nilshoffmann
 */
public interface IPipelineValidator {
    
    public void setFragmentCommands(List<IFragmentCommand> fragmentCommands);
    
    public List<IFragmentCommand> getFragmentCommands();
    
    public void setInputFragments(TupleND<IFileFragment> inputFragments);
    
    public TupleND<IFileFragment> getInputFragments();
    
    public void validate() throws ConstraintViolationException;
}
