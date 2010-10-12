/**
 * 
 */
package cross.datastructures.workflow;

import cross.tools.EvalTools;

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

	private int currentStep;

	private int numberOfSteps;

	private int overallProgress;

	private int[] progressInStep;

	private String[] stepNames;

	public DefaultWorkflowProgressResult(final IWorkflowElement iwe,
	        final WorkflowSlot ws) {
		this.iwe = iwe;
		this.ws = ws;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowResult#getIWorkflowElement()
	 */
	@Override
	public IWorkflowElement getIWorkflowElement() {
		return this.iwe;
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
	 * @see
	 * cross.datastructures.workflow.IWorkflowResult#setIWorkflowElement(cross
	 * .datastructures.workflow.IWorkflowElement)
	 */
	@Override
	public void setIWorkflowElement(IWorkflowElement iwe) {
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
	public void setWorkflowSlot(WorkflowSlot ws) {
		EvalTools.notNull(ws, this);
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
		return this.overallProgress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.datastructures.workflow.IWorkflowProgressResult#getProgressInStep
	 * (int)
	 */
	@Override
	public int getProgressInStep(int i) {
		return this.progressInStep[i];
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
	 * @see cross.datastructures.workflow.IWorkflowProgressResult#nextStep()
	 */
	@Override
	public void nextStep() {
		if (this.currentStep < this.stepNames.length - 1) {
			this.currentStep++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.datastructures.workflow.IWorkflowProgressResult#setCurrentStep(int)
	 */
	@Override
	public void setCurrentStep(int j) {
		this.currentStep = j;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.datastructures.workflow.IWorkflowProgressResult#setOverallProgress
	 * (int)
	 */
	@Override
	public void setOverallProgress(int i) {
		this.overallProgress = i;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.datastructures.workflow.IWorkflowProgressResult#setProgressInStep
	 * (int, int)
	 */
	@Override
	public void setProgressInStep(int j, int i) {
		this.progressInStep[j] = i;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.datastructures.workflow.IWorkflowProgressResult#setStepNames(java
	 * .lang.String[])
	 */
	@Override
	public void setStepNames(String[] s) {
		this.stepNames = s;
	}

}
