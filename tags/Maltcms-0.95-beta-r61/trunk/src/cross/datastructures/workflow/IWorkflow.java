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

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.jdom.Element;

import cross.IConfigurable;
import cross.event.IEventSource;

/**
 * Workflow models a sequence of produced IWorkflowResults, which usually are
 * files created by {@link cross.datastructures.workflow.IWorkflowElement}
 * objects.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IWorkflow extends IEventSource<IWorkflowResult>, IConfigurable {

	public void append(IWorkflowResult iwr);

	public Configuration getConfiguration();

	public String getName();

	public Iterator<IWorkflowResult> getResults();

	public Date getStartupDate();

	public void readXML(Element e) throws IOException, ClassNotFoundException;

	public void save();

	/**
	 * @param configuration
	 */
	public void setConfiguration(Configuration configuration);

	public void setName(String name);

	/**
	 * @param date
	 */
	public void setStartupDate(Date date);

	public Element writeXML() throws IOException;

}
