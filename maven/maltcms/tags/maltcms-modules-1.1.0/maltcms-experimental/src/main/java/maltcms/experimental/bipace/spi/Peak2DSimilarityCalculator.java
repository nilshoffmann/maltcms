/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.experimental.bipace.spi;

import lombok.Data;
import maltcms.experimental.bipace.datastructures.spi.Peak2D;
import maltcms.experimental.bipace.api.PeakSimilarityCalculator;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public class Peak2DSimilarityCalculator extends PeakSimilarityCalculator<Peak2D> {

    private double maxRt1Difference = 60.0d;
    private double maxRt2Difference = 4.0d;

    @Override
    public double calculateSimilarity(Peak2D p1, Peak2D p2) {
        // skip peaks, which are too far apart
        double p1rt1 = p1.getRetentionTime1();
        double p1rt2 = p1.getRetentionTime2();

        double p2rt1 = p2.getRetentionTime1();
        double p2rt2 = p2.getRetentionTime2();
        // cutoff to limit calculation work
        // this has a better effect, than applying the limit
        // within the similarity function only
        // of course, this limit should be larger
        // than the limit within the similarity function

        if (Math.abs(p1rt1 - p2rt1) < maxRt1Difference && Math.abs(p1rt2 - p2rt2) < maxRt2Difference) {
            // the similarity is symmetric:
            // sim(a,b) = sim(b,a)
            return getSimilarityFunction().apply(new double[]{p1rt1, p1rt2},
                    new double[]{p2rt1, p2rt2}, p1.getMSIntensities(), p2.
                    getMSIntensities());
        }
        return Double.NaN;
    }

    @Override
    public PeakSimilarityCalculator<Peak2D> copy() {
        Peak2DSimilarityCalculator copy = new Peak2DSimilarityCalculator();
        copy.setPeakListA(getPeakListA());
        copy.setPeakListB(getPeakListB());
        copy.setSimilarityFunction(getSimilarityFunction());
        copy.setMaxRt1Difference(maxRt1Difference);
        copy.setMaxRt2Difference(maxRt2Difference);
        return copy;
    }
}
