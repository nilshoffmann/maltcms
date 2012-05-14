/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import maltcms.datastructures.array.Sparse;

import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import cross.Logging;

/**
 * Class providing static utility methods for
 * {@link maltcms.datastructures.array.Sparse} arrays.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * @deprecated You can use the usual array operations on Sparse array instances.
 */
public class SparseTools {

	protected static final ExecutorService es = Executors.newFixedThreadPool(4);

	public static final Map<Sparse, Double> normalized = Collections
	        .synchronizedMap(new HashMap<Sparse, Double>());

	private static Logger log = Logging.getLogger(SparseTools.class);

//	public static double arccos(final Sparse s, final Sparse t) {
//		return Math.acos(SparseTools.cos(s, t));
//	}


//	public static double cos(final Sparse s, final Sparse t) {
//		synchronized (SparseTools.normalized) {
//			if (!SparseTools.normalized.containsKey(s)) {
//				SparseTools.normalized.put(s, SparseTools.norm(s));
//			}
//			if (!SparseTools.normalized.containsKey(t)) {
//				SparseTools.normalized.put(t, SparseTools.norm(t));
//			}
//		}
//
//		final double dot = SparseTools.dot(s, t);
//		if (dot == 0.0d) {
//			return Math.cos(dot);
//		}
//		// System.out.println("Dot "+dot);
//		// System.out.println("Normalized s "+normalized.get(s));
//		// System.out.println("Normalized t "+normalized.get(t));
//		// (1.0 / 0.0) * 0.0 -> NaN
//		double prod = 0.0d;
//		synchronized (SparseTools.normalized) {
//			prod = SparseTools.normalized.get(s)
//			        * SparseTools.normalized.get(t);
//		}
//		// System.out.println("PRod: "+prod);
//		final double div = dot / prod;
//		// System.out.println(div);
//		double cos = Math.cos(div);
//		if (Double.isNaN(cos)) {// check for NaN
//			cos = 0.0d;
//		}
//		// System.out.println("acos="+acos);
//		return cos;
//	}

	public static Array[] create(final List<Array> indices,
	        final List<Array> values, final int minindex, final int maxindex,
	        final int nbins, final double massPrecision) {
		// FutureTask<Array[]> future = new FutureTask<Array[]>(
		// new Callable<Array[]>() {
		// public Array[] call() {
		if (indices.size() == values.size()) {
			final Iterator<Array> idx = indices.iterator();
			final Iterator<Array> vls = values.iterator();
			final Array[] s = new Array[indices.size()];
			int i = 0;
			SparseTools.log.info("Building {} Sparse Arrays!", values.size());
			while (idx.hasNext() && vls.hasNext()) {
				try {
					final Array a = idx.next();
					final Array b = vls.next();
					idx.remove();
					vls.remove();
					// System.out.println("Sparse array "+i);
					s[i++] = new Sparse(a, b, minindex, maxindex, nbins,
					        massPrecision);
				} catch (final ClassCastException cce) {
					cce.printStackTrace();
				}
			}
			return s;
		}
		throw new IllegalArgumentException(
		        "Number of elements in argument lists differ!");
	}

	public static List<Array> createAsList(final List<Array> indices,
	        final List<Array> values, final int minindex, final int maxindex,
	        final int nbins, final double massPrecision) {
		final Array[] a = SparseTools.create(indices, values, minindex,
		        maxindex, nbins, massPrecision);
		SparseTools.log.info("Length of Array[] created: {}", a.length);
		final ArrayList<Array> arr = new ArrayList<Array>(a.length);
		for (final Array element : a) {
			arr.add(element);
		}
		return arr;
	}

//	public static double dist(final Sparse s, final Sparse t, final double type) {
//		final ArrayLp alp = new ArrayLp();
//		return alp.apply(0, 0, -1, -1, s, t);
//	}
//
//	public static double dot(final Sparse s, final Sparse t) {
//		final ArrayDot ad = new ArrayDot();
//		return ad.apply(0, 0, -1, -1, s, t);
//	}

	public static Sparse mult(final Sparse s, final double d) {
		final IndexIterator sk = s.getIndexIterator();
		while (sk.hasNext()) {
			sk.setDoubleCurrent(sk.getDoubleNext() * d);
		}
		return s;
	}

//	public static double norm(final Sparse s) {
//		final double norm = Math.sqrt(SparseTools.dot(s, s));
//		// System.out.println(norm);
//		return norm;
//	}

	public static Sparse randomGaussian(final int minindex, final int size,
	        final double mean, final double stddev) {
		final Sparse s = new Sparse(size, minindex, minindex + size - 1);
		for (int i = 0; i < size; i++) {
			s.set(i, (ArrayTools.nextGaussian() - mean) * stddev);
		}
		return s;
	}

	public static Sparse randomUniform(final int minindex, final int size,
	        final double mean, final double scale) {
		final Sparse s = new Sparse(size, minindex, minindex + size - 1);
		for (int i = 0; i < size; i++) {
			s.set(i, (ArrayTools.nextUniform() - mean) * scale);
		}
		return s;
	}

}