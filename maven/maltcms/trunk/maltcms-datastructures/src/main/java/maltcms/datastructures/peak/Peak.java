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

import cross.datastructures.cache.SerializableArray;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;



import ucar.ma2.Array;
import cross.exception.ResourceNotAvailableException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.feature.DefaultFeatureVector;

/**
 * Shorthand class for peaks.
 *
 * @author Nils Hoffmann
 *
 */
@Data
@Slf4j
public class Peak extends DefaultFeatureVector implements IPeak {

    /**
     *
     */
    private static final long serialVersionUID = -4337180586706400884L;
    private final int scanIndex;
	@Getter(AccessLevel.PRIVATE)
    private final SerializableArray msIntensities;
    private final double sat;
    private final Map<String, Map<IPeak, Double>> sims = new ConcurrentHashMap<String, Map<IPeak, Double>>();
    private final Map<String, List<IPeak>> sortedPeaks = new ConcurrentHashMap<String, List<IPeak>>();
    private String name = "";
    private int peakIndex = -1;
    private final String association;
    private final boolean storeOnlyBestSimilarities;

	public Peak(int scanIndex, Array msIntensities, double sat, String association, boolean storeOnlyBestSimilarities) {
        this.scanIndex = scanIndex;
		this.msIntensities = new SerializableArray(msIntensities);
		this.sat = sat;
		this.association = association;
		this.storeOnlyBestSimilarities = storeOnlyBestSimilarities;
    }
	
	@Override
	public Array getMsIntensities() {
		return msIntensities.getArray();
	}
	
    /**
     * Add a similarity to Peak p. Resets the sortedPeaks list for the
     * associated FileFragment of Peak p, so that a subsequent call to
     * getPeakWithHighestSimilarity or getPeaksSortedBySimilarity will rebuild
     * the list of peaks sorted ascending according to their similarity to this
     * peak.
     *
     * @param p
     * @param similarity
     */
	@Override
    public void addSimilarity(final IPeak p, final double similarity) {
        if (this.storeOnlyBestSimilarities) {
            if (!Double.isInfinite(similarity) && !Double.isNaN(similarity)) {
                Map<IPeak, Double> hm = null;
                IPeak best = p;
                if (this.sims.containsKey(p.getAssociation())) {
                    hm = this.sims.get(p.getAssociation());
                    IPeak s = p;
                    double sim = similarity;
                    //since we only keep the best sim, hm will only contain at
                    //most one key
                    for (IPeak q : hm.keySet()) {
                        double qsim = hm.get(q);
                        if (qsim > similarity) {
                            s = q;
                            sim = q.getSimilarity(this);
                        }
                    }
                    hm.clear();
                    hm.put(s, sim);
                    best = s;
                } else {
                    hm = new ConcurrentHashMap<IPeak, Double>();
                    hm.put(p, similarity);
                    this.sims.put(p.getAssociation(), hm);
                }
                this.sortedPeaks.put(best.getAssociation(), Arrays.asList(best));
//                if (this.sortedPeaks.containsKey(p.getAssociation())) {
//                    this.sortedPeaks.remove(p.getAssociation());
//                }
            }
        } else {
            if (!Double.isInfinite(similarity) && !Double.isNaN(similarity)) {
                Map<IPeak, Double> hm = null;
                if (this.sims.containsKey(p.getAssociation())) {
                    hm = this.sims.get(p.getAssociation());
                    hm.put(p, similarity);
                } else {
                    hm = new ConcurrentHashMap<IPeak, Double>();
                    hm.put(p, similarity);
                    this.sims.put(p.getAssociation(), hm);
                }
                if (this.sortedPeaks.containsKey(p.getAssociation())) {
                    this.sortedPeaks.remove(p.getAssociation());
                }
            }
        }
    }

	@Override
    public void clearSimilarities() {
        this.sims.clear();
        this.sortedPeaks.clear();
    }

    /**
     * Only call this method, after having added all similarities!
     *
     * @param key
     * @return
     */
	@Override
    public List<IPeak> getPeaksSortedBySimilarity(final String key) {
        if (this.sims.containsKey(key)) {
            List<IPeak> peaks = null;
            if (this.sortedPeaks.containsKey(key)) {
                peaks = this.sortedPeaks.get(key);
            } else {
                final Set<Entry<IPeak, Double>> s = this.sims.get(key).entrySet();
                final ArrayList<Entry<IPeak, Double>> al = new ArrayList<Entry<IPeak, Double>>();
                for (final Entry<IPeak, Double> e : s) {
                    if (!e.getKey().getAssociation().equals(getAssociation())) {
                        al.add(e);
                    }
                }

                // al.addAll(s);
                Collections.sort(al, new Comparator<Entry<IPeak, Double>>() {
                    @Override
                    public int compare(final Entry<IPeak, Double> o1,
                            final Entry<IPeak, Double> o2) {
                        if (o1.getValue() == o2.getValue()) {
                            return 0;
                        } else if (o1.getValue() < o2.getValue()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
                peaks = new ArrayList<IPeak>(al.size());
                for (final Entry<IPeak, Double> e : al) {
                    peaks.add(e.getKey());
                }
                this.sortedPeaks.put(key, peaks);
            }
            return peaks;
        }
        return java.util.Collections.emptyList();
    }

	@Override
    public IPeak getPeakWithHighestSimilarity(final String key) {
        final List<IPeak> l = getPeaksSortedBySimilarity(key);
        if (l.isEmpty()) {
            return null;
        }
        return l.get(l.size() - 1);
    }

	@Override
    public double getScanAcquisitionTime() {
        return this.sat;
    }

	@Override
    public int getScanIndex() {
        return this.scanIndex;
    }

	@Override
    public double getSimilarity(final IPeak p) {
        if (this.sims.containsKey(p.getAssociation())) {
            if (this.sims.get(p.getAssociation()).containsKey(p)) {
                return this.sims.get(p.getAssociation()).get(p);
            }
        }
        return Double.NaN;
    }

	@Override
    public boolean isBidiBestHitFor(final IPeak p) {
        final IPeak pT = getPeakWithHighestSimilarity(p.getAssociation());
        final IPeak qT = p.getPeakWithHighestSimilarity(this.getAssociation());
        if (qT == null || pT == null) {
            return false;
        }

        if ((qT == this) && (pT == p)) {
            return true;
        }
        return false;
    }

	@Override
    public void retainSimilarityRemoveRest(final IPeak p) {
        if (this.sims.containsKey(p.getAssociation())) {
            final Map<IPeak, Double> hm = this.sims.get(p.getAssociation());
            if (hm.containsKey(p)) {
                log.debug("Retaining similarity to {} in {}", p, this);
                final Double lhsToRhs = hm.get(p);
                // Double rhsToLhs = p.getSimilarity(this);
                hm.clear();
                hm.put(p, lhsToRhs);
                this.sims.put(p.getAssociation(),
                        Collections.unmodifiableMap(hm));
                final ArrayList<IPeak> al = new ArrayList<IPeak>();
                al.add(p);
                this.sortedPeaks.put(p.getAssociation(), al);
            }
        }
    }

    @Override
    public int hashCode() {
        return getUniqueId().hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Peak number " + this.peakIndex + " at position " + this.scanIndex + " and rt: " + this.sat
                + " in file " + this.association);
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
     */
    @Override
    public Array getFeature(String name) {
        if (name.equals("scan_acquisition_time")) {
            return Array.factory(this.sat);
        } else if (name.equals("scan_index")) {
            return Array.factory(this.scanIndex);
        } else if (name.equals("binned_intensity_values")) {
            return this.msIntensities.getArray();
        }
        Array retVal = super.getFeature(name);
        if(retVal!=null) {
            return retVal;
        }
        throw new ResourceNotAvailableException("No such feature: " + name);
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IFeatureVector#getFeatureNames()
     */
    @Override
    public List<String> getFeatureNames() {
        List<String> superFeatureNames = super.getFeatureNames();
        LinkedList<String> allFeatures = new LinkedList<String>(superFeatureNames);
        allFeatures.addAll(Arrays.asList("scan_acquisition_time", "scan_index",
                "binned_intensity_values"));
        return allFeatures;
    }

    @Override
    public boolean equals(Object o) {
        if (o!=null && o instanceof Peak) {
            return getUniqueId().equals(((Peak)o).getUniqueId());
        }
        return false;
    }
}
