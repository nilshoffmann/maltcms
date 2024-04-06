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
package maltcms.commands.fragments.visualization;

import cross.Factory;
import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import java.io.File;
import java.util.List;
import lombok.Data;

import maltcms.commands.filters.array.AdditionFilter;
import maltcms.commands.filters.array.NormalizationFilter;
import maltcms.tools.ArrayTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYChart;
import org.apache.commons.configuration.Configuration;
import org.jfree.chart.plot.XYPlot;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.MAMath;

/**
 * Plot 1-dimensional arrays, possibly with an additional array providing domain
 * values.
 *
 * @author Nils Hoffmann
 * 
 */

@Data
@ServiceProvider(service = AFragmentCommand.class)
public class Array1DVisualizer extends AFragmentCommand {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Array1DVisualizer.class);

    private final String description = "Creates plots of 1-dimensional variables.";
    private final WorkflowSlot workflowSlot = WorkflowSlot.VISUALIZATION;
    @Configurable(name = "var.total_intensity")
    private String variableName = "total_intensity";
    @Configurable(name = "var.scan_acquisition_time")
    private String scanAcquisitionTimeVariableName = "scan_acquisition_time";
    @Configurable(name = "maltcms.commands.fragments.visualization.x_axis_label")
    private String xAxisLabel = "Scans";
    @Configurable(name = "maltcms.commands.fragments.visualization.y_axis_label")
    private String yAxisLabel = "Counts";
    @Configurable(description="If true, create pairwise plots.")
    private boolean pairwise = false;
    @Configurable(description="If true, create plots with first chromatogram.")
    private boolean pairwiseWithFirst = true;
    @Configurable(description="If true, subtract start time from displayed "
            + "retention times. This can be beneficial for chromatograms with "
            + "varying start times.")
    private boolean substractStartTime = true;
    @Configurable(description="If true, put all charts into one.")
    private boolean allInOneChart = false;
    @Configurable(description="The time unit to use. E.g. \"s\" for seconds.")
    private String timeUnit = "s";

    /** {@inheritDoc} */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        if (this.pairwise) {
            pairwise(t);
            residual(t);
            return t;
        }

        final String[] labels = new String[t.getSize()];
        final Array[] values = new Array[t.getSize()];
        final Array[] domains = new Array[t.getSize()];
        int i = 0;
        for (final IFileFragment ff : t) {
            log.info("Processing File fragment {}: {} of {}",
                    new Object[]{ff.getName(), (i + 1), t.getSize()});
            labels[i] = ff.getUri().toString();
            final IVariableFragment ivf = ff.getChild(this.variableName);
            values[i] = ivf.getArray();
            log.debug("Parent of {}:{}", ivf, ivf.getParent());

            if (!this.allInOneChart) {
                try {
                    domains[i] = ff.getChild(
                            this.scanAcquisitionTimeVariableName).getArray().
                            copy();
                    switch (this.timeUnit) {
                        case "min":
                            domains[i] = ArrayTools.divBy60(domains[i]);
                            break;
                        case "h":
                            domains[i] = ArrayTools.divBy60(ArrayTools.divBy60(
                                    domains[i]));
                            break;
                    }
                    log.debug("Using scan acquisition time0 {}",
                            domains[i]);

                    this.xAxisLabel = "time [" + this.timeUnit + "]";
                } catch (final ResourceNotAvailableException re) {
                    log.info(
                            "Could not load resource {} for domain axis, falling back to scan index domain!",
                            this.scanAcquisitionTimeVariableName);
                    domains[i] = ArrayTools.indexArray(values[i].getShape()[0],
                            0);
                }
                final AChart<XYPlot> xyc = new XYChart("1D Visualization of "
                        + this.variableName, new String[]{labels[i]},
                        new Array[]{values[i]}, new Array[]{domains[i]},
                        this.xAxisLabel, this.yAxisLabel);
                final PlotRunner pr = new PlotRunner(xyc.create(), "Plot of "
                        + this.variableName, ff.getName() + ">"
                        + this.variableName, getWorkflow().getOutputDirectory(
                                this));
                pr.configure(Factory.getInstance().getConfiguration());
                final File f = pr.getFile();
                try {
                    pr.call();
                } catch (Exception ex) {
                    log.error(ex.getLocalizedMessage());
                }
                final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f,
                        this, getWorkflowSlot(), ff);
                getWorkflow().append(dwr);
            }
            i++;
        }
        if (this.allInOneChart) {
            final AChart<XYPlot> xyc = new XYChart("1D Visualization of "
                    + this.variableName, labels, values, domains,
                    this.xAxisLabel, this.yAxisLabel);
            final PlotRunner pr = new PlotRunner(xyc.create(), "Plot of "
                    + this.variableName, this.variableName, getWorkflow().
                    getOutputDirectory(this));
            pr.configure(Factory.getInstance().getConfiguration());
            final File f = pr.getFile();
            try {
                pr.call();
            } catch (Exception ex) {
                log.error(ex.getLocalizedMessage());
            }
            final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f,
                    this, getWorkflowSlot(), t.toArray(new IFileFragment[]{}));
            getWorkflow().append(dwr);
        }
        return t;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        this.variableName = cfg.getString(this.getClass().getName()
                + ".variableName", "total_intensity");
        this.scanAcquisitionTimeVariableName = cfg.getString(
                "var.scan_acquisition_time", "scan_acquisition_time");
    }

    /**
     * <p>pairwise.</p>
     *
     * @param t a {@link cross.datastructures.tuple.TupleND} object.
     */
    protected void pairwise(final TupleND<IFileFragment> t) {
        List<Tuple2D<IFileFragment, IFileFragment>> l = null;
        if (this.pairwiseWithFirst) {
            l = t.getPairsWithFirstElement();
        } else {
            l = t.getPairs();
        }
        int pairs = 0;
        for (final Tuple2D<IFileFragment, IFileFragment> pair : l) {
            final String[] labels = new String[2];
            final Array[] values = new Array[2];
            final IFileFragment lhs = pair.getFirst();
            final IFileFragment rhs = pair.getSecond();
            final Array[] domains = new Array[2];
            // for(FileFragment ff:t) {
            labels[0] = lhs.getUri().toString();
            labels[1] = rhs.getUri().toString();
            if (lhs.hasChild(this.variableName)
                    && rhs.hasChild(this.variableName)) {
                values[0] = lhs.getChild(this.variableName).getArray().copy();
                values[1] = rhs.getChild(this.variableName).getArray().copy();
                if (lhs.hasChild(this.scanAcquisitionTimeVariableName)
                        && rhs.hasChild(this.scanAcquisitionTimeVariableName)) {
                    domains[0] = lhs.getChild(
                            this.scanAcquisitionTimeVariableName).getArray().
                            copy();
                    domains[1] = rhs.getChild(
                            this.scanAcquisitionTimeVariableName).getArray().
                            copy();
                    switch (this.timeUnit) {
                        case "min":
                            domains[0] = ArrayTools.divBy60(domains[0]);
                            domains[1] = ArrayTools.divBy60(domains[1]);
                            break;
                        case "h":
                            domains[0] = ArrayTools.divBy60(ArrayTools.divBy60(
                                    domains[0]));
                            domains[1] = ArrayTools.divBy60(ArrayTools.divBy60(
                                    domains[1]));
                            break;
                    }
                    final double min = MAMath.getMinimum(domains[0]);
                    final double min1 = MAMath.getMinimum(domains[1]);

                    this.xAxisLabel = "time [" + this.timeUnit + "]";
                    if (this.substractStartTime) {
                        final AdditionFilter af = new AdditionFilter(-min);
                        final AdditionFilter af1 = new AdditionFilter(-min1);
                        domains[0] = af.apply(new Array[]{domains[0]})[0];
                        domains[1] = af1.apply(new Array[]{domains[1]})[0];
                    }
                } else {
                    domains[0] = ArrayTools.indexArray(values[0].getShape()[0],
                            0);
                    domains[1] = ArrayTools.indexArray(values[1].getShape()[0],
                            0);
                    this.xAxisLabel = "scan number";
                }
                final AChart<XYPlot> xyc = new XYChart("1D Visualization of "
                        + this.variableName, labels, values, domains,
                        this.xAxisLabel, this.yAxisLabel);
                final PlotRunner pr = new PlotRunner(xyc.create(), "Plot of "
                        + this.variableName + " from files " + lhs.getName()
                        + " and " + rhs.getName(), this.variableName + "Chart-"
                        + lhs.getName() + "-" + rhs.getName(), getWorkflow().
                        getOutputDirectory(this));
                pr.configure(Factory.getInstance().getConfiguration());
                final File f = pr.getFile();
                try {
                    pr.call();
                } catch (Exception ex) {
                    log.error(ex.getLocalizedMessage());
                }
                final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f,
                        this, getWorkflowSlot(),
                        new IFileFragment[]{lhs, rhs});
                getWorkflow().append(dwr);
                pairs++;
            } else {
                throw new IllegalArgumentException(lhs.getUri()
                        + " has no child " + this.variableName);
            }
            // }

        }
    }

    /**
     * <p>residual.</p>
     *
     * @param t a {@link cross.datastructures.tuple.TupleND} object.
     */
    protected void residual(final TupleND<IFileFragment> t) {
        List<Tuple2D<IFileFragment, IFileFragment>> l = null;
        if (this.pairwiseWithFirst) {
            l = t.getPairsWithFirstElement();
        } else {
            l = t.getPairs();
        }
        for (final Tuple2D<IFileFragment, IFileFragment> pair : l) {
            final String[] labels = new String[2];
            final Array[] values = new Array[2];
            final IFileFragment lhs = pair.getFirst();
            final IFileFragment rhs = pair.getSecond();
            final Array[] domains = new Array[2];
            // for(FileFragment ff:t) {
            labels[0] = lhs.getUri().toString();
            labels[1] = rhs.getUri().toString();
            if (lhs.hasChild(this.variableName)
                    && rhs.hasChild(this.variableName)) {
                values[0] = lhs.getChild(this.variableName).getArray();
                values[1] = rhs.getChild(this.variableName).getArray();
                if (lhs.hasChild(this.scanAcquisitionTimeVariableName)
                        && rhs.hasChild(this.scanAcquisitionTimeVariableName)) {
                    domains[0] = lhs.getChild(
                            this.scanAcquisitionTimeVariableName).getArray();
                    domains[1] = rhs.getChild(
                            this.scanAcquisitionTimeVariableName).getArray();
                    final double min = MAMath.getMinimum(domains[0]);
                    final double min1 = MAMath.getMinimum(domains[1]);
                    this.xAxisLabel = "time [s]";
                    if (this.substractStartTime) {
                        final AdditionFilter af = new AdditionFilter(-min);
                        final AdditionFilter af1 = new AdditionFilter(-min1);
                        domains[0] = af.apply(new Array[]{domains[0]})[0];
                        domains[1] = af1.apply(new Array[]{domains[1]})[0];
                    }
                } else {
                    domains[0] = ArrayTools.indexArray(values[0].getShape()[0],
                            0);
                    domains[1] = ArrayTools.indexArray(values[1].getShape()[0],
                            0);
                    this.xAxisLabel = "scan number";
                }
                final Array maxA = (values[0].getShape()[0] > values[1].getShape()[0]) ? values[0] : values[1];
                final Array minA = (values[0].getShape()[0] <= values[1].
                        getShape()[0]) ? values[0] : values[1];
                final Array res = Array.factory(maxA.getDataType(),
                        new int[]{maxA.getShape()[0]});
                Array.arraycopy(minA, 0, res, 0, minA.getShape()[0]);

                final NormalizationFilter nf = new NormalizationFilter(
                        "Max-Min", false, true);
                // nf.configure(ArrayFactory.getConfiguration());
                final Array[] maxas = nf.apply(new Array[]{maxA});
                final Array[] resas = nf.apply(new Array[]{res});
                final Array diff = ArrayTools.diff(maxas[0], resas[0]);
                final Array powdiff = ArrayTools.pow(diff, 2.0d);
                final double RMSE = Math.sqrt((ArrayTools.integrate(powdiff) / (powdiff.
                        getShape()[0])));
                log.info("Root Mean Square Error={}", RMSE);
                // maxA = as[0];
                if (maxA.equals(values[0])) {
                } else {
                    ArrayTools.mult(diff, -1.0d);
                }
                final AChart<XYPlot> xyc2 = new XYChart("Residual plot of "
                        + lhs.getName() + " versus " + rhs.getName(),
                        new String[]{"Residual of " + lhs.getName()
                            + " versus " + rhs.getName()},
                        new Array[]{diff}, domains, this.xAxisLabel,
                        this.yAxisLabel);
                final PlotRunner pr2 = new PlotRunner(xyc2.create(),
                        "Residual Plot of " + this.variableName
                        + " from files " + lhs.getName() + " and "
                        + rhs.getName(), "residualChart-"
                        + lhs.getName() + "-" + rhs.getName(),
                        getWorkflow().getOutputDirectory(this));
                pr2.configure(Factory.getInstance().getConfiguration());
                final File f = pr2.getFile();
                try {
                    pr2.call();
                } catch (Exception ex) {
                    log.error(ex.getLocalizedMessage());
                }
                final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f,
                        this, WorkflowSlot.VISUALIZATION, new IFileFragment[]{
                            lhs, rhs});
                getWorkflow().append(dwr);
            } else {
                throw new IllegalArgumentException(lhs.getUri()
                        + " has no child " + this.variableName);
            }

        }
    }
}
