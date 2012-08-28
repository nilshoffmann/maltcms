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
package maltcms.io.xml.mzXML;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import maltcms.io.andims.NetcdfDataSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.systemsbiology.jrap.stax.MSXMLParser;
import org.systemsbiology.jrap.stax.Scan;
import org.systemsbiology.jrap.stax.ScanHeader;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.MAMath;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import cross.Factory;
import cross.datastructures.cache.CacheFactory;
import cross.datastructures.cache.ICacheDelegate;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableVariableFragment2;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.datastructures.tools.EvalTools;
import cross.tools.StringTools;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements {@link cross.io.IDataSource} for mzXML Files.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Slf4j
public class MZXMLStaxDataSource implements IDataSource {

    private final String[] fileEnding = new String[]{"mzxml", "mzxml.xml"};
    private String mass_values = "mass_values";
    private String intensity_values = "intensity_values";
    private String total_intensity = "total_intensity";
    private String scan_acquisition_time = "scan_acquisition_time";
    private String scan_index = "scan_index";
    private String mass_range_min = "mass_range_min";
    private String mass_range_max = "mass_range_max";
    private NetcdfDataSource ndf = null;
    private String source_files = "source_files";
    private int mslevel = 1;
    private MSXMLParser parser = null;
    private ICacheDelegate<IVariableFragment, Array> variableToArrayCache = CacheFactory.createDefaultCache(UUID.randomUUID().toString());

    /**
     * Checks, if passed in file is valid mzXML, by trying to invoke the parser
     * on it.
     */
    public int canRead(final IFileFragment ff) {
        final int dotindex = ff.getName().lastIndexOf(".");
        final String filename = ff.getName().toLowerCase();
        getParser(ff);
        if (dotindex == -1) {
            throw new RuntimeException("Could not determine File extension of "
                    + ff);
        }
        for (final String s : this.fileEnding) {
            if (filename.endsWith(s)) {
                return 1;
            }
        }
        this.log.debug("no!");
        return 0;
    }

    public void configurationChanged(final ConfigurationEvent arg0) {
        // TODO Auto-generated method stub
    }

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
        this.mslevel = configuration.getInt(this.getClass().getName()
                + ".mslevel", 1);
        this.ndf = new NetcdfDataSource();
        this.ndf.configure(configuration);
    }

    protected void convertScanIntensity(final int offset, final Scan s,
            final Array i) {
        final double[][] mzi = s.getMassIntensityList();
        this.log.debug("Scan has {} peaks", s.header.getPeaksCount());
        final Index iind = i.getIndex();
        for (int k = 0; k < s.header.getPeaksCount(); k++) {
            // log.debug("Reading intensity of peak {} of {}", k, s
            // .getPeaksCount());
            i.setDouble(iind.set(k + offset), mzi[1][k]);
        }
    }

    protected void convertScanMZ(final int offset, final Scan s, final Array mz) {
        final double[][] mzi = s.getMassIntensityList();
        this.log.debug("Scan has {} peaks", s.header.getPeaksCount());
        final Index mzind = mz.getIndex();
        for (int k = 0; k < s.header.getPeaksCount(); k++) {
            // log.debug("Reading mz of peak {} of {}", k, s.getPeaksCount());
            try {
                mz.setDouble(mzind.set(k + offset), mzi[0][k]);
            } catch (final RuntimeException re) {
                this.log.error(
                        "At offset {} and k={}, shape of mz array {}, Caught exception {}",
                        new Object[]{offset, k,
                            Arrays.toString(mz.getShape()),
                            re.getLocalizedMessage()});
            }
        }
    }

    /**
     * @return the intensity_values
     */
    public String getIntensityValuesVarName() {
        return this.intensity_values;
    }

    /**
     * @return the mass_range_max
     */
    public String getMassRangeMax() {
        return this.mass_range_max;
    }

    /**
     * @return the mass_range_min
     */
    public String getMassRangeMin() {
        return this.mass_range_min;
    }

    /**
     * @return the mass_values
     */
    public String getMassValuesVarName() {
        return this.mass_values;
    }

    private MSXMLParser getParser(final IFileFragment ff) {
        return getParser(ff.getAbsolutePath());
    }

    private MSXMLParser getParser(final String file) {
        if (this.parser == null) {
            log.info("Creating new parser for file {}", file);
            parser = new MSXMLParser(file);
            int sc = parser.getMaxScanNumber();
            EvalTools.inRangeI(1, Integer.MAX_VALUE, sc, this);
        }
        return parser;
    }

    /**
     * @return the scan_acquisition_time
     */
    public String getScanAcquisitionTimeVarName() {
        return this.scan_acquisition_time;
    }

    /**
     * @return the scan_index
     */
    public String getScanIndexVarName() {
        return this.scan_index;
    }

    private int getScans(final MSXMLParser mp, final int mslevel) {
        int scans = 0;
        int skippedScans = 0;
        this.log.info("Checking scans at mslevel: {}", mslevel);
        for (int i = 0; i < mp.getScanCount(); i++) {
            final ScanHeader h = mp.rapHeader(i + 1);
            if (h.getMsLevel() == mslevel) {
                scans++;
            } else {
                skippedScans++;
            }
        }
        this.log.info("Found {}/{} scans, skipped {}", new Object[]{scans,
                    scans + skippedScans, skippedScans});
        return scans;
    }

    /**
     * @return the source_files
     */
    public String getSourceFilesVarName() {
        return this.source_files;
    }

    /**
     * @return the total_intensity
     */
    public String getTotalIntensityVarName() {
        return this.total_intensity;
    }

    private IVariableFragment getVariable(final IFileFragment f,
            final String name) {
        return (f.hasChild(name) ? f.getChild(name) : new ImmutableVariableFragment2(f,
                name));
    }

    /**
     * Read min and max_mass_range to determine bin sizes.
     *
     * @param var
     * @param mp
     * @return a Tuple2D<Array,Array> with mass_range_min as first and
     * mass_range_max as second array
     */
    protected Tuple2D<Array, Array> initMinMaxMZ(final IVariableFragment var,
            final MSXMLParser mp) {
        this.log.debug("Loading {} and {}", new Object[]{this.mass_range_min,
                    this.mass_range_max});
        int scans = getScans(mp, this.mslevel);
        int start = 0;
        final Range[] r = var.getRange();
        if (r != null) {
            start = r[0].first();
            scans = r[0].length();
        }
        final ArrayDouble.D1 mass_range_min1 = new ArrayDouble.D1(scans);
        final ArrayDouble.D1 mass_range_max1 = new ArrayDouble.D1(scans);
        double min_mass = Double.MAX_VALUE;
        double max_mass = Double.MIN_VALUE;
        for (int i = start; i < scans; i++) {
            final Scan s = mp.rap((i + 1));
            if (s.getHeader().getMsLevel() == this.mslevel) {
                min_mass = Math.min(min_mass, s.header.getLowMz());
                max_mass = Math.max(max_mass, s.header.getHighMz());
            }
        }
        for (int i = 0; i < scans; i++) {
            mass_range_min1.setDouble(i, Math.floor(min_mass));
            mass_range_max1.setDouble(i, Math.ceil(max_mass));
        }
        return new Tuple2D<Array, Array>(mass_range_min1, mass_range_max1);
    }

    private Array loadArray(final IFileFragment f, final IVariableFragment var) {
        final MSXMLParser mp = getParser(f);
        Array a = variableToArrayCache.get(var);
        if (a != null) {
            log.info("Retrieved variable data array from cache for " + var);
            return a;
        }
        final String varname = var.getName();
        // Read mass_values or intensity_values for whole chromatogram
        if (varname.equals(this.mass_values)
                || varname.equals(this.intensity_values)) {
            a = readMZI(var, mp, this.mslevel);
        } else if (varname.equals(this.scan_index)) {
            a = readScanIndex(var, mp);
            // read total_intensity
        } else if (varname.equals(this.total_intensity)) {
            a = readTotalIntensitiesArray(var, mp);
            // read min and max_mass_range
        } else if (varname.equals(this.mass_range_min)
                || varname.equals(this.mass_range_max)) {
            a = readMinMaxMassValueArray(var, mp);
            // read scan_acquisition_time
        } else if (varname.equals(this.scan_acquisition_time)) {
            a = readScanAcquisitionTimeArray(var, mp);
        } else {
            throw new ResourceNotAvailableException(
                    "Unknown varname to mzXML mapping for varname " + varname);
        }
        if (a != null) {
            variableToArrayCache.put(var, a);
        }
        return a;
    }

    protected void prepareMZXMLOutput() {
        // MZXMLFileInfo mfi = new MZXMLFileInfo();
        // mfi.
    }

    public ArrayList<Array> readAll(final IFileFragment f) throws IOException,
            ResourceNotAvailableException {
        final ArrayList<IVariableFragment> al = readStructure(f);
        final ArrayList<Array> ral = new ArrayList<Array>(al.size());
        for (final IVariableFragment vf : al) {
            final Array a = readSingle(vf);
            ral.add(a);
        }
        return ral;
    }

    public ArrayList<Array> readIndexed(final IVariableFragment f)
            throws IOException, ResourceNotAvailableException {
        this.log.debug("{}", f.getParent().toString());
        if (f.getName().equals("mass_values")
                || f.getName().equals("intensity_values")) {
            this.log.debug("Reading {} and {}", this.mass_values,
                    this.intensity_values);
            // Tuple2D<ArrayList<Array>, ArrayList<Array>> t = MaltcmsTools
            // .getMZIs(f.getParent());
            // if (f.getVarname().equals("mass_values")) {
            // return t.getFirst();
            // } else {
            // return t.getSecond();
            // }
            final MSXMLParser mp = getParser(f.getParent());
            final ArrayList<Array> al = readMZIScans(f, mp, this.mslevel);
            // f.setIndexedArray(al);
            return al;
        } else {
            throw new ResourceNotAvailableException(
                    "Only mass_values and intensity_values can be read indexed!");
        }
    }

    private Array readMinMaxMassValueArray(final IVariableFragment var,
            final MSXMLParser mp) {
        this.log.debug("readMinMaxMassValueArray");
        final Tuple2D<Array, Array> t = initMinMaxMZ(var, mp);
        if (var.getName().equals(this.mass_range_min)) {
            return t.getFirst();
        }
        if (var.getName().equals(this.mass_range_max)) {
            return t.getSecond();
        }
        throw new IllegalArgumentException(
                "Method accepts only one of mass_range_min or mass_range_max as varname!");
    }

    private Array readMZI(final IVariableFragment var, final MSXMLParser mp,
            final int mslevel) {
        // if(!f.hasArray()) {
        int npeaks = 0;
        int start = 0;
        int scans = mp.getScanCount();
        final Range[] r = var.getRange();
        if (r != null) {
            start = r[0].first();
            scans = r[0].length();
        }
        for (int i = start; i < scans; i++) {
            final Scan s = mp.rap((i + 1));
            if (s.getHeader().getMsLevel() == mslevel) {
                npeaks += s.header.getPeaksCount();
            }
        }
        final ArrayDouble.D1 a = new ArrayDouble.D1(npeaks);

        if (var.getName().equals(this.mass_values)) {
            npeaks = 0;
            for (int i = start; i < scans; i++) {
                this.log.debug("Reading scan {} of {}", (i + 1), scans);
                final Scan s = mp.rap((i + 1));
                if (s.getHeader().getMsLevel() == mslevel) {
                    this.log.debug("Converting scan {} of {} with {} peaks",
                            new Object[]{(i + 1), scans,
                                s.header.getPeaksCount()});
                    convertScanMZ(npeaks, s, a);
                    this.log.debug("npeaks before: {}", npeaks);
                    npeaks += s.header.getPeaksCount();
                    this.log.debug("npeaks after: {}", npeaks);
                }
            }
            // f.setArray(a);
        } else if (var.getName().equals(this.intensity_values)) {
            npeaks = 0;
            for (int i = start; i < scans; i++) {
                this.log.debug("Reading scan {} of {}", (i + 1), scans);
                final Scan s = mp.rap((i + 1));
                if (s.getHeader().getMsLevel() == mslevel) {
                    this.log.debug("Converting scan {} of {} with {} peaks",
                            new Object[]{(i + 1), scans,
                                s.header.getPeaksCount()});
                    convertScanIntensity(npeaks, s, a);
                    this.log.debug("npeaks before: {}", npeaks);
                    npeaks += s.header.getPeaksCount();
                    this.log.debug("npeaks after: {}", npeaks);
                }
            }
            // f.setArray(a);
        }
        return a;
        // }
        // return f.getArray();
    }

    private ArrayList<Array> readMZIScans(final IVariableFragment var,
            final MSXMLParser mp, final int mslevel) {
        int scans = mp.getScanCount();
        int start = 0;
        final Range[] r = var.getRange();
        if (r != null) {
            start = r[0].first();
            scans = r[0].length();
        }
        final ArrayList<Array> al = new ArrayList<Array>();
        if (var.getName().equals(this.mass_values)) {
            int npeaks = 0;
            for (int i = start; i < scans; i++) {
                this.log.debug("Reading scan {} of {}", (i + 1), scans);
                final Scan s = mp.rap((i + 1));
                if (s.getHeader().getMsLevel() == mslevel) {
                    final ArrayDouble.D1 a = new ArrayDouble.D1(s.header.getPeaksCount());
                    this.log.debug("Converting scan {} of {} with {} peaks",
                            new Object[]{(i + 1), scans,
                                s.header.getPeaksCount()});
                    convertScanMZ(0, s, a);
                    this.log.debug("npeaks before: {}", npeaks);
                    npeaks += s.header.getPeaksCount();
                    this.log.debug("npeaks after: {}", npeaks);
                    al.add(a);
                }
            }
        } else if (var.getName().equals(this.intensity_values)) {
            int npeaks = 0;
            for (int i = start; i < scans; i++) {
                this.log.debug("Reading scan {} of {}", (i + 1), scans);
                final Scan s = mp.rap((i + 1));
                if (s.getHeader().getMsLevel() == mslevel) {
                    final ArrayDouble.D1 a = new ArrayDouble.D1(s.header.getPeaksCount());
                    this.log.debug("Converting scan {} of {} with {} peaks",
                            new Object[]{(i + 1), scans,
                                s.header.getPeaksCount()});
                    convertScanIntensity(0, s, a);
                    this.log.debug("npeaks before: {}", npeaks);
                    npeaks += s.header.getPeaksCount();
                    this.log.debug("npeaks after: {}", npeaks);
                    al.add(a);
                }
            }
        }
        return al;
    }

    private Array readScanAcquisitionTimeArray(final IVariableFragment var,
            final MSXMLParser mp) {
        this.log.debug("readScanAcquisitionTimeArray");
        int scans = mp.getScanCount();
        int levelnscans = getScans(mp, this.mslevel);
        int start = 0;
        final Range[] r = var.getRange();
        if (r != null) {
            start = r[0].first();
            scans = r[0].length();
        }
        final ArrayDouble.D1 sat = new ArrayDouble.D1(levelnscans);
        int cnt = 0;
        for (int i = start; i < scans; i++) {
            final Scan s = mp.rap((i + 1));
            if (s.getHeader().getMsLevel() == this.mslevel) {
                sat.set(cnt++, s.header.getDoubleRetentionTime());
            }
        }
        // f.setArray(sat);
        return sat;
    }

    private Array readScanIndex(final IVariableFragment var,
            final MSXMLParser mp) {
        int npeaks = 0;
        int scans = mp.getScanCount();
        int levelnscans = getScans(mp, this.mslevel);
        int start = 0;
        final Range[] r = var.getRange();
        if (r != null) {
            start = r[0].first();
            scans = r[0].length();
        }
        this.log.debug("Creating index array with {} elements", levelnscans);
        final ArrayInt.D1 scan_index = new ArrayInt.D1(levelnscans);
        int cnt = 0;
        for (int i = start; i < scans; i++) {
            final Scan s = mp.rap((i + 1));
            if (s.getHeader().getMsLevel() == this.mslevel) {
                // current npeaks is index into larger arrays for current scan
                this.log.debug("Scan {} from {} to {}", new Object[]{i,
                            npeaks, (npeaks + s.header.getPeaksCount() - 1)});
                scan_index.set(cnt++, npeaks);
                npeaks += s.header.getPeaksCount();
            }
        }
        EvalTools.notNull(scan_index, this);
        return scan_index;
    }

    /**
     * Reads a single array referenced by a IVariableFragment. This method
     * mimics the semantics of {@link maltcms.io.andims.NetcdfDataSource}.
     */
    public Array readSingle(final IVariableFragment f) throws IOException,
            ResourceNotAvailableException {
        this.log.info("readSingle of {} in {}", f.getName(), f.getParent().getAbsolutePath());
        if (f.hasArray()) {
            this.log.debug("{} already has an array set!", f);
        }
        final Array a = loadArray(f.getParent(), f);
        if (a == null) {
            throw new ResourceNotAvailableException("Could not find variable "
                    + f.getName() + " in file " + f.getParent().getName());
        }
        // f.setArray(a);
        return a;
    }

    public ArrayList<IVariableFragment> readStructure(final IFileFragment f)
            throws IOException {
        final IVariableFragment ti = getVariable(f, this.total_intensity);
        final IVariableFragment sat = getVariable(f, this.scan_acquisition_time);
        final IVariableFragment si = getVariable(f, this.scan_index);
        final IVariableFragment mrmin = getVariable(f, this.mass_range_min);
        final IVariableFragment mrmax = getVariable(f, this.mass_range_max);
        final IVariableFragment mv = getVariable(f, this.mass_values);
        final IVariableFragment iv = getVariable(f, this.intensity_values);
        final ArrayList<IVariableFragment> al = new ArrayList<IVariableFragment>();
        al.addAll(Arrays.asList(new IVariableFragment[]{ti, sat, si, mrmin,
                    mrmax, mv, iv}));
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
    @Override
    public IVariableFragment readStructure(final IVariableFragment f)
            throws IOException, ResourceNotAvailableException {
        final MSXMLParser mp = getParser(f.getParent());
        final int scancount = getScans(mp, this.mslevel);
        final String varname = f.getName();
        // Read mass_values or intensity_values for whole chromatogram
        if (varname.equals(this.scan_index)
                || varname.equals(this.total_intensity)
                || varname.equals(this.mass_range_min)
                || varname.equals(this.mass_range_max)
                || varname.equals(this.scan_acquisition_time)) {
            final Dimension[] dims = new Dimension[]{new Dimension(
                "scan_number", scancount, true)};
            f.setDimensions(dims);
        } else if (varname.equals(this.mass_values)
                || varname.equals(this.intensity_values)) {
            int npeaks = 0;
            try {
                for (int i = 0; i < scancount; i++) {
                    final ScanHeader sh = mp.rapHeader(i + 1);
                    if (sh.getMsLevel() == this.mslevel) {
                        npeaks += mp.rapHeader(i + 1).getPeaksCount();
                    }
                }
                final Dimension[] dims = new Dimension[]{new Dimension(
                    "point_number", npeaks, true)};
                f.setDimensions(dims);
            } catch (final NullPointerException npe) {
                throw new ResourceNotAvailableException(
                        "Could not rap header of file "
                        + f.getParent().getAbsolutePath());
            }
        } else {
            throw new ResourceNotAvailableException(
                    "Unknown varname to mzXML mapping for varname " + varname);
        }
        return f;
    }

    private Array readTotalIntensitiesArray(final IVariableFragment var,
            final MSXMLParser mp) {
        this.log.debug("readTotalIntensitiesArray");
        int levelnscans = getScans(mp, this.mslevel);
        int scans = mp.getScanCount();
        int start = 0;
        final Range[] r = var.getRange();
        if (r != null) {
            start = r[0].first();
            scans = r[0].length();
        }
        final ArrayDouble.D1 total_intensities = new ArrayDouble.D1(levelnscans);
        int cnt = 0;
        for (int i = start; i < scans; i++) {
            final ScanHeader s = mp.rapHeader((i + 1));
            if (s.getMsLevel() == this.mslevel) {
                total_intensities.set(cnt++, s.getTotIonCurrent());
            }
        }
        return total_intensities;
    }

    /**
     * @param intensity_values the intensity_values to set
     */
    public void setIntensityValuesVarName(final String intensity_values) {
        this.intensity_values = intensity_values;
    }

    /**
     * @param mass_range_max the mass_range_max to set
     */
    public void setMassRangeMax(final String mass_range_max) {
        this.mass_range_max = mass_range_max;
    }

    /**
     * @param mass_range_min the mass_range_min to set
     */
    public void setMassRangeMin(final String mass_range_min) {
        this.mass_range_min = mass_range_min;
    }

    /**
     * @param mass_values the mass_values to set
     */
    public void setMassValuesVarName(final String mass_values) {
        this.mass_values = mass_values;
    }

    /**
     * @param scan_acquisition_time the scan_acquisition_time to set
     */
    public void setScanAcquisitionTimeVarName(final String scan_acquisition_time) {
        this.scan_acquisition_time = scan_acquisition_time;
    }

    /**
     * @param scan_index the scan_index to set
     */
    public void setScanIndexVarName(final String scan_index) {
        this.scan_index = scan_index;
    }

    /**
     * @param source_files the source_files to set
     */
    public void setSourceFilesVarName(final String source_files) {
        this.source_files = source_files;
    }

    /**
     * @param total_intensity the total_intensity to set
     */
    public void setTotalIntensityVarName(final String total_intensity) {
        this.total_intensity = total_intensity;
    }

    public boolean structureWrite(final IFileFragment f,
            final ArrayList<IVariableFragment> a) {
        return write(f);
    }

    public List<String> supportedFormats() {
        return Arrays.asList(this.fileEnding);
    }

    protected Scan toScan(final int num, final Array mz, final Array i) {
        MAMath.conformable(mz, i);
        final double[][] mzi = new double[2][mz.getShape()[0]];
        final Index mzind = mz.getIndex();
        final Index iind = i.getIndex();
        for (int k = 0; k < mzi[0].length; k++) {
            mzi[0][k] = mz.getDouble(mzind.set(k));
            mzi[1][k] = mz.getDouble(iind.set(k));
        }
        final Scan s = new Scan();
        s.setMassIntensityList(mzi);
        s.header.setNum(num);
        s.header.setMsLevel(this.mslevel);
        s.header.setTotIonCurrent((float) MAMath.sumDouble(i));
        return s;
    }

    public boolean write(final IFileFragment f) {
        EvalTools.notNull(this.ndf, this);
        // TODO Implement real write support
        this.log.info("Saving {} with MZXMLStaxDataSource", f.getAbsolutePath());
        this.log.info("Changing output file from: {}", f.toString());
        final String source_file = f.getAbsolutePath();
        String filename = StringTools.removeFileExt(f.getAbsolutePath());
        filename += ".cdf";
        f.setFile(filename);
        f.addSourceFile(Factory.getInstance().getFileFragmentFactory().create(
                new File(source_file)));
        this.log.info("To: {}", filename);
        return Factory.getInstance().getDataSourceFactory().getDataSourceFor(f).write(f);
    }
}
