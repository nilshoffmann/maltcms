/**
 * 
 */
package maltcms.commands;

import java.util.List;
import maltcms.datastructures.array.IMutableFeatureVector;
import maltcms.datastructures.feature.DefaultFeatureVector;
import ucar.ma2.Array;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class ArrayStatsMap implements IMutableFeatureVector {

	/**
     * 
     */
    private static final long serialVersionUID = -8374451195942782968L;

    private DefaultFeatureVector featureVector = new DefaultFeatureVector();

    @Override
    public String toString() {
        return featureVector.toString();
    }

    @Override
    public List<String> getFeatureNames() {
        return featureVector.getFeatureNames();
    }

    @Override
    public Array getFeature(String name) {
        return featureVector.getFeature(name);
    }

    @Override
    public void addFeature(String name, Array a) {
        featureVector.addFeature(name, a);
    }



}
