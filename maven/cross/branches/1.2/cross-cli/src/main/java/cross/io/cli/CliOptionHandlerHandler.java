/*
 * 
 *
 * $Id$
 */

package cross.io.cli;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author nilshoffmann
 */
@ServiceProvider(service=ICliOptionHandler.class)
@Slf4j
public class CliOptionHandlerHandler implements ICliOptionHandler {
    
    public static final String GROUP = "DEFAULT";
    
    @Override
    public List<Option> getOptionList() {
        Option listCommandLineHandlers = OptionBuilder.withLongOpt("list-cli-handlers").withDescription("List available command line option handlers.").create();
        return Arrays.asList(listCommandLineHandlers);
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void handleOption(Option cli) {
        log.info("The following command line option handlers are registered: ");
        for(ICliOptionHandler handler:CommandLineHandler.getOptionHandlers()) {
            log.info("Name: {}",handler.getClass().getName());
            log.info("Priority: {}",handler.getPriority());
            log.info("Options: {}",handler.getOptionList());
        }
    }

    @Override
    public boolean isTerminateAfterInvocation() {
        return true;
    }

    @Override
    public String getGroup() {
        return GROUP;
    }
}
