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

package cross.commands.fragments;

import org.apache.commons.configuration.Configuration;
import org.jdom.Element;

import cross.commands.ICommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.IWorkflowResult;
import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;

/**
 * A class providing a default implementation for configure and a concrete
 * typing of the untyped superclass {@link cross.commands.ICommand}.
 * 
 * Use objects extending this class as commands within a FileFragment-based
 * pipeline.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * @param <V>
 * 
 */
public abstract class AFragmentCommand implements
        ICommand<TupleND<IFileFragment>, TupleND<IFileFragment>>,
        IEventSource<IWorkflowResult>, IWorkflowElement {

	// public abstract TupleND<VariableFragment> apply(TupleND<VariableFragment>
	// t) ;

	private final IEventSource<IWorkflowResult> ies = new EventSource<IWorkflowResult>();

	private IWorkflow iw = null;

	/**
	 * @param l
	 * @see 
	 *      cross.event.IEventSource#addListener(cross.event.IListener<cross.event
	 *      .IEvent<V>>[])
	 */
	public void addListener(final IListener<IEvent<IWorkflowResult>> l) {
		this.ies.addListener(l);
	}

	public void appendXML(final Element e) {

	}

	public void configure(final Configuration cfg) {

	}

	/**
	 * @param e
	 * @see cross.event.IEventSource#fireEvent(cross.event.IEvent)
	 */
	public void fireEvent(final IEvent<IWorkflowResult> e) {
		this.ies.fireEvent(e);
	}

	public abstract String getDescription();

	@Override
	public IWorkflow getIWorkflow() {
		return this.iw;
	}

	/**
	 * @param l
	 * @see cross.event.IEventSource#removeListener(cross.event.IListener<cross.
	 *      event.IEvent<V>>[])
	 */
	public void removeListener(final IListener<IEvent<IWorkflowResult>> l) {
		this.ies.removeListener(l);
	}

	@Override
	public void setIWorkflow(final IWorkflow iw1) {
		this.iw = iw1;
	}

}
