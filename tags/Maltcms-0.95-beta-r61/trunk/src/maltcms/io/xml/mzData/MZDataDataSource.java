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

package maltcms.io.xml.mzData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import maltcms.io.andims.NetcdfDataSource;
import maltcms.io.xml.mzData.MzData.SpectrumList.Spectrum;
import maltcms.io.xml.mzData.PeakListBinaryType.Data;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import cross.Logging;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ResourceNotAvailableException;
import cross.io.DataSourceFactory;
import cross.io.IDataSource;
import cross.io.misc.Base64Util;
import cross.tools.ArrayTools;
import cross.tools.EvalTools;
import cross.tools.StringTools;

public class MZDataDataSource implements IDataSource {

	private Logger log = Logging.getLogger(this);

	private final String[] fileEnding = new String[] { "mzdata" };

	private String mass_values = "mass_values";

	private String intensity_values = "intensity_values";

	private String total_intensity = "total_intensity";

	private String scan_acquisition_time = "scan_acquisition_time";

	private String scan_index = "scan_index";

	private String mass_range_min = "mass_range_min";

	private String mass_range_max = "mass_range_max";

	private NetcdfDataSource ndf = null;

	private String source_files = "source_files";

	@Override
	public int canRead(final IFileFragment ff) {
		MzData mzd = unmarshal(ff);
		if (StringTools.getFileExtension(ff.getAbsolutePath())
		        .equalsIgnoreCase("mzData")) {
			return 1;
		}
		return 0;
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
		this.ndf = new NetcdfDataSource();
		this.ndf.configure(configuration);
	}

	private List<Float> decodeBinaryArray(final Data d) {
		Logging.getLogger(this).debug("Number of data points {}", d.getLength());
		Logging.getLogger(this).debug("Number of Bytes in encoded array {}",
		        d.getValue().length);
		Logging.getLogger(this).debug("Precision of data points {}",
		        d.getPrecision());
		Logging.getLogger(this).debug("Endian encoding of data points {}",
		        d.getEndian());
		final List<Float> intens = Base64Util.byteArrayToFloatList(
		        d.getValue(), d.getEndian().equalsIgnoreCase("little") ? false
		                : true);
		return intens;
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

	/**
	 * Read min and max_mass_range to determine bin sizes.
	 * 
	 * @param var
	 * @param mp
	 * @return a Tuple2D<Array,Array> with mass_range_min as first and
	 *         mass_range_max as second array
	 */
	protected Tuple2D<Array, Array> initMinMaxMZ(final IVariableFragment var,
	        final MzData mp) {
		this.log.debug("Loading {} and {}", new Object[] { this.mass_range_min,
		        this.mass_range_max });
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
			Tuple2D<Double, Double> t = getMinMaxMassRange(mp, i);
			min_mass = Math.min(min_mass, t.getFirst());
			max_mass = Math.max(max_mass, t.getSecond());
		}
		ArrayTools.fill(mass_range_min1, Math.floor(min_mass));
		ArrayTools.fill(mass_range_max1, Math.ceil(max_mass));
		return new Tuple2D<Array, Array>(mass_range_min1, mass_range_max1);
	}

	private Array loadArray(final IFileFragment f, final IVariableFragment var) {
		final MzData mzd = unmarshal(f);
		Array a = null;
		final String varname = var.getVarname();
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
		return a;
	}

	private Array readTotalIntensitiesArray(final IVariableFragment var,
	        final MzData mp) {
		double[] d = new double[getScanCount(mp)];
		for (int i = 0; i < getScanCount(mp); i++) {
			Array a = getIntensityValues(mp, i);
			double v = ArrayTools.integrate(a);
			d[i] = v;
		}
		return Array.factory(d);
	}

	private Array readMinMaxMassValueArray(final IVariableFragment var,
	        final MzData mp) {
		this.log.debug("readMinMaxMassValueArray");
		final Tuple2D<Array, Array> t = initMinMaxMZ(var, mp);
		if (var.getVarname().equals(this.mass_range_min)) {
			return t.getFirst();
		}
		if (var.getVarname().equals(this.mass_range_max)) {
			return t.getSecond();
		}
		throw new IllegalArgumentException(
		        "Method accepts only one of mass_range_min or mass_range_max as varname!");
	}

	private Tuple2D<Double, Double> getMinMaxMassRange(final MzData mp, int scan) {
		SpectrumSettingsType.SpectrumInstrument si = mp.spectrumList.spectrum
		        .get(scan).getSpectrumDesc().getSpectrumSettings()
		        .getSpectrumInstrument();
		return new Tuple2D<Double, Double>(
		        Double.valueOf(si.getMzRangeStart()), Double.valueOf(si
		                .getMzRangeStop()));
	}

	private Array readMZI(final IVariableFragment var, final MzData mp) {
		// if(!f.hasArray()) {
		int npeaks = 0;
		int start = 0;
		int scans = getScanCount(mp);
		final Range[] r = var.getRange();
		if (r != null) {
			start = r[0].first();
			scans = r[0].length();
		}
		for (int i = start; i < scans; i++) {
			npeaks += getNumPeaks(mp, i);
		}

		if (var.getVarname().equals(this.mass_values)) {
			final Array a = new ArrayDouble.D1(npeaks);
			npeaks = 0;
			for (int i = start; i < scans; i++) {
				this.log.debug("Reading scan {} of {}", (i + 1), scans);
				Array b = getMassValues(mp, i);
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
				Array b = getIntensityValues(mp, i);
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

	private ArrayList<Array> readMZIScans(final IVariableFragment var,
	        final MzData mp) {
		int scans = getScanCount(mp);
		int start = 0;
		final Range[] r = var.getRange();
		if (r != null) {
			start = r[0].first();
			scans = r[0].length();
		}
		final ArrayList<Array> al = new ArrayList<Array>();
		if (var.getVarname().equals(this.mass_values)) {
			for (int i = start; i < scans; i++) {
				al.add(getMassValues(mp, i));
			}
		} else if (var.getVarname().equals(this.intensity_values)) {
			for (int i = start; i < scans; i++) {
				al.add(getIntensityValues(mp, i));
			}
		}
		return al;
	}

	private Array readScanAcquisitionTimeArray(final IVariableFragment var,
	        final MzData mp) {
		this.log.debug("readScanAcquisitionTimeArray");
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
			this.log.debug("RT({})={}", i, sat.get(i));
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
		this.log.debug("Creating index array with {} elements", scans);
		final ArrayInt.D1 scan_index = new ArrayInt.D1(scans);
		for (int i = start; i < scans; i++) {
			int peaks = getNumPeaks(mp, i);
			// current npeaks is index into larger arrays for current scan
			this.log.debug("Scan {} from {} to {}", new Object[] { i, npeaks,
			        (npeaks + peaks - 1) });
			scan_index.set(i, npeaks);
			npeaks += peaks;
		}
		EvalTools.notNull(scan_index, this);
		return scan_index;
	}

	@Override
	public ArrayList<Array> readIndexed(final IVariableFragment f)
	        throws IOException, ResourceNotAvailableException {
		if (f.getVarname().equals(this.mass_values)) {
			final ArrayList<Array> al = new ArrayList<Array>();
			final MzData mzd = unmarshal(f.getParent());
			final List<Spectrum> l = mzd.getSpectrumList().getSpectrum();
			int i = 0;
			for (final Spectrum s : l) {
				final Data d = s.getMzArrayBinary().getData();
				final List<Float> masses = decodeBinaryArray(d);
				Logging.getLogger(this).debug("Scan {} masses: {}", i,
				        Arrays.toString(masses.toArray()));
				final ArrayDouble.D1 a = new ArrayDouble.D1(masses.size());
				for (int j = 0; j < masses.size(); j++) {
					a.set(j, masses.get(j));
				}
				al.add(a);
				i++;
			}
			return al;
		}
		if (f.getVarname().equals(this.intensity_values)) {
			final ArrayList<Array> al = new ArrayList<Array>();
			final MzData mzd = unmarshal(f.getParent());
			final List<Spectrum> l = mzd.getSpectrumList().getSpectrum();
			int i = 0;
			for (final Spectrum s : l) {
				final Data d = s.getIntenArrayBinary().getData();
				final List<Float> intens = decodeBinaryArray(d);
				Logging.getLogger(this).debug("Scan {} intensities: {}", i,
				        Arrays.toString(intens.toArray()));
				final ArrayDouble.D1 a = new ArrayDouble.D1(intens.size());
				for (int j = 0; j < intens.size(); j++) {
					a.set(j, intens.get(j));
				}
				al.add(a);
				i++;
			}
			return al;
		}
		// return an empty list as default
		return new ArrayList<Array>();
	}

	@Override
	public Array readSingle(final IVariableFragment f) throws IOException,
	        ResourceNotAvailableException {
		this.log.debug("readSingle of {} in {}", f.getVarname(), f.getParent()
		        .getAbsolutePath());
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
	 * IVariableFragment)
	 */
	@Override
	public IVariableFragment readStructure(final IVariableFragment f)
	        throws IOException, ResourceNotAvailableException {
		final MzData mp = unmarshal(f.getParent());
		final int scancount = getScanCount(mp);
		final String varname = f.getVarname();
		// Read mass_values or intensity_values for whole chromatogram
		if (varname.equals(this.scan_index)
		        || varname.equals(this.total_intensity)
		        || varname.equals(this.mass_range_min)
		        || varname.equals(this.mass_range_max)
		        || varname.equals(this.scan_acquisition_time)) {
			final Dimension[] dims = new Dimension[] { new Dimension(
			        "scan_number", scancount, true) };
			f.setDimensions(dims);
		} else if (varname.equals(this.mass_values)
		        || varname.equals(this.intensity_values)) {
			int npeaks = 0;
			try {
				for (int i = 0; i < scancount; i++) {
					npeaks += getNumPeaks(mp, i);
				}
				Dimension[] dims = new Dimension[] { new Dimension(
				        "point_number", scancount, true) };
				f.setDimensions(dims);
			} catch (NullPointerException npe) {
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

	private int getNumPeaks(MzData mz, int i) {
		return decodeBinaryArray(
		        mz.getSpectrumList().getSpectrum().get(i).getMzArrayBinary()
		                .getData()).size();
	}

	private int getScanCount(MzData mz) {
		return mz.getSpectrumList().getCount();
	}

	private double getRT(MzData mz, int j) {
		Spectrum s = mz.getSpectrumList().getSpectrum().get(j);
		List<Object> l = s.getSpectrumDesc().getSpectrumSettings()
		        .getSpectrumInstrument().getCvParamOrUserParam();
		for (Object o : l) {
			CvParamType cv = (CvParamType) o;
			if (cv.name.equals("TimeInMinutes")) {
				// multiply with 60 to get seconds equivalent
				double d = Double.parseDouble(cv.value) * 60.0d;
				if (d == Double.NaN) {
					this.log.warn("Parsed time as NaN!");
					return 0.0d;
				}
				return d;
			}
		}
		return 0.0d;
	}

	private Array getMassValues(MzData mz, int j) {
		PeakListBinaryType blbt = mz.getSpectrumList().getSpectrum().get(j)
		        .getMzArrayBinary();
		return getPeakListArray(blbt);
	}

	private Array getIntensityValues(MzData mz, int j) {
		PeakListBinaryType blbt = mz.getSpectrumList().getSpectrum().get(j)
		        .getIntenArrayBinary();
		return getPeakListArray(blbt);
	}

	private int getPrecision(PeakListBinaryType pl) {
		return Integer.parseInt(pl.getData().getPrecision());
	}

	private Array getPeakListArray(PeakListBinaryType blbt) {
		byte[] b = blbt.getData().getValue();
		String endian = blbt.getData().getEndian();
		String precision = blbt.getData().getPrecision();
		int prec = Integer.parseInt(precision);
		int length = blbt.getData().getLength();
		this.log.debug("endian: {} precision: {} length: {}", new Object[] {
		        endian, precision, length });
		Array a = null;
		if (prec == 32) {
			float[] f = Base64Util.byteArrayToFloatArray(b, endian
			        .equals("little") ? false : true, length);
			this.log.debug("{}", Arrays.toString(f));
			// avoid later checking for double/float arrays
			a = new ArrayDouble.D1(f.length);
			for (int i = 0; i < f.length; i++) {
				((ArrayDouble.D1) a).set(i, f[i]);
			}
		} else if (prec == 64) {
			double[] d = Base64Util.byteArrayToDoubleArray(b, endian
			        .equals("little") ? false : true, length);
			this.log.debug("{}", Arrays.toString(d));
			a = new ArrayDouble.D1(d.length);
			for (int i = 0; i < d.length; i++) {
				((ArrayDouble.D1) a).set(i, d[i]);
			}
		} else {
			throw new IllegalArgumentException("Unknown precision: " + prec);
		}
		return a;
	}

	@Override
	public List<String> supportedFormats() {
		return Arrays.asList(this.fileEnding);
	}

	private MzData unmarshal(final IFileFragment iff) {
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance("maltcms.io.xml.mzData");
			final Unmarshaller u = jc.createUnmarshaller();
			final MzData mzd = (MzData) u.unmarshal(new File(iff
			        .getAbsolutePath()));
			return mzd;
		} catch (final JAXBException e) {
			throw new RuntimeException(e.fillInStackTrace());
		}
	}

	@Override
	public boolean write(final IFileFragment f) {
		EvalTools.notNull(this.ndf, this);
		// TODO Implement real write support
		this.log
		        .info("Saving {} with MZXMLStaxDataSource", f.getAbsolutePath());
		this.log.info("Changing output file from: {}", f.toString());
		final String source_file = f.getAbsolutePath();
		String filename = StringTools.removeFileExt(f.getAbsolutePath());
		filename += ".cdf";
		f.setFile(filename);
		f.addSourceFile(FileFragmentFactory.getInstance().create(source_file,
		        null));
		this.log.info("To: {}", filename);
		return DataSourceFactory.getInstance().getDataSourceFor(f).write(f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.apache.commons.configuration.event.ConfigurationListener#
	 * configurationChanged
	 * (org.apache.commons.configuration.event.ConfigurationEvent)
	 */
	@Override
	public void configurationChanged(ConfigurationEvent arg0) {
		// TODO Auto-generated method stub

	}

	private IVariableFragment getVariable(final IFileFragment f,
	        final String name) {
		return (f.hasChild(name) ? f.getChild(name) : new VariableFragment(f,
		        name));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecross.io.IDataSource#readStructure(cross.datastructures.fragments.
	 * IFileFragment)
	 */
	@Override
	public ArrayList<IVariableFragment> readStructure(IFileFragment f)
	        throws IOException {
		final IVariableFragment ti = getVariable(f, this.total_intensity);
		final IVariableFragment sat = getVariable(f, this.scan_acquisition_time);
		final IVariableFragment si = getVariable(f, this.scan_index);
		final IVariableFragment mrmin = getVariable(f, this.mass_range_min);
		final IVariableFragment mrmax = getVariable(f, this.mass_range_max);
		final IVariableFragment mv = getVariable(f, this.mass_values);
		final IVariableFragment iv = getVariable(f, this.intensity_values);
		final ArrayList<IVariableFragment> al = new ArrayList<IVariableFragment>();
		al.addAll(Arrays.asList(new IVariableFragment[] { ti, sat, si, mrmin,
		        mrmax, mv, iv }));
		for (final IVariableFragment ivf : al) {
			readStructure(ivf);
		}
		return al;
	}

}
