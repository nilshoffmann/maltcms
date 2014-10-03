/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2013, The authors of Maltcms. All rights reserved.
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
import java.util.List;
import java.util.UUID;
import maltcms.datastructures.peak.IPeak;
import ucar.ma2.Array;

/**
 * <p>IBipacePeak interface.</p>
 *
 * @author Nils Hoffmann
 * 
 * @since 1.3.2
 */
public interface IBipacePeak extends IPeak {

    /**
     * Add a similarity to Peak p. Resets the sortedPeaks list for the
     * associated FileFragment of Peak p, so that a subsequent call to
     * getPeakWithHighestSimilarity or getPeaksSortedBySimilarity will rebuild
     * the list of peaks sorted ascending according to their similarity to this
     * peak.
     *
     * @param p a {@link maltcms.commands.fragments.alignment.peakCliqueAlignment.IBipacePeak} object.
     * @param similarity a double.
     * @param edgeMap a {@link com.carrotsearch.hppc.LongObjectMap} object.
     */
    void addSimilarity(LongObjectMap<PeakEdge> edgeMap, final IBipacePeak p, final double similarity);

    /**
     * <p>clearSimilarities.</p>
     *
     * @param edgeMap a {@link com.carrotsearch.hppc.LongObjectMap} object.
     * @param associationId a int.
     */
    void clearSimilarities(LongObjectMap<PeakEdge> edgeMap, int associationId);

    /**
     * <p>getMsIntensities.</p>
     *
     * @return a {@link ucar.ma2.Array} object.
     */
    Array getMsIntensities();

    /**
     * <p>setMsIntensities.</p>
     *
     * @param a a {@link ucar.ma2.Array} object.
     */
    void setMsIntensities(Array a);

    /**
     * <p>getPeakWithHighestSimilarity.</p>
     *
     * @param edgeMap a {@link com.carrotsearch.hppc.LongObjectMap} object.
     * @param associationId a int.
     * @return a {@link java.util.UUID} object.
     */
    UUID getPeakWithHighestSimilarity(LongObjectMap<PeakEdge> edgeMap, final int associationId);

    /**
     * Only call this method, after having added all similarities!
     *
     * @param associationId a int.
     * @param edgeMap a {@link com.carrotsearch.hppc.LongObjectMap} object.
     * @return a {@link java.util.List} object.
     */
    List<UUID> getPeaksSortedBySimilarity(LongObjectMap<PeakEdge> edgeMap, final int associationId);

    /**
     * <p>getSimilarity.</p>
     *
     * @param edgeMap a {@link com.carrotsearch.hppc.LongObjectMap} object.
     * @param p a {@link maltcms.commands.fragments.alignment.peakCliqueAlignment.IBipacePeak} object.
     * @return a double.
     */
    double getSimilarity(LongObjectMap<PeakEdge> edgeMap, final IBipacePeak p);

    /**
     * <p>isBidiBestHitFor.</p>
     *
     * @param edgeMap a {@link com.carrotsearch.hppc.LongObjectMap} object.
     * @param p a {@link maltcms.commands.fragments.alignment.peakCliqueAlignment.IBipacePeak} object.
     * @return a boolean.
     */
    boolean isBidiBestHitFor(LongObjectMap<PeakEdge> edgeMap, final IBipacePeak p);

    /**
     * <p>retainSimilarityRemoveRest.</p>
     *
     * @param edgeMap a {@link com.carrotsearch.hppc.LongObjectMap} object.
     * @param p a {@link maltcms.commands.fragments.alignment.peakCliqueAlignment.IBipacePeak} object.
     */
    void retainSimilarityRemoveRest(LongObjectMap<PeakEdge> edgeMap, final IBipacePeak p);

    /**
     * This is a sequential id of the peak in the order the peak objects were
     * created. Do not use this for unequivocal identification. Use
     * getUniqueId() instead.
     *
     * @return a int.
     */
    public int getPeakId();

    /**
     * <p>getAssociationId.</p>
     *
     * @return a int.
     */
    public int getAssociationId();

    /**
     * <p>keyTo.</p>
     *
     * @param p a {@link maltcms.commands.fragments.alignment.peakCliqueAlignment.IBipacePeak} object.
     * @return a long.
     */
    public long keyTo(IBipacePeak p);

    /**
     * <p>keyTo.</p>
     *
     * @param associationId a int.
     * @return a long.
     */
    public long keyTo(int associationId);

}
