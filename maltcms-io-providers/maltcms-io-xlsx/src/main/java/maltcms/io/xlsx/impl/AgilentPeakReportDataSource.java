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
package maltcms.io.xlsx.impl;

import cross.Factory;
import cross.cache.CacheFactory;
import cross.cache.ICacheDelegate;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.tools.MathTools;
import cross.tools.StringTools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.WeakHashMap;

import maltcms.io.xlsx.IXLSDataSource;
import maltcms.io.xlsx.bridge.ICell;
import maltcms.io.xlsx.bridge.IInputStreamProvider;
import maltcms.io.xlsx.bridge.IRow;
import maltcms.io.xlsx.bridge.ISheet;
import maltcms.io.xlsx.bridge.IWorkbook;
import maltcms.io.xlsx.bridge.WorkbookBridge;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.OldExcelFormatException;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import static ucar.ma2.DataType.DOUBLE;
import static ucar.ma2.DataType.FLOAT;
import static ucar.ma2.DataType.INT;
import static ucar.ma2.DataType.LONG;
import static ucar.ma2.DataType.STRING;
import ucar.ma2.Range;
import ucar.nc2.Dimension;

/**
 * <p>AgilentPeakReportDataSource class.</p>
 *
 * @author Nils Hoffmann
 * 
 */

@ServiceProvider(service = IXLSDataSource.class)
public final class AgilentPeakReportDataSource implements IXLSDataSource {
        
    private static final Logger log = LoggerFactory.getLogger(AgilentPeakReportDataSource.class);
    
    private final String[] reportFileEnding = new String[]{"xls", "xlsx"};
    private final String[] fileEnding = new String[]{"d"};
    private HashMap<String, Mapping> varnameToMapping = new HashMap<>();
    private static final WeakHashMap<IFileFragment, IWorkbook> fileToWorkbook = new WeakHashMap<>();
    private static final ICacheDelegate<String, Array> variableToArrayCache = CacheFactory.createVolatileCache("maltcms.io.readcache");
    private static final HashMap<URI, WorkbookBridge.IMPL> uriToImpl = new HashMap<>();

    /**
     * <p>Constructor for AgilentPeakReportDataSource.</p>
     */
    public AgilentPeakReportDataSource() {
        log.info("Initializing AgilentPeakReportDataSource");
        varnameToMapping.put("scan_acquisition_time", new Mapping("IntResults1", "RetTime", DataType.DOUBLE));
        varnameToMapping.put("total_intensity", new Mapping("IntResults1", "Area", DataType.DOUBLE));
        varnameToMapping.put("peak_area", new Mapping("IntResults1", "Area", DataType.DOUBLE));
        varnameToMapping.put("intensity_values", new Mapping("IntResults1", "Area", DataType.DOUBLE));

    }

    private int getNumberOfPeaks(IWorkbook workbook, Mapping mapping) {
        ISheet s = workbook.getSheet(mapping.sheetName());
        IRow header = null;
        int nrows = Integer.parseInt(s.getRow(1).getCell(1).stringValue());
        return nrows;
    }

    private Array getSheetData(IWorkbook workbook, Mapping mapping) {
        LinkedHashMap<String, List<Object>> data = new LinkedHashMap<>();
        ISheet s = workbook.getSheet(mapping.sheetName());
        IRow header = null;
        int nrows = getNumberOfPeaks(workbook, mapping);
//		int ncols = Integer.parseInt(s.getRow(2).getCell(2).getStringCellValue());
        Array a = Array.factory(mapping.dataType(), new int[]{nrows});
        int colIdx = -1;
        int rowIdx = 0;
        for (IRow row : s) {
            if (header == null) {
                header = s.getRow(0);
                int cnt = 0;
                for (ICell c : header) {
                    if (c.stringValue().equals(mapping.columnName())) {
                        colIdx = cnt;
                        log.debug("Found {} at index {}!", mapping.columnName(), colIdx);
                        break;
                    }
                    cnt++;
                }
            } else {
                if (colIdx > -1) {
                    ICell c = row.getCell(colIdx);
                    switch (mapping.dataType()) {
                        case DOUBLE:
                            a.setDouble(rowIdx, c.doubleValue());
                            break;
                        case FLOAT:
                            a.setFloat(rowIdx, (float) c.doubleValue());
                            break;
                        case INT:
                            a.setInt(rowIdx, (int) c.doubleValue());
                            break;
                        case LONG:
                            a.setLong(rowIdx, (long) c.doubleValue());
                            break;
                        case STRING:
                            a.setObject(rowIdx, c.stringValue());
                            break;
                        default:
                            throw new ConstraintViolationException("Unmatched case: " + mapping.dataType());

                    }
                    rowIdx++;
                }
            }
        }
        return a;
    }

    private class AgilentDReportInputStreamProvider implements IInputStreamProvider {

        private final URI sourceDirectory;
        private FileInputStream fileInputStream;

        public AgilentDReportInputStreamProvider(URI sourceDirectory) {
            this.sourceDirectory = sourceDirectory;
        }

        @Override
        public InputStream openStream() {
            File f = new File(sourceDirectory);
            if (f.isDirectory() && f.exists()) {
                Collection<File> c = FileUtils.listFiles(f, reportFileEnding, false);
//				if(c.size()>1) {
//					throw new ConstraintViolationException("Found more than one report file in directory: "+f);
//				}
                if (!c.isEmpty()) {
                    for (File file : c) {
                        if (file.getName().toLowerCase().startsWith("report01")) {
                            log.info("Using report {}", file);
                            try {
                                fileInputStream = new FileInputStream(file);
                                return fileInputStream;
                            } catch (FileNotFoundException ex) {
                                throw new ConstraintViolationException(ex);
                            }
                        }
                    }
                }
                throw new ConstraintViolationException("Could not find any valid report files below: " + f);
            }
            throw new ConstraintViolationException("Could not find location: " + f);
        }

        @Override
        public void closeStream() {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ex) {
                    log.warn("Caught exception while closing stream to " + sourceDirectory);
                }
            }
        }

    }

    private IWorkbook open(IFileFragment fragment) {
        URI uri = fragment.getUri();
        if (fileToWorkbook.containsKey(fragment)) {
            return fileToWorkbook.get(fragment);
        }
        IWorkbook workbook = null;
        WorkbookBridge wb = new WorkbookBridge();
        if (uriToImpl.containsKey(uri)) {
            workbook = wb.getWorkbook(uri, uriToImpl.get(uri), new AgilentDReportInputStreamProvider(uri));
        } else {
            for (WorkbookBridge.IMPL impl : WorkbookBridge.IMPL.values()) {
                try {
                    workbook = wb.getWorkbook(uri, impl, new AgilentDReportInputStreamProvider(uri));
                    if (workbook != null) {
                        uriToImpl.put(uri, impl);
                        break;
                    }
                } catch (OldExcelFormatException ofe) {
                    log.info("Workbook Implementation {} does not support old Excel format!", impl);
                } catch (Exception e) {
                    log.warn("Caught exception while testing implementation " + impl);
                    log.warn("Details:", e);
                }
            }
        }
        if (workbook != null) {
            fileToWorkbook.put(fragment, workbook);
            return workbook;
        }
        throw new RuntimeException("Failed to load workbook for " + uri);
    }

    /** {@inheritDoc} */
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
//				try {
//					IWorkbook w = open(ff.getUri());
//					ISheet peak = w.getSheet("Peak");
//					ISheet intRes = w.getSheet("IntResults1");
//					if (peak != null && intRes != null) {
//						log.info("Found a valid agilent peak report file!");
//						return 1;
//					}
//				} catch (RuntimeException re) {
//					log.warn("Could not open excel file:", re);
//				}
                return 1;
            }
        }
        return 0;
    }

    public record Mapping(String sheetName, String columnName, DataType dataType) {}

    /**
     * <p>dimensions.</p>
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param ivf a {@link cross.datastructures.fragments.IVariableFragment} object.
     * @return an array of {@link ucar.nc2.Dimension} objects.
     */
    public Dimension[] dimensions(Array a, IVariableFragment ivf) {
        int[] shape = a.getShape();
        switch (ivf.getName()) {
            case "scan_acquisition_time":
            case "total_intensity":
            case "scan_index":
            case "mass_range_min":
            case "mass_range_max":
                return new Dimension[]{new Dimension("scan_number", shape[0])};
            case "mass_values":
            case "intensity_values":
                return new Dimension[]{new Dimension("point_number", shape[0])};
        }
        return new Dimension[]{};
    }

    /**
     * <p>ranges.</p>
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param ivf a {@link cross.datastructures.fragments.IVariableFragment} object.
     * @return an array of {@link ucar.ma2.Range} objects.
     */
    public Range[] ranges(Array a, IVariableFragment ivf) {
        int[] shape = a.getShape();
        switch (ivf.getName()) {
            case "scan_acquisition_time":
            case "total_intensity":
            case "scan_index":
            case "mass_range_min":
            case "mass_range_max":
                return new Range[]{new Range(shape[0])};
            case "mass_values":
            case "intensity_values":
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

    /** {@inheritDoc} */
    @Override
    public ArrayList<Array> readAll(IFileFragment f) throws IOException, ResourceNotAvailableException {
        IWorkbook w = open(f);
        ArrayList<Array> l = new ArrayList<>();
        addIfNew("scan_acquisition_time", f);
        addIfNew("total_intensity", f);
        addIfNew("scan_index", f);
        addIfNew("mass_values", f);
        addIfNew("intensity_values", f);
        addIfNew("mass_range_min", f);
        addIfNew("mass_range_max", f);
        addIfNew("peak_area", f);
        int scanNumber = getNumberOfPeaks(w, getMapping(f.getChild("scan_acquisition_time")));
        for (IVariableFragment ivf : f) {
            l.add(createArray(ivf, f, w));
        }
        return l;
    }

    private Array createArray(IVariableFragment ivf, IFileFragment f, IWorkbook w) {
        Array a = variableToArrayCache.get(f.getUri() + ">" + ivf.getName());
        if (a != null) {
            log.info("Retrieved variable data array from cache for " + ivf);
            log.info("Dimensions: {}", new Object[]{ivf.getDimensions()});
            if (ivf.getDimensions() == null) {
                ivf.setDimensions(dimensions(a, ivf));
            }
            if (ivf.getRange() == null) {
                ivf.setRange(ranges(a, ivf));
            }
            return a;
        }
        switch (ivf.getName()) {
            case "scan_index":
                a = createScanIndex(f, w);
                break;
            case "mass_values":
                a = createMassValues(f, w);
                break;
            case "mass_range_min":
                a = createMassRangeMin(f, w);
                break;
            case "mass_range_max":
                a = createMassRangeMax(f, w);
                break;
            default:
                a = getSheetData(w, getMapping(ivf));
                break;
        }
        if (a != null) {
            ivf.setDimensions(dimensions(a, ivf));
            ivf.setRange(ranges(a, ivf));
            variableToArrayCache.put(f.getUri() + ">" + ivf.getName(), a);
        }
        return a;
    }

    private Array createScanIndex(IFileFragment f, IWorkbook w) {
        addIfNew("scan_acquisition_time", f);
        int scanNumber = getNumberOfPeaks(w, getMapping(f.getChild("scan_acquisition_time")));
        Array a = Array.makeFromJavaArray(MathTools.seq(0, scanNumber - 1, 1));
        EvalTools.eqI(scanNumber, a.getShape()[0], this);
        return a;
    }

    private Array createMassValues(IFileFragment f, IWorkbook w) {
        addIfNew("mass_values", f);
        int scanNumber = getNumberOfPeaks(w, getMapping(f.getChild("scan_acquisition_time")));
        double[] mv = new double[scanNumber];
        Arrays.fill(mv, 0);
        Array a = Array.makeFromJavaArray(mv);
        EvalTools.eqI(scanNumber, a.getShape()[0], this);
        return a;
    }

    private Array createMassRangeMin(IFileFragment f, IWorkbook w) {
        int scanNumber = getNumberOfPeaks(w, getMapping(f.getChild("scan_acquisition_time")));
        final Array mass_range_min = new ArrayDouble.D1(scanNumber);
        return mass_range_min;
    }

    private Array createMassRangeMax(IFileFragment f, IWorkbook w) {
        int scanNumber = getNumberOfPeaks(w, getMapping(f.getChild("scan_acquisition_time")));
        double[] mass_range_max = new double[scanNumber];
        Arrays.fill(mass_range_max, 1.0d);
        return Array.makeFromJavaArray(mass_range_max);
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<Array> readIndexed(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        switch (f.getName()) {
            case "mass_values": {
                Array a = readSingle(f);
                ArrayList<Array> al = new ArrayList<>();
                for (int i = 0; i < a.getShape()[0]; i++) {
                    Array arr = Array.factory(a.getDataType(), new int[]{1});
                    arr.setDouble(0, a.getDouble(i));
                    al.add(arr);
                }
                return al;
            }
            case "intensity_values": {
                Array a = readSingle(f);
                ArrayList<Array> al = new ArrayList<>();
                for (int i = 0; i < a.getShape()[0]; i++) {
                    Array arr = Array.factory(a.getDataType(), new int[]{1});
                    arr.setDouble(0, a.getDouble(i));
                    al.add(arr);
                }
                return al;
            }
        }
        throw new ResourceNotAvailableException(
                "Unknown varname to xls/xlsx mapping for varname " + f.getName());
    }

    /** {@inheritDoc} */
    @Override
    public Array readSingle(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        IWorkbook w = open(f.getParent());
        return createArray(f, f.getParent(), w);
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<IVariableFragment> readStructure(IFileFragment f) throws IOException {
        readAll(f);
        return new ArrayList<>(f.getImmediateChildren());
    }

    /** {@inheritDoc} */
    @Override
    public IVariableFragment readStructure(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        readSingle(f);
        return f;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> supportedFormats() {
        return Arrays.asList(this.fileEnding);
    }

    /** {@inheritDoc} */
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
        IWorkbook workbook = fileToWorkbook.get(f);
        return Factory.getInstance().getDataSourceFactory().getDataSourceFor(f).write(f);
    }

    /** {@inheritDoc} */
    @Override
    public void configure(Configuration cfg) {
    }

    /** {@inheritDoc} */
    @Override
    public void configurationChanged(ConfigurationEvent ce) {
    }
}
