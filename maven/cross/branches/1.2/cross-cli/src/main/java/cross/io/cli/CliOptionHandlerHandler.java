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

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 * ICliOptionHandler implementation that registers for the --list-cli-handlers
 * command line argument. Prints a list of available implementations of 
 * ICliOptionHandler to System.out
 * and indicates that execution should be stopped after any of its valid 
 * arguments have been processed.
 * 
 * This handler belongs to the handler group DEFAULT.
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
