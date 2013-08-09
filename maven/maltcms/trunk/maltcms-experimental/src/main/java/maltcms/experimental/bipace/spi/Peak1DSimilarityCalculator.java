/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.experimental.bipace.spi;

import lombok.Data;
import maltcms.datastructures.peak.IPeak;
import maltcms.experimental.bipace.api.PeakSimilarityCalculator;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public class Peak1DSimilarityCalculator extends PeakSimilarityCalculator<IPeak> {

    private double maxRtDifference = 10.0d;

    @Override
    public double calculateSimilarity(IPeak p1, IPeak p2) {
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
    public PeakSimilarityCalculator<IPeak> copy() {
        Peak1DSimilarityCalculator copy = new Peak1DSimilarityCalculator();
        copy.setPeakListA(getPeakListA());
        copy.setPeakListB(getPeakListB());
        copy.setSimilarityFunction(getSimilarityFunction());
        copy.setMaxRtDifference(maxRtDifference);
        return copy;
    }
}
