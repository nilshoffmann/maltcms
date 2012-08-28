/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package cross.datastructures.workflow;

import cross.IConfigurable;
import cross.datastructures.pipeline.ICommandSequence;
import java.util.Date;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author nilshoffmann
 */
public interface IWorkflowFactory extends IConfigurable {

    /**
     *
     * @return
     */
    IWorkflow getCurrentWorkflowInstance();

    /**
     * Create a new IWorkflow instance with default name workflow.
     *
     * @param startup
     * @param ics
     * @return
     */
    IWorkflow getDefaultWorkflowInstance(final Date startup, final ICommandSequence ics);

    /**
     * Create a new DefaultWorkflow instance with custom name, if it does not
     * exist already. Otherwise returns the existing instance. Use this, if you
     * will only create one Workflow per VM instance. Otherwise use
     * getNewWorkflowInstance to create a new DefaultWorkflow instance with
     * custom configuration.
     *
     * @param startup
     * @param name
     * @param ics
     * @return
     */
    IWorkflow getDefaultWorkflowInstance(final Date startup, final String name, final ICommandSequence ics);

    /**
     *
     * @param startup
     * @param name
     * @param ics
     * @param cfg
     * @return
     */
    IWorkflow getNewWorkflowInstance(final Date startup, final String name, final ICommandSequence ics, final Configuration cfg);
}
