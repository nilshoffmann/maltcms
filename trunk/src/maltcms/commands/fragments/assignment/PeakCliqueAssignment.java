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
package maltcms.commands.fragments.assignment;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import javax.media.jai.JAI;

import maltcms.commands.distances.ArrayTimePenalizedDot;
import maltcms.commands.distances.IArrayDoubleComp;
import maltcms.commands.filters.array.MinMaxNormalizationFilter;
import maltcms.commands.fragments.peakfinding.TICPeakFinder;
import maltcms.datastructures.alignment.AlignmentFactory;
import maltcms.datastructures.peak.Clique;
import maltcms.datastructures.peak.Peak;
import maltcms.io.csv.CSVWriter;
import maltcms.io.csv.ColorRampReader;
import maltcms.io.xml.bindings.alignment.Alignment;
import maltcms.statistics.OneWayPeakAnova;
import maltcms.tools.ArrayTools;
import maltcms.tools.ImageTools;
import maltcms.ui.charts.PlotRunner;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.tools.EvalTools;
import cross.tools.MathTools;
import cross.tools.StringTools;

/**
 * For every peak in each chromatogram, its bi-directional best hits are
 * determined, and all bi-directional best hits are merged. If they apply, they
 * cover a set of k peaks in k chromatograms.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@RequiresVariables(names = { "var.tic_peaks", "var.binned_mass_values",
        "var.binned_intensity_values", "var.binned_scan_index",
        "var.scan_acquisition_time", "var.mass_values", "var.intensity_values",
        "var.scan_index" })
@ProvidesVariables(names = { "var.anchors.retention_index_names",
        "var.anchors.retention_times", "var.anchors.retention_indices",
        "var.anchors.retention_scans" })
public class PeakCliqueAssignment extends AFragmentCommand {

	class PeakComparator implements Comparator<Peak> {

		@Override
		public int compare(final Peak o1, final Peak o2) {
			if (o1.getScanIndex() == o2.getScanIndex()) {
				return 0;
			} else if (o1.getScanIndex() < o2.getScanIndex()) {
				return -1;
			}
			return 1;
		}
	}

	class PeakFileFragmentComparator implements Comparator<Peak> {
		@Override
		public int compare(final Peak o1, final Peak o2) {
			final int i = new PeakComparator().compare(o1, o2);
			if (i == 0) {
				return o1.getAssociation().compareTo(o2);
			}
			return i;
		}
	}

	private IArrayDoubleComp costFunctionClass = new ArrayTimePenalizedDot();

	@Configurable(name = "var.tic_peaks")
	private String ticPeaksVariableName = "tic_peaks";

	@Configurable(name = "var.mass_values")
	private String massValuesVariableName = "mass_values";

	@Configurable(name = "var.scan_index")
	private String scanIndexVariableName = "scan_index";

	@Configurable(name = "var.intensity_values")
	private String intensityValuesVariableName = "intensity_values";

	@Configurable
	private boolean useUserSuppliedAnchors = false;

	private final Logger log = Logging.getLogger(this.getClass());

	@Configurable
	private int minCliqueSize = -1;

	@Configurable(name = "var.retention_index_names")
	private String anchorNamesVariableName = "retention_index_names";

	@Configurable(name = "var.retention_times")
	private String anchorTimesVariableName = "retention_times";

	@Configurable(name = "var.retention_indices")
	private String anchorRetentionIndexVariableName = "retention_indices";

	@Configurable(name = "var.retention_scans")
	private String anchorScanIndexVariableName = "retention_scans";

	// private boolean keepOnlyBiDiBestHitsForAll = true;

	@Configurable(name = "var.binned_intensity_values")
	private String binnedIntensitiesVariableName = "binned_intensity_values";

	@Configurable(name = "var.binned_scan_index")
	private String binnedScanIndexVariableName = "binned_scan_index";

	@Configurable(name = "var.scan_acquisition_time")
	private String scanAcquisitionTimeVariableName = "scan_acquisition_time";

	@Configurable
	private boolean savePeakSimilarities;

	@Configurable
	private boolean exportAlignedFeatures;

	@Configurable
	private double maxRTDifference = 600;

	@Configurable
	private double intensityStdDeviationFactor = 1.0d;

	private HashMap<Peak, Clique> peakToClique = new HashMap<Peak, Clique>();

	/**
	 * FIXME this method seems to create correct scan indices only for the first
	 * two files.
	 * 
	 * @param al
	 * @param newFragments
	 * @param cliques
	 * @return
	 */
	private TupleND<IFileFragment> addAnchors(final List<List<Peak>> al,
	        final TupleND<IFileFragment> newFragments,
	        final List<Clique> cliques) {

		final String ri_names = this.anchorNamesVariableName;
		final String ri_times = this.anchorTimesVariableName;
		final String ri_indices = this.anchorRetentionIndexVariableName;
		final String ri_scans = this.anchorScanIndexVariableName;

		findCenter(newFragments, cliques);

		// Create Variables and arrays
		final HashMap<String, IFileFragment> hm = new HashMap<String, IFileFragment>();
		this.log.info("Preparing files!");
		for (final IFileFragment ff : newFragments) {
			this.log.debug("Source files of fragment {}: {}", ff
			        .getAbsolutePath(), ff.getSourceFiles());
			hm.put(ff.getName(), ff);
			final int size = getNumberOfPeaksWithinCliques(ff, cliques);
			final ArrayInt.D1 anchors = new ArrayInt.D1(size);
			ArrayTools.fillArray(anchors, Integer.valueOf(-1));
			final IVariableFragment ri = ff.hasChild(ri_scans) ? ff
			        .getChild(ri_scans) : new VariableFragment(ff, ri_scans);
			final ArrayDouble.D1 anchorTimes = new ArrayDouble.D1(size);
			ArrayTools.fillArray(anchorTimes, Double.valueOf(-1));
			final IVariableFragment riTimes = ff.hasChild(ri_times) ? ff
			        .getChild(ri_times) : new VariableFragment(ff, ri_times);
			final ArrayChar.D2 names = cross.tools.ArrayTools
			        .createStringArray(size, 256);
			final IVariableFragment riNames = ff.hasChild(ri_names) ? ff
			        .getChild(ri_names) : new VariableFragment(ff, ri_names);
			final ArrayDouble.D1 rindexA = new ArrayDouble.D1(size);
			final IVariableFragment rindex = ff.hasChild(ri_indices) ? ff
			        .getChild(ri_indices)
			        : new VariableFragment(ff, ri_indices);
			ri.setArray(anchors);
			riNames.setArray(names);
			riTimes.setArray(anchorTimes);
			rindex.setArray(rindexA);
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

		this.log.info("Setting anchors!");
		for (final Clique c : cliques) {
			// Matched Peaks
			for (final Peak p : c.getPeakList()) {
				final IFileFragment association = hm.get(p.getAssociation()
				        .getName());
				int slot = placeMap.get(association.getName());
				if (peakToClique.containsKey(p)) {
					id = nextIndex[slot];
					long cid = peakToClique.get(p).getID();
					this.log
					        .debug(
					                "Adding anchor at scan index {} and rt {} with id {} to file {}",
					                new Object[] { p.getScanIndex(),
					                        p.getScanAcquisitionTime(), cid,
					                        association.getName() });
					String name = "A" + cid;
					if (!(p.getName().equals(""))) {
						name = name + "_" + p.getName();

					}
					// this.log.info("Adding anchor with name {}", name);
					((ArrayChar.D2) association.getChild(ri_names).getArray())
					        .setString(id, name);
					((ArrayInt.D1) association.getChild(ri_scans).getArray())
					        .set(id, p.getScanIndex());
					((ArrayDouble.D1) association.getChild(ri_times).getArray())
					        .set(id, p.getScanAcquisitionTime());
					nextIndex[slot]++;
				}

			}
			id++;
		}

		// cleanup
		for (final IFileFragment ff : newFragments) {
			// make sure, that work array data is removed
			if (ff.hasChild(this.ticPeaksVariableName)) {
				ff.removeChild(ff.getChild(this.ticPeaksVariableName));
			}
			if (ff.hasChild(this.binnedIntensitiesVariableName)) {
				ff.removeChild(ff.getChild(this.binnedIntensitiesVariableName));
			}
			if (ff.hasChild(this.binnedScanIndexVariableName)) {
				ff.removeChild(ff.getChild(this.binnedScanIndexVariableName));
			}
			if (ff.hasChild(this.scanIndexVariableName)) {
				ff.removeChild(ff.getChild(this.scanIndexVariableName));
			}
			if (ff.hasChild(this.massValuesVariableName)) {
				ff.removeChild(ff.getChild(this.massValuesVariableName));
			}
			if (ff.hasChild(this.intensityValuesVariableName)) {
				ff.removeChild(ff.getChild(this.intensityValuesVariableName));
			}
			ff.getChild(this.scanAcquisitionTimeVariableName).getArray();
			final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
			        new File(ff.getAbsolutePath()), this, getWorkflowSlot(), ff);
			getIWorkflow().append(dwr);
			ff.save();
		}
		return newFragments;
	}

	private int getNumberOfPeaksWithinCliques(IFileFragment iff, List<Clique> l) {
		int npeaks = 0;
		for (Clique c : l) {
			for (Peak p : c.getPeakList()) {
				if (p.getAssociation().getName().equals(iff.getName())) {
					npeaks++;
				}
			}
		}
		return npeaks;
	}

	private List<Clique> getCommonCliques(IFileFragment a, IFileFragment b,
	        List<Clique> l) {
		List<Clique> commonCliques = new ArrayList<Clique>();
		this.log.debug("Retrieving common cliques");
		for (Clique c : l) {
			for (Peak p : c.getPeakList()) {
				if (p.getAssociation().getName().equals(a.getName())
				        || p.getAssociation().getName().equals(b.getName())) {
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
			for (Peak p : c.getPeakList()) {
				for (Peak q : c.getPeakList()) {
					if (p.getAssociation().getName().equals(a.getName())
					        && q.getAssociation().getName().equals(b.getName())) {
						double v = getSimilarity(p, q);
						log.debug("Similarity of {} and {} = {}", new Object[] {
						        p, q, v });
						score += v;
					}
				}
			}
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
			for (Peak p : c.getPeakList()) {
				cliqueNumbers[placeMap.get(p.getAssociation().getName())]++;
				cliqueSize[placeMap.get(p.getAssociation().getName())] += c
				        .getPeakList().size();
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
		this.log.info("Calculating fragment scores");
		ArrayChar.D2 fragmentNames = new ArrayChar.D2(newFragments.size(), 1024);
		for (int i = 0; i < newFragments.size(); i++) {
			fragmentNames.setString(i, newFragments.get(i).getName());
			for (int j = 0; j < newFragments.size(); j++) {
				if (i != j) {
					List<Clique> commonCliques = getCommonCliques(newFragments
					        .get(i), newFragments.get(j), cliques);
					if (!commonCliques.isEmpty()) {
						fragmentScores.set(i, j, getCommonScore(newFragments
						        .get(i), newFragments.get(j), commonCliques)
						        / ((double) commonCliques.size()));
						// FIXME this should not be necessary, in some cases,
						// similarities are
						// removed further upstream
						fragScores[i] += fragmentScores.get(i, j);
					} else {
						log.debug("Common cliques list is empty!");
					}
				} else {
					if (this.costFunctionClass.minimize()) {
						fragmentScores.set(i, j, 0.0);
					} else {
						fragmentScores.set(i, j, 1.0);
					}
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

		// // normalize scores by number of cliques for each FileFragment
		// for (IFileFragment f : newFragments) {
		// fragScores[placeMap.get(f.getName())] /= cliqueNumbers[placeMap
		// .get(f.getName())];
		// }

		// normalize scores by total score
		// for (IFileFragment f : newFragments) {
		// fragScores[placeMap.get(f.getName())] /= sumFragScores;
		// }

		for (int j = 0; j < newFragments.size(); j++) {
			log.info("File: {}, value: {}", newFragments.get(j).getName(),
			        fragScores[j]);
		}

		int optIndex = -1;
		boolean minimize = costFunctionClass.minimize();
		double optVal = minimize ? Double.POSITIVE_INFINITY
		        : Double.NEGATIVE_INFINITY;
		for (int i = 0; i < fragScores.length; i++) {
			if (minimize) {
				if (fragScores[i] < optVal) {
					optVal = Math.min(optVal, fragScores[i]);
					optIndex = i;
				}
			} else {
				if (fragScores[i] > optVal) {
					optVal = Math.max(optVal, fragScores[i]);
					optIndex = i;
				}
			}
		}

		log.info("{} with value {} is the center star!", newFragments
		        .get(optIndex), optVal);
	}

	public void saveToCSV(final ArrayDouble.D2 distances,
	        final ArrayChar.D2 names) {
		final CSVWriter csvw = Factory.getInstance().getObjectFactory()
		        .instantiate(CSVWriter.class);
		csvw.setIWorkflow(getIWorkflow());
		csvw.writeArray2DwithLabels(getIWorkflow().getOutputDirectory(this)
		        .getAbsolutePath(), "pairwise_distances.csv", distances, names,
		        this.getClass(), WorkflowSlot.STATISTICS, getIWorkflow()
		                .getStartupDate());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		for (final IFileFragment iff : t) {
			this.log.debug("{}", iff);
		}
		if (t.size() < 2) {
			this.log
			        .warn("At least two files required for peak clique assignment!");
		} else {
			final TupleND<IFileFragment> tret = identifyPeaks(t);
			for (final IFileFragment iff : tret) {
				this.log.debug("{}", iff);
			}
			return tret;
		}
		return t;
	}

	/**
	 * @param al
	 * @param fragmentToPeaks
	 */
	private void calculatePeakSimilarities(final TupleND<IFileFragment> al,
	        final HashMap<String, List<Peak>> fragmentToPeaks) {
		int n = 0;
		for (String key : fragmentToPeaks.keySet()) {
			n += fragmentToPeaks.get(key).size();
		}
		this.log.info(
		        "Calculating {} pairwise peak similarities for {} peaks!",
		        ((long) n * (long) n), n);
		this.log.info("Using {} as pairwise peak similarity!",
		        this.costFunctionClass.getClass().getName());
		// Loop over all pairs of FileFragments
		long elements = (long) n * (long) n;
		double percentDone = 0;
		final long parts = 10;
		long partCnt = 0;
		long elemCnt = 0;
		// k^{2}, could be reduced to k^{2}-k, if we only use pairwise distinct
		// file fragments
		for (IFileFragment f1 : al) {
			for (IFileFragment f2 : al) {
				// calculate similarity between peaks
				final List<Peak> lhsPeaks = fragmentToPeaks.get(f1.getName());
				final List<Peak> rhsPeaks = fragmentToPeaks.get(f2.getName());
				this.log.debug("Comparing {} and {}", f1.getName(), f2
				        .getName());
				// all-against-all peak list comparison
				// n^{2} for n=max(|lhsPeaks|,|rhsPeaks|)
				for (final Peak p1 : lhsPeaks) {
					for (final Peak p2 : rhsPeaks) {
						// skip peaks, which are too far apart
						double rt1 = p1.getScanAcquisitionTime();
						double rt2 = p2.getScanAcquisitionTime();
						// cutoff to limit calculation work
						// this has a better effect, than applying the limit
						// within the similarity function only
						// of course, this limit should be larger
						// than the limit within the similarity function
						if (Math.abs(rt1 - rt2) < this.maxRTDifference) {
							// the similarity is symmetric:
							// sim(a,b) = sim(b,a)
							final Double d = getSimilarity(p1, p2);
							p1.addSimilarity(p2, d);
							p2.addSimilarity(p1, d);
						}
						elemCnt++;
					}
				}
				// update progress
				percentDone = ArrayTools.calcPercentDone(elements, elemCnt);
				partCnt = ArrayTools.printPercentDone(percentDone, parts,
				        partCnt, this.log);
			}
		}

		if (this.savePeakSimilarities) {
			visualizePeakSimilarities(fragmentToPeaks, 256, "beforeBIDI");
		}
	}

	/**
	 * FIXME add support for 1D only chromatograms -> see TICDynamicTimeWarp for
	 * that
	 * 
	 * @param al
	 * @return
	 */
	private HashMap<String, List<Peak>> checkUserSuppliedAnchors(
	        final TupleND<IFileFragment> al) {
		// Check for already defined peaks
		final HashMap<String, List<Peak>> definedAnchors = new HashMap<String, List<Peak>>();
		for (final IFileFragment t : al) {
			IVariableFragment anames = null;
			IVariableFragment ascans = null;
			try {
				anames = t.getChild(this.anchorNamesVariableName);
				ascans = t.getChild(this.anchorScanIndexVariableName);
				final ArrayChar.D2 peakNames = (ArrayChar.D2) anames.getArray();
				final Array peakScans = ascans.getArray();
				final Index peakScansI = peakScans.getIndex();

				t.getChild(this.binnedIntensitiesVariableName).setIndex(
				        t.getChild(this.binnedScanIndexVariableName));
				this.log.info("Checking user supplied anchors for: {}", t);
				final Array scan_acquisition_time = t.getChild(
				        this.scanAcquisitionTimeVariableName).getArray();
				List<Peak> peaks = null;
				if (definedAnchors.containsKey(t.getName())) {
					peaks = definedAnchors.get(t.getName());
				} else {
					peaks = new ArrayList<Peak>();
				}

				final Index sat1 = scan_acquisition_time.getIndex();
				final List<Array> bintens = t.getChild(
				        this.binnedIntensitiesVariableName).getIndexedArray();
				for (int i = 0; i < peakScans.getShape()[0]; i++) {
					final String name = peakNames.getString(i);
					final int scan = peakScans.getInt(peakScansI.set(i));
					final double sat = scan_acquisition_time.getDouble(sat1
					        .set(scan));
					this.log.debug("{}", t.getName());
					final Peak p = new Peak(name, t, scan, bintens.get(scan),
					        sat);
					this.log.debug(
					        "Adding user supplied anchor {} with name {}", p, p
					                .getName());
					peaks.add(p);
				}
				definedAnchors.put(t.getName(), peaks);
			} catch (final ResourceNotAvailableException rne) {
				this.log.debug("Could not find any user-defined anchors!");
				definedAnchors.put(t.getName(), new ArrayList<Peak>(0));
			}
		}
		return definedAnchors;
	}

	/**
	 * @param al
	 * @param fragmentToPeaks
	 * @param ll
	 * @param minCliqueSize
	 * @return a list of clique objects
	 */
	private List<Clique> combineBiDiBestHits(final TupleND<IFileFragment> al,
	        final HashMap<String, List<Peak>> fragmentToPeaks,
	        final List<List<Peak>> ll, final int minCliqueSize) {

		// given: a hashmap of name<->peak list
		// an empty list of peaks belonging to a clique
		// a minimum size for a clique from when on it is considered valid
		peakToClique = new HashMap<Peak, Clique>();
		// every peak is assigned to at most one clique!!!
		// reassignment is invalid and should not occur
		// for all files
		for (IFileFragment iff : al) {
			final List<Peak> peaks = fragmentToPeaks.get(iff.getName());
			this.log.info("Checking {} peaks for file {}", peaks.size(), iff
			        .getName());
			// for all peaks in file

			// final List<Peak> bidiHits = new ArrayList<Peak>();
			// bidiHits.add(p);
			// for all other files
			for (final IFileFragment jff : al) {
				// only compare between partition matches, i!=j
				if (!iff.getName().equals(jff.getName())) {
					for (final Peak p : peaks) {
						// retrieve list of most similar peaks
						final Peak q = p.getPeakWithHighestSimilarity(jff);
						if (q == null) {
							log.debug("Skipping null peak");
							continue;
						}
						// bidirectional hit
						if (p.getSimilarity(q) == Double.NEGATIVE_INFINITY
						        || p.getSimilarity(q) == Double.POSITIVE_INFINITY) {
							throw new IllegalArgumentException(
							        "Infinite similarity value for associated peaks!");
						}
						if (q != null && q.isBidiBestHitFor(p)) {
							this.log
							        .debug(
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
							Clique c = null, d = null;
							if (peakToClique.containsKey(q)) {
								d = peakToClique.get(q);
								if (d != null) {
									log.debug("Found clique for peak q");
								}
							}
							if (peakToClique.containsKey(p)) {// p has a clique
								c = peakToClique.get(p);
								log.debug("Found clique for peak p");
								if (d != null && c != d) {
									log
									        .debug("Found different cliques for peak p and q!");
									log.debug("Clique for p: {}", c);
									log.debug("Clique for q: {}", d);
									// try to merge cliques
									int ds = d.getPeakList().size();
									int cs = c.getPeakList().size();
									log.debug("Merging cliques");
									// ds has more peaks than cs -> join cs into
									// ds
									if (ds > cs) {
										for (Peak pk : c.getPeakList()) {
											if (d.addPeak(pk)) {

											} else {
												log
												        .debug(
												                "Adding of peak {} failed",
												                pk);
											}
											peakToClique.put(pk, d);
										}
										c.clear();
									} else {// ds has less peaks than cs -> join
										// ds into cs
										for (Peak pk : d.getPeakList()) {
											if (c.addPeak(pk)) {

											} else {
												log
												        .debug(
												                "Adding of peak {} failed",
												                pk);
											}
											peakToClique.put(pk, c);
										}
										d.clear();
									}
								} else {
									if (c.addPeak(q)) {
										peakToClique.put(q, c);
									}
								}
							} else {// add a new clique, if both peaks are not
								// assigned yet
								c = new Clique();
								if (c.addPeak(p)) {
									peakToClique.put(p, c);
								}
								if (c.addPeak(q)) {
									peakToClique.put(q, c);
								}
							}
						} else {
							this.log
							        .debug(
							                "Peak q:{} and p:{} are no bidirectional best hits!",
							                p, q);
						}
					}
				}
			}
		}

		// retain all cliques, which exceed minimum size
		HashSet<Clique> cliques = new HashSet<Clique>();
		for (Clique c : peakToClique.values()) {
			if (!cliques.contains(c)) {
				this.log.debug("Size of clique: {}\n{}",
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

		// add all remaining cliques to ll
		this.log.debug("Minimum clique size: {}", minCliqueSize);
		ListIterator<Clique> li = l.listIterator();
		while (li.hasNext()) {
			Clique c = li.next();
			try {
				this.log.debug("Clique {}", c);
			} catch (NullPointerException npe) {
				this.log.debug("Clique empty?: {}", c.getPeakList());
			}
			if (c.getPeakList().size() >= minCliqueSize) {
				ll.add(c.getPeakList());
			} else {
				li.remove();
			}
		}

		String groupFileLocation = Factory.getInstance().getConfiguration()
		        .getString("groupFileLocation", "");

		OneWayPeakAnova owa = new OneWayPeakAnova();
		owa.setIWorkflow(getIWorkflow());
		owa.calcFisherRatios(l, al, groupFileLocation);

		DefaultBoxAndWhiskerCategoryDataset dscdRT = new DefaultBoxAndWhiskerCategoryDataset();
		for (Clique c : l) {
			dscdRT.add(c.createRTBoxAndWhisker(), "", c.getCliqueRTMean());
		}
		JFreeChart jfc = ChartFactory.createBoxAndWhiskerChart("Cliques",
		        "clique mean RT", "RT diff to centroid", dscdRT, true);
		jfc.getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
		PlotRunner pr = new PlotRunner(jfc.getCategoryPlot(),
		        "Clique RT diff to centroid", "cliquesRTdiffToCentroid.png",
		        getIWorkflow().getOutputDirectory(this));
		pr.configure(Factory.getInstance().getConfiguration());
		final File f = pr.getFile();
		final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f, this,
		        WorkflowSlot.VISUALIZATION, al.toArray(new IFileFragment[] {}));
		getIWorkflow().append(dwr);
		Factory.getInstance().submitJob(pr);

		DefaultBoxAndWhiskerCategoryDataset dscdTIC = new DefaultBoxAndWhiskerCategoryDataset();
		for (Clique c : l) {
			dscdTIC
			        .add(c.createApexTicBoxAndWhisker(), "", c
			                .getCliqueRTMean());
		}
		JFreeChart jfc2 = ChartFactory.createBoxAndWhiskerChart("Cliques",
		        "clique mean RT", "log(apex TIC centroid)-log(apex TIC)",
		        dscdTIC, true);
		jfc2.getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
		PlotRunner pr2 = new PlotRunner(jfc2.getCategoryPlot(),
		        "Clique log apex TIC centroid diff to log apex TIC",
		        "cliquesLogApexTICCentroidDiffToLogApexTIC.png", getIWorkflow()
		                .getOutputDirectory(this));
		pr.configure(Factory.getInstance().getConfiguration());
		final File g = pr.getFile();
		final DefaultWorkflowResult dwr2 = new DefaultWorkflowResult(g, this,
		        WorkflowSlot.VISUALIZATION, al.toArray(new IFileFragment[] {}));
		getIWorkflow().append(dwr2);
		Factory.getInstance().submitJob(pr2);

		this.log.info("Found {} cliques", l.size());
		return l;
	}

	@Override
	public void configure(final Configuration cfg) {
		super.configure(cfg);
		this.scanAcquisitionTimeVariableName = cfg.getString(
		        "var.scan_acquisition_time", "scan_acquisition_time");
		this.binnedIntensitiesVariableName = cfg.getString(
		        "var.binned_intensity_values", "binned_intensity_values");
		this.binnedScanIndexVariableName = cfg.getString(
		        "var.binned_scan_index", "binned_scan_index");
		this.anchorNamesVariableName = cfg.getString(
		        "var.anchors.retention_index_names", "retention_index_names");
		this.anchorTimesVariableName = cfg.getString(
		        "var.anchors.retention_times", "retention_times");
		this.anchorRetentionIndexVariableName = cfg.getString(
		        "var.anchors.retention_indices", "retention_indices");
		this.anchorScanIndexVariableName = cfg.getString(
		        "var.anchors.retention_scans", "retention_scans");
		this.massValuesVariableName = cfg.getString("var.mass_values",
		        "mass_values");
		this.scanIndexVariableName = cfg.getString("var.scan_index",
		        "scan_index");
		this.intensityValuesVariableName = cfg.getString(
		        "var.intensity_values", "intensity_values");
		this.ticPeaksVariableName = cfg.getString("var.tic_peaks", "tic_peaks");
		this.minCliqueSize = cfg.getInt(this.getClass().getName()
		        + ".minCliqueSize", -1);
		final String aldist = "maltcms.commands.distances.ArrayLp";
		this.costFunctionClass = Factory.getInstance().getObjectFactory()
		        .instantiate(
		                cfg.getString(this.getClass().getName()
		                        + ".costFunction", aldist),
		                IArrayDoubleComp.class);
		this.useUserSuppliedAnchors = cfg.getBoolean(this.getClass().getName()
		        + ".useUserSuppliedAnchors", false);
		this.savePeakSimilarities = cfg.getBoolean(this.getClass().getName()
		        + ".savePeakSimilarities", false);
		this.exportAlignedFeatures = cfg.getBoolean(this.getClass().getName()
		        + ".exportAlignedFeatures", false);
		this.maxRTDifference = cfg.getDouble(this.getClass().getName()
		        + ".maxRTDifference", 600.0);
		this.intensityStdDeviationFactor = cfg.getDouble(this.getClass()
		        .getName()
		        + ".intensityStdDeviationFactor", 5);
	}

	/**
	 * @param al
	 * @param fragmentToPeaks
	 */
	private void findBiDiBestHits(final TupleND<IFileFragment> al,
	        final HashMap<String, List<Peak>> fragmentToPeaks) {
		// For each pair of FileFragments
		final Set<Peak> matchedPeaks = new HashSet<Peak>();
		for (final Tuple2D<IFileFragment, IFileFragment> t : al.getPairs()) {

			final List<Peak> lhsPeaks = fragmentToPeaks.get(t.getFirst()
			        .getName());
			final List<Peak> rhsPeaks = fragmentToPeaks.get(t.getSecond()
			        .getName());
			this.log.debug("lhsPeaks: {}", lhsPeaks.size());
			this.log.debug("rhsPeaks: {}", rhsPeaks.size());
			for (final Peak plhs : lhsPeaks) {
				for (final Peak prhs : rhsPeaks) {
					this.log.debug("Checking peaks {} and {}", plhs, prhs);
					if (plhs.isBidiBestHitFor(prhs)) {
						this.log.debug(
						        "Found a bidirectional best hit: {} and {}",
						        plhs, prhs);
						matchedPeaks.add(plhs);
						matchedPeaks.add(prhs);
						prhs.retainSimilarityRemoveRest(plhs);
						plhs.retainSimilarityRemoveRest(prhs);
					}

				}
			}
		}

		this.log.info("Retained {} matched peaks!", matchedPeaks.size());
		this.log.debug("Counting and removing unmatched peaks!");
		int peaks = 0;
		for (final IFileFragment t : al) {
			final List<Peak> lhsPeaks = fragmentToPeaks.get(t.getName());
			this.log.debug("lhsPeaks: {}", lhsPeaks.size());
			ListIterator<Peak> liter = lhsPeaks.listIterator();
			while (liter.hasNext()) {
				final Peak plhs = liter.next();
				if (!matchedPeaks.contains(plhs)) {
					peaks++;
					liter.remove();
				}
			}
		}
		this.log.info("Removed {} unmatched peaks!", peaks);
	}

	public void saveSimilarityMatrix(final TupleND<IFileFragment> al,
	        final HashMap<String, List<Peak>> fragmentToPeaks) {
		for (final IFileFragment iff1 : al) {
			for (final IFileFragment iff2 : al) {
				final List<Peak> lhsPeaks = fragmentToPeaks.get(iff1.getName());
				final List<Peak> rhsPeaks = fragmentToPeaks.get(iff2.getName());
				double score = 0;
				int bidihits = 0;
				this.log.debug("lhsPeaks: {}", lhsPeaks.size());
				this.log.debug("rhsPeaks: {}", rhsPeaks.size());
				for (final Peak plhs : lhsPeaks) {
					for (final Peak prhs : rhsPeaks) {
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
	@Override
	public String getDescription() {
		return "Assigns peak candidates as pairs and groups them into cliques of size k";
	}

	public double getSimilarity(final Peak a, final Peak b) {
		final double v = this.costFunctionClass.apply(-1, -1, a
		        .getScanAcquisitionTime(), b.getScanAcquisitionTime(), a
		        .getMSIntensities(), b.getMSIntensities());
		return this.costFunctionClass.minimize() ? -v : v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.PEAKMATCHING;
	}

	private TupleND<IFileFragment> identifyPeaks(
	        final TupleND<IFileFragment> originalFragments) {
		this.log.debug("Matching peaks");
		final HashMap<String, List<Peak>> fragmentToPeaks = new HashMap<String, List<Peak>>();
		final HashMap<String, Integer> columnMap = new HashMap<String, Integer>(
		        originalFragments.size());

		final ArrayList<IFileFragment> al2 = new ArrayList<IFileFragment>();
		for (final IFileFragment iff : originalFragments) {
			final IFileFragment iff2 = Factory.getInstance()
			        .getFileFragmentFactory().create(
			                new File(getIWorkflow().getOutputDirectory(this),
			                        iff.getName()));
			iff2.addSourceFile(iff);
			this.log.debug("Created work file {}", iff2);
			al2.add(iff2);
		}
		final TupleND<IFileFragment> t = new TupleND<IFileFragment>(al2);
		initializePeaks(t, fragmentToPeaks, columnMap);
		this.log.info("Calculating all-against-all peak similarities");
		calculatePeakSimilarities(t, fragmentToPeaks);

		this.log.info("Searching for bidirectional best hits");
		final long startT = System.currentTimeMillis();
		findBiDiBestHits(t, fragmentToPeaks);
		this.log.info("Found bidi best hits in {} milliseconds", System
		        .currentTimeMillis()
		        - startT);

		final long startT2 = System.currentTimeMillis();
		final List<List<Peak>> ll = new ArrayList<List<Peak>>();
		List<Clique> cliques;
		if (this.minCliqueSize == -1 || this.minCliqueSize == t.size()) {
			this.log
			        .info("Combining bidirectional best hits if present in all files");
			cliques = combineBiDiBestHits(t, fragmentToPeaks, ll, t.size());
			// combineBiDiBestHitsAll(t, fragmentToPeaks, ll);
		} else {
			this.log.info("Combining bidirectional best hits");
			cliques = combineBiDiBestHits(t, fragmentToPeaks, ll,
			        this.minCliqueSize);
		}
		removePeakSimilaritiesWhichHaveNoBestHits(t, fragmentToPeaks);
		this.log.info("Found cliques in {} milliseconds", System
		        .currentTimeMillis()
		        - startT2);
		if (this.savePeakSimilarities) {
			visualizePeakSimilarities(fragmentToPeaks, 2, "afterBIDI");
		}
		if (this.exportAlignedFeatures) {
			saveToLangeTautenhahnFormat(columnMap, ll);
		}
		this.log.info("Saving peak match tables!");
		savePeakMatchTable(columnMap, ll);
		savePeakMatchRTTable(columnMap, ll);
		saveToXMLAlignment(t, ll);
		this.log.info("Adding anchor variables");
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
	        final HashMap<String, List<Peak>> fragmentToPeaks,
	        final HashMap<String, Integer> columnMap) {
		int column = 0;
		// int maxPeaks = Integer.MIN_VALUE;
		int npeaks = 0;
		HashMap<String, List<Peak>> definedAnchors = new HashMap<String, List<Peak>>();
		if (this.useUserSuppliedAnchors) {
			definedAnchors = checkUserSuppliedAnchors(originalFileFragments);
			for (final IFileFragment iff : originalFileFragments) {
				definedAnchors.put(iff.getName(), new ArrayList<Peak>(0));
			}
		}
		this.log.debug("{}", definedAnchors.toString());
		// Insert Peaks into HashMap
		for (final IFileFragment t : originalFileFragments) {
			// if we have a valid variable defined,
			// use those peak indices
			Array peakCandidates1;
			try {
				IVariableFragment peakCandidates = t
				        .getChild(this.ticPeaksVariableName);
				peakCandidates1 = peakCandidates.getArray();
				this.log.debug("Peaks for file {}: {}", t.getAbsolutePath(),
				        peakCandidates1);
			} catch (ResourceNotAvailableException rnae) {
				// otherwise, create an index array for all scans!!!
				Array sidx = t.getChild(this.binnedScanIndexVariableName)
				        .getArray();
				peakCandidates1 = ArrayTools.indexArray(sidx.getShape()[0], 0);
			}

			// maxPeaks = Math.max(maxPeaks, peakCandidates1.getShape()[0]);
			t.getChild(this.binnedIntensitiesVariableName).setIndex(
			        t.getChild(this.binnedScanIndexVariableName));
			final List<Array> bintens = t.getChild(
			        this.binnedIntensitiesVariableName).getIndexedArray();
			final Array scan_acquisition_time = t.getChild(
			        this.scanAcquisitionTimeVariableName).getArray();
			EvalTools.notNull(peakCandidates1, this);
			List<Peak> peaks = null;
			if (fragmentToPeaks.containsKey(t.getName())) {
				peaks = fragmentToPeaks.get(t.getName());
			} else {
				peaks = new ArrayList<Peak>();
			}
			this.log.debug("Adding peaks for {}", t.getName());
			final Index pc1 = peakCandidates1.getIndex();
			final Index sat1 = scan_acquisition_time.getIndex();
			final List<Peak> userDefinedAnchors = definedAnchors.get(t
			        .getName());
			for (int i = 0; i < peakCandidates1.getShape()[0]; i++) {
				final int pc1i = peakCandidates1.getInt(pc1.set(i));
				final Peak p = new Peak("", t, pc1i, bintens.get(pc1i),
				        scan_acquisition_time.getDouble(sat1.set(pc1i)));
				peaks.add(p);
				npeaks++;
			}

			if ((userDefinedAnchors != null) && this.useUserSuppliedAnchors) {
				for (final Peak p : userDefinedAnchors) {

					final int n = Collections.binarySearch(peaks, p,
					        new PeakComparator());
					if (n >= 0) {// if found in list, remove and add to anchors
						final Peak q = peaks.get(n);
						q.setName(p.getName());
						this.log.debug("{} with name {} annotated by user!", p,
						        p.getName());
					} else {// else add at proposed insert position
						this.log.debug("Adding peak at position {}", n);
						peaks.add(((-1) * n) - 1, p);
					}
				}
			}

			fragmentToPeaks.put(t.getName(), peaks);
			columnMap.put(t.getName(), column++);
			// clearing space
			t.clearArrays();
		}
		this.log.debug("{} peaks present", npeaks);
	}

	public boolean isBidiBestHitForAll(final List<Peak> peaks,
	        final int numberOfFiles) {
		return isBidiBestHitForK(peaks, numberOfFiles, numberOfFiles);
	}

	public boolean isBidiBestHitForK(final List<Peak> peaks,
	        final int numberOfFiles, final int minCliqueSize) {
		int i = 0;
		int j = 0;
		for (final Peak p : peaks) {
			for (final Peak q : peaks) {
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
			Logging
			        .getLogger(PeakCliqueAssignment.class)
			        .info(
			                "Illegal value for minCliqueSize = {}, allowed values are -1, >=2 <= number of chromatograms",
			                minCliqueSize);
		}
		if (i >= minCliqueSize) {
			Logging.getLogger(PeakCliqueAssignment.class).debug(
			        "{} are BidiBestHits of each other: {}", i, peaks);
			return true;
		}
		return false;
	}

	public boolean isFirstBidiBestHitForRest(final List<Peak> peaks,
	        final int expectedHits) {
		int i = 0;
		final Peak p0 = peaks.get(0);
		for (final Peak p : peaks) {
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
			Logging.getLogger(TICPeakFinder.class).debug(
			        "All elements are BidiBestHits to first Peak: {}", peaks);
			return true;
		}
		return false;
	}

	private void removePeakSimilaritiesWhichHaveNoBestHits(
	        final TupleND<IFileFragment> t,
	        final HashMap<String, List<Peak>> fragmentToPeaks) {
		// no best hits means, that the corresponding list of sorted peaks has
		// length greater than one
		for (final String s : fragmentToPeaks.keySet()) {
			for (final Peak p : fragmentToPeaks.get(s)) {
				for (final IFileFragment iff : t) {
					final List<Peak> l = p.getPeaksSortedBySimilarity(iff);
					// clear similarities, if a best hit hasn't been assigned
					if (l.size() > 1) {
						log.debug("Clearing similarities for {} and {}", iff
						        .getName(), p);
						p.clearSimilarities();
					}
				}
			}
		}
	}

	/**
	 * @param columnMap
	 * @param ll
	 */
	private void savePeakMatchRTTable(final HashMap<String, Integer> columnMap,
	        final List<List<Peak>> ll) {
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
		this.log.debug("Adding row {}", headers);
		rows.add(headers);
		final DecimalFormat df = (DecimalFormat) NumberFormat
		        .getInstance(Locale.US);
		// this is a fix, default rounding convention is HALF_EVEN,
		// which allows less error to accumulate, but is seldomly used
		// outside of java...
		df.setRoundingMode(RoundingMode.HALF_UP);
		df.applyPattern("0.000");
		for (final List<Peak> l : ll) {
			final String[] line = new String[columnMap.size()];
			for (int i = 0; i < line.length; i++) {
				line[i] = "-";
			}
			this.log.debug("Adding {} peaks", l.size());
			for (final Peak p : l) {
				final IFileFragment iff = p.getAssociation();
				EvalTools.notNull(iff, this);
				final int pos = columnMap.get(iff.getName()).intValue();
				this.log
				        .debug("Insert position for {}: {}", iff.getName(), pos);
				if (pos >= 0) {
					if (line[pos].equals("-")) {
						final double sat = p.getScanAcquisitionTime() / 60.0d;
						line[pos] = df.format(sat) + "";
					} else {
						this.log.warn("Array position {} already used!", pos);
					}
				}
			}
			final List<String> v = Arrays.asList(line);
			rows.add(v);
			this.log.debug("Adding row {}", v);
		}

		final CSVWriter csvw = new CSVWriter();
		csvw.setIWorkflow(getIWorkflow());
		csvw.writeTableByRows(getIWorkflow().getOutputDirectory(this)
		        .getAbsolutePath(), "matched_peaksRT.csv", rows,
		        WorkflowSlot.ALIGNMENT);
	}

	/**
	 * @param columnMap
	 * @param ll
	 */
	private void savePeakMatchTable(final HashMap<String, Integer> columnMap,
	        final List<List<Peak>> ll) {
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
		this.log.debug("Adding row {}", headers);
		rows.add(headers);
		for (final List<Peak> l : ll) {
			final String[] line = new String[columnMap.size()];
			for (int i = 0; i < line.length; i++) {
				line[i] = "-";
			}
			this.log.debug("Adding {} peaks: {}", l.size(), l);
			for (final Peak p : l) {
				final IFileFragment iff = p.getAssociation();
				EvalTools.notNull(iff, this);
				final int pos = columnMap.get(iff.getName()).intValue();
				this.log
				        .debug("Insert position for {}: {}", iff.getName(), pos);
				if (pos >= 0) {
					if (line[pos].equals("-")) {
						line[pos] = p.getScanIndex() + "";
					} else {
						this.log.warn("Array position {} already used!", pos);
					}
				}
			}
			final List<String> v = Arrays.asList(line);
			rows.add(v);
			this.log.debug("Adding row {}", v);
		}

		final CSVWriter csvw = new CSVWriter();
		csvw.setIWorkflow(getIWorkflow());
		csvw.writeTableByRows(getIWorkflow().getOutputDirectory(this)
		        .getAbsolutePath(), "matched_peaks.csv", rows,
		        WorkflowSlot.ALIGNMENT);
	}

	private void saveToXMLAlignment(final TupleND<IFileFragment> tuple,
	        final List<List<Peak>> ll) {
		AlignmentFactory af = new AlignmentFactory();
		Alignment a = af.createNewAlignment(this.getClass().getName(), false);
		HashMap<IFileFragment, List<Integer>> fragmentToScanIndexMap = new HashMap<IFileFragment, List<Integer>>();
		for (final List<Peak> l : ll) {
			this.log.debug("Adding {} peaks: {}", l.size(), l);
			HashMap<IFileFragment, Peak> fragToPeak = new HashMap<IFileFragment, Peak>();
			for (final Peak p : l) {
				fragToPeak.put(p.getAssociation(), p);
			}
			for (final IFileFragment iff : tuple) {
				int scanIndex = -1;
				if (fragToPeak.containsKey(iff)) {
					Peak p = fragToPeak.get(iff);
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
			af.addScanIndexMap(a, new File(iff.getAbsolutePath()).toURI(),
			        fragmentToScanIndexMap.get(iff), false);
		}
		File out = new File(getIWorkflow().getOutputDirectory(this),
		        "peakCliqueAssignment.maltcmsAlignment.xml");
		af.save(a, out);
		DefaultWorkflowResult dwr = new DefaultWorkflowResult(out, this,
		        WorkflowSlot.ALIGNMENT, tuple.toArray(new IFileFragment[] {}));
		getIWorkflow().append(dwr);
	}

	private void saveToLangeTautenhahnFormat(
	        final HashMap<String, Integer> columnMap, final List<List<Peak>> ll) {
		// filename intensity rt m/z
		// final List<List<String>> rows = new
		// ArrayList<List<String>>(ll.size());
		File outputFile = new File(getIWorkflow().getOutputDirectory(this)
		        .getAbsolutePath(), "peakCliqueAssignment_matched_features.csv");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			final DecimalFormat df = (DecimalFormat) NumberFormat
			        .getInstance(Locale.US);
			df.applyPattern("0.000");
			for (final List<Peak> l : ll) {

				// check peak group for common masses within bounds
				// round mz to second digit after comma -> mass binning
				// for each peak, map its mz values to intensity
				// store Peak and original mz with mass binned mz as key
				final TreeMap<Double, LinkedHashMap<Peak, double[]>> keyToPeakMz = new TreeMap<Double, LinkedHashMap<Peak, double[]>>();
				for (final Peak p : l) {
					final IFileFragment iff = p.getAssociation();
					final IVariableFragment sindex = iff
					        .getChild(this.scanIndexVariableName);
					final IVariableFragment masses = iff
					        .getChild(this.massValuesVariableName);
					masses.setIndex(sindex);
					final IVariableFragment intensities = iff
					        .getChild(this.intensityValuesVariableName);
					intensities.setIndex(sindex);
					EvalTools.notNull(iff, this);

					Array massA = masses.getArray();
					IndexIterator iter = massA.getIndexIterator();
					Array intensA = intensities.getArray();
					IndexIterator iiter = intensA.getIndexIterator();
					// double mz = MaltcmsTools.getMaxMass(massA, intensA);
					// double intens =
					// MaltcmsTools.getMaxMassIntensity(intensA);
					// map mzKey to peak to mz
					LinkedHashMap<Peak, double[]> peakToMz;
					while (iter.hasNext() && iiter.hasNext()) {
						double mz = iter.getDoubleNext();
						double intens = iiter.getDoubleNext();
						double rmz = mz * 100.0;
						rmz = Math.round(rmz);
						rmz = rmz / 100.0;

						if (keyToPeakMz.containsKey(rmz)) {
							peakToMz = keyToPeakMz.get(rmz);
						} else {
							peakToMz = new LinkedHashMap<Peak, double[]>();
							keyToPeakMz.put(rmz, peakToMz);
						}
						peakToMz.put(p, new double[] { mz, intens });
					}
				}
				int minNumberOfCommonFeatures = this.minCliqueSize == -1 ? columnMap
				        .size()
				        : this.minCliqueSize;
				// check bins and remove features, which do not occur often
				// enough
				HashSet<Double> keysToRemove = new HashSet<Double>();
				StandardDeviation sd = new StandardDeviation();
				for (Double d : keyToPeakMz.keySet()) {
					LinkedHashMap<Peak, double[]> lhm = keyToPeakMz.get(d);
					if (lhm.keySet().size() >= minNumberOfCommonFeatures) {
						int validFeatures = 0;
						double[] intensities = new double[lhm.keySet().size()];
						int i = 0;
						for (Peak p : lhm.keySet()) {
							intensities[i++] = lhm.get(p)[1];
						}

						double median = MathTools.median(intensities);
						double stddev = sd.evaluate(intensities);
						log.info("Median: {}, stddev: {}", median, stddev);
						for (int j = 0; j < intensities.length; j++) {
							double v = Math.pow(intensities[j] - median, 2.0);
							// if (v < 1.0 * stddev) {
							validFeatures++;
							// }
						}
						if (validFeatures < minNumberOfCommonFeatures) {
							keysToRemove.add(d);
							log
							        .info(
							                "Skipping mass {}, not enough well features within bounds: {},{}!",
							                new Object[] {
							                        d,
							                        median
							                                - this.intensityStdDeviationFactor
							                                * stddev,
							                        median
							                                + this.intensityStdDeviationFactor });
						}

					} else {// remove if not enough features
						keysToRemove.add(d);
						log.info("Skipping mass {}, not enough features!", d);
					}
				}
				for (Double d : keysToRemove) {
					keyToPeakMz.remove(d);
				}
				this.log.info("Found {} common features!", keyToPeakMz.keySet()
				        .size());

				// build feature line for each mz group

				// line[0] = l.get(0).getAssociation().getName();
				this.log.debug("Adding {} peaks: {}", l.size(), l);

				for (double d : keyToPeakMz.keySet()) {
					LinkedHashMap<Peak, double[]> peakToMz = keyToPeakMz.get(d);

					final String[] line = new String[columnMap.size() * 3];
					for (Peak p : peakToMz.keySet()) {
						IFileFragment iff = p.getAssociation();
						final int pos = columnMap.get(iff.getName()).intValue() * 3;
						this.log.debug("Insert position for {}: {}", iff
						        .getName(), pos);
						if (pos >= 0) {
							double[] tpl = peakToMz.get(p);
							double mz = tpl[0];
							double intensity = tpl[1];
							line[pos] = intensity + "";
							line[pos + 1] = p.getScanAcquisitionTime() + "";
							line[pos + 2] = mz + "";
							this.log.debug("SAT: {}", line[pos + 2]);
						}
					}
					// final List<String> v = Arrays.asList(line);
					StringBuilder sb = new StringBuilder();
					for (String s : line) {
						sb.append(s + "\t");
					}
					sb.append("\n");
					bw.write(sb.toString());

					// this.log.debug("Adding row {}", v);
				}
				// for (final Peak p : l) {
				// final IFileFragment iff = p.getAssociation();
				// final IVariableFragment sindex = iff
				// .getChild(this.scanIndexVariableName);
				// final IVariableFragment masses = iff
				// .getChild(this.massValuesVariableName);
				// masses.setIndex(sindex);
				// final IVariableFragment intensities = iff
				// .getChild(this.intensityValuesVariableName);
				// intensities.setIndex(sindex);
				// EvalTools.notNull(iff, this);
				// final int pos = columnMap.get(iff.getName()).intValue() * 3;
				// this.log.debug("Insert position for {}: {}", iff.getName(),
				// pos);
				// if (pos >= 0) {
				// // line[pos] = iff.getName();
				// this.log.debug("Reading scan {}", p.getScanIndex());
				// final Array mza = masses.getIndexedArray().get(
				// p.getScanIndex());
				// final Array intena = intensities.getIndexedArray().get(
				// p.getScanIndex());
				// // List<Tuple2D<Double, Double>> pm = MaltcmsTools
				// // .getPeakingMasses(iff, p.getScanIndex(), p
				// // .getScanIndex() - 1, 3, 1.0d);
				// // this.log.info("Found peaking masses for scan {}:{}",
				// // p
				// // .getScanIndex(), pm);
				// double peakMass = MaltcmsTools.getMaxMass(mza, intena);
				// double peakIntens = MaltcmsTools
				// .getMaxMassIntensity(intena);
				// // double peakMass = pm.get(pm.size() - 1).getSecond();
				// // double peakIntens = pm.get(pm.size() - 1).getFirst();
				// line[pos] = peakIntens + "";
				// line[pos + 1] = p.getScanAcquisitionTime() + "";
				// line[pos + 2] = peakMass + "";
				// this.log.debug("SAT: {}", line[pos + 2]);
				// }
				// }
				// StringBuilder sb = new StringBuilder();
				// for (String s : line) {
				// sb.append(s + " ");
				// }
				// sb.append("\n");
				// bw.write(sb.toString());
			}
			bw.flush();
			bw.close();
			DefaultWorkflowResult dwr = new DefaultWorkflowResult(outputFile,
			        this, WorkflowSlot.ALIGNMENT);
			getIWorkflow().append(dwr);
		} catch (IOException ioe) {
			log.error("{}", ioe.getLocalizedMessage());
		}
	}

	private void visualizePeakSimilarities(
	        final HashMap<String, List<Peak>> hm, final int samples,
	        final String prefix) {
		int npeaks = 0;
		for (final String key : hm.keySet()) {
			npeaks += hm.get(key).size();
		}
		if (npeaks == 0) {
			this.log.warn("No peak similarities to visualize!");
			return;
		}
		final List<String> keys = new ArrayList<String>(hm.keySet());
		Collections.sort(keys);
		boolean minimize = this.costFunctionClass.minimize();
		for (final String keyl : keys) {
			final List<Peak> l = hm.get(keyl);
			for (final String keyr : keys) {
				if (!keyl.equals(keyr)) {
					final List<Peak> r = hm.get(keyr);
					final ArrayDouble.D2 psims = new ArrayDouble.D2(l.size(), r
					        .size());
					int i = 0;
					for (final Peak pl : l) {
						int j = 0;
						for (final Peak pr : r) {
							double sim = pl.getSimilarity(pr);
							if (Double.isNaN(sim)) {
								// this.log.warn("NaN occurred!");
								if (minimize) {
									sim = Double.POSITIVE_INFINITY;
								} else {
									sim = Double.NEGATIVE_INFINITY;
								}
								// throw new IllegalArgumentException();
							}
							this.log.debug("Setting index {}/{},{}/{}",
							        new Object[] { i, l.size() - 1, j,
							                r.size() - 1 });
							psims
							        .set(
							                i,
							                j,
							                sim == Double.NEGATIVE_INFINITY ? 0
							                        : sim == Double.POSITIVE_INFINITY ? Double.MAX_VALUE
							                                : sim);
							j++;
						}
						i++;
					}

					MinMax mm = MAMath.getMinMax(psims);

					MinMaxNormalizationFilter mmnf = new MinMaxNormalizationFilter(
					        mm.min, mm.max);
					ArrayDouble.D2 img = (ArrayDouble.D2) mmnf.apply(psims);

					int nsamples = 256;
					double[] sampleTable = ImageTools
					        .createSampleTable(nsamples);
					final ColorRampReader crr = new ColorRampReader();
					final int[][] colorRamp = crr.getDefaultRamp();
					Color[] cRamp = ImageTools.rampToColorArray(colorRamp);
					BufferedImage crampImg = ImageTools.createColorRampImage(
					        sampleTable, Transparency.TRANSLUCENT, cRamp);
					BufferedImage sourceImg = ImageTools.makeImage2D(img,
					        nsamples);
					BufferedImage destImg = ImageTools.applyLut(sourceImg,
					        ImageTools.createLookupTable(crampImg, 1.0f,
					                nsamples));

					// final RenderedImage bi = ImageTools.makeImage2D(psims,
					// samples, Double.NEGATIVE_INFINITY);
					JAI.create("filestore", destImg, new File(getIWorkflow()
					        .getOutputDirectory(this), prefix + "_" + keyl
					        + "-" + keyr + "_peak_similarities.png")
					        .getAbsolutePath(), "PNG");
					final CSVWriter csvw = new CSVWriter();
					csvw.setIWorkflow(getIWorkflow());
					csvw.writeArray2D(getIWorkflow().getOutputDirectory(this)
					        .getAbsolutePath(), prefix + "_" + keyl + "-"
					        + keyr + "_peak_similarities.csv", psims);
				}
			}

		}

	}

}