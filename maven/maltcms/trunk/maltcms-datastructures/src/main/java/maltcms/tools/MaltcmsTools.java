/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;

import maltcms.commands.filters.array.SqrtFilter;
import maltcms.commands.filters.array.TopHatFilter;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.datastructures.array.Sparse;
import maltcms.datastructures.ms.Chromatogram1D;
import maltcms.datastructures.ms.ChromatogramFactory;
import maltcms.datastructures.ms.IAnchor;
import maltcms.datastructures.ms.IChromatogram1D;
import maltcms.datastructures.ms.IScan1D;
import maltcms.datastructures.ms.RetentionInfo;
import maltcms.io.csv.CSVWriter;


import ucar.ma2.Array;
import ucar.ma2.ArrayBoolean;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayChar.StringIterator;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import cross.Factory;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.*;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FragmentTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DDoubleComp;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.tuple.TupleND;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;
import lombok.extern.slf4j.Slf4j;
import ucar.nc2.Dimension;

/**
 * Utility class providing many comfort methods, providing more direct access to
 * andims compatible variables. Sort of an abstraction layer.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Slf4j
public class MaltcmsTools {

    private enum RoundMode {

        RINT, RFLOORINT, ROPINT;
    }
    private static RoundMode binMZMode = RoundMode.RFLOORINT;

    // private static HashMap<String, IFileFragment> pairwiseAlignments = new
    // HashMap<String, IFileFragment>();
    // public static void addPairwiseAlignment(final IFileFragment ref,
    // final IFileFragment query, final IFileFragment pa) {
    // // FIXME: replace "" by ?
    // MaltcmsTools.addPairwiseAlignment(ref, query, pa, "");
    // }
    // public static void addPairwiseAlignment(final IFileFragment ref,
    // final IFileFragment query, final IFileFragment pa,
    // final String extension) {
    // final String key1 = MaltcmsTools.getKey(ref, query, extension);
    // final String key2 = MaltcmsTools.getKey(query, ref, extension);
    // if (MaltcmsTools.hasPairwiseAlignment(ref, query, extension)) {
    // MaltcmsTools.log.warn(
    // "PairwiseAlignment of {} and {} already contained in map!",
    // ref.getAbsolutePath(), query.getAbsolutePath());
    // }
    // MaltcmsTools.pairwiseAlignments.put(key1, pa);
    // MaltcmsTools.pairwiseAlignments.put(key2, pa);
    // }
    /**
     * Calculates the normalized index of massValues, by subtracting minmz and
     * dividing by maxmz-minmz.
     *
     * @param massValues mass value to bin
     * @param minmz minimum massValues of all mass values over all
     * chromatograms, can be zero
     * @param maxmz maximum massValues of all mass values over all chromatograms
     * @param resolution multiplication factor to scale the mass range
     * @return an integer bin for massValues, starting at 0
     */
    public static int binMZ(final double mz, final double minmz,
            final double maxmz, final double resolution) {
        final double v = ((mz - minmz) / (maxmz - minmz));
        // Logging.getLogger(MaltcmsTools.class).debug("massValues: {}, v: {}",massValues,v);
        final double scaledMz = mz * resolution;
        final double scaledMinMz = minmz * resolution;
        // Logging.getLogger(MaltcmsTools.class).debug("smz: {}",scaledMz);
        // v = v*massValues;
        // Logging.getLogger(MaltcmsTools.class).info("v*massValues: {}",v);
        // v = v*resolution;
        // Logging.getLogger(MaltcmsTools.class).info("v*massValues*resolution: {}",v);
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
        // "massValues: {}, minmz: {}, resolution: {}, binnedMZ: {}, roundBinnedMZ: {}, rescaledMZ: {}"
        // ,new Object[]{massValues,minmz,resolution,v,rval,y});
        // return Math.max((int)z,0);//(int)y;
        return (int) z;
    }

    /**
     * Use standard rounding.
     *
     * @param massValues
     * @return
     */
    public static int binMZDefault(final double mz) {
        return (int) Math.rint(mz);
        // binMZFloor produces a high number of artifacts (wrongly binned
        // intensities),
        // so differences between chromatograms will tend to be higher
        // return binMZFloor(massValues);
    }

    /**
     *
     * @param massValues
     * @return
     */
    public static int binMZFloor(final double mz) {
        return (int) Math.floor(mz);
    }

    /**
     * Use rounding according to Heiko's distribution analysis.
     *
     * @param massValues
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

    /**
     *
     * @param denseArrays
     * @param binary_mass_values
     * @param mass_values
     * @param intensity_values
     * @param scan_index
     * @param maskedMasses
     * @return
     */
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
        final List<Array> intens = denseArrays.getChild(intensity_values).getIndexedArray();
        final List<Array> masses = denseArrays.getChild(mass_values).getIndexedArray();
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
     * flattened masses and intensities within the second tuple
     */
    public static Tuple2D<Array, Tuple2D<Array, Array>> chrom2crs(
            final Chromatogram1D chrom) {
        final List<Array> intensities = chrom.getIntensities();
        final List<Array> masses = chrom.getMasses();
        EvalTools.eqI(intensities.size(), masses.size(), chrom);
        final ArrayInt.D1 indices = new ArrayInt.D1(masses.size());
        final int size = cross.datastructures.tools.ArrayTools.getSizeForFlattenedArrays(masses);
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

    /**
     *
     * @param intensities
     * @param eics
     * @return
     */
    public static List<Array> copyEics(final List<Array> intensities,
            final Integer[] eics) {
        final ArrayList<Array> al = new ArrayList<Array>(intensities.size());
        for (int i = 0; i < intensities.size(); i++) {
            final Array aorig = intensities.get(i);
            final Array a = Array.factory(aorig.getElementType(),
                    new int[]{eics.length});
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

    /**
     *
     * @param file
     * @param masses
     * @param intensities
     * @return
     */
    public static Tuple2D<VariableFragment, Tuple2D<VariableFragment, VariableFragment>> createFlattenedArrays(
            final IFileFragment file, final List<Array> masses,
            final List<Array> intensities) {
        MaltcmsTools.log.debug("Creating flattened arrays");
        EvalTools.notNull(new Object[]{file, masses, intensities},
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
        final VariableFragment new_mz = new VariableFragment(file, Factory.getInstance().getConfiguration().getString(
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

    /**
     *
     * @param file
     * @param varname
     * @param values
     * @return
     */
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
        EvalTools.notNull(new Object[]{fileFragment, a}, MaltcmsTools.class);
        final IVariableFragment intensity = new VariableFragment(fileFragment,
                Factory.getInstance().getConfiguration().getString(
                "var.total_intensity", "total_intensity"));
        intensity.setArray(a);
    }

    /**
     *
     * @param file
     * @param scans
     * @return
     */
    public static Tuple2D<IVariableFragment, IVariableFragment> createMinMaxMassValueArrays(
            final IFileFragment file, final List<Array> scans) {
        EvalTools.notNull(new Object[]{file, scans}, MaltcmsTools.class);
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

    /**
     *
     * @param index
     * @param values
     * @param minindex
     * @param maxindex
     * @param nbins
     * @param massBinResolution
     * @return
     */
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

    /**
     *
     * @param ff
     * @param mmin
     * @param mmax
     * @param fallback
     * @return
     */
    public static Tuple2D<Double, Double> findGlobalMinMax(
            final TupleND<IFileFragment> ff, final String mmin,
            final String mmax, final String fallback) {
        Double min = Double.MAX_VALUE;
        Double max = Double.MIN_VALUE;
        final boolean ignoreMinMaxMassArrays = Factory.getInstance().getConfiguration().getBoolean(
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
                min = Math.min(min, mm.min);
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

    /**
     *
     * @param ff1
     * @param ff2
     * @return
     */
    public static Tuple2D<List<IAnchor>, List<IAnchor>> getAnchors(
            final IFileFragment ff1, final IFileFragment ff2) {
        final List<IAnchor> l1 = MaltcmsTools.prepareAnchors(ff1);
        final List<IAnchor> l2 = MaltcmsTools.prepareAnchors(ff2);
        MaltcmsTools.log.info("Number of anchors lhs: {}, rhs: {}", l1.size(),
                l2.size());
        final List<String> l2s = new ArrayList<String>(l2.size());
        MaltcmsTools.log.debug("Anchors in {}", ff1.getName());
        for (final IAnchor ia2 : l2) {
            MaltcmsTools.log.debug("{} at {}", ia2.getName(), ia2.getScanIndex());
            l2s.add(ia2.getName());
        }
        final TreeSet<String> s = new TreeSet<String>();
        MaltcmsTools.log.debug("Anchors in {}", ff2.getName());
        for (final IAnchor ia1 : l1) {
            MaltcmsTools.log.debug("{} at {}", ia1.getName(), ia1.getScanIndex());
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

    /**
     *
     * @param iff
     * @param i
     * @return
     */
    public static Tuple2D<Array, Array> getBinnedMS(final IFileFragment iff,
            final int i) {
        log.info("Reading scan {}", i);
        final String sindex = Factory.getInstance().getConfiguration().getString("var.binned_scan_index", "binned_scan_index");
        final Array masses = FragmentTools.getIndexed(iff, Factory.getInstance().getConfiguration().getString(
                "var.binned_mass_values", "binned_mass_values"),
                sindex, i);
        final Array intensities = FragmentTools.getIndexed(iff, Factory.getInstance().getConfiguration().getString(
                "var.binned_intensity_values",
                "binned_intensity_values"), sindex, i);
        return new Tuple2D<Array, Array>(masses, intensities);
    }

    /**
     *
     * @param ff
     * @return
     */
    public static Tuple2D<List<Array>, List<Array>> getBinnedMZIs(
            final IFileFragment ff) {
        final String mz = Factory.getInstance().getConfiguration().getString(
                "var.binned_mass_values", "binned_mass_values");
        final String intens = Factory.getInstance().getConfiguration().getString("var.binned_intensity_values",
                "binned_intensity_values");
        final String scan_index = Factory.getInstance().getConfiguration().getString("var.binned_scan_index", "binned_scan_index");
        final IVariableFragment index = ff.getChild(scan_index);
        final IVariableFragment mzV = ff.getChild(mz);
        mzV.setIndex(index);
        final List<Array> mzs = mzV.getIndexedArray();
        final IVariableFragment iV = ff.getChild(intens);
        iV.setIndex(index);
        final List<Array> is = iV.getIndexedArray();

        return new Tuple2D<List<Array>, List<Array>>(mzs, is);
    }

    // public static IFileFragment getDenseArrayFragmentFor(final IFileFragment
    // iff) {
    // return MaltcmsTools.getDenseArrayFragmentFor(iff.getName());
    // }
    // public static IFileFragment getDenseArrayFragmentFor(
    // final String fragmentName) {
    // if (MaltcmsTools.denseArrays.containsKey(fragmentName)) {
    // return MaltcmsTools.denseArrays.get(fragmentName);
    // }
    // return null;
    // }
    /**
     *
     * @param f
     * @param eicStart
     * @param eicStop
     * @param normalize
     * @param keepMaxInBin
     * @param start
     * @param nscans
     * @return
     */
    public static Array getEIC(final IFileFragment f, final double eicStart,
            final double eicStop, final boolean normalize,
            final boolean keepMaxInBin, int start, int nscans) {
        MaltcmsTools.log.info(
                "Retrieving EIC from {}, scan start {}, stop {}, mz start {}, stop{}",
                new Object[]{f.getAbsolutePath(), start,
                    start + nscans - 1, eicStart, eicStop});
        final IVariableFragment index1 = f.getChild(Factory.getInstance().getConfiguration().getString("var.scan_index", "scan_index"));
        final String massVar = Factory.getInstance().getConfiguration().getString("var.mass_values", "mass_values");
        final String intensVar = Factory.getInstance().getConfiguration().getString("var.intensity_values", "intensity_values");
        if (f.getChild(massVar).getIndex() == null) {
            f.getChild(massVar).setIndex(index1);
            f.getChild(intensVar).setIndex(index1);
        }
        final List<Array> intens1 = f.getChild(intensVar).getIndexedArray();
        final List<Array> mass1 = f.getChild(massVar).getIndexedArray();
        final ArrayDouble.D1 eic = new ArrayDouble.D1(nscans);
        final ArrayInt.D1 eicbinCnt = new ArrayInt.D1(nscans);
        for (int i = 0; i < nscans; i++) {
            final Array masses = mass1.get(start + i);
            final Index mind = masses.getIndex();
            final Array intensities = intens1.get(start + i);
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

    /**
     *
     * @param f
     * @param eicStart
     * @param eicStop
     * @param normalize
     * @param keepMaxInBin
     * @return
     */
    public static Array getEIC(final IFileFragment f, final double eicStart,
            final double eicStop, final boolean normalize,
            final boolean keepMaxInBin) {
        final IVariableFragment index1 = f.getChild(Factory.getInstance().getConfiguration().getString("var.scan_index", "scan_index"));
        Array si = index1.getArray();
        return getEIC(f, eicStart, eicStop, normalize, keepMaxInBin, 0, si.getShape()[0]);
    }

    /**
     *
     * @param pwdFile
     * @param varname
     * @return
     */
    public static TupleND<IFileFragment> getFileFragmentsFromStringArray(
            final IFileFragment pwdFile, final String varname) {
        final Collection<String> s = FragmentTools.getStringArray(pwdFile,
                varname);
        final ArrayList<IFileFragment> al = new ArrayList<IFileFragment>();
        for (final String sn : s) {
            if (FileFragment.hasFragment(sn)) {
                al.add(FileFragment.getFragment(sn));
            } else {
                al.add(Factory.getInstance().getFileFragmentFactory().create(
                        new File(sn)));
            }
        }
        final TupleND<IFileFragment> t = new TupleND<IFileFragment>(al);
        return t;
    }

    /**
     * Returns the index of the element with highest value in array intens.
     * Applies a filter array before, which sets all ranks to -1 if that index
     * is masked. Then the first index with rank >=0 is searched and returned.
     *
     * @param intens
     * @param maskedMassesIndices
     * @return
     */
    public static int getIndexOfMaxIntensity(final Array intens,
            final int[] maskedMassesIndices) {
        final int[] ranksByIntensity = MaltcmsTools.ranksByIntensity(intens);
        for (int i = 0; i < maskedMassesIndices.length; i++) {
            ranksByIntensity[maskedMassesIndices[i]] = -1;
        }
        for (int j = 0; j < ranksByIntensity.length; j++) {
            if (ranksByIntensity[j] >= 0) {
                return ranksByIntensity[j];
            }
        }
        return -1;
    }

    /**
     *
     * @param intens
     * @return
     */
    public static int getIndexOfMaxIntensity(final Array intens) {
        final int[] ranksByIntensity = MaltcmsTools.ranksByIntensity(intens);
        return ranksByIntensity[0];
    }

    /**
     *
     * @param intens
     * @param k
     * @return
     */
    public static int[] getTopKIndicesOfMaxIntensity(final Array intens,
            final int k) {
        final int[] ranksByIntensity = MaltcmsTools.ranksByIntensity(intens);
        return Arrays.copyOf(ranksByIntensity, k);
    }

    // private static String getKey(final IFileFragment ref,
    // final IFileFragment query, final String extension) {
    // // final String key = ref.getAbsolutePath() "-"
    // // + query.getAbsolutePath() + extension;
    // final String key = ref.getName() + "-" + query.getName() + extension;
    // return key;
    // }
    /**
     * Returns mass of signal with maximum intensity.
     *
     * @param masses
     * @param intens
     * @return
     */
    public static double getMaxMass(final Array masses, final Array intens) {
        final int[] ranksByIntensity = MaltcmsTools.ranksByIntensity(intens);
        if (ranksByIntensity.length > 0) {
            final Index idx = masses.getIndex();
            MaltcmsTools.log.debug("Rank 0: {}", ranksByIntensity[0]);
            final double maxMass = masses.getDouble(idx.set(ranksByIntensity[0]));
            return maxMass;
        }
        return Double.NaN;
    }

    /**
     * Determine, whether the mass with maximum intensity within this scan is
     * the same (within epsilon) of maxMass. Returns -1 if no mass was found
     * within epsilon and otherwise the index of the mass within the scan.
     *
     * @param masses
     * @param intens
     * @param maxMass
     * @param epsilon
     * @return
     */
    public static List<Integer> isMaxMass(final Array masses,
            final Array intens, final double maxMass, final double epsilon) {
        final double[] m = (double[]) masses.copyTo1DJavaArray();
        final int i = Arrays.binarySearch(m, maxMass);
        // if left neighbor is > epsilon away, proceed to neighbor on right
        // if right neighbor is > epsilon away -> no matching mass found
        // else while next neighbor to right is <= epsilon away, continue

        // exact match
        if (i >= 0) {
            log.debug("Exact mass match at {}: {}", m[i], i);
            return Arrays.asList(Integer.valueOf(i));
        } else {// check for insertion position
            log.debug("Insertion at {}", i);
            final int idx = (-i) - 1;
            int lcnt = idx - 1;
            final ArrayList<Integer> al = new ArrayList<Integer>();
            // extend to left
            while (lcnt >= 0) {
                if (Math.abs(m[lcnt] - maxMass) <= epsilon) {
                    log.debug("mass at index {} within epsilon of {}", lcnt,
                            maxMass);
                    lcnt--;
                } else {
                    break;
                }
            }
            int rcnt = idx;
            // extend to right
            while (rcnt < m.length) {
                if (Math.abs(m[rcnt] - maxMass) <= epsilon) {
                    log.debug("mass at index {} within epsilon of {}", rcnt,
                            maxMass);
                    rcnt++;
                } else {
                    break;
                }
            }
            for (int j = lcnt + 1; j < rcnt; j++) {
                log.debug("Adding mass at index {}", j);
                al.add(j);
            }
            return al;
        }
    }

    /**
     * Returns maximum intensity in scan.
     *
     * @param intens
     * @return
     */
    public static double getMaxMassIntensity(final Array intens) {
        final Index idx = intens.getIndex();
        MaltcmsTools.log.debug("Size of intens {}", intens.getShape()[0]);
        final double maxIntens = intens.getDouble(idx.set(MaltcmsTools.getIndexOfMaxIntensity(intens, new int[]{})));
        return maxIntens;
    }

    /**
     * Returns a list of pairs, where the first element corresponds to the
     * intensity, and the second to the associated mass channel. The elements in
     * the list are ordered increasingly by intensity. The mass channel with the
     * highest intensity can thus be accessed at l.get(l.size()-1).
     *
     * apex is from 0...nscans-1
     *
     * @param f
     * @param apex
     * @param start
     * @param nscans
     * @param massResolution
     * @return
     */
    public static List<Tuple2D<Double, Double>> getPeakingMasses(
            final IFileFragment f, int apex, int start, int nscans,
            double massResolution) {
        Tuple2D<Double, Double> tple = getMinMaxMassRange(f);
        int bins = getNumberOfIntegerMassBins(tple.getFirst(),
                tple.getSecond(), massResolution);
        double lmass = tple.getFirst();
        ArrayList<Tuple2D<Double, Double>> peakingMasses = new ArrayList<Tuple2D<Double, Double>>();
        for (int i = 0; i < bins; i++) {
            Array arr = MaltcmsTools.getEIC(f, lmass, lmass + massResolution,
                    false, false, start, nscans);
            // scan range is from
            // start (inclusive) to start+nscans (exclusive)
            // apex should be at position
            // apex-start in arr since apex >= start and apex < start+nscans
            TopHatFilter mhwf = new TopHatFilter();
            mhwf.setWindow(nscans / 2);
            Array filtered = mhwf.apply(arr);
            double maxVal = getMaxMassIntensity(arr);
            int maxIdx = getIndexOfMaxIntensity(filtered);
            if (maxIdx + start == apex) {
                log.info("Found peaking mass at index {}", maxIdx);
                peakingMasses.add(new Tuple2D<Double, Double>(maxVal, lmass));
            }
            lmass += massResolution;
        }
        Collections.sort(peakingMasses,
                new Comparator<Tuple2D<Double, Double>>() {
                    @Override
                    public int compare(Tuple2D<Double, Double> o1,
                            Tuple2D<Double, Double> o2) {
                        if (o1.getFirst() < o2.getFirst()) {
                            return -1;
                        } else if (o1.getFirst() > o2.getFirst()) {
                            return 1;
                        }
                        return 0;
                    }
                });
        return peakingMasses;
    }

    /**
     *
     * @param refMass
     * @param testMass
     * @param epsilon
     * @return
     */
    public static boolean isMassWithinEpsilon(final double refMass,
            final double testMass, final double epsilon) {
        if (Math.abs(refMass - testMass) <= epsilon) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param intens
     * @param epsilon
     * @return
     */
    public static double getMaxMassIntensity(final Array intens,
            final double epsilon) {
        final Index idx = intens.getIndex();
        final double maxIntens = intens.getDouble(idx.set(MaltcmsTools.getIndexOfMaxIntensity(intens)));
        return maxIntens;
    }

    /**
     *
     * @param f
     * @return
     */
    public static Tuple2D<Double, Double> getMinMaxMassRange(
            final IFileFragment f) {
        EvalTools.notNull(f, MaltcmsTools.class);
        MinMax mm = null;
        try {
            log.info("Using masses in mass_range_min, mass_range_max");
            Array minMass = f.getChild(
                    Factory.getInstance().getConfiguration().getString(
                    "var.mass_range_min", "mass_range_min")).getArray();
            Array maxMass = f.getChild(
                    Factory.getInstance().getConfiguration().getString(
                    "var.mass_range_max", "mass_range_max")).getArray();
            mm = new MAMath.MinMax(MAMath.getMinimum(minMass), MAMath.getMaximum(maxMass));
        } catch (ResourceNotAvailableException rnae) {
            log.info("Inferring mass range from mass_values");
            final String mmin = Factory.getInstance().getConfiguration().getString("var.mass_values", "mass_values");
            final Array ref_min = f.getChild(mmin).getArray();
            mm = MAMath.getMinMax(ref_min);
        }

        return new Tuple2D<Double, Double>(mm.min, mm.max);
    }

    /**
     *
     * @param fragments
     * @return
     */
    public static Tuple2D<Double, Double> getMinMaxMassRange(Collection<IFileFragment> fragments) {
        double min_mass = Double.POSITIVE_INFINITY;
        double max_mass = Double.NEGATIVE_INFINITY;
        for (final IFileFragment t : fragments) {
            final Tuple2D<Double, Double> tple = MaltcmsTools.getMinMaxMassRange(t);
            min_mass = Math.min(tple.getFirst(), min_mass);
            max_mass = Math.max(tple.getSecond(), max_mass);
        }
        return new Tuple2D<Double, Double>(min_mass, max_mass);
    }

    /**
     *
     * @param reference
     * @param query
     * @return
     */
    public static Tuple2D<Double, Double> getMinMaxMassRange(
            final IFileFragment reference, final IFileFragment query) {
        EvalTools.notNull(new Object[]{reference, query}, MaltcmsTools.class);
        final Tuple2D<Double, Double> a = MaltcmsTools.getMinMaxMassRange(reference);
        final Tuple2D<Double, Double> b = MaltcmsTools.getMinMaxMassRange(query);
        return new Tuple2D<Double, Double>(
                Math.min(a.getFirst(), b.getFirst()), Math.max(a.getSecond(), b.getSecond()));
    }

    /**
     *
     * @param iff
     * @param i
     * @return
     */
    public static Tuple2D<Array, Array> getMS(final IFileFragment iff,
            final int i) {
        log.info("Reading scan {}", i);
        final String sindex = Factory.getInstance().getConfiguration().getString("var.scan_index", "scan_index");
        final Array masses = FragmentTools.getIndexed(iff, Factory.getInstance().getConfiguration().getString("var.mass_values",
                "mass_values"), sindex, i);
        final Array intensities = FragmentTools.getIndexed(iff, Factory.getInstance().getConfiguration().getString(
                "var.intensity_values", "intensity_values"), sindex, i);
        return new Tuple2D<Array, Array>(masses, intensities);
    }

    /**
     *
     * @param ff
     * @return
     */
    public static Tuple2D<List<Array>, List<Array>> getMZIs(
            final IFileFragment ff) {
        final String mz = Factory.getInstance().getConfiguration().getString(
                "var.mass_values", "mass_values");
        final String intens = Factory.getInstance().getConfiguration().getString("var.intensity_values", "intensity_values");
        final String scan_index = Factory.getInstance().getConfiguration().getString("var.scan_index", "scan_index");
        final IVariableFragment index = ff.getChild(scan_index);
        final IVariableFragment mzV = ff.getChild(mz);
        mzV.setIndex(index);
        final List<Array> mzs = mzV.getIndexedArray();
        final IVariableFragment iV = ff.getChild(intens);
        iV.setIndex(index);
        final List<Array> is = iV.getIndexedArray();

        return new Tuple2D<List<Array>, List<Array>>(mzs, is);
    }

    /**
     *
     * @param minMass
     * @param maxMass
     * @param resolution
     * @return
     */
    public static int getNumberOfIntegerMassBins(final double minMass,
            final double maxMass, final double resolution) {
        return (int) Math.ceil(((Math.ceil(maxMass) - Math.floor(minMass)) + 1)
                * resolution);
    }

    /**
     *
     * @param iff
     * @return
     */
    public static int getNumberOfBinnedScans(final IFileFragment iff) {
        final IVariableFragment scan_index = iff.getChild(Factory.getInstance().getConfiguration().getString("var.binned_scan_index",
                "binned_scan_index"));
        log.info("Number of scans in file {}:{}", iff, scan_index.getArray().getShape()[0]);
        return scan_index.getArray().getShape()[0];
    }

    /**
     *
     * @param iff
     * @return
     */
    public static int getNumberOfScans(final IFileFragment iff) {
        final IVariableFragment scan_index = iff.getChild(Factory.getInstance().getConfiguration().getString("var.scan_index", "scan_index"));
        return scan_index.getArray().getShape()[0];
    }

    // public static IFileFragment getPairwiseAlignment(final IFileFragment ref,
    // final IFileFragment query) {
    // // FIXME: replace "" by ?
    // return MaltcmsTools.getPairwiseAlignment(ref, query, "");
    // }
    /**
     *
     * @param f
     * @param pwdExtension
     * @return
     * @throws ResourceNotAvailableException
     */
    public static IFileFragment getPairwiseDistanceFragment(
            final IFileFragment f, final String pwdExtension)
            throws ResourceNotAvailableException {
        IVariableFragment matrix = f.getChild(Factory.getInstance().getConfiguration().getString("var.pairwise_distance_matrix",
                "pairwise_distance_matrix"));
        //System.out.println(matrix.getArray());
        return matrix.getParent();
    }

    /**
     *
     * @param t
     * @return
     * @throws ResourceNotAvailableException
     */
    public static IFileFragment getPairwiseDistanceFragment(
            final TupleND<IFileFragment> t)
            throws ResourceNotAvailableException {
        return getPairwiseDistanceFragment(t, "");
    }

    /**
     *
     * @param t
     * @param pwdExtension
     * @return
     * @throws ResourceNotAvailableException
     */
    public static IFileFragment getPairwiseDistanceFragment(
            final TupleND<IFileFragment> t, final String pwdExtension)
            throws ResourceNotAvailableException {
        EvalTools.neqI(0, t.size(), MaltcmsTools.class);
        return MaltcmsTools.getPairwiseDistanceFragment(t.get(0), pwdExtension);
    }

    /**
     *
     * @param pwd
     * @return
     */
    public static TupleND<IFileFragment> getAlignmentsFromFragment(
            final IFileFragment pwd) {
        final String varname = Factory.getInstance().getConfiguration().getString("var.pairwise_distance_alignment_names",
                "pairwise_distance_alignment_names");
        return getFileFragmentsFromStringArray(pwd, varname);
    }

    /**
     *
     * @param pwdf
     * @return
     */
    public static TupleND<IFileFragment> getPairwiseAlignments(
            final IFileFragment pwdf) {
        final String varname = Factory.getInstance().getConfiguration().getString("var.pairwise_distance_alignment_names",
                "pairwise_distance_alignment_names");
        final TupleND<IFileFragment> t = MaltcmsTools.getFileFragmentsFromStringArray(pwdf, varname);
        return t;
    }

    /**
     *
     * @param pwdf
     * @param ref
     * @param query
     * @return
     */
    public static IFileFragment getPairwiseAlignment(final IFileFragment pwdf,
            final IFileFragment ref, final IFileFragment query) {
        return MaltcmsTools.getPairwiseAlignment(MaltcmsTools.getPairwiseAlignments(pwdf), ref, query);
    }

    /**
     *
     * @param pwalignments
     * @param ref
     * @param query
     * @return
     */
    public static IFileFragment getPairwiseAlignment(
            final TupleND<IFileFragment> pwalignments, final IFileFragment ref,
            final IFileFragment query) {
        for (IFileFragment iff : pwalignments) {
            IFileFragment lhs = FragmentTools.getLHSFile(iff);
            IFileFragment rhs = FragmentTools.getRHSFile(iff);
            if ((lhs.getName().equals(ref.getName()) && rhs.getName().equals(
                    query.getName()))
                    || (lhs.getName().equals(query.getName()) && rhs.getName().equals(ref.getName()))) {
                return iff;
            }
        }
        throw new ResourceNotAvailableException(
                "Could not retrieve a matching alignment for " + ref.getName()
                + " and " + query.getName() + "!");
    }

    /**
     *
     * @param iff
     * @param scan
     * @return
     */
    public static double getScanAcquisitionTime(final IFileFragment iff,
            final int scan) {
        final String sat = Factory.getInstance().getConfiguration().getString(
                "var.scan_acquisition_time", "scan_acquisition_time");
        final IVariableFragment satvar = iff.getChild(sat);
        final Array a = satvar.getArray();
        final Index ai = a.getIndex();
        final double d = a.getDouble(ai.set(scan));
        return d;
    }

    /**
     *
     * @param f
     * @return
     */
    public static Array getTIC(final IFileFragment f) {
        EvalTools.notNull(f, MaltcmsTools.class);
        final String tics = Factory.getInstance().getConfiguration().getString(
                "var.total_intensity", "total_intensity");
        IVariableFragment v;
        v = f.getChild(tics);
        return v.getArray();
    }

    /**
     *
     * @param iff
     * @param scan
     * @return
     */
    public static double getTIC(final IFileFragment iff, final int scan) {
        final String sat = Factory.getInstance().getConfiguration().getString(
                "var.total_intensity", "total_intensity");
        final IVariableFragment satvar = iff.getChild(sat);
        final Array a = satvar.getArray();
        final Index ai = a.getIndex();
        final double d = a.getDouble(ai.set(scan));
        return d;
    }

    /**
     *
     * @param ff
     * @return
     */
    public static List<Tuple2DI> getWarpPath(final IFileFragment ff) {
        final String wpi = Factory.getInstance().getConfiguration().getString(
                "var.warp_path_i", "warp_path_i");
        final String wpj = Factory.getInstance().getConfiguration().getString(
                "var.warp_path_j", "warp_path_j");
        final IVariableFragment wpiV = ff.getChild(wpi);
        final IVariableFragment wpjV = ff.getChild(wpj);
        final Array wpiA = wpiV.getArray();
        final Array wpjA = wpjV.getArray();
        EvalTools.notNull(new Object[]{wpiA, wpjA}, MaltcmsTools.class);
        return PathTools.fromArrays(wpiA, wpjA);
    }

    /**
     *
     * @param eics1
     * @param eics2
     * @return
     */
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
        final Integer[] arr = hs1.toArray(new Integer[]{});
        // sort ascending
        Arrays.sort(arr);
        return arr;
    }

    /**
     *
     * @param l
     * @return
     */
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

    /**
     *
     * @param ff
     * @return
     */
    public static List<IAnchor> prepareAnchors(final IFileFragment ff) {
        EvalTools.notNull(ff, MaltcmsTools.class);
        final ArrayList<IAnchor> al = new ArrayList<IAnchor>();
        final String ri_names = Factory.getInstance().getConfiguration().getString("var.anchors.retention_index_names",
                "retention_index_names");
        final String ri_times = Factory.getInstance().getConfiguration().getString("var.anchors.retention_times", "retention_times");
        final String ri_indices = Factory.getInstance().getConfiguration().getString("var.anchors.retention_indices", "retention_indices");
        final String ri_scans = Factory.getInstance().getConfiguration().getString("var.anchors.retention_scans", "retention_scans");

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
                MaltcmsTools.log.debug("Parent of riNames: {}", ri_namesV.getParent());
            }
        } catch (final ResourceNotAvailableException e) {
            MaltcmsTools.log.debug(e.getLocalizedMessage());
        }

        try {
            final IVariableFragment ri_timesV = ff.getChild(ri_times);
            ri_timesA = ri_timesV.getArray();
            if (!ff.getAbsolutePath().equals(
                    ri_timesV.getParent().getAbsolutePath())) {
                MaltcmsTools.log.debug("Parent of riTimes: {}", ri_timesV.getParent());
            }
        } catch (final ResourceNotAvailableException e) {
            MaltcmsTools.log.debug(e.getLocalizedMessage());
        }

        try {
            final IVariableFragment ri_indicesV = ff.getChild(ri_indices);
            ri_indicesA = ri_indicesV.getArray();
            if (!ff.getAbsolutePath().equals(
                    ri_indicesV.getParent().getAbsolutePath())) {
                MaltcmsTools.log.debug("Parent of riIndices: {}", ri_indicesV.getParent());
            }
        } catch (final ResourceNotAvailableException e) {
            MaltcmsTools.log.debug(e.getLocalizedMessage());
        }

        try {
            final IVariableFragment ri_scansV = ff.getChild(ri_scans);
            ri_scansA = ri_scansV.getArray();
            if (!ff.getAbsolutePath().equals(
                    ri_scansV.getParent().getAbsolutePath())) {
                MaltcmsTools.log.debug("Parent of riScans: {}", ri_scansV.getParent());
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
                    } else {// restore time from scan_acquisition_time
                        Array sat = ff.getChild("scan_acquisition_time").getArray();
                        Index sati = sat.getIndex();
                        ri.setRetentionTime(sat.getDouble(sati.set(scan)));
                    }

                    // Retention index is optional
                    if (ri_indicesA != null) {
                        final double index = ri_indicesA.getDouble(indicesi.set(i));
                        if (index > -1) {
                            ri.setRetentionIndex(index);
                        }
                    }
                    ri.setScanIndex(scan);
                    al.add(ri);
                } else {
                    MaltcmsTools.log.debug("Skipping anchor due to empty name!");
                }
                i++;
            }
        } catch (final ResourceNotAvailableException e) {
            MaltcmsTools.log.warn(e.getLocalizedMessage());
        }
        if (al.isEmpty()) {
            MaltcmsTools.log.warn("Could not prepare anchors for file {}", ff.getName());
        }
        return al;
    }

    /**
     *
     * @param ff
     * @param index_name
     * @param mz_name
     * @param intens_name
     * @param mass_range_min_name
     * @param mass_range_max_name
     * @return
     */
    public static List<Array> prepareArraysMZIasList(final IFileFragment ff,
            final String index_name, final String mz_name,
            final String intens_name, final String mass_range_min_name,
            final String mass_range_max_name) {

        MaltcmsTools.log.debug("Creating variables for file {}!", ff.getAbsolutePath());
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
        } catch (final ResourceNotAvailableException rnae) {
            MaltcmsTools.log.warn("Could not find mass range variables, checking all masses!");
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
        final double massResolution = Factory.getInstance().getConfiguration().getDouble("dense_arrays.massBinResolution", 1.0d);
        return SparseTools.createAsList(mza, intena, (min), (max), MaltcmsTools.getNumberOfIntegerMassBins(min, max, massResolution),
                massResolution);
    }

    /**
     *
     * @param input
     * @param output
     * @param scan_index
     * @param mass_values
     * @param intensity_values
     * @param binned_scan_index
     * @param binned_mass_values
     * @param binned_intensity_values
     * @param min_mass
     * @param max_mass
     * @param outputDir
     * @return
     */
    public static IFileFragment prepareDenseArraysMZI(final IFileFragment input, final IFileFragment output,
            final String scan_index, final String mass_values,
            final String intensity_values, final String binned_scan_index,
            final String binned_mass_values,
            final String binned_intensity_values, final Double min_mass,
            final Double max_mass, final File outputDir) {
        MaltcmsTools.log.debug(
                "Created target IFileFragment {}, with parent {}", output.getAbsolutePath(), input.getAbsolutePath());
        // Retrieve original scan index
        final IVariableFragment scanIndex = input.getChild(scan_index);
        final Array scanIndexArray = scanIndex.getArray();
        EvalTools.notNull(scanIndexArray, MaltcmsTools.class);
        // Retrieve original mass_values
        final IVariableFragment massValues = input.getChild(mass_values, true);
        // Manually set index
        massValues.setIndex(scanIndex);
        // Retrieve original intensity_values
        final IVariableFragment intensityValues = input.getChild(intensity_values, true);
        // Manually set index
        intensityValues.setIndex(scanIndex);
        // Read mass_values with index
        final List<Array> massValuesList = massValues.getIndexedArray();
        // Read intensity_values with index
        final List<Array> intensityValuesList = intensityValues.getIndexedArray();

        VariableFragment binnedScanIndex = new VariableFragment(output, binned_scan_index);
        VariableFragment binnedMassValues = new VariableFragment(output, binned_mass_values);
        binnedMassValues.setIndex(binnedScanIndex);
        VariableFragment binnedIntensityValues = new VariableFragment(output, binned_intensity_values);
        binnedIntensityValues.setIndex(binnedScanIndex);
        // Check, that original indexed arrays are present
        EvalTools.notNull(massValuesList, MaltcmsTools.class);
        EvalTools.notNull(intensityValuesList, MaltcmsTools.class);
        // Set fillvalue for missing intensities, e.g. if a mass channel was
        // not recorded for a given bin
        final double fillvalue = Factory.getInstance().getConfiguration().getDouble("intensity.missing.value", 0.0d);
        // Number of bins, currently only resolution of 1 m/z
        // TODO integrate resolution
        final double massBinResolution = Factory.getInstance().getConfiguration().getDouble("dense_arrays.massBinResolution",
                1.0d);
        final int size = MaltcmsTools.getNumberOfIntegerMassBins(min_mass,
                max_mass, massBinResolution);
        // Check, that size is at least 1 and at most largest integer
        EvalTools.inRangeI(1, Integer.MAX_VALUE, size, MaltcmsTools.class);
        MaltcmsTools.log.debug("Creating dense arrays with " + size
                + " elements and resolution of {}!", massBinResolution);
        // Create new index array with size = number of scans
        final ArrayInt.D1 binnedScanIndexArray = new ArrayInt.D1(massValuesList.size());
        int points = 0;
        int scans = 0;
        List<Array> binnedMassValuesList = binnedMassValues.getIndexedArray();
        List<Array> binnedIntensityValuesList = binnedIntensityValues.getIndexedArray();
        // For all scans
        for (int i = 0; i < massValuesList.size(); i++) {
            MaltcmsTools.log.debug("Processing scan {}/{}", i + 1, massValuesList.size());
            // Create a binned massValues array
            final Array indx = Array.factory(massValues.getDataType(),
                    new int[]{size});
            // Create a binned intensity array
            final Array vals = Array.factory(massValues.getDataType(),
                    new int[]{size});
            // Fill arrays with values, massValues starts at min_mass, goes until
            // max_mass, with unit increment
            ArrayTools.createDenseArray(massValuesList.get(i), intensityValuesList.get(i),
                    new Tuple2D<Array, Array>(indx, vals), ((int) Math.floor(min_mass)), ((int) Math.ceil(max_mass)),
                    size, massBinResolution, fillvalue);
            
            binnedMassValuesList.add(indx);
            binnedIntensityValuesList.add(vals);
            binnedScanIndexArray.set(i, i * size);
            points+=size;
            scans++;
        }
        Dimension binnedScanNumber = new Dimension("binned_scan_number", scans);
        binnedScanIndex.setDimensions(new Dimension[]{binnedScanNumber});
        Dimension binnedPointNumber = new Dimension("binned_point_number", points);
        binnedIntensityValues.setDimensions(new Dimension[]{binnedPointNumber});
        binnedMassValues.setDimensions(new Dimension[]{binnedPointNumber});
        
        binnedScanIndex.setArray(binnedScanIndexArray);
        
        log.info("Children of {}",output.getAbsolutePath());
        for(IVariableFragment ivf:output.getImmediateChildren()) {
            log.info("Child: {}",ivf.getName());
        }
        // index
        EvalTools.notNull(binnedScanIndexArray, MaltcmsTools.class);
        EvalTools.notNull(output, MaltcmsTools.class);
        log.info("Output: {}", output);
        return output;
    }

    /**
     *
     * @param input
     * @param output
     * @param scan_index
     * @param mass_values
     * @param intensity_values
     * @param binned_scan_index
     * @param binned_mass_values
     * @param binned_intensity_values
     * @param min_mass
     * @param max_mass
     * @param outputDir
     * @return
     */
//    public static IFileFragment prepareDenseArraysMZI(final IFileFragment input, final IFileFragment output,
//            final String scan_index, final String mass_values,
//            final String intensity_values, final String binned_scan_index,
//            final String binned_mass_values,
//            final String binned_intensity_values, final Double min_mass,
//            final Double max_mass, final File outputDir) {
//        MaltcmsTools.log.debug(
//                "Created target IFileFragment {}, with parent {}", output.getAbsolutePath(), input.getAbsolutePath());
//        // Retrieve original scan index
//        final IVariableFragment scanIndex = input.getChild(scan_index);
//        final Array scanIndexArray = scanIndex.getArray();
//        EvalTools.notNull(scanIndexArray, MaltcmsTools.class);
//        // Retrieve original mass_values
//        final IVariableFragment massValues = input.getChild(mass_values, true);
//        // Manually set index
//        massValues.setIndex(scanIndex);
//        // Retrieve original intensity_values
//        final IVariableFragment intensityValues = input.getChild(intensity_values, true);
//        // Manually set index
//        intensityValues.setIndex(scanIndex);
//        // Read mass_values with index
//        final List<Array> massValuesList = massValues.getIndexedArray();
//        // Read intensity_values with index
//        final List<Array> intensityValuesList = intensityValues.getIndexedArray();
//        // Create new VariableFragment for mass_values
//        // VariableFragment retMZ = new VariableFragment(output, massValues.getVarname());
//        // Create new VariableFragment for intensity_values
//        // VariableFragment retInten = new VariableFragment(output, intensityValues
//        // .getVarname());
//        // Create new VariableFragment for scan_index
//        // VariableFragment retScanIndex = new VariableFragment(output, scanIndex
//        // .getVarname());
//        // Check, that all created objects are correctly initialized
//        // EvalTools.notNull(new Object[] { retMZ, retInten, retScanIndex },
//        // MaltcmsTools.class);
//        // Check, that original indexed arrays are present
//        EvalTools.notNull(massValuesList, MaltcmsTools.class);
//        EvalTools.notNull(intensityValuesList, MaltcmsTools.class);
//        // Set fillvalue for missing intensities, e.g. if a mass channel was
//        // not recorded for a given bin
//        final double fillvalue = Factory.getInstance().getConfiguration().getDouble("intensity.missing.value", 0.0d);
//        // Number of bins, currently only resolution of 1 m/z
//        // TODO integrate resolution
//        final double massBinResolution = Factory.getInstance().getConfiguration().getDouble("dense_arrays.massBinResolution",
//                1.0d);
//        final int size = MaltcmsTools.getNumberOfIntegerMassBins(min_mass,
//                max_mass, massBinResolution);
//        // Check, that size is at least 1 and at most largest integer
//        EvalTools.inRangeI(1, Integer.MAX_VALUE, size, MaltcmsTools.class);
//        MaltcmsTools.log.debug("Creating dense arrays with " + size
//                + " elements and resolution of {}!", massBinResolution);
//        // Create new index array with size = number of scans
//        final ArrayInt.D1 binnedScanIndexArray = new ArrayInt.D1(massValuesList.size());
//        final ArrayList<Array> retMZa = new ArrayList<Array>(massValuesList.size());
//        final ArrayList<Array> retIntena = new ArrayList<Array>(massValuesList.size());
//        // For all scans
//        for (int i = 0; i < massValuesList.size(); i++) {
//            MaltcmsTools.log.debug("Processing scan {}/{}", i + 1, massValuesList.size());
//            // Create a binned massValues array
//            final Array indx = Array.factory(massValues.getDataType(),
//                    new int[]{size});
//            // Create a binned intensity array
//            final Array vals = Array.factory(massValues.getDataType(),
//                    new int[]{size});
//            // Fill arrays with values, massValues starts at min_mass, goes until
//            // max_mass, with unit increment
//            ArrayTools.createDenseArray(massValuesList.get(i), intensityValuesList.get(i),
//                    new Tuple2D<Array, Array>(indx, vals), ((int) Math.floor(min_mass)), ((int) Math.ceil(max_mass)),
//                    size, massBinResolution, fillvalue);
//            retMZa.add(indx);
//            retIntena.add(vals);
//            binnedScanIndexArray.set(i, i * size);
//        }
//        // mass values
//        EvalTools.notNull(retMZa, MaltcmsTools.class);
//        //IVariableFragment mzRet = new VariableFragment(output, massValues.getName());
//        // mzRet.setIndexedArray(retMZa);
//        if (!output.hasChild(binned_scan_index)) {
//            log.debug("Adding binned_scan_index");
//            final IVariableFragment siRetDense = new VariableFragment(output,
//                    binned_scan_index);
//            siRetDense.setArray(binnedScanIndexArray);
//        } else {
//            throw new ConstraintViolationException("FileFragment " + output.getName() + "already has a child named " + binned_scan_index);
//        }
//        if (!output.hasChild(binned_mass_values)) {
//            log.debug("Adding binned_mass_values");
//            final IVariableFragment mzRetDense = new VariableFragment(output,
//                    binned_mass_values);
//            mzRetDense.setIndex(output.getChild(binned_scan_index));
//            // mzRetDense.setArray(ArrayTools.glue(retMZa));
//            mzRetDense.setIndexedArray(retMZa);
//        } else {
//            throw new ConstraintViolationException("FileFragment " + output.getName() + "already has a child named " + binned_mass_values);
//        }
//
//        // intensities
//        EvalTools.notNull(retIntena, MaltcmsTools.class);
//        // IVariableFragment intenRet = new VariableFragment(output,
//        // intensityValues.getVarname());
//        // intenRet.setIndexedArray(retIntena);
//        if (!output.hasChild(binned_intensity_values)) {
//            log.debug("Adding binned_intensity_values");
//            final IVariableFragment intenRetDense = new VariableFragment(output,
//                    binned_intensity_values);
//            intenRetDense.setIndex(output.getChild(binned_scan_index));
//            // intenRetDense.setIndex(siRetDense);
//            intenRetDense.setIndexedArray(retIntena);
//
//            // intenRetDense.setArray(ArrayTools.glue(retIntena));
//            //final Array a = intenRetDense.getArray();
//            //MaltcmsTools.log.debug("{}", a);
//            //MaltcmsTools.log.debug("{}", binnedScanIndexArray);
//
////            final List<Array> al = intenRetDense.getIndexedArray();
//            //MaltcmsTools.log.debug("{}", al);
//        } else {
//            throw new ConstraintViolationException("FileFragment " + output.getName() + "already has a child named " + binned_intensity_values);
//        }
//        // index
//        EvalTools.notNull(binnedScanIndexArray, MaltcmsTools.class);
//        // IVariableFragment siRet = new VariableFragment(output, scanIndex.getVarname());
//        // siRet.setArray(binnedScanIndexArray);
//        // mzRet.setIndex(siRet);
//        // intenRet.setIndex(siRet);
//
//        // siRetDense.setArray(binnedScanIndexArray);
//        // mzRetDense.setIndex(siRetDense);
//        // intenRetDense.setIndex(siRetDense);
//
//        EvalTools.notNull(output, MaltcmsTools.class);
//        //MaltcmsTools.log.debug("{}", output);
//        return output;
//    }
    /**
     *
     * @param ff
     * @param scan_index
     * @param mass_values
     * @param intensity_values
     * @param binned_scan_index
     * @param binned_mass_values
     * @param binned_intensity_values
     * @param min_mass
     * @param max_mass
     * @param outputDir
     * @return
     */
    public static IFileFragment prepareDenseArraysMZI(final IFileFragment ff,
            final String scan_index, final String mass_values,
            final String intensity_values, final String binned_scan_index,
            final String binned_mass_values,
            final String binned_intensity_values, final Double min_mass,
            final Double max_mass, final File outputDir) {

        // Create target file fragment
        final IFileFragment f = Factory.getInstance().getFileFragmentFactory().create(new File(outputDir, ff.getName()));
        f.addSourceFile(ff);
        return prepareDenseArraysMZI(ff, f, scan_index, mass_values,
                intensity_values, binned_scan_index, binned_mass_values,
                binned_intensity_values, min_mass, max_mass, outputDir);
    }

    /**
     * @param t
     * @param caller
     * @param d
     * @return
     */
    public static List<IFileFragment> prepareEICFragments(
            final TupleND<IFileFragment> t, final Class<?> caller,
            final File outputDir) {
        final List<IFileFragment> unalignedEICFragments = new ArrayList<IFileFragment>(
                t.size());
        try {
            for (final IFileFragment iff : t) {
                final IFileFragment eicFragment = Factory.getInstance().getFileFragmentFactory().create(
                        new File(outputDir, iff.getName()));
                for (final IFileFragment initIff : Factory.getInstance().getInputDataFactory().getInitialFiles()) {
                    if (StringTools.removeFileExt(iff.getName()).equals(
                            StringTools.removeFileExt(initIff.getName()))) {
                        final String massV = Factory.getInstance().getConfiguration().getString(
                                "var.mass_values", "mass_values");
                        final String intV = Factory.getInstance().getConfiguration().getString(
                                "var.intensity_values",
                                "intensity_values");
                        final String scanV = Factory.getInstance().getConfiguration().getString("var.scan_index",
                                "scan_index");
                        final Array masses = initIff.getChild(massV).getArray();
                        final Array intensities = initIff.getChild(intV).getArray();
                        final Array scan_index = initIff.getChild(scanV).getArray();
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
        } catch (final ResourceNotAvailableException rnae) {
            MaltcmsTools.log.warn("Failed to load resource: {}", rnae.getLocalizedMessage());
        }
        return unalignedEICFragments;
    }

    /**
     *
     * @param t
     * @return
     */
    public static Tuple2D<List<Array>, List<Array>> prepareInputArraysTICasList(
            final Tuple2D<IFileFragment, IFileFragment> t) {
        EvalTools.notNull(new Object[]{t, t.getFirst(), t.getSecond()}, t);
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

    /**
     *
     * @param ff
     * @param scan_index
     * @param mass_values
     * @param intensity_values
     * @param min_mass
     * @param max_mass
     * @return
     */
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
        final double massBinResolution = Factory.getInstance().getConfiguration().getDouble("dense_arrays.massBinResolution",
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
            // Fill arrays with values, massValues starts at min_mass, goes until
            // max_mass, with unit increment
            final ArrayDouble.D1 sparse = new Sparse(mza.get(i), intena.get(i),
                    (int) Math.floor(min_mass), (int) Math.ceil(max_mass),
                    size, massBinResolution);
            EvalTools.eqI(size, sparse.getShape()[0], MaltcmsTools.class);
            l.add(sparse);
        }
        return l;
    }

    /**
     *
     * @param arrays
     * @return
     */
    public static Tuple2D<ArrayDouble.D1, ArrayDouble.D1> calculateMeanVarSparse(
            final List<Array> arrays) {
        int bins = arrays.get(0).getShape()[0];
        ArrayDouble.D1 mean = new ArrayDouble.D1(bins);
        final ArrayDouble.D1 var = new ArrayDouble.D1(bins);
        final ArrayInt.D1 nn = new ArrayInt.D1(bins);

        int n = 0;
        double delta = 0, meanCurrent = 0, x = 0, v = 0;
        double inten;
        int mass;
        for (int i = 0; i < arrays.size(); i++) {
//            if (i % 100 == 0) {
//                MaltcmsTools.log.info("scan {}", i);
//            }
            for (final Array ms : arrays) {
                final IndexIterator massIter = ms.getIndexIterator();
                final IndexIterator intenIter = ms.getIndexIterator();
                while (massIter.hasNext() && intenIter.hasNext()) {
                    inten = intenIter.getDoubleNext();
                    mass = massIter.getIntNext();
                    n = nn.get(mass) + 1;
                    nn.set(mass, n);
                    meanCurrent = mean.get(mass);
                    x = inten;
                    delta = x - meanCurrent;
                    meanCurrent = meanCurrent + delta / n;
                    mean.set(mass, meanCurrent);
                    v = var.get(mass);
                    var.set(mass, v + delta * (x - meanCurrent));
                }
            }
        }
        final IndexIterator iter = var.getIndexIterator();
        final IndexIterator niter = nn.getIndexIterator();
        double m2;
        while (iter.hasNext()) {
            m2 = iter.getDoubleNext();
            n = niter.getIntNext();
            if (n > 0) {
                iter.setDoubleCurrent(m2 / (n - 1));
            }
        }
        return new Tuple2D<ArrayDouble.D1, ArrayDouble.D1>(mean, var);
    }

    /**
     *
     * @param f
     * @param intensities
     * @param k
     * @param creator
     * @param outputDir
     * @return
     */
    public static List<Tuple2D<Double, Double>> rankEICsByVariance(
            final IFileFragment f, final List<Array> intensities, final int k,
            final Class<?> creator, final File outputDir) {
        final ArrayStatsScanner ass = new ArrayStatsScanner();
        final List<ArrayDouble.D1> eics1 = ArrayTools.tiltD1(ArrayTools.convertArrays(intensities));
        final StatsMap[] sm1 = ass.apply(eics1.toArray(new Array[]{}));
        final List<Tuple2D<Double, Double>> vals1 = new ArrayList<Tuple2D<Double, Double>>(
                sm1.length);
        for (int i = 0; i < sm1.length; i++) {
            vals1.add(new Tuple2D<Double, Double>(sm1[i].get(cross.datastructures.Vars.Variance.toString()),
                    (double) i));
        }
        Collections.sort(vals1, Collections.reverseOrder(new Tuple2DDoubleComp()));
        final CSVWriter csvw = new CSVWriter();
        csvw.writeStatsMaps(outputDir.getAbsolutePath(), StringTools.removeFileExt(f.getName())
                + "_eic_stats.csv", sm1);
        return vals1.subList(0, Math.min(vals1.size() - 1, k - 1));
    }

    /**
     *
     * @param intensities
     * @return
     */
    public static int[] ranksByIntensity(final Array intensities) {
        return ranksByIntensity(intensities, 1.0d);
    }

    /**
     *
     * @param ff
     * @return
     */
    public static ArrayDouble.D1 getMassesToFilterByCoefficientOfVariation(
            IFileFragment ff) {
        ChromatogramFactory cf = Factory.getInstance().getObjectFactory().instantiate(ChromatogramFactory.class);
        IChromatogram1D c = cf.createChromatogram1D(ff);
        log.info("Number of scans: {}", c.getNumberOfScans());
        Tuple2D<Double, Double> t = MaltcmsTools.getMinMaxMassRange(ff);
        double minmz = t.getFirst().doubleValue();
        double maxmz = t.getSecond().doubleValue();
        double res = 1.0d;
        int bins = MaltcmsTools.getNumberOfIntegerMassBins(t.getFirst(), t.getSecond(), res);
        final ArrayDouble.D1 mean = new ArrayDouble.D1(bins);
        final ArrayDouble.D1 var = new ArrayDouble.D1(bins);
        final ArrayDouble.D1 tins = new ArrayDouble.D1(bins);
        final ArrayDouble.D1 nn = new ArrayDouble.D1(bins);

        double n = 0;
        double delta = 0, meanCurrent = 0, x = 0, v = 0;
        double inten;
        double mass;
        int bin;
        // calculate mean and variance over all mass channels
        for (IScan1D s : c) {
            final IndexIterator massIter = s.getMasses().getIndexIterator();
            final IndexIterator intenIter = s.getIntensities().getIndexIterator();
            while (massIter.hasNext() && intenIter.hasNext()) {
                inten = intenIter.getDoubleNext();
                mass = massIter.getDoubleNext();
                bin = MaltcmsTools.binMZ(mass, minmz, maxmz, res);
                n = nn.get(bin) + 1;
                nn.set(bin, n);
                meanCurrent = mean.get(bin);
                x = inten;
                delta = x - meanCurrent;
                meanCurrent = meanCurrent + delta / n;
                mean.set(bin, meanCurrent);
                v = var.get(bin);
                var.set(bin, v + delta * (x - meanCurrent));
                tins.set(bin, tins.get(bin) + inten);
            }
        }

        final IndexIterator iter = var.getIndexIterator();
        final IndexIterator niter = nn.getIndexIterator();
        double m2;
        while (iter.hasNext()) {
            m2 = iter.getDoubleNext();
            n = niter.getIntNext();
            if (n > 0) {
                iter.setDoubleCurrent(m2 / (n - 1));
            }
        }

        ArrayDouble.D1 massIndex = new ArrayDouble.D1(bins);
        for (int i = 0; i < bins; i++) {
            massIndex.set(i, i + minmz);
        }
        // calculate standard deviation
        SqrtFilter sf = new SqrtFilter();
        ArrayDouble.D1 sdev = (ArrayDouble.D1) sf.apply(var);
        ArrayDouble.D1 coeffVar = new ArrayDouble.D1(c.getNumberOfScans());
        ArrayList<Double> massChannelsToFilter = new ArrayList<Double>();
        // calculate coefficient of variation, stddev/mean
        for (int j = 0; j < bins; j++) {
            // s>mean -> cvar > 1 -> -log(cvar) < 0 => channels with large std
            // deviation profile
            // s<mean -> cvar < 1 -> -log(cvar) > 0 => channels with small std
            // deviation profile
            coeffVar.set(j, -(Math.log(sdev.get(j) / mean.get(j)) / Math.log(2.0d)));
            if (coeffVar.get(j) < 0) {
                massChannelsToFilter.add(j + minmz);
            }
            // this.log.info("Coefficient of variation on channel {}: {}",minmz+j,);
        }
        ArrayDouble.D1 massesToFilter = new ArrayDouble.D1(massChannelsToFilter.size());
        for (int j = 0; j < massChannelsToFilter.size(); j++) {
            massesToFilter.set(j, massChannelsToFilter.get(j));
        }
        return massesToFilter;
    }

    /**
     *
     * @param ff
     * @return
     */
    public static ArrayList<Integer> getMassesToFilterByCoefficientOfVariation2(
            IFileFragment ff) {
        ChromatogramFactory cf = Factory.getInstance().getObjectFactory().instantiate(ChromatogramFactory.class);
        IChromatogram1D c = cf.createChromatogram1D(ff);
        log.info("Number of scans: {}", c.getNumberOfScans());
        Tuple2D<Double, Double> t = MaltcmsTools.getMinMaxMassRange(ff);
        double minmz = t.getFirst().doubleValue();
        double maxmz = t.getSecond().doubleValue();
        double res = 1.0d;
        int bins = MaltcmsTools.getNumberOfIntegerMassBins(t.getFirst(), t.getSecond(), res);
        final ArrayDouble.D1 mean = new ArrayDouble.D1(bins);
        final ArrayDouble.D1 var = new ArrayDouble.D1(bins);
        final ArrayDouble.D1 tins = new ArrayDouble.D1(bins);
        final ArrayDouble.D1 nn = new ArrayDouble.D1(bins);

        double n = 0;
        double delta = 0, meanCurrent = 0, x = 0, v = 0;
        double inten;
        double mass;
        int bin;
        // calculate mean and variance over all mass channels
        for (IScan1D s : c) {
            final IndexIterator massIter = s.getMasses().getIndexIterator();
            final IndexIterator intenIter = s.getIntensities().getIndexIterator();
            while (massIter.hasNext() && intenIter.hasNext()) {
                inten = intenIter.getDoubleNext();
                mass = massIter.getDoubleNext();
                bin = MaltcmsTools.binMZ(mass, minmz, maxmz, res);
                n = nn.get(bin) + 1;
                nn.set(bin, n);
                meanCurrent = mean.get(bin);
                x = inten;
                delta = x - meanCurrent;
                meanCurrent = meanCurrent + delta / n;
                mean.set(bin, meanCurrent);
                v = var.get(bin);
                var.set(bin, v + delta * (x - meanCurrent));
                tins.set(bin, tins.get(bin) + inten);
            }
        }

        final IndexIterator iter = var.getIndexIterator();
        final IndexIterator niter = nn.getIndexIterator();
        double m2;
        while (iter.hasNext()) {
            m2 = iter.getDoubleNext();
            n = niter.getIntNext();
            if (n > 0) {
                iter.setDoubleCurrent(m2 / (n - 1));
            }
        }

        ArrayDouble.D1 massIndex = new ArrayDouble.D1(bins);
        for (int i = 0; i < bins; i++) {
            massIndex.set(i, i + minmz);
        }
        // calculate standard deviation
        SqrtFilter sf = new SqrtFilter();
        ArrayDouble.D1 sdev = (ArrayDouble.D1) sf.apply(var);
        ArrayDouble.D1 coeffVar = new ArrayDouble.D1(c.getNumberOfScans());
        ArrayList<Integer> massChannelsToFilter = new ArrayList<Integer>();
        // calculate coefficient of variation, stddev/mean
        for (int j = 0; j < bins; j++) {
            // s>mean -> cvar > 1 -> -log(cvar) < 0 => channels with large std
            // deviation profile
            // s<mean -> cvar < 1 -> -log(cvar) > 0 => channels with small std
            // deviation profile
            coeffVar.set(j, -(Math.log(sdev.get(j) / mean.get(j)) / Math.log(2.0d)));
            if (coeffVar.get(j) < 0) {
                massChannelsToFilter.add(j);
            }
            // this.log.info("Coefficient of variation on channel {}: {}",minmz+j,);
        }
        return massChannelsToFilter;
    }

    /**
     * Expects intensities to be sorted in ascending order of masses. Returns
     * intensities sorted ascending by intensity, so the index of the mass
     * channel with highest intensity is at intensities.getShape()[0]-1.
     *
     * @param intensities
     * @param intensityRangeToCover between 0 and 1
     * @return
     */
    public static int[] ranksByIntensity(final Array intensities,
            final double intensityRangeToCover) {
        final double intensityRange = Math.max(Math.min(intensityRangeToCover,
                1.0d), 0.0d);
        log.info("Intensity range: {}", intensityRange);
        final Index mint = intensities.getIndex();
        final List<Tuple2D<Integer, Double>> l = new ArrayList<Tuple2D<Integer, Double>>(
                intensities.getShape()[0]);
        final int[] ranks = new int[intensities.getShape()[0]];
        final double totalIntensity = ArrayTools.integrate(intensities);
        // identity
        for (int i = 0; i < ranks.length; i++) {
            l.add(new Tuple2D<Integer, Double>(i, Double.valueOf(intensities.getDouble(mint.set(i)))));
            ranks[i] = i;
        }
        // reverse comparator
        Collections.sort(l, Collections.reverseOrder(new Comparator<Tuple2D<Integer, Double>>() {
            @Override
            public int compare(final Tuple2D<Integer, Double> o1,
                    final Tuple2D<Integer, Double> o2) {
                if (o1.getSecond() < o2.getSecond()) {
                    return -1;
                } else if (o1.getSecond() > o2.getSecond()) {
                    return 1;
                }
                return 0;
            }
        }));
        double sum = 0;
        int[] ret = null;
        for (int i = 0; i < ranks.length; i++) {
            sum += l.get(i).getSecond();
            // return partial ranks, as soon as we reach the limit of intensity
            // to cover
            if (sum / totalIntensity >= (intensityRange * totalIntensity)) {
                ret = new int[i + 1];
                System.arraycopy(ranks, 0, ret, 0, i + 1);
                return ret;
            }
            ranks[i] = l.get(i).getFirst();
        }
        return ranks;
    }

    /**
     *
     */
    public static void setBinMZbyConfig() {
        MaltcmsTools.setBinMZDefault(Factory.getInstance().getConfiguration().getString("MaltcmsTools.binMZ.mode",
                RoundMode.RFLOORINT.name()));
    }

    /**
     *
     * @param s
     */
    public static void setBinMZDefault(final String s) {
        MaltcmsTools.binMZMode = RoundMode.valueOf(s);
    }

    // public static void setDenseArrayFragmentFor(final IFileFragment iff,
    // final IFileFragment daFragment) {
    // MaltcmsTools.setDenseArrayFragmentFor(iff.getName(), daFragment);
    // }
    //
    // public static void setDenseArrayFragmentFor(final String fragmentName,
    // final IFileFragment daFragment) {
    // MaltcmsTools.denseArrays.put(fragmentName, daFragment);
    // }
    /**
     * @param masses
     * @return FIXME this could be implemented more efficiently Complexity:
     * every element in masses needs to be checked once
     */
    public static List<Integer> findMaskedMasses(final Array masses,
            final List<Double> maskedMasses, final double epsilon) {
        IndexIterator ii = masses.getIndexIterator();
        ArrayList<Integer> masked = new ArrayList<Integer>();
        int i = 0;
        // O(n)
        while (ii.hasNext()) {
            double cm = ii.getDoubleNext();
            int j = 0;
            // O(m), could be improved by using a datastructure supporting ranqe
            // queries
            for (; j < maskedMasses.size(); j++) {
                // if mass is within range
                if (maskedMasses.get(j) - epsilon <= cm
                        && maskedMasses.get(j) + epsilon >= cm) {
                    masked.add(i);
                }
            }
            i++;
        }
        // total: O(m*n) worst case
        return masked;
    }

    /**
     * Creates a copy of l, then sorts the list and returns a condensed version
     * of the list, with only one instance of every Object remaining.
     *
     * @param l
     * @return
     */
    public static <T extends Comparable<T>> List<T> unique(final List<T> l) {
        List<T> sorted = new ArrayList<T>(l);
        MaltcmsTools.log.debug("{}", sorted);
        // O(nlogn)
        Collections.sort(sorted);
        T last = null;
        ListIterator<T> li = sorted.listIterator();
        // O(n)
        while (li.hasNext()) {
            if (last == null) {
                last = li.next();
            } else {
                T next = li.next();
                if (last.equals(next)) {
                    li.remove();
                }
                last = next;
            }
        }
        // total: O(nlogn)
        MaltcmsTools.log.debug("{}", sorted);
        return sorted;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        List<Integer> l = Arrays.asList(1, 2, 3, 4, 5, 5, 5, 6, 3, 3, 2, 2, 1,
                1);
        MaltcmsTools.unique(l);
        double[] d = new double[]{50.03023, 50.05032, 50.9, 50.995, 51.234,
            51.934, 52.2};
        Array a = Array.factory(d);
        List<Double> masked = Arrays.asList(49.9, 51.2, 52.0);
        double epsilon = 1;
        for (int i = 0; i < 10; i++) {
            double delta = epsilon / ((double) (i + 1));
            System.out.println(delta);
            List<Integer> maskedIndices = MaltcmsTools.unique(MaltcmsTools.findMaskedMasses(a, masked, delta));
            System.out.println("Masked indices: " + maskedIndices);
            Array b = ArrayTools.filterIndices(a, maskedIndices, 0.0d);
            System.out.println(b);
        }

    }
}
