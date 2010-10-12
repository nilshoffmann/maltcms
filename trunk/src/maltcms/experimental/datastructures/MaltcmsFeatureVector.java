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
package maltcms.experimental.datastructures;

import java.util.ArrayList;
import java.util.List;

import maltcms.datastructures.array.IFeatureVector;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;

/**
 * FeatureVector implementation, which directly accesses the IFileFragment
 * required at construction time.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public class MaltcmsFeatureVector implements IFeatureVector {

	/**
     * 
     */
	private static final long serialVersionUID = 1423070246312970401L;
	private IFileFragment iff = null;
	private int index = -1;

	public void addFeatures(IFileFragment iff, int i) {
		this.iff = iff;
		this.index = i;
	}

	@Override
	public Array getFeature(String name) {
		if (this.iff.getChild(name).getIndex() == null) {
			Array a = this.iff.getChild(name).getArray();
			try {
				return a.section(new int[] { this.index },
				        new int[] { this.index });
			} catch (InvalidRangeException ex) {
				System.err.println(ex.getLocalizedMessage());
			}
		}
		return this.iff.getChild(name).getIndexedArray().get(this.index);
	}

	@Override
	public List<String> getFeatureNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (IVariableFragment ivf : this.iff) {
			names.add(ivf.getVarname());
		}
		return names;
	}

}
