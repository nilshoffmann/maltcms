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
package maltcms.commands.filters.array;

import cross.annotations.Configurable;
import java.util.Arrays;
import lombok.Data;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;

/**
 * Implementation of the Savitzky-Golay Filter. The current implementation will
 * only work for the values tabulated in the paper. Reference: Savitzky,A. and
 * Golay,M.J.E. (1964) Smoothing and Differentiation of Data by Simplified Least
 * Squares Procedures. Analytical Chemistry, 36, 1627–1639.
 *
 * @author Nils Hoffmann
 *
 *
 */
@Data
@ServiceProvider(service = AArrayFilter.class)
public class SavitzkyGolayFilter extends AArrayFilter {

	@Configurable
	private int window = 5;

	public SavitzkyGolayFilter() {
		super();
	}

	public SavitzkyGolayFilter(int window) {
		this();
		this.window = window;
	}

	@Override
	public Array apply(final Array a) {
		int pointNumber = 1 + window * 2;
		int index = Arrays.binarySearch(points, pointNumber);
		if (index < 0) {
			throw new IllegalArgumentException("No coefficients for filter of width 2x" + window + "+1=" + pointNumber);
		}
		//prepare filter coefficients
		double[] filterCoeffs = new double[pointNumber];
		for (int i = 0; i < window; i++) {
			filterCoeffs[window + i] = coefficients[index][i];
			filterCoeffs[window - i] = coefficients[index][i];
		}
		//convolve signal with coefficients
		double[] filtered = convolve(filterCoeffs, (double[]) a.get1DJavaArray(double.class));
		//normalize
		for (int j = 0; j < filtered.length; j++) {
			filtered[j] /= (double) norms[index];
		}
		return Array.factory(filtered);

	}

	@Override
	public SavitzkyGolayFilter copy() {
		return new SavitzkyGolayFilter(window);
	}

	/**
	 *
	 * Boundaries are padded internally according to n with values d[0] and
	 * d[d.length-1]. Resulting, filtered array is still of length d.length.
	 *
	 * @param filterCoeffs an array of size 2n+1 of filter coefficients
	 * @param d            the data array
	 * @return
	 */
	public static double[] convolve(final double[] filterCoeffs, final double[] d) {
		double[] ret = new double[d.length];
		double[] filterArray = new double[d.length + (filterCoeffs.length - 1)];
		//System.out.println("Length of filter array: "+filterArray.length);
		int offset = (filterCoeffs.length - 1) / 2;
		//initialize boundaries by padding
		for (int i = 0; i < offset; i++) {
			filterArray[i] = d[0];
			filterArray[i + d.length] = d[d.length - 1];
		}
		for (int i = 0; i < d.length; i++) {
			filterArray[i + offset] = d[i];
		}
		for (int i = offset; i < filterArray.length - offset; i++) {
			double sum = 0.0d;
			//System.out.println("Convolution from: "+(i-offset)+" to "+(i+offset));
			for (int j = 0; j < filterCoeffs.length; j++) {

				sum += filterCoeffs[j] * filterArray[(i - offset) + j];
			}
			ret[i - offset] = sum;
		}
		return ret;
	}

	@Override
	public void configure(final Configuration cfg) {
		super.configure(cfg);
		this.window = cfg.getInt(this.getClass().getName() + ".window", 10);
	}
	public static final int[][] coefficients = {
		{17, 12, -3},
		{7, 6, 3, -2},
		{59, 54, 39, 14, -21},
		{89, 84, 69, 44, 9, -36},
		{25, 24, 21, 16, 9, 0, -11},
		{167, 162, 147, 122, 87, 42, -13, -78},
		{43, 42, 39, 34, 27, 18, 7, -6, -21},
		{269, 264, 249, 224, 189, 144, 89, 24, -51, -136},
		{329, 324, 309, 284, 249, 204, 149, 84, 9, -76, -171},
		{79, 78, 75, 70, 63, 54, 43, 30, 15, -2, -21, -42},
		{467, 462, 447, 422, 387, 343, 287, 222, 147, 62, -33, -138, -253}
	};
	public static final int[] points = {5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25};
	public static final int[] norms = {
		35,
		21,
		231,
		429,
		143,
		1105,
		323,
		2261,
		3059,
		8059,
		5175
	};
}
