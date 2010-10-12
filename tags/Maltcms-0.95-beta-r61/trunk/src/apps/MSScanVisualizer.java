/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */

/**
 * 
 */
package apps;

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

import ucar.ma2.Array;
import ucar.ma2.Index;
import cross.Factory;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.tools.FragmentTools;

/**
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class MSScanVisualizer {
	public static void main(final String[] args) {
		final Maltcms m = Maltcms.getInstance();
		System.out.println("Starting Maltcms");
		Factory.getInstance().configure(m.parseCommandLine(args));
		System.out.println("Configured Factory");
		final String[] s = Factory.getInstance().getConfiguration()
		        .getStringArray("input.dataInfo");
		final int imwidth = Factory.getInstance().getConfiguration().getInt(
		        "images.width", 640);
		final int imheight = Factory.getInstance().getConfiguration().getInt(
		        "images.height", 480);
		final String mv = Factory.getInstance().getConfiguration().getString(
		        "var.mass_values", "mass_values");
		final String iv = Factory.getInstance().getConfiguration().getString(
		        "var.intensity_values", "intensity_values");
		final String si = Factory.getInstance().getConfiguration().getString(
		        "var.scan_index", "scan_index");
		final Date date = new Date();
		// String mmin = ArrayFactory.getConfiguration().getString(
		// "var.mass_range_min", "mass_range_min");
		// String mmax = ArrayFactory.getConfiguration().getString(
		// "var.mass_range_max", "mass_range_max");
		// String scan_index = ArrayFactory.getConfiguration().getString(
		// "var.scan_index", "scan_index");
		for (final String str : s) {
			System.out.println("Reading input.DataInfo: " + str);
			final IFileFragment parent = FileFragmentFactory.getInstance()
			        .fromString(str);
			//
			final IFileFragment al = new FileFragment(MSScanVisualizer.class,
			        date);
			al.addSourceFile(parent);
			FragmentTools.loadDefaultVars(al);
			System.out.println(al);

			if (al.hasChild(mv) && al.hasChild(iv)) {
				al.getChild(mv).setIndex(al.getChild(si));
				al.getChild(iv).setIndex(al.getChild(si));
				final List<Array> mzs = al.getChild(mv).getIndexedArray();
				final List<Array> intens = al.getChild(iv).getIndexedArray();
				final NormalizationFilter nf = Factory.getInstance()
				        .instantiate(NormalizationFilter.class);
				// nf.configure(ArrayFactory.getConfiguration());
				final Array[] res = nf.apply(intens.toArray(new Array[0]));
				final List<Array> l = Arrays.asList(res);
				final ArrayList<Array> intens2 = new ArrayList<Array>(l);
				final XYSeriesCollection xysc = new XYSeriesCollection();
				System.out.println(intens2.size());
				for (int i = 0; i < intens2.size(); i++) {
					System.out.println("Generating plot for scan " + i);
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
				dir.setShapesVisible(false);
				dir.setLinesVisible(true);
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

					System.out.println("Saving to file " + f.getAbsolutePath());
					// if(f.exists() &&
					// !ArrayFactory.getConfiguration().getBoolean(
					// "output.overwrite")
					// )
					// {
					// System.err.println("File "+f.getAbsolutePath()+" exists
					// and option output.overwrite is set to false, stopping!");
					// System.exit(-1);
					//						
					// }else {
					final FileOutputStream fos = new FileOutputStream(f);
					EncoderUtil.writeBufferedImage(jfc.createBufferedImage(
					        imwidth, imheight), "png", fos);
					// }
				} catch (final FileNotFoundException e) {
					e.printStackTrace();
				} catch (final IOException e) {
					e.printStackTrace();
				}
				for (int i = 0; i < intens2.size(); i++) {
					dir = new XYLineAndShapeRenderer();
					dir.setShapesVisible(false);
					dir.setLinesVisible(true);
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

						System.out.println("Saving to file "
						        + f.getAbsolutePath());
						// if(f.exists() &&
						// !ArrayFactory.getConfiguration().getBoolean(
						// "output.overwrite"))
						// {
						// System.err.println("File "+f.getAbsolutePath()+"
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
						e.printStackTrace();
					} catch (final IOException e) {
						e.printStackTrace();
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