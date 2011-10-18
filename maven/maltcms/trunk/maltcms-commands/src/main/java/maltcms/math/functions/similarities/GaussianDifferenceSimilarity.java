/*
 * $license$
 *
 * $Id$
 */
package maltcms.math.functions.similarities;

import lombok.Data;
import maltcms.math.functions.IScalarSimilarity;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
@ServiceProvider(service=IScalarSimilarity.class)
public class GaussianDifferenceSimilarity implements
        IScalarSimilarity {

    private double tolerance = 5.0d;
    private double threshold = 0.0d;

    /**
     * Calculates the scalar
     * @param time1
     * @param time2
     * @return 
     */
    @Override
    public double apply(double time1, double time2) {
        // if no time is supplied, use 1 as default -> cosine/dot product
        // similarity
        final double weight = ((time1 == -1) || (time2 == -1)) ? 1.0d
                : Math.exp(
                -((time1 - time2) * (time1 - time2) / (2.0d * this.tolerance * this.tolerance)));
        // 1 for perfect time correspondence, 0 for really bad time
        // correspondence (towards infinity)
        if (weight - this.threshold < 0) {
            return Double.NEGATIVE_INFINITY;
        }
        return weight;
    }

}
