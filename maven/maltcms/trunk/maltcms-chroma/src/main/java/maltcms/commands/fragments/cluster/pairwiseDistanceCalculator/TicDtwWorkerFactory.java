/*
 * $license$
 *
 * $Id$
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
        dtwr.set(similarity.getMatchWeight(), similarity.getCompressionWeight(), similarity.getExpansionWeight());
        dtwr.setMinimize(false);
        mdtw.setRecurrence(dtwr);
        worker.setSimilarity(mdtw);
        return worker;
    }
}
