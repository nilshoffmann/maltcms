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

import cross.Factory;
import cross.IConfigurable;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import maltcms.commands.filters.array.NormalizationFilter;
import org.apache.commons.configuration.Configuration;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import ucar.ma2.Array;

/**
 * Abstract base class for Charts of Type T extending Plot of JFreeChart.
 *
 * @author Nils Hoffmann
 * @param <T extends Plot>
 * @version $Id: $Id
 */
public abstract class AChart<T extends Plot> implements IConfigurable {

    private double yaxis_min = -1;
    private double yaxis_max = -1;
    private Color[] seriesColors;
    private static final Color[] baseColors = new Color[]{
            new Color(166, 206, 227),
            new Color(178, 223, 138),
            new Color(251, 154, 153),
            new Color(253, 191, 111),
            new Color(202, 178, 214),
            new Color(31, 120, 180),
            new Color(51, 160, 44),
            new Color(227, 26, 28),
            new Color(255, 127, 0),
            new Color(106, 61, 154)};

    /**
     * <p>Constructor for AChart.</p>
     */
    public AChart() {
        int cnt = 0;
        seriesColors = new Color[2 * baseColors.length];
        for (Color c : baseColors) {
            seriesColors[cnt] = c.darker();
            seriesColors[baseColors.length + cnt] = c;
            cnt++;
        }
    }
    
    /*
     * (non-Javadoc)
     *
     * @see
     * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
     * )
     */
    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
    }

    /**
     * <p>create.</p>
     *
     * @return a T object.
     */
    public abstract T create();

    /**
     * <p>getTitle.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getTitle();

    /**
     * <p>Getter for the field <code>yaxis_max</code>.</p>
     *
     * @return the yaxis_max
     */
    public double getYaxis_max() {
        return this.yaxis_max;
    }

    /**
     * <p>Getter for the field <code>yaxis_min</code>.</p>
     *
     * @return the yaxis_min
     */
    public double getYaxis_min() {
        return this.yaxis_min;
    }

    /**
     * <p>normalize.</p>
     *
     * @param c a {@link java.util.List} object.
     * @param normalization a {@link java.lang.String} object.
     * @param normalize_global a boolean.
     * @return a {@link java.util.List} object.
     */
    public List<Array> normalize(final List<Array> c,
            final String normalization, final boolean normalize_global) {
        final NormalizationFilter nf = new NormalizationFilter(normalization,
                false, normalize_global);
        nf.configure(Factory.getInstance().getConfiguration());
        final Array[] as = nf.apply(c.toArray(new Array[]{}));
        return Arrays.asList(as);
    }

    /**
     * <p>setTitle.</p>
     *
     * @param s a {@link java.lang.String} object.
     */
    public abstract void setTitle(String s);

    /**
     * <p>Setter for the field <code>yaxis_max</code>.</p>
     *
     * @param yaxis_max the yaxis_max to set
     */
    public void setYaxis_max(final double yaxis_max) {
        this.yaxis_max = yaxis_max;
    }

    /**
     * <p>Setter for the field <code>yaxis_min</code>.</p>
     *
     * @param yaxis_min the yaxis_min to set
     */
    public void setYaxis_min(final double yaxis_min) {
        this.yaxis_min = yaxis_min;
    }
    
    /**
     * <p>applySeriesColors.</p>
     *
     * @param plot a {@link org.jfree.chart.plot.XYPlot} object.
     * @param plotColors an array of {@link java.awt.Color} objects.
     * @param alpha a float.
     */
    public static void applySeriesColors(XYPlot plot, Color[] plotColors, float alpha) {
        XYItemRenderer renderer = plot.getRenderer();
        int series = plot.getSeriesCount();
        for (int i = 0; i < series; i++) {
            renderer.setSeriesPaint(i,
                    withAlpha(plotColors[i % plotColors.length], alpha));
        }
    }

    /**
     * <p>withAlpha.</p>
     *
     * @param color a {@link java.awt.Color} object.
     * @param alpha a float.
     * @return a {@link java.awt.Color} object.
     * @since 1.3.2
     */
    public static Color withAlpha(Color color, float alpha) {
        Color ca = new Color(color.getRed(), color.getGreen(), color.getBlue(),
                (int) (alpha * 255.0f));
        return ca;
    }
    
    /**
     * <p>Getter for the field <code>seriesColors</code>.</p>
     *
     * @return an array of {@link java.awt.Color} objects.
     */
    public Color[] getSeriesColors() {
        return seriesColors;
    }
    
    /**
     * <p>Setter for the field <code>seriesColors</code>.</p>
     *
     * @param colors an array of {@link java.awt.Color} objects.
     */
    public void setSeriesColors(Color[] colors) {
        this.seriesColors = colors;
    }
}
