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
 * @author Nils Hoffmann
 * @version $Id: $Id
 */
public class CombinedDomainXYChart extends AChart<XYPlot> {

    private String title = "";
    private String x_axis = "";
    private List<XYPlot> subplots = new ArrayList<>();
    private CombinedDomainXYPlot cdxyp = null;
    private int gap = 10;

    /**
     * <p>Constructor for CombinedDomainXYChart.</p>
     *
     * @param title1 a {@link java.lang.String} object.
     * @param domain_axis_name a {@link java.lang.String} object.
     * @param headless a boolean.
     * @param subplots1 a {@link java.util.List} object.
     */
    public CombinedDomainXYChart(final String title1,
            final String domain_axis_name, final boolean headless,
            final List<XYPlot> subplots1) {
        this.title = title1;
        this.x_axis = domain_axis_name;
        this.subplots = subplots1;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return this.title;
    }

    /**
     * <p>Setter for the field <code>gap</code>.</p>
     *
     * @param i a int.
     */
    public void setGap(final int i) {
        this.gap = i;
    }

    /** {@inheritDoc} */
    @Override
    public void setTitle(final String s) {
        this.title = s;
    }
}
