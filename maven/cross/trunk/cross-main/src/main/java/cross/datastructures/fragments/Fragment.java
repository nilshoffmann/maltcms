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
import cross.datastructures.fragments.IFragment;
import cross.datastructures.tools.EvalTools;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jdom.Element;
import ucar.nc2.Attribute;

/**
 * Abstract base class for Fragments. Defines and implements default behavior.
 *
 * @author Nils Hoffmann
 *
 */
public class Fragment implements IFragment {

    private StatsMap stats;
    private Metadata attributes = new Metadata();
    private final UUID uid = UUID.randomUUID();

    /**
     * Append attributes to Element e.
     *
     * @param e
     */
    protected void appendAttributes(final Element e) {
        // log.debug("Appending xml for named group "+getName());
        if (this.attributes != null) {
            final Element group = new Element("attributes");
            group.setAttribute("size", ""
                    + this.attributes.asCollection().size());
            e.addContent(group);
            for (final String s : this.attributes.keySet()) {
                final Element attr = new Element("attribute");
                final Attribute a = this.attributes.get(s);
                if (a.isString()) {
                    attr.setAttribute(a.getName(), a.getStringValue());
                } else if (a.isArray()) {
                    attr.setAttribute(a.getName(), a.toString());
                }
                group.addContent(attr);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.io.misc.IXMLSerializable#appendXML(org.jdom.Element)
     */
    /**
     *
     * @param e
     */
    @Override
    public void appendXML(final Element e) {
        appendAttributes(e);
    }

    /**
     * Compare Fragments by comparing their string representations.
     */
    @Override
    public int compare(final IFragment arg0, final IFragment arg1) {
        return arg0.toString().compareTo(arg1.toString());
    }

    /**
     * Only perform comparison on instances of Fragment.
     */
    @Override
    public int compareTo(final Object arg0) {
        if (arg0 instanceof IFragment) {
            return (compare(this, (IFragment) arg0));
        }

        return -1;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFragment#getAttribute(ucar.nc2.Attribute)
     */
    @Override
    public Attribute getAttribute(final Attribute a) {
        return getAttribute(a.getName());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFragment#getAttribute(java.lang.String)
     */
    @Override
    public Attribute getAttribute(final String name) {
        return this.attributes.get(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFragment#getAttributes()
     */
    @Override
    public List<Attribute> getAttributes() {
        final ArrayList<Attribute> al = new ArrayList<Attribute>();
        if (this.attributes != null) {
            al.addAll(this.attributes.asCollection());
        }
        return al;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFragment#getStats()
     */
    @Override
    public StatsMap getStats() {
        if (this.stats == null) {
            this.stats = new StatsMap(this);
        }
        return this.stats;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFragment#hasAttribute(ucar.nc2.Attribute)
     */
    @Override
    public boolean hasAttribute(final Attribute a) {
        return hasAttribute(a.getName());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFragment#hasAttribute(java.lang.String)
     */
    @Override
    public boolean hasAttribute(final String name) {
        return this.attributes.has(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFragment#setAttributes(ucar.nc2.Attribute
     * )
     */
    @Override
    public void setAttributes(final Attribute... a) {
        if (this.attributes == null) {
            this.attributes = new Metadata();
        }
        for (final Attribute attr : a) {
            if (attr != null) {
                // if (!this.attributes.has(attr.getName())) {
                this.attributes.add(attr);
                // }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFragment#setStats(cross.datastructures
     * .StatsMap)
     */
    @Override
    public void setStats(final StatsMap stats1) {
        EvalTools.notNull(stats1, this);
        this.stats = stats1;
    }

    @Override
    public void addAttribute(Attribute a) {
        this.attributes.add(a);
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.uid != null ? this.uid.hashCode() : 0);
        return hash;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Fragment other = (Fragment) obj;
        if (this.uid != other.uid && (this.uid == null || !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }
}
