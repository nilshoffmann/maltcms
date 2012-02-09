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
