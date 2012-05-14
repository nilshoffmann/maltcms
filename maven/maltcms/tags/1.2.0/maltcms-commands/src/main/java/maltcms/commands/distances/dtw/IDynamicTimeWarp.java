/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.commands.distances.dtw;

import java.util.List;

import maltcms.commands.distances.DtwRecurrence;
import maltcms.commands.distances.PairwiseFeatureSequenceSimilarity;
import maltcms.commands.distances.PairwiseFeatureSimilarity;
import maltcms.datastructures.alignment.AnchorPairSet;
import maltcms.datastructures.array.IArrayD2Double;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;

/**
 * Refinement of PairwiseFeatureSequenceSimilarity, adding necessary methods for alignment
 * with DTW.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IDynamicTimeWarp extends PairwiseFeatureSequenceSimilarity {

    /**
     * 
     * @param tuple
     * @param ris
     * @param maxdev
     * @param sat_ref
     *            scan_acquisition_time array for reference
     * @param sat_query
     *            scan_acquisition_time array for query
     * @return
     */
    public abstract IArrayD2Double align(
            Tuple2D<List<Array>, List<Array>> tuple, AnchorPairSet ris,
            double maxdev, ArrayDouble.D1 sat_ref, ArrayDouble.D1 sat_query);

    public Tuple2D<List<Array>, List<Array>> createTuple(
            Tuple2D<IFileFragment, IFileFragment> t);

    public abstract DtwRecurrence getRecurrence();

    public abstract PairwiseFeatureSimilarity getPairwiseFeatureSimilarity();

    public abstract void setRecurrence(DtwRecurrence cd);

    public abstract void setFileFragments(IFileFragment a, IFileFragment b);

    public abstract void setPairwiseFeatureSimilarity(PairwiseFeatureSimilarity psd);
}