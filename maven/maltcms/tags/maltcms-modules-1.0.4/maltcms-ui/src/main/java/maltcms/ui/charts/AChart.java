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
 * $Id: AChart.java 43 2009-10-16 17:22:55Z nilshoffmann $
 */

package maltcms.ui.charts;

import java.util.Arrays;
import java.util.List;

import maltcms.commands.filters.array.NormalizationFilter;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.plot.Plot;

import ucar.ma2.Array;
import cross.Factory;
import cross.IConfigurable;

/**
 * Abstract base class for Charts of Type T extending Plot of JFreeChart.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 * @param <T extends Plot>
 */
public abstract class AChart<T extends Plot> implements IConfigurable {

	private double yaxis_min = -1;
	private double yaxis_max = -1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
	 * )
	 */
	@Override
	public void configure(final Configuration cfg) {

	}

	public abstract T create();

	public abstract String getTitle();

	/**
	 * @return the yaxis_max
	 */
	public double getYaxis_max() {
		return this.yaxis_max;
	}

	/**
	 * @return the yaxis_min
	 */
	public double getYaxis_min() {
		return this.yaxis_min;
	}

	public List<Array> normalize(final List<Array> c,
	        final String normalization, final boolean normalize_global) {
		final NormalizationFilter nf = new NormalizationFilter(normalization,
		        false, normalize_global);
		nf.configure(Factory.getInstance().getConfiguration());
		final Array[] as = nf.apply(c.toArray(new Array[] {}));
		return Arrays.asList(as);
	}

	public abstract void setTitle(String s);

	/**
	 * @param yaxis_max
	 *            the yaxis_max to set
	 */
	public void setYaxis_max(final double yaxis_max) {
		this.yaxis_max = yaxis_max;
	}

	/**
	 * @param yaxis_min
	 *            the yaxis_min to set
	 */
	public void setYaxis_min(final double yaxis_min) {
		this.yaxis_min = yaxis_min;
	}

}
