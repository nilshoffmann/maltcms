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
