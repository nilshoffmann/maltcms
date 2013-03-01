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
package maltcms.io.xlsx.impl;

import cross.Factory;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.tools.MathTools;
import cross.tools.StringTools;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.io.xlsx.IXLSDataSource;
import maltcms.io.xlsx.bridge.ICell;
import maltcms.io.xlsx.bridge.IRow;
import maltcms.io.xlsx.bridge.ISheet;
import maltcms.io.xlsx.bridge.IWorkbook;
import maltcms.io.xlsx.bridge.WorkbookBridge;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import static ucar.ma2.DataType.DOUBLE;
import ucar.ma2.Range;
import ucar.nc2.Dimension;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@ServiceProvider(service=IXLSDataSource.class)
public final class AgilentPeakReportDataSource implements IXLSDataSource {

	private final String[] fileEnding = new String[]{"xls", "xlsx"};
	private HashMap<String, Mapping> varnameToMapping = new HashMap<String, Mapping>();
	private static HashMap<URI,WorkbookBridge.IMPL> uriToImpl = new HashMap<URI,WorkbookBridge.IMPL>();

//	private final String[] variables = {
//		"peak_retention_time",
//		"peak_name",
//		"peak_amount",
//		"peak_start_time",
//		"peak_end_time",
//		"peak_width",
//		"peak_area",
//		"peak_area_percent",
//		"peak_height",
//		"peak_height_percent",
//		"peak_start_detection_code",
//		"peak_stop_detection_code",
//		"baseline_start_time",
//		"baseline_start_value",
//		"baseline_stop_time",
//		"baseline_stop_value",
//		"retention_index",
//		"peak_asymmetry",
//		"peak_efficiency"
//	};
//	private final String[] dimensions = {
//		"peak_number",
//		"scan_number"
//	};
	public AgilentPeakReportDataSource() {
		log.info("Initializing AgilentPeakReportDataSource");
		varnameToMapping.put("scan_acquisition_time", new Mapping("IntResults1", "RetTime", DataType.DOUBLE));
		varnameToMapping.put("total_intensity", new Mapping("IntResults1", "Area", DataType.DOUBLE));
		varnameToMapping.put("intensity_values", new Mapping("IntResults1", "Area", DataType.DOUBLE));
	}

	private int getNumberOfPeaks(IWorkbook workbook, Mapping mapping) {
		ISheet s = workbook.getSheet(mapping.getSheetName());
		IRow header = null;
		int nrows = Integer.parseInt(s.getRow(1).getCell(1).stringValue());
		return nrows;
	}

	private Array getSheetData(IWorkbook workbook, Mapping mapping) {
		LinkedHashMap<String, List<Object>> data = new LinkedHashMap<String, List<Object>>();
		ISheet s = workbook.getSheet(mapping.getSheetName());
		IRow header = null;
		int nrows = getNumberOfPeaks(workbook, mapping);
//		int ncols = Integer.parseInt(s.getRow(2).getCell(2).getStringCellValue());
		Array a = Array.factory(mapping.getDataType(), new int[]{nrows});
		int colIdx = -1;
		int rowIdx = 0;
		for (IRow row : s) {
			if (header == null) {
				header = s.getRow(0);
				int cnt = 0;
				for (ICell c : header) {
					if (c.stringValue().equals(mapping.getColumnName())) {
						colIdx = cnt;
						System.out.println("Found " + mapping.getColumnName() + " at index " + colIdx + "!");
						break;
					}
					cnt++;
				}
			} else {
				if (colIdx > -1) {
					ICell c = row.getCell(colIdx);
					switch (mapping.getDataType()) {
						case DOUBLE:
							a.setDouble(rowIdx, c.doubleValue());
							break;
						case FLOAT:
							a.setFloat(rowIdx, (float)c.doubleValue());
							break;
						case INT:
							a.setInt(rowIdx, (int)c.doubleValue());
							break;
						case LONG:
							a.setLong(rowIdx, (long)c.doubleValue());
							break;
						case STRING:
							a.setObject(rowIdx, c.stringValue());
							break;
						default:
							throw new ConstraintViolationException("Unmatched case: " + mapping.getDataType());

					}
					rowIdx++;
				}
			}
		}
		System.out.println("Returning " + a);
		return a;
	}

	private IWorkbook open(URI uri) {
		WorkbookBridge wb = new WorkbookBridge();
		if(uriToImpl.containsKey(uri)) {
			return wb.getWorkbook(uri, uriToImpl.get(uri));
		}
		for(WorkbookBridge.IMPL impl: WorkbookBridge.IMPL.values()) {
			try{
				IWorkbook workbook = wb.getWorkbook(uri, impl);
				if(workbook!=null) {
					uriToImpl.put(uri, impl);
					return workbook;
				}
			}catch(Exception e) {
				log.warn("Caught exception while testing implementation "+impl);
			}
		}
		throw new RuntimeException("Failed to load workbook for "+uri);
	}

	@Override
	public int canRead(IFileFragment ff) {
		final int dotindex = ff.getName().lastIndexOf(".");
		final String filename = ff.getName().toLowerCase();
		if (dotindex == -1) {
			throw new RuntimeException("Could not determine File extension of "
					+ ff);
		}
		for (final String s : this.fileEnding) {
			if (filename.endsWith(s)) {
				try {
					IWorkbook w = open(ff.getUri());
					ISheet peak = w.getSheet("Peak");
					ISheet intRes = w.getSheet("IntResults1");
					if (peak != null && intRes != null) {
						log.info("Found a valid agilent peak report file!");
						return 1;
					}
				} catch (RuntimeException re) {
					log.warn("Could not open excel file:", re);
				}
			}
		}
		return 0;
	}

	@Data
	public class Mapping {

		private final String sheetName;
		private final String columnName;
		private final DataType dataType;
	}

	public Dimension[] dimensions(Array a, IVariableFragment ivf) {
		int[] shape = a.getShape();
		if (ivf.getName().equals("scan_acquisition_time") || ivf.getName().equals("total_intensity") || ivf.getName().equals("scan_index")) {
			return new Dimension[]{new Dimension("scan_number", shape[0])};
		} else if (ivf.getName().equals("mass_values") || ivf.getName().equals("intensity_values")) {
			return new Dimension[]{new Dimension("point_number", shape[0])};
		}
		return new Dimension[]{};
	}
	
	public Range[] ranges(Array a, IVariableFragment ivf) {
		int[] shape = a.getShape();
		if (ivf.getName().equals("scan_acquisition_time") || ivf.getName().equals("total_intensity") || ivf.getName().equals("scan_index")) {
			return new Range[]{new Range(shape[0])};
		} else if (ivf.getName().equals("mass_values") || ivf.getName().equals("intensity_values")) {
			return new Range[]{new Range(shape[0])};
		}
		return new Range[]{};
	}

	private Mapping getMapping(IVariableFragment ivf) {
		Mapping m = varnameToMapping.get(ivf.getName());
		if (m == null) {
			throw new ResourceNotAvailableException("No mapping available for variable " + ivf.getName());
		}
		return m;
	}

	private void addIfNew(String variableName, IFileFragment f) {
		if (!f.hasChild(variableName)) {
			f.addChild(variableName);
		}
	}

	@Override
	public ArrayList<Array> readAll(IFileFragment f) throws IOException, ResourceNotAvailableException {
		IWorkbook w = open(f.getUri());
		ArrayList<Array> l = new ArrayList<Array>();
		addIfNew("scan_acquisition_time", f);
		addIfNew("total_intensity", f);
		addIfNew("scan_index", f);
		addIfNew("mass_values", f);
		addIfNew("intensity_values", f);
		int scanNumber = getNumberOfPeaks(w, getMapping(f.getChild("scan_acquisition_time")));
		for (IVariableFragment ivf : f) {
			l.add(createArray(ivf, f, w));
		}
		return l;
	}
	
	private Array createArray(IVariableFragment ivf, IFileFragment f, IWorkbook w) {
		Array a;
		if (ivf.getName().equals("scan_index")) {
			a = createScanIndex(f, w);
		} else if (ivf.getName().equals("mass_values")) {
			a = createMassValues(f, w);
		} else {
			a = getSheetData(w, getMapping(ivf));
		}
		if(a!=null) {
			ivf.setDimensions(dimensions(a, ivf));
			ivf.setRange(ranges(a, ivf));
		}
		return a;
	}
	
	private Array createScanIndex(IFileFragment f, IWorkbook w) {
		addIfNew("scan_acquisition_time", f);
		int scanNumber = getNumberOfPeaks(w, getMapping(f.getChild("scan_acquisition_time")));
		Array a = Array.factory(MathTools.seq(0, scanNumber - 1, 1));
		EvalTools.eqI(scanNumber, a.getShape()[0], this);
		return a;
	}
	
	private Array createMassValues(IFileFragment f, IWorkbook w) {
		addIfNew("mass_values", f);
		int scanNumber = getNumberOfPeaks(w, getMapping(f.getChild("scan_acquisition_time")));
		double[] mv = new double[scanNumber];
		Arrays.fill(mv, -1);
		Array a = Array.factory(mv);
		EvalTools.eqI(scanNumber, a.getShape()[0], this);
		return a;
	}

	@Override
	public ArrayList<Array> readIndexed(IVariableFragment f) throws IOException, ResourceNotAvailableException {
		if (f.getName().equals("mass_values")) {
			Array a = readSingle(f);
			ArrayList<Array> al = new ArrayList<Array>();
			for (int i = 0; i < a.getShape()[0]; i++) {
				Array arr = Array.factory(DataType.getType(a.getElementType()), new int[]{1});
				arr.setDouble(0, a.getDouble(i));
				al.add(arr);
			}
			return al;
		} else if (f.getName().equals("intensity_values")) {
			Array a = readSingle(f);
			ArrayList<Array> al = new ArrayList<Array>();
			for (int i = 0; i < a.getShape()[0]; i++) {
				Array arr = Array.factory(DataType.getType(a.getElementType()), new int[]{1});
				arr.setDouble(0, a.getDouble(i));
				al.add(arr);
			}
			return al;
		}
		throw new ResourceNotAvailableException(
				"Unknown varname to xls/xlsx mapping for varname " + f.getName());
	}

	@Override
	public Array readSingle(IVariableFragment f) throws IOException, ResourceNotAvailableException {
		IWorkbook w = open(f.getParent().getUri());
		return createArray(f, f.getParent(), w);
	}

	@Override
	public ArrayList<IVariableFragment> readStructure(IFileFragment f) throws IOException {
		readAll(f);
		return new ArrayList<IVariableFragment>(f.getImmediateChildren());
	}

	@Override
	public IVariableFragment readStructure(IVariableFragment f) throws IOException, ResourceNotAvailableException {
		readSingle(f);
		return f;
	}

	@Override
	public List<String> supportedFormats() {
		return Arrays.asList(this.fileEnding);
	}

	@Override
	public boolean write(IFileFragment f) {
		// TODO Implement real write support
		log.info("Saving {} with AgilentPeakReportDataSource", f.getUri());
		log.info("Changing output file from: {}", f.toString());
		File file = new File(f.getUri());
		String filename = StringTools.removeFileExt(file.getAbsolutePath());
		filename += ".cdf";
		f.setFile(filename);
		f.addSourceFile(new FileFragment(f.getUri()));
		log.info("To: {}", filename);
		return Factory.getInstance().getDataSourceFactory().getDataSourceFor(f).write(f);
	}

	@Override
	public void configure(Configuration cfg) {
	}

	@Override
	public void configurationChanged(ConfigurationEvent ce) {
	}
}