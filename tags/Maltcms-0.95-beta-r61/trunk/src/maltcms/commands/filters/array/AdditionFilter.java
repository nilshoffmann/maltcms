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

package maltcms.commands.filters.array;

import maltcms.commands.filters.AElementFilter;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * Add a given double to elements of an array.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class AdditionFilter extends AArrayFilter {

	private AElementFilter aef = null;

	public AdditionFilter(final double add) {
		this.aef = new AElementFilter() {
			public Double apply(final Double d) {
				return d + add;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.ucar.ma2.ArrayFilter#filter(maltcms.ucar.ma2.Array)
	 */
	@Override
	public Array[] apply(final Array[] a) {
		final Array[] b = super.apply(a);
		for (final Array arr : b) {
			final IndexIterator ii = arr.getIndexIteratorFast();
			double next = 0.0d;
			while (ii.hasNext()) {
				next = ii.getDoubleNext();
				ii.setDoubleCurrent(this.aef.apply(next));
			}
		}
		return b;
	}

}
