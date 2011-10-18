/*
 * $license$
 *
 * $Id$
 */
package maltcms.math.functions.similarities;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class SimilarityTools {

    public static double transformToUnitRange(double value) {
        double result = Math.exp(
                -((value) * (value) / (2.0d)));
        return result;
    }
}
