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
import cross.datastructures.cache.SerializableArray;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableVariableFragment2;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FragmentTools;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.tools.StringTools;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import maltcms.commands.fragments.io.MZMLExporter;

import maltcms.io.andims.NetcdfDataSource;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
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
 * <p>MZMLDataSource class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@ServiceProvider(service = IDataSource.class)
public class MZMLDataSource implements IDataSource {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MZMLDataSource.class);

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
    private static final ICacheDelegate<URI, MzMLUnmarshaller> fileToIndex = CacheFactory.createVolatileCache(MZMLDataSource.class.getName() + "-unmarshaller", 3600, 7200, 100);
    private static final ICacheDelegate<MzMLUnmarshaller, Run> unmarshallerToRun = CacheFactory.createVolatileCache(MZMLDataSource.class.getName() + "-unmarshaller-to-run", 3600, 7200, 20);
    private static final ICacheDelegate<String, SerializableArray> variableToArrayCache = CacheFactory.createVolatileCache("maltcms.io.readcache");

    private ICacheDelegate<String, SerializableArray> getCache() {
        return MZMLDataSource.variableToArrayCache;
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
        MzMLUnmarshaller um = MZMLDataSource.fileToIndex.get(ff.getUri());
        if (um != null) {
            log.info("Retrieved unmarshaller from cache!");
            return um;
        }
        try {
            if (new File(ff.getUri()).exists()) {
                log.debug("Initializing unmarshaller for file {}", ff.getUri());
                um = new MzMLUnmarshaller(ff.getUri().toURL(), true);
                MZMLDataSource.fileToIndex.put(ff.getUri(), um);
                log.debug("mzML file {} is indexed: {}", ff.getUri(), um.isIndexedmzML());
                return um;
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
        throw new ResourceNotAvailableException("File fragment " + ff.getUri() + " does not exist!");
    }

    private Run getRun(MzMLUnmarshaller mzmlu) {
        Run run = MZMLDataSource.unmarshallerToRun.get(mzmlu);
        if (run != null) {
            log.debug("Retrieved run from cache!");
            return run;
        }
        run = mzmlu.unmarshalFromXpath("/run", Run.class);
        MZMLDataSource.unmarshallerToRun.put(mzmlu, run);
        return run;
    }

    private SourceFileList getSourceFiles(MzMLUnmarshaller mzmlu) {
        log.info("Retrieving fileDescription/sourceFileList from mzML");
        FileDescription fd = mzmlu.unmarshalFromXpath("/fileDescription", FileDescription.class);
        if (fd == null) {
            throw new ResourceNotAvailableException("Could not retrieve fileDescription element in mzML file.");
        }
        SourceFileList sfl = fd.getSourceFileList();
        return sfl;
    }

    /*
     * FIXME unify spectrum iteration, so that all relevant CV terms are read and cached
     * if the whole spectra list is iterated to avoid repeated iteration.
     */
    private Spectrum getSpectrum(MzMLUnmarshaller um, int idx) {
        try {
            if (um.isIndexedmzML()) {
                log.debug("Retrieving spectrum by id from spectrum index!");
                return um.getSpectrumById(um.getSpectrumIDFromSpectrumIndex(idx));
            } else {
                log.warn("Not using indexed mzML, this is really inefficient!");
                return getRun(um).getSpectrumList().getSpectrum().get(idx);
            }
        } catch (NullPointerException npe) {
            log.warn("Not using indexed mzML, this is really inefficient!");
            return getRun(um).getSpectrumList().getSpectrum().get(idx);
        } catch (MzMLUnmarshallerException ex) {
            java.util.logging.Logger.getLogger(MZMLDataSource.class.getName()).log(Level.SEVERE, null, ex);
            throw new ResourceNotAvailableException(ex);
        }
    }

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

            }
        }
        log.warn("m/z binary data array is not annotated with expected CVParam MS:1000514. Falling back to selecting the first binary data array in list!");
        if (s.getBinaryDataArrayList().getCount() > 0) {
            return s.getBinaryDataArrayList().getBinaryDataArray().get(0);
        }
        throw new ResourceNotAvailableException("Could not find m/z binary data array as child of spectrum " + s.getId());
    }

    private BinaryDataArray getIntensityBinaryDataArray(final Spectrum s) {
        for (BinaryDataArray bda : s.getBinaryDataArrayList().getBinaryDataArray()) {
            try {
                log.debug("CVs for BinaryDataArray: {}", bda.getCvParam());
                CVParam param = findParam(bda.getCvParam(), "MS:1000515");
                if (param != null) {
                    return bda;
                }
            } catch (ResourceNotAvailableException rnae) {

            }
        }
        log.warn("intensity binary data array is not annotated with expected CVParam MS:1000515. Falling back to selecting the second binary data array in list!");
        if (s.getBinaryDataArrayList().getCount() > 1) {
            return s.getBinaryDataArrayList().getBinaryDataArray().get(1);
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
     * @param um a {@link uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller} object.
     * @return a Tuple2D<Array,Array> with mass_range_min as first and
     * mass_range_max as second array
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
        SerializableArray sa = getCache().get(getVariableCacheKey(var));
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
        }
        if (varname.equals(this.mass_values)
                || varname.equals(this.intensity_values)) {
            a = readMZI(var, mzu);
        } else if (varname.equals(this.scan_index)) {
            a = readScanIndex(var, mzu);
            // read total_intensity
        } else if (varname.equals(this.total_intensity)) {
            a = readTotalIntensitiesArray(var.getParent(), intensity_values, mzu);
            // read min and max_mass_range
        } else if (varname.equals(this.mass_range_min)
                || varname.equals(this.mass_range_max)) {
            sa = readMinMaxMassValueArray(var, mzu);
            return sa.getArray();
            // read scan_acquisition_time
        } else if (varname.equals(this.scan_acquisition_time)) {
            a = readScanAcquisitionTimeArray(var, mzu);
        } else if (varname.equals(this.modulation_time)) {
            a = readModulationTimeArray(var, mzu);
        } else if (varname.equals(this.ms_level)) {
            a = readMsLevelArray(var, mzu);
        } else if (varname.equals(this.total_ion_current_chromatogram)) {
            a = readTotalIonCurrentChromatogram(var, mzu, true);
        } else if (varname.equals(this.total_ion_current_chromatogram_scan_acquisition_time)) {
            a = readTotalIonCurrentChromatogram(var, mzu, false);
        } else if (varname.equals(this.first_column_elution_time)) {
            a = readElutionTimeArray(var, mzu, this.first_column_elution_timeAccession);
        } else if (varname.equals(this.second_column_elution_time)) {
            a = readElutionTimeArray(var, mzu, this.second_column_elution_timeAccession);
        } else {
            throw new ResourceNotAvailableException(
                    "Unknown variable name to mzML mapping for " + varname);
        }
        if (a != null) {
            getCache().put(getVariableCacheKey(var), new SerializableArray(a));
        } else {
            throw new ResourceNotAvailableException("Array for variable " + var.getName() + " could not be loaded!");
        }
        return a;
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

    /** {@inheritDoc} */
    @Override
    public ArrayList<Array> readIndexed(final IVariableFragment f)
            throws IOException, ResourceNotAvailableException {
        MzMLUnmarshaller um = getUnmarshaller(f.getParent());
        if (f.getName().equals(this.mass_values)) {
            final ArrayList<Array> al = new ArrayList<>();
            int start = 0;
            int len = getScanCount(um);
            if (f.getIndex() != null) {
                Range[] r = f.getIndex().getRange();
                if (r != null && r[0] != null) {
                    start = Math.max(0, r[0].first());
                    len = Math.min(len, r[0].length());
                }
            }
            MzMLObjectIterator<Spectrum> spectrumIterator = um.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
            int i = 0;
            while (spectrumIterator.hasNext()) {
                Spectrum spectrum = spectrumIterator.next();
                if (i >= start && i < start + len) {
                    al.add(getMassValues(spectrum));
                    i++;
                }
            }
            return al;
        }
        if (f.getName().equals(this.intensity_values)) {
            final ArrayList<Array> al = new ArrayList<>();
            int start = 0;
            int len = getScanCount(um);
            if (f.getIndex() != null) {
                Range[] r = f.getIndex().getRange();
                if (r != null && r[0] != null) {
                    start = Math.max(0, r[0].first());
                    len = Math.min(len, r[0].length());
                }
            }
            MzMLObjectIterator<Spectrum> spectrumIterator = um.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
            int i = 0;
            while (spectrumIterator.hasNext()) {
                Spectrum spectrum = spectrumIterator.next();
                if (i >= start && i < start + len) {
                    al.add(getIntensityValues(spectrum));
                    i++;
                }
            }
            return al;
        }
        // return an empty list as default
        return new ArrayList<>();
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

    private SerializableArray readMinMaxMassValueArray(final IVariableFragment var,
            final MzMLUnmarshaller um) {
        log.debug("readMinMaxMassValueArray");
        SerializableArray sa = getCache().get(getVariableCacheKey(var));
        if (sa == null) {
            final Tuple2D<Array, Array> t = initMinMaxMZ(var, um);
            getCache().put(var.getParent().getUri() + ">" + this.mass_range_min, new SerializableArray(t.getFirst()));
            getCache().put(var.getParent().getUri() + ">" + this.mass_range_max, new SerializableArray(t.getSecond()));
        }
        if (var.getName().equals(this.mass_range_min)) {
            return getCache().get(getVariableCacheKey(getVariable(var.getParent(), this.mass_range_min)));
        } else if (var.getName().equals(this.mass_range_max)) {
            return getCache().get(getVariableCacheKey(getVariable(var.getParent(), this.mass_range_max)));
        }
        throw new IllegalArgumentException(
                "Method accepts only one of mass_range_min or mass_range_max as varname!");
    }

    private String getVariableCacheKey(final IVariableFragment var) {
        return var.getParent().getUri() + ">" + var.getName();
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

    private Array readMZI(final IVariableFragment var, final MzMLUnmarshaller um) {
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
        if (var.getIndex() != null) {
            final Range[] r = var.getIndex().getRange();
            if (r != null) {
                start = Math.max(0, r[0].first());
                scans = Math.min(scans, r[0].length());
            }
        }
        log.debug("Reading from {} to {} (inclusive)", start, start + scans - 1);
        return createGluedMassIntensityArray(var.getName(), um, start, scans);

    }

    private Array createGluedMassIntensityArray(String variable, final MzMLUnmarshaller um, int start, int scans) throws ConstraintViolationException {
        ArrayList<Array> scanList = new ArrayList<Array>();
        int npeaks;
        npeaks = 0;
        MzMLObjectIterator<Spectrum> spectrumIterator = um.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
        int i = 0;
        Precision p = null;
        while (spectrumIterator.hasNext()) {
            Spectrum spectrum = spectrumIterator.next();
            if (i >= start && i < start + scans) {
                if (p == null) {
                    if (variable.equals(this.mass_values)) {
                        p = getMzBinaryDataArray(spectrum).getPrecision();
                    } else if (variable.equals(this.intensity_values)) {
                        p = getIntensityBinaryDataArray(spectrum).getPrecision();
                    } else {
                        throw new IllegalArgumentException(
                                "Don't know how to handle variable: " + variable);
                    }
                } else {
                    Precision q = null;
                    if (variable.equals(this.mass_values)) {
                        q = getMzBinaryDataArray(spectrum).getPrecision();
                    } else if (variable.equals(this.intensity_values)) {
                        q = getIntensityBinaryDataArray(spectrum).getPrecision();
                    } else {
                        throw new IllegalArgumentException(
                                "Don't know how to handle variable: " + variable);
                    }
                    if (!p.equals(q)) {
                        throw new ConstraintViolationException("Mismatch between precisions of mass values: " + p + "!=" + q);
                    }
                }
                Array b = null;
                if (variable.equals(this.mass_values)) {
                    b = getMassValues(spectrum);
                } else if (variable.equals(this.intensity_values)) {
                    b = getIntensityValues(spectrum);
                } else {
                    throw new IllegalArgumentException(
                            "Don't know how to handle variable: " + variable);
                }
                scanList.add(b);
                log.debug("Reading scan {} of {}", (i + 1), scans);
                npeaks += b.getShape()[0];
                i++;
            }
        }
        if (p == null) {
            p = Precision.FLOAT32BIT;
        }
        Array a = createCompatibleArray(p, new int[]{npeaks});
        int offset = 0;
        for (i = 0; i < scanList.size(); i++) {
            Array b = scanList.get(i);
            Array.arraycopy(b, 0, a, offset, b.getShape()[0]);
            offset += b.getShape()[0];
        }
        return a;
    }

    private Array readMsLevelArray(final IVariableFragment var, final MzMLUnmarshaller um) {
        int start = 0;
        int scans = getScanCount(um);
        final Range[] r = var.getRange();
        if (r != null) {
            start = Math.max(0, r[0].first());
            scans = Math.min(scans, r[0].length());
        }
        Array a = new ArrayInt.D1(scans);
        log.debug("Reading from {} to {} (inclusive)", start, start + scans - 1);
        MzMLObjectIterator<Spectrum> spectrumIterator = um.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
        int i = 0;
        while (spectrumIterator.hasNext()) {
            Spectrum spectrum = spectrumIterator.next();
            if (i >= start && i < start + scans) {
                try {
                    CVParam param = findParam(spectrum.getCvParam(), msLevelAccession);
                    int paramMsLevel = -1;
                    if (param != null && !param.getValue().isEmpty()) {
                        paramMsLevel = Integer.parseInt(param.getValue());
                    }
                    a.setInt(i, paramMsLevel);
                } catch (NullPointerException | ResourceNotAvailableException npe) {
                    log.warn("Could not retrieve ms level for spectrum {}! Assuming level 1!", spectrum.getId());
                    a.setInt(i, 1);
                }
                i++;
            }
        }
        return a;
    }

    private Array readScanAcquisitionTimeArray(final IVariableFragment var,
            final MzMLUnmarshaller um) {
        log.debug("readScanAcquisitionTimeArray");
        IteratedSpectrumFunction<ArrayDouble.D1> f = new IteratedSpectrumFunction<>(DataType.DOUBLE, um, new SpectrumFunction<ArrayDouble.D1>() {
            @Override
            public void apply(Spectrum spectrum, ArrayDouble.D1 array, int i) {
                array.set(i, getRT(spectrum));
                log.debug("RT({})={}", i, array.get(i));
            }
        }, var);
        return f.apply();
    }

    private interface SpectrumFunction<T extends Array> {

        void apply(Spectrum spectrum, T array, int i);
    }

    private class IteratedSpectrumFunction<T extends Array> {

        private int start;
        private int scans;
        private final T array;
        private final MzMLObjectIterator<Spectrum> spectrumIterator;
        private final SpectrumFunction<T> spectrumFunction;

        public IteratedSpectrumFunction(DataType arrayType, MzMLUnmarshaller um, SpectrumFunction spectrumFunction, IVariableFragment var) {
            this.spectrumIterator = um.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
            this.spectrumFunction = spectrumFunction;
            this.array = create(arrayType, um, var);
        }

        final T create(DataType arrayType, MzMLUnmarshaller um, IVariableFragment var) {
            scans = getScanCount(um);
            start = 0;
            final Range[] r = var.getRange();
            if (r != null) {
                start = Math.max(0, r[0].first());
                scans = Math.min(scans, r[0].length());
            }
            Array a = Array.factory(arrayType, new int[]{scans});
            return (T) a;
        }

        final T apply() {
            int i = 0;
            while (spectrumIterator.hasNext()) {
                Spectrum spectrum = spectrumIterator.next();
                if (i >= start && i < start + scans) {
                    spectrumFunction.apply(spectrum, array, i);
                    i++;
                }
            }
            return array;
        }
    }

    private Array readScanIndex(final IVariableFragment var, final MzMLUnmarshaller um) {
        int npeaks = 0;
        int scans = getScanCount(um);
        int start = 0;
        final Range[] r = var.getRange();
        if (r != null) {
            start = Math.max(0, r[0].first());
            scans = Math.min(scans, r[0].length());
        }
        log.debug("Creating index array with {} elements", scans);
        final ArrayInt.D1 scan_index = new ArrayInt.D1(scans);
        MzMLObjectIterator<Spectrum> spectrumIterator = um.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
        int i = 0;
        while (spectrumIterator.hasNext()) {
            Spectrum spectrum = spectrumIterator.next();
            if (i >= start && i < start + scans) {
                final int peaks = spectrum.getDefaultArrayLength();
                // current npeaks is index into larger arrays for current scan
                log.debug("Scan {} from {} to {}", new Object[]{i, npeaks,
                    (npeaks + peaks - 1)});
                scan_index.set(i, npeaks);
                npeaks += peaks;
                i++;
            }
        }
        EvalTools.notNull(scan_index, this);
        return scan_index;
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
//		final IVariableFragment msLevel = getVariable(f, this.ms_level);
//		final IVariableFragment tic = getVariable(f, this.total_ion_current_chromatogram);
//		final IVariableFragment ticSat = getVariable(f, this.total_ion_current_chromatogram_scan_acquisition_time);
        //TODO add first and second_column_elution_time with fast query, whether they
        //are contained in the file
        al.addAll(Arrays.asList(new IVariableFragment[]{ti, sat, si, mrmin,
            mrmax, mv, iv}));// msLevel}));//, tic, ticSat}));
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
