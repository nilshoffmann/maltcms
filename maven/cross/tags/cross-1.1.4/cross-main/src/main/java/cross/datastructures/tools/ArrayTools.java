/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
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
 * $Id: ArrayTools.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */

package cross.datastructures.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.ArrayChar.StringIterator;
import ucar.nc2.Dimension;
import cross.exception.ConstraintViolationException;

/**
 * Utility class providing methods for Sparse and Dense Arrays.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class ArrayTools {

	private static Logger log = LoggerFactory.getLogger(ArrayTools.class);

	private static int cnt = 0;

	private static HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();

	private static Random random;;

	public static ArrayChar.D2 createStringArray(final int numstrings,
	        final int maxlength) {
		final ArrayChar.D2 d = new ArrayChar.D2(numstrings, maxlength);
		return d;
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

	public static Collection<String> getStringsFromArray(final Array a) {
		ArrayTools.log.debug("Retrieved Array: {}", a);
		EvalTools.notNull(a, FragmentTools.class);
		ArrayList<String> s = new ArrayList<String>();
		if (a instanceof ArrayChar) {
			final ArrayChar d = ((ArrayChar) a);
			s = new ArrayList<String>();
			final StringIterator si = d.getStringIterator();
			while (si.hasNext()) {
				final String str = si.next();
				ArrayTools.log.debug("Adding String {}", str);
				s.add(str);
			}
			return s;
		} else {
			throw new ConstraintViolationException(
			        "Array is not of type ArrayChar.D2, but of type: "
			                + a.getClass().getName());
		}
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
			final Iterator<Array> iter = al.iterator();
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
				} catch (final IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final InvalidRangeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				offset += len;
			}
			return target;
		}

	}

}
