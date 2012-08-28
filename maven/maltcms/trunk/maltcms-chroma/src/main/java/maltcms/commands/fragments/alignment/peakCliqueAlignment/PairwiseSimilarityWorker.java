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
public class PairwiseSimilarityWorker implements Callable<Integer>, Serializable {

    private String name;
    private List<Peak> lhsPeaks;
    private List<Peak> rhsPeaks;
    private IScalarArraySimilarity similarityFunction;
    private double maxRTDifference = 60.0d;

    @Override
    public Integer call() {
        log.debug(name);
        EvalTools.notNull(lhsPeaks, this);
        EvalTools.notNull(rhsPeaks, this);
        int elemCnt = 0;
        for (final Peak p1 : lhsPeaks) {
            for (final Peak p2 : rhsPeaks) {
                // skip peaks, which are too far apart
                double rt1 = p1.getScanAcquisitionTime();
                double rt2 = p2.getScanAcquisitionTime();
                // cutoff to limit calculation work
                // this has a better effect, than applying the limit
                // within the similarity function only
                // of course, this limit should be larger
                // than the limit within the similarity function
                if (Math.abs(rt1 - rt2) < this.maxRTDifference) {
                    // the similarity is symmetric:
                    // sim(a,b) = sim(b,a)
                    final Double d = similarityFunction.apply(new double[]{rt1}, new double[]{rt2}, p1.getMsIntensities(), p2.getMsIntensities());
                    p1.addSimilarity(p2, d);
                    p2.addSimilarity(p1, d);
                }
                elemCnt++;
            }
        }
        return elemCnt;
    }
}
