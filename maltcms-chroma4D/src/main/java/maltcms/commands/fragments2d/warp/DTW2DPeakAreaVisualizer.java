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
package maltcms.commands.fragments2d.warp;

import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.io.File;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.tools.ArrayTools;
import maltcms.ui.charts.EPlotRunner;
import maltcms.ui.charts.PlotRunner;
import org.apache.commons.configuration.Configuration;
import org.jfree.chart.JFreeChart;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;

/**
 * Default visualization pipeline command.
 *
 * @author Mathias Wilhelm
 */
@Slf4j
@Data
@RequiresVariables(names = {"var.scan_acquisition_time_1d",
    "var.modulation_time", "var.scan_rate", "var.second_column_time",
    "var.region_index_list",
    // "var.warp_path_i", "var.warp_path_j",
    "var.boundary_index_list"})
@ServiceProvider(service = AFragmentCommand.class)
public class DTW2DPeakAreaVisualizer extends DTW2DTicVisualizer {

    @Configurable(name = "var.boundary_index_list",
        value = "boundary_index_list")
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
//        this.fillPeakArea = cfg.getBoolean(this.getClass().getName()
//                + ".fillPeakArea", false);
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
            log.info("Filling peak area; using {}",
                this.regionIndexListVar);
            refBoundary = ff.getChild(this.regionIndexListVar).getArray();
        } else {
            log.info("Do not fill peak area; using {}",
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
                log.error("IndexOutOfBounds for index {}(" + (k / spm)
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
        final PlotRunner pl = new EPlotRunner(chart,
            StringTools.removeFileExt(ref.getName())
            + "_vs_" + StringTools.removeFileExt(query.getName()) + "-PA",
            getWorkflow().getOutputDirectory(this));
        pl.configure(Factory.getInstance().getConfiguration());
        final File f = pl.getFile();
        final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f, this,
            WorkflowSlot.VISUALIZATION, ref, query);
        getWorkflow().append(dwr);
        Factory.getInstance().submitJob(pl);
    }
}
