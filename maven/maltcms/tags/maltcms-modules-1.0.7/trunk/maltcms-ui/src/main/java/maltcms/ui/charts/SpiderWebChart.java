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
 * $Id: SpiderWebChart.java 43 2009-10-16 17:22:55Z nilshoffmann $
 */

package maltcms.ui.charts;

import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import cross.datastructures.tools.EvalTools;

/**
 * Chart displaying a plot of different variables organized as a spider's web.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class SpiderWebChart extends AChart<SpiderWebPlot> {

	public static CategoryDataset createCategoryDataset(
	        final String[] collabels, final String[] rowlabels,
	        final double[][] data) {
		final DefaultCategoryDataset cd = new DefaultCategoryDataset();
		EvalTools.eqI(collabels.length, data[0].length, cd);
		EvalTools.eqI(rowlabels.length, data.length, cd);
		for (int i = 0; i < collabels.length; i++) {
			for (int j = 0; j < rowlabels.length; j++) {
				cd.addValue(data[j][i], rowlabels[j], collabels[i]);
			}
		}
		return cd;
	}

	private String title = "";

	private CategoryDataset cd = null;

	public SpiderWebChart(final String title1, final CategoryDataset cd1) {
		this.title = title1;
		this.cd = cd1;
	}

	public SpiderWebChart(final String title1, final String[] collabels,
	        final String[] rowlabels, final double[][] data) {
		this(title1, SpiderWebChart.createCategoryDataset(collabels, rowlabels,
		        data));
	}

	@Override
	public SpiderWebPlot create() {
		final SpiderWebPlot swp = new SpiderWebPlot(this.cd);
		return swp;
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
