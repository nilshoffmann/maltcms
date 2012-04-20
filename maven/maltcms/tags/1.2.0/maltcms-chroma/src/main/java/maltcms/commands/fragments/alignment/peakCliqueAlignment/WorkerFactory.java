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

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import maltcms.datastructures.peak.Peak;
import maltcms.math.functions.IScalarArraySimilarity;
import maltcms.math.functions.ProductSimilarity;
import maltcms.math.functions.similarities.ArrayCorr;
import maltcms.math.functions.similarities.GaussianDifferenceSimilarity;

/**
 *
 * @author nilshoffmann
 */
@Data
public class WorkerFactory {

    private double maxRTDifference = 60.0d;
    private IScalarArraySimilarity similarityFunction;
    private boolean assumeSymmetricSimilarity = false;

    public WorkerFactory() {
        similarityFunction = new ProductSimilarity();
        GaussianDifferenceSimilarity gds = new GaussianDifferenceSimilarity();
        similarityFunction.setScalarSimilarities(gds);
        ArrayCorr ac = new ArrayCorr();
        similarityFunction.setArraySimilarities(ac);
    }

    public List<PairwiseSimilarityWorker> create(TupleND<IFileFragment> input, Map<String, List<Peak>> fragmentToPeaks) {
        List<PairwiseSimilarityWorker> worker = new LinkedList<PairwiseSimilarityWorker>();
        if (assumeSymmetricSimilarity) {
            for (Tuple2D<IFileFragment, IFileFragment> t : input.getPairs()) {
                // calculate similarity between peaks
                final List<Peak> lhsPeaks = fragmentToPeaks.get(t.getFirst().getName());
                final List<Peak> rhsPeaks = fragmentToPeaks.get(t.getSecond().getName());
//                log.debug("Comparing {} and {}", t.getFirst().getName(),
//                        t.getSecond().getName());
                PairwiseSimilarityWorker psw = new PairwiseSimilarityWorker();
                psw.setMaxRTDifference(maxRTDifference);
                psw.setSimilarityFunction(similarityFunction);
                psw.setLhsPeaks(lhsPeaks);
                psw.setRhsPeaks(rhsPeaks);
                psw.setName("Calculating pairwise peak similarities between " + t.getFirst().getName() + " and " + t.getSecond().getName());
                worker.add(psw);
            }
        } else {
            for (IFileFragment f1 : input) {
                for (IFileFragment f2 : input) {
                    // calculate similarity between peaks
                    final List<Peak> lhsPeaks = fragmentToPeaks.get(f1.getName());
                    final List<Peak> rhsPeaks = fragmentToPeaks.get(f2.getName());
//                log.debug("Comparing {} and {}", t.getFirst().getName(),
//                        t.getSecond().getName());
                    PairwiseSimilarityWorker psw = new PairwiseSimilarityWorker();
                    psw.setMaxRTDifference(maxRTDifference);
                    psw.setSimilarityFunction(similarityFunction);
                    psw.setLhsPeaks(lhsPeaks);
                    psw.setRhsPeaks(rhsPeaks);
                    psw.setName("Calculating pairwise peak similarities between " + f1.getName() + " and " + f2.getName());
                    worker.add(psw);
                }
            }
        }

        return worker;
    }
}
