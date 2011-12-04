/*
 * $license$
 *
 * $Id$
 */
package maltcms.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.pipeline.CommandPipeline;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflow;
import cross.datastructures.workflow.IWorkflow;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public abstract class AFragmentCommandTest {
    
    public IWorkflow createWorkflow(File outputDirectory, List<IFragmentCommand> commands, List<File> inputFiles) {
    	Properties props = new Properties();
        props.setProperty("log4j.rootLogger", "INFO, A1");
        props.setProperty("log4j.appender.A1",
                "org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.A1.layout.ConversionPattern",
                "%m%n");
        props.setProperty("log4j.category.cross", "WARN");
        props.setProperty("log4j.category.cross.datastructures.pipeline",
                "INFO");
        props.setProperty("log4j.category.maltcms.commands.fragments",
                "INFO");
        props.setProperty("log4j.category.maltcms.commands.fragments2d",
                "INFO");
        props.setProperty("log4j.category.maltcms", "WARN");
        props.setProperty("log4j.category.ucar", "WARN");
        props.setProperty("log4j.category.smueller", "WARN");
        PropertyConfigurator.configure(props);
    	CommandPipeline cp = new CommandPipeline();
        List<IFileFragment> fragments = new ArrayList<IFileFragment>();
        for(File f:inputFiles) {
            fragments.add(new FileFragment(f));
        }
        cp.setCommands(commands);
        cp.setInput(new TupleND<IFileFragment>(fragments));
        System.out.println("Workflow using commands "+commands);
        System.out.println("Workflow using inputFiles "+inputFiles);
        DefaultWorkflow dw = new DefaultWorkflow();
        dw.setStartupDate(new Date());
        dw.setName("testWorkflow");
        dw.setCommandSequence(cp);
        dw.setExecuteLocal(true);
        dw.setOutputDirectory(outputDirectory);
        return dw;
    }
    
}
