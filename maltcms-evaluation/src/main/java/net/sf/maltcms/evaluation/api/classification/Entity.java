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
import maltcms.datastructures.array.IFeatureVector;

/**
 * An entity represents a classified object. It has a feature vector, a category
 * and a class label.
 *
 * @author Nils Hoffmann
 *
 *
 */
public class Entity<T extends IFeatureVector> implements Serializable {

    private final String classLabel;
    private final Category c;
    private final T featureVector;

    public Entity(T featureVector, Category c, String classLabel) {
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

    public T getFeatureVector() {
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
