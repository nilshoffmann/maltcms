/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package net.sf.maltcms.evaluation.spi.classification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Nils Hoffmann
 */
public class MultiMap<K, V> {

    private Map<K, Collection<V>> map = new LinkedHashMap<K, Collection<V>>();

    public Set<K> keySet() {
        return map.keySet();
    }

    public Collection<V> get(K k) {
        Collection<V> c = map.get(k);
        if (c == null) {
            return Collections.emptyList();
        }
        return c;
    }

    public void put(K k, V v) {
        if (map.containsKey(k)) {
            Collection<V> c = map.get(k);
            c.add(v);
        } else {
            Collection<V> c = new ArrayList<V>();
            c.add(v);
            map.put(k, c);
        }
    }

    public void remove(K k) {
        map.remove(k);
    }
}
