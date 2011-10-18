/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
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
 * $Id: MSPeaklistAlignment.java 159 2010-08-31 18:44:07Z nilshoffmann $
 */
package maltcms.experimental.operations;

import maltcms.commands.distances.dtwng.TwoFeatureVectorOperation;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JFrame;

import maltcms.datastructures.array.IFeatureVector;
import maltcms.experimental.ui.ImagePanel;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;

/**
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
@RequiresVariables(names = { "var.mass_values", "var.intensity_values" })
public class MSPeaklistAlignment extends TwoFeatureVectorOperation {

	@Configurable(name = "var.mass_values")
	private String mass_values = "mass_values";
	@Configurable(name = "var.intensity_values")
	private String intensity_values = "intensity_values";

	public double matchMass(double m1, double m2, double epsilon) {
		if (Math.abs(m1 - m2) <= epsilon) {
			return 0.0d;
		}
		return 1.0d;
	}

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
		sc = matchMass(m1, m2, 0.2);
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

	public ArrayList<Point> traceback(Point[][] trace) {
		for (int i = 0; i < trace.length; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < trace[0].length; j++) {
				sb.append(trace[i][j].toString() + " ");
			}
			System.out.println(sb.toString());
		}
		ArrayList<Point> al = new ArrayList<Point>();
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

	public void printMatrix(final double[][] mat, final double[] masses1,
	        final double[] masses2) {
		ImagePanel jp = new ImagePanel();
		jp.setData(mat, masses1, masses2);
		JFrame jf = new JFrame();
		jf.add(jp);
		jf.setVisible(true);
		jf.pack();
	}

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

	public double apply(Array m1, Array m2, Array i1, Array i2) {
		return peakCntAlignment((double[]) m1.get1DJavaArray(double.class),
		        (double[]) i1.get1DJavaArray(double.class), (double[]) m2
		                .get1DJavaArray(double.class), (double[]) i2
		                .get1DJavaArray(double.class));
	}

	@Override
	public boolean isMinimize() {
		return false;
	}

	@Override
	public void configure(Configuration cfg) {
		super.configure(cfg);
		this.mass_values = cfg.getString("var.mass_values");
		this.intensity_values = cfg.getString("var.intensity_values");
	}

}
