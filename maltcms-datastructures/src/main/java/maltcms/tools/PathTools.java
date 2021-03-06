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
package maltcms.tools;

import cross.Factory;
import cross.IConfigurable;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.FragmentTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.MathTools;
import cross.tools.StringTools;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.array.IArrayD2Double;
import maltcms.io.csv.CSVWriter;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.IndexIterator;
import ucar.nc2.Dimension;

/**
 * Utility class providing methods for handling of the path obtained from
 * alignment.
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
public class PathTools implements IConfigurable {

    public enum Direction {

        W, N, NW;
    }

    private enum STATE {

        C, O;
    }
    @Configurable
    private double threshold = 0.95;

    /**
     * <p>allInfinite.</p>
     *
     * @param a a double.
     * @param b a double.
     * @param c a double.
     * @return a boolean.
     */
    public static boolean allInfinite(final double a, final double b,
            final double c) {
        return (Double.isInfinite(a) && Double.isInfinite(b) && Double
                .isInfinite(c));
    }

    /**
     * Constructs List of pairs of indices from array representation.
     *
     * @param i a {@link ucar.ma2.Array} object.
     * @param j a {@link ucar.ma2.Array} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Tuple2DI> fromArrays(final Array i, final Array j) {
        final IndexIterator jiter = j.getIndexIterator();
        final IndexIterator iiter = i.getIndexIterator();
        final ArrayList<Tuple2DI> al = new ArrayList<>(i.getShape()[0]);
        while (iiter.hasNext() && jiter.hasNext()) {
            al.add(new Tuple2DI(iiter.getIntNext(), jiter.getIntNext()));
        }
        return al;
    }

    /**
     * Constructs List of pairs of indices from array representation.
     *
     * @param i a {@link ucar.ma2.Array} object.
     * @param j a {@link ucar.ma2.Array} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Point> pointListFromArrays(final Array i, final Array j) {
        final IndexIterator jiter = j.getIndexIterator();
        final IndexIterator iiter = i.getIndexIterator();
        final ArrayList<Point> al = new ArrayList<>(i.getShape()[0]);
        while (iiter.hasNext() && jiter.hasNext()) {
            al.add(new Point(iiter.getIntNext(), jiter.getIntNext()));
        }
        return al;
    }

    /**
     * Adds VariableFragments and arrays corresponding to path to parent
     * FileFragment.
     *
     * @param parent a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param al a {@link java.util.List} object.
     * @param ia a {@link maltcms.datastructures.array.IArrayD2Double} object.
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
        wpi.setDimensions(new Dimension[]{d});
        pathDist.setDimensions(new Dimension[]{d});
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
        wpj.setDimensions(new Dimension[]{d});
        wpi.setArray(t.getFirst());
        wpj.setArray(t.getSecond());
    }

    /**
     * <p>nequal.</p>
     *
     * @param a a double.
     * @param b a double.
     * @param c a double.
     * @return a int.
     */
    public static int nequal(final double a, final double b, final double c) {
        if ((a == b) && (a == c)) {
            return 3;
        }
        if (((a == b) && (a != c)) || ((a == c) && (a != b))) {
            return 2;
        }
        return 0;
    }

    /**
     * <p>ninf.</p>
     *
     * @param a a double.
     * @param b a double.
     * @param c a double.
     * @return a int.
     */
    public static int ninf(final double a, final double b, final double c) {
        int n = (Double.isInfinite(a) ? 1 : 0);
        n += (Double.isInfinite(b) ? 1 : 0);
        n += (Double.isInfinite(c) ? 1 : 0);
        return n;
    }

    /**
     * Inverts pairs of matching indices.
     *
     * @param path a {@link java.util.ArrayList} object.
     * @return a {@link java.util.ArrayList} object.
     */
    public static ArrayList<Tuple2DI> swapPath(final ArrayList<Tuple2DI> path) {
        final ArrayList<Tuple2DI> swapped = new ArrayList<>(path.size());
        for (final Tuple2DI t : path) {
            swapped.add(new Tuple2DI(t.getSecond(), t.getFirst()));
        }
        return swapped;
    }

    /**
     * Constructs array representation from List of pairs of indices.
     *
     * @param al a {@link java.util.List} object.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
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

    /**
     * Constructs array representation from List of pairs of indices.
     *
     * @param al a {@link java.util.List} object.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public static Tuple2D<Array, Array> pointListToArrays(final List<Point> al) {
        final ArrayInt.D1 js = new ArrayInt.D1(al.size());
        final ArrayInt.D1 is = new ArrayInt.D1(al.size());
        final IndexIterator jiter = js.getIndexIterator();
        final IndexIterator iiter = is.getIndexIterator();
        final Iterator<Point> iter = al.iterator();
        while (iter.hasNext() && jiter.hasNext() && iiter.hasNext()) {
            final Point t = iter.next();
            iiter.setIntNext(t.x);
            jiter.setIntNext(t.y);
        }
        return new Tuple2D<Array, Array>(is, js);
    }

    /**
     * Converts a tuple 2D list into a point list.
     *
     * @param al a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Point> toPointList(final List<Tuple2DI> al) {
        final ArrayList<Point> ret = new ArrayList<>(al.size());
        for (Tuple2DI p : al) {
            ret.add(new Point(p.getFirst(), p.getSecond()));
        }
        return ret;
    }

    /**
     * Converts a point list into a tuple 2D list.
     *
     * @param al a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Tuple2DI> toTupleList(final List<Point> al) {
        final ArrayList<Tuple2DI> ret = new ArrayList<>(al.size());
        for (Point p : al) {
            ret.add(new Tuple2DI(p.x, p.y));
        }
        return ret;
    }
    private int nbranches = 0;
    private String symbolicPath = "";
    private List<Tuple2DI> map = null;
    private int nexp, ncomp, ndiag;
    @Configurable
    private int window = 1;

    /**
     * <p>addStepN.</p>
     *
     * @param a a {@link java.lang.Integer} object.
     * @param b a {@link java.lang.Integer} object.
     * @param l a {@link java.util.List} object.
     * @param sb a {@link java.lang.StringBuffer} object.
     * @return a {@link cross.datastructures.tuple.Tuple2DI} object.
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
     * <p>addStepNW.</p>
     *
     * @param a a {@link java.lang.Integer} object.
     * @param b a {@link java.lang.Integer} object.
     * @param l a {@link java.util.List} object.
     * @param sb a {@link java.lang.StringBuffer} object.
     * @return a {@link cross.datastructures.tuple.Tuple2DI} object.
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
     * <p>addStepW.</p>
     *
     * @param a a {@link java.lang.Integer} object.
     * @param b a {@link java.lang.Integer} object.
     * @param l a {@link java.util.List} object.
     * @param sb a {@link java.lang.StringBuffer} object.
     * @return a {@link cross.datastructures.tuple.Tuple2DI} object.
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
    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        this.window = cfg.getInt(this.getClass().getName() + ".window", 1);
        this.threshold = cfg.getDouble(
                this.getClass().getName() + ".threshold", 0.95);
    }

    /**
     * <p>createSmoothPath.</p>
     *
     * @param map1 a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    @Deprecated
    public List<Tuple2DI> createSmoothPath(final List<Tuple2DI> map1) {
        final ArrayList<Tuple2DI> ret = new ArrayList<>();
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

    /**
     * <p>decorate.</p>
     *
     * @param parent a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param ia a {@link maltcms.datastructures.array.IArrayD2Double} object.
     */
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
     * @param pwdist
     * @param map1
     * @param isDist
     */
    private ArrayList<Tuple2DI> findLocalPathOptima(
            final IArrayD2Double pwdist, final List<Tuple2DI> map1,
            final boolean isDist) {
        final SortedSet<Integer> localPathOptima = new TreeSet<>();
        final double[] arr = new double[map1.size()];
        int i = 0;
        for (final Tuple2DI t : map1) {
            arr[i++] = pwdist.get(t.getFirst(), t.getSecond());
        }
        String symbol = String.valueOf(this.symbolicPath.charAt(0));
        STATE state = STATE.O; // default
        int rangeStart = -1;
        int rangeEnd = -1;
        for (i = 0; i < arr.length; i++) {
            symbol = String.valueOf(this.symbolicPath.charAt(i));
            // if we find a one-to-one matching
            if (symbol.equals("o")) {
                // and we already were on one-to-one mode
                if (state == STATE.C) {
                    PathTools.log.info("Previous state: Match");
                    // we extend the range
                    rangeEnd++;
                } else {
                    PathTools.log.info("Previous state: Other");
                    // otherwise, we start a new range
                    rangeStart = i;
                    rangeEnd = i;
                }
                // and set state to one-to-one
                state = STATE.C;
                PathTools.log.info("Next state: Match");
                PathTools.log.info("rangeStart: {}, rangeEnd: {}", rangeStart,
                        rangeEnd);
            } else {// skip all other modes, these will mostly be
                // non-bidirectional
                // hits
                // otherwise, we are in a different state
                // if we were in one-to-one mode, check range for optima
                if (state == STATE.C) {
                    PathTools.log.info("Previous state: Match");
                    if (isDist) {
                        localPathOptima.addAll(getMinInRange(map1, rangeStart,
                                rangeEnd - 1, arr));
                    } else {
                        localPathOptima.addAll(getMaxInRange(map1, rangeStart,
                                rangeEnd - 1, arr));
                    }
                }
                // set state to other
                // reset start and end
                state = STATE.O;
                PathTools.log.info("Next state: Other");
                rangeStart = -1;
                rangeEnd = -1;
                PathTools.log.info("rangeStart: {}, rangeEnd: {}", rangeStart,
                        rangeEnd);
            }
        }
        final ArrayList<Tuple2DI> al = new ArrayList<>();
        for (final Integer itg : localPathOptima) {
            al.add(map1.get(itg));
        }
        return al;
    }

    private List<Tuple2DI> getAlignedPeaksAlongPath(final boolean minimize,
            final IArrayD2Double pwdist, final List<Tuple2DI> map1) {
        // double[] values = new double[map1.size()];
        // int cnt = 0;
        // for(Tuple2DI t:map1) {
        // values[cnt++] = pwdist.get(t.getFirst(), t.getSecond());
        // }
        // ArrayList<Tuple2DI> alignedPeaks = new ArrayList<Tuple2DI>();
        // for(int i = 0; i < values.length; i++) {
        // if(isCandidate(i, values, 1, minimize)) {
        // alignedPeaks.add(map1.get(i));
        // }
        // }
        return findLocalPathOptima(pwdist, map1, minimize);
    }

    /**
     * <p>Getter for the field <code>map</code>.</p>
     *
     * @return the map
     */
    public List<Tuple2DI> getMap() {
        return this.map;
    }

    private List<Integer> getMaxInRange(final List<Tuple2DI> path,
            final int start, final int end, final double[] values) {
        final ArrayList<Integer> al = new ArrayList<>();
        for (int i = start; i <= Math.min(values.length - 1, end); i++) {
            if (isMaxCandidate(i, values, start, end)) {
                al.add(i);
            }
        }
        return al;
    }

    private List<Integer> getMinInRange(final List<Tuple2DI> path,
            final int start, final int end, final double[] values) {
        final ArrayList<Integer> al = new ArrayList<>();
        for (int i = start; i <= Math.min(values.length - 1, end); i++) {
            if (isMinCandidate(i, values, start, end)) {
                al.add(i);
            }
        }
        return al;

    }

    /**
     * <p>Getter for the field <code>ncomp</code>.</p>
     *
     * @return a int.
     */
    public int getNcomp() {
        return this.ncomp;
    }

    /**
     * <p>Getter for the field <code>ndiag</code>.</p>
     *
     * @return a int.
     */
    public int getNdiag() {
        return this.ndiag;
    }

    /**
     * <p>Getter for the field <code>nexp</code>.</p>
     *
     * @return a int.
     */
    public int getNexp() {
        return this.nexp;
    }

    /**
     * <p>Getter for the field <code>symbolicPath</code>.</p>
     *
     * @return the symbolicPath
     */
    public String getSymbolicPath() {
        return this.symbolicPath;
    }

    /**
     * <p>handleOneInfinite.</p>
     *
     * @param neq a int.
     * @param val a double.
     * @param i a double.
     * @param j a double.
     * @param k a double.
     * @param a a {@link java.lang.Integer} object.
     * @param b a {@link java.lang.Integer} object.
     * @param l a {@link java.util.List} object.
     * @param sb a {@link java.lang.StringBuffer} object.
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
     * <p>handleThreeEqual.</p>
     *
     * @param i a double.
     * @param j a double.
     * @param k a double.
     * @param a a {@link java.lang.Integer} object.
     * @param b a {@link java.lang.Integer} object.
     * @param l a {@link java.util.List} object.
     * @param sb a {@link java.lang.StringBuffer} object.
     */
    @Deprecated
    public void handleThreeEqual(final double i, final double j,
            final double k, final Integer a, final Integer b,
            final List<Tuple2DI> l, final StringBuffer sb) {
        this.nbranches++;
        PathTools.log
                .debug(
                        "BACKTRACKING SEED: WEST and NORTH and NORTHWEST have equal values {}={}={}",
                        new Object[]{i, j, k});
        addStepNW(a, b, l, sb);
    }

    /**
     * <p>handleTwoEqual.</p>
     *
     * @param i a double.
     * @param j a double.
     * @param k a double.
     * @param a a {@link java.lang.Integer} object.
     * @param b a {@link java.lang.Integer} object.
     * @param l a {@link java.util.List} object.
     * @param sb a {@link java.lang.StringBuffer} object.
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
     * <p>handleTwoInfinites.</p>
     *
     * @param i a double.
     * @param j a double.
     * @param k a double.
     * @param a a {@link java.lang.Integer} object.
     * @param b a {@link java.lang.Integer} object.
     * @param l a {@link java.util.List} object.
     * @param sb a {@link java.lang.StringBuffer} object.
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
     * <p>handleZeroEqual.</p>
     *
     * @param val a double.
     * @param i a double.
     * @param j a double.
     * @param k a double.
     * @param a a {@link java.lang.Integer} object.
     * @param b a {@link java.lang.Integer} object.
     * @param l a {@link java.util.List} object.
     * @param sb a {@link java.lang.StringBuffer} object.
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

    private boolean isCandidate(final int index, final double[] values,
            final int window, final boolean minimize) {
        if (minimize) {
            final double min = MathTools.min(values, index - window, index
                    + window);
            final double indxVal = values[index];
            if (min == indxVal) {
                return true;
            }
            return false;
        } else {
            final double max = MathTools.max(values, index - window, index
                    + window);
            final double indxVal = values[index];
            if (max == indxVal) {
                return true;
            }
            return false;
        }
    }

    private boolean isMaxCandidate(final int index, final double[] values,
            final int start, final int end) {
        final double max = MathTools.max(values, start, end);
        final double indxVal = values[index];
        if ((max == indxVal) && (max > this.threshold)) {
            return true;
        }
        return false;
    }

    private boolean isMinCandidate(final int index, final double[] values,
            final int start, final int end) {
        final double min = MathTools.min(values, start, end);
        final double indxVal = values[index];
        if ((min == indxVal) && (min < this.threshold)) {
            return true;
        }
        return false;
    }

    /**
     * <p>savePathCSV.</p>
     *
     * @param parent a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param cdist a {@link maltcms.datastructures.array.IArrayD2Double} object.
     * @param pwdist a {@link maltcms.datastructures.array.IArrayD2Double} object.
     * @param map1 a {@link java.util.List} object.
     * @param iw a {@link cross.datastructures.workflow.IWorkflow} object.
     * @param isDist a boolean.
     */
    public void savePathCSV(final IFileFragment parent,
            final IArrayD2Double cdist, final IArrayD2Double pwdist,
            final List<Tuple2DI> map1, final IWorkflow iw, final boolean isDist) {
        final CSVWriter csvw = Factory.getInstance().getObjectFactory()
                .instantiate(CSVWriter.class);
        final String filename = StringTools.removeFileExt(parent.getName());
        final File pathCSV = new File(new File(parent.getUri())
                .getParent(), filename + "_path.csv");
        final File pathPWCSV = new File(new File(parent.getUri())
                .getParent(), filename + "_path_pw.csv");
        final File pathPWAlignedPeaks = new File(new File(parent
                .getUri()).getParent(), filename
                + "_path_alignedPeaks.csv");
        // final File pathCondensed = new File(new
        // File(parent.getUri())
        // .getParent(), filename + "_path_condensed.csv");
        // DefaultWorkflowResult dwr = new DefaultWorkflowResult();
        // dwr.setFile(pathCSV);
        // dwr.setIWorkflowElement(csvw);
        // dwr.setWorkflowSlot(WorkflowSlot.ALIGNMENT);
        // iw.append(dwr);

        double[] pathValues = new double[map1.size()];

        for (int i = 0; i < map.size(); i++) {
            final Tuple2DI t = map.get(i);
            pathValues[i] = pwdist.get(t.getFirst(), t.getSecond());
        }

        csvw.setWorkflow(iw);
        csvw.writeAlignmentPath(pathCSV.getParent(), pathCSV.getName(), map1,
                pathValues, cdist.rows(), cdist.columns(), FragmentTools.getLHSFile(parent).getName(),
                FragmentTools.getRHSFile(parent).getName(),
                "cumulative_distance", this.symbolicPath);
        // ArrayList<Tuple2DI> condensedFeatures = findLocalPathOptima(parent,
        // pwdist, map1, isDist);
        csvw.writeAlignmentPath(pathPWCSV.getParent(), pathPWCSV.getName(),
                map1, pathValues, cdist.rows(), cdist.columns(), FragmentTools.getLHSFile(parent).getName(),
                FragmentTools.getRHSFile(parent).getName(),
                "pairwise_distance", this.symbolicPath);
        csvw.writeAlignmentPath(pathPWAlignedPeaks.getParent(),
                pathPWAlignedPeaks.getName(), getAlignedPeaksAlongPath(isDist,
                        pwdist, map1), pathValues, cdist.rows(), cdist.columns(), FragmentTools.getLHSFile(parent)
                .getName(), FragmentTools.getRHSFile(parent).getName(),
                "pairwise_distance", this.symbolicPath);
        // csvw.writeAlignmentPath(pathCondensed.getParent(), pathCondensed
        // .getName(), map1, pwdist, FragmentTools.getLHSFile(parent)
        // .getName(), FragmentTools.getRHSFile(parent).getName(),
        // "pairwise-distance", this.symbolicPath);

        final File f = new File(new File(parent.getUri()).getParent(),
                filename + "_path-symbolic.txt");
        final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(f, csvw,
                WorkflowSlot.ALIGNMENT, parent);
        iw.append(dwr2);
        try {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                bw.write(this.symbolicPath);
                bw.flush();
            }
        } catch (final IOException e) {
            PathTools.log.error(e.getLocalizedMessage());
        }
    }

    /**
     * Calculates a trace based on the ArrayByte.D2 argument
     * <code>predecessors</code>. Expects 1 for diagonal steps, 2 for vertical
     * steps and 3 for horizontal steps.
     *
     * @param predecessors a {@link ucar.ma2.ArrayByte.D2} object.
     * @param ref a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param target a {@link cross.datastructures.fragments.IFileFragment} object.
     * @return a {@link java.util.List} object.
     */
    public List<Tuple2DI> traceback(final ArrayByte.D2 predecessors,
            final IFileFragment ref, final IFileFragment target) {
        log.info("Shape of predecessors: {}", Arrays.toString(predecessors.getShape()));
        int a = predecessors.getShape()[0] - 1;
        int b = predecessors.getShape()[1] - 1;
        int i;
        this.nexp = this.ndiag = this.ncomp = 0;
        final StringBuilder sb = new StringBuilder();
        final ArrayList<Tuple2DI> al = new ArrayList<>();
        int prev = 0;
        while ((a != 0) && (b != 0)) {
            i = predecessors.get(a, b);
            log.info("a: {}, b: {}", a, b);
            switch (i) {
                case 1: {
                    al.add(new Tuple2DI(a, b));
                    a--;
                    b--;
                    sb.append("o");
                    this.ndiag++;
                    break;
                }
                case 2: {
                    al.add(new Tuple2DI(a, b));
                    a--;
                    sb.append("-");
                    this.ncomp++;
                    break;
                }
                case 3: {
                    al.add(new Tuple2DI(a, b));
                    b--;
                    sb.append("+");
                    this.nexp++;
                    break;
                }
                default: {
                    log
                            .warn(
                                    "While tracing back alignment of {} and {}: Encountered unknown predecessor value {} at position {},{}",
                                    new Object[]{ref.getName(),
                                        target.getName(), i, a, b});
                    // al.add(new Tuple2DI(a, b));
                    // a--;
                    // b--;
                    // sb.append("o");
                    // this.ndiag++;
                    // break;

                    throw new IllegalArgumentException(
                            "Don't know how to handle predecessor of type " + i
                            + " at position " + a + " " + b);
                }
            }
            prev = i;
        }
        if ((a == 0) && (b == 0)) {
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
        PathTools.log.debug("{}", this.symbolicPath);
        this.map = al;
        return this.map;
    }
}
