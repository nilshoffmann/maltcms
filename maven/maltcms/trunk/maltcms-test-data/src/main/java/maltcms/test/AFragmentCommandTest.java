/*
 * $license$
 *
 * $Id$
 */
package maltcms.test;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.pipeline.CommandPipeline;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflow;
import cross.datastructures.workflow.IWorkflow;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public abstract class AFragmentCommandTest {
    
    public IWorkflow createWorkflow(File outputDirectory, List<IFragmentCommand> commands, List<File> inputFiles) {
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
