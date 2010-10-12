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
package maltcms.commands.distances;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;

/**
 * Implementation of normalized Euclidean distance.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public class ArrayVarNormLp implements IArrayDoubleComp {

	private ArrayDouble.D1 variance;
	private double max = 1.0d;

	/**
	 * Default constructor.
	 */
	public ArrayVarNormLp() {
		this.variance = new ArrayDouble.D1(750);
		final IndexIterator iter = this.variance.getIndexIterator();
		while (iter.hasNext()) {
			iter.setDoubleNext(1.0d);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double apply(final int i1, final int i2, final double time1,
			final double time2, final Array t1, final Array t2) {

		Double cost = Double.MAX_VALUE;
		if (MAMath.conformable(t1, t2) && MAMath.conformable(t1, this.variance)) {
			double sum = 0.0d;
			final IndexIterator iter1 = t1.getIndexIterator(), iter2 = t2
					.getIndexIterator(), var = this.variance.getIndexIterator();
			while (iter1.hasNext() && iter2.hasNext() && var.hasNext()) {
				sum += Math.pow(iter1.getDoubleNext() - iter2.getDoubleNext(),
						2.0d)
						/ (var.getDoubleNext() + 1);
			}
			cost = Math.sqrt(sum);
		}

		return cost;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCompressionWeight() {
		return 1.0d;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getDiagonalWeight() {
		return 1.0d;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getExpansionWeight() {
		return 1.0d;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean minimize() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Configuration cfg) {
	}

	/**
	 * Setter.
	 * 
	 * @param varianceArray
	 *            variance for each mass bin
	 */
	public void setVarianceArray(final ArrayDouble.D1 varianceArray) {
		this.variance = (ArrayDouble.D1) varianceArray.copy();
		for (int i = 0; i < this.variance.getShape()[0]; i++) {
			if (this.max < this.variance.get(i)) {
				this.max = this.variance.get(i);
			}
		}
	}

}
