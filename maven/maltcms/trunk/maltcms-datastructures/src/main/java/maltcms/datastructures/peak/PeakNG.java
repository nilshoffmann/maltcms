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
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.feature.DefaultFeatureVector;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheWriterConfiguration;
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
//	private static final Ehcache edgeCache;//, peakIdEdgeIdCache;//,peakArrayCache;
	private final int scanIndex;
	private final double sat;
	private static ConcurrentHashMap<UUID, UUID> peakIdEdgeIdCache = new ConcurrentHashMap<UUID, UUID>(20, 0.8f, 8);
	private static ConcurrentHashMap<UUID, PeakEdge> edgeCache = new ConcurrentHashMap<UUID, PeakEdge>(20, 0.8f, 8);
	private static Set<String> associations = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private String name = "";
	private final String peakKey;
	private int peakIndex = -1;
	private final String association;
	private final boolean storeOnlyBestSimilarities;
	private final SerializableArray msIntensities;

	private final class PeakEdge implements Serializable {

		final UUID sourcePeakId, targetPeakId, edgeId;
		final double similarity;

		public PeakEdge(IPeak sourcePeak, IPeak targetPeak, double similarity) {
			this.sourcePeakId = sourcePeak.getUniqueId();
			this.targetPeakId = targetPeak.getUniqueId();
			this.similarity = similarity;
			edgeId = UUID.nameUUIDFromBytes((sourcePeakId.toString() + targetPeakId.toString()).getBytes());
		}

		public UUID key() {
			return edgeId;
		}
	}

	static {
//		peakArrayCache = net.sf.ehcache.CacheManager.getInstance().addCacheIfAbsent("PEAKNG-CACHE");
//		CacheConfiguration cc = peakArrayCache.getCacheConfiguration();
//		cc.setDiskSpoolBufferSizeMB(512);
//		cc.setMaxElementsInMemory(100000);
//		cc.setMaxElementsOnDisk(Integer.MAX_VALUE);
//		cc.setOverflowToDisk(true);
//		cc.setMemoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU.toString());
//		edgeCache = net.sf.ehcache.CacheManager.getInstance().addCacheIfAbsent("PEAKNG-EDGE-CACHE");
//		CacheWriterConfiguration cwc1 = new CacheWriterConfiguration();
//		cwc1.setWriteBatching(true);
//		cwc1.setWriteBatchSize(100);
//		cwc1.setWriteCoalescing(true);
//		cwc1.setMaxWriteDelay(5);
//		cwc1.setWriteMode(CacheWriterConfiguration.WriteMode.WRITE_BEHIND.toString());
//		CacheConfiguration cc2 = edgeCache.getCacheConfiguration();
//		cc2.cacheWriter(cwc1);
//		cc2.setDiskSpoolBufferSizeMB(512);
//		cc2.setDiskAccessStripes(8);
//		cc2.setMaxElementsInMemory(10000000);
//		cc2.setMaxElementsOnDisk(Integer.MAX_VALUE);
//		cc2.setTransactionalMode(CacheConfiguration.TransactionalMode.OFF.toString());
//		cc2.setOverflowToDisk(true);
//		cc2.setDiskPersistent(false);
//		cc2.setMemoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU.toString());
//		peakIdEdgeIdCache = net.sf.ehcache.CacheManager.getInstance().addCacheIfAbsent("PEAKNG-ID-EDGE-CACHE");
//		CacheWriterConfiguration cwc2 = new CacheWriterConfiguration();
//		cwc2.setWriteBatching(true);
//		cwc2.setWriteBatchSize(100);
//		cwc2.setMaxWriteDelay(1);
//		cwc2.setWriteCoalescing(true);
//		cwc2.setWriteMode(CacheWriterConfiguration.WriteMode.WRITE_BEHIND.toString());
//		CacheConfiguration cc3 = edgeCache.getCacheConfiguration();
//		cc3.cacheWriter(cwc2);
//		cc3.setDiskSpoolBufferSizeMB(512);
//		cc3.setDiskAccessStripes(8);
//		cc3.setMaxElementsInMemory(10000000);
//		cc3.setMaxElementsOnDisk(Integer.MAX_VALUE);
//		cc3.setTransactionalMode(CacheConfiguration.TransactionalMode.OFF.toString());
//		cc3.setOverflowToDisk(true);
//		cc3.setMemoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU.toString());
	}

	public PeakNG(int scanIndex, Array array, double sat, String association, boolean storeOnlyBestSimilarities) {
		this.scanIndex = scanIndex;
//		peakArrayCache.put(new Element(this.getUniqueId(), new SerializableArray(array.copy())));
		this.msIntensities = new SerializableArray(array.copy());
		this.sat = sat;
		this.association = association.intern();
		associations.add(this.association);
		this.storeOnlyBestSimilarities = storeOnlyBestSimilarities;
		this.peakKey = (this.association + "-" + this.scanIndex).intern();
//		this.msIntensities = array;
	}

	@Override
	public Array getMsIntensities() {
//		Serializable s = peakArrayCache.get(getUniqueId()).getValue();
//		return ((SerializableArray) s).getArray();
		return msIntensities.getArray();
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
//				String peakToEdgeId = p.getAssociation();
				UUID key = keyTo(p);
				UUID id = getSims().get(key);
				if (id != null) {
					PeakEdge pe = (PeakEdge) edgeCache.get(id);
					if (pe.similarity < similarity) {
						edgeCache.remove(pe.edgeId);
						PeakEdge edge = new PeakEdge(this, p, similarity);
						edgeCache.put(edge.edgeId, edge);
						peakIdEdgeIdCache.put(key, edge.edgeId);
					}
				} else {
					PeakEdge edge = new PeakEdge(this, p, similarity);
					edgeCache.put(edge.edgeId, edge);
					peakIdEdgeIdCache.put(key, edge.edgeId);
				}
			}
		} else {
			throw new NotImplementedException();
		}
	}

	private UUID keyTo(IPeak p) {
		return keyTo(p.getAssociation());
	}

	private UUID keyTo(String association) {
		try {
			String key = peakKey + "-" + association;
			return UUID.nameUUIDFromBytes(key.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			return null;
		}
	}

	@Override
	public void clearSimilarities() {
		if (peakIdEdgeIdCache != null) {
			peakIdEdgeIdCache.clear();
//			peakIdEdgeIdCache.dispose();
		}
	}

	private Map<UUID, UUID> getSims() {
//		if (sims == null) {
//			sims = new ConcurrentHashMap<UUID, UUID>(20, 0.8f, 4);
//		}
		return peakIdEdgeIdCache;
	}

	/**
	 * Only call this method, after having added all similarities!
	 *
	 * @param association
	 * @return
	 */
	@Override
	public List<UUID> getPeaksSortedBySimilarity(final String association) {
		UUID key = keyTo(association);
		UUID id = getSims().get(key);
		if (id != null) {
			PeakEdge pe = (PeakEdge) edgeCache.get((UUID) peakIdEdgeIdCache.get(key));
			return Arrays.asList(pe.targetPeakId);
		}
		return java.util.Collections.emptyList();
	}

	@Override
	public UUID getPeakWithHighestSimilarity(final String association) {
		UUID key = keyTo(association);
		if (storeOnlyBestSimilarities) {
			UUID id = getSims().get(key);
			if (id != null) {
				PeakEdge pe = (PeakEdge) edgeCache.get((UUID) peakIdEdgeIdCache.get(key));
				return pe.targetPeakId;
			}
			return null;
		} else {
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
		UUID key = keyTo(p);//p.getAssociation();
		UUID id = (UUID) peakIdEdgeIdCache.get(key);
		if (id != null) {
			PeakEdge t = (PeakEdge) edgeCache.get(id);
			if (t != null && t.targetPeakId.equals(p.getUniqueId())) {
				return t.similarity;
			}
		}
		return Double.NaN;
	}

	@Override
	public boolean isBidiBestHitFor(final IPeak p) {
		final UUID pT = getPeakWithHighestSimilarity(p.getAssociation());
		final UUID qT = p.getPeakWithHighestSimilarity(this.association);
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
//		for (String association : associations) {
//			if (!p.getAssociation().equals(association)) {
//				UUID peakToEdgeId = keyTo(association);
//				UUID edgeId = (UUID) peakIdEdgeIdCache.get(peakToEdgeId);
//				if(edgeId!=null) {
//					edgeCache.remove(edgeId);
//				}
//				peakIdEdgeIdCache.remove(peakToEdgeId);
//			}
//		}
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
		if (retVal != null) {
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
		if (o != null && o instanceof PeakNG) {
			return getUniqueId().equals(((PeakNG) o).getUniqueId());
		}
		return false;
	}
}
