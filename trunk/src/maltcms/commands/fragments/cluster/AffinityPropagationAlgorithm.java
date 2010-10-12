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
 * $Id$
 */

package maltcms.commands.fragments.cluster;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageIO;

import cross.tools.FileTools;
import cross.tools.MathTools;

/**
 * Implementation of Affinity Propagation, see ... for details. FIXME add
 * citation
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class AffinityPropagationAlgorithm extends ClusteringAlgorithm implements
        Runnable {

	private final double[][] similarities, availabilities, responsibilities;

	private String[] labels;

	private boolean similarity = false;

	private boolean logValues = false;

	public AffinityPropagationAlgorithm(final double[][] similarities1,
	        final boolean similarity1, final boolean logValues1) {
		Locale.setDefault(Locale.US);
		this.similarities = similarities1;
		final int sx = this.similarities.length;
		final int sy = this.similarities[0].length;
		this.logValues = logValues1;
		this.similarity = similarity1;
		System.out.println("AffinityPropagation: Shapes: sx = " + sx + " sy = "
		        + sy);
		this.availabilities = new double[sx][sy];
		this.responsibilities = new double[sx][sy];
		this.labels = new String[sx];
		for (int i = 0; i < sx; i++) {
			this.labels[i] = "" + i;
		}
	}

	public AffinityPropagationAlgorithm(final double[][] similarities1,
	        final boolean similarity1, final boolean log1,
	        final String[] labels1) {
		this(similarities1, similarity1, log1);
		this.labels = labels1;
	}

	public BufferedImage createImage(final double[][] responsibilities1) {
		final BufferedImage bia = new BufferedImage(responsibilities1.length,
		        responsibilities1[0].length, BufferedImage.TYPE_INT_RGB);
		double amin = Double.MAX_VALUE;
		double amax = Double.MIN_VALUE;
		for (int i = 0; i < responsibilities1.length; i++) {
			for (int j = 0; j < responsibilities1[0].length; j++) {
				amin = Math.min(responsibilities1[i][j], amin);
				amax = Math.max(responsibilities1[i][j], amax);
			}
		}
		final WritableRaster wr = bia.getRaster();
		for (int i = 0; i < responsibilities1.length; i++) {
			for (int j = 0; j < responsibilities1[0].length; j++) {
				// int v = (int) Math
				// .rint(((responsibilities[i][j] - (amax - amin) / 2.0f) /
				// (amax - amin)) * 256.0f);
				final int v = (int) Math
				        .rint(((responsibilities1[i][j]) / (amax - amin)) * 256.0f);
				System.out.println();
				wr.setPixel(i, j, new int[] { v, v, v });
			}
		}
		bia.setData(wr);
		return bia;
	}

	@Override
	public double[] dmat(final int i, final int j, final int k) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void findBestD(final int l) {
		// TODO Auto-generated method stub

	}

	private void findExemplars(final double[][] availabilities2,
	        final double[][] responsibilities2) {
		for (int i = 0; i < availabilities2.length; i++) {
			int maxk = i;
			double maxv = availabilities2[i][i] + responsibilities2[i][i];
			// System.out.println("a(i,i)="+availabilities2[i][i]);
			// System.out.println("r(i,i)="+responsibilities2[i][i]);
			// System.out.println("maxv="+maxv);
			for (int k = 0; k < availabilities2[0].length; k++) {
				if (i != k) {
					// System.out.println("a(i,i)="+availabilities2[i][i]);
					// System.out.println("r(i,i)="+responsibilities2[i][i]);
					final double f = availabilities2[i][k]
					        + responsibilities2[i][k];
					if (f > maxv) {
						maxv = f;
						maxk = k;
					}
					// System.out.println("maxv="+maxv);
				}
			}
			System.out.println("Best exemplar for point " + i + "("
			        + this.labels[i] + ") is point " + maxk + "("
			        + this.labels[maxk] + ") with value " + maxv);
		}

	}

	@Override
	public void joinIJtoK(final int i, final int j, final int k,
	        final double[] dist) {
		// TODO Auto-generated method stub

	}

	public void preprocessSimilarities(final double[][] s,
	        final boolean similarity1, final boolean log1) {
		final int sx = s.length;
		final int sy = s[0].length;
		// Calculate median
		final double median = MathTools.median(s);
		System.out.println("Median = " + median);
		// Use log values
		if (log1) {
			System.out.println("Using log values");
			for (int i = 0; i < sx; i++) {
				for (int j = 0; j < sy; j++) {
					s[i][j] = Math.log(s[i][j]);
				}
			}
		}
		// Set values negative for distance, leave positive for similarities
		for (int i = 0; i < sx; i++) {
			for (int j = 0; j < sy; j++) {
				if (i != j) {
					if (similarity1) {
						s[i][j] = s[i][j];
					} else {
						s[i][j] = -s[i][j];
					}
				} else {
					// Use median on diagonal elements as preference
					if (similarity1) {
						s[i][j] = median;
					} else {
						s[i][j] = -median;
					}
				}
			}
		}
		System.out
		        .println(printMatrix(s, this.labels, "Corrected Similarities"));
		saveToFiles(s);
	}

	public String printMatrix(final double[][] matrix, final String[] labels1,
	        final String title) {
		final StringBuilder sb = new StringBuilder();
		sb.append(title + "\n");
		sb.append("\t");
		for (int j = 0; j < labels1.length; j++) {
			sb.append(String.format("%7s", labels1[j]) + "\t");
		}
		sb.append("\n");
		for (int i = 0; i < matrix.length; i++) {
			sb.append(labels1[i] + "\t");
			for (int j = 0; j < matrix.length; j++) {
				sb.append(String.format("% 7f", matrix[i][j]) + "\t");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public void run() {
		preprocessSimilarities(this.similarities, this.similarity,
		        this.logValues);
		for (int i = 0; i < 100; i++) {
			updateResponsibilities(this.similarities, this.availabilities,
			        this.responsibilities);
			updateAvailabilities(this.similarities, this.availabilities,
			        this.responsibilities);
			findExemplars(this.availabilities, this.responsibilities);
		}
		System.out.println(printMatrix(this.availabilities, this.labels,
		        "Availabilities"));
		System.out.println(printMatrix(this.responsibilities, this.labels,
		        "Responsibilities"));
		final BufferedImage bia = createImage(this.availabilities);
		try {
			ImageIO.write(bia, "PNG", new File("availabilities.png"));
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final BufferedImage bir = createImage(this.responsibilities);
		try {
			ImageIO.write(bir, "PNG", new File("responsibilities.png"));
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void saveToFiles(final double[][] similarities1) {
		final File sims = FileTools.prepareOutput(getIWorkflow()
		        .getOutputDirectory(this).getAbsolutePath(), "Similarities",
		        "txt");
		final File prefs = FileTools.prepareOutput(getIWorkflow()
		        .getOutputDirectory(this).getAbsolutePath(), "Preferences",
		        "txt");
		try {
			final BufferedWriter simsw = new BufferedWriter(
			        new FileWriter(sims));
			final BufferedWriter prefsw = new BufferedWriter(new FileWriter(
			        prefs));
			for (int i = 0; i < similarities1.length; i++) {
				for (int j = 0; j < similarities1[i].length; j++) {
					// if(j>i){
					if (i != j) {
						simsw.write(Integer.toString(i + 1) + "\t"
						        + Integer.toString(j + 1) + "\t"
						        + Double.toString(similarities1[i][j]));
						simsw.newLine();
					}
					if (i == j) {
						prefsw.write(Double.toString(similarities1[i][j]));
						prefsw.newLine();
					}
				}
			}
			simsw.flush();
			simsw.close();
			prefsw.flush();
			prefsw.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateAvailabilities(final double[][] s, final double[][] a,
	        final double[][] r) {
		for (int i = 0; i < s.length; i++) {// process all points
			double sum = 0;
			for (int k = 0; k < s[0].length; k++) {// process all points
				for (int l = 0; l < s.length; l++) {// inner sum for
					// l!=i and l!=k
					if ((l != i) && (l != k)) {
						sum += Math.max(0, r[l][k]);
					}
				}
				double tmp = 0;
				if (i != k) {
					tmp = Math.min(0, r[k][k] + sum);
				} else {
					tmp = sum;
				}
				final double aik = (0.5f * a[i][k]) + (1.0f - 0.5f) * tmp;
				a[i][k] = aik;
			}
		}
	}

	public void updateResponsibilities(final double[][] s, final double[][] a,
	        final double[][] r) {
		for (int i = 0; i < s.length; i++) {
			final double[] imax = new double[s.length];
			for (int k = 0; k < s[0].length; k++) {
				if (i != k) {
					imax[i] = Math.max(imax[i], a[i][k] + s[i][k]);
				} else {
					imax[i] = Math.max(imax[i], s[i][k]);
				}
			}
			for (int k = 0; k < s[0].length; k++) {
				// float tmp = s[i][k];
				final double rv = r[i][k];
				final double sv = s[i][k];
				final double rik = (0.5f * rv) + (1.0f - 0.5f) * (sv - imax[i]);
				r[i][k] = rik;
			}
		}
	}

}
