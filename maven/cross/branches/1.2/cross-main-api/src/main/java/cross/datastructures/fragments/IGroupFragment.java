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
