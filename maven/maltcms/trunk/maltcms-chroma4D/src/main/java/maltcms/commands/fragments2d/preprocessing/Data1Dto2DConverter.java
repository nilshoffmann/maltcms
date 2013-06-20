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
package maltcms.commands.fragments2d.preprocessing;

import cross.annotations.Configurable;
import cross.cache.ICacheDelegate;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.NotImplementedException;
import cross.tools.StringTools;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * Joins a number of sequentially acquired chromatograms into a two-dimensional
 * chromatogram.
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
public class Data1Dto2DConverter extends AFragmentCommand {

	private final String description = "Joins a number of sequentially acquired chromatograms into a two-dimensional chromatogram.";
	private final WorkflowSlot workflowSlot = WorkflowSlot.FILECONVERSION;
	@Configurable
	private String outputFileName = null;
	@Configurable(description = "The modulation period between column switching in seconds.")
	private double modulationTime = 60.0;
	@Configurable(description = "The number of scans to keep in memory before writing.")
	private int scanCacheSize = 5000;

	@Override
	public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
		File f = null;
//		Collections.sort(msLevel);
		if (outputFileName == null) {
			List<String> filenames = new LinkedList<String>();
			for (IFileFragment ff : in) {
				filenames.add(ff.getName());
			}
			outputFileName = getLCP(filenames);
			if (outputFileName.isEmpty()) {
				outputFileName = StringTools.removeFileExt(filenames.get(0));
			}
		}
		File outputDirectory = getWorkflow().getOutputDirectory(this);
		//TODO maybe change to mzml
//		f = new FileFragment();
		f = new File(outputDirectory, outputFileName + ".cdf");
		log.info("Writing output to {}", f.getName());
//		List<Array> massValuesCache = new CachedReadWriteList<Array>(UUID.nameUUIDFromBytes((f.getUri() + ">mass_values").getBytes()).toString(), new SerializableArrayProxy(), 5000);
//		List<Array> intensityValuesCache = new CachedReadWriteList<Array>(UUID.nameUUIDFromBytes((f.getUri() + ">intensity_values").getBytes()).toString(), new SerializableArrayProxy(), 5000);
//		ICacheDelegate<Integer, Float> satCache = cross.cache.CacheFactory.createDefaultCache(UUID.nameUUIDFromBytes((f.getUri() + "-satValues").getBytes()).toString(), 5000);
//		ICacheDelegate<Integer, Float> fcetCache = cross.cache.CacheFactory.createDefaultCache(UUID.nameUUIDFromBytes((f.getUri() + "-firstColumnElutionTimeValues").getBytes()).toString(), 5000);
//		ICacheDelegate<Integer, Float> scetCache = cross.cache.CacheFactory.createDefaultCache(UUID.nameUUIDFromBytes((f.getUri() + "-secondColumnElutionTimeValues").getBytes()).toString(), 5000);
//		ICacheDelegate<Integer, Integer> ticCache = cross.cache.CacheFactory.createDefaultCache(UUID.nameUUIDFromBytes((f.getUri() + "-ticValues").getBytes()).toString(), 5000);
//		ICacheDelegate<Integer, Integer> msLevelCache = cross.cache.CacheFactory.createDefaultCache(UUID.nameUUIDFromBytes((f.getUri() + "-msLevelValues").getBytes()).toString(), 5000);
//		ICacheDelegate<Integer, Integer> scanIndexCache = cross.cache.CacheFactory.createDefaultCache(UUID.nameUUIDFromBytes((f.getUri() + "-scanIndexValues").getBytes()).toString(), 5000);
		ArrayList<String> inSorted = new ArrayList<String>();

		Map<String, IFileFragment> nameToFragment = new HashMap<String, IFileFragment>();
		int scanCount = 0;
		int pointCount = 0;
		int ticChromScanCount = 0;
		for (IFileFragment ff : in) {
			log.info("Reading layout of file {}", ff);
			inSorted.add(ff.getName());
			nameToFragment.put(ff.getName(), ff);
			scanCount += ff.getChild("scan_index", true).getDimensions()[0].getLength();
			pointCount += ff.getChild("mass_values", true).getDimensions()[0].getLength();
			ticChromScanCount += ff.getChild("total_ion_current_chromatogram", true).getDimensions()[0].getLength();
		}
		try {
			log.info("Creating output file!");
			NetcdfFileWriteable nfw = NetcdfFileWriteable.createNew(f.getAbsolutePath(), true);
			nfw.setLargeFile(true);
			nfw.addDimension("scan_number", scanCount, true, false, false);
			nfw.addDimension("point_number", 100, true, true, false);
			nfw.addDimension("total_ion_current_chromatogram_scan_number", ticChromScanCount, true, false, false);
			Variable nfwScanIndex = nfw.addVariable("scan_index", DataType.INT, "scan_number");
			Variable nfwMassValues = nfw.addVariable("mass_values", DataType.DOUBLE, "point_number");
			Variable nfwIntensityValues = nfw.addVariable("intensity_values", DataType.INT, "point_number");
			Variable nfwSat = nfw.addVariable("scan_acquisition_time", DataType.FLOAT, "scan_number");
			Variable nfwTic = nfw.addVariable("total_intensity", DataType.INT, "scan_number");
			Variable nfwMsLevel = nfw.addVariable("ms_level", DataType.SHORT, "scan_number");
			Variable nfwFcet = nfw.addVariable("first_column_elution_time", DataType.FLOAT, "scan_number");
			Variable nfwScet = nfw.addVariable("second_column_elution_time", DataType.FLOAT, "scan_number");
			Variable nfwTicChrom = nfw.addVariable("total_ion_current_chromatogram", DataType.INT, "total_ion_current_chromatogram_scan_number");
			Variable nfwTicSat = nfw.addVariable("total_ion_current_chromatogram_scan_acquisition_time", DataType.FLOAT, "total_ion_current_chromatogram_scan_number");
			//add total_ion_current_chromatogram etc.
			log.info("Creating output file structure!");
			nfw.create();
			nfw.close();
			nfw = NetcdfFileWriteable.openExisting(f.getAbsolutePath());
			Collections.sort(inSorted, new NaturalSortOrderComparator());
			int scanIndexOffset = 0;
			int offset = 0;
			int globalScanIndex = 0;
			double globalScanTime = Double.NaN, globalModulationStartTime = Double.NaN;
			for (String key : inSorted) {
				IFileFragment ff = nameToFragment.remove(key);
				log.info("Processing file {}", ff.getName());
				IVariableFragment satVar = ff.getChild("scan_acquisition_time");
				IVariableFragment scanIndexVar = ff.getChild("scan_index");
//				Array sourceScanIndexArrray = scanIndexVar.getArray();
				Array sourceSatArray = satVar.getArray();
				satVar.clear();
				int sourceScans = sourceSatArray.getShape()[0];
				Array targetSatArray = Array.factory(DataType.FLOAT, sourceSatArray.getShape());
				Array targetScanIndexArray = Array.factory(DataType.INT, sourceSatArray.getShape());
				Array targetFcetArray = Array.factory(DataType.FLOAT, sourceSatArray.getShape());
				Array targetScetArray = Array.factory(DataType.FLOAT, sourceSatArray.getShape());

				IVariableFragment massValuesVar = ff.getChild("mass_values");
				massValuesVar.setIndex(scanIndexVar);
				IVariableFragment intensityValuesVar = ff.getChild("intensity_values");
				intensityValuesVar.setIndex(scanIndexVar);
				List<Array> sourceMassValues = massValuesVar.getIndexedArray();
				List<Array> sourceIntensityValues = intensityValuesVar.getIndexedArray();

				//initialize local scan time to zero (no local offset)
				double localScanTime = 0;
				//initialize local modulation start time to the first scan acquisition time value
				double localModulationStartTime = sourceSatArray.getDouble(0);
				//list of mass values and intensity values, write every few scans
				MSChunk chunk = new MSChunk(nfw, scanCacheSize);
				int scanIndex = 0;
				for (int i = 0; i < sourceScans; i++) {
					log.info("Local scan {}/{}, global scan {}/{}", new Object[]{(i + 1), sourceScans, (globalScanIndex + 1), scanCount});
					double sat = sourceSatArray.getDouble(i);
					if (Double.isNaN(globalModulationStartTime)) {
						log.info("Initializing globalModulationStartTime to {}", sat);
						globalModulationStartTime = sat;
					}
					//difference to modulation start is local scan time
					localScanTime = sat - localModulationStartTime;
					//local scan time offset by the last global modulation start time is the global scan time
					globalScanTime = globalModulationStartTime + localScanTime;
					targetSatArray.setDouble(i, globalScanTime);
					//duration of this modulation
					double modulationDuration = localScanTime - localModulationStartTime;
					if (modulationDuration > modulationTime) {
						//we are at the beginning of a new modulation
						//update localModulationStartTime
						localModulationStartTime = localScanTime;
						globalModulationStartTime = globalScanTime;
					}
					//set values accordingly
					targetFcetArray.setDouble(i, globalModulationStartTime);
					targetScetArray.setDouble(i, localScanTime);

					targetScanIndexArray.setInt(scanIndex, scanIndexOffset);
					Array massArray = sourceMassValues.get(i);
					chunk.addMassValues(massArray);
					chunk.addIntensityValues(sourceIntensityValues.get(i));
					scanIndexOffset += massArray.getShape()[0];
					chunk.checkWriteable();
					if (i == sourceScans - 1) {
						log.debug("Forcing write on last scan!");
						chunk.write();
					}
					scanIndex++;
					globalScanIndex++;
				}
				scanIndexVar.clear();
				massValuesVar.clear();
				intensityValuesVar.clear();
				chunk = null;
				try {
					nfw.write("total_intensity", new int[]{offset}, ff.getChild("total_intensity").getArray());
					nfw.write("ms_level", new int[]{offset}, ff.getChild("ms_level").getArray());
					nfw.write("scan_acquisition_time", new int[]{offset}, targetSatArray);
					targetSatArray = null;
					nfw.write("first_column_elution_time", new int[]{offset}, targetFcetArray);
					targetFcetArray = null;
					nfw.write("second_column_elution_time", new int[]{offset}, targetScetArray);
					targetScetArray = null;
					nfw.write("scan_index", new int[]{offset}, targetScanIndexArray);
					targetScanIndexArray = null;
				} catch (InvalidRangeException ex) {
					Logger.getLogger(Data1Dto2DConverter.class.getName()).log(Level.SEVERE, null, ex);
				}
				offset += sourceScans;
				ff.clearArrays();
				ff = null;
//				file++;
			}
			nfw.close();
			log.info("Done.");
		} catch (IOException ex) {
			Logger.getLogger(Data1Dto2DConverter.class.getName()).log(Level.SEVERE, null, ex);
		}

//		//
//		IVariableFragment scanIndexVar = f.addChild("scan_index");
//		scanIndexVar.setArray(toArray(scanIndexCache, DataType.INT));
//		//
//		IVariableFragment massValuesVar = f.addChild("mass_values");
//		massValuesVar.setIndex(scanIndexVar);
//		massValuesVar.setIndexedArray(massValuesCache);
//		massValuesVar.setDataType(DataType.FLOAT);
//		//
//		IVariableFragment intensityValuesVar = f.addChild("intensity_values");
//		intensityValuesVar.setIndex(scanIndexVar);
//		intensityValuesVar.setIndexedArray(intensityValuesCache);
//		intensityValuesVar.setDataType(DataType.INT);
//		//
//		IVariableFragment satVar = f.addChild("scan_acquisition_time");
//		satVar.setArray(toArray(satCache, DataType.FLOAT));
//		//
//		IVariableFragment ticVar = f.addChild("total_intensity");
//		ticVar.setArray(toArray(ticCache, DataType.INT));
//		//
//		IVariableFragment fcetVar = f.addChild("first_column_elution_time");
//		fcetVar.setArray(toArray(fcetCache, DataType.FLOAT));
//		//
//		IVariableFragment scetVar = f.addChild("second_column_elution_time");
//		scetVar.setArray(toArray(scetCache, DataType.FLOAT));
//		//
//		IVariableFragment modTimeVar = f.addChild("modulation_time");
//		modTimeVar.setDimensions(new Dimension("modulation_time", 1));
//		modTimeVar.setArray(Array.factory(new double[]{modulationTime}));
//		//
//		IVariableFragment scanRateVar = f.addChild("scan_rate");
//		scanRateVar.setDimensions(new Dimension("scan_rate", 1));
//		scanRateVar.setArray(Array.factory(new double[]{scanRate}));

//		IVariableFragment msLevelVar = f.addChild("ms_level");
//		msLevelVar.setDimensions(new Dimension("scan_index", msLevelCache.keys().size()));
//		msLevelVar.setArray(toArray(msLevelCache, DataType.INT));
//		log.info("Storing results.");
//		f.save();
		return new TupleND<IFileFragment>(new FileFragment(f));
	}

	protected Array toArray(ICacheDelegate<Integer, ?> delegate, DataType dataType) {
		Array a = Array.factory(dataType, new int[]{delegate.keys().size()});
		for (int i = 0; i < delegate.keys().size(); i++) {
			Object o = delegate.get(i);
			switch (dataType) {
				case BOOLEAN:
					a.setBoolean(i, (Boolean) o);
					break;
				case BYTE:
					a.setByte(i, (Byte) o);
					break;
				case CHAR:
					a.setChar(i, (Character) o);
					break;
				case DOUBLE:
					a.setDouble(i, (Double) o);
					break;
				case FLOAT:
					a.setFloat(i, (Float) o);
					break;
				case INT:
					a.setInt(i, (Integer) o);
					break;
				case LONG:
					a.setLong(i, (Long) o);
					break;
				case SHORT:
					a.setShort(i, (Short) o);
					break;
				case STRING:
					a.setObject(i, (String) o);
					break;
				default:
					throw new NotImplementedException("Handling for data type " + dataType + " is not implemented!");
			}
		}
		return a;
	}

	protected String getLCP(List<String> strings) {
		if (strings.size() == 1) {
			return strings.get(0);
		}
		String a = strings.get(0);
		String lcp = null;
		for (int i = 1; i < strings.size(); i++) {
			String b = strings.get(i);
			lcp = getLCP(a, b);
			if (lcp.isEmpty()) {
				return "";
			}
		}
		return lcp;
	}

	protected String getLCP(String a, String b) {
		for (int i = 0; i < Math.min(a.length(), b.length()); i++) {
			if (!a.substring(0, i).startsWith(b.substring(0, i))) {
				return a.substring(0, Math.max(0, i - 1));
			}
		}
		return "";
	}

	private class NaturalSortOrderComparator implements Comparator<String> {

		private String str1, str2;
		private int pos1, pos2, len1, len2;

		@Override
		public int compare(String s1, String s2) {
			str1 = s1;
			str2 = s2;
			len1 = str1.length();
			len2 = str2.length();
			pos1 = pos2 = 0;

			int result = 0;
			while (result == 0 && pos1 < len1 && pos2 < len2) {
				char ch1 = str1.charAt(pos1);
				char ch2 = str2.charAt(pos2);

				if (Character.isDigit(ch1)) {
					result = Character.isDigit(ch2) ? compareNumbers() : -1;
				} else if (Character.isLetter(ch1)) {
					result = Character.isLetter(ch2) ? compareOther(true) : 1;
				} else {
					result = Character.isDigit(ch2) ? 1
							: Character.isLetter(ch2) ? -1
							: compareOther(false);
				}

				pos1++;
				pos2++;
			}

			return result == 0 ? len1 - len2 : result;
		}

		private int compareNumbers() {
			int end1 = pos1 + 1;
			while (end1 < len1 && Character.isDigit(str1.charAt(end1))) {
				end1++;
			}
			int fullLen1 = end1 - pos1;
			while (pos1 < end1 && str1.charAt(pos1) == '0') {
				pos1++;
			}

			int end2 = pos2 + 1;
			while (end2 < len2 && Character.isDigit(str2.charAt(end2))) {
				end2++;
			}
			int fullLen2 = end2 - pos2;
			while (pos2 < end2 && str2.charAt(pos2) == '0') {
				pos2++;
			}

			int delta = (end1 - pos1) - (end2 - pos2);
			if (delta != 0) {
				return delta;
			}

			while (pos1 < end1 && pos2 < end2) {
				delta = str1.charAt(pos1++) - str2.charAt(pos2++);
				if (delta != 0) {
					return delta;
				}
			}

			pos1--;
			pos2--;

			return fullLen2 - fullLen1;
		}

		private int compareOther(boolean isLetters) {
			char ch1 = str1.charAt(pos1);
			char ch2 = str2.charAt(pos2);

			if (ch1 == ch2) {
				return 0;
			}

			if (isLetters) {
				ch1 = Character.toUpperCase(ch1);
				ch2 = Character.toUpperCase(ch2);
				if (ch1 != ch2) {
					ch1 = Character.toLowerCase(ch1);
					ch2 = Character.toLowerCase(ch2);
				}
			}

			return ch1 - ch2;
		}
	};

	private class MSChunk {

		private final int maxChunkSize;
		private int offset = 0;
		private int arraySize = 0;
		private final NetcdfFileWriteable nfw;
		private List<Array> massValues = new ArrayList<Array>();
		private List<Array> intensityValues = new ArrayList<Array>();

		public MSChunk(NetcdfFileWriteable nfw, int maxChunkSize) {
			this.nfw = nfw;
			this.maxChunkSize = maxChunkSize;
		}

		public void addMassValues(Array a) {
			massValues.add(a);
			arraySize += a.getShape()[0];
		}

		public void addIntensityValues(Array a) {
			intensityValues.add(a);
		}

		public void checkWriteable() {
			if ((massValues.size() == intensityValues.size()) && (massValues.size() == maxChunkSize)) {
				write();
			}
		}

		public void write() {
			log.info("Writing {} mass value arrays to netcdf file!", arraySize);
			createAndWriteMassValues();
			log.info("Writing {} intensity value arrays to netcdf file!", arraySize);
			createAndWriteIntensityValues();
			offset += arraySize;
			arraySize = 0;
		}

		private void createAndWriteMassValues() {
			int localOffset = 0;
			Array a = Array.factory(massValues.get(0).getElementType(), new int[]{arraySize});
			ListIterator<Array> iter = massValues.listIterator();
			while (iter.hasNext()) {
				Array massArray = iter.next();
				iter.remove();
				Array.arraycopy(massArray, 0, a, localOffset, massArray.getShape()[0]);
				localOffset += massArray.getShape()[0];
			}
			massValues = new ArrayList<Array>();
			try {
				nfw.write("mass_values", new int[]{offset}, a);
				a = null;
			} catch (IOException ex) {
				Logger.getLogger(Data1Dto2DConverter.class.getName()).log(Level.SEVERE, null, ex);
			} catch (InvalidRangeException ex) {
				Logger.getLogger(Data1Dto2DConverter.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		private void createAndWriteIntensityValues() {
			int localOffset = 0;
			Array a = Array.factory(intensityValues.get(0).getElementType(), new int[]{arraySize});
			ListIterator<Array> iter = intensityValues.listIterator();
			while (iter.hasNext()) {
				Array intensityArray = iter.next();
				iter.remove();
				Array.arraycopy(intensityArray, 0, a, localOffset, intensityArray.getShape()[0]);
				localOffset += intensityArray.getShape()[0];
			}
			intensityValues = new ArrayList<Array>();
			try {
				nfw.write("intensity_values", new int[]{offset}, a);
				a = null;
			} catch (IOException ex) {
				Logger.getLogger(Data1Dto2DConverter.class.getName()).log(Level.SEVERE, null, ex);
			} catch (InvalidRangeException ex) {
				Logger.getLogger(Data1Dto2DConverter.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	};
}
