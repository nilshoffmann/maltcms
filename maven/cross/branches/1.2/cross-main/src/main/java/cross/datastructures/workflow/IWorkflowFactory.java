/*
 * $license$
 *
 * $Id$
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
