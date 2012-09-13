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
package maltcms.experimental.bipace.datastructures.api;

import maltcms.experimental.bipace.datastructures.api.CliqueStatistics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import cross.datastructures.fragments.IFileFragment;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.Peak;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
@Slf4j
public class Clique<T extends Peak> {

    private static long CLIQUEID = -1;
    private long id = -1;
//    private double cliqueMean = 0, cliqueVar = 0;
    private HashMap<String, T> clique = new HashMap<String, T>();
    private int maxBBHErrors = 0;
    private int bbhErrors = 0;
    private int bidiHits = 0;
    private CliqueStatistics<T> cliqueStatistics;

    public Clique() {
        this.id = ++CLIQUEID;
    }

    public CliqueStatistics<T> getCliqueStatistics() {
        return cliqueStatistics;
    }

    public void setCliqueStatistics(CliqueStatistics<T> cliqueStatistics) {
        this.cliqueStatistics = cliqueStatistics;
    }

    public long getID() {
        return this.id;
    }

    public int size() {
        return clique.size();
    }

    public Set<String> keySet() {
        return clique.keySet();
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
    public boolean addPeak(T p) throws IllegalArgumentException {
        if (clique.containsKey(p.getAssociation())) {
            T q = clique.get(p.getAssociation());
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
    private boolean handleConflictingPeak(T p) {
        Collection<T> currentPeaks = clique.values();
        T q = clique.get(p.getAssociation());
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
            return addPeak(p);
        } else {
            log.info("BBH count draw between peaks p:{}, q:{} with value: {}",
                    new Object[]{p, q, bbh1});
            // if we have a draw, we need to consider the
            // distance to the center
            int result = cliqueStatistics.compareDraw(p, q);
            //conflict resolution: nearest rt neighbor to clique RT mean wins
            if (result < 0) {
                removePeak(q);
                return addPeak(p);
            } else if (result > 0) {
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
    private boolean handleNonConflictingPeak(T p) {
        if (clique.containsValue(p)) {
            log.debug("Peak {} already contained in clique!", p);
            return false;
        } else if (clique.isEmpty()) {
            clique.put(p.getAssociation(), p);
            update();
            return true;
        } else {
            // if (clique.isEmpty()) {
            int actualBidiHits = getBBHCount(p);
            int diff = clique.size() - actualBidiHits;
            if (((bbhErrors + diff) > maxBBHErrors)) {
                return false;
            }
            bbhErrors += diff;
            log.debug(
                    "Adding peak {} with {}/{} bbh hit(s) to clique",
                    new Object[]{p.getAssociation() + "@"
                        + p.getScanAcquisitionTime(), actualBidiHits, clique.
                        size()});
            clique.put(p.getAssociation(), p);
            update();
            return true;
        }
    }

    /**
     * @param p
     * @return
     */
    private int getBBHCount(T p) {
        return getBBHCount(p, getPeakList());
    }

    private int getBBHCount(T p, Collection<T> c) {
        int bidiHits = 0;
        // check bidi best hit assumption
        // bail out if assumption fails!
        for (T q : getPeakList()) {
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

    public boolean removePeak(T p) {
        if (clique.containsValue(p)) {
            clique.remove(p.getAssociation());
            if (clique.isEmpty()) {
                clear();
                return true;
            }
            int actualBidiHits = getBBHCount(p);
            int diff = clique.size() - actualBidiHits;
            bbhErrors -= diff;
            update();
            return true;
        }
        return false;
    }

    public void clear() {
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

    private void update() {
        cliqueStatistics.update();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.cliqueStatistics.getCentroid() != null) {
            sb.append("Center: ").append(this.cliqueStatistics.getCentroid().toString()).append("\n");
        } else {
            sb.append("Center: null\n");
        }
        sb.append("\tMean: ").append(this.cliqueStatistics.getCliqueMean()).
                append("\n");
        sb.append("\tVariance: ").
                append(this.cliqueStatistics.getCliqueVariance()).append("\n");
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

    public List<T> getPeakList() {
        List<T> peaks = new ArrayList<T>(this.clique.values());
        Collections.sort(peaks, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.getAssociation().compareTo(o2.getAssociation());
            }
        });
        return peaks;
    }

    public double getSimilarityForPeaks(IFileFragment a, IFileFragment b) {
        return this.clique.get(a.getName()).getSimilarity(this.clique.get(b.getName()));
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
        if (this.clique != other.clique && (this.clique == null || !this.clique.
                equals(other.clique))) {
            return false;
        }
        return true;
    }

    public int getMaxBBHErrors() {
        return maxBBHErrors;
    }

    public void setMaxBBHErrors(int maxBBHErrors) {
        this.maxBBHErrors = maxBBHErrors;
    }
}
