/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
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
 * 
 * $Id$
 */

/**
 * 
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
	 * @param maxrecursion
	 * @return
	 */
	public abstract IVariableFragment getChild(String varname)
	        throws ResourceNotAvailableException;

	/**
	 * Return the child with name varname. If varname is not found in local
	 * structure, try to locate it in sourcefiles. First hit wins. Otherwise
	 * throw IllegalArgumentException.
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
	public abstract String getName();

	public abstract IGroupFragment getParent();

	/**
	 * Return the number of children of this FileFragment.
	 * 
	 * @return
	 */
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

	/**
	 * Creates an iterator over all children of this FileFragment by the time of
	 * creation of the iterator.
	 */
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

	public abstract String toString();

	public boolean isModified();

}
