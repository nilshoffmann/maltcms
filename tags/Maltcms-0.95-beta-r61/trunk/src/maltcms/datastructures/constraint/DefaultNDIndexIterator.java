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

package maltcms.datastructures.constraint;

/**
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class DefaultNDIndexIterator implements INDIndexIterator {

	public static final DefaultNDIndexIterator newInstance(final int[] start,
	        final int[] end, final int[] stride) {
		final DefaultNDIndexIterator dii = new DefaultNDIndexIterator();
		dii.setStart(start);
		dii.setEnd(end);
		dii.setStride(stride);
		return dii;
	}

	private int[] start = null, end = null, incr = null;

	public DefaultNDIndexIterator() {

	}

	@Override
	public int end(final int dim) {
		return this.end[dim];
	}

	@Override
	public int incr(final int dim) {
		return this.incr[dim];
	}

	@Override
	public void next() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	private void setEnd(final int[] end1) {
		this.end = end1.clone();

	}

	private void setStart(final int[] start1) {
		this.start = start1.clone();
	}

	private void setStride(final int[] stride) {
		this.incr = stride.clone();
	}

	@Override
	public int start(final int dim) {
		return this.start[dim];
	}

}
