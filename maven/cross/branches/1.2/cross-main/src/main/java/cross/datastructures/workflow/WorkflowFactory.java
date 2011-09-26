/*
 * 
 *
 * $Id$
 */

package cross.datastructures.workflow;

import java.io.File;
import java.util.Date;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

import cross.Factory;
import cross.applicationContext.DefaultApplicationContextFactory;
import cross.datastructures.pipeline.ICommandSequence;
import cross.exception.NotInitializedException;
import cross.datastructures.tools.FileTools;
import cross.tools.StringTools;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * Factory for the creation of {@link cross.datastructures.workflow.IWorkflow}
 * instances. Currently, only instances of DefaultWorkflow can be generated.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
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
        @Override
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
        @Override
	public IWorkflow getNewWorkflowInstance(final Date startup,
	        final String name, final ICommandSequence ics,
	        final Configuration cfg) {
                if(cfg.containsKey("pipeline.xml")) {
                    List<?> pathList = cfg.getList("pipeline.xml");
                    log.info("Using spring beans pipeline definitios: {}",pathList);
                    String[] paths = StringTools.toStringList(pathList).toArray(new String[pathList.size()]);
                    DefaultApplicationContextFactory dacf = new DefaultApplicationContextFactory(paths);
                    ApplicationContext context = dacf.createApplicationContext();
                    return context.getBean(IWorkflow.class);
                }else{
                    log.warn("Using pipeline= and pipeline.properties definitions is deprecated starting with cross-1.1.5.\nPlease use pipeline.xml= followed by comma separated paths to the spring bean xml configuration files instead.\nSee cfg/chroma.xml as an example!");
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
        
}
