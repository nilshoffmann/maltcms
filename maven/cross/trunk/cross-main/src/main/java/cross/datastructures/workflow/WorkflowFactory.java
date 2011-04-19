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
 * $Id: WorkflowFactory.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */

package cross.datastructures.workflow;

import java.io.File;
import java.util.Date;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

import cross.Factory;
import cross.IConfigurable;
import cross.datastructures.pipeline.ICommandSequence;
import cross.exception.NotInitializedException;
import cross.datastructures.tools.FileTools;

/**
 * Factory for the creation of {@link cross.datastructures.workflow.IWorkflow}
 * instances. Currently, only instances of DefaultWorkflow can be generated.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class WorkflowFactory implements IWorkflowFactory {

	private IWorkflow currentWorkflow = null;

	private PropertiesConfiguration cfg = new PropertiesConfiguration();

	@Override
	public void configure(final Configuration cfg) {
		this.cfg = new PropertiesConfiguration();
		ConfigurationUtils.copy(cfg, this.cfg);
	}

	/**
	 * 
	 * @return
	 */
	public IWorkflow getCurrentWorkflowInstance() {
		if (this.currentWorkflow == null) {
			throw new NotInitializedException(
			        "Workflow was not initialized yet! Please call getDefaultWorkflowInstance!");
		}
		return this.currentWorkflow;
	}

	/**
	 * Create a new IWorkflow instance with default name workflow.
	 * 
	 * @param startup
	 * @param ics
	 * @return
	 */
	public IWorkflow getDefaultWorkflowInstance(final Date startup,
	        final ICommandSequence ics) {
		return getDefaultWorkflowInstance(startup, "workflow", ics);
	}

	/**
	 * Create a new DefaultWorkflow instance with custom name, if it does not
	 * exist already. Otherwise returns the existing instance. Use this, if you
	 * will only create one Workflow per VM instance. Otherwise use
	 * getNewWorkflowInstance to create a new DefaultWorkflow instance with
	 * custom configuration.
	 * 
	 * @param startup
	 * @param name
	 * @param ics
	 * @return
	 */
	public IWorkflow getDefaultWorkflowInstance(final Date startup,
	        final String name, final ICommandSequence ics) {
		if (this.currentWorkflow == null) {
			this.currentWorkflow = getNewWorkflowInstance(startup, name, ics,
			        this.cfg);
		}

		return this.currentWorkflow;
	}

	/**
	 * 
	 * @param startup
	 * @param name
	 * @param ics
	 * @param cfg
	 * @return
	 */
	public IWorkflow getNewWorkflowInstance(final Date startup,
	        final String name, final ICommandSequence ics,
	        final Configuration cfg) {
		final IWorkflow dw = Factory.getInstance().getObjectFactory()
		        .instantiate("cross.datastructures.workflow.DefaultWorkflow",
		                IWorkflow.class);
		dw.setCommandSequence(ics);
		dw.setStartupDate(startup);
		dw.setName(new File(FileTools.prependDefaultDirsWithPrefix("", null, dw
		        .getStartupDate()), name + ".xml").getAbsolutePath());
		dw.setConfiguration(cfg);
		return dw;
	}
}
