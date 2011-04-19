/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: PropertiesFileFilter.java 110 2010-03-25 15:21:19Z nilshoffmann $
 */

/**
 * 
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
