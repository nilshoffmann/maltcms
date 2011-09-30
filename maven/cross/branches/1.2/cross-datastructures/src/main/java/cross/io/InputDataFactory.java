/**
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
 */
/*
 * 
 *
 * $Id$
 */

package cross.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IFileFragmentFactory;
import cross.datastructures.tuple.TupleND;
import cross.exception.ConstraintViolationException;
import cross.tools.StringTools;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
@Slf4j
@ServiceProvider(service=IInputDataFactory.class)
public class InputDataFactory implements IInputDataFactory {
    
    private String[] input = new String[]{};
    private String basedir = ".";
    private boolean recurse = false;
    
    @Setter(AccessLevel.PRIVATE)
    private List<IFileFragment> initialFiles = Collections.emptyList();

    @Override
    public List<IFileFragment> getInitialFiles() {
        return this.initialFiles;
    }

    protected IFileFragment initFile(final String s) {
        this.log.debug("Adding: " + s);
        FragmentStringParser fsp = new FragmentStringParser();
        
        final IFileFragment inputFragment = Lookup.getDefault().lookup(IFileFragmentFactory.class).fromString(s);
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
    public TupleND<IFileFragment> prepareInputData() {
        this.log.info("Preparing Input data!");
        this.initialFiles = new ArrayList<IFileFragment>();
        if (this.input.length > 0) {
            log.debug("{}", this.input);
            if (this.input.length == 1) {
                if (this.input[0].startsWith("*")) {
                    final File dir = new File(this.basedir);
                    final boolean recurse = this.recurse;
                    final FilenameFilter filter = new FilenameFilter() {

                        @Override
                        public boolean accept(final File dir1, final String name) {
                            return name.endsWith(StringTools.getFileExtension(
                                    InputDataFactory.this.input[0]));
                        }
                    };
                    this.log.debug("Assuming input under: {}", dir.
                            getAbsolutePath());
                    final ArrayList<String> files = new ArrayList<String>();
                    if (recurse) {
                        this.log.debug("Descending into subdirectories");
                        final ArrayList<String> subdirs = new ArrayList<String>();
                        subdirs.add(dir.getAbsolutePath());
                        while (!subdirs.isEmpty()) {
                            this.log.debug("Subdirectories: {}", subdirs);
                            final File directory = new File(subdirs.remove(0));
                            this.log.debug("IsDir? {}", dir.isDirectory());
                            this.log.info("Checking dir: {}", directory);
                            final String[] dirs = directory.list();// subdirfilter);
                            if (dirs != null) {
                                for (final String s : dirs) {
                                    if (new File(directory, s).isDirectory()) {
                                        this.log.debug(
                                                "Adding subdirectory {}", s);
                                        subdirs.add(new File(directory, s).
                                                getAbsolutePath());
                                    }
                                }
                            }
                            final String[] fils = directory.list(filter);
                            if (fils != null) {
                                for (final String s : fils) {
                                    this.log.info("Adding file {}", s);
                                    files.add(new File(directory, s).
                                            getAbsolutePath());
                                }
                            }

                        }
                    } else {
                        this.log.info("Checking dir: {}", dir);
                        final String[] fls = dir.list(filter);
                        for (final String s : fls) {
                            this.log.info("Adding file {}", s);
                            files.add(new File(dir, s).getAbsolutePath());
                        }
                    }

                    final String[] children = files.toArray(new String[]{});// dir.list(filter);
                    if (children == null) {
                        this.log.error("Could not locate directory "
                                + dir.getAbsolutePath() + ", aborting!");
                        System.exit(-1);
                    } else if (children.length == 0) {
                        this.log.error("Could not locate files in "
                                + dir.getAbsolutePath() + ", aborting!");
                        System.exit(-1);
                    } else {
                        for (int i = 0; i < children.length; i++) {
                            this.log.info("Adding file " + (i + 1) + "/"
                                    + children.length + ": " + children[i]);
                            this.initialFiles.add(initFile(children[i]));
                        }
                    }
                    return new TupleND<IFileFragment>(this.initialFiles);
                }
            }
            if (this.input.length == 0) {
                log.error("Could not split dataInfo!");
                System.exit(-1);
            }

            int cnt = 0;
            for (final String s : this.input) {
                log.info("Adding file " + (cnt + 1) + "/"
                        + this.input.length + ": " + s);
                cnt++;
                this.initialFiles.add(initFile(s));
            }
            return new TupleND<IFileFragment>(this.initialFiles);
        }
        throw new ConstraintViolationException("No input data given, aborting!");
    }
}
