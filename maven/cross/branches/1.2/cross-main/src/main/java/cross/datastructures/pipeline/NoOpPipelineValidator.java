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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author nilshoffmann
 */
@Data
@Slf4j
public class NoOpPipelineValidator implements IPipelineValidator {

    private List<IFragmentCommand> fragmentCommands;
    private TupleND<IFileFragment> inputFragments;
    
    @Override
    public void validate() throws ConstraintViolationException {
        
    }
    
}
