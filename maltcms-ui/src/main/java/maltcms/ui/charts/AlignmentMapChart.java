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

import cross.datastructures.tuple.Tuple2DI;
import java.util.List;

import maltcms.ui.renderer.LineVectorRenderer;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.VectorRenderer;
import org.jfree.data.xy.VectorSeries;
import org.jfree.data.xy.VectorSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.Index;

/**
 * Creates a Plot displaying the mapping between two aligned time series.
 *
 * @author Nils Hoffmann
 * 
 */

public class AlignmentMapChart extends AChart<XYPlot> {
    
    private static Logger log = LoggerFactory.getLogger(AlignmentMapChart.class);

    private List<Tuple2DI> map = null;
    private int height = 100;
    private String title = "";
    private Array domain1 = null, domain2 = null;
    private final String x_label = "Scan";
    private int stride = 1;

    /**
     * <p>Constructor for AlignmentMapChart.</p>
     *
     * @param map1 a {@link java.util.List} object.
     * @param domain1 a {@link ucar.ma2.Array} object.
     * @param domain2 a {@link ucar.ma2.Array} object.
     * @param x_label a {@link java.lang.String} object.
     * @param height1 a int.
     * @param stride1 a int.
     */
    public AlignmentMapChart(final List<Tuple2DI> map1, final Array domain1,
            final Array domain2, final String x_label, final int height1,
            final int stride1) {
        this.map = map1;
        this.height = height1;
        this.stride = stride1;
        this.domain1 = domain1;
        this.domain2 = domain2;
    }

    /** {@inheritDoc} */
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
                    this.log.debug("i={}, j={}, x={},dx={}", new Object[]{
                        t.getFirst(), t.getSecond(), x, dx});
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
