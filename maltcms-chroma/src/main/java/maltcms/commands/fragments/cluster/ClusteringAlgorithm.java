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
package maltcms.commands.fragments.cluster;

import cross.Factory;
import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.distances.PairwiseFeatureSequenceSimilarity;
import maltcms.commands.distances.dtw.MZIDynamicTimeWarp;
import maltcms.commands.fragments.warp.PathWarp;
import maltcms.datastructures.cluster.BinaryCluster;
import maltcms.datastructures.fragments.PairwiseDistances;
import maltcms.io.misc.StatsWriter;
import maltcms.tools.MaltcmsTools;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;

/**
 * Abstract base class for clustering algorithms based on similarity or distance
 * matrices.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
@Data
public abstract class ClusteringAlgorithm extends AFragmentCommand implements
        IClusteringAlgorithm {

    @Override
    public String toString() {
        return getClass().getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.commands.fragments.cluster.IClusteringAlgorithm#toGraphML()
     */
    public static String toGraphML(final IClusteringAlgorithm ica) {
        final StringBuilder sb = new StringBuilder();
        final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns "
                + "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">";

        final String gbegin = "<graph id=\"G\" edgedefault=\"undirected\">";
        final String gend = "</graph>";
        final String keyid_name = "<key id=\"d0\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>";
        final String keyid_weight = "<key id=\"d1\" for=\"edge\" attr.name=\"label\" attr.type=\"double\"/>";
        String nodes = "";
        String edges = "";
        int edgecnt = 0;
        for (final Entry<Integer, BinaryCluster> e : ica.getClusters()) {
            nodes += "<node id=\"n" + e.getKey() + "\">";
            nodes += "<data key=\"d0\">" + e.getValue().getName() + "</data>";
            nodes += "</node>";
            final int lc = e.getValue().getLChildID();
            final int rc = e.getValue().getRChildID();
            if (lc != -1) {
                final double lcv = ica.getCluster(lc).getDistanceToParent();
                edges += "<edge id=\"e" + (edgecnt++) + "\" source=\"n" + lc
                        + "\" target=\"n" + e.getKey() + "\">";
                edges += "<data key=\"d1\">" + lcv + "</data>";
                edges += "</edge>";
            }
            if (rc != -1) {
                final double rcv = ica.getCluster(rc).getDistanceToParent();
                edges += "<edge id=\"e" + (edgecnt++) + "\" source=\"n" + rc
                        + "\" target=\"n" + e.getKey() + "\">";
                edges += "<data key=\"d1\">" + rcv + "</data>";
                edges += "</edge>";
            }
        }
        sb.append(header);
        sb.append(keyid_name);
        sb.append(keyid_weight);
        sb.append(gbegin);
        sb.append(nodes);
        sb.append(edges);
        sb.append(gend);
        sb.append("</graphml>");
        return sb.toString();
    }
    @Configurable
    private boolean normalize_scans = false;
    private TupleND<IFileFragment> inputFiles = null;
    private IFileFragment consensus = null;
    private double[][] dist = null;
    private String[] names = null;
    // private double[][] D = null;
    private int L = -1;
    private HashSet<Integer> usedIndices = null;
    private HashMap<Integer, BinaryCluster> cluster = null;
    private HashMap<Integer, IFileFragment> fragments = null;
    private HashMap<Integer, Tuple2D<String, String>> nameToNameLookup = null;
    @Configurable(name = "guide.tree.distance")
    private PairwiseFeatureSequenceSimilarity chromatogramDistanceFunctionClass = new MZIDynamicTimeWarp();
    private String[] clusterNames;
    @Configurable(name = "var.pairwise_distance_matrix")
    private String pairwiseDistanceMatrixVariableName = "pairwise_distance_matrix";
    @Configurable(name = "var.pairwise_distance_names")
    private String pairwiseDistanceNamesVariableName = "pairwise_distance_names";
    @Configurable(name = "var.minimizing_array_comp")
    private String minimizingArrayCompVariableName = "minimizing_array_comp";
    @Configurable(name = "maltcms.commands.fragments.warp.AWarp")
    private AFragmentCommand chromatogramWarpCommandClass = new PathWarp();
    // minimize or maximize
    private boolean minimizeDist = true;
    @Configurable
    private boolean drawTICs;
    @Configurable
    private boolean drawEICs;
    private List<IFileFragment> alignments = new ArrayList<>();
    private ArrayDouble.D2 pwds = null;
    private String minArrayComp;

    public ClusteringAlgorithm() {
    }

    public void addNodeK(final int i, final int j, final int k) {

        this.names[k] = "((" + this.names[i] + ")" + this.names[j] + ")";
        this.clusterNames[k] = this.clusterNames[i] + "_vs_"
                + this.clusterNames[j];
        this.nameToNameLookup.put(k, new Tuple2D<>(
                this.clusterNames[i], this.clusterNames[j]));
        log.debug("Merging cluster " + i + " (" + this.names[i] + ") and "
                + j + " (" + this.names[j] + ") with prior distance of "
                + d(i, j));
        joinIJtoK(i, j, k, dmat(i, j, k));
    }

    /*
     * (non-Javadoc)
     *
     * @seemaltcms.commands.fragments.cluster.IClusteringAlgorithm#apply(cross.
     * datastructures.tuple.TupleND)
     */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        setInputFiles(t);
        final IFileFragment pwd = MaltcmsTools.getPairwiseDistanceFragment(t);
        init(pwd, t);
        pwds = new ArrayDouble.D2(this.names.length, this.names.length);
        merge();
        final ArrayList<IFileFragment> al = new ArrayList<>(
                this.fragments.size());
        log.debug("Finished merging!");
        for (int i = 0; i < this.fragments.size(); i++) {
            final IFileFragment ff = this.fragments.get(i);
            // al.add(ff);
            if (!(new File(ff.getUri()).exists())) {
                ff.save();
            }
            log.debug("File i={} with name {}", i, ff.getUri());
        }
        al.add(getConsensus());
        for (final IFileFragment iff : getInputFiles()) {
            if (!(new File(iff.getUri()).exists())) {
                iff.save();
            }
            al.add(iff);
        }

        ArrayChar.D2 names = new ArrayChar.D2(al.size(), initMaxLength(al.iterator()));
        int i = 0;
        for (IFileFragment iff : al) {
            names.setString(i++, iff.getUri().toString());
        }

        final String name = "pairwise_distances.cdf";
        final PairwiseDistances pd = new PairwiseDistances();
        pd.setName(name);
        pd.setPairwiseDistances(this.pwds);
        pd.setMinArrayComp(this.minArrayComp);
        pd.setMinimize(this.minimizeDist);
        pd.setNames(names);
        pd.setAlignments(new TupleND<>(this.alignments));
        pd.setWorkflow(getWorkflow());
        final IFileFragment ret = new FileFragment(
                new File(getWorkflow().getOutputDirectory(pd), name));
        pd.modify(ret);
        ret.save();
        final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
                ret.getUri(), this, WorkflowSlot.STATISTICS, ret);
        getWorkflow().append(dwr);

        log.info("Returned FileFragments: {}", al);
        TupleND<IFileFragment> retFiles = new TupleND<>();
        for (final IFileFragment iff : al) {
            final IFileFragment rf = new FileFragment(
                    new File(getWorkflow().getOutputDirectory(this),
                            iff.getName()));
            rf.addSourceFile(iff);
            rf.addSourceFile(ret);
            retFiles.add(rf);
            rf.save();
            final DefaultWorkflowResult wr = new DefaultWorkflowResult(
                    rf.getUri(), this,
                    WorkflowSlot.CLUSTERING, rf);
            getWorkflow().append(wr);
        }
        final File graphml = new File(getWorkflow().getOutputDirectory(this),
                "maltcms.graphml");
        try {
            try (BufferedWriter bw = new BufferedWriter(
                    new FileWriter(graphml))) {
                bw.write(ClusteringAlgorithm.toGraphML(this));
                bw.flush();
            }
        } catch (final IOException e) {
            log.error(e.getLocalizedMessage());
        }
        final File newick = new File(getWorkflow().getOutputDirectory(this),
                "maltcms.newick");
        try {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(newick))) {
                bw.write(this.cluster.get(this.dist.length - 1).toNewick() + ";");
                bw.flush();
            }
        } catch (final IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return new TupleND<>(retFiles);
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

    @Override
    public void configure(final Configuration cfg) {
        this.pairwiseDistanceMatrixVariableName = cfg.getString(
                "var.pairwise_distance_matrix", "pairwise_distance_matrix");
        this.pairwiseDistanceNamesVariableName = cfg.getString(
                "var.pairwise_distance_names", "pairwise_distance_names");
        this.minimizingArrayCompVariableName = cfg.getString(
                "var.minimizing_array_comp", "minimizing_array_comp");
        this.minArrayComp = cfg.getString("var.minimizing_array_comp",
                "minimizing_array_comp");
    }

    private IFileFragment createDenseArrays(final IFileFragment iff) {

        AFragmentCommand dap = Factory.getInstance().getObjectFactory().instantiate(
                "maltcms.commands.fragments.preprocessing.DenseArrayProducer",
                AFragmentCommand.class);
        dap.setWorkflow(getWorkflow());
        return dap.apply(new TupleND<>(iff)).get(0);
    }

    public double d(final int i, final int j) {
        if (this.cluster.containsKey(i) && this.cluster.containsKey(j)) {
            return this.cluster.get(Math.max(i, j)).getDistanceTo(
                    Math.min(i, j));
        } else {
            throw new IllegalArgumentException("ICluster " + i + " or cluster "
                    + j + " unknown!");
        }
    }

    public abstract double[] dmat(int i, int j, int k);

    public abstract void findBestD(int l);

    /**
     * @return the chromatogramDistanceFunctionClass
     */
    public PairwiseFeatureSequenceSimilarity getChromatogramDistanceFunction() {
        return this.chromatogramDistanceFunctionClass;
    }

    /**
     * @return the chromatogramWarpCommandClass
     */
    public AFragmentCommand getChromatogramWarpCommand() {
        return this.chromatogramWarpCommandClass;
    }

    /**
     * @return the cluster
     */
    public HashMap<Integer, BinaryCluster> getCluster() {
        return this.cluster;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.commands.fragments.cluster.IClusteringAlgorithm#getCluster(int)
     */
    @Override
    public BinaryCluster getCluster(final int i) {
        return this.cluster.get(i);
    }

    /**
     * @return the clusterNames
     */
    public String[] getClusterNames() {
        return this.clusterNames;
    }

    @Override
    public Set<Entry<Integer, BinaryCluster>> getClusters() {
        return Collections.unmodifiableSet(this.cluster.entrySet());
    }

    @Override
    public IFileFragment getConsensus() {
        return this.consensus;
    }

    @Override
    public String getDescription() {
        return "Calculates clustering based on pairwise similarities/distances.";
    }

    /**
     * @return the dist
     */
    public double[][] getDist() {
        return this.dist;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.commands.fragments.cluster.IClusteringAlgorithm#getFragments()
     */
    @Override
    public HashMap<Integer, IFileFragment> getFragments() {
        return this.fragments;
    }

    @Override
    public TupleND<IFileFragment> getInputFiles() {
        return this.inputFiles;
    }

    public int getL() {
        return this.L;
    }

    /**
     * @return the minimizingArrayCompVariableName
     */
    public String getMinimizingArrayCompVariableName() {
        return this.minimizingArrayCompVariableName;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.commands.fragments.cluster.IClusteringAlgorithm#getNames()
     */
    @Override
    public String[] getNames() {
        return this.names;
    }

    /**
     * @return the pairwiseDistanceMatrixVariableName
     */
    public String getPairwiseDistanceMatrixVariableName() {
        return this.pairwiseDistanceMatrixVariableName;
    }

    /**
     * @return the pairwiseDistanceNamesVariableName
     */
    public String getPairwiseDistanceNamesVariableName() {
        return this.pairwiseDistanceNamesVariableName;
    }

    public HashSet<Integer> getUsedIndices() {
        return this.usedIndices;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.CLUSTERING;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.commands.fragments.cluster.IClusteringAlgorithm#handleFileFragments
     * (int, int, int)
     */
    public void handleFileFragments(final int i, final int j, final int k) {
        final IFileFragment ff1 = this.fragments.get(i);
        final IFileFragment ff2 = this.fragments.get(j);
        EvalTools.notNull(this.chromatogramDistanceFunctionClass, this);
        final long t_start = System.currentTimeMillis();
        final IFileFragment dtw = this.chromatogramDistanceFunctionClass.apply(
                ff1, ff2);
        DefaultWorkflowResult dwr = new DefaultWorkflowResult(dtw.getUri(), this, WorkflowSlot.ALIGNMENT, dtw);
        getWorkflow().append(dwr);
        this.chromatogramWarpCommandClass.setWorkflow(getWorkflow());
        final IFileFragment warped = this.chromatogramWarpCommandClass.apply(
                new TupleND<>(dtw)).get(0);
        alignments.add(dtw);
        final long t_end = System.currentTimeMillis() - t_start;
        final StatsMap sm = new StatsMap(new FileFragment(
                new File(getWorkflow().getOutputDirectory(this), "NJ_"
                        + StringTools.removeFileExt(ff1.getName())
                        + "_"
                        + StringTools.removeFileExt(ff2.getName())
                        + ".csv")));
        sm.put("time", new Double(t_end));
        pwds.set(k, k, this.chromatogramDistanceFunctionClass.getResult().get());
        final StatsWriter sw = Factory.getInstance().getObjectFactory().instantiate(StatsWriter.class);
        sw.setWorkflow(getWorkflow());
        sw.write(sm);
        dwr = new DefaultWorkflowResult(warped.getUri(),
                this, WorkflowSlot.WARPING, warped);
        getWorkflow().append(dwr);

        final IFileFragment res = createDenseArrays(warped);

        log.debug("{}", ff1.toString());
        log.debug("{}", warped.toString());
        log.debug("{}", res);
        // FileFragment merged = this.merge.mergeAll(ff1, warped);

        this.fragments.put(k, res);
        // this.fragments.get(k).save();

    }

    public void init(final double[][] distances, final String[] names1,
            final TupleND<IFileFragment> fragments1) {
        this.usedIndices = new HashSet<>();
        this.nameToNameLookup = new HashMap<>();
        final int numjoins = (distances.length) * 2 - 1;
        if (fragments1 != null) {
            this.fragments = new HashMap<>();
        }
        this.clusterNames = new String[numjoins];
        log.debug("Total expected number of elements: " + numjoins);
        setL(distances.length);
        // this.D = new double[numjoins][numjoins];
        this.dist = new double[numjoins][numjoins];
        for (int i = 0; i < numjoins; i++) {
            for (int j = 0; j < numjoins; j++) {
                this.dist[i][j] = Double.NaN;
            }
        }
        this.cluster = new HashMap<>();
        for (int i = 0; i < distances.length; i++) {
            System.arraycopy(distances[i], 0, this.dist[i], 0,
                    distances.length);
            final BinaryCluster njc = new BinaryCluster(names1[i], i,
                    this.dist[i]);
            if (fragments1 != null) {
                this.fragments.put(i, fragments1.get(i));
            }
            this.cluster.put(i, njc);
            // System.out.println("Adding new ICluster: "+i+" "+names[i]);
            log.debug("Adding initial ICluster (" + (i + 1) + "/"
                    + distances.length + ") : " + njc.toString());
        }
        this.names = new String[numjoins];
        System.arraycopy(names1, 0, this.names, 0, names1.length);
        System.arraycopy(names1, 0, this.clusterNames, 0, names1.length);
        log.debug("Starting with the following clusters: {}", Arrays.toString(this.names));
        // System.out.println("Number of joins: "+numjoins);
        // System.out.println("Initial number of clusters: "+this.L);
        printDistMatrix();
        this.chromatogramDistanceFunctionClass.setWorkflow(getWorkflow());
    }

    /*
     * (non-Javadoc)
     *
     * @seemaltcms.commands.fragments.cluster.IClusteringAlgorithm#init(cross.
     * datastructures.fragments.FileFragment,
     * cross.datastructures.tuple.TupleND)
     */
    @Override
    public void init(final IFileFragment pwd, final TupleND<IFileFragment> t) {
        // check for existing children
        // if (pwd.hasChild(this.pwdistMName) && pwd.hasChild(this.pwdistNames)
        // && pwd.hasChild(this.minDistComp)) {
        final ArrayDouble.D2 pwdist = (ArrayDouble.D2) pwd.getChild(
                this.pairwiseDistanceMatrixVariableName).getArray();
        final ArrayChar.D2 names1 = (ArrayChar.D2) pwd.getChild(
                this.pairwiseDistanceNamesVariableName).getArray();
        final ArrayInt.D0 minimizeDistA = (ArrayInt.D0) pwd.getChild(
                this.minimizingArrayCompVariableName).getArray();
        if (minimizeDistA.get() == 0) {
            this.minimizeDist = false;
        } else {
            this.minimizeDist = true;
        }
        EvalTools.notNull(new Object[]{pwdist, names1}, this);
        final double[][] distances = new double[pwdist.getShape()[0]][pwdist.getShape()[1]];
        final String[] cnames1 = new String[distances.length];
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < pwdist.getShape()[0]; i++) {
            for (int j = 0; j < pwdist.getShape()[1]; j++) {
                distances[i][j] = pwdist.get(i, j);
                sb.append(distances[i][j] + "\t");
            }
            sb.append("\n");
            cnames1[i] = new File(names1.getString(i)).getName();
        }
        log.debug(sb.toString());
        init(distances, cnames1, t);
        // } else {
        // throw new IllegalArgumentException("FileFragment " + pwd
        // + " misses one or more of the following children: "
        // + this.pwdistMName + ", " + this.pwdistNames);
        // }
    }

    /**
     * @return the minimizeDist
     */
    public boolean isMinimizeDist() {
        return this.minimizeDist;
    }

    public boolean isMinimizing() {
        return this.minimizeDist;
    }

    @Override
    public Iterator<IFileFragment> iterator() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.commands.fragments.cluster.IClusteringAlgorithm#joinIJtoK(int,
     * int, int, double[])
     */
    public abstract void joinIJtoK(int i, int j, int k, double[] dist1);

    /*
     * (non-Javadoc)
     *
     * @see maltcms.commands.fragments.cluster.IClusteringAlgorithm#merge()
     */
    @Override
    public void merge() {
        log.info("#############################################################################");
        log.info("# Running " + this.getClass().getName()
                + " as clustering algorithm");
        log.info("#############################################################################");
        int newIndex = getL();// L is the number of clusters
        // findMaxDist(newIndex);
        while (getL() > 2) {
            // System.out.println("L: "+getL()+" newIndex: "+newIndex);
            findBestD(newIndex++);
            printDistMatrix();
            setL(getL() - 1);

        }
        findBestD(newIndex);

        final StringBuilder sb = new StringBuilder();
        sb.append(this.cluster.get(this.dist.length - 1));
        // System.out.println(sb.toString());
        log.debug(this.cluster.get(this.dist.length - 1).toNewick() + ";");
        printDistMatrix();
        setConsensus(this.fragments.get(this.dist.length - 1));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.commands.fragments.multiplealignment.ClusteringAlgorithm#printCluster
     * ()
     */
    public void printCluster() {
        log.info("Printing cluster:");
        final Set<Integer> s = this.cluster.keySet();
        for (final Integer i : s) {
            log.info(this.cluster.get(i).toString());
        }
    }

    public void printDistanceToNewCluster(final int i, final int j,
            final int nci) {
        log.debug("Distance of " + this.names[i] + " to new cluster "
                + this.names[nci] + " " + getCluster(i).getDistanceToParent());
        log.debug("Distance of " + this.names[j] + " to new cluster "
                + this.names[nci] + " " + getCluster(j).getDistanceToParent());
    }

    /*
     * (non-Javadoc)
     *
     * @seemaltcms.commands.fragments.multiplealignment.ClusteringAlgorithm#
     * printDistMatrix()
     */
    public void printDistMatrix() {
        // System.out.println("Printing corrected Distance Matrix:");
        // System.out.println(java.util.Arrays.deepToString(this.D));
        log.info("Printing Distance Matrix:");
        log.debug(java.util.Arrays.deepToString(this.dist));
        for (int i = 0; i < this.cluster.size(); i++) {
            for (int j = i; j < this.cluster.size(); j++) {
                if (!this.usedIndices.contains(i)
                        && !this.usedIndices.contains(j)) {
                    log.info("d({},{})={}", new Object[]{
                        this.cluster.get(i).getName(),
                        this.cluster.get(j).getName(),
                        this.cluster.get(i).getDistanceTo(j)});
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @seemaltcms.commands.fragments.multiplealignment.ClusteringAlgorithm#
     * printNamesMatrix()
     */
    public void printNamesMatrix() {
        log.info("Printing names:");
        log.info(java.util.Arrays.toString(this.names));
    }

    public void putCluster(final int i, final BinaryCluster bc) {
        this.cluster.put(i, bc);
    }

    /**
     * @param chromatogramDistanceFunction
     */
    public void setChromatogramDistanceFunction(
            final PairwiseFeatureSequenceSimilarity chromatogramDistanceFunction) {
        this.chromatogramDistanceFunctionClass = chromatogramDistanceFunction;
    }

    /**
     * @param chromatogramWarpCommand
     */
    public void setChromatogramWarpCommand(
            final AFragmentCommand chromatogramWarpCommand) {
        this.chromatogramWarpCommandClass = chromatogramWarpCommand;
    }

    /**
     * @param cluster the cluster to set
     */
    public void setCluster(final HashMap<Integer, BinaryCluster> cluster) {
        this.cluster = cluster;
    }

    /**
     * @param clusterNames the clusterNames to set
     */
    public void setClusterNames(final String[] clusterNames) {
        this.clusterNames = clusterNames;
    }

    @Override
    public void setConsensus(final IFileFragment f) {
        this.consensus = f;
    }

    public void setd(final int i, final int j, final double v) {
        this.dist[i][j] = v;
        this.dist[j][i] = v;
    }

    /**
     * @param dist the dist to set
     */
    public void setDist(final double[][] dist) {
        this.dist = dist;
    }

    /**
     * @param fragments the fragments to set
     */
    public void setFragments(final HashMap<Integer, IFileFragment> fragments) {
        this.fragments = fragments;
    }

    @Override
    public void setInputFiles(final TupleND<IFileFragment> t) {
        this.inputFiles = t;
    }

    public void setL(final int l) {
        this.L = l;
    }

    public void setLDF(final PairwiseFeatureSequenceSimilarity ldf1) {
        this.chromatogramDistanceFunctionClass = ldf1;
    }

    /**
     * @param minimizeDist the minimizeDist to set
     */
    public void setMinimizeDist(final boolean minimizeDist) {
        this.minimizeDist = minimizeDist;
    }

    /**
     * @param minimizingArrayCompVariableName the
     * minimizingArrayCompVariableName to set
     */
    public void setMinimizingArrayCompVariableName(
            final String minimizingArrayCompVariableName) {
        this.minimizingArrayCompVariableName = minimizingArrayCompVariableName;
    }

    /**
     * @param names the names to set
     */
    public void setNames(final String[] names) {
        this.names = names;
    }

    /**
     * @param pairwiseDistanceMatrixVariableName the
     * pairwiseDistanceMatrixVariableName to set
     */
    public void setPairwiseDistanceMatrixVariableName(
            final String pairwiseDistanceMatrixVariableName) {
        this.pairwiseDistanceMatrixVariableName = pairwiseDistanceMatrixVariableName;
    }

    /**
     * @param pairwiseDistanceNamesVariableName the
     * pairwiseDistanceNamesVariableName to set
     */
    public void setPairwiseDistanceNamesVariableName(
            final String pairwiseDistanceNamesVariableName) {
        this.pairwiseDistanceNamesVariableName = pairwiseDistanceNamesVariableName;
    }
}
