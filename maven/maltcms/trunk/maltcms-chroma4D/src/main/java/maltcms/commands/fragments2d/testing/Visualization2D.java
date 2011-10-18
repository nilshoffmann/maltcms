/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
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
 * $Id: Visualization2D.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.commands.fragments2d.testing;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.MinimumFilter;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.tools.ArrayTools;
import maltcms.tools.ArrayTools2;
import maltcms.tools.ImageTools;


import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.tuple.Tuple2D;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Slf4j
@Data
public class Visualization2D {

	private int currentrasterline = -1;

	private boolean holdHorizontalI = false;
	private boolean holdVerticalI = false;
	private boolean holdHorizontalJ = false;
	private boolean holdVerticalJ = false;
	private boolean black = true;

	private double threshold = 6.0d;
	private boolean globalmax = false;
	private boolean filter = false;
	private boolean normalize = true;
	private int binSize = 256;

	public BufferedImage createImage(List<Array> scanlinesi,
	        List<Array> scanlinesj, final List<Point> horizontal,
	        final List<Point> vertical) {

		Tuple2D<List<Array>, List<Array>> scanlines = new Tuple2D<List<Array>, List<Array>>(
		        scanlinesi, scanlinesj);

		if (horizontal != null) {
			scanlines = createNewScanlines(scanlines.getFirst(), scanlines
			        .getSecond(), horizontal, this.holdHorizontalI,
			        this.holdHorizontalJ);
		}

		if (vertical != null) {
			scanlines = createNewScanlines(ArrayTools2.transpose(scanlines
			        .getFirst()), ArrayTools2.transpose(scanlines.getSecond()),
			        vertical, this.holdVerticalI, this.holdVerticalJ);
			scanlines = new Tuple2D<List<Array>, List<Array>>(ArrayTools2
			        .transpose(scanlines.getFirst()), ArrayTools2
			        .transpose(scanlines.getSecond()));
		}

		final Tuple2D<double[], Tuple2D<double[], double[]>> sb = getSampleAndBreakpointTable(
		        scanlinesi, scanlinesj);

		return ci(scanlines.getFirst(), scanlines.getSecond(), sb.getFirst(),
		        sb.getSecond().getFirst(), sb.getSecond().getSecond());
	}

	public Tuple2D<List<Array>, List<Array>> createNewScanlines(
	        List<Array> scanlinesi, List<Array> scanlinesj,
	        List<Point> warpPath, final boolean holdi, final boolean holdj) {

		this.currentrasterline = -1;

		final List<Tuple2D<Array, Array>> outputintensities = new ArrayList<Tuple2D<Array, Array>>();
		final List<Tuple2D<Integer, Integer>> outputintensitiescounter = new ArrayList<Tuple2D<Integer, Integer>>();

		// log.info("Conserve left chromatogram axis: {}", this.holdi);
		// log.info("Conserve top chromatogram axis: {}", this.holdj);

		int oldi = -1, oldj = -1;
		for (Point p : warpPath) {
			final int scanindexi = p.x;
			final int scanindexj = p.y;

			// log.info("{} - {}", scanindexi, scanindexj);

			final Array scanlinei = scanlinesi.get(scanindexi);
			final Array scanlinej = scanlinesj.get(scanindexj);

			if (oldi != scanindexi && oldj != scanindexj) {
				// new pixel line
				outputintensities.add(new Tuple2D<Array, Array>(scanlinei,
				        scanlinej));
				outputintensitiescounter
				        .add(new Tuple2D<Integer, Integer>(1, 1));
				this.currentrasterline++;
			} else if (oldi != scanindexi && oldj == scanindexj) {
				if (holdj) {
					// adding pixelline to current sum
					int c = outputintensitiescounter
					        .get(this.currentrasterline).getFirst();
					outputintensitiescounter.get(this.currentrasterline)
					        .setFirst(++c);
					final Tuple2D<Array, Array> tmp = outputintensities
					        .get(this.currentrasterline);
					tmp.setFirst(ArrayTools.sum(tmp.getFirst(), scanlinei));
				} else {
					// adding pixelline
					outputintensities.add(new Tuple2D<Array, Array>(scanlinei,
					        scanlinej));
					outputintensitiescounter.add(new Tuple2D<Integer, Integer>(
					        1, 1));
					this.currentrasterline++;
				}
			} else if (oldi == scanindexi && oldj != scanindexj) {
				if (holdi) {
					// add pixelline to current sum
					int c = outputintensitiescounter
					        .get(this.currentrasterline).getSecond();
					outputintensitiescounter.get(this.currentrasterline)
					        .setSecond(++c);
					final Tuple2D<Array, Array> tmp = outputintensities
					        .get(this.currentrasterline);
					tmp.setSecond(ArrayTools.sum(tmp.getSecond(), scanlinej));
				} else {
					// adding pixelline
					outputintensities.add(new Tuple2D<Array, Array>(scanlinei,
					        scanlinej));
					outputintensitiescounter.add(new Tuple2D<Integer, Integer>(
					        1, 1));
					this.currentrasterline++;
				}
			} else {
				throw new RuntimeException("Warppath exception. Invalid path.");
			}
			oldi = scanindexi;
			oldj = scanindexj;
		}

		return convertToScanlines(outputintensities, outputintensitiescounter);
	}

	private Tuple2D<List<Array>, List<Array>> convertToScanlines(
	        List<Tuple2D<Array, Array>> outputintensities,
	        List<Tuple2D<Integer, Integer>> outputintensitiescounter) {

		final List<Array> scanlinesi = new ArrayList<Array>();
		final List<Array> scanlinesj = new ArrayList<Array>();

		for (int i = 0; i < outputintensities.size(); i++) {
			scanlinesi.add(ArrayTools.mult(outputintensities.get(i).getFirst(),
			        1.0d / outputintensitiescounter.get(i).getFirst()));
			scanlinesj.add(ArrayTools.mult(
			        outputintensities.get(i).getSecond(),
			        1.0d / outputintensitiescounter.get(i).getSecond()));
		}

		return new Tuple2D<List<Array>, List<Array>>(scanlinesi, scanlinesj);
	}

	protected BufferedImage ci(final List<Array> scanlinesi,
	        final List<Array> scanlinesj, final double[] samples,
	        final double[] breakpointsi, final double[] breakpointsj) {

		if (scanlinesi.size() != scanlinesj.size()) {
			System.out.println("ERROR!!! scanlines nicht gleichlang");
			return null;
		}

		final int imageheight = scanlinesi.get(0).getShape()[0];
		final int imagewidth = scanlinesi.size();
		final BufferedImage img = new BufferedImage(imagewidth, imageheight,
		        BufferedImage.TYPE_INT_RGB);
		final WritableRaster raster = img.getRaster();

		IndexIterator iter1, iter2;
		double intensityi, intensityj;
		int[] rgbvalueEqual = null;
		int c = 0;
		int rasterline = 0;

		for (int i = 0; i < scanlinesi.size(); i++) {
			iter1 = scanlinesi.get(i).getIndexIterator();
			iter2 = scanlinesj.get(i).getIndexIterator();
			c = 0;
			while (iter1.hasNext() && iter2.hasNext()) {
				intensityi = iter1.getDoubleNext();
				intensityj = iter2.getDoubleNext();
				if (this.normalize) {
					rgbvalueEqual = getRasterColor(ImageTools.getSample(
					        samples, breakpointsi, intensityi), 1.0d,
					        ImageTools.getSample(samples, breakpointsj,
					                intensityj), 1.0d);
				} else if (!this.normalize && this.binSize == 2) {
					if (intensityi > 0.0d) {
						intensityi = 1.0d;
					}
					if (intensityj > 0.0d) {
						intensityj = 1.0d;
					}
					rgbvalueEqual = getRasterColor(intensityi, 1.0d,
					        intensityj, 1.0d);
				} else {
					rgbvalueEqual = getRasterColor(ImageTools.getSample(
					        samples, breakpointsi, intensityi), 1.0d,
					        ImageTools.getSample(samples, breakpointsj,
					                intensityj), 1.0d);
				}

				if ((rasterline < imagewidth)
				        && ((imageheight - c - 1) < imageheight)
				        && (imageheight - c - 1) >= 0) {
					raster.setPixel(rasterline, imageheight - c - 1,
					        rgbvalueEqual);
				}

				c++;
			}
			rasterline++;
		}

		return img;
	}

	/**
	 * Will create an array int[3] containing the rgb values for the raster.
	 * 
	 * @param ci
	 *            current intensity of the first series
	 * @param maxci
	 *            maximum intensity of the first series
	 * @param cj
	 *            current intensity of the second series
	 * @param maxcj
	 *            maximum intensity of the second series
	 * @return rgb color array
	 */
	protected int[] getRasterColor(final double ci, final double maxci,
	        final double cj, final double maxcj) {
		final int vi = (int) (ci * 255.0d / maxci);
		final int vj = (int) (cj * 255.0d / maxcj);

		if (this.black) {
			return new int[] { vi, vj, 0 };
		} else {
			return new int[] { 255 - vj, 255 - vi, 255 - Math.max(vi, vj) };
		}
	}

	/**
	 * Creates the samples table and the breakpoint table for reference and
	 * query arrays.
	 * 
	 * @param scanlinesi
	 *            scanlines of the reference
	 * @param scanlinesj
	 *            scanlines of the query
	 * @return {smaple table,{breakpoints i, breakpoints j}}
	 */
	protected Tuple2D<double[], Tuple2D<double[], double[]>> getSampleAndBreakpointTable(
	        final List<Array> scanlinesi, final List<Array> scanlinesj) {
		final double[] samples = ImageTools.createSampleTable(this.binSize);
		final Array scanlinesiC = cross.datastructures.tools.ArrayTools.glue(scanlinesi);
		final Array scanlinesjC = cross.datastructures.tools.ArrayTools.glue(scanlinesj);

		final ArrayStatsScanner ass = new ArrayStatsScanner();
		final StatsMap[] sm = ass
		        .apply(new Array[] { scanlinesiC, scanlinesjC });
		final Double meani = sm[0].get(Vars.Mean.toString());
		final Double meanj = sm[1].get(Vars.Mean.toString());

		double thresholdi, thresholdj;
		if (this.globalmax) {
			thresholdi = ((meani + meanj) / 2.0d) / this.threshold;
			thresholdj = thresholdi;
		} else {
			thresholdi = meani / this.threshold;
			thresholdj = meanj / this.threshold;
		}

		log.info("Using thresholdi: {}", thresholdi);
		log.info("Using thresholdj: {}", thresholdj);
		if (thresholdi != 0) {
			if (this.filter) {
				final AArrayFilter minFilteri = new MinimumFilter(thresholdi);
				minFilteri.apply(new Array[] { scanlinesiC });
				final AArrayFilter minFilterj = new MinimumFilter(thresholdj);
				minFilterj.apply(new Array[] { scanlinesjC });
			} else {
				log.info("Filtering was turned off");
			}
		} else {
			log.info("Skipping threshold minimization.");
		}

		final double[] breakpointsi = ImageTools.getBreakpoints(scanlinesiC,
		        this.binSize, Double.NEGATIVE_INFINITY);
		final double[] breakpointsj = ImageTools.getBreakpoints(scanlinesjC,
		        this.binSize, Double.NEGATIVE_INFINITY);

		return new Tuple2D<double[], Tuple2D<double[], double[]>>(samples,
		        new Tuple2D<double[], double[]>(breakpointsi, breakpointsj));
	}

}
