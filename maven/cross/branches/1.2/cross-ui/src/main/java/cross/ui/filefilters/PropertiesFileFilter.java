/*
 * $license$
 *
 * $Id$
 */

package cross.ui.filefilters;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import cross.tools.StringTools;

public class PropertiesFileFilter extends FileFilter {

	@Override
	public boolean accept(final File f) {
            if(f.isDirectory()) {
                return true;
            }
		final String ext = StringTools.getFileExtension(f.getAbsolutePath());
		if (ext.equals("properties")) {
			return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		return ".properties";
	}
}
