/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */
package maltcms.experimental.operations;

import java.util.List;

import maltcms.datastructures.array.IFeatureVector;
import maltcms.experimental.datastructures.IFileFragmentModifier;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
public interface IPairwiseFeatureVectorSequenceOperation<RESULT> extends
        IFileFragmentModifier {

	public abstract RESULT apply(List<IFeatureVector> l1,
	        List<IFeatureVector> l2);

	public abstract void setPairwiseFeatureVectorOperation(
	        TwoFeatureVectorOperation pao);

	public TwoFeatureVectorOperation getPairwiseFeatureVectorOperation();

}
