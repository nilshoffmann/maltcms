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

import java.awt.Color;
import java.io.IOException;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;

import ucar.ma2.ArrayDouble;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tools.EvalTools;

/**
 * Creates a chart with labeled domain axis, displaying a heatmap as
 * backgroundimage.
 *
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public class BHeatMapChart extends AChart<XYBPlot> {

    private String xlabel, ylabel;
    private String label = "";
    private String title = "";
    private String filename = "";
    private ArrayDouble.D1 xaxis = null;
    private ArrayDouble.D1 yaxis = null;

    /**
     * Default constructor.
     *
     * @param bFilename filename of the background image
     * @param xLabel x axis label
     * @param yLabel y axis label
     * @param axis axis ranges
     * @param seriesLabel series label
     */
    public BHeatMapChart(final String bFilename, final String xLabel,
            final String yLabel,
            final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> axis,
            final String seriesLabel) {
        EvalTools.notNull(new Object[]{bFilename, xLabel, yLabel}, this);
        this.filename = bFilename;
        this.xlabel = xLabel;
        this.ylabel = yLabel;
        this.xaxis = axis.getFirst();
        this.yaxis = axis.getSecond();
        this.label = seriesLabel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XYBPlot create() {
        final NumberAxis domain = new NumberAxis(xlabel);
        domain.setLowerMargin(0);
        domain.setUpperMargin(0);
        domain.setAutoRange(false);
        domain.setRange(xaxis.get(0), xaxis.get(xaxis.getShape()[0] - 1));
        final NumberAxis values = new NumberAxis(ylabel);
        values.setLowerMargin(0);
        values.setUpperMargin(0);
        values.setAutoRange(false);
        values.setRange(yaxis.get(0), yaxis.get(yaxis.getShape()[0] - 1));
        XYBPlot xybp = null;
        try {
            xybp = new XYBPlot(this.filename, domain, values);

            final LegendItem litem = new LegendItem(this.label);
            litem.setFillPaint(Color.WHITE);
            final LegendItemCollection items = new LegendItemCollection();
            items.add(litem);
            xybp.setFixedLegendItems(items);

        } catch (final IOException e) {
            e.printStackTrace();
        }
        return xybp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return this.title;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTitle(final String s) {
        this.title = s;
    }
}
