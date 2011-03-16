/**
 * 
 */
package maltcms.datastructures.peak;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import maltcms.datastructures.array.IFeatureVector;

import org.slf4j.Logger;

import ucar.ma2.Array;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.exception.ResourceNotAvailableException;
import java.util.LinkedHashMap;

/**
 * Shorthand class for peaks.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
public class Peak implements IFeatureVector {

    /**
     * 
     */
    private static final long serialVersionUID = -4337180586706400884L;
    private Logger log = Logging.getLogger(this);
    private IFileFragment association = null;
    private final int scanIndex;
    private final Array msIntensities;
    private final double sat;
    private String name = "";
    private final HashMap<IFileFragment, Map<Peak, Double>> sims = new HashMap<IFileFragment, Map<Peak, Double>>();
    private final HashMap<IFileFragment, List<Peak>> sortedPeaks = new HashMap<IFileFragment, List<Peak>>();
    private boolean storeOnlyBestSimilarities = true;

    public Peak(final String name, final IFileFragment file,
            final int scanIndex, final Array msIntensities,
            final double scan_acquisition_time) {
        this.name = name;
        // EvalTools.notNull(file, this);
        this.association = file;
        this.scanIndex = scanIndex;
        this.msIntensities = msIntensities;
        this.sat = scan_acquisition_time;
    }

    /**
     * Call this directly after creation of Peak.
     * @param b
     */
    public void setStoreOnlyBestSimilarities(boolean b) {
        this.storeOnlyBestSimilarities = b;
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
    public void addSimilarity(final Peak p, final Double similarity) {
        if (this.storeOnlyBestSimilarities) {
            if (!Double.isInfinite(similarity) && !Double.isNaN(similarity)) {
                Map<Peak, Double> hm = null;
                Peak best = p;
                if (this.sims.containsKey(p.getAssociation())) {
                    hm = this.sims.get(p.getAssociation());
                    Peak s = p;
                    double sim = similarity;
                    //since we only keep the best sim, hm will only contain at
                    //most one key
                    for (Peak q : hm.keySet()) {
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
                    hm = new LinkedHashMap<Peak, Double>();
                    hm.put(p, similarity);
                    this.sims.put(p.getAssociation(), hm);
                }
                this.sortedPeaks.put(best.getAssociation(),Arrays.asList(best));
//                if (this.sortedPeaks.containsKey(p.getAssociation())) {
//                    this.sortedPeaks.remove(p.getAssociation());
//                }
            }
        } else {
            if (!Double.isInfinite(similarity) && !Double.isNaN(similarity)) {
                Map<Peak, Double> hm = null;
                if (this.sims.containsKey(p.getAssociation())) {
                    hm = this.sims.get(p.getAssociation());
                    hm.put(p, similarity);
                } else {
                    hm = new HashMap<Peak, Double>();
                    hm.put(p, similarity);
                    this.sims.put(p.getAssociation(), hm);
                }
                if (this.sortedPeaks.containsKey(p.getAssociation())) {
                    this.sortedPeaks.remove(p.getAssociation());
                }
            }
        }
    }

    public void clearSimilarities() {
        this.sims.clear();
        this.sortedPeaks.clear();
    }

    public IFileFragment getAssociation() {
        return this.association;
    }

    public Array getMSIntensities() {
        return this.msIntensities;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Only call this method, after having added all similarities!
     *
     * @param iff
     * @return
     */
    public List<Peak> getPeaksSortedBySimilarity(final IFileFragment iff) {
        if (this.sims.containsKey(iff)) {
            List<Peak> peaks = null;
            if (this.sortedPeaks.containsKey(iff)) {
                peaks = this.sortedPeaks.get(iff);
            } else {
                final Set<Entry<Peak, Double>> s = this.sims.get(iff).entrySet();
                final ArrayList<Entry<Peak, Double>> al = new ArrayList<Entry<Peak, Double>>();
                for (final Entry<Peak, Double> e : s) {
                    if (!e.getKey().getAssociation().getName().equals(getAssociation().getName())) {
                        al.add(e);
                    }
                }

                // al.addAll(s);
                Collections.sort(al, new Comparator<Entry<Peak, Double>>() {

                    @Override
                    public int compare(final Entry<Peak, Double> o1,
                            final Entry<Peak, Double> o2) {
                        if (o1.getValue() == o2.getValue()) {
                            return 0;
                        } else if (o1.getValue() < o2.getValue()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
                peaks = new ArrayList<Peak>(al.size());
                for (final Entry<Peak, Double> e : al) {
                    peaks.add(e.getKey());
                }
                this.sortedPeaks.put(iff, peaks);
            }
            return peaks;
        }
        return java.util.Collections.emptyList();
    }

    public Peak getPeakWithHighestSimilarity(final IFileFragment iff) {
        final List<Peak> l = getPeaksSortedBySimilarity(iff);
        if (l.isEmpty()) {
            return null;
        }
        return l.get(l.size() - 1);
    }

    public double getScanAcquisitionTime() {
        return this.sat;
    }

    public int getScanIndex() {
        return this.scanIndex;
    }

    public Double getSimilarity(final Peak p) {
        if (this.sims.containsKey(p.getAssociation())) {
            if (this.sims.get(p.getAssociation()).containsKey(p)) {
                return this.sims.get(p.getAssociation()).get(p);
            }
        }
        return Double.NaN;
    }

    public boolean isBidiBestHitFor(final Peak p) {
        final Peak pT = getPeakWithHighestSimilarity(p.getAssociation());
        final Peak qT = p.getPeakWithHighestSimilarity(this.getAssociation());
        if (qT == null || pT == null) {
            return false;
        }

        if ((qT == this) && (pT == p)) {
            return true;
        }
        return false;
    }

    public void retainSimilarityRemoveRest(final Peak p) {
        if (this.sims.containsKey(p.getAssociation())) {
            final Map<Peak, Double> hm = this.sims.get(p.getAssociation());
            if (hm.containsKey(p)) {
                log.debug("Retaining similarity to {} in {}", p, this);
                final Double lhsToRhs = hm.get(p);
                // Double rhsToLhs = p.getSimilarity(this);
                hm.clear();
                hm.put(p, lhsToRhs);
                this.sims.put(p.getAssociation(),
                        Collections.unmodifiableMap(hm));
                final ArrayList<Peak> al = new ArrayList<Peak>();
                al.add(p);
                this.sortedPeaks.put(p.getAssociation(), al);
            }
        }
    }

    public void setName(final String s) {
        this.name = s;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Peak at position " + this.scanIndex + " and rt: " + this.sat
                + " in file " + this.association.getName());
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
            return this.msIntensities;
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
        return Arrays.asList("scan_acquisition_time", "scan_index",
                "binned_intensity_values");
    }

    @Override
    public boolean equals(Object o) {
        return toString().equals(o.toString());
    }
}
