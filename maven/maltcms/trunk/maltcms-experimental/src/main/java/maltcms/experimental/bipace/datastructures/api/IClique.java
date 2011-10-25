/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package maltcms.experimental.bipace.datastructures.api;

import java.util.List;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.array.IMutableFeatureVector;

/**
 *
 * @author nilshoffmann
 */
public interface IClique<T extends IFeatureVector> {

    boolean add(T p) throws IllegalArgumentException;

    void clear();

    T getCliqueCentroid();

    List<T> getFeatureVectorList();

    long getID();

    void setCentroid(T ifv);

    int size();

    IMutableFeatureVector getArrayStatsMap();

}
