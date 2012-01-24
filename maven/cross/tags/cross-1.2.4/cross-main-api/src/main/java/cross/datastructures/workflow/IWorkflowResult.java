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
 * $Id: IWorkflowResult.java 116 2010-06-17 08:46:30Z nilshoffmann $
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
