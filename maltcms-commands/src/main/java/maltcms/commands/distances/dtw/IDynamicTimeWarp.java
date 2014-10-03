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
package maltcms.commands.distances.dtw;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import java.util.List;
import maltcms.commands.distances.DtwRecurrence;
import maltcms.commands.distances.PairwiseFeatureSequenceSimilarity;
import maltcms.commands.distances.PairwiseFeatureSimilarity;
import maltcms.datastructures.alignment.AnchorPairSet;
import maltcms.datastructures.array.IArrayD2Double;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 * Refinement of PairwiseFeatureSequenceSimilarity, adding necessary methods for
 * alignment with DTW.
 *
 * @author Nils Hoffmann
 * 
 */
public interface IDynamicTimeWarp extends PairwiseFeatureSequenceSimilarity {

    /**
     * <p>align.</p>
     *
     * @param tuple a {@link cross.datastructures.tuple.Tuple2D} object.
     * @param ris a {@link maltcms.datastructures.alignment.AnchorPairSet} object.
     * @param maxdev a double.
     * @param sat_ref scan_acquisition_time array for reference
     * @param sat_query scan_acquisition_time array for query
     * @return a {@link maltcms.datastructures.array.IArrayD2Double} object.
     */
    public abstract IArrayD2Double align(
            Tuple2D<List<Array>, List<Array>> tuple, AnchorPairSet ris,
            double maxdev, ArrayDouble.D1 sat_ref, ArrayDouble.D1 sat_query);

    /**
     * <p>createTuple.</p>
     *
     * @param t a {@link cross.datastructures.tuple.Tuple2D} object.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public Tuple2D<List<Array>, List<Array>> createTuple(
            Tuple2D<IFileFragment, IFileFragment> t);

    /**
     * <p>getRecurrence.</p>
     *
     * @return a {@link maltcms.commands.distances.DtwRecurrence} object.
     */
    public abstract DtwRecurrence getRecurrence();

    /**
     * <p>getPairwiseFeatureSimilarity.</p>
     *
     * @return a {@link maltcms.commands.distances.PairwiseFeatureSimilarity} object.
     */
    public abstract PairwiseFeatureSimilarity getPairwiseFeatureSimilarity();

    /**
     * <p>setRecurrence.</p>
     *
     * @param cd a {@link maltcms.commands.distances.DtwRecurrence} object.
     */
    public abstract void setRecurrence(DtwRecurrence cd);

    /**
     * <p>setFileFragments.</p>
     *
     * @param a a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param b a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public abstract void setFileFragments(IFileFragment a, IFileFragment b);

    /**
     * <p>setPairwiseFeatureSimilarity.</p>
     *
     * @param psd a {@link maltcms.commands.distances.PairwiseFeatureSimilarity} object.
     */
    public abstract void setPairwiseFeatureSimilarity(PairwiseFeatureSimilarity psd);
}
