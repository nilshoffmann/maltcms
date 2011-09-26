/**
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 *
 * This file is part of Cross/Maltcms.
 *
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 */
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
 * CommandLineHandler is a singleton class, which provides access 
 * to registered handlers for command line options. These handlers 
 * need to be present on the classpath given to the application on 
 * startup. Handlers are discovered using the Java service provider
 * mechanism by registration in the jar file's META-INF/services folder.
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
                    log.warn("Option {} was not claimed by any handler!",opt);
                }
            }
            for(ICliOptionHandler handler:tm.keySet()) {
                log.debug("Handling option with {}",handler.getClass().getName());
                handler.handleOption(tm.get(handler));
                if(handler.isTerminateAfterInvocation()) {
                    System.exit(1);
                }
            }
        } catch (ParseException ex) {
            log.error("Parsing of command line failed!", ex);
        }
    }
}
