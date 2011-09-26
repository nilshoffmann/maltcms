/*
 * 
 *
 * $Id$
 */

package cross.datastructures.fragments;

import java.io.Externalizable;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.jdom.Element;

import ucar.nc2.Dimension;
import cross.exception.ResourceNotAvailableException;

/**
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IFileFragment extends IGroupFragment, IFragment,
        Externalizable {

    /**
     * Add a number of children.
     *
     * @param fragments
     */
    @Override
    public abstract void addChildren(IVariableFragment... fragments);

    /**
     * Add dimensions to this IFileFragment.
     *
     * @param dims1
     */
    public abstract void addDimensions(Dimension... dims1);

    /**
     * Add source FileFragments contained in Collection c to this FileFragment.
     *
     * @param c
     */
    public abstract void addSourceFile(Collection<IFileFragment> c);

    /**
     * Add source FileFragments contained in ff to this FileFragment.
     *
     * @param ff
     */
    public abstract void addSourceFile(IFileFragment... ff);

    /**
     * Append structural information of this FileFragment to Element e.
     */
    @Override
    public abstract void appendXML(Element e);

    /**
     * Iterates through all VariableFragments, clearing in memory arrays, except
     * for source_files. Throws IllegalStateException if VariableData has been
     * altered.
     *
     * @throws java.lang.IllegalStateException
     */
    public abstract void clearArrays() throws IllegalStateException;

    /**
     * Return this FileFragment's storage location as string representation.
     *
     * @return
     */
    public abstract String getAbsolutePath();

    /**
     * Return the child with name varname. If varname is not found in local
     * structure, try to locate it in sourcefiles. First hit wins. Otherwise
     * throw IllegalArgumentException.
     *
     * @param varname
     * @return
     */
    @Override
    public abstract IVariableFragment getChild(String varname)
            throws ResourceNotAvailableException;

    /**
     * Return the child with name varname. If varname is not found in local
     * structure, try to locate it in sourcefiles. First hit wins. Otherwise
     * throw IllegalArgumentException. If <code>loadStructureOnly</code> is true,
     * only the variable structure is retrieved, not the data.
     *
     * @param varname
     * @param loadStructureOnly
     * @return
     */
    public abstract IVariableFragment getChild(String varname,
            boolean loadStructureOnly) throws ResourceNotAvailableException;

    /**
     * The unique ID (between runs) of this FileFragment.
     *
     * @return
     */
    public abstract long getID();

    /**
     * Return the name of this FileFragment, does not include directory or other
     * prefixed information.
     *
     * @return
     */
    @Override
    public abstract String getName();

    @Override
    public abstract IGroupFragment getParent();

    /**
     * Return the number of children of this FileFragment.
     *
     * @return
     */
    @Override
    public abstract int getSize();

    /**
     * Return all source FileFragments.
     *
     * @return
     */
    public abstract Collection<IFileFragment> getSourceFiles();

    /**
     * Query FileFragment for the given VariableFragments.
     *
     * @param vf
     * @return
     */
    public abstract boolean hasChildren(IVariableFragment... vf);

    /**
     * Query FileFragment for children with the given strings as names.
     *
     * @param s
     * @return
     */
    public abstract boolean hasChildren(String... s);

    public boolean isModified();

    /**
     * Creates an iterator over all children of this FileFragment by the time of
     * creation of the iterator.
     */
    @Override
    public abstract Iterator<IVariableFragment> iterator();

    /**
     * Remove the given IVariableFragment from the list of this FileFragment's
     * children.
     *
     * @param variableFragment
     */
    public abstract void removeChild(IVariableFragment variableFragment);

    /**
     * Remove the given source file.
     *
     * @param ff
     */
    public abstract void removeSourceFile(IFileFragment ff);

    /**
     * Removes all currently associated source files.
     */
    public abstract void removeSourceFiles();

    public abstract boolean save();

    /**
     * Change the filename of this Fragment.
     *
     * @param f1
     */
    public abstract void setFile(File f1);

    /**
     * Change the filename of this Fragment.
     *
     * @param file
     */
    public abstract void setFile(String file);

    @Override
    public abstract String toString();
}
