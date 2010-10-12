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

package maltcms.tests;

import java.awt.Color;
import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import maltcms.commands.distances.ArrayDot;
import maltcms.datastructures.array.Sparse;
import maltcms.io.csv.CSVWriter;
import maltcms.tools.ArrayTools;
import maltcms.tools.SparseTools;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;

import ucar.ma2.ArrayDouble;
import apps.Maltcms;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;

import com.sun.medialib.mlib.Algebra;

import cross.Factory;
import cross.Logging;
import cross.datastructures.StatsMap;

/**
 * Benchmark of different array implementations and operations on them.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */

public class SparseColtvsMultiArray {

	public static byte[] toByteArray(final double[] d) {
		final ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		final DataOutputStream datastream = new DataOutputStream(bytestream);
		for (final double v : d) {
			try {
				datastream.writeDouble(v);
			} catch (final IOException e) {
				SparseColtvsMultiArray.log.error(e.getLocalizedMessage());
			}
		}
		try {
			datastream.flush();
		} catch (final IOException e) {
			SparseColtvsMultiArray.log.error(e.getLocalizedMessage());
		}
		return bytestream.toByteArray();
	}

	public static byte[] toByteArray(final float[] d) {
		final ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		final DataOutputStream datastream = new DataOutputStream(bytestream);
		for (final float v : d) {
			try {
				datastream.writeFloat(v);
			} catch (final IOException e) {
				SparseColtvsMultiArray.log.error(e.getLocalizedMessage());
			}
		}
		try {
			datastream.flush();
		} catch (final IOException e) {
			SparseColtvsMultiArray.log.error(e.getLocalizedMessage());
		}
		return bytestream.toByteArray();
	}

	public final int N_ARRAYS = 10;

	public int ARRAY_SIZE = 100;

	public final int EXEC_N_TIMES = 10;

	public double N_ZEROS = 0.1d;// ARRAY_SIZE/100;

	private final ArrayDot dpd = new ArrayDot();

	private static Logger log = Logging.getLogger(SparseColtvsMultiArray.class);

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final Maltcms m = Maltcms.getInstance();
		System.out.println("Starting Maltcms");
		Factory.getInstance().configure(m.parseCommandLine(args));
		final SparseColtvsMultiArray scma = new SparseColtvsMultiArray();
		scma.run();
		// System.exit(0);
	}

	public ArrayList<DoubleMatrix1D> createColtDense(final int nArrays,
	        final int arraySize) {
		final ArrayList<DoubleMatrix1D> al = new ArrayList<DoubleMatrix1D>(
		        nArrays);
		for (int i = 0; i < nArrays; i++) {
			al.add(DoubleFactory1D.dense.random(arraySize));
		}
		return fillWithZerosColt(al, this.N_ZEROS);
	}

	public ArrayList<DoubleMatrix1D> createColtSparse(final int nArrays,
	        final int arraySize) {
		final ArrayList<DoubleMatrix1D> al = new ArrayList<DoubleMatrix1D>(
		        nArrays);
		for (int i = 0; i < nArrays; i++) {
			al.add(DoubleFactory1D.sparse.random(arraySize));
		}
		return fillWithZerosColt(al, this.N_ZEROS);
	}

	public ArrayList<ArrayDouble.D1> createMADense(final int nArrays,
	        final int arraySize) {
		final ArrayList<ArrayDouble.D1> al = new ArrayList<ArrayDouble.D1>(
		        nArrays);
		for (int i = 0; i < nArrays; i++) {
			al.add(ArrayTools.randomUniform(arraySize, 0.0d, 1.0d));
		}
		System.out.println("Created " + nArrays + " dense arrays!");
		return fillWithZerosDense(al, this.N_ZEROS);
	}

	public ArrayList<Sparse> createMASparse(final int nArrays,
	        final int arraySize) {
		final ArrayList<Sparse> al = new ArrayList<Sparse>(nArrays);
		for (int i = 0; i < nArrays; i++) {
			al.add(SparseTools.randomUniform(0, arraySize - 1, 0.0d, 1.0d));
		}
		return fillWithZerosSparse(al, this.N_ZEROS);
	}

	public ArrayList<byte[]> createMedialib(final int nArrays,
	        final int arraySize) {
		final ArrayList<byte[]> al = new ArrayList<byte[]>(nArrays);
		for (int i = 0; i < nArrays; i++) {
			final double[] d = new double[arraySize];
			// float [] d = new float[arraySize];
			for (int j = 0; j < arraySize; j++) {
				d[j] = Math.random();
				// d[j] = (float)Math.random();
			}
			final byte[] ba = SparseColtvsMultiArray.toByteArray(d);
			al.add(ba);
		}
		System.out.println("Created " + nArrays + " dense arrays!");
		return fillWithZerosMedialib(al, this.N_ZEROS);
	}

	public long dotColt(final DoubleMatrix1D a, final DoubleMatrix1D b) {
		final long begin = System.currentTimeMillis();
		a.zDotProduct(b);
		final long d = begin - System.currentTimeMillis();
		// System.out.print(d/1000.0d+" ");
		return (d);
	}

	public long dotMA(final Sparse a, final Sparse b) {
		final long begin = System.currentTimeMillis();
		this.dpd.apply(0, 0, -1, -1, a, b);
		// SparseTools.dot(a,b);
		final long d = begin - System.currentTimeMillis();
		// System.out.print(d/1000.0d+" ");
		return (d);
	}

	public long dotMADense(final ArrayDouble.D1 a, final ArrayDouble.D1 b) {
		final long begin = System.currentTimeMillis();
		this.dpd.apply(0, 0, -1, -1, a, b);
		final long d = begin - System.currentTimeMillis();
		// System.out.print(d/1000.0d+" ");
		return (d);
	}

	public long dotMediaLib(final byte[] a, final byte[] b) {

		final long begin = System.currentTimeMillis();
		Algebra.DotProd(a, b);
		// System.out.println(res);
		final long d = begin - System.currentTimeMillis();
		// System.out.print(d/1000.0d+" ");
		return (d);
	}

	public long execColt(final ArrayList<Double> al, final int times,
	        final StatsMap sm, final boolean sparse) {
		long ret = 0;
		System.out.println("MA: Using "
		        + (float) (this.N_ZEROS * this.ARRAY_SIZE)
		        + " zero elements per array");
		System.out.println("COLT: Creating " + this.N_ARRAYS
		        + " input arrays A each with size " + this.ARRAY_SIZE);
		final ArrayList<DoubleMatrix1D> a = (sparse ? createColtSparse(
		        this.N_ARRAYS, this.ARRAY_SIZE) : createColtDense(
		        this.N_ARRAYS, this.ARRAY_SIZE));
		System.out.println("COLT: Creating " + this.N_ARRAYS
		        + " input arrays B each with size " + this.ARRAY_SIZE);
		final ArrayList<DoubleMatrix1D> b = createColtSparse(this.N_ARRAYS,
		        this.ARRAY_SIZE);
		for (int i = 0; i < times; i++) {
			final long begin = System.currentTimeMillis();
			pwColtIlast(a, b);
			final long end = System.currentTimeMillis() - begin;
			System.out.println("Colt Run " + i + " in " + end + " mus");
			ret += (end);
			al.add(end / 1000.0d);
		}
		return ret;
	}

	public long execMA(final ArrayList<Double> al, final int times,
	        final StatsMap sm) {
		long ret = 0;
		System.out.println("MA: Using "
		        + (float) (this.N_ZEROS * this.ARRAY_SIZE)
		        + " zero elements per array");
		System.out.println("MA: Creating " + this.N_ARRAYS
		        + " input arrays A each with size " + this.ARRAY_SIZE);
		final ArrayList<Sparse> a = createMASparse(this.N_ARRAYS,
		        this.ARRAY_SIZE);
		System.out.println("MA: Creating " + this.N_ARRAYS
		        + " input arrays B each with size " + this.ARRAY_SIZE);
		final ArrayList<Sparse> b = createMASparse(this.N_ARRAYS,
		        this.ARRAY_SIZE);
		for (int i = 0; i < times; i++) {
			final long begin = System.currentTimeMillis();
			pwMAIlast(a, b);
			final long end = System.currentTimeMillis() - begin;
			System.out.println("MA Run " + i + " in " + end + " mus");
			ret += (end);
			al.add(end / 1000.0d);
		}
		return ret;
	}

	public long execMADense(final ArrayList<Double> al, final int times,
	        final StatsMap sm) {
		long ret = 0;
		System.out.println("MADense: Using "
		        + (float) (this.N_ZEROS * this.ARRAY_SIZE)
		        + " zero elements per array");
		System.out.println("MADense: Creating " + this.N_ARRAYS
		        + " input arrays A each with size " + this.ARRAY_SIZE);
		final ArrayList<ArrayDouble.D1> a = createMADense(this.N_ARRAYS,
		        this.ARRAY_SIZE);
		System.out.println("MADense: Creating " + this.N_ARRAYS
		        + " input arrays B each with size " + this.ARRAY_SIZE);
		final ArrayList<ArrayDouble.D1> b = createMADense(this.N_ARRAYS,
		        this.ARRAY_SIZE);
		for (int i = 0; i < times; i++) {
			final long begin = System.currentTimeMillis();
			pwMADenseIlast(a, b);
			final long end = System.currentTimeMillis() - begin;
			System.out.println("MA Dense Run " + i + " in " + end + " mus");
			ret += (end);
			al.add(end / 1000.0d);
		}
		return ret;
	}

	public long execMediaLib(final ArrayList<Double> al, final int times,
	        final StatsMap sm) {
		long ret = 0;
		System.out.println("Medialib: Using "
		        + (float) (this.N_ZEROS * this.ARRAY_SIZE)
		        + " zero elements per array");
		System.out.println("Medialib: Creating " + this.N_ARRAYS
		        + " input arrays A each with size " + this.ARRAY_SIZE);
		final ArrayList<byte[]> a = createMedialib(this.N_ARRAYS,
		        this.ARRAY_SIZE);
		System.out.println("Medialib: Creating " + this.N_ARRAYS
		        + " input arrays B each with size " + this.ARRAY_SIZE);
		final ArrayList<byte[]> b = createMedialib(this.N_ARRAYS,
		        this.ARRAY_SIZE);
		for (int i = 0; i < times; i++) {
			final long begin = System.currentTimeMillis();
			pwMedialibIlast(a, b);
			final long end = System.currentTimeMillis() - begin;
			System.out.println("Medialib Run " + i + " in " + end + " mus");
			ret += (end);
			al.add(end / 1000.0d);
		}
		return ret;
	}

	public ArrayList<DoubleMatrix1D> fillWithZerosColt(
	        final ArrayList<DoubleMatrix1D> al, final double numzeros) {
		for (final DoubleMatrix1D d : al) {
			int j = 0;
			final int size = d.size() - 1;
			final HashSet<Integer> s = new HashSet<Integer>(size);
			for (int i = 0; i < numzeros * this.ARRAY_SIZE;) {
				j = (int) (Math.rint(Math.random() * size));
				// System.out.println("Checking index "+j);
				if (!s.contains(j)) {
					// System.out.println("Not set!");
					d.set(j, 0.0d);
					s.add(j);
					i++;
				}
			}
			// System.out.println(d.toString());
		}
		return al;
	}

	public ArrayList<ArrayDouble.D1> fillWithZerosDense(
	        final ArrayList<ArrayDouble.D1> al, final double numzeros) {
		for (final ArrayDouble.D1 d : al) {
			int j = 0;
			final int size = d.getShape()[0] - 1;
			final HashSet<Integer> s = new HashSet<Integer>(size);
			for (int i = 0; i < numzeros * this.ARRAY_SIZE;) {
				j = (int) (Math.rint(Math.random() * size));
				// System.out.println("Checking index "+j);
				if (!s.contains(j)) {
					// System.out.println("Not set!");
					d.set(j, 0.0d);
					s.add(j);
					i++;
				}
			}
			// System.out.println(d.toString());
		}
		return al;
	}

	public ArrayList<byte[]> fillWithZerosMedialib(final ArrayList<byte[]> al,
	        final double numzeros) {
		for (final byte[] d : al) {
			int j = 0;
			final int size = d.length - 1;
			final HashSet<Integer> s = new HashSet<Integer>(size);
			for (int i = 0; i < numzeros * this.ARRAY_SIZE;) {
				j = (int) (Math.rint(Math.random() * size));
				// System.out.println("Checking index "+j);
				if (!s.contains(j)) {
					// System.out.println("Not set!");
					d[j] = (char) 0;
					s.add(j);
					i++;
				}
			}
			// System.out.println(d.toString());
		}
		return al;
	}

	public ArrayList<Sparse> fillWithZerosSparse(final ArrayList<Sparse> al,
	        final double numzeros) {
		for (final Sparse d : al) {
			int j = 0;
			final int size = d.getShape()[0];
			final HashSet<Integer> s = new HashSet<Integer>(size);
			for (int i = 0; i < numzeros * this.ARRAY_SIZE;) {
				j = (int) (Math.rint(Math.random() * size));
				// System.out.println("Checking index "+j);
				if (!s.contains(j)) {
					// System.out.println("Not set!");
					d.set(j, 0.0d);
					s.add(j);
					i++;
				}
			}
			// System.out.println(d.toString());
		}
		return al;
	}

	public long pwColtIlast(final ArrayList<DoubleMatrix1D> a,
	        final ArrayList<DoubleMatrix1D> b) {
		long ret = System.currentTimeMillis();
		for (int i = 0; i < a.size(); i++) {
			// System.out.println("Row "+i+"/"+asize);
			for (int j = 0; j < b.size(); j++) {
				final long begin = System.currentTimeMillis();
				dotColt(a.get(i), b.get(j));
				// System.out.println((cnt++)+"/"+size);
				final long d = System.currentTimeMillis() - begin;
				// System.out.println("Time: "+(d/1000.0d)+" s");
				ret += (d);
			}
		}
		// System.out.println();
		System.out.println("Accumulated time: " + ((ret) / 1000.0d));
		return ret;
	}

	public long pwColtJlast(final ArrayList<DoubleMatrix1D> a,
	        final ArrayList<DoubleMatrix1D> b) {
		long ret = System.currentTimeMillis();
		for (int j = 0; j < b.size(); j++) {
			// System.out.println("Row "+j+"/"+bsize);
			for (int i = 0; i < a.size(); i++) {
				final long begin = System.currentTimeMillis();
				dotColt(a.get(i), b.get(j));
				// System.out.println((cnt++)+"/"+size);
				final long d = System.currentTimeMillis() - begin;
				// System.out.println("Time: "+(d/1000.0d)+" s");
				ret += (d);
			}
		}
		// System.out.println();
		System.out.println("Accumulated time: " + ((ret) / 1000.0d));
		return ret;
	}

	public long pwMADenseIlast(final ArrayList<ArrayDouble.D1> a,
	        final ArrayList<ArrayDouble.D1> b) {
		long ret = System.currentTimeMillis();
		for (int i = 0; i < a.size(); i++) {
			// System.out.println("Row "+(i+1)+"/"+asize);
			for (int j = 0; j < b.size(); j++) {
				final long begin = System.currentTimeMillis();
				// System.out.println(a.get(i));
				// System.out.println(b.get(j));
				dotMADense(a.get(i), b.get(j));
				// System.out.println((cnt++)+"/"+size);
				final long d = System.currentTimeMillis() - begin;
				// System.out.println("Time: "+(d/1000.0d)+" s");
				ret += (d);
			}
		}
		// System.out.println();
		System.out.println("Accumulated time: " + ((ret) / 1000.0d));
		return ret;
	}

	public long pwMAIlast(final ArrayList<Sparse> a, final ArrayList<Sparse> b) {
		long ret = System.currentTimeMillis();
		for (int i = 0; i < a.size(); i++) {
			// System.out.println("Row "+i+"/"+asize);
			for (int j = 0; j < b.size(); j++) {
				final long begin = System.currentTimeMillis();
				dotMA(a.get(i), b.get(j));
				// System.out.println((cnt++)+"/"+size);
				final long d = System.currentTimeMillis() - begin;
				// System.out.println("Time: "+(d/1000.0d)+" s");
				ret += (d);
			}
		}
		// System.out.println();
		System.out.println("Accumulated time: " + ((ret) / 1000.0d));
		return ret;
	}

	public long pwMAJlast(final ArrayList<Sparse> a, final ArrayList<Sparse> b) {
		long ret = System.currentTimeMillis();
		for (int j = 0; j < b.size(); j++) {
			// System.out.println("Row "+j+"/"+bsize);
			for (int i = 0; i < a.size(); i++) {
				final long begin = System.currentTimeMillis();
				dotMA(a.get(i), b.get(j));
				// System.out.println((cnt++)+"/"+size);
				final long d = System.currentTimeMillis() - begin;
				// System.out.println("Time: "+(d/1000.0d)+" s");
				ret += (d);
			}
		}
		// System.out.println();
		System.out.println("Accumulated time: " + ((ret) / 1000.0d));
		return ret;
	}

	public long pwMedialibIlast(final ArrayList<byte[]> a,
	        final ArrayList<byte[]> b) {
		long ret = System.currentTimeMillis();
		for (int i = 0; i < a.size(); i++) {
			// System.out.println("Row "+(i+1)+"/"+asize);
			for (int j = 0; j < b.size(); j++) {
				final long begin = System.currentTimeMillis();
				// System.out.println(a.get(i));
				// System.out.println(b.get(j));
				dotMediaLib(a.get(i), b.get(j));
				// System.out.println((cnt++)+"/"+size);
				final long d = System.currentTimeMillis() - begin;
				// System.out.println("Time: "+(d/1000.0d)+" s");
				ret += (d);
			}
		}
		// System.out.println();
		System.out.println("Accumulated time: " + ((ret) / 1000.0d));
		return ret;
	}

	public void run() {
		final StatsMap coltsm = new StatsMap(Factory.getInstance()
		        .getFileFragmentFactory().create("colt.csv"));
		final StatsMap masm = new StatsMap(Factory.getInstance()
		        .getFileFragmentFactory().create("ma.csv"));
		final StatsMap madensem = new StatsMap(Factory.getInstance()
		        .getFileFragmentFactory().create("madense.csv"));
		final StatsMap medialibm = new StatsMap(Factory.getInstance()
		        .getFileFragmentFactory().create("medialib.csv"));
		final ArrayList<Double> coltRes = new ArrayList<Double>();
		final ArrayList<Double> maRes = new ArrayList<Double>();
		final ArrayList<Double> madenseRes = new ArrayList<Double>();
		final ArrayList<Double> medialibRes = new ArrayList<Double>();
		final int[] array_sizes = new int[] { 10, 20 };// 40, 80, 160, 320, 640,
		// 1280 };
		// CombinedDomainCategoryPlot cdcp = new CombinedDomainCategoryPlot();
		final XYSeriesCollection xyd1 = new XYSeriesCollection();

		final XYSeries xs1 = new XYSeries("Sparse MultiArray");
		final XYSeries xs2 = new XYSeries("Sparse Colt Matrix1D");
		final XYSeries xs3 = new XYSeries("Dense MultiArray");
		final XYSeries xs4 = new XYSeries("Dense MediaLib");

		xyd1.addSeries(xs1);
		xyd1.addSeries(xs2);
		xyd1.addSeries(xs3);
		xyd1.addSeries(xs4);

		final CategoryAxis ca = new CategoryAxis("Instances");
		final ValueAxis va = new NumberAxis("Seconds [s]");
		final JFrame jf = new JFrame();
		final int columns = (int) Math.ceil(Math.sqrt(array_sizes.length));
		final int rows = (int) Math.ceil(Math.sqrt(array_sizes.length));
		jf.setLayout(new GridLayout(rows, columns));
		for (int i = 0; i < array_sizes.length; i++) {
			this.ARRAY_SIZE = array_sizes[i];
			long ma = 0, colt = 0, madense = 0, medialib = 0;
			// if(rnd<0.3333){
			ma = execMA(maRes, this.EXEC_N_TIMES, masm);
			colt = execColt(coltRes, this.EXEC_N_TIMES, coltsm, false);
			madense = execMADense(madenseRes, this.EXEC_N_TIMES, madensem);
			medialib = execMediaLib(medialibRes, this.EXEC_N_TIMES, medialibm);
			// } else if(rnd<0.6666){
			// colt = execColt(EXEC_N_TIMES, coltsm);
			// madense = execMADense(EXEC_N_TIMES, madensem);
			// ma = execMA(EXEC_N_TIMES, masm);
			// } else {
			// madense = execMADense(EXEC_N_TIMES, madensem);
			// ma = execMA(EXEC_N_TIMES, masm);
			// colt = execColt(EXEC_N_TIMES, coltsm);
			// }
			final double cmean = (colt / (((double) this.EXEC_N_TIMES)) / 1000.0d);
			final double mamean = (ma / (((double) this.EXEC_N_TIMES)) / 1000.0d);
			final double madensemean = (madense
			        / (((double) this.EXEC_N_TIMES)) / 1000.0d);
			final double medialibmean = (medialib
			        / (((double) this.EXEC_N_TIMES)) / 1000.0d);

			xs1.add(this.ARRAY_SIZE, mamean);
			xs2.add(this.ARRAY_SIZE, cmean);
			xs3.add(this.ARRAY_SIZE, madensemean);
			xs4.add(this.ARRAY_SIZE, medialibmean);

			coltsm.put("MEAN", cmean);
			coltsm.put("TOTAL_RUNTIME", (double) colt);
			coltsm.put("TIMES_RUN", (double) this.EXEC_N_TIMES);
			masm.put("MEAN", mamean);
			masm.put("TOTAL_RUNTIME", (double) ma);
			masm.put("TIMES_RUN", (double) this.EXEC_N_TIMES);
			madensem.put("MEAN", madensemean);
			madensem.put("TOTAL_RUNTIME", (double) madense);
			madensem.put("TIMES_RUN", (double) this.EXEC_N_TIMES);
			medialibm.put("MEAN", medialibmean);
			medialibm.put("TOTAL_RUNTIME", (double) medialib);
			medialibm.put("TIMES_RUN", (double) this.EXEC_N_TIMES);
			System.out.println("Colt: " + cmean + " s");
			System.out.println("MultiArray Sparse: " + mamean + " s");
			System.out.println("MultiArray Dense: " + madensemean + " s");
			System.out.println("Medialib: " + medialibmean + " s");
			// }
			final DefaultBoxAndWhiskerCategoryDataset dbwd1 = new DefaultBoxAndWhiskerCategoryDataset();
			final BoxAndWhiskerItem bawi1 = BoxAndWhiskerCalculator
			        .calculateBoxAndWhiskerStatistics(maRes);
			final BoxAndWhiskerItem bawi2 = BoxAndWhiskerCalculator
			        .calculateBoxAndWhiskerStatistics(coltRes);
			final BoxAndWhiskerItem bawi3 = BoxAndWhiskerCalculator
			        .calculateBoxAndWhiskerStatistics(madenseRes);
			final BoxAndWhiskerItem bawi4 = BoxAndWhiskerCalculator
			        .calculateBoxAndWhiskerStatistics(medialibRes);

			dbwd1.add(bawi1, "Sparse MultiArray", 0);
			dbwd1.add(bawi2, "Sparse Colt Matrix1D", 1);
			dbwd1.add(bawi3, "Dense MultiArray", 2);
			dbwd1.add(bawi4, "Dense MediaLib", 3);

			// ArrayList<Title> subtitles = new ArrayList<Title>();
			final Title size_a = new TextTitle("Size of arrays per run: "
			        + this.ARRAY_SIZE);
			final Title num_zero = new TextTitle(
			        "Number of zero elements per array: " + this.N_ZEROS
			                * this.ARRAY_SIZE);
			final Title num_a = new TextTitle("Number of arrays per run: "
			        + this.N_ARRAYS);
			final Title num_rep = new TextTitle(
			        "Number of repetitions per instance: " + this.EXEC_N_TIMES);
			// cdcp.setDataset(i,dbwd1);
			// cdcp.add(cp);
			final CategoryPlot cp = new CategoryPlot(dbwd1, ca, va,
			        new BoxAndWhiskerRenderer());
			// cdcp.add(cp);
			final JFreeChart jfchart = new JFreeChart(cp);
			final ChartPanel cpan = new ChartPanel(jfchart);
			cpan.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			jf.getContentPane().add(cpan);
			// ChartFrame frame = new ChartFrame("",jf);
			// frame.setVisible(true);
			jfchart.addSubtitle(size_a);
			jfchart.addSubtitle(num_zero);
			jfchart.addSubtitle(num_a);
			jfchart.addSubtitle(num_rep);

			final CSVWriter cw = new CSVWriter();
			cw.writeStatsMap(System.getProperty("user.home"), "colt.csv",
			        coltsm);
			cw.writeStatsMap(System.getProperty("user.home"), "ma.csv", masm);
			cw.writeStatsMap(System.getProperty("user.home"), "madense.csv",
			        madensem);
			cw.writeStatsMap(System.getProperty("user.home"), "medialib.csv",
			        madensem);
		}
		final JFreeChart xy = ChartFactory.createXYLineChart(
		        "Array size versus average runtime", "Array size [units]",
		        "Runtime [s]", xyd1, PlotOrientation.VERTICAL, true, true,
		        false);
		final ChartFrame cf = new ChartFrame(
		        "Comparison of runtimes for dot-product calculation of double precision arrays",
		        xy);
		cf.setSize(640, 480);
		cf.setVisible(true);
		// CategoryAxis ca = new CategoryAxis("Instances");
		// ValueAxis va = new NumberAxis("s");
		// cdcp.setDomainAxis(ca);
		// cdcp.setRangeAxis(va);
		// //
		// JFreeChart jfc = new JFreeChart(cdcp);
		// ChartFrame cf = new ChartFrame("Comparison of runtimes for
		// dot-product calculation of double precision arrays",jfc);
		// //jfc.setSubtitles(subtitles);
		// cf.setVisible(true);
		jf.setSize(640, 480);
		jf.setVisible(true);
	}

}
