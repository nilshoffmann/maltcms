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
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Chart displaying a plot of different variables organized as a spider's web.
 *
 * @author Nils Hoffmann
 * @version $Id: $Id
 */
public class SpiderWebChart extends AChart<SpiderWebPlot> {

    /**
     * <p>createCategoryDataset.</p>
     *
     * @param collabels an array of {@link java.lang.String} objects.
     * @param rowlabels an array of {@link java.lang.String} objects.
     * @param data an array of double.
     * @return a {@link org.jfree.data.category.CategoryDataset} object.
     */
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

    /**
     * <p>Constructor for SpiderWebChart.</p>
     *
     * @param title1 a {@link java.lang.String} object.
     * @param cd1 a {@link org.jfree.data.category.CategoryDataset} object.
     */
    public SpiderWebChart(final String title1, final CategoryDataset cd1) {
        this.title = title1;
        this.cd = cd1;
    }

    /**
     * <p>Constructor for SpiderWebChart.</p>
     *
     * @param title1 a {@link java.lang.String} object.
     * @param collabels an array of {@link java.lang.String} objects.
     * @param rowlabels an array of {@link java.lang.String} objects.
     * @param data an array of double.
     */
    public SpiderWebChart(final String title1, final String[] collabels,
            final String[] rowlabels, final double[][] data) {
        this(title1, SpiderWebChart.createCategoryDataset(collabels, rowlabels,
                data));
    }

    /** {@inheritDoc} */
    @Override
    public SpiderWebPlot create() {
        final SpiderWebPlot swp = new SpiderWebPlot(this.cd);
        return swp;
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
