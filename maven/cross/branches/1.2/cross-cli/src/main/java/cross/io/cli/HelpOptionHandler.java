/*
 * 
 *
 * $Id$
 */

package cross.io.cli;

import cross.IApplicationInfo;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author nilshoffmann
 */
@ServiceProvider(service=ICliOptionHandler.class)
@Slf4j
@Data
public class HelpOptionHandler implements ICliOptionHandler {
    
    public static final String GROUP = "DEFAULT";
    
    private int priority = Integer.MIN_VALUE;
    
    @Override
    public List<Option> getOptionList() {
        Option showHelp = 
                OptionBuilder.withLongOpt("help").
                withArgName("-?").
                withArgName("-h").
                withDescription("List available command line option handlers.").
                create();
        return Arrays.asList(showHelp);
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void handleOption(Option cli) {
        final HelpFormatter formatter = new HelpFormatter();
        IApplicationInfo appInfo = Lookup.getDefault().lookup(IApplicationInfo.class);
        formatter.printHelp(
                "call " + appInfo.getName()
                + " with the following arguments", " Version "
                + appInfo.getVersion(),
                CommandLineHandler.getInstance().getOptions(), appInfo.getLicense());
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
