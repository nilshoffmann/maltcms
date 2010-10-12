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
 * Created by hoffmann at 12.02.2007
 */
package old;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PipedInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import cross.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;

/**
 * Visualizes Spectra with JFreeChart
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class SpectrumVisualizer implements PISConsumer<Double> {

	HashSet<Tuple2D<IFileFragment, IFileFragment>> hs;

	HashMap<String, PipedInputStream> diSToPis;

	HashMap<IVariableFragment, Tuple2D<IFileFragment, IFileFragment>> diToTuple;

	SortedMap<IVariableFragment, XYSeries> set;

	XYSeriesCollection xyseries;

	ExecutorService tp = null;

	Thread t;

	protected XYPlot p;

	static Logger log;

	static {
		SpectrumVisualizer.log = Logger.getAnonymousLogger();
	}

	boolean finished = false;

	public SpectrumVisualizer(
	        final List<Tuple2D<IFileFragment, IFileFragment>> list) {
		// int i = 0;
		this.hs = new HashSet<Tuple2D<IFileFragment, IFileFragment>>();
		this.diSToPis = new HashMap<String, PipedInputStream>();
		this.diToTuple = new HashMap<IVariableFragment, Tuple2D<IFileFragment, IFileFragment>>();
		this.xyseries = new XYSeriesCollection();
		this.xyseries.setAutoWidth(true);
		this.tp = Executors.newFixedThreadPool(20);
		final String total_intensity = Factory.getInstance().getConfiguration()
		        .getString("var.total_intensity", "total_intensity");
		final Iterator<Tuple2D<IFileFragment, IFileFragment>> iter = list
		        .iterator();
		while (iter.hasNext()) {
			final Tuple2D<IFileFragment, IFileFragment> t1 = iter.next();
			this.diToTuple.put(t1.getFirst().getChild(total_intensity), t1);
			this.diToTuple.put(t1.getSecond().getChild(total_intensity), t1);
			this.hs.add(t1);
		}
		this.set = new TreeMap<IVariableFragment, XYSeries>();
		SpectrumVisualizer.log.log(Level.INFO, ""
		        + this.xyseries.getSeriesCount() + " " + this.hs.size());
		show();
	}

	public synchronized void addXYSeries(final IVariableFragment di,
	        final XYSeries xys) {
		// synchronized(this.xyseries){
		SpectrumVisualizer.this.set.put(di, xys);
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.system.PISConsumer#consume(java.lang.String,
	 * java.lang.String, java.io.PipedOutputStream, java.lang.Object)
	 */
	public synchronized void consume(final IVariableFragment di,
	        final PipedInputStream pis) {
		if (pis == null) {
			SpectrumVisualizer.log.log(Level.WARNING,
			        "PipedInputStream is null!");
		}
		if (this.diToTuple.containsKey(di)) {
			SpectrumVisualizer.log.log(Level.INFO, "VariableFragment known!");
			final Tuple2D<IFileFragment, IFileFragment> tuple = this.diToTuple
			        .get(di);

			synchronized (SpectrumVisualizer.this.diSToPis) {
				this.diSToPis.put(di.toString(), pis);
				if (this.diSToPis.containsKey(tuple.getFirst().toString())
				        && this.diSToPis.containsKey(tuple.getSecond()
				                .toString())) {
					SpectrumVisualizer.log.log(Level.INFO,
					        "Connecting pipes for " + tuple);
					final Runnable r1 = new Runnable() {

						public void run() {
							SpectrumVisualizer.log.log(Level.INFO,
							        "Assembling XYSeries");
							final String total_intensity = Factory
							        .getInstance().getConfiguration()
							        .getString("var.total_intensity",
							                "total_intensity");
							final IVariableFragment vf1 = tuple.getFirst()
							        .getChild(total_intensity);
							final IVariableFragment vf2 = tuple.getSecond()
							        .getChild(total_intensity);
							final XYSeries xs1 = new XYSeries(vf1.getVarname()
							        + "(" + (vf1.getRange()[0]).first() + ":"
							        + vf1.getRange()[0].last() + ") vs. "
							        + vf2.getVarname() + "("
							        + vf2.getRange()[0].first() + ":"
							        + vf2.getRange()[0].last() + ")");
							double a1, a2;
							ObjectInputStream ois1 = null;
							ObjectInputStream ois2 = null;
							try {
								PipedInputStream pis1 = null;
								PipedInputStream pis2 = null;
								SpectrumVisualizer.log.log(Level.INFO,
								        "Retrieving VariableFragment Tuples");

								pis1 = SpectrumVisualizer.this.diSToPis
								        .get(tuple.getFirst().toString());
								pis2 = SpectrumVisualizer.this.diSToPis
								        .get(tuple.getSecond().toString());
								if ((pis1 != null) && (pis2 != null)) {
									SpectrumVisualizer.log.log(Level.INFO,
									        "Attaching Object-Input Streams");
									ois1 = new ObjectInputStream(pis1);
									ois2 = new ObjectInputStream(pis2);
									SpectrumVisualizer.log
									        .log(Level.INFO,
									                "Reading from Object-Input Streams");
									boolean fullyRead = false;
									while (!fullyRead) {
										if ((pis1.available() == -1)
										        && (pis2.available() == -1)) {
											fullyRead = true;
											continue;
										}
										try {
											if (pis1.available() == -1) {
												a1 = Double.NaN;
											} else {
												a1 = (Double) ois1.readObject();
											}
											if (pis2.available() == -1) {
												a2 = Double.NaN;
											} else {
												a2 = (Double) ois2.readObject();
											}
											xs1.add(a1, a2, false);
										} catch (final EOFException eof) {
											fullyRead = true;
										}
									}
									SpectrumVisualizer.log.log(Level.INFO,
									        "Adding XYSeries");
									addXYSeries(tuple.getFirst().getChild(
									        total_intensity), xs1);
									// log.log(Level.INFO,"Removing
									// VariableFragment");
									// SpectrumVisualizer.this.diToTuple.remove(di
									// );
									// log.log(Level.INFO,"Removing Tuple");
									// SpectrumVisualizer.this.diSToPis.remove(
									// tuple);
								} else {
									SpectrumVisualizer.log.log(Level.INFO,
									        "Could not open InputStreams!");
								}
							} catch (final IOException e) {
								e.printStackTrace();
							} catch (final ClassNotFoundException e) {
								e.printStackTrace();
							}
							if (ois1 != null) {
								try {
									ois1.close();
								} catch (final IOException e) {
									e.printStackTrace();
								}
							}
							if (ois2 != null) {
								try {
									ois2.close();
								} catch (final IOException e) {
									e.printStackTrace();
								}
							}
						}

					};
					this.tp.execute(r1);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.system.PISConsumer#getDataInfo()
	 */
	public synchronized List<IVariableFragment> getDataInfo() {
		return new ArrayList<IVariableFragment>(this.diToTuple.keySet());
	}

	public synchronized void show() {
		final Runnable r = new Runnable() {

			public void run() {
				while (!(SpectrumVisualizer.this.set.size() == SpectrumVisualizer.this.hs
				        .size())) {
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				}
				SpectrumVisualizer.log.log(Level.INFO,
				        "diToTuple is empty! Progressing to visualization!");
				final DefaultXYItemRenderer dir = new DefaultXYItemRenderer();
				dir.setShapesVisible(false);
				final Iterator<IVariableFragment> iter = SpectrumVisualizer.this.set
				        .keySet().iterator();
				while (iter.hasNext()) {
					SpectrumVisualizer.this.xyseries
					        .addSeries(SpectrumVisualizer.this.set.get(iter
					                .next()));
				}
				SpectrumVisualizer.this.p = new XYPlot(
				        SpectrumVisualizer.this.xyseries, new NumberAxis("x"),
				        new NumberAxis("y"), dir);
				SpectrumVisualizer.this.p.setDomainCrosshairVisible(false);
				SpectrumVisualizer.this.p
				        .setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
				SpectrumVisualizer.this.p
				        .setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
				final JFreeChart jfc = new JFreeChart(SpectrumVisualizer.this.p);
				jfc.setAntiAlias(false);
				final ChartPanel cp = new ChartPanel(jfc);
				final JFrame jf = new JFrame();
				jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				jf.add(cp);
				jf.setVisible(true);
				jf.pack();
				FileOutputStream fos;
				try {
					fos = new FileOutputStream("test.png");
					EncoderUtil.writeBufferedImage(jfc.createBufferedImage(
					        1024, 768), "PNG", fos);
				} catch (final FileNotFoundException e) {
					e.printStackTrace();
				} catch (final IOException e) {
					e.printStackTrace();
				}

			}

		};
		SwingUtilities.invokeLater(r);
		SpectrumVisualizer.log.log(Level.INFO, ""
		        + this.xyseries.getSeriesCount() + " " + this.hs.size());
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		final Iterator<IVariableFragment> iter = getDataInfo().iterator();
		while (iter.hasNext()) {
			sb.append(iter.next());
		}
		return sb.toString();
	}

}
