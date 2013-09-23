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

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import lombok.Data;
import maltcms.datastructures.peak.IBipacePeak;
import maltcms.datastructures.peak.IPeak;
import maltcms.math.functions.IScalarArraySimilarity;
import maltcms.math.functions.ProductSimilarity;
import maltcms.math.functions.similarities.ArrayCorr;
import maltcms.math.functions.similarities.GaussianDifferenceSimilarity;

/**
 *
 * @author nilshoffmann
 */
@Data
public class WorkerFactory implements IWorkerFactory {

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

    @Override
    public List<Callable<BBHPeakEdgeSet>> create(TupleND<IFileFragment> input, Map<String, List<IBipacePeak>> fragmentToPeaks) {
        List<Callable<BBHPeakEdgeSet>> worker = new LinkedList<Callable<BBHPeakEdgeSet>>();
        if (assumeSymmetricSimilarity) {
            for (Tuple2D<IFileFragment, IFileFragment> t : input.getPairs()) {
                // calculate similarity between peaks
                final List<IBipacePeak> lhsPeaks = fragmentToPeaks.get(t.getFirst().getName());
                final List<IBipacePeak> rhsPeaks = fragmentToPeaks.get(t.getSecond().getName());
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
                    final List<IBipacePeak> lhsPeaks = fragmentToPeaks.get(f1.getName());
                    final List<IBipacePeak> rhsPeaks = fragmentToPeaks.get(f2.getName());
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