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
 * $Id: MZMLDataSource.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.io.xml.mzML;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

import maltcms.io.andims.NetcdfDataSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.MAMath;
import ucar.ma2.Range;
import ucar.ma2.MAMath.MinMax;
import ucar.nc2.Dimension;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.Chromatogram;
import uk.ac.ebi.jmzml.model.mzml.Run;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.model.mzml.SpectrumList;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;
import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.datastructures.tools.EvalTools;
import cross.tools.StringTools;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=IDataSource.class)
public class MZMLDataSource implements IDataSource {

    private final Logger log = Logging.getLogger(this.getClass());
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
    private static WeakHashMap<IFileFragment, MzMLUnmarshaller> fileToIndex = new WeakHashMap<IFileFragment, MzMLUnmarshaller>();

    @Override
    public int canRead(final IFileFragment ff) {
        final int dotindex = ff.getName().lastIndexOf(".");
        final String fileending = ff.getName().substring(dotindex + 1);
        if (dotindex == -1) {
            throw new RuntimeException("Could not determine File extension of "
                    + ff);
        }
        for (final String s : this.fileEnding) {
            if (s.equalsIgnoreCase(fileending)) {
                return 1;
            }
        }

        this.log.debug("no!");
        return 0;
    }

    @Override
    public void configurationChanged(final ConfigurationEvent arg0) {
        // TODO Auto-generated method stub
    }

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
        this.ndf = new NetcdfDataSource();
        this.ndf.configure(configuration);
    }

    private MzMLUnmarshaller getUnmarshaller(final IFileFragment ff) {
        if (MZMLDataSource.fileToIndex.containsKey(ff)) {
            return MZMLDataSource.fileToIndex.get(ff);
        }
        MzMLUnmarshaller um = new MzMLUnmarshaller(new File(ff.getAbsolutePath()), true);
        MZMLDataSource.fileToIndex.put(ff, um);
        return um;
    }

    private Run getRun(MzMLUnmarshaller mzmlu, final IFileFragment ff) {
        Run run = mzmlu.unmarshalFromXpath("/run", Run.class);
        return run;
    }

    private Spectrum getSpectrum(Run run, int idx) {
        SpectrumList cl = run.getSpectrumList();
        Spectrum s = cl.getSpectrum().get(idx);
        return s;
    }

    private Array getMassValues(final Spectrum s) {
        Number[] n = s.getBinaryDataArrayList().getBinaryDataArray().get(0).getBinaryDataAsNumberArray();
        ArrayDouble.D1 masses = new ArrayDouble.D1(n.length);
        int j = 0;
        for (Number num : n) {
            masses.set(j++, num.doubleValue());
        }
        return masses;
    }

    private Array getIntensityValues(final Spectrum s) {
        Number[] n = s.getBinaryDataArrayList().getBinaryDataArray().get(1).getBinaryDataAsNumberArray();
        ArrayDouble.D1 intensities = new ArrayDouble.D1(n.length);
        int j = 0;
        for (Number num : n) {
            intensities.set(j++, num.doubleValue());
        }
        return intensities;
    }

    private Tuple2D<Double, Double> getMinMaxMassRange(final Array massValues) {
        MinMax mm = MAMath.getMinMax(massValues);
        return new Tuple2D<Double, Double>(mm.min, mm.max);
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

    private String getRTUnit(final Spectrum s) {
        String rtUnit = "seconds";
        try {
            CVParam rtp = findParam(s.getScanList().getScan().get(0).getCvParam(), "MS:1000016");
            rtUnit = rtp.getUnitName();
        } catch (ResourceNotAvailableException rne) {
        }
        return rtUnit;
    }

    private double getRT(final Spectrum s) {
        // TODO autoconvert to seconds, if unit is different
        double rt = Double.NaN;
        try {
            CVParam rtp = findParam(s.getScanList().getScan().get(0).getCvParam(), "MS:1000016");
            rt = Double.parseDouble(rtp.getValue());
        } catch (ResourceNotAvailableException rne) {
        }
        return rt;
    }

    private int getScanCount(final Run r) {
        return r.getSpectrumList().getSpectrum().size();
    }

    private IVariableFragment getVariable(final IFileFragment f,
            final String name) {
        return (f.hasChild(name) ? f.getChild(name) : new VariableFragment(f,
                name));
    }

    /**
     * Read min and max_mass_range to determine bin sizes.
     *
     * @param var
     * @param run
     * @return a Tuple2D<Array,Array> with mass_range_min as first and
     *         mass_range_max as second array
     */
    protected Tuple2D<Array, Array> initMinMaxMZ(final IVariableFragment var,
            final Run run) {
        this.log.debug("Loading {} and {}", new Object[]{this.mass_range_min,
                    this.mass_range_max});
        int scans = getScanCount(run);
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
            Spectrum spec = getSpectrum(run, i);
            Array a = getMassValues(spec);
            final Tuple2D<Double, Double> t = getMinMaxMassRange(a);
            min_mass = Math.min(min_mass, t.getFirst());
            max_mass = Math.max(max_mass, t.getSecond());
        }
        for (int i = 0; i < scans; i++) {
            mass_range_min1.setDouble(i, Math.floor(min_mass));
            mass_range_max1.setDouble(i, Math.ceil(max_mass));
        }
        return new Tuple2D<Array, Array>(mass_range_min1, mass_range_max1);
    }

    private Array loadArray(final IFileFragment f, final IVariableFragment var) {
        final Run r = getRun(getUnmarshaller(f), f);
        Array a = null;
        final String varname = var.getVarname();
        // Read mass_values or intensity_values for whole chromatogram
        if (varname.equals(this.mass_values)
                || varname.equals(this.intensity_values)) {
            a = readMZI(var, r);
        } else if (varname.equals(this.scan_index)) {
            a = readScanIndex(var, r);
            // read total_intensity
        } else if (varname.equals(this.total_intensity)) {
            a = readTotalIntensitiesArray(var, r);
            // read min and max_mass_range
        } else if (varname.equals(this.mass_range_min)
                || varname.equals(this.mass_range_max)) {
            a = readMinMaxMassValueArray(var, r);
            // read scan_acquisition_time
        } else if (varname.equals(this.scan_acquisition_time)) {
            a = readScanAcquisitionTimeArray(var, r);
        } else {
            throw new ResourceNotAvailableException(
                    "Unknown varname to mzXML mapping for varname " + varname);
        }
        return a;
    }

    @Override
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

    @Override
    public ArrayList<Array> readIndexed(final IVariableFragment f)
            throws IOException, ResourceNotAvailableException {
        if (f.getVarname().equals(this.mass_values)) {
            final ArrayList<Array> al = new ArrayList<Array>();
            final Run run = getRun(getUnmarshaller(f.getParent()), f.getParent());
            int start = 0;
            int len = getScanCount(run);
            if (f.getIndex() != null) {
                Range[] r = f.getIndex().getRange();
                if (r != null && r[0] != null) {
                    start = r[0].first();
                    len = r[0].length();
                }
            }
            for (int i = start; i < len; i++) {
                al.add(getMassValues(getSpectrum(run, i)));
            }
            return al;
        }
        if (f.getVarname().equals(this.intensity_values)) {
            final ArrayList<Array> al = new ArrayList<Array>();
            final Run run = getRun(getUnmarshaller(f.getParent()), f.getParent());
            int start = 0;
            int len = getScanCount(run);
            if (f.getIndex() != null) {
                Range[] r = f.getIndex().getRange();
                if (r != null && r[0] != null) {
                    start = r[0].first();
                    len = r[0].length();
                }
            }
            for (int i = start; i < len; i++) {
                al.add(getIntensityValues(getSpectrum(run, i)));
            }
            return al;
        }
        // return an empty list as default
        return new ArrayList<Array>();
    }

    private Array readMinMaxMassValueArray(final IVariableFragment var,
            final Run run) {
        this.log.debug("readMinMaxMassValueArray");
        final Tuple2D<Array, Array> t = initMinMaxMZ(var, run);
        if (var.getVarname().equals(this.mass_range_min)) {
            return t.getFirst();
        }
        if (var.getVarname().equals(this.mass_range_max)) {
            return t.getSecond();
        }
        throw new IllegalArgumentException(
                "Method accepts only one of mass_range_min or mass_range_max as varname!");
    }

    private Array readMZI(final IVariableFragment var, final Run run) {
        // if(!f.hasArray()) {
        int npeaks = 0;
        int start = 0;
        int scans = getScanCount(run);
        final Range[] r = var.getRange();
        if (r != null) {
            start = r[0].first();
            scans = r[0].length();
        }
        for (int i = start; i < scans; i++) {
            npeaks += getMassValues(getSpectrum(run, i)).getShape()[0];
        }

        if (var.getVarname().equals(this.mass_values)) {
            final Array a = new ArrayDouble.D1(npeaks);
            npeaks = 0;
            for (int i = start; i < scans; i++) {
                this.log.debug("Reading scan {} of {}", (i + 1), scans);
                final Array b = getMassValues(getSpectrum(run, i));
                Array.arraycopy(b, 0, a, npeaks, b.getShape()[0]);
                npeaks += b.getShape()[0];
            }
            return a;
            // f.setArray(a);
        } else if (var.getVarname().equals(this.intensity_values)) {
            final Array a = new ArrayDouble.D1(npeaks);
            npeaks = 0;
            for (int i = start; i < scans; i++) {
                this.log.debug("Reading scan {} of {}", (i + 1), scans);
                final Array b = getIntensityValues(getSpectrum(run, i));
                Array.arraycopy(b, 0, a, npeaks, b.getShape()[0]);
                npeaks += b.getShape()[0];
                this.log.debug("npeaks after: {}", npeaks);
            }
            return a;
            // f.setArray(a);
        }
        throw new IllegalArgumentException(
                "Don't know how to handle variable: " + var.getVarname());
        // }
        // return f.getArray();
    }

    private Array readScanAcquisitionTimeArray(final IVariableFragment var,
            final Run run) {
        this.log.debug("readScanAcquisitionTimeArray");
        int scans = getScanCount(run);
        int start = 0;
        final Range[] r = var.getRange();
        if (r != null) {
            start = r[0].first();
            scans = r[0].length();
        }
        final ArrayDouble.D1 sat = new ArrayDouble.D1(scans);
        for (int i = start; i < scans; i++) {
            sat.set(i, getRT(getSpectrum(run, i)));
            this.log.debug("RT({})={}", i, sat.get(i));
        }
        // f.setArray(sat);
        return sat;
    }

    private Array readScanIndex(final IVariableFragment var, final Run run) {
        int npeaks = 0;
        int scans = getScanCount(run);
        int start = 0;
        final Range[] r = var.getRange();
        if (r != null) {
            start = r[0].first();
            scans = r[0].length();
        }
        this.log.debug("Creating index array with {} elements", scans);
        final ArrayInt.D1 scan_index = new ArrayInt.D1(scans);
        for (int i = start; i < scans; i++) {
            final int peaks = getMassValues(getSpectrum(run, i)).getShape()[0];
            // current npeaks is index into larger arrays for current scan
            this.log.debug("Scan {} from {} to {}", new Object[]{i, npeaks,
                        (npeaks + peaks - 1)});
            scan_index.set(i, npeaks);
            npeaks += peaks;
        }
        EvalTools.notNull(scan_index, this);
        return scan_index;
    }

    @Override
    public Array readSingle(final IVariableFragment f) throws IOException,
            ResourceNotAvailableException {
        this.log.debug("readSingle of {} in {}", f.getVarname(), f.getParent().getAbsolutePath());
        if (f.hasArray()) {
            this.log.warn("{} already has an array set!", f);
        }
        final Array a = loadArray(f.getParent(), f);
        if (a == null) {
            throw new ResourceNotAvailableException("Could not find variable "
                    + f.getVarname() + " in file " + f.getParent().getName());
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
    @Override
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
        final Run run = getRun(getUnmarshaller(f.getParent()), f.getParent());
        final int scancount = getScanCount(run);
        final String varname = f.getVarname();
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
                    npeaks += getMassValues(getSpectrum(run, i)).getShape()[0];
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
                    "Unknown varname to mzML mapping for varname " + varname);
        }
        return f;
    }

    private Array readTotalIntensitiesArray(final IVariableFragment var,
            final Run run) {
        // Current assumption is, that global time for ms scans correspond to
        // chromatogram times
        Chromatogram c = run.getChromatogramList().getChromatogram().get(0);
        BinaryDataArray timeArray = c.getBinaryDataArrayList().getBinaryDataArray().get(0);
        BinaryDataArray intensitiesArray = c.getBinaryDataArrayList().getBinaryDataArray().get(1);
        // TODO add a unit check
        String rtUnit = "seconds";
        try {
            CVParam rtUnitP = findParam(timeArray.getCvParam(), "MS:1000595");
            rtUnit = rtUnitP.getUnitName();
        } catch (ResourceNotAvailableException rnae) {
        }
        Number[] intensities = intensitiesArray.getBinaryDataAsNumberArray();
        final ArrayDouble.D1 intensA = new ArrayDouble.D1(intensities.length);
        for (int i = 0; i < intensities.length; i++) {
            intensA.set(i, intensities[i].doubleValue());
        }
        return intensA;
    }

    @Override
    public List<String> supportedFormats() {
        return Arrays.asList(this.fileEnding);
    }

    @Override
    public boolean write(final IFileFragment f) {
        EvalTools.notNull(this.ndf, this);
        // TODO Implement real write support
        this.log.info("Saving {} with MZMLDataSource", f.getAbsolutePath());
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
