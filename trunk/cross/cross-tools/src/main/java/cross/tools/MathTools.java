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
package cross.tools;

import java.util.*;

/**
 * Utility class providing some mathematical helpers.
 *
 * @author Nils Hoffmann
 *
 */
public class MathTools {

	static double[] faculty = null;

	public static double average(final double[] d, final int i, final int j) {
		final int ii = Math.max(0, i);
		final int jj = Math.min(d.length - 1, j);
		double average = 0;
		for (int k = ii; k <= jj; k++) {
			average += d[k];
		}
		return average / (jj - ii);
	}

	public static double averageOfSquares(final double[] d, final int i,
			final int j) {
		final int ii = Math.max(0, i);
		final int jj = Math.min(d.length - 1, j);
		double average = 0;
		for (int k = ii; k <= jj; k++) {
			average += Math.pow(d[k], 2.0d);
		}
		return average / (jj - ii);
	}

	public static double[] binCoeff(final int n) {
		final double[] coeffs = new double[n + 1];
		for (int i = 0; i < n + 1; i++) {
			try {
				coeffs[i] = MathTools.binomial(n, i);
			} catch (final IllegalArgumentException iae) {
				coeffs[i] = -1.0d;
			}
		}
		return coeffs;
	}

	public static double binomial(final int n, final int k) {
		if (n >= 171) {
			throw new IllegalArgumentException(
					"Cannot calculate beyond faculty(171)!");
			// return 0.0d;
		}
		final double b = (MathTools.faculty(k) * MathTools.faculty(n - k));
		// if(b==0){
		// return (faculty(n));
		// }
		// System.out.println("n "+n+" k "+k+" b "+b+" "+faculty(k)+" "+(n-k)+"
		// "+faculty(n-k));
		// System.out.println(faculty(n-k));
		return MathTools.faculty(n) / b;
	}

	public static final Collection<Integer> diff(
			final Collection<Integer> values) {
		final ArrayList<Integer> diffs = new ArrayList<Integer>();
		if (values.size() <= 1) {
			return diffs;
		}
		final Integer[] vals = values.toArray(new Integer[]{});
		int l = vals[0].intValue();
		for (int i = 1; i < vals.length; i++) {
			final int diff = vals[i].intValue() - l;
			diffs.add(Integer.valueOf(diff));
			l = diff + l;
		}
		return diffs;
	}

	/**
	 * Returns exact faculty for n<=170. Above, Stirling's approximation is
	 * used. @param n @return
	 */
	public static double faculty(final int n) {
		if (MathTools.faculty == null) {
			MathTools.faculty = new double[171];
			MathTools.faculty[0] = 1;
			for (int i = 1; i < MathTools.faculty.length; i++) {
				MathTools.faculty[i] = MathTools.faculty[i - 1] * i;
				// if(faculty[i]==Double.POSITIVE_INFINITY){
				// System.out.println(i+" "+faculty[i]);
				// }
			}
		}

		return MathTools.faculty[n];
	}

	public static double getLinearInterpolatedY(final double x0,
			final double y0, final double x1, final double y1, final double x) {
		if ((x1 == x0) || (x == x0) || (y1 == y0)) {
			return y0;
		}
		return y0 + ((x - x0) / (x1 - x0)) * (y1 - y0);
	}

	public static double max(final double... d) {
		double max = Double.NEGATIVE_INFINITY;
		for (final double dbl : d) {
			max = Math.max(dbl, max);
		}
		return max;
	}

	public static double max(final double[] d, final int i, final int j) {
		final int ii = Math.max(0, i);
		final int jj = Math.min(d.length - 1, j);
		double max = Double.NEGATIVE_INFINITY;
		for (int k = ii; k <= jj; k++) {
			max = Math.max(max, d[k]);
		}
		return max;
	}

	public static int max(final int... d) {
		int max = Integer.MIN_VALUE;
		for (final int dbl : d) {
			max = Math.max(dbl, max);
		}
		return max;
	}

	public static int max(final int[] d, final int i, final int j) {
		final int ii = Math.max(0, i);
		final int jj = Math.min(d.length - 1, j);
		int max = Integer.MIN_VALUE;
		for (int k = ii; k <= jj; k++) {
			max = Math.max(max, d[k]);
		}
		return max;
	}

	public static double median(final Collection<Integer> values) {
		final List<Integer> c = new ArrayList<Integer>(values);
		Collections.sort(c);
		if (c.size() <= 1) {
			return c.get(0);
		}
		if (c.size() % 2 == 0) {
			// System.out.print("| %2 == 0 ");
			final double v1 = c.get((c.size() / 2) - 1).doubleValue();
			final double v2 = c.get((c.size() / 2)).doubleValue();
			return (v1 + v2) / 2.0d;
		} else {
			// System.out.print("| %2 != 0 ");
			return c.get(c.size() / 2);
		}
	}

	public static double median(final double... values) {
		Arrays.sort(values);
		// for(int i = 0;i<values.length;i++){
		// System.out.print(values[i]+", ");
		// }
		return MathTools.medianOnSorted(values);
	}

	public static double median(final double[] d, final int i, final int j) {
		final int ii = Math.max(0, i);
		final int jj = Math.min(d.length - 1, j);
		final double median = MathTools.median(Arrays.copyOfRange(d, ii, jj));
//		LoggerFactory.getLogger(MathTools.class).debug("{}", median);
		return median;
	}

	public static double median(final double[][] values) {
		final int sx = values.length;
		int elems = 0;
		// Count number of elements (arrays can be ragged)
		for (int i = 0; i < sx; i++) {
			elems += values[i].length;
		}
		final double[] medianvals = new double[elems];

		elems = 0;
		// Copy all arrays in values to medianvals
		for (int i = 0; i < sx; i++) {
			final int len = values[i].length;
			System.arraycopy(values[i], 0, medianvals, elems, len);
			elems += len;
		}
		return MathTools.median(medianvals);
	}

	public static double median(final int... values) {
		Arrays.sort(values);
		// for(int i = 0;i<values.length;i++){
		// System.out.print(values[i]+", ");
		// }
		double median = 0;
		if (values.length % 2 == 0) {
			// System.out.print("| %2 == 0 ");
			final double v1 = values[(values.length / 2) - 1];
			final double v2 = values[values.length / 2];
			median = (v1 + v2) / 2.0d;
		} else {
			// System.out.print("| %2 != 0 ");
			median = values[values.length / 2];
		}
		// Logging.getLogger(MathTools.class).info("{}",Arrays.toString(values));
		// System.out.println(" median = "+median);
		return median;
	}

	public static double medianOnSorted(final double... values) {
		double median = 0;
		if (values.length % 2 == 0) {
			// System.out.print("| %2 == 0 ");
			final double v1 = values[(values.length / 2) - 1];
			final double v2 = values[values.length / 2];
			median = (v1 + v2) / 2.0d;
		} else {
			// System.out.print("| %2 != 0 ");
			median = values[values.length / 2];
		}
		// Logging.getLogger(MathTools.class).info("{}",Arrays.toString(values));
		// System.out.println(" median = "+median);
		return median;
	}

	public static double min(final double... d) {
		double min = Double.POSITIVE_INFINITY;
		for (final double dbl : d) {
			min = Math.min(dbl, min);
		}
		return min;
	}

	public static double min(final double[] d, final int i, final int j) {
		final int ii = Math.max(0, i);
		final int jj = Math.min(d.length - 1, j);
		double min = Double.POSITIVE_INFINITY;
		for (int k = ii; k <= jj; k++) {
			min = Math.min(min, d[k]);
		}
		return min;
	}

	public static int min(final int... d) {
		int min = Integer.MAX_VALUE;
		for (final int dbl : d) {
			min = Math.min(dbl, min);
		}
		return min;
	}

	public static int min(final int[] d, final int i, final int j) {
		final int ii = Math.max(0, i);
		final int jj = Math.min(d.length - 1, j);
		int min = Integer.MAX_VALUE;
		for (int k = ii; k <= jj; k++) {
			min = Math.min(min, d[k]);
		}
		return min;
	}

	public static double[] relativeBinCoeff(final int n) {
		final double[] c = MathTools.binCoeff(n);
		double sum = 0;
		for (final Double d : c) {
			sum += d;
		}
		if (sum == 0) {
			sum = 1;
		}
		for (int i = 0; i < c.length; i++) {
			if (c[i] < 0.0d) {
				c[i] = 0.0d;
			}
			c[i] = c[i] / sum;
		}
		return c;
	}

	public static double sum(final double[] d) {
		double sum = 0.0d;
		for (double dbl : d) {
			sum += dbl;
		}
		return sum;
	}

	public static double[] weightedAverage(int r, final double[] d) {
		double[] ret = new double[d.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = weightedAverage(d, i - r, i + r);
		}
		return ret;
	}

	public static double weightedAverage(final double[] d, final int i,
			final int j) {
		final int ii = Math.max(0, i);
		final int jj = Math.min(d.length - 1, j);
		double average = 0;
		final double[] bc = MathTools.binCoeff(jj - ii);
//		LoggerFactory.getLogger(MathTools.class).debug("len: {}, {}", bc.length,
//		        Arrays.toString(bc));
		double bcnorm = 0;
		for (int k = 0; k < bc.length; k++) {
			bcnorm += bc[k];
		}
		for (int k = ii; k <= jj; k++) {
			average += ((bc[k - ii] / bcnorm) * d[k]);
		}
		return average;// / (jj - ii);
	}

	public static double[] dilate(int r, double[] d) {
		double[] ret = new double[d.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = dilate(i, r, d);
		}
		return ret;
	}

	public static double[] erode(int r, double[] d) {
		double[] ret = new double[d.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = erode(i, r, d);
		}
		return ret;
	}

	public static double dilate(int i, int r, double[] d) {
		return MathTools.max(d, i - r, i + r);
	}

	public static double erode(int i, int r, double[] d) {
		return MathTools.min(d, i - r, i + r);
	}

	public static double[] opening(int r, double[] d) {
		return dilate(r, erode(r, d));
	}

	public static double[] closing(int r, double[] d) {
		return erode(r, dilate(r, d));
	}

	public static double[] topHat(int r, double[] d) {
		double[] opening = opening(r, d);
		double[] ret = new double[d.length];
		for (int i = 0; i < d.length; i++) {
			ret[i] = d[i] - opening[i];
		}
		return ret;
	}

	public static double[] bottomHat(int r, double[] d) {
		double[] closing = closing(r, d);
		double[] ret = new double[d.length];
		for (int i = 0; i < d.length; i++) {
			ret[i] = closing[i] - d[i];
		}
		return ret;
	}

	public static int[] seq(int from, int to, int by) {
		//0..11, by 2 -> 0,2,4,6,8,10
		//11-0 -> ceil(11/2) = 5.x 
		int quotient = Math.abs((to - from) / by);
		int steps = 1 + (int) quotient;
		System.out.println("Steps: " + steps);
		int[] values = new int[steps];
		int i = from;
		for (int cnt = 0; cnt < values.length; cnt++) {
//            values[];
			values[cnt] = i;
			i += by;
		}
		return values;
	}

	public static double[] seq(double from, double to, double by) {
		System.out.println("From " + from + " to " + to + " by " + by);
		double quotient = Math.abs((to - from) / by);
		System.out.println("Quotient: " + quotient);
		int steps = 1 + (int) quotient;
		System.out.println("Steps: " + steps);
		double[] values = new double[steps];
		double value = from;
		for (int i = 0; i < values.length; i++) {
			values[i] = value;
			value += by;
		}
		return values;
	}
}
