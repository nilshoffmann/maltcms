/*
 * $license$
 *
 * $Id$
 */

package cross.io.misc;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.configuration.Configuration;

import cross.IConfigurable;

/**
 * Matches any given file.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class DefaultMatchAllFileFilter implements FileFilter, IConfigurable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(final File pathname) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
	 * )
	 */
	@Override
	public void configure(final Configuration cfg) {

	}

}
