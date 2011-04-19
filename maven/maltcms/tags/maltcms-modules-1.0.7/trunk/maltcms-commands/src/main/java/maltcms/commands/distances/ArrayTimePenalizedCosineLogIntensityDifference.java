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
package maltcms.commands.distances;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import maltcms.commands.filters.array.AdditionFilter;
import maltcms.commands.filters.array.LogFilter;
import maltcms.datastructures.ms.ChromatogramFactory;
import maltcms.datastructures.ms.ProfileChromatogram1D;
import maltcms.tools.ArrayTools;
import maltcms.tools.ImageTools;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
import cross.datastructures.fragments.FileFragment;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class ArrayTimePenalizedCosineLogIntensityDifference implements
        IArrayDoubleComp {

	private volatile ArrayCos dot = new ArrayCos();

	@Configurable(name = "expansion_weight")
	private double wExp = 1.0d;
	@Configurable(name = "compression_weight")
	private double wComp = 1.0d;
	@Configurable(name = "diagonal_weight")
	private double wDiag = 2.0d;

	@Configurable
	private double logIntensityTolerance = 2.0d;

	@Configurable
	private double logIntensityEpsilon = 0.01d;

	@Configurable
	private double rtTolerance = 2.5d;

	@Configurable
	private double rtEpsilon = 0.01d;

	@Override
	public Double apply(final int i1, final int i2, final double time1,
	        final double time2, final Array t1, final Array t2) {
		// if no time is supplied, use 1 as default -> cosine/dot product
		// similarity
		final double weight = ((time1 == -1) || (time2 == -1)) ? 1.0d
		        : Math
		                .exp(-((time1 - time2) * (time1 - time2) / (2.0d * this.rtTolerance * this.rtTolerance)));
                // 1 for perfect time correspondence, 0 for really bad time
		// correspondence (towards infinity)
		if (weight - this.rtEpsilon < 0) {
			return Double.NEGATIVE_INFINITY;
		}
		// add 1
		AdditionFilter af = new AdditionFilter(1.0f);
		// apply log10 elementwise
		LogFilter lf = new LogFilter();
		// elementwise difference operation
		Array diff = ArrayTools.diff(lf.apply(af.apply(t1.copy())), lf.apply(af
		        .apply(t2.copy())));
		double sumOfSquares = ArrayTools.integrate(ArrayTools.sq(diff));
		// final double t1i = Math.log10(ArrayTools.integrate(t1));
		// final double t2i = Math.log10(ArrayTools.integrate(t2));
		// System.out.println("I1:" + t1i + " I2:" + t2i + " sumOfSquares: "
		// + sumOfSquares);
		double divisor = 2.0d * this.logIntensityTolerance
		        * this.logIntensityTolerance;
		double arg = -(sumOfSquares / (divisor));
		if (arg != Double.NEGATIVE_INFINITY)
			System.out.println("SumOfSquares: " + sumOfSquares + " Argument: "
			        + arg + " divisor: " + divisor);
		// if no time is supplied, use 1 as default -> cosine/dot product
		// similarity
		final double iweight = Math.exp(arg);
		// 1 for perfect time correspondence, 0 for really bad time
		// correspondence (towards infinity)
		// System.out.println("Intensity relation weight: " + iweight);
		if (iweight - this.logIntensityEpsilon < 0) {
			return Double.NEGATIVE_INFINITY;
		}
		final double dotP = this.dot.apply(i1, i2, time1, time2, t1, t2);
		// System.out.println("cosine: " + dotP);
		// Robinson
		double score = dotP * weight * iweight;
		return score;
	}

	@Override
	public void configure(final Configuration cfg) {
		this.rtTolerance = cfg.getDouble(this.getClass().getName()
		        + ".rtTolerance", 2.0d);
		this.rtEpsilon = cfg.getDouble(
		        this.getClass().getName() + ".rtEpsilon", 0.01d);
		this.logIntensityTolerance = cfg.getDouble(this.getClass().getName()
		        + ".rtTolerance", 2.0d);
		this.logIntensityEpsilon = cfg.getDouble(this.getClass().getName()
		        + ".rtEpsilon", 0.01d);
		this.wComp = cfg.getDouble(this.getClass().getName()
		        + ".compression_weight", 1.0d);
		this.wExp = cfg.getDouble(this.getClass().getName()
		        + ".expansion_weight", 1.0d);
		this.wDiag = cfg.getDouble(this.getClass().getName()
		        + ".diagonal_weight", 1.0d);
		StringBuilder sb = new StringBuilder();
		sb
		        .append("logIntensityTolerance: " + this.logIntensityTolerance
		                + ", ");
		sb.append("logIntensityEpsilon: " + this.logIntensityEpsilon + ", ");
		sb.append("wComp: " + this.wComp + ", ");
		sb.append("wExp: " + this.wExp + ", ");
		sb.append("wDiag: " + this.wDiag);
		Logging.getLogger(this).info("Parameters of class {}: {}",
		        this.getClass().getName(), sb.toString());
	}

	public double getCompressionWeight() {
		return this.wComp;
	}

	public double getDiagonalWeight() {
		return this.wDiag;
	}

	public double getExpansionWeight() {
		return this.wExp;
	}

	@Override
	public boolean minimize() {
		return false;
	}

	public static void main(String[] args) {
		// //common peaks
		// Array[] motherChrom = new Array[80];
		// ArrayDouble.D1 mct = new ArrayDouble.D1(motherChrom.length);
		// ArrayDouble.D1 mctic = new ArrayDouble.D1(motherChrom.length);
		// for (int i = 0; i < motherChrom.length; i++) {
		// motherChrom[i] = ArrayTools.randomUniform(500, 0, 50000000);
		// mct.set(i, (i + 1) * 10);
		// mctic.set(i, ArrayTools.integrate(motherChrom[i]));
		// }

		// Array[] as = new Array[50];
		// ArrayDouble.D1 at = new ArrayDouble.D1(as.length);
		// ArrayDouble.D1 atic = new ArrayDouble.D1(as.length);
		// // ArrayTools.randomUniform(500, 0, 50000000);
		// for (int i = 0; i < as.length; i++) {
		// as[i] = ArrayTools.randomUniform(500, 0, 50000000);
		// at.set(i, (i + 1) * 10);
		// atic.set(i, ArrayTools.integrate(as[i]));
		// }
		//
		// // ///////////////////////////////////////////////////////
		// List<Point> matchedPositions = new ArrayList<Point>();
		// Array[] bs = new Array[50];
		// ArrayDouble.D1 bt = new ArrayDouble.D1(bs.length);
		// ArrayDouble.D1 btic = new ArrayDouble.D1(bs.length);
		// for (int i = 0; i < bs.length; i++) {
		// if (Math.random() > 0.2) {
		// bs[i] = ArrayTools.sum(as[i], ArrayTools.randomGaussian(500, 0,
		// 1000));
		// // bt.set(i,)
		// matchedPositions.add(new Point(i, i));
		// } else {
		// bs[i] = ArrayTools.randomUniform(500, 0, 50000000);
		// }
		// //
		// btic.set(i, ArrayTools.integrate(bs[i]));
		// }
		// Array arts = ArrayTools.randomGaussian(as.length, 0, 20);
		//
		// arts = ArrayTools.sum(at, arts);
		// Array brts = ArrayTools.sum(arts,
		// ArrayTools.randomGaussian(bs.length,
		// 0, 5));
		// // brts = ArrayTools.sum(bt, brts);
		// System.out.println(arts);
		// System.out.println(brts);
		//
		// XYChart xyc = new XYChart("Test", new String[] { "a1", "b1" },
		// new Array[] { atic, btic }, new Array[] { arts, brts }, "RT",
		// "Intensity");
		// ChartFrame cf = new ChartFrame("Test", new JFreeChart(xyc.create()),
		// true);
		// cf.setVisible(true);
		// cf.pack();
		Factory f = Factory.getInstance();
		try {
			f.configure(new PropertiesConfiguration(args[0]));
		} catch (ConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ChromatogramFactory cf = new ChromatogramFactory();
		ProfileChromatogram1D c1d = cf
		        .createProfileChromatogram1D(new FileFragment(new File(args[1])));
		ProfileChromatogram1D c2d = cf
		        .createProfileChromatogram1D(new FileFragment(new File(args[2])));
		ArrayTimePenalizedCosineLogIntensityDifference atp = new ArrayTimePenalizedCosineLogIntensityDifference();
		File outdir = new File("atpclid");
		outdir.mkdirs();
		int maxCnt = 10;
		int cnt = 1;
		// Index artsIdx = arts.getIndex();
		// Index brtsIdx = brts.getIndex();
		for (int c1 = 0; c1 < 10; c1++) {
			double lit = c1;
			// for (int c2 = 0; c2 < maxCnt; c2++) {
			// double lie = c2 / 100.0d;
			// for (int c3 = 0; c3 < maxCnt; c3++) {
			// double rtt = c3;
			// for (int c4 = 0; c4 < maxCnt; c4++) {
			System.out
			        .println("Creating image " + (cnt++) + "/" + (3 * maxCnt));
			// double rte = c4 / 100.0d;
			ArrayDouble.D2 arr = new ArrayDouble.D2(
			        c1d.getNumberOfScans() / 100, c2d.getNumberOfScans() / 100);
			atp.logIntensityTolerance = lit;
			atp.logIntensityEpsilon = 0;
			atp.rtEpsilon = 0;
			atp.rtTolerance = 10;
			boolean save = false;
			double val = 0;
			List<Array> l1 = c1d.getBinnedIntensities();
			for (int i = 0; i < c1d.getNumberOfScans() / 100; i++) {
				// artsIdx.set(i);
				double rt1 = c1d.getScan(i).getScanAcquisitionTime();
				Array a1 = l1.get(i);
				for (int j = 0; j < c2d.getNumberOfScans() / 100; j++) {
					// System.out.println(as[i]);
					// System.out.println(bs[j]);
					// brtsIdx.set(j);
					List<Array> l2 = c2d.getBinnedIntensities();

					val = atp.apply(-1, -1, rt1, c2d.getScan(j)
					        .getScanAcquisitionTime(), a1, l2.get(j));
					arr.set(i, j, val);
					if (val != 0) {
						save = true;
					}
					// System.out.println("" + arr.get(i, j));
				}
			}
			if (save) {
				BufferedImage bi = ImageTools.makeImage2D(arr, 256);
				// try {
				// ImageIO.write(bi, "png", new File(
				// "atpclid/img-LIT" + lit + "-LIE" + lie
				// + "-RTT" + rtt + "-RTE" + rte
				// + ".png"));
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				try {
					ImageIO.write(bi, "png", new File("atpclid/img-LIT" + lit
					        + ".png"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// }
			// }
			// }
		}

		// JLabel jl = new JLabel(new ImageIcon(bi));
		// JFrame jf = new JFrame();
		// jf.add(jl);
		// jf.setVisible(true);
		// jf.pack();
	}
}
