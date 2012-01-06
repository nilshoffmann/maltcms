/**
 * 
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
