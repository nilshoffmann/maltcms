/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.evaluation.spi.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import lombok.Data;
import net.sf.maltcms.evaluation.api.IPostProcessor;
import net.sf.maltcms.evaluation.api.tasks.ITask;
import net.sf.maltcms.evaluation.spi.tasks.maltcms.WorkflowResult;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public class Task implements ITask<WorkflowResult> {

    private static final long serialVersionUID = 986719239076016L;
    private final List<String> commandLine;
    private final HashMap<String,String> additionalEnvironment = new HashMap<String,String>();
    private final File workingDirectory;
    private final List<IPostProcessor> postProcessors;
    private final File outputDirectory;

    @Override
    public WorkflowResult call() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(commandLine).directory(
                workingDirectory).redirectErrorStream(true);
        pb.environment().putAll(additionalEnvironment);
        System.out.println(pb.environment());
        Process p = pb.start();
        try {
            String line;
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        int status = p.waitFor();
        if (status != 0) {
            throw new RuntimeException(
                    "Task finished with non-zero exit status: " + status + "\n Commandline: " + commandLine + ", working directory: " + workingDirectory);
        }
        return new WorkflowResult(outputDirectory);
    }
}
