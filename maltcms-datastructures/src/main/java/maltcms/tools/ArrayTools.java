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
import cross.datastructures.collections.CachedLazyList;
import cross.datastructures.collections.IElementProvider;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.exception.ConstraintViolationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayBoolean;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayLong;
import ucar.ma2.ArrayShort;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.Sparse;

/**
 * Utility class providing methods for Sparse and Dense Arrays.
 *
 * @author Nils Hoffmann
 * 
 */

public class ArrayTools {
        
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ArrayTools.class);

    private static Random random;

    /**
     * <p>calcPercentDone.</p>
     *
     * @param elements a long.
     * @param elemCnt a long.
     * @return a double.
     */
    public static double calcPercentDone(final long elements, final long elemCnt) {
        double percentDone;
        percentDone = (double) elemCnt / (double) elements;
        return percentDone;
    }

    /**
     * <p>combine.</p>
     *
     * @param a1 a {@link ucar.ma2.ArrayDouble.D1} object.
     * @param a2 a {@link ucar.ma2.ArrayDouble.D1} object.
     * @param asColumns a boolean.
     * @return a {@link ucar.ma2.ArrayDouble.D2} object.
     */
    public static ArrayDouble.D2 combine(final ArrayDouble.D1 a1,
            final ArrayDouble.D1 a2, final boolean asColumns) {
        if (a1.getShape().length == a2.getShape().length) {
            ArrayDouble.D2 ret = null;
            if (asColumns) {
                ret = ArrayTools.matrix(a1.getShape()[0], 2);
                final IndexIterator a1iter = a1.getIndexIterator();
                final IndexIterator a2iter = a2.getIndexIterator();
                int i = 0;
                while (a1iter.hasNext() && a2iter.hasNext()) {
                    ret.set(i, 0, a1iter.getDoubleNext());
                    ret.set(i, 1, a2iter.getDoubleNext());
                    i++;
                }
            } else {
                ret = ArrayTools.matrix(2, a1.getShape()[0]);
                final IndexIterator a1iter = a1.getIndexIterator();
                final IndexIterator a2iter = a2.getIndexIterator();
                int i = 0;
                while (a1iter.hasNext() && a2iter.hasNext()) {
                    ret.set(0, i, a1iter.getDoubleNext());
                    ret.set(1, i, a2iter.getDoubleNext());
                    i++;
                }
            }
            return ret;
        } else {
            throw new IllegalArgumentException(
                    "Shapes of input Arrays to be combined differ!");
        }
    }

    /**
     * <p>factory.</p>
     *
     * @param d a double.
     * @return a {@link ucar.ma2.ArrayDouble.D1} object.
     */
    public static ArrayDouble.D1 factory(double... d) {
        return (ArrayDouble.D1) Array.makeFromJavaArray(d);
    }

    /**
     * <p>factory.</p>
     *
     * @param d a int.
     * @return a {@link ucar.ma2.ArrayInt.D1} object.
     */
    public static ArrayInt.D1 factory(int... d) {
        return (ArrayInt.D1) Array.makeFromJavaArray(d);
    }

    /**
     * <p>factory.</p>
     *
     * @param d a float.
     * @return a {@link ucar.ma2.ArrayFloat.D1} object.
     */
    public static ArrayFloat.D1 factory(float... d) {
        return (ArrayFloat.D1) Array.makeFromJavaArray(d);
    }

    /**
     * This factory is usable for scalar values and primitive array instances.
     *
     * @param o a {@link java.lang.Object} object.
     * @return a {@link ucar.ma2.Array} object.
     * @throws java.lang.IllegalArgumentException if any.
     */
    public static Array factoryScalar(Object o) throws IllegalArgumentException {
        if (o == null) {
            throw new IllegalArgumentException("Provided object is null!");
        }
        if (o instanceof Array) {
            return (Array) o;
        }

        if (o.getClass().isArray()) {
            return Array.makeFromJavaArray(o);
        }
        if (o.getClass().isPrimitive()) {
            //this will probably never be called due to autoboxing of the Object o parameter!
            if (o.getClass().equals(Double.TYPE)) {
                ArrayDouble.D0 a = new ArrayDouble.D0();
                a.set((Double) o);
                return a;
            } else if (o.getClass().equals(Float.TYPE)) {
                ArrayFloat.D0 a = new ArrayFloat.D0();
                a.set((Float) o);
                return a;
            } else if (o.getClass().equals(Long.TYPE)) {
                ArrayLong.D0 a = new ArrayLong.D0(false);
                a.set((Long) o);
                return a;
            } else if (o.getClass().equals(Integer.TYPE)) {
                ArrayInt.D0 a = new ArrayInt.D0(false);
                a.set((Integer) o);
                return a;
            } else if (o.getClass().equals(Short.TYPE)) {
                ArrayShort.D0 a = new ArrayShort.D0(false);
                a.set((Short) o);
                return a;
            } else if (o.getClass().equals(Byte.TYPE)) {
                ArrayByte.D0 a = new ArrayByte.D0(false);
                a.set((Byte) o);
                return a;
            } else if (o.getClass().equals(Boolean.TYPE)) {
                ArrayBoolean.D0 a = new ArrayBoolean.D0();
                a.set((Boolean) o);
                return a;
            }
            throw new IllegalArgumentException(
                    "Primitive type " + o.getClass() + " is not supported!");
        }
        if (o instanceof Double) {
            ArrayDouble.D0 a = new ArrayDouble.D0();
            a.set((Double) o);
            return a;
        } else if (o instanceof Float) {
            ArrayFloat.D0 a = new ArrayFloat.D0();
            a.set((Float) o);
            return a;
        } else if (o instanceof Long) {
            ArrayLong.D0 a = new ArrayLong.D0(false);
            a.set((Long) o);
            return a;
        } else if (o instanceof Integer) {
            ArrayInt.D0 a = new ArrayInt.D0(false);
            a.set((Integer) o);
            return a;
        } else if (o instanceof Short) {
            ArrayShort.D0 a = new ArrayShort.D0(false);
            a.set((Short) o);
            return a;
        } else if (o instanceof Byte) {
            ArrayByte.D0 a = new ArrayByte.D0(false);
            a.set((Byte) o);
            return a;
        } else if (o instanceof Boolean) {
            ArrayBoolean.D0 a = new ArrayBoolean.D0();
            a.set((Boolean) o);
            return a;
        } else if (o instanceof String) {
            int size = (int) Math.pow(2.0d, 1.0d + (Math.log10(((String) o).
                    length()) / Math.log(2.0d)));
            // check for overflow
            if (size <= 0) {
                size = ((String) o).length();
            }
            return ArrayChar.makeFromString((String) o, size);
        }
        throw new IllegalArgumentException(
                "Could not create Array for Object: " + o + " of type: "
                + o.getClass().getName());
    }

    /**
     * <p>compress.</p>
     *
     * @param sourceA a {@link java.util.List} object.
     * @param source a int.
     * @param length a int.
     * @return a {@link ucar.ma2.ArrayDouble.D1} object.
     */
    public static ArrayDouble.D1 compress(final List<Array> sourceA,
            final int source, final int length) {
        final ArrayDouble.D1 res = ArrayTools.vector(sourceA.get(source).
                getShape()[0]);
        for (int i = 0; i < length; i++) {
            final Array sa = sourceA.get(source + i);
            if (sa instanceof ArrayDouble.D1) {
                final ArrayDouble.D1 saa = (ArrayDouble.D1) sa;
                for (int j = 0; j < res.getShape()[0]; j++) {
                    res.set(j, res.get(j) + saa.get(j));
                }
            } else {
                throw new IllegalArgumentException(
                        "Compression only works on ArrayDouble.D1!");
            }
        }
        for (int i = 0; i < length; i++) {
            final double d = res.get(i);
            res.set(i, d / length);
        }
        return res;
    }

    /**
     * <p>convertArrays.</p>
     *
     * @param al a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    public static List<ArrayDouble.D1> convertArrays(final List<Array> al) {
        final ArrayList<ArrayDouble.D1> ret = new ArrayList<>(al.
                size());
        for (Array a : al) {
            if (a instanceof Sparse) {
                // Set<Integer> s = ((Sparse)a).getKeySet();
                // int minindex = ((Sparse)a).getMapMinIndex();
                // int size = s.size();
                // ArrayDouble.D1 ad = new ArrayDouble.D1(size);
                // log.info("Copying scan {} to array of size {}",i,size);
                // int j = 0;
                // for(Integer integ:s){//;cnt<a.getShape()[0];cnt++){
                // log.info("{},{}",integ,integ-minindex);
                // double v = ((Sparse)a).get(integ);
                // log.info("Setting {} = {}",j,v);
                // ad.set(j, v);
                // j++;
                // }
                ArrayTools.log.debug("Sparse");
                ret.add((ArrayDouble.D1) a);
            } else if (a instanceof ArrayDouble.D1) {
                ret.add((ArrayDouble.D1) a);
            } else if (a instanceof ArrayFloat.D1) {
                final ArrayDouble.D1 b = new ArrayDouble.D1(a.getShape()[0]);
                MAMath.copyDouble(b, a);
                ret.add(b);
            } else if (a instanceof ArrayInt.D1) {
                final ArrayDouble.D1 b = new ArrayDouble.D1(a.getShape()[0]);
                MAMath.copyDouble(b, a);
                ret.add(b);
            } else {
                throw new ClassCastException(
                        "Only Arrays of type ArrayDouble.D1 can be converted! Received type was "
                        + a.getClass().getName());
            }
        }
        return ret;
    }

    /**
     * <p>convertArraysIntD1.</p>
     *
     * @param al a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    public static List<ArrayInt.D1> convertArraysIntD1(final List<Array> al) {
        final ArrayList<ArrayInt.D1> ret = new ArrayList<>(al.size());
        for (Array a : al) {
            if (a instanceof ArrayInt.D1) {
                ret.add((ArrayInt.D1) a);
            } else {
                throw new ClassCastException(
                        "Only Arrays of type ArrayDouble.D1 can be converted!");
            }
        }
        return ret;
    }

    /**
     * <p>convertScan.</p>
     *
     * @param masses a {@link ucar.ma2.Array} object.
     * @param intensities a {@link ucar.ma2.Array} object.
     * @return a {@link java.util.TreeMap} object.
     */
    public static TreeMap<Double, Integer> convertScan(final Array masses,
            final Array intensities) {
        // Convert masses and intensities to tree map holding the mass-intensity
        // value pairs
        EvalTools.eqI(masses.getShape()[0], intensities.getShape()[0],
                ArrayTools.class);
        ArrayTools.log.debug("Adding {} elems", masses.getShape()[0]);
        final TreeMap<Double, Integer> ts = new TreeMap<>();
        final IndexIterator im = masses.getIndexIterator(), ii = intensities.
                getIndexIterator();
        while (im.hasNext() && ii.hasNext()) {
            final double key = im.getDoubleNext();
            if (ts.containsKey(key)) {
                final int value = ts.get(Double.valueOf(key));
                ts.put(key, ii.getIntNext()
                        + value);
            } else {
                ts.put(key, ii.getIntNext());
            }
        }
        return ts;
    }

    /**
     * Returns a Tuple2D holding an ArrayInt.D1 as scan indices to the arrays
     * held in the Tuple2D, mass values and intensity values.
     *
     * @param al a {@link java.util.List} object.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public static Tuple2D<Array, Tuple2D<Array, Array>> createCombinedArray(
            final List<Sparse> al) {
        final int size = ArrayTools.getRequiredSize(al);
        final ArrayInt.D1 scanIndices = new ArrayInt.D1(al.size(), false);
        final ArrayDouble.D1 targetIndices = new ArrayDouble.D1(size);
        final ArrayDouble.D1 targetValues = new ArrayDouble.D1(size);
        int i = 0;
        for (int scan = 0; scan < al.size(); scan++) {
            // j = i+al.get(scan).getShape()[0]-1;
            final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> tuple = al.get(scan).
                    toArrays();
            final int length = tuple.getFirst().getShape()[0];
            Array.arraycopy(tuple.getFirst(), 0, targetIndices, i, length);
            Array.arraycopy(tuple.getSecond(), 0, targetValues, i, length);
            scanIndices.set(scan, i);
            i = i + length;
            scan++;
        }
        final Tuple2D<Array, Array> mzi = new Tuple2D<Array, Array>(
                targetIndices, targetValues);
        final Tuple2D<Array, Tuple2D<Array, Array>> ret = new Tuple2D<Array, Tuple2D<Array, Array>>(
                scanIndices, mzi);
        return ret;
    }

    /**
     * Create binned arrays from source mass_values and intensity_values arrays.
     * Takes a Tuple of two initially null or intialized arrays which are the
     * binned representations. Requires minimum and maximum mass values, number
     * of bins, resolution and the fillvalue for empty bins.
     *
     * @param source_ind a {@link ucar.ma2.Array} object.
     * @param source_val a {@link ucar.ma2.Array} object.
     * @param tiv a {@link cross.datastructures.tuple.Tuple2D} object.
     * @param min a double.
     * @param max a double.
     * @param nbins a int.
     * @param resolution a double.
     * @param fillvalue a double.
     */
    public static void createDenseArray(final Array source_ind,
            final Array source_val, final Tuple2D<Array, Array> tiv,
            final double min, final double max, final int nbins,
            final double resolution, final double fillvalue) {
        // log.info("Creating a dense Array!");
        final Index nii = source_ind.getIndex();
        final Index vii = source_val.getIndex();
        ArrayTools.log.debug("Size of new arrays {}", (nbins));
        if (tiv.getFirst() == null) {
            tiv.setFirst(Array.factory(source_ind.getDataType(),
                    new int[]{nbins}));
        }
        if (tiv.getSecond() == null) {
            tiv.setSecond(Array.factory(source_val.getDataType(),
                    new int[]{nbins}));
        }
        final Array target_ind = tiv.getFirst();
        final Array target_val = tiv.getSecond();
        final Index tnii = target_ind.getIndex();
        for (int i = 0; i < (nbins); i++) {// set index bins, integer
            // spacing
            target_ind.setDouble(tnii.set(i), (min + i));
        }
        final Index tvii = target_val.getIndex();
        // log.info("Source index array: "+source_ind.getShape()[0]);
        // log.info("Source value array: "+source_val.getShape()[0]);

        // log.info("Target index array: "+target_ind.getShape()[0]);
        // log.info("Target value array: "+target_val.getShape()[0]);
        int curr = 0;
        double lastm = -1;
        double currm = 0;
        final boolean average = Factory.getInstance().getConfiguration().
                getBoolean("ArrayTools.createDenseArray.average_bins", true);
        MaltcmsTools.setBinMZbyConfig();
        final ArrayInt.D1 overlapCounter = new ArrayInt.D1(nbins, false);
        for (int i = 0; i < source_ind.getShape()[0]; i++) {// fill in real
            // masses
            final double d = source_ind.getDouble(nii.set(i));
            currm = d;
            if (d < min) {
                throw new IllegalArgumentException(
                        "Found mass value below minimum! " + d + "<" + min);
            }
            // log.info("Index "+i+" d="+d);
            // values
            final double v = source_val.getDouble(vii.set(i));
            // log.info("Value "+i+" v="+v);
            // int idx = ((int) Math.rint(d));
            curr = MaltcmsTools.binMZ(d, min, max, resolution);
            // double f = Math.floor(d);
            // if(curr-f!=0.0d){
            // log.info("rint = {}, floor = {}",curr,f);
            // }
            try {
                final double last_v = target_val.getDouble(tvii.set(curr));

                // increase overlap counter
                if (last_v != 0) {
                    ArrayTools.log.debug("Overlapping masses {},{} for bin {}",
                            new Object[]{lastm, currm, curr});
                    ArrayTools.log.debug("With values {},{}", last_v, v);
                    overlapCounter.set(curr, overlapCounter.get(curr) + 1);
                }
                final double avg = (v + last_v);
                ArrayTools.log.debug("Setting {} = {}", curr, avg);
                // log.debug("mz {}, min_mz {}",f,min);
                target_ind.setDouble(tnii.set(curr), MaltcmsTools.binMZ(d, 0,
                        max, resolution)
                        / resolution);
                target_val.setDouble(tvii.set(curr), avg);
            } catch (final ArrayIndexOutOfBoundsException aioe) {
                ArrayTools.log.error(
                        "ArrayIndexOutOfBounds: tried to access binned mz {} with original value {}, min_mz {}",
                        new Object[]{curr, d, min});
                ArrayTools.log.error(
                        "ArrayIndexOutOfBounds: Shape of target array: {}",
                        Arrays.toString(target_ind.getShape()));
                throw (new RuntimeException(aioe.getLocalizedMessage()));
            }
            lastm = currm;
        }
        for (int i = 0; i < overlapCounter.getShape()[0]; i++) {
            double avg = target_val.getDouble(tvii.set(i));
            if (average) {
                ArrayTools.log.debug("Using average of values {}", avg);
                avg /= (overlapCounter.get(i) + 1);
                target_val.setDouble(tvii.set(i), avg);
            } else {
                ArrayTools.log.debug("Using sum of values {}", avg);
            }
        }

        // log.debug(target_ind.toString());
        // log.debug(target_val.toString());
    }

    /**
     * <p>createSparseIndexArray.</p>
     *
     * @param index a {@link ucar.ma2.ArrayDouble.D1} object.
     * @param values a {@link ucar.ma2.ArrayDouble.D1} object.
     * @param minindex a int.
     * @param maxindex a int.
     * @param nbins a int.
     * @param massBinResolution a double.
     * @return a {@link ucar.ma2.Sparse} object.
     */
    public static Sparse createSparseIndexArray(final ArrayDouble.D1 index,
            final ArrayDouble.D1 values, final int minindex,
            final int maxindex, final int nbins, final double massBinResolution) {
        final Sparse s = new Sparse(index, values, minindex, maxindex, nbins,
                massBinResolution);
        return s;
    }

    /**
     * Calculates A-B elementwise.
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param b a {@link ucar.ma2.Array} object.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array diff(final Array a, final Array b) {
        if (MAMath.conformable(a, b)) {
            final IndexIterator ii1 = a.getIndexIterator();
            final IndexIterator ii2 = b.getIndexIterator();
            final Array ret = Array.factory(DataType.DOUBLE, a.getShape());
            final IndexIterator reti = ret.getIndexIterator();
            // Iterators should be conformable, so only check one
            while (ii1.hasNext()) {
                reti.setDoubleNext(ii1.getDoubleNext() - ii2.getDoubleNext());
            }
            return ret;
        } else {
            throw new IllegalArgumentException("Arrays are not conformable!");
        }
    }

    /**
     * Calculates A/B elementwise.
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param b a {@link ucar.ma2.Array} object.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array div(final Array a, final Array b) {
        if (MAMath.conformable(a, b)) {
            final IndexIterator ii1 = a.getIndexIterator();
            final IndexIterator ii2 = b.getIndexIterator();
            final Array ret = Array.factory(DataType.DOUBLE, a.getShape());
            final IndexIterator reti = ret.getIndexIterator();
            // Iterators should be conformable, so only check one
            while (ii1.hasNext()) {
                reti.setDoubleNext(ii1.getDoubleNext() / ii2.getDoubleNext());
            }
            return ret;
        } else {
            throw new IllegalArgumentException("Arrays are not conformable!");
        }
    }

    /**
     * Calculates A*B elementwise.
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param b a {@link ucar.ma2.Array} object.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array mult(final Array a, final Array b) {
        if (MAMath.conformable(a, b)) {
            final IndexIterator ii1 = a.getIndexIterator();
            final IndexIterator ii2 = b.getIndexIterator();
            final Array ret = Array.factory(DataType.DOUBLE, a.getShape());
            final IndexIterator reti = ret.getIndexIterator();
            // Iterators should be conformable, so only check one
            while (ii1.hasNext()) {
                reti.setDoubleNext(ii1.getDoubleNext() * ii2.getDoubleNext());
            }
            return ret;
        } else {
            throw new IllegalArgumentException("Arrays are not conformable!");
        }
    }

    /**
     * Divides values in array by 60, convenience method for second to minute
     * conversion.
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array divBy60(final Array a) {
        return ArrayTools.mult(a, 1.0d / 60.0d);
    }

    /**
     * Expansion, as used in Dynamic Time Warping, defined as copying the
     * expanded element length times.
     *
     * @param source a {@link ucar.ma2.Array} object.
     * @param length a int.
     * @return a {@link java.util.List} object.
     */
    public static List<ArrayDouble.D1> expand(final Array source,
            final int length) {
        if (source instanceof ArrayDouble.D1) {
            final ArrayDouble.D1 sourceA = (ArrayDouble.D1) source;
            final ArrayDouble.D1[] arr = new ArrayDouble.D1[length];
            // ArrayList<ArrayDouble.D1> al = new ArrayList<ArrayDouble.D1>();
            for (int i = 0; i < length; i++) {
                arr[i] = (ArrayDouble.D1) sourceA.copy();
                // al.add((ArrayDouble.D1)sourceA.copy());
            }
            return new ArrayList<>(Arrays.asList(arr));
        } else {
            throw new IllegalArgumentException(
                    "Expansion only works on ArrayDouble.D1!");
        }
    }

    /**
     * Fill array a with double value d.
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param d a double.
     */
    public static void fill(final Array a, final double d) {
        final IndexIterator iter = a.getIndexIterator();
        while (iter.hasNext()) {
            iter.setDoubleNext(d);
        }
    }

    /**
     * Fill array a with Double value d.
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param d a {@link java.lang.Double} object.
     */
    public static void fillArray(final Array a, final Double d) {
        ArrayTools.fill(a, d);
    }

    /**
     * Fill array a with int value i.
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param i a int.
     */
    public static void fillArray(final Array a, final int i) {
        final IndexIterator ii = a.getIndexIterator();
        while (ii.hasNext()) {
            ii.setIntNext(i);
        }
    }

    /**
     * Fill array a with Integer value i.
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param i a {@link java.lang.Integer} object.
     */
    public static void fillArray(final Array a, final Integer i) {
        ArrayTools.fillArray(a, i.intValue());
    }

    /**
     * Restore List of Array objects from contiguous representation, using an
     * index array containing the offsets for each individual array.
     *
     * @param indices a {@link ucar.ma2.ArrayInt.D1} object.
     * @param values a {@link ucar.ma2.Array} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Array> fromCRS(final ArrayInt.D1 indices,
            final Array values) {
        final int size = indices.getShape()[0];
        final ArrayList<Array> al = new ArrayList<>(size);
        int offset = 0;
        int len = 0;
        for (int i = 0; i < size - 1; i++) {
            offset = indices.get(i);
            len = indices.get(i + 1) - 1 - offset;
            ArrayTools.log.debug("Range for scan {}: Offset {}, Length: {}",
                    new Object[]{i, offset, len});
            try {
                final Array a = values.section(new int[]{offset},
                        new int[]{len});
                // log.info("Scan " + (i + 1));
                // log.info(a.toString());
                al.add(a);
            } catch (final InvalidRangeException e) {
                throw new ConstraintViolationException(e);
            }
        }
        offset = indices.get(size - 1);
        len = values.getShape()[0] - offset;
        ArrayTools.log.debug("Offset: {}, len: {}, shape of array: {}",
                new Object[]{
                    offset, len, Arrays.toString(values.getShape())});
        try {
            final Array a = values.section(new int[]{offset},
                    new int[]{len});
            // log.info("Scan " + size);
            // log.info(a.toString());
            al.add(a);
        } catch (final InvalidRangeException e) {
            throw new ConstraintViolationException(e);
        }
        return al;
    }

    /**
     * Return a list, which is more generally typed than the input list.
     *
     * @param al a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Array> generalizeList(final List<ArrayDouble.D1> al) {
        final ArrayList<Array> ret = new ArrayList<Array>(al);
        return ret;
    }

    /**
     * <p>getNewIndexOnLHS.</p>
     *
     * @param oldIndex a int.
     * @param al a {@link java.util.List} object.
     * @return a int.
     */
    public static int getNewIndexOnLHS(final int oldIndex,
            final List<Tuple2DI> al) {
        final List<Integer> indices = new ArrayList<>(1);
        for (final Tuple2DI t : al) {
            if (t.getSecond() == oldIndex) {
                indices.add(t.getFirst());
            }
        }
        double sum = 0;
        for (final Integer integ : indices) {
            sum += integ.doubleValue();
        }
        final int newIndex = (int) Math.rint(sum / indices.size());
        ArrayTools.log.debug("oldIndex: {}, newIndex: {}", oldIndex, newIndex);
        ArrayTools.log.debug("Range: {}", indices.toString());
        return newIndex;
    }

    /**
     * <p>getNewIndexOnRHS.</p>
     *
     * @param oldIndex a int.
     * @param al a {@link java.util.List} object.
     * @return a int.
     */
    public static int getNewIndexOnRHS(final int oldIndex,
            final List<Tuple2DI> al) {

        final List<Integer> indices = new ArrayList<>(1);
        for (final Tuple2DI t : al) {
            if (t.getFirst() == oldIndex) {
                indices.add(t.getSecond());
            }
        }
        double sum = 0;
        for (final Integer integ : indices) {
            sum += integ.doubleValue();
        }
        final int newIndex = (int) Math.rint(sum / indices.size());
        ArrayTools.log.debug("oldIndex: {}, newIndex: {}", oldIndex, newIndex);
        ArrayTools.log.debug("Range: {}", indices.toString());
        return newIndex;
    }

    /**
     * Compute total number of elements for all Sparse arrays in list.
     *
     * @param al a {@link java.util.List} object.
     * @return a int.
     */
    public static int getRequiredSize(final List<Sparse> al) {
        int i = 0;
        for (final Sparse s : al) {
            final int k = s.getShape()[0];
            if (i + k < Integer.MAX_VALUE) {
                i += k;
            } else {
                throw new IllegalArgumentException(
                        "Exceeded maximum Integer value " + Integer.MAX_VALUE
                        + " for array size!");
            }
        }
        return i;
    }

    /**
     * Returns the number of elements required for a one-dimensional
     * representation of multidimensional array.
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @return a int.
     */
    public static int getSizeForFlattenedArray(final Array a) {
        long l = 0;
        for (int d = 0; d < a.getRank(); d++) {
            final long ds = a.getShape()[d];
            l += ds;
        }
        if (l >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Size exceeds integer precision!");
        }
        return (int) l;
    }

    /**
     * Create an index array with given size, whose integer indexing begins at
     * begin_index.
     *
     * @param size a int.
     * @param begin_index a int.
     * @return a {@link ucar.ma2.ArrayInt.D1} object.
     */
    public static ArrayInt.D1 indexArray(final int size, final int begin_index) {
        final ArrayInt.D1 arr = new ArrayInt.D1(size, false);
        for (int i = 0; i < size; i++) {
            arr.set(i, i + begin_index);
        }
        return arr;
    }

    /**
     * <p>initRandom.</p>
     */
    public static void initRandom() {
        ArrayTools.random = new Random();
    }

    /**
     * <p>initRandom.</p>
     *
     * @param seed a long.
     */
    public static void initRandom(final long seed) {
        ArrayTools.random = new Random(seed);
    }

    /**
     * <p>insertRandomGauss.</p>
     *
     * @param intensity_values a {@link java.util.List} object.
     * @param at_position a int.
     * @param number_of_scans a int.
     * @param dim1 a int.
     * @return a {@link java.util.List} object.
     */
    public static List<Array> insertRandomGauss(
            final List<Array> intensity_values, final int at_position,
            final int number_of_scans, final int dim1) {
        final int dim = intensity_values.size() > 0 ? intensity_values.get(0).
                getShape()[0] : dim1;
        final ArrayList<Array> newScans = new ArrayList<>(number_of_scans);
        for (int i = 0; i < number_of_scans; i++) {
            newScans.add(ArrayTools.randomGaussian(dim, 0.0, 1.0));
        }
        intensity_values.addAll(at_position, newScans);
        return intensity_values;
    }

    /**
     * <p>integrate.</p>
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @return a double.
     */
    public static double integrate(final Array a) {
        return MAMath.sumDouble(a);
    }

    /**
     * Sum over all values of all arrays within a list.
     *
     * @param values a {@link java.util.List} object.
     * @return a {@link ucar.ma2.ArrayDouble.D1} object.
     */
    public static ArrayDouble.D1 integrate(final List<Array> values) {
        final ArrayDouble.D1 total_intensity = new ArrayDouble.D1(values.size());
        int index = 0;
        final Index ind = total_intensity.getIndex();
        for (final Array a : values) {
            final double d = ArrayTools.integrate(a);
            total_intensity.setDouble(ind.set(index++), d);
        }
        return total_intensity;
    }

    /**
     * <p>matrix.</p>
     *
     * @param rows a int.
     * @param columns a int.
     * @return a {@link ucar.ma2.ArrayDouble.D2} object.
     */
    public static ArrayDouble.D2 matrix(final int rows, final int columns) {
        final ArrayDouble.D2 a = new ArrayDouble.D2(rows, columns);
        return a;
    }

    /**
     * <p>merge.</p>
     *
     * @param rhs a {@link java.util.List} object.
     * @param average a boolean.
     * @return a {@link java.util.TreeMap} object.
     */
    public static TreeMap<Double, Integer> merge(
            final List<TreeMap<Double, Integer>> rhs, final boolean average) {
        final HashMap<Double, Integer> bincounter = new HashMap<>();
        final TreeMap<Double, Integer> lhs = new TreeMap<>();
        // Build cumulated bins
        for (final TreeMap<Double, Integer> tm : rhs) {
            ArrayTools.log.debug("Merging scan with {} elements", tm.keySet().
                    size());
            final ArrayList<Double> keys = new ArrayList<>(tm.keySet());
            // Collections.sort(keys);
            for (final Double d : keys) {
                if (lhs.containsKey(d)) {
                    // accumulate new intensity in bin
                    final Integer tmp = lhs.get(d) + tm.get(d);
                    lhs.put(d, tmp);
                    // Count number of hits in mass bin
                    if (bincounter.containsKey(d)) {
                        final Integer integ = bincounter.get(d);
                        bincounter.put(d, integ + 1);
                    } else {
                        bincounter.put(d, 1);
                    }
                } else {
                    lhs.put(d, tm.get(d));
                    bincounter.put(d, 1);
                }
            }
        }
        // Correct for multiple hits in one bin
        for (final Double d : lhs.keySet()) {
            // bincounter was increased, so set to average
            if (bincounter.containsKey(d)) {
                final Integer count = bincounter.get(d);
                final Integer val = lhs.get(d);
                lhs.put(d, (int) Math.rint(((double) val) / ((double) count)));
            }
        }
        return lhs;
    }

    /**
     * <p>merge2.</p>
     *
     * @param lhsMasses a {@link java.util.List} object.
     * @param lhsIntensities a {@link java.util.List} object.
     * @param rhsMasses a {@link java.util.List} object.
     * @param rhsIntensities a {@link java.util.List} object.
     * @param al a {@link java.util.List} object.
     * @param average a boolean.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public static Tuple2D<List<Array>, List<Array>> merge2(
            final List<Array> lhsMasses, final List<Array> lhsIntensities,
            final List<Array> rhsMasses, final List<Array> rhsIntensities,
            final List<Tuple2DI> al, final boolean average) {

        // Create warped lists
        final ArrayList<ArrayList<Array>> warpedMasses = new ArrayList<>(
                al.size());
        final ArrayList<ArrayList<Array>> warpedIntens = new ArrayList<>(
                al.size());
        for (Tuple2DI al1 : al) {
            warpedMasses.add(new ArrayList<Array>(0));
            warpedIntens.add(new ArrayList<Array>(0));
        }
        // Create final ArrayLists, which will contain warped arrays
        final ArrayList<Array> wmasses = new ArrayList<>(
                warpedMasses.size());
        final ArrayList<Array> wintens = new ArrayList<>(
                warpedIntens.size());
        // Proceed through path
        int i = 0;
        for (final Tuple2DI t : al) {
            final Array lhsM = lhsMasses.get(t.getFirst()).copy();
            final Array rhsM = rhsMasses.get(t.getSecond()).copy();

            final Array lhsI = lhsIntensities.get(t.getFirst()).copy();
            final Array rhsI = rhsIntensities.get(t.getSecond()).copy();

            // Create TreeMaps for scans
            final ArrayList<TreeMap<Double, Integer>> al1 = new ArrayList<>(
                    2);
            al1.add(ArrayTools.convertScan(lhsM, lhsI));
            al1.add(ArrayTools.convertScan(rhsM, rhsI));
            // Merge TreeMaps
            final TreeMap<Double, Integer> tm = ArrayTools.merge(al1, average);
            final Tuple2D<Array, Array> tple = ArrayTools.toArrays(tm);
            wmasses.add(tple.getFirst());
            // wmasses.add(mz.get(0));
            // ArrayInt.D1 intens = new ArrayInt.D1(500);
            // MAMath.copyInt(intens, tple.getSecond());
            wintens.add(tple.getSecond());

            i++;
        }
        return new Tuple2D<List<Array>, List<Array>>(wmasses, wintens);
    }

    /**
     * <p>mult.</p>
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param d a double.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array mult(final Array a, final double d) {
        final Array ret = a.copy();
        final IndexIterator iter = ret.getIndexIterator();
        while (iter.hasNext()) {
            iter.setDoubleCurrent(iter.getDoubleNext() * d);
        }
        return ret;
    }

    /**
     * <p>nextGaussian.</p>
     *
     * @return a double.
     */
    public static double nextGaussian() {
        if (ArrayTools.random == null) {
            ArrayTools.initRandom();
        }
        return ArrayTools.random.nextGaussian();
    }

    /**
     * <p>nextUniform.</p>
     *
     * @return a double.
     */
    public static double nextUniform() {
        if (ArrayTools.random == null) {
            ArrayTools.initRandom();
        }
        return ArrayTools.random.nextDouble();
    }

    /**
     * <p>pow.</p>
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param exp a double.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array pow(final Array a, final double exp) {
        final IndexIterator ii1 = a.getIndexIterator();
        final Array ret = Array.factory(a.getDataType(), a.getShape());
        final IndexIterator reti = ret.getIndexIterator();
        // Iterators should be conformable, so only check one
        while (ii1.hasNext()) {
            reti.setDoubleNext(Math.pow(ii1.getDoubleNext(), exp));
        }
        return ret;
    }

    /**
     * <p>printPercentDone.</p>
     *
     * @param percentDone a double.
     * @param parts a long.
     * @param partCnt1 a long.
     * @param log a {@link org.slf4j.Logger} object.
     * @return a long.
     */
    public static long printPercentDone(final double percentDone,
            final long parts, final long partCnt1, final Logger log) {
        long partCnt = partCnt1;
        if (percentDone >= (double) (partCnt + 1) / (double) parts) {
            log.info("{}%", (int) (percentDone * 100));
            partCnt++;

        }
        return partCnt;
    }

    /**
     * <p>printPercentDone.</p>
     *
     * @param percentDone a double.
     * @param parts a long.
     * @param partCnt1 a long.
     * @param os a {@link java.io.OutputStream} object.
     * @return a long.
     */
    public static long printPercentDone(final double percentDone,
            final long parts, final long partCnt1, final OutputStream os) {
        long partCnt = partCnt1;
        if (percentDone >= (double) (partCnt + 1) / (double) parts) {
            try {
                os.write(((int) (percentDone * 100) + "%").getBytes());
                os.flush();
                partCnt++;
            } catch (final IOException ex) {
            }
        }
        return partCnt;
    }

    /**
     * <p>project2.</p>
     *
     * @param toLHS a boolean.
     * @param massesRef a {@link java.util.List} object.
     * @param intenRef a {@link java.util.List} object.
     * @param al a {@link java.util.List} object.
     * @param massesToBeWarped a {@link java.util.List} object.
     * @param intenToBeWarped a {@link java.util.List} object.
     * @param average a boolean.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public static Tuple2D<List<Array>, List<Array>> project2(
            final boolean toLHS, final List<Array> massesRef,
            final List<Array> intenRef, final List<Tuple2DI> al,
            final List<Array> massesToBeWarped,
            final List<Array> intenToBeWarped, final boolean average) {
        // Check, that dimensions match
        EvalTools.eqI(massesToBeWarped.size(), intenToBeWarped.size(),
                ArrayTools.class);
        EvalTools.eqI(massesRef.size(), intenRef.size(), ArrayTools.class);
        // Create warped lists
        final ArrayList<ArrayList<Array>> warpedMasses = new ArrayList<>(
                massesRef.size());
        final ArrayList<ArrayList<Array>> warpedIntens = new ArrayList<>(
                massesRef.size());
        for (Array massesRef1 : massesRef) {
            warpedMasses.add(new ArrayList<Array>(0));
            warpedIntens.add(new ArrayList<Array>(0));
        }
        // Proceed through path
        for (final Tuple2DI t : al) {
            if (toLHS) {
                warpedMasses.get(t.getFirst()).add(
                        massesToBeWarped.get(t.getSecond()).copy());
                warpedIntens.get(t.getFirst()).add(
                        intenToBeWarped.get(t.getSecond()).copy());
            } else {
                warpedMasses.get(t.getSecond()).add(
                        massesToBeWarped.get(t.getFirst()).copy());
                warpedIntens.get(t.getSecond()).add(
                        intenToBeWarped.get(t.getFirst()).copy());
            }
        }
        // Create final ArrayLists, which will contain warped arrays
        final ArrayList<Array> wmasses = new ArrayList<>(
                warpedMasses.size());
        final ArrayList<Array> wintens = new ArrayList<>(
                warpedIntens.size());
        for (int i = 0; i < warpedMasses.size(); i++) {
            final ArrayList<Array> mz = warpedMasses.get(i);
            final ArrayList<Array> in = warpedIntens.get(i);
            // if we map multiple elements to one=> compression => merge
            if (mz.size() > 1) {
                // Create TreeMaps for scans
                final ArrayList<TreeMap<Double, Integer>> al1 = new ArrayList<>(
                        mz.size());
                for (int j = 0; j < mz.size(); j++) {
                    al1.add(ArrayTools.convertScan(mz.get(j), in.get(j)));
                }
                // Merge TreeMaps
                final TreeMap<Double, Integer> tm = ArrayTools.merge(al1,
                        average);
                final Tuple2D<Array, Array> tple = ArrayTools.toArrays(tm);
                wmasses.add(tple.getFirst());
                // wmasses.add(mz.get(0));
                // ArrayInt.D1 intens = new ArrayInt.D1(500);
                // MAMath.copyInt(intens, tple.getSecond());
                wintens.add(tple.getSecond());
            } else if (mz.size() == 1) {// one to one or expansion, copy scans
                wmasses.add(mz.get(0));
                wintens.add(in.get(0));
            } else {
                throw new ConstraintViolationException(
                        "No scans to be warped at " + i
                        + "! Check path mappping for missing pairs!");
            }
        }
        return new Tuple2D<List<Array>, List<Array>>(wmasses, wintens);
    }

    /**
     * <p>projectToLHS.</p>
     *
     * @param lhs a {@link ucar.ma2.Array} object.
     * @param al a {@link java.util.List} object.
     * @param rhs a {@link ucar.ma2.Array} object.
     * @param average a boolean.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array projectToLHS(final Array lhs, final List<Tuple2DI> al,
            final Array rhs, final boolean average) {
        final Array rhsm = Array.factory(lhs.getDataType(), lhs.getShape());
        final Array binCounter = Array.factory(lhs.getDataType(), lhs.
                getShape());
        final Index bci = binCounter.getIndex();
        final Index rhsi = rhs.getIndex();
        final Index rhsmi = rhsm.getIndex();
        for (final Tuple2DI tpl : al) {
            ArrayTools.log.debug("value={}", tpl);
            // increase bin counter of lhs
            bci.set(tpl.getFirst());
            binCounter.setDouble(bci, binCounter.getDouble(bci) + 1);
            rhsmi.set(tpl.getFirst());
            rhsi.set(tpl.getSecond());
            final double val = rhsm.getDouble(rhsmi) + rhs.getDouble(rhsi);
            rhsm.setDouble(rhsmi, val);
        }
        if (average) {
            final IndexIterator bcii = binCounter.getIndexIterator();
            final IndexIterator bcm = rhsm.getIndexIterator();
            while (bcii.hasNext() && bcm.hasNext()) {
                final double v = bcm.getDoubleNext();
                final double div = bcii.getDoubleNext();
                bcm.setDoubleCurrent(v / div);
            }
        }
        return rhsm;
    }

    /**
     * <p>projectToRHS.</p>
     *
     * @param rhs a {@link ucar.ma2.Array} object.
     * @param al a {@link java.util.List} object.
     * @param lhs a {@link ucar.ma2.Array} object.
     * @param average a boolean.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array projectToRHS(final Array rhs, final List<Tuple2DI> al,
            final Array lhs, final boolean average) {
        final Array lhsm = Array.factory(rhs.getDataType(), rhs.getShape());
        final Array binCounter = Array.factory(rhs.getDataType(), rhs.
                getShape());
        final Index bci = binCounter.getIndex();
        final Index lhsi = lhs.getIndex();
        final Index lhsmi = lhsm.getIndex();
        for (final Tuple2DI tpl : al) {
            ArrayTools.log.debug("value={}", tpl);
            // increase bin counter of lhs
            bci.set(tpl.getSecond());
            binCounter.setDouble(bci, binCounter.getDouble(bci) + 1);
            lhsmi.set(tpl.getSecond());
            lhsi.set(tpl.getFirst());
            final double val = lhsm.getDouble(lhsmi) + lhs.getDouble(lhsi);
            lhsm.setDouble(lhsmi, val);
        }
        if (average) {
            final IndexIterator bcii = binCounter.getIndexIterator();
            final IndexIterator bcm = lhsm.getIndexIterator();
            while (bcii.hasNext() && bcm.hasNext()) {
                final double v = bcm.getDoubleNext();
                final double div = bcii.getDoubleNext();
                bcm.setDoubleCurrent(v / div);
            }
        }
        return lhsm;
    }

    /**
     * <p>randomGaussian.</p>
     *
     * @param size a int.
     * @param mean a double.
     * @param stddev a double.
     * @return a {@link ucar.ma2.ArrayDouble.D1} object.
     */
    public static ArrayDouble.D1 randomGaussian(final int size,
            final double mean, final double stddev) {
        final ArrayDouble.D1 arr = ArrayTools.vector(size);
        for (int i = 0; i < size; i++) {
            arr.set(i, (ArrayTools.nextGaussian() - mean) * stddev);
        }
        return arr;
    }

    /**
     * <p>randomUniform.</p>
     *
     * @param size a int.
     * @param mean a double.
     * @param scale a double.
     * @return a {@link ucar.ma2.ArrayDouble.D1} object.
     */
    public static ArrayDouble.D1 randomUniform(final int size,
            final double mean, final double scale) {
        final ArrayDouble.D1 arr = ArrayTools.vector(size);
        for (int i = 0; i < size; i++) {
            arr.set(i, (ArrayTools.nextUniform() - mean) * scale);
        }
        return arr;
    }

    // public static Tuple2D<Array, Tuple2D<Array, Array>> flatten(
    // ArrayList<Array> scans) {
    // ArrayInt.D1 indices = new ArrayInt.D1(scans.size());
    //
    // int size = ArrayTools.getSizeForFlattenedArrays(scans);
    // ArrayTools.log.info("Size of output variable " + size
    // + " Number of returned arrays " + scans.size());
    // // int size =
    // // (dref.getIndex().getLimits().getSecond()-dref.getIndex().getLimits().
    // // getFirst());
    // ArrayDouble.D1 mz = new ArrayDouble.D1(size);
    // ArrayDouble.D1 intens = new ArrayDouble.D1(size);
    //
    // IndexIterator mziter = mz.getIndexIterator();
    // IndexIterator intensiter = intens.getIndexIterator();
    // IndexIterator indicesIter = indices.getIndexIterator();
    //
    // int offset = 0;
    // for (Array a : scans) {// Loop over all returned arrays
    // // FIXME generation of scan index is wrong, maybe even try long
    // // instead of int
    // if (a instanceof Sparse) {// if this is a sparse array
    // Sparse s = (Sparse) a;
    // Tuple2D<ArrayDouble.D1, ArrayDouble.D1> t = s.toArrays();// retrieve
    // // the
    // // index
    // // and
    // // the
    // // values
    // // from
    // // s
    // IndexIterator i1 = t.getFirst().getIndexIterator();
    // IndexIterator i2 = t.getSecond().getIndexIterator();
    // // while both have a next element (never should only one of both
    // // have a next element)
    // while (i1.hasNext() && i2.hasNext()) {
    // if (mziter.hasNext() && intensiter.hasNext()) {
    // mziter.setDoubleNext(i1.getDoubleNext());
    // intensiter.setDoubleNext(i2.getDoubleNext());
    // } else {
    // ArrayTools.log.error("Unequal number of elements!");
    // System.exit(-1);
    // }
    // }
    // if (indicesIter.hasNext()) {
    // indicesIter.setIntNext(offset);
    // }
    // offset += s.getKeySet().size();
    // } else {
    // ArrayDouble.D1 mz_index = ArrayTools.indexArray(
    // a.getShape()[0], 50);
    // IndexIterator i1 = mz_index.getIndexIterator();
    // IndexIterator i2 = a.getIndexIterator();
    // while (i1.hasNext() && i2.hasNext()) {
    // if (mziter.hasNext() && intensiter.hasNext()) {
    // mziter.setDoubleNext(i1.getDoubleNext());
    // intensiter.setDoubleNext(i2.getDoubleNext());
    // } else {
    // ArrayTools.log.error("Unequal number of elements!");
    // System.exit(-1);
    // }
    // }
    // if (indicesIter.hasNext()) {
    // indicesIter.setIntNext(offset);
    // }
    // offset += mz_index.getShape()[0];
    // // throw new IllegalArgumentException(
    // // "Only Sparse Index arrays can be processed");
    // }
    // }
    // return new Tuple2D<Array, Tuple2D<Array, Array>>(indices,
    // new Tuple2D<Array, Array>(mz, intens));
    // }
    /**
     * <p>rootMeanSquareError.</p>
     *
     * @param map a {@link java.util.List} object.
     * @param refInt a {@link java.util.List} object.
     * @param queryInt a {@link java.util.List} object.
     * @return a double.
     */
    public static double rootMeanSquareError(final List<Tuple2DI> map,
            final List<Array> refInt, final List<Array> queryInt) {
        // ArrayList<Array> res = new ArrayList<Array>(refInt.size());
        // int i = 0;
        double res = 0.0d;
        for (final Tuple2DI t : map) {
            res += ArrayTools.integrate(ArrayTools.pow(
                    ArrayTools.diff(refInt.get(t.getFirst()).copy(), queryInt.
                            get(t.getSecond()).copy()), 2.0d));
        }
        return Math.sqrt((res / (map.size())));
    }

    /**
     * <p>scalar.</p>
     *
     * @param d a double.
     * @return a {@link ucar.ma2.ArrayDouble.D0} object.
     */
    public static ArrayDouble.D0 scalar(final double d) {
        final ArrayDouble.D0 a = new ArrayDouble.D0();
        a.set(d);
        return a;
    }

    /**
     * <p>sq.</p>
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array sq(final Array a) {
        final IndexIterator ii1 = a.getIndexIterator();
        final Array ret = Array.factory(a.getDataType(), a.getShape());
        final IndexIterator reti = ret.getIndexIterator();
        double tmp = 0;
        // Iterators should be conformable, so only check one
        while (ii1.hasNext()) {
            tmp = ii1.getDoubleNext();
            reti.setDoubleNext(tmp * tmp);
        }
        return ret;
    }

    /**
     * Calculates A + B elementwise.
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param b a {@link ucar.ma2.Array} object.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array sum(final Array a, final Array b) {
        if (MAMath.conformable(a, b)) {
            final IndexIterator ii1 = a.getIndexIterator();
            final IndexIterator ii2 = b.getIndexIterator();
            final Array ret = Array.factory(a.getDataType(), a.getShape());
            final IndexIterator reti = ret.getIndexIterator();
            // Iterators should be conformable, so only check one
            while (ii1.hasNext()) {
                reti.setDoubleNext(ii1.getDoubleNext() + ii2.getDoubleNext());
            }
            return ret;
        } else {
            throw new IllegalArgumentException(
                    "Arrays are not conformable with shapes "
                    + Arrays.toString(a.getShape()) + " and "
                    + Arrays.toString(b.getShape()));
        }
    }

    /**
     * Creates a new Array containing all values of elements in al along every
     * dimension.
     *
     * The list returned is a CachedList implementation. It creates the tilted
     * arrays lazily from the original supplied list.
     *
     * @param al a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Array> tilt(final List<Array> al) {
        EvalTools.notNull(al, ArrayTools.class);
        final int size = al.get(0).getShape()[0];
        final CachedLazyList<Array> cll = new CachedLazyList<>(new IElementProvider<Array>() {
            private final List<Array> originalList = al;
            private final int lazySize = size;

            @Override
            public long sizeLong() {
                return lazySize;
            }

            @Override
            public int size() {
                return lazySize;
            }

            @Override
            public Array get(long index) {
                return get((int) index);
            }

            @Override
            public Array get(int index) {
                final int nscans = originalList.size();
                Array ret = Array.factory(al.get(0).getDataType(),
                        new int[]{nscans});
                for (int scans = 0; scans < nscans; scans++) {
                    final Array source = al.get(scans);
                    // iterate over elements in each ArrayDouble.D1
                    //for (int i = 0; i < size; i++) {
                    // get array for dim i, set value at scan to value of dim i in
                    // source
                    ret.setDouble(scans, source.getDouble(index));
                    //}
                }
                return ret;
            }

            @Override
            public List<Array> get(long start, long stop) {
                return get((int) start, (int) stop);
            }

            @Override
            public List<Array> get(int start, int stop) {
                final ArrayList<Array> ret = new ArrayList<>();
                final int nscans = originalList.size();
                // initialize new arrays
                for (int i = 0; i < stop - start; i++) {
                    ret.add(Array.factory(al.get(0).getDataType(),
                            new int[]{nscans}));
                }
                for (int scans = 0; scans < nscans; scans++) {
                    final Array source = al.get(scans);
                    final Index sidx = source.getIndex();
                    // iterate over elements in each ArrayDouble.D1
                    for (int i = start; i <= stop; i++) {
                        // get array for dim i, set value at scan to value of dim i in
                        // source
                        ret.get(i).setDouble(scans,
                                source.getDouble(i));
                    }
                }
                return ret;
            }

            @Override
            public void reset() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
//        final ArrayList<Array> ret = new ArrayList<Array>();
//        final int nscans = al.size();
//        // initialize new arrays
//        for (int i = 0; i < size; i++) {
//            ret.add(Array.factory(al.get(0).getElementType(),
//                    new int[]{nscans}));
//        }
//        for (int scans = 0; scans < nscans; scans++) {
//            final Array source = al.get(scans);
//            final Index sidx = source.getIndex();
//            // iterate over elements in each ArrayDouble.D1
//            for (int i = 0; i < size; i++) {
//                final Index ridx = ret.get(i).getIndex();
//                // get array for dim i, set value at scan to value of dim i in
//                // source
//                ret.get(i).setDouble(ridx.set(scans),
//                        source.getDouble(sidx.set(i)));
//            }
//        }
//        return ret;
        return cll;
    }

    /**
     * Creates a new Array containing all values of elements in al along every
     * dimension.
     *
     * @param al a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    public static List<ArrayDouble.D1> tiltD1(final List<ArrayDouble.D1> al) {
        EvalTools.notNull(al, ArrayTools.class);
        final int size = al.get(0).getShape()[0];
        final ArrayList<ArrayDouble.D1> ret = new ArrayList<>();
        final int nscans = al.size();
        // initialize new arrays
        for (int i = 0; i < size; i++) {
            ret.add(new ArrayDouble.D1(nscans));
        }
        for (int scans = 0; scans < nscans; scans++) {
            final ArrayDouble.D1 source = al.get(scans);
            // iterate over elements in each ArrayDouble.D1
            for (int i = 0; i < size; i++) {
                // get array for dim i, set value at scan to value of dim i in
                // source
                ret.get(i).set(scans, source.get(i));
            }
        }
        return ret;
    }

    /**
     * <p>toArrays.</p>
     *
     * @param s a {@link java.util.TreeMap} object.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public static Tuple2D<Array, Array> toArrays(
            final TreeMap<Double, Integer> s) {
        // Convert treemap of scan to mass and intensity array of scan
        final int elems = s.size();
        ArrayTools.log.debug("Number of elems in scan {}", elems);
        final ArrayDouble.D1 masses = new ArrayDouble.D1(elems);
        final ArrayInt.D1 intens = new ArrayInt.D1(elems, false);
        final ArrayList<Double> keys = new ArrayList<>(s.keySet());
        Collections.sort(keys);
        final Iterator<Double> iter = keys.iterator();
        int i = 0;
        while (iter.hasNext()) {
            final Double d = iter.next();
            masses.set(i, d);
            intens.set(i, s.get(d));
            i++;
        }
        return new Tuple2D<Array, Array>(masses, intens);
    }

    /**
     * <p>toCRS.</p>
     *
     * @param values a {@link java.util.List} object.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public static Tuple2D<ArrayDouble.D1, ArrayDouble.D1> toCRS(
            final List<Array> values) {
        final int size = cross.datastructures.tools.ArrayTools.
                getSizeForFlattenedArrays(values);
        ArrayTools.log.debug("Number of Arrays in List: {}", values.size());
        ArrayTools.log.debug("Size for flattened Arrays: {}", size);
        final List<ArrayDouble.D1> al = ArrayTools.convertArrays(values);
        final ArrayDouble.D1 target = new ArrayDouble.D1(size);
        final ArrayDouble.D1 indices = new ArrayDouble.D1(values.size());
        int offset = 0;
        for (int i = 0; i < values.size(); i++) {
            final Array scan = al.get(i);
            if (scan instanceof Sparse) {
                return ((Sparse) scan).toArrays();
            } else {
                final int len = scan.getShape()[0];
                ArrayTools.log.debug("Scan " + i + " from " + 0
                        + " with length " + len + " to target with size "
                        + target.getShape()[0] + " at position " + (offset));
                // log.info(scan.toString());
                Array.arraycopy(scan, 0, target, offset, len);
                indices.set(i, offset);
                offset += len;
            }
        }
        return new Tuple2D<>(indices, target);
    }

    /**
     * <p>vector.</p>
     *
     * @param elems a int.
     * @return a {@link ucar.ma2.ArrayDouble.D1} object.
     */
    public static ArrayDouble.D1 vector(final int elems) {
        final ArrayDouble.D1 a = new ArrayDouble.D1(elems);
        return a;
    }

    /**
     * <p>filterIndices.</p>
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param removeIndices a {@link java.util.List} object.
     * @param defaultValue a double.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array filterIndices(Array a, List<Integer> removeIndices,
            double defaultValue) {
        return filterIndices(a, removeIndices, false, defaultValue);
    }

    /**
     * Given an array and a list of indices within that array, removes all
     * values at those indices and returns a new array where the respective
     * values have been set to a definable minimum value.
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param removeIndices a {@link java.util.List} object.
     * @param invert a boolean.
     * @param defaultValue a double.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array filterIndices(Array a, List<Integer> removeIndices,
            boolean invert, double defaultValue) {
        if (removeIndices.isEmpty()) {
            return a;
        }
        ArrayTools.log.debug("Before: {}", a);
        int shape = a.getShape()[0];// - removeIndices.size();
        ArrayTools.log.debug("a:" + a.getShape()[0] + " removeIndices:"
                + removeIndices.size() + " new size:" + shape);
        Array b = Array.factory(DataType.getType(a.getElementType(), false),
                new int[]{shape});
        Index ib = b.getIndex();
        Index ia = a.getIndex();
        // at least one element is present in removeIndices
        Iterator<Integer> maskIter = removeIndices.iterator();
        int idx = maskIter.next();
        for (int i = 0; i < a.getShape()[0]; i++) {
            // remove index only, if i==idx
            if (invert) {
                if (i == idx) { // copy value at idx
                    ArrayTools.log.debug("Current index to keep: {}", i);
                    b.setObject(ib.set(i), a.getObject(ia.set(i)));
                    // set maskIter to next idx to remove
                    if (maskIter.hasNext()) {
                        idx = maskIter.next();
                    }
                } else { // replace value at i with default
                    b.setObject(ib.set(i), defaultValue);
                }
            } else {
                if (i == idx) { // replace value at i with default
                    // set index to next index to be removed, if any are left
                    ArrayTools.log.debug("Current index to remove: {}", i);
                    b.setObject(ib.set(i), defaultValue);
                    if (maskIter.hasNext()) {
                        idx = maskIter.next();
                    }
                } else {// copy value at i
                    b.setObject(ib.set(i), a.getObject(ia.set(i)));
                }
            }
        }
        ArrayTools.log.debug("After: {}", b);
        return b;
    }
}
