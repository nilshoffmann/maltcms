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
package maltcms.experimental.bipace.api;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import lombok.Data;
import maltcms.datastructures.peak.Peak;
import maltcms.experimental.bipace.datastructures.spi.PairwisePeakListSimilarities;
import maltcms.math.functions.IScalarArraySimilarity;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public abstract class PeakSimilarityCalculator<T extends Peak> implements
        Callable<PairwisePeakListSimilarities>,
        Serializable {

    private UUID uniqueId;
    private List<T> peakListA;
    private List<T> peakListB;
    private IScalarArraySimilarity similarityFunction;
    private boolean storeOnlyBestSimilarities = false;

    @Override
    public PairwisePeakListSimilarities call() throws Exception {
        DoubleMatrix2D sdm = null;
        if (storeOnlyBestSimilarities) {
            sdm = new SparseDoubleMatrix2D(peakListA.size(), peakListB.size());
        } else {
            sdm = new DenseDoubleMatrix2D(peakListA.size(), peakListB.size());
        }
        // all-against-all peak list comparison
        // l^{2} for l=max(|lhsPeaks|,|rhsPeaks|)
        for (int i = 0; i < peakListA.size(); i++) {
            T a = peakListA.get(i);
            for (int j = 0; j < peakListB.size(); j++) {
                T b = peakListB.get(j);
                final double d = calculateSimilarity(a, b);
                if (!Double.isNaN(d)) {
                    sdm.setQuick(i, j, d);
                    sdm.setQuick(j, i, d);
                }
            }
        }
        PairwisePeakListSimilarities ppls = new PairwisePeakListSimilarities();
        ppls.setProducerId(uniqueId);
        ppls.setResult(sdm);
        return ppls;
    }

    public abstract double calculateSimilarity(T p1, T p2);

    public abstract PeakSimilarityCalculator<T> copy();
}
