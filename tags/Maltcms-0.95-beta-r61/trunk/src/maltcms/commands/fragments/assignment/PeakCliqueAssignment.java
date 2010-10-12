/**
 * 
 */
package maltcms.commands.fragments.assignment;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.media.jai.JAI;

import maltcms.commands.distances.ArrayTimePenalizedDot;
import maltcms.commands.distances.IArrayDoubleComp;
import maltcms.commands.fragments.TICPeakFinder;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.MAMath;
import annotations.RequiresVariables;
import cross.Factory;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.io.csv.CSVWriter;
import cross.tools.ArrayTools;
import cross.tools.EvalTools;
import cross.tools.FileTools;
import cross.tools.ImageTools;

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
public class PeakCliqueAssignment extends AFragmentCommand {

	/**
	 * Shorthand class for peaks.
	 * 
	 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
	 * 
	 */
	public class Peak {

		private IFileFragment association = null;
		private int scanIndex = -1;
		private Array msIntensities = null;
		private final int id = -1;
		private double sat = 0;
		private String name = "";
		private final HashMap<IFileFragment, HashMap<Peak, Double>> sims = new HashMap<IFileFragment, HashMap<Peak, Double>>();
		private final HashMap<IFileFragment, List<Peak>> sortedPeaks = new HashMap<IFileFragment, List<Peak>>();

		public Peak(final String name, final IFileFragment file,
		        final int scanIndex, final Array msIntensities,
		        final double scan_acquisition_time) {
			this.name = name;
			// EvalTools.notNull(file, this);
			this.association = file;
			this.scanIndex = scanIndex;
			this.msIntensities = msIntensities;
			this.sat = scan_acquisition_time;
		}

		/**
		 * Add a similarity to Peak p. Resets the sortedPeaks list for the
		 * associated FileFragment of Peak p, so that a subsequent call to
		 * getPeakWithHighestSimilarity or getPeaksSortedBySimilarity will
		 * rebuild the list of peaks sorted ascending according to their
		 * similarity to this peak.
		 * 
		 * @param p
		 * @param similarity
		 */
		public void addSimilarity(final Peak p, final Double similarity) {
			HashMap<Peak, Double> hm = null;
			if (this.sims.containsKey(p.getAssociation())) {
				hm = this.sims.get(p.getAssociation());
				hm.put(p, similarity);
			} else {
				hm = new HashMap<Peak, Double>();
				hm.put(p, similarity);
			}
			this.sims.put(p.getAssociation(), hm);
			if (this.sortedPeaks.containsKey(p.getAssociation())) {
				this.sortedPeaks.remove(p.getAssociation());
			}
		}

		public IFileFragment getAssociation() {
			return this.association;
		}

		public Array getMSIntensities() {
			return this.msIntensities;
		}

		public String getName() {
			return this.name;
		}

		/**
		 * Only call this method, after having added all similarities!
		 * 
		 * @param iff
		 * @return
		 */
		public List<Peak> getPeaksSortedBySimilarity(final IFileFragment iff) {
			if (this.sims.containsKey(iff)) {
				List<Peak> peaks = null;
				if (this.sortedPeaks.containsKey(iff)) {
					peaks = this.sortedPeaks.get(iff);
				} else {
					final Set<Entry<Peak, Double>> s = this.sims.get(iff)
					        .entrySet();
					final ArrayList<Entry<Peak, Double>> al = new ArrayList<Entry<Peak, Double>>();
					for (final Entry<Peak, Double> e : s) {
						if (!e.getKey().getAssociation().getName().equals(
						        getAssociation().getName())) {
							al.add(e);
						}
					}

					// al.addAll(s);
					Collections.sort(al, new Comparator<Entry<Peak, Double>>() {

						@Override
						public int compare(final Entry<Peak, Double> o1,
						        final Entry<Peak, Double> o2) {
							if (o1.getValue() == o2.getValue()) {
								return 0;
							} else if (o1.getValue() < o2.getValue()) {
								return -1;
							} else {
								return 1;
							}
						}
					});
					peaks = new ArrayList<Peak>(al.size());
					for (final Entry<Peak, Double> e : al) {
						peaks.add(e.getKey());
					}
					this.sortedPeaks.put(iff, peaks);
				}
				return peaks;
			}
			return java.util.Collections.emptyList();
		}

		public Peak getPeakWithHighestSimilarity(final IFileFragment iff) {
			final List<Peak> l = getPeaksSortedBySimilarity(iff);
			if (l.isEmpty()) {
				return null;
			}
			return l.get(l.size() - 1);
		}

		public double getScanAcquisitionTime() {
			return this.sat;
		}

		public int getScanIndex() {
			return this.scanIndex;
		}

		public Double getSimilarity(final Peak p) {
			if (this.sims.containsKey(p.getAssociation())) {
				if (this.sims.get(p.getAssociation()).containsKey(p)) {
					return this.sims.get(p.getAssociation()).get(p);
				}
			}
			return Double.NEGATIVE_INFINITY;
		}

		public boolean isBidiBestHitFor(final Peak p) {
			final Peak pT = getPeakWithHighestSimilarity(p.getAssociation());
			final Peak qT = p.getPeakWithHighestSimilarity(this
			        .getAssociation());
			if (qT == null) {
				return false;
			}
			if (qT.equals(this) && pT.equals(p)) {
				return true;
			}
			return false;
		}

		public void removeSimilarity(final Peak p) {
			if (this.sims.containsKey(p.getAssociation())) {
				final HashMap<Peak, Double> hm = this.sims.get(p
				        .getAssociation());
				if (hm.containsKey(p)) {
					Logging.getLogger(PeakCliqueAssignment.class).debug(
					        "Removing similarity to {} in {}", p, this);
					hm.remove(p);
					this.sims.put(p.getAssociation(), hm);
				}
				if (this.sortedPeaks.containsKey(p.getAssociation())) {
					this.sortedPeaks.get(p.getAssociation()).remove(p);
				}
			}
		}

		public void clearSimilarities() {
			this.sims.clear();
			this.sortedPeaks.clear();
		}

		public void retainSimilarityRemoveRest(final Peak p) {
			if (this.sims.containsKey(p.getAssociation())) {
				final HashMap<Peak, Double> hm = this.sims.get(p
				        .getAssociation());
				if (hm.containsKey(p)) {
					log.debug("Retaining similarity to {} in {}", p, this);
					final Double lhsToRhs = hm.get(p);
					// Double rhsToLhs = p.getSimilarity(this);
					hm.clear();
					hm.put(p, lhsToRhs);
					this.sims.put(p.getAssociation(), hm);
					final ArrayList<Peak> al = new ArrayList<Peak>();
					al.add(p);
					this.sortedPeaks.put(p.getAssociation(), al);
				}
			}
		}

		public void setName(final String s) {
			this.name = s;
		}

		public void setScanAcquisitionTime(final double sat) {
			this.sat = sat;
		}

		public void setScanIndex(final int si) {
			this.scanIndex = si;
		}

		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("Peak at position " + this.scanIndex + " in file "
			        + this.association.getName());
			return sb.toString();
		}

	}

	class PeakComparator implements Comparator<Peak> {

		@Override
		public int compare(final Peak o1, final Peak o2) {
			if (o1.scanIndex == o2.scanIndex) {
				return 0;
			} else if (o1.scanIndex < o2.scanIndex) {
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

	public double getSimilarity(final Peak a, final Peak b) {
		//
		// final double sdev = 100.0d;
		// final double satD = a.getScanAcquisitionTime()
		// - b.getScanAcquisitionTime();
		// final double d = PeakCliqueAssignment.sim.apply(-1, -1, a
		// .getScanAcquisitionTime(), b.getScanAcquisitionTime(), a
		// .getMSIntensities(), b.getMSIntensities());
		// final double penalty = Math.exp(-((satD) * (satD) / 2 * sdev *
		// sdev));
		// // / 20.0 * Math.exp(satD * satD);
		// // if(d>0)Logging.getLogger(TICPeakFinder.class).info(
		// // "Similarity of {} and {} = {}", new Object[]{a,b,d});
		// return d * penalty;
		// // return d;
		return this.costFunction.apply(-1, -1, a.getScanAcquisitionTime(), b
		        .getScanAcquisitionTime(), a.getMSIntensities(), b
		        .getMSIntensities());
	}

	private IArrayDoubleComp costFunction = new ArrayTimePenalizedDot();

	private String ticPeaksVariableName = "tic_peaks";

	private String massValuesVariableName = "mass_values";

	private String scanIndexVariableName = "scan_index";

	private String intensityValuesVariableName = "intensity_values";

	private boolean useUserSuppliedAnchors = false;

	public boolean isBidiBestHitForAll(final List<Peak> peaks,
	        final int numberOfFiles) {
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

		if ((i == j) && (i == (numberOfFiles * numberOfFiles) - numberOfFiles)) {
			Logging.getLogger(TICPeakFinder.class).debug(
			        "All elements are BidiBestHits of each other: {}", peaks);
			return true;
		}
		return false;
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

		if (minCliqueSize < 2 && minCliqueSize >= -1) {
			Logging
			        .getLogger(PeakCliqueAssignment.class)
			        .info(
			                "Illegal value for minCliqueSize = {}, allowed values are -1, >=2 <= number of chromatograms",
			                minCliqueSize);
		}
		if (i >= minCliqueSize) {
			Logging.getLogger(PeakCliqueAssignment.class).info(
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

	/**
	 * FIXME this method seems to create correct scan indices only for the first
	 * two files.
	 * 
	 * @param al
	 * @param newFragments
	 * @return
	 */
	private TupleND<IFileFragment> addAnchors(final List<List<Peak>> al,
	        final TupleND<IFileFragment> newFragments) {

		final String ri_names = this.anchorNamesVariableName;
		final String ri_times = this.anchorTimesVariableName;
		final String ri_indices = this.anchorRetentionIndexVariableName;
		final String ri_scans = this.anchorScanIndexVariableName;
		// Create Variables and arrays
		HashMap<String, IFileFragment> hm = new HashMap<String, IFileFragment>();
		for (final IFileFragment ff : newFragments) {
			this.log.debug("Source files of fragment {}: {}", ff
			        .getAbsolutePath(), ff.getSourceFiles());
			hm.put(ff.getName(), ff);
			final int size = al.size();
			final ArrayInt.D1 anchors = new ArrayInt.D1(size);
			ArrayTools.fillArray(anchors, Integer.valueOf(-1));
			final IVariableFragment ri = ff.hasChild(ri_scans) ? ff
			        .getChild(ri_scans) : new VariableFragment(ff, ri_scans);
			final ArrayDouble.D1 anchorTimes = new ArrayDouble.D1(size);
			ArrayTools.fillArray(anchorTimes, Double.valueOf(-1));
			final IVariableFragment riTimes = ff.hasChild(ri_times) ? ff
			        .getChild(ri_times) : new VariableFragment(ff, ri_times);
			final ArrayChar.D2 names = ArrayTools.createStringArray(size, 256);
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
		for (final List<Peak> l : al) {
			// Matched Peaks
			for (final Peak p : l) {
				final IFileFragment association = hm.get(p.getAssociation()
				        .getName());
				this.log
				        .debug(
				                "Adding anchor at scan index {} and rt {} with id {} to file {}",
				                new Object[] { p.getScanIndex(),
				                        p.getScanAcquisitionTime(), id,
				                        association.getName() });
				String name = "A" + id;
				if (!(p.getName().equals(""))) {
					name = name + "_" + p.getName();

				}
				// this.log.info("Adding anchor with name {}", name);
				((ArrayChar.D2) association.getChild(ri_names).getArray())
				        .setString(id, name);
				((ArrayInt.D1) association.getChild(ri_scans).getArray()).set(
				        id, p.getScanIndex());
				((ArrayDouble.D1) association.getChild(ri_times).getArray())
				        .set(id, p.getScanAcquisitionTime());
			}
			id++;
		}
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
			        new File(ff.getAbsolutePath()), this, getWorkflowSlot());
			getIWorkflow().append(dwr);
			ff.save();
		}
		return newFragments;
	}

	/**
	 * @param al
	 * @param fragmentToPeaks
	 */
	private void calculatePeakSimilarities(final TupleND<IFileFragment> al,
	        final HashMap<String, List<Peak>> fragmentToPeaks) {
		// Loop over all pairs of FileFragments
		for (final Tuple2D<IFileFragment, IFileFragment> t : al.getPairs()) {

			// calculate similarity between peaks
			final List<Peak> lhsPeaks = fragmentToPeaks.get(t.getFirst()
			        .getName());
			final List<Peak> rhsPeaks = fragmentToPeaks.get(t.getSecond()
			        .getName());
			for (final Peak p1 : lhsPeaks) {
				for (final Peak p2 : rhsPeaks) {
					final Double d = getSimilarity(p1, p2);
					p1.addSimilarity(p2, d);
					p2.addSimilarity(p1, d);
				}
			}
		}

		visualizePeakSimilarities(fragmentToPeaks, 256, "beforeBIDI");
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
				List<Array> bintens = t.getChild(
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
	 */
	private void combineBiDiBestHits(final TupleND<IFileFragment> al,
	        final HashMap<String, List<Peak>> fragmentToPeaks,
	        final List<List<Peak>> ll, final int minCliqueSize) {
		// for(IFileFragment iff:al) {
		final IFileFragment iff = al.get(0);
		final List<Peak> peaks = fragmentToPeaks.get(iff.getName());
		for (final Peak p : peaks) {
			final List<Peak> bidiHits = new ArrayList<Peak>();
			bidiHits.add(p);
			for (final IFileFragment jff : al) {
				if (!iff.equals(jff)) {
					final List<Peak> l = p.getPeaksSortedBySimilarity(jff);
					if (l.size() == 1) {
						bidiHits.add(l.get(0));
					} else if (l.size() > 1) {
						this.log
						        .debug("Similarity list for peak and FileFragment should only contain at most one element!");
					}
				}
			}
			if (isBidiBestHitForK(bidiHits, al.size(), minCliqueSize)) {
				ll.add(bidiHits);
			}
		}
		this.log.info("Found {} bidirectional best hits ", ll.size());
		// }
	}

	/**
	 * @param al
	 * @param fragmentToPeaks
	 * @param ll
	 */
	private void combineBiDiBestHitsAll(final TupleND<IFileFragment> al,
	        final HashMap<String, List<Peak>> fragmentToPeaks,
	        final List<List<Peak>> ll) {
		// for(IFileFragment iff:al) {
		final IFileFragment iff = al.get(0);
		final List<Peak> peaks = fragmentToPeaks.get(iff.getName());
		for (final Peak p : peaks) {
			final List<Peak> bidiHits = new ArrayList<Peak>();
			bidiHits.add(p);
			for (final IFileFragment jff : al) {
				if (!iff.equals(jff)) {
					final List<Peak> l = p.getPeaksSortedBySimilarity(jff);
					if (l.size() == 1) {
						bidiHits.add(l.get(0));
					} else if (l.size() > 1) {
						this.log
						        .debug("Similarity list for peak and FileFragment should only contain at most one element!");
					}
				}
			}

			if (isBidiBestHitForAll(bidiHits, al.size())) {
				ll.add(bidiHits);
			}
		}
		this.log.info("Found {} peaks common to all files", ll.size());
		// }
	}

	/**
	 * @param al
	 * @param fragmentToPeaks
	 */
	private void findBiDiBestHits(final TupleND<IFileFragment> al,
	        final HashMap<String, List<Peak>> fragmentToPeaks) {
		// For each pair of FileFragments
		Set<Peak> unmatchedPeaks = new HashSet<Peak>();
		Set<Peak> matchedPeaks = new HashSet<Peak>();
		for (final Tuple2D<IFileFragment, IFileFragment> t : al.getPairs()) {

			final List<Peak> lhsPeaks = fragmentToPeaks.get(t.getFirst()
			        .getName());
			final List<Peak> rhsPeaks = fragmentToPeaks.get(t.getSecond()
			        .getName());
			this.log.debug("lhsPeaks: {}", lhsPeaks.size());
			this.log.debug("rhsPeaks: {}", rhsPeaks.size());
			for (final Peak plhs : lhsPeaks) {
				for (final Peak prhs : rhsPeaks) {
					// two way matching
					this.log.debug("Checking peaks {} and {}", plhs, prhs);
					final Peak prhsC = plhs.getPeakWithHighestSimilarity(t
					        .getSecond());
					final Peak plhsC = prhs.getPeakWithHighestSimilarity(t
					        .getFirst());
					if (prhsC.equals(prhs) && plhsC.equals(plhs)) {
						this.log.debug(
						        "Found a bidirectional best hit: {} and {}",
						        plhs, prhs);
						matchedPeaks.add(plhs);
						matchedPeaks.add(prhs);
						prhs.retainSimilarityRemoveRest(plhs);
						plhs.retainSimilarityRemoveRest(prhs);
					}
					// add peaks to unmatched by default
					unmatchedPeaks.add(plhs);
					unmatchedPeaks.add(prhs);
				}
			}
		}

		this.log.info("{} peaks were matched!", matchedPeaks.size());
		this.log.debug("Clearing similarities of unmatched ones!");
		for (final Tuple2D<IFileFragment, IFileFragment> t : al.getPairs()) {

			final List<Peak> lhsPeaks = fragmentToPeaks.get(t.getFirst()
			        .getName());
			final List<Peak> rhsPeaks = fragmentToPeaks.get(t.getSecond()
			        .getName());
			this.log.debug("lhsPeaks: {}", lhsPeaks.size());
			this.log.debug("rhsPeaks: {}", rhsPeaks.size());
			for (final Peak plhs : lhsPeaks) {
				for (final Peak prhs : rhsPeaks) {
					if (!matchedPeaks.contains(plhs)) {
						plhs.clearSimilarities();
					}
					if (!matchedPeaks.contains(prhs)) {
						prhs.clearSimilarities();
					}
				}
			}
		}
	}

	private TupleND<IFileFragment> identifyPeaks(
	        final TupleND<IFileFragment> originalFragments) {
		this.log.debug("Matching peaks");
		final HashMap<String, List<Peak>> fragmentToPeaks = new HashMap<String, List<Peak>>();
		final HashMap<String, Integer> columnMap = new HashMap<String, Integer>(
		        originalFragments.size());

		ArrayList<IFileFragment> al2 = new ArrayList<IFileFragment>();
		for (IFileFragment iff : originalFragments) {
			IFileFragment iff2 = FileFragmentFactory.getInstance().create(
			        FileTools.prependDefaultDirs(iff.getName(),
			                this.getClass(), getIWorkflow().getStartupDate()));
			iff2.addSourceFile(iff);
			log.debug("Created work file {}", iff2);
			al2.add(iff2);
		}
		TupleND<IFileFragment> t = new TupleND<IFileFragment>(al2);
		initializePeaks(t, fragmentToPeaks, columnMap);
		final List<List<Peak>> ll = new ArrayList<List<Peak>>();
		this.log.info("Calculating all-against-all peak similarities");
		calculatePeakSimilarities(t, fragmentToPeaks);

		this.log.info("Searching for bidirectional best hits");
		final long startT = System.currentTimeMillis();
		findBiDiBestHits(t, fragmentToPeaks);
		this.log.info("Found bidi best hits in {} milliseconds", System
		        .currentTimeMillis()
		        - startT);

		final long startT2 = System.currentTimeMillis();
		if (this.keepOnlyBiDiBestHitsForAll || this.minCliqueSize == -1) {
			this.log
			        .info("Combining bidirectional best hits if present in all files");
			combineBiDiBestHitsAll(t, fragmentToPeaks, ll);
		} else {
			this.log.info("Combining bidirectional best hits");
			combineBiDiBestHits(t, fragmentToPeaks, ll, this.minCliqueSize);
		}
		removePeakSimilaritiesWhichHaveNoBestHits(t, fragmentToPeaks);
		this.log.info("Reduced bidi best hits in {} milliseconds", System
		        .currentTimeMillis()
		        - startT2);
		visualizePeakSimilarities(fragmentToPeaks, 256, "afterBIDI");
		saveToLangeTautenhahnFormat(columnMap, ll);
		savePeakMatchTable(columnMap, ll);
		savePeakMatchRTTable(columnMap, ll);
		this.log.debug("Adding anchor variables");
		TupleND<IFileFragment> ret = addAnchors(ll, t);
		return ret;
	}

	private void saveToLangeTautenhahnFormat(
	        HashMap<String, Integer> columnMap, List<List<Peak>> ll) {
		// filename intensity rt m/z
		final Vector<Vector<String>> rows = new Vector<Vector<String>>(ll
		        .size());
		final DecimalFormat df = (DecimalFormat) NumberFormat
		        .getInstance(Locale.US);
		df.applyPattern("0.000");
		for (final List<Peak> l : ll) {
			final String[] line = new String[columnMap.size() * 3];
			// line[0] = l.get(0).getAssociation().getName();
			this.log.debug("Adding {} peaks: {}", l.size(), l);
			for (final Peak p : l) {
				final IFileFragment iff = p.getAssociation();
				final IVariableFragment sindex = iff.getChild("scan_index");
				final IVariableFragment masses = iff.getChild("mass_values");
				masses.setIndex(sindex);
				final IVariableFragment intensities = iff
				        .getChild("intensity_values");
				intensities.setIndex(sindex);
				EvalTools.notNull(iff, this);
				final int pos = columnMap.get(iff.getName()).intValue() * 3;
				this.log.debug("Insert position for {}: {}", iff.getName(),
				        pos);
				if (pos >= 0) {
					//line[pos] = iff.getName();
					this.log.debug("Reading scan {}", p.getScanIndex());
					Array mza = masses.getIndexedArray().get(p.getScanIndex());
					Array intena = intensities.getIndexedArray().get(
					        p.getScanIndex());
					line[pos] = getMaxMassIntensity(intena)+"";
					line[pos+1] = p.getScanAcquisitionTime()+"";
					line[pos+2] = getMaxMass(mza, intena)+"";
					this.log.debug("SAT: {}", line[pos + 2]);
				}
			}
			final Vector<String> v = new Vector<String>(Arrays.asList(line));
			rows.add(v);
			this.log.debug("Adding row {}", v);
		}

		final CSVWriter csvw = new CSVWriter();
		csvw.setIWorkflow(getIWorkflow());
		csvw.writeTableByRows(FileTools.prependDefaultDirs(this.getClass(),
		        getIWorkflow().getStartupDate()).getAbsolutePath(),
		        "chroma_matched_features.csv", rows, WorkflowSlot.ALIGNMENT);
	}

	/**
	 * @param epsilon
	 * @return
	 */
	private double getMaxMassIntensity(final Array intens) {
		final int[] ranksByIntensity = ranksByIntensity(intens);
		final Index idx = intens.getIndex();
		final double maxIntens = intens.getDouble(idx.set(ranksByIntensity[0]));
		return maxIntens;
	}

	/**
	 * @param masses
	 * @param intens
	 * @return
	 */
	private double getMaxMass(final Array masses, final Array intens) {
		final int[] ranksByIntensity = ranksByIntensity(intens);
		final Index idx = masses.getIndex();
		final double maxMass = masses.getDouble(idx.set(ranksByIntensity[0]));
		log.debug("Max mass {} at index {}", maxMass, ranksByIntensity[0]);
		double maxIntens = MAMath.getMaximum(intens);
		log.debug("Max intens {}={}", maxIntens, intens.getDouble(intens
		        .getIndex().set(ranksByIntensity[0])));
		EvalTools.eqD(maxIntens, intens.getDouble(intens.getIndex().set(
		        ranksByIntensity[0])), this);
		// return new Tuple2D<List<Integer>, List<Double>>(Arrays.asList(Integer
		// .valueOf(maxMassIdx)), Arrays.asList(Double.valueOf(maxMass)));
		return maxMass;
	}

	/**
	 * Expects intensities to be sorted in ascending order of masses. Returns
	 * intensities sorted ascending by intensity, so the index of the mass
	 * channel with highest intensity is at intensities.getShape()[0]-1.
	 * 
	 * @param intensities
	 * @return
	 */
	private int[] ranksByIntensity(Array intensities) {
		Index mint = intensities.getIndex();
		List<Tuple2D<Integer, Double>> l = new ArrayList<Tuple2D<Integer, Double>>(
		        intensities.getShape()[0]);
		int[] ranks = new int[intensities.getShape()[0]];
		// identity
		for (int i = 0; i < ranks.length; i++) {
			l.add(new Tuple2D<Integer, Double>(i, Double.valueOf(intensities
			        .getDouble(mint.set(i)))));
			ranks[i] = i;
		}
		// reverse comparator
		Collections.sort(l, Collections
		        .reverseOrder(new Comparator<Tuple2D<Integer, Double>>() {

			        @Override
			        public int compare(Tuple2D<Integer, Double> o1,
			                Tuple2D<Integer, Double> o2) {
				        if (o1.getSecond() < o2.getSecond()) {
					        return -1;
				        } else if (o1.getSecond() > o2.getSecond()) {
					        return 1;
				        }
				        return 0;
			        }
		        }));
		for (int i = 0; i < ranks.length; i++) {
			ranks[i] = l.get(i).getFirst();
		}
		return ranks;
	}

	private void removePeakSimilaritiesWhichHaveNoBestHits(
	        TupleND<IFileFragment> t,
	        HashMap<String, List<Peak>> fragmentToPeaks) {
		// no best hits means, that the corresponding list of sorted peaks has
		// length one
		for (String s : fragmentToPeaks.keySet()) {
			for (Peak p : fragmentToPeaks.get(s)) {
				for (IFileFragment iff : t) {
					List<Peak> l = p.getPeaksSortedBySimilarity(iff);
					// clear similarities, if a best hit hasn't been assigned
					if (l.size() > 1) {
						p.clearSimilarities();
					}
				}
			}
		}
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
			final Array peakCandidates1 = t.getChild(this.ticPeaksVariableName)
			        .getArray();
			this.log.debug("Peaks for file {}: {}", t.getAbsolutePath(),
			        peakCandidates1);
			// maxPeaks = Math.max(maxPeaks, peakCandidates1.getShape()[0]);
			t.getChild(this.binnedIntensitiesVariableName).setIndex(
			        t.getChild(this.binnedScanIndexVariableName));
			List<Array> bintens = t
			        .getChild(this.binnedIntensitiesVariableName)
			        .getIndexedArray();
			final Array scan_acquisition_time = t.getChild(
			        this.scanAcquisitionTimeVariableName).getArray();
			EvalTools.notNull(peakCandidates1, this);
			List<Peak> peaks = null;
			if (fragmentToPeaks.containsKey(t.getName())) {
				peaks = fragmentToPeaks.get(t.getName());
			} else {
				peaks = new ArrayList<Peak>();
			}
			log.info("Adding peaks for {}", t.getName());
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

			if (userDefinedAnchors != null && this.useUserSuppliedAnchors) {
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

			// Collections.sort(peaks,new PeakComparator());
			fragmentToPeaks.put(t.getName(), peaks);
			columnMap.put(t.getName(), column++);
			// clearing space
			t.clearArrays();
		}
		this.log.debug("{} peaks present", npeaks);
	}

	/**
	 * @param columnMap
	 * @param ll
	 */
	private void savePeakMatchTable(final HashMap<String, Integer> columnMap,
	        final List<List<Peak>> ll) {
		final Vector<Vector<String>> rows = new Vector<Vector<String>>(ll
		        .size());
		Vector<String> headers = null;
		final String[] headerLine = new String[columnMap.size()];
		for (int i = 0; i < headerLine.length; i++) {
			headerLine[i] = "";
		}
		headers = new Vector<String>(Arrays.asList(headerLine));
		for (final String s : columnMap.keySet()) {
			headers.set(columnMap.get(s), s);
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
			final Vector<String> v = new Vector<String>(Arrays.asList(line));
			rows.add(v);
			this.log.debug("Adding row {}", v);
		}

		final CSVWriter csvw = new CSVWriter();
		csvw.setIWorkflow(getIWorkflow());
		csvw.writeTableByRows(FileTools.prependDefaultDirs(this.getClass(),
		        getIWorkflow().getStartupDate()).getAbsolutePath(),
		        "matched_peaks.csv", rows, WorkflowSlot.ALIGNMENT);
	}

	/**
	 * @param columnMap
	 * @param ll
	 */
	private void savePeakMatchRTTable(final HashMap<String, Integer> columnMap,
	        final List<List<Peak>> ll) {
		final Vector<Vector<String>> rows = new Vector<Vector<String>>(ll
		        .size());
		Vector<String> headers = null;
		final String[] headerLine = new String[columnMap.size()];
		for (int i = 0; i < headerLine.length; i++) {
			headerLine[i] = "";
		}
		headers = new Vector<String>(Arrays.asList(headerLine));
		for (final String s : columnMap.keySet()) {
			headers.set(columnMap.get(s), s);
		}
		this.log.debug("Adding row {}", headers);
		rows.add(headers);
		for (final List<Peak> l : ll) {
			final DecimalFormat df = (DecimalFormat) NumberFormat
			        .getInstance(Locale.US);
			df.applyPattern("0.000");
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
						line[pos] = df
						        .format(p.getScanAcquisitionTime() / 60.0d)
						        + "";
					} else {
						this.log.warn("Array position {} already used!", pos);
					}
				}
			}
			final Vector<String> v = new Vector<String>(Arrays.asList(line));
			rows.add(v);
			this.log.debug("Adding row {}", v);
		}

		final CSVWriter csvw = new CSVWriter();
		csvw.setIWorkflow(getIWorkflow());
		csvw.writeTableByRows(FileTools.prependDefaultDirs(this.getClass(),
		        getIWorkflow().getStartupDate()).getAbsolutePath(),
		        "matched_peaksRT.csv", rows, WorkflowSlot.ALIGNMENT);
	}

	private void visualizePeakSimilarities(
	        final HashMap<String, List<Peak>> hm, final int samples,
	        String prefix) {
		int npeaks = 0;
		for (final String key : hm.keySet()) {
			npeaks += hm.get(key).size();
		}
		if (npeaks == 0) {
			this.log.warn("No peak similarities to visualize!");
			return;
		}
		final ArrayDouble.D2 sims = new ArrayDouble.D2(npeaks, npeaks);
		final List<String> keys = new ArrayList<String>(hm.keySet());
		ArrayList<Integer> peaksPerFile = new ArrayList<Integer>();
		Collections.sort(keys);
		final List<Peak> allPeaks = new ArrayList<Peak>();
		for (final String key : keys) {
			final List<Peak> l = hm.get(key);
			peaksPerFile.add(l.size());
			allPeaks.addAll(l);
		}

		for (int i = 0; i < allPeaks.size(); i++) {
			for (int j = 0; j < allPeaks.size(); j++) {
				final double sim = allPeaks.get(i).getSimilarity(
				        allPeaks.get(j));
				if (Double.isNaN(sim)) {
					this.log.warn("NaN occurred!");
					throw new IllegalArgumentException();
				}
				this.log.debug("Sim {},{}: {}", new Object[] { i, j, sim });
				sims.set(i, j, sim == Double.NEGATIVE_INFINITY ? 0
				        : sim == Double.POSITIVE_INFINITY ? Double.MAX_VALUE
				                : sim);
				// sims.set(j, i, sim);
			}
		}

		final CSVWriter csvw = new CSVWriter();
		csvw.setIWorkflow(getIWorkflow());
		csvw.writeArray2D(FileTools.prependDefaultDirs(this.getClass(),
		        getIWorkflow().getStartupDate()).getAbsolutePath(), prefix
		        + "_peak_similarities.csv", sims);

		final BufferedImage bi = ImageTools.drawSquareMatrixWithLabels(
		        peaksPerFile, keys, sims, Double.NEGATIVE_INFINITY);
		// final double[] breakPoints = ImageTools.getBreakpoints(sims, samples,
		// Double.NEGATIVE_INFINITY);

		// SampleModel sampleModel =
		// RasterFactory.createBandedSampleModel(DataBuffer
		// .TYPE_INT,npeaks,npeaks,3);
		// final BufferedImage bi = new BufferedImage(npeaks, npeaks,
		// BufferedImage.TYPE_INT_RGB);
		// final WritableRaster raster = bi.getRaster();
		// final ColorRampReader crr = new ColorRampReader();
		// final int[][] colorRamp = crr.readColorRamp(this.colorRampLocation);
		// ImageTools.makeImage2D(raster, sims, samples, colorRamp, 0.0d,
		// breakPoints);
		JAI.create("filestore", bi, FileTools.prependDefaultDirs(
		        prefix + "_peak_similarities.png", this.getClass(),
		        getIWorkflow().getStartupDate()).getAbsolutePath(), "PNG");
	}

	@Override
	public void configure(Configuration cfg) {
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
		this.keepOnlyBiDiBestHitsForAll = cfg.getBoolean(this.getClass()
		        .getName()
		        + ".keepOnlyBiDiBestHitsForAll", true);
		this.minCliqueSize = cfg.getInt(this.getClass().getName()
		        + ".minCliqueSize", -1);
		this.colorRampLocation = cfg.getString("images.colorramp",
		        "res/colorRamps/bw.csv");
		final String aldist = "maltcms.commands.distances.ArrayLp";
		this.costFunction = Factory.getInstance().instantiate(
		        cfg.getString(this.getClass().getName() + ".costFunction",
		                aldist), IArrayDoubleComp.class);
		this.useUserSuppliedAnchors = cfg.getBoolean(this.getClass().getName()
		        + "useUserSuppliedAnchors", false);
	}

	private final Logger log = Logging.getLogger(this.getClass());

	private int minCliqueSize = -1;

	private String anchorNamesVariableName = "retention_index_names";

	private String anchorTimesVariableName = "retention_times";

	private String anchorRetentionIndexVariableName = "retention_indices";

	private String anchorScanIndexVariableName = "retention_scans";

	private String colorRampLocation = "res/colorRamps/bw.csv";

	private boolean keepOnlyBiDiBestHitsForAll = true;

	private String binnedIntensitiesVariableName = "binned_intensity_values";

	private String binnedScanIndexVariableName = "binned_scan_index";

	private String scanAcquisitionTimeVariableName = "scan_acquisition_time";

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.fragments.AFragmentCommand#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Assigns peak candidates as pairs and tries to group them into cliques of size k";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.PEAKMATCHING;
	}

}
