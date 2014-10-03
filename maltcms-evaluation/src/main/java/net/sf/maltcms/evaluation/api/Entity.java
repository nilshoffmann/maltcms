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
package net.sf.maltcms.evaluation.api;

import maltcms.datastructures.array.IFeatureVector;

/**
 * An entity represents a classified object. It has a feature vector, a category
 * and a class label.
 *
 * @author Nils Hoffmann
 * 
 */
public class Entity {

    private final String classLabel;
    private final Category c;
    private final IFeatureVector featureVector;

    /**
     * <p>Constructor for Entity.</p>
     *
     * @param featureVector a {@link maltcms.datastructures.array.IFeatureVector} object.
     * @param c a {@link net.sf.maltcms.evaluation.api.Category} object.
     * @param classLabel a {@link java.lang.String} object.
     */
    public Entity(IFeatureVector featureVector, Category c, String classLabel) {
        this.featureVector = featureVector;
        this.c = c;
        this.classLabel = classLabel;
    }

    /**
     * <p>Getter for the field <code>classLabel</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassLabel() {
        return classLabel;
    }

    /**
     * <p>getCategory.</p>
     *
     * @return a {@link net.sf.maltcms.evaluation.api.Category} object.
     */
    public Category getCategory() {
        return c;
    }

    /**
     * <p>Getter for the field <code>featureVector</code>.</p>
     *
     * @return a {@link maltcms.datastructures.array.IFeatureVector} object.
     */
    public IFeatureVector getFeatureVector() {
        return featureVector;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
//		sb.append("Entity for category "+getCategory()+" with label "+getClassLabel()+"\n");
//                sb.append(getClassLabel());
        sb.append(getFeatureVector().toString());
        return sb.toString();
    }
}
