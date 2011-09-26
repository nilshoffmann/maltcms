/*
 * $license$
 *
 * $Id$
 */

package cross.datastructures.workflow;

import cross.io.xml.IXMLSerializable;

/**
 * A result of a {@link cross.datastructures.workflow.IWorkflowElement}, linking
 * to a created file or immediate resources (e.g. statistics).
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
public interface IWorkflowResult extends IXMLSerializable {

	public IWorkflowElement getWorkflowElement();

	public WorkflowSlot getWorkflowSlot();

	public void setWorkflowElement(IWorkflowElement iwe);

	public void setWorkflowSlot(WorkflowSlot ws);

}
