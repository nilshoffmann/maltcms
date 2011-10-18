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
 * $Id: MZIDistributionVisualizer.java 115 2010-04-23 15:42:15Z nilshoffmann $
 */
package maltcms.commands.fragments.visualization;

import java.io.File;

import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.PlotRunner;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import cross.Factory;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * Plot the distribution of values looking at mz bin values.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
@Data
@ServiceProvider(service=AFragmentCommand.class)
public class MZIDistributionVisualizer extends AFragmentCommand {

    protected IFileFragment filea;
    private String x_var = "mass_values";
    private String y_var = "intensity_values";
    private final boolean one_for_each = true;

    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        for (final IFileFragment f : t) {
            log.info("Processing {}", f.getName());
            final Array[] vals = new Array[1];
            vals[0] = f.getChild(this.y_var).getArray().copy();
            final Array[] domains = new Array[1];
            domains[0] = f.getChild(this.x_var).getArray().copy();
            final Index mindx = domains[0].getIndex();
            final Index iindx = vals[0].getIndex();
            log.info("Adding elements!");
            final MinMax mm = MAMath.getMinMax(domains[0]);
            final double res = 1.0d;
            final int nbins = MaltcmsTools.getNumberOfIntegerMassBins(mm.min,
                    mm.max, res);
            log.info("Using nbins: {}", nbins);
            final ArrayDouble.D1 binVal = new ArrayDouble.D1(nbins);
            final ArrayDouble.D1 binCnt = new ArrayDouble.D1(nbins);
            for (int i = 0; i < domains[0].getShape()[0]; i++) {
                final double mz = domains[0].getDouble(mindx.set(i));
                final double intens = vals[0].getDouble(iindx.set(i));
                final int bin = MaltcmsTools.binMZ(mz, mm.min, mm.max, res);
                log.debug("Setting bin: {}", bin);
                binVal.set(bin, MaltcmsTools.binMZ(mz + mm.min, mm.min, mm.max,
                        res));
                binCnt.set(bin, binCnt.get(bin) + intens);
            }
            final MinMax mmi = MAMath.getMinMax(binCnt);
            final XYIntervalSeries dcd = new XYIntervalSeries(f.getName());
            for (int i = 0; i < nbins; i++) {
                final double bv = binVal.get(i);
                dcd.add(bv, bv, bv + res, binCnt.get(i), mmi.min, mmi.max);
            }
            log.info("Creating plot");
            final XYIntervalSeriesCollection xsc = new XYIntervalSeriesCollection();
            xsc.addSeries(dcd);
            final XYBarRenderer xyb = new XYBarRenderer(0.0);
            xyb.setShadowVisible(false);
            xyb.setGradientPaintTransformer(null);
            final XYPlot cp = new XYPlot(xsc, new NumberAxis("mass value"),
                    new NumberAxis("intensity value"), xyb);
            final PlotRunner pl = new PlotRunner(cp,
                    "Intensity Distribution Plot of " + f.getName(),
                    "intensDistrPlot-" + f.getName(), getWorkflow().
                    getOutputDirectory(this));
            pl.configure(Factory.getInstance().getConfiguration());
            final File file = pl.getFile();
            final DefaultWorkflowResult dwr = new DefaultWorkflowResult(file,
                    this, WorkflowSlot.VISUALIZATION, f);
            getWorkflow().append(dwr);
            Factory.getInstance().submitJob(pl);
        }
        return t;
    }

    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.x_var = cfg.getString(this.getClass().getName() + ".x_var",
                "mass_values");
        this.y_var = cfg.getString(this.getClass().getName() + ".y_var",
                "intensity_values");
    }

    @Override
    public String getDescription() {
        return "Creates plot of distribution of mass values versus intensity values in a chromatogram.";
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.VISUALIZATION;
    }
}
