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

package maltcms.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import maltcms.commands.distances.IRecurrence;
import maltcms.datastructures.array.IArrayD2Double;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.IndexIterator;
import ucar.nc2.Dimension;
import cross.Factory;
import cross.IConfigurable;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.WorkflowSlot;
import cross.io.csv.CSVWriter;
import cross.tools.FragmentTools;
import cross.tools.StringTools;

/**
 * Utility class providing methods for handling of the path obtained from
 * alignment.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class PathTools implements IConfigurable {

	public enum Direction {
		W, N, NW;
	}

	protected static final Logger log = Logging.getLogger(PathTools.class);

	public static boolean allInfinite(final double a, final double b,
	        final double c) {
		return (Double.isInfinite(a) && Double.isInfinite(b) && Double
		        .isInfinite(c));
	}

	/**
	 * Constructs List of pairs of indices from array representation.
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public static List<Tuple2DI> fromArrays(final Array i, final Array j) {
		final IndexIterator jiter = j.getIndexIterator();
		final IndexIterator iiter = i.getIndexIterator();
		final ArrayList<Tuple2DI> al = new ArrayList<Tuple2DI>(i.getShape()[0]);
		while (iiter.hasNext() && jiter.hasNext()) {
			al.add(new Tuple2DI(iiter.getIntNext(), jiter.getIntNext()));
		}
		return al;
	}

	/**
	 * Adds VariableFragments and arrays corresponding to path to parent
	 * FileFragment.
	 * 
	 * @param parent
	 * @param al
	 * @param ia
	 */
	public static void getFragments(final IFileFragment parent,
	        final List<Tuple2DI> al, final IArrayD2Double ia) {
		final Tuple2D<Array, Array> t = PathTools.toArrays(al);
		final Dimension d = new Dimension("steps", al.size(), true, false,
		        false);
		final IVariableFragment pathDist = new VariableFragment(parent, Factory
		        .getInstance().getConfiguration().getString(
		                "var.warp_path_distance", "warp_path_distance"));
		final IVariableFragment wpi = new VariableFragment(parent, Factory
		        .getInstance().getConfiguration().getString("var.warp.path.i",
		                "warp_path_i"));
		wpi.setDimensions(new Dimension[] { d });
		pathDist.setDimensions(new Dimension[] { d });
		final ArrayDouble.D1 dists = new ArrayDouble.D1(al.size());
		int i = 0;
		for (final Tuple2DI tp : al) {
			dists.set(i, ia.get(tp.getFirst(), tp.getSecond()));
			i++;
		}
		pathDist.setArray(dists);
		final IVariableFragment wpj = new VariableFragment(parent, Factory
		        .getInstance().getConfiguration().getString("var.warp.path.j",
		                "warp_path_j"));
		wpj.setDimensions(new Dimension[] { d });
		wpi.setArray(t.getFirst());
		wpj.setArray(t.getSecond());
	}

	public static int nequal(final double a, final double b, final double c) {
		if ((a == b) && (a == c)) {
			return 3;
		}
		if (((a == b) && (a != c)) || ((a == c) && (a != b))) {
			return 2;
		}
		return 0;
	}

	public static int ninf(final double a, final double b, final double c) {
		int n = (Double.isInfinite(a) ? 1 : 0);
		n += (Double.isInfinite(b) ? 1 : 0);
		n += (Double.isInfinite(c) ? 1 : 0);
		return n;
	}

	/**
	 * Inverts pairs of matching indices.
	 * 
	 * @param path
	 * @return
	 */
	public static ArrayList<Tuple2DI> swapPath(final ArrayList<Tuple2DI> path) {
		final ArrayList<Tuple2DI> swapped = new ArrayList<Tuple2DI>(path.size());
		for (final Tuple2DI t : path) {
			swapped.add(new Tuple2DI(t.getSecond(), t.getFirst()));
		}
		return swapped;
	}

	/**
	 * Constructs array representation from List of pairs of indices.
	 * 
	 * @param al
	 * @return
	 */
	public static Tuple2D<Array, Array> toArrays(final List<Tuple2DI> al) {
		final ArrayInt.D1 js = new ArrayInt.D1(al.size());
		final ArrayInt.D1 is = new ArrayInt.D1(al.size());
		final IndexIterator jiter = js.getIndexIterator();
		final IndexIterator iiter = is.getIndexIterator();
		final Iterator<Tuple2DI> iter = al.iterator();
		while (iter.hasNext() && jiter.hasNext() && iiter.hasNext()) {
			final Tuple2DI t = iter.next();
			iiter.setIntNext(t.getFirst());
			jiter.setIntNext(t.getSecond());
		}
		return new Tuple2D<Array, Array>(is, js);
	}

	private int nbranches = 0;

	private String symbolicPath = "";

	private List<Tuple2DI> map = null;

	private int nexp, ncomp, ndiag;

	public int getNexp() {
		return nexp;
	}

	public int getNcomp() {
		return ncomp;
	}

	public int getNdiag() {
		return ndiag;
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param l
	 * @param sb
	 * @return
	 */
	@Deprecated
	public Tuple2DI addStepN(final Integer a, final Integer b,
	        final List<Tuple2DI> l, final StringBuffer sb) {
		sb.append("+");
		PathTools.log.debug("NORTH");
		final Tuple2DI t = new Tuple2DI(Math.max(0, a - 1), Math.max(0, b));
		l.add(t);
		return t;
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param l
	 * @param sb
	 * @return
	 */
	@Deprecated
	public Tuple2DI addStepNW(final Integer a, final Integer b,
	        final List<Tuple2DI> l, final StringBuffer sb) {
		sb.append("o");
		PathTools.log.debug("NORTHWEST");
		final Tuple2DI t = new Tuple2DI(Math.max(0, a - 1), Math.max(0, b - 1));
		l.add(t);
		return t;
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param l
	 * @param sb
	 * @return
	 */
	@Deprecated
	public Tuple2DI addStepW(final Integer a, final Integer b,
	        final List<Tuple2DI> l, final StringBuffer sb) {
		sb.append("-");
		PathTools.log.debug("WEST");
		final Tuple2DI t = new Tuple2DI(Math.max(0, a), Math.max(0, b - 1));
		l.add(t);
		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
	 * )
	 */
	@Override
	public void configure(final Configuration cfg) {

	}

	/**
	 * 
	 * @param map1
	 * @return
	 */
	@Deprecated
	public List<Tuple2DI> createSmoothPath(final List<Tuple2DI> map1) {
		final ArrayList<Tuple2DI> ret = new ArrayList<Tuple2DI>();
		final Tuple2DI start = new Tuple2DI(-1, -1);
		PathTools.log.debug("Creating smooth path!");
		final Tuple2DI finish = new Tuple2DI(map1.get(map1.size() - 1)
		        .getFirst() + 1, map1.get(map1.size() - 1).getSecond() + 1);
		int lastKink = 0;
		for (int i = 0; i < map1.size(); i++) {
			Tuple2DI last = null;
			if (i > 0) {
				last = map1.get(i - 1);
			} else {
				last = start;
			}
			final Tuple2DI current = map1.get(i);
			Tuple2DI next = null;
			if (i < map1.size() - 1) {
				next = map1.get(i + 1);
			} else {
				next = finish;
			}
			if ((current != null) && (next != null) && (last != null)) {// kinks
				final int di_lc = Math
				        .abs(last.getFirst() - current.getFirst());
				final int dj_lc = Math.abs(last.getSecond()
				        - current.getSecond());
				final int di_cn = Math
				        .abs(next.getFirst() - current.getFirst());
				final int dj_cn = Math.abs(next.getSecond()
				        - current.getSecond());
				if ((di_lc == 0) && (dj_lc == 1) && (di_cn == 1)
				        && (dj_cn == 0) && (i - lastKink > 1)) {// right corner
					PathTools.log.debug("Left Kink at {}", current);
					lastKink = i;
				} else {
					if ((di_lc == 1) && (dj_lc == 0) && (di_cn == 0)
					        && (dj_cn == 1) && (i - lastKink > 1)) {
						PathTools.log.debug("Right Kink at {}", current);
						lastKink = i;
					} else {
						PathTools.log.debug("King detector not matching at {}",
						        current);
						ret.add(current);
					}
				}
			} else {
				PathTools.log.debug("No Kink at {}", current);
				ret.add(current);
			}
		}
		return ret;
	}

	public void decorate(final IFileFragment parent, final IArrayD2Double ia) {
		PathTools.getFragments(parent, this.map, ia);
		// Tuple2D<Array, Array> tp = PathTools.toArrays(map);
		// Array ai = tp.getFirst();
		// Array aj = tp.getSecond();
		// IVariableFragment path_i = new VariableFragment(parent, Factory
		// .getInstance().getConfiguration().getString("var.warp_path_i",
		// "warp_path_i"));
		// IVariableFragment path_j = new VariableFragment(parent, Factory
		// .getInstance().getConfiguration().getString("var.warp_path_j",
		// "warp_path_j"));
		// // VariableFragment weight = FragmentTools.getVariable(parent,
		// // ArrayFactory.getConfiguration
		// // ().getString("var.warp_weight","warp_weight"));
		// // weight.setArray(weight_sum);
		// path_i.setArray(ai);
		// path_j.setArray(aj);
		PathTools.log.debug("Found {} potential seeds for backtracking!",
		        this.nbranches);
		PathTools.log.debug("Created and set variables");
	}

	/**
	 * @return the map
	 */
	public List<Tuple2DI> getMap() {
		return this.map;
	}

	/**
	 * @return the symbolicPath
	 */
	public String getSymbolicPath() {
		return this.symbolicPath;
	}

	/**
	 * 
	 * @param neq
	 * @param val
	 * @param i
	 * @param j
	 * @param k
	 * @param a
	 * @param b
	 * @param l
	 * @param sb
	 */
	@Deprecated
	public void handleOneInfinite(final int neq, final double val,
	        final double i, final double j, final double k, final Integer a,
	        final Integer b, final List<Tuple2DI> l, final StringBuffer sb) {
		if (Double.isInfinite(i)) {// j and k cannot be infinite
			if (neq == 2) {// let j win, if j = k
				addStepNW(a, b, l, sb);
			} else {
				if (val == j) {
					addStepNW(a, b, l, sb);
				} else if (val == k) {
					addStepN(a, b, l, sb);
				}
			}

		} else if (Double.isInfinite(j)) {
			if (neq == 2) {// Problem, branching at i,k
				this.nbranches++;
				PathTools.log
				        .debug(
				                "BACKTRACKING SEED: WEST and NORTH have equal values {}={}",
				                i, k);
				addStepN(a, b, l, sb);
			} else {
				if (val == i) {
					addStepW(a, b, l, sb);
				} else if (val == k) {
					addStepN(a, b, l, sb);
				}
			}
		} else if (Double.isInfinite(k)) {
			if (neq == 2) {// let j win, if j = i
				addStepNW(a, b, l, sb);
			} else {
				if (val == j) {
					addStepNW(a, b, l, sb);
				} else if (val == i) {
					addStepW(a, b, l, sb);
				}
			}

		}
	}

	/**
	 * @param i
	 * @param j
	 * @param k
	 * @param a
	 * @param b
	 * @param l
	 * @param sb
	 */
	@Deprecated
	public void handleThreeEqual(final double i, final double j,
	        final double k, final Integer a, final Integer b,
	        final List<Tuple2DI> l, final StringBuffer sb) {
		this.nbranches++;
		PathTools.log
		        .debug(
		                "BACKTRACKING SEED: WEST and NORTH and NORTHWEST have equal values {}={}={}",
		                new Object[] { i, j, k });
		addStepNW(a, b, l, sb);
	}

	/**
	 * 
	 * @param i
	 * @param j
	 * @param k
	 * @param a
	 * @param b
	 * @param l
	 * @param sb
	 */
	@Deprecated
	public void handleTwoEqual(final double i, final double j, final double k,
	        final Integer a, final Integer b, final List<Tuple2DI> l,
	        final StringBuffer sb) {
		this.nbranches++;
		if ((i == j)) {// prefer the diagonal
			PathTools.log
			        .debug(
			                "BACKTRACKING SEED: WEST and NORTHWEST have equal values {}={}",
			                i, j);
			addStepNW(a, b, l, sb);
		} else if ((j == k)) {// prefer the diagonal
			PathTools.log
			        .debug(
			                "BACKTRACKING SEED: WEST and NORTHWEST have equal values {}={}",
			                k, j);
			addStepNW(a, b, l, sb);
		} else if ((i == k)) {// requires backtracking!!!
			PathTools.log
			        .debug(
			                "BACKTRACKING SEED: WEST and NORTH have equal values {}={}",
			                i, k);
			addStepN(a, b, l, sb);
		}
	}

	/**
	 * 
	 * @param i
	 * @param j
	 * @param k
	 * @param a
	 * @param b
	 * @param l
	 * @param sb
	 */
	@Deprecated
	public void handleTwoInfinites(final double i, final double j,
	        final double k, final Integer a, final Integer b,
	        final List<Tuple2DI> l, final StringBuffer sb) {
		if (Double.isInfinite(i) && Double.isInfinite(k)) {
			addStepNW(a, b, l, sb);
		} else if (Double.isInfinite(i) && Double.isInfinite(j)) {
			addStepN(a, b, l, sb);
		} else if (Double.isInfinite(j) && Double.isInfinite(k)) {
			addStepW(a, b, l, sb);
		}
	}

	/**
	 * 
	 * @param val
	 * @param i
	 * @param j
	 * @param k
	 * @param a
	 * @param b
	 * @param l
	 * @param sb
	 */
	@Deprecated
	public void handleZeroEqual(final double val, final double i,
	        final double j, final double k, final Integer a, final Integer b,
	        final List<Tuple2DI> l, final StringBuffer sb) {
		if (val == j) {
			addStepNW(a, b, l, sb);
		} else if (val == i) {
			addStepW(a, b, l, sb);
		} else if (val == k) {
			addStepN(a, b, l, sb);
		}
	}

	/**
	 * 
	 * @param parent
	 * @param cdist
	 * @param d
	 * @param minimize
	 * @param cd
	 * @return
	 */
	@Deprecated
	public List<Tuple2DI> makeMap(final IFileFragment parent,
	        final IArrayD2Double cdist, final IArrayD2Double d,
	        final boolean minimize, final IRecurrence cd) {
		// if(traceForConvex) {
		// return makeMapForConvex(parent, cdist, d, minimize, cd);
		// }
		PathTools.log.debug("Backtracing");
		// ArrayDouble.D0 weight_sum = new ArrayDouble.D0();
		// weight_sum.set(0.0d);
		final ArrayList<Tuple2DI> map1 = new ArrayList<Tuple2DI>();
		Tuple2DI ret = null;
		int i = cdist.rows() - 1;
		int j = cdist.columns() - 1;
		ret = new Tuple2DI(i, j);
		// log.debug("i: {}, j: {}, v= {}",new Object[]{i,j,a.get(i, j)});
		map1.add(ret);
		this.nbranches = 0;
		boolean done = false;
		final StringBuffer sb = new StringBuffer();
		while (!done) {
			ret = nextStep2(map1, cdist, d, i, j, minimize, cd, sb);
			j = ret.getSecond();
			i = ret.getFirst();
			// log.debug("i: {}, j: {}, v= {}",new Object[]{i,j,a.get(i,j)});

			// map.add(new Tuple2DI(u,v));
			if ((i == 0) && (j == 0)) {
				done = true;
			}
		}
		sb.reverse();
		this.symbolicPath = sb.toString();
		PathTools.log.debug("{}", this.symbolicPath);
		PathTools.log.debug("Finished!");
		PathTools.log.debug("Reversing map!");
		Collections.reverse(map1);
		this.map = map1;
		return map1;
	}

	/**
	 * 
	 * @param parent
	 * @param cdist
	 * @param d
	 * @param minimize
	 * @param cd
	 * @return
	 */
	@Deprecated
	public List<Tuple2DI> makeMapForConvex(final IFileFragment parent,
	        final IArrayD2Double cdist, final IArrayD2Double d,
	        final boolean minimize, final IRecurrence cd) {
		PathTools.log.debug("Backtracing");
		// ArrayDouble.D0 weight_sum = new ArrayDouble.D0();
		// weight_sum.set(0.0d);
		final ArrayList<Tuple2DI> map1 = new ArrayList<Tuple2DI>();
		Tuple2DI ret = null;
		int i = cdist.rows() - 1;
		int j = cdist.columns() - 1;
		ret = new Tuple2DI(i, j);
		// log.debug("i: {}, j: {}, v= {}",new Object[]{i,j,a.get(i, j)});
		map1.add(ret);
		this.nbranches = 0;
		boolean done = false;
		final StringBuffer sb = new StringBuffer();
		while (!done) {
			ret = nextStep2(map1, cdist, d, i, j, minimize, cd, sb);
			j = ret.getSecond();
			i = ret.getFirst();
			// log.debug("i: {}, j: {}, v= {}",new Object[]{i,j,a.get(i,j)});

			// map.add(new Tuple2DI(u,v));
			if ((i == 0) && (j == 0)) {
				done = true;
			}
		}
		sb.reverse();
		this.symbolicPath = sb.toString();
		PathTools.log.debug("{}", this.symbolicPath);
		PathTools.log.debug("Finished!");
		PathTools.log.debug("Reversing map!");
		Collections.reverse(map1);
		this.map = map1;
		return map1;
	}

	/**
	 * 
	 * @param map1
	 * @param arr
	 * @param d
	 * @param a
	 * @param b
	 * @param minimize
	 * @param cd
	 * @param sb
	 * @return
	 */
	@Deprecated
	public Tuple2DI nextStep2(final List<Tuple2DI> map1,
	        final IArrayD2Double arr, final IArrayD2Double d, final Integer a,
	        final Integer b, final boolean minimize, final IRecurrence cd,
	        final StringBuffer sb) {
		return traceback(map1, arr, d, a, b, cd, sb, minimize);
		// if (minimize) {
		// return minimizeSteps(map1, arr, d, a, b, cd, sb);
		// } else {
		// return maximizeSteps(map1, arr, d, a, b, cd, sb);
		// }
	}

	public void savePathCSV(final IFileFragment parent,
	        final IArrayD2Double cdist, final IArrayD2Double pwdist,
	        final List<Tuple2DI> map1, final IWorkflow iw, final boolean isDist) {
		final CSVWriter csvw = Factory.getInstance().instantiate(
		        CSVWriter.class);
		final String filename = StringTools.removeFileExt(parent.getName());
		final File pathCSV = new File(new File(parent.getAbsolutePath())
		        .getParent(), filename + "_path.csv");
		final File pathPWCSV = new File(new File(parent.getAbsolutePath())
		        .getParent(), filename + "_path_pw.csv");
		// final File pathCondensed = new File(new
		// File(parent.getAbsolutePath())
		// .getParent(), filename + "_path_condensed.csv");
		// DefaultWorkflowResult dwr = new DefaultWorkflowResult();
		// dwr.setFile(pathCSV);
		// dwr.setIWorkflowElement(csvw);
		// dwr.setWorkflowSlot(WorkflowSlot.ALIGNMENT);
		// iw.append(dwr);
		csvw.setIWorkflow(iw);
		csvw.writeAlignmentPath(pathCSV.getParent(), pathCSV.getName(), map1,
		        cdist, FragmentTools.getLHSFile(parent).getName(),
		        FragmentTools.getRHSFile(parent).getName(),
		        "cumulative-distance", this.symbolicPath);
		// ArrayList<Tuple2DI> condensedFeatures = findLocalPathOptima(parent,
		// pwdist, map1, isDist);
		csvw.writeAlignmentPath(pathPWCSV.getParent(), pathPWCSV.getName(),
		        map1, pwdist, FragmentTools.getLHSFile(parent).getName(),
		        FragmentTools.getRHSFile(parent).getName(),
		        "pairwise_distance", this.symbolicPath);

		// csvw.writeAlignmentPath(pathCondensed.getParent(), pathCondensed
		// .getName(), map1, pwdist, FragmentTools.getLHSFile(parent)
		// .getName(), FragmentTools.getRHSFile(parent).getName(),
		// "pairwise-distance", this.symbolicPath);

		final File f = new File(new File(parent.getAbsolutePath()).getParent(),
		        filename + "_path-symbolic.txt");
		final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(f, csvw,
		        WorkflowSlot.ALIGNMENT);
		iw.append(dwr2);
		try {
			final BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write(this.symbolicPath);
			bw.flush();
			bw.close();
		} catch (final IOException e) {
			PathTools.log.error(e.getLocalizedMessage());
		}
	}

	//
	// /**
	// * @param parent
	// * @param pwdist
	// * @param map1
	// * @param isDist
	// */
	// private ArrayList<Tuple2DI> findLocalPathOptima(final IFileFragment
	// parent,
	// final IArrayD2Double pwdist, final List<Tuple2DI> map1,
	// final boolean isDist) {
	// SortedSet<Integer> localPathOptima = new TreeSet<Integer>();
	// double[] arr = new double[map1.size()];
	// int i = 0;
	// for (Tuple2DI t : map1) {
	// arr[i++] = pwdist.get(t.getFirst(), t.getSecond());
	// }
	// String symbol = String.valueOf(this.symbolicPath.charAt(0));
	// int state = 0; // default
	// int rangeStart = 0;
	// int rangeEnd = 0;
	// for (i = 0; i < arr.length; i++) {
	// // if we find a one-to-one matching
	// if (symbol.equals("o")) {
	// // and we already were on one-to-one mode
	// if (state == 0) {
	// // we extend the range
	// rangeEnd++;
	// } else {
	// // otherwise, we start a new range
	// rangeStart = i;
	// }
	// // and set state to one-to-one
	// state = 0;
	// } else {
	// // otherwise, we are in a different state
	// // if we were in one-to-one mode, check range for optima
	// if (state == 0) {
	// if (isDist) {
	// localPathOptima.addAll(getMinInRange(map1, rangeStart,
	// rangeEnd, arr));
	// } else {
	// localPathOptima.addAll(getMaxInRange(map1, rangeStart,
	// rangeEnd, arr));
	// }
	// }
	// state = 1;
	// }
	// }
	// ArrayList<Tuple2DI> al = new ArrayList<Tuple2DI>();
	// for (Integer itg : localPathOptima) {
	// al.add(map1.get(itg.intValue()));
	// }
	// return al;
	// }

	// private List<Integer> getMaxInRange(List<Tuple2DI> path, int start,
	// int end, double[] values) {
	// ArrayList<Integer> al = new ArrayList<Integer>();
	// for (int i = start; i <= Math.min(values.length - 1, end); i++) {
	// if (isMaxCandidate(i, values, start, end)) {
	// al.add(i);
	// }
	// }
	// return al;
	// }
	//
	// private List<Integer> getMinInRange(List<Tuple2DI> path, int start,
	// int end, double[] values) {
	// ArrayList<Integer> al = new ArrayList<Integer>();
	// for (int i = start; i <= Math.min(values.length - 1, end); i++) {
	// if (isMinCandidate(i, values, start, end)) {
	// al.add(i);
	// }
	// }
	// return al;
	//
	// }

	// private boolean isMaxCandidate(final int index, double[] values, int
	// start,
	// int end) {
	// double max = MathTools.max(values, start, end);
	// double indxVal = values[index];
	// if (max == indxVal) {
	// return true;
	// }
	// return false;
	// }
	//
	// private boolean isMinCandidate(final int index, double[] values, int
	// start,
	// int end) {
	// double min = MathTools.min(values, start, end);
	// double indxVal = values[index];
	// if (min == indxVal) {
	// return true;
	// }
	// return false;
	// }

	/**
	 * Calculates a trace based on the int[][] argument. Expects 0 for diagonal
	 * steps, 1 for vertical steps and -1 for horizontal steps.
	 * 
	 * @param predecessors
	 * @return
	 */
	public List<Tuple2DI> traceback(final int[][] predecessors) {
		int a = predecessors.length - 1;
		int b = predecessors[0].length - 1;
		int i;
		this.nexp = this.ndiag = this.ncomp = 0;
		final StringBuilder sb = new StringBuilder();
		final ArrayList<Tuple2DI> al = new ArrayList<Tuple2DI>();
		int prev = 0;
		while ((a != 0) && (b != 0)) {
			i = predecessors[a][b];
			switch (i) {
				case 0: {
					al.add(new Tuple2DI(a, b));
					a--;
					b--;
					sb.append("o");
					this.ndiag++;
					break;
				}
				case 1: {
					al.add(new Tuple2DI(a, b));
					a--;
					sb.append("-");
					this.ncomp++;
					break;
				}
				case -1: {
					al.add(new Tuple2DI(a, b));
					b--;
					sb.append("+");
					this.nexp++;
					break;
				}
				default: {
					throw new IllegalArgumentException(
					        "Don't know how to handle predecessor of type " + i);
				}
			}
			prev = i;
		}
		if (a == 0 && b == 0) {
			al.add(new Tuple2DI(a, b));
			sb.append("o");
			this.ndiag++;
		} else if ((a > 0) && (b == 0)) {
			while (a >= 0) {
				al.add(new Tuple2DI(a, b));
				a--;
				sb.append("-");
				this.ncomp++;
			}
		} else if ((a == 0) && (b > 0)) {
			while (b >= 0) {
				al.add(new Tuple2DI(a, b));
				b--;
				sb.append("+");
				this.nexp++;
			}
		}
		Collections.reverse(al);
		sb.reverse();
		this.symbolicPath = sb.toString();
		this.log.debug("{}", this.symbolicPath);
		this.map = al;
		return this.map;
	}

	/**
	 * 
	 * @param map1
	 * @param arr
	 * @param d
	 * @param a
	 * @param b
	 * @param cd
	 * @param sb
	 * @param minimize
	 * @return
	 */
	@Deprecated
	public Tuple2DI traceback(final List<Tuple2DI> map1,
	        final IArrayD2Double arr, final IArrayD2Double d, final Integer a,
	        final Integer b, final IRecurrence cd, final StringBuffer sb,
	        final boolean minimize) {
		double i = -1, j = -1, k = -1;
		PathTools.log.debug("Looking for predecessor of {},{}", a, b);

		if (a == 0) {// first row
			PathTools.log.debug("({},{})", a, b);
			// weight_sum.set(weight_sum.get()+cd.getExpansionWeight());
			return addStepW(a, b, map1, sb);
			// return map.get(map.size() - 1);
		}

		if (b == 0) {// first column
			PathTools.log.debug("({},{})", a, b);
			// weight_sum.set(weight_sum.get()+cd.getCompressionWeight());
			return addStepN(a, b, map1, sb);
			// return map.get(map.size() - 1);
		}

		// double cdist = arr.get(a,b);
		try {// get values of predecessors
			// i = cd.cumDist(a-1, b, arr, d.get(a-1, b));
			// j = cd.cumDist(a-1, b-1, arr, d.get(a-1, b-1));
			// k = cd.cumDist(a, b-1, arr, d.get(a, b-1));
			i = arr.get(a - 1, b);
			j = arr.get(a - 1, b - 1);
			k = arr.get(a, b - 1);
			// i = cd.cumDist(a-1, b, arr, d.get(a,b));//arr.get(a-1, b);//
			// cd.cumDist(a, y, arr,
			// // d.get(a,b));//(arr.getDouble(ind.set(a,
			// // y)));// map b and b-1
			// // values to a
			// j = cd.cumDist(a-1, b-1, arr, d.get(a,b));//arr.get(a-1, b-1);//
			// cd.cumDist(x, y, arr,
			// // d.get(a,b));//(arr.getDouble(ind.set(x,
			// // y)));// map a to b
			// k = cd.cumDist(a, b-1, arr, d.get(a,b));//arr.get(a, b-1);//
			// cd.cumDist(x, b, arr,
			// // d.get(a,b));//(arr.getDouble(ind.set(x,
			// // b)));// map a and a-1
			// // values to b
			PathTools.log.debug(
			        "({},{}): Values of predecessors i={}, j={}, k={}",
			        new Object[] { a, b, i, j, k });
		} catch (final ArrayIndexOutOfBoundsException aiex) {
			PathTools.log.error("Error out of bounds at i=" + i + ", j=" + j
			        + ", k=" + k);
			System.exit(-1);
		}
		if (minimize) {
			if (i == Double.NEGATIVE_INFINITY) {
				i = Double.POSITIVE_INFINITY;
			}
			if (j == Double.NEGATIVE_INFINITY) {
				j = Double.POSITIVE_INFINITY;
			}
			if (k == Double.NEGATIVE_INFINITY) {
				k = Double.POSITIVE_INFINITY;
			}
		} else {
			if (i == Double.POSITIVE_INFINITY) {
				i = Double.NEGATIVE_INFINITY;
			}
			if (j == Double.POSITIVE_INFINITY) {
				j = Double.NEGATIVE_INFINITY;
			}
			if (k == Double.POSITIVE_INFINITY) {
				k = Double.NEGATIVE_INFINITY;
			}
		}

		final double val = (minimize ? Math.min(i, Math.min(j, k)) : Math.max(
		        i, Math.max(j, k)));
		if (PathTools.allInfinite(i, j, k)) {
			throw new IllegalArgumentException();
		}
		if ((val == j)) {
			// weight_sum.set(weight_sum.get()+cd.getDiagonalWeight());
			return addStepNW(a, b, map1, sb);
		} else if ((val == i)) {
			// weight_sum.set(weight_sum.get()+cd.getCompressionWeight());
			return addStepN(a, b, map1, sb);
		} else if ((val == k)) {
			// weight_sum.set(weight_sum.get()+cd.getExpansionWeight());
			return addStepW(a, b, map1, sb);
		} else {
			throw new IllegalArgumentException();
		}
	}

}
