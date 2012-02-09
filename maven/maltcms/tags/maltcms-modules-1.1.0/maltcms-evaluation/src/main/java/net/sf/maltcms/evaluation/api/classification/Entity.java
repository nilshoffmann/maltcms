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
package net.sf.maltcms.evaluation.api.classification;

import maltcms.datastructures.array.IFeatureVector;

/**
 * An entity represents a classified object.
 * It has a feature vector, a category and a class label.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
public class Entity {

    private final String classLabel;
    private final Category c;
    private final IFeatureVector featureVector;

    public Entity(IFeatureVector featureVector, Category c, String classLabel) {
        this.featureVector = featureVector;
        this.c = c;
        this.classLabel = classLabel;
    }

    public String getClassLabel() {
        return classLabel;
    }

    public Category getCategory() {
        return c;
    }

    public IFeatureVector getFeatureVector() {
        return featureVector;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
//		sb.append("Entity for category "+getCategory()+" with label "+getClassLabel()+"\n");
//                sb.append(getClassLabel());
        sb.append(getFeatureVector().toString());
        return sb.toString();
    }
}
