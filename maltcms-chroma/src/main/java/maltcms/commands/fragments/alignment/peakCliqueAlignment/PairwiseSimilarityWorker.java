/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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

import com.carrotsearch.hppc.LongObjectOpenHashMap;
import com.carrotsearch.hppc.cursors.LongObjectCursor;
import cross.datastructures.tools.EvalTools;
import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import maltcms.math.functions.IScalarArraySimilarity;

/**
 *
 * @author Nils Hoffmann
 */
@Value
@Slf4j
public final class PairwiseSimilarityWorker implements Callable<PairwiseSimilarityResult>, Serializable {

    private final String name;
    private final String lhsName;
    private final String rhsName;
    private final List<? extends IBipacePeak> lhsPeaks;
    private final List<? extends IBipacePeak> rhsPeaks;
    private final IScalarArraySimilarity similarityFunction;
    private final boolean savePeakSimilarities;
    private final File outputDirectory;
    private final double maxRTDifference;

    @Override
    public PairwiseSimilarityResult call() {
        log.debug(name);
        EvalTools.notNull(lhsPeaks, this);
        EvalTools.notNull(rhsPeaks, this);
        IScalarArraySimilarity sim = similarityFunction.copy();
        LongObjectOpenHashMap<PeakEdge> edgeMap = new LongObjectOpenHashMap<>();
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
                    final double d = sim.apply(new double[]{rt1}, new double[]{rt2}, p1.getMsIntensities(), p2.getMsIntensities());
                    p1.addSimilarity(edgeMap, p2, d);
                    p2.addSimilarity(edgeMap, p1, d);
                }
            }
        }
        if (savePeakSimilarities) {
            PeakSimilarityVisualizer psv = new PeakSimilarityVisualizer();
            psv.visualizePairwisePeakSimilarities(outputDirectory, edgeMap, lhsName, lhsPeaks, rhsName, rhsPeaks, 256, "beforeBIDI", false);
        }
        BBHFinder bbhfinder = new BBHFinder();
        BBHPeakList bbhpr = bbhfinder.findBiDiBestHits(edgeMap, lhsPeaks, rhsPeaks);
        long[] keys = new long[edgeMap.size()];
        PeakEdge[] values = new PeakEdge[edgeMap.size()];
        int i = 0;
        Iterator<LongObjectCursor<PeakEdge>> iter = edgeMap.iterator();
        while (iter.hasNext()) {
            LongObjectCursor<PeakEdge> l = iter.next();
            keys[i] = l.key;
            values[i] = l.value;
            i++;
        }
        if (savePeakSimilarities) {
            PeakSimilarityVisualizer psv = new PeakSimilarityVisualizer();
            psv.visualizePairwisePeakSimilarities(outputDirectory, edgeMap, lhsName, lhsPeaks, rhsName, rhsPeaks, 256, "afterBIDI", false);
        }
        PairwiseSimilarityResult result = new PairwiseSimilarityResult(bbhpr, keys, values);
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
