/**
 * 
 */
package maltcms.math.functions.similarities;

import ucar.ma2.Array;
import ucar.ma2.Index;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayWeightedCosine implements IArraySimilarity {

    @Override
    public double apply(final Array t1, final Array t2) {
        Index i1idx = t1.getIndex();
        Index i2idx = t2.getIndex();
        double s1 = 0, s2 = 0;
        double v = 0.0d;
        for (int i = 0; i < t1.getShape()[0]; i++) {
            s1 += (i + 1.0) * t1.getDouble(i1idx.set(i));
            s2 += (i + 1.0) * t2.getDouble(i2idx.set(i));
        }
        double w1 = 0, w2 = 0;
        for (int i = 0; i < t1.getShape()[0]; i++) {
            w1 = (i + 1.0) * t1.getDouble(i1idx.set(i)) / s1;
            w2 = (i + 1.0) * t2.getDouble(i2idx.set(i)) / s2;
            v += (w1 * w2);
        }
        return v;
    }

}
