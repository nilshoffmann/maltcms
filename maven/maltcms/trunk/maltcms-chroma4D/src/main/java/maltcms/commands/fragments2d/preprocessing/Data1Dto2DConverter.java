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
import cross.datastructures.cache.SerializableArrayProxy;
import cross.datastructures.collections.CachedReadWriteList;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.NotImplementedException;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;

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
	@Configurable
	private List<Integer> msLevel = Arrays.asList(Integer.valueOf(1));

	@Override
	public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
		IFileFragment f = null;
		Collections.sort(msLevel);
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
		f = new FileFragment(new File(outputDirectory, outputFileName + ".cdf"));
		log.info("Writing output to {}", f.getName());
		double modulationStartTime = 0;
		double modulationEndTime = 0;
		double modulationTime = 0;
		double scanRate = 0;
		double firstColumnElutionTime = 0;
		double secondColumnElutionTime = 0;
		List<Array> massValuesCache = new CachedReadWriteList<Array>(UUID.nameUUIDFromBytes((f.getUri() + ">mass_values").getBytes()).toString(), new SerializableArrayProxy(), 5000);
		List<Array> intensityValuesCache = new CachedReadWriteList<Array>(UUID.nameUUIDFromBytes((f.getUri() + ">intensity_values").getBytes()).toString(), new SerializableArrayProxy(), 5000);
		ICacheDelegate<Integer, Float> satCache = cross.cache.CacheFactory.createDefaultCache(UUID.nameUUIDFromBytes((f.getUri() + "-satValues").getBytes()).toString(), 5000);
		ICacheDelegate<Integer, Float> fcetCache = cross.cache.CacheFactory.createDefaultCache(UUID.nameUUIDFromBytes((f.getUri() + "-firstColumnElutionTimeValues").getBytes()).toString(), 5000);
		ICacheDelegate<Integer, Float> scetCache = cross.cache.CacheFactory.createDefaultCache(UUID.nameUUIDFromBytes((f.getUri() + "-secondColumnElutionTimeValues").getBytes()).toString(), 5000);
		ICacheDelegate<Integer, Integer> ticCache = cross.cache.CacheFactory.createDefaultCache(UUID.nameUUIDFromBytes((f.getUri() + "-ticValues").getBytes()).toString(), 5000);
		ICacheDelegate<Integer, Integer> msLevelCache = cross.cache.CacheFactory.createDefaultCache(UUID.nameUUIDFromBytes((f.getUri() + "-msLevelValues").getBytes()).toString(), 5000);
		ICacheDelegate<Integer, Integer> scanIndexCache = cross.cache.CacheFactory.createDefaultCache(UUID.nameUUIDFromBytes((f.getUri() + "-scanIndexValues").getBytes()).toString(), 5000);
		int scanIndex = 0;
		ArrayList<String> inSorted = new ArrayList<String>();
		Map<String, IFileFragment> nameToFragment = new HashMap<String, IFileFragment>();
		for (IFileFragment ff : in) {
			inSorted.add(ff.getName());
			nameToFragment.put(ff.getName(), ff);
		}
		Collections.sort(inSorted, new NaturalSortOrderComparator());
		int scanIndexOffset = 0;
		for (String key : inSorted) {
			IFileFragment ff = nameToFragment.remove(key);
			log.info("Processing file {}", ff.getName());
			IVariableFragment scanIndexVar = ff.getChild("scan_index");
			IVariableFragment massValuesVar = ff.getChild("mass_values");
			massValuesVar.setIndex(scanIndexVar);
			IVariableFragment intensityValuesVar = ff.getChild("intensity_values");
			intensityValuesVar.setIndex(scanIndexVar);
			IVariableFragment satVar = ff.getChild("scan_acquisition_time");
			Array sat = satVar.getArray();
			IVariableFragment ticVar = ff.getChild("total_intensity");
			Array tic = ticVar.getArray();
			IVariableFragment msLevelVar = null;
			Array msLevelArray = null;
			try {
				msLevelVar = ff.getChild("ms_level");
				msLevelArray = msLevelVar.getArray();
			} catch (ResourceNotAvailableException rnae) {
				log.warn("ms_level not present, only ms level one spectra will be returned!");
			}
			List<Array> massValues = massValuesVar.getIndexedArray();
			List<Array> intensityValues = intensityValuesVar.getIndexedArray();
			int scans = massValues.size();
			double previousMs1Sat = 0.0d;
			for (int i = 0; i < scans; i++) {
				int scanMsLevel = -1;
				if (msLevelArray != null) {
					scanMsLevel = msLevelArray.getInt(i);
				}else{
					scanMsLevel = 1;
				}
				int atIndex = Collections.binarySearch(msLevel, Integer.valueOf(scanMsLevel));
				if (atIndex > -1) {
					//found in atIndex
					if (i == 0) {
						modulationStartTime = modulationEndTime + sat.getDouble(i);
						firstColumnElutionTime = modulationStartTime;
					} else if (i == scans - 1) {
						modulationEndTime = modulationStartTime + sat.getDouble(i);
					}
					if (scanMsLevel > -1) {
						msLevelCache.put(scanIndex, scanMsLevel);
					}
					scanIndexCache.put(scanIndex, scanIndexOffset);
					secondColumnElutionTime = sat.getDouble(i) - modulationStartTime;
					massValuesCache.add(scanIndex, massValues.get(i));
					intensityValuesCache.add(scanIndex, intensityValues.get(i));
					double localScanTime = modulationStartTime + secondColumnElutionTime;
					satCache.put(scanIndex, (float) localScanTime);
					fcetCache.put(scanIndex, (float) modulationStartTime);
					scetCache.put(scanIndex, (float) secondColumnElutionTime);
					ticCache.put(scanIndex, tic.getInt(scanIndex));
					scanIndexOffset += massValues.get(i).getShape()[0];
					if (scanMsLevel == 1) {
						log.info("Scan time delta to previous scan: {}", secondColumnElutionTime - previousMs1Sat);
						if(i>0) {
							scanRate = Math.min(scanRate, 1.0d / (sat.getDouble(i) - sat.getDouble(i - 1)));
						}
						previousMs1Sat = secondColumnElutionTime;
					}
					modulationTime = Math.max(modulationTime, (modulationEndTime - modulationStartTime));
					scanIndex++;
				} else {
					log.info("Skipping scan with ms level {}. Not in whitelist!", scanMsLevel);
				}

			}
			ff.clearArrays();
		}
		log.info("Adding variable data.");
		//
		IVariableFragment scanIndexVar = f.addChild("scan_index");
		scanIndexVar.setArray(toArray(scanIndexCache, DataType.INT));
		//
		IVariableFragment massValuesVar = f.addChild("mass_values");
		massValuesVar.setIndex(scanIndexVar);
		massValuesVar.setIndexedArray(massValuesCache);
		massValuesVar.setDataType(DataType.FLOAT);
		//
		IVariableFragment intensityValuesVar = f.addChild("intensity_values");
		intensityValuesVar.setIndex(scanIndexVar);
		intensityValuesVar.setIndexedArray(intensityValuesCache);
		intensityValuesVar.setDataType(DataType.INT);
		//
		IVariableFragment satVar = f.addChild("scan_acquisition_time");
		satVar.setArray(toArray(satCache, DataType.FLOAT));
		//
		IVariableFragment ticVar = f.addChild("total_intensity");
		ticVar.setArray(toArray(ticCache, DataType.INT));
		//
		IVariableFragment fcetVar = f.addChild("first_column_elution_time");
		fcetVar.setArray(toArray(fcetCache, DataType.FLOAT));
		//
		IVariableFragment scetVar = f.addChild("second_column_elution_time");
		scetVar.setArray(toArray(scetCache, DataType.FLOAT));
		//
		IVariableFragment modTimeVar = f.addChild("modulation_time");
		modTimeVar.setDimensions(new Dimension("modulation_time", 1));
		modTimeVar.setArray(Array.factory(new double[]{modulationTime}));
		//
		IVariableFragment scanRateVar = f.addChild("scan_rate");
		scanRateVar.setDimensions(new Dimension("scan_rate", 1));
		scanRateVar.setArray(Array.factory(new double[]{scanRate}));

		IVariableFragment msLevelVar = f.addChild("ms_level");
		msLevelVar.setDimensions(new Dimension("scan_index", 1));
		msLevelVar.setArray(toArray(msLevelCache, DataType.INT));
		log.info("Storing results.");
		f.save();
		return new TupleND<IFileFragment>(f);
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
}
