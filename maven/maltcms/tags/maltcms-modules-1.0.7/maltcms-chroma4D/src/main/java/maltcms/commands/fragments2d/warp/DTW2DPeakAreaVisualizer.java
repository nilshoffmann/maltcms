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
 * $Id: DTW2DPeakAreaVisualizer.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.commands.fragments2d.warp;

import java.io.File;
import java.util.List;

import maltcms.tools.ArrayTools;
import maltcms.ui.charts.EPlotRunner;
import maltcms.ui.charts.PlotRunner;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;
import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;

/**
 * Default visualization pipeline command.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@RequiresVariables(names = { "var.scan_acquisition_time_1d",
        "var.modulation_time", "var.scan_rate", "var.second_column_time",
        "var.region_index_list",
        // "var.warp_path_i", "var.warp_path_j",
        "var.boundary_index_list" })
@RequiresOptionalVariables(names = { "" })
@ProvidesVariables(names = { "" })
public class DTW2DPeakAreaVisualizer extends DTW2DTicVisualizer {

	private final Logger log = Logging.getLogger(this);

	@Configurable(name = "var.boundary_index_list", value = "boundary_index_list")
	private String boundaryPeakListVar = "boundary_index_list";
	@Configurable(name = "var.region_index_list", value = "region_index_list")
	private String regionIndexListVar = "region_index_list";
	@Configurable(value = "false")
	private boolean fillPeakArea = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Configuration cfg) {
		super.configure(cfg);
		this.boundaryPeakListVar = cfg.getString("var.boundary_index_list",
		        "boundary_index_list");
		this.regionIndexListVar = cfg.getString("var.region_index_list",
		        "region_index_list");
		this.fillPeakArea = cfg.getBoolean(this.getClass().getName()
		        + ".fillPeakArea", false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<Array> getScanlineFor(final IFileFragment ff, final int spm) {
		final List<Array> scanlines = super.getScanlineFor(ff, spm);
		super.normalize(false);
		super.setBinSize(2);
		for (int k = 0; k < scanlines.size(); k++) {
			ArrayTools.fill(scanlines.get(k), 0);
		}
		Array refBoundary = null;
		if (this.fillPeakArea) {
			this.log.info("Filling peak area; using {}",
			        this.regionIndexListVar);
			refBoundary = ff.getChild(this.regionIndexListVar).getArray();
		} else {
			this.log.info("Do not fill peak area; using {}",
			        this.boundaryPeakListVar);
			refBoundary = ff.getChild(this.boundaryPeakListVar).getArray();
		}
		final IndexIterator iter = refBoundary.getIndexIterator();
		int k = 0;
		ArrayDouble.D1 tmp;
		while (iter.hasNext()) {
			k = iter.getIntNext();
			try {
				tmp = (ArrayDouble.D1) scanlines.get(k / spm);
				tmp.set(k % spm, 1.0d);
			} catch (final IndexOutOfBoundsException e) {
				this.log.error("IndexOutOfBounds for index {}(" + (k / spm)
				        + "," + (k % spm) + ")", k);
			}
		}

		return scanlines;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveChart(final JFreeChart chart, final IFileFragment ref,
	        final IFileFragment query) {
		final PlotRunner pl = new EPlotRunner(chart, StringTools
		        .removeFileExt(ref.getName())
		        + "_vs_" + StringTools.removeFileExt(query.getName()) + "-PA",
		        getIWorkflow().getOutputDirectory(this));
		pl.configure(Factory.getInstance().getConfiguration());
		final File f = pl.getFile();
		final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f, this,
		        WorkflowSlot.VISUALIZATION, ref, query);
		getIWorkflow().append(dwr);
		Factory.getInstance().submitJob(pl);
	}

}
