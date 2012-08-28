/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.ui.charts;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;

/**
 * Combines Different plots (favorably XYPlots) to share a common domain, such
 * that they are vertically stacked (default).
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
public class CombinedDomainXYChart extends AChart<XYPlot> {

    private String title = "";
    private String x_axis = "";
    private List<XYPlot> subplots = new ArrayList<XYPlot>();
    private CombinedDomainXYPlot cdxyp = null;
    private int gap = 10;

    public CombinedDomainXYChart(final String title1,
            final String domain_axis_name, final boolean headless,
            final List<XYPlot> subplots1) {
        this.title = title1;
        this.x_axis = domain_axis_name;
        this.subplots = subplots1;
    }

    @Override
    public XYPlot create() {
        this.cdxyp = new CombinedDomainXYPlot(new NumberAxis(this.x_axis));
        this.cdxyp.setGap(this.gap);
        for (final XYPlot p : this.subplots) {
            // p.setDomainAxis(null);
            // p.getDomainAxis().setLowerMargin(0.0d);
            // p.getDomainAxis().setUpperMargin(0.0d);
            // p.getRangeAxis().setLowerMargin(0.0d);
            // p.getRangeAxis().setUpperMargin(0.0d);
            this.cdxyp.add(p, p.getWeight());
        }

        // this.cdxyp.setDomainCrosshairVisible(true);
        // this.cdxyp.setRangeCrosshairLockedOnData(true);
        // this.cdxyp.setRangeCrosshairVisible(true);
        this.cdxyp.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
        this.cdxyp.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        return this.cdxyp;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public void setGap(final int i) {
        this.gap = i;
    }

    @Override
    public void setTitle(final String s) {
        this.title = s;
    }
}
