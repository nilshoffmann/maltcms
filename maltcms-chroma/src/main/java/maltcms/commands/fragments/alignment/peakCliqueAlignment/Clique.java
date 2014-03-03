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

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.LongObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.IPeak;
import maltcms.tools.ArrayTools;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerItem;

/**
 * @author Nils Hoffmann
 *
 *
 */
@Slf4j
public class Clique<T extends IBipacePeak> {

    private static long CLIQUEID = -1;
    private long id = -1;
    private double cliqueMean = 0, cliqueVar = 0;
    private IntObjectMap<T> clique = new IntObjectOpenHashMap<T>();
    private IBipacePeak centroid = null;
    private double minBbhFraction = 1.0d;
    private int maxBBHErrors = 0;
    private int bbhErrors = 0;
    private int bidiHits = 0;

    public Clique() {
        this.id = ++CLIQUEID;
    }

    public long getID() {
        return this.id;
    }

    public boolean addPeak(LongObjectMap<PeakEdge> edgeMap, T p, boolean force) {
        if (force) {
            if (clique.containsKey(p.getAssociationId())) {
                T q = clique.get(p.getAssociationId());
                if (p.equals(q)) {
                    return true;
                }
//				log.debug("Clique already contains peak from file: {}",
//						p.getAssociation());
                return false;
            }
            return handleForceAddPeak(edgeMap, p);
        } else {
            return addPeak(edgeMap, p);
        }
    }

    public int size() {
        return clique.size();
    }

    /**
     * Returns false, if a peak did not meet the criteria to be added to the
     * clique. Returns true, if a peak is either already contained in the
     * clique, or if it has been successfully added, due to satisfaction of all
     * required criteria.
     *
     * @param p
     * @return
     * @throws IllegalArgumentException
     */
    public boolean addPeak(LongObjectMap<PeakEdge> edgeMap, T p) throws IllegalArgumentException {
        return addPeak2(edgeMap, p);
    }

    public boolean addPeak2(LongObjectMap<PeakEdge> edgeMap, T p) throws IllegalArgumentException {
        if (clique.containsKey(p.getAssociationId())) {
            T q = clique.get(p.getAssociationId());
            if (p.equals(q)) {
                return true;
            }
//			log.debug("Clique already contains peak from file: {}",
//					p.getAssociation());
            return handleConflictingPeak(edgeMap, p);
        }
        return handleNonConflictingPeak(edgeMap, p);
    }

    /**
     * @param p
     * @return
     */
    private boolean handleConflictingPeak(LongObjectMap<PeakEdge> edgeMap, T p) {
        Collection<T> currentPeaks = new ArrayList<T>();
        T[] t = clique.values().toArray(IBipacePeak.class);
        currentPeaks.addAll(Arrays.asList(t));
        T q = clique.get(p.getAssociationId());
        currentPeaks.remove(q);
        // calculate bbh scores for both peaks
        int bbh1 = getBBHCount(edgeMap, q, currentPeaks);
        int bbh2 = getBBHCount(edgeMap, p, currentPeaks);
        // use the peak, with the higher bbh count
        if (bbh1 > bbh2) {
            // do nothing, but return false,
            // since we did not add p
            return false;
        } else if (bbh1 < bbh2) {
//			log.debug("Replacing peak with better bbh candidate: " + p);
            // return true, since we add p
            removePeak(edgeMap, q);
            // call addPeak, since we removed q, this
            // should be okay
            return addPeak2(edgeMap, p);
        } else {
            if (bbh1 == 0 && bbh2 == 0) {
//				log.debug("Neither peak has a bbh, retaining original peak!");
                //do nothing, the original peak was okay already
                return false;
            }
//			log.debug("BBH count draw between peaks p:{}, q:{} with value: {}",
//					new Object[]{p, q, bbh1});
            // if we have a draw, we need to consider the
            // distance to the center
            double p1 = Math.abs(getCliqueRTMean() - p.getScanAcquisitionTime());
            double q2 = Math.abs(getCliqueRTMean() - q.getScanAcquisitionTime());
            //conflict resolution: nearest rt neighbor to clique RT mean wins
            if (p1 < q2) {
//				log.debug("Selecting peak p: {}", p);
                removePeak(edgeMap, q);
                return addPeak2(edgeMap, p);
            } else if (p1 > q2) {
//				log.debug("Retaining peak q: {}", q);
                return false;
            } else {
                log.warn("Draw resolution failed, retaining peak q: {}");
            }
        }
        return false;
    }

    /**
     * @param p
     * @return
     */
    private boolean handleNonConflictingPeak(LongObjectMap<PeakEdge> edgeMap, T p) {
        if (clique.containsKey(p.getAssociationId()) && clique.get(p.getAssociationId()).equals(p)) {
//			log.debug("Peak {} already contained in clique!", p);
            return false;
        } else if (clique.isEmpty()) {
            update(p);
            clique.put(p.getAssociationId(), p);
            selectCentroid();
            return true;
        } else {
            int actualBidiHits = getBBHCount(edgeMap, p);
            int diff = clique.size() - actualBidiHits;
            double fraction = (double) actualBidiHits / (double) clique.size();
//			log.debug("Clique bbh fraction for peak: " + fraction);
            if (((fraction) < minBbhFraction)) {
//				log.error("Rejected: below minBbhFraction!");
                return false;
            }
//			if (((bbhErrors + diff) > maxBBHErrors)) {
//				return false;
//			}
            bbhErrors += diff;
//			log.debug(
//					"Adding peak {} with {}/{} bbh hit(s) to clique",
//					new Object[]{p.getAssociation() + "@"
//				+ p.getScanAcquisitionTime(), actualBidiHits, clique.size()});
            update(p);
            clique.put(p.getAssociationId(), p);
            selectCentroid();
            return true;
        }
    }

    private boolean handleForceAddPeak(LongObjectMap<PeakEdge> edgeMap, T p) {
        if (clique.containsKey(p.getAssociationId()) && clique.get(p.getAssociationId()).equals(p)) {
//			log.debug("Peak {} already contained in clique!", p);
            return false;
        } else if (clique.isEmpty()) {
            update(p);
            clique.put(p.getAssociationId(), p);
            selectCentroid();
            return true;
        } else {
            int actualBidiHits = getBBHCount(edgeMap, p);
            int diff = clique.size() - actualBidiHits;
            bbhErrors += diff;
//			log.debug(
//					"Adding peak {} with {}/{} bbh hit(s) to clique",
//					new Object[]{p.getAssociation() + "@"
//				+ p.getScanAcquisitionTime(), actualBidiHits, clique.size()});
            update(p);
            clique.put(p.getAssociationId(), p);
            selectCentroid();
            return true;
        }
    }

    /**
     * @param p
     * @return
     */
    private int getBBHCount(LongObjectMap<PeakEdge> edgeMap, T p) {
//		return getBBHCount(edgeMap, p, clique);
        int bidiHits = 0;
        // check and count bidi best hit
        for (IntObjectCursor<T> t : clique) {
            if (!p.isBidiBestHitFor(edgeMap, t.value)) {
//				log.debug(
//						"Peak q: {} in clique is not a bidirectional best hit for peak p: {}",
//						q, p);
            } else {
                bidiHits++;
            }
        }
        return bidiHits;
    }

    private int getBBHCount(LongObjectMap<PeakEdge> edgeMap, T p, Collection<T> c) {
        int bidiHits = 0;
        // check and count bidi best hit
        for (T t : c) {
            if (!p.isBidiBestHitFor(edgeMap, t)) {
//				log.debug(
//						"Peak q: {} in clique is not a bidirectional best hit for peak p: {}",
//						q, p);
            } else {
                bidiHits++;
            }
        }
        return bidiHits;
    }

    public boolean removePeak(LongObjectMap<PeakEdge> edgeMap, T p) {
        if (clique.containsKey(p.getAssociationId())) {
            if (clique.get(p.getAssociationId()).equals(p)) {
                clique.remove(p.getAssociationId());
                if (clique.isEmpty()) {
                    clear();
                    return true;
                }
                int actualBidiHits = getBBHCount(edgeMap, p);
                int diff = clique.size() - actualBidiHits;
                bbhErrors -= diff;
                updateRemoval(p);
                selectCentroid();
                return true;
            }
        }
        return false;
    }

    public void clear() {
        cliqueMean = 0;
        cliqueVar = 0;
        centroid = null;
        clique.clear();
        bidiHits = 0;
    }

    public int getBBHs() {
        return this.bidiHits;
    }

    public int getExpectedBBHs() {
        return getExpectedBBHs(this.clique.size());
    }

    public int getExpectedBBHs(int groupSize) {
        return (int) Math.ceil((groupSize * (groupSize - 1)) / 2.0d);
    }

    public BoxAndWhiskerItem createRTBoxAndWhisker() {
        List<Double> l = new ArrayList<Double>();
        for (int f : this.clique.keys().toArray()) {
            l.add(centroid.getScanAcquisitionTime()
                - this.clique.get(f).getScanAcquisitionTime());
        }
        return BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(l);
    }

    public BoxAndWhiskerItem createApexTicBoxAndWhisker() {
        List<Double> l = new ArrayList<Double>();
        for (int f : this.clique.keys().toArray()) {
            l.add(Math.log(ArrayTools.integrate(centroid.getMsIntensities()))
                - ArrayTools.integrate(this.clique.get(f).getMsIntensities()));
        }
        return BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(l);
    }

    private void selectCentroid() {
        double mindist = Double.POSITIVE_INFINITY;
        double[] dists = new double[clique.size()];
        int i = 0;
        IBipacePeak[] peaks = clique.values().toArray(IBipacePeak.class);
        for (IBipacePeak peak : peaks) {
            for (IBipacePeak peak1 : peaks) {
                dists[i] += Math.pow(
                    peak.getScanAcquisitionTime()
                    - peak1.getScanAcquisitionTime(), 2.0d);
            }
            i++;
        }
        int mindistIdx = 0;
        for (int j = 0; j < dists.length; j++) {
            if (dists[j] < mindist) {
                mindist = dists[j];
                mindistIdx = j;
            }
        }
        this.log.debug("Clique centroid is {}", peaks[mindistIdx]);
        this.centroid = peaks[mindistIdx];
    }

    public double getRTDistanceToCentroid(IPeak p) {
        double mean = getCliqueRTMean();
        return Math.pow(mean - p.getScanAcquisitionTime(), 2);
    }

    public double getRatioOfRTDistanceToCentroidAndCliqueVariance(IPeak p) {
        double d = getRTDistanceToCentroid(p);
        return d / getCliqueRTVariance();
    }

    private void update(IPeak p) {
        int n = 0;
        double mean = cliqueMean;
        double var = cliqueVar;
//		log.debug(
//				"Clique variance before adding peak: {}, clique mean before: {}",
//				var, mean);
        double delta = 0;
        double rt = p.getScanAcquisitionTime();
        n = clique.size() + 1;
        delta = rt - mean;
        if (n > 0) {
            mean = mean + delta / n;
        }
        if (n > 2) {
            var = (var + (delta * (rt - mean))) / ((double) (n - 2));
        }
        cliqueMean = mean;
        cliqueVar = var;
//		log.debug(
//				"Clique variance after adding peak: {}, clique mean before: {}",
//				var, mean);
    }

    private void updateRemoval(IPeak p) {
        int n = 0;
        double mean = cliqueMean;
        double var = cliqueVar;
//		log.debug(
//				"Clique variance before removing peak: {}, clique mean before: {}",
//				var, mean);
        double delta = 0;
        double rt = p.getScanAcquisitionTime();
        n = clique.size() - 1;
        delta = rt - mean;
        if (n > 0) {
            mean = mean - delta / n;
        }
        if (n > 2) {
            var = (var - (delta * (rt - mean))) / ((double) (n - 2));
        }
        cliqueMean = mean;
        cliqueVar = var;
//		log.debug(
//				"Clique variance after removing peak: {}, clique mean before: {}",
//				var, mean);
    }

    public double getCliqueRTVariance() {
        return this.cliqueVar;
    }

    public double getCliqueRTMean() {
        return this.cliqueMean;
    }

    public IPeak getCliqueCentroid() {
        return this.centroid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.centroid != null) {
            sb.append("Center: ").append(this.centroid.toString()).append("\n");
        } else {
            sb.append("Center: null\n");
        }
        sb.append("\tMean: ").append(this.cliqueMean).append("\n");
        sb.append("\tVariance: ").append(this.cliqueVar).append("\n");
        for (int f : this.clique.keys().toArray()) {
            if (this.clique.get(f) != null) {
                sb.append(this.clique.get(f).toString());
            } else {
                sb.append("null");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public List<T> getPeakList() {
        List<T> peaks = Arrays.asList(this.clique.values().toArray(IBipacePeak.class));
        Collections.sort(peaks, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.getAssociation().compareTo(o2.getAssociation());
            }
        });
        return peaks;
    }

    public double getSimilarityForPeaks(int a, int b, LongObjectMap<PeakEdge> peakEdgeMap) {
        return this.clique.get(a).getSimilarity(peakEdgeMap, this.clique.get(b));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Clique other = (Clique) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public double getCliqueMean() {
        return cliqueMean;
    }

    public double getCliqueVar() {
        return cliqueVar;
    }

    public int getMaxBBHErrors() {
        return maxBBHErrors;
    }

    public void setMaxBBHErrors(int maxBBHErrors) {
        this.maxBBHErrors = maxBBHErrors;
    }

    public void setMinBbhFraction(double fraction) {
        if (fraction <= 0.0d || fraction > 1.0d) {
            throw new IllegalArgumentException("Value of minBbhFraction must be in the left open interval (0,1] (zero exclusive, one inclusive). Was: " + fraction);
        }
        this.minBbhFraction = fraction;
    }

    public double getMinBbhFraction() {
        return this.minBbhFraction;
    }
}
