/*
 * $license$
 *
 * $Id$
 */
package cross.datastructures.workflow;

import cross.datastructures.fragments.IFileFragment;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public interface IWorkflowObjectResult<T> extends IWorkflowResult {

	public T getObject();

	public void setObject(T t);

	public IFileFragment[] getResources();

	public void setResources(IFileFragment... resources);
    
}
