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

import java.util.List;

import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
public class JFreeChartTools {

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

    public static DefaultXYDataset getXYDataset(double[] d, String name) {
        DefaultXYDataset dx = new DefaultXYDataset();
        dx.addSeries(name, getXYDataSeries(d));
        return dx;
    }

    public static double[][] getXYDataSeries(double[] d) {
        double[][] data = new double[2][d.length];
        for (int i = 0; i < d.length; i++) {
            data[0][i] = i;
            data[1][i] = d[i];
        }
        return data;
    }

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
