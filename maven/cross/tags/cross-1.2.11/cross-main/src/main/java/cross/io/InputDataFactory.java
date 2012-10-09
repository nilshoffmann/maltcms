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

import cross.Factory;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.ImmutableFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.exception.ConstraintViolationException;
import cross.exception.ExitVmException;
import cross.tools.StringTools;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;

/**
 *
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
            this.basedir = System.getProperty("user.dir");
        }
        this.recurse = cfg.getBoolean("input.basedir.recurse", false);
    }

    @Override
    public List<IFileFragment> getInitialFiles() {
        return this.initialFiles;
    }

    protected IFileFragment initFile(final String s) {
        log.debug("Adding: " + s);
        final IFileFragment inputFragment = Factory.getInstance().
                getFileFragmentFactory().fromString(s);
        if (!(new File(inputFragment.getAbsolutePath()).exists())) {
            throw new ConstraintViolationException("Input file "
                    + inputFragment.getAbsolutePath() + " does not exist!");
        }
        return inputFragment;
    }

    /**
     * Preprocess input data (files and variables).
     *
     * @return
     */
    @Override
    public TupleND<IFileFragment> prepareInputData(String[] input) {
        log.info("Preparing Input data!");
        this.initialFiles = new ArrayList<IFileFragment>();
        if (this.input.length > 0) {
            Factory.getInstance().log.debug("{}", this.input);
            if (this.input.length == 1) {
                if (this.input[0].startsWith("*")) {
                    final File dir = new File(this.basedir);
                    final boolean recurse = this.recurse;
                    final FilenameFilter filter = new FilenameFilter() {
                        @Override
                        public boolean accept(final File dir1, final String name) {
                            return name.endsWith(StringTools.getFileExtension(
                                    InputDataFactory.this.input[0])) || name.toLowerCase().endsWith(StringTools.getFileExtension(
                                    InputDataFactory.this.input[0]).toLowerCase());
                        }
                    };
                    log.debug("Assuming input under: {}", dir.
                            getAbsolutePath());
                    final ArrayList<String> files = new ArrayList<String>();
                    if (recurse) {
                        log.debug("Descending into subdirectories");
                        final ArrayList<String> subdirs = new ArrayList<String>();
                        subdirs.add(dir.getAbsolutePath());
                        while (!subdirs.isEmpty()) {
                            log.debug("Subdirectories: {}", subdirs);
                            final File directory = new File(subdirs.remove(0));
                            log.debug("IsDir? {}", dir.isDirectory());
                            log.info("Checking dir: {}", directory);
                            final String[] dirs = directory.list();// subdirfilter);
                            if (dirs != null) {
                                for (final String s : dirs) {
                                    if (new File(directory, s).isDirectory()) {
                                        log.debug(
                                                "Adding subdirectory {}", s);
                                        subdirs.add(new File(directory, s).
                                                getAbsolutePath());
                                    }
                                }
                            }
                            final String[] fils = directory.list(filter);
                            if (fils != null) {
                                for (final String s : fils) {
                                    log.info("Adding file {}", s);
                                    files.add(new File(directory, s).
                                            getAbsolutePath());
                                }
                            }

                        }
                    } else {
                        log.info("Checking dir: {}", dir);
                        final String[] fls = dir.list(filter);
                        for (final String s : fls) {
                            log.info("Adding file {}", s);
                            files.add(new File(dir, s).getAbsolutePath());
                        }
                    }

                    final String[] children = files.toArray(new String[]{});// dir.list(filter);
                    if (children == null) {
                        log.error("Could not find directory "
                                + dir.getAbsolutePath() + ", aborting!");
                        System.exit(-1);
                    } else if (children.length == 0) {
                        log.error("Could not find " + Arrays.toString(input) + " in "
                                + dir.getAbsolutePath() + ", aborting!");
                        System.exit(-1);
                    } else {
                        for (int i = 0; i < children.length; i++) {
                            log.info("Adding file " + (i + 1) + "/"
                                    + children.length + ": " + children[i]);
                            this.initialFiles.add(initFile(children[i]));
                        }
                    }
                    return new TupleND<IFileFragment>(this.initialFiles);
                }
            }
            if (this.input.length == 0) {
                Factory.getInstance().log.error("{} could not be tokenized!", Arrays.toString(input));
                System.exit(-1);
            }

            int cnt = 0;
            for (final String s : this.input) {
                Factory.getInstance().log.info("Adding file " + (cnt + 1) + "/"
                        + this.input.length + ": " + s);
                cnt++;
                this.initialFiles.add(initFile(s));
            }
            return new TupleND<IFileFragment>(this.initialFiles);
        }
        throw new ExitVmException("No input data given, aborting!");
    }
}