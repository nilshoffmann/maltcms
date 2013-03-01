/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.io.misc;

import cross.IConfigurable;
import cross.tools.StringTools;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;

/**
 * Filter matching Image and Text-File formats (configurable).
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class DefaultConfigurableFileFilter implements FileFilter, IConfigurable {

    private List<String> fileTypesToKeep = StringTools.toStringList(Arrays
            .asList(new String[]{"png", "jpg", "jpeg", "svg", "txt",
                "properties", "csv", "tsv"}));
    private List<String> prefixesToMatch = StringTools.toStringList(Arrays
            .asList(new String[]{"warped"}));
    private List<String> suffixesToMatch = StringTools.toStringList(Arrays
            .asList(new String[]{"ChromatogramWarp", "PathWarp"}));

    @Override
    public boolean accept(final File f) {
        // First match path suffixes
        for (final String s : this.suffixesToMatch) {
            if (!s.isEmpty()) {
                log.debug("Checking {} against suffix {}", f.getParent(),
                        s);
                if (f.getParent().endsWith(s)) {
                    log.debug("FileFilter matched on suffix {}", s);
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
                    log.debug("FileFilter matched on prefix {}", s);
                    return true;
                }
            }
        }
        // Third match filename extensions
        final String extension = StringTools.getFileExtension(f
                .getAbsolutePath());
		if(extension.equals(f.getName())) {
			log.warn("File has no proper file extension: {}",f);
			return false;
		}else{
			for (final String s : this.fileTypesToKeep) {
				if (extension.equalsIgnoreCase(s)) {
					log.debug("FileFilter matched on {}", extension);
					return true;
				}
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
                + ".fileTypesToKeep", Arrays.asList(new Object[]{"png",
                    "jpg", "jpeg", "svg", "txt", "properties", "csv", "tsv"})));
        this.prefixesToMatch = StringTools.toStringList(cfg.getList(this
                .getClass().getName()
                + ".prefixesToMatch", Arrays
                .asList(new Object[]{"alignment"})));
        this.suffixesToMatch = StringTools.toStringList(cfg.getList(this
                .getClass().getName()
                + ".suffixesToMatch", Arrays
                .asList(new Object[]{"ChromatogramWarp"})));
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
     * @param fileTypesToKeep the fileTypesToKeep to set
     */
    public void setFileTypesToKeep(final List<String> fileTypesToKeep) {
        this.fileTypesToKeep = fileTypesToKeep;
    }

    /**
     * @param prefixesToMatch the prefixesToMatch to set
     */
    public void setPrefixesToMatch(final List<String> prefixesToMatch) {
        this.prefixesToMatch = prefixesToMatch;
    }

    /**
     * @param suffixesToMatch the suffixesToMatch to set
     */
    public void setSuffixesToMatch(final List<String> suffixesToMatch) {
        this.suffixesToMatch = suffixesToMatch;
    }
}
