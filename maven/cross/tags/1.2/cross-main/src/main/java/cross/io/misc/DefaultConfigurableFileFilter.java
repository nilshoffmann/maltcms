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
 * $Id: DefaultConfigurableFileFilter.java 43 2009-10-16 17:22:55Z nilshoffmann
 * $
 */
package cross.io.misc;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import java.util.Arrays;
import org.slf4j.Logger;

import cross.IConfigurable;
import cross.Logging;
import cross.tools.StringTools;

/**
 * Filter matching Image and Text-File formats (configurable).
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class DefaultConfigurableFileFilter implements FileFilter, IConfigurable {

	private final Logger log = Logging.getLogger(this);
	private List<String> fileTypesToKeep = StringTools.toStringList(Arrays
	        .asList(new String[] { "png", "jpg", "jpeg", "svg", "txt",
	                "properties", "csv", "tsv" }));

	private List<String> prefixesToMatch = StringTools.toStringList(Arrays
	        .asList(new String[] { "warped" }));

	private List<String> suffixesToMatch = StringTools.toStringList(Arrays
	        .asList(new String[] { "ChromatogramWarp", "PathWarp" }));

	@Override
	public boolean accept(final File f) {
		// First match path suffixes
		for (final String s : this.suffixesToMatch) {
			if (!s.isEmpty()) {
				this.log.debug("Checking {} against suffix {}", f.getParent(),
				        s);
				if (f.getParent().endsWith(s)) {
					this.log.debug("FileFilter matched on suffix {}", s);
					return true;
				} else {
					this.log
					        .debug(
					                "FileFilter did not match on suffix {} for string {}",
					                s, f.getParent());
				}
			}
		}
		// Second match filename prefixes
		for (final String s : this.prefixesToMatch) {
			if (!s.isEmpty()) {
				if (f.getName().startsWith(s)) {
					this.log.debug("FileFilter matched on prefix {}", s);
					return true;
				}
			}
		}
		// Third match filename extensions
		final String extension = StringTools.getFileExtension(f
		        .getAbsolutePath());
		for (final String s : this.fileTypesToKeep) {
			if (extension.equalsIgnoreCase(s)) {
				this.log.debug("FileFilter matched on {}", extension);
				return true;
			}
		}
		return false;
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
		this.fileTypesToKeep = StringTools.toStringList(cfg.getList(this
		        .getClass().getName()
		        + ".fileTypesToKeep", Arrays.asList(new String[] { "png",
		        "jpg", "jpeg", "svg", "txt", "properties", "csv", "tsv" })));
		this.prefixesToMatch = StringTools.toStringList(cfg.getList(this
		        .getClass().getName()
		        + ".prefixesToMatch", Arrays
		        .asList(new String[] { "alignment" })));
		this.suffixesToMatch = StringTools.toStringList(cfg.getList(this
		        .getClass().getName()
		        + ".suffixesToMatch", Arrays
		        .asList(new String[] { "ChromatogramWarp" })));
	}

	/**
	 * @return the fileTypesToKeep
	 */
	public List<String> getFileTypesToKeep() {
		return this.fileTypesToKeep;
	}

	/**
	 * @return the prefixesToMatch
	 */
	public List<String> getPrefixesToMatch() {
		return this.prefixesToMatch;
	}

	/**
	 * @return the suffixesToMatch
	 */
	public List<String> getSuffixesToMatch() {
		return this.suffixesToMatch;
	}

	/**
	 * @param fileTypesToKeep
	 *            the fileTypesToKeep to set
	 */
	public void setFileTypesToKeep(final List<String> fileTypesToKeep) {
		this.fileTypesToKeep = fileTypesToKeep;
	}

	/**
	 * @param prefixesToMatch
	 *            the prefixesToMatch to set
	 */
	public void setPrefixesToMatch(final List<String> prefixesToMatch) {
		this.prefixesToMatch = prefixesToMatch;
	}

	/**
	 * @param suffixesToMatch
	 *            the suffixesToMatch to set
	 */
	public void setSuffixesToMatch(final List<String> suffixesToMatch) {
		this.suffixesToMatch = suffixesToMatch;
	}

}
