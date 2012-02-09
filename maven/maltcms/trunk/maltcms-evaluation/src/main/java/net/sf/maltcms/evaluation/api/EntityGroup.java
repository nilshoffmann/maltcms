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
package net.sf.maltcms.evaluation.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import maltcms.datastructures.array.IFeatureVector;

/**
 * A group of Entity objects which all share the same 
 * class label, meaning they are grouped by some algorithm
 * as related entities.
 * 
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
public class EntityGroup implements Comparable<EntityGroup> {

    private final HashMap<Category, Entity> categoryToEntityMap;

    public EntityGroup(Entity... e) {
        categoryToEntityMap = new HashMap<Category, Entity>();
        for (Entity ent : e) {
            categoryToEntityMap.put(ent.getCategory(), ent);
        }
    }

    public void addEntity(Category c, Entity e) {
        if (this.categoryToEntityMap.containsKey(c)) {
            System.err.println("Element with key " + c + " already contained in EntityGroup!");
        }
        this.categoryToEntityMap.put(c, e);
    }

    public List<Entity> getEntities() {
        return new ArrayList<Entity>(categoryToEntityMap.values());
    }

    public Entity getEntityForCategory(Category c) {
        return categoryToEntityMap.get(c);
    }

    public Set<Category> getCategories() {
        return categoryToEntityMap.keySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<Category> l = new ArrayList<Category>(getCategories());
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
        if(equals(o)) {
            return 0;
        }
        Set<Category> s = getCategories();
        for(Category c:s) {
            IFeatureVector ifv = getEntityForCategory(c).getFeatureVector();
            IFeatureVector ofv = o.getEntityForCategory(c).getFeatureVector();
            List<String> features = ifv.getFeatureNames();
            LinkedHashSet<String> commonFeatures = new LinkedHashSet<String>(features);
            commonFeatures.retainAll(ofv.getFeatureNames());
            for(String featureName:commonFeatures) {
                int v = ifv.getFeature(featureName).toString().compareTo(ofv.getFeature(featureName).toString());
                if(v!=0) {
                    return v;
                }
            }
        }
        return 0;
    }
}
