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
