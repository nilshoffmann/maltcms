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
 * $Id: IWorkflow.java 116 2010-06-17 08:46:30Z nilshoffmann $
 */

package cross.datastructures.workflow;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.configuration.Configuration;
import org.jdom.Element;

import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.tuple.TupleND;
import cross.event.IEventSource;

/**
 * Workflow models a sequence of produced IWorkflowResults, which usually are
 * files created by {@link cross.datastructures.workflow.IWorkflowElement}
 * objects.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IWorkflow extends IEventSource<IWorkflowResult>, IConfigurable, Callable<TupleND<IFileFragment>> {

	/**
	 * Append IWorkflowResult to this IWorkflow instance.
	 * 
	 * @param iwr
	 */
	public abstract void append(IWorkflowResult iwr);

	/**
	 * Return the active ICommandSequence instance.
	 * 
	 * @return
	 */
	public abstract ICommandSequence getCommandSequence();

	/**
	 * Returns the currently active configuration for this workflow.
	 * 
	 * @return
	 */
	public abstract Configuration getConfiguration();

	/**
	 * Returns the name of this IWorkflow.
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * Returns an iterator over all currently available results.
	 * 
	 * @return
	 */
	public abstract Iterator<IWorkflowResult> getResults();

	/**
	 * Returns the results for a specific IFileFragment.
	 * 
	 * @param iff
	 * @return
	 */
	public abstract List<IWorkflowResult> getResultsFor(IFileFragment iff);

	/**
	 * Returns the results created by a specific IWorkflowElement.
	 * 
	 * @param afc
	 * @return
	 */
	public abstract List<IWorkflowResult> getResultsFor(IWorkflowElement afc);

	/**
	 * Returns the results created by a specific IWorkflowElement for a given
	 * IFileFragment.
	 * 
	 * @param afc
	 * @param iff
	 * @return
	 */
	public abstract List<IWorkflowResult> getResultsFor(IWorkflowElement afc,
	        IFileFragment iff);

	/**
	 * Returns the list of results matching a given file extension pattern.
	 * 
	 * @param fileExtension
	 * @return
	 */
	public abstract List<IWorkflowResult> getResultsOfType(String fileExtension);

	/**
	 * Returns the list of results matching a given file extension pattern
	 * created by a given IWorkflowElement implementation.
	 * 
	 * @param fileExtension
	 * @return
	 */
	public abstract List<IWorkflowResult> getResultsOfType(
	        IWorkflowElement afc, String fileExtension);
        
        /**
	 * Returns the list of results matching a given class
	 * created by a given IWorkflowElement implementation.
	 * 
	 * @param fileExtension
	 * @return
	 */
        public abstract <T> List<IWorkflowObjectResult> getResultsOfType(IWorkflowElement afc, Class<? extends T> c);

	/**
	 * Return the startup data of this IWorkflow.
	 * 
	 * @return
	 */
	public abstract Date getStartupDate();

	/**
	 * Restore state of this IWorkflow instance from an XML document.
	 * 
	 * @param e
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public abstract void readXML(Element e) throws IOException,
	        ClassNotFoundException;

	/**
	 * Save this IWorkflow.
	 */
	public abstract void save();

	/**
	 * Set ics to be the active ICommandSequence instance.
	 * 
	 * @param ics
	 */
	public abstract void setCommandSequence(ICommandSequence ics);

	/**
	 * Set the currently active configuration.
	 * 
	 * @param configuration
	 */
	public abstract void setConfiguration(Configuration configuration);

	/**
	 * Set the name of this IWorkflow instance.
	 * 
	 * @param name
	 */
	public abstract void setName(String name);

	/**
	 * Set the startup date of this IWorkflow instance.
	 * 
	 * @param date
	 */
	public abstract void setStartupDate(Date date);

	/**
	 * Write the state of this object to XML.
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract Element writeXML() throws IOException;

	/**
	 * Returns the output directory for the given object.
	 * 
	 * @param iwe
	 * @return
	 */
	public abstract File getOutputDirectory(Object iwe);
        
        public abstract File getOutputDirectory();
        
        public abstract boolean isExecuteLocal();
        
        public abstract void setExecuteLocal(boolean b);
        
        public abstract void setOutputDirectory(File f);

}
