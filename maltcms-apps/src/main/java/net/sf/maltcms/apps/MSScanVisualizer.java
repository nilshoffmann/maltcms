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
package net.sf.maltcms.apps;

import cross.Factory;
import cross.IFactory;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tools.FragmentTools;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

import maltcms.commands.filters.array.NormalizationFilter;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.Index;

/**
 * <p>
 * MSScanVisualizer class.</p>
 *
 * @author Nils Hoffmann
 *
 */

public class MSScanVisualizer {
        
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MSScanVisualizer.class);

    /**
     * <p>
     * main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(final String[] args) {
        final Maltcms m = Maltcms.getInstance();
        IFactory factory = Factory.getInstance();
        log.info("Starting Maltcms");
        factory.configure(m.parseCommandLine(args));
        log.info("Configured Factory");
        final String[] s = factory.getConfiguration()
                .getStringArray("input.dataInfo");
        final int imwidth = factory.getConfiguration().getInt(
                "images.width", 640);
        final int imheight = factory.getConfiguration().getInt(
                "images.height", 480);
        final String mv = factory.getConfiguration().getString(
                "var.mass_values", "mass_values");
        final String iv = factory.getConfiguration().getString(
                "var.intensity_values", "intensity_values");
        final String si = factory.getConfiguration().getString(
                "var.scan_index", "scan_index");
        final Date date = new Date();
        for (final IFileFragment parent : factory.getInputDataFactory().prepareInputData(s)) {
            log.info("Reading fragment: " + parent);
            //
            final IFileFragment al = new FileFragment(
                    new File(FileTools.getDefaultDirs(date), parent
                            .getName()));
            al.addSourceFile(parent);
            FragmentTools.loadDefaultVars(al);
            log.info("{}", al);

            if (al.hasChild(mv) && al.hasChild(iv)) {
                al.getChild(mv).setIndex(al.getChild(si));
                al.getChild(iv).setIndex(al.getChild(si));
                final List<Array> mzs = al.getChild(mv).getIndexedArray();
                final List<Array> intens = al.getChild(iv).getIndexedArray();
                final NormalizationFilter nf = Factory.getInstance()
                        .getObjectFactory().instantiate(
                                NormalizationFilter.class);
                // nf.configure(ArrayFactory.getConfiguration());
                final Array[] res = nf.apply(intens.toArray(new Array[0]));
                final List<Array> l = Arrays.asList(res);
                final ArrayList<Array> intens2 = new ArrayList<>(l);
                final XYSeriesCollection xysc = new XYSeriesCollection();
                log.info("{}", intens2.size());
                for (int i = 0; i < intens2.size(); i++) {
                    log.info("Generating plot for scan " + i);
                    final XYSeries xs = new XYSeries(al.getName() + "_scan_"
                            + i);
                    final Array a = mzs.get(i);
                    final Array b = intens.get(i);
                    final Index ia = a.getIndex();
                    final Index ib = b.getIndex();
                    for (int j = 0; j < a.getShape()[0]; j++) {
                        xs.add(a.getDouble(ia.set(j)), b.getDouble(ib.set(j)));
                    }
                    xysc.addSeries(xs);
                }
                XYLineAndShapeRenderer dir = new XYLineAndShapeRenderer();
                dir.setDefaultShapesFilled(false);
                dir.setDefaultLinesVisible(true);
                XYPlot p = new XYPlot(xysc, new NumberAxis("m/z"),
                        new NumberAxis("rel. intensity"), dir);
                p.setDomainCrosshairVisible(false);
                p.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
                p.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
                JFreeChart jfc = new JFreeChart(p);
                jfc.removeLegend();
                jfc.setAntiAlias(true);
                try {
                    final File d = new File(Factory.getInstance()
                            .getConfiguration().getString("output.basedir", ""));
                    if (!d.exists()) {
                        d.mkdirs();
                    }
                    // StringBuilder sb = new StringBuilder();
                    // Formatter formatter = new Formatter(sb);
                    final File f = new File(d, al.getName() + "_all_scans.png");

                    log.info("Saving to file " + f.getAbsolutePath());
                    // if(f.exists() &&
                    // !ArrayFactory.getConfiguration().getBoolean(
                    // "output.overwrite")
                    // )
                    // {
                    // log.warn("File "+f.getAbsolutePath()+" exists
                    // and option output.overwrite is set to false, stopping!");
                    // System.exit(-1);
                    //
                    // }else {
                    final FileOutputStream fos = new FileOutputStream(f);
                    EncoderUtil.writeBufferedImage(jfc.createBufferedImage(
                            imwidth, imheight), "png", fos);
                    // }
                } catch (final FileNotFoundException e) {
                    log.warn(e.getLocalizedMessage());
                } catch (final IOException e) {
                    log.warn(e.getLocalizedMessage());
                }
                for (int i = 0; i < intens2.size(); i++) {
                    dir = new XYLineAndShapeRenderer();
                    dir.setDefaultShapesVisible(false);
                    dir.setDefaultLinesVisible(true);
                    final XYSeriesCollection xysc2 = new XYSeriesCollection();
                    xysc2.setAutoWidth(false);
                    xysc2.addSeries(xysc.getSeries(i));
                    final ValueAxis masschannels = new NumberAxis("m/z");
                    masschannels.setRange(50.0d, 550.0d);
                    final ValueAxis intensities = new NumberAxis(
                            "rel. intensity");
                    intensities.setRange(0.0d, 1.0d);
                    p = new XYPlot(xysc2, masschannels, intensities, dir);
                    p.setDomainCrosshairVisible(false);
                    p.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
                    p.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
                    jfc = new JFreeChart(p);
                    jfc.setAntiAlias(true);
                    // ChartPanel cp = new ChartPanel(jfc);
                    // JFrame jf = new JFrame();
                    // jf.setDefaultCloseOperation(WindowConstants.
                    // DISPOSE_ON_CLOSE);
                    // jf.add(cp);
                    // jf.setVisible(true);
                    // jf.pack();
                    try {
                        final File d = new File(Factory.getInstance()
                                .getConfiguration().getString("output.basedir",
                                        ""));
                        if (!d.exists()) {
                            d.mkdirs();
                        }
                        final StringBuilder sb = new StringBuilder();
                        final Formatter formatter = new Formatter(sb);
                        formatter.format(
                                "%0" + (int) Math.ceil(Math.log10(mzs.size()))
                                + "d", (i));
                        final File f = new File(d, al.getName() + "_scan_"
                                + sb.toString() + ".png");

                        log.info("Saving to file "
                                + f.getAbsolutePath());
                        // if(f.exists() &&
                        // !ArrayFactory.getConfiguration().getBoolean(
                        // "output.overwrite"))
                        // {
                        // log.warn("File "+f.getAbsolutePath()+"
                        // exists and option output.overwrite is set to false,
                        // stopping!");
                        // System.exit(-1);
                        //
                        // }else {
                        final FileOutputStream fos = new FileOutputStream(f);
                        EncoderUtil.writeBufferedImage(jfc.createBufferedImage(
                                imwidth, imheight), "png", fos);
                        // }
                    } catch (final FileNotFoundException e) {
                        log.warn(e.getLocalizedMessage());
                    } catch (final IOException e) {
                        log.warn(e.getLocalizedMessage());
                    }
                }
            } else {
                System.err
                        .println("Only two arrays can currently be processed!");
            }
        }
        System.exit(0);
    }
}
