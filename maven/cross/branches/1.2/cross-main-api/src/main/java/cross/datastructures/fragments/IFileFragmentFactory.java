/*
 * 
 *
 * $Id$
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
