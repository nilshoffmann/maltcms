/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package maltcms.commands.fragments2d.tools;

import cross.Factory;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.tools.StringTools;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.IndexIterator;

/**
 *
 * @author nilshoffmann
 */
public class ArrayTools {

    /**
	 * Visualization of the sorted standard deviation.
	 *
	 * @param ff
	 *            file fragment
	 * @param sd
	 *            standard deviation
	 * @param quantil
	 *            array quantils
	 * @param visualize
	 *            visualize sorted array
	 */
	public static double[] getQuantilValue(final IFileFragment ff,
			final Array sd, final double[] quantil, final boolean visualize,
			final AFragmentCommand ac) {
		final IndexIterator iter = sd.getIndexIterator();
		final List<Tuple2D<Integer, Integer>> stdL = new ArrayList<Tuple2D<Integer, Integer>>();
		int c = 0;
		int stdC = 0;
		int stdSum = 0;
		while (iter.hasNext()) {
			stdC = iter.getIntNext();
			stdSum += stdC;
			stdL.add(new Tuple2D<Integer, Integer>(c++, stdC));
		}
		Collections.sort(stdL, new Comparator<Tuple2D<Integer, Integer>>() {
			@Override
			public int compare(final Tuple2D<Integer, Integer> o1,
					final Tuple2D<Integer, Integer> o2) {
				return Double.compare(o1.getSecond(), o2.getSecond());
			}
		});

		final ArrayInt.D1 h = new ArrayInt.D1(stdL.size());
		final ArrayInt.D1 g = new ArrayInt.D1(stdL.size());
		c = 0;
		double tSum = 0;
		double[] position = new double[quantil.length];
		double[] value = new double[quantil.length];
		int tmp = 0;
		for (Tuple2D<Integer, Integer> e : stdL) {
			tmp = e.getSecond();
			if (tmp == 0) {
				tmp = 1;
			}
			h.set(c, tmp);
			g.set(c, c);
			c++;
			tSum += (double) e.getSecond();
			for (int i = 0; i < quantil.length; i++) {
				if (tSum / (double) stdSum <= quantil[i]) {
					position[i] = c;
					value[i] = e.getSecond();
				}
			}
		}
		if (visualize) {
			final AChart<XYPlot> xyc1 = new XYChart(
					"Visualization of sorted standard deviation",
					new String[] { StringTools.removeFileExt(ff.getName()) },
					new Array[] { h }, new Array[] { g }, "#",
					"standard deviation", true);
			final XYPlot plot = xyc1.create();
			for (int i = 0; i < quantil.length; i++) {
				plot.addDomainMarker(new ValueMarker(position[i]));
			}
			final PlotRunner pr1 = new PlotRunner(plot,
					"Plot of sorted standard deviation", StringTools
							.removeFileExt(ff.getName())
							+ "_sortedStd", ac.getWorkflow()
							.getOutputDirectory(ac));
			pr1.configure(Factory.getInstance().getConfiguration());
			final File f1 = pr1.getFile();
			final DefaultWorkflowResult dwr1 = new DefaultWorkflowResult(f1,
					ac, ac.getWorkflowSlot(), ff);
			ac.getWorkflow().append(dwr1);
			Factory.getInstance().submitJob(pr1);
		}
		return value;
	}

}
