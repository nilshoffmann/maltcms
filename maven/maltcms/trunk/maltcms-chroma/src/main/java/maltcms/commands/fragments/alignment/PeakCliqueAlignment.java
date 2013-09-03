/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.commands.fragments.alignment;

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;
import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import maltcms.datastructures.peak.Clique;
import maltcms.datastructures.peak.IPeak;
import maltcms.io.csv.CSVWriter;
import maltcms.statistics.OneWayPeakAnova;
import maltcms.tools.ArrayTools;
import maltcms.ui.charts.PlotRunner;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.tools.StringTools;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.BBHPeakEdgeList;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.CliqueTable;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.IWorkerFactory;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.PeakComparator;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.PeakSimilarityVisualizer;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.UnmatchedPeaksList;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.WorkerFactory;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.peakFactory.IPeakFactory;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.peakFactory.IPeakFactoryImpl;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.peakFactory.Peak1DMSFactory;
import maltcms.datastructures.alignment.AlignmentFactory;
import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.ms.Metabolite;
import maltcms.datastructures.peak.PeakEdge;
import maltcms.datastructures.peak.PeakNG;
import maltcms.io.xml.bindings.alignment.Alignment;
import maltcms.tools.MaltcmsTools;
import net.sf.mpaxs.api.ICompletionService;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.MAMath;

/**
 * For every peak in each chromatogram, its bi-directional best hits are
 * determined, and all bi-directional best hits are merged. If they apply, they
 * cover a set of k peaks in k chromatograms.
 *
 * @author Nils Hoffmann
 *
 *
 */
@RequiresVariables(names = {"var.binned_mass_values",
	"var.binned_intensity_values", "var.binned_scan_index",
	"var.scan_acquisition_time", "var.mass_values", "var.intensity_values",
	"var.scan_index"})
@RequiresOptionalVariables(names = {"var.tic_peaks",
	"var.first_column_elution_time", "var.second_column_elution_time", "var.peak_area"})
@ProvidesVariables(names = {"var.anchors.retention_index_names",
	"var.anchors.retention_times", "var.anchors.retention_indices",
	"var.anchors.retention_scans"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class PeakCliqueAlignment extends AFragmentCommand {

//    private IScalarArraySimilarity similarityFunction;
	@Configurable(name = "var.tic_peaks")
	private String ticPeaks = "tic_peaks";
	@Configurable(name = "var.mass_values")
	private String massValues = "mass_values";
	@Configurable(name = "var.scan_index")
	private String scanIndex = "scan_index";
	@Configurable(name = "var.intensity_values")
	private String intensityValues = "intensity_values";
	@Configurable(name = "var.retention_index_names")
	private String anchorNames = "retention_index_names";
	@Configurable(name = "var.retention_times")
	private String anchorTimes = "retention_times";
	@Configurable(name = "var.retention_indices")
	private String anchorRetentionIndex = "retention_indices";
	@Configurable(name = "var.retention_scans")
	private String anchorScanIndex = "retention_scans";
	@Configurable(name = "var.binned_intensity_values")
	private String binnedIntensities = "binned_intensity_values";
	@Configurable(name = "var.binned_scan_index")
	private String binnedScanIndex = "binned_scan_index";
	@Configurable(name = "var.scan_acquisition_time")
	private String scanAcquisitionTime = "scan_acquisition_time";
	@Configurable(name = "var.peak_area")
	private String peakAreaVariable = "peak_area";
	@Configurable
	private boolean useUserSuppliedAnchors = false;
	@Configurable
	private int minCliqueSize = -1;
	@Configurable
	private boolean savePeakSimilarities = false;
	@Configurable
	private boolean savePeakMatchRTTable = true;
	@Configurable
	private boolean savePeakMatchAreaTable = true;
	@Configurable
	private boolean savePeakMatchAreaPercentTable = true;
	@Configurable
	private boolean saveXMLAlignment = true;
	@Deprecated
	@Configurable
	private int maxBBHErrors = 0;
	@Configurable
	private double minBbhFraction = 1.0d;
	@Configurable
	private boolean savePlots = false;
	@Configurable
	private boolean saveUnmatchedPeaks = false;
	@Configurable
	private boolean saveIncompatiblePeaks = false;
	@Configurable
	private boolean saveUnassignedPeaks = false;
	@Configurable
	private boolean useSparseArrays = false;
	@Configurable
	private boolean use2DRetentionTimes = false;
	@Configurable
	private IWorkerFactory workerFactory = new WorkerFactory();
	@Configurable
	private IPeakFactory peakFactory = new Peak1DMSFactory();
	@Configurable
	private double rtNormalizationFactor = 1 / 60.0d;
	@Configurable
	private String rtOutputFormat = "0.000";
	@Configurable
	private boolean postProcessCliques = false;
	/*
	 * private scope
	 */
	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	private HashMap<IPeak, Clique> peakToClique = new HashMap<IPeak, Clique>();

	@Override
	public String toString() {
		return getClass().getName();
	}

	/**
	 *
	 * @param al
	 * @param newFragments
	 * @param cliques
	 * @return
	 */
	private TupleND<IFileFragment> addAnchors(final List<List<IPeak>> al,
			final TupleND<IFileFragment> newFragments,
			final List<Clique> cliques) {

		final String ri_names = this.anchorNames;
		final String ri_times = this.anchorTimes;
		final String ri_indices = this.anchorRetentionIndex;
		final String ri_scans = this.anchorScanIndex;

		findCenter(newFragments, cliques);

		// Create Variables and arrays
		final HashMap<String, IFileFragment> hm = new HashMap<String, IFileFragment>();
		log.info("Preparing files!");
		for (final IFileFragment ff : newFragments) {
			log.debug("Source files of fragment {}: {}",
					ff.getUri(), ff.getSourceFiles());
			hm.put(ff.getName(), ff);
			final int size = getNumberOfPeaksWithinCliques(ff, cliques);
			if (size > 0) {
				final ArrayInt.D1 anchors = new ArrayInt.D1(size);
				ArrayTools.fillArray(anchors, Integer.valueOf(-1));
				final IVariableFragment ri = ff.hasChild(ri_scans) ? ff.getChild(
						ri_scans) : new VariableFragment(ff, ri_scans);
				final ArrayDouble.D1 anchorTimes = new ArrayDouble.D1(size);
				ArrayTools.fillArray(anchorTimes, Double.valueOf(-1));
				final IVariableFragment riTimes = ff.hasChild(ri_times) ? ff.getChild(ri_times) : new VariableFragment(ff, ri_times);
				final ArrayChar.D2 names = cross.datastructures.tools.ArrayTools.createStringArray(size, 256);
				final IVariableFragment riNames = ff.hasChild(ri_names) ? ff.getChild(ri_names) : new VariableFragment(ff, ri_names);
				final ArrayDouble.D1 rindexA = new ArrayDouble.D1(size);
				final IVariableFragment rindex = ff.hasChild(ri_indices) ? ff.getChild(ri_indices)
						: new VariableFragment(ff, ri_indices);
				ri.setArray(anchors);
				riNames.setArray(names);
				riTimes.setArray(anchorTimes);
				rindex.setArray(rindexA);
			}
		}

		// Set Anchors
		int id = 0;
		int[] nextIndex = new int[newFragments.size()];
		HashMap<String, Integer> placeMap = new HashMap<String, Integer>();
		int cnt = 0;
		// fill placeMap with unique id aka slot in cliqueNumbers for each
		// FileFragment
		for (IFileFragment f : newFragments) {
			placeMap.put(f.getName(), cnt++);
		}

		log.info("Setting anchors!");
		for (final Clique c : cliques) {
			// Matched IPeaks
			for (final IPeak p : c.getPeakList()) {
				final IFileFragment association = hm.get(p.getAssociation());
				int slot = placeMap.get(association.getName());
				if (peakToClique.containsKey(p)) {
					id = nextIndex[slot];
					long cid = peakToClique.get(p).getID();
					log.debug(
							"Adding anchor at scan index {} and rt {} with id {} to file {}",
							new Object[]{p.getScanIndex(),
						p.getScanAcquisitionTime(), cid,
						association.getName()});
					String name = "A" + cid;
					if (!(p.getName().equals(""))) {
						name = name + "_" + p.getName();

					}
					// log.info("Adding anchor with name {}", name);
					((ArrayChar.D2) association.getChild(ri_names).getArray()).setString(id, name);
					((ArrayInt.D1) association.getChild(ri_scans).getArray()).set(id, p.getScanIndex());
					((ArrayDouble.D1) association.getChild(ri_times).getArray()).set(id, p.getScanAcquisitionTime());
					nextIndex[slot]++;
				}

			}
			id++;
		}

		// cleanup
		for (final IFileFragment ff : newFragments) {
			// make sure, that work array data is removed
			if (ff.hasChild(this.ticPeaks)) {
				ff.removeChild(ff.getChild(this.ticPeaks));
			}
			if (ff.hasChild(this.binnedIntensities)) {
				ff.removeChild(ff.getChild(this.binnedIntensities));
			}
			if (ff.hasChild(this.binnedScanIndex)) {
				ff.removeChild(ff.getChild(this.binnedScanIndex));
			}
			if (ff.hasChild(this.scanIndex)) {
				ff.removeChild(ff.getChild(this.scanIndex));
			}
			if (ff.hasChild(this.massValues)) {
				ff.removeChild(ff.getChild(this.massValues));
			}
			if (ff.hasChild(this.intensityValues)) {
				ff.removeChild(ff.getChild(this.intensityValues));
			}
			ff.getChild(this.scanAcquisitionTime).getArray();
			final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
					ff.getUri(), this, getWorkflowSlot(), ff);
			getWorkflow().append(dwr);
			ff.save();
		}
		return newFragments;
	}

	private int getNumberOfPeaksWithinCliques(IFileFragment iff, List<Clique> l) {
		int npeaks = 0;
		for (Clique c : l) {
			for (IPeak p : c.getPeakList()) {
				if (p.getAssociation().equals(iff.getName())) {
					npeaks++;
				}
			}
		}
		return npeaks;
	}

	@Deprecated
	private List<Clique> getCommonCliques(IFileFragment a, IFileFragment b,
			List<Clique> l) {
		List<Clique> commonCliques = new ArrayList<Clique>();
		log.debug("Retrieving common cliques");
		for (Clique c : l) {
			for (IPeak p : c.getPeakList()) {
				if (p.getAssociation().equals(a.getName())
						|| p.getAssociation().equals(b.getName())) {
					commonCliques.add(c);
				}
			}
		}
		return commonCliques;
	}

	private double getCommonScore(IFileFragment a, IFileFragment b,
			List<Clique> commonCliques) {
		double score = 0;
		for (Clique c : commonCliques) {
			double v = 0;
			v = c.getSimilarityForPeaks(a.getName(), b.getName());
			if (Double.isNaN(v) || Double.isInfinite(v)) {
				v = 0;
			}
			score += v;
		}
		return score;
	}

	/**
	 * @param newFragments
	 * @param cliques
	 */
	private void findCenter(final TupleND<IFileFragment> newFragments,
			final List<Clique> cliques) {
		// cliqueNumbers -> number of cliques per FileFragment
		// FileFragments with highest number of cliques are favorites
		double[] cliqueNumbers = new double[newFragments.size()];
		double[] cliqueSize = new double[newFragments.size()];
		HashMap<String, Integer> placeMap = new HashMap<String, Integer>();
		int cnt = 0;
		// fill placeMap with unique id aka slot in cliqueNumbers for each
		// FileFragment
		for (IFileFragment f : newFragments) {
			placeMap.put(f.getName(), cnt++);
		}
		// for all peaks, increment number of cliques for FileFragment
		// associated to each peak
		int npeaks = 0;
		for (Clique c : cliques) {
			for (IPeak p : c.getPeakList()) {
				cliqueNumbers[placeMap.get(p.getAssociation())]++;
				cliqueSize[placeMap.get(p.getAssociation())] += c.getPeakList().size();
				npeaks++;
			}

		}

		// which FileFragment is the best reference for alignment?
		// the one, which participates in the highest number of cliques
		// or the one, which has the overall highest average similarity to all
		// others
		double[] fragScores = new double[newFragments.size()];
		// double sumFragScores = 0;
		ArrayDouble.D2 fragmentScores = new ArrayDouble.D2(newFragments.size(),
				newFragments.size());
		CliqueTable ct = new CliqueTable(newFragments, cliques);
		log.info("Calculating fragment scores");
		ArrayChar.D2 fragmentNames = new ArrayChar.D2(newFragments.size(), 1024);
		for (int i = 0; i < newFragments.size(); i++) {
			fragmentNames.setString(i, newFragments.get(i).getName());
			for (int j = 0; j < newFragments.size(); j++) {
				List<Clique> commonCliques = ct.getCommonCliques(
						newFragments.get(i), newFragments.get(j), cliques);
				if (!commonCliques.isEmpty()) {
					fragmentScores.set(
							i,
							j,
							getCommonScore(newFragments.get(i),
							newFragments.get(j), commonCliques)
							/ ((double) commonCliques.size()));
					fragScores[i] += fragmentScores.get(i, j);
				} else {
					log.debug("Common cliques list is empty!");
				}

			}
		}
		saveToCSV(fragmentScores, fragmentNames);
		// log number of cliques
		cnt = 0;
		for (IFileFragment iff : newFragments) {
			log.info("FileFragment " + iff.getName() + " is member of "
					+ cliqueNumbers[cnt] + " cliques with average cliqueSize: "
					+ cliqueSize[cnt] / cliqueNumbers[cnt]);
			cnt++;
		}

		boolean minimize = false;//costFunction.minimize();
		for (int j = 0; j < newFragments.size(); j++) {
			log.info("File: {}, value: {}", newFragments.get(j).getName(),
					(minimize ? -fragScores[j] : fragScores[j]));
		}

		int optIndex = 0;

		double optVal = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < fragScores.length; i++) {
			if (fragScores[i] > optVal) {
				optVal = Math.max(optVal, fragScores[i]);
				optIndex = i;
			}
//            }
		}

		final CSVWriter csvw = Factory.getInstance().getObjectFactory().
				instantiate(CSVWriter.class);
		csvw.setWorkflow(getWorkflow());
		List<List<String>> tble = new ArrayList<List<String>>();
		tble.add(Arrays.asList(newFragments.get(optIndex).getName()));
		csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
				getAbsolutePath(), "center-star.csv", tble,
				WorkflowSlot.CLUSTERING);

		log.info("{} with value {} is the center!",
				newFragments.get(optIndex).getName(),
				(minimize ? -optVal : optVal));
	}

	public void saveToCSV(final ArrayDouble.D2 distances,
			final ArrayChar.D2 names) {
		final CSVWriter csvw = Factory.getInstance().getObjectFactory().
				instantiate(CSVWriter.class);
		csvw.setWorkflow(getWorkflow());
		csvw.writeArray2DwithLabels(getWorkflow().getOutputDirectory(this).
				getAbsolutePath(), "pairwise_distances.csv", distances, names,
				this.getClass(), WorkflowSlot.STATISTICS, getWorkflow().
				getStartupDate());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		for (final IFileFragment iff : t) {
			log.debug("{}", iff);
		}
		if (t.size() < 2) {
			log.warn("At least two files are required for peak clique alignment!");
		} else {
			final TupleND<IFileFragment> tret = identifyPeaks(t);
			for (final IFileFragment iff : tret) {
				log.debug("{}", iff);
			}
			return tret;
		}
		return t;
	}

	/**
	 * @param al the list of FileFragments
	 * @param fragmentToPeaks the map of FileFragment names to IPeak lists
	 * @param n the number of peaks TODO: Paralellize like in
	 * PairwiseDistanceCalculator
	 */
	private UnmatchedPeaksList calculatePeakSimilarities(final TupleND<IFileFragment> al,
			final Map<String, List<IPeak>> fragmentToPeaks, final int n) {
		log.info(
				"Calculating {} pairwise peak similarities for {} peaks!",
				((long) n * (long) n), n);
//        log.info("Using {} as pairwise peak similarity!",
//                this.similarityFunction.getClass().getName());
		// Loop over all pairs of FileFragments
		ICompletionService<BBHPeakEdgeList> ics = createCompletionService(BBHPeakEdgeList.class);
		List<Callable<BBHPeakEdgeList>> workers = workerFactory.create(al, fragmentToPeaks);
		log.info("Running {} pairwise similarity tasks!", workers.size());
		for (Callable<BBHPeakEdgeList> worker : workers) {
			ics.submit(worker);
		}
		final Map<UUID,IPeak> unmatchedPeaks = new LinkedHashMap<UUID,IPeak>();
		try {
			List<BBHPeakEdgeList> bbhPeaksList = ics.call();
			for (IFileFragment f : al) {
				for(IPeak peak:fragmentToPeaks.get(f.getName())) {
					unmatchedPeaks.put(peak.getUniqueId(),peak);
				}
			}
			for (BBHPeakEdgeList upl : bbhPeaksList) {
				for (Tuple2D<PeakEdge, PeakEdge> t : upl) {
					PeakEdge p1 = t.getFirst();
					PeakEdge p2 = t.getSecond();
					unmatchedPeaks.remove(p1.getSourcePeakId());
					unmatchedPeaks.remove(p2.getSourcePeakId());
				}
			}
		} catch (Exception ex) {
			log.error("Caught exception while executing workers: ", ex);
			throw new RuntimeException(ex);
		}

		if (this.savePeakSimilarities) {
			PeakSimilarityVisualizer psv = new PeakSimilarityVisualizer();
			psv.setWorkflow(getWorkflow());
			psv.visualizePeakSimilarities(
					fragmentToPeaks, 256, "beforeBIDI");
		}
		return new UnmatchedPeaksList(unmatchedPeaks.values());
	}

	/**
	 * FIXME add support for 1D only chromatograms -> see TICDynamicTimeWarp for
	 * that
	 *
	 * @param al
	 * @return
	 */
	private HashMap<String, List<IPeak>> checkUserSuppliedAnchors(
			final TupleND<IFileFragment> al) {
		// Check for already defined peaks
		final HashMap<String, List<IPeak>> definedAnchors = new HashMap<String, List<IPeak>>();
		for (final IFileFragment t : al) {
			List<IPeak> anchors = checkUserSuppliedAnchors(t);
			log.info("Using {} user-supplied anchors for file {}!",anchors.size(),t.getName());
			definedAnchors.put(t.getName(), anchors);
		}
		return definedAnchors;
	}

	public List<IPeak> checkUserSuppliedAnchors(final IFileFragment t) {
		IVariableFragment anames = null;
		IVariableFragment ascans = null;
		List<IPeak> peaks = new ArrayList<IPeak>();
		try {
			anames = t.getChild(this.anchorNames);
			ascans = t.getChild(this.anchorScanIndex);
			final ArrayChar.D2 peakNames = (ArrayChar.D2) anames.getArray();
			final Array peakScans = ascans.getArray();
			final Index peakScansI = peakScans.getIndex();

			IVariableFragment biv = t.getChild(this.binnedIntensities);
			IVariableFragment bsi = t.getChild(this.binnedScanIndex);
			biv.setIndex(bsi);
			log.info("Checking user supplied anchors for: {}", t);
			final Array scan_acquisition_time = t.getChild(
					this.scanAcquisitionTime).getArray();
			final Index sat1 = scan_acquisition_time.getIndex();
			final List<Array> bintens = biv.getIndexedArray();
			for (int i = 0; i < peakScans.getShape()[0]; i++) {
				final String name = peakNames.getString(i);
				final int scan = peakScans.getInt(peakScansI.set(i));
				final double sat = scan_acquisition_time.getDouble(sat1.set(
						scan));
				log.debug("{}", t.getName());
				final IPeak p = new PeakNG(scan, bintens.get(scan),
						sat, t.getName(), this.savePeakSimilarities);
				p.setName(name);
				log.debug(
						"Adding user supplied anchor {} with name {}", p,
						p.getName());
				peaks.add(p);
			}
			return peaks;
		} catch (final ResourceNotAvailableException rne) {
			log.debug("Could not find any user-defined anchors!");
			return Collections.emptyList();
		}
	}

	@Data
	private class BBHResult {

		private final List<Clique> cliques;
		private final Set<IPeak> incompatiblePeaks;
		private final Set<IPeak> unassignedPeaks;
	}

	/**
	 * @param al
	 * @param nameToFragment
	 * @param fragmentToPeaks
	 * @param minCliqueSize
	 * @param npeaks
	 * @return a BBHResult
	 */
	private BBHResult combineBiDiBestHits(final TupleND<IFileFragment> al, final Map<String, IFileFragment> nameToFragment,
			final Map<String, List<IPeak>> fragmentToPeaks, final int minCliqueSize, int npeaks) {

		// given: a hashmap of name<->peak list
		// an empty list of peaks belonging to a clique
		// a minimum size for a clique from when on it is considered valid
		peakToClique = new HashMap<IPeak, Clique>();
		Set<IPeak> incompatiblePeaks = new LinkedHashSet<IPeak>();
		Set<IPeak> unassignedPeaks = new LinkedHashSet<IPeak>();
		ObjectObjectOpenHashMap<UUID, IPeak> peakRepository = new ObjectObjectOpenHashMap<UUID, IPeak>();
		for (String key : fragmentToPeaks.keySet()) {
			for (IPeak p : fragmentToPeaks.get(key)) {
				peakRepository.put(p.getUniqueId(), p);
			}
		}
		// every peak is assigned to at most one clique!!!
		// reassignment is invalid and should not occur
		// for all files
		// file comparisons: k*(k-1)
		// per peak comparison: 2*l
		// check for clique membership: (k*l)
		for (IFileFragment iff : al) {
			final List<IPeak> peaks = fragmentToPeaks.get(iff.getName());
			log.info("Checking {} peaks for file {}", peaks.size(),
					iff.getName());
			// for all peaks in file

			// final List<IPeak> bidiHits = new ArrayList<IPeak>();
			// bidiHits.add(p);
			// for all other files
			for (final IFileFragment jff : al) {
				// only compare between partition matches, i!=j
				if (!iff.getName().equals(jff.getName())) {
					for (final IPeak p : peaks) {
						// retrieve list of most similar peaks
						final IPeak q = peakRepository.get(p.getPeakWithHighestSimilarity(jff.getName()));
						if (q == null) {
							// null peaks have no bidi best hit, so they are
							// removed
							// beforehand
							log.debug("Skipping null peak");
							unassignedPeaks.add(p);
							continue;
						}
						// security check, this should never happen, but if
						// the similarity function is wrongly parameterized,
						// this may
						// lead to false assignments, so inform the user that
						// something
						// is not right!
						if (p.getSimilarity(q) == Double.NEGATIVE_INFINITY
								|| p.getSimilarity(q) == Double.POSITIVE_INFINITY) {
							throw new IllegalArgumentException(
									"Infinite similarity value for associated peaks!");
						}
						// bidirectional hit
						if (q != null && q.isBidiBestHitFor(p)) {
							log.debug(
									"Found bidirectional best hit for peak {}: {}",
									p, q);
							// Possible cases, if we found a bidirectional hit
							// for p
							// 1: p is already in a clique
							// 3: p and q are already in a clique
							// 3: a: p and q are already in the same clique???
							// 3: b: p and q are in different cliques !!!
							// conflict!!!
							// 4: p and q are not in a clique, create a new
							// clique and add both

							// initialization of cliques, if present
							Clique c = null, d = null;
							if (peakToClique.containsKey(q)) {
								d = peakToClique.get(q);
								if (d != null) {
									log.debug("Found clique for peak q");
								}
							}
							if (peakToClique.containsKey(p)) {// p has a clique
								c = peakToClique.get(p);
								if (c != null) {
									log.debug("Found clique for peak p");
								}
							}

							//
							if (d != null && c != null && c != d) {
								log.debug(
										"Found different cliques for peak p and q!");
								log.debug("Clique for p: {}", c);
								log.debug("Clique for q: {}", d);
								// try to merge cliques
								incompatiblePeaks.addAll(mergeCliques(c, d));
							} else if (c != null && d == null) {
								if (c.addPeak(q)) {
									peakToClique.put(q, c);
								}
							} else if (d != null && c == null) {
								if (d.addPeak(p)) {
									peakToClique.put(p, d);
								}
							} else if (c == null && d == null) {
								createNewClique(p, q);
							} else if (c == d) {
								if (c.addPeak(p)) {
									peakToClique.put(p, c);
								}
							} else {
								log.error(
										"Unhandled case in if else! Missed a state?: c={} d={}, p={}, q={}",
										new Object[]{c, d, p, q});
							}
						} else {
							log.debug(
									"Peak q:{} and p:{} are no bidirectional best hits!",
									p, q);
						}
					}
				}
			}
		}

//        if (incompatiblePeaks.size() > 0) {
		log.info("Found {}/{} incompatible peaks.",
				incompatiblePeaks.size(), npeaks);
//        }
//        if (unassignedPeaks.size() > 0) {
		log.info("Found {}/{} unassigned peaks.", unassignedPeaks.size(), npeaks);
//        }

		if (saveIncompatiblePeaks) {
			savePeakList(nameToFragment, incompatiblePeaks, "incompatiblePeaks.msp", "INCOMPATIBLE");
		}

		if (saveUnassignedPeaks) {
			savePeakList(nameToFragment, unassignedPeaks, "unassignedPeaks.msp", "UNASSIGNED");
		}
		for (IPeak p : incompatiblePeaks) {
			log.debug("Incompatible peak: " + p);
		}

//        for (Peak p : incompatiblePeaks) {
//            p.clearSimilarities();
//        }

//		for (Peak p : unassignedPeaks) {
//            p.clearSimilarities();
//        }

		// retain all cliques, which exceed minimum size
		HashSet<Clique> cliques = new HashSet<Clique>();
		for (Clique c : peakToClique.values()) {
			if (!cliques.contains(c)) {
				log.debug("Size of clique: {}\n{}",
						c.getPeakList().size(), c);
				cliques.add(c);
			}
		}

		// sort cliques by clique rt mean
		List<Clique> l = new ArrayList<Clique>(cliques);
		Collections.sort(l, new Comparator<Clique>() {
			@Override
			public int compare(Clique o1, Clique o2) {
				double rt1 = o1.getCliqueRTMean();
				double rt2 = o2.getCliqueRTMean();
				if (rt1 > rt2) {
					return 1;
				} else if (rt1 < rt2) {
					return -1;
				}
				return 0;
			}
		});

		if (this.savePlots) {
			String groupFileLocation = Factory.getInstance().getConfiguration().
					getString("groupFileLocation", "");
			OneWayPeakAnova owa = new OneWayPeakAnova();
			owa.setWorkflow(getWorkflow());
			owa.calcFisherRatios(l, al, groupFileLocation);
			saveCliquePlots(l, al);
		}

		return new BBHResult(l, incompatiblePeaks, unassignedPeaks);
	}

	/**
	 * @param p
	 * @param q
	 */
	private void createNewClique(final IPeak p, final IPeak q) {
		Clique c;
		// assigned yet
		c = new Clique();
//		c.setMaxBBHErrors(this.maxBBHErrors);
		c.setMinBbhFraction(this.minBbhFraction);
		if (c.addPeak(p)) {
			peakToClique.put(p, c);
		}
		if (c.addPeak(q)) {
			peakToClique.put(q, c);
		}
	}

	/**
	 * @param c
	 * @param d
	 * @return
	 */
	private List<IPeak> mergeCliques(Clique c, Clique d) {
		int ds = d.getPeakList().size();
		int cs = c.getPeakList().size();
		//if either clique is empty, we can not merge,
		//so we can not have any incompatible peaks,
		//so we return an empty list
		if (ds == 0 || cs == 0) {
			return Collections.emptyList();
		}

		//start merging if both cliques have at least one peak in them
		log.debug("Merging cliques: c={}, d={}", c.toString(),
				d.toString());
		// ds has more peaks than cs -> join cs into
		// ds
		List<IPeak> incompatiblePeaks = new LinkedList<IPeak>();
		if (ds > cs) {
			for (IPeak pk : c.getPeakList()) {
				if (d.addPeak(pk)) {
					// c.removePeak(pk);
					peakToClique.put(pk, d);
				} else {
					incompatiblePeaks.add(pk);
					log.debug("Adding of peak {} into clique {} failed", pk, d);
				}

			}
			log.debug("Clique {} has {} peaks left!", c, c.getPeakList().size());
			c.clear();
		} else {// ds has less peaks than cs -> join
			// ds into cs
			for (IPeak pk : d.getPeakList()) {
				if (c.addPeak(pk)) {
					// d.removePeak(pk);
					peakToClique.put(pk, c);
				} else {
					incompatiblePeaks.add(pk);
					log.debug("Adding of peak {} into clique {} failed", pk, c);
				}

			}
			log.debug("Clique {} has {} peaks left!", d, d.getPeakList().size());
			d.clear();
		}
		return incompatiblePeaks;
	}

	@Override
	public void configure(final Configuration cfg) {
		super.configure(cfg);
		/*
		 this.scanAcquisitionTime = cfg.getString(
		 "var.scan_acquisition_time", "scan_acquisition_time");
		 this.binnedIntensities = cfg.getString(
		 "var.binned_intensity_values", "binned_intensity_values");
		 this.binnedScanIndex = cfg.getString(
		 "var.binned_scan_index", "binned_scan_index");
		 this.anchorNames = cfg.getString(
		 "var.anchors.retention_index_names", "retention_index_names");
		 this.anchorTimes = cfg.getString(
		 "var.anchors.retention_times", "retention_times");
		 this.anchorRetentionIndex = cfg.getString(
		 "var.anchors.retention_indices", "retention_indices");
		 this.anchorScanIndex = cfg.getString(
		 "var.anchors.retention_scans", "retention_scans");
		 this.massValues = cfg.getString("var.mass_values",
		 "mass_values");
		 this.scanIndex = cfg.getString("var.scan_index",
		 "scan_index");
		 this.intensityValues = cfg.getString(
		 "var.intensity_values", "intensity_values");
		 this.ticPeaks = cfg.getString("var.tic_peaks", "tic_peaks");
		 */
		//this.peakAreaVariable = cfg.getString("var.peak_area", "peak_area");
	}

	/**
	 *
	 * @param al
	 * @param fragmentToPeaks
	 */
	public void saveSimilarityMatrix(final TupleND<IFileFragment> al,
			final HashMap<String, List<IPeak>> fragmentToPeaks) {
		for (final IFileFragment iff1 : al) {
			for (final IFileFragment iff2 : al) {
				final List<IPeak> lhsPeaks = fragmentToPeaks.get(iff1.getName());
				final List<IPeak> rhsPeaks = fragmentToPeaks.get(iff2.getName());
				double score = 0;
				int bidihits = 0;
				log.debug("lhsPeaks: {}", lhsPeaks.size());
				log.debug("rhsPeaks: {}", rhsPeaks.size());
				for (final IPeak plhs : lhsPeaks) {
					for (final IPeak prhs : rhsPeaks) {
						double d = plhs.getSimilarity(prhs);
						if (d != Double.NEGATIVE_INFINITY
								&& d != Double.POSITIVE_INFINITY) {
							score += d;
							bidihits++;
						}
					}
				}
				score /= ((double) (bidihits));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cross.commands.fragments.AFragmentCommand#getDescription()
	 */
	/**
	 *
	 * @return
	 */
	@Override
	public String getDescription() {
		return "Assigns peak candidates as pairs and groups them into cliques of size k";
	}

//    public double getSimilarity(final Peak a, final Peak b) {
//        final double v = this.similarityFunction.apply(new double[]{
//                    a.getScanAcquisitionTime()}, new double[]{b.getScanAcquisitionTime()},
//                a.getMSIntensities(), b.getMSIntensities());
//        return v;
//    }

	/*
	 * (non-Javadoc)
	 *
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	/**
	 *
	 * @return
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.PEAKMATCHING;
	}

	private TupleND<IFileFragment> identifyPeaks(
			final TupleND<IFileFragment> originalFragments) {
		log.debug("Matching peaks");
		final Map<String, IFileFragment> nameToFragment = new ConcurrentHashMap<String, IFileFragment>();
		final Map<String, List<IPeak>> fragmentToPeaks = new ConcurrentHashMap<String, List<IPeak>>();
		final Map<String, Integer> columnMap = new ConcurrentHashMap<String, Integer>(
				originalFragments.size());

		final ArrayList<IFileFragment> al2 = new ArrayList<IFileFragment>();
		for (final IFileFragment iff : originalFragments) {
			final IFileFragment iff2 = new FileFragment(new File(getWorkflow().
					getOutputDirectory(this),
					iff.getName()));
			iff2.addSourceFile(iff);
			nameToFragment.put(iff2.getName(), iff2);
			log.debug("Created work file {}", iff2);
			al2.add(iff2);
		}
		final TupleND<IFileFragment> t = new TupleND<IFileFragment>(al2);
		initializePeaks(originalFragments, fragmentToPeaks, columnMap);
		log.debug("Calculating all-against-all peak similarities");
		int n = 0;
		for (String key : fragmentToPeaks.keySet()) {
			n += fragmentToPeaks.get(key).size();
		}
		log.info("Searching for bidirectional best hits");
		final long startT = System.currentTimeMillis();
		final List<IPeak> unmatchedPeaks = calculatePeakSimilarities(t, fragmentToPeaks, n);
		log.info("Found {}/{} unmatched peaks!", unmatchedPeaks.size(), n);
		if (saveUnmatchedPeaks) {
			savePeakList(nameToFragment, unmatchedPeaks, "unmatchedPeaks.msp", "UNMATCHED");
		}
		log.info("Found bidi best hits in {} milliseconds",
				System.currentTimeMillis() - startT);

		final long startT2 = System.currentTimeMillis();
		final List<List<IPeak>> ll = new ArrayList<List<IPeak>>();

		BBHResult result;
		if (this.minCliqueSize == -1 || this.minCliqueSize == t.size()) {
			log.info("Combining bidirectional best hits if present in all files");
			result = combineBiDiBestHits(t, nameToFragment, fragmentToPeaks, t.size(), n - unmatchedPeaks.size());
		} else {
			if (this.minCliqueSize > t.size()) {
				log.info("Resetting minimum group size to: {}, was: {}",
						t.size(), this.minCliqueSize);
				this.minCliqueSize = t.size();
			}
			log.info("Combining bidirectional best hits, minimum group size: {}",
					this.minCliqueSize);
			result = combineBiDiBestHits(t, nameToFragment, fragmentToPeaks,
					this.minCliqueSize, n - unmatchedPeaks.size());
		}
		// add all remaining cliques to ll
		log.info("Minimum clique size: {}", minCliqueSize);
		final List<Clique> cliques = result.getCliques();
		int peakNumberCorrection = 0;
		int unmatchedAdded = 0, incompatibleAdded = 0, unassignedAdded = 0;
		//reinspect
		if (postProcessCliques) {
			log.error("Post processing cliques and unmatched peaks");
//			SparseDoubleMatrix2D sdm = new SparseDoubleMatrix2D(cliques.size(), result.getIncompatiblePeaks().size() + result.getUnassignedPeaks().size() + unmatchedPeaks.size());
			//identify all peaks that are not contained in the final cliques
//			int clique = 0;
			for (Clique c : cliques) {
				if (!c.getPeakList().isEmpty()) {
					log.debug("Checking clique at {} with size {} and rtvariance {}", new Object[]{c.getCliqueRTMean(), c.getPeakList().size(), c.getCliqueRTVariance()});
					//log.info("RT variance within clique {} containing {} peaks ={}; Stdev: {}", new Object[]{c.getCliqueRTMean(), c.getPeakList().size(), c.getCliqueRTVariance(), Math.sqrt(c.getCliqueRTVariance())});
					int peak = 0;
					for (IPeak q : result.getIncompatiblePeaks()) {
//						
//						double ratio = c.getRatioOfRTDistanceToCentroidAndCliqueVariance(q);
////						double distToCentroid = c.getRTDistanceToCentroid(q);
////						log.info("Distance to centroid of clique: {}", distToCentroid);
////						log.info("Similarity to centroid of clique: {}", q.getSimilarity(p));
////						sdm.set(clique, peak, distToCentroid);
//						if (ratio <= 1) {
						if (c.addPeak(q)) {
							log.debug("Adding formerly incompatible peak {} to clique", q);
							peakNumberCorrection++;
							incompatibleAdded++;
						}
//						}
						peak++;
					}
					for (IPeak q : result.getUnassignedPeaks()) {
						double ratio = c.getRatioOfRTDistanceToCentroidAndCliqueVariance(q);
//						double distToCentroid = c.getRTDistanceToCentroid(q);
//						sdm.set(clique, peak, distToCentroid);
//						log.info("Distance to centroid of clique: {}", distToCentroid);
//						log.info("Similarity to centroid of clique: {}", q.getSimilarity(p));
//						if (ratio <= 1) {
						if (c.addPeak(q)) {
							log.debug("Adding formerly unassigned peak {} to clique", q);
							peakNumberCorrection++;
							unassignedAdded++;
						}
//						}
						peak++;
					}
					for (IPeak q : unmatchedPeaks) {
						double ratio = c.getRatioOfRTDistanceToCentroidAndCliqueVariance(q);
//						double distToCentroid = c.getRTDistanceToCentroid(q);
//						sdm.set(clique, peak, distToCentroid);
//						log.info("Distance to centroid of clique: {}", distToCentroid);
//						log.info("Similarity to centroid of clique: {}", q.getSimilarity(p));
//						if (ratio <= 1) {
						if (c.addPeak(q)) {
							log.debug("Adding formerly unmatched peak {} to clique", q);
							peakNumberCorrection++;
							unmatchedAdded++;
						}
//						}
						peak++;
					}
				} else {
					log.debug("Skipping empty clique at {}", c.getCliqueRTMean());
				}
			}
			log.error("Post processing assigned {} incompatible, {} unassigned, and {} unmatched peaks to cliques!", new Object[]{incompatibleAdded, unassignedAdded, unmatchedAdded});
		}
		ListIterator<Clique> li = cliques.listIterator();
		while (li.hasNext()) {
			Clique c = li.next();
			try {
				log.debug("Clique {}", c);
			} catch (NullPointerException npe) {
				log.debug("Clique empty?: {}", c.getPeakList());
			}
			if (c.getPeakList().size() >= minCliqueSize) {
				ll.add(c.getPeakList());
			} else {
				li.remove();
			}
		}

		removePeakSimilaritiesWhichHaveNoBestHits(t, fragmentToPeaks);
		log.info("Found {} cliques in {} milliseconds", cliques.size(),
				System.currentTimeMillis() - startT2);
		float matchedPeaks = n - (unmatchedPeaks.size() + result.incompatiblePeaks.size() + result.unassignedPeaks.size() - peakNumberCorrection);
		log.info("Percentage of peaks covered in cliques: {}%", 100.0f * (matchedPeaks / (float) n));
		if (this.savePeakSimilarities) {
			PeakSimilarityVisualizer psv = new PeakSimilarityVisualizer();
			psv.setWorkflow(getWorkflow());
			psv.visualizePeakSimilarities(
					fragmentToPeaks, 256, "afterBIDI");
		}

		log.info("Saving peak match tables!");
		savePeakMatchTable(columnMap, ll);
		if (savePeakMatchRTTable) {
			savePeakMatchRTTable(columnMap, ll);
		}
		if (savePeakMatchAreaTable) {
			savePeakMatchAreaTable(columnMap, ll, nameToFragment);
		}
		if (savePeakMatchAreaPercentTable) {
			savePeakMatchAreaPercentTable(columnMap, ll, nameToFragment);
		}
		if (saveXMLAlignment) {
			log.info("Saving alignment to xml!");
			saveToXMLAlignment(t, ll);
		}
		log.info("Adding anchor variables");
		final TupleND<IFileFragment> ret = cliques.isEmpty() ? originalFragments
				: addAnchors(ll, t, cliques);
		for (IFileFragment iff : originalFragments) {
			iff.clearArrays();
		}
		return ret;
	}

	/**
	 * FIXME add support for 1D only chromatograms -> see TICDynamicTimeWarp for
	 * that
	 *
	 * @param al
	 * @param fragmentToPeaks
	 * @param peakFinderFragments
	 * @param columnMap
	 */
	private void initializePeaks(
			final TupleND<IFileFragment> originalFileFragments,
			final Map<String, List<IPeak>> fragmentToPeaks,
			final Map<String, Integer> columnMap) {
		int column = 0;
		// int maxPeaks = Integer.MIN_VALUE;
		int npeaks = 0;
		HashMap<String, List<IPeak>> definedAnchors = new HashMap<String, List<IPeak>>();
		if (this.useUserSuppliedAnchors) {
			log.debug("Checking for user-supplied anchors!");
			definedAnchors = checkUserSuppliedAnchors(originalFileFragments);
			for (final IFileFragment iff : originalFileFragments) {
				definedAnchors.put(iff.getName(), new ArrayList<IPeak>(0));
			}
		}
		log.debug("{}", definedAnchors.toString());
		final double massBinResolution = Factory.getInstance().getConfiguration().getDouble("dense_arrays.massBinResolution",
				1.0d);
		Tuple2D<Double, Double> minMaxMassRange = MaltcmsTools.getMinMaxMassRange(originalFileFragments);
		final int size = MaltcmsTools.getNumberOfIntegerMassBins(minMaxMassRange.getFirst(),
				minMaxMassRange.getSecond(), massBinResolution);
		if (useSparseArrays) {
			log.info("Using sparse arrays!");
		}
		// Insert Peaks into HashMap
		for (final IFileFragment t : originalFileFragments) {
			// if we have a valid variable defined,
			// use those peak indices
			Array peakCandidates1;
			try {
				IVariableFragment peakCandidates = t.getChild(this.ticPeaks);
				peakCandidates1 = peakCandidates.getArray();
				log.debug("Peaks for file {}: {}", t.getUri(),
						peakCandidates1);
			} catch (ResourceNotAvailableException rnae) {
				// otherwise, create an index array for all scans!!!
				Array sidx = t.getChild(this.binnedScanIndex).getArray();
				peakCandidates1 = ArrayTools.indexArray(sidx.getShape()[0], 0);
			}
			EvalTools.notNull(peakCandidates1, this);
			List<IPeak> peaks = null;
			if (fragmentToPeaks.containsKey(t.getName())) {
				peaks = fragmentToPeaks.get(t.getName());
			} else {
				peaks = new ArrayList<IPeak>();
			}
			log.debug("Adding peaks for {}", t.getName());
			final List<IPeak> userDefinedAnchors = definedAnchors.get(t.getName());
			IPeakFactoryImpl pfi = peakFactory.createInstance(t, !savePeakSimilarities, minMaxMassRange, size, massBinResolution, useSparseArrays, savePeakSimilarities);

			for (int i = 0; i < peakCandidates1.getShape()[0]; i++) {
				final int pc1i = peakCandidates1.getInt(i);
				IPeak p = null;
				p = pfi.create(i, pc1i);
				peaks.add(p);
				npeaks++;
			}

			if ((userDefinedAnchors != null) && this.useUserSuppliedAnchors) {
				log.info("Using user-defined anchors for {}", t.getName());
				for (final IPeak p : userDefinedAnchors) {

					final int n = Collections.binarySearch(peaks, p,
							new PeakComparator());
					if (n >= 0) {// if found in list, remove and add to anchors
						final IPeak q = peaks.get(n);
						q.setName(p.getName());
						log.debug("{} with name {} annotated by user!", p,
								p.getName());
					} else {// else add at proposed insert position
						log.debug("Adding peak at position {}", n);
						peaks.add(((-1) * n) - 1, p);
					}
				}
			}

			fragmentToPeaks.put(t.getName(), peaks);
			columnMap.put(t.getName(), column++);
			// clearing space
			t.clearArrays();
		}
		log.debug("{} peaks present", npeaks);
	}

	/**
	 *
	 * @param peaks
	 * @param numberOfFiles
	 * @return
	 */
	public boolean isBidiBestHitForAll(final List<IPeak> peaks,
			final int numberOfFiles) {
		return isBidiBestHitForK(peaks, numberOfFiles, numberOfFiles);
	}

	/**
	 *
	 * @param peaks
	 * @param numberOfFiles
	 * @param minCliqueSize
	 * @return
	 */
	public boolean isBidiBestHitForK(final List<IPeak> peaks,
			final int numberOfFiles, final int minCliqueSize) {
		int i = 0;
		int j = 0;
		for (final IPeak p : peaks) {
			for (final IPeak q : peaks) {
				if (!p.equals(q)) {
					if (q.isBidiBestHitFor(p)) {
						i++;
					} else {
					}
					j++;
				}
			}
		}

		if ((minCliqueSize < 2) && (minCliqueSize >= -1)) {
			log.info(
					"Illegal value for minCliqueSize = {}, allowed values are -1, >=2 <= number of chromatograms",
					minCliqueSize);
		}
		if (i >= minCliqueSize) {
			log.debug(
					"{} are BidiBestHits of each other: {}", i, peaks);
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param peaks
	 * @param expectedHits
	 * @return
	 */
	public boolean isFirstBidiBestHitForRest(final List<IPeak> peaks,
			final int expectedHits) {
		int i = 0;
		final IPeak p0 = peaks.get(0);
		for (final IPeak p : peaks) {
			// for(Peak q:peaks) {
			if (!p.equals(p0)) {
				if (p0.isBidiBestHitFor(p)) {
					i++;
				} else {
				}
			}
			// }
		}
		if (i == expectedHits) {
			log.debug(
					"All elements are BidiBestHits to first Peak: {}", peaks);
			return true;
		}
		return false;
	}

	private void removePeakSimilaritiesWhichHaveNoBestHits(
			final TupleND<IFileFragment> t,
			final Map<String, List<IPeak>> fragmentToPeaks) {
		// no best hits means, that the corresponding list of sorted peaks has
		// length greater than one
		for (final String s : fragmentToPeaks.keySet()) {
			for (final IPeak p : fragmentToPeaks.get(s)) {
				for (final IFileFragment iff : t) {
					final List<UUID> l = p.getPeaksSortedBySimilarity(iff.getName());
					// clear similarities, if a best hit hasn't been assigned
					if (l.size() > 1) {
						log.debug("Clearing similarities for {} and {}",
								iff.getName(), p);
						p.clearSimilarities(iff.getName());
					}
				}
			}
		}
	}

	/**
	 * @param columnMap
	 * @param ll
	 */
	private void savePeakMatchAreaTable(final Map<String, Integer> columnMap,
			final List<List<IPeak>> ll, final Map<String, IFileFragment> nameToFragment) {
		final List<List<String>> rows = new ArrayList<List<String>>(ll.size());
		List<String> headers = null;
		final String[] headerLine = new String[columnMap.size()];
		for (int i = 0; i < headerLine.length; i++) {
			headerLine[i] = "";
		}
		headers = Arrays.asList(headerLine);
		for (final String s : columnMap.keySet()) {
			headers.set(columnMap.get(s), StringTools.removeFileExt(s));
		}
		log.debug("Adding row {}", headers);
		rows.add(headers);
		int rowCounter = 0;
		for (final List<IPeak> l : ll) {
			log.debug("Adding row {}", rowCounter);
			final String[] line = new String[columnMap.size()];
			for (int i = 0; i < line.length; i++) {
				line[i] = "-";
			}
			log.debug("Adding {} peaks", l.size());
			for (final IPeak p : l) {
				final String iff = p.getAssociation();
				log.debug("Adding peak {} from {}", p, iff);
				EvalTools.notNull(iff, this);
				IFileFragment fragment = nameToFragment.get(iff);
				EvalTools.notNull(fragment, this);
				final int pos = columnMap.get(iff).intValue();
				try {
					Array peakAreas = fragment.getChild(peakAreaVariable).getArray();
					log.debug("PeakAreas for {}: {}", fragment.getName(), peakAreas.getShape()[0]);
					log.debug("Insert position for {}: {}", iff, pos);
					if (pos >= 0) {
						if (line[pos].equals("-")) {
							if (p.getPeakIndex() != -1) {
								line[pos] = peakAreas.getDouble(p.getPeakIndex()) + "";
							} else {
								line[pos] = "NaN";
							}
						} else {
							log.warn("Array position {} already used!", pos);
						}
					}
				} catch (ResourceNotAvailableException rnae) {
					log.debug("Could not find {} as starting from {}!", peakAreaVariable, fragment.getName());
				}
			}
			final List<String> v = Arrays.asList(line);
			rows.add(v);
			log.debug("Adding row {}", v);
			rowCounter++;
		}

		final CSVWriter csvw = new CSVWriter();
		csvw.setWorkflow(getWorkflow());
		csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
				getAbsolutePath(), "multiple-alignmentArea.csv", rows,
				WorkflowSlot.ALIGNMENT);
	}

	/**
	 * @param columnMap
	 * @param ll
	 */
	private void savePeakMatchAreaPercentTable(final Map<String, Integer> columnMap,
			final List<List<IPeak>> ll, final Map<String, IFileFragment> nameToFragment) {
		final List<List<String>> rows = new ArrayList<List<String>>(ll.size());
		List<String> headers = null;
		final String[] headerLine = new String[columnMap.size()];
		for (int i = 0; i < headerLine.length; i++) {
			headerLine[i] = "";
		}
		headers = Arrays.asList(headerLine);
		for (final String s : columnMap.keySet()) {
			headers.set(columnMap.get(s), StringTools.removeFileExt(s));
		}
		log.debug("Adding row {}", headers);
		rows.add(headers);
		int rowCounter = 0;
		for (final List<IPeak> l : ll) {
			log.debug("Adding row {}", rowCounter);
			final String[] line = new String[columnMap.size()];
			for (int i = 0; i < line.length; i++) {
				line[i] = "-";
			}
			log.debug("Adding {} peaks", l.size());
			for (final IPeak p : l) {
				final String iff = p.getAssociation();
				log.debug("Adding peak {} from {}", p, iff);
				EvalTools.notNull(iff, this);
				IFileFragment fragment = nameToFragment.get(iff);
				EvalTools.notNull(fragment, this);
				final int pos = columnMap.get(iff).intValue();
				try {
					Array peakAreas = fragment.getChild(peakAreaVariable).getArray();
					log.debug("PeakAreas for {}: {}", fragment.getName(), peakAreas.getShape()[0]);
					log.debug("Insert position for {}: {}", iff, pos);
					if (pos >= 0) {
						if (line[pos].equals("-")) {
							if (p.getPeakIndex() != -1) {
								line[pos] = peakAreas.getDouble(p.getPeakIndex()) * 100.0 + "";
							} else {
								line[pos] = "NaN";
							}
						} else {
							log.warn("Array position {} already used!", pos);
						}
					}
				} catch (ResourceNotAvailableException rnae) {
					log.debug("Could not find {} as starting from {}!", peakAreaVariable, fragment.getName());
				}
			}
			final List<String> v = Arrays.asList(line);
			rows.add(v);
			log.debug("Adding row {}", v);
			rowCounter++;
		}

		final CSVWriter csvw = new CSVWriter();
		csvw.setWorkflow(getWorkflow());
		csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
				getAbsolutePath(), "multiple-alignmentAreaPercent.csv", rows,
				WorkflowSlot.ALIGNMENT);
	}

	/**
	 * @param columnMap
	 * @param ll
	 */
	private void savePeakMatchRTTable(final Map<String, Integer> columnMap,
			final List<List<IPeak>> ll) {
		final List<List<String>> rows = new ArrayList<List<String>>(ll.size());
		List<String> headers = null;
		final String[] headerLine = new String[columnMap.size()];
		for (int i = 0; i < headerLine.length; i++) {
			headerLine[i] = "";
		}
		headers = Arrays.asList(headerLine);
		for (final String s : columnMap.keySet()) {
			headers.set(columnMap.get(s), StringTools.removeFileExt(s));
		}
		log.debug("Adding row {}", headers);
		rows.add(headers);
		final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
				Locale.US);
		// this is a fix, default rounding convention is HALF_EVEN,
		// which allows less error to accumulate, but is seldomly used
		// outside of java...
		df.setRoundingMode(RoundingMode.HALF_UP);
		df.applyPattern(rtOutputFormat);
		for (final List<IPeak> l : ll) {
			final String[] line = new String[columnMap.size()];
			for (int i = 0; i < line.length; i++) {
				line[i] = "-";
			}
			log.debug("Adding {} peaks", l.size());
			for (final IPeak p : l) {
				final String iff = p.getAssociation();
				EvalTools.notNull(iff, this);
				final int pos = columnMap.get(iff).intValue();
				log.debug("Insert position for {}: {}", iff, pos);
				if (pos >= 0) {
					if (line[pos].equals("-")) {
						final double sat = p.getScanAcquisitionTime() * rtNormalizationFactor;
						line[pos] = df.format(sat) + "";
					} else {
						log.warn("Array position {} already used!", pos);
					}
				}
			}
			final List<String> v = Arrays.asList(line);
			rows.add(v);
			log.debug("Adding row {}", v);
		}

		final CSVWriter csvw = new CSVWriter();
		csvw.setWorkflow(getWorkflow());
		csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
				getAbsolutePath(), "multiple-alignmentRT.csv", rows,
				WorkflowSlot.ALIGNMENT);
	}

	/**
	 * @param columnMap
	 * @param ll
	 */
	private void savePeakMatchTable(final Map<String, Integer> columnMap,
			final List<List<IPeak>> ll) {
		final List<List<String>> rows = new ArrayList<List<String>>(ll.size());
		List<String> headers = null;
		final String[] headerLine = new String[columnMap.size()];
		for (int i = 0; i < headerLine.length; i++) {
			headerLine[i] = "";
		}
		headers = Arrays.asList(headerLine);
		for (final String s : columnMap.keySet()) {
			headers.set(columnMap.get(s), StringTools.removeFileExt(s));
		}
		log.debug("Adding row {}", headers);
		rows.add(headers);
		for (final List<IPeak> l : ll) {
			final String[] line = new String[columnMap.size()];
			for (int i = 0; i < line.length; i++) {
				line[i] = "-";
			}
			log.debug("Adding {} peaks: {}", l.size(), l);
			for (final IPeak p : l) {
				final String iff = p.getAssociation();
				EvalTools.notNull(iff, this);
				final int pos = columnMap.get(iff).intValue();
				log.debug("Insert position for {}: {}", iff, pos);
				if (pos >= 0) {
					if (line[pos].equals("-")) {
						line[pos] = p.getScanIndex() + "";
					} else {
						log.warn("Array position {} already used!", pos);
					}
				}
			}
			final List<String> v = Arrays.asList(line);
			rows.add(v);
			log.debug("Adding row {}", v);
		}

		final CSVWriter csvw = new CSVWriter();
		csvw.setWorkflow(getWorkflow());
		csvw.writeTableByRows(getWorkflow().getOutputDirectory(this).
				getAbsolutePath(), "multiple-alignment.csv", rows,
				WorkflowSlot.ALIGNMENT);
	}

	private void savePeakList(Map<String, IFileFragment> nameToFragment, Collection<IPeak> peaks, String filename, String type) {
		File output = new File(getWorkflow().getOutputDirectory(this), filename);
		output.getParentFile().mkdirs();
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(output));
			int i = 1;
			for (final IPeak p : peaks) {
				IFileFragment fragment = nameToFragment.get(p.getAssociation());
				Tuple2D<Array, Array> t = MaltcmsTools.getMS(fragment, p.getScanIndex());
				ArrayInt.D1 intens = new ArrayInt.D1(t.getSecond().getShape()[0]);
				MAMath.copyInt(intens, t.getSecond());
				ArrayDouble.D1 massArray = new ArrayDouble.D1(t.getFirst().getShape()[0]);
				MAMath.copyDouble(massArray, t.getFirst());
				String name = p.getAssociation() + "-IDX_" + p.getScanIndex() + "-RT_" + p.getScanAcquisitionTime();
				IMetabolite im = new Metabolite(p.getName().isEmpty() ? name : p.getName(), p.getAssociation() + "-IDX_" + p.getScanIndex() + "-RT_" + p.getScanAcquisitionTime(), getClass().getSimpleName() + "-" + type, i++, "", "", "", Double.NaN, p.getScanAcquisitionTime(), "sec", -1, "", p.getName(), massArray, intens);
				bw.write(im.toString());
				bw.newLine();
			}
			bw.close();
			getWorkflow().append(new DefaultWorkflowResult(output, this, WorkflowSlot.FILEIO, nameToFragment.values().toArray(new IFileFragment[0])));
		} catch (IOException ex) {
			log.warn("{}", ex);
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException ex1) {
					log.warn("{}", ex1);
				}
			}
		}

	}

	private void saveToXMLAlignment(final TupleND<IFileFragment> tuple,
			final List<List<IPeak>> ll) {
		AlignmentFactory af = new AlignmentFactory();
		Alignment a = af.createNewAlignment(this.getClass().getName(), false);
		HashMap<IFileFragment, List<Integer>> fragmentToScanIndexMap = new HashMap<IFileFragment, List<Integer>>();
		for (final List<IPeak> l : ll) {
			log.debug("Adding {} peaks: {}", l.size(), l);
			HashMap<String, IPeak> fragToPeak = new HashMap<String, IPeak>();
			for (final IPeak p : l) {
				fragToPeak.put(p.getAssociation(), p);
			}
			for (final IFileFragment iff : tuple) {
				int scanIndex = -1;
				if (fragToPeak.containsKey(iff.getName())) {
					IPeak p = fragToPeak.get(iff.getName());
					scanIndex = p.getScanIndex();
				}

				List<Integer> scans = null;
				if (fragmentToScanIndexMap.containsKey(iff)) {
					scans = fragmentToScanIndexMap.get(iff);
				} else {
					scans = new ArrayList<Integer>();
					fragmentToScanIndexMap.put(iff, scans);
				}

				scans.add(scanIndex);
			}
		}

		for (IFileFragment iff : fragmentToScanIndexMap.keySet()) {
			af.addScanIndexMap(a, iff.getUri(),
					fragmentToScanIndexMap.get(iff), false);
		}
		File out = new File(getWorkflow().getOutputDirectory(this),
				"peakCliqueAssignment.maltcmsAlignment.xml");
		af.save(a, out);
		DefaultWorkflowResult dwr = new DefaultWorkflowResult(out, this,
				WorkflowSlot.ALIGNMENT, tuple.toArray(new IFileFragment[]{}));
		getWorkflow().append(dwr);
	}

	private void saveCliquePlots(final List<Clique> l, final TupleND<IFileFragment> al) {
		DefaultBoxAndWhiskerCategoryDataset dscdRT = new DefaultBoxAndWhiskerCategoryDataset();
		for (Clique c : l) {
			dscdRT.add(c.createRTBoxAndWhisker(), "", c.getCliqueRTMean());
		}
		JFreeChart jfc = ChartFactory.createBoxAndWhiskerChart("Cliques",
				"clique mean RT", "RT diff to centroid", dscdRT, true);
		jfc.getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
		PlotRunner pr = new PlotRunner(jfc.getCategoryPlot(),
				"Clique RT diff to centroid", "cliquesRTdiffToCentroid.png",
				getWorkflow().getOutputDirectory(this));
		pr.configure(Factory.getInstance().getConfiguration());
		pr.setImgwidth(800);
		pr.setImgheight(25 * l.size());
		final File f = pr.getFile();
		final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f, this,
				WorkflowSlot.VISUALIZATION,
				al.toArray(new IFileFragment[]{}));
		getWorkflow().append(dwr);
		Factory.getInstance().submitJob(pr);

		DefaultBoxAndWhiskerCategoryDataset dscdTIC = new DefaultBoxAndWhiskerCategoryDataset();
		for (Clique c : l) {
			dscdTIC.add(c.createApexTicBoxAndWhisker(), "", c.getCliqueRTMean());
		}
		JFreeChart jfc2 = ChartFactory.createBoxAndWhiskerChart("Cliques",
				"clique mean RT", "log(apex TIC centroid)-log(apex TIC)",
				dscdTIC, true);
		jfc2.getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
		PlotRunner pr2 = new PlotRunner(jfc2.getCategoryPlot(),
				"Clique log apex TIC centroid diff to log apex TIC",
				"cliquesLogApexTICCentroidDiffToLogApexTIC.png",
				getWorkflow().getOutputDirectory(this));
		pr2.configure(Factory.getInstance().getConfiguration());
		pr2.setImgwidth(800);
		pr2.setImgheight(25 * l.size());
		final File g = pr.getFile();
		final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(g, this,
				WorkflowSlot.VISUALIZATION,
				al.toArray(new IFileFragment[]{}));
		getWorkflow().append(dwr2);
		Factory.getInstance().submitJob(pr2);
	}
}
