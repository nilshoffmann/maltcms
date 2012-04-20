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
                    new double[]{rt2}, p1.getMsIntensities(), p2.
                    getMsIntensities());
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
