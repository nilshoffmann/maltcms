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

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.annotations.Configurable;

/**
 * Normalize all values of an array given a normalization string.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class MinMaxNormalizationFilter extends AArrayFilter {

	@Configurable
	private double min = 0;

	@Configurable
	private double max = 1;

	public MinMaxNormalizationFilter() {
		super();
	}

	public MinMaxNormalizationFilter(final double min, final double max) {
		this();
		this.min = min;
		this.max = max;
	}

	@Override
	public Array apply(final Array a) {
		// final Array[] b = super.apply(a);
		final AdditionFilter af = new AdditionFilter(-this.min);
		// shift by minimum
		Array c = af.apply(a);
		// normalize by max-min
		final MultiplicationFilter mf = new MultiplicationFilter(
		        1.0d / (this.max - this.min));
		c = mf.apply(c);
		return c;
	}

	@Override
	public void configure(final Configuration cfg) {
		super.configure(cfg);
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

}