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
package maltcms.experimental.operations;

import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import java.awt.Point;
import java.util.ArrayList;
import javax.swing.JFrame;
import lombok.Data;
import maltcms.commands.distances.dtwng.TwoFeatureVectorOperation;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.experimental.ui.ImagePanel;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;

/**
 * <p>MSPeaklistAlignment class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Data
@RequiresVariables(names = {"var.mass_values", "var.intensity_values"})
public class MSPeaklistAlignment extends TwoFeatureVectorOperation {

    @Configurable(name = "var.mass_values")
    private String mass_values = "mass_values";
    @Configurable(name = "var.intensity_values")
    private String intensity_values = "intensity_values";
    @Configurable
    private double epsilon = 0.0001;

    /**
     * <p>matchMass.</p>
     *
     * @param m1 a double.
     * @param m2 a double.
     * @param epsilon a double.
     * @return a double.
     */
    public double matchMass(double m1, double m2, double epsilon) {
        if (Math.abs(m1 - m2) <= epsilon) {
            return 0.0d;
        }
        return 1.0d;
    }

    /**
     * <p>recurse.</p>
     *
     * @param amat an array of double.
     * @param i a int.
     * @param j a int.
     * @param m1 a double.
     * @param m2 a double.
     * @param i1 a double.
     * @param i2 a double.
     * @param p an array of {@link java.awt.Point} objects.
     * @return a double.
     */
    public double recurse(double[][] amat, int i, int j, double m1, double m2,
            double i1, double i2, Point[][] p) {
        // fill gaps
        if (i == 0 && j > 0) {
            amat[i][j] = amat[i][j - 1] - 1;
            return amat[i][j];
        } else if (i > 0 && j == 0) {
            amat[i][j] = amat[i - 1][j] - 1;
            return amat[i][j];
        }
        double sc = 0;
        sc = matchMass(m1, m2, epsilon);
        double up = amat[i - 1][j] + 1;
        double left = amat[i][j - 1] + 1;
        double diag = amat[i - 1][j - 1] + sc;

        // diag
        double min = Math.min(left, Math.min(diag, up));
        if (min == diag) {
            p[i][j] = (new Point(i - 1, j - 1));
        } else if (min == up) {// up
            p[i][j] = (new Point(i - 1, j));
        } else {// left
            p[i][j] = (new Point(i, j - 1));
        }
        amat[i][j] = min;

        return amat[i][j];
    }

    /**
     * <p>traceback.</p>
     *
     * @param trace an array of {@link java.awt.Point} objects.
     * @return a {@link java.util.ArrayList} object.
     */
    public ArrayList<Point> traceback(Point[][] trace) {
        for (Point[] trace1 : trace) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < trace[0].length; j++) {
                sb.append(trace1[j].toString() + " ");
            }
            System.out.println(sb.toString());
        }
        ArrayList<Point> al = new ArrayList<>();
        Point end = trace[trace.length - 1][trace[0].length - 1];
        if (end == null) {
            System.err.println("end is null");
        }
        Point start = new Point(0, 0);
        Point s = trace[end.x][end.y];
        while (!s.equals(start)) {
            al.add(end);
            System.out.println(end);
            end = s;
            s = trace[end.x][end.y];
        }
        al.add(end);
        return al;
    }

    /**
     * <p>peakCntAlignment.</p>
     *
     * @param masses1 an array of double.
     * @param intens1 an array of double.
     * @param masses2 an array of double.
     * @param intens2 an array of double.
     * @return a double.
     */
    public double peakCntAlignment(double[] masses1, double[] intens1,
            double[] masses2, double[] intens2) {
        System.out.println("Length 1: " + masses1.length + " length 2: "
                + masses2.length);
        Point[][] trace = new Point[masses1.length + 1][masses2.length + 1];
        double[][] amat = new double[masses1.length + 1][masses2.length + 1];
        amat[0][0] = 0;
        trace[0][0] = new Point(-1, -1);
        for (int i = 1; i < amat.length; i++) {
            for (int j = 1; j < amat[0].length; j++) {
                recurse(amat, i, j, masses1[i - 1], masses2[j - 1],
                        intens1[i - 1], intens2[j - 1], trace);
            }
        }
        printMatrix(amat, masses1, masses2);
        return amat[masses1.length][masses2.length];
    }

    /**
     * <p>printMatrix.</p>
     *
     * @param mat an array of double.
     * @param masses1 an array of double.
     * @param masses2 an array of double.
     */
    public void printMatrix(final double[][] mat, final double[] masses1,
            final double[] masses2) {
        ImagePanel jp = new ImagePanel();
        jp.setData(mat, masses1, masses2);
        JFrame jf = new JFrame();
        jf.add(jp);
        jf.setVisible(true);
        jf.pack();
    }

    /** {@inheritDoc} */
    @Override
    public double apply(IFeatureVector f1, IFeatureVector f2) {
        Array m1 = f1.getFeature(this.mass_values);
        Array m2 = f2.getFeature(this.mass_values);
        Array i1 = f1.getFeature(this.intensity_values);
        Array i2 = f2.getFeature(this.intensity_values);
        return peakCntAlignment((double[]) m1.get1DJavaArray(double.class),
                (double[]) i1.get1DJavaArray(double.class), (double[]) m2
                .get1DJavaArray(double.class), (double[]) i2
                .get1DJavaArray(double.class));
    }

    /**
     * <p>apply.</p>
     *
     * @param m1 a {@link ucar.ma2.Array} object.
     * @param m2 a {@link ucar.ma2.Array} object.
     * @param i1 a {@link ucar.ma2.Array} object.
     * @param i2 a {@link ucar.ma2.Array} object.
     * @return a double.
     */
    public double apply(Array m1, Array m2, Array i1, Array i2) {
        return peakCntAlignment((double[]) m1.get1DJavaArray(double.class),
                (double[]) i1.get1DJavaArray(double.class), (double[]) m2
                .get1DJavaArray(double.class), (double[]) i2
                .get1DJavaArray(double.class));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isMinimize() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(Configuration cfg) {
        super.configure(cfg);
        this.mass_values = cfg.getString("var.mass_values");
        this.intensity_values = cfg.getString("var.intensity_values");
    }
}
