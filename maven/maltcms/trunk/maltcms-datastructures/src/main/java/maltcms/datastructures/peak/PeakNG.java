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
import cross.exception.NotImplementedException;
import java.util.Arrays;
import java.util.List;



import ucar.ma2.Array;
import cross.exception.ResourceNotAvailableException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
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
	private ConcurrentHashMap<String, PeakEdge> sims = new ConcurrentHashMap<String,PeakEdge>(20,0.8f,4);
    private String name = "";
    private int peakIndex = -1;
    private final String association;
    private final boolean storeOnlyBestSimilarities;
	
	private final class PeakEdge implements Serializable {
		final UUID sourcePeakId, targetPeakId;
		final double similarity;
		public PeakEdge(IPeak sourcePeak, IPeak targetPeak, double similarity) {
			this.sourcePeakId = sourcePeak.getUniqueId();
			this.targetPeakId = targetPeak.getUniqueId();
			this.similarity = similarity;
		}
		
		public Serializable key() {
			return sourcePeakId+"-"+targetPeakId;
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
    }
	
	@Override
	public Array getMsIntensities() {
		Serializable s = peakArrayCache.get(getUniqueId()).getValue();
		return ((SerializableArray)s).getArray();
	}
	
    /**
     * Add a similarity to Peak p. Resets the sortedPeaks list for the
     * associated FileFragment of Peak p, so that a subsequent call to
     * getPeakWithHighestSimilarity or getPeaksSortedBySimilarity will rebuild
     * the list of peaks sorted ascending according to their similarity to this
     * peakId.
     *
     * @param p
     * @param similarity
     */
	@Override
    public void addSimilarity(final IPeak p, final double similarity) {
        if (this.storeOnlyBestSimilarities) {
            if (!Double.isInfinite(similarity) && !Double.isNaN(similarity)) {
				String key = p.getAssociation();
				PeakEdge t = getSims().get(key);
				if(t!=null) {
					if(t.similarity<similarity) {
						this.sims.put(key,new PeakEdge(this,p,similarity));
					}
				}else{
					this.sims.put(key,new PeakEdge(this,p,similarity));
				}
            }
        } else {
			throw new NotImplementedException();
        }
    }

	@Override
    public void clearSimilarities() {
		if(this.sims!=null) {
			this.sims.clear();
			this.sims = null;
		}
    }
	
	private Map<String,PeakEdge> getSims() {
		if(sims==null) {
			synchronized(sims) {
				sims = new ConcurrentHashMap<String,PeakEdge>(20,0.8f,4);
			}
		}
		return sims;
	}

    /**
     * Only call this method, after having added all similarities!
     *
     * @param key
     * @return
     */
	@Override
    public List<UUID> getPeaksSortedBySimilarity(final String key) {
        if (getSims().containsKey(key)) {
			return Arrays.asList(this.sims.get(key).targetPeakId);
        }
        return java.util.Collections.emptyList();
    }

	@Override
    public UUID getPeakWithHighestSimilarity(final String key) {
		if(storeOnlyBestSimilarities) {
			if(getSims().containsKey(key)) {
				return this.sims.get(key).targetPeakId;
			}
			return null;
		}else{
			throw new NotImplementedException();
		}
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
			PeakEdge t = getSims().get(key);
			if(t!=null && t.targetPeakId.equals(p.getUniqueId())) {
				return t.similarity;
			}
        return Double.NaN;
    }

	@Override
    public boolean isBidiBestHitFor(final IPeak p) {
        final UUID pT = getPeakWithHighestSimilarity(p.getAssociation());
        final UUID qT = p.getPeakWithHighestSimilarity(this.getAssociation());
        if (qT == null || pT == null) {
            return false;
        }

        if ((qT == this.getUniqueId()) && (pT == p.getUniqueId())) {
            return true;
        }
        return false;
    }

	@Override
    public void retainSimilarityRemoveRest(final IPeak p) {
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
