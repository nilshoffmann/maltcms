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
package maltcms.ui.charts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * Will create a {@link XYPlot} bar chart.
 *
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public class MassSpectrumPlot extends AChart<XYPlot> {

    private String title;
    private List<String> legendName;
    private List<Array> values;
    private boolean useLogScale = false;
    private boolean showLegend = false;
    private List<Color> seriesColor;
    private double barWidth = 0.5d;
    private double barDistance = 0.1d;
    private int maxAnnotations = 10;

    /**
     * Constructor to create an empty plot.
     *
     * @param iTitle title
     * @param iUseLogScale use logarithmic scale for the y axis
     * @param iShowLegend show legend items
     */
    public MassSpectrumPlot(final String iTitle, final boolean iUseLogScale,
            final boolean iShowLegend) {
        this.title = iTitle;
        this.showLegend = iShowLegend;
        this.useLogScale = iUseLogScale;
        this.values = new ArrayList<Array>();
        this.legendName = new ArrayList<String>();
        this.seriesColor = new ArrayList<Color>();
    }

    /**
     * Default constructor.
     *
     * @param iTitle title of this plot
     * @param iLegendName series name
     * @param iValues values
     * @param iUseLogScale use logarithmic scale for range axis
     * @param iShowLegend show legend label
     */
    public MassSpectrumPlot(final String iTitle, final String iLegendName,
            final Array iValues, final boolean iUseLogScale,
            final boolean iShowLegend) {
        this(iTitle, iUseLogScale, iShowLegend);
        addSeries(iLegendName, iValues, Color.RED.darker());
    }

    /**
     * Add a new mass spectra to the plot.
     *
     * @param nLegendName legend name
     * @param nValues mass spectra
     * @param nColor color
     */
    public void addSeries(final String nLegendName, final Array nValues,
            final Color nColor) {
        this.legendName.add(nLegendName);
        this.values.add(nValues);
        this.seriesColor.add(nColor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XYPlot create() {
        final XYSeriesCollection collection = new XYSeriesCollection();
        final List<Map<Integer, Double>> lists = new ArrayList<Map<Integer, Double>>();
        final int[] counters = new int[this.values.size()];
        for (int i = 0; i < this.values.size(); i++) {
            final IndexIterator iter = this.values.get(i).getIndexIterator();
            final Map<Integer, Double> list = new HashMap<Integer, Double>();
            counters[i] = 0;
            final XYSeries series = new XYSeries(this.legendName.get(i));
            while (iter.hasNext()) {
                if (iter.getDoubleNext() == 0.0d && this.useLogScale) {
                    series.add(counters[i], Double.MIN_NORMAL);
                } else {
                    series.add(counters[i], iter.getDoubleCurrent());
                }
                list.put(counters[i], iter.getDoubleCurrent());
                counters[i]++;
            }
            collection.addSeries(series);
            lists.add(list);
        }
        final XYBarDataset dataset = new XYBarDataset(collection, this.barWidth);

        NumberAxis yaxis = null;
        if (this.useLogScale) {
            yaxis = new LogarithmicAxis("intensity values (log)");
            for (Array a : this.values) {
                final IndexIterator iter = a.getIndexIterator();
                while (iter.hasNext()) {
                    if (iter.getDoubleNext() == 0) {
                        iter.setDoubleCurrent(1.0d);
                    }
                }
            }
        } else {
            yaxis = new NumberAxis("intensity values");
        }
        final NumberAxis xaxis = new NumberAxis("mz");

        final XYBarRenderer renderer = new XYBarRenderer(this.barDistance);
        renderer.setShadowVisible(false);
        for (int i = 0; i < this.seriesColor.size(); i++) {
            renderer.setSeriesPaint(i, this.seriesColor.get(i));
        }
        for (int i = 0; i < this.legendName.size(); i++) {
            renderer.setSeriesVisibleInLegend(i, this.showLegend);
        }
        renderer.setGradientPaintTransformer(null);

        final XYPlot plot = new XYPlot(dataset, xaxis, yaxis, renderer);
        final TreeSet<Double> sortedKeys = new TreeSet<Double>(
                new Comparator<Double>() {
                    public int compare(final Double double1,
                            final Double double2) {
                        return -1
                                * Double.valueOf(double1.compareTo(double2))
                                .intValue();
                    }
                });
        for (int i = 0; i < lists.size(); i++) {
            sortedKeys.clear();
            sortedKeys.addAll(lists.get(i).values());
            counters[i] = 1;
            for (Double value : sortedKeys) {
                for (Integer x : getXList(lists.get(i), value)) {
                    final XYPointerAnnotation pointer = new XYPointerAnnotation(
                            x + "", x, value.intValue(), 7 * Math.PI / 4.0d);
                    pointer.setPaint(this.seriesColor.get(i));
                    pointer.setTipRadius(0.0d);
                    plot.addAnnotation(pointer);
                    if (counters[i]++ > this.maxAnnotations) {
                        break;
                    }
                }
                if (counters[i] > this.maxAnnotations) {
                    break;
                }
            }
        }

        return plot;
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

    /**
     * Getter.
     *
     * @param map map
     * @param value value
     * @return list of indices of all occurrences of value
     */
    private List<Integer> getXList(final Map<Integer, Double> map,
            final double value) {
        final List<Integer> xlist = new ArrayList<Integer>();
        for (Integer x : map.keySet()) {
            if (map.get(x) == value) {
                xlist.add(x);
            }
        }

        return xlist;
    }

    /**
     * Getter.
     *
     * @return bar width
     */
    public double getBarWidth() {
        return barWidth;
    }

    /**
     * Setter.
     *
     * @param nBarWidth bar width
     */
    public void setBarWidth(final double nBarWidth) {
        this.barWidth = nBarWidth;
    }

    /**
     * Getter.
     *
     * @return bar distance
     */
    public double getBarDistance() {
        return barDistance;
    }

    /**
     * Setter.
     *
     * @param nBarDistance bar distance
     */
    public void setBarDistance(final double nBarDistance) {
        this.barDistance = nBarDistance;
    }

    /**
     * Getter.
     *
     * @return max annotation
     */
    public int getMaxAnnotations() {
        return maxAnnotations;
    }

    /**
     * Setter.
     *
     * @param nMaxAnnotations max cross.annotations
     */
    public void setMaxAnnotations(final int nMaxAnnotations) {
        this.maxAnnotations = nMaxAnnotations;
    }
}
