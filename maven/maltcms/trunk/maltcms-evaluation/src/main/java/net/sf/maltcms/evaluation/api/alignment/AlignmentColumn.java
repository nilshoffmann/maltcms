/**
 *
 */
package net.sf.maltcms.evaluation.api.alignment;

import java.util.Arrays;
import java.util.List;

import maltcms.datastructures.array.IFeatureVector;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
public class AlignmentColumn implements IFeatureVector {

    /**
     *
     */
    private static final long serialVersionUID = -5936343655074144856L;
    private final ArrayDouble.D1 values;

    public AlignmentColumn(double... rt) {
        this.values = (ArrayDouble.D1) Array.factory(rt);
    }

    /*
     * (non-Javadoc) @see
     * maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
     */
    @Override
    public Array getFeature(String name) {
        if (name.equals("RT")) {
            return this.values;
        }
        return null;
    }

    /*
     * (non-Javadoc) @see
     * maltcms.datastructures.array.IFeatureVector#getFeatureNames()
     */
    @Override
    public List<String> getFeatureNames() {
        return Arrays.asList("RT");
    }

    public ArrayDouble.D1 getRT() {
        return this.values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RT = " + getRT());
        return sb.toString();
    }
}
