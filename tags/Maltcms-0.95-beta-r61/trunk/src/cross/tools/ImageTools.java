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

package cross.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.TransposeDescriptor;

import maltcms.datastructures.array.Sparse;
import maltcms.datastructures.ms.IAnchor;
import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYChart;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.plot.XYPlot;
import org.slf4j.Logger;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import cross.Factory;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.io.csv.ColorRampReader;

/**
 * Utility class concerned with creation and saving of images.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class ImageTools {
	private static Logger log = Logging.getLogger(ImageTools.class);

	public static double[] createSampleTable(final int nsamples) {
		final double[] samples = new double[nsamples];
		ImageTools.log.debug("Creating sample table with size {}", nsamples);
		for (int i = 0; i < samples.length; i++) {
			samples[i] = ((double) i) / ((double) nsamples);
		}
		return samples;
	}

	public static File[] drawEICs(final Class<?> creator,
	        final TupleND<IFileFragment> t, final String satVar,
	        final IFileFragment ref, final double[] eics, final double binsize,
	        final String filePrefix, final Date date) {
		if (t.isEmpty()) {
			return new File[] {};
		}
		final String[] labels = new String[t.size()];
		final File[] returnFiles = new File[2 * eics.length];
		int retFileCnt = 0;
		int j = 0;
		// List<IFileFragment> workFrag = new
		// ArrayList<IFileFragment>(t.size());
		final List<List<IAnchor>> allLabels = new ArrayList<List<IAnchor>>();
		int nanchors = 0;
		try {
			for (final IFileFragment iff : t) {
				labels[j] = t.get(j).getName();
				j++;
				// IFileFragment workFileFrag =
				// FileFragmentFactory.getInstance().create
				// (FileTools.prependDefaultDirs
				// (ImageTools.class).getAbsolutePath(), iff.getName(),
				// Arrays.asList(new IFileFragment[]{iff}));
				for (int i = 0; i < eics.length; i++) {
					final IVariableFragment ivf = new VariableFragment(iff,
					        filePrefix + "_eic_" + eics[i]);
					ImageTools.log.debug("Adding eic var {}", ivf.getVarname());
					ImageTools.log.debug("to {}", iff.toString());
					final Array eic = MaltcmsTools.getEIC(iff, eics[i], eics[i]
					        + binsize, true, false);
					ImageTools.log.debug("{}", eic);
					EvalTools.notNull(eic, ImageTools.class);
					ivf.setArray(eic);
				}
				final List<IAnchor> l = MaltcmsTools.prepareAnchors(iff);
				if (l != null) {
					nanchors += l.size();
					allLabels.add(l);
				}
				// workFrag.add(workFileFrag);
			}
			// for each eic
			for (int i = 0; i < eics.length; i++) {
				// prepare arrays and domain arrays according to number of
				// FileFragments
				final Array[] arrays = new Array[t.size()];
				final Array[] domains = new Array[t.size()];
				// create label positions array for total number of anchors
				final ArrayDouble.D1 labelPos = new ArrayDouble.D1(nanchors);
				final ArrayDouble.D1 labelVal = new ArrayDouble.D1(nanchors);
				// create array for names of anchors
				final String[] labelNames = new String[nanchors];
				int offset = 0;
				// for each FileFragment
				for (j = 0; j < t.size(); j++) {
					ImageTools.log
					        .debug("Trying to read {} from {}", filePrefix
					                + "_eic_" + eics[i], t.get(j).toString());
					arrays[j] = t.get(j).getChild(
					        filePrefix + "_eic_" + eics[i]).getArray();
					ImageTools.log.debug("{}", arrays[j]);
					domains[j] = t.get(j).getChild(satVar).getArray();
					// get the corresponding anchors
					final List<IAnchor> l = allLabels.get(j);
					// get the corresponding scan acquisition time array
					final Array sat = domains[j];
					// get the index to access elements in array
					final Index satI = sat.getIndex();
					final Index valI = arrays[j].getIndex();
					// global anchor counter
					int cnt = 0;
					// for each anchor in anchor list for FileFragment k
					for (final IAnchor ia : l) {
						// set label position at position offset+cnt
						// to value of scan_acquisition_time at the given scan
						// index
						labelPos.set(offset + cnt, sat.getDouble(satI.set(ia
						        .getScanIndex())));
						labelVal.set(offset + cnt, arrays[j].getDouble(valI
						        .set(ia.getScanIndex())));
						// set name
						labelNames[offset + cnt] = ia.getName();
						// update counter
						cnt++;
					}
					offset += l.size();

				}
				final AChart<XYPlot> xyc = new XYChart("Plot of EIC "
				        + filePrefix + " " + eics[i], labels, arrays, domains,
				        labelPos, labelVal, labelNames, satVar + " [s]",
				        "Intensity");
				final PlotRunner pr = new PlotRunner(xyc.create(),
				        "Plot of EIC " + eics[i], filePrefix + "_eic_"
				                + eics[i] + ".png", date);
				pr.configure(Factory.getInstance().getConfiguration());
				final File f = pr.getFile();
				returnFiles[retFileCnt++] = f;
				Factory.getInstance().submitJob(pr);
				returnFiles[retFileCnt++] = ImageTools.drawTICS(creator,
				        new TupleND<IFileFragment>(t), filePrefix + "_eic_"
				                + eics[i], satVar, ref, filePrefix + "_eic_"
				                + eics[i] + ".png", date);
			}
		} catch (ResourceNotAvailableException rnae) {
			log.warn("Could not load resource: {}", rnae);
			return new File[] {};
		}
		return returnFiles;
	}

	public static File[] drawEICs(final Class<?> creator,
	        final TupleND<IFileFragment> t, final String satVar,
	        final IFileFragment ref, final String filePrefix, final Date date) {
		if (t.isEmpty()) {
			return new File[] {};
		}
		final List<?> eics = Factory.getInstance().getConfiguration().getList(
		        ImageTools.class.getName() + ".drawEICs",
		        Arrays.asList(new String[] { "73.0", "150.0", "300.0" }));
		final List<String> l = StringTools.toStringList(eics);
		final double[] d = new double[l.size()];
		for (int i = 0; i < l.size(); i++) {
			d[i] = Double.parseDouble(l.get(i));
		}
		final double binsize = Factory.getInstance().getConfiguration()
		        .getDouble(ImageTools.class.getName() + ".eicBinSize", 1.0d);
		return ImageTools.drawEICs(creator, t, satVar, ref, d, binsize,
		        filePrefix, date);
	}

	private static int getMaxStringLengthForTuple(
	        Collection<Tuple2D<String, Array>> c, Font f) {
		// determine maximum allowed string length
		int maxStringLength = 0;
		final BufferedImage gtest = new BufferedImage(1, 1,
		        BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2d = (Graphics2D) gtest.getGraphics();
		g2d.setFont(f);
		for (Tuple2D<String, Array> t : c) {
			final int swidth = g2d.getFontMetrics().stringWidth(t.getFirst());
			maxStringLength = Math.max(maxStringLength, swidth);
		}
		return maxStringLength;
	}

	private static int getMaxStringLength(Collection<Integer> heightPerRow,
	        Collection<String> c, String fontFamily, int fontsize) {
		// determine maximum allowed string length
		Font f = new Font(fontFamily == null ? "Lucida Sans" : fontFamily,
		        Font.PLAIN, fontsize);
		int maxStringLength = 0;
		final BufferedImage gtest = new BufferedImage(1, 1,
		        BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2d = (Graphics2D) gtest.getGraphics();
		g2d.setFont(f);
		for (String s : c) {
			final int swidth = g2d.getFontMetrics().stringWidth(s);
			maxStringLength = Math.max(maxStringLength, swidth);
		}
		return maxStringLength;
	}

	public static BufferedImage drawSquareMatrixWithLabels(
	        List<Integer> heightPerRow, List<String> labels, Array a,
	        double skipvalue) {
		String fontFamily = "Lucida Sans";
		int fontsize = 10;// Collections.min(heightPerRow).intValue() * 2 / 3;
		final int maxStringLength = getMaxStringLength(heightPerRow, labels,
		        fontFamily, fontsize);
		final int legendWidth = (int) (Math.ceil(a.getShape()[0] / 0.05));
		final Font f = new Font(fontFamily, Font.PLAIN, fontsize);
		// add legendWidth, draw legend
		BufferedImage fullImage = new BufferedImage(a.getShape()[0], a
		        .getShape()[1], BufferedImage.TYPE_INT_RGB);
		final ColorRampReader crr = new ColorRampReader();
		final int[][] colorRamp = crr.readColorRamp(Factory.getInstance()
		        .getConfiguration().getString("images.colorramp",
		                "res/colorRamps/bw.csv"));
		final double[] breakpoints = ImageTools.getBreakpoints(a, 1024,
		        skipvalue);
		makeImage2D(fullImage.getSubimage(0, 0, a.getShape()[0],
		        a.getShape()[1]).getRaster(), a, 1024, colorRamp, 0.0,
		        breakpoints);
		int prevHeight = 0;
		for (int i = 0; i < heightPerRow.size(); i++) {
			final BufferedImage label = fullImage.getSubimage(prevHeight,
			        prevHeight, heightPerRow.get(i), heightPerRow.get(i));
			prevHeight += heightPerRow.get(i);
			final Graphics2D g = label.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, label.getWidth(), label.getHeight());
			g.setColor(Color.BLACK);
			g.setFont(f);
			final TextLayout tl = new TextLayout(labels.get(i), f, g
			        .getFontRenderContext());
			tl.draw(g, heightPerRow.get(i)
			        - (int) Math.ceil(tl.getBounds().getWidth() / 2),
			        heightPerRow.get(i) - tl.getAscent());
		}
		return fullImage;
	}

	private static File drawTIC(final Class<?> creator, final int heightPerTIC,
	        final int maxLength, final ArrayList<Tuple2D<String, Array>> a,
	        final String filename, final Array scan_acquisition_time,
	        final HashMap<String, Array> anchors,
	        final HashMap<String, List<String>> anchorNames, final Date date) {
		// string length per row
		final int fontsize = heightPerTIC * 2 / 3;
		Font f = new Font("Lucida Sans", Font.PLAIN, fontsize);
		int maxStringLength = getMaxStringLengthForTuple(a, f);
		final Font f2 = new Font("Lucida Sans", Font.PLAIN, heightPerTIC / 3);
		// create BufferedImage for plot, add 2*heightPerTIC for labels
		final BufferedImage bi = new BufferedImage(maxLength + maxStringLength,
		        (heightPerTIC * a.size()) + (2 * heightPerTIC),
		        BufferedImage.TYPE_INT_RGB);
		final ColorRampReader crr = new ColorRampReader();
		final int[][] colorRamp = crr.readColorRamp(Factory.getInstance()
		        .getConfiguration().getString("images.colorramp",
		                "res/colorRamps/bw.csv"));
		// Sort Arrays according to lexicographical order of names
		Collections.sort(a, new Comparator<Tuple2D<String, Array>>() {

			@Override
			public int compare(final Tuple2D<String, Array> o1,
			        final Tuple2D<String, Array> o2) {
				return o1.getFirst().compareTo(o2.getFirst());
			}
		});

		int nelements = 0;
		for (final Tuple2D<String, Array> t : a) {
			nelements += t.getSecond().getShape()[0];
		}
		final ArrayDouble.D1 arr = new ArrayDouble.D1(nelements);
		int start = 0;
		for (final Tuple2D<String, Array> t : a) {
			final IndexIterator ii = t.getSecond().getIndexIterator();
			while (ii.hasNext()) {
				arr.set(start++, ii.getDoubleNext());
			}
		}
		EvalTools.eqI(start, nelements, ImageTools.class);

		final double[] breakpoints = ImageTools.getBreakpoints(arr, 1024,
		        Double.POSITIVE_INFINITY);

		// for each array, draw a line of height heightPerTIC
		for (int i = 0; i < a.size(); i++) {
			final String name = a.get(i).getFirst();
			final BufferedImage tic = bi.getSubimage(maxStringLength, i
			        * heightPerTIC, maxLength, heightPerTIC);
			final ArrayList<Array> al = new ArrayList<Array>(a.get(i)
			        .getSecond().getShape()[0]);
			final ucar.ma2.Index idx = a.get(i).getSecond().getIndex();
			for (int j = 0; j < a.get(i).getSecond().getShape()[0]; j++) {
				final ArrayInt.D1 b = new ArrayInt.D1(heightPerTIC);
				ArrayTools.fill(b, a.get(i).getSecond().getDouble(idx.set(j)));
				al.add(b);
			}
			ImageTools.makeImage(tic.getRaster(), al, 1024, colorRamp, 0.0d,
			        breakpoints);
			final Color c = tic.getGraphics().getColor();
			String aname = name;
			if (name.contains(" *")) {
				aname = name.substring(0, name.indexOf(" *"));
			}
			// add anchor annotations
			if (anchors.containsKey(aname)) {
				int j = 0;
				final List<String> anchorNamesList = anchorNames.get(aname);
				final IndexIterator iter = anchors.get(aname)
				        .getIndexIterator();
				tic.getGraphics().setColor(Color.WHITE);
				while (iter.hasNext()) {
					final int scan = iter.getIntNext();
					final int width = heightPerTIC / 4;
					final int height = width;
					tic.getGraphics().drawOval(scan - (width / 2),
					        (heightPerTIC / 2) - (width / 2), width, height);
					final TextLayout tl = new TextLayout(anchorNamesList
					        .get(j++), f2, ((Graphics2D) tic.getGraphics())
					        .getFontRenderContext());
					tl.draw((Graphics2D) tic.getGraphics(), scan
					        - (float) (tl.getBounds().getWidth() / 2), tl
					        .getAscent());
				}
			} else {
				ImageTools.log.warn("No anchor annotation present for {}",
				        aname);
			}
			tic.getGraphics().setColor(c);
			final BufferedImage label = bi.getSubimage(0, i * heightPerTIC,
			        maxStringLength, heightPerTIC);
			final Graphics2D g = label.createGraphics();
			g.setColor(Color.WHITE);
			g.setFont(f);
			final TextLayout tl = new TextLayout(name, f, g
			        .getFontRenderContext());
			tl.draw(g, 0, tl.getAscent());
		}

		// draw ticks
		BufferedImage tic = bi.getSubimage(0, a.size() * heightPerTIC,
		        maxLength + maxStringLength, heightPerTIC);
		Graphics2D g = tic.createGraphics();
		g.setColor(Color.white);
		f = new Font("Lucida Sans", Font.PLAIN, fontsize / 2);
		final int tickInterval = Factory.getInstance().getConfiguration()
		        .getInt(ImageTools.class.getName() + ".tickInterval", 250);
		TextLayout tl = new TextLayout("index", f, g.getFontRenderContext());
		tl.draw(g, (maxStringLength / 2)
		        - (int) (Math.ceil(tl.getBounds().getWidth() / 2.0)),
		        (heightPerTIC / 2) + tl.getAscent());
		for (int i = 0; i < maxLength; i++) {
			if (i % tickInterval == 0) {
				g.drawLine(maxStringLength + i, 0, maxStringLength + i,
				        heightPerTIC / 2);
				tl = new TextLayout((i) + "", f, g.getFontRenderContext());
				tl.draw(g, maxStringLength + i
				        - (int) (Math.rint(tl.getBounds().getWidth() / 2.0d)),
				        (heightPerTIC / 2) + tl.getAscent());
			}
		}
		tic = bi.getSubimage(0, (a.size() + 1) * heightPerTIC, maxLength
		        + maxStringLength, heightPerTIC);
		g = tic.createGraphics();
		tl = new TextLayout("time [min]", f, g.getFontRenderContext());
		tl.draw(g, (maxStringLength / 2)
		        - (int) (Math.ceil(tl.getBounds().getWidth() / 2.0)),
		        (heightPerTIC / 2) + tl.getAscent());
		final Array sat = ArrayTools.divBy60(scan_acquisition_time);
		final Index satI = sat.getIndex();
		final int timeInterval = Factory.getInstance().getConfiguration()
		        .getInt(ImageTools.class.getName() + ".timeInterval", 100);
		for (int i = 0; i < maxLength; i++) {
			final double value = (sat.getDouble(satI.set(i)));
			if (i % timeInterval == 0) {
				g.drawLine(maxStringLength + i, 0, maxStringLength + i,
				        heightPerTIC / 2);
				tl = new TextLayout(String.format("%.2f", value), f, g
				        .getFontRenderContext());
				tl.draw(g, maxStringLength + i
				        - (int) (Math.rint(tl.getBounds().getWidth() / 2.0d)),
				        (heightPerTIC / 2) + tl.getAscent());
			}
		}
		File out = null;
		final File d = FileTools.prependDefaultDirs(creator, date);
		out = new File(d, filename);
		try {
			ImageTools.log.info("Saving image to " + out.getAbsolutePath());

			ImageIO.write(bi, "png", out);
			return out;
		} catch (final IOException e) {
			ImageTools.log.error(e.getLocalizedMessage());
		}
		return null;
	}

	public static File drawTICS(final Class<?> creator,
	        final TupleND<IFileFragment> t, final String ticvar,
	        final String satVar, final IFileFragment ref,
	        final String filename, final Date date) {
		final int heightPerTIC = 50;
		int maxLength = 0;
		// Map of Names to projected anchors
		final HashMap<String, Array> anchors = new HashMap<String, Array>();
		final HashMap<String, List<String>> anchorNames = new HashMap<String, List<String>>();
		final ArrayList<Tuple2D<String, Array>> a = new ArrayList<Tuple2D<String, Array>>(
		        t.size());
		int k = 0;
		for (int i = 0; i < t.size(); i++) {
			if ((ref != null) && ref.getName().equals(t.get(i).getName())) {
				a.add(new Tuple2D<String, Array>(StringTools.removeFileExt(t
				        .get(i).getName())
				        + " *", t.get(i).getChild(ticvar).getArray()));
			} else {
				a.add(new Tuple2D<String, Array>(StringTools.removeFileExt(t
				        .get(i).getName()), t.get(i).getChild(ticvar)
				        .getArray()));
			}
			if (a.get(i).getSecond().getShape()[0] > maxLength) {
				maxLength = a.get(i).getSecond().getShape()[0];
				k = i;
			}
			final List<IAnchor> l1 = MaltcmsTools.prepareAnchors(t.get(i));
			final ArrayInt.D1 anchPos1 = new ArrayInt.D1(l1.size());
			final List<String> anchNames = new ArrayList<String>(l1.size());
			for (int j = 0; j < l1.size(); j++) {
				final int anchor = l1.get(j).getScanIndex();
				anchPos1.set(j, anchor);
				anchNames.add(l1.get(j).getName());
			}
			anchors
			        .put(StringTools.removeFileExt(t.get(i).getName()),
			                anchPos1);
			anchorNames.put(StringTools.removeFileExt(t.get(i).getName()),
			        anchNames);
		}
		final Array sat = ref == null ? t.get(k).getChild(satVar).getArray()
		        : ref.getChild(satVar).getArray();
		return ImageTools.drawTIC(creator, heightPerTIC, maxLength, a,
		        filename, sat, anchors, anchorNames, date);
	}

	public static ArrayList<Array> filter(final ArrayList<Array> arrays,
	        final int channels) {
		final ArrayList<Array> ret = new ArrayList<Array>(arrays.size());
		final ArrayDouble.D2 a = new ArrayDouble.D2(arrays.size(), channels);
		// ArrayDouble.D2 c = new ArrayDouble.D2(arrays.size(), channels);
		// Loop over scans
		int scan = 0;
		int minMass = 0;
		for (final Array arr : arrays) {
			final Index ind = arr.getIndex();
			if (arr instanceof Sparse) {
				final Sparse s = (Sparse) arr;
				minMass = s.getMinIndex();
				// Loop over mass channels
				for (int i = 0; i < s.getShape()[0]; i++) {
					a.set(scan, i, s.get(i + minMass));
				}
			} else {
				// Loop over mass channels
				for (int i = 0; i < arr.getShape()[0]; i++) {
					a.set(scan, i, arr.getDouble(ind.set(i)));
				}
			}

			scan++;
		}
		// int median_window = 10;
		// double lmedian = 0.0d;
		// double lstddev = 0.0d;
		// ArrayFactory.getConfiguration().setProperty(
		// MedianBaselineFilter.class.getName() + ".num_scans", arrays.size());
		// ArrayFactory.getConfiguration().setProperty(
		// MedianBaselineFilter.class.getName() + ".num_channels", channels);
		// ArrayFactory.getConfiguration().setProperty(
		// MedianBaselineFilter.class.getName() + ".median_window", 10);
		// ArrayFactory.getConfiguration().setProperty(
		// MedianBaselineFilter.class.getName() + ".snr_minimum", 0.0d);
		// MedianBaselineFilter mbf = ArrayFactory
		// .instantiate(MedianBaselineFilter.class);
		// mbf.configure(ArrayFactory.getConfiguration());
		// extract all channels
		// log.info("Shape of c = {}",Arrays.toString(a.getShape()));
		// ArrayDouble.D2 c = (ArrayDouble.D2) mbf.apply(new Array[] { a })[0];
		// MinMax mma = MAMath.getMinMax(a);
		// MinMax mmc = MAMath.getMinMax(c);
		// MultiplicationFilter mf = new MultiplicationFilter(mma.max/mmc.max);
		// c = (ArrayDouble.D2)mf.apply(new Array[]{a})[0];
		// log.info("Shape of c = {}",Arrays.toString(c.getShape()));
		// EvalTools.notNull(c);
		for (int j = 0; j < arrays.size(); j++) {
			ret.add(a.slice(0, j));
		}
		return ret;
	}

	public static BufferedImage fullSpectrum(final String chromatogramName,
	        final List<Array> arrays, final int height,
	        final int[][] colorRamp, final int sampleSize, final boolean flip,
	        final double threshold) {
		if (arrays.get(0).getRank() != 1) {
			ImageTools.log.error("Only rank 1 arrays allowed!");
			return null;
		}
		// if (!(arrays.get(0) instanceof Sparse)) {
		// log.error("Only Sparse arrays allowed!");
		// return;
		// }
		ImageTools.log.debug("Creating full spectrum image!");
		ImageTools.log.debug("Number of bins: " + height);
		BufferedImage bim = new BufferedImage(arrays.size(), height,
		        BufferedImage.TYPE_INT_RGB);
		final WritableRaster r = bim.getRaster();
		// Array glued = cross.tools.ArrayTools.flatten(arrays).getSecond()
		// .getSecond();
		ImageTools.makeImage(r, arrays, sampleSize, colorRamp, threshold);
		if (flip) {
			// Create a transposed image, flipped about the vertical axis
			final RenderedOp ro = TransposeDescriptor.create(bim,
			        TransposeDescriptor.FLIP_VERTICAL, null);
			bim = ro.getAsBufferedImage();
		}
		return bim;
	}

	public static double[] getBreakpoints(final Array values1,
	        final int samples, final double skipvalue) {
		final Array values = values1.copy();
		final double[] sorted = (double[]) values.get1DJavaArray(double.class);
		Arrays.sort(sorted);
		int skipped = 0;
		int skipoff = 0;
		ImageTools.log.debug("Skipvalue: {}", skipvalue);
		for (int i = 0; i < sorted.length; i++) {
			if (sorted[i] == skipvalue) {
				if (skipvalue == Double.POSITIVE_INFINITY) {
					sorted[i] = sorted[sorted.length - 1];
					skipped++;
				} else if (skipvalue == Double.NEGATIVE_INFINITY) {
					sorted[i] = sorted[0];
					skipoff++;
				} else {
					sorted[i] = sorted[0];
					skipped++;
				}
			}
		}
		double[] svals = null;
		if ((skipped == 0) && (skipoff == 0)) {
			svals = sorted;
		} else {
			if ((skipoff == 0) && (skipped > 0)) {
				svals = new double[sorted.length - skipped];
				System.arraycopy(sorted, 0, svals, 0, svals.length);
			} else if ((skipoff > 0) && (skipped == 0)) {
				svals = new double[sorted.length - skipoff];
				System.arraycopy(sorted, skipoff, svals, 0, svals.length);
			} else {
				svals = new double[sorted.length - skipoff - skipped];
				System.arraycopy(sorted, skipoff, svals, 0, svals.length);
			}
		}

		ImageTools.log.debug("Size of svals array={}", svals.length);
		double min = svals[0];
		double max = svals[svals.length - 1];

		double sum = 0;
		// IndexIterator ii = values.getIndexIterator();
		for (int i = 0; i < svals.length; i++) {
			sum += Math.abs(svals[i]);
		}

		ImageTools.log.debug("Total sum of intensities: " + sum);
		ImageTools.log.debug("Min value: " + svals[0] + " Max value: "
		        + svals[svals.length - 1]);
		final double nthPart = sum / ((double) samples);
		ImageTools.log.debug("Intensity per sample: " + nthPart);
		final double[] breakpoints = new double[samples];
		double psum = 0.0d;
		int cnt = 0;
		for (int i = 0; i < samples; i++) {
			while ((Math.abs(psum) < (i + 1) * nthPart)
			        && (cnt < (svals.length))) {
				psum += svals[cnt++];
			}
			ImageTools.log.debug("Breakpoint " + i + " = "
			        + svals[Math.max(0, cnt - 1)]);
			breakpoints[i] = svals[Math.max(0, cnt - 1)];
		}
		ImageTools.log.info("Using {} breakpoints", breakpoints.length);
		ImageTools.log.info(Arrays.toString(breakpoints));
		return breakpoints;
	}

	public static double getSample(final double[] samples,
	        final double[] breakpoints, final double value) {
		final int i = Arrays.binarySearch(breakpoints, value);
		ImageTools.log.debug("Binary search index " + i);
		if (i < 0) {
			if (value < breakpoints[0]) {
				ImageTools.log.debug("Value is smaller, returning "
				        + samples[0]);
				return samples[0];
			}
			if (value > breakpoints[breakpoints.length - 1]) {
				ImageTools.log.debug("Value is greater, returning "
				        + samples[breakpoints.length - 1]);
				return samples[breakpoints.length - 1];
			}
			ImageTools.log
			        .debug("Value in breakpoints, not exact, Returning sample "
			                + samples[Math.abs(i) - 1]);
			return samples[Math.abs(i) - 1];
		} else if (i >= 0) {
			ImageTools.log.debug("i >= 0, Returning sample " + samples[i]);
			return samples[i];
		} else {
			ImageTools.log.debug("Else: Returning sample " + samples[0]);
			return samples[0];
		}
	}

	public static void makeImage(final WritableRaster w,
	        final List<Array> arrays, final int nsamples,
	        final int[][] colorRamp, final double threshold) {
		final Array values = ArrayTools.glue(arrays);
		final double[] breakpoints = ImageTools.getBreakpoints(values,
		        nsamples, Double.POSITIVE_INFINITY);
		ImageTools.makeImage(w, arrays, nsamples, colorRamp, threshold,
		        breakpoints);
	}

	public static void makeImage(final WritableRaster w,
	        final List<Array> arrays, final int nsamples,
	        final int[][] colorRamp, final double threshold,
	        final double[] breakpoints) {
		final double[] samples = ImageTools.createSampleTable(nsamples);
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (final Array a : arrays) {
			final MinMax m = MAMath.getMinMax(a);
			if (m.max > max) {
				max = m.max;
			}
			if (m.min < min) {
				min = m.min;
			}
		}

		double t = threshold;
		if (t > 1.0d) {
			t = 1.0d;
		}
		if (t < 0.0d) {
			t = 0.0d;
		}
		int minmass = 0;
		for (int i = 0; i < arrays.size(); i++) {
			final Array s = arrays.get(i);
			Index si = null;
			if (s instanceof Sparse) {
				minmass = ((Sparse) s).getMinIndex();
				ImageTools.log.debug("MinMass: {}", minmass);
			} else {
				minmass = 0;
			}
			si = s.getIndex();
			final int bins = s.getShape()[0];
			ImageTools.log.debug("Scan {} has {} bins!", i, bins);
			for (int j = 0; j < bins; j++) {
				double v = 0.0d;
				if (s instanceof Sparse) {
					ImageTools.log.debug("Shape of s : {}, Setting index {}",
					        Arrays.toString(s.getShape()), (j));
					ImageTools.log.debug("Offset: {}", minmass);
					v = ImageTools.getSample(samples, breakpoints, ((Sparse) s)
					        .get(j + minmass));
					ImageTools.log.debug("Sample value: " + v + " original="
					        + ((Sparse) s).get(j + minmass));
				} else {
					v = ImageTools.getSample(samples, breakpoints, s
					        .getDouble(si.set(j)));
					ImageTools.log.debug("Sample value: " + v + " original="
					        + s.getDouble(si.set(j)));
				}
				if (v > 1.0) {
					v = 1.0;
				} else if (v < 0.0 || v < threshold) {
					v = 0.0;
				}
				v *= 255.0d;

				final int floor = (int) Math.floor(v);
				final int ceil = (int) Math.ceil(v);
				int v1, v2, v3;
				if (floor == ceil) {
					v1 = colorRamp[floor][0];
					v2 = colorRamp[floor][1];
					v3 = colorRamp[floor][2];
					// fleqceil++;
				} else {
					v1 = (int) Math.floor(MathTools.getLinearInterpolatedY(
					        floor, colorRamp[floor][0], ceil,
					        colorRamp[ceil][0], v));
					v2 = (int) Math.floor(MathTools.getLinearInterpolatedY(
					        floor, colorRamp[floor][1], ceil,
					        colorRamp[ceil][1], v));
					v3 = (int) Math.floor(MathTools.getLinearInterpolatedY(
					        floor, colorRamp[floor][2], ceil,
					        colorRamp[ceil][2], v));
					// interp++;
				}
				// double v1 = colorRamp[(int)floor][0];
				// double v2 = colorRamp[(int)floor][1];
				// double v3 = colorRamp[(int)floor][2];
				try {
					// if (Double.isNaN(v1) || Double.isNaN(v2)
					// || Double.isNaN(v3)) {
					// log.warn("Value of one of {},{},{} is NaN",
					// new Object[] { v1, v2, v3 });
					// }
					if (floor >= t) {
						w.setPixel(i, j, new int[] {
						// Double.isNaN(v1) ? colorRamp[0][0] : v1,
						        // Double.isNaN(v2) ? colorRamp[0][1] : v2,
						        // Double.isNaN(v3) ? colorRamp[0][2] : v3 });
						        v1, v2, v3 });// colorRamp[floor][0],
						// colorRamp[floor][1],
						// colorRamp[floor][2] });
					} else {
						w.setPixel(i, j, new int[] {
						        // Double.isNaN(v1) ? colorRamp[0][0] : v1,
						        // Double.isNaN(v2) ? colorRamp[0][1] : v2,
						        // Double.isNaN(v3) ? colorRamp[0][2] : v3 });
						        colorRamp[0][0], colorRamp[0][1],
						        colorRamp[0][2] });
					}
				} catch (final ArrayIndexOutOfBoundsException aio) {
					ImageTools.log.error("Index out of bounds at {},{}", i, j);
					throw aio;
				}
			}
		}
		ImageTools.log.debug("Maxval: {}  minval: {}", max, min);
		// log.info("{} ceil=floor, {} interpolations",fleqceil,interp);
	}

	public static RenderedImage makeImage2D(final Array values, int nsamples,
	        double skipvalue) {
		final BufferedImage ba = new BufferedImage(values.getShape()[0], values
		        .getShape()[1], BufferedImage.TYPE_3BYTE_BGR);
		final WritableRaster wr = ba.getRaster();
		final double[] breakpoints = getBreakpoints(values, nsamples, skipvalue);
		final int[][] colorRamp = new ColorRampReader().getDefaultRamp();
		makeImage2D(wr, values, nsamples, colorRamp, 0, breakpoints);
		return ba;
	}

	public static void makeImage2D(final WritableRaster w, final Array values,
	        final int nsamples, final int[][] colorRamp,
	        final double threshold, final double[] breakpoints) {
		if (values.getRank() != 2) {
			throw new IllegalArgumentException(
			        "Method only accepts arrays of rank 2");
		}
		final double[] samples = ImageTools.createSampleTable(nsamples);
		final MinMax m = MAMath.getMinMax(values);
		double t = threshold;
		if (t > 255.0d) {
			t = 255.0d;
		}
		if (t < 0.0d) {
			t = 0.0d;
		}
		final double maxval = m.max;
		final double minval = m.min;
		final Index idx = values.getIndex();
		for (int i = 0; i < values.getShape()[0]; i++) {
			for (int j = 0; j < values.getShape()[1]; j++) {
				double v = 0.0d;
				v = ImageTools.getSample(samples, breakpoints, values
				        .getDouble(idx.set(i, j)));
				if (v > 1.0) {
					v = 1.0;
				} else if (v < 0.0) {
					v = 0.0;
				}
				v *= 255.0d;

				final int floor = (int) Math.floor(v);
				final int ceil = (int) Math.ceil(v);
				int v1, v2, v3;
				if (floor == ceil) {
					v1 = colorRamp[floor][0];
					v2 = colorRamp[floor][1];
					v3 = colorRamp[floor][2];
					// fleqceil++;
				} else {
					v1 = (int) Math.floor(MathTools.getLinearInterpolatedY(
					        floor, colorRamp[floor][0], ceil,
					        colorRamp[ceil][0], v));
					v2 = (int) Math.floor(MathTools.getLinearInterpolatedY(
					        floor, colorRamp[floor][1], ceil,
					        colorRamp[ceil][1], v));
					v3 = (int) Math.floor(MathTools.getLinearInterpolatedY(
					        floor, colorRamp[floor][2], ceil,
					        colorRamp[(int) ceil][2], v));
					// interp++;
				}
				// double v1 = colorRamp[(int)floor][0];
				// double v2 = colorRamp[(int)floor][1];
				// double v3 = colorRamp[(int)floor][2];
				try {
					// if (Double.isNaN(v1) || Double.isNaN(v2)
					// || Double.isNaN(v3)) {
					// log.warn("Value of one of {},{},{} is NaN",
					// new Object[] { v1, v2, v3 });
					// }
					if (floor >= t) {
						w.setPixel(i, j, new int[] {
						// Double.isNaN(v1) ? colorRamp[0][0] : v1,
						        // Double.isNaN(v2) ? colorRamp[0][1] : v2,
						        // Double.isNaN(v3) ? colorRamp[0][2] : v3 });
						        v1, v2, v3 });// colorRamp[floor][0],
						// colorRamp[floor][1],
						// colorRamp[floor][2] });
					} else {
						w.setPixel(i, j, new int[] {
						        // Double.isNaN(v1) ? colorRamp[0][0] : v1,
						        // Double.isNaN(v2) ? colorRamp[0][1] : v2,
						        // Double.isNaN(v3) ? colorRamp[0][2] : v3 });
						        colorRamp[0][0], colorRamp[0][1],
						        colorRamp[0][2] });
					}
				} catch (final ArrayIndexOutOfBoundsException aio) {
					ImageTools.log.error("Index out of bounds at {},{}", i, j);
					throw aio;
				}
			}
		}
		ImageTools.log.debug("Maxval: {}  minval: {}", maxval, minval);
	}

	/**
	 * 
	 * @param bim
	 *            the image to save
	 * @param imgname
	 *            the filename
	 * @param format
	 *            the format, e.g. "png", "jpeg"
	 * @param caller
	 *            the class which called this method
	 * @param iw
	 *            may be null
	 */
	public static void saveImage(final RenderedImage bim, final String imgname,
	        final String format, final Class<?> caller,
	        final IWorkflowElement iw) {
		final String name = StringTools.removeFileExt(imgname);
		File out = null;
		if (name.contains(File.separator)) {
			throw new IllegalArgumentException(
			        "Name of chromatogram must not include file separator character!");
		}
		// final String filename = StringTools.removeFileExt(imgname) + "."
		// + format;
		final File d = FileTools.prependDefaultDirs(caller, iw.getIWorkflow()
		        .getStartupDate());
		out = new File(d, imgname + "." + format);
		if (iw != null) {
			final DefaultWorkflowResult dwr = new DefaultWorkflowResult(out,
			        iw, WorkflowSlot.VISUALIZATION);
			iw.getIWorkflow().append(dwr);
		}
		try {
			ImageTools.log.info("Saving image to " + out.getAbsolutePath());

			ImageIO.write(bim, format, out);
		} catch (final IOException e) {
			ImageTools.log.error(e.getLocalizedMessage());
		}
	}

	public static void writeImage(final JFreeChart chart, final File file,
	        final int imgwidth, final int imgheight) {
		try {
			final String ext = StringTools.getFileExtension(file
			        .getAbsolutePath());
			if (ext.equalsIgnoreCase("svg")) {
				// File f = new File(d, fname + "." + filetype);
				ImageTools.log
				        .info("Saving to file {}", file.getAbsolutePath());
				final FileOutputStream fos = new FileOutputStream(file);
				ImageTools.writeSVG(chart, fos, imgwidth, imgheight);
			} else if (ext.equalsIgnoreCase("png")) {// use png as default
				final File f = file;
				ImageTools.log.info("Saving to file {}", f.getAbsolutePath());
				final FileOutputStream fos = new FileOutputStream(f);
				ImageTools.writePNG(chart, fos, imgwidth, imgheight);
			} else {
				ImageTools.log.warn("Cannot handle image of type " + ext
				        + "! Saving as png!");
				final File f = new File(StringTools.removeFileExt(file
				        .getAbsolutePath())
				        + ".png");
				ImageTools.log.info("Saving to file {}", f.getAbsolutePath());
				final FileOutputStream fos = new FileOutputStream(f);
				ImageTools.writePNG(chart, fos, imgwidth, imgheight);
			}
		} catch (final FileNotFoundException e) {
			ImageTools.log.error(e.getLocalizedMessage());
		}
	}

	public static void writePNG(final JFreeChart chart,
	        final FileOutputStream fos, final int imgwidth, final int imgheight) {
		try {
			EncoderUtil.writeBufferedImage(chart.createBufferedImage(imgwidth,
			        imgheight), "png", fos);
		} catch (final IOException e) {
			ImageTools.log.error(e.getLocalizedMessage());
		}
	}

	public static void writeSVG(final JFreeChart chart,
	        final FileOutputStream fos, final int imgwidth, final int imgheight) {
		try {
			// Following code is adapted from JFreeChart Developers manual
			final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
			final DOMImplementation domImpl = SVGDOMImplementation
			        .getDOMImplementation();
			// Create an instance of org.w3c.dom.Document
			final Document document = domImpl
			        .createDocument(svgNS, "svg", null);
			// Create an instance of the SVG Generator
			final SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
			// set the precision to avoid a null pointer exception in Batik 1.5
			svgGenerator.getGeneratorContext().setPrecision(6);
			// Ask the chart to render into the SVG Graphics2D implementation
			svgGenerator.setSVGCanvasSize(new Dimension(imgwidth, imgheight));
			chart.draw(svgGenerator, new Rectangle2D.Double(0, 0, imgwidth,
			        imgheight), null);
			// Finally, stream out SVG to a file using UTF-8 character to
			// byte encoding
			final boolean useCSS = true;
			Writer out;
			try {
				out = new OutputStreamWriter(fos, "UTF-8");
				svgGenerator.stream(out, useCSS);
			} catch (final UnsupportedEncodingException e) {
				ImageTools.log.error(e.getLocalizedMessage());
			} catch (final SVGGraphics2DIOException e) {
				ImageTools.log.error(e.getLocalizedMessage());
			}
		} catch (NoClassDefFoundError cnfe) {
			ImageTools.log.warn("Batik is not present on the classpath!");
		}

	}
}
