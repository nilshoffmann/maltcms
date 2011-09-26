/*
 * $license$
 *
 * $Id$
 */

package cross.io;

import cross.datastructures.fragments.IFileFragment;

/**
 * Interface for Objects, which provide a IFileFragment.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IFileFragmentProvider {

	public IFileFragment provideFileFragment();

}
