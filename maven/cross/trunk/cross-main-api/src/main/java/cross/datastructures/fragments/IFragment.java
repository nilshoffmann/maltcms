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

import cross.datastructures.StatsMap;
import cross.io.xml.IXMLSerializable;
import java.util.Comparator;
import java.util.List;
import ucar.nc2.Attribute;

/**
 *
 *
 * @author Nils Hoffmann
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
     * Add an attribute
     *
     * @param a
     */
    public abstract void addAttribute(Attribute a);

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
