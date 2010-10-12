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
 * $Id$
 */
package maltcms.commands.fragments2d.peakfinding;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.MovingAverageFilter;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.PeakArea2D;
import maltcms.tools.ArrayTools2;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYChart;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;
import cross.Factory;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.WorkflowSlot;

/**
 * Does the Peak Integration on basis of the unique masses.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public class PeakIntegration implements IPeakIntegration {

	@Configurable(value = "1")
	private int k = 1;
	@Configurable(value = "false")
	private boolean plotIntegration = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Configuration cfg) {
		this.plotIntegration = cfg.getBoolean(this.getClass().getName()
		        + ".plot", false);
		this.k = cfg.getInt(this.getClass().getName() + ".k", 1);
	}

	/**
	 * Determines the start and end position of the peak in a specific scanline.
	 * 
	 * @param scanline
	 *            scanline
	 * @param pa
	 *            peakarea
	 * @return start and end of this peak
	 */
	private int[] getStartAndEnd(final int scanline, final PeakArea2D pa) {
		final int[] startEnd = new int[] { 1000, 0, };
		for (final Point p : pa.getRegionPoints()) {
			if (p.x == scanline) {
				if (p.y < startEnd[0]) {
					startEnd[0] = p.y - 1;
				}
				if (p.y > startEnd[1]) {
					startEnd[1] = p.y + 1;
				}
			}
		}
		return startEnd;
	}

	/**
	 * Creates the tic.
	 * 
	 * @param scanline
	 *            scanline
	 * @return tic array
	 */
	@Deprecated
	private ArrayDouble.D1 getTicArray(final List<Array> scanline) {
		final ArrayDouble.D1 ticArray = new ArrayDouble.D1(scanline.size());
		IndexIterator iter;
		double sum;
		for (int i = 0; i < scanline.size(); i++) {
			sum = 0;
			iter = scanline.get(i).getIndexIterator();
			while (iter.hasNext()) {
				sum += iter.getIntNext();
			}
			ticArray.set(i, sum);
		}
		return ticArray;
	}

	/**
	 * {@inheritDoc}
	 */
	public void integrate(final Peak2D peak, final IFileFragment ff,
	        final IWorkflow workflow) {
		final List<Tuple2D<Integer, Double>> sortedMS = ArrayTools2
		        .getUniqueMasses(peak.getPeakArea().getSeedMS());
		final int sortedSize = sortedMS.size();
		final IScanLine slc = ScanLineCacheFactory.getScanLineCache(ff);

		final List<Integer> scanlineNumbers = new ArrayList<Integer>();
		for (final Point p : peak.getPeakArea().getRegionPoints()) {
			if (!scanlineNumbers.contains(p.x)) {
				scanlineNumbers.add(p.x);
			}
		}

		final Map<Integer, ArrayDouble.D1[]> um = new HashMap<Integer, ArrayDouble.D1[]>();
		final Map<Integer, ArrayDouble.D1> tic = new HashMap<Integer, ArrayDouble.D1>();
		ArrayDouble.D1[] intenArray;
		int c = 0;
		List<Array> scanline = null;
		for (final Integer x : scanlineNumbers) {
			scanline = slc.getScanlineMS(x);
			intenArray = new ArrayDouble.D1[this.k];
			for (int i = 0; i < this.k; i++) {
				intenArray[i] = new ArrayDouble.D1(slc.getScansPerModulation());
			}
			ArrayDouble.D1 msd = null;
			int j = 0;
			double inten = 0;
			c = 0;
			for (final Array ms : scanline) {
				msd = (ArrayDouble.D1) ms;
				for (int i = 0; i < this.k; i++) {
					j = sortedMS.get(sortedSize - i - 1).getFirst();
					inten = msd.get(j);
					intenArray[i].set(c, inten);
				}
				c++;
			}
			um.put(x, intenArray);
			// TODO: richtiges array nehmen: also auch v_total_intensity
			tic.put(x, getTicArray(scanline));
		}

		final AArrayFilter filter1 = new MovingAverageFilter();

		final Map<Integer, Array[]> um1 = new HashMap<Integer, Array[]>();
		for (final Integer x : um.keySet()) {
			um1.put(x, filter1.apply(um.get(x)));
		}
		final Map<Integer, Array> tic1 = new HashMap<Integer, Array>();
		for (final Integer x : tic.keySet()) {
			tic1.put(x, filter1.apply(new Array[] { tic.get(x) })[0]);
		}

		if (this.plotIntegration) {
			Array v1, v2;
			int j;
			int[] startEnd;
			XYPlot plot;
			for (final Integer x : um.keySet()) {
				for (int i = 0; i < this.k + 1; i++) {
					if (i < this.k) {
						v1 = um.get(x)[i];
						v2 = um1.get(x)[i];
						j = sortedMS.get(sortedSize - i - 1).getFirst();
					} else {
						v1 = tic.get(x);
						v2 = tic1.get(x);
						j = 0;
					}
					final AChart<XYPlot> xyc = new XYChart(
					        "1D Visualization of Scanline " + (x + 1),
					        new String[] { j + " unfiltered", j + " filtered", },
					        new Array[] { v1, v2, }, "time", "intensity");
					plot = xyc.create();

					startEnd = getStartAndEnd(x, peak.getPeakArea());
					final Marker currentStart = new ValueMarker(startEnd[0]);
					currentStart.setPaint(Color.red);
					plot.addDomainMarker(currentStart);
					final Marker currentEnd = new ValueMarker(startEnd[1]);
					currentEnd.setPaint(Color.red);
					plot.addDomainMarker(currentEnd);

					final PlotRunner pr = new PlotRunner(plot, "Plot of Peak"
					        + peak.getIndex(), peak.getIndex() + "_sl"
					        + (x + 1) + "_m" + j, workflow
					        .getOutputDirectory(this));
					pr.configure(Factory.getInstance().getConfiguration());
					try {
						final File f = pr.getFile();
						final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
						        f, SeededRegionGrowing.class.newInstance(),
						        WorkflowSlot.VISUALIZATION, ff);
						workflow.append(dwr);
						Factory.getInstance().submitJob(pr);
					} catch (final InstantiationException e) {
						e.printStackTrace();
					} catch (final IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}

		final double[] peaksum2 = new double[this.k];
		ArrayDouble.D1 tt = null;
		for (final Point p : peak.getPeakArea().getRegionPoints()) {
			for (int i = 0; i < this.k; i++) {
				tt = (ArrayDouble.D1) um1.get(p.x)[i];
				try {
					peaksum2[i] += tt.get(p.y);
				} catch (java.lang.ArrayIndexOutOfBoundsException ex) {
					System.err.println("Array index out of bounds for peak "
					        + p + " and index " + i + ". Array length is "
					        + tt.getShape()[0]);
				}
			}
		}

		for (int i = 0; i < this.k; i++) {
			peak.getPeakArea().addAreaIntensity(
			        sortedMS.get(sortedSize - i - 1).getFirst(), peaksum2[i]);
		}
	}
}
