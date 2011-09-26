/*
 * 
 *
 * $Id$
 */

package cross.io.cli;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openide.util.Lookup;

/**
 *
 * @author nilshoffmann
 */
@Slf4j
public class CommandLineHandler {

    private Options options = new Options();
    private final SortedSet<ICliOptionHandler> optionHandlers;
    private final LinkedHashMap<Option,ICliOptionHandler> optionToHandler;

    public static SortedSet<ICliOptionHandler> getOptionHandlers() {
        //Lookup and register ICliOptionHandler instances
        Collection<? extends ICliOptionHandler> handlers = Lookup.getDefault().
                lookupAll(ICliOptionHandler.class);
        SortedSet<ICliOptionHandler> optionHandlers = new TreeSet<ICliOptionHandler>(
                new CliOptionHandlerComparator());
        optionHandlers.addAll(handlers);
        return optionHandlers;
    }   
    
    private static CommandLineHandler commandLineHandler = null;
    public static CommandLineHandler getInstance() {
        if(commandLineHandler==null) {
            commandLineHandler = new CommandLineHandler();
        }
        return commandLineHandler;
    }
    
    private CommandLineHandler() {
        optionHandlers = getOptionHandlers();
        optionToHandler = new LinkedHashMap<Option,ICliOptionHandler>();
        for (ICliOptionHandler handler : optionHandlers) {
            for(Option opt:handler.getOptionList()) {
                if(optionToHandler.containsKey(opt)) {
                    throw new IllegalArgumentException("Option "+opt+" already claimed by handler "+optionToHandler.get(opt).getClass().getName()+" with priority "+optionToHandler.get(opt).getPriority());
                }
                optionToHandler.put(opt, handler);
                options.addOption(opt);
            }
        }
    }
    
    public Options getOptions() {
        return options;
    }

    public void handleCommandLine(String[] args) {
        final CommandLineParser clp = new GnuParser();
        final CommandLine cl;
        try {
            cl = clp.parse(options, args);
            final Option[] opts = cl.getOptions();
            final TreeMap<ICliOptionHandler,Option> tm = new TreeMap<ICliOptionHandler,Option>(new CliOptionHandlerComparator());
            for(Option opt:opts) {
                if(optionToHandler.containsKey(opt)) {
                    tm.put(optionToHandler.get(opt), opt);
                }else{
                    throw new IllegalArgumentException("Option "+opt+" not claimed by any handler!");
                }
            }
            for(ICliOptionHandler handler:tm.keySet()) {
                System.out.println("Handling option with "+handler.getClass().getName());
                handler.handleOption(tm.get(handler));
                if(handler.isTerminateAfterInvocation()) {
                    System.exit(1);
                }
            }
        } catch (ParseException ex) {
            Logger.getLogger(CommandLineHandler.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
}
