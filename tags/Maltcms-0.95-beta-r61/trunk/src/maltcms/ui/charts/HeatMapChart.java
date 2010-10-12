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

package maltcms.ui.charts;

import java.awt.image.BufferedImage;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;

import ucar.ma2.ArrayDouble;
import cross.datastructures.tuple.Tuple2D;
import cross.tools.EvalTools;

/**
 * Creates a chart with labeled domain axis, displaying a heatmap.
 * 
 * @author Nils.Hoffman@cebitec.uni-bielefeld.de
 * 
 */
public class HeatMapChart extends AChart<XYPlot> {

	private BufferedImage hm = null;
	// private Array xdom, ydom;
	private final String xlabel, ylabel;
	private String label = "";
	private String title = "";
	private ArrayDouble.D1 xaxis = null;
	private ArrayDouble.D1 yaxis = null;

	public HeatMapChart(final BufferedImage heatMap, final String x_label,
	        final String y_label,
	        final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> axis,
	        final String seriesLabel) {
		EvalTools.notNull(new Object[] { heatMap, x_label, y_label }, this);
		this.hm = heatMap;
		// this.xdom = x_domain;
		// this.ydom = y_domain;
		this.xlabel = x_label;
		this.ylabel = y_label;
		this.xaxis = axis.getFirst();
		this.yaxis = axis.getSecond();
		this.label = seriesLabel;
	}

	@Override
	public XYPlot create() {
		final XYPlot xyp = new XYPlot();
		// DefaultXYDataset dxyd = new DefaultXYDataset();
		// dxyd.addSeries("", new double[][] { { 0, this.hm.getWidth() },
		// { 0, this.hm.getHeight() } });
		xyp.setBackgroundImage(this.hm);
		xyp.setBackgroundImageAlpha(1.0f);
		xyp.setBackgroundAlpha(1.0f);
		final NumberAxis domain = new NumberAxis(this.xlabel);
		domain.setLowerMargin(0);
		domain.setUpperMargin(0);
		domain.setAutoRange(false);
		domain.setRange(this.xaxis.get(0), this.xaxis
		        .get(this.xaxis.getShape()[0] - 1));
		final NumberAxis values = new NumberAxis(this.ylabel);
		values.setLowerMargin(0);
		values.setUpperMargin(0);
		values.setAutoRange(false);
		values.setRange(this.yaxis.get(0), this.yaxis
		        .get(this.yaxis.getShape()[0] - 1));
		xyp.setDomainAxis(domain);
		xyp.setRangeAxis(values);
		// xyp.setDataset(dxyd);
		return xyp;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public void setTitle(final String s) {
		this.title = s;
	}

}
