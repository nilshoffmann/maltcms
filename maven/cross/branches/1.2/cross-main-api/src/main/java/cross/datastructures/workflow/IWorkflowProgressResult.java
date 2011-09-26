/*
 * 
 *
 * $Id$
 */

package cross.datastructures.workflow;

/**
 * 
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public interface IWorkflowProgressResult extends IWorkflowResult {

	/**
	 * Returns the index of the currently active step.
	 * 
	 * @return
	 */
	public int getCurrentStep();

	/**
	 * Returns the number of defined steps in IWorkflowElement. Default is 1.
	 * 
	 * @return
	 */
	public int getNumberOfSteps();

	/**
	 * Returns the overall progress of an IWorkflowElement, range is from 0 (no
	 * progress yet) via 1 to 100 (complete)
	 * 
	 * @return
	 */
	public int getOverallProgress();

	/**
	 * Return string names of each step.
	 * 
	 * @return
	 */
	public String[] getStepNames();

	/**
	 * Advances the internal index by one, to the next step. Has no effect, if
	 * last step has been reached.
	 */
	public IWorkflowProgressResult nextStep();

}
