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
package maltcms.commands.fragments.alignment.peakCliqueAlignment2;

import cross.annotations.Configurable;
import cross.datastructures.fragments.FileFragment;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.feature.PairwiseValueMap;
import maltcms.datastructures.peak.Peak;
import maltcms.math.functions.IScalarArraySimilarity;

/**
 *
 * @author nilshoffmann
 */
@Data
@Slf4j
public class PairwiseSimilarityWorker implements Callable<PairwiseValueMap>, Serializable {

    private String name;
    private URI lhs;
    private URI rhs;
    private UUID id;
    @Configurable
    private IScalarArraySimilarity similarityFunction;
    @Configurable
    private double maxRTDifference = 60.0d;
    @Configurable
    private String storageType = "ROW_COMPRESSED";
    @Configurable
    private double defaultValue = Double.NEGATIVE_INFINITY;
    @Configurable
    private IPeakLoader peakLoader;

    @Override
    public PairwiseValueMap call() {
        log.debug(name);
        List<Peak> lhsPeaks = peakLoader.loadPeaks(new FileFragment(lhs));
        int lhsPeaksLength = lhsPeaks.size();
        List<Peak> rhsPeaks = peakLoader.loadPeaks(new FileFragment(rhs));
        int rhsPeaksLength = rhsPeaks.size();
        int elemCnt = 0;
        PairwiseValueMap pvm = new PairwiseValueMap(id, lhsPeaks.size(), rhsPeaks.size(), false, PairwiseValueMap.StorageType.valueOf(storageType), defaultValue);
        for (int i = 0; i < lhsPeaks.size(); i++) {
            final Peak p1 = lhsPeaks.get(i);
            for (int j = 0; j < rhsPeaks.size(); j++) {
                final Peak p2 = rhsPeaks.get(j);
                // skip peaks, which are too far apart
                final double rt1 = p1.getScanAcquisitionTime();
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
                    pvm.setValue(i, j, d);
                }
                elemCnt++;
            }
        }
        return pvm;
    }

    @Override
    public String toString() {
        return name;
    }

}
