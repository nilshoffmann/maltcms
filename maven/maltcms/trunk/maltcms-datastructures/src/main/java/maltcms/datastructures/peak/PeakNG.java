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

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.carrotsearch.hppc.sorting.IndirectComparator;
import com.carrotsearch.hppc.sorting.IndirectSort;
import cross.datastructures.cache.SerializableArray;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.NotImplementedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;



import ucar.ma2.Array;
import cross.exception.ResourceNotAvailableException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.feature.DefaultFeatureVector;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * Shorthand class for peaks.
 *
 * @author Nils Hoffmann
 *
 */
@Data
@Slf4j
public class PeakNG extends DefaultFeatureVector implements IPeak {

    /**
     *
     */
    private static final long serialVersionUID = -4337180586706400884L;
	private static final Ehcache peakArrayCache;
    private final int scanIndex;
    private final double sat;
//    private final Map<String, ObjectDoubleOpenHashMap<IPeak>> sims = new ConcurrentHashMap<String, ObjectDoubleOpenHashMap<IPeak>>();
	private final ConcurrentHashMap<String, PeakEntry> sims = new ConcurrentHashMap<String,PeakEntry>(20,0.8f,4);//new ConcurrentHashMap<String, PeakEntry>(200, 0.9f, 4);
//    private final Map<String, IPeak[]> sortedPeaks = new ConcurrentHashMap<String, IPeak[]>();
    private String name = "";
    private int peakIndex = -1;
    private final String association;
    private final boolean storeOnlyBestSimilarities;
//	private final Array msIntensities;
	
	private final class PeakEntry {
		final IPeak peak;
		final double similarity;
		public PeakEntry(IPeak peak, double similarity) {
			this.peak = peak;
			this.similarity = similarity;
		}
	}
	
	static {
		peakArrayCache = net.sf.ehcache.CacheManager.getInstance().addCacheIfAbsent("PEAKNG-CACHE");
		CacheConfiguration cc = peakArrayCache.getCacheConfiguration();
		cc.setDiskSpoolBufferSizeMB(128);
		cc.setMaxElementsInMemory(100000);
		cc.setMaxElementsOnDisk(Integer.MAX_VALUE);
		cc.setOverflowToDisk(true);
		cc.setMemoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU.toString());
	}
	
	public PeakNG(int scanIndex, Array array, double sat, String association, boolean storeOnlyBestSimilarities) {
        this.scanIndex = scanIndex;
		peakArrayCache.put(new Element(this.getUniqueId(), new SerializableArray(array.copy())));
		this.sat = sat;
		this.association = association.intern();
		this.storeOnlyBestSimilarities = storeOnlyBestSimilarities;
//		this.msIntensities = array.copy();
    }
	
	@Override
	public Array getMsIntensities() {
		Serializable s = peakArrayCache.get(getUniqueId()).getValue();
		return ((SerializableArray)s).getArray();
//		return msIntensities;
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
				String key = p.getAssociation();
				PeakEntry t = this.sims.get(key);
				if(t!=null) {
					if(t.similarity<similarity) {
						this.sims.put(key,new PeakEntry(p,similarity));
					}
				}else{
					this.sims.put(key,new PeakEntry(p,similarity));
				}
//                if (this.sims.containsKey(p.getAssociation())) {
//					
//                    hm = this.sims.get(p.getAssociation());
//                    IPeak s = p;
//                    double sim = similarity;
//                    //since we only keep the best sim, hm will only contain at
//                    //most one key
//                    for (ObjectCursor<IPeak> q : hm.keys()) {
//                        double qsim = hm.get(q.value);
//                        if (qsim > similarity) {
//                            s = q.value;
//                            sim = q.value.getSimilarity(this);
//                        }
//                    }
//                    hm.clear();
//                    hm.put(s, sim);
//                    best = s;
//                } else {
//                    hm = new ObjectDoubleOpenHashMap<IPeak>(1);
//                    hm.put(p, similarity);
//                    this.sims.put(p.getAssociation(), hm);
//                }
//                this.sortedPeaks.put(best.getAssociation(), new IPeak[]{best});
//                if (this.sortedPeaks.containsKey(p.getAssociation())) {
//                    this.sortedPeaks.remove(p.getAssociation());
//                }
            }
        } else {
			throw new NotImplementedException();
//            if (!Double.isInfinite(similarity) && !Double.isNaN(similarity)) {
//                ObjectDoubleOpenHashMap<IPeak> hm = null;
//                if (this.sims.containsKey(p.getAssociation())) {
//                    hm = this.sims.get(p.getAssociation());
//                    hm.put(p, similarity);
//                } else {
//                    hm = new ObjectDoubleOpenHashMap<IPeak>();
//                    hm.put(p, similarity);
//                    this.sims.put(p.getAssociation(), hm);
//                }
//                if (this.sortedPeaks.containsKey(p.getAssociation())) {
//                    this.sortedPeaks.remove(p.getAssociation());
//                }
//            }
        }
    }

	@Override
    public void clearSimilarities() {
        this.sims.clear();
//        this.sortedPeaks.clear();
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
			return Arrays.asList(this.sims.get(key).peak);
//            List<IPeak> peaks = null;
//            if (this.sortedPeaks.containsKey(key)) {
//                peaks = Arrays.asList(this.sortedPeaks.get(key));
//            } else {
//				double[] similarities = this.sims.get(key).values;
//				int[] indices = IndirectSort.mergesort(0, similarities.length, new IndirectComparator.AscendingDoubleComparator(similarities));
//                peaks = new ArrayList<IPeak>();
//				for(int idx:indices) {
//					IPeak peak = this.sims.get(key).keys[idx];
//					if (!peak.getAssociation().equals(getAssociation())) {
//                        peaks.add(peak);
//                    }
//				}
//                this.sortedPeaks.put(key, peaks.toArray(new IPeak[peaks.size()]));
//            }
//            return peaks;
        }
        return java.util.Collections.emptyList();
    }

	@Override
    public IPeak getPeakWithHighestSimilarity(final String key) {
		if(storeOnlyBestSimilarities) {
			if(this.sims.containsKey(key)) {
				return this.sims.get(key).peak;
			}
			return null;
//			if(sortedPeaks.containsKey(key)) {
//				return this.sortedPeaks.get(key)[0];
//			}else{
//				return null;
//			}
		}else{
			throw new NotImplementedException();
		}
//        final List<IPeak> l = getPeaksSortedBySimilarity(key);
//        if (l.isEmpty()) {
//            return null;
//        }
//        return l.get(l.size() - 1);
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
		String key = p.getAssociation();
//        if (this.sims.containsKey(key)) {
//            if (this.sims.get(p.getAssociation()).containsKey(p)) {
//                return this.sims.get(p.getAssociation()).get(p);
//            }
			PeakEntry t = this.sims.get(key);
			if(t!=null && t.peak.equals(p)) {
				return t.similarity;
			}
//        }
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
//		String key = p.getAssociation();
//        if (this.sims.containsKey(key)) {
//			Tuple2D<IPeak,Double> t = this.sims.get(key);
//			if(t.getFirst().equals(p)) {
//				
//			}
////            final ObjectDoubleOpenHashMap<IPeak> hm = this.sims.get(p.getAssociation());
////            if (hm.containsKey(p)) {
////                log.debug("Retaining similarity to {} in {}", p, this);
////                final double lhsToRhs = hm.get(p);
////                // Double rhsToLhs = p.getSimilarity(this);
////                hm.clear();
////                hm.put(p, lhsToRhs);
////                this.sims.put(p.getAssociation(),hm);
////                this.sortedPeaks.put(p.getAssociation(), new IPeak[]{p});
////            }
//        }
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
            return getMsIntensities();
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
        if (o!=null && o instanceof PeakNG) {
            return getUniqueId().equals(((PeakNG)o).getUniqueId());
        }
        return false;
    }
}
