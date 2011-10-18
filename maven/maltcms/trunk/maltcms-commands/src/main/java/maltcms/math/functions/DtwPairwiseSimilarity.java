/*
 * $license$
 *
 * $Id$
 */
package maltcms.math.functions;

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
public class DtwPairwiseSimilarity implements IDtwScoreFunction {

    private double expansionWeight = 1.0;
    private double diagonalWeight = 1.0;
    private double compressionWeight = 1.0;
    
    private IArraySimilarity denseMassSpectraScore = new ArrayCorr();
    
    @Override
    public double apply(int i1, int i2, double time1, double time2, Array t1,
            Array t2) {
        return denseMassSpectraScore.apply(t1, t2);
    }

    @Override
    public boolean minimize() {
        return false;
    }

    @Override
    public void configure(Configuration cfg) {
        
    }
    
}
