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
 * The effective point number for smoothing is 1+(window*2). So effectively
 * +/-window points around the current point.
 *
 * @author Nils Hoffmann
 * 
 */
@Data
@ServiceProvider(service = AArrayFilter.class)
public class SavitzkyGolayFilter extends AArrayFilter {

    @Configurable
    private int window = 5;
    @Configurable
    private int polynomialDegree = 2;

    /**
     * <p>Constructor for SavitzkyGolayFilter.</p>
     */
    public SavitzkyGolayFilter() {
        super();
    }

    /**
     * <p>Constructor for SavitzkyGolayFilter.</p>
     *
     * @param window a int.
     * @since 1.3.2
     */
    public SavitzkyGolayFilter(int window) {
        this();
        this.window = window;
    }

    /** {@inheritDoc} */
    @Override
    public Array apply(final Array a) {
        switch (polynomialDegree) {
            case 2:
                return applyQuadraticCubic(a);
            case 4:
                return applyQuarticQuintic(a);
            default:
                throw new IllegalArgumentException("polynomialDegree must be one of 2 or 4, for quadratic/cubic interpolation, or for quartic/quintic interpolation. Was: " + polynomialDegree);
        }
    }

    private Array applyFilter(final Array a, int index, int pointNumber, int[][] coefficients, int[] norms) {
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
        return Array.makeFromJavaArray(filtered);
    }

    private Array applyQuadraticCubic(final Array a) {
        int pointNumber = 1 + window * 2;
        int index = Arrays.binarySearch(quadCubicPoints, pointNumber);
        if (index < 0) {
            throw new IllegalArgumentException("No coefficients for quadratic / cubic filter of width 2x" + window + "+1=" + pointNumber + ". Minimum width is " + quadCubicPoints[0]);
        }
        return applyFilter(a, index, pointNumber, quadCubicCoefficients, quadCubicNorms);
    }

    private Array applyQuarticQuintic(final Array a) {
        int pointNumber = 1 + window * 2;
        int index = Arrays.binarySearch(quarticQuinticPoints, pointNumber);
        if (index < 0) {
            throw new IllegalArgumentException("No coefficients for quartic / quintic filter of width 2x" + window + "+1=" + pointNumber + ". Minimum width is " + quarticQuinticPoints[0]);
        }
        return applyFilter(a, index, pointNumber, quarticQuinticCoefficients, quarticQuinticNorms);
    }

    /** {@inheritDoc} */
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
     * @param d the data array
     * @return an array of double.
     */
    public static double[] convolve(final double[] filterCoeffs, final double[] d) {
        double[] ret = new double[d.length];
        double[] filterArray = new double[d.length + (filterCoeffs.length - 1)];
        //log.info("Length of filter array: "+filterArray.length);
        int offset = (filterCoeffs.length - 1) / 2;
        //initialize boundaries by padding
        for (int i = 0; i < offset; i++) {
            filterArray[i] = d[0];
            filterArray[i + d.length] = d[d.length - 1];
        }
        System.arraycopy(d, 0, filterArray, offset, d.length);
        for (int i = offset; i < filterArray.length - offset; i++) {
            double sum = 0.0d;
            //log.info("Convolution from: "+(i-offset)+" to "+(i+offset));
            for (int j = 0; j < filterCoeffs.length; j++) {
                sum += filterCoeffs[j] * filterArray[(i - offset) + j];
            }
            ret[i - offset] = sum;
        }
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
//        this.window = cfg.getInt(this.getClass().getName() + ".window", 10);
    }
    /** Constant <code>quadCubicCoefficients={
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
    }</code> */
    public static final int[][] quadCubicCoefficients = {
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
    /** Constant <code>quarticQuinticCoefficients={
        {131, 75, -30, 5},
        {179, 135, 30, -55, 15},
        {143, 120, 60, -10, -45, 18},
        {677, 600, 390, 110, -160, -198, 110},
        {11063, 10125, 7500, 3755, -165, -2937, -2860, 2145},
        {883, 825, 660, 415, 135, -117, -260, -195, 195},
        {1393, 1320, 1110, 790, 405, 18, -290, -420, -255, 340},
        {44003, 42120, 36660, 28190, 17655, 6375, -3940, -11220, -13005, -6460, 11628},
        {1011, 975, 870, 705, 495, 261, 30, -165, -285, -285, -114, 285},
        {4253, 4125, 3750, 3155, 2385, 1503, 590, -255, -915, -1255, -1122, -345, 1265}
    }</code> */
    public static final int[][] quarticQuinticCoefficients = {
        {131, 75, -30, 5},
        {179, 135, 30, -55, 15},
        {143, 120, 60, -10, -45, 18},
        {677, 600, 390, 110, -160, -198, 110},
        {11063, 10125, 7500, 3755, -165, -2937, -2860, 2145},
        {883, 825, 660, 415, 135, -117, -260, -195, 195},
        {1393, 1320, 1110, 790, 405, 18, -290, -420, -255, 340},
        {44003, 42120, 36660, 28190, 17655, 6375, -3940, -11220, -13005, -6460, 11628},
        {1011, 975, 870, 705, 495, 261, 30, -165, -285, -285, -114, 285},
        {4253, 4125, 3750, 3155, 2385, 1503, 590, -255, -915, -1255, -1122, -345, 1265}
    };
    /** Constant <code>quadCubicPoints={5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25}</code> */
    public static final int[] quadCubicPoints = {5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25};
    /** Constant <code>quarticQuinticPoints={7, 9, 11, 13, 15, 17, 19, 21, 23, 25}</code> */
    public static final int[] quarticQuinticPoints = {7, 9, 11, 13, 15, 17, 19, 21, 23, 25};
    /** Constant <code>quadCubicNorms={
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
    }</code> */
    public static final int[] quadCubicNorms = {
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
    /** Constant <code>quarticQuinticNorms={
        231,
        429,
        429,
        2431,
        46189,
        4199,
        7429,
        260015,
        6555,
        30015
    }</code> */
    public static final int[] quarticQuinticNorms = {
        231,
        429,
        429,
        2431,
        46189,
        4199,
        7429,
        260015,
        6555,
        30015
    };
}
