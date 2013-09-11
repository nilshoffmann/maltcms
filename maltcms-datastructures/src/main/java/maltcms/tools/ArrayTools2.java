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
package maltcms.tools;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.IndexIterator;
import cross.Factory;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.tools.StringTools;
import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.SqrtFilter;

/**
 * Array tools.
 *
 * @author Mathias Wilhelm
 */
public class ArrayTools2 {

    /**
     * Creates an {@link ArrayInt} out of an other array.
     *
     * @param array array
     * @return intarray
     */
    public static ArrayInt.D1 createIntegerArray(final Array array) {
        final ArrayInt.D1 intarray = new ArrayInt.D1(array.getShape()[0]);
        final IndexIterator iter = intarray.getIndexIterator();
        final IndexIterator arrayIter = array.getIndexIterator();
        while (iter.hasNext()) {
            iter.setIntNext(arrayIter.getIntNext());
        }

        return intarray;
    }

    /**
     * Creates an {@link ArrayInt} out of an other array. All listed masses in
     * masqMasses will be set to 0.
     *
     * @param array array
     * @param masqMasses masses which will be set to 0 instead of the original
     * value
     * @return intarray
     */
    public static ArrayInt.D1 createIntegerArray(final Array array,
            final List<Integer> masqMasses) {
        final ArrayInt.D1 intarray = new ArrayInt.D1(array.getShape()[0]);
        final IndexIterator iter = intarray.getIndexIterator();
        final IndexIterator arrayIter = array.getIndexIterator();
        int i = 0;
        int intensity = 0;
        while (iter.hasNext()) {
            intensity = arrayIter.getIntNext();
            if (!masqMasses.contains(i)) {
                iter.setIntNext(intensity);
            } else {
                iter.setIntNext(0);
            }
            i++;
        }
        return intarray;
    }

    /**
     * Creates an {@link ArrayInt} from a given {@link Vector}.
     *
     * @param list list of Integers.
     * @return {@link ArrayInt}
     */
    public static ArrayInt.D1 createIntegerArray(final Vector<Integer> list) {
        final ArrayInt.D1 array = new ArrayInt.D1(list.size());
        final IndexIterator iter = array.getIndexIterator();
        for (final Integer i : list) {
            iter.setDoubleNext(i);
        }
        return array;
    }

    /**
     * Creates a warp map.
     *
     * @param pathTwo reference path
     * @param pathOne query path
     * @return map map
     */
    public static Map<Integer, Integer[]> createPath(final Array pathOne,
            final Array pathTwo) {
        final Map<Integer, Integer[]> map = new HashMap<Integer, Integer[]>();

        final IndexIterator one = pathOne.getIndexIterator();
        final IndexIterator two = pathTwo.getIndexIterator();

        while (one.hasNext() && two.hasNext()) {
            final int ione = one.getIntNext();
            final int itwo = two.getIntNext();
            if (map.containsKey(ione)) {
                final Integer[] ci = map.get(ione);
                final Integer[] nci = new Integer[ci.length + 1];
                for (int i = 0; i < ci.length; i++) {
                    nci[i] = ci[i];
                }
                nci[ci.length] = itwo;
                map.put(ione, nci);
            } else {
                map.put(ione, new Integer[]{itwo});
            }
        }

        return map;
    }

    /**
     * Filters one array with a given filter.
     *
     * @param a array
     * @param filter filter
     * @return new array
     */
    public static Array filter(final Array a, final AArrayFilter filter) {
        return filter.apply(new Array[]{a.copy()})[0];
    }

    /**
     * Creates a sparse array.
     *
     * TODO: Should use filterExclude filterInclude instead
     *
     * @param source source
     * @param hold mass values which will be hold
     * @param retNew return a new array or the source array with 0.0d at all non
     * hold positions
     * @return sparse array
     */
    @Deprecated
    public static Array filter(final Array source, final Array hold,
            final boolean retNew) {
        final IndexIterator holdIter = hold.getIndexIterator();
        final IndexIterator sourceIter = source.getIndexIterator();

        final ArrayDouble.D1 ret = new ArrayDouble.D1(hold.getShape()[0]);
        final IndexIterator retIter = ret.getIndexIterator();

        int start = 0;
        int end = 0;
        double intensity = 0.0d;
        final int sourceLen = source.getShape()[0];
        while (holdIter.hasNext() && sourceIter.hasNext()) {
            end = holdIter.getIntNext();
            if (sourceLen >= end + 1) {
                for (int i = start; i < end; i++) {
                    sourceIter.getDoubleNext();
                }
                intensity = sourceIter.getDoubleCurrent();
                retIter.setDoubleNext(intensity);
                sourceIter.setDoubleNext(0.0d);
                start = end + 1;
            } else {
                break;
            }
        }

        if (retNew) {
            return ret;
        } else {
            return source;
        }
    }

    /**
     * .
     *
     * @param a array
     * @param list list of indices
     * @param exclude <code>true</code> if the indices should be excluded
     * @return new array
     */
    public static Array filter(final Array a, final List<Integer> list,
            final boolean exclude) {
        if (exclude) {
            final ArrayDouble.D1 ret = (ArrayDouble.D1) a.copy();
            for (final Integer i : list) {
                ret.set(i, 0.0d);
            }
            return ret;
        } else {
            final ArrayDouble.D1 ret = new ArrayDouble.D1(a.getShape()[0]);
            final ArrayDouble.D1 aa = (ArrayDouble.D1) a.copy();
            for (final Integer i : list) {
                ret.set(i, aa.get(i));
            }
            return ret;
        }
    }

    /**
     * Filters a list of all array with a given filter.
     *
     * @param list list of arrays
     * @param filter filter
     * @return new list
     */
    public static List<Array> filter(final List<Array> list,
            final AArrayFilter filter) {
        final List<Array> ret = new ArrayList<Array>();
        for (final Array a : list) {
            ret.add(ArrayTools2.filter(a, filter));
        }
        return ret;
    }

    /**
     * Returns an new array where all listed indices are excluded.
     *
     * @param a array
     * @param list indices which will be removed (set to 0.0d).
     * @return an copy of the list
     */
    public static Array filterExclude(final Array a, final List<Integer> list) {
        return ArrayTools2.filter(a, list, true);
    }

    /**
     * Excludes all indices from all arrays of a given list.
     *
     * @param alist list of array
     * @param list indices which will be removed (set to 0.0d).
     * @return an copy of the list
     */
    public static List<Array> filterExclude(final List<Array> alist,
            final List<Integer> list) {
        final List<Array> ret = new ArrayList<Array>();
        for (final Array a : alist) {
            ret.add(ArrayTools2.filterExclude(a, list));
        }
        return ret;
    }

    /**
     * Returns an new array where all not listed indices are excluded.
     *
     * @param a array
     * @param list indices which will be used other will set to 0.0d.
     * @return an copy of the list
     */
    public static Array filterInclude(final Array a, final List<Integer> list) {
        return ArrayTools2.filter(a, list, false);
    }

    /**
     * Returns a list of array, where each array has only th indices which are
     * given by the second list.
     *
     * @param alist list of array
     * @param list indices which will be used other will set to 0.0d.
     * @return an copy of the list
     */
    public static List<Array> filterInclude(final List<Array> alist,
            final List<Integer> list) {
        final List<Array> ret = new ArrayList<Array>();
        for (final Array a : alist) {
            ret.add(ArrayTools2.filterInclude(a, list));
        }
        return ret;
    }

    /**
     * Sorts.
     *
     * @param intensities ms
     * @return sorted
     */
    public static List<Tuple2D<Integer, Double>> getUniqueMasses(
            final Array intensities) {
        final ArrayList<Tuple2D<Integer, Double>> ms = new ArrayList<Tuple2D<Integer, Double>>();
        final IndexIterator iter = intensities.getIndexIterator();
        int c = 0;
        while (iter.hasNext()) {
            ms.add(new Tuple2D<Integer, Double>(c++, iter.getDoubleNext()));
        }
        Collections.sort(ms, new Comparator<Tuple2D<Integer, Double>>() {
            @Override
            public int compare(final Tuple2D<Integer, Double> o1,
                    final Tuple2D<Integer, Double> o2) {
                return o1.getSecond().compareTo(o2.getSecond());
            }
        });

        return ms;
    }

    /**
     * Transpose a list of arrays.
     *
     * @param list list
     * @return transposed arrays
     */
    public static List<Array> transpose(final List<Array> list) {
        final List<ArrayDouble.D1> newList = new ArrayList<ArrayDouble.D1>();

        for (int i = 0; i < list.get(0).getShape()[0]; i++) {
            newList.add(new ArrayDouble.D1(list.size()));
        }

        int c = 0, i = 0;
        for (final Array a : list) {
            final IndexIterator iter = a.getIndexIterator();
            i = 0;
            while (iter.hasNext()) {
                if (newList.size() > i) {
                    newList.get(i).set(c, iter.getDoubleNext());
                } else {
                    iter.getDoubleNext();
                }
                i++;
            }
            c++;
        }

        final List<Array> ret = new ArrayList<Array>();
        for (final Array a : newList) {
            ret.add(a);
        }

        return ret;
    }

    /**
     * Return a new array where each dimension is the maximum between a and b.
     *
     * @param a array a
     * @param b array b
     * @return new array
     */
    public static Array max(final Array a, final Array b) {
        if (a.getShape()[0] == b.getShape()[0]) {
            final Array c = a.copy();
            final IndexIterator iter1 = c.getIndexIterator();
            final IndexIterator iter2 = b.getIndexIterator();
            double i1, i2;
            while (iter1.hasNext() && iter2.hasNext()) {
                i1 = iter1.getDoubleNext();
                i2 = iter2.getDoubleNext();
                if (i1 < i2) {
                    iter1.setDoubleCurrent(i2);
                }
            }
            return c;
        }
        return null;
    }

    /**
     * Return a new list where each array is the max(a.get(i), b.get(i)).
     *
     * @param a first list arrays
     * @param b second list of arrays
     * @return new list
     */
    public static List<Array> max(final List<Array> a, final List<Array> b) {
        if (a.size() != b.size()) {
            final List<Array> nl = new ArrayList<Array>();
            for (int i = 0; i < a.size(); i++) {
                nl.add(ArrayTools2.max(a.get(i), b.get(i)));
            }
            return nl;
        }
        return null;
    }

    /**
     * Computes a mean Array from the given list.
     *
     * @param list list of arrays.
     * @return mean array
     */
    public static Array mean(final List<Array> list) {
        if (list.size() == 0) {
            return null;
        }
        if (list.size() == 1) {
            return list.get(0).copy();
        }
        Array ret = maltcms.tools.ArrayTools.sum(list.get(0), list.get(1));
        for (int i = 2; i < list.size(); i++) {
            ret = maltcms.tools.ArrayTools.sum(ret, list.get(i));
        }
        ret = maltcms.tools.ArrayTools.mult(ret, 1.0d / list.size());
        return ret;
    }

    /**
     * Return a new array where each dimension is the minimum between a and b.
     *
     * @param a array a
     * @param b array b
     * @return new array
     */
    public static Array min(final Array a, final Array b) {
        if (a.getShape()[0] == b.getShape()[0]) {
            final Array c = a.copy();
            final IndexIterator iter1 = c.getIndexIterator();
            final IndexIterator iter2 = b.getIndexIterator();
            while (iter1.hasNext() && iter2.hasNext()) {
                if (iter1.getDoubleNext() > iter2.getDoubleNext()) {
                    iter1.setDoubleCurrent(iter2.getDoubleCurrent());
                }
            }
            return c;
        }
        return null;
    }

    /**
     * Return a new list where each array is the min(a.get(i), b.get(i)).
     *
     * @param a first list arrays
     * @param b second list of arrays
     * @return new list
     */
    public static List<Array> min(final List<Array> a, final List<Array> b) {
        if (a.size() != b.size()) {
            final List<Array> nl = new ArrayList<Array>();
            for (int i = 0; i < a.size(); i++) {
                nl.add(ArrayTools2.min(a.get(i), b.get(i)));
            }
            return nl;
        }
        return null;
    }

    /**
     * Will create a new Array containing all missing mass bins.
     *
     * @param massValuesA mass values
     * @param massIntensitiesA intensity values
     * @param binNumber bin size
     * @param log log
     * @return new Array containing all mass bins
     */
    public static Array normalize(final Array massValuesA,
            final Array massIntensitiesA, final int binNumber, final Logger log) {
        final ArrayDouble.D1 nms = new ArrayDouble.D1(binNumber);
        final IndexIterator nmsiter = nms.getIndexIterator();
        final IndexIterator mviter = massValuesA.getIndexIterator();
        final IndexIterator miiter = massIntensitiesA.getIndexIterator();
        int oldMZ = 0;
        while (mviter.hasNext() && miiter.hasNext() && nmsiter.hasNext()) {
            final int mz = mviter.getIntNext();
            final double i = miiter.getDoubleNext();
            for (; oldMZ < mz; oldMZ++) {
                if (nmsiter.hasNext()) {
                    nmsiter.setDoubleNext(0);
                }
            }
            if (nmsiter.hasNext()) {
                nmsiter.setDoubleNext(i);
                oldMZ = mz + 1;
            } else {
                if (log != null) {
                    log.error("Skipping value {}", mz);
                }
                break;
            }
        }
        return nms;
    }

    /**
     * Concrete implementation of a filter with a square root.
     *
     * @param a array
     * @return filtered array
     */
    public static Array sqrt(final Array a) {
        return ArrayTools2.filter(a, new SqrtFilter());
    }

    /**
     * Concrete implementation of a filter with a square root.
     *
     * @param list list of arrays
     * @return new list
     */
    public static List<Array> sqrt(final List<Array> list) {
        return ArrayTools2.filter(list, new SqrtFilter());
    }

    protected static Tuple2D<Array, Array> getWarpPath(
            final List<Point> warpPath) {
        final ArrayInt.D1 pathi = new ArrayInt.D1(warpPath.size());
        final ArrayInt.D1 pathj = new ArrayInt.D1(warpPath.size());

        int c = 0;
        for (Point p : warpPath) {
            pathi.set(c, p.x);
            pathj.set(c, p.y);
            c++;
        }

        return new Tuple2D<Array, Array>(pathi, pathj);
    }

    // protected static AlignmentPath2D createWarpPath(
    // final Tuple2D<Array, Array> horizontal,
    // final Tuple2D<Array, Array> vertical) {
    //
    // IndexIterator iter1 = horizontal.getFirst().getIndexIterator(), iter2 =
    // horizontal
    // .getSecond().getIndexIterator();
    // final List<Point> horizontalL = new ArrayList<Point>();
    // while (iter1.hasNext() && iter2.hasNext()) {
    // horizontalL.add(new Point(iter1.getIntNext(), iter2.getIntNext()));
    // }
    //
    // iter1 = vertical.getFirst().getIndexIterator();
    // iter2 = vertical.getSecond().getIndexIterator();
    // final List<Point> verticalL = new ArrayList<Point>();
    // while (iter1.hasNext() && iter2.hasNext()) {
    // verticalL.add(new Point(iter1.getIntNext(), iter2.getIntNext()));
    // }
    //
    // return new AlignmentPath2D(horizontalL, verticalL);
    // }
    public static List<Integer> getUsedMasses(final IFileFragment ff,
            final String usedMassValuesVar) {
        final List<Integer> sdd = new ArrayList<Integer>();
        final Array sda = ff.getChild(usedMassValuesVar).getArray();
        final IndexIterator sdaiter = sda.getIndexIterator();
        while (sdaiter.hasNext()) {
            sdd.add(sdaiter.getIntNext());
        }
        return sdd;
    }

    public static Array getIndexArray(final List<Array> data) {
        final Array index = new ArrayInt.D1(data.size());

        final IndexIterator iter = index.getIndexIterator();
        int s = 0;
        iter.setIntNext(s);
        for (int i = 0; i < data.size() - 1; i++) {
            s += data.get(i).getShape()[0];
            iter.setIntNext(s);
        }

        return index;
    }

    public static Tuple2D<Array, Array> excludeSparse(Tuple2D<Array, Array> ms,
            List<Integer> hold) {

        int c = 0;
        final IndexIterator massiter = ms.getFirst().getIndexIterator();
        while (massiter.hasNext()) {
            if (hold.contains(massiter.getIntNext())) {
                c++;
            }
        }

        Tuple2D<Array, Array> newms = new Tuple2D<Array, Array>(
                new ArrayInt.D1(c), new ArrayDouble.D1(c));
        final IndexIterator iter1 = ms.getFirst().getIndexIterator(), iter2 = ms
                .getSecond().getIndexIterator();
        final IndexIterator siter1 = newms.getFirst().getIndexIterator(), siter2 = newms
                .getSecond().getIndexIterator();
        int mass;
        double value;
        while (iter1.hasNext() && iter2.hasNext()) {
            mass = iter1.getIntNext();
            value = iter2.getDoubleNext();
            if (hold.contains(mass)) {
                siter1.setIntNext(mass);
                siter2.setDoubleNext(value);
            }
        }
        return newms;
    }

    public static Array[] glue(List<Tuple2D<Array, Array>> scanlineMS,
            int scansPerModulation) {
        List<Array> mzs = new ArrayList<Array>();
        List<Array> inten = new ArrayList<Array>();
        ArrayInt.D1 scanIndex = new ArrayInt.D1(scansPerModulation);
        final IndexIterator iter = scanIndex.getIndexIterator();
        iter.setIntNext(0);
        int off = 0;
        for (Tuple2D<Array, Array> ms : scanlineMS) {
            mzs.add(ms.getFirst());
            inten.add(ms.getSecond());
            if (iter.hasNext()) {
                off += ms.getFirst().getShape()[0];
                iter.setIntNext(off);
            }
        }

        return new Array[]{scanIndex, cross.datastructures.tools.ArrayTools.glue(mzs),
                    cross.datastructures.tools.ArrayTools.glue(inten)};
    }
}
