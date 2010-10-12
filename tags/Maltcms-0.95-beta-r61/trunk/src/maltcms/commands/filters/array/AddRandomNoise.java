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

import java.util.Random;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * Add additive gaussian noise to array values with given mean and std
 * deviation.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class AddRandomNoise extends AArrayFilter {

	private final Random rg;

	private double mean = 0.0d, stddev = 1.0d;

	public AddRandomNoise(final double mean1, final double stddev1) {
		super();
		this.rg = new Random();
		this.mean = mean1;
		this.stddev = stddev1;
	}

	@Override
	public Array[] apply(final Array[] a) {
		final Array[] b = super.apply(a);
		for (final Array arr : b) {
			final IndexIterator ii = arr.getIndexIteratorFast();
			while (ii.hasNext()) {
				final double v = ii.getDoubleNext();
				ii.setDoubleCurrent(v + (this.stddev * this.rg.nextGaussian())
				        + this.mean);
			}
		}
		return b;
	}

}
