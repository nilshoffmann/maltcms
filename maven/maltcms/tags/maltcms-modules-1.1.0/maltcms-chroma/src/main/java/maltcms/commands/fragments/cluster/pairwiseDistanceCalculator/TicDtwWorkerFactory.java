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
package maltcms.commands.fragments.cluster.pairwiseDistanceCalculator;

import org.openide.util.lookup.ServiceProvider;

import maltcms.commands.distances.dtw.TICDynamicTimeWarp;
import maltcms.commands.distances.dtw.MZIDynamicTimeWarp;
import lombok.Data;
import maltcms.commands.distances.DtwRecurrence;
import maltcms.commands.distances.IDtwSimilarityFunction;
import maltcms.commands.distances.PairwiseFeatureSimilarity;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */

@Data
@ServiceProvider(service=AWorkerFactory.class)
public class TicDtwWorkerFactory extends AWorkerFactory {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8359038356253166675L;
	
    private double bandWidthPercentage;
    private double globalGapPenalty;
    private String extension;
    private boolean globalBand;
    private int minScansBetweenAnchors;
    private boolean precalculatePairwiseDistances;
    private boolean saveLayoutImage;
    private boolean useAnchors;
    private int anchorRadius;
    private IDtwSimilarityFunction similarity;

    @Override
    public PairwiseDistanceWorker create() {
        PairwiseDistanceWorker worker = new PairwiseDistanceWorker();
        TICDynamicTimeWarp mdtw = new TICDynamicTimeWarp();
        mdtw.setAnchorRadius(anchorRadius);
        mdtw.setBandWidthPercentage(bandWidthPercentage);
        mdtw.setExtension(extension);
        mdtw.setGlobalBand(globalBand);
        mdtw.setMinScansBetweenAnchors(minScansBetweenAnchors);
        mdtw.setPrecalculatePairwiseDistances(
                precalculatePairwiseDistances);
        mdtw.setSaveLayoutImage(saveLayoutImage);
        mdtw.setUseAnchors(useAnchors);
        //setup pairwise comparison function
        PairwiseFeatureSimilarity pfs = new PairwiseFeatureSimilarity();
        pfs.setSimilarityFunction(similarity);
        mdtw.setPairwiseFeatureSimilarity(pfs);
        //setup recurrence function
        DtwRecurrence dtwr = new DtwRecurrence();
        dtwr.setGlobalGapPenalty(globalGapPenalty);
        dtwr.set(similarity.getCompressionWeight(), similarity.getExpansionWeight(),similarity.getMatchWeight());
        dtwr.setMinimize(false);
        mdtw.setRecurrence(dtwr);
        worker.setSimilarity(mdtw);
        return worker;
    }
}
