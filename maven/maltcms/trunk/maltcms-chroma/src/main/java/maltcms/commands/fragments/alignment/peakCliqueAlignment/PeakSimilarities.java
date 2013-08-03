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

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import maltcms.datastructures.peak.IPeak;

/**
 *
 * @author Nils Hoffmann
 */
public class PeakSimilarities {

	private final String[] partitions;
	private final ObjectIntOpenHashMap<String> partitionNameToIndex;
	private final int[] partitionSizes;
	private final int[] partitionOffset;
	private final DoubleMatrix2D sdm;
	private final IntObjectOpenHashMap<Map<UUID, IPeak>> peakIdToBestSimilarity;

	public PeakSimilarities(String[] partitionNames, int[] partitionSizes) {
		this.partitions = partitionNames;
		int size = 0;
		this.partitionNameToIndex = new ObjectIntOpenHashMap<String>();
		this.partitionOffset = new int[partitionSizes.length];
		for (int i = 0; i < partitionSizes.length; i++) {
			partitionOffset[i] = size;
			size += partitionSizes[i];
			partitionNameToIndex.put(partitionNames[i], i);
		}
		sdm = new SparseDoubleMatrix2D(size, size);
		this.partitionSizes = partitionSizes;
		this.peakIdToBestSimilarity = new IntObjectOpenHashMap<Map<UUID, IPeak>>();
	}

	public String getPartitionName(IPeak peak) {
		return peak.getAssociation();
	}
	
//	public double getSimilarityFor(IPeak peak1, IPeak peak2) {
//		sdm.
//	}

	public void addSimilarity(IPeak peak1, IPeak peak2, double similarity) {
		sdm.setQuick(partitionOffset[partitionNameToIndex.get(peak1.getAssociation())] + peak1.getPeakIndex(), partitionOffset[partitionNameToIndex.get(peak2.getAssociation())] + peak2.getPeakIndex(), similarity);
		IPeak p = getPeakWithHighestSimilarity(peak1, peak2.getAssociation());
		if(p==null) {
			int key = partitionNameToIndex.get(peak2.getAssociation());
			Map<UUID,IPeak> m = peakIdToBestSimilarity.get(key);
			if(m==null) {
				m = new ConcurrentHashMap<UUID,IPeak>();
			}
//			peakIdToBestSimilarity.put(key, null)
		}
	}
	

	public IPeak getPeakWithHighestSimilarity(IPeak p, String partition) {
		int key = partitionNameToIndex.get(partition);
		Map<UUID,IPeak> m = peakIdToBestSimilarity.get(key);
		if(m==null) {
			return null;
		}
		return m.get(p.getUniqueId());
	}

	public DoubleMatrix1D getSimilaritiesForPeak(IPeak p, String partition) {
		int partitionIndex = partitionNameToIndex.get(partition);
		int offset = partitionOffset[partitionIndex];
		int width;
		if (partitionIndex == partitionOffset.length - 1) {
			width = sdm.columns() - offset;
		} else {
			width = partitionOffset[partitionIndex + 1] - offset;
		}
		return sdm.viewRow(getIndexForPeak(p)).viewPart(offset, width);
	}

	public int getIndexForPeak(IPeak p) {
		int partitionIndex = partitionNameToIndex.get(p.getAssociation());
		int offset = partitionOffset[partitionIndex];
		return offset + p.getPeakIndex();
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
	public void addSimilarity(final IPeak p, final double similarity) {
//        if (this.storeOnlyBestSimilarities) {
//            if (!Double.isInfinite(similarity) && !Double.isNaN(similarity)) {
//                ObjectDoubleOpenHashMap<IPeak> hm = null;
//                IPeak best = p;
//                if (this.sims.containsKey(p.getAssociation())) {
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
////                if (this.sortedPeaks.containsKey(p.getAssociation())) {
////                    this.sortedPeaks.remove(p.getAssociation());
////                }
//            }
//        } else {
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
//        }
	}

	public void clearSimilarities() {
//        this.sims.clear();
//        this.sortedPeaks.clear();
	}

	/**
	 * Only call this method, after having added all similarities!
	 *
	 * @param key
	 * @return
	 */
	public List<IPeak> getPeaksSortedBySimilarity(final IPeak peak, final String otherAssociation) {
//        if (this.sims.containsKey(key)) {
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
//        }
		return java.util.Collections.emptyList();
	}

//	public IPeak getPeakWithHighestSimilarity(final IPeak peak, final String key) {
////		if(storeOnlyBestSimilarities) {
////			if(sortedPeaks.containsKey(key)) {
////				return this.sortedPeaks.get(key)[0];
////			}else{
////				return null;
////			}
////		}
////        final List<IPeak> l = getPeaksSortedBySimilarity(key);
////        if (l.isEmpty()) {
////            return null;
////        }
////        return l.get(l.size() - 1);
//		return null;
//	}

	public double getSimilarity(final IPeak p) {
//        if (this.sims.containsKey(p.getAssociation())) {
//            if (this.sims.get(p.getAssociation()).containsKey(p)) {
//                return this.sims.get(p.getAssociation()).get(p);
//            }
//        }
		return Double.NaN;
	}

	public boolean isBidiBestHitFor(final IPeak source, final IPeak p) {
//        final IPeak pT = getPeakWithHighestSimilarity(p.getAssociation());
//        final IPeak qT = p.getPeakWithHighestSimilarity(this.getAssociation());
//        if (qT == null || pT == null) {
//            return false;
//        }
//
//        if ((qT == this) && (pT == p)) {
//            return true;
//        }
		return false;
	}

	public void retainSimilarityRemoveRest(final IPeak source, final IPeak p) {
//        if (this.sims.containsKey(p.getAssociation())) {
//            final ObjectDoubleOpenHashMap<IPeak> hm = this.sims.get(p.getAssociation());
//            if (hm.containsKey(p)) {
//                log.debug("Retaining similarity to {} in {}", p, this);
//                final Double lhsToRhs = hm.get(p);
//                // Double rhsToLhs = p.getSimilarity(this);
//                hm.clear();
//                hm.put(p, lhsToRhs);
//                this.sims.put(p.getAssociation(),hm);
//                this.sortedPeaks.put(p.getAssociation(), new IPeak[]{p});
//            }
//        }
	}
}
