/**
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
 */
/*
 * 
 *
 * $Id$
 */
package cross.commands.workflow;

import java.io.File;


import cross.commands.pipeline.ICommandSequence;
import cross.exception.NotInitializedException;
import cross.io.FileTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for the creation of {@link cross.datastructures.workflow.IWorkflow}
 * instances. Currently, only instances of DefaultWorkflow can be generated.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Data
@Slf4j
public class WorkflowFactory implements IWorkflowFactory {

    private IWorkflow currentWorkflow;
    private FileTools fileTools;

//	private PropertiesConfiguration cfg = new PropertiesConfiguration();
//	@Override
//	public void configure(final Configuration cfg) {
//		this.cfg = new PropertiesConfiguration();
//		ConfigurationUtils.copy(cfg, this.cfg);
//	}
    /**
     * 
     * @return
     */
    @Override
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
    @Override
    public IWorkflow getDefaultWorkflowInstance(
            final ICommandSequence ics) {
        return getDefaultWorkflowInstance("workflow", ics);
    }

    /**
     * Create a new DefaultWorkflow instance with custom name, if it does not
     * exist already. Otherwise returns the existing instance. Use this, if you
     * will only create one Workflow per VM instance.
     * 
     * @param name
     * @param ics
     * @return
     */
    @Override
    public IWorkflow getDefaultWorkflowInstance(
            final String name, final ICommandSequence ics) {
        if (this.currentWorkflow == null) {
            DefaultWorkflow dw = new DefaultWorkflow();
            dw.setCommandSequence(ics);
            dw.setName(new File(fileTools.prependDefaultDirsWithPrefix("", null,
                    dw.getStartupDate()), name + ".xml").getAbsolutePath());
            this.currentWorkflow = dw;
        }

        return this.currentWorkflow;
    }
//	/**
//	 * 
//	 * @param startup
//	 * @param name
//	 * @param ics
//	 * @param cfg
//	 * @return
//	 */
//        @Override
//	public IWorkflow getNewWorkflowInstance(final Date startup,
//	        final String name, final ICommandSequence ics) {
//                    DefaultWorkflow dw = new DefaultWorkflow();
//                    dw.setCommandSequence(ics);
//                    dw.setStartupDate(startup);
//                    dw.setName(new File(FileTools.prependDefaultDirsWithPrefix("", null, dw
//                            .getStartupDate()), name + ".xml").getAbsolutePath());
//                    return dw;
//                }
//	}
}
