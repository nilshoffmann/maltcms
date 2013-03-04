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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import ucar.nc2.Attribute;

/**
 * Objects of this type hold netcdf associated metadata as Attributes,
 * accessible by name.
 *
 * @author Nils Hoffmann
 *
 */
public class Metadata implements Iterable<Attribute> {

    /**
     *
     */
    protected HashMap<String, Attribute> hm = new HashMap<String, Attribute>();

    public Metadata() {
    }

    public Metadata(final List<Attribute> l) {
        this();
        for (final Attribute a : l) {
            add(a);
        }
    }

    /**
     *
     * @param a
     */
    public void add(final Attribute a) {
        final Attribute b = new Attribute(a.getName(), a);
        this.hm.put(b.getName(), b);
    }

    /**
     *
     * @return
     */
    public Collection<Attribute> asCollection() {
        return this.hm.values();
    }

    /**
     *
     * @param name
     * @return
     */
    public Attribute get(final String name) {
        return this.hm.get(name);
    }

    /**
     *
     * @param name
     * @return
     */
    public boolean has(final String name) {
        return this.hm.containsKey(name);
    }

    /**
     *
     * @return
     */
    @Override
    public Iterator<Attribute> iterator() {
        return this.hm.values().iterator();
    }

    /**
     *
     * @return
     */
    public Collection<String> keySet() {
        return this.hm.keySet();
    }
}
