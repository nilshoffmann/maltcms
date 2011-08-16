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
 * $Id: Array1DVisualizer.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.commands.fragments.visualization;

import java.io.File;
import java.util.List;

import maltcms.commands.filters.array.AdditionFilter;
import maltcms.commands.filters.array.NormalizationFilter;
import maltcms.tools.ArrayTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYChart;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.plot.XYPlot;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.MAMath;
import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;

/**
 * Plot 1-dimensional arrays, possibly with an additional array providing domain
 * values.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Array1DVisualizer extends AFragmentCommand {

    private String variableName = "total_intensity";
    private String scanAcquisitionTimeVariableName = "scan_acquisition_time";
    @Configurable(name = "maltcms.commands.fragments.visualization.x_axis_label")
    private String xAxisLabel = "Scans";
    @Configurable(name = "maltcms.commands.fragments.visualization.y_axis_label")
    private String yAxisLabel = "Counts";
    private boolean pairwise = false;
    private boolean pairwiseWithFirst = true;
    private boolean substractStartTime = true;
    private boolean allInOneChart = false;
    private String timeUnit = "s";
    private final Logger log = Logging.getLogger(this);

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
			        new Object[] { ff.getName(), (i + 1), t.getSize() });
			labels[i] = ff.getAbsolutePath();
			final IVariableFragment ivf = ff.getChild(this.variableName);
			values[i] = ivf.getArray();
			log.debug("Parent of {}:{}", ivf, ivf.getParent());

			if (!this.allInOneChart) {
				try {
					domains[i] = ff.getChild(
					        this.scanAcquisitionTimeVariableName).getArray()
					        .copy();
					if (this.timeUnit.equals("min")) {
						domains[i] = ArrayTools.divBy60(domains[i]);
					} else if (this.timeUnit.equals("h")) {
						domains[i] = ArrayTools.divBy60(ArrayTools
						        .divBy60(domains[i]));
					}
					log.debug("Using scan acquisition time0 {}",
					        domains[i]);

					this.xAxisLabel = "time [" + this.timeUnit + "]";
				} catch (final ResourceNotAvailableException re) {
					log
					        .info(
					                "Could not load resource {} for domain axis, falling back to scan index domain!",
					                this.scanAcquisitionTimeVariableName);
					domains[i] = ArrayTools.indexArray(values[i].getShape()[0],
					        0);
				}
				final AChart<XYPlot> xyc = new XYChart("1D Visualization of "
				        + this.variableName, new String[] { labels[i] },
				        new Array[] { values[i] }, new Array[] { domains[i] },
				        this.xAxisLabel, this.yAxisLabel);
				final PlotRunner pr = new PlotRunner(xyc.create(), "Plot of "
				        + this.variableName, ff.getName() + ">"
				        + this.variableName, getWorkflow().getOutputDirectory(
				        this));
				pr.configure(Factory.getInstance().getConfiguration());
				final File f = pr.getFile();
				final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f,
				        this, getWorkflowSlot(), ff);
				getWorkflow().append(dwr);
				Factory.getInstance().submitJob(pr);
			}
			i++;
		}
		if (this.allInOneChart) {
			final AChart<XYPlot> xyc = new XYChart("1D Visualization of "
			        + this.variableName, labels, values, domains,
			        this.xAxisLabel, this.yAxisLabel);
			final PlotRunner pr = new PlotRunner(xyc.create(), "Plot of "
			        + this.variableName, this.variableName, getWorkflow()
			        .getOutputDirectory(this));
			pr.configure(Factory.getInstance().getConfiguration());
			final File f = pr.getFile();
			final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f,
			        this, getWorkflowSlot(), t.toArray(new IFileFragment[] {}));
			getWorkflow().append(dwr);
			Factory.getInstance().submitJob(pr);
		}
		return t;
	}

    @Override
    public void configure(final Configuration cfg) {
        this.variableName = cfg.getString(this.getClass().getName()
                + ".variable", "total_intensity");
        this.xAxisLabel = cfg.getString(this.getClass().getName()
                + ".x_axis_label", "domain");
        this.yAxisLabel = cfg.getString(this.getClass().getName()
                + ".y_axis_label", "values");
        this.scanAcquisitionTimeVariableName = cfg.getString(
                "var.scan_acquisition_time", "scan_acquisition_time");
        this.allInOneChart = cfg.getBoolean(this.getClass().getName()
                + ".allInOneChart", false);
        this.substractStartTime = cfg.getBoolean(this.getClass().getName()
                + ".substract_start_time", true);
        this.timeUnit = cfg.getString(this.getClass().getName() + ".timeUnit",
                "min");
    }

    @Override
    public String getDescription() {
        return "Creates plots of 1-dimensional variables.";
    }

    /**
     * @return the scanAcquisitionTimeVariableName
     */
    public String getScanAcquisitionTimeVariableName() {
        return this.scanAcquisitionTimeVariableName;
    }

    /**
     * @return the timeUnit
     */
    public String getTimeUnit() {
        return this.timeUnit;
    }

    /**
     * @return the variableName
     */
    public String getVariableName() {
        return this.variableName;
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

    /**
     * @return the xAxisLabel
     */
    public String getXAxisLabel() {
        return this.xAxisLabel;
    }

    /**
     * @return the yAxisLabel
     */
    public String getYAxisLabel() {
        return this.yAxisLabel;
    }

    /**
     * @return the allInOneChart
     */
    public boolean isAllInOneChart() {
        return this.allInOneChart;
    }

    /**
     * @return the pairwise
     */
    public boolean isPairwise() {
        return this.pairwise;
    }

    /**
     * @return the pairwiseWithFirst
     */
    public boolean isPairwiseWithFirst() {
        return this.pairwiseWithFirst;
    }

    /**
     * @return the substractStartTime
     */
    public boolean isSubstractStartTime() {
        return this.substractStartTime;
    }

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
            labels[0] = lhs.getAbsolutePath();
            labels[1] = rhs.getAbsolutePath();
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
                    if (this.timeUnit.equals("min")) {
                        domains[0] = ArrayTools.divBy60(domains[0]);
                        domains[1] = ArrayTools.divBy60(domains[1]);
                    } else if (this.timeUnit.equals("h")) {
                        domains[0] = ArrayTools.divBy60(ArrayTools.divBy60(
                                domains[0]));
                        domains[1] = ArrayTools.divBy60(ArrayTools.divBy60(
                                domains[1]));
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
                final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f,
                        this, getWorkflowSlot(),
                        new IFileFragment[]{lhs, rhs});
                getWorkflow().append(dwr);
                Factory.getInstance().submitJob(pr);
                pairs++;
            } else {
                throw new IllegalArgumentException(lhs.getAbsolutePath()
                        + " has no child " + this.variableName);
            }
            // }

        }
    }

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
            labels[0] = lhs.getAbsolutePath();
            labels[1] = rhs.getAbsolutePath();
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
                final Array res = Array.factory(maxA.getElementType(),
                        new int[]{maxA.getShape()[0]});
                Array.arraycopy(minA, 0, res, 0, minA.getShape()[0]);

				final NormalizationFilter nf = new NormalizationFilter(
				        "Max-Min", false, true);
				// nf.configure(ArrayFactory.getConfiguration());
				final Array[] maxas = nf.apply(new Array[] { maxA });
				final Array[] resas = nf.apply(new Array[] { res });
				final Array diff = ArrayTools.diff(maxas[0], resas[0]);
				final Array powdiff = ArrayTools.pow(diff, 2.0d);
				final double RMSE = Math
				        .sqrt((ArrayTools.integrate(powdiff) / (powdiff
				                .getShape()[0])));
				log.info("Root Mean Square Error={}", RMSE);
				// maxA = as[0];
				if (maxA.equals(values[0])) {

        }
    }

    /**
     * @param allInOneChart
     *            the allInOneChart to set
     */
    public void setAllInOneChart(final boolean allInOneChart) {
        this.allInOneChart = allInOneChart;
    }

    /**
     * @param pairwise
     *            the pairwise to set
     */
    public void setPairwise(final boolean pairwise) {
        this.pairwise = pairwise;
    }

    /**
     * @param pairwiseWithFirst
     *            the pairwiseWithFirst to set
     */
    public void setPairwiseWithFirst(final boolean pairwiseWithFirst) {
        this.pairwiseWithFirst = pairwiseWithFirst;
    }

    /**
     * @param scanAcquisitionTimeVariableName
     *            the scanAcquisitionTimeVariableName to set
     */
    public void setScanAcquisitionTimeVariableName(
            final String scanAcquisitionTimeVariableName) {
        this.scanAcquisitionTimeVariableName = scanAcquisitionTimeVariableName;
    }

    // protected void orbit(TupleND<FileFragment> t) {
    // for (FileFragment ff : t) {
    // Array a = ff.getChild(variableName).getArray();
    // }
    // }
    /**
     * @param substractStartTime
     *            the substractStartTime to set
     */
    public void setSubstractStartTime(final boolean substractStartTime) {
        this.substractStartTime = substractStartTime;
    }

    /**
     * @param timeUnit
     *            the timeUnit to set
     */
    public void setTimeUnit(final String timeUnit) {
        this.timeUnit = timeUnit;
    }

    /**
     * @param variableName
     *            the variableName to set
     */
    public void setVariableName(final String variableName) {
        this.variableName = variableName;
    }

    /**
     * @param axisLabel
     *            the xAxisLabel to set
     */
    public void setXAxisLabel(final String axisLabel) {
        this.xAxisLabel = axisLabel;
    }

    /**
     * @param axisLabel
     *            the yAxisLabel to set
     */
    public void setYAxisLabel(final String axisLabel) {
        this.yAxisLabel = axisLabel;
    }
}
