/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
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
 * Will squar all elements.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public class SqrtFilter extends AArrayFilter {

	private AElementFilter aef = null;

	/**
	 * Default constructor.
	 */
	public SqrtFilter() {
		super();
		this.aef = new AElementFilter() {
			public Double apply(final Double d) {
				return Math.sqrt(d);
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Array apply(final Array a) {
		final Array arr = super.apply(a);
		final IndexIterator ii = arr.getIndexIteratorFast();
		while (ii.hasNext()) {
			ii.setDoubleCurrent(this.aef.apply(ii.getDoubleNext()));
		}
		return arr;
	}

}