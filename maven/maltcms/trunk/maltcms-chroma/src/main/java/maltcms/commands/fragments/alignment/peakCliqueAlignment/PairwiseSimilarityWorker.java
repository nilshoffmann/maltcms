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
package maltcms.commands.fragments.alignment.peakCliqueAlignment;

import cross.datastructures.tools.EvalTools;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.IBipacePeak;
import maltcms.datastructures.peak.IPeak;
import maltcms.math.functions.IScalarArraySimilarity;

/**
 *
 * @author nilshoffmann
 */
@Data
@Slf4j
public class PairwiseSimilarityWorker implements Callable<BBHPeakEdgeList>, Serializable {

    private String name;
    private List<? extends IBipacePeak> lhsPeaks;
    private List<? extends IBipacePeak> rhsPeaks;
    private IScalarArraySimilarity similarityFunction;
    private double maxRTDifference = 60.0d;

    @Override
    public BBHPeakEdgeList call() {
        log.debug(name);
        EvalTools.notNull(lhsPeaks, this);
        EvalTools.notNull(rhsPeaks, this);
        for (final IBipacePeak p1 : lhsPeaks) {
            final double rt1 = p1.getScanAcquisitionTime();
            for (final IBipacePeak p2 : rhsPeaks) {
                // skip peaks, which are too far apart
                final double rt2 = p2.getScanAcquisitionTime();
                // cutoff to limit calculation work
                // this has a better effect, than applying the limit
                // within the similarity function only
                // of course, this limit should be larger
                // than the limit within the similarity function
                if (Math.abs(rt1 - rt2) < this.maxRTDifference) {
                    // the similarity is symmetric:
                    // sim(a,b) = sim(b,a)
					final double d = similarityFunction.apply(new double[]{rt1}, new double[]{rt2}, p1.getMsIntensities(), p2.getMsIntensities());
					p1.addSimilarity(p2, d);
					p2.addSimilarity(p1, d);
				}
            }
        }
        BBHFinder bbhfinder = new BBHFinder();
		BBHPeakEdgeList bbhpr = bbhfinder.findBiDiBestHits(lhsPeaks,rhsPeaks);
		return bbhpr;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
