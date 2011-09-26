/*
 * 
 *
 * $Id$
 */

package cross.datastructures.workflow;

import org.jdom.Element;

import cross.datastructures.tools.EvalTools;

/**
 * To avoid numerous instances of this object, you should only instantiate it
 * once per IWorkflowElement and then update its state, before dispatching it
 * with an event.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
public class DefaultWorkflowProgressResult implements IWorkflowProgressResult {

	private IWorkflowElement iwe;

	private WorkflowSlot ws;

	private int currentStep = 0;

	private final int numberOfSteps;

	private String[] stepNames = new String[] { "" };

	public DefaultWorkflowProgressResult(final String[] stepNames,
	        final IWorkflowElement iwe, final WorkflowSlot ws) {
		this.stepNames = stepNames;
		this.numberOfSteps = this.stepNames.length;
		this.iwe = iwe;
		this.ws = ws;
	}

	public DefaultWorkflowProgressResult(final int numberOfSteps,
	        final IWorkflowElement iwe, final WorkflowSlot ws) {
		this.numberOfSteps = numberOfSteps;
		this.iwe = iwe;
		this.ws = ws;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.datastructures.workflow.IWorkflowProgressResult#getCurrentStep()
	 */
	@Override
	public int getCurrentStep() {
		return this.currentStep;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowResult#getWorkflowElement()
	 */
	@Override
	public IWorkflowElement getWorkflowElement() {
		return this.iwe;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.datastructures.workflow.IWorkflowProgressResult#getNumberOfSteps()
	 */
	@Override
	public int getNumberOfSteps() {
		return this.numberOfSteps;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.datastructures.workflow.IWorkflowProgressResult#getOverallProgress
	 * ()
	 */
	@Override
	public int getOverallProgress() {
		return (int) (100.0f * ((float) this.currentStep / (float) this.numberOfSteps));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowProgressResult#getStepNames()
	 */
	@Override
	public String[] getStepNames() {
		return this.stepNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowResult#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return this.ws;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowProgressResult#nextStep()
	 */
	@Override
	public IWorkflowProgressResult nextStep() {
		if (this.currentStep < this.stepNames.length - 1) {
			this.currentStep++;
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.datastructures.workflow.IWorkflowResult#setWorkflowElement(cross
	 * .datastructures.workflow.IWorkflowElement)
	 */
	@Override
	public void setWorkflowElement(final IWorkflowElement iwe) {
		EvalTools.notNull(iwe, this);
		this.iwe = iwe;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecross.datastructures.workflow.IWorkflowResult#setWorkflowSlot(cross.
	 * datastructures.workflow.WorkflowSlot)
	 */
	@Override
	public void setWorkflowSlot(final WorkflowSlot ws) {
		EvalTools.notNull(ws, this);
		this.ws = ws;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.io.xml.IXMLSerializable#appendXML(org.jdom.Element)
	 */
	@Override
	public void appendXML(Element e) {
		// do nothing

	}

}
