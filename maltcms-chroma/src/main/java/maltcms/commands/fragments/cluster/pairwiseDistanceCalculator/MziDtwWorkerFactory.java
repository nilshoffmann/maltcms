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
package maltcms.commands.fragments.cluster.pairwiseDistanceCalculator;

import lombok.Data;
import maltcms.commands.distances.DtwRecurrence;
import maltcms.commands.distances.IDtwSimilarityFunction;
import maltcms.commands.distances.PairwiseFeatureSimilarity;
import maltcms.commands.distances.dtw.MZIDynamicTimeWarp;
import maltcms.math.functions.DtwTimePenalizedPairwiseSimilarity;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Nils Hoffmann
 */
@Data
@ServiceProvider(service = AWorkerFactory.class)
public class MziDtwWorkerFactory extends AWorkerFactory {

    /**
     *
     */
    private static final long serialVersionUID = 626200987946759092L;
    private double bandWidthPercentage = 0.25;
    private double globalGapPenalty = 1.0;
    private String extension = "";
    private boolean globalBand = false;
    private int minScansBetweenAnchors = 10;
    private boolean precalculatePairwiseDistances = false;
    private boolean saveLayoutImage = false;
    private boolean useAnchors = false;
    private int numberOfEICsToSelect = 0;
    private boolean useSparseArrays = false;
    private int anchorRadius = 0;
    private IDtwSimilarityFunction similarity = new DtwTimePenalizedPairwiseSimilarity();
    private boolean saveDtwMatrix = false;
    private boolean savePairwiseSimilarityMatrix = false;
    private boolean normalizeAlignmentValue = false;

    /**
     *
     * @return
     */
    @Override
    public PairwiseDistanceWorker create() {
        PairwiseDistanceWorker worker = new PairwiseDistanceWorker();
        MZIDynamicTimeWarp mdtw = new MZIDynamicTimeWarp();
        mdtw.setNumberOfEICsToSelect(numberOfEICsToSelect);
        mdtw.setUseSparseArrays(useSparseArrays);
        mdtw.setAnchorRadius(anchorRadius);
        mdtw.setBandWidthPercentage(bandWidthPercentage);
        mdtw.setExtension(extension);
        mdtw.setGlobalBand(globalBand);
        mdtw.setMinScansBetweenAnchors(minScansBetweenAnchors);
        mdtw.setPrecalculatePairwiseDistances(
                precalculatePairwiseDistances);
        mdtw.setSaveLayoutImage(saveLayoutImage);
        mdtw.setUseAnchors(useAnchors);
        mdtw.setSaveDtwMatrix(saveDtwMatrix);
        mdtw.setSavePairwiseSimilarityMatrix(savePairwiseSimilarityMatrix);
        mdtw.setNormalizeAlignmentValue(normalizeAlignmentValue);
        //setup pairwise comparison function
        PairwiseFeatureSimilarity pfs = new PairwiseFeatureSimilarity();
        pfs.setSimilarityFunction(similarity);
        mdtw.setPairwiseFeatureSimilarity(pfs);
        //setup recurrence function
        DtwRecurrence dtwr = new DtwRecurrence();
        dtwr.setGlobalGapPenalty(globalGapPenalty);
        dtwr.set(similarity.getCompressionWeight(), similarity.getExpansionWeight(), similarity.getMatchWeight());
        dtwr.setMinimize(false);
        mdtw.setRecurrence(dtwr);
        worker.setSimilarity(mdtw);
        return worker;
    }
}
