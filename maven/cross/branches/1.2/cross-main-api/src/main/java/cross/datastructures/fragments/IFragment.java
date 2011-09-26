/*
 * 
 *
 * $Id$
 */

package cross.datastructures.fragments;

import java.util.Comparator;
import java.util.List;

import ucar.nc2.Attribute;
import cross.datastructures.StatsMap;
import cross.io.xml.IXMLSerializable;

/**
 * 
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IFragment extends Comparable<Object>, Comparator<IFragment>,
        IXMLSerializable {

	/**
	 * Compare Fragments by comparing their string representations.
	 */
    @Override
	public int compare(IFragment arg0, IFragment arg1);

	/**
	 * Only perform comparison on instances of Fragment.
	 */
    @Override
	public int compareTo(Object arg0);

	/**
	 * Return a given Attribute.
	 * 
	 * @param a
	 * @return
	 */
	public abstract Attribute getAttribute(Attribute a);

	/**
	 * Return a given Attribute by name.
	 * 
	 * @param name
	 * @return
	 */
	public abstract Attribute getAttribute(String name);

	/**
	 * Return attributes of Fragment.
	 * 
	 * @return
	 */
	public abstract List<Attribute> getAttributes();

	/**
	 * Retrieve statistics from a Fragment.
	 * 
	 * @return
	 */
	public abstract StatsMap getStats();

	/**
	 * Query for an Attribute.
	 * 
	 * @param a
	 * @return
	 */
	public abstract boolean hasAttribute(Attribute a);

	/**
	 * Query for an Attribute by name.
	 * 
	 * @param name
	 * @return
	 */
	public abstract boolean hasAttribute(String name);

	/**
	 * Set attributes on a fragment.
	 * 
	 * @param a
	 */
	public abstract void setAttributes(Attribute... a);

	/**
	 * Set statistics on a Fragment.
	 * 
	 * @param stats1
	 */
	public abstract void setStats(StatsMap stats1);

}
