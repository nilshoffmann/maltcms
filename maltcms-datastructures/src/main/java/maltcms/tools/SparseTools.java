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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.Sparse;

/**
 * Class providing static utility methods for
 * {@link maltcms.datastructures.array.Sparse} arrays.
 *
 * @author Nils Hoffmann
 * @deprecated You can use the usual array operations on Sparse array instances.
 * 
 */
@Slf4j
public class SparseTools {

    /** Constant <code>es</code> */
    protected static final ExecutorService es = Executors.newFixedThreadPool(4);
    /** Constant <code>normalized</code> */
    public static final Map<Sparse, Double> normalized = Collections
            .synchronizedMap(new HashMap<Sparse, Double>());

    /**
     * <p>create.</p>
     *
     * @param indices a {@link java.util.List} object.
     * @param values a {@link java.util.List} object.
     * @param minindex a int.
     * @param maxindex a int.
     * @param nbins a int.
     * @param massPrecision a double.
     * @return an array of {@link ucar.ma2.Array} objects.
     */
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
            log.info("Building {} Sparse Arrays!", values.size());
            while (idx.hasNext() && vls.hasNext()) {
                try {
                    final Array a = idx.next();
                    final Array b = vls.next();
                    idx.remove();
                    vls.remove();
                    // log.info("Sparse array "+i);
                    s[i++] = new Sparse(a, b, minindex, maxindex, nbins,
                            massPrecision);
                } catch (final ClassCastException cce) {
                    log.warn(cce.getLocalizedMessage());
                }
            }
            return s;
        }
        throw new IllegalArgumentException(
                "Number of elements in argument lists differ!");
    }

    /**
     * <p>createAsList.</p>
     *
     * @param indices a {@link java.util.List} object.
     * @param values a {@link java.util.List} object.
     * @param minindex a int.
     * @param maxindex a int.
     * @param nbins a int.
     * @param massPrecision a double.
     * @return a {@link java.util.List} object.
     */
    public static List<Array> createAsList(final List<Array> indices,
            final List<Array> values, final int minindex, final int maxindex,
            final int nbins, final double massPrecision) {
        final Array[] a = SparseTools.create(indices, values, minindex,
                maxindex, nbins, massPrecision);
        log.info("Length of Array[] created: {}", a.length);
        final ArrayList<Array> arr = new ArrayList<>(a.length);
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
    /**
     * <p>mult.</p>
     *
     * @param s a {@link ucar.ma2.Sparse} object.
     * @param d a double.
     * @return a {@link ucar.ma2.Sparse} object.
     */
    public static Sparse mult(final Sparse s, final double d) {
        final IndexIterator sk = s.getIndexIterator();
        while (sk.hasNext()) {
            sk.setDoubleCurrent(sk.getDoubleNext() * d);
        }
        return s;
    }

//	public static double norm(final Sparse s) {
//		final double norm = Math.sqrt(SparseTools.dot(s, s));
//		// log.info(norm);
//		return norm;
//	}
    /**
     * <p>randomGaussian.</p>
     *
     * @param minindex a int.
     * @param size a int.
     * @param mean a double.
     * @param stddev a double.
     * @return a {@link ucar.ma2.Sparse} object.
     */
    public static Sparse randomGaussian(final int minindex, final int size,
            final double mean, final double stddev) {
        final Sparse s = new Sparse(size, minindex, minindex + size - 1);
        for (int i = 0; i < size; i++) {
            s.set(i, (ArrayTools.nextGaussian() - mean) * stddev);
        }
        return s;
    }

    /**
     * <p>randomUniform.</p>
     *
     * @param minindex a int.
     * @param size a int.
     * @param mean a double.
     * @param scale a double.
     * @return a {@link ucar.ma2.Sparse} object.
     */
    public static Sparse randomUniform(final int minindex, final int size,
            final double mean, final double scale) {
        final Sparse s = new Sparse(size, minindex, minindex + size - 1);
        for (int i = 0; i < size; i++) {
            s.set(i, (ArrayTools.nextUniform() - mean) * scale);
        }
        return s;
    }
}
