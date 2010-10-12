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
 * $Id$
 */

package maltcms.commands.fragments.cluster;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import maltcms.commands.distances.ListDistanceFunction;
import maltcms.commands.fragments.warp.ChromatogramWarp;
import maltcms.datastructures.cluster.BinaryCluster;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;
import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Arrays;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.MAVector;
import annotations.Configurable;
import cross.Factory;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.io.csv.CSVWriter;
import cross.io.misc.StatsWriter;
import cross.tools.ArrayTools;
import cross.tools.EvalTools;
import cross.tools.FileTools;
import cross.tools.FragmentTools;
import cross.tools.ImageTools;
import cross.tools.StringTools;

/**
 * Abstract base class for clustering algorithms based on similarity or distance
 * matrices.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public abstract class ClusteringAlgorithm extends AFragmentCommand implements
        IClusteringAlgorithm {

	private String mass_values = "mass_values";

	private String intensity_values = "intensity_values";

	private String scan_index = "scan_index";

	@Override
	public IFileFragment getConsensus() {
		return this.consensus;
	}

	@Override
	public TupleND<IFileFragment> getInputFiles() {
		return this.inputFiles;
	}

	@Override
	public void setConsensus(IFileFragment f) {
		this.consensus = f;
	}

	@Override
	public void setInputFiles(TupleND<IFileFragment> t) {
		this.inputFiles = t;
	}

	private String total_intensity = "total_intensity";

	private String binned_intensity_values = "binned_intensity_values";

	private String binned_mass_values = "binned_mass_values";

	private String binned_scan_index = "binned_scan_index";

	private String mapping_file = "input_to_tmp_files.cdf",
	        mapping_file_var = "file_map";

	private String minvarname = "mass_range_min";

	private String maxvarname = "mass_range_max";

	private List<Double> maskedMasses = null;

	private boolean invertMaskedMasses;

	private double massBinResolution = 1.0d;

	boolean normalize_scans = false;

	private TupleND<IFileFragment> inputFiles = null;

	private IFileFragment consensus = null;

	// private double[][] D = null;

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

	Logger log = Logging.getLogger(this.getClass());

	private double[][] dist = null;

	private String[] names = null;

	private int L = -1;

	private HashSet<Integer> usedIndices = null;

	private HashMap<Integer, BinaryCluster> cluster = null;

	private HashMap<Integer, IFileFragment> fragments = null;

	// private int initial_names;

	private HashMap<Integer, Tuple2D<String, String>> nameToNameLookup = null;

	@Configurable(name = "guide.tree.distance")
	private ListDistanceFunction chromatogramDistanceFunction = null;

	private String[] clusterNames;

	@Configurable(name = "var.pairwise_distance_matrix")
	private String pairwiseDistanceMatrixVariableName = "pairwise_distance_matrix";

	@Configurable(name = "var.pairwise_distance_names")
	private String pairwiseDistanceNamesVariableName = "pairwise_distance_names";

	@Configurable(name = "var.minimizing_array_comp")
	private String minimizingArrayCompVariableName = "minimizing_array_comp";

	@Configurable(name = "maltcms.commands.fragments.warp.AWarp")
	private AFragmentCommand chromatogramWarpCommand = null;

	private TupleND<IFileFragment> initialFragments = null;

	// minimize or maximize
	private boolean minimizeDist = true;

	private boolean drawTICs;

	private boolean drawEICs;

	public ClusteringAlgorithm() {

	}

	public void addNodeK(final int i, final int j, final int k) {

		this.names[k] = "((" + this.names[i] + ")" + this.names[j] + ")";
		this.clusterNames[k] = this.clusterNames[i] + "_vs_"
		        + this.clusterNames[j];
		this.nameToNameLookup.put(k, new Tuple2D<String, String>(
		        this.clusterNames[i], this.clusterNames[j]));
		this.log.debug("Merging cluster " + i + " (" + this.names[i] + ") and "
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
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		setInputFiles(t);
		if (this.drawTICs) {
			final File utics = ImageTools.drawTICS(this.getClass(), t, Factory
			        .getInstance().getConfiguration().getString(
			                "var.total_intensity", "total_intensity"), Factory
			        .getInstance().getConfiguration().getString(
			                "var.scan_acquisition_time",
			                "scan_acquisition_time"), null,
			        "unaligned-tics.png", getIWorkflow().getStartupDate());
			DefaultWorkflowResult dwrut = new DefaultWorkflowResult(utics,
			        this, WorkflowSlot.VISUALIZATION);
			getIWorkflow().append(dwrut);
		}

		if (this.drawEICs) {
			final List<IFileFragment> unalignedEICFragments = MaltcmsTools
			        .prepareEICFragments(t, this.getClass(), getIWorkflow()
			                .getStartupDate());

			final File[] files = ImageTools.drawEICs(this.getClass(),
			        new TupleND<IFileFragment>(unalignedEICFragments), Factory
			                .getInstance().getConfiguration().getString(
			                        "var.scan_acquisition_time",
			                        "scan_acquisition_time"), null,
			        "unaligned", getIWorkflow().getStartupDate());
			for (final File file : files) {
				DefaultWorkflowResult dwrut = new DefaultWorkflowResult(file,
				        this, WorkflowSlot.VISUALIZATION);
				getIWorkflow().append(dwrut);
			}
		}
		final IFileFragment pwd = MaltcmsTools.getPairwiseDistanceFragment();
		init(pwd, t);
		merge();
		final ArrayList<IFileFragment> al = new ArrayList<IFileFragment>(
		        this.fragments.size());
		this.log.debug("Finished merging!");
		for (int i = 0; i < this.fragments.size(); i++) {
			final IFileFragment ff = this.fragments.get(i);
			// al.add(ff);
			if (!(new File(ff.getAbsolutePath()).exists())) {
				ff.save();
			}
			this.log.debug("File i={} with name {}", i, ff.getAbsolutePath());
		}
		al.add(getConsensus());
		for (IFileFragment iff : getInputFiles()) {
			if (!(new File(iff.getAbsolutePath()).exists())) {
				iff.save();
			}
			al.add(iff);
		}
		this.log.info("Returned FileFragments: {}", al);
		final File graphml = FileTools.prependDefaultDirs("maltcms.graphml",
		        this.getClass(), getIWorkflow().getStartupDate());
		try {
			final BufferedWriter bw = new BufferedWriter(
			        new FileWriter(graphml));
			bw.write(ClusteringAlgorithm.toGraphML(this));
			bw.flush();
			bw.close();
		} catch (final IOException e) {
			this.log.error(e.getLocalizedMessage());
		}
		final File newick = FileTools.prependDefaultDirs("maltcms.newick", this
		        .getClass(), getIWorkflow().getStartupDate());
		try {
			final BufferedWriter bw = new BufferedWriter(new FileWriter(newick));
			bw.write(this.cluster.get(this.dist.length - 1).toNewick() + ";");
			bw.flush();
			bw.close();
		} catch (final IOException e) {
			this.log.error(e.getLocalizedMessage());
		}
		return new TupleND<IFileFragment>(al);
	}

	@Override
	public void configure(final Configuration cfg) {
		this.pairwiseDistanceMatrixVariableName = cfg.getString(
		        "var.pairwise_distance_matrix", "pairwise_distance_matrix");
		this.pairwiseDistanceNamesVariableName = cfg.getString(
		        "var.pairwise_distance_names", "pairwise_distance_names");
		this.chromatogramDistanceFunction = Factory.getInstance().instantiate(
		        cfg.getString("guide.tree.distance",
		                "maltcms.commands.distances.MZIDynamicTimeWarp"),
		        ListDistanceFunction.class);
		this.chromatogramWarpCommand = Factory.getInstance().instantiate(
		        cfg.getString("maltcms.commands.fragments.warp.AWarp",
		                "maltcms.commands.fragments.warp.PathWarp"),
		        AFragmentCommand.class);

		this.minimizingArrayCompVariableName = cfg.getString(
		        "var.minimizing_array_comp", "minimizing_array_comp");
		this.mass_values = cfg.getString("var.mass_values", "mass_values");
		this.intensity_values = cfg.getString("var.intensity_values",
		        "intensity_values");
		this.total_intensity = cfg.getString("var.total_intensity",
		        "total_intensity");
		this.minvarname = cfg.getString("var.mass_range_min", "mass_range_min");
		this.maxvarname = cfg.getString("var.mass_range_max", "mass_range_max");
		this.scan_index = cfg.getString("var.scan_index", "scan_index");
		this.binned_intensity_values = cfg.getString(
		        "var.binned_intensity_values", "binned_intensity_values");
		this.binned_mass_values = cfg.getString("var.binned_mass_values",
		        "binned_mass_values");
		this.binned_scan_index = cfg.getString("var.binned_scan_index",
		        "binned_scan_index");
		this.mapping_file = cfg.getString("input_to_tmp_files_file_name",
		        "input_to_tmp_files.cdf");
		this.mapping_file_var = cfg.getString("var.file_map", "file_map");
		this.normalize_scans = cfg.getBoolean(this.getClass().getName()
		        + ".normalizeScans", false);
		this.maskedMasses = MaltcmsTools.parseMaskedMassesList(cfg.getList(this
		        .getClass().getName()
		        + ".maskMasses", Collections.emptyList()));
		this.invertMaskedMasses = cfg.getBoolean(this.getClass().getName()
		        + ".invertMaskedMasses", false);
		this.massBinResolution = cfg.getDouble(
		        "dense_arrays.massBinResolution", 1.0d);
		this.drawTICs = cfg.getBoolean(
		        "cross.tools.ImageTools.createTICCharts", false);
		this.drawEICs = cfg.getBoolean(
		        "cross.tools.ImageTools.createEICCharts", false);
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
	 * @return the chromatogramDistanceFunction
	 */
	public ListDistanceFunction getChromatogramDistanceFunction() {
		return this.chromatogramDistanceFunction;
	}

	/**
	 * @return the chromatogramWarpCommand
	 */
	public AFragmentCommand getChromatogramWarpCommand() {
		return this.chromatogramWarpCommand;
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
	public BinaryCluster getCluster(final int i) {
		return this.cluster.get(i);
	}

	/**
	 * @return the clusterNames
	 */
	public String[] getClusterNames() {
		return this.clusterNames;
	}

	public Set<Entry<Integer, BinaryCluster>> getClusters() {
		return Collections.unmodifiableSet(this.cluster.entrySet());
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
	public HashMap<Integer, IFileFragment> getFragments() {
		return this.fragments;
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

	// private Merge merge = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.commands.fragments.cluster.IClusteringAlgorithm#getNames()
	 */
	public String[] getNames() {
		return this.names;
	}

	private IFileFragment getOriginalFileFor(final IFileFragment iff) {
		final List<IFileFragment> l = Factory.getInitialFiles();
		final String iffname = StringTools.removeFileExt(iff.getName());
		for (final IFileFragment f : l) {
			final String fname = StringTools.removeFileExt(f.getName());

			if (iffname.equals(fname)) {
				return f;
			}
		}
		// throw new ConstraintViolationException(
		this.log.warn("Could not find original file, returning argument!");
		return iff;
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
		EvalTools.notNull(this.chromatogramDistanceFunction, this);
		final long t_start = System.currentTimeMillis();
		final IFileFragment dtw = this.chromatogramDistanceFunction.apply(ff1,
		        ff2);
		DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(dtw
		        .getAbsolutePath()), this, WorkflowSlot.ALIGNMENT);
		getIWorkflow().append(dwr);
		this.chromatogramWarpCommand.setIWorkflow(getIWorkflow());
		final IFileFragment warped = this.chromatogramWarpCommand.apply(
		        new TupleND<IFileFragment>(dtw)).get(0);

		final long t_end = System.currentTimeMillis() - t_start;
		final StatsMap sm = new StatsMap(FileFragmentFactory.getInstance()
		        .create(
		                FileTools.prependDefaultDirs("NJ_"
		                        + StringTools.removeFileExt(ff1.getName())
		                        + "_"
		                        + StringTools.removeFileExt(ff2.getName())
		                        + ".csv", this.getClass(), getIWorkflow()
		                        .getStartupDate())));
		sm.put("time", new Double(t_end));
		final StatsWriter sw = Factory.getInstance().instantiate(
		        StatsWriter.class);
		sw.setIWorkflow(getIWorkflow());
		sw.write(sm);
		dwr = new DefaultWorkflowResult(new File(warped.getAbsolutePath()),
		        this, WorkflowSlot.WARPING);
		getIWorkflow().append(dwr);

		IFileFragment res = createDenseArrays(warped);

		this.log.debug("{}", ff1.toString());
		this.log.debug("{}", warped.toString());
		this.log.debug("{}", res);
		// FileFragment merged = this.merge.mergeAll(ff1, warped);

		this.fragments.put(k, res);
		// this.fragments.get(k).save();

	}

	private IFileFragment createDenseArrays(IFileFragment iff) {

		TupleND<IFileFragment> t = new TupleND<IFileFragment>(iff);
		this.log.debug("Creating dense arrays!");
		final ArrayList<Tuple2D<IFileFragment, IFileFragment>> map = new ArrayList<Tuple2D<IFileFragment, IFileFragment>>();
		this.log.debug("Looking for minimum and maximum values!");
		final Tuple2D<Double, Double> minmax = MaltcmsTools.findGlobalMinMax(t,
		        this.minvarname, this.maxvarname, this.mass_values);
		EvalTools.notNull(minmax, this);
		this.log.debug("Minimum mass: {}; Maximum mass; {}", minmax.getFirst(),
		        minmax.getSecond());
		final IWorkflowElement iwe = this;
		ArrayList<IFileFragment> al = new ArrayList<IFileFragment>();
		for (final IFileFragment ff : t) {
			// Runnable r = new Runnable() {
			// @Override
			// public void run() {
			this.log.debug("Loading scans for file {}", ff.getName());
			IFileFragment f = MaltcmsTools.prepareDenseArraysMZI(ff,
			        this.scan_index, this.mass_values, this.intensity_values,
			        this.binned_scan_index, this.binned_mass_values,
			        this.binned_intensity_values, minmax.getFirst(), minmax
			                .getSecond(), getIWorkflow().getStartupDate());
			// f.save();
			this.log.debug("Loaded scans for file {}, stored in {}", ff, f);
			this.log.debug("Source Files of f={} : {}", f, f.getSourceFiles());
			final int bins = MaltcmsTools.getNumberOfIntegerMassBins(minmax
			        .getFirst(), minmax.getSecond(), this.massBinResolution);
			// set masked masschannels to zero intensity
			if ((this.maskedMasses != null) && !this.maskedMasses.isEmpty()) {
				this.log.info("Filtering masked masses!");
				final ArrayDouble.D1 selector = new ArrayDouble.D1(bins);
				if (this.invertMaskedMasses) {
					ArrayTools.fill(selector, 1.0d);
					for (final Double integ : this.maskedMasses) {
						this.log.info("Retaining mass {} at index {}", integ,
						        MaltcmsTools.binMZ(integ, minmax.getFirst(),
						                minmax.getSecond(),
						                this.massBinResolution));

						selector.set(MaltcmsTools.binMZ(integ, minmax
						        .getFirst(), minmax.getSecond(),
						        this.massBinResolution), 0.0d);
						// - (int) (Math.floor(minmax.getFirst())), 0.0d);
					}
				} else {
					for (final Double integ : this.maskedMasses) {
						this.log.info("Filtering mass {} at index {}", integ,
						        MaltcmsTools.binMZ(integ, minmax.getFirst(),
						                minmax.getSecond(),
						                this.massBinResolution));
						selector.set(MaltcmsTools.binMZ(integ, minmax
						        .getFirst(), minmax.getSecond(),
						        this.massBinResolution), 1.0d);
						// - (int) (Math.floor(minmax.getFirst())), 1.0d);
					}
				}
				final IVariableFragment ivf = f
				        .getChild(this.binned_intensity_values);
				final IVariableFragment sidx = f
				        .getChild(this.binned_scan_index);
				final IVariableFragment total_intens = f
				        .getChild(this.total_intensity);
				final Array ta = total_intens.getArray();
				final Array tan = Array.factory(ta.getElementType(), ta
				        .getShape());
				final Index taidx = ta.getIndex();
				final Index tanidx = tan.getIndex();
				ivf.setIndex(sidx);
				final List<Array> intens = ivf.getIndexedArray();
				// Over all scans
				int scan = 0;
				final ArrayList<Array> filtered = new ArrayList<Array>(intens
				        .size());
				for (final Array a : intens) {
					final Index aidx = a.getIndex();
					EvalTools.eqI(1, a.getRank(), this);
					double accum = 0;
					for (int i = 0; i < a.getShape()[0]; i++) {
						if (selector.get(i) == 1.0d) {
							// log.info("Selector index {} = 1.0",i);
							aidx.set(i);
							accum += a.getDouble(aidx);
							a.setDouble(aidx, 0);
						}
					}
					this.log.debug("accumulated intensity = {} in scan {}",
					        accum, scan);
					taidx.set(scan);
					tanidx.set(scan);
					final double prev = ta.getDouble(taidx);
					this.log.debug("previous {}, new {}", prev, prev - accum);
					tan.setDouble(tanidx, prev - accum);
					filtered.add(a);
					scan++;
				}
				ivf.setIndexedArray(filtered);
				total_intens.setArray(tan);
			}
			if (this.normalize_scans) {
				this.log.info("Normalizing scans to length 1");
				final IVariableFragment ivf = f
				        .getChild(this.binned_intensity_values);
				final IVariableFragment sidx = f
				        .getChild(this.binned_scan_index);
				ivf.setIndex(sidx);
				final List<Array> intens = ivf.getIndexedArray();
				final List<Array> normIntens = new ArrayList<Array>();
				for (final Array a : intens) {
					final MAVector ma = new MAVector(a);
					final double norm = ma.norm();
					normIntens.add(ArrayTools.mult(a, 1.0d / norm));
				}
				ivf.setIndexedArray(normIntens);
			}
			// MaltcmsTools
			// .buildBinaryMassVectors(f, binary_mass_values,
			// mass_values, intensity_values, scan_index,
			// maskedMasses);
			this.log.debug("Created IFileFragment {}", f);
			// MaltcmsTools.loadDefaultVars(f);
			// FragmentTools.loadAdditionalVars(f);
			this.log.debug("Adding pair {},{}", ff, f);
			map.add(new Tuple2D<IFileFragment, IFileFragment>(ff, f));

			this.log.debug("{}", FileFragment.printFragment(f));
			f.save();
			al.add(f);
			final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
			        new File(f.getAbsolutePath()), iwe,
			        WorkflowSlot.GENERAL_PREPROCESSING);
			getIWorkflow().append(dwr);
		}
		return al.get(0);
	}

	public void init(final double[][] distances, final String[] names1,
	        final TupleND<IFileFragment> fragments1) {
		this.usedIndices = new HashSet<Integer>();
		this.nameToNameLookup = new HashMap<Integer, Tuple2D<String, String>>();
		final int numjoins = (distances.length) * 2 - 1;
		if (fragments1 != null) {
			this.fragments = new HashMap<Integer, IFileFragment>();
		}
		this.clusterNames = new String[numjoins];
		this.log.debug("Total expected number of elements: " + numjoins);
		setL(distances.length);
		// this.D = new double[numjoins][numjoins];
		this.dist = new double[numjoins][numjoins];
		for (int i = 0; i < numjoins; i++) {
			for (int j = 0; j < numjoins; j++) {
				this.dist[i][j] = Double.NaN;
			}
		}
		this.cluster = new HashMap<Integer, BinaryCluster>();
		for (int i = 0; i < distances.length; i++) {
			System
			        .arraycopy(distances[i], 0, this.dist[i], 0,
			                distances.length);
			final BinaryCluster njc = new BinaryCluster(names1[i], i,
			        this.dist[i]);
			if (fragments1 != null) {
				this.fragments.put(i, fragments1.get(i));
			}
			this.cluster.put(i, njc);
			// System.out.println("Adding new ICluster: "+i+" "+names[i]);
			this.log.debug("Adding initial ICluster (" + (i + 1) + "/"
			        + distances.length + ") : " + njc.toString());
		}
		this.names = new String[numjoins];
		System.arraycopy(names1, 0, this.names, 0, names1.length);
		System.arraycopy(names1, 0, this.clusterNames, 0, names1.length);
		this.log.debug("Starting with the following clusters: {}", Arrays
		        .toString(this.names));
		// System.out.println("Number of joins: "+numjoins);
		// System.out.println("Initial number of clusters: "+this.L);
		printDistMatrix();
		this.chromatogramDistanceFunction.setIWorkflow(getIWorkflow());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seemaltcms.commands.fragments.cluster.IClusteringAlgorithm#init(cross.
	 * datastructures.fragments.FileFragment,
	 * cross.datastructures.tuple.TupleND)
	 */
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
		EvalTools.notNull(new Object[] { pwdist, names1 }, this);
		final double[][] distances = new double[pwdist.getShape()[0]][pwdist
		        .getShape()[1]];
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
		this.log.debug(sb.toString());
		this.initialFragments = new TupleND<IFileFragment>(t);
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
	public void merge() {
		this.log
		        .info("#############################################################################");
		this.log.info("# Running " + this.getClass().getName()
		        + " as clustering algorithm");
		this.log
		        .info("#############################################################################");
		final String ticVar = Factory.getInstance().getConfiguration()
		        .getString("var.total_intensity", "total_intensity");
		final String satVar = Factory
		        .getInstance()
		        .getConfiguration()
		        .getString("var.scan_acquisition_time", "scan_acquisition_time");
		// File utics = ImageTools.drawTICS(this.getClass(), new
		// TupleND<IFileFragment
		// >(initialFragments),ticVar,satVar,null,"unaligned-tics.png");
		// DefaultWorkflowResult dwrat = new DefaultWorkflowResult(utics, this,
		// WorkflowSlot.VISUALIZATION);
		// getIWorkflow().append(dwrat);
		// File[] files = ImageTools.drawEICs(this.getClass(), new
		// TupleND<IFileFragment>(initialFragments),
		// Factory.getInstance().getConfiguration
		// ().getString("var.scan_acquisition_time","scan_acquisition_time"),
		// null, new Double[]{73.0,120.0,150.0,200.0},1.0d, "unaligned");
		// for(File file:files) {
		// dwrat = new DefaultWorkflowResult(file, this,
		// WorkflowSlot.VISUALIZATION);
		// getIWorkflow().append(dwrat);
		// }
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
		this.log.debug(this.cluster.get(this.dist.length - 1).toNewick() + ";");
		printDistMatrix();
		setConsensus(this.fragments.get(Integer.valueOf(this.dist.length - 1)));
		// System.out.println(toGraphML());
		// for(int i=0;i<)
		// final IFileFragment representative = getFragments().get(
		// this.dist.length - 1);

		// for (final IFileFragment ff : this.initialFragments) {
		// final IFileFragment aligned = this.chromatogramDistanceFunction
		// .apply(representative, ff);
		// al.add(aligned);
		// fragments.add(ff);
		// this.log.debug("Name of alignment file {}", aligned
		// .getAbsolutePath());
		// }
		//
		// // final List<IFileFragment> alignedFrags =
		// // saveAlignment(this.getClass(),
		// // this.initialFragments, new TupleND<IFileFragment>(al), ticVar,
		// // representative);
		// final List<IFileFragment> alignedFrags =
		// saveAlignment(this.getClass(),
		// this.initialFragments, new TupleND<IFileFragment>(al), ticVar,
		// representative);
		// if (this.drawTICs) {
		// final File atics = ImageTools.drawTICS(this.getClass(),
		// new TupleND<IFileFragment>(alignedFrags), Factory
		// .getInstance().getConfiguration().getString(
		// "var.total_intensity", "total_intensity"),
		// Factory.getInstance().getConfiguration().getString(
		// "var.scan_acquisition_time",
		// "scan_acquisition_time"), representative,
		// "aligned-tics.png", getIWorkflow().getStartupDate());
		// DefaultWorkflowResult dwrat2 = new DefaultWorkflowResult(atics,
		// this, WorkflowSlot.VISUALIZATION);
		// getIWorkflow().append(dwrat2);
		// }
		// if (this.drawEICs) {
		// final File[] files = ImageTools.drawEICs(this.getClass(),
		// new TupleND<IFileFragment>(alignedFrags), Factory
		// .getInstance().getConfiguration().getString(
		// "var.scan_acquisition_time",
		// "scan_acquisition_time"), representative,
		// "aligned", getIWorkflow().getStartupDate());
		// for (final File file : files) {
		// DefaultWorkflowResult dwrat2 = new DefaultWorkflowResult(file,
		// this, WorkflowSlot.VISUALIZATION);
		// getIWorkflow().append(dwrat2);
		// }
		// }
		// // clean up
		// for (final IFileFragment iff : alignedFrags) {
		// iff.clearArrays();
		// }
		// Factory.getInstance().getConfiguration().setProperty("output.basedir",
		// basedir);
		// Factory.getInstance().getConfiguration().setProperty(
		// "omitUserTimePrefix", Boolean.valueOf(false));
		// ImageTools.drawAlignedTICS(this.getClass(), new
		// TupleND<IFileFragment>(
		// fragments), new
		// TupleND<IFileFragment>(al),ticVar,satVar,representative);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.commands.fragments.multiplealignment.ClusteringAlgorithm#printCluster
	 * ()
	 */
	public void printCluster() {
		this.log.info("Printing cluster:");
		final Set<Integer> s = this.cluster.keySet();
		for (final Integer i : s) {
			this.log.info(this.cluster.get(i).toString());
		}
	}

	public void printDistanceToNewCluster(final int i, final int j,
	        final int nci) {
		this.log.debug("Distance of " + this.names[i] + " to new cluster "
		        + this.names[nci] + " " + getCluster(i).getDistanceToParent());
		this.log.debug("Distance of " + this.names[j] + " to new cluster "
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
		this.log.info("Printing Distance Matrix:");
		this.log.debug(java.util.Arrays.deepToString(this.dist));
		for (int i = 0; i < this.cluster.size(); i++) {
			for (int j = i; j < this.cluster.size(); j++) {
				if (!this.usedIndices.contains(i)
				        && !this.usedIndices.contains(j)) {
					this.log.info("d({},{})={}", new Object[] {
					        this.cluster.get(i).getName(),
					        this.cluster.get(j).getName(),
					        this.cluster.get(i).getDistanceTo(j) });
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
		this.log.info("Printing names:");
		this.log.info(java.util.Arrays.toString(this.names));
	}

	public void putCluster(final int i, final BinaryCluster bc) {
		this.cluster.put(i, bc);
	}

	private List<IFileFragment> saveAlignment(final Class<?> creator,
	        final TupleND<IFileFragment> t,
	        final TupleND<IFileFragment> alignment, final String ticvar,
	        final IFileFragment top) {
		// Set representative name
		final String refname = top.getName();
		// List to hold alignments, where representative is LHS
		final ArrayList<IFileFragment> repOnLHS = new ArrayList<IFileFragment>();
		// List to hold alignment, where representative is RHS
		final ArrayList<IFileFragment> repOnRHS = new ArrayList<IFileFragment>();
		// List of warped files
		final List<IFileFragment> warped = new ArrayList<IFileFragment>();
		// warped.add(top);
		final ChromatogramWarp cw = Factory.getInstance().instantiate(
		        ChromatogramWarp.class);
		final IFileFragment centerCopy = cw.copyReference(
		        getOriginalFileFor(top), getIWorkflow());
		centerCopy.addSourceFile(top);
		DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(
		        centerCopy.getAbsolutePath()), this, WorkflowSlot.WARPING);
		getIWorkflow().append(dwr);
		warped.add(centerCopy);

		for (final IFileFragment iff : alignment) {
			final IFileFragment ref = FragmentTools.getLHSFile(iff);
			if (ref.getName().equals(refname)) {
				repOnLHS.add(iff);
			} else if (refname.equals(FragmentTools.getRHSFile(iff).getName())) {
				repOnRHS.add(iff);
			}
		}
		final ArrayList<Tuple2D<String, Array>> a = new ArrayList<Tuple2D<String, Array>>(
		        repOnLHS.size() + repOnRHS.size() + 1);
		this.log.debug(
		        "#alignments to reference: {}, #alignments to query: {}",
		        repOnLHS.size(), repOnRHS.size());
		String topName = StringTools.removeFileExt(top.getName());
		if (alignment.size() > 1) {// only add a star if we have more than one
			// alignment
			topName = topName + " *";
		}
		a.add(new Tuple2D<String, Array>(topName, top.getChild(ticvar)
		        .getArray()));
		final Vector<Vector<String>> table = new Vector<Vector<String>>();
		final Array topa = top.getChild(ticvar).getArray();
		final Vector<String> topCol = new Vector<String>(topa.getShape()[0] + 1);
		topCol.add(StringTools.removeFileExt(top.getName()));
		for (int i = 0; i < topa.getShape()[0]; i++) {
			topCol.add(i + "");
		}
		table.add(topCol);
		// top/representative is on lhs side of alignment
		for (int i = 0; i < repOnLHS.size(); i++) {
			final List<Tuple2DI> al = MaltcmsTools.getWarpPath(repOnLHS.get(i));
			final IFileFragment ifwarped = cw.warp(top,
			        getOriginalFileFor(FragmentTools
			                .getRHSFile(repOnLHS.get(i))), FragmentTools
			                .getRHSFile(repOnLHS.get(i)), al, true,
			        getIWorkflow());
			dwr = new DefaultWorkflowResult(
			        new File(ifwarped.getAbsolutePath()), this,
			        WorkflowSlot.WARPING);
			getIWorkflow().append(dwr);
			warped.add(ifwarped);
			final String name = StringTools.removeFileExt(FragmentTools
			        .getRHSFile(repOnLHS.get(i)).getName());
			final Vector<String> column = new Vector<String>();
			column.add(name);
			int lastLIndex = 0;
			int mapCount = 0;
			final double value = 0;
			StringBuffer sb = new StringBuffer(1);
			// map from right to left
			// if multiple elements of right are mapped to single left
			// add as comma separated list
			// else if multiple elements of left are mapped to single right
			// insert right value accordingly often
			for (final Tuple2DI tpl : al) {
				this.log.debug("value={}", value);
				if (mapCount == 0) {
					column.add(tpl.getSecond() + "");
					lastLIndex = tpl.getFirst();
				} else {
					// compression from rhs to lhs, last element was start of
					// compression
					if (lastLIndex == tpl.getFirst()) {
						sb.append(tpl.getSecond() + ",");
					} else {// different lhs indices, leaving compression range
						if (sb.length() > 0) {// StringBuffer length > 1 =>
							// compression
							sb.append(tpl.getSecond());
							column.add(sb.toString());// add compression range
							// to column
							sb = new StringBuffer(1);// reset StringBuffer
						} else {// String Buffer length == 0, match
							column.add(sb.append(tpl.getSecond()).toString());
							sb = new StringBuffer(1);
						}
					}
					lastLIndex = tpl.getFirst();
				}
				mapCount++;
			}
			table.add(column);
		}
		// Alignments where rep is on rhs
		// so retrieve warp path and lhs array and warp to rhs array via path

		for (int i = 0; i < repOnRHS.size(); i++) {
			final List<Tuple2DI> al = MaltcmsTools.getWarpPath(repOnRHS.get(i));
			final IFileFragment ifwarped = cw.warp(top,
			        getOriginalFileFor(FragmentTools
			                .getLHSFile(repOnRHS.get(i))), FragmentTools
			                .getLHSFile(repOnRHS.get(i)), al, false,
			        getIWorkflow());
			dwr = new DefaultWorkflowResult(
			        new File(ifwarped.getAbsolutePath()), this,
			        WorkflowSlot.WARPING);
			getIWorkflow().append(dwr);
			warped.add(ifwarped);
			final String name = StringTools.removeFileExt(FragmentTools
			        .getLHSFile(repOnRHS.get(i)).getName());
			final Vector<String> column = new Vector<String>();
			column.add(name);
			int lastRIndex = 0;
			int mapCount = 0;
			final double value = 0;
			StringBuffer sb = new StringBuffer(1);
			// map from right to left
			// if multiple elements of right are mapped to single left
			// add as comma separated list
			// else if multiple elements of left are mapped to single right
			// insert right value accordingly often
			for (final Tuple2DI tpl : al) {
				this.log.debug("value={}", value);
				if (mapCount == 0) {
					column.add(tpl.getFirst() + "");
					lastRIndex = tpl.getSecond();
				} else {
					// compression from rhs to lhs, last element was start of
					// compression
					if (lastRIndex == tpl.getSecond()) {
						sb.append(tpl.getFirst() + ",");
					} else {// different lhs indices, leaving compression range
						if (sb.length() > 0) {// StringBuffer length > 1 =>
							// compression
							sb.append(tpl.getFirst());
							column.add(sb.toString());// add compression range
							// to column
							sb = new StringBuffer(1);// reset StringBuffer
						} else {// String Buffer length == 0, match
							column.add(sb.append(tpl.getFirst()).toString());
							sb = new StringBuffer(1);
						}
					}
					lastRIndex = tpl.getSecond();
				}
				mapCount++;
			}
			table.add(column);
		}
		final CSVWriter csv = Factory.getInstance()
		        .instantiate(CSVWriter.class);
		csv.setIWorkflow(getIWorkflow());
		csv.writeTableByCols(FileTools.prependDefaultDirs(creator,
		        getIWorkflow().getStartupDate()).getAbsolutePath(),
		        "multiple-alignment.csv", table, WorkflowSlot.ALIGNMENT);
		return warped;
	}

	/**
	 * @param chromatogramDistanceFunction
	 *            the chromatogramDistanceFunction to set
	 */
	public void setChromatogramDistanceFunction(
	        final ListDistanceFunction chromatogramDistanceFunction) {
		this.chromatogramDistanceFunction = chromatogramDistanceFunction;
	}

	/**
	 * @param chromatogramWarpCommand
	 *            the chromatogramWarpCommand to set
	 */
	public void setChromatogramWarpCommand(
	        final AFragmentCommand chromatogramWarpCommand) {
		this.chromatogramWarpCommand = chromatogramWarpCommand;
	}

	/**
	 * @param cluster
	 *            the cluster to set
	 */
	public void setCluster(final HashMap<Integer, BinaryCluster> cluster) {
		this.cluster = cluster;
	}

	/**
	 * @param clusterNames
	 *            the clusterNames to set
	 */
	public void setClusterNames(final String[] clusterNames) {
		this.clusterNames = clusterNames;
	}

	public void setd(final int i, final int j, final double v) {
		this.dist[i][j] = v;
		this.dist[j][i] = v;
	}

	/**
	 * @param dist
	 *            the dist to set
	 */
	public void setDist(final double[][] dist) {
		this.dist = dist;
	}

	/**
	 * @param fragments
	 *            the fragments to set
	 */
	public void setFragments(final HashMap<Integer, IFileFragment> fragments) {
		this.fragments = fragments;
	}

	public void setL(final int l) {
		this.L = l;
	}

	public void setLDF(final ListDistanceFunction ldf1) {
		this.chromatogramDistanceFunction = ldf1;
	}

	/**
	 * @param minimizeDist
	 *            the minimizeDist to set
	 */
	public void setMinimizeDist(final boolean minimizeDist) {
		this.minimizeDist = minimizeDist;
	}

	/**
	 * @param minimizingArrayCompVariableName
	 *            the minimizingArrayCompVariableName to set
	 */
	public void setMinimizingArrayCompVariableName(
	        final String minimizingArrayCompVariableName) {
		this.minimizingArrayCompVariableName = minimizingArrayCompVariableName;
	}

	/**
	 * @param names
	 *            the names to set
	 */
	public void setNames(final String[] names) {
		this.names = names;
	}

	/**
	 * @param pairwiseDistanceMatrixVariableName
	 *            the pairwiseDistanceMatrixVariableName to set
	 */
	public void setPairwiseDistanceMatrixVariableName(
	        final String pairwiseDistanceMatrixVariableName) {
		this.pairwiseDistanceMatrixVariableName = pairwiseDistanceMatrixVariableName;
	}

	/**
	 * @param pairwiseDistanceNamesVariableName
	 *            the pairwiseDistanceNamesVariableName to set
	 */
	public void setPairwiseDistanceNamesVariableName(
	        final String pairwiseDistanceNamesVariableName) {
		this.pairwiseDistanceNamesVariableName = pairwiseDistanceNamesVariableName;
	}

}
