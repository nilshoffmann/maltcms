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
package maltcms.datastructures.feature;

import java.util.HashSet;
import java.util.List;

import maltcms.datastructures.array.IFeatureVector;

import org.apache.commons.configuration.Configuration;

import cross.datastructures.fragments.IFileFragment;
import cross.exception.NotImplementedException;
import maltcms.datastructures.IFileFragmentModifier;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class FeatureVectorList<T extends IFeatureVector> implements
        IFileFragmentModifier {

	private final List<T> l;

	private final List<String> featureNames;

	public FeatureVectorList(List<T> l, List<String> featureNames) {
		this.l = l;
		this.featureNames = featureNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.experimental.datastructures.IFileFragmentModifier#modify(cross
	 * .datastructures.fragments.IFileFragment)
	 */
	@Override
	public void modify(IFileFragment iff) {
		HashSet<String> features = new HashSet<String>();
		for (String name : this.featureNames) {
			for (T t : l) {

			}
		}
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
	 * )
	 */
	@Override
	public void configure(Configuration cfg) {
		// TODO Auto-generated method stub

	}

}
