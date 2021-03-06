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
package maltcms.io.xml.mzData;

import cross.Factory;
import cross.cache.CacheFactory;
import cross.cache.ICacheDelegate;
import cross.datastructures.cache.SerializableArray;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableVariableFragment2;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.io.misc.Base64Util;
import cross.tools.StringTools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import lombok.extern.slf4j.Slf4j;
import maltcms.io.andims.NetcdfDataSource;
import maltcms.io.xml.mzData.MzData.SpectrumList.Spectrum;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.Range;
import ucar.nc2.Dimension;

@Slf4j
/**
 * <p>MZDataDataSource class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@ServiceProvider(service = IDataSource.class)
public class MZDataDataSource implements IDataSource {

    private final String[] fileEnding = new String[]{"mzdata", "mzdata.xml"};
    private String mass_values = "mass_values";
    private String intensity_values = "intensity_values";
    private String total_intensity = "total_intensity";
    private final String scan_acquisition_time = "scan_acquisition_time";
    private String scan_index = "scan_index";
    private String mass_range_min = "mass_range_min";
    private String mass_range_max = "mass_range_max";
    private NetcdfDataSource ndf = null;
    private String source_files = "source_files";
    private static final ICacheDelegate<URI, Unmarshaller> fileToIndex = CacheFactory.createVolatileCache(MZDataDataSource.class.getName() + "-unmarshaller", 3600, 7200, 100);
    private static final ICacheDelegate<URI, MzData> fileToMzData = CacheFactory.createVolatileCache(MZDataDataSource.class.getName() + "-mzData", 3600, 7200, 100);
    private static final ICacheDelegate<String, SerializableArray> variableToArrayCache = CacheFactory.createVolatileCache("maltcms.io.readcache");

    private ICacheDelegate<String, SerializableArray> getCache() {
        return MZDataDataSource.variableToArrayCache;
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

    /*
     * (non-Javadoc)
     *
     * @seeorg.apache.commons.configuration.event.ConfigurationListener#
     * configurationChanged
     * (org.apache.commons.configuration.event.ConfigurationEvent)
     */
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
        this.ndf = new NetcdfDataSource();
        this.ndf.configure(configuration);
    }

    private Array getIntensityValues(final MzData mz, final int j) {
        final PeakListBinaryType blbt = mz.getSpectrumList().getSpectrum().get(
                j).getIntenArrayBinary();
        return getPeakListArray(blbt);
    }

    private Array getMassValues(final MzData mz, final int j) {
        final PeakListBinaryType blbt = mz.getSpectrumList().getSpectrum().get(
                j).getMzArrayBinary();
        return getPeakListArray(blbt);
    }

    private Tuple2D<Double, Double> getMinMaxMassRange(final MzData mp,
            final int scan) {
        final SpectrumSettingsType.SpectrumInstrument si = mp.spectrumList.spectrum.get(scan).getSpectrumDesc().getSpectrumSettings().getSpectrumInstrument();
        return new Tuple2D<>(
                Double.valueOf(si.getMzRangeStart()), Double.valueOf(si.getMzRangeStop()));
    }

    private int getNumPeaks(final MzData mz, final int i) {
        return mz.getSpectrumList().getSpectrum().get(i).getMzArrayBinary().data.length;
    }

    private Array getPeakListArray(final PeakListBinaryType blbt) {
        final byte[] b = blbt.getData().getValue();
        final String endian = blbt.getData().getEndian();
        final String precision = blbt.getData().getPrecision();
        final int prec = Integer.parseInt(precision);
        final int length = blbt.getData().getLength();
        log.debug("endian: {} precision: {} length: {}", new Object[]{
            endian, precision, length});
        Array a = null;
        if (prec == 32) {
            final float[] f = Base64Util.byteArrayToFloatArray(b, endian.equals("little") ? false : true, length);
            log.info("{}", Arrays.toString(f));
            // avoid later checking for double/float arrays
            a = new ArrayDouble.D1(f.length);
            for (int i = 0; i < f.length; i++) {
                ((ArrayDouble.D1) a).set(i, f[i]);
            }
        } else if (prec == 64) {
            final double[] d = Base64Util.byteArrayToDoubleArray(b, endian.equals("little") ? false : true, length);
            log.debug("{}", Arrays.toString(d));
            a = new ArrayDouble.D1(d.length);
            for (int i = 0; i < d.length; i++) {
                ((ArrayDouble.D1) a).set(i, d[i]);
            }
        } else {
            throw new IllegalArgumentException("Unknown precision: " + prec);
        }
        return a;
    }

    private int getPrecision(final PeakListBinaryType pl) {
        return Integer.parseInt(pl.getData().getPrecision());
    }

    private double getRT(final MzData mz, final int j) {
        final Spectrum s = mz.getSpectrumList().getSpectrum().get(j);
        final List<Object> l = s.getSpectrumDesc().getSpectrumSettings().getSpectrumInstrument().getCvParamOrUserParam();
        for (final Object o : l) {
            final CvParamType cv = (CvParamType) o;
            if (cv.name.equals("TimeInMinutes")) {
                // multiply with 60 to get seconds equivalent
                final double d = Double.parseDouble(cv.value) * 60.0d;
                if (d == Double.NaN) {
                    log.warn("Parsed time as NaN!");
                    return 0.0d;
                }
                return d;
            }
        }
        return 0.0d;
    }

    private int getScanCount(final MzData mz) {
//		EvalTools.eqI(mz.getSpectrumList().getSpectrum().size(), mz.getSpectrumList().getCount(), this);
        return mz.getSpectrumList().getSpectrum().size();
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
     * @param mp a {@link maltcms.io.xml.mzData.MzData} object.
     * @return a Tuple2D<Array,Array> with mass_range_min as first and
     * mass_range_max as second array
     */
    protected Tuple2D<Array, Array> initMinMaxMZ(final IVariableFragment var,
            final MzData mp) {
        log.debug("Loading {} and {}", new Object[]{this.mass_range_min,
            this.mass_range_max});
        int scans = getScanCount(mp);
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
            final Tuple2D<Double, Double> t = getMinMaxMassRange(mp, i);
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
        SerializableArray sa = variableToArrayCache.get(f.getUri() + ">" + var.getName());
        Array a = null;
        if (sa != null) {
            a = sa.getArray();
        }
        if (a != null) {
            log.info("Retrieved variable data array from cache for " + var);
            return a;
        }
        final String varname = var.getName();
        final MzData mzd = unmarshal(f);
        // Read mass_values or intensity_values for whole chromatogram
        if (varname.equals(this.mass_values)
                || varname.equals(this.intensity_values)) {
            a = readMZI(var, mzd);
        } else if (varname.equals(this.scan_index)) {
            a = readScanIndex(var, mzd);
            // read total_intensity
        } else if (varname.equals(this.total_intensity)) {
            a = readTotalIntensitiesArray(var, mzd);
            // read min and max_mass_range
        } else if (varname.equals(this.mass_range_min)
                || varname.equals(this.mass_range_max)) {
            a = readMinMaxMassValueArray(var, mzd);
            // read scan_acquisition_time
        } else if (varname.equals(this.scan_acquisition_time)) {
            a = readScanAcquisitionTimeArray(var, mzd);
        } else {
            throw new ResourceNotAvailableException(
                    "Unknown varname to mzXML mapping for varname " + varname);
        }
        if (a != null) {
            variableToArrayCache.put(f.getUri() + ">" + var.getName(), new SerializableArray(a));
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
        if (f.getName().equals(this.mass_values)) {
            final ArrayList<Array> al = new ArrayList<>();
            final MzData mzd = unmarshal(f.getParent());
            int start = 0;
            int len = getScanCount(mzd);
            if (f.getIndex() != null) {
                Range[] r = f.getIndex().getRange();
                if (r != null && r[0] != null) {
                    start = r[0].first();
                    len = r[0].length();
                }
            }
            final List<Spectrum> l = mzd.getSpectrumList().getSpectrum();
            for (int i = 0; i < len; i++) {
                al.add(getPeakListArray(l.get(i + start).getMzArrayBinary()));
            }
            return al;
        }
        if (f.getName().equals(this.intensity_values)) {
            final ArrayList<Array> al = new ArrayList<>();
            final MzData mzd = unmarshal(f.getParent());
            int start = 0;
            int len = getScanCount(mzd);
            if (f.getIndex() != null) {
                Range[] r = f.getIndex().getRange();
                if (r != null && r[0] != null) {
                    start = r[0].first();
                    len = r[0].length();
                }
            }
            final List<Spectrum> l = mzd.getSpectrumList().getSpectrum();
            for (int i = 0; i < len; i++) {
                al.add(getPeakListArray(l.get(i + start).getIntenArrayBinary()));
            }
            return al;
        }
        // return an empty list as default
        return new ArrayList<>();
    }

    private Array readMinMaxMassValueArray(final IVariableFragment var,
            final MzData mp) {
        log.debug("readMinMaxMassValueArray");
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

    private Array readMZI(final IVariableFragment var, final MzData mp) {
        if (var.getIndex() == null) {
            IVariableFragment scanIndex = null;
            try {
                scanIndex = var.getParent().getChild(scan_index);
            } catch (ResourceNotAvailableException rnae) {
                scanIndex = var.getParent().addChild(scan_index);
            }
            var.setIndex(scanIndex);
        }
        int npeaks = 0;
        int start = 0;
        int scans = getScanCount(mp);
        log.debug("Scan count: {}", scans);
        if (var.getIndex() != null) {
            final Range[] r = var.getIndex().getRange();
            if (r != null) {
                start = r[0].first();
                scans = r[0].length();
            }
        }
        log.debug("Scans: {}", scans);
        for (int i = start; i < scans; i++) {
            npeaks += getNumPeaks(mp, i);
        }

        if (var.getName().equals(this.mass_values)) {
            final Array a = new ArrayDouble.D1(npeaks);
            npeaks = 0;
            for (int i = start; i < scans; i++) {
                log.debug("Reading scan {} of {}", (i + 1), scans);
                final Array b = getMassValues(mp, i);
                Array.arraycopy(b, 0, a, npeaks, b.getShape()[0]);
                npeaks += b.getShape()[0];
            }
            return a;
            // f.setArray(a);
        } else if (var.getName().equals(this.intensity_values)) {
            final Array a = new ArrayDouble.D1(npeaks);
            npeaks = 0;
            for (int i = start; i < scans; i++) {
                log.debug("Reading scan {} of {}", (i + 1), scans);
                final Array b = getIntensityValues(mp, i);
                Array.arraycopy(b, 0, a, npeaks, b.getShape()[0]);
                npeaks += b.getShape()[0];
                log.debug("npeaks after: {}", npeaks);
            }
            return a;
            // f.setArray(a);
        }
        throw new IllegalArgumentException(
                "Don't know how to handle variable: " + var.getName());
        // }
        // return f.getArray();
    }

    private Array readScanAcquisitionTimeArray(final IVariableFragment var,
            final MzData mp) {
        log.debug("readScanAcquisitionTimeArray");
        int scans = getScanCount(mp);
        int start = 0;
        final Range[] r = var.getRange();
        if (r != null) {
            start = r[0].first();
            scans = r[0].length();
        }
        final ArrayDouble.D1 sat = new ArrayDouble.D1(scans);
        for (int i = start; i < scans; i++) {
            sat.set(i, getRT(mp, i));
            log.debug("RT({})={}", i, sat.get(i));
        }
        // f.setArray(sat);
        return sat;
    }

    private Array readScanIndex(final IVariableFragment var, final MzData mp) {
        int npeaks = 0;
        int scans = getScanCount(mp);
        int start = 0;
        final Range[] r = var.getRange();
        if (r != null) {
            start = r[0].first();
            scans = r[0].length();
        }
        log.debug("Creating index array with {} elements", scans);
        final ArrayInt.D1 scan_index = new ArrayInt.D1(scans);
        for (int i = start; i < scans; i++) {
            final int peaks = getNumPeaks(mp, i);
            // current npeaks is index into larger arrays for current scan
            log.debug("Scan {} from {} to {}", new Object[]{i, npeaks,
                (npeaks + peaks - 1)});
            scan_index.set(i, npeaks);
            npeaks += peaks;
        }
        EvalTools.notNull(scan_index, this);
        return scan_index;
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
        final IVariableFragment ti = getVariable(f, this.total_intensity);
        final IVariableFragment sat = getVariable(f, this.scan_acquisition_time);
        final IVariableFragment si = getVariable(f, this.scan_index);
        final IVariableFragment mrmin = getVariable(f, this.mass_range_min);
        final IVariableFragment mrmax = getVariable(f, this.mass_range_max);
        final IVariableFragment mv = getVariable(f, this.mass_values);
        final IVariableFragment iv = getVariable(f, this.intensity_values);
        final ArrayList<IVariableFragment> al = new ArrayList<>();
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
    /** {@inheritDoc} */
    @Override
    public IVariableFragment readStructure(final IVariableFragment f)
            throws IOException, ResourceNotAvailableException {
        final MzData mp = unmarshal(f.getParent());
        final int scancount = getScanCount(mp);
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
                for (int i = 0; i < scancount; i++) {
                    npeaks += getNumPeaks(mp, i);
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
                        "Could not rap header of file "
                        + f.getParent().getUri());
            }
        } else {
            throw new ResourceNotAvailableException(
                    "Unknown varname to mzXML mapping for varname " + varname);
        }
        return f;
    }

    private Array readTotalIntensitiesArray(final IVariableFragment var,
            final MzData mp) {
        final double[] d = new double[getScanCount(mp)];
        for (int i = 0; i < getScanCount(mp); i++) {
            final Array a = getIntensityValues(mp, i);
            final double v = MAMath.sumDouble(a);
            d[i] = v;
        }
        return Array.factory(d);
    }

    /** {@inheritDoc} */
    @Override
    public List<String> supportedFormats() {
        return Arrays.asList(this.fileEnding);
    }

    private Unmarshaller getUnmarshaller(final IFileFragment ff) {
        Unmarshaller um = MZDataDataSource.fileToIndex.get(ff.getUri());
        if (um != null) {
            log.info("Retrieved unmarshaller from cache!");
            return um;
        }
        if (new File(ff.getUri()).exists()) {
            log.debug("Initializing unmarshaller for file {}", ff.getUri());
            JAXBContext jc;
            try {
                jc = JAXBContext.newInstance("maltcms.io.xml.mzData");
                final Unmarshaller u = jc.createUnmarshaller();
                MZDataDataSource.fileToIndex.put(ff.getUri(), u);
                return u;
            } catch (final JAXBException e) {
                throw new RuntimeException(e.fillInStackTrace());
            }
        }
        throw new ResourceNotAvailableException("File fragment " + ff.getUri() + " does not exist!");
    }

    private MzData unmarshal(final IFileFragment iff) {
        if (new File(iff.getUri()).exists()) {
            MzData mzData = fileToMzData.get(iff.getUri());
            if (mzData == null) {
                try {
                    final Unmarshaller u = getUnmarshaller(iff);
                    XMLStreamReader xmlStreamReader;
                    try {
                        xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(new File(iff.getUri())));
                        mzData = u.unmarshal(xmlStreamReader, MzData.class).getValue();
                        fileToMzData.put(iff.getUri(), mzData);
                    } catch (XMLStreamException | FileNotFoundException ex) {
                        Logger.getLogger(MZDataDataSource.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (final JAXBException e) {
                    throw new RuntimeException(e.fillInStackTrace());
                }
            }
            return mzData;
        }
        throw new ResourceNotAvailableException("File fragment " + iff.getUri() + " does not exist!");
    }

    /** {@inheritDoc} */
    @Override
    public boolean write(final IFileFragment f
    ) {
        EvalTools.notNull(this.ndf, this);
        // TODO Implement real write support
        log.info("Saving {} with MZXMLStaxDataSource", f.getUri());
        log.info("Changing output file from: {}", f.toString());
        File file = new File(f.getUri());
        String filename = StringTools.removeFileExt(file.getAbsolutePath());
        filename += ".cdf";
        f.setFile(filename);
        f.addSourceFile(new FileFragment(f.getUri()));
        log.info("To: {}", filename);
        return Factory.getInstance().getDataSourceFactory().getDataSourceFor(f).write(f);
    }
}
