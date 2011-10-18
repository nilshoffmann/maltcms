/*
 * $license$
 *
 * $Id$
 */
package maltcms.math.functions;

import maltcms.math.functions.similarities.GaussianDifferenceSimilarity;
import maltcms.math.functions.similarities.ArrayCorr;
import lombok.Data;
import maltcms.commands.distances.IDtwScoreFunction;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public class DtwTimePenalizedPairwiseSimilarity implements IDtwScoreFunction {

    private double expansionWeight = 1.0;
    private double diagonalWeight = 1.0;
    private double compressionWeight = 1.0;
    
    private IScalarSimilarity retentionTimeScore = new GaussianDifferenceSimilarity();
    private IArraySimilarity denseMassSpectraScore = new ArrayCorr();
    
    @Override
    public double apply(int i1, int i2, double time1, double time2, Array t1,
            Array t2) {
        final double  rtScore = retentionTimeScore.apply(time1, time2);
        if(rtScore==Double.NEGATIVE_INFINITY || rtScore==Double.NaN) {
            return Double.NEGATIVE_INFINITY;
        }
        double score = denseMassSpectraScore.apply(t1, t2);
        return rtScore*score;
    }

    @Override
    public boolean minimize() {
        return false;
    }

    @Override
    public void configure(Configuration cfg) {
        
    }
    
}
