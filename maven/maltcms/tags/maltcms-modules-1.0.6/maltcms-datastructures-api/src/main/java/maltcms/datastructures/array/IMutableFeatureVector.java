/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package maltcms.datastructures.array;

import ucar.ma2.Array;

/**
 *
 * @author nilshoffmann
 */
public interface IMutableFeatureVector extends IFeatureVector{

    /**
         *
         * @param name
         * @param a
         */
        public void addFeature(String name, Array a);

}
