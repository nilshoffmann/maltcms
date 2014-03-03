/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.ui.renderer;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.VectorRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.VectorXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 * Extension of default VectorRenderer, omitting heads / tails of vectors.
 *
 * @author Nils Hoffmann
 *
 */
public class LineVectorRenderer extends VectorRenderer {

    /**
     *
     */
    private static final long serialVersionUID = 7297064254819024794L;

    @Override
    public void drawItem(final Graphics2D g2, final XYItemRendererState state,
        final Rectangle2D dataArea, final PlotRenderingInfo info,
        final XYPlot plot, final ValueAxis domainAxis,
        final ValueAxis rangeAxis, final XYDataset dataset,
        final int series, final int item,
        final CrosshairState crosshairState, final int pass) {

        final double x = dataset.getXValue(series, item);
        final double y = dataset.getYValue(series, item);
        double dx = 0.0;
        double dy = 0.0;
        if (dataset instanceof VectorXYDataset) {
            dx = ((VectorXYDataset) dataset).getVectorXValue(series, item);
            dy = ((VectorXYDataset) dataset).getVectorYValue(series, item);
        }
        final double xx0 = domainAxis.valueToJava2D(x, dataArea, plot
            .getDomainAxisEdge());
        final double yy0 = rangeAxis.valueToJava2D(y, dataArea, plot
            .getRangeAxisEdge());
        final double xx1 = domainAxis.valueToJava2D(x + dx, dataArea, plot
            .getDomainAxisEdge());
        final double yy1 = rangeAxis.valueToJava2D(y + dy, dataArea, plot
            .getRangeAxisEdge());
        Line2D line;
        final PlotOrientation orientation = plot.getOrientation();
        if (orientation.equals(PlotOrientation.HORIZONTAL)) {
            line = new Line2D.Double(yy0, xx0, yy1, xx1);
        } else {
            line = new Line2D.Double(xx0, yy0, xx1, yy1);
        }
        g2.setPaint(getItemPaint(series, item));
        g2.setStroke(getItemStroke(series, item));
        g2.draw(line);
    }
}
