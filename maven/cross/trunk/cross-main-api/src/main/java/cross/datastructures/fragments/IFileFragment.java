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

import cross.datastructures.cache.ICacheDelegate;
import cross.exception.ResourceNotAvailableException;
import java.io.Externalizable;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.nc2.Dimension;

/**
 * @author Nils Hoffmann
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
     * Resets all dimensions of this fragment. Does not set Dimensions to null
     * in IVariableFragments having these Dimensions!
     */
    public abstract void clearDimensions();

    /**
     * Return this FileFragment's storage location as string representation.
     *
     * @return
     */
    public abstract String getAbsolutePath();

    /**
     * Return a Cache for variable fragment array data for this fragment.
     * @return 
     */
    public abstract ICacheDelegate<IVariableFragment, List<Array>> getCache();
    
    /**
     * Returns the child with name varname. If varname is not found in local
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
     * Returns the child with name varname. If varname is not found in local
     * structure, try to locate it in sourcefiles. First hit wins. Otherwise
     * throw IllegalArgumentException. If
     * <code>loadStructureOnly</code> is true, only the variable structure is
     * retrieved, not the data.
     *
     * @param varname
     * @param loadStructureOnly
     * @return
     */
    public abstract IVariableFragment getChild(String varname,
            boolean loadStructureOnly) throws ResourceNotAvailableException;

    /**
     * Returns the immediate children of this fileFragment. Does not return 
     * children that are only found in referenced source files.
     * 
     * @return 
     */
    public abstract List<IVariableFragment> getImmediateChildren();
    
    /**
     * The registered dimensions of this FileFragment.
     *
     * @return
     */
    public abstract Set<Dimension> getDimensions();

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
