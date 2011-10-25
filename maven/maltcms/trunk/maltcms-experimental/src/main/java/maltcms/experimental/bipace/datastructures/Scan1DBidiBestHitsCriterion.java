/**
 * 
 */
package maltcms.experimental.bipace.datastructures;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import maltcms.datastructures.array.IArrayD2Double;
import maltcms.datastructures.peak.Peak1D;
import cross.exception.NotImplementedException;
import maltcms.datastructures.ms.IScan1D;
import maltcms.experimental.bipace.datastructures.api.IClique;
import maltcms.experimental.bipace.datastructures.api.ICliqueMemberCriterion;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * @param <T>
 * 
 * 
 */
public class Scan1DBidiBestHitsCriterion implements
        ICliqueMemberCriterion<IScan1D> {

    public class Peak1DSimilarities {

        private final IArrayD2Double sims;
        private final List<IScan1D> peaks;

        public Peak1DSimilarities(List<IScan1D> peaks, IArrayD2Double sims) {
            this.peaks = peaks;
            this.sims = sims;
            if (sims.rows() != peaks.size() || sims.columns() != peaks.size()) {
                throw new IllegalArgumentException(
                        "Number of peaks and dimensions of similarity matrix differ! Check for transposition!");
            }
        }

        public int getBestHitForLhs(int i) {
            // i means for peaks in rows -> l1
            double maxSim = Double.NEGATIVE_INFINITY;
            double val = 0;
            int maxSimIndex = -1;
            for (int j = 0; j < sims.columns(); j++) {
                val = sims.get(i, j);
                if (val > maxSim) {
                    maxSim = val;
                    maxSimIndex = j;
                }
            }
            return maxSimIndex;
        }

        public int getBestHitForRhs(int j) {
            // j means for peaks in columns -> l2
            double maxSim = Double.NEGATIVE_INFINITY;
            double val = 0;
            int maxSimIndex = -1;
            for (int i = 0; i < sims.rows(); i++) {
                val = sims.get(i, j);
                if (val > maxSim) {
                    maxSim = val;
                    maxSimIndex = i;
                }
            }
            return maxSimIndex;
        }

        public HashMap<Integer, Integer> getBestHitsForLhs() {
            HashMap<Integer, Integer> bestHitsLhs = new HashMap<Integer, Integer>();
            for (int i = 0; i < sims.rows(); i++) {
                bestHitsLhs.put(i, getBestHitForLhs(i));
            }
            return bestHitsLhs;
        }

        public HashMap<Integer, Integer> getBestHitsForRhs() {
            HashMap<Integer, Integer> bestHitsRhs = new HashMap<Integer, Integer>();
            for (int j = 0; j < sims.columns(); j++) {
                bestHitsRhs.put(j, getBestHitForRhs(j));
            }
            return bestHitsRhs;
        }

        public boolean isBidirectionalBestHit(int lhs, int rhs) {
            int bestHitForLhs = getBestHitForLhs(lhs);
            int bestHitForRhs = getBestHitForRhs(rhs);
            if (bestHitForLhs == rhs && bestHitForRhs == lhs) {
                return true;
            }
            return false;
        }

        public Set<Point> getBidirectionalBestHits() {

            // SortedSet<Point> set = new TreeSet<Point>();
            // for (int i = 0; i < lhs.size(); i++) {
            // for (int j = 0; j < rhs.size(); j++) {
            // if (isBidirectionalBestHit(i, j)) {
            // set.add(new Point(i, j));
            // }
            // }
            // }
            // return set;
            throw new NotImplementedException();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * maltcms.datastructures.cluster.ICliqueMemberCriterion#shouldBeMemberOf
     * (maltcms.datastructures.cluster.Clique,
     * maltcms.datastructures.array.IFeatureVector)
     */
    @Override
    public boolean shouldBeMemberOf(IClique<IScan1D> c, IScan1D t) {
        // test t against all members of c
        // if all are bidibesthits, t should be a member
        for (IScan1D p : c.getFeatureVectorList()) {
            // if (!areBidiBestHits(p, t)) {
            // // otherwise not
            // return false;
            // }
        }
        return false;
    }
    // public boolean areBidiBestHits(Peak1D p1, Peak1D p2) {
    // Peak bestHitP1 = getPeakWithHighestSimilarity(p1, p2);
    // }
    //
    // public Peak1D getPeakWithHighestSimilarity(final Peak1D lhs,
    // final Peak1D rhs) {
    // final List<Peak> l = getPeaksSortedBySimilarity(lhs.getFile(), rhs
    // .getFile());
    // if (l.isEmpty()) {
    // return null;
    // }
    // return l.get(l.size() - 1);
    // }
    //
    // public List<Peak> getPeaksSortedBySimilarity(Peak1D lhs, Peak1D rhs) {
    // String fileLhs = lhs.getFile();
    // String fileRhs = rhs.getFile();
    // }
}
