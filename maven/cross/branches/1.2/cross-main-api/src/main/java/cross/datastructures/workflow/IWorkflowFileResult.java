/*
 * 
 *
 * $Id$
 */

package cross.datastructures.workflow;

import java.io.File;

import cross.datastructures.fragments.IFileFragment;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public interface IWorkflowFileResult extends IWorkflowResult {

	public File getFile();

	public void setFile(File iff);

	public IFileFragment[] getResources();

	public void setResources(IFileFragment... resources);

}
