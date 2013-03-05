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

/**
 *
 *
 * @author Nils Hoffmann
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
