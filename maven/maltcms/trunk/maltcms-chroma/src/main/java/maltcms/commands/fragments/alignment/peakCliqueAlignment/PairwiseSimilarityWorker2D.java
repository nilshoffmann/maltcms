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
import maltcms.datastructures.peak.Peak;
import maltcms.math.functions.IScalarArraySimilarity;

/**
 *
 * @author nilshoffmann
 */
@Data
@Slf4j
public class PairwiseSimilarityWorker2D implements Callable<Integer>, Serializable {

    private String name;
    private List<Peak> lhsPeaks;
    private List<Peak> rhsPeaks;
    private IScalarArraySimilarity similarityFunction;
    private double maxRTDifferenceRt1 = 60.0d;
    private double maxRTDifferenceRt2 = 1.0d;

    @Override
    public Integer call() {
        log.debug(name);
        EvalTools.notNull(lhsPeaks, this);
        EvalTools.notNull(rhsPeaks, this);
        int elemCnt = 0;
        for (final Peak p1 : lhsPeaks) {
            for (final Peak p2 : rhsPeaks) {
                // skip peaks, which are too far apart
                double rt1p1 = p1.getFeature("first_column_elution_time").getDouble(0);
                double rt1p2 = p2.getFeature("first_column_elution_time").getDouble(0);
                double rt2p1 = p1.getFeature("second_column_elution_time").getDouble(0);
                double rt2p2 = p2.getFeature("second_column_elution_time").getDouble(0);
                // cutoff to limit calculation work
                // this has a better effect, than applying the limit
                // within the similarity function only
                // of course, this limit should be larger
                // than the limit within the similarity function
                if (Math.abs(rt1p1 - rt1p2) < this.maxRTDifferenceRt1 || Math.abs(rt2p1 - rt2p2) < this.maxRTDifferenceRt2) {
                    // the similarity is symmetric:
                    // sim(a,b) = sim(b,a)
                    final Double d = similarityFunction.apply(new double[]{rt1p1,rt2p1}, new double[]{rt1p2,rt2p2}, p1.getMsIntensities(), p2.getMsIntensities());
                    p1.addSimilarity(p2, d);
                    p2.addSimilarity(p1, d);
                }
                elemCnt++;
            }
        }
        return elemCnt;
    }
}