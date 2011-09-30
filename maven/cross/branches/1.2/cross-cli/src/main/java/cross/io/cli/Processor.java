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

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * Processor is a class, which provides access 
 * to registered handlers for command line options. These handlers 
 * need to be present on the classpath given to the application on 
 * startup. Handlers are discovered using the Java service provider
 * mechanism by registration in the jar file's META-INF/services folder.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
@Slf4j
@Data
public class Processor {

    private Options options;
    private List<IOptionHandler> optionHandlers;
    private HashMap<Option, IOptionHandler> optionToHandler;

    public void processCommandLine(String[] args) {
        final CommandLineParser clp = new GnuParser();
        final CommandLine cl;
        try {
            cl = clp.parse(options, args);
            final Option[] opts = cl.getOptions();
            final TreeMap<IOptionHandler, Option> tm = new TreeMap<IOptionHandler, Option>(
                    new OptionHandlerComparator());
            for (Option opt : opts) {
                if (optionToHandler.containsKey(opt)) {
                    tm.put(optionToHandler.get(opt), opt);
                } else {
                    log.warn("Option {} was not claimed by any handler!", opt);
                }
            }
            for (IOptionHandler handler : tm.keySet()) {
                log.debug("Handling option with {}",
                        handler.getClass().getName());
                handler.handleOption(tm.get(handler));
                if (handler.isTerminateAfterInvocation()) {
                    System.exit(1);
                }
            }
        } catch (ParseException ex) {
            log.error("Parsing of command line failed!", ex);
        }
    }
}