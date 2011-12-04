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
    
    /**
     * 
     * @param distance
     * @return
     */
    public static double asSimilarity(double distance) {
        return -distance;
    }
}
