/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: DefaultWorkflowProgressResult.java 73 2009-12-16 08:45:14Z nilshoffmann
 * $
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
