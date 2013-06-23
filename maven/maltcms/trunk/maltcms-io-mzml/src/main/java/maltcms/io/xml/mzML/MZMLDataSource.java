/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import maltcms.io.andims.NetcdfDataSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.MAMath;
import ucar.ma2.Range;
import ucar.ma2.MAMath.MinMax;
import ucar.nc2.Dimension;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.Run;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;
import cross.Factory;
import cross.annotations.Configurable;
import cross.cache.CacheFactory;
import cross.cache.ICacheDelegate;
import cross.datastructures.cache.SerializableArray;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableVariableFragment2;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.datastructures.tools.EvalTools;
import cross.tools.StringTools;
import java.io.File;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import uk.ac.ebi.jmzml.model.mzml.Chromatogram;
import uk.ac.ebi.jmzml.model.mzml.FileDescription;
import uk.ac.ebi.jmzml.model.mzml.SourceFile;
import uk.ac.ebi.jmzml.model.mzml.SourceFileList;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

@Slf4j
@ServiceProvider(service = IDataSource.class)
public class MZMLDataSource implements IDataSource {

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
	private String second_column_elution_timeAccession = "MS:1002083 ";
	@Configurable(name = "var.total_ion_current_chromatogram", value = "total_ion_current_chromatogram")
	private String total_ion_current_chromatogram = "total_ion_current_chromatogram";
	private String total_ion_current_chromatogramAccession = "MS:1000235";
	@Configurable(name = "var.total_ion_current_chromatogram_scan_acquisition_time", value = "total_ion_current_chromatogram_scan_acquisition_time")
	private String total_ion_current_chromatogram_scan_acquisition_time = "total_ion_current_chromatogram_scan_acquisition_time";
	private String total_ion_current_chromatogram_scan_acquisition_timeAccession = "MS:1000595";
	private String ms_level = "ms_level";
	private String msLevelAccession = "MS:1000511";
	private static ICacheDelegate<IFileFragment, MzMLUnmarshaller> fileToIndex = CacheFactory.createVolatileCache(MZMLDataSource.class.getName() + "-unmarshaller", 300, 600, 20);
	private static ICacheDelegate<MzMLUnmarshaller, Run> unmarshallerToRun = CacheFactory.createVolatileCache(MZMLDataSource.class.getName() + "-unmarshaller-to-run", 300, 600, 2);
	private static ICacheDelegate<IVariableFragment, SerializableArray> variableToArrayCache = CacheFactory.createDefaultCache(MZMLDataSource.class.getName(), 50);

	private ICacheDelegate<IVariableFragment, SerializableArray> getCache() {
		return MZMLDataSource.variableToArrayCache;
	}

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

	@Override
	public void configurationChanged(final ConfigurationEvent arg0) {
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
		MzMLUnmarshaller um = MZMLDataSource.fileToIndex.get(ff);
		if (um != null) {
			log.info("Retrieved unmarshaller from cache!");
			return um;
		}
		try {
			log.debug("Initializing unmarshaller for file {}", ff.getUri());
			um = new MzMLUnmarshaller(ff.getUri().toURL());
			MZMLDataSource.fileToIndex.put(ff, um);
			log.debug("mzML file {} is indexed: {}", ff.getUri(), um.isIndexedmzML());
			return um;
		} catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Run getRun(MzMLUnmarshaller mzmlu) {
		Run run = MZMLDataSource.unmarshallerToRun.get(mzmlu);
		if (run != null) {
			log.info("Retrieved run from cache!");
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

	private Spectrum getSpectrum(MzMLUnmarshaller um, int idx) {
		try {
			if (um.isIndexedmzML()) {
				return um.getSpectrumById(um.getSpectrumIDFromSpectrumIndex(Integer.valueOf(idx)));
			} else {
				return getRun(um).getSpectrumList().getSpectrum().get(idx);
			}
		} catch (NullPointerException npe) {
			return getRun(um).getSpectrumList().getSpectrum().get(idx);
		} catch (MzMLUnmarshallerException ex) {
			java.util.logging.Logger.getLogger(MZMLDataSource.class.getName()).log(Level.SEVERE, null, ex);
			throw new ResourceNotAvailableException(ex);
		}
	}

	private int getPointCount(final Spectrum s) {
		return s.getBinaryDataArrayList().getBinaryDataArray().get(0).getBinaryDataAsNumberArray().length;
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
			String unit = getRTUnit(s);
			rt = convertRT(rt, unit);
		} catch (NullPointerException npe) {
			log.warn("Could not retrieve spectrum acquisition time!");
		} catch (ResourceNotAvailableException rne) {
			log.warn("Could not retrieve spectrum acquisition time!", rne);
		}
		return rt;
	}

	private int getScanCount(final MzMLUnmarshaller um) {
		if (um.isIndexedmzML()) {
			return um.getSpectrumIDs().size();
		}
		return getRun(um).getSpectrumList().getCount();
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
	 * @param run
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
		for (int i = start; i < start + scans; i++) {
			Spectrum spec = getSpectrum(um, i);
			Array a = getMassValues(spec);
			final Tuple2D<Double, Double> t = getMinMaxMassRange(a);
			min_mass = Math.min(min_mass, t.getFirst());
			max_mass = Math.max(max_mass, t.getSecond());
		}
		for (int i = 0; i < start + scans; i++) {
			mass_range_min1.setDouble(i, Math.floor(min_mass));
			mass_range_max1.setDouble(i, Math.ceil(max_mass));
		}
		return new Tuple2D<Array, Array>(mass_range_min1, mass_range_max1);
	}

	private Array loadArray(final IFileFragment f, final IVariableFragment var) {
		SerializableArray sa = getCache().get(var);
		if (sa != null) {
			log.debug("Retrieved variable data array from cache for " + var);
			return sa.getArray();
		}
		Array a = null;
		MzMLUnmarshaller mzu = getUnmarshaller(f);
		final Run r = getRun(mzu);
		final String varname = var.getName();
		log.info("Trying to read variable " + var.getName());
		List<CVParam> parameters = r.getCvParam();
		log.debug("Run has {} cvparams!", parameters.size());
		for (CVParam param : parameters) {
			log.debug("CVParam: {}", param);
		}
		if (varname.equals(this.source_files)) {
			a = readSourceFiles(f, mzu);
			// Read mass_values or intensity_values for whole chromatogram
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
			a = readMinMaxMassValueArray(var, mzu);
			// read scan_acquisition_time
		} else if (varname.equals(this.scan_acquisition_time)) {
			a = readScanAcquisitionTimeArray(var, mzu);
		} else if (varname.equals(this.modulation_time)) {
			a = readModulationTimeArray(var, mzu);
		} else if (varname.equals(this.first_column_elution_time)) {
			a = readElutionTimeArray(var, r, mzu, this.first_column_elution_timeAccession);
		} else if (varname.equals(this.second_column_elution_time)) {
			a = readElutionTimeArray(var, r, mzu, this.second_column_elution_timeAccession);
		} else if (varname.equals(this.ms_level)) {
			a = readMsLevelArray(var, mzu);
		} else if (varname.equals(this.total_ion_current_chromatogram)) {
			a = readTotalIonCurrentChromatogram(var, mzu, true);
		} else if (varname.equals(this.total_ion_current_chromatogram_scan_acquisition_time)) {
			a = readTotalIonCurrentChromatogram(var, mzu, false);
		} else {
			throw new ResourceNotAvailableException(
					"Unknown variable name to mzML mapping for " + varname);
		}
		if (a != null) {
			getCache().put(var, new SerializableArray(a));
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
		MzMLUnmarshaller um = getUnmarshaller(f.getParent());
		if (f.getName().equals(this.mass_values)) {
			final ArrayList<Array> al = new ArrayList<Array>();
			int start = 0;
			int len = getScanCount(um);
			if (f.getIndex() != null) {
				Range[] r = f.getIndex().getRange();
				if (r != null && r[0] != null) {
					start = Math.max(0, r[0].first());
					len = Math.min(len, r[0].length());
				}
			}
			for (int i = start; i < start + len; i++) {
				al.add(getMassValues(getSpectrum(um, i)));
			}
			return al;
		}
		if (f.getName().equals(this.intensity_values)) {
			final ArrayList<Array> al = new ArrayList<Array>();
			int start = 0;
			int len = getScanCount(um);
			if (f.getIndex() != null) {
				Range[] r = f.getIndex().getRange();
				if (r != null && r[0] != null) {
					start = Math.max(0, r[0].first());
					len = Math.min(len, r[0].length());
				}
			}
			for (int i = start; i < start + len; i++) {
				al.add(getIntensityValues(getSpectrum(um, i)));
			}
			return al;
		}
		// return an empty list as default
		return new ArrayList<Array>();
	}

	private Array readSourceFiles(final IFileFragment f, final MzMLUnmarshaller mzmu) {
		SourceFileList sfl = getSourceFiles(mzmu);
		List<String> sourceFilePaths = new LinkedList<String>();
		for (SourceFile sfs : sfl.getSourceFile()) {
			sourceFilePaths.add(sfs.getLocation());
		}
		if (sourceFilePaths.isEmpty()) {
			return null;
		}
		Array a = Array.makeArray(DataType.STRING, new LinkedList<String>(sourceFilePaths));
		log.info("Returning source files: ", a);
		return a;
	}

	private Array readElutionTimeArray(final IVariableFragment var, final Run run, final MzMLUnmarshaller um, final String accession) {
		int scans = getScanCount(um);
		int start = 0;
		final Range[] r = var.getRange();
		if (r != null) {
			start = Math.max(0, r[0].first());
			scans = Math.min(scans, r[0].length());
		}
		log.debug("Creating index array with {} elements", scans);
		final ArrayDouble.D1 elutionTime = new ArrayDouble.D1(scans);
		for (int i = start; i < start + scans; i++) {
			Spectrum s = getSpectrum(um, i);
			List<CVParam> cvParams = s.getCvParam();
			CVParam cvp = findParam(cvParams, accession);
			String value = cvp.getValue();
			double rt = convertRT(Double.parseDouble(value), cvp.getUnitName());
			elutionTime.set(i, rt);
		}
		EvalTools.notNull(elutionTime, this);
		return elutionTime;
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
			for (int i = start; i < start + scans; i++) {
				log.debug("Reading scan {} of {}", (i + 1), scans);
				tic.setDouble(i, MAMath.sumDouble(getIntensityValues(getSpectrum(um, i))));
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
		int npeaks = 0;
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
		for (int i = start; i < start + scans; i++) {
			npeaks += getPointCount(getSpectrum(um, i));
		}

		if (var.getName().equals(this.mass_values)) {
			final Array a = new ArrayDouble.D1(npeaks);
			npeaks = 0;
			for (int i = start; i < start + scans; i++) {
				log.debug("Reading scan {} of {}", (i + 1), scans);
				final Array b = getMassValues(getSpectrum(um, i));
				Array.arraycopy(b, 0, a, npeaks, b.getShape()[0]);
				npeaks += b.getShape()[0];
			}
			return a;
			// f.setArray(a);
		} else if (var.getName().equals(this.intensity_values)) {
			final Array a = new ArrayDouble.D1(npeaks);
			npeaks = 0;
			for (int i = start; i < start + scans; i++) {
				log.debug("Reading scan {} of {}", (i + 1), scans);
				final Array b = getIntensityValues(getSpectrum(um, i));
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
		for (int i = start; i < start + scans; i++) {
			CVParam param = findParam(getSpectrum(um, i).getCvParam(), msLevelAccession);
			int paramMsLevel = -1;
			if (param != null && !param.getValue().isEmpty()) {
				paramMsLevel = Integer.parseInt(param.getValue());
			}
			a.setInt(i, paramMsLevel);
		}
		return a;
	}

	private Array readScanAcquisitionTimeArray(final IVariableFragment var,
			final MzMLUnmarshaller um) {
		log.debug("readScanAcquisitionTimeArray");
		int scans = getScanCount(um);
		int start = 0;
		final Range[] r = var.getRange();
		if (r != null) {
			start = Math.max(0, r[0].first());
			scans = Math.min(scans, r[0].length());
		}
		final ArrayDouble.D1 sat = new ArrayDouble.D1(scans);
		for (int i = start; i < start + scans; i++) {
			sat.set(i, getRT(getSpectrum(um, i)));
			log.debug("RT({})={}", i, sat.get(i));
		}
		// f.setArray(sat);
		return sat;
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
		for (int i = start; i < start + scans; i++) {
			final int peaks = getMassValues(getSpectrum(um, i)).getShape()[0];
			// current npeaks is index into larger arrays for current scan
			log.debug("Scan {} from {} to {}", new Object[]{i, npeaks,
				(npeaks + peaks - 1)});
			scan_index.set(i, npeaks);
			npeaks += peaks;
		}
		EvalTools.notNull(scan_index, this);
		return scan_index;
	}

	private Array readModulationTimeArray(IVariableFragment var, MzMLUnmarshaller um) {
		List<CVParam> parameters = getRun(um).getCvParam();
		for (CVParam param : parameters) {
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
	@Override
	public ArrayList<IVariableFragment> readStructure(final IFileFragment f)
			throws IOException {
		final ArrayList<IVariableFragment> al = new ArrayList<IVariableFragment>();
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
	@Override
	public IVariableFragment readStructure(final IVariableFragment f)
			throws IOException, ResourceNotAvailableException {
		MzMLUnmarshaller um = getUnmarshaller(f.getParent());
		final int scancount = getScanCount(um);
		final String varname = f.getName();
		// Read mass_values or intensity_values for whole chromatogram
		if (varname.equals(this.scan_index)
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
				for (int i = 0; i < scancount; i++) {
					npeaks += getPointCount(getSpectrum(um, i));
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
		if (chromatograms == null || chromatograms.isEmpty()) {
			throw new ResourceNotAvailableException("No chromatograms defined in mzML file {}" + f.getName());
		}
		Chromatogram ticChromatogram = null;
		try {
			ticChromatogram = readTotalIonCurrentChromatogram(um, f);
		} catch (ResourceNotAvailableException rnae) {
			//ignore, will handle null chromatogram further down
		}
		if (ticChromatogram != null && ticChromatogram.getDefaultArrayLength() != getScanCount(um)) {
			log.warn("TIC Chromatogram point number does not match scan count! Recreating TIC from spectra!");
			ticChromatogram = null;
		}
		if (ticChromatogram == null) {
			log.warn("No TIC chromatograms defined in mzML file {} reconstructing from spectra!", f.getName());
			return readTicFromMzi(f.getChild(fallback), um);
		}
		BinaryDataArray intensitiesArray = ticChromatogram.getBinaryDataArrayList().getBinaryDataArray().get(1);
		Number[] intensities = intensitiesArray.getBinaryDataAsNumberArray();
		final ArrayDouble.D1 intensA = new ArrayDouble.D1(intensities.length);
		for (int i = 0; i < intensities.length; i++) {
			intensA.set(i, intensities[i].doubleValue());
		}
		return intensA;
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
			CVParam rtp = findParam(ticSatArray.getCvParam(), "MS:1000595");
			String unit = rtp.getUnitName();
			for (int i = 0; i < time.length; i++) {
				double value = convertRT(time[i].doubleValue(), unit);
				ticSats.set(i, value);
			}
			return ticSats;
		}
	}

	@Override
	public List<String> supportedFormats() {
		return Arrays.asList(this.fileEnding);
	}

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
