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
package maltcms.test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

/**
 * <p>ExtractClassPathFiles class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class ExtractClassPathFiles extends ExternalResource {

    private final TemporaryFolder tf;
    private final String[] resourcePaths;
    private final List<File> files = new LinkedList<>();
    private File baseFolder;

    /**
     * <p>Constructor for ExtractClassPathFiles.</p>
     *
     * @param tf a {@link org.junit.rules.TemporaryFolder} object.
     * @param resourcePaths a {@link java.lang.String} object.
     */
    public ExtractClassPathFiles(TemporaryFolder tf, String... resourcePaths) {
        this.tf = tf;
        this.resourcePaths = resourcePaths;
    }

    /** {@inheritDoc} */
    @Override
    protected void before() throws Throwable {
        try {
            this.tf.create();
        } catch (IOException ex) {
            throw ex;
        }
        baseFolder = tf.newFolder();
        int i = 0;
        for (String resource : resourcePaths) {
            File file = ZipResourceExtractor.extract(
                    resource, baseFolder);
            files.add(file);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void after() {
        for (File f : files) {
            f.delete();
        }
    }

    /**
     * <p>Getter for the field <code>files</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<File> getFiles() {
        return this.files;
    }

    /**
     * <p>getBaseDir.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getBaseDir() {
        return baseFolder;
    }
}
