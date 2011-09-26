/*
 * $license$
 *
 * $Id$
 */

package cross.datastructures.workflow;

import cross.io.xml.IXMLSerializable;

/**
 * A Workflow element.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
public interface IWorkflowElement extends IXMLSerializable {

	public abstract IWorkflow getWorkflow();

	public abstract WorkflowSlot getWorkflowSlot();

	public abstract void setWorkflow(IWorkflow iw);

}
