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

import java.util.List;
import org.apache.commons.cli.Option;

/**
 *
 * Objects which require input from the command line can register themselves
 * with the CommandLineHandler by implementing this interface and exposing 
 * @ServiceProvider(service=IOptionHandler.class) 
 * (requires org-openide-util-lookup-RELEASE701.jar as a dependency)
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public interface IOptionHandler {
    
    public void setProcessor(Processor processor);
    
    public Processor getProcessor();

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
