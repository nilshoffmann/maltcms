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
package cross.datastructures.tuple;

import java.util.Comparator;

/**
 * Comparator which can be used to sort collections of Tuple2D<Double,Double>
 * based on the first element of the tuple.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 * 
 */
public class Tuple2DDoubleComp implements Comparator<Tuple2D<Double, Double>> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(final Tuple2D<Double, Double> o1,
	        final Tuple2D<Double, Double> o2) {
		if (o1.getFirst() > o2.getFirst()) {
			return 1;
		} else if (o1.getFirst() < o2.getFirst()) {
			return -1;
		}
		return 0;
	}

}
