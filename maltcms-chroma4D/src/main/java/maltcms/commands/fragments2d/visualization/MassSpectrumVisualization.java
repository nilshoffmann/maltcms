/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.commands.fragments2d.visualization;

import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.io.File;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.tools.ImageTools;
import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.MassSpectrumPlot;
import org.apache.commons.configuration.Configuration;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.MAMath;

/**
 * Visualization for one mass spectra.
 *
 * @author Mathias Wilhelm
 */
@Slf4j
@Data
@RequiresVariables(names = {"var.mass_values", "var.intensity_values",
	"var.scan_index", "var.mass_range_min", "var.mass_range_max",
	"var.modulation_time", "var.scan_rate", "var.modulation_time",
	"var.scan_rate"})
@ServiceProvider(service = AFragmentCommand.class)
public class MassSpectrumVisualization extends AFragmentCommand {

	@Configurable(name = "var.modulation_time", value = "modulation_time")
	private String modulationVar = "modulation_time";
	@Configurable(name = "var.scan_rate", value = "scan_rate")
	private String scanRateVar = "scan_rate";
	@Configurable(value = "0")
	private int[] indices = new int[]{0};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		Tuple2D<Double, Double> massRange = MaltcmsTools.getMinMaxMassRange(t);
		ScanLineCacheFactory.setMinMass(massRange.getFirst());
		ScanLineCacheFactory.setMaxMass(massRange.getSecond());
		for (final IFileFragment ff : t) {
			final IScanLine scl = ScanLineCacheFactory.getScanLineCache(ff);
			final int modulation = ff.getChild(this.modulationVar).getArray().
				getInt(Index.scalarIndexImmutable);
			final int scanRate = ff.getChild(this.scanRateVar).getArray().getInt(
				Index.scalarIndexImmutable);
			final int scansPerModulation = modulation * scanRate;
			for (Integer index : this.indices) {
				if ((index >= 0) && (index <= scl.getLastIndex())) {
					final int x = index / scansPerModulation;
					final int y = index % scansPerModulation;
					// final List<Array> scanline = scl.getScanlineMS(x);
					final Array massSpectra = scl.getScanlineMS(x).get(y);
					final String title = "mass spectrum for index " + index
						+ "(TIC:" + (int) MAMath.sumDouble(massSpectra)
						+ ")";
					log.info(
						"Plotting index {}(" + MAMath.sumDouble(massSpectra) + "): {}",
						index, massSpectra);
					final AChart<XYPlot> plot = new MassSpectrumPlot(title,
						StringTools.removeFileExt(ff.getName()) + " Idx:"
						+ index, massSpectra, false, false);

					ImageTools.writeImage(new JFreeChart(plot.create()),
						new File(getWorkflow().getOutputDirectory(this),
							StringTools.removeFileExt(ff.getName())
							+ "_ms-" + index + ".png"), 1024, 768);
				} else {
					log.error("Index {} out of range.", index);
				}
			}
		}
		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Configuration cfg) {
		this.modulationVar = cfg.getString("var.modulation_time",
			"modulation_time");
		this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
//        String[] indA = cfg.getStringArray(this.getClass().getName()
//                + ".indices");
//        this.indices = new int[indA.length];
//        int c = 0;
//        for (String i : indA) {
//            log.info("Writing {} in array", i);
//            this.indices[c++] = Integer.parseInt(i);
//        }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return "Visualize one mass spectrum.";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.VISUALIZATION;
	}
}
