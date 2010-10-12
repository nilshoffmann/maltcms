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
public class MeanStdDevNormalizationFilter extends AArrayFilter {

	@Configurable
	private double mean = 0;

	@Configurable
	private double stddev = 1;

	public MeanStdDevNormalizationFilter() {
		super();
	}

	public MeanStdDevNormalizationFilter(final double mean, final double stddev) {
		this();
		this.mean = mean;
		this.stddev = stddev;
	}

	@Override
	public Array apply(final Array a) {
		// final Array[] b = super.apply(a);
		final AdditionFilter af = new AdditionFilter(-this.mean);
		// shift by mean
		Array c = af.apply(a);
		// normalize by stddev
		final MultiplicationFilter mf = new MultiplicationFilter(
		        1.0d / (this.stddev));
		c = mf.apply(c);
		return c;
	}

	@Override
	public void configure(final Configuration cfg) {
		super.configure(cfg);
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getStddev() {
		return stddev;
	}

	public void setStddev(double stddev) {
		this.stddev = stddev;
	}

}
