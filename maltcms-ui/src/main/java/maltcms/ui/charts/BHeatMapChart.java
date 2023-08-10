/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.ui.charts;

import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import java.awt.Color;
import java.io.IOException;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

/**
 * Creates a chart with labeled domain axis, displaying a heatmap as
 * backgroundimage.
 *
 * @author Mathias Wilhelm
 * 
 */

public class BHeatMapChart extends AChart<XYBPlot> {

    private static Logger log = LoggerFactory.getLogger(BHeatMapChart.class);

    private String xlabel, ylabel;
    private String label = "";
    private String title = "";
    private String filename = "";
    private Array xaxis = null;
    private Array yaxis = null;

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
            final Tuple2D<Array, Array> axis,
            final String seriesLabel) {
        EvalTools.notNull(new Object[]{bFilename, xLabel, yLabel}, this);
        this.filename = bFilename;
        this.xlabel = xLabel;
        this.ylabel = yLabel;
        this.xaxis = axis.getFirst();
        this.yaxis = axis.getSecond();
        this.label = seriesLabel;
    }

    /** {@inheritDoc} */
    @Override
    public XYBPlot create() {
        final NumberAxis domain = new NumberAxis(xlabel);
        domain.setLowerMargin(0);
        domain.setUpperMargin(0);
        domain.setAutoRange(false);
        domain.setRange(xaxis.getDouble(0), xaxis.getDouble(xaxis.getShape()[0] - 1));
        final NumberAxis values = new NumberAxis(ylabel);
        values.setLowerMargin(0);
        values.setUpperMargin(0);
        values.setAutoRange(false);
        values.setRange(yaxis.getDouble(0), yaxis.getDouble(yaxis.getShape()[0] - 1));
        XYBPlot xybp = null;
        try {
            xybp = new XYBPlot(this.filename, domain, values);

            final LegendItem litem = new LegendItem(this.label);
            litem.setFillPaint(Color.WHITE);
            final LegendItemCollection items = new LegendItemCollection();
            items.add(litem);
            xybp.setFixedLegendItems(items);

        } catch (final IOException e) {
            log.warn(e.getLocalizedMessage());
        }
        return xybp;
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return this.title;
    }

    /** {@inheritDoc} */
    @Override
    public void setTitle(final String s) {
        this.title = s;
    }
}
