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

import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cross.datastructures.tools.EvalTools;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.feature.PairwiseValueMap;
import maltcms.datastructures.peak.IPeak;
import maltcms.math.functions.IScalarArraySimilarity;

/**
 *
 * @author nilshoffmann
 */
@Data
@Slf4j
public class PairwiseSimilarityWorker2D implements Callable<BBHPeakEdgeList>, Serializable {

    private String name;
    private List<? extends IPeak> lhsPeaks;
    private List<? extends IPeak> rhsPeaks;
    private IScalarArraySimilarity similarityFunction;
    private double maxRTDifferenceRt1 = 60.0d;
    private double maxRTDifferenceRt2 = 1.0d;

    @Override
    public BBHPeakEdgeList call() {
        log.debug(name);
        EvalTools.notNull(lhsPeaks, this);
        EvalTools.notNull(rhsPeaks, this);
        for (final IPeak p1 : lhsPeaks) {
            final Peak2D p12d = (Peak2D)p1;
            final double rt1p1 = p12d.getFirstColumnElutionTime();
            final double rt2p1 = p12d.getSecondColumnElutionTime();
            for (final IPeak p2 : rhsPeaks) {
                final Peak2D p22d = (Peak2D)p2;
                // skip peaks, which are too far apart
                final double rt1p2 = p22d.getFirstColumnElutionTime();
                final double rt2p2 = p22d.getSecondColumnElutionTime();
                // cutoff to limit calculation work
                // this has a better effect, than applying the limit
                // within the similarity function only
                // of course, this limit should be larger
                // than the limit within the similarity function
                if ((Math.abs(rt1p1 - rt1p2) < this.maxRTDifferenceRt1 && Math.abs(rt2p1 - rt2p2) < this.maxRTDifferenceRt2)) {
                    // the similarity is symmetric:
                    // sim(a,b) = sim(b,a)
                    final double d = similarityFunction.apply(new double[]{rt1p1,rt2p1}, new double[]{rt1p2,rt2p2}, p1.getMsIntensities(), p2.getMsIntensities());
					p1.addSimilarity(p2, d);
					p2.addSimilarity(p1, d);
                }
            }
        }
		BBHFinder bbhfinder = new BBHFinder();
		return bbhfinder.findBiDiBestHits(lhsPeaks,rhsPeaks);
    }
    
    @Override
    public String toString() {
        return name;
    }
}
