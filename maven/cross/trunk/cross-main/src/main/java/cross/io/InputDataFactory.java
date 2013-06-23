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
package cross.io;

import cross.annotations.Configurable;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tuple.TupleND;
import cross.exception.ConstraintViolationException;
import cross.exception.ExitVmException;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * Implementation of {@link IInputDataFactory}.
 * @author Nils Hoffmann
 */
@Slf4j
@Data
public class InputDataFactory implements IInputDataFactory {

    private final static InputDataFactory idf = new InputDataFactory();

    public static InputDataFactory getInstance() {
        return InputDataFactory.idf;
    }
	
    @Configurable(name = "input.dataInfo")
    private String[] input = new String[]{};
    @Configurable(name = "input.basedir")
    private String basedir = "";
    @Configurable(name = "input.basedir.recurse")
    private boolean recurse = false;
    @Setter(AccessLevel.NONE)
    private List<IFileFragment> initialFiles = Collections.emptyList();

    @Override
    public void configure(final Configuration cfg) {
        if (cfg.containsKey("input.dataInfo")) {
            this.input = cfg.getStringArray("input.dataInfo");
        }
        this.basedir = cfg.getString("input.basedir", "");
        if (this.basedir.isEmpty() || this.basedir.equals(".")) {
            log.debug("Configuration has no value for input.basedir! {}", this.basedir);
            this.basedir = System.getProperty("user.dir");
        }
        this.recurse = cfg.getBoolean("input.basedir.recurse", false);
    }

    @Override
    public List<IFileFragment> getInitialFiles() {
        return this.initialFiles;
    }

    public Collection<File> getInputFiles(String[] input) {
        LinkedHashSet<File> files = new LinkedHashSet<File>();
        for (String inputString : input) {
            log.debug("Processing input string {}", inputString);
            //separate wildcards from plain files
            String name = FilenameUtils.getName(inputString);
            boolean isWildcard = name.contains("?") || name.contains("*");
            String fullPath = FilenameUtils.getFullPath(inputString);
            File path = new File(fullPath);
			File baseDirFile = new File(this.basedir);
			if(!baseDirFile.exists()) {
				throw new ExitVmException("Input base directory '"+baseDirFile+"' does not exist!");
			}
			if(!baseDirFile.isDirectory()) {
				throw new ExitVmException("Input base directory '"+baseDirFile+"' is not a directory!");
			}
            log.debug("Path is absolute: {}", path.isAbsolute());
            //identify absolute and relative files
            if (!path.isAbsolute()) {
                log.info("Resolving relative file against basedir: {}", this.basedir);
                path = new File(this.basedir, fullPath);
            }
            //normalize filenames
            fullPath = FilenameUtils.normalize(path.getAbsolutePath());
            log.debug("After normalization: {}", fullPath);
            IOFileFilter dirFilter = this.recurse ? TrueFileFilter.INSTANCE : null;
            if (isWildcard) {
                log.debug("Using wildcard matcher for {}", name);
                files.addAll(FileUtils.listFiles(new File(fullPath), new WildcardFileFilter(name, IOCase.INSENSITIVE), dirFilter));
            } else {
                log.debug("Using name for {}", name);
				File f = new File(fullPath, name);
				if(!f.exists()) {
					throw new ExitVmException("Input file '"+f+"' does not exist!");
				}
				if(!f.isFile()) {
					throw new ExitVmException("Input file '"+f+"' is not a file!");
				}
                files.add(f);
            }
        }
        return files;
    }

    /**
     * Preprocess input data (files and variables).
     *
     * @return
     */
    @Override
    public TupleND<IFileFragment> prepareInputData(String[] input) {
        if (input == null || input.length == 0) {
            throw new ExitVmException("No input data given, aborting!");
        }
        log.info("Preparing input data!");
        log.debug("Received paths: {}",Arrays.toString(input));
        this.initialFiles = new ArrayList<IFileFragment>();
        for (String s : input) {
            File inputFile = new File(s);
            URI uri = null;
            if(inputFile.isFile() && inputFile.exists()) {
                uri = inputFile.toURI();
            }else{
                uri = URI.create(FileTools.escapeUri(s));
            }
            if (uri.getScheme() != null) {
                this.initialFiles.add(new FileFragment(uri));
            } else {
                for (File f : getInputFiles(new String[]{s})) {
                    log.info("Adding file {}", f.getAbsolutePath());
                    initialFiles.add(new FileFragment(f.toURI()));
                }
            }
        }

        if (initialFiles.isEmpty()) {
            throw new ExitVmException("Could not create input data for files " + Arrays.toString(input));
        }
        return new TupleND<IFileFragment>(initialFiles);
    }
}
