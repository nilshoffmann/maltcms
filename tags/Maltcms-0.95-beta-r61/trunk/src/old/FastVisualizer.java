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

package old;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
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
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasetCache;
import cross.Logging;
import cross.datastructures.Vars;

/**
 * Uses a shared Array, backing multiple views, which depend on entries from the
 * scan_index variable. Time vs. total_intensity.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class FastVisualizer implements Runnable {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		NetcdfDatasetCache.init();
		final ArrayList<String> files = new ArrayList<String>();
		for (final String s : args) {
			if (s.endsWith(".cdf")) {
				files.add(s);
			}
		}
		// for
		// files.add("/vol/meltdb/data/RT-Drift/ATCC13032_20050131.cdf");
		// files.add("/vol/meltdb/data/RT-Drift/ATCC13032_20050523.cdf");
		// files.add("/vol/meltdb/data/RT-Drift/ATCC13032_20051201.cdf");
		// FastVisualizer fv = new
		//FastVisualizer(files,"mass_values","intensity_values",false,false,true
		// ,true
		// ,"scan_index",true,
		// "mass_intensities_projection_plain_normalized_inv.png",true);
		// Thread t = new Thread(fv);
		// t.start();
		final FastVisualizer fv2 = new FastVisualizer(files, "mass_values",
		        "intensity_values", false, false, true, true, "scan_index",
		        true, "mass_intensities.png", false);
		final Thread t2 = new Thread(fv2);
		t2.start();
		// FastVisualizer fv2 = new
		// FastVisualizer(files,"","total_intensity",false,false,true,true,
		// "scan_index",false,"total_intensities.png");
		// Thread t2 = new Thread(fv2);
		// t2.start();
		// FastVisualizer fv3 = new
		//FastVisualizer(files,"","scan_acquisition_time",false,false,false,false
		// ,"scan_index",false,"scan_acq_time.png");
		// Thread t3 = new Thread(fv3);
		// t3.start();
		// FastVisualizer fv4 = new
		// FastVisualizer(files,"","point_count",false,false,false,false,
		// "scan_index",false,"point_count.png");
		// Thread t4 = new Thread(fv4);
		// t4.start();
		NetcdfDatasetCache.exit();
	}

	private boolean display = true;

	private boolean useIndexArray = true;

	private final List<String> filenames;

	private boolean useL2Norm = false;

	private String indexArrayName = "scan_index";

	private String arrayXName = "mass_values";

	private String arrayYName = "intensity_values";

	private final boolean normalize;

	private final boolean normalizeGlobal;

	private final boolean timeScaleProjection;

	private final String filename;

	private final Logger log;

	private final HashMap<String, HashMap<Vars, Double>> arrayStats;

	public FastVisualizer(final List<String> filenames1, final String varX,
	        final String varY, final boolean display1,
	        final boolean useL2Norm1, final boolean normalize1,
	        final boolean normalizeGlobal1, final String indexArrayName1,
	        final boolean useIndexArray1, final String filename1,
	        final boolean timeScaleProjection1) {
		this.log = Logging.getInstance().logger;
		this.display = display1;
		this.filenames = filenames1;
		this.useL2Norm = useL2Norm1;
		this.normalize = normalize1;
		this.normalizeGlobal = normalizeGlobal1;
		this.indexArrayName = indexArrayName1;
		this.useIndexArray = useIndexArray1;
		this.timeScaleProjection = timeScaleProjection1;
		this.arrayXName = varX;
		this.arrayYName = varY;
		this.filename = filename1;
		this.arrayStats = new HashMap<String, HashMap<Vars, Double>>();
	}

	public HashMap<Vars, Double> getStats(final Array array) {
		if (this.arrayStats.containsKey(array.toString())) {
			return this.arrayStats.get(array.toString());
		}
		final IndexIterator iter = array.getIndexIteratorFast();
		long cnt = 0;
		double min = 0.0d, max = 0.0d;
		double mean = 0.0d;
		while (iter.hasNext()) {
			final double d = iter.getDoubleNext();
			if (cnt == 0) {
				min = max = d;
				cnt = 1;
			} else {
				min = Math.min(min, d);
				max = Math.max(max, d);
			}
			mean += d;
			cnt++;
		}
		mean = mean / (cnt - 1);
		final HashMap<Vars, Double> hm = new HashMap<Vars, Double>();
		hm.put(Vars.Min, min);
		hm.put(Vars.Max, max);
		hm.put(Vars.Mean, mean);
		hm.put(Vars.Size, new Double(cnt));
		this.arrayStats.put(array.toString(), hm);
		return hm;
	}

	public HashMap<Vars, Double> getStats(final String array) {
		if (this.arrayStats.containsKey(array)) {
			return this.arrayStats.get(array);
		}
		return new HashMap<Vars, Double>();
	}

	// public void applyFilter(Array array, FilterIndexIterator filter) {
	//		
	// }
	//	
	public void make2DLandscape(final List<Array> arrays) {
		if (arrays.size() % 2 != 0) {
			this.log
			        .debug("List has uneven number of elements, omitting last element!");
		}

		final int width = 600;// number of scans
		final int height = arrays.size() / 2;// m/z interval
		int[] fPixels;
		fPixels = new int[width * height];
		java.util.Arrays.fill(fPixels, Color.white.getRGB());
		final Iterator<Array> it = arrays.iterator();
		int i = 0;
		int j = 0;
		HashMap<Vars, Double> hm2 = null;
		while (it.hasNext()) {
			Array a1 = null;
			a1 = it.next();
			Array a2 = null;
			if (it.hasNext()) {
				a2 = it.next();
			} else {
				return;// omitting last element
			}
			hm2 = getStats(a2);
			if (a1.getSize() == a2.getSize()) {
				j = 0;
				final IndexIterator iter1 = a1.getIndexIteratorFast();
				final IndexIterator iter2 = a2.getIndexIteratorFast();
				while (iter1.hasNext() && iter2.hasNext()) {
					final int mz = iter1.getIntNext();
					double intensity = iter2.getDoubleNext();
					// if(j>500&&j<2500){
					// System.out.print("mz: "+mz+" intensity: "+intensity);
					intensity = intensity
					        / (hm2.get(Vars.Max) - hm2.get(Vars.Min));
					if ((intensity < 0.0d) || (intensity > 1.0d)) {
						this.log.info(" normalized: " + intensity + "\n");
					}

					// }
					final double val = intensity;
					// double val =
					// 1.0d/Math.sqrt(2.0d*Math.PI)*(Math.exp(-0.5d*Math.pow(
					// intensity-0.5,2)));
					// double val =
					// 1.0d-Math.pow((1.0d-Math.pow(intensity,1.0d)),3.0d);//CDF
					// of Kumaraswamy distribution
					fPixels[i * width + mz] = new Color((float) val,
					        (float) val, (float) val).getRGB();
					// System.out.println("Index: "+((i*height)+mz)+" x:
					// "+(i*height));
					j++;
				}
				// Now create the image from the pixel array.
				// fImage = new BufferedImage();
			} else {
				this.log.warn("Size mismatch!");
			}
			i++;// time counter
		}
		final MemoryImageSource mis = new MemoryImageSource(width, height,
		        fPixels, 0, width);
		final BufferedImage b = new BufferedImage(width, height,
		        BufferedImage.TYPE_INT_ARGB);
		final Graphics g = b.getGraphics();
		final Image img = Toolkit.getDefaultToolkit().createImage(mis);
		g.drawImage(img, 0, 0, null);
		try {
			ImageIO.write(b, "PNG", new File(this.filename));
		} catch (final IOException e) {
			this.log.error(e.getLocalizedMessage());
		}
	}

	public void prepare(final List<String> filenames1) {
		final Iterator<String> name = filenames1.iterator();
		final ArrayList<Array> list = new ArrayList<Array>();
		this.log.info("Preparing input from");
		while (name.hasNext()) {
			NetcdfDataset nds;
			try {
				final String filename1 = name.next();
				this.log.info(filename1);
				nds = NetcdfDatasetCache.acquire(filename1, null);
				if (!this.arrayXName.equals("")) {// value vs value plot
					final Variable v1 = nds.findVariable(this.arrayXName);
					final Variable v2 = nds.findVariable(this.arrayYName);
					if ((v1.getShape().length != 1)
					        || (v2.getShape().length != 1)) {
						this.log.debug("Shape != 1");
						return;
					}
					if (!v1.isScalar() && !v2.isScalar()) {
						Array a1, a2;
						try {
							if (this.normalizeGlobal) {
								// a1 = (D1) v1.read();
								//this.arrayStats.put(a1.toString(),getStats(a1)
								// );
								a2 = v2.read();// normalize y-Axis
								this.arrayStats.put("global_stats",
								        getStats(a2));
							}
							if (this.useIndexArray) {
								this.log.info("Using index array");
								final Variable index = nds
								        .findVariable(this.indexArrayName);
								final ArrayInt.D1 indArr = (ArrayInt.D1) index
								        .read();
								final IndexIterator iter = indArr
								        .getIndexIteratorFast();
								if (iter.hasNext()) {
									int start = iter.getIntNext();
									int end = start;
									while (iter.hasNext()) {
										end = iter.getIntNext();
										this.log.info("Reading " + start + ":"
										        + end);
										try {
											a1 = v1.read(start + ":" + end);
											list.add(a1);
											a2 = v2.read(start + ":" + end);
											this.arrayStats.put(a2.toString(),
											        getStats("global_stats"));
											list.add(a2);
											start = end;
										} catch (final InvalidRangeException e) {
											e.printStackTrace();
										}
									}
								}
							} else {
								this.log.info("Using array directly");
								a1 = v1.read();
								list.add(a1);
								a2 = v2.read();
								list.add(a2);
							}
						} catch (final IOException e) {
							e.printStackTrace();
						}
					}
				} else {// time vs value plot
					// Variable v1 = nds.findVariable(this.arrayXName);
					final Variable v2 = nds.findVariable(this.arrayYName);
					if (v2.getShape().length != 1) {
						this.log.warn("Shape != 1");
						return;
					}
					if (!v2.isScalar()) {
						Array a2;
						try {
							if (this.normalizeGlobal) {
								// a1 = (D1) v1.read();
								//this.arrayStats.put(a1.toString(),getStats(a1)
								// );
								a2 = v2.read();// normalize y-Axis
								this.arrayStats.put("global_stats",
								        getStats(a2));
							}
							if (this.useIndexArray) {
								this.log.info("Using index array");
								final Variable index = nds
								        .findVariable(this.indexArrayName);
								final ArrayInt.D1 indArr = (ArrayInt.D1) index
								        .read();
								final IndexIterator iter = indArr
								        .getIndexIteratorFast();
								if (iter.hasNext()) {
									int start = iter.getIntNext();
									int end = start;
									while (iter.hasNext()) {
										end = iter.getIntNext();
										this.log.info("Reading " + start + ":"
										        + end);
										try {
											a2 = v2.read(start + ":" + end);
											list.add(a2);
											this.arrayStats.put(a2.toString(),
											        getStats("global_stats"));
											start = end;
										} catch (final InvalidRangeException e) {
											e.printStackTrace();
										}
									}
								}
							} else {
								this.log.info("Using array directly");
								a2 = v2.read();
								list.add(a2);
							}
						} catch (final IOException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
		}
		if (this.timeScaleProjection) {
			make2DLandscape(list);
		} else {
			show(list);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		prepare(this.filenames);
	}

	public void show(final List<Array> arrays) {
		final Iterator<Array> it = arrays.iterator();
		final XYSeriesCollection ds = new XYSeriesCollection();
		while (it.hasNext()) {

			final XYSeries xs = new XYSeries(
			        this.arrayXName.equals("") ? "time" : this.arrayXName
			                + this.arrayYName);
			Array a1 = null;
			if (!this.arrayXName.equals("")) {
				a1 = it.next();
			}
			if (!it.hasNext()) {
				continue;
			}
			final Array a2 = it.next();

			IndexIterator iter2 = null;
			HashMap<Vars, Double> hm = new HashMap<Vars, Double>();
			if (this.normalize) {
				if (this.normalizeGlobal) {
					hm = getStats(a2.toString());
				}
				hm = getStats(a2);
			}
			long cnt = 1;

			if (!this.arrayXName.equals("") && (a1 != null)) {
				final IndexIterator iter1 = a1.getIndexIteratorFast();
				iter2 = a2.getIndexIteratorFast();
				while (iter1.hasNext() && iter2.hasNext()) {
					final double d1 = iter1.getDoubleNext();
					double d2 = iter2.getDoubleNext();
					if (this.normalize) {
						if (this.useL2Norm) {
							d2 = d2
							        / Math.sqrt((hm.get(Vars.Max) - hm
							                .get(Vars.Min))
							                * (hm.get(Vars.Max) - hm
							                        .get(Vars.Min)));
						} else {
							d2 = d2 / (hm.get(Vars.Max) - hm.get(Vars.Min));
						}
					}
					xs.add(d1, d2, false);
					cnt++;
				}
			} else {
				iter2 = a2.getIndexIteratorFast();
				while (iter2.hasNext()) {
					double d2 = iter2.getDoubleNext();
					if (this.normalize) {
						if (this.useL2Norm) {
							d2 = d2
							        / Math.sqrt((hm.get(Vars.Max) - hm
							                .get(Vars.Min))
							                * (hm.get(Vars.Max) - hm
							                        .get(Vars.Min)));
						} else {
							d2 = d2 / (hm.get(Vars.Max) - hm.get(Vars.Min));
						}
					}
					xs.add(cnt, d2, false);
					cnt++;
				}
			}
			ds.addSeries(xs);
		}
		final DefaultXYItemRenderer dir = new DefaultXYItemRenderer();
		dir.setShapesVisible(false);
		if (this.arrayXName.equals("")) {
			this.arrayXName = "time";
		}
		final XYPlot p = new XYPlot(ds, new NumberAxis(this.arrayXName),
		        new NumberAxis(this.arrayYName), dir);
		p.setDomainCrosshairVisible(false);
		p.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
		p.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		final JFreeChart jfc = new JFreeChart(p);
		jfc.removeLegend();
		// jfc.setAntiAlias(false);
		if (this.display) {
			final ChartPanel cp = new ChartPanel(jfc);
			final JFrame jf = new JFrame();
			jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			jf.add(cp);
			jf.setVisible(true);
			jf.pack();
		} else {
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(this.filename);
				EncoderUtil.writeBufferedImage(jfc.createBufferedImage(1280,
				        1024), "png", fos);
				fos.flush();
				fos.close();
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

}
