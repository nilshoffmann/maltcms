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
package cross.datastructures.fragments;

import cross.annotations.Configurable;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tools.FragmentTools;
import cross.io.misc.FragmentStringParser;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Provides a number of convenience methods to create new {@link IFileFragment}
 * instances. However, you can also create {@link FileFragment} and 
 * {@link ImmutableFileFragment} instances directly.
 *
 * @author Nils Hoffmann
 *
 */
@ServiceProvider(service = IFileFragmentFactory.class)
public class FileFragmentFactory implements IFileFragmentFactory {

    @Configurable(name = "input.basedir")
    private String inputBasedir = "";
    private FragmentStringParser fsp;

    /**
     *
     * @param cfg
     */
    @Override
    public void configure(final Configuration cfg) {
        this.inputBasedir = cfg.getString("input.basedir", "");
    }

    /**
     *
     * @param f
     * @return
     */
    @Override
    public IFileFragment create(final File f) {
//        if (FileFragment.hasFragment(f.getAbsolutePath())) {
//            return FileFragment.getFragment(f.getAbsolutePath());
//        }
        return new FileFragment(f);
    }

    /**
     *
     * @param f
     * @param sourceFiles
     * @return
     */
    @Override
    public IFileFragment create(final File f,
            final IFileFragment... sourceFiles) {
        final IFileFragment iff = create(f);
        iff.addSourceFile(sourceFiles);
        return iff;
    }

    /**
     *
     * @param dirname
     * @param filename
     * @return
     */
    @Override
    public IFileFragment create(final String dirname, final String filename) {
        return create(dirname, filename, new LinkedList<IFileFragment>());
    }

    /**
     *
     * @param dirname
     * @param filename
     * @param sourceFiles
     * @return
     */
    @Override
    public IFileFragment create(final String dirname, final String filename,
            final IFileFragment... sourceFiles) {
        final IFileFragment iff = create(new File(dirname, filename));
        iff.addSourceFile(sourceFiles);
        return iff;
    }

    /**
     *
     * @param dir
     * @param filename
     * @param sourceFiles
     * @return
     */
    @Override
    public IFileFragment create(final File dir, final String filename,
            final IFileFragment... sourceFiles) {
        final IFileFragment iff = create(new File(dir, filename));
        iff.addSourceFile(sourceFiles);
        return iff;
    }

    /**
     *
     * @param dirname
     * @param filename
     * @param resourceFiles
     * @return
     * @throws IllegalArgumentException
     */
    @Override
    public IFileFragment create(final String dirname, final String filename,
            final Collection<IFileFragment> resourceFiles)
            throws IllegalArgumentException {
        File f = null;
        IFileFragment ff = null;
        if (filename == null) {
            ff = new FileFragment(new File(dirname), null);
            ff.addSourceFile(resourceFiles);
        } else {
            f = new File(dirname, filename);
            ff = new FileFragment(f);
            ff.addSourceFile(resourceFiles);
        }
        return ff;
    }

    /**
     * Creates a new AFragment with default name. Both original FileFragments
     * files are stored as variables below the newly created fragment.
     *
     * @param f1
     * @param f2
     * @return
     */
    @Override
    public IFileFragment create(final IFileFragment f1, final IFileFragment f2,
            final File outputdir) {
        EvalTools.notNull(new Object[]{f1, f2}, this);
        final IFileFragment ff = create(outputdir.getAbsolutePath(), null);
        FragmentTools.setLHSFile(ff, f1);
        FragmentTools.setRHSFile(ff, f2);
        return ff;
    }

    /**
     *
     * @param s
     * @return
     */
    @Override
    public IFileFragment create(final String s) {
        String filename = "";
        String dirname = "";
        // unqualified filename, without path information
        if (!s.contains(File.separator)) {
            dirname = this.inputBasedir;
            filename = s;
        } else {// qualified file, with at least some path information
            dirname = FileTools.getDirname(s);
            filename = FileTools.getFilename(s);
        }
        final IFileFragment ff = create(dirname, filename,
                new LinkedList<IFileFragment>());
        return ff;
    }

    /**
     * Create a FileFragment and possibly associated VariableFragments.
     *
     * @param dataInfo
     * @return
     */
    @Override
    public IFileFragment fromString(final String dataInfo) {
        if (this.fsp == null) {
            this.fsp = new FragmentStringParser();
        }
        return this.fsp.parse(dataInfo);
    }
}
