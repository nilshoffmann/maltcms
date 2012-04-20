/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package net.sf.maltcms.evaluation.spi.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.db4o.config.annotations.Indexed;
import net.sf.maltcms.evaluation.api.tasks.IPostProcessor;
import net.sf.maltcms.evaluation.api.tasks.ITask;
import net.sf.maltcms.evaluation.api.tasks.ITaskResult;
import net.sf.maltcms.evaluation.spi.tasks.maltcms.DefaultTaskResult;
import net.sf.maltcms.evaluation.spi.tasks.maltcms.MaltcmsTaskResult;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class Task implements ITask<ITaskResult> {

    private static final long serialVersionUID = 986719239076016L;
    private final List<String> commandLine;
    private final HashMap<String,String> additionalEnvironment = new HashMap<String,String>();
    private final File workingDirectory;
    private final List<IPostProcessor> postProcessors;
    private final File outputDirectory;
    @Indexed
    private UUID taskId;

    public Task(List<String> commandLine, File workingDirectory, List<IPostProcessor> postProcessors, File outputDirectory) {
        this.commandLine = commandLine;
        this.workingDirectory = workingDirectory;
        this.postProcessors = postProcessors;
        this.outputDirectory = outputDirectory;
        taskId = UUID.fromString(commandLine.toString());
    }

    @Override
    public ITaskResult call() throws Exception {
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
        for(IPostProcessor postProcessor:postProcessors) {
            postProcessor.process(this);
        }
        //one of the post processors may have removed the output directory
        //if not, return the correct workflow result output
        if(outputDirectory.exists()) {
            return new MaltcmsTaskResult(outputDirectory);
        }
        Logger.getLogger(Task.class.getName()).
                log(Level.INFO,"Output directory for task "+getTaskId()+" was removed by post processor!");
        //otherwise return the singleton empty result
        return DefaultTaskResult.EMPTY;
    }

    @Override
    public HashMap<String, String> getAdditionalEnvironment() {
        return additionalEnvironment;
    }

    @Override
    public List<String> getCommandLine() {
        return commandLine;
    }

    @Override
    public File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public List<IPostProcessor> getPostProcessors() {
        return postProcessors;
    }

    @Override
    public UUID getTaskId() {
        return taskId;
    }

    @Override
    public File getWorkingDirectory() {
        return workingDirectory;
    }
}
