/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.evaluation.api.tasks;


import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public interface ITask<T extends Serializable> extends Callable<T>, Serializable{
    public List<IPostProcessor> getPostProcessors();
    public HashMap<String,String> getAdditionalEnvironment();
    public List<String> getCommandLine();
    public File getWorkingDirectory();
    public File getOutputDirectory();
    public UUID getTaskId();
}
