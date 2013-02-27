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
package maltcms.datastructures.peak;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

import maltcms.tools.ArrayTools;

import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerItem;

/**
 * @author Nils Hoffmann
 *
 *
 */
@Slf4j
public class Clique {

    private static long CLIQUEID = -1;
    private long id = -1;
    private double cliqueMean = 0, cliqueVar = 0;
    private Map<String, Peak> clique = new ConcurrentHashMap<String, Peak>();
    private Peak centroid = null;
    private int maxBBHErrors = 0;
    private int bbhErrors = 0;
    private int bidiHits = 0;

    public Clique() {
        this.id = ++CLIQUEID;
    }

    public long getID() {
        return this.id;
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
    public boolean addPeak(Peak p) throws IllegalArgumentException {
        // if (clique.contains(p)) {
        // log.debug("Peak {} already contained in clique!", p);
        // return false;
        // } else {
        // // if (clique.isEmpty()) {
        //
        // // check bidi best hit assumption
        // // bail out if assumption fails!
        // for (Peak q : getPeakList()) {
        // if (!p.isBidiBestHitFor(q)) {
        // log.debug(
        // "Peak q: {} in clique is not a bidirectional best hit for peak p: {}",
        // q, p);
        // return false;
        // }
        // }
        // log.debug("Adding peak {} to clique", p);
        // update(p);
        // clique.add(p);
        // selectCentroid();
        // return true;
        // }
        return addPeak2(p);
    }

    public boolean addPeak2(Peak p) throws IllegalArgumentException {
        if (clique.containsKey(p.getAssociation())) {
            Peak q = clique.get(p.getAssociation());
            if (p.equals(q)) {
                return true;
            }
            log.debug("Clique already contains peak from file: {}",
                    p.getAssociation());
            return handleConflictingPeak(p);
        }
        return handleNonConflictingPeak(p);
    }

    /**
     * @param p
     * @return
     */
    private boolean handleConflictingPeak(Peak p) {
        Collection<Peak> currentPeaks = clique.values();
        Peak q = clique.get(p.getAssociation());
        currentPeaks.remove(q);
        // calculate bbh scores for both peaks
        int bbh1 = getBBHCount(q, currentPeaks);
        int bbh2 = getBBHCount(p, currentPeaks);
        // use the peak, with the higher bbh count
        if (bbh1 > bbh2) {
            // do nothing, but return false,
            // since we did not add p
            return false;
        } else if (bbh1 < bbh2) {
            // return true, since we add p
            removePeak(q);
            // call addPeak, since we removed q, this
            // should be okay
            return addPeak2(p);
        } else {
            log.debug("BBH count draw between peaks p:{}, q:{} with value: {}",
                    new Object[]{p, q, bbh1});
            // if we have a draw, we need to consider the
            // distance to the center
            double p1 = Math.abs(getCliqueRTMean() - p.getScanAcquisitionTime());
            double q2 = Math.abs(getCliqueRTMean() - q.getScanAcquisitionTime());
            //conflict resolution: nearest rt neighbor to clique RT mean wins
            if (p1 < q2) {
                removePeak(q);
                return addPeak2(p);
            } else if (p1 > q2) {
                return false;
            } else {
                log.warn("Draw resolution failed!");
            }
        }
        return false;
    }

    /**
     * @param p
     * @return
     */
    private boolean handleNonConflictingPeak(Peak p) {
        if (clique.containsValue(p)) {
            log.debug("Peak {} already contained in clique!", p);
            return false;
        } else if (clique.isEmpty()) {
            update(p);
            clique.put(p.getAssociation(), p);
            selectCentroid();
            return true;
        } else {
            int actualBidiHits = getBBHCount(p);
            int diff = clique.size() - actualBidiHits;
            if (((bbhErrors + diff) > maxBBHErrors)) {
                return false;
            }
            bbhErrors += diff;
            log.debug(
                    "Adding peak {} with {}/{} bbh hit(s) to clique",
                    new Object[]{p.getAssociation() + "@"
                        + p.getScanAcquisitionTime(), actualBidiHits, clique.size()});
            update(p);
            clique.put(p.getAssociation(), p);
            selectCentroid();
            return true;
        }
    }

    /**
     * @param p
     * @return
     */
    private int getBBHCount(Peak p) {
        return getBBHCount(p, getPeakList());
    }

    private int getBBHCount(Peak p, Collection<Peak> c) {
        int bidiHits = 0;
        // check and count bidi best hit
        for (Peak q : getPeakList()) {
            if (!p.isBidiBestHitFor(q)) {
                log.debug(
                        "Peak q: {} in clique is not a bidirectional best hit for peak p: {}",
                        q, p);
            } else {
                bidiHits++;
            }
        }
        return bidiHits;
    }

    public boolean removePeak(Peak p) {
        if (clique.containsValue(p)) {
            clique.remove(p.getAssociation());
            if (clique.isEmpty()) {
                clear();
                return true;
            }
            int actualBidiHits = getBBHCount(p);
            int diff = clique.size() - actualBidiHits;
            bbhErrors -= diff;
            updateRemoval(p);
            selectCentroid();
            return true;
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
        for (String f : this.clique.keySet()) {
            l.add(centroid.getScanAcquisitionTime()
                    - this.clique.get(f).getScanAcquisitionTime());
        }
        return BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(l);
    }

    public BoxAndWhiskerItem createApexTicBoxAndWhisker() {
        List<Double> l = new ArrayList<Double>();
        for (String f : this.clique.keySet()) {
            l.add(Math.log(ArrayTools.integrate(centroid.getMsIntensities()))
                    - ArrayTools.integrate(this.clique.get(f).getMsIntensities()));
        }
        return BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(l);
    }

    private void selectCentroid() {
        double mindist = Double.POSITIVE_INFINITY;
        double[] dists = new double[clique.size()];
        int i = 0;
        Peak[] peaks = clique.values().toArray(new Peak[]{});
        for (Peak peak : peaks) {
            for (Peak peak1 : peaks) {
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

    public double getDistanceToCentroid(Peak p) {
        double mean = getCliqueRTMean();
        return Math.pow(mean - p.getScanAcquisitionTime(), 2);
    }

    public double getStdDevOfDistanceToCentroid(Peak p) {
        double d = getDistanceToCentroid(p);
        return d / Math.sqrt(getCliqueRTVariance());
    }

    private void update(Peak p) {
        int n = 0;
        double mean = cliqueMean;
        double var = cliqueVar;
        log.debug(
                "Clique variance before adding peak: {}, clique mean before: {}",
                var, mean);
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
        log.debug(
                "Clique variance after adding peak: {}, clique mean before: {}",
                var, mean);
    }

    private void updateRemoval(Peak p) {
        int n = 0;
        double mean = cliqueMean;
        double var = cliqueVar;
        log.debug(
                "Clique variance before removing peak: {}, clique mean before: {}",
                var, mean);
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
        log.debug(
                "Clique variance after removing peak: {}, clique mean before: {}",
                var, mean);
    }

    public double getCliqueRTVariance() {
        return this.cliqueVar;
    }

    public double getCliqueRTMean() {
        return this.cliqueMean;
    }

    public Peak getCliqueCentroid() {
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
        for (String f : this.clique.keySet()) {
            if (this.clique.get(f) != null) {
                sb.append(this.clique.get(f).toString());
            } else {
                sb.append("null");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public List<Peak> getPeakList() {
        List<Peak> peaks = new ArrayList<Peak>(this.clique.values());
        Collections.sort(peaks, new Comparator<Peak>() {
            @Override
            public int compare(Peak o1, Peak o2) {
                return o1.getAssociation().compareTo(o2.getAssociation());
            }
        });
        return peaks;
    }

    public double getSimilarityForPeaks(String a, String b) {
        return this.clique.get(a).getSimilarity(this.clique.get(b));
    }

    @Override
    public int hashCode() {
        return super.hashCode();
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
        if (this.clique != other.clique && (this.clique == null || !this.clique.equals(other.clique))) {
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
}
