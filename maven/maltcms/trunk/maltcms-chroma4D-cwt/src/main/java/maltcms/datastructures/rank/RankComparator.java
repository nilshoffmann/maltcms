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
package maltcms.datastructures.rank;

import java.util.Comparator;

import maltcms.datastructures.ridge.Ridge;

public class RankComparator implements Comparator<Rank<Ridge>> {

	private final String feature;
	
	public RankComparator(String feature) {
		this.feature = feature;
	}
	
	@Override
	public int compare(Rank<Ridge> o1, Rank<Ridge> o2) {
		double d1 = o1.getRank(this.feature);
		double d2 = o2.getRank(this.feature);
		return Double.compare(d1, d2);
	}

}
