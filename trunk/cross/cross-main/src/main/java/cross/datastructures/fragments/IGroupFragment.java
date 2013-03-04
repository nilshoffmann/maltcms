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

/**
 * Abstraction bundling a number of VariableFragments into one logical group.
 *
 * @author Nils Hoffmann
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
     * FileFragment, else null.
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
     * false else.
     */
    public abstract boolean hasChild(String varname);

    public abstract long nextGID();

    public abstract void setID(long id);
}
