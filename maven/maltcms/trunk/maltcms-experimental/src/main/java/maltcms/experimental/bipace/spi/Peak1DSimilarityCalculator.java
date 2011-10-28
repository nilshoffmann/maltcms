/*
 * $license$
 *
 * $Id$
 */
package maltcms.experimental.bipace.spi;

import lombok.Data;
import maltcms.datastructures.peak.Peak;
import maltcms.experimental.bipace.api.PeakSimilarityCalculator;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public class Peak1DSimilarityCalculator extends PeakSimilarityCalculator<Peak> {

    private double maxRtDifference = 10.0d;

    @Override
    public double calculateSimilarity(Peak p1, Peak p2) {
        // skip peaks, which are too far apart
        double rt1 = p1.getScanAcquisitionTime();
        double rt2 = p2.getScanAcquisitionTime();
        // cutoff to limit calculation work
        // this has a better effect, than applying the limit
        // within the similarity function only
        // of course, this limit should be larger
        // than the limit within the similarity function

        if (Math.abs(rt1 - rt2) < maxRtDifference) {
            // the similarity is symmetric:
            // sim(a,b) = sim(b,a)
            return getSimilarityFunction().apply(new double[]{rt1},
                    new double[]{rt2}, p1.getMSIntensities(), p2.
                    getMSIntensities());
        }
        return Double.NaN;
    }

    @Override
    public PeakSimilarityCalculator<Peak> copy() {
        Peak1DSimilarityCalculator copy = new Peak1DSimilarityCalculator();
        copy.setPeakListA(getPeakListA());
        copy.setPeakListB(getPeakListB());
        copy.setSimilarityFunction(getSimilarityFunction());
        copy.setMaxRtDifference(maxRtDifference);
        return copy;
    }
}
