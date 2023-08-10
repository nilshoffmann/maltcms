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
package maltcms.io.xml.mzML;

import cross.Factory;
import cross.annotations.Configurable;
import cross.cache.CacheFactory;
import cross.cache.ICacheDelegate;
import cross.datastructures.cache.ISerializationProxy;
import cross.datastructures.cache.SerializableArray;
import cross.datastructures.collections.CachedLazyList;
import cross.datastructures.collections.CachedReadWriteList;
import cross.datastructures.collections.IElementProvider;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableVariableFragment2;
import cross.datastructures.tools.ArrayTools;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FragmentTools;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.tools.StringTools;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import maltcms.io.andims.NetcdfDataSource;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayShort;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray.Precision;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.Chromatogram;
import uk.ac.ebi.jmzml.model.mzml.FileDescription;
import uk.ac.ebi.jmzml.model.mzml.Run;
import uk.ac.ebi.jmzml.model.mzml.SourceFile;
import uk.ac.ebi.jmzml.model.mzml.SourceFileList;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.xml.io.MzMLObjectIterator;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 * Implementation of IDataSource for the mzML format.
 *
 * @author Nils Hoffmann
 * 
 * @since 1.3.2
 */

//@ServiceProvider(service = IDataSource.class)
public class MZMLDataSource2 implements IDataSource {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MZMLDataSource2.class);

    private final String[] fileEnding = new String[]{"mzml", "mzml.xml"};
    @Configurable(name = "var.mass_values", value = "mass_values")
    private String mass_values = "mass_values";
    @Configurable(name = "var.intensity_values", value = "intensity_values")
    private String intensity_values = "intensity_values";
    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String total_intensity = "total_intensity";
    @Configurable(name = "var.scan_acquisition_time", value = "scan_acquisition_time")
    private final String scan_acquisition_time = "scan_acquisition_time";
    @Configurable(name = "var.scan_index", value = "scan_index")
    private String scan_index = "scan_index";
    @Configurable(name = "var.mass_range_min", value = "mass_range_min")
    private String mass_range_min = "mass_range_min";
    @Configurable(name = "var.mass_range_max", value = "mass_range_max")
    private String mass_range_max = "mass_range_max";
    private NetcdfDataSource ndf = null;
    @Configurable(name = "var.source_files", value = "source_files")
    private String source_files = "source_files";
    @Configurable(name = "var.modulation_time", value = "modulation_time")
    private String modulation_time = "modulation_time";
    private String modulationTimeAccession = "MS:1002042";
    @Configurable(name = "var.first_column_elution_time", value = "first_column_elution_time")
    private String first_column_elution_time = "first_column_elution_time";
    private String first_column_elution_timeAccession = "MS:1002082";
    @Configurable(name = "var.second_column_elution_time", value = "second_column_elution_time")
    private String second_column_elution_time = "second_column_elution_time";
    private String second_column_elution_timeAccession = "MS:1002083";
    @Configurable(name = "var.total_ion_current_chromatogram", value = "total_ion_current_chromatogram")
    private String total_ion_current_chromatogram = "total_ion_current_chromatogram";
    private String total_ion_current_chromatogramAccession = "MS:1000235";
    @Configurable(name = "var.total_ion_current_chromatogram_scan_acquisition_time", value = "total_ion_current_chromatogram_scan_acquisition_time")
    private String total_ion_current_chromatogram_scan_acquisition_time = "total_ion_current_chromatogram_scan_acquisition_time";
    private String total_ion_current_chromatogram_scan_acquisition_timeAccession = "MS:1000595";
    private String ms_level = "ms_level";
    private String msLevelAccession = "MS:1000511";
    private static final ICacheDelegate<URI, MzMLUnmarshaller> fileToIndex = CacheFactory.createVolatileCache(MZMLDataSource2.class.getName() + "-unmarshaller", 3600, 7200, 100);
    private static final ICacheDelegate<MzMLUnmarshaller, Run> unmarshallerToRun = CacheFactory.createVolatileCache(MZMLDataSource2.class.getName() + "-unmarshaller-to-run", 3600, 7200, 20);
    private static final ICacheDelegate<String, SerializableArray> variableToArrayCache = CacheFactory.createVolatileCache("maltcms.io.readcache");

    private ICacheDelegate<String, SerializableArray> getCache() {
        return MZMLDataSource2.variableToArrayCache;
    }

    /** {@inheritDoc} */
    @Override
    public int canRead(final IFileFragment ff) {
        final int dotindex = ff.getName().lastIndexOf(".");
        if (dotindex == -1) {
            throw new RuntimeException("Could not determine File extension of "
                    + ff);
        }
        final String filename = ff.getName().toLowerCase();
        for (final String s : this.fileEnding) {
            if (filename.endsWith(s)) {
                return 1;
            }
        }
        log.debug("no!");
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void configurationChanged(final ConfigurationEvent arg0) {
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration configuration) {
        this.mass_values = configuration.getString("var.mass_values",
                "mass_values");
        this.intensity_values = configuration.getString("var.intensity_values",
                "intensity_values");
        this.total_intensity = configuration.getString("var.total_intensity",
                "total_intensity");
        this.scan_index = configuration.getString("var.scan_index",
                "scan_index");
        this.mass_range_min = configuration.getString("var.mass_range_min",
                "mass_range_min");
        this.mass_range_max = configuration.getString("var.mass_range_max",
                "mass_range_max");
        this.source_files = configuration.getString("var.source_files",
                "source_files");
        this.modulation_time = configuration.getString("var.modulation_time",
                "modulation_time");
        this.first_column_elution_time = configuration.getString("var.first_column_elution_time",
                "first_column_elution_time");
        this.second_column_elution_time = configuration.getString("var.second_column_elution_time",
                "second_column_elution_time");
        this.total_ion_current_chromatogram = configuration.getString("var.total_ion_current_chromatogram", "total_ion_current_chromatogram");
        this.total_ion_current_chromatogram_scan_acquisition_time = configuration.getString("var.total_ion_current_chromatogram_scan_acquisition_time", "total_ion_current_chromatogram_scan_acquisition_time");
        this.ms_level = configuration.getString("var.ms_level", "ms_level");
        this.ndf = new NetcdfDataSource();
        this.ndf.configure(configuration);
    }

    private MzMLUnmarshaller getUnmarshaller(final IFileFragment ff) {
        MzMLUnmarshaller um = MZMLDataSource2.fileToIndex.get(ff.getUri());
        if (um != null) {
            log.info("Retrieved unmarshaller from cache!");
            return um;
        }
        try {
            if (new File(ff.getUri()).exists()) {
                log.debug("Initializing unmarshaller for file {}", ff.getUri());
                um = new MzMLUnmarshaller(ff.getUri().toURL(), true);
                MZMLDataSource2.fileToIndex.put(ff.getUri(), um);
                log.debug("mzML file {} is indexed: {}", ff.getUri(), um.isIndexedmzML());
                return um;
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
        throw new ResourceNotAvailableException("File fragment " + ff.getUri() + " does not exist!");
    }

//    private Run getRun(MzMLUnmarshaller mzmlu) {
//        Run run = MZMLDataSource.unmarshallerToRun.get(mzmlu);
//        if (run != null) {
//            log.debug("Retrieved run from cache!");
//            return run;
//        }
//        run = mzmlu.unmarshalFromXpath("/run", Run.class);
//        MZMLDataSource.unmarshallerToRun.put(mzmlu, run);
//        return run;
//    }
    private SourceFileList getSourceFiles(MzMLUnmarshaller mzmlu) {
        log.info("Retrieving fileDescription/sourceFileList from mzML");
        FileDescription fd = mzmlu.unmarshalFromXpath("/fileDescription", FileDescription.class);
        if (fd == null) {
            throw new ResourceNotAvailableException("Could not retrieve fileDescription element in mzML file.");
        }
        SourceFileList sfl = fd.getSourceFileList();
        return sfl;
    }

//    /*
//     * FIXME unify spectrum iteration, so that all relevant CV terms are read and cached
//     * if the whole spectra list is iterated to avoid repeated iteration.
//     */
//    private Spectrum getSpectrum(MzMLUnmarshaller um, int idx) {
//        try {
//            if (um.isIndexedmzML()) {
//                log.debug("Retrieving spectrum by id from spectrum index!");
//                return um.getSpectrumById(um.getSpectrumIDFromSpectrumIndex(idx));
//            } else {
//                log.warn("Not using indexed mzML, this is really inefficient!");
//                return getRun(um).getSpectrumList().getSpectrum().get(idx);
//            }
//        } catch (NullPointerException npe) {
//            log.warn("Not using indexed mzML, this is really inefficient!");
//            return getRun(um).getSpectrumList().getSpectrum().get(idx);
//        } catch (MzMLUnmarshallerException ex) {
//            java.util.logging.Logger.getLogger(MZMLDataSource.class.getName()).log(Level.SEVERE, null, ex);
//            throw new ResourceNotAvailableException(ex);
//        }
//    }
    private int getPointCount(final Spectrum s) {
        return s.getBinaryDataArrayList().getBinaryDataArray().get(0).getBinaryDataAsNumberArray().length;
    }

    private Array createCompatibleArray(Precision p, int[] shape) {
        Array array = null;
        switch (p) {
            case FLOAT32BIT:
                array = Array.factory(DataType.FLOAT, shape);
                break;
            case FLOAT64BIT:
                array = Array.factory(DataType.DOUBLE, shape);
                break;
            case INT32BIT:
                array = Array.factory(DataType.INT, shape);
                break;
            case INT64BIT:
                array = Array.factory(DataType.LONG, shape);
                break;
            default:
                throw new ConstraintViolationException("Unsupported precision for binary data array: " + p.name());
        }
        return array;
    }

    private Array createCompatibleArray(BinaryDataArray bda) {
        Integer arrayLength = bda.getArrayLength();
        if (arrayLength == null) {
            arrayLength = bda.getBinaryDataAsNumberArray().length;
        }
        return createCompatibleArray(bda.getPrecision(), new int[]{arrayLength});
    }

    private Tuple2D<? extends Number, ? extends Number> findMinMax(BinaryDataArray bda) {
        Number[] n = bda.getBinaryDataAsNumberArray();
        Number min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        for (Number number : n) {
            min = Math.min(min.doubleValue(), number.doubleValue());
            max = Math.max(max.doubleValue(), number.doubleValue());
        }
        Precision p = bda.getPrecision();
        switch (p) {
            case FLOAT32BIT:
                return new Tuple2D<>(min.floatValue(), max.floatValue());
            case FLOAT64BIT:
                return new Tuple2D<>(min.doubleValue(), max.doubleValue());
            case INT32BIT:
                return new Tuple2D<>(min.intValue(), max.intValue());
            case INT64BIT:
                return new Tuple2D<>(min.longValue(), max.longValue());
            default:
                throw new ConstraintViolationException("Unsupported precision for binary data array: " + p.name());
        }
    }

    private Array fillArrayFromBinaryData(BinaryDataArray bda) {
        Number[] n = bda.getBinaryDataAsNumberArray();
        log.info("{}", bda.toString());
//        EvalTools.gt(0, n.length, MZMLDataSource.class);
        Precision p = bda.getPrecision();
        Array array = createCompatibleArray(bda);
        switch (p) {
            case FLOAT32BIT:
                int j = 0;
                for (Number num : n) {
                    array.setFloat(j++, num.floatValue());
                }
                break;
            case FLOAT64BIT:
                j = 0;
                for (Number num : n) {
                    array.setDouble(j++, num.doubleValue());
                }
                break;
            case INT32BIT:
                j = 0;
                for (Number num : n) {
                    array.setInt(j++, num.intValue());
                }
                break;
            case INT64BIT:
                j = 0;
                for (Number num : n) {
                    array.setLong(j++, num.longValue());
                }
                break;
            default:
                throw new ConstraintViolationException("Unsupported precision for binary data array: " + p.name());
        }
        return array;
    }

    private BinaryDataArray getMzBinaryDataArray(final Spectrum s) {
        for (BinaryDataArray bda : s.getBinaryDataArrayList().getBinaryDataArray()) {
            try {
                CVParam param = findParam(bda.getCvParam(), "MS:1000514");
                if (param != null) {
                    return bda;
                }
            } catch (ResourceNotAvailableException rnae) {
                log.warn("m/z binary data array is not annotated with expected CVParam MS:1000514. Falling back to selecting the first binary data array in list!");
                if (s.getBinaryDataArrayList().getCount() > 0) {
                    return s.getBinaryDataArrayList().getBinaryDataArray().get(0);
                }
            }
        }
        throw new ResourceNotAvailableException("Could not find m/z binary data array as child of spectrum " + s.getId());
    }

    private BinaryDataArray getIntensityBinaryDataArray(final Spectrum s) {
        for (BinaryDataArray bda : s.getBinaryDataArrayList().getBinaryDataArray()) {
            try {
                CVParam param = findParam(bda.getCvParam(), "MS:1000515");
                if (param != null) {
                    return bda;
                }
            } catch (ResourceNotAvailableException rnae) {
                log.warn("intensity binary data array is not annotated with expected CVParam MS:1000515. Falling back to selecting the second binary data array in list!");
                if (s.getBinaryDataArrayList().getCount() > 1) {
                    return s.getBinaryDataArrayList().getBinaryDataArray().get(1);
                }
            }
        }
        throw new ResourceNotAvailableException("Could not find intensity binary data array as child of spectrum " + s.getId());
    }

    private Array getMassValues(final Spectrum s) {
        return fillArrayFromBinaryData(getMzBinaryDataArray(s));
    }

    private Array getIntensityValues(final Spectrum s) {
        return fillArrayFromBinaryData(getIntensityBinaryDataArray(s));
    }

    private Tuple2D<Double, Double> getMinMaxMassRange(final Array massValues) {
        MinMax mm = MAMath.getMinMax(massValues);
        return new Tuple2D<>(mm.min, mm.max);
    }

    private CVParam findParam(List<CVParam> l, String accession) {
        for (CVParam cvp : l) {
            if (cvp.getAccession().equalsIgnoreCase(accession)) {
                return cvp;
            }
        }
        throw new ResourceNotAvailableException("CVParam with accession "
                + accession + " not contained in list!");
    }

    private String getRTUnit(final Spectrum s, final String cvTerm) {
        String rtUnit = "seconds";
        try {
            CVParam rtp = findParam(s.getScanList().getScan().get(0).getCvParam(), cvTerm);
            rtUnit = rtp.getUnitName();
        } catch (ResourceNotAvailableException rne) {
            log.warn("Could not retrieve rt unit for spectrum {}!", s.getId());
        }
        return rtUnit;
    }

    private double convertRT(double rtValue, String rtUnit) {
        if (rtUnit.equalsIgnoreCase("seconds") || rtUnit.equalsIgnoreCase("second")) {
            return rtValue;
        } else if (rtUnit.equalsIgnoreCase("minutes") || rtUnit.equalsIgnoreCase("minute")) {
            return rtValue *= 60.0;
        } else if (rtUnit.equalsIgnoreCase("hours") || rtUnit.equalsIgnoreCase("hour")) {
            return rtValue *= 3600.0;
        }
        throw new IllegalArgumentException("Unknown rt unit for rtValue conversion: " + rtUnit);
    }

    private double getRT(final Spectrum s) {
        double rt = Double.NaN;
        try {
            CVParam rtp = findParam(s.getScanList().getScan().get(0).getCvParam(), "MS:1000016");
            rt = Double.parseDouble(rtp.getValue());
            String unit = getRTUnit(s, "MS:1000016");
            rt = convertRT(rt, unit);
        } catch (NullPointerException | ResourceNotAvailableException npe) {
            log.warn("Could not retrieve spectrum acquisition time!");
        }
        return rt;
    }

    private int getScanCount(final MzMLUnmarshaller um) {
        return um.getObjectCountForXpath("/run/spectrumList/spectrum");
//		if (um.isIndexedmzML()) {
//			return um.getSpectrumIDs().size();
//		}
//		return getRun(um).getSpectrumList().getCount();
    }

    private IVariableFragment getVariable(final IFileFragment f,
            final String name) {
        return (f.hasChild(name) ? f.getChild(name) : new ImmutableVariableFragment2(f,
                name));
    }

    /**
     * Read min and max_mass_range to determine bin sizes.
     *
     * @param var a {@link cross.datastructures.fragments.IVariableFragment} object.
     * @return a Tuple2D<Array,Array> with mass_range_min as first and
     * mass_range_max as second array
     * @param um a {@link uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller} object.
     */
    protected Tuple2D<Array, Array> initMinMaxMZ(final IVariableFragment var,
            final MzMLUnmarshaller um) {
        log.debug("Loading {} and {}", new Object[]{this.mass_range_min,
            this.mass_range_max});
        int scans = getScanCount(um);
        int start = 0;
        final Range[] r = var.getRange();
        if (r != null) {
            start = Math.max(0, r[0].first());
            scans = Math.min(scans, r[0].length());
        }
        final ArrayDouble.D1 mass_range_min1 = new ArrayDouble.D1(scans);
        final ArrayDouble.D1 mass_range_max1 = new ArrayDouble.D1(scans);
        double min_mass = Double.MAX_VALUE;
        double max_mass = Double.MIN_VALUE;
        MzMLObjectIterator<Spectrum> spectrumIterator = um.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
        int i = 0;
        while (spectrumIterator.hasNext()) {
            Spectrum spectrum = spectrumIterator.next();
            if (i >= start && i < start + scans) {
                Array a = getMassValues(spectrum);
                final Tuple2D<Double, Double> t = getMinMaxMassRange(a);
                min_mass = Math.min(min_mass, t.getFirst());
                max_mass = Math.max(max_mass, t.getSecond());
                i++;
            }
        }
        for (i = 0; i < start + scans; i++) {
            mass_range_min1.setDouble(i, Math.floor(min_mass));
            mass_range_max1.setDouble(i, Math.ceil(max_mass));
        }
        return new Tuple2D<Array, Array>(mass_range_min1, mass_range_max1);
    }

    private Array loadArray(final IFileFragment f, final IVariableFragment var) {
        SerializableArray sa = getArrayFromCache(var);
        if (sa != null) {
            log.debug("Retrieved variable data array from cache for " + var);
            return sa.getArray();
        }
        Array a = null;
        MzMLUnmarshaller mzu = getUnmarshaller(f);
        final String varname = var.getName();
        log.info("Trying to read variable " + var.getName());
        if (varname.equals(this.source_files)) {
            a = readSourceFiles(f, mzu);
        } else if (varname.equals(this.total_intensity)) {
            a = readTotalIntensitiesArray(var.getParent(), intensity_values, mzu);
        } else if (varname.equals(this.modulation_time)) {
            a = readModulationTimeArray(var, mzu);
        } else if (varname.equals(this.total_ion_current_chromatogram)) {
            a = readTotalIonCurrentChromatogram(var, mzu, true);
        } else if (varname.equals(this.total_ion_current_chromatogram_scan_acquisition_time)) {
            a = readTotalIonCurrentChromatogram(var, mzu, false);
        } else if (varname.equals(this.first_column_elution_time)) {
            a = readElutionTimeArray(var, mzu, this.first_column_elution_timeAccession);
        } else if (varname.equals(this.second_column_elution_time)) {
            a = readElutionTimeArray(var, mzu, this.second_column_elution_timeAccession);
        } else {
            a = readArray(var, mzu);
        }

        if (a != null) {
            putArrayIntoCache(var, a);
        } else {
            throw new ResourceNotAvailableException("Array for variable " + var.getName() + " could not be loaded!");
        }
        return a;
    }

    private String getKeyForCache(IVariableFragment variableFragment) {
        return variableFragment.getParent().getUri() + ">" + variableFragment.getName();
    }

    private SerializableArray getArrayFromCache(IVariableFragment variableFragment) {
        Range[] r = variableFragment.getRange();
        SerializableArray sa = getCache().get(getKeyForCache(variableFragment));
        if (sa == null || r == null) {
            return sa;
        }
        int[] shape = sa.getArray().getShape();
        //if range has been set, try to honour it and compare against cached shape
        if (r.length != shape.length) {
            log.debug("Cached array shape and set ranges on variable fragment differ!");
            return null;
        }
        //otherwise, check whether contents/lengths differ
        for (int i = 0; i < shape.length; i++) {
            if ((r[i].length() - shape[i]) != 0) {
                log.debug("Shape mismatch on component " + i + ". Set range was " + r.toString() + "; got shape from cache: " + shape[i]);
                return null;
            }
        }
        //only return the cached array if it matches our requested shape
        return sa;
    }

    private void putArrayIntoCache(IVariableFragment variableFragment, Array array) {
        getCache().put(getKeyForCache(variableFragment), new SerializableArray(array));
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<Array> readAll(final IFileFragment f) throws IOException,
            ResourceNotAvailableException {
        final ArrayList<IVariableFragment> al = readStructure(f);
        final ArrayList<Array> ral = new ArrayList<>(al.size());
        for (final IVariableFragment vf : al) {
            final Array a = readSingle(vf);
            ral.add(a);
        }
        return ral;
    }

    private class IndexedListArrayProvider implements IElementProvider<Array> {

        private final int size;
        private final int start;
        private final int numScans;
        private final int numPoints;
        private final Array scanIndexArray;
        private final Array dataArray;
        private final IVariableFragment variable;

        public IndexedListArrayProvider(IVariableFragment scanIndexVariable, IVariableFragment variable) throws ResourceNotAvailableException {
            Range[] oldRange = scanIndexVariable.getRange();
            int startIndex = 0;
            int scans = 0;
            if (oldRange != null) {
                startIndex = oldRange[0].first();
                scans = oldRange[0].length();
            }
            scanIndexVariable.setRange(new Range[0]);
            try {
                this.scanIndexArray = readSingle(scanIndexVariable);
                this.numScans = this.scanIndexArray.getShape()[0];
            } catch (IOException ex) {
                throw new ResourceNotAvailableException(ex);
            } catch (ResourceNotAvailableException ex) {
                throw ex;
            } finally {
                scanIndexVariable.setRange(oldRange);
            }
            oldRange = variable.getRange();
            variable.setRange(new Range[0]);
            try {
                this.dataArray = readSingle(variable);
                this.numPoints = this.dataArray.getShape()[0];
            } catch (IOException ex) {
                throw new ResourceNotAvailableException(ex);
            } catch (ResourceNotAvailableException ex) {
                throw ex;
            } finally {
                variable.setRange(oldRange);
            }
            this.variable = variable;
            this.start = startIndex;
            this.size = scans;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public long sizeLong() {
            return size;
        }

        @Override
        public Array get(int i) {
            if (i >= start && i < (start + size)) {
                try {
                    int[] startStop = getStartStopIndices(i, numScans, numPoints, start, scanIndexArray);
                    return dataArray.section(Arrays.asList(new Range(startStop[0], startStop[1])));
                } catch (ResourceNotAvailableException ex) {
                    throw ex;
                } catch (InvalidRangeException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new IndexOutOfBoundsException("Tried to access list at index " + i + "! Index access is only valid between " + start + " (inclusive) and " + (start + size) + " (exclusive)!");
        }

        private int[] getStartStopIndices(int i, int num_arrays, int num_points, int index_start, Array index_array) {

            int data_start = index_array.getInt((index_start + i));
            int data_end = data_start;
            // if we have reached the last scan start contained in index_array
            // use the length of the data array -1 as absolute end of last array
            if ((i + index_start + 1) == num_arrays) {
                data_end = num_points - 1;
            } else {
                data_end = index_array.getInt((index_start + i + 1)) - 1;
            }

            return new int[]{data_start, data_end};
        }

        @Override
        public List<Array> get(int start, int stop) {
            if (start >= this.start && stop < (this.start + size) && start <= stop) {
                ArrayList<Array> l = new ArrayList<Array>();
                for (int i = 0; i < stop - start + 1; i++) {
                    l.add(get(i));
                }
                return l;
            }
            throw new IndexOutOfBoundsException("Tried to access list between " + start + " until " + stop + "! Index access is only valid between " + start + " (inclusive) and " + (start + size - 1) + " (inclusive)!");
        }

        @Override
        public void reset() {

        }

        @Override
        public Array get(long l) {
            return get((int) l);
        }

        @Override
        public List<Array> get(long start, long stop) {
            return get((int) start, (int) stop);
        }
    }

    private class ArrayListAdapter extends ArrayList<Array> {

        private final List<Array> delegate;

        ArrayListAdapter(List<Array> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            if (o instanceof Array) {
                return delegate.contains((Array) o);
            }
            throw new IllegalArgumentException("Expected object if type ucar.ma2.Array!");
        }

        @Override
        public Iterator<Array> iterator() {
            return delegate.iterator();
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return delegate.toArray(a);
        }

        @Override
        public boolean add(Array e) {
            return delegate.add(e);
        }

        @Override
        public boolean remove(Object o) {
            if (o instanceof Array) {
                return delegate.remove((Array) o);
            }
            throw new IllegalArgumentException("Expected object if type ucar.ma2.Array!");
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return delegate.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends Array> c) {
            return delegate.addAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends Array> c) {
            return delegate.addAll(index, c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return delegate.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return delegate.retainAll(c);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof Array) {
                return delegate.equals((Array)o);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public Array get(int index) {
            return delegate.get(index);
        }

        @Override
        public Array set(int index, Array element) {
            return delegate.set(index, element);
        }

        @Override
        public void add(int index, Array element) {
            delegate.add(index, element);
        }

        @Override
        public Array remove(int index) {
            return delegate.remove(index);
        }

        @Override
        public int indexOf(Object o) {
            return delegate.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return delegate.lastIndexOf(o);
        }

        @Override
        public ListIterator<Array> listIterator() {
            return delegate.listIterator();
        }

        @Override
        public ListIterator<Array> listIterator(int index) {
            return delegate.listIterator(index);
        }

        @Override
        public List<Array> subList(int fromIndex, int toIndex) {
            return delegate.subList(fromIndex, toIndex);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<Array> readIndexed(final IVariableFragment f)
            throws IOException, ResourceNotAvailableException {
        EvalTools.notNull(f.getIndex(), this);
        return new ArrayListAdapter(new CachedLazyList<Array>(new IndexedListArrayProvider(f.getIndex(), f)));
//        MzMLUnmarshaller um = getUnmarshaller(f.getParent());
//        if (f.getName().equals(this.mass_values)) {
//            final ArrayList<Array> al = new ArrayList<>();
//            int start = 0;
//            int len = getScanCount(um);
//            if (f.getIndex() != null) {
//                Range[] r = f.getIndex().getRange();
//                if (r != null && r[0] != null) {
//                    start = Math.max(0, r[0].first());
//                    len = Math.min(len, r[0].length());
//                }
//            }
//            MzMLObjectIterator<Spectrum> spectrumIterator = um.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
//            int i = 0;
//            while (spectrumIterator.hasNext()) {
//                Spectrum spectrum = spectrumIterator.next();
//                if (i >= start && i < start + len) {
//                    al.add(getMassValues(spectrum));
//                    i++;
//                }
//            }
//            return al;
//        }
//        if (f.getName().equals(this.intensity_values)) {
//            final ArrayList<Array> al = new ArrayList<>();
//            int start = 0;
//            int len = getScanCount(um);
//            if (f.getIndex() != null) {
//                Range[] r = f.getIndex().getRange();
//                if (r != null && r[0] != null) {
//                    start = Math.max(0, r[0].first());
//                    len = Math.min(len, r[0].length());
//                }
//            }
//            MzMLObjectIterator<Spectrum> spectrumIterator = um.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
//            int i = 0;
//            while (spectrumIterator.hasNext()) {
//                Spectrum spectrum = spectrumIterator.next();
//                if (i >= start && i < start + len) {
//                    al.add(getIntensityValues(spectrum));
//                    i++;
//                }
//            }
//            return al;
//        }
//        // return an empty list as default
//        return new ArrayList<>();
    }

    private Array readSourceFiles(final IFileFragment f, final MzMLUnmarshaller mzmu) {
        SourceFileList sfl = getSourceFiles(mzmu);
        List<IFileFragment> sourceFilePaths = new LinkedList<>();
        Set<String> allowedSchemes = new HashSet<>(Arrays.asList(new String[]{"http", "https", "ftp"}));
        for (SourceFile sfs : sfl.getSourceFile()) {
            try {
                IFileFragment fragment = new FileFragment(URI.create(sfs.getLocation()));
                File fragmentFile = new File(fragment.getUri());
                if ((fragmentFile.isFile() && fragmentFile.exists()) || (allowedSchemes.contains(fragment.getUri().getScheme()))) {
                    sourceFilePaths.add(fragment);
                } else {
                    log.info("Not adding non-existant source file to active source files of " + f.getUri());
                }
            } catch (java.lang.IllegalArgumentException ex) {
                log.warn("Location is not a valid URI: {}", sfs.getLocation());
            }
        }
        if (sourceFilePaths.isEmpty()) {
            return null;
        }
        Array a = FragmentTools.createSourceFilesArray(f, sourceFilePaths);
        log.info("Returning source files: ", a);
        return a;
    }

    private Array readElutionTimeArray(final IVariableFragment var, final MzMLUnmarshaller um, final String accession) {
        int scans = getScanCount(um);
        int start = 0;
        final Range[] r = var.getRange();
        if (r != null) {
            start = Math.max(0, r[0].first());
            scans = Math.min(scans, r[0].length());
        }
        log.debug("Creating index array with {} elements", scans);
        final ArrayDouble.D1 elutionTime = new ArrayDouble.D1(scans);
        MzMLObjectIterator<Spectrum> spectrumIterator = um.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
        int i = 0;
        while (spectrumIterator.hasNext()) {
            Spectrum s = spectrumIterator.next();
            if (i >= start && i < start + scans) {
                double rt = readElutionTime(s, accession);
                //fallback for legacy maltcms < 1.3.1
                if (Double.isNaN(rt)) {
                    try {
                        List<CVParam> cvParams = s.getCvParam();
                        CVParam cvp = findParam(cvParams, accession);
                        String value = cvp.getValue();
                        rt = convertRT(Double.parseDouble(value), cvp.getUnitName());
                    } catch (NullPointerException | ResourceNotAvailableException npe) {
                        log.warn("Could not retrieve legacy elution time!");
                    }
                }
                elutionTime.set(i, rt);
                i++;
            }
        }
        EvalTools.notNull(elutionTime, this);
        return elutionTime;
    }

    private double readElutionTime(Spectrum s, final String accession) {
        double rt = Double.NaN;
        try {
            CVParam rtp = findParam(s.getScanList().getScan().get(0).getCvParam(), accession);
            rt = Double.parseDouble(rtp.getValue());
            String unit = getRTUnit(s, accession);
            rt = convertRT(rt, unit);
        } catch (NullPointerException npe) {
            log.warn("Could not retrieve elution time!");
        } catch (ResourceNotAvailableException rne) {
            log.warn("Could not retrieve elution time!", rne);
        }
        return rt;
    }

    private Array readMinMaxMassValueArray(final IVariableFragment var,
            final MzMLUnmarshaller um) {
        log.debug("readMinMaxMassValueArray");
        final Tuple2D<Array, Array> t = initMinMaxMZ(var, um);
        if (var.getName().equals(this.mass_range_min)) {
            return t.getFirst();
        }
        if (var.getName().equals(this.mass_range_max)) {
            return t.getSecond();
        }
        throw new IllegalArgumentException(
                "Method accepts only one of mass_range_min or mass_range_max as varname!");
    }

    private Array readTicFromMzi(final IVariableFragment var, final MzMLUnmarshaller um) {
        if (var.getIndex() == null) {
            IVariableFragment scanIndex = null;
            try {
                scanIndex = var.getParent().getChild(scan_index);
            } catch (ResourceNotAvailableException rnae) {
                scanIndex = var.getParent().addChild(scan_index);
            }
            var.setIndex(scanIndex);
        }
        int start = 0;
        int scans = getScanCount(um);
        final Range[] r = var.getIndex().getRange();
        if (r != null) {
            start = Math.max(0, r[0].first());
            scans = Math.min(scans, r[0].length());
        }
        ArrayDouble.D1 tic = new ArrayDouble.D1(scans);
        log.debug("Reading from {} to {} (inclusive)", start, start + scans - 1);
        if (var.getName().equals(this.intensity_values)) {
            double sum = 0.0d;
            MzMLObjectIterator<Spectrum> spectrumIterator = um.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
            int i = 0;
            while (spectrumIterator.hasNext()) {
                Spectrum spectrum = spectrumIterator.next();
                if (i >= start && i < start + scans) {
                    log.debug("Reading scan {} of {}", (i + 1), scans);
                    tic.setDouble(i, MAMath.sumDouble(getIntensityValues(spectrum)));
                    i++;
                }
            }
            return tic;
        }
        throw new IllegalArgumentException(
                "Don't know how to handle variable: " + var.getName());
        // }
        // return f.getArray();
    }

    private short getMsLevel(Spectrum s) {
        try {
            CVParam param = findParam(s.getCvParam(), msLevelAccession);
            short paramMsLevel = 0;
            if (param != null && !param.getValue().isEmpty()) {
                paramMsLevel = Short.parseShort(param.getValue());
            }
            return paramMsLevel;
        } catch (NullPointerException | ResourceNotAvailableException npe) {
            log.warn("Could not retrieve ms level for spectrum {}!", s.getId());
        }
        return 0;
    }

    private Array readArray(final IVariableFragment var, final MzMLUnmarshaller um) {
        log.debug("read " + var.getName() + " array");
        Set<String> variableNames = new LinkedHashSet<>(Arrays.asList(mass_values, intensity_values, total_intensity, mass_range_min, mass_range_max, scan_acquisition_time, scan_index, ms_level));
        if (variableNames.contains(var.getName())) {
            SerializableArray a = getArrayFromCache(var);
            if (a == null) {
                applyIteratedSpectrumFunctions(um, var.getParent());
            }
            return getArrayFromCache(var).getArray();
        }
        return null;
    }

//    private class TICFunction extends SpectrumFunction<Array> {
//
//        @Override
//        Array createArray(Spectrum s, int scans) {
//            createCompatibleArray(getIntensityBinaryDataArray(s).getPrecision());
//        }
//        
//    }
//    private class MinMaxMassFunction extends SpectrumFunction<Array> {
//
//        private Array maxMassValues;
//        private IVariableFragment maxMassVariable;
//
//        MinMaxMassFunction(IVariableFragment minMassVariable, IVariableFragment maxMassVariable) {
//            super(minMassVariable);
//            this.maxMassVariable = maxMassVariable;
//        }
//
//        @Override
//        void apply(Spectrum spectrum, int i, int scans) {
//            super.apply(spectrum, i, scans);
//            Tuple2D<Double, Double> mm = getMinMaxMassRange(spectrum);
//            super.array.setDouble(i, mm.getFirst());
//            maxMassValues.setDouble(i, mm.getSecond());
//        }
//
//        @Override
//        Array createArray(Spectrum s, int scans) {
//            this.maxMassValues = createCompatibleArray(getMzBinaryDataArray(s).getPrecision(), new int[]{scans});
//            return this.maxMassValues.copy();
//        }
//
//        @Override
//        void addArray(IteratedSpectrumFunction isf) {
//            isf.addArray(variable, super.array);
//            isf.addArray(maxMassVariable, maxMassValues);
//        }
//
//    }
//
//    private class MaxMassFunction extends SpectrumFunction<Array> {
//
//        MaxMassFunction(IVariableFragment variable) {
//            super(variable);
//        }
//
//        @Override
//        void apply(Spectrum spectrum, int i, int scans) {
//            super.apply(spectrum, i, scans);
//        }
//
//        @Override
//        Array createArray(Spectrum s, int scans) {
//            return createCompatibleArray(getMzBinaryDataArray(s).getPrecision(), new int[]{scans});
//        }
//
//    }
    private class MassesFunction extends SpectrumFunction<Array> {

        private Array minMassesArray, maxMassesArray;
        private CachedReadWriteList<Array> crwl;
        private IVariableFragment massesVariable, minMassesVariable, maxMassesVariable;

        public MassesFunction(IVariableFragment massesVariable, IVariableFragment minMassesVariable, IVariableFragment maxMassesVariable) {
            this.massesVariable = massesVariable;
            this.minMassesVariable = minMassesVariable;
            this.maxMassesVariable = maxMassesVariable;
            this.crwl = new CachedReadWriteList<>(massesVariable.getParent().getUri().toString() + ">" + massesVariable.getName() + "-massesFunction", new ISerializationProxy<Array>() {

                @Override
                public Serializable convert(Array t) {
                    return new SerializableArray(t);
                }

                @Override
                public Array reverseConvert(Object o) {
                    return ((SerializableArray) o).getArray();
                }
            });
        }

        @Override
        void apply(Spectrum spectrum, int i, int scans) {
            Precision p = getMzBinaryDataArray(spectrum).getPrecision();
            if (minMassesArray == null || maxMassesArray == null) {
                minMassesArray = createCompatibleArray(p, new int[]{scans});
                maxMassesArray = minMassesArray.copy();
            }
            final Array b = getMassValues(spectrum);
            this.crwl.add(b);
        }

        @Override
        Array createArray(Spectrum s, int scans) {
            Precision p = getMzBinaryDataArray(s).getPrecision();
            return createCompatibleArray(p, new int[]{scans});
        }

        @Override
        void addArray(IteratedSpectrumFunction isf) {
            isf.addArray(minMassesVariable, minMassesArray);
            isf.addArray(maxMassesVariable, maxMassesArray);
//            isf.addArray(intensityVariable, ArrayTools.glue(crwl));
        }

    }

    private class IntensitiesFunction extends SpectrumFunction<Array> {

        private Array ticArray;
        private CachedReadWriteList<Array> crwl;
        private IVariableFragment intensityVariable;
        private IVariableFragment ticVariable;

        public IntensitiesFunction(IVariableFragment intensityVariable, IVariableFragment ticVariable) {
            this.intensityVariable = intensityVariable;
            this.ticVariable = ticVariable;
            this.crwl = new CachedReadWriteList<>(intensityVariable.getParent().getUri().toString() + ">" + intensityVariable.getName() + "-intensitiesFunction", new ISerializationProxy<Array>() {

                @Override
                public Serializable convert(Array t) {
                    return new SerializableArray(t);
                }

                @Override
                public Array reverseConvert(Object o) {
                    return ((SerializableArray) o).getArray();
                }
            });
        }

        @Override
        void apply(Spectrum spectrum, int i, int scans) {
            if (ticArray == null) {
                ticArray = createArray(spectrum, scans);
            }
            final Array b = getIntensityValues(spectrum);
            this.crwl.add(b);
            ticArray.setDouble(i, MAMath.sumDouble(b));
        }

        @Override
        Array createArray(Spectrum s, int scans) {
            Precision p = getIntensityBinaryDataArray(s).getPrecision();
            return createCompatibleArray(p, new int[]{scans});
        }

        @Override
        void addArray(IteratedSpectrumFunction isf) {
            isf.addArray(ticVariable, ticArray);
            isf.addArray(intensityVariable, ArrayTools.glue(crwl));
        }

    }

    private class ScanAcquisitionTimeFunction extends SingleVariableSpectrumFunction<ArrayDouble.D1> {

        ScanAcquisitionTimeFunction(IVariableFragment variable) {
            super(variable);
        }

        @Override
        void apply(Spectrum spectrum, int i, int scans) {
            super.apply(spectrum, i, scans);
            super.array.set(i, getRT(spectrum));
            log.debug("RT({})={}", i, super.array.get(i));
        }

        @Override
        ArrayDouble.D1 createArray(Spectrum s, int scans) {
            return createArray(DataType.DOUBLE, scans);
        }

    }

    private class MsLevelFunction extends SingleVariableSpectrumFunction<ArrayShort.D1> {

        MsLevelFunction(IVariableFragment variable) {
            super(variable);
        }

        @Override
        void apply(Spectrum spectrum, int i, int scans) {
            super.apply(spectrum, i, scans);
            super.array.set(i, getMsLevel(spectrum));
            log.debug("MS_LEVEL({})={}", i, super.array.get(i));
        }

        @Override
        ArrayShort.D1 createArray(Spectrum s, int scans) {
            return createArray(DataType.SHORT, scans);
        }

    }

    private void applyIteratedSpectrumFunctions(MzMLUnmarshaller um, IFileFragment fileFragment) {
        Range commonRange = new Range(getScanCount(um));
        try {
            List<IVariableFragment> variables = readStructure(fileFragment);
            for (IVariableFragment ivf : variables) {
                if (ivf.getName().equals(this.mass_values) || ivf.getName().equals(this.intensity_values)) {
                    //skipping, range may be different on these variables
                } else {
                    Range varRange = ivf.getRange()[0];
                    if (varRange != null) {
                        try {
                            commonRange = commonRange.intersect(varRange);
                        } catch (InvalidRangeException ex) {
                            Logger.getLogger(MZMLDataSource.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
//            IteratedSpectrumFunction isf = new IteratedSpectrumFunction(um, commonRange,
//                    new ScanIndexFunction(fileFragment.getChild(scan_index)),
//                    new ScanAcquisitionTimeFunction(fileFragment.getChild(scan_acquisition_time)),
//                    new MassesFunction(fileFragment.getChild(mass_values)),
//                    new IntensitiesFunction(fileFragment.getChild(intensity_values)),
//                    new MsLevelFunction(fileFragment.getChild(ms_level)),
//                    new MinMaxMassFunction(fileFragment.getChild(mass_range_min), fileFragment.getChild(mass_range_max))
//            );
//            isf.apply(um, commonRange);
        } catch (IOException ex) {
            Logger.getLogger(MZMLDataSource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class ScanIndexFunction extends SingleVariableSpectrumFunction<ArrayInt.D1> {

        ScanIndexFunction(IVariableFragment variable) {
            super(variable);
        }

        @Override
        void apply(Spectrum spectrum, int i, int scans) {
            super.apply(spectrum, i, scans);
            if (i == 0) {
                super.array.set(i, 0);
            } else {
                super.array.set(i, super.array.get(i - 1) + getPointCount(spectrum));
            }
            log.debug("SCAN_INDEX({})={}", i, super.array.get(i));
        }

        @Override
        ArrayInt.D1 createArray(Spectrum s, int scans) {
            return createArray(DataType.INT, scans);
        }

    }

    private abstract class SingleVariableSpectrumFunction<T extends Array> extends SpectrumFunction<T> {

        T array;
        IVariableFragment variable;

        SingleVariableSpectrumFunction(IVariableFragment variable) {
            super();
            this.variable = variable;
        }

        @Override
        void apply(Spectrum spectrum, int i, int scans) {
            if (array == null) {
                array = createArray(spectrum, scans);
            }
        }

        @Override
        void addArray(IteratedSpectrumFunction isf) {
            isf.addArray(variable, array);
        }

    }

    private abstract class SpectrumFunction<T extends Array> {

        SpectrumFunction() {

        }

        T createArray(DataType arrayType, int scans) {
            return (T) Array.factory(arrayType, new int[]{scans});
        }

        abstract T createArray(Spectrum s, int scans);

        abstract void apply(Spectrum spectrum, int i, int scans);

        abstract void addArray(IteratedSpectrumFunction isf);

    }

    private class IteratedSpectrumFunction {

        private int start;
        private int scans;
        private final MzMLObjectIterator<Spectrum> spectrumIterator;
        private final SpectrumFunction[] spectrumFunction;

        public IteratedSpectrumFunction(MzMLUnmarshaller um, Range range, SpectrumFunction... spectrumFunction) {
            this.spectrumIterator = um.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
            this.spectrumFunction = spectrumFunction;
        }

        final void apply(MzMLUnmarshaller um, Range range) {
            scans = getScanCount(um);
            start = 0;
            if (range != null) {
                start = Math.max(0, range.first());
                scans = Math.min(scans, range.length());
            }
            int i = 0;
            while (spectrumIterator.hasNext()) {
                Spectrum spectrum = spectrumIterator.next();
                if (i >= start && i < start + scans) {
                    for (SpectrumFunction function : spectrumFunction) {
                        function.apply(spectrum, i, scans);
                    }
                    i++;
                }
            }
            for (SpectrumFunction function : spectrumFunction) {
                function.addArray(this);
            }
        }

        public void addArray(IVariableFragment variable, Array array) {
            getCache().put(variable.getParent().getUri() + ">" + variable.getName(), new SerializableArray(array));
        }
    }

    private Array readScanIndex(final IVariableFragment var, final MzMLUnmarshaller um) {
        return null;
//        return readScanIndexArray(var, um);
//        int npeaks = 0;
//        int scans = getScanCount(um);
//        int start = 0;
//        final Range[] r = var.getRange();
//        if (r != null) {
//            start = Math.max(0, r[0].first());
//            scans = Math.min(scans, r[0].length());
//        }
//        log.debug("Creating index array with {} elements", scans);
//        final ArrayInt.D1 scan_index = new ArrayInt.D1(scans);
//        MzMLObjectIterator<Spectrum> spectrumIterator = um.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
//        int i = 0;
//        while (spectrumIterator.hasNext()) {
//            Spectrum spectrum = spectrumIterator.next();
//            if (i >= start && i < start + scans) {
//                final int peaks = spectrum.getDefaultArrayLength();
//                // current npeaks is index into larger arrays for current scan
//                log.debug("Scan {} from {} to {}", new Object[]{i, npeaks,
//                    (npeaks + peaks - 1)});
//                scan_index.set(i, npeaks);
//                npeaks += peaks;
//                i++;
//            }
//        }
//        EvalTools.notNull(scan_index, this);
//        return scan_index;
    }

    private Array readModulationTimeArray(IVariableFragment var, MzMLUnmarshaller um) {
        MzMLObjectIterator<CVParam> parameters = um.unmarshalCollectionFromXpath("/run/cvParam", CVParam.class);
        while (parameters.hasNext()) {
            CVParam param = parameters.next();
            if (param.getAccession().equalsIgnoreCase(modulationTimeAccession)) {
                log.info("Found modulation time parameter as attribute of run");
                ArrayDouble.D0 a = new ArrayDouble.D0();
                a.set(Double.parseDouble(param.getValue()));
                String rtUnit = param.getUnitName();
                if (rtUnit.equalsIgnoreCase("second")) {
                } else if (rtUnit.equalsIgnoreCase("minute")) {
                    //convert to seconds
                    a.set(a.get() / 60.0);
                } else {
                    throw new RuntimeException("Could not convert modulation time unit: " + rtUnit + "!");
                }
                return a;
            }
        }
        throw new ResourceNotAvailableException("Unknown variable name to mzML mapping for " + var.getName());
    }

    /** {@inheritDoc} */
    @Override
    public Array readSingle(final IVariableFragment f) throws IOException,
            ResourceNotAvailableException {
        log.debug("readSingle of {} in {}", f.getName(), f.getParent().getUri());
        if (f.hasArray()) {
            log.warn("{} already has an array set!", f);
        }
        final Array a = loadArray(f.getParent(), f);
        if (a == null) {
            throw new ResourceNotAvailableException("Could not find variable "
                    + f.getName() + " in file " + f.getParent().getName());
        }
        // f.setArray(a);
        return a;
    }

    /*
     * (non-Javadoc)
     *
     * @seecross.io.IDataSource#readStructure(cross.datastructures.fragments.
     * IFileFragment)
     */
    /** {@inheritDoc} */
    @Override
    public ArrayList<IVariableFragment> readStructure(final IFileFragment f)
            throws IOException {
        final ArrayList<IVariableFragment> al = new ArrayList<>();
        final IVariableFragment ti = getVariable(f, this.total_intensity);
        final IVariableFragment sat = getVariable(f, this.scan_acquisition_time);
        final IVariableFragment si = getVariable(f, this.scan_index);
        final IVariableFragment mrmin = getVariable(f, this.mass_range_min);
        final IVariableFragment mrmax = getVariable(f, this.mass_range_max);
        final IVariableFragment mv = getVariable(f, this.mass_values);
        final IVariableFragment iv = getVariable(f, this.intensity_values);
        final IVariableFragment msLevel = getVariable(f, this.ms_level);
//		final IVariableFragment tic = getVariable(f, this.total_ion_current_chromatogram);
//		final IVariableFragment ticSat = getVariable(f, this.total_ion_current_chromatogram_scan_acquisition_time);
        //TODO add first and second_column_elution_time with fast query, whether they
        //are contained in the file
        al.addAll(Arrays.asList(new IVariableFragment[]{ti, sat, si, mrmin,
            mrmax, mv, iv, msLevel}));//, tic, ticSat}));
        for (final IVariableFragment ivf : al) {
            readStructure(ivf);
        }
        return al;
    }

    /*
     * (non-Javadoc)
     *
     * @seecross.io.IDataSource#readStructure(cross.datastructures.fragments.
     * IVariableFragment)
     */
    /** {@inheritDoc} */
    @Override
    public IVariableFragment readStructure(final IVariableFragment f)
            throws IOException, ResourceNotAvailableException {
        MzMLUnmarshaller um = getUnmarshaller(f.getParent());
        final int scancount = getScanCount(um);
        final String varname = f.getName();
        // Read mass_values or intensity_values for whole chromatogram
        if (varname.equals(this.source_files)) {
            Array a = readSourceFiles(f.getParent(), um);
            final Dimension d1 = new Dimension("source_file_number", a.getShape()[0], true);
            final Dimension d2 = new Dimension("source_file_max_chars", a.getShape()[1], true);
            final Dimension[] dims = new Dimension[]{d1, d2};
            f.setDimensions(dims);
            final Range[] ranges = new Range[]{new Range(d1.getLength()), new Range(d2.getLength())};
            f.setRange(ranges);
        } else if (varname.equals(this.scan_index)
                || varname.equals(this.total_intensity)
                || varname.equals(this.mass_range_min)
                || varname.equals(this.mass_range_max)
                || varname.equals(this.scan_acquisition_time)
                || varname.equals(this.first_column_elution_time)
                || varname.equals(this.second_column_elution_time)
                || varname.equals(this.ms_level)) {
            final Dimension[] dims = new Dimension[]{new Dimension(
                "scan_number", scancount, true)};
            f.setDimensions(dims);
            final Range[] ranges;
            try {
                ranges = new Range[]{new Range(0, scancount - 1)};
                f.setRange(ranges);
            } catch (InvalidRangeException ex) {
                log.warn("Invalid range: ", ex);
            }
        } else if (varname.equals(this.mass_values)
                || varname.equals(this.intensity_values)) {
            int npeaks = 0;
            try {
                MzMLObjectIterator<Spectrum> spectrumIterator = um.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
                while (spectrumIterator.hasNext()) {
                    Spectrum spectrum = spectrumIterator.next();
                    npeaks += getPointCount(spectrum);
                }
                final Dimension[] dims = new Dimension[]{new Dimension(
                    "point_number", npeaks, true)};
                f.setDimensions(dims);
                final Range[] ranges;
                try {
                    ranges = new Range[]{new Range(0, npeaks - 1)};
                    f.setRange(ranges);
                } catch (InvalidRangeException ex) {
                    log.warn("Invalid range: ", ex);
                }

            } catch (final NullPointerException npe) {
                throw new ResourceNotAvailableException(
                        "Could not read header of file "
                        + f.getParent().getUri());
            }
        } else if (varname.equals(this.modulation_time)) {
            Array a = readModulationTimeArray(f, um);
            final Dimension[] dims = new Dimension[]{new Dimension(this.modulation_time, 1, true, false, false)};
            f.setDimensions(dims);
            final Range[] ranges = new Range[]{new Range(1)};
            f.setRange(ranges);
        } else if (varname.equals(this.total_ion_current_chromatogram) || varname.equals(this.total_ion_current_chromatogram_scan_acquisition_time)) {
            Chromatogram c = readTotalIonCurrentChromatogram(um, f.getParent());
            final Dimension[] dims = new Dimension[]{new Dimension("total_ion_current_chromatogram_scan_number", c.getDefaultArrayLength(), true, false, false)};
            f.setDimensions(dims);
            final Range[] ranges = new Range[]{new Range(c.getDefaultArrayLength())};
            f.setRange(ranges);
        } else {
            throw new ResourceNotAvailableException(
                    "Unknown varname to mzML mapping for varname " + varname);
        }
        return f;
    }

    private Array readTotalIntensitiesArray(final IFileFragment f, final String fallback, final MzMLUnmarshaller um) {
        // Current assumption is, that global time for ms scans correspond to
        // chromatogram times
        Set<String> chromatograms = um.getChromatogramIDs();
        if (chromatograms != null && !chromatograms.isEmpty()) {
            Chromatogram ticChromatogram = null;
            try {
                ticChromatogram = readTotalIonCurrentChromatogram(um, f);
                BinaryDataArray bda = getBinaryDataArrayForCV(ticChromatogram, "MS:1000235");
                if (bda != null) {
                    return fillArrayFromBinaryData(bda);
                }
            } catch (ResourceNotAvailableException rnae) {
                //ignore, will handle null chromatogram further down
            }

        }
        log.warn("No TIC chromatograms defined in mzML file {} reconstructing from spectra!", f.getUri());
        return readTicFromMzi(f.getChild(fallback), um);
    }

    private BinaryDataArray getBinaryDataArrayForCV(Chromatogram chrom, String cv) {
        // time array MS:1000595
        // intensity array MS:1000515
        for (BinaryDataArray bda : chrom.getBinaryDataArrayList().getBinaryDataArray()) {
            try {
                CVParam param = findParam(bda.getCvParam(), cv);
                if (param != null) {
                    return bda;
                }
            } catch (ResourceNotAvailableException rnae) {
                //this can be ignored
            }
        }
        return null;
    }

    private Array readTotalIonCurrentChromatogram(final IVariableFragment f, MzMLUnmarshaller um, boolean loadTotalIonCurrentChromatogram) {
        Set<String> chromatograms = um.getChromatogramIDs();
        if (chromatograms == null || chromatograms.isEmpty()) {
            throw new ResourceNotAvailableException("No chromatograms defined in mzML file {}" + f.getName());
        }
        Chromatogram ticChromatogram = readTotalIonCurrentChromatogram(um, f.getParent());
        // time array MS:1000595
        // intensity array MS:1000515
        if (loadTotalIonCurrentChromatogram) {
            BinaryDataArray intensitiesArray = getBinaryDataArrayForCV(ticChromatogram, "MS:1000515");
            if (intensitiesArray == null) {
                throw new ResourceNotAvailableException("Could not retrieve binary data array with CV term MS:1000515 = 'intensity array'");
            }
            Number[] intensities = intensitiesArray.getBinaryDataAsNumberArray();
            final ArrayDouble.D1 intensA = new ArrayDouble.D1(intensities.length);
            for (int i = 0; i < intensities.length; i++) {
                intensA.set(i, intensities[i].doubleValue());
            }
            return intensA;
        } else {
            //load time array
            BinaryDataArray ticSatArray = getBinaryDataArrayForCV(ticChromatogram, "MS:1000595");
            if (ticSatArray == null) {
                throw new ResourceNotAvailableException("Could not retrieve binary data array with CV term MS:1000595 = 'time array'");
            }
            Number[] time = ticSatArray.getBinaryDataAsNumberArray();
            final ArrayDouble.D1 ticSats = new ArrayDouble.D1(time.length);
            CVParam rtp = null;
            String unit = "second";
            try {
                rtp = findParam(ticSatArray.getCvParam(), "MS:1000595");
                unit = rtp.getUnitName();
            } catch (ResourceNotAvailableException rna) {
                log.warn("Could not find time unit for chromatogram {}! Assuming seconds!", ticChromatogram.getId());
            }
            for (int i = 0; i < time.length; i++) {
                double value = convertRT(time[i].doubleValue(), unit);
                ticSats.set(i, value);
            }
            return ticSats;
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<String> supportedFormats() {
        return Arrays.asList(this.fileEnding);
    }

    /** {@inheritDoc} */
    @Override
    public boolean write(final IFileFragment f) {
        EvalTools.notNull(this.ndf, this);
        // TODO Implement real write support
        log.info("Saving {} with MZMLDataSource", f.getUri());
        log.info("Changing output file from: {}", f.toString());
        File file = new File(f.getUri());
        String filename = StringTools.removeFileExt(file.getAbsolutePath());
        filename += ".cdf";
        f.setFile(filename);
        f.addSourceFile(new FileFragment(f.getUri()));
        log.info("To: {}", filename);
        return Factory.getInstance().getDataSourceFactory().getDataSourceFor(f).write(f);
    }

    private Chromatogram readTotalIonCurrentChromatogram(MzMLUnmarshaller um, final IFileFragment f) {
        Chromatogram ticChromatogram = null;
        Set<String> chromatogramIds = um.getChromatogramIDs();
        for (String id : chromatogramIds) {
            try {
                Chromatogram c = um.getChromatogramById(id);
                CVParam param = findParam(c.getCvParam(), "MS:1000235");
                if (param != null) {
                    if (ticChromatogram != null) {
                        log.warn("mzML file {} contains more than one tic chromatogram, defaulting to first!", f.getName());
                    } else {
                        ticChromatogram = c;
                    }
                }
            } catch (ResourceNotAvailableException rnae) {
                log.debug("Chromatogram is not a 'total ion current chromatogram'");
            } catch (MzMLUnmarshallerException ex) {
                log.warn("Failed to unmarshal chromatogram {}", id);
            }
        }
        if (ticChromatogram == null) {
            throw new ResourceNotAvailableException("Could not retrieve tic chromatogram for file " + f.getName());
        }
        return ticChromatogram;
    }
}
