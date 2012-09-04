/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.datastructures.workflow;

import cross.IConfigurable;
import cross.datastructures.pipeline.ICommandSequence;
import java.util.Date;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author Nils Hoffmann
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
