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

package maltcms.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;

import maltcms.commands.fragments.DenseArrayProducer;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.datastructures.array.Sparse;
import maltcms.datastructures.ms.Chromatogram1D;
import maltcms.datastructures.ms.IAnchor;
import maltcms.datastructures.ms.RetentionInfo;

import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Arrays;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayBoolean;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.Range;
import ucar.ma2.ArrayChar.StringIterator;
import ucar.ma2.MAMath.MinMax;
import cross.Factory;
import cross.Logging;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DDoubleComp;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.tuple.TupleND;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.io.csv.CSVWriter;
import cross.tools.ArrayTools;
import cross.tools.EvalTools;
import cross.tools.FileTools;
import cross.tools.FragmentTools;
import cross.tools.StringTools;

/**
 * Utility class providing many comfort methods, providing more direct access to
 * andims compatible variables. Sort of an abstraction layer.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class MaltcmsTools {

	private enum RoundMode {
		RINT, RFLOORINT, ROPINT;
	}

	public static final Logger log = Logging.getLogger(MaltcmsTools.class);

	private static RoundMode binMZMode = RoundMode.RFLOORINT;

	private static HashMap<String, IFileFragment> pairwiseAlignments = new HashMap<String, IFileFragment>();

	private static HashMap<String, IFileFragment> denseArrays = new HashMap<String, IFileFragment>();

	public static void addPairwiseAlignment(final IFileFragment ref,
	        final IFileFragment query, final IFileFragment pa) {
		final String key1 = MaltcmsTools.getKey(ref, query);
		final String key2 = MaltcmsTools.getKey(query, ref);
		if (MaltcmsTools.hasPairwiseAlignment(ref, query)) {
			MaltcmsTools.log.warn(
			        "PairwiseAlignment of {} and {} already contained in map!",
			        ref.getAbsolutePath(), query.getAbsolutePath());
		}
		MaltcmsTools.pairwiseAlignments.put(key1, pa);
		MaltcmsTools.pairwiseAlignments.put(key2, pa);
	}

	/**
	 * Calculates the normalized index of mz, by subtracting minmz and dividing
	 * by maxmz-minmz.
	 * 
	 * @param mz
	 *            mass value to bin
	 * @param minmz
	 *            minimum mz of all mass values over all chromatograms, can be
	 *            zero
	 * @param maxmz
	 *            maximum mz of all mass values over all chromatograms
	 * @param resolution
	 *            multiplication factor to scale the mass range
	 * @return an integer bin for mz, starting at 0
	 */
	public static int binMZ(final double mz, final double minmz,
	        final double maxmz, final double resolution) {
		final double v = ((mz - minmz) / (maxmz - minmz));
		// Logging.getLogger(MaltcmsTools.class).debug("mz: {}, v: {}",mz,v);
		final double scaledMz = mz * resolution;
		final double scaledMinMz = minmz * resolution;
		// Logging.getLogger(MaltcmsTools.class).debug("smz: {}",scaledMz);
		// v = v*mz;
		// Logging.getLogger(MaltcmsTools.class).info("v*mz: {}",v);
		// v = v*resolution;
		// Logging.getLogger(MaltcmsTools.class).info("v*mz*resolution: {}",v);
		int rval = 0;
		int minMzRval = 0;
		if (MaltcmsTools.binMZMode == RoundMode.RINT) {
			rval = MaltcmsTools.binMZDefault(scaledMz);
			minMzRval = MaltcmsTools.binMZDefault(scaledMinMz);
		} else if (MaltcmsTools.binMZMode == RoundMode.ROPINT) {
			rval = MaltcmsTools.binMZHeiko(scaledMz);
			minMzRval = MaltcmsTools.binMZHeiko(scaledMinMz);
		} else {
			rval = MaltcmsTools.binMZFloor(scaledMz);
			minMzRval = MaltcmsTools.binMZFloor(scaledMinMz);
		}
		final double z = (double) rval - (double) minMzRval;
		// Logging.getLogger(MaltcmsTools.class).debug(
		// "rval: {}, minMzRval: {}, z: {}",new Object[]{rval,minMzRval,z});
		// double y = (((double)rval)*(maxmz-minmz))/mz/resolution;
		// Logging.getLogger(MaltcmsTools.class).info(
		// "mz: {}, minmz: {}, resolution: {}, binnedMZ: {}, roundBinnedMZ: {}, rescaledMZ: {}"
		// ,new Object[]{mz,minmz,resolution,v,rval,y});
		// return Math.max((int)z,0);//(int)y;
		return (int) z;
	}

	/**
	 * Use standard rounding.
	 * 
	 * @param mz
	 * @return
	 */
	public static int binMZDefault(final double mz) {
		return (int) Math.rint(mz);
		// binMZFloor produces a high number of artifacts (wrongly binned
		// intensities),
		// so differences between chromatograms will tend to be higher
		// return binMZFloor(mz);
	}

	public static int binMZFloor(final double mz) {
		return (int) Math.floor(mz);
	}

	/**
	 * Use rounding according to Heiko's distribution analysis.
	 * 
	 * @param mz
	 * @return
	 */
	public static int binMZHeiko(final double mz) {
		final int preComma = (int) (mz);
		final double rest = (mz) - preComma;
		if (rest > 0.7) {
			return preComma + 1;
		}
		return preComma;
	}

	public static IFileFragment buildBinaryMassVectors(
	        final IFileFragment denseArrays, final String binary_mass_values,
	        final String mass_values, final String intensity_values,
	        final String scan_index, final List<Integer> maskedMasses) {
		IVariableFragment bm = null;
		if (denseArrays.hasChild(binary_mass_values)) {
			bm = denseArrays.getChild(binary_mass_values);
		} else {
			bm = new VariableFragment(denseArrays, binary_mass_values);
		}
		final List<Array> intens = denseArrays.getChild(intensity_values)
		        .getIndexedArray();
		final List<Array> masses = denseArrays.getChild(mass_values)
		        .getIndexedArray();
		final IVariableFragment scn_idx = denseArrays.getChild(scan_index);
		final ArrayList<Array> binMasses = new ArrayList<Array>(intens.size());
		for (int i = 0; i < intens.size(); i++) {
			final Array a = intens.get(i);
			final Array m = masses.get(i);
			EvalTools.eqI(1, a.getRank(), MaltcmsTools.class);
			final ArrayBoolean.D1 ab = new ArrayBoolean.D1(a.getShape()[0]);
			final IndexIterator iter = a.getIndexIterator();
			final IndexIterator miter = m.getIndexIterator();
			int j = 0;
			int massMask = 0;
			while (iter.hasNext() && miter.hasNext()) {
				double d = iter.getDoubleNext();
				final double mass = miter.getDoubleNext();
				// set current value to zero
				if ((maskedMasses != null) && (massMask < maskedMasses.size())
				        && (maskedMasses.get(massMask) == (Math.floor(mass)))) {
					d = 0;
					massMask++;
				}
				if (d > 0.0d) {
					ab.set(j, true);
				}
				j++;
			}
			binMasses.add(ab);
		}
		bm.setIndexedArray(binMasses);
		bm.setIndex(scn_idx);
		return denseArrays;
	}

	/**
	 * 
	 * @param arrays
	 * @return a tuple containing the index offsets as first array and the
	 *         flattened masses and intensities within the second tuple
	 */
	public static Tuple2D<Array, Tuple2D<Array, Array>> chrom2crs(
	        final Chromatogram1D chrom) {
		final List<Array> intensities = chrom.getIntensities();
		final List<Array> masses = chrom.getMasses();
		EvalTools.eqI(intensities.size(), masses.size(), chrom);
		final ArrayInt.D1 indices = new ArrayInt.D1(masses.size());
		final int size = ArrayTools.getSizeForFlattenedArrays(masses);
		final ArrayDouble.D1 ms = new ArrayDouble.D1(size);
		final ArrayDouble.D1 is = new ArrayDouble.D1(size);
		int offset = 0;
		int len = 0;
		for (int i = 0; i < masses.size(); i++) {
			indices.set(i, offset);
			final Array ma = masses.get(i);
			final Array ia = intensities.get(i);
			len = ma.getShape()[0];
			Array.arraycopy(ma, 0, ms, offset, len);
			Array.arraycopy(ia, 0, is, offset, len);
			offset += len;
		}
		return new Tuple2D<Array, Tuple2D<Array, Array>>(indices,
		        new Tuple2D<Array, Array>(ms, is));
	}

	public static List<Array> copyEics(final List<Array> intensities,
	        final Integer[] eics) {
		final ArrayList<Array> al = new ArrayList<Array>(intensities.size());
		for (int i = 0; i < intensities.size(); i++) {
			final Array aorig = intensities.get(i);
			final Array a = Array.factory(aorig.getElementType(),
			        new int[] { eics.length });
			final Index ai = a.getIndex();
			final Index aorigi = aorig.getIndex();
			int nindex = 0;
			// First is variance, second is index
			for (final Integer integ : eics) {
				// get index in original array -> EIC - minimum mass
				final int idx = integ.intValue();
				// set nth index of new array to value of
				a.setDouble(ai.set(nindex++), aorig.getDouble(aorigi.set(idx)));
			}
			al.add(a);
		}
		return al;
	}

	public static Tuple2D<VariableFragment, Tuple2D<VariableFragment, VariableFragment>> createFlattenedArrays(
	        final IFileFragment file, final List<Array> masses,
	        final List<Array> intensities) {
		MaltcmsTools.log.debug("Creating flattened arrays");
		EvalTools.notNull(new Object[] { file, masses, intensities },
		        MaltcmsTools.class);
		final VariableFragment new_scan_index = new VariableFragment(file,
		        Factory.getInstance().getConfiguration().getString(
		                "var.binned_scan_index", "binned_scan_index"));// FragmentTools
		// .
		// create
		// (
		// FragmentTools
		// .
		// create
		// (filenameout),this.scan_index,null);
		final VariableFragment new_intensities = new VariableFragment(file,
		        Factory.getInstance().getConfiguration().getString(
		                "var.binned_intensity_values",
		                "binned_intensity_values"));
		final VariableFragment new_mz = new VariableFragment(file, Factory
		        .getInstance().getConfiguration().getString(
		                "var.binned_mass_values", "binned_mass_values"));
		// int var_size = refA.size();

		final ArrayInt.D1 scan_indexa = new ArrayInt.D1(intensities.size());

		int si = 0;
		for (int i = 0; i < intensities.size(); i++) {
			MaltcmsTools.log.debug("scan offset {}", si);
			scan_indexa.set(i, si);
			si += intensities.get(i).getShape()[0];
		}

		new_scan_index.setDataType(DataType.INT);
		new_intensities.setDataType(DataType.DOUBLE);
		new_mz.setDataType(DataType.DOUBLE);

		new_mz.setIndex(new_scan_index);
		new_intensities.setIndex(new_scan_index);

		new_mz.setIndexedArray(masses);
		new_intensities.setIndexedArray(intensities);
		new_scan_index.setArray(scan_indexa);
		return new Tuple2D<VariableFragment, Tuple2D<VariableFragment, VariableFragment>>(
		        new_scan_index,
		        new Tuple2D<VariableFragment, VariableFragment>(new_mz,
		                new_intensities));
	}

	public static IVariableFragment createIntegratedValueArray(
	        final IFileFragment file, final String varname,
	        final List<Array> values) {
		String vname = null;
		if (varname == null) {
			vname = Factory.getInstance().getConfiguration().getString(
			        "var.total_intensity", "total_intensity");
		}
		final IVariableFragment vf = new VariableFragment(file, vname);
		vf.setArray(ArrayTools.integrate(values));
		return vf;
	}

	/**
	 * @param fileFragment
	 * @param a
	 */
	public static void createIntensitiesArray(final IFileFragment fileFragment,
	        final Array a) {
		EvalTools.notNull(new Object[] { fileFragment, a }, MaltcmsTools.class);
		final IVariableFragment intensity = new VariableFragment(fileFragment,
		        Factory.getInstance().getConfiguration().getString(
		                "var.total_intensity", "total_intensity"));
		intensity.setArray(a);
	}

	public static Tuple2D<IVariableFragment, IVariableFragment> createMinMaxMassValueArrays(
	        final IFileFragment file, final List<Array> scans) {
		EvalTools.notNull(new Object[] { file, scans }, MaltcmsTools.class);
		double minmassv = Double.POSITIVE_INFINITY;
		double maxmassv = Double.NEGATIVE_INFINITY;
		// int size = 0;// retList.get(0).getShape()[0];
		for (final Array a : scans) {
			if (a instanceof Sparse) {
				final Sparse s = ((Sparse) a);
				minmassv = Math.min(minmassv, s.getMinIndex());
				maxmassv = Math.max(minmassv, s.getMaxIndex());
				// size += s.getKeySet().size();// FIXME if this doesnt work use
				// s.getMaxIndex()-s.getMinIndex()
			} else {
				// size += a.getShape()[0];
				final MAMath.MinMax mm = MAMath.getMinMax(a);
				minmassv = Math.min(minmassv, mm.min);
				maxmassv = Math.max(maxmassv, mm.max);
			}
		}
		final ArrayDouble.D1 minmass = new ArrayDouble.D1(scans.size());
		final ArrayDouble.D1 maxmass = new ArrayDouble.D1(scans.size());
		final IndexIterator minmassiter = minmass.getIndexIteratorFast();
		final IndexIterator maxmassiter = maxmass.getIndexIteratorFast();
		while (minmassiter.hasNext() && maxmassiter.hasNext()) {
			minmassiter.setDoubleNext(minmassv);
			maxmassiter.setDoubleNext(maxmassv);
		}
		final String mmin = Factory.getInstance().getConfiguration().getString(
		        "var.mass_range_min", "mass_range_min");
		final String mmax = Factory.getInstance().getConfiguration().getString(
		        "var.mass_range_max", "mass_range_max");
		IVariableFragment new_mass_range_min = null;
		IVariableFragment new_mass_range_max = null;
		if (file.hasChild(mmin)) {
			new_mass_range_min = file.getChild(mmin);
		} else {
			new_mass_range_min = new VariableFragment(file, mmin);
		}
		if (file.hasChild(mmax)) {
			new_mass_range_max = file.getChild(mmax);
		} else {
			new_mass_range_max = new VariableFragment(file, mmax);
		}
		new_mass_range_min.setArray(minmass);
		new_mass_range_max.setArray(maxmass);
		return new Tuple2D<IVariableFragment, IVariableFragment>(
		        new_mass_range_min, new_mass_range_max);
	}

	public static Sparse createSparse(final IVariableFragment index,
	        final IVariableFragment values, final int minindex,
	        final int maxindex, final int nbins, final double massBinResolution) {
		final Array indx = index.getArray();
		final Array vals = values.getArray();
		if ((indx instanceof ArrayDouble.D1)
		        && (vals instanceof ArrayDouble.D1)) {
			return ArrayTools.createSparseIndexArray((ArrayDouble.D1) indx,
			        (ArrayDouble.D1) vals, minindex, maxindex, nbins,
			        massBinResolution);
		}
		throw new IllegalArgumentException("Cannot create Sparse Index Array!");
	}

	public static Tuple2D<Double, Double> findGlobalMinMax(
	        final TupleND<IFileFragment> ff, final String mmin,
	        final String mmax, final String fallback) {
		Double min = Double.MAX_VALUE;
		Double max = Double.MIN_VALUE;
		final boolean ignoreMinMaxMassArrays = Factory.getInstance()
		        .getConfiguration().getBoolean(
		                "maltcms.tools.MaltcmsTools.ignoreMinMaxMassArrays",
		                true);
		boolean useFallback = true;
		for (final IFileFragment f : ff) {
			if (!ignoreMinMaxMassArrays) {
				try {
					MaltcmsTools.log.info(
					        "Trying to load children from file {}", f);
					final IVariableFragment vmin = f.getChild(mmin);
					final IVariableFragment vmax = f.getChild(mmax);
					min = Math.min(MAMath.getMinimum(vmin.getArray()), min);
					max = Math.max(MAMath.getMaximum(vmax.getArray()), max);
					MaltcmsTools.log.info("Min={},Max={}", min, max);
					useFallback = false;
				} catch (final ResourceNotAvailableException e) {
					MaltcmsTools.log.debug(
					        "Trying to load children from file {} failed", f);
					MaltcmsTools.log.warn(e.getLocalizedMessage());
				}
			}
			if (useFallback) {
				// There are some vendor formats of netcdf, where values in min
				// mass
				// value
				// array are 0, which is not the minimum of measured masses, so
				// check
				// values unless we were successful above
				MaltcmsTools.log.debug("Trying to load fallback {} from {}",
				        fallback, f);
				final IVariableFragment mass_vals = f.getChild(fallback);
				final Array a = mass_vals.getArray();
				final MAMath.MinMax mm = MAMath.getMinMax(a);
				min = Math.min(min,mm.min);
				max = Math.max(max, mm.max);
				MaltcmsTools.log.info(" From fallback: Min={},Max={}", min, max);
			}
		}
		EvalTools.neqD(min, Double.MAX_VALUE, MaltcmsTools.class);
		EvalTools.neqD(max, Double.MIN_VALUE, MaltcmsTools.class);
		MaltcmsTools.log.info("Found minimum mass: {} and maximum mass {}",
		        min, max);
		Factory.getInstance().getConfiguration().setProperty(
		        "maltcms.commands.filters.DenseArrayProducer.min_mass", min);
		Factory.getInstance().getConfiguration().setProperty(
		        "maltcms.commands.filters.DenseArrayProducer.max_mass", max);

		return new Tuple2D<Double, Double>(min, max);
	}

	public static Tuple2D<List<IAnchor>, List<IAnchor>> getAnchors(
	        final IFileFragment ff1, final IFileFragment ff2) {
		final List<IAnchor> l1 = MaltcmsTools.prepareAnchors(ff1);
		final List<IAnchor> l2 = MaltcmsTools.prepareAnchors(ff2);
		MaltcmsTools.log.info("Number of anchors lhs: {}, rhs: {}", l1.size(),
		        l2.size());
		final List<String> l2s = new ArrayList<String>(l2.size());
		MaltcmsTools.log.debug("Anchors in {}", ff1.getName());
		for (final IAnchor ia2 : l2) {
			MaltcmsTools.log.debug("{} at {}", ia2.getName(), ia2
			        .getScanIndex());
			l2s.add(ia2.getName());
		}
		final TreeSet<String> s = new TreeSet<String>();
		MaltcmsTools.log.debug("Anchors in {}", ff2.getName());
		for (final IAnchor ia1 : l1) {
			MaltcmsTools.log.debug("{} at {}", ia1.getName(), ia1
			        .getScanIndex());
			s.add(ia1.getName());
		}
		s.retainAll(l2s);
		MaltcmsTools.log.debug("Using {} paired anchors", s.size());
		final Iterator<String> iter = s.iterator();
		while (iter.hasNext()) {
			MaltcmsTools.log.debug("{}", iter.next());
		}
		final ListIterator<IAnchor> li1 = l1.listIterator();
		while (li1.hasNext()) {
			final IAnchor ia = li1.next();
			if (!s.contains(ia.getName())) {
				li1.remove();
			}
		}
		final ListIterator<IAnchor> li2 = l2.listIterator();
		while (li2.hasNext()) {
			final IAnchor ia = li2.next();
			if (!s.contains(ia.getName())) {
				li2.remove();
			}
		}

		final Tuple2D<List<IAnchor>, List<IAnchor>> t = new Tuple2D<List<IAnchor>, List<IAnchor>>(
		        l1, l2);
		return t;
	}

	public static IFileFragment getDenseArrayFragmentFor(final IFileFragment iff) {
		return MaltcmsTools.getDenseArrayFragmentFor(iff.getName());
	}

	public static IFileFragment getDenseArrayFragmentFor(
	        final String fragmentName) {
		if (MaltcmsTools.denseArrays.containsKey(fragmentName)) {
			return MaltcmsTools.denseArrays.get(fragmentName);
		}
		return null;
	}

	public static Array getEIC(final IFileFragment f, final double eicStart,
	        final double eicStop, boolean normalize, boolean keepMaxInBin) {
		MaltcmsTools.log.info("Retrieving EIC from {}", f.getAbsolutePath());
		final IVariableFragment index1 = f.getChild(Factory.getInstance()
		        .getConfiguration().getString("var.scan_index", "scan_index"));
		final String massVar = Factory.getInstance().getConfiguration()
		        .getString("var.mass_values", "mass_values");
		final String intensVar = Factory.getInstance().getConfiguration()
		        .getString("var.intensity_values", "intensity_values");
		if (f.getChild(massVar).getIndex() == null) {
			f.getChild(massVar).setIndex(index1);
			f.getChild(intensVar).setIndex(index1);
		}
		final List<Array> intens1 = f.getChild(intensVar).getIndexedArray();
		final List<Array> mass1 = f.getChild(massVar).getIndexedArray();
		final ArrayDouble.D1 eic = new ArrayDouble.D1(intens1.size());
		final ArrayInt.D1 eicbinCnt = new ArrayInt.D1(intens1.size());
		for (int i = 0; i < eic.getShape()[0]; i++) {
			final Array masses = mass1.get(i);
			final Index mind = masses.getIndex();
			final Array intensities = intens1.get(i);
			final Index intind = intensities.getIndex();
			if (keepMaxInBin) {// only keep max in bin (xcms)
				int max = Integer.MIN_VALUE;
				for (int j = 0; j < masses.getShape()[0]; j++) {
					mind.set(j);
					intind.set(j);
					final double m = masses.getDouble(mind);
					// in range
					if ((m >= eicStart) && (m < eicStop)) {
						final int val = intensities.getInt(intind);
						if (val > max) {
							eic.set(i, val);
							max = val;
						}
					}
				}
			} else {// sum all intensities in bin
				for (int j = 0; j < masses.getShape()[0]; j++) {
					mind.set(j);
					intind.set(j);
					final double m = masses.getDouble(mind);
					// in range
					if ((m >= eicStart) && (m < eicStop)) {
						final int val = intensities.getInt(intind);
						eicbinCnt.set(i, eicbinCnt.get(i) + 1);
						eic.set(i, val + eic.get(i));
					}
				}
			}
		}
		if (normalize && !keepMaxInBin) {// only normalize if we have summed
			// intensities
			for (int i = 0; i < eic.getShape()[0]; i++) {
				eic.set(i, eic.get(i) / (double) eicbinCnt.get(i));
			}
		}
		return eic;
	}

	public static TupleND<IFileFragment> getFileFragmentsFromStringArray(
	        final IFileFragment pwdFile, final String varname) {
		final Collection<String> s = FragmentTools.getStringArray(pwdFile,
		        varname);
		final ArrayList<IFileFragment> al = new ArrayList<IFileFragment>();
		for (final String sn : s) {
			if (FileFragment.hasFragment(sn)) {
				al.add(FileFragment.getFragment(sn));
			} else {
				al.add(new FileFragment(new File(sn), null, null));
			}
		}
		final TupleND<IFileFragment> t = new TupleND<IFileFragment>(al);
		return t;
	}

	private static String getKey(final IFileFragment ref,
	        final IFileFragment query) {
		final String key = ref.getAbsolutePath() + "-"
		        + query.getAbsolutePath();
		return key;
	}

	public static Tuple2D<Double, Double> getMinMaxMassRange(
	        final IFileFragment f) {
		EvalTools.notNull(f, MaltcmsTools.class);
		final String mmin = Factory.getInstance().getConfiguration().getString(
		        "var.mass_values", "mass_values");
		final Array ref_min = f.getChild(mmin).getArray();
		final MinMax mm = MAMath.getMinMax(ref_min);
		return new Tuple2D<Double, Double>(mm.min, mm.max);
	}

	public static Tuple2D<Double, Double> getMinMaxMassRange(
	        final IFileFragment reference, final IFileFragment query) {
		EvalTools
		        .notNull(new Object[] { reference, query }, MaltcmsTools.class);
		final Tuple2D<Double, Double> a = MaltcmsTools
		        .getMinMaxMassRange(reference);
		final Tuple2D<Double, Double> b = MaltcmsTools
		        .getMinMaxMassRange(query);
		return new Tuple2D<Double, Double>(
		        Math.min(a.getFirst(), b.getFirst()), Math.max(a.getSecond(), b
		                .getSecond()));
	}

	public static Tuple2D<List<Array>, List<Array>> getMZIs(
	        final IFileFragment ff) {
		final String mz = Factory.getInstance().getConfiguration().getString(
		        "var.mass_values", "mass_values");
		final String intens = Factory.getInstance().getConfiguration()
		        .getString("var.intensity_values", "intensity_values");
		final String scan_index = Factory.getInstance().getConfiguration()
		        .getString("var.scan_index", "scan_index");
		final IVariableFragment index = ff.getChild(scan_index);
		final IVariableFragment mzV = ff.getChild(mz);
		mzV.setIndex(index);
		final List<Array> mzs = mzV.getIndexedArray();
		final IVariableFragment iV = ff.getChild(intens);
		iV.setIndex(index);
		final List<Array> is = iV.getIndexedArray();

		return new Tuple2D<List<Array>, List<Array>>(mzs, is);
	}

	public static Tuple2D<List<Array>, List<Array>> getBinnedMZIs(
	        final IFileFragment ff) {
		final String mz = Factory.getInstance().getConfiguration().getString(
		        "var.binned_mass_values", "binned_mass_values");
		final String intens = Factory.getInstance().getConfiguration()
		        .getString("var.binned_intensity_values",
		                "binned_intensity_values");
		final String scan_index = Factory.getInstance().getConfiguration()
		        .getString("var.binned_scan_index", "binned_scan_index");
		final IVariableFragment index = ff.getChild(scan_index);
		final IVariableFragment mzV = ff.getChild(mz);
		mzV.setIndex(index);
		final List<Array> mzs = mzV.getIndexedArray();
		final IVariableFragment iV = ff.getChild(intens);
		iV.setIndex(index);
		final List<Array> is = iV.getIndexedArray();

		return new Tuple2D<List<Array>, List<Array>>(mzs, is);
	}

	public static int getNumberOfIntegerMassBins(final double minMass,
	        final double maxMass, final double resolution) {
		return (int) Math.ceil(((Math.ceil(maxMass) - Math.floor(minMass)) + 1)
		        * resolution);
	}

	public static IFileFragment getPairwiseAlignment(final IFileFragment ref,
	        final IFileFragment query) {
		final String key1 = MaltcmsTools.getKey(ref, query);
		final String key2 = MaltcmsTools.getKey(query, ref);
		if (MaltcmsTools.pairwiseAlignments.containsKey(key1)) {
			return MaltcmsTools.pairwiseAlignments.get(key1);
		}
		if (MaltcmsTools.pairwiseAlignments.containsKey(key2)) {
			return MaltcmsTools.pairwiseAlignments.get(key2);
		}
		throw new ResourceNotAvailableException(
		        "No pairwise alignment available for " + ref.getAbsolutePath()
		                + " and " + query.getAbsolutePath());
	}

	public static IFileFragment getPairwiseDistanceFragment() {
		final String name = Factory.getInstance().getConfiguration().getString(
		        "pairwise_distances_location");
		if (name.equals("")) {
			return null;
		}
		IFileFragment ff = FileFragment.getFragment(name);
		if (ff == null) {
			ff = FileFragmentFactory.getInstance().create(new File(name));
			final TupleND<IFileFragment> t = MaltcmsTools
			        .getPairwiseDistanceFragments(ff);
			for (final IFileFragment iff : t) {
				MaltcmsTools.log.info("Loading pairwise distance fragment {}",
				        iff.getAbsolutePath());
				MaltcmsTools.addPairwiseAlignment(
				        FragmentTools.getLHSFile(iff), FragmentTools
				                .getRHSFile(iff), iff);
			}
		}
		return ff;
	}

	public static TupleND<IFileFragment> getPairwiseDistanceFragments(
	        final IFileFragment pwdFile) {
		final String varname = Factory.getInstance().getConfiguration()
		        .getString("var.pairwise_distance_alignment_names",
		                "pairwise_distance_alignment_names");
		final TupleND<IFileFragment> t = MaltcmsTools
		        .getFileFragmentsFromStringArray(pwdFile, varname);
		return t;
	}

	public static Array getTIC(final IFileFragment f) {
		EvalTools.notNull(f, MaltcmsTools.class);
		final String tics = Factory.getInstance().getConfiguration().getString(
		        "var.total_intensity", "total_intensity");
		IVariableFragment v;
		v = f.getChild(tics);
		return v.getArray();
	}

	public static List<Tuple2DI> getWarpPath(final IFileFragment ff) {
		final String wpi = Factory.getInstance().getConfiguration().getString(
		        "var.warp_path_i", "warp_path_i");
		final String wpj = Factory.getInstance().getConfiguration().getString(
		        "var.warp_path_j", "warp_path_j");
		final IVariableFragment wpiV = ff.getChild(wpi);
		final IVariableFragment wpjV = ff.getChild(wpj);
		final Array wpiA = wpiV.getArray();
		final Array wpjA = wpjV.getArray();
		EvalTools.notNull(new Object[] { wpiA, wpjA }, MaltcmsTools.class);
		return PathTools.fromArrays(wpiA, wpjA);
	}

	public static boolean hasDenseArrayFragmentFor(final IFileFragment iff) {
		return MaltcmsTools.hasDenseArrayFragmentFor(iff.getName());
	}

	public static boolean hasDenseArrayFragmentFor(final String fragmentName) {
		return MaltcmsTools.denseArrays.containsKey(fragmentName);
	}

	public static boolean hasPairwiseAlignment(final IFileFragment ref,
	        final IFileFragment query) {
		final String key1 = MaltcmsTools.getKey(ref, query);
		final String key2 = MaltcmsTools.getKey(query, ref);
		if (MaltcmsTools.pairwiseAlignments.containsKey(key1)) {
			return true;
		}
		if (MaltcmsTools.pairwiseAlignments.containsKey(key2)) {
			return true;
		}
		return false;
	}

	public static Integer[] pairedEICs(
	        final List<Tuple2D<Double, Double>> eics1,
	        final List<Tuple2D<Double, Double>> eics2) {
		final HashSet<Integer> hs1 = new HashSet<Integer>(eics1.size());
		for (final Tuple2D<Double, Double> t : eics1) {
			hs1.add(new Integer(t.getSecond().intValue()));
		}
		final HashSet<Integer> hs2 = new HashSet<Integer>(eics1.size());
		for (final Tuple2D<Double, Double> t : eics2) {
			hs2.add(new Integer(t.getSecond().intValue()));
		}

		hs1.retainAll(hs2);
		final Integer[] arr = hs1.toArray(new Integer[] {});
		// sort ascending
		Arrays.sort(arr);
		return arr;
	}

	public static List<Double> parseMaskedMassesList(final List<?> l) {
		if (l.size() > 0) {
			MaltcmsTools.log.info("Masking the following masses:");
		} else {
			return Collections.emptyList();
		}
		final ArrayList<Double> al = new ArrayList<Double>(l.size());
		final StringBuffer sb = new StringBuffer();
		for (final Object o : l) {
			if (o instanceof String) {
				if (!((String) o).isEmpty()) {
					final Double dble = Double.parseDouble((String) o);
					sb.append(dble + ",");
					al.add(dble);
				}
			}
		}
		if (sb.length() > 1) {
			sb.deleteCharAt(sb.length() - 1);
		}
		if (!l.isEmpty()) {
			MaltcmsTools.log.info("{}", sb);
		}
		return al;
	}

	public static List<IAnchor> prepareAnchors(final IFileFragment ff) {
		EvalTools.notNull(ff, MaltcmsTools.class);
		final ArrayList<IAnchor> al = new ArrayList<IAnchor>();
		final String ri_names = Factory.getInstance().getConfiguration()
		        .getString("var.anchors.retention_index_names",
		                "retention_index_names");
		final String ri_times = Factory.getInstance().getConfiguration()
		        .getString("var.anchors.retention_times", "retention_times");
		final String ri_indices = Factory
		        .getInstance()
		        .getConfiguration()
		        .getString("var.anchors.retention_indices", "retention_indices");
		final String ri_scans = Factory.getInstance().getConfiguration()
		        .getString("var.anchors.retention_scans", "retention_scans");

		Array ri_namesA = null;
		Array ri_timesA = null;
		Array ri_indicesA = null;
		Array ri_scansA = null;

		IVariableFragment ri_namesV;
		try {
			ri_namesV = ff.getChild(ri_names);
			ri_namesA = ri_namesV.getArray();
			if (!ff.getAbsolutePath().equals(
			        ri_namesV.getParent().getAbsolutePath())) {
				MaltcmsTools.log.debug("Parent of riNames: {}", ri_namesV
				        .getParent());
			}
		} catch (final ResourceNotAvailableException e) {
			MaltcmsTools.log.debug(e.getLocalizedMessage());
		}

		try {
			final IVariableFragment ri_timesV = ff.getChild(ri_times);
			ri_timesA = ri_timesV.getArray();
			if (!ff.getAbsolutePath().equals(
			        ri_timesV.getParent().getAbsolutePath())) {
				MaltcmsTools.log.debug("Parent of riTimes: {}", ri_timesV
				        .getParent());
			}
		} catch (final ResourceNotAvailableException e) {
			MaltcmsTools.log.debug(e.getLocalizedMessage());
		}

		try {
			final IVariableFragment ri_indicesV = ff.getChild(ri_indices);
			ri_indicesA = ri_indicesV.getArray();
			if (!ff.getAbsolutePath().equals(
			        ri_indicesV.getParent().getAbsolutePath())) {
				MaltcmsTools.log.debug("Parent of riIndices: {}", ri_indicesV
				        .getParent());
			}
		} catch (final ResourceNotAvailableException e) {
			MaltcmsTools.log.debug(e.getLocalizedMessage());
		}

		try {
			final IVariableFragment ri_scansV = ff.getChild(ri_scans);
			ri_scansA = ri_scansV.getArray();
			if (!ff.getAbsolutePath().equals(
			        ri_scansV.getParent().getAbsolutePath())) {
				MaltcmsTools.log.debug("Parent of riScans: {}", ri_scansV
				        .getParent());
			}
			if (ri_scansA == null) {
				throw new ResourceNotAvailableException(
				        "Could not load anchor scans!");
			}
			// EvalTools.notNull(ri_scansA,ri_indicesA,ri_timesA);
			String name = "";
			// log.info(ri_namesA.getElementType().getName());
			ArrayChar.D2 names = null;
			if (ri_namesA == null) {
				names = new ArrayChar.D2(ri_scansA.getShape()[0], 64);
				final Index riscansIndex = ri_scansA.getIndex();
				for (int i = 0; i < names.getShape()[0]; i++) {
					if (ri_scansA.getInt(riscansIndex.set(i)) == -1) {
						names.setString(i, "ANCHOR" + (i + 1));
					}
				}

			} else if (ri_namesA instanceof ArrayChar.D2) {
				names = (ArrayChar.D2) ri_namesA;
			}
			// Ensure that we have at least names and matching scans
			EvalTools.notNull(names, MaltcmsTools.class);
			EvalTools.notNull(ri_scansA, MaltcmsTools.class);
			final StringIterator si = names.getStringIterator();
			Index timesi = null;
			if (ri_timesA != null) {
				timesi = ri_timesA.getIndex();
			}
			Index indicesi = null;
			if (ri_indicesA != null) {
				indicesi = ri_indicesA.getIndex();
			}
			final IndexIterator ii4 = ri_scansA.getIndexIterator();
			int i = 0;
			while (si.hasNext() && ii4.hasNext()) {
				final RetentionInfo ri = new RetentionInfo();
				name = si.next();
				final int scan = ii4.getIntNext();
				if (!name.isEmpty() && (scan > -1)) {
					ri.setName(name);
					// Retention time is optional
					if (ri_timesA != null) {
						final double time = ri_timesA.getDouble(timesi.set(i));
						if (time > -1) {
							ri.setRetentionTime(time);
						}
					}
					// Retention index is optional
					if (ri_indicesA != null) {
						final double index = ri_indicesA.getDouble(indicesi
						        .set(i));
						if (index > -1) {
							ri.setRetentionIndex(index);
						}
					}
					ri.setScanIndex(scan);
					al.add(ri);
				} else {
					MaltcmsTools.log
					        .debug("Skipping anchor due to empty name!");
				}
				i++;
			}
		} catch (final ResourceNotAvailableException e) {
			MaltcmsTools.log.warn(e.getLocalizedMessage());
		}
		if (al.isEmpty()) {
			MaltcmsTools.log.warn("Could not prepare anchors for file {}", ff
			        .getName());
		}
		return al;
	}

	public static List<Array> prepareArraysMZIasList(final IFileFragment ff,
	        final String index_name, final String mz_name,
	        final String intens_name, final String mass_range_min_name,
	        final String mass_range_max_name) {

		MaltcmsTools.log.debug("Creating variables for file {}!", ff
		        .getAbsolutePath());
		final IVariableFragment index = ff.getChild(index_name);
		final IVariableFragment mz = FragmentTools.createVariable(ff, mz_name,
		        index);
		final IVariableFragment inten = FragmentTools.createVariable(ff,
		        intens_name, index);
		IVariableFragment minmz = null;
		IVariableFragment maxmz = null;
		try {
			minmz = ff.getChild(mass_range_min_name);
			maxmz = ff.getChild(mass_range_max_name);
		} catch (ResourceNotAvailableException rnae) {
			MaltcmsTools.log
			        .warn("Could not find mass range variables, checking all masses!");
		}
		MaltcmsTools.log.debug("Done!");
		MaltcmsTools.log.debug("Loading arrays!");
		final Array minmza = minmz == null ? null : minmz.getArray();
		final Array maxmza = maxmz == null ? null : maxmz.getArray();
		MaltcmsTools.log.debug("Done!");
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		MaltcmsTools.log.debug("Calculating min and max!");
		if ((minmza == null) || (maxmza == null)) {
			min = (int) Math.rint(MAMath.getMinimum(mz.getArray()));
			max = (int) Math.rint(MAMath.getMaximum(mz.getArray()));
		} else {
			min = (int) Math.rint(MAMath.getMinimum(minmza));
			max = (int) Math.rint(MAMath.getMaximum(maxmza));
		}
		MaltcmsTools.log.debug("Done, min = {}, max = {}!", min, max);
		// int w = (max - min);
		mz.setIndex(index);
		final List<Array> mza = mz.getIndexedArray();
		inten.setIndex(index);
		final List<Array> intena = inten.getIndexedArray();
		MaltcmsTools.log.debug("Created indexed arrays!");
		MaltcmsTools.log.debug("Done!");
		final double massResolution = Factory.getInstance().getConfiguration()
		        .getDouble("dense_arrays.massBinResolution", 1.0d);
		return SparseTools.createAsList(mza, intena, (min), (max), MaltcmsTools
		        .getNumberOfIntegerMassBins(min, max, massResolution),
		        massResolution);
	}

	public static IFileFragment prepareDenseArraysMZI(final IFileFragment ff,
	        final String scan_index, final String mass_values,
	        final String intensity_values, final String binned_scan_index,
	        final String binned_mass_values,
	        final String binned_intensity_values, final Double min_mass,
	        final Double max_mass, final Date d) {

		// Create target file fragment
		final IFileFragment f = FileFragmentFactory.getInstance().create(
		        FileTools.prependDefaultDirs(ff.getName(),
		                DenseArrayProducer.class, d), DenseArrayProducer.class);
		f.addSourceFile(ff);
		MaltcmsTools.log.debug(
		        "Created target IFileFragment {}, with parent {}", f
		                .getAbsolutePath(), ff.getAbsolutePath());
		// Retrieve original scan index
		final IVariableFragment si = ff.getChild(scan_index);
		final Array b = si.getArray();
		EvalTools.notNull(b, MaltcmsTools.class);
		// Retrieve original mass_values
		final IVariableFragment mz = ff.getChild(mass_values);
		// Manually set index
		mz.setIndex(si);
		// Retrieve original intensity_values
		final IVariableFragment inten = ff.getChild(intensity_values);
		// Manually set index
		inten.setIndex(si);
		// Read mass_values with index
		final List<Array> mza = mz.getIndexedArray();
		// Read intensity_values with index
		final List<Array> intena = inten.getIndexedArray();
		// Create new VariableFragment for mass_values
		// VariableFragment retMZ = new VariableFragment(f, mz.getVarname());
		// Create new VariableFragment for intensity_values
		// VariableFragment retInten = new VariableFragment(f, inten
		// .getVarname());
		// Create new VariableFragment for scan_index
		// VariableFragment retScanIndex = new VariableFragment(f, si
		// .getVarname());
		// Check, that all created objects are correctly initialized
		// EvalTools.notNull(new Object[] { retMZ, retInten, retScanIndex },
		// MaltcmsTools.class);
		// Check, that original indexed arrays are present
		EvalTools.notNull(mza, MaltcmsTools.class);
		EvalTools.notNull(intena, MaltcmsTools.class);
		// Set fillvalue for missing intensities, e.g. if a mass channel was
		// not recorded for a given bin
		final double fillvalue = Factory.getInstance().getConfiguration()
		        .getDouble("intensity.missing.value", 0.0d);
		// Number of bins, currently only resolution of 1 m/z
		// TODO integrate resolution
		final double massBinResolution = Factory.getInstance()
		        .getConfiguration().getDouble("dense_arrays.massBinResolution",
		                1.0d);
		final int size = MaltcmsTools.getNumberOfIntegerMassBins(min_mass,
		        max_mass, massBinResolution);
		// Check, that size is at least 1 and at most largest integer
		EvalTools.inRangeI(1, Integer.MAX_VALUE, size, MaltcmsTools.class);
		MaltcmsTools.log.debug("Creating dense arrays with " + size
		        + " elements!");
		// Create new index array with size = number of scans
		final ArrayInt.D1 retIndexArray = new ArrayInt.D1(mza.size());
		final ArrayList<Array> retMZa = new ArrayList<Array>(mza.size());
		final ArrayList<Array> retIntena = new ArrayList<Array>(mza.size());
		// For all scans
		for (int i = 0; i < mza.size(); i++) {
			MaltcmsTools.log.debug("Processing scan {}/{}", i + 1, mza.size());
			// Create a binned mz array
			final Array indx = Array.factory(mz.getDataType(),
			        new int[] { size });
			// Create a binned intensity array
			final Array vals = Array.factory(mz.getDataType(),
			        new int[] { size });
			// Fill arrays with values, mz starts at min_mass, goes until
			// max_mass, with unit increment
			ArrayTools.createDenseArray(mza.get(i), intena.get(i),
			        new Tuple2D<Array, Array>(indx, vals), ((int) Math
			                .floor(min_mass)), ((int) Math.ceil(max_mass)),
			        size, massBinResolution, fillvalue);
			retMZa.add(indx);
			retIntena.add(vals);
			retIndexArray.set(i, i * size);
		}
		// mass values
		EvalTools.notNull(retMZa, MaltcmsTools.class);
		// IVariableFragment mzRet = new VariableFragment(f, mz.getVarname());
		// mzRet.setIndexedArray(retMZa);
		if (!f.hasChild(binned_scan_index)) {
			final IVariableFragment siRetDense = new VariableFragment(f,
			        binned_scan_index);
			siRetDense.setArray(retIndexArray);
		}
		if (!f.hasChild(binned_mass_values)) {
			final IVariableFragment mzRetDense = new VariableFragment(f,
			        binned_mass_values);
			// mzRetDense.setIndex(siRetDense);
			// mzRetDense.setArray(ArrayTools.glue(retMZa));
			mzRetDense.setIndexedArray(retMZa);
		}

		// intensities
		EvalTools.notNull(retIntena, MaltcmsTools.class);
		// IVariableFragment intenRet = new VariableFragment(f,
		// inten.getVarname());
		// intenRet.setIndexedArray(retIntena);
		if (!f.hasChild(binned_intensity_values)) {
			final IVariableFragment intenRetDense = new VariableFragment(f,
			        binned_intensity_values);
			// intenRetDense.setIndex(siRetDense);
			intenRetDense.setIndexedArray(retIntena);

			// intenRetDense.setArray(ArrayTools.glue(retIntena));
			Array a = intenRetDense.getArray();
			MaltcmsTools.log.debug("{}", a);
			MaltcmsTools.log.debug("{}", retIndexArray);
			intenRetDense.setIndex(f.getChild(binned_scan_index));
			List<Array> al = intenRetDense.getIndexedArray();
			MaltcmsTools.log.debug("{}", al);
		}
		// index
		EvalTools.notNull(retIndexArray, MaltcmsTools.class);
		// IVariableFragment siRet = new VariableFragment(f, si.getVarname());
		// siRet.setArray(retIndexArray);
		// mzRet.setIndex(siRet);
		// intenRet.setIndex(siRet);

		// siRetDense.setArray(retIndexArray);
		// mzRetDense.setIndex(siRetDense);
		// intenRetDense.setIndex(siRetDense);

		EvalTools.notNull(f, MaltcmsTools.class);
		MaltcmsTools.log.debug("{}", f);
		return f;
	}

	public static Tuple2D<Array, Array> getBinnedMS(final IFileFragment iff,
	        final int i) {
		Logging.getLogger(MaltcmsTools.class).info("Reading scan {}", i);
		final String sindex = Factory.getInstance().getConfiguration()
		        .getString("var.binned_scan_index", "binned_scan_index");
		Array masses = getIndexed(iff, Factory.getInstance().getConfiguration()
		        .getString("var.binned_mass_values", "binned_mass_values"),
		        sindex, i);
		Array intensities = getIndexed(iff, Factory.getInstance()
		        .getConfiguration().getString("var.binned_intensity_values",
		                "binned_intensity_values"), sindex, i);
		return new Tuple2D<Array, Array>(masses, intensities);
	}

	public static Array getIndexed(IFileFragment iff, String var,
	        String indexVar, int i) throws ConstraintViolationException {
		IVariableFragment si = iff.getChild(indexVar);
		Array sia = si.getArray();
		Range[] sir = si.getRange();
		IVariableFragment variable = iff.getChild(var);
		IVariableFragment oldIndex = variable.getIndex();
		Array old = variable.getArray();
		List<Array> indexedArray = variable.getIndexedArray();
		// if indexed array is already loaded with same index, simply return ith
		// element
		if (oldIndex != null && oldIndex.getVarname().equals(si.getVarname())) {
			return indexedArray.get(i);
		}
		// if (variable.hasArray()) {
		// throw new ConstraintViolationException("Array already set on "
		// + variable.getVarname()
		// + ", please clear before calling getIndexed()!");

		// }
		// just to be sure
		variable.setArray(null);
		variable.setIndex(si);
		try {
			si.setRange(new Range[] { new Range(i, i) });
		} catch (InvalidRangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Array ma = variable.getIndexedArray().get(0);
		si.setRange(sir);
		si.setArray(sia);
		// reset variable array
		variable.setIndex(oldIndex);
		if (indexedArray != null) {
			variable.setIndexedArray(indexedArray);
		} else {
			variable.setArray(old);
		}
		return ma;
	}

	public static Tuple2D<Array, Array> getMS(IFileFragment iff, int i) {
		Logging.getLogger(MaltcmsTools.class).info("Reading scan {}", i);
		final String sindex = Factory.getInstance().getConfiguration()
		        .getString("var.scan_index", "scan_index");
		Array masses = getIndexed(iff, Factory.getInstance().getConfiguration()
		        .getString("var.mass_values", "mass_values"), sindex, i);
		Array intensities = getIndexed(iff, Factory.getInstance()
		        .getConfiguration().getString("var.intensity_values",
		                "intensity_values"), sindex, i);
		return new Tuple2D<Array, Array>(masses, intensities);
	}

	/**
	 * @param t
	 * @param caller
	 * @param d
	 * @return
	 */
	public static List<IFileFragment> prepareEICFragments(
	        final TupleND<IFileFragment> t, final Class<?> caller, final Date d) {
		final List<IFileFragment> unalignedEICFragments = new ArrayList<IFileFragment>(
		        t.size());
		try {
			for (final IFileFragment iff : t) {
				final IFileFragment eicFragment = FileFragmentFactory
				        .getInstance()
				        .create(
				                FileTools.prependDefaultDirs(caller, d)
				                        .getAbsolutePath(), iff.getName(), null);
				for (final IFileFragment initIff : Factory.getInitialFiles()) {
					if (StringTools.removeFileExt(iff.getName()).equals(
					        StringTools.removeFileExt(initIff.getName()))) {
						final String massV = Factory.getInstance()
						        .getConfiguration().getString(
						                "var.mass_values", "mass_values");
						final String intV = Factory.getInstance()
						        .getConfiguration().getString(
						                "var.intensity_values",
						                "intensity_values");
						final String scanV = Factory.getInstance()
						        .getConfiguration().getString("var.scan_index",
						                "scan_index");
						final Array masses = initIff.getChild(massV).getArray();
						final Array intensities = initIff.getChild(intV)
						        .getArray();
						final Array scan_index = initIff.getChild(scanV)
						        .getArray();
						final IVariableFragment indexf = new VariableFragment(
						        eicFragment, scanV);
						indexf.setArray(scan_index);
						final IVariableFragment mvf = new VariableFragment(
						        eicFragment, massV, indexf);
						mvf.setArray(masses);
						final IVariableFragment indf = new VariableFragment(
						        eicFragment, intV, indexf);
						indf.setArray(intensities);
						eicFragment.addSourceFile(iff);
						// eicFragment.save();
						// initIff.clearArrays();
					}
				}
				unalignedEICFragments.add(eicFragment);
			}
		} catch (ResourceNotAvailableException rnae) {
			log.warn("Failed to load resource: {}", rnae.getLocalizedMessage());
		}
		return unalignedEICFragments;
	}

	public static Tuple2D<List<Array>, List<Array>> prepareInputArraysTICasList(
	        final Tuple2D<IFileFragment, IFileFragment> t) {
		EvalTools.notNull(new Object[] { t, t.getFirst(), t.getSecond() }, t);
		final String ti = Factory.getInstance().getConfiguration().getString(
		        "var.total_intensity", "total_intensity");
		final Array ref = t.getFirst().getChild(ti).getArray();
		final Array query = t.getSecond().getChild(ti).getArray();
		final ArrayList<Array> a = new ArrayList<Array>();
		a.add(ref);
		final ArrayList<Array> b = new ArrayList<Array>();
		b.add(query);
		final Tuple2D<List<Array>, List<Array>> tuple = new Tuple2D<List<Array>, List<Array>>(
		        a, b);
		return tuple;
	}

	public static List<Array> prepareSparseMZI(final IFileFragment ff,
	        final String scan_index, final String mass_values,
	        final String intensity_values, final Double min_mass,
	        final Double max_mass) {
		final IVariableFragment si = ff.getChild(scan_index);
		final Array b = si.getArray();
		EvalTools.notNull(b, MaltcmsTools.class);
		// Retrieve original mass_values
		final IVariableFragment mz = ff.getChild(mass_values);
		// Manually set index
		mz.setIndex(si);
		// Retrieve original intensity_values
		final IVariableFragment inten = ff.getChild(intensity_values);
		// Manually set index
		inten.setIndex(si);
		// Read mass_values with index
		final List<Array> mza = mz.getIndexedArray();
		// Read intensity_values with index
		final List<Array> intena = inten.getIndexedArray();
		EvalTools.notNull(mza, MaltcmsTools.class);
		EvalTools.notNull(intena, MaltcmsTools.class);
		// Set fillvalue for missing intensities, e.g. if a mass channel was
		// not recorded for a given bin
		// Number of bins, currently only resolution of 1 m/z
		// TODO integrate resolution
		final double massBinResolution = Factory.getInstance()
		        .getConfiguration().getDouble("dense_arrays.massBinResolution",
		                1.0d);
		final int size = MaltcmsTools.getNumberOfIntegerMassBins(min_mass,
		        max_mass, massBinResolution);
		// Check, that size is at least 1 and at most largest integer
		EvalTools.inRangeI(1, Integer.MAX_VALUE, size, MaltcmsTools.class);
		final List<Array> l = new ArrayList<Array>(mza.size());
		MaltcmsTools.log.info("Creating sparse arrays with " + size
		        + " elements!");
		// For all scans
		for (int i = 0; i < mza.size(); i++) {
			MaltcmsTools.log.debug("Processing scan {}/{}", i + 1, mza.size());
			// Fill arrays with values, mz starts at min_mass, goes until
			// max_mass, with unit increment
			final ArrayDouble.D1 sparse = new Sparse(mza.get(i), intena.get(i),
			        (int) Math.floor(min_mass), (int) Math.ceil(max_mass),
			        size, massBinResolution);
			EvalTools.eqI(size, sparse.getShape()[0], MaltcmsTools.class);
			l.add(sparse);
		}
		return l;
	}

	public static List<Tuple2D<Double, Double>> rankEICsByVariance(
	        final IFileFragment f, final List<Array> intensities, final int k,
	        final Class<?> creator, final Date d) {
		final ArrayStatsScanner ass = new ArrayStatsScanner();
		final List<ArrayDouble.D1> eics1 = ArrayTools.tiltD1(ArrayTools
		        .convertArrays(intensities));
		final StatsMap[] sm1 = ass.apply(eics1.toArray(new Array[] {}));
		final List<Tuple2D<Double, Double>> vals1 = new ArrayList<Tuple2D<Double, Double>>(
		        sm1.length);
		for (int i = 0; i < sm1.length; i++) {
			vals1.add(new Tuple2D<Double, Double>(sm1[i]
			        .get(cross.datastructures.Vars.Variance.toString()),
			        (double) i));
		}
		Collections.sort(vals1, Collections
		        .reverseOrder(new Tuple2DDoubleComp()));
		final CSVWriter csvw = new CSVWriter();
		csvw.writeStatsMaps(FileTools.prependDefaultDirs(creator, d)
		        .getAbsolutePath(), StringTools.removeFileExt(f.getName())
		        + "_eic_stats.csv", sm1);
		return vals1.subList(0, Math.min(vals1.size() - 1, k - 1));
	}

	public static void setBinMZbyConfig() {
		MaltcmsTools.setBinMZDefault(Factory.getInstance().getConfiguration()
		        .getString("MaltcmsTools.binMZ.mode",
		                RoundMode.RFLOORINT.name()));
	}

	public static void setBinMZDefault(final String s) {
		MaltcmsTools.binMZMode = RoundMode.valueOf(s);
	}

	public static void setDenseArrayFragmentFor(final IFileFragment iff,
	        final IFileFragment daFragment) {
		MaltcmsTools.setDenseArrayFragmentFor(iff.getName(), daFragment);
	}

	public static void setDenseArrayFragmentFor(final String fragmentName,
	        final IFileFragment daFragment) {
		MaltcmsTools.denseArrays.put(fragmentName, daFragment);
	}

}
