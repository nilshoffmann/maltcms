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
package smueller;

import cross.Factory;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FragmentTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.datastructures.fragments.PairwiseDistances;
import maltcms.io.csv.CSVWriter;
import maltcms.io.csv.ColorRampReader;
import maltcms.io.misc.StatsWriter;
import maltcms.tools.ArrayTools;
import maltcms.tools.ImageTools;
import maltcms.tools.MaltcmsTools;
import net.sf.maltcms.apps.Maltcms;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import smueller.alignment.OneAffineAlignment;
import smueller.datastructure.AlignmentOutput;
import smueller.datastructure.BreakPoints;
import smueller.datastructure.DistanceMatrix;
import smueller.datastructure.NewDistanceMatrix;
import smueller.datastructure.SortedJavArrays;
import smueller.tools.DimensionReduce;
import smueller.tools.Standardizer;
import smueller.tools.SymbolConvert;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;

/**
 * <p>SymbolicRepresentationAlignment class.</p>
 *
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 * @version $Id: $Id
 */
@Slf4j
@RequiresVariables(names = {"var.total_intensity"})
@ServiceProvider(service = AFragmentCommand.class)
public class SymbolicRepresentationAlignment extends AFragmentCommand {

    private static int alphabetgr; // mindestgr. 3, max 10
    private static Array[] intensearrays;
    private static HashMap<IFileFragment, Array> hm;
    private static int fenstergr;
    private static double gapinit;
    private static OneAffineAlignment al;
    private static SortedJavArrays sorti;
    private static BreakPoints bpois;
    private static DistanceMatrix distmatrix;
    private static Standardizer stand;
    private static String format;
    private static String location;

    /**
     * <p>Getter for the field <code>al</code>.</p>
     *
     * @return a {@link smueller.alignment.OneAffineAlignment} object.
     */
    public static OneAffineAlignment getAl() {
        return SymbolicRepresentationAlignment.al;
    }

    /**
     * <p>Getter for the field <code>alphabetgr</code>.</p>
     *
     * @return a int.
     */
    public static int getAlphabetgr() {
        return SymbolicRepresentationAlignment.alphabetgr;
    }

    /**
     * <p>Getter for the field <code>bpois</code>.</p>
     *
     * @return a {@link smueller.datastructure.BreakPoints} object.
     */
    public static BreakPoints getBpois() {
        return SymbolicRepresentationAlignment.bpois;
    }

    /**
     * <p>Getter for the field <code>distmatrix</code>.</p>
     *
     * @return a {@link smueller.datastructure.DistanceMatrix} object.
     */
    public static DistanceMatrix getDistmatrix() {
        return SymbolicRepresentationAlignment.distmatrix;
    }

    /**
     * <p>Getter for the field <code>fenstergr</code>.</p>
     *
     * @return a int.
     */
    public static int getFenstergr() {
        return SymbolicRepresentationAlignment.fenstergr;
    }

    /**
     * <p>Getter for the field <code>gapinit</code>.</p>
     *
     * @return a double.
     */
    public static double getGapinit() {
        return SymbolicRepresentationAlignment.gapinit;
    }

    /**
     * <p>Getter for the field <code>sorti</code>.</p>
     *
     * @return a {@link smueller.datastructure.SortedJavArrays} object.
     */
    public static SortedJavArrays getSorti() {
        return SymbolicRepresentationAlignment.sorti;
    }

    /**
     * <p>Getter for the field <code>stand</code>.</p>
     *
     * @return a {@link smueller.tools.Standardizer} object.
     */
    public static Standardizer getStand() {
        return SymbolicRepresentationAlignment.stand;
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(final String[] args) {
        final Maltcms m = Maltcms.getInstance();
        final Configuration cfg = m.parseCommandLine(args);
        EvalTools.notNull(cfg, cfg);
        Factory.getInstance().configure(cfg);
        SymbolicRepresentationAlignment.fenstergr = cfg.getInt(
                "maltcms.soeren.dimensionreduce.window_size", 10);
        SymbolicRepresentationAlignment.alphabetgr = cfg.getInt(
                "maltcms.soeren.symbolic.alphabet_size", 20);
        SymbolicRepresentationAlignment.gapinit = cfg.getDouble(
                "maltcms.soeren.alignment.gapinit", 3);
        SymbolicRepresentationAlignment.format = cfg.getString(
                "maltcms.soeren.alignment.output_format", "pair");
        SymbolicRepresentationAlignment.location = cfg.getString(
                "maltcms.soeren.alignmentoutput.location", "GERMANY");

        // Set up the command sequence
        final ICommandSequence cs = Factory.getInstance()
                .createCommandSequence();
        try {
            cs.getWorkflow().call();
            cs.getWorkflow().save();
        } catch (final Exception e) {
            log.error(e.getLocalizedMessage());
        }
        System.exit(0);
    }
    private boolean pairsWithFirst = false;
    private String minArrayComp;

    /** {@inheritDoc} */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        final int size = t.getSize();
        SymbolicRepresentationAlignment.intensearrays = new Array[size];
        final double[] median = new double[size];
        final double[] minmax = new double[size * 2];
        SymbolicRepresentationAlignment.hm = new HashMap<>();
        SymbolicRepresentationAlignment.sorti = new SortedJavArrays();
        SymbolicRepresentationAlignment.stand = new Standardizer();
        int i = 0;

        for (final IFileFragment f : t) {
            SymbolicRepresentationAlignment.intensearrays[i] = f.getChild(
                    "total_intensity").getArray().copy();

            SymbolicRepresentationAlignment.hm
                    .put(
                            f,
                            DimensionReduce
                            .paa(
                                    SymbolicRepresentationAlignment.stand
                                    .cleanbaseline(SymbolicRepresentationAlignment.stand
                                            .scale(SymbolicRepresentationAlignment.stand
                                                    .logData(SymbolicRepresentationAlignment.intensearrays[i]))),
                                    SymbolicRepresentationAlignment.fenstergr));
            median[i] = SymbolicRepresentationAlignment.stand.getMedian();
            if (i < 1) {
                minmax[i] = SymbolicRepresentationAlignment.stand.getMin();
                minmax[i + 1] = SymbolicRepresentationAlignment.stand.getMax();
            }
            i++;
        }

        final HashMap<IFileFragment, Integer> filenameToIndex = new HashMap<>();
        final int nextIndex = 0;
        final ArrayDouble.D2 pairwiseDistances = new ArrayDouble.D2(
                t.getSize(), t.getSize());
        final Iterator<IFileFragment> ffiter = t.getIterator();
        final int maxlength = initMaxLength(ffiter);
        final ArrayChar.D2 names = initNames(t, filenameToIndex, nextIndex,
                maxlength);

        NewDistanceMatrix ndm = null;
        final TupleND<IFileFragment> alignments = new TupleND<>();
        final List<Tuple2D<IFileFragment, IFileFragment>> tpl = (this.pairsWithFirst ? t
                .getPairsWithFirstElement()
                : t.getPairs());
        for (final Tuple2D<IFileFragment, IFileFragment> F : tpl) {

            final String filename = "PW_DISTANCE_"
                    + StringTools.removeFileExt(F.getFirst().getName()) + "_"
                    + StringTools.removeFileExt(F.getSecond().getName())
                    + ".csv";
            final IFileFragment iff = new FileFragment(
                    new File(getWorkflow().getOutputDirectory(this),
                            filename));
            final StatsMap sm = new StatsMap(iff);
            final long t_start = System.currentTimeMillis();
            SymbolicRepresentationAlignment.sorti.sort(
                    SymbolicRepresentationAlignment.hm.get(F.getFirst()),
                    SymbolicRepresentationAlignment.hm.get(F.getSecond()));
            SymbolicRepresentationAlignment.bpois = new BreakPoints(
                    SymbolicRepresentationAlignment.hm.get(F.getFirst()),
                    SymbolicRepresentationAlignment.hm.get(F.getSecond()));

            ndm = new NewDistanceMatrix(SymbolicRepresentationAlignment.bpois
                    .getCommon());
            final int aoriglen = F.getFirst().getChild("total_intensity")
                    .getArray().getShape()[0];
            final int boriglen = F.getSecond().getChild("total_intensity")
                    .getArray().getShape()[0];
            SymbolicRepresentationAlignment.al = new OneAffineAlignment(F
                    .getFirst(), F.getSecond());
            SymbolicRepresentationAlignment.al.setKostenfunktion(ndm
                    .getDistmat());
            SymbolicRepresentationAlignment.al.computeMatrix("-"
                    + SymbolConvert.symbolic(
                            SymbolicRepresentationAlignment.hm
                            .get(F.getFirst()),
                            SymbolicRepresentationAlignment.bpois.getCommon())
                    .toString().replaceAll(" ", ""), "-"
                    + SymbolConvert.symbolic(
                            SymbolicRepresentationAlignment.hm.get(F
                                    .getSecond()),
                            SymbolicRepresentationAlignment.bpois.getCommon())
                    .toString().replaceAll(" ", ""));
            SymbolicRepresentationAlignment.al.createAlignments(
                    SymbolicRepresentationAlignment.al.getSeq1(),
                    SymbolicRepresentationAlignment.al.getSeq2());
            final long t_end = System.currentTimeMillis() - t_start;
            final AlignmentOutput put = new AlignmentOutput();
            for (int fr = 0; fr < SymbolicRepresentationAlignment.al
                    .getAllalignments().size(); fr++) {
                put.writefile(SymbolicRepresentationAlignment.al.getSeq1(),
                        SymbolicRepresentationAlignment.al.getSeq2(),
                        SymbolicRepresentationAlignment.al.getAllalignments(),
                        SymbolicRepresentationAlignment.al.getMatrix(),
                        SymbolicRepresentationAlignment.format, ndm
                        .getDistmat(),
                        SymbolicRepresentationAlignment.location, fr,
                        SymbolicRepresentationAlignment.al, aoriglen, boriglen,
                        getWorkflow());
            }
            final int maplength = MaltcmsTools.getWarpPath(
                    put.getResult().provideFileFragment()).size();
            sm.setLabel(F.getFirst().getName() + "-" + F.getSecond().getName());
            sm.put("time", new Double(t_end));

            sm.put("nanchors", (double) (0));
            sm.put("lhsNscans", (double) aoriglen);
            sm.put("rhsNscans", (double) boriglen);
            sm.put("longestPath", (double) (aoriglen + boriglen - 1));

            sm.put("maplength", new Double(maplength));
            put.getResult().provideFileFragment().save();
            final double value = put.getResult().getResult().get();
            sm.put("value", value);
            final Integer i1 = filenameToIndex.get(F.getFirst());
            final Integer i2 = filenameToIndex.get(F.getSecond());
            pairwiseDistances.set(i1, i2, value);
            pairwiseDistances.set(i2, i1, value);
            final StatsWriter sw = Factory.getInstance().getObjectFactory()
                    .instantiate(StatsWriter.class);
            sw.setWorkflow(getWorkflow());
            sw.write(sm);
            alignments.add(put.getResult().provideFileFragment());
        }

        drawTICS(t, new TupleND<>(alignments));
        final String name = "pairwise_distances.cdf";
        final PairwiseDistances pd = new PairwiseDistances();
        pd.setName(name);
        pd.setPairwiseDistances(pairwiseDistances);
        pd.setNames(names);
        final IFileFragment ret = new FileFragment(new File(getWorkflow().getOutputDirectory(this), name));
        pd.modify(ret);
        ret.save();
        saveToCSV(ret, pairwiseDistances, names);
        EvalTools.notNull(alignments, this);
        return t;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        SymbolicRepresentationAlignment.fenstergr = cfg.getInt(
                "maltcms.soeren.dimensionreduce.window_size", 4);
        SymbolicRepresentationAlignment.alphabetgr = cfg.getInt(
                "maltcms.soeren.symbolic.alphabet_size", 7);
        SymbolicRepresentationAlignment.gapinit = cfg.getDouble(
                "maltcms.soeren.alignment.gapinit", 1);
        SymbolicRepresentationAlignment.format = cfg.getString(
                "maltcms.soeren.alignment.output_format", "pair");
        SymbolicRepresentationAlignment.location = cfg.getString(
                "maltcms.soeren.alignmentoutput.location", "GERMANY");
        this.pairsWithFirst = cfg
                .getBoolean(
                        "maltcms.commands.fragments.PairwiseDistanceCalculator.pairsWithFirstElement",
                        true);
        this.minArrayComp = cfg.getString("var.minimizing_array_comp",
                "minimizing_array_comp");
    }

    private void drawAlignedTICS(final TupleND<IFileFragment> t,
            final TupleND<IFileFragment> alignment) {
        final String refname = t.get(0).getName();
        final ArrayList<IFileFragment> toRefAlignments = new ArrayList<>();
        for (final IFileFragment iff : alignment) {
            final IFileFragment ref = FragmentTools.getLHSFile(iff);
            if (ref.getName().equals(refname)) {
                toRefAlignments.add(iff);
            }
        }
        final int heightPerTIC = 50;
        int maxLength = 0;
        final Array[] a = new Array[toRefAlignments.size() + 1];
        a[0] = t.get(0).getChild("total_intensity").getArray();
        maxLength = a[0].getShape()[0];
        for (int i = 0; i < toRefAlignments.size(); i++) {
            final List<Tuple2DI> al = MaltcmsTools.getWarpPath(toRefAlignments
                    .get(i));
            final Array rhs = FragmentTools.getRHSFile(toRefAlignments.get(i))
                    .getChild("total_intensity").getArray();
            final Array rhsm = Array.factory(a[0].getElementType(), a[0]
                    .getShape());
            final Index rhsi = rhs.getIndex();
            final Index rhsmi = rhsm.getIndex();
            for (final Tuple2DI tpl : al) {
                final double value = rhsm.getDouble(rhsmi.set(tpl.getFirst()));
                rhsm.setDouble(rhsmi.set(tpl.getFirst()), (value + rhs
                        .getDouble(rhsi.set(tpl.getSecond()))) / 2.0d);
            }
            a[i + 1] = rhsm;

            // maxLength = (a[i + 1].getShape()[0] > maxLength ? a[i + 1]
            // .getShape()[0] : maxLength);
        }
        final ArrayStatsScanner ass = new ArrayStatsScanner();
        final StatsMap[] sm = ass.apply(a);
        final StatsMap gsm = ass.getGlobalStatsMap();
        final int maxStringLength = 250;
        final BufferedImage bi = new BufferedImage(maxLength + maxStringLength,
                heightPerTIC * t.getSize(), BufferedImage.TYPE_INT_RGB);
        // double[] samples = ImageTools.createSampleTable(1024);
        final ColorRampReader crr = new ColorRampReader();
        final int[][] colorRamp = crr.readColorRamp("res/colorRamps/bw.csv");
        for (int i = 0; i < t.getSize(); i++) {
            final String name = StringTools.removeFileExt(t.get(i).getName());
            final BufferedImage tic = bi.getSubimage(maxStringLength, i
                    * heightPerTIC, maxLength, heightPerTIC);
            // double[] brks = ImageTools.getBreakpoints(a[i], 1024);
            final ArrayList<Array> al = new ArrayList<>(a[i].getShape()[0]);
            final ucar.ma2.Index idx = a[i].getIndex();
            for (int j = 0; j < a[i].getShape()[0]; j++) {
                final ArrayInt.D1 b = new ArrayInt.D1(heightPerTIC);
                ArrayTools.fill(b, a[i].getDouble(idx.set(j)));
                al.add(b);
            }
            ImageTools.makeImage(tic.getRaster(), al, 1024, colorRamp, 0.0d);
            final BufferedImage label = bi.getSubimage(0, i * heightPerTIC,
                    maxStringLength, heightPerTIC);
            final Graphics2D g = label.createGraphics();
            g.setColor(Color.WHITE);
            final int fontsize = heightPerTIC * 2 / 3;
            this.log.info("Fontsize: {}", fontsize);
            final Font f = new Font("Arial", Font.PLAIN, fontsize);
            g.setFont(f);
            final LineMetrics lm = f.getLineMetrics(name, g
                    .getFontRenderContext());
            final TextLayout tl = new TextLayout(name, f, g
                    .getFontRenderContext());
            tl.draw(g, 0, tl.getAscent());
            // g.drawString(name, 0, lm.getAscent() / heightPerTIC);
        }
        File out = null;
        final String filename = "aligned-tics." + "png";
        final File d = getWorkflow().getOutputDirectory(this);
        out = new File(d, filename);
        try {
            this.log.info("Saving image to " + out.getAbsolutePath());

            ImageIO.write(bi, "png", out);
        } catch (final IOException e) {
            this.log.error(e.getLocalizedMessage());
        }

    }

    private void drawTICS(final TupleND<IFileFragment> t,
            final TupleND<IFileFragment> alignments) {
        final int heightPerTIC = 50;
        int maxLength = 0;
        final Array[] a = new Array[t.size()];
        for (int i = 0; i < t.size(); i++) {
            a[i] = t.get(i).getChild("total_intensity").getArray();
            maxLength = (a[i].getShape()[0] > maxLength ? a[i].getShape()[0]
                    : maxLength);
        }
        final ArrayStatsScanner ass = new ArrayStatsScanner();
        final StatsMap[] sm = ass.apply(a);
        final StatsMap gsm = ass.getGlobalStatsMap();
        final int maxStringLength = 250;
        final BufferedImage bi = new BufferedImage(maxLength + maxStringLength,
                heightPerTIC * t.getSize(), BufferedImage.TYPE_INT_RGB);
        // double[] samples = ImageTools.createSampleTable(1024);
        final ColorRampReader crr = new ColorRampReader();
        final int[][] colorRamp = crr.readColorRamp("res/colorRamps/bw.csv");
        for (int i = 0; i < t.getSize(); i++) {
            final String name = StringTools.removeFileExt(t.get(i).getName());
            final BufferedImage tic = bi.getSubimage(maxStringLength, i
                    * heightPerTIC, maxLength, heightPerTIC);
            // double[] brks = ImageTools.getBreakpoints(a[i], 1024);
            final ArrayList<Array> al = new ArrayList<>(a[i].getShape()[0]);
            final ucar.ma2.Index idx = a[i].getIndex();
            for (int j = 0; j < a[i].getShape()[0]; j++) {
                final ArrayInt.D1 b = new ArrayInt.D1(heightPerTIC);
                ArrayTools.fill(b, a[i].getDouble(idx.set(j)));
                al.add(b);
            }
            ImageTools.makeImage(tic.getRaster(), al, 1024, colorRamp, 0.0d);
            final BufferedImage label = bi.getSubimage(0, i * heightPerTIC,
                    maxStringLength, heightPerTIC);
            final Graphics2D g = label.createGraphics();
            g.setColor(Color.WHITE);
            final int fontsize = heightPerTIC * 2 / 3;
            this.log.info("Fontsize: {}", fontsize);
            final Font f = new Font("Arial", Font.PLAIN, fontsize);
            g.setFont(f);
            final LineMetrics lm = f.getLineMetrics(name, g
                    .getFontRenderContext());
            final TextLayout tl = new TextLayout(name, f, g
                    .getFontRenderContext());
            tl.draw(g, 0, tl.getAscent());
            // g.drawString(name, 0, lm.getAscent() / heightPerTIC);
        }
        File out = null;
        final String filename = "unaligned-tics." + "png";
        final File d = getWorkflow().getOutputDirectory(this);
        out = new File(d, filename);
        try {
            this.log.info("Saving image to " + out.getAbsolutePath());

            ImageIO.write(bi, "png", out);
        } catch (final IOException e) {
            this.log.error(e.getLocalizedMessage());
        }
        drawAlignedTICS(t, alignments);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Calculates alignment on symbolic representation of time series, using affine gap costs.";
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /** {@inheritDoc} */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.ALIGNMENT;
    }

    private int initMaxLength(final Iterator<IFileFragment> ffiter) {
        int maxlength = 512;
        while (ffiter.hasNext()) {
            final IFileFragment ff = ffiter.next();
            final int len = ff.getName().length();
            if (len > maxlength) {
                maxlength = len;
            }
        }
        return maxlength;
    }

    private ArrayChar.D2 initNames(final TupleND<IFileFragment> t,
            final HashMap<IFileFragment, Integer> filenameToIndex,
            final int nextIndex1, final int maxlength) {
        int nextIndex = nextIndex1;
        Iterator<IFileFragment> ffiter;
        final ArrayChar.D2 names = new ArrayChar.D2(t.size(), maxlength);
        ffiter = t.getIterator();
        while (ffiter.hasNext()) {
            final IFileFragment ff = ffiter.next();
            if (!filenameToIndex.containsKey(ff)) {
                filenameToIndex.put(ff, nextIndex);
            }

            names.setString(nextIndex, ff.getUri().toString());
            nextIndex++;
        }
        return names;
    }

    /**
     * <p>saveToCSV.</p>
     *
     * @param pwdist a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param distances a {@link ucar.ma2.ArrayDouble.D2} object.
     * @param names a {@link ucar.ma2.ArrayChar.D2} object.
     */
    public void saveToCSV(final IFileFragment pwdist,
            final ArrayDouble.D2 distances, final ArrayChar.D2 names) {
        final CSVWriter csvw = Factory.getInstance().getObjectFactory()
                .instantiate(CSVWriter.class);
        csvw.setWorkflow(getWorkflow());
        csvw.writeArray2DwithLabels(getWorkflow().getOutputDirectory(this)
                .getAbsolutePath(), "pairwise_distances.csv", distances, names,
                this.getClass(), WorkflowSlot.STATISTICS, getWorkflow()
                .getStartupDate(), pwdist);
    }
}
