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

import cross.IConfigurable;
import java.io.File;
import java.util.Collection;

/**
 * Interface for factories creating IFileFragments.
 *
 * @author Nils Hoffmann
 */
public interface IFileFragmentFactory extends IConfigurable {

    IFileFragment create(final File f);

    IFileFragment create(final File f, final IFileFragment... sourceFiles);

    IFileFragment create(final String dirname, final String filename);

    IFileFragment create(final String dirname, final String filename, final IFileFragment... sourceFiles);

    IFileFragment create(final File dir, final String filename, final IFileFragment... sourceFiles);

    IFileFragment create(final String dirname, final String filename, final Collection<IFileFragment> resourceFiles) throws IllegalArgumentException;

    /**
     * Creates a new AFragment with default name. Both original FileFragments
     * files are stored as variables below the newly created fragment.
     *
     * @param f1
     * @param f2
     * @return
     */
    IFileFragment create(final IFileFragment f1, final IFileFragment f2, final File outputdir);

    IFileFragment create(final String s);

    /**
     * Create a FileFragment and possibly associated VariableFragments.
     *
     * @param dataInfo
     * @return
     */
    IFileFragment fromString(final String dataInfo);
}
