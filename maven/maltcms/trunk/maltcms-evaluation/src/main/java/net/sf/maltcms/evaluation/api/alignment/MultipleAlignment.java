/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package net.sf.maltcms.evaluation.api.alignment;

import net.sf.maltcms.evaluation.api.alignment.AlignmentColumn;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sf.maltcms.evaluation.api.Category;

/**
 *
 * @author nilshoffmann
 */
public class MultipleAlignment implements Map<Category, AlignmentColumn> {

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return map.equals(o);
    }

    @Override
    public Collection<AlignmentColumn> values() {
        return map.values();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public AlignmentColumn remove(Object o) {
        return map.remove(o);
    }

    @Override
    public void putAll(Map<? extends Category, ? extends AlignmentColumn> map) {
        this.map.putAll(map);
    }

    @Override
    public AlignmentColumn put(Category k, AlignmentColumn v) {
        return map.put(k, v);
    }

    @Override
    public Set<Category> keySet() {
        return map.keySet();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Set<Entry<Category, AlignmentColumn>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Object clone() {
        return map.clone();
    }

    @Override
    public AlignmentColumn get(Object o) {
        return map.get(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return map.containsValue(o);
    }

    @Override
    public void clear() {
        map.clear();
    }
    private LinkedHashMap<Category, AlignmentColumn> map = new LinkedHashMap<Category, AlignmentColumn>();
}
