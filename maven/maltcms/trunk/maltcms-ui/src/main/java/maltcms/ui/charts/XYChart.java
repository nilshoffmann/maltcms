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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import cross.Factory;
import lombok.extern.slf4j.Slf4j;

/**
 * Chart to display one Variable's data, or additionally combined with domain
 * values.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Slf4j
public class XYChart extends AChart<XYPlot> {

    private final String xaxis, yaxis;
    private final List<String> labels = new ArrayList<String>();
    private List<String> annotations = new ArrayList<String>();
    private final List<Array> arrays = new ArrayList<Array>();
    private final Array[] domains;
    private String title = "";
    private Array annotation_positions_x = null, annotation_positions_y;
    private boolean renderArea = false;
    private float fgAlpha = 1.0f;
    private boolean diffRender = false;
    private boolean logScale = false;

    public XYChart(final String title1, final String[] labels1,
            final Array[] arrays1, final Array[] domains1,
            final Array annotation_positions_x1,
            final Array annotation_positions_y1, final String[] annotations1,
            final String x_axis, final String y_axis) {
        this(title1, labels1, arrays1, domains1, x_axis, y_axis);
        this.annotation_positions_x = annotation_positions_x1;
        this.annotation_positions_y = annotation_positions_y1;
        this.annotations = Arrays.asList(annotations1);
    }

    public XYChart(final String title1, final String[] labels1,
            final Array[] arrays1, final Array[] domains1, final String x_axis,
            final String y_axis, final boolean useLogScale) {
        this.title = title1;
        this.labels.addAll(Arrays.asList(labels1));
        this.arrays.addAll(Arrays.asList(arrays1));
        this.xaxis = x_axis;
        this.yaxis = y_axis;
        this.domains = domains1;
        this.logScale = useLogScale;
    }

    public XYChart(final String title1, final String[] labels1,
            final Array[] arrays1, final Array[] domains1, final String x_axis,
            final String y_axis) {
        this(title1, labels1, arrays1, domains1, x_axis, y_axis, false);
    }

    public XYChart(final String title1, final String[] labels1,
            final Array[] arrays1, final String x_axis, final String y_axis) {
        this(title1, labels1, arrays1, null, x_axis, y_axis);
    }

    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.renderArea = cfg.getBoolean(this.getClass().getName()
                + ".renderArea", false);
        this.fgAlpha = cfg.getFloat(this.getClass().getName()
                + ".foregroundAlpha", 1.0f);
        this.diffRender = cfg.getBoolean(this.getClass().getName()
                + ".differenceRenderer", false);
    }

    @Override
    public XYPlot create() {
        configure(Factory.getInstance().getConfiguration());
        return createXYPlot();
    }

    private XYPlot createXYPlot() {
        final XYSeriesCollection xysc = new XYSeriesCollection();
        for (int k = 0; k < this.arrays.size(); k++) {
            final String key = ((this.labels == null) || this.labels.isEmpty() || (this.labels
                    .get(k) == null)) ? "" + k : this.labels.get(k);
            Array domainK = null;
            if (this.domains != null) {
                if (this.domains.length == 1) {
                    domainK = this.domains[0];
                } else if (k < this.domains.length) {
                    domainK = this.domains[k];
                }
            }
            final XYSeries xs = new XYSeries(key);
            final Index idx = this.arrays.get(k).getIndex();
            // if(domainK!=null) {
            // EvalTools.eqI(arrays.get(k).getShape()[0],domainK.getShape()[0],
            // this);
            // }
            final Array a = this.arrays.get(k);
            for (int j = 0; j < a.getShape()[0]; j++) {
                double domX = j;
                if (domainK != null) {
                    final Index didx = domainK.getIndex();
                    domX = domainK.getDouble(didx.set(j));
                }
                xs.add(domX, a.getDouble(idx.set(j)));
            }
            xysc.addSeries(xs);
        }
        XYItemRenderer dir = null;
        if (this.renderArea) {
            dir = new XYAreaRenderer(XYAreaRenderer.AREA);
        } else if (this.diffRender) {
            dir = new XYDifferenceRenderer();
        } else {
            dir = new XYLineAndShapeRenderer(true, false);
        }
        this.log.info("Using {}", dir.getClass().getName());
        NumberAxis ynumberaxis = null;
        if (this.logScale) {
            ynumberaxis = new LogarithmicAxis(this.yaxis);
        } else {
            ynumberaxis = new NumberAxis(this.yaxis);
        }
        final XYPlot p = new XYPlot(xysc, new NumberAxis(this.xaxis),
                ynumberaxis, dir);
        int labelCounter = 0;
        if ((this.annotation_positions_x != null)
                && (this.annotation_positions_y != null)) {
            final IndexIterator ii = this.annotation_positions_x
                    .getIndexIterator();
            final IndexIterator jj = this.annotation_positions_y
                    .getIndexIterator();
            while (ii.hasNext() && jj.hasNext()) {
                String label = "";
                final double pos = ii.getDoubleNext();
                final double val = jj.getDoubleNext();
                if ((this.annotations != null) && !this.annotations.isEmpty()) {
                    label = this.annotations.get(labelCounter++);
                } else {
                    label = val + "";
                }
                final XYPointerAnnotation ta = new XYPointerAnnotation(label,
                        pos, val, -0.7);
                ta.setTipRadius(0);
                ta.setTextAnchor(TextAnchor.BASELINE_LEFT);

                // if(cross.annotations!=null){
                // vm.setLabel(cross.annotations[i]);
                // }
                p.addAnnotation(ta);
            }
        }
        if ((getYaxis_min() != -1) || (getYaxis_max() != -1)) {
            p.getRangeAxis().setRangeWithMargins(
                    new Range(getYaxis_min(), getYaxis_max()), true, true);
        }
        ((NumberAxis) p.getDomainAxis()).setAutoRangeIncludesZero(false);
        p.setDomainCrosshairVisible(false);
        p.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
        p.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        this.log.info("Using foregroundAlpha={}", this.fgAlpha);
        p.setForegroundAlpha(this.fgAlpha);
        return p;
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
