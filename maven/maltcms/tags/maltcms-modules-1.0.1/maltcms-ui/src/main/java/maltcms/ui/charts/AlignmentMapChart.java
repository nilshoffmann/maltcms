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
 * $Id: AlignmentMapChart.java 43 2009-10-16 17:22:55Z nilshoffmann $
 */

package maltcms.ui.charts;

import java.util.List;

import maltcms.ui.renderer.LineVectorRenderer;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.VectorRenderer;
import org.jfree.data.xy.VectorSeries;
import org.jfree.data.xy.VectorSeriesCollection;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.Index;
import cross.Logging;
import cross.datastructures.tuple.Tuple2DI;

/**
 * Creates a Plot displaying the mapping between two aligned time series.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class AlignmentMapChart extends AChart<XYPlot> {

	private List<Tuple2DI> map = null;

	private int height = 100;

	private String title = "";

	private Array domain1 = null, domain2 = null;

	private final String x_label = "Scan";

	private final Logger log = Logging.getLogger(this);

	private int stride = 1;

	public AlignmentMapChart(final List<Tuple2DI> map1, final Array domain1,
	        final Array domain2, final String x_label, final int height1,
	        final int stride1) {
		this.map = map1;
		this.height = height1;
		this.stride = stride1;
		this.domain1 = domain1;
		this.domain2 = domain2;
	}

	@Override
	public XYPlot create() {
		final VectorSeries vs = new VectorSeries("Map");
		final int mod = this.stride;
		int cnt = 0;
		for (final Tuple2DI t : this.map) {
			if ((cnt % mod == 0) || (cnt == 0)
			        || (cnt == (this.map.size() - 1))) {
				double x = t.getSecond();
				double dx = t.getFirst() - t.getSecond();
				if ((this.domain1 != null) && (this.domain2 != null)) {
					final Index id1 = this.domain1.getIndex();
					final Index id2 = this.domain2.getIndex();

					x = this.domain2.getDouble(id2.set(t.getSecond()));
					dx = this.domain1.getDouble(id1.set(t.getFirst())) - x;
					this.log.debug("domain1 = {}, domain2 = {}", this.domain1
					        .getShape()[0], this.domain2.getShape()[0]);
					this.log.debug("i={}, j={}, x={},dx={}", new Object[] {
					        t.getFirst(), t.getSecond(), x, dx });
				}
				vs.add(x, 0.0d, dx, this.height);
			}
			cnt++;
		}
		final VectorSeriesCollection xyd = new VectorSeriesCollection();
		xyd.addSeries(vs);
		final VectorRenderer vr = new LineVectorRenderer();
		final NumberAxis scans = new NumberAxis(this.x_label);
		scans.setVisible(false);
		final NumberAxis matches = new NumberAxis("Matches");
		matches.setAutoRange(false);
		matches.setRange(0.0d, this.height);
		matches.setVisible(false);
		final XYPlot xyp = new XYPlot(xyd, scans, matches, vr);
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
