/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
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
 * $Id$
 */

package cross.datastructures.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import cross.datastructures.tuple.Tuple2D;
import cross.tools.EvalTools;

/**
 * Default implementation of an <code>IWorkflowResult</code>.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
public class DefaultWorkflowResult implements IWorkflowFileResult {

	private File f = null;

	private WorkflowSlot ws = null;

	private IWorkflowElement iwe = null;

	private Collection<Tuple2D<String, String>> immRes = null;

	public DefaultWorkflowResult(final File f1, final IWorkflowElement iwe1,
	        final WorkflowSlot ws1) {
		EvalTools.notNull(new Object[]{f1,iwe1,ws1},this);
		this.f = f1;
		this.iwe = iwe1;
		this.ws = ws1;
	}

	@Override
	public File getFile() {
		return this.f;
	}

	@Override
	public Collection<Tuple2D<String, String>> getImmediateResults() {
		if (this.immRes == null) {
			this.immRes = new ArrayList<Tuple2D<String, String>>();
		}
		return this.immRes;
	}

	@Override
	public IWorkflowElement getIWorkflowElement() {
		return this.iwe;
	}

	@Override
	public WorkflowSlot getWorkflowSlot() {
		return this.ws;
	}

	@Override
	public void setFile(final File iff1) {
		EvalTools.notNull(iff1, this);
		this.f = iff1;
	}

	@Override
	public void setImmediateResult(final Collection<Tuple2D<String, String>> ir) {
		this.immRes = ir;
	}

	@Override
	public void setIWorkflowElement(final IWorkflowElement iwe1) {
		EvalTools.notNull(iwe1, this);
		this.iwe = iwe1;
	}

	@Override
	public void setWorkflowSlot(final WorkflowSlot ws1) {
		EvalTools.notNull(ws1, this);
		this.ws = ws1;
	}

}
