/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.datastructures.tools;

import cross.exception.ConstraintViolationException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar.StringIterator;
import ucar.ma2.*;
import ucar.nc2.Dimension;

/**
 * Utility class providing methods for Sparse and Dense Arrays.
 *
 * @author Nils Hoffmann
 *
 */
public class ArrayTools {

	private static Logger log = LoggerFactory.getLogger(ArrayTools.class);
	private static int cnt = 0;
	private static HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
	private static Random random;

	public static int[] getShapeForIndexedArrays(List<Array> l) {
		int length = 0;
		for (Array a : l) {
			if(a.getRank()>1) {
				throw new IllegalArgumentException(
						"Can only handle arrays of rank <=1, yours was of rank "
						+ a.getRank());
			}
			length+=a.getShape()[0];
		}
		return new int[]{length};
	}

	public static Dimension[] getDefaultDimensionsForIndexedArray(List<Array> l) {
		EvalTools.notNull(l, ArrayTools.class);
		int[] shape = getShapeForIndexedArrays(l);
		final Dimension[] d = new Dimension[shape.length];
		final String[] names = new String[shape.length];
		for (int i = 0; i < names.length; i++) {
			if (ArrayTools.hm.containsKey(shape[i])) {
				names[i] = "dimension" + ArrayTools.hm.get(shape[i]);
				// System.out.println("Dimension found, reusing "+s[i]);
			} else {
				ArrayTools.hm.put(shape[i], ArrayTools.cnt++);
				names[i] = "dimension" + ArrayTools.hm.get(shape[i]);
				// System.out.println("Dimension not found, creating new
				// "+s[i]);
			}
		}
		for (int i = 0; i < shape.length; i++) {
			d[i] = new Dimension(names[i],shape[i], true, false, false);
		}
		return d;
	}

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
			if (a.getRank() <= 1) {
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
						new Object[]{len, offset, offset + len - 1,
					target.getShape()[0]});
				ArrayTools.log.debug("Source type {}, source class {}", a
						.getElementType(), a.getClass().getName());
				if (!a.getClass().getName().equals(target.getClass().getName())) {
					ArrayTools.log.debug("Source type {} target type {}", a
							.getClass().getName(), target.getClass().getName());
				}
				try {
					MAMath.copyDouble(target.section(new int[]{offset},
							new int[]{len}), a);
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

	public static Array random(Random random, Class<?> elementType, int[] shape) {
		return random(random, 0, 1, elementType, shape);
	}

	public static Array random(Random random, double offset, double scale, Class<?> elementType, int[] shape) {
		Array a = Array.factory(elementType, shape);
		IndexIterator iter = a.getIndexIterator();
		while (iter.hasNext()) {
			iter.setDoubleNext((random.nextDouble() * scale) - offset);
		}
		return a;
	}
}
