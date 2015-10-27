/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
package net.sf.maltcms.evaluation.api.alignment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sf.maltcms.evaluation.api.Category;

/**
 * <p>MultipleAlignment class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class MultipleAlignment implements Map<Category, AlignmentColumn> {

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return map.toString();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return map.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        return map.equals(o);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<AlignmentColumn> values() {
        return map.values();
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return map.size();
    }

    /** {@inheritDoc} */
    @Override
    public AlignmentColumn remove(Object o) {
        return map.remove(o);
    }

    /** {@inheritDoc} */
    @Override
    public void putAll(Map<? extends Category, ? extends AlignmentColumn> map) {
        this.map.putAll(map);
    }

    /** {@inheritDoc} */
    @Override
    public AlignmentColumn put(Category k, AlignmentColumn v) {
        return map.put(k, v);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Category> keySet() {
        return map.keySet();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<Category, AlignmentColumn>> entrySet() {
        return map.entrySet();
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    /** {@inheritDoc} */
    @Override
    public Object clone() {
        return map.clone();
    }

    /** {@inheritDoc} */
    @Override
    public AlignmentColumn get(Object o) {
        return map.get(o);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(Object o) {
        return map.containsValue(o);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        map.clear();
    }
    private LinkedHashMap<Category, AlignmentColumn> map = new LinkedHashMap<>();
}
