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
package net.sf.maltcms.evaluation.api.classification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import maltcms.datastructures.array.IFeatureVector;
import net.sf.maltcms.evaluation.spi.classification.Peak2DFeatureVector;

/**
 *
 * @author Nils Hoffmann
 */
public class EntityGroupList<T extends IFeatureVector> implements List<EntityGroup<T>> {

    private List<EntityGroup<T>> delegate = new ArrayList<EntityGroup<T>>();

    private final Set<Category> categories;

    public EntityGroupList(List<Category> categories) {
        ArrayList<Category> cats = new ArrayList<Category>(categories);
        Collections.sort(cats, new Comparator<Category>() {

            @Override
            public int compare(Category o1, Category o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        this.categories = new LinkedHashSet<Category>(cats);
    }

    public EntityGroupList(Category... category) {
        this(Arrays.asList(category));
    }

    public int getCategoriesSize() {
        return categories.size();
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public Entity<T> findMatching(Entity<T> template, String feature) {
        for (Entity<T> e : getEntities(template.getCategory())) {
            if (template.getFeatureVector() instanceof Peak2DFeatureVector && e.getFeatureVector() instanceof Peak2DFeatureVector) {
                Peak2DFeatureVector p2dtemplate = (Peak2DFeatureVector) template.getFeatureVector();
                Peak2DFeatureVector p2de = (Peak2DFeatureVector) e.getFeatureVector();
                if (p2dtemplate.getRowIndex() != -1 && p2de.getRowIndex() != -1 && p2dtemplate.getRowIndex() == p2de.getRowIndex()) {
                    return e;
                }
            } else {
                if (e.getFeatureVector().getFeature(feature).equals(template.getFeatureVector().getFeature(feature))) {
                    return e;
                }
            }
        }
        return null;
    }

    public boolean containsEntity(Entity<T> template, String feature) {
        Entity<T> result = findMatching(template, feature);
        return result == null ? false : true;
    }

    public List<Entity<T>> getEntities(Category c) {
        List<Entity<T>> es = new ArrayList<Entity<T>>();
        for (EntityGroup<T> eg : this) {
            es.add(eg.getEntityForCategory(c));
        }
        return es;
    }

    public EntityGroupList<T> getSubList(Category... categories) {
        EntityGroupList<T> entityGroups = new EntityGroupList<T>(categories);
        for (EntityGroup<T> eg : this) {
            entityGroups.add(eg.subGroup(categories));
        }
        return entityGroups;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Iterator<EntityGroup<T>> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean add(EntityGroup<T> e) {
        if (categories.containsAll(e.getCategories())) {
            return delegate.add(e);
        } else {
            throw new IllegalArgumentException("Can only add a subset of categories!");
        }
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends EntityGroup<T>> c) {
        boolean b = false;
        for (EntityGroup eg : c) {
            b = add(eg);
        }
        return b;
    }

    @Override
    public boolean addAll(int index, Collection<? extends EntityGroup<T>> c) {
        return delegate.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public EntityGroup<T> get(int index) {
        return delegate.get(index);
    }

    @Override
    public EntityGroup<T> set(int index, EntityGroup<T> element) {
        if (categories.containsAll(element.getCategories())) {
            return delegate.set(index, element);
        } else {
            throw new IllegalArgumentException("Can only add a subset of categories!");
        }
    }

    @Override
    public void add(int index, EntityGroup<T> element) {
        if (categories.containsAll(element.getCategories())) {
            delegate.add(index, element);
        } else {
            throw new IllegalArgumentException("Can only add a subset of categories!");
        }
    }

    @Override
    public EntityGroup<T> remove(int index) {
        return delegate.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    @Override
    public ListIterator<EntityGroup<T>> listIterator() {
        return delegate.listIterator();
    }

    @Override
    public ListIterator<EntityGroup<T>> listIterator(int index) {
        return delegate.listIterator(index);
    }

    @Override
    public List<EntityGroup<T>> subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }
}
