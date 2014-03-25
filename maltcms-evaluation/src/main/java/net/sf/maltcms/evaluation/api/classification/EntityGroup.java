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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import maltcms.datastructures.array.IFeatureVector;

/**
 * A group of Entity objects which all share the same class label, meaning they
 * are grouped by some algorithm as related entities.
 *
 *
 * @author Nils Hoffmann
 *
 *
 */
public class EntityGroup<T extends IFeatureVector> implements Comparable<EntityGroup>, Serializable {

    private final HashMap<Category, Entity<T>> categoryToEntityMap;

    public EntityGroup(Entity<T>... e) {
        categoryToEntityMap = new HashMap<>();
        for (Entity<T> ent : e) {
            categoryToEntityMap.put(ent.getCategory(), ent);
        }
    }

    public EntityGroup<T> subGroup(Category... c) {
        List<Entity<T>> entities = new ArrayList<>(c.length);
        for (Category cat : c) {
            entities.add(getEntityForCategory(cat));
        }
        EntityGroup<T> eg = new EntityGroup<>(entities.toArray(new Entity[c.length]));
        return eg;
    }

    public void addEntity(Category c, Entity<T> e) {
        if (this.categoryToEntityMap.containsKey(c)) {
            System.err.println("Element with key " + c + " already contained in EntityGroup!");
        }
        this.categoryToEntityMap.put(c, e);
    }

    public List<Entity<T>> getEntities() {
        return new ArrayList<>(categoryToEntityMap.values());
    }

    public Entity<T> getEntityForCategory(Category c) {
        return categoryToEntityMap.get(c);
    }

    public Set<Category> getCategories() {
        return categoryToEntityMap.keySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<Category> l = new ArrayList<>(getCategories());
        Collections.sort(l);
        StringBuilder header = new StringBuilder();
        for (Category c : l) {
            header.append(c.getName() + "\t");
            sb.append(getEntityForCategory(c) + "\t");
        }
        header.append("\n");
        header.append(sb);
        return header.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + (this.categoryToEntityMap != null ? this.categoryToEntityMap.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntityGroup other = (EntityGroup) obj;
        if (this.categoryToEntityMap != other.categoryToEntityMap && (this.categoryToEntityMap == null || !this.categoryToEntityMap.equals(other.categoryToEntityMap))) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(EntityGroup o) {
        if (equals(o)) {
            return 0;
        }
        Set<Category> s = getCategories();
        for (Category c : s) {
            IFeatureVector ifv = getEntityForCategory(c).getFeatureVector();
            IFeatureVector ofv = o.getEntityForCategory(c).getFeatureVector();
            List<String> features = ifv.getFeatureNames();
            LinkedHashSet<String> commonFeatures = new LinkedHashSet<>(features);
            commonFeatures.retainAll(ofv.getFeatureNames());
            for (String featureName : commonFeatures) {
                int v = ifv.getFeature(featureName).toString().compareTo(ofv.getFeature(featureName).toString());
                if (v != 0) {
                    return v;
                }
            }
        }
        return 0;
    }
}
