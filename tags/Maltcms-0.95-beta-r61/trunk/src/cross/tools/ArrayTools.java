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

package cross.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import maltcms.commands.filters.array.MultiplicationFilter;
import maltcms.commands.filters.array.NormalizationFilter;
import maltcms.datastructures.array.Sparse;
import maltcms.tools.MaltcmsTools;

import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Collections;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.MAVector;
import ucar.nc2.Dimension;
import cross.Factory;
import cross.Logging;
import cross.datastructures.StatsMap;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.exception.ConstraintViolationException;

/**
 * Utility class providing methods for Sparse and Dense Arrays.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class ArrayTools {

	private enum State {
		START, END, MATCH, COMP, EXP
	}

	private static Logger log = Logging.getLogger(ArrayTools.class);

	private static int cnt = 0;

	private static HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();

	private static Random random;;

	public static double calcPercentDone(final int elements, final int elemCnt) {
		double percentDone;
		percentDone = (double) elemCnt / (double) elements;
		return percentDone;
	}

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

	public static ArrayDouble.D1 compress(final List<Array> sourceA,
	        final int source, final int length) {
		final ArrayDouble.D1 res = ArrayTools.vector(sourceA.get(source)
		        .getShape()[0]);
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

	public static List<ArrayDouble.D1> convertArrays(final List<Array> al) {
		final ArrayList<ArrayDouble.D1> ret = new ArrayList<ArrayDouble.D1>(al
		        .size());
		// System.out.println("Copying "+al.size()+" scans!");
		for (int i = 0; i < al.size(); i++) {
			final Array a = al.get(i);
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

	public static List<ArrayInt.D1> convertArraysIntD1(final List<Array> al) {
		final ArrayList<ArrayInt.D1> ret = new ArrayList<ArrayInt.D1>(al.size());
		for (int i = 0; i < al.size(); i++) {
			final Array a = al.get(i);
			if (a instanceof ArrayInt.D1) {
				ret.add((ArrayInt.D1) a);
			} else {
				throw new ClassCastException(
				        "Only Arrays of type ArrayDouble.D1 can be converted!");
			}
		}
		return ret;
	}

	public static TreeMap<Double, Integer> convertScan(final Array masses,
	        final Array intensities) {
		// Convert masses and intensities to tree map holding the mass-intensity
		// value pairs
		EvalTools.eqI(masses.getShape()[0], intensities.getShape()[0],
		        ArrayTools.class);
		ArrayTools.log.debug("Adding {} elems", masses.getShape()[0]);
		final TreeMap<Double, Integer> ts = new TreeMap<Double, Integer>();
		final IndexIterator im = masses.getIndexIterator(), ii = intensities
		        .getIndexIterator();
		while (im.hasNext() && ii.hasNext()) {
			final double key = im.getDoubleNext();
			if (ts.containsKey(Double.valueOf(key))) {
				final int value = ts.get(Double.valueOf(key));
				ts.put(Double.valueOf(key), Integer.valueOf(ii.getIntNext()
				        + value));
			} else {
				ts.put(Double.valueOf(key), Integer.valueOf(ii.getIntNext()));
			}
		}
		return ts;
	}

	/**
	 * Returns a Tuple2D holding an ArrayInt.D1 as scan indices to the arrays
	 * held in the Tuple2D, mass values and intensity values.
	 * 
	 * @param al
	 * @return
	 */
	public static Tuple2D<Array, Tuple2D<Array, Array>> createCombinedArray(
	        final List<Sparse> al) {
		final int size = ArrayTools.getRequiredSize(al);
		final ArrayInt.D1 scanIndices = new ArrayInt.D1(al.size());
		final ArrayDouble.D1 targetIndices = new ArrayDouble.D1(size);
		final ArrayDouble.D1 targetValues = new ArrayDouble.D1(size);
		int i = 0;
		for (int scan = 0; scan < al.size(); scan++) {
			// j = i+al.get(scan).getShape()[0]-1;
			final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> tuple = al.get(scan)
			        .toArrays();
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
	 * @param source_ind
	 * @param source_val
	 * @param tiv
	 * @param min
	 * @param max
	 * @param nbins
	 * @param resolution
	 * @param fillvalue
	 */
	public static void createDenseArray(final Array source_ind,
	        final Array source_val, final Tuple2D<Array, Array> tiv,
	        final double min, final double max, final int nbins,
	        final double resolution, final double fillvalue) {
		// System.out.println("Creating a dense Array!");
		final Index nii = source_ind.getIndex();
		final Index vii = source_val.getIndex();
		ArrayTools.log.debug("Size of new arrays {}", (nbins));
		if (tiv.getFirst() == null) {
			tiv.setFirst(Array.factory(source_ind.getElementType(),
			        new int[] { nbins }));
		}
		if (tiv.getSecond() == null) {
			tiv.setSecond(Array.factory(source_val.getElementType(),
			        new int[] { nbins }));
		}
		final Array target_ind = tiv.getFirst();
		final Array target_val = tiv.getSecond();
		final Index tnii = target_ind.getIndex();
		for (int i = 0; i < (nbins); i++) {// set index bins, integer
			// spacing
			target_ind.setDouble(tnii.set(i), (min + i));
		}
		final Index tvii = target_val.getIndex();
		// System.out.println("Source index array: "+source_ind.getShape()[0]);
		// System.out.println("Source value array: "+source_val.getShape()[0]);

		// System.out.println("Target index array: "+target_ind.getShape()[0]);
		// System.out.println("Target value array: "+target_val.getShape()[0]);
		int curr = 0;
		int last = -1;
		double lastm = -1;
		double currm = 0;
		final boolean average = Factory.getInstance().getConfiguration()
		        .getBoolean("ArrayTools.createDenseArray.average_bins", true);
		MaltcmsTools.setBinMZbyConfig();
		final ArrayInt.D1 overlapCounter = new ArrayInt.D1(nbins);
		for (int i = 0; i < source_ind.getShape()[0]; i++) {// fill in real
			// masses
			final double d = source_ind.getDouble(nii.set(i));
			currm = d;
			if (d < min) {
				throw new IllegalArgumentException(
				        "Found mass value below minimum! " + d + "<" + min);
			}
			// System.out.println("Index "+i+" d="+d);
			// values
			final double v = source_val.getDouble(vii.set(i));
			// System.out.println("Value "+i+" v="+v);
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
					        new Object[] { lastm, currm, curr });
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
				ArrayTools.log
				        .error(
				                "ArrayIndexOutOfBounds: tried to access binned mz {} with original value {}, min_mz {}",
				                new Object[] { curr, d, min });
				ArrayTools.log.error(
				        "ArrayIndexOutOfBounds: Shape of target array: {}",
				        Arrays.toString(target_ind.getShape()));
				throw (new RuntimeException(aioe.getLocalizedMessage()));
			}
			last = curr;
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

	public static Sparse createSparseIndexArray(final ArrayDouble.D1 index,
	        final ArrayDouble.D1 values, final int minindex,
	        final int maxindex, final int nbins, final double massBinResolution) {
		final Sparse s = new Sparse(index, values, minindex, maxindex, nbins,
		        massBinResolution);
		return s;
	}

	public static ArrayChar.D2 createStringArray(final int numstrings,
	        final int maxlength) {
		final ArrayChar.D2 d = new ArrayChar.D2(numstrings, maxlength);
		return d;
	}

	/**
	 * Calculates A-B elementwise.
	 * 
	 * @param a
	 * @param b
	 * @return
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
	 * @param a
	 * @param b
	 * @return
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
	 * Divides values in array by 60, convenience method for second to minute
	 * conversion.
	 * 
	 * @param a
	 * @return
	 */
	public static Array divBy60(final Array a) {
		return ArrayTools.mult(a, 1.0d / 60.0d);
	}

	/**
	 * Expansion, as used in Dynamic Time Warping, defined as copying the
	 * expanded element length times.
	 * 
	 * @param source
	 * @param length
	 * @return
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
			return new ArrayList<ArrayDouble.D1>(Arrays.asList(arr));
		} else {
			throw new IllegalArgumentException(
			        "Expansion only works on ArrayDouble.D1!");
		}
	}

	/**
	 * Fill array a with double value d.
	 * 
	 * @param a
	 * @param d
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
	 * @param a
	 * @param d
	 */
	public static void fillArray(final Array a, final Double d) {
		fill(a, d.doubleValue());
	}

	/**
	 * Fill array a with Integer value i.
	 * 
	 * @param a
	 * @param i
	 */
	public static void fillArray(final Array a, final Integer i) {
		fillArray(a, i.intValue());
	}

	/**
	 * Fill array a with int value i.
	 * 
	 * @param a
	 * @param i
	 */
	public static void fillArray(final Array a, final int i) {
		final IndexIterator ii = a.getIndexIterator();
		while (ii.hasNext()) {
			ii.setIntNext(i);
		}
	}

	/**
	 * Restore List of Array objects from contiguous representation, using an
	 * index array containing the offsets for each individual array.
	 * 
	 * @param indices
	 * @param values
	 * @return
	 */
	public static List<Array> fromCRS(final ArrayInt.D1 indices,
	        final Array values) {
		final int size = indices.getShape()[0];
		final ArrayList<Array> al = new ArrayList<Array>(size);
		int offset = 0;
		int len = 0;
		for (int i = 0; i < size - 1; i++) {
			offset = indices.get(i);
			len = indices.get(i + 1) - 1 - offset;
			ArrayTools.log.debug("Range for scan {}: Offset {}, Length: {}",
			        new Object[] { i, offset, len });
			try {
				final Array a = values.section(new int[] { offset },
				        new int[] { len });
				// System.out.println("Scan " + (i + 1));
				// System.out.println(a.toString());
				al.add(a);
			} catch (final InvalidRangeException e) {
				throw new ConstraintViolationException(e);
			}
		}
		offset = indices.get(size - 1);
		len = values.getShape()[0] - offset;
		ArrayTools.log
		        .debug("Offset: {}, len: {}, shape of array: {}", new Object[] {
		                offset, len, Arrays.toString(values.getShape()) });
		try {
			final Array a = values.section(new int[] { offset },
			        new int[] { len });
			// System.out.println("Scan " + size);
			// System.out.println(a.toString());
			al.add(a);
		} catch (final InvalidRangeException e) {
			throw new ConstraintViolationException(e);
		}
		return al;
	}

	/**
	 * Return a list, which is more generally typed than the input list.
	 * 
	 * @param al
	 * @return
	 */
	public static List<Array> generalizeList(final List<ArrayDouble.D1> al) {
		final ArrayList<Array> ret = new ArrayList<Array>(al);
		return ret;
	}

	/**
	 * Return default dimensions for input array.
	 * 
	 * @param a
	 * @return
	 */
	public static Dimension[] getDefaultDimensions(final Array a) {
		EvalTools.notNull(a, ArrayTools.class);
		final Dimension[] d = new Dimension[a.getRank()];
		final String[] names = ArrayTools.getDefaultDimnames(a);
		for (int i = 0; i < a.getRank(); i++) {
			d[i] = new Dimension(names[i], a.getShape()[i], true, false, false);
		}
		return d;
	}

	/**
	 * Get names of default dimensions for input array.
	 * 
	 * @param a
	 * @return
	 */
	public static String[] getDefaultDimnames(final Array a) {
		final String[] s = new String[a.getShape().length];
		for (int i = 0; i < s.length; i++) {
			if (ArrayTools.hm.containsKey(a.getShape()[i])) {
				s[i] = "dimension" + ArrayTools.hm.get(a.getShape()[i]);
				// System.out.println("Dimension found, reusing "+s[i]);
			} else {
				ArrayTools.hm.put(a.getShape()[i], ArrayTools.cnt++);
				s[i] = "dimension" + ArrayTools.hm.get(a.getShape()[i]);
				// System.out.println("Dimension not found, creating new
				// "+s[i]);
			}
		}
		return s;
	}

	public static int getNewIndexOnLHS(final int oldIndex,
	        final List<Tuple2DI> al) {
		final List<Integer> indices = new ArrayList<Integer>(1);
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

	public static int getNewIndexOnRHS(final int oldIndex,
	        final List<Tuple2DI> al) {

		final List<Integer> indices = new ArrayList<Integer>(1);
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
	 * @param al
	 * @return
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
	 * @param a
	 * @return
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
	 * Compute total number of elements for all arrays in list.
	 * 
	 * @param scans
	 * @return
	 */
	public static int getSizeForFlattenedArrays(final List<Array> scans) {
		int size = 0;
		for (final Array a : scans) {
			if (a.getRank() == 1) {
				size += a.getShape()[0];// ArrayTools.getSizeForFlattenedArray
				// (a);
			} else {
				throw new IllegalArgumentException(
				        "Can only handle arrays of rank 1, yours was of rank "
				                + a.getRank());
			}
		}
		return size;
	}

	/**
	 * Concatenates one-dimensional arrays in list into one contiguous array, in
	 * iteration order. Note that this method currently only accepts numeric
	 * arrays as input and returns an Array of type ArrayDouble.D1.
	 * 
	 * Access to the list is synchronized within the method, so access will be
	 * exclusive to this method while executing it.
	 * 
	 * @param al
	 * @return
	 */
	public static Array glue(final List<Array> al) {
		synchronized (al) {
			EvalTools.notNull(al, ArrayTools.class);
			ArrayTools.log
			        .debug("Glueing array list with {} arrays", al.size());
			final int size = ArrayTools.getSizeForFlattenedArrays(al);
			int offset = 0, len = 0;
			final Array target = new ArrayDouble.D1(size);
			ArrayTools.log.debug("Glue: Target type is: {}", target
			        .getElementType());
			Iterator<Array> iter = al.iterator();
			while (iter.hasNext()) {
				final Array a = iter.next();
				if (a.getRank() > 1) {
					throw new IllegalArgumentException(
					        "Only one-dimensional arrays can be glued!");
				}
				len = a.getShape()[0];
				ArrayTools.log
				        .debug(
				                "Copying {} elements from {} to {} in target with {} elements",
				                new Object[] { len, offset, offset + len - 1,
				                        target.getShape()[0] });
				ArrayTools.log.debug("Source type {}, source class {}", a
				        .getElementType(), a.getClass().getName());
				if (!a.getClass().getName().equals(target.getClass().getName())) {
					ArrayTools.log.debug("Source type {} target type {}", a
					        .getClass().getName(), target.getClass().getName());
				}
				try {
					MAMath.copyDouble(target.section(new int[] { offset },
					        new int[] { len }), a);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidRangeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				offset += len;
			}
			return target;
		}

	}

	/**
	 * Create an index array with given size, whose integer indexing begins at
	 * begin_index.
	 * 
	 * @param size
	 * @param begin_index
	 * @return
	 */
	public static ArrayInt.D1 indexArray(final int size, final int begin_index) {
		final ArrayInt.D1 arr = new ArrayInt.D1(size);
		for (int i = 0; i < size; i++) {
			arr.set(i, i + begin_index);
		}
		return arr;
	}

	public static void initRandom() {
		ArrayTools.random = new Random();
	}

	public static void initRandom(final long seed) {
		ArrayTools.random = new Random(seed);
	}

	public static List<Array> insertRandomGauss(
	        final List<Array> intensity_values, final int at_position,
	        final int number_of_scans, final int dim1) {
		final int dim = intensity_values.size() > 0 ? intensity_values.get(0)
		        .getShape()[0] : dim1;
		final ArrayList<Array> newScans = new ArrayList<Array>(number_of_scans);
		for (int i = 0; i < number_of_scans; i++) {
			newScans.add(ArrayTools.randomGaussian(dim, 0.0, 1.0));
		}
		intensity_values.addAll(at_position, newScans);
		return intensity_values;
	}

	public static double integrate(final Array a) {
		return MAMath.sumDouble(a);
	}

	/**
	 * Sum over all values of all arrays within a list.
	 * 
	 * @param valuesvalues
	 * @return
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

	public static ArrayDouble.D2 matrix(final int rows, final int columns) {
		final ArrayDouble.D2 a = new ArrayDouble.D2(rows, columns);
		return a;
	}

	public static TreeMap<Double, Integer> merge(
	        final List<TreeMap<Double, Integer>> rhs, boolean average) {
		final HashMap<Double, Integer> bincounter = new HashMap<Double, Integer>();
		final TreeMap<Double, Integer> lhs = new TreeMap<Double, Integer>();
		// Build cumulated bins
		for (final TreeMap<Double, Integer> tm : rhs) {
			ArrayTools.log.debug("Merging scan with {} elements", tm.keySet()
			        .size());
			final ArrayList<Double> keys = new ArrayList<Double>(tm.keySet());
			// Collections.sort(keys);
			for (final Double d : keys) {
				if (lhs.containsKey(d)) {
					// accumulate new intensity in bin
					final Integer tmp = Integer.valueOf(lhs.get(d).intValue()
					        + tm.get(d).intValue());
					lhs.put(d, tmp);
					// Count number of hits in mass bin
					if (bincounter.containsKey(d)) {
						final Integer integ = bincounter.get(d);
						bincounter
						        .put(d, Integer.valueOf(integ.intValue() + 1));
					} else {
						bincounter.put(d, Integer.valueOf(1));
					}
				} else {
					lhs.put(d, tm.get(d));
					bincounter.put(d, Integer.valueOf(1));
				}
			}
		}
		// Correct for multiple hits in one bin
		for (Double d : lhs.keySet()) {
			// bincounter was increased, so set to average
			if (bincounter.containsKey(d)) {
				Integer count = bincounter.get(d);
				Integer val = lhs.get(d);
				lhs.put(d, Integer.valueOf((int) Math.rint(((double) val
				        .intValue())
				        / ((double) count.intValue()))));
			}
		}
		return lhs;
	}

	public static Array mult(final Array a, final double d) {
		final Array ret = a.copy();
		final IndexIterator iter = ret.getIndexIterator();
		while (iter.hasNext()) {
			iter.setDoubleCurrent(iter.getDoubleNext() * d);
		}
		return ret;
	}

	public static double nextGaussian() {
		if (ArrayTools.random == null) {
			ArrayTools.initRandom();
		}
		return ArrayTools.random.nextGaussian();
	}

	public static double nextUniform() {
		if (ArrayTools.random == null) {
			ArrayTools.initRandom();
		}
		return ArrayTools.random.nextDouble();
	}

	public static Array normalize(final Array a, final StatsMap sm,
	        final String normalization, final boolean log1) {
		final MultiplicationFilter mf = new MultiplicationFilter(
		        1.0d / (log1 ? (Math.log(1.0d + EvalTools.eval(normalization,
		                sm))) : (EvalTools.eval(normalization, sm))));
		return mf.apply(new Array[] { a })[0];
	}

	public static List<Array> normalize(final List<Array> al,
	        final String normalization) {
		final NormalizationFilter nf = Factory.getInstance().instantiate(
		        NormalizationFilter.class);
		nf.setNormalization(normalization);
		nf.setNormalizeGlobal(false);
		nf.setLog(false);
		return Arrays.asList(nf.apply(al.toArray(new Array[] {})));
	}

	public static List<Array> normalizeGlobal(final List<Array> al) {
		final NormalizationFilter nf = Factory.getInstance().instantiate(
		        NormalizationFilter.class);
		nf.setNormalization("Max-Min");
		nf.setNormalizeGlobal(true);
		nf.setLog(false);
		return Arrays.asList(nf.apply(al.toArray(new Array[] {})));
	}

	public static List<Array> normalizeLocalToUnitLength(final List<Array> al) {
		for (final Array a : al) {
			if (a.getRank() == 1) {
				final MAVector mav = new MAVector(a);
				mav.normalize();
			} else {
				ArrayTools.log
				        .error(
				                "Can not normalize a matrix to unit length, array has rank {}",
				                a.getRank());
			}
		}
		return al;
	}

	public static Array pow(final Array a, final double exp) {
		final IndexIterator ii1 = a.getIndexIterator();
		final Array ret = Array.factory(a.getElementType(), a.getShape());
		final IndexIterator reti = ret.getIndexIterator();
		// Iterators should be conformable, so only check one
		while (ii1.hasNext()) {
			reti.setDoubleNext(Math.pow(ii1.getDoubleNext(), exp));
		}
		return ret;
	}

	public static int printPercentDone(final double percentDone,
	        final int parts, final int partCnt1, final Logger log) {
		int partCnt = partCnt1;
		if (percentDone >= (double) (partCnt + 1) / (double) parts) {
			log.info("{}%", (int) (percentDone * 100));
			partCnt++;

		}
		return partCnt;
	}

	public static Tuple2D<List<Array>, List<Array>> merge2(
	        final List<Array> lhsMasses, final List<Array> lhsIntensities,
	        final List<Array> rhsMasses, final List<Array> rhsIntensities,
	        final List<Tuple2DI> al, boolean average) {

		// Create warped lists
		final ArrayList<ArrayList<Array>> warpedMasses = new ArrayList<ArrayList<Array>>(
		        al.size());
		final ArrayList<ArrayList<Array>> warpedIntens = new ArrayList<ArrayList<Array>>(
		        al.size());
		// Initialize ArrayLists
		for (int i = 0; i < al.size(); i++) {
			warpedMasses.add(new ArrayList<Array>(0));
			warpedIntens.add(new ArrayList<Array>(0));
		}
		// Create final ArrayLists, which will contain warped arrays
		final ArrayList<Array> wmasses = new ArrayList<Array>(warpedMasses
		        .size());
		final ArrayList<Array> wintens = new ArrayList<Array>(warpedIntens
		        .size());
		// Proceed through path
		int i = 0;
		for (final Tuple2DI t : al) {
			Array lhsM = lhsMasses.get(t.getFirst()).copy();
			Array rhsM = rhsMasses.get(t.getSecond()).copy();

			Array lhsI = lhsIntensities.get(t.getFirst()).copy();
			Array rhsI = rhsIntensities.get(t.getSecond()).copy();

			// Create TreeMaps for scans
			final ArrayList<TreeMap<Double, Integer>> al1 = new ArrayList<TreeMap<Double, Integer>>(
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

	public static Tuple2D<List<Array>, List<Array>> project2(
	        final boolean toLHS, final List<Array> massesRef,
	        final List<Array> intenRef, final List<Tuple2DI> al,
	        final List<Array> massesToBeWarped,
	        final List<Array> intenToBeWarped, boolean average) {
		// Check, that dimensions match
		EvalTools.eqI(massesToBeWarped.size(), intenToBeWarped.size(),
		        ArrayTools.class);
		EvalTools.eqI(massesRef.size(), intenRef.size(), ArrayTools.class);
		// Create warped lists
		final ArrayList<ArrayList<Array>> warpedMasses = new ArrayList<ArrayList<Array>>(
		        massesRef.size());
		final ArrayList<ArrayList<Array>> warpedIntens = new ArrayList<ArrayList<Array>>(
		        massesRef.size());
		// Initialize ArrayLists
		for (int i = 0; i < massesRef.size(); i++) {
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
		final ArrayList<Array> wmasses = new ArrayList<Array>(warpedMasses
		        .size());
		final ArrayList<Array> wintens = new ArrayList<Array>(warpedIntens
		        .size());
		for (int i = 0; i < warpedMasses.size(); i++) {
			final ArrayList<Array> mz = warpedMasses.get(i);
			final ArrayList<Array> in = warpedIntens.get(i);
			// if we map multiple elements to one=> compression => merge
			if (mz.size() > 1) {
				// Create TreeMaps for scans
				final ArrayList<TreeMap<Double, Integer>> al1 = new ArrayList<TreeMap<Double, Integer>>(
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

	public static Array projectToLHS(final Array lhs, final List<Tuple2DI> al,
	        final Array rhs, final boolean average) {
		final Array rhsm = Array.factory(lhs.getElementType(), lhs.getShape());
		final Array binCounter = Array.factory(lhs.getElementType(), lhs
		        .getShape());
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

	public static Array projectToRHS(final Array rhs, final List<Tuple2DI> al,
	        final Array lhs, final boolean average) {
		final Array lhsm = Array.factory(rhs.getElementType(), rhs.getShape());
		final Array binCounter = Array.factory(rhs.getElementType(), rhs
		        .getShape());
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

	public static ArrayDouble.D1 randomGaussian(final int size,
	        final double mean, final double stddev) {
		final ArrayDouble.D1 arr = ArrayTools.vector(size);
		for (int i = 0; i < size; i++) {
			arr.set(i, (ArrayTools.nextGaussian() - mean) * stddev);
		}
		return arr;
	}

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

	public static double rootMeanSquareError(final List<Tuple2DI> map,
	        final List<Array> refInt, final List<Array> queryInt) {
		// ArrayList<Array> res = new ArrayList<Array>(refInt.size());
		// int i = 0;
		double res = 0.0d;
		for (final Tuple2DI t : map) {
			res += ArrayTools.integrate(ArrayTools.pow(ArrayTools.diff(refInt
			        .get(t.getFirst()).copy(), queryInt.get(t.getSecond())
			        .copy()), 2.0d));
		}
		return Math.sqrt((res / (map.size())));
	}

	public static ArrayDouble.D0 scalar(final double d) {
		final ArrayDouble.D0 a = new ArrayDouble.D0();
		a.set(d);
		return a;
	}

	public static Array sq(final Array a) {
		final IndexIterator ii1 = a.getIndexIterator();
		final Array ret = Array.factory(a.getElementType(), a.getShape());
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
	 * @param a
	 * @param b
	 * @return
	 */
	public static Array sum(final Array a, final Array b) {
		if (MAMath.conformable(a, b)) {
			final IndexIterator ii1 = a.getIndexIterator();
			final IndexIterator ii2 = b.getIndexIterator();
			final Array ret = Array.factory(a.getElementType(), a.getShape());
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
	 * @param al
	 * @return
	 */
	public static List<ArrayDouble.D1> tiltD1(final List<ArrayDouble.D1> al) {
		EvalTools.notNull(al, ArrayTools.class);
		final int size = al.get(0).getShape()[0];
		final ArrayList<ArrayDouble.D1> ret = new ArrayList<ArrayDouble.D1>();
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
	 * Creates a new Array containing all values of elements in al along every
	 * dimension.
	 * 
	 * @param al
	 * @return
	 */
	public static List<Array> tilt(final List<Array> al) {
		EvalTools.notNull(al, ArrayTools.class);
		final int size = al.get(0).getShape()[0];
		final ArrayList<Array> ret = new ArrayList<Array>();
		final int nscans = al.size();
		// initialize new arrays
		for (int i = 0; i < size; i++) {
			ret.add(Array.factory(al.get(0).getElementType(),
			        new int[] { nscans }));
		}
		for (int scans = 0; scans < nscans; scans++) {
			final Array source = al.get(scans);
			final Index sidx = source.getIndex();
			// iterate over elements in each ArrayDouble.D1
			for (int i = 0; i < size; i++) {
				final Index ridx = ret.get(i).getIndex();
				// get array for dim i, set value at scan to value of dim i in
				// source
				ret.get(i).setDouble(ridx.set(scans),
				        source.getDouble(sidx.set(i)));
			}
		}
		return ret;
	}

	public static Tuple2D<Array, Array> toArrays(
	        final TreeMap<Double, Integer> s) {
		// Convert treemap of scan to mass and intensity array of scan
		final int elems = s.size();
		ArrayTools.log.debug("Number of elems in scan {}", elems);
		final ArrayDouble.D1 masses = new ArrayDouble.D1(elems);
		final ArrayInt.D1 intens = new ArrayInt.D1(elems);
		final ArrayList<Double> keys = new ArrayList<Double>(s.keySet());
		Collections.sort(keys);
		final Iterator<Double> iter = keys.iterator();
		int i = 0;
		while (iter.hasNext()) {
			final Double d = iter.next();
			masses.set(i, d);
			intens.set(i, s.get(d).intValue());
			i++;
		}
		return new Tuple2D<Array, Array>(masses, intens);
	}

	public static Tuple2D<ArrayDouble.D1, ArrayDouble.D1> toCRS(
	        final List<Array> values) {
		final int size = ArrayTools.getSizeForFlattenedArrays(values);
		ArrayTools.log.debug("Number of Arrays in List: {}", values.size());
		ArrayTools.log.debug("Size for flattened Arrays: {}", size);
		final List<ArrayDouble.D1> al = ArrayTools.convertArrays(values);
		final ArrayDouble.D1 target = new ArrayDouble.D1(size);
		final ArrayDouble.D1 indices = new ArrayDouble.D1(values.size());
		int offset = 0;
		for (int i = 0; i < values.size(); i++) {
			final ArrayDouble.D1 scan = al.get(i);
			if (scan instanceof Sparse) {
				return ((Sparse) scan).toArrays();
			} else {
				final int len = scan.getShape()[0];
				ArrayTools.log.debug("Scan " + i + " from " + 0
				        + " with length " + len + " to target with size "
				        + target.getShape()[0] + " at position " + (offset));
				// System.out.println(scan.toString());
				Array.arraycopy(scan, 0, target, offset, len);
				indices.set(i, offset);
				offset += len;
			}
		}
		return new Tuple2D<ArrayDouble.D1, ArrayDouble.D1>(indices, target);
	}

	public static ArrayDouble.D1 vector(final int elems) {
		final ArrayDouble.D1 a = new ArrayDouble.D1(elems);
		return a;
	}

}
