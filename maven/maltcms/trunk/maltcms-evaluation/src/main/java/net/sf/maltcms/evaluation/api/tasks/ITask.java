/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.evaluation.api.tasks;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;
import net.sf.maltcms.evaluation.api.IPostProcessor;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public interface ITask<T extends Serializable> extends Callable<T>, Serializable{
    public List<IPostProcessor> getPostProcessors();
}
