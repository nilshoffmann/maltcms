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
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import ucar.ma2.Array;
import ucar.ma2.Index;

/**
 * Heatmap like chart, using to variable's values as indices for a third
 * variable's value.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class XYZChart extends AChart<XYPlot> {

    private Array x = null;
    private ArrayList<Array> y = null;
    private ArrayList<Array> z = null;
    private final String name = "";
    private String xaxis = "";
    private String yaxis = "";
    private String title = "";

    public XYZChart(final String title1, final String name1,
            final String xaxis1, final String yaxis1, final Array x1,
            final ArrayList<Array> y1, final ArrayList<Array> z1) {
        EvalTools.eqI(y1.size(), z1.size(), this);
        this.x = x1;
        this.y = y1;
        this.z = z1;
        this.title = title1;
        this.xaxis = xaxis1;
        this.yaxis = yaxis1;
    }

    @Override
    public XYPlot create() {
        final DefaultXYZDataset data = new DefaultXYZDataset();
        final int elements = this.y.size() * this.x.getShape()[0];
        final double[][] d = new double[3][elements];
        final Index xi = this.x.getIndex();
        // over all scans
        double u = 0.0d, v = 0.0d, w = 0.0d;
        int cnt = 0;
        this.log.info("Creating dataset: {}x{}", this.x.getShape(), this.y
                .size());
        int zerocnt = 0;
        for (int i = 0; i < this.x.getShape()[0]; i++) {
            this.log.info("Processing scan {} of {}", i,
                    this.x.getShape()[0] - 1);
            u = this.x.getDouble(xi.set(i));
            final Array mzs = this.y.get(i);
            final Array ints = this.z.get(i);
            final Index mzsi = mzs.getIndex();
            final Index intsi = ints.getIndex();
            this.log.info("{}", ints.getShape()[0]);
            for (int k = 0; k < mzs.getShape()[0]; k++, cnt++) {
                v = mzs.getDouble(mzsi.set(k));
                w = mzs.getDouble(intsi.set(k));
                if (w != 0.0d) {
                    d[0][cnt] = u;
                    d[1][cnt] = v;
                    d[2][cnt] = w;
                } else {
                    zerocnt++;
                }
            }
        }
        this.log.info("{}  points with zero intensity skipped", zerocnt);
        data.addSeries(this.name, d);
        final XYBlockRenderer r = new XYBlockRenderer();
        final XYPlot p = new XYPlot(data, new NumberAxis(this.xaxis),
                new NumberAxis(this.yaxis), r);
        p.setDomainCrosshairVisible(false);
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
