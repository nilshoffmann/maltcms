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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import maltcms.datastructures.array.IFeatureVector;
import ucar.ma2.Array;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
public class DefaultFeatureVector implements IFeatureVector {

	/**
     * 
     */
	private static final long serialVersionUID = -2245293151270164895L;

	private Map<String, Integer> featureToIndex = new HashMap<String, Integer>();

	private List<Array> datalist = new ArrayList<Array>();

	@Override
	public Array getFeature(String name) {
		final int idx = getFeatureIndex(name);
		if (idx >= 0) {
			return this.datalist.get(idx);
		}
		return null;
	}

	private int getFeatureIndex(String name) {
		if (featureToIndex.containsKey(name)) {
			return this.featureToIndex.get(name).intValue();
		}
		return -1;
	}

	public void addFeature(String name, Array a) {
		final int idx = getFeatureIndex(name);
		if (idx >= 0) {
			this.datalist.set(idx, a);
		} else {
			this.featureToIndex.put(name, this.datalist.size());
			this.datalist.add(a);
		}
	}

	@Override
	public List<String> getFeatureNames() {
		final List<String> l = new ArrayList<String>();
		l.addAll(this.featureToIndex.keySet());
		Collections.sort(l);
		return l;
	}

}
