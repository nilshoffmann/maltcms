/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cross.datastructures.feature;

import ucar.ma2.Array;

/**
 *
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public interface IMutableFeatureVector extends IFeatureVector {

    /**
     *
     * @param name
     * @param a
     */
    public void addFeature(String name, Array a);
}
