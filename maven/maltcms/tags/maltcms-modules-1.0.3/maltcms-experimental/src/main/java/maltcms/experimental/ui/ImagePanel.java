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
 * $Id: ImagePanel.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.experimental.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import maltcms.io.csv.ColorRampReader;
import maltcms.tools.ImageTools;
import ucar.ma2.ArrayDouble;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
public class ImagePanel extends JPanel {

	/**
     * 
     */
	private static final long serialVersionUID = 4300139196647706338L;

	private BufferedImage bi = null;

	public void setData(final double[][] mat, final double[] masses1,
	        final double[] masses2) {
		BufferedImage bi = new BufferedImage(mat[0].length, mat.length,
		        BufferedImage.TYPE_INT_RGB);
		System.out.println("Creating image: " + mat.length + "x"
		        + mat[0].length);
		double max = 0;
		double min = 0;
		ArrayDouble.D2 a = new ArrayDouble.D2(mat.length, mat[0].length);
		for (int i = 0; i < mat.length; i++) {
			for (int j = 0; j < mat[0].length; j++) {
				max = Math.max(max, mat[i][j]);
				min = Math.min(min, mat[i][j]);
				a.set(i, j, mat[i][j]);
			}
		}

		ColorRampReader crr = new ColorRampReader();
		int[][] colorRamp = crr.getDefaultRamp();
		int samples = 1024;
		double[] d = ImageTools.getBreakpoints(a, samples,
		        Double.NEGATIVE_INFINITY);
		ImageTools.makeImage2D(bi.getRaster(), a.transpose(1, 0), samples,
		        colorRamp, 0.0, d);
		// double minMass = Math.min(masses1[0], masses2[0]);
		// double maxMass = Math.max(masses1[0], masses2[0]);
		// Graphics2D g2 = (Graphics2D)bi.getGraphics();
		// Color bg = g2.getColor();
		// Color c = Color.red;
		// Color b = Color.black;
		// g2.setColor(b);
		// g2.fillRect(0, 0, width, height);
		// g2.setColor(c);
		// for (int i = 0; i < masses1.length; i++) {
		// for (int j = 0; j < masses2.length; j++) {
		// float f = (float)((mat[i][j]-min)/(max-min));
		// System.out.println(f);
		// g2.setColor(new Color(f,f,f));
		// g2.fillRect(j, i, 1, 1);
		// }
		// }
		// g2.setColor(bg);
		this.bi = bi;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (this.bi != null) {
			// System.out.println("Painting image");
			setPreferredSize(new Dimension(this.bi.getWidth(), this.bi
			        .getHeight()));
			g.drawImage(this.bi, 0, 0, getWidth(), getHeight(), null);
		}
	}

}
