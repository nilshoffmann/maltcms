/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package cross.datastructures.fragments;

import cross.IConfigurable;
import java.io.File;
import java.util.Collection;

/**
 * Interface for factories creating IFileFragments.
 * @author nilshoffmann
 */
public interface IFileFragmentFactory extends IConfigurable{

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
