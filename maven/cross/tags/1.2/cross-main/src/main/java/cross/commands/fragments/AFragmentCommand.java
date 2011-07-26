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
 * $Id: AFragmentCommand.java 135 2010-07-05 08:04:51Z nilshoffmann $
 */

package cross.commands.fragments;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.jdom.Element;

import cross.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowResult;
import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import cross.tools.StringTools;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

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
@Data
public abstract class AFragmentCommand implements IFragmentCommand {

        @Getter(AccessLevel.NONE)
	private final IEventSource<IWorkflowResult> ies = new EventSource<IWorkflowResult>();

	private IWorkflow workflow = null;

	/**
	 * @param l
	 * @see 
	 *      cross.event.IEventSource#addListener(cross.event.IListener<cross.event
	 *      .IEvent<V>>[])
	 */
        @Override
	public void addListener(final IListener<IEvent<IWorkflowResult>> l) {
		this.ies.addListener(l);
	}

        @Override
	public void appendXML(final Element e) {

	}

        @Override
	public void configure(final Configuration cfg) {

	}

	/**
	 * @param e
	 * @see cross.event.IEventSource#fireEvent(cross.event.IEvent)
	 */
        @Override
	public void fireEvent(final IEvent<IWorkflowResult> e) {
		this.ies.fireEvent(e);
	}

	public abstract String getDescription();

	/**
	 * Utility method to create mutable FileFragments from a given tuple of
	 * FileFragments.
	 * 
	 * @param t
	 * @return
	 */
	public TupleND<IFileFragment> createWorkFragments(TupleND<IFileFragment> t) {
		TupleND<IFileFragment> wt = new TupleND<IFileFragment>();
		for (IFileFragment iff : t) {
			wt.add(createWorkFragment(iff));
		}
		return wt;
	}

	/**
	 * Utility method to create a mutable FileFragment to work on.
	 * 
	 * @param iff
	 * @return
	 */
	public IFileFragment createWorkFragment(IFileFragment iff) {
		final IFileFragment copy = Factory.getInstance()
		        .getFileFragmentFactory().create(
		                new File(getWorkflow().getOutputDirectory(this),
		                        StringTools.removeFileExt(iff.getName())
		                                + ".cdf"));
		copy.addSourceFile(iff);
		return copy;
	}

	/**
	 * @param l
	 * @see cross.event.IEventSource#removeListener(cross.event.IListener<cross.
	 *      event.IEvent<V>>[])
	 */
        @Override
	public void removeListener(final IListener<IEvent<IWorkflowResult>> l) {
		this.ies.removeListener(l);
	}

}
