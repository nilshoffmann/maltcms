/*
 * $license$
 *
 * $Id$
 */

package cross.io.cli;

import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

/**
 *
 * @author nilshoffmann
 */
public interface ICliOptionHandler {

    public List<Option> getOptionList();

    /**
     * Startup related handlers should receive a negative integer number > Integer.MIN_VALUE.
     * Integer.MIN_VALUE is reserved for CommandLineHandler.
     * 
     * These handlers could abort normal startup, if a certain input is detected,
     * such as "-h" or no input at all.
     * 
     * Other cli handlers should provide numbers >=0. For handlers with the same 
     * priority, the order of invocation is undefined. 
     * 
     * Handlers will be invoked in ascending priority order, however, they should not depend on the order of initialization.
     * @return 
     */
    public int getPriority();

    /**
     * Handles the given option. The passed in options will be one of those returned 
     * by @see #getOptionList() .
     * @param option 
     */
    public void handleOption(Option option);
    
    /**
     * Whether the invocation of this option handler should terminate the running program.
     * Useful for showing options and stopping execution for further user actions.
     * The exit code of the programm will be non-zero.
     * @return 
     */
    public boolean isTerminateAfterInvocation();
    
    /**
     * Allows to freely define option groups.
     * An option group allows only one active option within its group.
     * It is currently not possible to define a group as being required.
     * However, individual options may be defined to be required.
     * @return 
     */
    public String getGroup();
    
}
