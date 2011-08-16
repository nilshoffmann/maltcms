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
 * $Id: PairwiseAlignmentVisualizer.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.commands.fragments.visualization;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import maltcms.commands.filters.array.AdditionFilter;
import maltcms.commands.filters.array.NormalizationFilter;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.AlignmentMapChart;
import maltcms.ui.charts.CombinedDomainXYChart;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYChart;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.TextAnchor;
import java.util.Arrays;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import cross.Factory;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FragmentTools;
import cross.tools.StringTools;

/**
 * Plots a pairwise alignment, by displaying both time series' tics, connected
 * by lines depicting the path obtained from alignment.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class PairwiseAlignmentVisualizer extends AFragmentCommand {

    private final Logger log = Logging.getLogger(this.getClass());
    protected boolean showMZs = false;
    private boolean normalize = false;
    private boolean normalize_global = false;
    private int mapheight = 100;
    private String total_intensity = "total_intensity";
    private String scan_acquisition_time = "scan_acquisition_time";
    private BufferedImage ima;
    private BufferedImage imb;
    private BufferedImage map;
    private boolean substract_start_time = true;
    private final JPanel jp = new JPanel();
    private boolean pairsWithFirstElement;
    private boolean showChromatogramHeatmap = false;
    private int chromheight = 100;
    private String timeUnit = "min";
    private boolean createMapTICChart = true;
    private boolean createComparativeTICChart = true;
    private boolean createDifferentialTICChart = true;
    private boolean createRatioTICChart = true;
    private boolean createSuperimposedTICChart = true;
    private String y_axis_label = "TIC";

    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        final IFileFragment iff = MaltcmsTools.getPairwiseDistanceFragment(t);
        TupleND<IFileFragment> pwdt = MaltcmsTools.getPairwiseAlignments(iff);
        // if (iff == null) {// No alignment has been calculated in this run
        // for (final IFileFragment ifrg : t) {
        // EvalTools.notNull(ifrg, this);
        // final IFileFragment ref = FragmentTools.getLHSFile(ifrg);
        // final IFileFragment query = FragmentTools.getRHSFile(ifrg);
        // this.log.info(ref.toString());
        // this.log.info(query.toString());
        // setFragments(ref, query, ifrg);
        // }
        // } else {
        // pwdt = MaltcmsTools.getPairwiseDistanceFragments(iff);
        for (final IFileFragment ff : pwdt) {
            final IFileFragment ref = FragmentTools.getLHSFile(ff);
            final IFileFragment query = FragmentTools.getRHSFile(ff);
            this.log.info(ref.toString());
            this.log.info(query.toString());
            setFragments(ref, query, ff);
        }
        // }

        return t;
    }

    private void calcRMSE(final List<Tuple2DI> map1, final IFileFragment ref,
            final IFileFragment query) {
        final double RMSE = ArrayTools.rootMeanSquareError(map1,
                NormalizationFilter.normalizeGlobal(MaltcmsTools.getBinnedMZIs(
                ref).getSecond()),
                NormalizationFilter.normalizeGlobal(MaltcmsTools.getBinnedMZIs(
                query).getSecond()));
        this.log.info("RMSE of full chromatograms={}", RMSE);
    }

    @Override
    public void configure(final Configuration cfg) {
        this.total_intensity = cfg.getString("var.total_intensity",
                "total_intensity");
        this.y_axis_label = cfg.getString(this.getClass().getName()
                + ".y_axis_label", "TIC");
        this.scan_acquisition_time = cfg.getString("var.scan_acquisition_time",
                "scan_acquisition_time");
        this.substract_start_time = cfg.getBoolean(this.getClass().getName()
                + ".substract_start_time", true);
        this.mapheight = cfg.getInt(this.getClass().getName() + ".mapheight",
                100);
        this.chromheight = cfg.getInt(this.getClass().getName()
                + ".chromheight", 100);
        this.normalize = cfg.getBoolean(this.getClass().getName()
                + ".normalize", false);
        this.normalize_global = cfg.getBoolean(this.getClass().getName()
                + ".normalize_global", false);
        this.pairsWithFirstElement = cfg.getBoolean(
                "maltcms.commands.fragments.PairwiseDistanceCalculator"
                + ".pairsWithFirstElement", true);
        this.showChromatogramHeatmap = cfg.getBoolean(this.getClass().getName()
                + ".showChromatogramHeatmap");
        this.timeUnit = cfg.getString(this.getClass().getName() + ".timeUnit",
                "min");
        this.createMapTICChart = cfg.getBoolean(this.getClass().getName()
                + ".createMapTICChart", true);
        this.createComparativeTICChart = cfg.getBoolean(this.getClass().getName()
                + ".createComparativeTICChart", true);
        this.createDifferentialTICChart = cfg.getBoolean(this.getClass().getName()
                + ".createDifferentialTICChart", true);
        this.createRatioTICChart = cfg.getBoolean(this.getClass().getName()
                + ".createRatioTICChart", true);
        this.createSuperimposedTICChart = cfg.getBoolean(this.getClass().getName()
                + ".createSuperimposedTICChart", true);
    }

    public BufferedImage createMapImage(final List<Tuple2DI> l,
            final int upper_width, final int lower_width, final int height) {
        this.log.info("Creating map visualization!");
        final BufferedImage bim = new BufferedImage(Math.max(upper_width,
                lower_width), height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = bim.createGraphics();
        // g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.
        // VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, bim.getWidth(), bim.getHeight());
        g2.setColor(Color.BLACK);
        final int mod = 1;
        int cnt = 0;
        final Composite c = g2.getComposite();
        final AlphaComposite ac = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 0.2f);
        g2.setComposite(ac);
        for (final Tuple2DI t : l) {
            if ((cnt % mod == 0) || (cnt == 0) || (cnt == (l.size() - 1))) {
                g2.drawLine(t.getFirst(), 0, t.getSecond(), bim.getHeight());
            }
            cnt++;
        }
        g2.setComposite(c);
        g2.dispose();
        return bim;
    }

    public BufferedImage createSuperimposedImage(final List<Tuple2DI> l,
            final Array lhs, final Array rhs, final int height) {
        this.log.info("Creating superimposed image!");
        final BufferedImage bi = new BufferedImage(l.size(), height,
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = bi.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        g2.setColor(Color.BLACK);
        // int mod = 1;
        int cnt = 0;
        final double lmax = MAMath.getMaximum(lhs);
        final double lmin = MAMath.getMinimum(lhs);
        final double rmax = MAMath.getMaximum(rhs);
        final double rmin = MAMath.getMinimum(rhs);
        this.log.info("lmax={},lmin={},rmax={},rmin={}", new Object[]{lmax,
                    lmin, rmax, rmin});
        final Composite c = g2.getComposite();
        final AlphaComposite ac = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 0.75f);
        g2.setComposite(ac);
        final Index lhsi = lhs.getIndex();
        final Index rhsi = rhs.getIndex();
        final double lnorm = lmax - lmin;
        final double rnorm = rmax - rmin;
        for (final Tuple2DI t : l) {
            // if (cnt % mod == 0 || cnt == 0 || cnt == (l.size()-1)) {
            lhsi.set(t.getFirst());
            rhsi.set(t.getSecond());
            g2.setColor(Color.BLUE);
            final double rheight = height * (rhs.getDouble(rhsi) / rnorm);
            // log.info("First line from {},{} to {},{}",new
            // Object[]{t.getFirst(), 0, t.getSecond(),
            // (int)((double)height*(double)rhs.getInt(rhsi)/(rmax-rmin))});
            this.log.debug("Absolute height: {}; Relative height: {}",
                    new Object[]{rhs.getDouble(rhsi), rheight});
            g2.drawLine(cnt, height, cnt, (int) Math.floor((height) - rheight));
            g2.setColor(Color.RED);
            final double lheight = height * (lhs.getDouble(lhsi) / lnorm);
            // log.info("Second line from {},{} to {},{}",new
            // Object[]{t.getFirst(), 0, t.getSecond(),
            // (int)((double)height*(double)lhs.getInt(lhsi)/(lmax-lmin))});
            this.log.debug("Absolute height: {}; Relative height: {}",
                    new Object[]{lhs.getDouble(lhsi), lheight});
            g2.drawLine(cnt, height, cnt, (int) Math.floor((height) - lheight));
            // }
            cnt++;
        }
        g2.setComposite(c);
        g2.dispose();
        return bi;
    }

    public Collection<XYAnnotation> getAnnotations(final IFileFragment iff,
            final Array domain, final Array yVals) {
        final ArrayList<XYAnnotation> al = new ArrayList<XYAnnotation>();
        final MinMax mm = MAMath.getMinMax(yVals);
        final String anchorNamesVar = Factory.getInstance().getConfiguration().
                getString("var.anchors.retention_index_names",
                "retention_index_names");
        final String anchorScansVar = Factory.getInstance().getConfiguration().
                getString("var.anchors.retention_scans", "retention_scans");
        final IVariableFragment asv = iff.getChild(anchorScansVar);
        final Collection<String> cnames = FragmentTools.getStringArray(iff,
                anchorNamesVar);
        final Array a = asv.getArray();
        final Index domIndex = domain.getIndex();
        final Index yValsI = yVals.getIndex();
        final IndexIterator ii = a.getIndexIterator();
        for (final String s : cnames) {
            final int v = ii.getIntNext();
            final XYPointerAnnotation xyp = new XYPointerAnnotation(
                    s
                    + " "
                    + String.format("%1.2f", domain.getDouble(domIndex.set(v))),
                    domain.getDouble(domIndex.set(v)), yVals.getDouble(yValsI.
                    set(v))
                    / (mm.max - mm.min) + (0.01 * (mm.max - mm.min)),
                    -0.25);
            xyp.setTextAnchor(TextAnchor.CENTER_LEFT);
            xyp.setTipRadius(0.01);
            al.add(xyp);
        }
        return al;
    }

    @Override
    public String getDescription() {
        return "Creates different plots for pairwise alignments of chromatograms.";
    }

    public JPanel getJPanel() {
        return this.jp;
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

    public void makeComparativeTICChart(final AChart<XYPlot> tc1,
            final AChart<XYPlot> tc2, final IFileFragment ref,
            final IFileFragment query, final String x_label,
            final String y_label, final Collection<XYAnnotation> c1,
            final Collection<XYAnnotation> c2, final double minY,
            final double maxY) {
        EvalTools.notNull(new Object[]{ref, query}, this);
        final List<XYPlot> l = new ArrayList<XYPlot>();
        final XYPlot xyp1 = tc1.create();
        for (final XYAnnotation xya : c1) {
            xyp1.addAnnotation(xya);
        }
        xyp1.setWeight(this.chromheight);
        l.add(xyp1);
        final XYPlot xyp2 = tc2.create();
        xyp2.getRangeAxis().setInverted(true);
        xyp2.setWeight(this.chromheight);
        for (final XYAnnotation xya : c2) {
            xyp2.addAnnotation(xya);
        }
        l.add(xyp2);
        final CombinedDomainXYChart cdxy = new CombinedDomainXYChart(
                "Comparative plot of alignment of " + ref.getName() + " with "
                + query.getName(), x_label, true, l);
        cdxy.setGap(0);
        final PlotRunner pl = new PlotRunner(cdxy.create(), "Alignment of "
                + StringTools.removeFileExt(ref.getName()) + " and "
                + StringTools.removeFileExt(query.getName()),
                "alignmentComparativeChart_"
                + StringTools.removeFileExt(ref.getName()) + "-vs-"
                + StringTools.removeFileExt(query.getName()),
                getWorkflow().getOutputDirectory(this));
        pl.configure(Factory.getInstance().getConfiguration());
        final File file = pl.getFile();
        final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(file,
                this, WorkflowSlot.VISUALIZATION, new IFileFragment[]{ref,
                    query});
        getWorkflow().append(dwr2);
        Factory.getInstance().submitJob(pl);
    }

    public void makeDifferentialTICChart(final List<Tuple2DI> map1,
            final IFileFragment ref, final IFileFragment query,
            final Array lhs, final Array rhs, final Array lhs_domain,
            final String x_label, final String y_label,
            final Collection<XYAnnotation> c1, final Collection<XYAnnotation> c2) {
        EvalTools.notNull(new Object[]{lhs, rhs, ref, query}, this);
        final Array rhsm = ArrayTools.projectToLHS(lhs, map1, rhs, true);

        final Array rhsmcopy = rhsm.copy();
        final Array lhsmcopy = lhs.copy();
        EvalTools.notNull(rhsmcopy, "RHS copy is null", this);
        EvalTools.notNull(lhsmcopy, "LHS copy is null", this);
        // NormalizationFilter nf = new NormalizationFilter("Max-Min", false,
        // true);
        // nf.configure(ArrayFactory.getConfiguration());
        // Array[] maxas = nf.apply(new Array[] { lhsmcopy,rhsmcopy });
        final Array diff = ArrayTools.diff(lhsmcopy, rhsmcopy);

        final AChart<XYPlot> xy1 = new XYChart(
                "Differential (LHS-RHS) plot of alignment of"
                + StringTools.removeFileExt(ref.getName()) + " and "
                + StringTools.removeFileExt(query.getName()),
                new String[]{ref.getName() + " - " + query.getName()},
                new Array[]{diff}, new Array[]{lhs_domain}, x_label,
                y_label);
        final XYPlot p1 = xy1.create();
        for (final XYAnnotation xya : c1) {
            p1.addAnnotation(xya);
        }
        final PlotRunner pl = new PlotRunner(p1,
                "Differential (LHS-RHS) plot of alignment of "
                + StringTools.removeFileExt(ref.getName()) + " and "
                + StringTools.removeFileExt(query.getName()),
                "alignmentDifferentialChart_"
                + StringTools.removeFileExt(ref.getName()) + "-vs-"
                + StringTools.removeFileExt(query.getName()),
                getWorkflow().getOutputDirectory(this));
        pl.configure(Factory.getInstance().getConfiguration());
        final File file = pl.getFile();
        final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(file,
                this, WorkflowSlot.VISUALIZATION, ref, query);
        getWorkflow().append(dwr2);
        Factory.getInstance().submitJob(pl);
    }

    public void makeMapChart(final AChart<XYPlot> tc1,
            final AChart<XYPlot> tc2, final IFileFragment alignment,
            final Array tc1Domain, final Array tc2Domain, final String x_label,
            final IFileFragment filea1, final IFileFragment fileb1,
            final Collection<XYAnnotation> c1, final Collection<XYAnnotation> c2) {
        final AlignmentMapChart alC = new AlignmentMapChart(MaltcmsTools.
                getWarpPath(alignment), tc1Domain, tc2Domain, x_label,
                this.mapheight, 1);
        final ArrayList<XYPlot> al = new ArrayList<XYPlot>();
        final XYPlot bottomplt = tc2.create();
        bottomplt.getRangeAxis().setInverted(true);
        for (final XYAnnotation xya : c2) {
            if (xya instanceof XYPointerAnnotation) {
                ((XYPointerAnnotation) xya).setAngle(((XYPointerAnnotation) xya).
                        getAngle() * (-1));
            }
            bottomplt.addAnnotation(xya);
        }
        bottomplt.setWeight(this.chromheight);
        final XYPlot mapplt = alC.create();
        mapplt.setWeight(this.mapheight);
        mapplt.setForegroundAlpha(0.2f);
        final XYPlot topplt = tc1.create();
        for (final XYAnnotation xya : c1) {
            topplt.addAnnotation(xya);
        }
        topplt.setWeight(this.chromheight);
        EvalTools.notNull(new Object[]{topplt, mapplt, bottomplt}, this);
        al.add(topplt);
        al.add(mapplt);
        al.add(bottomplt);
        final CombinedDomainXYChart cdxyc = new CombinedDomainXYChart("Map",
                x_label, true, al);
        cdxyc.configure(Factory.getInstance().getConfiguration());
        cdxyc.setGap(0);
        final PlotRunner pl = new PlotRunner(cdxyc.create(),
                "Alignment Map of "
                + StringTools.removeFileExt(filea1.getName()) + " and "
                + StringTools.removeFileExt(fileb1.getName()),
                "alignmentMapChart_"
                + StringTools.removeFileExt(filea1.getName()) + "-vs-"
                + StringTools.removeFileExt(fileb1.getName()),
                getWorkflow().getOutputDirectory(this));
        pl.configure(Factory.getInstance().getConfiguration());
        final File f = pl.getFile();
        final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f, this,
                WorkflowSlot.VISUALIZATION, alignment, filea1, fileb1);
        getWorkflow().append(dwr);
        Factory.getInstance().submitJob(pl);
    }

    public void makeRatioTICChart(final List<Tuple2DI> map1,
            final IFileFragment ref, final IFileFragment query,
            final Array lhs, final Array rhs, final Array lhs_domain,
            final String x_label, final String y_label,
            final Collection<XYAnnotation> c1, final Collection<XYAnnotation> c2) {
        EvalTools.notNull(new Object[]{lhs, rhs, ref, query}, this);
        final Array rhsm = ArrayTools.projectToLHS(lhs, map1, rhs, true);

        final Array rhsmcopy = rhsm.copy();
        final Array lhsmcopy = lhs.copy();
        EvalTools.notNull(rhsmcopy, "RHS copy is null", this);
        EvalTools.notNull(lhsmcopy, "LHS copy is null", this);
        // NormalizationFilter nf = new NormalizationFilter("Max-Min", false,
        // true);
        // nf.configure(ArrayFactory.getConfiguration());
        // Array[] maxas = nf.apply(new Array[] { lhsmcopy,rhsmcopy });
        final Array diff = ArrayTools.div(lhsmcopy, rhsmcopy);

        final AChart<XYPlot> xy1 = new XYChart(
                "Ratio (LHS/RHS) plot of alignment of"
                + StringTools.removeFileExt(ref.getName()) + " and "
                + StringTools.removeFileExt(query.getName()),
                new String[]{ref.getName() + " - " + query.getName()},
                new Array[]{diff}, new Array[]{lhs_domain}, x_label,
                y_label);
        final XYPlot p1 = xy1.create();
        for (final XYAnnotation xya : c1) {
            p1.addAnnotation(xya);
        }
        final PlotRunner pl = new PlotRunner(p1,
                "Ratio (LHS/RHS) plot of alignment of "
                + StringTools.removeFileExt(ref.getName()) + " and "
                + StringTools.removeFileExt(query.getName()),
                "alignmentRatioChart_"
                + StringTools.removeFileExt(ref.getName()) + "-vs-"
                + StringTools.removeFileExt(query.getName()),
                getWorkflow().getOutputDirectory(this));
        pl.configure(Factory.getInstance().getConfiguration());
        final File file = pl.getFile();
        final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(file,
                this, WorkflowSlot.VISUALIZATION, ref, query);
        getWorkflow().append(dwr2);
        Factory.getInstance().submitJob(pl);
    }

    public void makeSuperimposedChart(final List<Tuple2DI> map1,
            final IFileFragment ref, final IFileFragment query,
            final Array lhs, final Array rhs, final Array lhs_domain,
            final String x_label, final String y_label,
            final Collection<XYAnnotation> c1, final Collection<XYAnnotation> c2) {
        EvalTools.notNull(new Object[]{lhs, rhs, ref, query}, this);
        final Array rhsm = ArrayTools.projectToLHS(lhs, map1, rhs, true);

        final Array rhsmcopy = rhsm.copy();
        final Array lhsmcopy = lhs.copy();
        EvalTools.notNull(rhsmcopy, "RHS copy is null", this);
        EvalTools.notNull(lhsmcopy, "LHS copy is null", this);

        // Array[] resas = nf.apply(new Array[] { rhsmcopy });
        final Array diff = ArrayTools.diff(lhsmcopy, rhsmcopy);
        final Array powdiff = ArrayTools.pow(diff, 2.0d);
        final double TICRMSE = Math.sqrt((ArrayTools.integrate(powdiff) / map1.
                size()));
        this.log.info("TICRoot Mean Square Error={}", TICRMSE);
        this.log.info(ref.toString());
        this.log.info(query.toString());
        this.log.debug("Shapes of arrays:ref {}, mapped {}",
                Arrays.toString(lhs.getShape()), Arrays.toString(rhs.getShape()));
        final AChart<XYPlot> xyc = new XYChart(
                "Superimposition of alignment of "
                + StringTools.removeFileExt(ref.getName()) + " with "
                + StringTools.removeFileExt(query.getName()),
                new String[]{ref.getAbsolutePath(), query.getAbsolutePath()},
                new Array[]{lhsmcopy, rhsmcopy}, new Array[]{lhs_domain},
                x_label, y_label);
        final XYPlot p = xyc.create();
        for (final XYAnnotation xya : c1) {
            p.addAnnotation(xya);
        }
        final PlotRunner pl = new PlotRunner(p, "Alignment of "
                + StringTools.removeFileExt(ref.getName()) + " and "
                + StringTools.removeFileExt(query.getName()),
                "alignmentSuperimpositionChart_"
                + StringTools.removeFileExt(ref.getName()) + "-vs-"
                + StringTools.removeFileExt(query.getName()),
                getWorkflow().getOutputDirectory(this));
        pl.configure(Factory.getInstance().getConfiguration());
        final File file = pl.getFile();
        final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(file,
                this, WorkflowSlot.VISUALIZATION, ref, query);
        getWorkflow().append(dwr2);
        Factory.getInstance().submitJob(pl);
    }

	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		final IFileFragment iff = MaltcmsTools.getPairwiseDistanceFragment(t);
		TupleND<IFileFragment> pwdt = MaltcmsTools.getPairwiseAlignments(iff);
		// if (iff == null) {// No alignment has been calculated in this run
		// for (final IFileFragment ifrg : t) {
		// EvalTools.notNull(ifrg, this);
		// final IFileFragment ref = FragmentTools.getLHSFile(ifrg);
		// final IFileFragment query = FragmentTools.getRHSFile(ifrg);
		// log.info(ref.toString());
		// log.info(query.toString());
		// setFragments(ref, query, ifrg);
		// }
		// } else {
		// pwdt = MaltcmsTools.getPairwiseDistanceFragments(iff);
		for (final IFileFragment ff : pwdt) {
			final IFileFragment ref = FragmentTools.getLHSFile(ff);
			final IFileFragment query = FragmentTools.getRHSFile(ff);
			log.info(ref.toString());
			log.info(query.toString());
			setFragments(ref, query, ff);
		}
		// }

    public void setFragments(final IFileFragment filea1,
            final IFileFragment fileb1, final IFileFragment alignment) {
        this.log.info("Preparing variables to be read!");
        this.log.info("Source files of {}", filea1);
        for (final IFileFragment iff : filea1.getSourceFiles()) {
            this.log.info("{}", iff);
        }
        this.log.info("Source files of {}", fileb1);
        for (final IFileFragment iff : fileb1.getSourceFiles()) {
            this.log.info("{}", iff);
        }
        // FragmentTools.loadDefaultVars(filea1, "default.vars");
        // FragmentTools.loadDefaultVars(fileb1, "default.vars");
        Array a1 = filea1.getChild(this.total_intensity).getArray().copy();
        Array b1 = fileb1.getChild(this.total_intensity).getArray().copy();

	private void calcRMSE(final List<Tuple2DI> map1, final IFileFragment ref,
	        final IFileFragment query) {
		final double RMSE = ArrayTools.rootMeanSquareError(map1, NormalizationFilter.normalizeGlobal(MaltcmsTools.getBinnedMZIs(ref).getSecond()),
		        NormalizationFilter.normalizeGlobal(MaltcmsTools.getBinnedMZIs(query)
		                .getSecond()));
		log.info("RMSE of full chromatograms={}", RMSE);
	}

            x_label = "time [" + this.timeUnit + "]";
            if (this.substract_start_time) {
                final AdditionFilter af = new AdditionFilter(-Math.min(min,
                        min1));
                final AdditionFilter af1 = new AdditionFilter(-Math.min(min,
                        min1));
                domains[0] = af.apply(new Array[]{domains[0].copy()})[0];
                domains[1] = af1.apply(new Array[]{domains[1].copy()})[0];
            }
        } catch (final ResourceNotAvailableException re) {
            this.log.info(
                    "Could not load resource {} for domain axis, falling back to scan index domain!",
                    this.scan_acquisition_time);
            domains[0] = ArrayTools.indexArray(a1.getShape()[0], 0);
            domains[1] = ArrayTools.indexArray(b1.getShape()[0], 0);
        }

	public BufferedImage createMapImage(final List<Tuple2DI> l,
	        final int upper_width, final int lower_width, final int height) {
		log.info("Creating map visualization!");
		final BufferedImage bim = new BufferedImage(Math.max(upper_width,
		        lower_width), height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = bim.createGraphics();
		// g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.
		// VALUE_ANTIALIAS_ON);
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, bim.getWidth(), bim.getHeight());
		g2.setColor(Color.BLACK);
		final int mod = 1;
		int cnt = 0;
		final Composite c = g2.getComposite();
		final AlphaComposite ac = AlphaComposite.getInstance(
		        AlphaComposite.SRC_OVER, 0.2f);
		g2.setComposite(ac);
		for (final Tuple2DI t : l) {
			if ((cnt % mod == 0) || (cnt == 0) || (cnt == (l.size() - 1))) {
				g2.drawLine(t.getFirst(), 0, t.getSecond(), bim.getHeight());
			}
			cnt++;
		}
		g2.setComposite(c);
		g2.dispose();
		return bim;
	}

	public BufferedImage createSuperimposedImage(final List<Tuple2DI> l,
	        final Array lhs, final Array rhs, final int height) {
		log.info("Creating superimposed image!");
		final BufferedImage bi = new BufferedImage(l.size(), height,
		        BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = bi.createGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, bi.getWidth(), bi.getHeight());
		g2.setColor(Color.BLACK);
		// int mod = 1;
		int cnt = 0;
		final double lmax = MAMath.getMaximum(lhs);
		final double lmin = MAMath.getMinimum(lhs);
		final double rmax = MAMath.getMaximum(rhs);
		final double rmin = MAMath.getMinimum(rhs);
		log.info("lmax={},lmin={},rmax={},rmin={}", new Object[] { lmax,
		        lmin, rmax, rmin });
		final Composite c = g2.getComposite();
		final AlphaComposite ac = AlphaComposite.getInstance(
		        AlphaComposite.SRC_OVER, 0.75f);
		g2.setComposite(ac);
		final Index lhsi = lhs.getIndex();
		final Index rhsi = rhs.getIndex();
		final double lnorm = lmax - lmin;
		final double rnorm = rmax - rmin;
		for (final Tuple2DI t : l) {
			// if (cnt % mod == 0 || cnt == 0 || cnt == (l.size()-1)) {
			lhsi.set(t.getFirst());
			rhsi.set(t.getSecond());
			g2.setColor(Color.BLUE);
			final double rheight = height * (rhs.getDouble(rhsi) / rnorm);
			// log.info("First line from {},{} to {},{}",new
			// Object[]{t.getFirst(), 0, t.getSecond(),
			// (int)((double)height*(double)rhs.getInt(rhsi)/(rmax-rmin))});
			log.debug("Absolute height: {}; Relative height: {}",
			        new Object[] { rhs.getDouble(rhsi), rheight });
			g2.drawLine(cnt, height, cnt, (int) Math.floor((height) - rheight));
			g2.setColor(Color.RED);
			final double lheight = height * (lhs.getDouble(lhsi) / lnorm);
			// log.info("Second line from {},{} to {},{}",new
			// Object[]{t.getFirst(), 0, t.getSecond(),
			// (int)((double)height*(double)lhs.getInt(lhsi)/(lmax-lmin))});
			log.debug("Absolute height: {}; Relative height: {}",
			        new Object[] { lhs.getDouble(lhsi), lheight });
			g2.drawLine(cnt, height, cnt, (int) Math.floor((height) - lheight));
			// }
			cnt++;
		}
		g2.setComposite(c);
		g2.dispose();
		return bi;
	}

        EvalTools.notNull(new Object[]{tc1, tc2}, this);
        final Collection<XYAnnotation> c1 = Collections.emptyList();// getAnnotations
        // (filea1,
        // domains
        // [0],a1c);
        final Collection<XYAnnotation> c2 = Collections.emptyList();// getAnnotations
        // (fileb1,
        // domains
        // [1],b1c);
        if (this.createMapTICChart) {
            makeMapChart(tc1, tc2, alignment, domains[0], domains[1], x_label,
                    filea1, fileb1, c1, c2);
        }
        if (this.createComparativeTICChart) {
            makeComparativeTICChart(tc1, tc2, filea1, fileb1, x_label,
                    this.y_axis_label, c1, c2, min, max);
        }
        if (this.createDifferentialTICChart) {
            makeDifferentialTICChart(MaltcmsTools.getWarpPath(alignment),
                    filea1, fileb1, a1, b1, domains[0], x_label,
                    this.y_axis_label, c1, c2);
        }
        if (this.createRatioTICChart) {
            makeRatioTICChart(MaltcmsTools.getWarpPath(alignment), filea1,
                    fileb1, a1, b1, domains[0], x_label, this.y_axis_label, c1,
                    c2);
        }
        if (this.createSuperimposedTICChart) {
            makeSuperimposedChart(MaltcmsTools.getWarpPath(alignment), filea1,
                    fileb1, a1, b1, domains[0], x_label, this.y_axis_label, c1,
                    c2);
        }

    }

	public JPanel getJPanel() {
		return this.jp;
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

	public void makeComparativeTICChart(final AChart<XYPlot> tc1,
	        final AChart<XYPlot> tc2, final IFileFragment ref,
	        final IFileFragment query, final String x_label,
	        final String y_label, final Collection<XYAnnotation> c1,
	        final Collection<XYAnnotation> c2, final double minY,
	        final double maxY) {
		EvalTools.notNull(new Object[] { ref, query }, this);
		final List<XYPlot> l = new ArrayList<XYPlot>();
		final XYPlot xyp1 = tc1.create();
		for (final XYAnnotation xya : c1) {
			xyp1.addAnnotation(xya);
		}
		xyp1.setWeight(this.chromheight);
		l.add(xyp1);
		final XYPlot xyp2 = tc2.create();
		xyp2.getRangeAxis().setInverted(true);
		xyp2.setWeight(this.chromheight);
		for (final XYAnnotation xya : c2) {
			xyp2.addAnnotation(xya);
		}
		l.add(xyp2);
		final CombinedDomainXYChart cdxy = new CombinedDomainXYChart(
		        "Comparative plot of alignment of " + ref.getName() + " with "
		                + query.getName(), x_label, true, l);
		cdxy.setGap(0);
		final PlotRunner pl = new PlotRunner(cdxy.create(), "Alignment of "
		        + StringTools.removeFileExt(ref.getName()) + " and "
		        + StringTools.removeFileExt(query.getName()),
		        "alignmentComparativeChart_"
		                + StringTools.removeFileExt(ref.getName()) + "-vs-"
		                + StringTools.removeFileExt(query.getName()),
		        getWorkflow().getOutputDirectory(this));
		pl.configure(Factory.getInstance().getConfiguration());
		final File file = pl.getFile();
		final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(file,
		        this, WorkflowSlot.VISUALIZATION, new IFileFragment[] { ref,
		                query });
		getWorkflow().append(dwr2);
		Factory.getInstance().submitJob(pl);
	}

	public void makeDifferentialTICChart(final List<Tuple2DI> map1,
	        final IFileFragment ref, final IFileFragment query,
	        final Array lhs, final Array rhs, final Array lhs_domain,
	        final String x_label, final String y_label,
	        final Collection<XYAnnotation> c1, final Collection<XYAnnotation> c2) {
		EvalTools.notNull(new Object[] { lhs, rhs, ref, query }, this);
		final Array rhsm = ArrayTools.projectToLHS(lhs, map1, rhs, true);

		final Array rhsmcopy = rhsm.copy();
		final Array lhsmcopy = lhs.copy();
		EvalTools.notNull(rhsmcopy, "RHS copy is null", this);
		EvalTools.notNull(lhsmcopy, "LHS copy is null", this);
		// NormalizationFilter nf = new NormalizationFilter("Max-Min", false,
		// true);
		// nf.configure(ArrayFactory.getConfiguration());
		// Array[] maxas = nf.apply(new Array[] { lhsmcopy,rhsmcopy });
		final Array diff = ArrayTools.diff(lhsmcopy, rhsmcopy);

		final AChart<XYPlot> xy1 = new XYChart(
		        "Differential (LHS-RHS) plot of alignment of"
		                + StringTools.removeFileExt(ref.getName()) + " and "
		                + StringTools.removeFileExt(query.getName()),
		        new String[] { ref.getName() + " - " + query.getName() },
		        new Array[] { diff }, new Array[] { lhs_domain }, x_label,
		        y_label);
		final XYPlot p1 = xy1.create();
		for (final XYAnnotation xya : c1) {
			p1.addAnnotation(xya);
		}
		final PlotRunner pl = new PlotRunner(p1,
		        "Differential (LHS-RHS) plot of alignment of "
		                + StringTools.removeFileExt(ref.getName()) + " and "
		                + StringTools.removeFileExt(query.getName()),
		        "alignmentDifferentialChart_"
		                + StringTools.removeFileExt(ref.getName()) + "-vs-"
		                + StringTools.removeFileExt(query.getName()),
		        getWorkflow().getOutputDirectory(this));
		pl.configure(Factory.getInstance().getConfiguration());
		final File file = pl.getFile();
		final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(file,
		        this, WorkflowSlot.VISUALIZATION, ref, query);
		getWorkflow().append(dwr2);
		Factory.getInstance().submitJob(pl);
	}

	public void makeMapChart(final AChart<XYPlot> tc1,
	        final AChart<XYPlot> tc2, final IFileFragment alignment,
	        final Array tc1Domain, final Array tc2Domain, final String x_label,
	        final IFileFragment filea1, final IFileFragment fileb1,
	        final Collection<XYAnnotation> c1, final Collection<XYAnnotation> c2) {
		final AlignmentMapChart alC = new AlignmentMapChart(MaltcmsTools
		        .getWarpPath(alignment), tc1Domain, tc2Domain, x_label,
		        this.mapheight, 1);
		final ArrayList<XYPlot> al = new ArrayList<XYPlot>();
		final XYPlot bottomplt = tc2.create();
		bottomplt.getRangeAxis().setInverted(true);
		for (final XYAnnotation xya : c2) {
			if (xya instanceof XYPointerAnnotation) {
				((XYPointerAnnotation) xya)
				        .setAngle(((XYPointerAnnotation) xya).getAngle() * (-1));
			}
			bottomplt.addAnnotation(xya);
		}
		bottomplt.setWeight(this.chromheight);
		final XYPlot mapplt = alC.create();
		mapplt.setWeight(this.mapheight);
		mapplt.setForegroundAlpha(0.2f);
		final XYPlot topplt = tc1.create();
		for (final XYAnnotation xya : c1) {
			topplt.addAnnotation(xya);
		}
		topplt.setWeight(this.chromheight);
		EvalTools.notNull(new Object[] { topplt, mapplt, bottomplt }, this);
		al.add(topplt);
		al.add(mapplt);
		al.add(bottomplt);
		final CombinedDomainXYChart cdxyc = new CombinedDomainXYChart("Map",
		        x_label, true, al);
		cdxyc.configure(Factory.getInstance().getConfiguration());
		cdxyc.setGap(0);
		final PlotRunner pl = new PlotRunner(cdxyc.create(),
		        "Alignment Map of "
		                + StringTools.removeFileExt(filea1.getName()) + " and "
		                + StringTools.removeFileExt(fileb1.getName()),
		        "alignmentMapChart_"
		                + StringTools.removeFileExt(filea1.getName()) + "-vs-"
		                + StringTools.removeFileExt(fileb1.getName()),
		        getWorkflow().getOutputDirectory(this));
		pl.configure(Factory.getInstance().getConfiguration());
		final File f = pl.getFile();
		final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f, this,
		        WorkflowSlot.VISUALIZATION, alignment, filea1, fileb1);
		getWorkflow().append(dwr);
		Factory.getInstance().submitJob(pl);
	}

	public void makeRatioTICChart(final List<Tuple2DI> map1,
	        final IFileFragment ref, final IFileFragment query,
	        final Array lhs, final Array rhs, final Array lhs_domain,
	        final String x_label, final String y_label,
	        final Collection<XYAnnotation> c1, final Collection<XYAnnotation> c2) {
		EvalTools.notNull(new Object[] { lhs, rhs, ref, query }, this);
		final Array rhsm = ArrayTools.projectToLHS(lhs, map1, rhs, true);

		final Array rhsmcopy = rhsm.copy();
		final Array lhsmcopy = lhs.copy();
		EvalTools.notNull(rhsmcopy, "RHS copy is null", this);
		EvalTools.notNull(lhsmcopy, "LHS copy is null", this);
		// NormalizationFilter nf = new NormalizationFilter("Max-Min", false,
		// true);
		// nf.configure(ArrayFactory.getConfiguration());
		// Array[] maxas = nf.apply(new Array[] { lhsmcopy,rhsmcopy });
		final Array diff = ArrayTools.div(lhsmcopy, rhsmcopy);

		final AChart<XYPlot> xy1 = new XYChart(
		        "Ratio (LHS/RHS) plot of alignment of"
		                + StringTools.removeFileExt(ref.getName()) + " and "
		                + StringTools.removeFileExt(query.getName()),
		        new String[] { ref.getName() + " - " + query.getName() },
		        new Array[] { diff }, new Array[] { lhs_domain }, x_label,
		        y_label);
		final XYPlot p1 = xy1.create();
		for (final XYAnnotation xya : c1) {
			p1.addAnnotation(xya);
		}
		final PlotRunner pl = new PlotRunner(p1,
		        "Ratio (LHS/RHS) plot of alignment of "
		                + StringTools.removeFileExt(ref.getName()) + " and "
		                + StringTools.removeFileExt(query.getName()),
		        "alignmentRatioChart_"
		                + StringTools.removeFileExt(ref.getName()) + "-vs-"
		                + StringTools.removeFileExt(query.getName()),
		        getWorkflow().getOutputDirectory(this));
		pl.configure(Factory.getInstance().getConfiguration());
		final File file = pl.getFile();
		final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(file,
		        this, WorkflowSlot.VISUALIZATION, ref, query);
		getWorkflow().append(dwr2);
		Factory.getInstance().submitJob(pl);
	}

	public void makeSuperimposedChart(final List<Tuple2DI> map1,
	        final IFileFragment ref, final IFileFragment query,
	        final Array lhs, final Array rhs, final Array lhs_domain,
	        final String x_label, final String y_label,
	        final Collection<XYAnnotation> c1, final Collection<XYAnnotation> c2) {
		EvalTools.notNull(new Object[] { lhs, rhs, ref, query }, this);
		final Array rhsm = ArrayTools.projectToLHS(lhs, map1, rhs, true);

		final Array rhsmcopy = rhsm.copy();
		final Array lhsmcopy = lhs.copy();
		EvalTools.notNull(rhsmcopy, "RHS copy is null", this);
		EvalTools.notNull(lhsmcopy, "LHS copy is null", this);

		// Array[] resas = nf.apply(new Array[] { rhsmcopy });
		final Array diff = ArrayTools.diff(lhsmcopy, rhsmcopy);
		final Array powdiff = ArrayTools.pow(diff, 2.0d);
		final double TICRMSE = Math.sqrt((ArrayTools.integrate(powdiff) / map1
		        .size()));
		log.info("TICRoot Mean Square Error={}", TICRMSE);
		log.info(ref.toString());
		log.info(query.toString());
		log.debug("Shapes of arrays:ref {}, mapped {}", Arrays
		        .toString(lhs.getShape()), Arrays.toString(rhs.getShape()));
		final AChart<XYPlot> xyc = new XYChart(
		        "Superimposition of alignment of "
		                + StringTools.removeFileExt(ref.getName()) + " with "
		                + StringTools.removeFileExt(query.getName()),
		        new String[] { ref.getAbsolutePath(), query.getAbsolutePath() },
		        new Array[] { lhsmcopy, rhsmcopy }, new Array[] { lhs_domain },
		        x_label, y_label);
		final XYPlot p = xyc.create();
		for (final XYAnnotation xya : c1) {
			p.addAnnotation(xya);
		}
		final PlotRunner pl = new PlotRunner(p, "Alignment of "
		        + StringTools.removeFileExt(ref.getName()) + " and "
		        + StringTools.removeFileExt(query.getName()),
		        "alignmentSuperimpositionChart_"
		                + StringTools.removeFileExt(ref.getName()) + "-vs-"
		                + StringTools.removeFileExt(query.getName()),
		        getWorkflow().getOutputDirectory(this));
		pl.configure(Factory.getInstance().getConfiguration());
		final File file = pl.getFile();
		final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(file,
		        this, WorkflowSlot.VISUALIZATION, ref, query);
		getWorkflow().append(dwr2);
		Factory.getInstance().submitJob(pl);
	}

	protected void repaint() {
		final Graphics g2 = getJPanel().getGraphics();
		if ((this.ima != null) && (this.imb != null) && (this.map != null)) {
			int yoffset = 0;
			g2.drawImage(this.ima, 0, yoffset, this.ima.getWidth(), this.ima
			        .getHeight(), null);
			yoffset += this.ima.getHeight();
			g2.drawImage(this.map, 0, yoffset, this.map.getWidth(), this.map
			        .getHeight(), null);
			yoffset += this.map.getHeight();
			g2.drawImage(this.imb, 0, yoffset, this.imb.getWidth(), this.imb
			        .getHeight(), null);
			yoffset += this.imb.getHeight();
			this.jp
			        .setPreferredSize(new Dimension(this.map.getWidth(),
			                yoffset));
		}
	}

	public void setFragments(final IFileFragment filea1,
	        final IFileFragment fileb1, final IFileFragment alignment) {
		log.info("Preparing variables to be read!");
		log.info("Source files of {}", filea1);
		for (final IFileFragment iff : filea1.getSourceFiles()) {
			log.info("{}", iff);
		}
		log.info("Source files of {}", fileb1);
		for (final IFileFragment iff : fileb1.getSourceFiles()) {
			log.info("{}", iff);
		}
		// FragmentTools.loadDefaultVars(filea1, "default.vars");
		// FragmentTools.loadDefaultVars(fileb1, "default.vars");
		Array a1 = filea1.getChild(this.total_intensity).getArray().copy();
		Array b1 = fileb1.getChild(this.total_intensity).getArray().copy();

		// boolean drawTIC = true;
		String x_label = "scan number";
		final Array[] domains = new Array[2];
		try {
			domains[0] = filea1.getChild(this.scan_acquisition_time).getArray()
			        .copy();
			domains[1] = fileb1.getChild(this.scan_acquisition_time).getArray()
			        .copy();
			if (this.timeUnit.equals("min")) {
				domains[0] = ArrayTools.divBy60(domains[0]);
				domains[1] = ArrayTools.divBy60(domains[1]);
			} else if (this.timeUnit.equals("h")) {
				domains[0] = ArrayTools.divBy60(ArrayTools.divBy60(domains[0]));
				domains[1] = ArrayTools.divBy60(ArrayTools.divBy60(domains[1]));
			}
			log.debug("Using scan acquisition time0 {}", domains[0]);
			log.debug("Using scan acquisition time1 {}", domains[1]);
			final double min = MAMath.getMinimum(domains[0]);
			final double min1 = MAMath.getMinimum(domains[1]);

			x_label = "time [" + this.timeUnit + "]";
			if (this.substract_start_time) {
				final AdditionFilter af = new AdditionFilter(-Math.min(min,
				        min1));
				final AdditionFilter af1 = new AdditionFilter(-Math.min(min,
				        min1));
				domains[0] = af.apply(new Array[] { domains[0].copy() })[0];
				domains[1] = af1.apply(new Array[] { domains[1].copy() })[0];
			}
		} catch (final ResourceNotAvailableException re) {
			log
			        .info(
			                "Could not load resource {} for domain axis, falling back to scan index domain!",
			                this.scan_acquisition_time);
			domains[0] = ArrayTools.indexArray(a1.getShape()[0], 0);
			domains[1] = ArrayTools.indexArray(b1.getShape()[0], 0);
		}

		EvalTools.notNull(new Object[] { a1, b1 }, this);
		AChart<XYPlot> tc1, tc2;
		// if(drawTIC){
		log.info("Drawing TIC");

		Array[] arrs = null;
		if (this.normalize) {
			final NormalizationFilter nf = new NormalizationFilter("Max-Min",
			        false, this.normalize_global);
			arrs = nf.apply(new Array[] { a1, b1 });
			// b1c = nf.apply(new Array[] { b1c })[0];
			a1 = arrs[0];
			b1 = arrs[1];
		}
		final ArrayStatsScanner ass = new ArrayStatsScanner();
		ass.apply(new Array[] { a1, b1 });
		final StatsMap sm = ass.getGlobalStatsMap();
		final double min = sm.get(Vars.Min.toString());
		final double max = sm.get(Vars.Max.toString());
		// if(showChromatogramHeatmap) {
		// filea1.getChild("intensity_values").setIndex(
		// filea1.getChild("scan_index"));
		// ArrayList<Array> aa =
		// filea1.getChild("intensity_values").getIndexedArray();
		// Array masses = filea1.getChild("mass_values").getArray();
		// ArrayDouble.D1 sat = null;
		// x_label = "scans";
		// try{
		// sat = (ArrayDouble.D1) filea1.getChild(
		// "scan_acquisition_time").getArray();
		// x_label = "time [s]";
		// }catch(ResourceNotAvailableException r){
		// log.info(
		// "Resource {} not available, falling back to scan index as domain axis"
		// ,this.scanAcquisitionTime);
		// sat = ArrayTools.indexArray(aa.size(), 0);
		// }
		// MinMax mm = MAMath.getMinMax(masses);
		// int bins = MaltcmsTools.getNumberOfIntegerMassBins(mm.min, mm.max);
		// ArrayDouble.D1 massAxis = new ArrayDouble.D1(bins);
		// for (int i = 0; i < bins; i++) {
		// massAxis.set(i, mm.min + i);
		// }
		// if (!aa.isEmpty()) {
		// BufferedImage bi = ImageTools.fullSpectrum(fileA.getName(), aa, fileA
		// .getChild("intensity_values").getArray(), bins, colorRamp,
		// sampleSize, this.format, this.getClass(), true);
		// }
		// }else{
		tc1 = new XYChart(filea1.getName(), new String[] { filea1
		        .getAbsolutePath() }, new Array[] { a1 },
		        new Array[] { domains[0] }, x_label, this.y_axis_label);
		tc1.setYaxis_min(min);
		tc1.setYaxis_max(max);
		tc2 = new XYChart(fileb1.getName(), new String[] { fileb1
		        .getAbsolutePath() }, new Array[] { b1 },
		        new Array[] { domains[1] }, x_label, this.y_axis_label);
		tc2.setYaxis_min(min);
		tc2.setYaxis_max(max);
		// }

		EvalTools.notNull(new Object[] { tc1, tc2 }, this);
		final Collection<XYAnnotation> c1 = Collections.emptyList();// getAnnotations
		// (filea1,
		// domains
		// [0],a1c);
		final Collection<XYAnnotation> c2 = Collections.emptyList();// getAnnotations
		// (fileb1,
		// domains
		// [1],b1c);
		if (this.createMapTICChart) {
			makeMapChart(tc1, tc2, alignment, domains[0], domains[1], x_label,
			        filea1, fileb1, c1, c2);
		}
		if (this.createComparativeTICChart) {
			makeComparativeTICChart(tc1, tc2, filea1, fileb1, x_label,
			        this.y_axis_label, c1, c2, min, max);
		}
		if (this.createDifferentialTICChart) {
			makeDifferentialTICChart(MaltcmsTools.getWarpPath(alignment),
			        filea1, fileb1, a1, b1, domains[0], x_label,
			        this.y_axis_label, c1, c2);
		}
		if (this.createRatioTICChart) {
			makeRatioTICChart(MaltcmsTools.getWarpPath(alignment), filea1,
			        fileb1, a1, b1, domains[0], x_label, this.y_axis_label, c1,
			        c2);
		}
		if (this.createSuperimposedTICChart) {
			makeSuperimposedChart(MaltcmsTools.getWarpPath(alignment), filea1,
			        fileb1, a1, b1, domains[0], x_label, this.y_axis_label, c1,
			        c2);
		}

	}

	public void setImages(final BufferedImage a1im, final BufferedImage b1im,
	        final BufferedImage map1) {
		this.ima = a1im;
		this.imb = b1im;
		this.map = map1;
	}
}
