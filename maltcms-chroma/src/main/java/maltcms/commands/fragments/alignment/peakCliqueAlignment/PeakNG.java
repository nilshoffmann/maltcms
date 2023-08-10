/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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

import com.carrotsearch.hppc.LongObjectMap;
import cross.datastructures.cache.SerializableArray;
import cross.exception.ResourceNotAvailableException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import maltcms.datastructures.feature.DefaultFeatureVector;
import ucar.ma2.Array;

/**
 * Shorthand class for peaks.
 *
 * @author Nils Hoffmann
 * 
 * @since 1.3.2
 */
@Data

public class PeakNG extends DefaultFeatureVector implements IBipacePeak {

    /**
     *
     */
    private static final long serialVersionUID = -4337180586706400884L;
    @Setter(AccessLevel.NONE)
    private int scanIndex;
    @Setter(AccessLevel.NONE)
    private double sat;
    private String name = "";
    @Setter(AccessLevel.NONE)
    private String peakKey;
    private int peakIndex = -1;
    @Setter(AccessLevel.NONE)
    private String association;
    private transient Array msIntensities;
    @Setter(AccessLevel.NONE)
    private int peakId;
//	private static NonBlockingHashMapLong<PeakEdge> bestHits = new NonBlockingHashMapLong<PeakEdge>(true);
    private static int peakIDs = 0;
    @Setter(AccessLevel.NONE)
    private int associationId;

    /**
     * <p>Constructor for PeakNG.</p>
     *
     * @param scanIndex a int.
     * @param array a {@link ucar.ma2.Array} object.
     * @param sat a double.
     * @param association a {@link java.lang.String} object.
     * @param associationId a int.
     */
    public PeakNG(int scanIndex, Array array, double sat, String association, int associationId) {
        super(UUID.nameUUIDFromBytes((association + "-" + scanIndex).getBytes()));
        this.scanIndex = scanIndex;
        this.association = association.intern();
        this.peakKey = (this.association + "-" + this.scanIndex);
        this.msIntensities = array.copy();
        this.sat = sat;
        this.associationId = associationId;
        this.peakId = peakIDs++;
    }

    /** {@inheritDoc} */
    @Override
    public Array getMsIntensities() {
        return msIntensities;
    }

    /** {@inheritDoc} */
    @Override
    public void setMsIntensities(Array a) {
        if (a == null) {
            msIntensities = null;
        } else {
            msIntensities = a.copy();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Add a similarity to Peak p. Resets the sortedPeaks list for the
     * associated FileFragment of Peak p, so that a subsequent call to
     * getPeakWithHighestSimilarity or getPeaksSortedBySimilarity will rebuild
     * the list of peaks sorted ascending according to their similarity to this
     * peakId.
     */
    @Override
    public void addSimilarity(LongObjectMap<PeakEdge> bestHits, final IBipacePeak p, final double similarity) {
        if (!Double.isInfinite(similarity) && !Double.isNaN(similarity)) {
            long key = keyTo(p);
            PeakEdge peakEdge = bestHits.get(key);
            if (peakEdge != null) {
                if (peakEdge.similarity < similarity) {
//						log.info("Replacing PeakEdge");
                    PeakEdge edge = new PeakEdge(this, p, similarity);
                    bestHits.put(key, edge);
                }
            } else {
                PeakEdge edge = new PeakEdge(this, p, similarity);
                bestHits.put(key, edge);
//					log.info("Key "+key+" value="+edge);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public long keyTo(IBipacePeak p) {
        return keyTo(p.getAssociationId());
    }

    /** {@inheritDoc} */
    @Override
    public long keyTo(int associationId) {
        return keyTo(associationId, peakId);
    }

//	private long cantorPair(int partition, int peak) {
//		long s1 = partition + peak;
//		s1 = (s1 * (s1 + 1)) / 2;
//		return s1 + peak;
//	}
    /**
     * <p>keyTo.</p>
     *
     * @param source a {@link maltcms.commands.fragments.alignment.peakCliqueAlignment.IBipacePeak} object.
     * @param target a {@link maltcms.commands.fragments.alignment.peakCliqueAlignment.IBipacePeak} object.
     * @return a long.
     */
    public long keyTo(IBipacePeak source, IBipacePeak target) {
        return keyTo(target.getAssociationId(), source.getPeakId());
    }

    /**
     * <p>keyTo.</p>
     *
     * @param source a {@link maltcms.commands.fragments.alignment.peakCliqueAlignment.IBipacePeak} object.
     * @param targetPartitionIndex a int.
     * @return a long.
     */
    public long keyTo(IBipacePeak source, int targetPartitionIndex) {
        return keyTo(targetPartitionIndex, source.getPeakId());
    }

    /**
     * <p>keyTo.</p>
     *
     * @param partitionKey a int.
     * @param peakId a int.
     * @return a long.
     */
    public long keyTo(int partitionKey, int peakId) {
        return highLowPair(partitionKey, peakId);
    }

    /**
     * <p>highLowPair.</p>
     *
     * @param x a int.
     * @param y a int.
     * @return a long.
     */
    public static long highLowPair(int x, int y) {
        return ((long) x) << 32 | (long) y;
    }

    /**
     * <p>highLowUnPair.</p>
     *
     * @param z a long.
     * @return an array of int.
     */
    public static int[] highLowUnPair(long z) {
        return new int[]{(int) (z >> 32), (int) (z & 0xFFFFFFFF)};
    }

    /** {@inheritDoc} */
    @Override
    public void clearSimilarities(LongObjectMap<PeakEdge> bestHits, int associationId) {
        long key = keyTo(associationId);
        bestHits.remove(key);
    }

//	private Map<Long, PeakEdge> getBestHits() {
//		return bestHits;
//	}
    /**
     * {@inheritDoc}
     *
     * Only call this method, after having added all similarities!
     */
    @Override
    public List<UUID> getPeaksSortedBySimilarity(LongObjectMap<PeakEdge> bestHits, final int associationId) {
        long key = keyTo(associationId);
        PeakEdge id = bestHits.get(key);
        if (id != null) {
            return Arrays.asList(id.targetPeakId);
        }
        return java.util.Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Override
    public UUID getPeakWithHighestSimilarity(LongObjectMap<PeakEdge> bestHits, final int associationId) {
        long key = keyTo(associationId);
        PeakEdge id = bestHits.get(key);
        if (id != null) {
            return id.targetPeakId;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public double getScanAcquisitionTime() {
        return this.sat;
    }

    /** {@inheritDoc} */
    @Override
    public int getScanIndex() {
        return this.scanIndex;
    }

    /** {@inheritDoc} */
    @Override
    public double getSimilarity(LongObjectMap<PeakEdge> bestHits, final IBipacePeak p) {
        long key = keyTo(p);
        PeakEdge id = bestHits.get(key);
        if (id != null && id.targetPeakId.equals(p.getUniqueId())) {
            return id.similarity;
        }
        return Double.NaN;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBidiBestHitFor(LongObjectMap<PeakEdge> bestHits, final IBipacePeak p) {
        final UUID pT = getPeakWithHighestSimilarity(bestHits, p.getAssociationId());
        final UUID qT = p.getPeakWithHighestSimilarity(bestHits, this.associationId);
        if (qT == null || pT == null) {
            return false;
        }

        if ((qT.equals(this.getUniqueId())) && (pT.equals(p.getUniqueId()))) {
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void retainSimilarityRemoveRest(LongObjectMap<PeakEdge> bestHits, final IBipacePeak p) {
//		for (String association : keyMap.keySet()) {
//			if (!p.getAssociation().equals(association)) {
//				log.info("Removing non-best hit association!");
//				Long peakToEdgeId = keyTo(association);
//				bestHits.remove(peakToEdgeId);
//			}
//		}
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return getUniqueId().hashCode();
    }

    /** {@inheritDoc} */
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
    /** {@inheritDoc} */
    @Override
    public Array getFeature(String name) {
        switch (name) {
            case "scan_acquisition_time":
                return Array.factory(this.sat);
            case "scan_index":
                return Array.factory(this.scanIndex);
            case "binned_intensity_values":
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
    /** {@inheritDoc} */
    @Override
    public List<String> getFeatureNames() {
        List<String> superFeatureNames = super.getFeatureNames();
        LinkedList<String> allFeatures = new LinkedList<>(superFeatureNames);
        allFeatures.addAll(Arrays.asList("scan_acquisition_time", "scan_index",
                "binned_intensity_values"));
        return allFeatures;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof PeakNG) {
            return getUniqueId().equals(((PeakNG) o).getUniqueId());
        }
        return false;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(scanIndex);
        out.writeDouble(sat);
        out.writeUTF(name);
        out.writeUTF(peakKey);
        out.writeInt(peakIndex);
        out.writeUTF(association);
        out.writeObject(new SerializableArray(msIntensities));
        out.writeInt(peakId);
        out.writeInt(associationId);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        scanIndex = in.readInt();
        sat = in.readDouble();
        name = in.readUTF();
        peakKey = in.readUTF();
        peakIndex = in.readInt();
        association = in.readUTF();
        msIntensities = ((SerializableArray)in.readObject()).getArray();
        peakId = in.readInt();
        associationId = in.readInt();
    }
}
