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
 * $Id: PairwiseAlignmentMatrixVisualizer.java 636 2009-02-18 13:42:36Z hoffmann
 * $
 */
package maltcms.commands.fragments.visualization;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.datastructures.alignment.AnchorPairSet;
import maltcms.datastructures.ms.IAnchor;
import maltcms.io.csv.ColorRampReader;
import maltcms.tools.ArrayTools;
import maltcms.tools.ImageTools;
import maltcms.tools.MaltcmsTools;
import maltcms.tools.PathTools;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import cross.Factory;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FragmentTools;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * Draw the pairwise distance and/or cumulative distance matrix used for
 * alignment.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
@Data
@ServiceProvider(service=AFragmentCommand.class)
public class PairwiseAlignmentMatrixVisualizer extends AFragmentCommand {

    Thread t = null;
    private String filename;
    private String format = "png";
    private int chromatogramHeight = 200;
    private String left_chromatogram_var;
    private String top_chromatogram_var;
    private String reference_file_name;
    private String query_file_name;
    private String path_i;
    private String path_j;
    private String mz_var;
    private String inten_var;
    private String scan_index_var;
    private String mass_range_min_var;
    private String mass_range_max_var;
    private boolean full_spec;
    private String colorramp_location = "res/colorRamps/bw.csv";
    private int sampleSize = 1024;
    private boolean pairsWithFirstElement;
    private boolean drawPath;
    private ArrayList<String> matrix_vars;
    private int fontsize = 30;

    protected BufferedImage addSpectra(final String queryName,
            final String refName, final Array query, final Array ref,
            final BufferedImage bi, final int specwidth1,
            final int colorlegendwidth, final int colorlegendheight,
            final int margin, final int[][] colorTable, final boolean minimize,
            final List<Integer> refAnchors, final List<Integer> queryAnchors) {
        log.info("Adding Spectra!");
        log.info("Reference {} with length: {}", refName, ref.getSize());
        log.info("Query {} with length: {}", queryName, query.getSize());
        log.info("TiledImage size: (wxh) " + bi.getWidth() + " x "
                + bi.getHeight());
        final BufferedImage bi_ref = addSpectrumImage(refName, ref, specwidth1,
                Color.RED, Color.WHITE, refAnchors);
        final BufferedImage bi_query = addSpectrumImage(queryName, query,
                specwidth1, Color.BLUE, Color.WHITE, queryAnchors);
        log.info("Reference img length: " + bi_ref.getWidth());
        log.info("Query img length: " + bi_query.getWidth());
        final BufferedImage bout = new BufferedImage(bi.getWidth() + specwidth1
                + colorlegendwidth + (2 * margin) + 1, bi.getHeight()
                + (2 * margin) + specwidth1 + 1, bi.getType());
        final Graphics2D g = (Graphics2D) bout.getGraphics();

        // reset affine transform
        g.setTransform(AffineTransform.getTranslateInstance(0.0d, 0.0d));

        // set affine transform for left side of matrix
        // move to origin
        AffineTransform at = AffineTransform.getTranslateInstance(bi_ref.
                getHeight() / 2.0d, specwidth1 + (bi_ref.getWidth() / 2.0d)
                + 1.0);
        // rotate by 270 degrees
        // at.concatenate(AffineTransform.getRotateInstance(Math
        // .toRadians(-270.0d)));
        at.concatenate(AffineTransform.getScaleInstance(-1, 1));
        at.concatenate(AffineTransform.getRotateInstance(Math.toRadians(90.0d)));

        // move to original position
        at.concatenate(AffineTransform.getTranslateInstance(
                -bi_ref.getWidth() / 2, -bi_ref.getHeight() / 2));
        log.info("Adding {} to the left!", refName);
        // AffineTransform or = g.getTransform();
        g.setColor(Color.BLACK);
        // g.setTransform(at);
        g.fillRect(0, specwidth1, bi_ref.getHeight(), bi_ref.getWidth());
        // g.setTransform(or);
        g.drawImage(bi_ref, at, null);

        // reset affine transform
        g.setTransform(AffineTransform.getTranslateInstance(0.0d, 0.0d));
        // affine transform for top side of matrix
        // move to origin
        at = AffineTransform.getTranslateInstance(specwidth1 + 1.0, 0);
        // at = AffineTransform.getTranslateInstance(specwidth1
        // + (bi_query.getWidth() / 2.0d) + 1.0, bi_query.getHeight() / 2.0d);
        // at.concatenate(AffineTransform.getRotateInstance(Math
        // .toRadians(-180.0d)));
        // at.concatenate(AffineTransform.getScaleInstance(-1.0, 1.0));
        // at.concatenate(AffineTransform.getTranslateInstance(
        // -bi_query.getWidth() / 2.0d, -bi_query.getHeight() / 2.0d));
        log.info("Adding {} on top!", queryName);
        g.drawImage(bi_query, at, null);

        g.setTransform(AffineTransform.getTranslateInstance(0.0d, 0.0d));
        log.info("Filling distance matrix!");
        at = AffineTransform.getTranslateInstance(specwidth1
                + (bi.getWidth() / 2.0d) + 1.0, specwidth1 + bi.getHeight()
                / 2.0d + 1.0);
        // at.concatenate(AffineTransform.getScaleInstance(-1.0, -1.0));
        at.concatenate(AffineTransform.getTranslateInstance(
                -bi.getWidth() / 2.0d, -bi.getHeight() / 2.0d));
        g.drawImage(bi, at, null);
        g.setColor(Color.WHITE);
        // int fontsize = Math.max(50, Math.min(100, specwidth1 / 3));
        // int fontsize = (chromatogramHeight * 2 / 3);// >Math.min(8, );
        final Font f = new Font("Lucida Sans", Font.PLAIN, this.fontsize);
        g.setFont(f);
        final TextLayout tl = new TextLayout("Top: " + this.query_file_name, f,
                g.getFontRenderContext());

        final TextLayout tl2 = new TextLayout("Left: "
                + this.reference_file_name, f, g.getFontRenderContext());

        final int x = specwidth1 + 10;
        final int y = bout.getHeight() - (margin);
        tl.draw(g, x, y - tl.getAscent());
        tl2.draw(g, x, y + (margin / 2) - tl.getAscent());
        // set a 1% margin from the left

        final WritableRaster wr = bout.getRaster();
        log.info("Setting base for color table to x={},y={}", bout.getWidth()
                - colorlegendwidth - margin, bout.getHeight()
                - colorlegendheight - margin);
        // distance case, the smaller, the better
        final int colortableOffset = (bout.getHeight() - (2 * margin)) / 2
                - colorlegendheight / 2;
        if (minimize) {
            for (int i = 0; i < colorlegendheight; i++) {
                for (int j = 0; j < colorlegendwidth; j++) {
                    wr.setPixel(
                            bout.getWidth() - colorlegendwidth - margin + j,
                            colortableOffset + i, new int[]{
                                colorTable[255 - i][0],
                                colorTable[255 - i][1],
                                colorTable[255 - i][2]});
                }
            }
            final TextLayout tlmax = new TextLayout("Max", f, g.
                    getFontRenderContext());
            tlmax.draw(g, bout.getWidth() - margin + 15,
                    (bout.getHeight() - (2 * margin)) / 2 - colorlegendheight
                    / 2);
            final TextLayout tlmin = new TextLayout("Min", f, g.
                    getFontRenderContext());
            tlmin.draw(g, bout.getWidth() - margin + 15,
                    (bout.getHeight() - (2 * margin)) / 2 + colorlegendheight
                    / 2);
            final TextLayout tlsim = new TextLayout("distance", f, g.
                    getFontRenderContext());
            tlsim.draw(g, bout.getWidth() - margin - colorlegendwidth / 2
                    - (float) tlsim.getBounds().getWidth() / 2.0f, (bout.
                    getHeight() - (2 * margin))
                    / 2
                    + colorlegendheight
                    / 2
                    + margin
                    / 2
                    - tlmin.getDescent());
        } else {// similarity case, the bigger the better
            for (int i = 0; i < colorlegendheight; i++) {
                for (int j = 0; j < colorlegendwidth; j++) {
                    wr.setPixel(
                            bout.getWidth() - colorlegendwidth - margin + j,
                            colortableOffset + i, new int[]{
                                colorTable[255 - i][0],
                                colorTable[255 - i][1],
                                colorTable[255 - i][2]});
                }
            }
            final TextLayout tlmax = new TextLayout("Max", f, g.
                    getFontRenderContext());
            tlmax.draw(g, bout.getWidth() - margin + 15,
                    (bout.getHeight() - (2 * margin)) / 2 - colorlegendheight
                    / 2);
            final TextLayout tlmin = new TextLayout("Min", f, g.
                    getFontRenderContext());
            tlmin.draw(g, bout.getWidth() - margin + 15,
                    (bout.getHeight() - (2 * margin)) / 2 + colorlegendheight
                    / 2);
            final TextLayout tlsim = new TextLayout("similarity", f, g.
                    getFontRenderContext());
            tlsim.draw(g, bout.getWidth() - margin - colorlegendwidth / 2
                    - (float) tlsim.getBounds().getWidth() / 2.0f, (bout.
                    getHeight() - (2 * margin))
                    / 2
                    + colorlegendheight
                    / 2
                    + margin
                    / 2
                    - tlmin.getDescent());
        }

        return bout;
    }

    protected BufferedImage addSpectrumImage(final String name, final Array a,
            final int height, final Color fg, final Color bg,
            final List<Integer> anchorPositions) {
        final int[] img = new int[a.getShape()[0] * height];
        Arrays.fill(img, bg.getRGB());
        final IndexIterator iter = a.getIndexIterator();
        final double max = MAMath.getMaximum(a);
        final double min = MAMath.getMinimum(a);
        final int width = a.getShape()[0];
        int col = 0;
        int row_idx = 0;
        while (iter.hasNext()) {
            final double d = Math.floor(height
                    * (iter.getDoubleNext() / (max - min)));
            row_idx = (int) d;
            // System.out.println(" row_idx "+row_idx+" col "+col+" index:
            // "+idx);
            if (col == 0) {
                for (int i = height - 1; i >= 0; i--) {
                    img[(i * (width)) + col] = Color.GREEN.getRGB();
                }
            } else if (col == width - 1) {
                for (int i = height - 1; i >= 0; i--) {
                    img[(i * (width)) + col] = Color.RED.getRGB();
                }
            } else {
                for (int i = height - 1; i >= Math.max(height - row_idx, 0); i--) {
                    img[(i * (width)) + col] = fg.getRGB();
                }
            }
            col++;
        }

        final MemoryImageSource mis = new MemoryImageSource(width, height, img,
                0, width);
        final BufferedImage bim = new BufferedImage(a.getShape()[0], height,
                BufferedImage.TYPE_INT_RGB);
        final Graphics g = bim.getGraphics();
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        final Image im = Toolkit.getDefaultToolkit().createImage(mis);
        g.drawImage(im, 0, 0, null);
        for (final Integer anchor : anchorPositions) {
            for (int i = height - 1; i >= 0; i--) {
                g.setXORMode(fg);
                g.drawLine(anchor, 0, anchor, height);
            }
        }
        g.setColor(Color.black);
        // int fontsize = Math.max(50, Math.min(100, specwidth1 / 3));
        // int fontsize = (chromatogramHeight * 2 / 3);// >Math.min(8, );
        return bim;
    }

    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t1) {
        final IFileFragment iff = MaltcmsTools.getPairwiseDistanceFragment(t1);
        TupleND<IFileFragment> pwdt = MaltcmsTools.getPairwiseAlignments(iff);
        // if (iff == null) {// No alignment has been calculated in this run
        // log.info("Running stand-alone");
        //
        // // ignore the input variables, we will use the ones we were
        // // configured
        // // for
        //
        // for (final IFileFragment f : t1) {
        // createImage(f);
        // }
        // } else {
        // pwdt = MaltcmsTools.getPairwiseDistanceFragments(iff);
        for (final IFileFragment ff : pwdt) {
            createImage(ff);
        }
        // }
        return t1;
    }

    @Override
    public void configure(final Configuration cfg) {
        this.format = cfg.getString(this.getClass().getName() + ".format",
                "PNG");
        this.matrix_vars = StringTools.toStringList(cfg.getList(this.getClass().
                getName()
                + ".matrixVariables", new ArrayList<String>(Arrays.asList(
                "cumulative_distance", "pairwise_distance"))));
        this.left_chromatogram_var = cfg.getString(this.getClass().getName()
                + ".left_chromatogram_var", "total_intensity");
        this.top_chromatogram_var = cfg.getString(this.getClass().getName()
                + ".top_chromatogram_var", "total_intensity");
        this.chromatogramHeight = cfg.getInt(this.getClass().getName()
                + ".chromatogram_height", 200);
        this.path_i = cfg.getString("var.warp_path_i", "warp_path_i");
        this.path_j = cfg.getString("var.warp_path_j", "warp_path_j");
        this.colorramp_location = cfg.getString("images.colorramp",
                "res/colorRamps/bw.csv");
        this.sampleSize = cfg.getInt("images.samples", 1024);
        this.pairsWithFirstElement = cfg.getBoolean(
                "maltcms.commands.fragments.PairwiseDistanceCalculator"
                + ".pairsWithFirstElement", true);
        this.drawPath = Factory.getInstance().getConfiguration().getBoolean(
                this.getClass().getName() + ".draw_path", true);
        this.fontsize = Factory.getInstance().getConfiguration().getInt(
                this.getClass().getName() + ".fontsize", 30);
    }

    protected void createAlignmentMatrixImage(final Array a,
            final IVariableFragment di, final IFileFragment rF,
            final IFileFragment qF, final Array reference, final Array query,
            final List<Tuple2DI> path, final File output, final boolean minimize) {
        EvalTools.notNull(a, this);
        if ((di != null) && di.getStats().isEmpty()) {
            final ArrayStatsScanner ass = new ArrayStatsScanner();
            di.setStats(ass.apply(new Array[]{a})[0]);
        }

        final Iterator<Tuple2DI> piter = path.iterator();
        Tuple2DI t1 = new Tuple2DI(-1, -1);
        // this.specwidth = 200;
        // if ((query != null) && (reference != null)) {
        // this.specwidth = (int) Math.rint(Math.max(query.getShape()[0],
        // reference.getShape()[0])/100.0);
        // this.specwidth = Math.max(50,this.specwidth);
        // }

        log.info("Height of IChromatogram: " + this.chromatogramHeight);
        final int rows = a.getShape()[0];
        final int cols = a.getShape()[1];
        log.info("Size of Image {}x{} = {}", new Object[]{cols, rows,
                    (cols * rows)});
        BufferedImage bi = new BufferedImage(cols, rows,
                BufferedImage.TYPE_INT_RGB);
        final Index ind = a.getIndex();
        if (piter.hasNext()) {
            t1 = piter.next();
        }

        final WritableRaster wr = bi.getRaster();
        final boolean drawPath = this.drawPath;
        final int white = 255;
        int red = 0, green = 0, blue = 0;
        final int[] skipcolor = new int[]{192, 192, 192};
        final ColorRampReader crr = new ColorRampReader();
        final int[][] colorRamp = crr.readColorRamp(this.colorramp_location);
        final double[] samples = ImageTools.createSampleTable(this.sampleSize);
        final double[] breakpoints = ImageTools.getBreakpoints(a,
                this.sampleSize, minimize ? Double.POSITIVE_INFINITY
                : Double.NEGATIVE_INFINITY);
        log.debug("{}", Arrays.toString(breakpoints));
        log.info("Drawing image with {} rows and {} cols", rows, cols);
        double percentDone = 0.0d;
        final long elements = cols * rows;
        long elemCnt = 0;
        final long parts = 10;
        long partCnt = 0;
        boolean infinite = false;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                ind.set(i, j);
                final double av = a.getDouble(ind);
                double v = 0.0d;
                if ((av == Double.POSITIVE_INFINITY)
                        || (av == Double.NEGATIVE_INFINITY)) {
                    log.debug("Found infinite value at {},{}", i, j);
                    infinite = true;
                } else {
                    v = ImageTools.getSample(samples, breakpoints, av);
                    v = v * 255.0d;
                    v = 255.0d - v;
                }
                final int floor = (int) Math.floor(v);

                if (drawPath && (t1.getSecond() == i) && (t1.getFirst() == j)) {
                    wr.setPixel(j, i, new int[]{white, white, white});
                    // //draw a wider path
                    // if(i<width-1 && j<height-1){
                    // wr.setPixel(i+1, j, new
                    // float[]{1.0f,1.0f,1.0f,1.0f});
                    // wr.setPixel(i, j+1, new
                    // float[]{1.0f,1.0f,1.0f,1.0f});
                    // }
                    if (piter.hasNext()) {
                        t1 = piter.next();
                    }
                } else {

                    if (infinite) {
                        red = skipcolor[0];
                        green = skipcolor[1];
                        blue = skipcolor[2];
                        infinite = false;
                    } else {
                        if (minimize) {
                            red = colorRamp[255 - floor][0];
                            green = colorRamp[255 - floor][1];
                            blue = colorRamp[255 - floor][2];
                        } else {
                            red = colorRamp[255 - floor][0];
                            green = colorRamp[255 - floor][1];
                            blue = colorRamp[255 - floor][2];
                        }
                    }
                    wr.setPixel(j, i, new int[]{red, green, blue});
                    percentDone = ArrayTools.calcPercentDone(elements, elemCnt);
                    partCnt = ArrayTools.printPercentDone(percentDone, parts,
                            partCnt, log);
                    elemCnt++;
                }
            }
        }

        if ((reference != null) && (query != null)) {
            final Tuple2D<List<IAnchor>, List<IAnchor>> anchors = MaltcmsTools.
                    getAnchors(rF, qF);
            final AnchorPairSet aps = new AnchorPairSet(anchors.getFirst(),
                    anchors.getSecond(), rows, cols);
            final List<Tuple2D<Integer, Integer>> l = aps.getCorrespondingScans();
            final List<Integer> l1 = new ArrayList<Integer>();
            final List<Integer> l2 = new ArrayList<Integer>();
            for (final Tuple2D<Integer, Integer> tple : l) {
                l1.add(tple.getFirst());
                l2.add(tple.getSecond());
            }
            bi = addSpectra(qF.getName(), rF.getName(), query, reference, bi,
                    this.chromatogramHeight, 50, 255, 100, colorRamp, minimize,
                    l1, l2);
        }

        log.info("Writing Image to file {}", output);
        try {
            ImageIO.write(bi, this.format, output);
            final DefaultWorkflowResult dwr = new DefaultWorkflowResult(output,
                    this, WorkflowSlot.VISUALIZATION, new IFileFragment[]{rF,
                        qF});
            getWorkflow().append(dwr);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }

    }

    private void createImage(final IFileFragment f) {
        log.info("Processing file {}", f.getName());
        for (final String s : this.matrix_vars) {
            try {
                final IVariableFragment matrix_frag = f.getChild(s);
                Array matrix = null;
                matrix = matrix_frag.getArray().transpose(0, 1);
                final MinMax mm = MAMath.getMinMax(matrix);
//                final String ac = FragmentTools.getStringVar(f, "array_comp");
//                final IArrayDoubleComp iadc = Factory.getInstance().
//                        getObjectFactory().instantiate(ac,
//                        IArrayDoubleComp.class);
                final boolean minimize = false;//iadc.minimize();
                if ((mm.min == Double.NEGATIVE_INFINITY)
                        && (mm.max == Double.POSITIVE_INFINITY)) {
                    log.warn(
                            "Found -INF as minimum and +INF as maximum, can not create image!");
                    return;
                }
                final IFileFragment refF = FragmentTools.getLHSFile(f);
                final IFileFragment queryF = FragmentTools.getRHSFile(f);
                this.query_file_name = queryF.getName();
                this.reference_file_name = refF.getName();
                final IVariableFragment query_spec = queryF.getChild(
                        this.top_chromatogram_var);
                final IVariableFragment reference_spec = refF.getChild(
                        this.left_chromatogram_var);
                this.filename = StringTools.removeFileExt(refF.getName())
                        + "_vs_" + StringTools.removeFileExt(queryF.getName())
                        + "_" + s + "." + this.format;
                final Array qval = query_spec.getArray();
                final Array rval = reference_spec.getArray();
                if ((qval.getRank() == 1) && (rval.getRank() == 1)) {// ensure
                    // equal
                    // ranks
                    final IVariableFragment path_i_fragment = f.getChild(
                            this.path_i);
                    final IVariableFragment path_j_fragment = f.getChild(
                            this.path_j);
                    final Array pi = path_i_fragment.getArray();
                    final Array pj = path_j_fragment.getArray();
                    final List<Tuple2DI> l = PathTools.fromArrays(pi, pj);
                    createAlignmentMatrixImage(matrix, matrix_frag,
                            reference_spec.getParent(), query_spec.getParent(),
                            qval, rval, l, new File(getWorkflow().
                            getOutputDirectory(this), this.filename),
                            minimize);
                    System.gc();
                } else {
                    throw new IllegalArgumentException(
                            "Cannot deal with arrays with more than one dimension!");
                }
            } catch (final ResourceNotAvailableException re) {
                log.warn("Could not load variable {}", s);
            }
        }
    }

    @Override
    public String getDescription() {
        return "Creates a plot of the pairwise distance and alignment matrices created during alignment.";
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

    protected int mapToBin(final double value, final double maxval,
            final double minval, final int numbins) {
        return (int) Math.floor(value / (maxval - minval)) * numbins;
    }
}
