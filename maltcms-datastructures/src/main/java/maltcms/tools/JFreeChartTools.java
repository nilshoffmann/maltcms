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

import java.util.List;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;

/**
 * <p>JFreeChartTools class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class JFreeChartTools {

    /**
     * <p>addXYZDataset.</p>
     *
     * @param l a {@link java.util.List} object.
     * @param result an array of double.
     * @param row a int.
     * @return a {@link java.util.List} object.
     */
    public static List<double[]> addXYZDataset(List<double[]> l,
            double[] result, int row) {
        for (int i = 0; i < result.length; i++) {
            double[] d = new double[3];
            d[0] = i;
            d[1] = row;
            d[2] = result[i];
            l.add(d);
        }
        return l;
    }

    /**
     * <p>getXYDataset.</p>
     *
     * @param d an array of double.
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.jfree.data.xy.DefaultXYDataset} object.
     */
    public static DefaultXYDataset getXYDataset(double[] d, String name) {
        DefaultXYDataset dx = new DefaultXYDataset();
        dx.addSeries(name, getXYDataSeries(d));
        return dx;
    }

    /**
     * <p>getXYDataSeries.</p>
     *
     * @param d an array of double.
     * @return an array of double.
     */
    public static double[][] getXYDataSeries(double[] d) {
        double[][] data = new double[2][d.length];
        for (int i = 0; i < d.length; i++) {
            data[0][i] = i;
            data[1][i] = d[i];
        }
        return data;
    }

    /**
     * <p>getXYZDataset.</p>
     *
     * @param l a {@link java.util.List} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.jfree.data.xy.XYZDataset} object.
     */
    public static XYZDataset getXYZDataset(List<double[]> l, String name) {
        DefaultXYZDataset d = new DefaultXYZDataset();
        double[][] data = new double[3][l.size()];
        int i = 0;
        for (double[] a : l) {
            data[0][i] = a[0];
            data[1][i] = a[1];
            data[2][i] = a[2];
            i++;
        }
        // System.out.println(Arrays.deepToString(data));
        d.addSeries(name, data);
        return d;
    }
}
