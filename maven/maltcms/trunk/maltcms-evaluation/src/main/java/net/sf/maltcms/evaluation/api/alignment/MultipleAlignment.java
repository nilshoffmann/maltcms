/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class MultipleAlignment implements Map<Category, AlignmentColumn>{

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
    
    private LinkedHashMap<Category,AlignmentColumn> map = new LinkedHashMap<Category, AlignmentColumn>();
    
    
}
