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

package cross.datastructures.fragments;

/**
 * Abstraction bundling a number of VariableFragments into one logical group.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IGroupFragment extends IIterableFragment {

	/**
	 * Add a number of children.
	 * 
	 * @param fragments
	 */
	public abstract void addChildren(IVariableFragment... fragments);

	/**
	 * Return the child given by the argument. Returns null, if child is not
	 * contained, so run <i>hasChild</i> before.
	 * 
	 * @param varname
	 * @return the VariableFragment given by name, if known to this
	 *         FileFragment, else null.
	 */
	public abstract IVariableFragment getChild(String varname);

	public abstract String getName();

	public abstract IGroupFragment getParent();

	public abstract int getSize();

	/**
	 * Query this fragment for knowledge of a given VariableFragment.
	 * 
	 * @param vf
	 * @return true if a child vf is attached to this FileFragment, else false.
	 */
	public abstract boolean hasChild(IVariableFragment vf);

	/**
	 * Query this fragment for knowledge of a child variable with the given
	 * name.
	 * 
	 * @param varname
	 * @return true if a child of this name is attached to this FileFragment,
	 *         false else.
	 */
	public abstract boolean hasChild(String varname);

	public abstract long nextGID();

	public abstract void setID(long id);

}
