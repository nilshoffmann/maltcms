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
package cross.io.cli.handlers;

import cross.io.IApplicationInfo;
import cross.io.cli.Processor;
import cross.io.cli.IOptionHandler;
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
 * IOptionHandler implementation that registers for -h,-? and --help command
 * line arguments. Prints a list of available command line options to System.out
 * and indicates that execution should be stopped after any of its valid 
 * arguments have been processed.
 * 
 * This handler belongs to the handler group DEFAULT.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
@ServiceProvider(service = IOptionHandler.class)
@Slf4j
@Data
public class Help implements IOptionHandler {

    public static final String GROUP = "DEFAULT";
    private int priority = Integer.MIN_VALUE;
    private Processor processor;

    @Override
    public List<Option> getOptionList() {
        Option showHelp =
                OptionBuilder.withLongOpt("help").
                withArgName("?").
                withArgName("h").
                withDescription("List available command line option handlers.").
                create();
        return Arrays.asList(showHelp);
    }

    @Override
    public void handleOption(Option cli) {
        final HelpFormatter formatter = new HelpFormatter();
        IApplicationInfo appInfo = Lookup.getDefault().lookup(
                IApplicationInfo.class);
        formatter.printHelp(
                "call " + appInfo.getName()
                + " with the following arguments", " Version "
                + appInfo.getVersion(),
                processor.getOptions(), appInfo.getLicense());
    }

    @Override
    public boolean isTerminateAfterInvocation() {
        return true;
    }

    @Override
    public String getGroup() {
        return GROUP;
    }

    @Override
    public String toString() {
        return "Help{" + "priority=" + priority + ", group=" + GROUP + '}';
    }
}