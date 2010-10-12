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
package maltcms.experimental.operations;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import maltcms.datastructures.array.ArrayFactory;
import maltcms.datastructures.array.IArrayD2Double;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.constraint.ConstraintFactory;
import maltcms.experimental.datastructures.FeatureVectorFactory;
import maltcms.tools.ArrayTools;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.interpolation.UnivariateRealInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialsUtils;
import org.jdom.Element;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import apps.Maltcms;
import cross.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.FragmentTools;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
public class DTW implements IAlignment, IWorkflowElement, Serializable {

	/**
     * 
     */
	private static final long serialVersionUID = -4958589994859792653L;

	private String lhsID, rhsID;

	private TwoFeatureVectorOperation tfvo = new WeightedCosine();

	private List<Point> alignmentMap = Collections.emptyList();

	private Area a = null;

	private double defaultValue = Double.NEGATIVE_INFINITY;

	private IOptimizationFunction iof = new ThreePredecessorsOptimization();

	private IWorkflow iw = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.experimental.operation.PairwiseFeatureVectorSequenceOperation
	 * #apply(java.util.List, java.util.List)
	 */
	@Override
	public Double apply(List<IFeatureVector> l1, List<IFeatureVector> l2) {
		final ArrayFactory f = Factory.getInstance().getObjectFactory()
		        .instantiate(ArrayFactory.class);
		final IArrayD2Double alignment = f.create(l1.size(), l2.size(),
		        this.defaultValue, this.a);
		final IArrayD2Double pwvalues = f.createSharedLayout(alignment);
		saveImage(this.lhsID + "-" + this.rhsID + "_layout", alignment,
		        new ArrayList<Point>());
		iof.init(l1, l2, alignment, pwvalues, this.tfvo);
		double percentDone = 0;
		long elemCnt = 0;
		long elements = alignment.getNumberOfStoredElements();
		long partCnt = 0;
		int[] point = new int[] { 0, 0 };
		for (int i = 0; i < alignment.rows(); i++) {
			final int[] bounds = alignment.getColumnBounds(i);
			for (int j = bounds[0]; j < bounds[0] + bounds[1]; j++) {
				percentDone = ArrayTools.calcPercentDone(elements, elemCnt);
				partCnt = ArrayTools.printPercentDone(percentDone, 10, partCnt,
				        System.out);
				elemCnt++;
				point[0] = i;
				point[1] = j;
				iof.apply(point);
			}
		}
		// ((ThreePredecessorsOptimization) iof).showCumScoreMatrix();
		// ((ThreePredecessorsOptimization) iof).showPwScoreMatrix();
		percentDone = ArrayTools.calcPercentDone(elements, elemCnt);
		ArrayTools.printPercentDone(percentDone, 10, partCnt, System.out);
		this.alignmentMap = iof.getTrace();
		System.out.println("Number of Points in trace: "
		        + this.alignmentMap.size());

		// minimum of four points for each polynomial

		// UnivariateRealInterpolator interpolator = new SplineInterpolator();
		// try {
		//
		List<double[]> l = new ArrayList<double[]>();
		// create list of all aligned points
		for (int i = 0; i < this.alignmentMap.size() - 1; i++) {
			Point p = this.alignmentMap.get(i);
			l.add(new double[] { p.x, p.y, pwvalues.get(p.x, p.y) });
		}

		// ContinuityArgmaxNodeBuilder canb10 = new
		// ContinuityArgmaxNodeBuilder(10);
		// List<Point> interp10 = canb10.eval(l, 3);
		//
		// ContinuityArgmaxNodeBuilder canb20 = new
		// ContinuityArgmaxNodeBuilder(20);
		// List<Point> interp20 = canb20.eval(l, 3);
		//
		// ContinuityArgmaxNodeBuilder canb100 = new
		// ContinuityArgmaxNodeBuilder(
		// 100);
		// List<Point> interp100 = canb100.eval(l, 3);
		// // remove start and end point -> fixed anchors
		// // double[] start = l.remove(0);
		// // double[] end = l.remove(l.size() - 1);
		// // List<double[]> anchors = new ArrayList<double[]>();
		// //
		// // Point q = this.alignmentMap.get(this.alignmentMap.size() - 1);
		// // if (q.getX() == p.getX() || q.getY() == p.getY()) {
		// // l.remove(l.size() - 1);
		// // }
		// // l.add(q);
		// // System.out.println("Number of Surviving Points: " + l.size());
		// // double[] x = new double[l.size()];
		// // double[] y = new double[l.size()];
		// // for (int i = 0; i < l.size(); i++) {
		// // x[i] = l.get(i).getX();
		// // y[i] = l.get(i).getY();
		// // }
		// // UnivariateRealFunction function = interpolator.interpolate(x, y);
		// // List<Point> interp = new ArrayList<Point>();
		// // for (int i = 0; i < alignment.rows(); i += 10) {
		// // Point ip = new Point(i, (int) (Math.round(function
		// // .value((double) i))));
		// // interp.add(ip);
		// // }
		// // if (interp.get(interp.size() - 1).x != alignment.columns() - 1) {
		// // Point ip = new Point(alignment.columns() - 1,
		// // (int) (Math.round(function.value((double) alignment
		// // .columns() - 1))));
		// // interp.add(ip);
		// // }
		// BufferedImage bi = createImage(alignment);
		// addAlignmentMap(bi, this.alignmentMap, Color.WHITE);
		// // addAlignmentMap(bi, l, Color.LIGHT_GRAY);
		// addAlignmentMap(bi, interp10, Color.RED);
		// addAlignmentMap(bi, interp20, Color.BLUE);
		// addAlignmentMap(bi, interp100, Color.GREEN);
		// saveImage(this.lhsID + "-" + this.rhsID
		// + "_interpolatedLayoutWithTrace", bi);
		// // } catch (MathException e) {
		// //
		// // e.printStackTrace();
		// // }
		// BufferedImage rp = createRecurrencePlot(pwvalues, 0.99);
		// saveImage("recurrencePlot-" + this.lhsID + "-" + this.rhsID, rp);
		// saveImage(this.lhsID + "-" + this.rhsID + "_layoutWithTrace",
		// alignment, this.alignmentMap);
		return iof.getOptimalValue();
	}

	public abstract class NodeBuilder {
		private List<double[]> nodes = Collections.emptyList();

		public abstract List<Point> eval(List<double[]> points, int polyOrder);

		public double[] getNodesByDimension(int i, double[] dim) {
			double[] dimt = dim == null ? new double[nodes.size()] : dim;
			for (double[] d : nodes) {
				dimt[i] = d[i];
			}
			return dimt;
		}

		public List<Point> getPointList(List<double[]> points) {
			List<Point> l = new ArrayList<Point>();
			for (double[] d : points) {
				l.add(new Point((int) d[0], (int) d[1]));
			}
			return l;
		}
	}

	public class IdentityNodeBuilder extends NodeBuilder {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * maltcms.experimental.operations.DTW.NodeBuilder#eval(java.util.List)
		 */
		@Override
		public List<Point> eval(List<double[]> points, int polyOrder) {
			super.nodes = points;
			return getPointList(super.nodes);
		}

	}

	public class ContinuityArgmaxNodeBuilder extends NodeBuilder {

		// private final double[] penaltyLookupTable;

		private final double scale = 1.0;

		private final int maxlevel;

		public ContinuityArgmaxNodeBuilder(int levels) {
			this.maxlevel = levels;
			// // this.scale = scale;
			// // penaltyLookupTable = new double[(int) (1.0 * scale) + 1];
			// // for (int i = 0; i < penaltyLookupTable.length; i++) {
			// // penaltyLookupTable[i] = Math.exp(-Math.pow(
			// // (((double) i) / scale), 2.0d) / 2.0d);
			// // System.out.println("p(" + (((double) i) / scale) + ")="
			// // + penaltyLookupTable[i]);
			// // }
			//
		}

		private int map(double v, double min, double max, double scale) {
			return (int) ((v / (max - min)) * scale);
		}

		@Override
		public List<Point> eval(List<double[]> points, int polyOrder) {
			Tuple2D<double[], double[]> minMax = getMinMax(points);
			List<double[]> anchors = calcArgmax(points, minMax.getFirst(),
			        minMax.getSecond());
			System.out.println("Found " + anchors.size() + " anchors!");
			System.out.println("Anchors: ");
			for (double[] d : anchors) {
				System.out.println(Arrays.toString(d));
			}
			return getPointList(anchors);
			// the following rules apply:
			// -the first element and the last are always used as anchors
			//
			// -let a be the previous valid anchor and a'' the next valid
			// anchor,
			// then, either x(a) < x(b) < x(c) or y(a) < y(b) < y(c) must hold
			// to guarantee monotonicity. a < b < c define an open interval
			// (a,c):={b\inNXN| a<b<c}
			// -Additionally, (b_{1},...,b_{k}) = argmax
			// (z(a),...,z(b),...,z(c))
			// must hold, such
			// that all b have the maximum value within the open interval
			// defined by a and c, (a, c), such that
			// a and c are not members of the interval.

			// how many b_{i} will we usually find? how is this connected to the
			// interval size?
			//

			// Using a polynomial of order n requires n+1 points within a
			// partition to
			// uniquely determine the polynomial.

			// level 1: select start and end
			// find arg max between start and end -> (start,end), preferrably
			// close to the midpoint -> penalty (1/exp((x1-x2)^{2}/2))*z(a)
			// then subdivide into (start,s1), and (s1,end)

		}

		private Tuple2D<double[], double[]> getMinMax(List<double[]> points) {
			double[] min = new double[points.get(0).length];
			double[] max = new double[points.get(0).length];
			Arrays.fill(min, Double.POSITIVE_INFINITY);
			Arrays.fill(max, Double.NEGATIVE_INFINITY);
			for (double[] d : points) {
				for (int i = 0; i < min.length; i++) {
					min[i] = Math.min(d[i], min[i]);
					max[i] = Math.max(d[i], max[i]);
				}
			}
			return new Tuple2D<double[], double[]>(min, max);
		}

		private List<Integer> getArgmax(final List<double[]> l,
		        final double[] a, final double[] c, final double[] min,
		        final double[] max) {
			List<Integer> maxima = new LinkedList<Integer>();
			int maxIdx = 0;
			double maxscore = Double.NEGATIVE_INFINITY;
			int i = 0;
			for (double[] d : l) {
				if (i > 0) {
					double amscore = getScore(d, a, c, min, max, this.scale);
					if (amscore > maxscore) {
						maxIdx = i;
						maxscore = amscore;
					}
				} else {
					maxIdx = i;
					maxscore = getScore(d, a, c, min, max, this.scale);
				}
				i++;
			}
			maxima.add(Integer.valueOf(maxIdx));
			System.out.println("Found " + maxima.size() + " maxima between "
			        + Arrays.toString(a) + " and " + Arrays.toString(c));
			return maxima;
		}

		private List<double[]> calcArgmax(List<double[]> points,
		        final double[] min, final double[] max) {
			final List<double[]> rl = new LinkedList<double[]>();
			int level = 0;
			argmaxRecursion(points, min, max, rl, level);
			rl.add(0, points.get(0));
			rl.add(points.get(points.size() - 1));
			return rl;
		}

		private void argmaxRecursion(List<double[]> lpart, double[] min,
		        double[] max, List<double[]> res, int level) {
			if (lpart.size() <= 2 || level == maxlevel) {
				return;
			}
			// get start
			final double[] a = lpart.remove(0);
			// get end
			final double[] c = lpart.remove(lpart.size() - 1);
			// get dividing element/pivot index
			final List<Integer> amax = getArgmax(lpart, a, c, min, max);

			// retrieve pivot element
			final int nodeIndex = amax.get(0);
			final double[] b = lpart.get(amax.get(0));
			final LinkedList<double[]> rl = new LinkedList<double[]>();
			// add left boundary
			// rl.add(a);
			// recurse into left branch
			System.out.println("Left recursion");
			if (nodeIndex > 0) {
				argmaxRecursion(lpart.subList(0, nodeIndex), min, max, rl,
				        level + 1);
			}
			// add pivot element
			rl.add(b);

			// recurse into right branch
			System.out.println("Right recursion");
			if (nodeIndex < lpart.size() - 1) {
				argmaxRecursion(lpart.subList(nodeIndex, lpart.size() - 1),
				        min, max, rl, level + 1);
			}
			// add right boundary
			// rl.add(c);
			res.addAll(rl);
		}

		private double getScore(final double[] v, final double[] a,
		        final double[] c, final double[] min, final double[] max,
		        final double scale) {
			// double d = 0;
			// for (int i = 0; i < v.length; i++) {
			// d += ((penaltyLookupTable[map(Math.abs(v[i] - a[i]), min[i],
			// max[i], 1.0)] * (penaltyLookupTable[map(Math.abs(v[i]
			// - c[i]), min[i], max[i], scale)])));
			// }
			// return d;
			return v[v.length - 1];
		}
	}

	public final class PChipInterpolator implements UnivariateRealInterpolator {

		public Tuple2D<double[], double[]> getNodes(List<double[]> points) {
			return null;
		}

		private final PolynomialFunction pf;

		public PChipInterpolator() {
			this.pf = PolynomialsUtils.createHermitePolynomial(3);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.commons.math.analysis.interpolation.UnivariateRealInterpolator
		 * #interpolate(double[], double[])
		 */
		@Override
		public UnivariateRealFunction interpolate(double[] arg0, double[] arg1)
		        throws MathException {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private void saveImage(String name, IArrayD2Double alignment,
	        List<Point> l, Color mapColor) {
		BufferedImage bi = createImage(alignment);
		addAlignmentMap(bi, l, mapColor);
		saveImage(name, bi);
	}

	private void saveImage(String name, BufferedImage bi) {
		try {
			ImageIO.write(bi, "PNG", new File(name + ".png"));
		} catch (IOException ex) {
			Logger.getLogger(DTW.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private BufferedImage createImage(IArrayD2Double alignment) {
		final ArrayFactory f = Factory.getInstance().getObjectFactory()
		        .instantiate(ArrayFactory.class);
		BufferedImage bi = f.createLayoutImage(alignment);
		return bi;
	}

	private BufferedImage createRecurrencePlot(IArrayD2Double pwd,
	        double threshold) {
		BufferedImage bi = createImage(pwd);
		Graphics2D g2 = bi.createGraphics();
		Color hit = Color.BLACK;
		Color miss = Color.WHITE;
		for (int x = 0; x < bi.getWidth(); x++) {
			for (int y = 0; y < bi.getHeight(); y++) {
				if (pwd.get(y, x) > threshold) {
					g2.setColor(hit);
					g2.fillRect(x, y, 1, 1);
				} else {
					g2.setColor(miss);
					g2.fillRect(x, y, 1, 1);
				}
			}
		}
		return bi;
	}

	private void addAlignmentMap(BufferedImage bi, List<Point> l, Color mapColor) {
		Color c = mapColor;
		Graphics2D g2 = (Graphics2D) bi.getGraphics();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
		        0.3f));
		g2.setColor(c);
		Point last = null;
		for (Point p : l) {
			if (last == null) {
				last = p;
			} else {
				g2.fillRect(last.y, last.x, p.y - last.y, p.x - last.x);
				last = p;
			}
		}
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
		        0.8f));
		GeneralPath gp = new GeneralPath();
		gp.moveTo(0, 0);
		for (Point p : l) {
			gp.lineTo(p.getY(), p.getX());
		}
		g2.setColor(c);
		g2.draw(gp);
	}

	private void saveImage(String name, IArrayD2Double alignment, List<Point> l) {
		saveImage(name, alignment, l, Color.WHITE);
	}

	@Override
	public List<Point> getMap() {
		return this.alignmentMap;
	}

	@Override
	public String getLHSID() {
		return this.lhsID;
	}

	@Override
	public String getRHSID() {
		return this.rhsID;
	}

	@Override
	public void setLHSID(String lhsid) {
		this.lhsID = lhsid;
	}

	@Override
	public void setRHSID(String rhsid) {
		this.rhsID = rhsid;
	}

	@Override
	public void setPairwiseFeatureVectorOperation(TwoFeatureVectorOperation tfvo) {
		this.tfvo = tfvo;
	}

	@Override
	public void setConstraints(Area a) {
		this.a = a;
	}

	@Override
	public void setDefaultValue(double d) {
		this.defaultValue = d;
	}

	@Override
	public void setIOptimizationFunction(IOptimizationFunction iof) {
		this.iof = iof;
	}

	public static void main(String[] args) {
		Maltcms m = Maltcms.getInstance();
		// set up Maltcms configuration
		CompositeConfiguration cfg = m.parseCommandLine(args);
		cfg.setProperty("maltcms.ui.charts.PlotRunner.headless", Boolean
		        .valueOf(false));
		cfg
		        .setProperty(
		                "cross.datastructures.fragments.VariableFragment.useCachedList",
		                Boolean.valueOf(false));
		cfg
		        .setProperty(
		                "maltcms.datastructures.fragments.PairwiseAlignment.normalizeAlignmentValueByMapWeights",
		                Boolean.valueOf(false));
		String pipeline = "maltcms.commands.fragments.preprocessing.DefaultVarLoader,maltcms.commands.fragments.preprocessing.DenseArrayProducer";
		cfg.setProperty("pipeline", pipeline);
		cfg.setProperty("cross.io.IDataSource", Arrays
		        .asList(new String[] { "maltcms.io.andims.NetcdfDataSource" }));
		// cfg.setProperty("alignment.normalizeAlignmentValueByMapWeights",
		// true);
		cfg.setProperty("alignment.save.cumulative.distance.matrix", true);
		cfg.setProperty("alignment.save.pairwise.distance.matrix", true);
		cfg.setProperty("alignment.algorithm.distance",
		        "maltcms.commands.distances.ArrayCos");
		cfg.setProperty(
		        "maltcms.commands.distances.ArrayCos.compression_weight", 1.0);
		cfg.setProperty("maltcms.commands.distances.ArrayCos.expansion_weight",
		        1.0);
		cfg.setProperty("maltcms.commands.distances.ArrayCos.diagonal_weight",
		        1.0);
		// cfg.save(new BufferedOutputStream(System.out));
		cross.Factory.getInstance().configure(cfg);
		System.out.println("Preparing command sequence");
		ICommandSequence cp = Factory.getInstance().createCommandSequence();

		System.out.println("Running commands");
		TupleND<IFileFragment> res = null;
		while (cp.hasNext()) {
			res = cp.next();
		}
		System.out.println("Finished running commands");
		IFileFragment iff3 = res.get(0);
		IFileFragment iff4 = res.get(1);

		System.out.println("Creating feature vectors");
		// prepare feature vectors
		FeatureVectorFactory fvf = FeatureVectorFactory.getInstance();
		iff3.getChild("binned_intensity_values").setIndex(
		        iff3.getChild("binned_scan_index"));
		List<Array> bi1 = iff3.getChild("binned_intensity_values")
		        .getIndexedArray();
		System.out.println("Length of binned intensity values: " + bi1.size());

		// 
		List<IFeatureVector> l1 = fvf.createBinnedMSFeatureVectorList(iff3,
		        true);
		List<IFeatureVector> l2 = fvf.createBinnedMSFeatureVectorList(iff4,
		        true);

		System.out.println("Preparing alignment");
		// prepare alignment
		AlignmentFactory af = new AlignmentFactory();
		Area constraints = ConstraintFactory.getInstance()
		        .createBandConstraint(0, 0, l1.size(), l2.size(), 0.05);
		IAlignment ia = af.getDTWInstance(Factory.getInstance()
		        .getObjectFactory().instantiate(
		                ThreePredecessorsOptimization.class), Factory
		        .getInstance().getObjectFactory().instantiate(Cosine.class),
		        constraints);
		// set alignment properties
		ia.setLHSID(iff3.getName());
		ia.setRHSID(iff4.getName());
		System.out.println("Calculating alignment");
		// apply and retrieve score
		double v = ia.apply(l1, l2);
		// retrieve map
		List<Point> l = ia.getMap();
		System.out.println(ia.getIOptimizationFunction()
		        .getOptimalOperationSequenceString());
		System.out.println("Done!");
		System.out.println(v);
		System.out.println(ia.getIOptimizationFunction().getOptimalValue());
		IFileFragment ares = Factory.getInstance().getFileFragmentFactory()
		        .create(iff3, iff4, cp.getIWorkflow().getOutputDirectory(ia));
		ia.modify(ares);
		ares.save();
		// ADynamicTimeWarp adtw = Factory.getInstance().getObjectFactory()
		// .instantiate(
		// "maltcms.commands.distances.dtw.MZIDynamicTimeWarp",
		// ADynamicTimeWarp.class);
		// adtw.setIWorkflow(cp.getIWorkflow());
		// IFileFragment iff = adtw.apply(iff3, iff4);
		// List<Tuple2DI> ll = MaltcmsTools.getWarpPath(iff);
		// List<Point> pl2 = PathTools.toPointList(ll);
		// if (pl2.equals(l)) {
		// System.out.println("Both alignments return the same result!");
		// }
		// System.out.println("New: " + l);
		// System.out.println("Old: " + pl2);
		// System.out.println("New value: " + v);
		// System.out.println("Old value: " + adtw.getResult().get());
	}

	@Override
	public IOptimizationFunction getIOptimizationFunction() {
		return this.iof;
	}

	@Override
	public TwoFeatureVectorOperation getPairwiseFeatureVectorOperation() {
		return this.tfvo;
	}

	@Override
	public IWorkflow getIWorkflow() {
		return this.iw;
	}

	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.ALIGNMENT;
	}

	@Override
	public void setIWorkflow(IWorkflow iw) {
		this.iw = iw;
	}

	@Override
	public void appendXML(Element elmnt) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.experimental.datastructures.IFileFragmentModifier#decorate(cross
	 * .datastructures.fragments.IFileFragment)
	 */
	@Override
	public void modify(IFileFragment iff) {
		String arrayComparatorVariableName = Factory.getInstance()
		        .getConfiguration().getString(
		                "var.alignment.pairwise_distance.class",
		                "pairwise_distance_class");
		String arrayDistanceClassName = Factory.getInstance()
		        .getConfiguration().getString(
		                "var.alignment.cumulative_distance.class",
		                "cumulative_distance_class");
		String alignmentClassVariableName = Factory.getInstance()
		        .getConfiguration().getString("var.alignment.class",
		                "alignment_class");
		FragmentTools.createString(iff, arrayComparatorVariableName, this.tfvo
		        .getClass().getName());
		FragmentTools.createString(iff, arrayDistanceClassName, this.iof
		        .getClass().getName());
		FragmentTools.createString(iff, alignmentClassVariableName, this
		        .getClass().getName());
		ArrayDouble.D0 result = new ArrayDouble.D0();
		result.set(getIOptimizationFunction().getOptimalValue());
		final String distvar = Factory.getInstance().getConfiguration()
		        .getString("var.alignment.distance", "distance");
		final IVariableFragment dvar = new VariableFragment(iff, distvar);
		dvar.setArray(result);
		this.iof.modify(iff);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
	 * )
	 */
	@Override
	public void configure(Configuration cfg) {

	}

}
