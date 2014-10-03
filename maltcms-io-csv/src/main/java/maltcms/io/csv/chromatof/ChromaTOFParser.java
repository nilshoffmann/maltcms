/*
 * Maui, Maltcms User Interface.
 * Copyright (C) 2008-2012, The authors of Maui. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maui may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maui, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maui is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.io.csv.chromatof;

import cross.datastructures.tuple.Tuple2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.Peak1D;
import maltcms.datastructures.peak.Peak2D;

/**
 * <p>ChromaTOFParser class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
public class ChromaTOFParser {

    /** Constant <code>FIELD_SEPARATOR="\t"</code> */
    public static String FIELD_SEPARATOR = "\t";
    /** Constant <code>QUOTATION_CHARACTER="\""</code> */
    public static String QUOTATION_CHARACTER = "\"";
    /** Constant <code>defaultLocale</code> */
    public static Locale defaultLocale = Locale.getDefault();

    /**
     * <p>getFilenameToGroupMap.</p>
     *
     * @param f a {@link java.io.File} object.
     * @return a {@link java.util.HashMap} object.
     */
    public static HashMap<String, String> getFilenameToGroupMap(File f) {
        List<String> header = null;
        HashMap<String, String> filenameToGroupMap = new LinkedHashMap<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line = "";
            int lineCount = 0;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    String[] lineArray = line.split(String.valueOf(FIELD_SEPARATOR));
                    if (lineCount > 0) {
                        //                        log.info(
                        //                                "Adding file to group mapping: " + lineArray[0] + " " + lineArray[1]);
                        filenameToGroupMap.put(lineArray[0], lineArray[1]);
                    }
                    lineCount++;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ChromaTOFParser.class.getName()).log(Level.SEVERE,
                    null, ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(ChromaTOFParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return filenameToGroupMap;
    }

    /**
     * <p>getIndexOfHeaderColumn.</p>
     *
     * @param header a {@link java.util.List} object.
     * @param columnName a {@link java.lang.String} object.
     * @return a int.
     */
    public static int getIndexOfHeaderColumn(List<String> header,
            String columnName) {
        int idx = 0;
        for (String str : header) {
            if (str.equalsIgnoreCase(columnName)) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

    /**
     * <p>convertMassSpectrum.</p>
     *
     * @param massSpectrum a {@link java.lang.String} object.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public static Tuple2D<double[], int[]> convertMassSpectrum(
            String massSpectrum) {
        if (massSpectrum == null) {
            log.warn("Warning: mass spectral data was null!");
            return new Tuple2D<>(new double[0], new int[0]);
        }
        String[] mziTuples = massSpectrum.split(" ");
        TreeMap<Float, Integer> tm = new TreeMap<>();
        for (String tuple : mziTuples) {
            if (tuple.contains(":")) {
                String[] tplArray = tuple.split(":");
                tm.put(Float.valueOf(tplArray[0]), Integer.valueOf(tplArray[1]));
            } else {
                log.warn(
                        "Warning: encountered strange tuple: " + tuple + " within ms: " + massSpectrum);
            }
        }
        double[] masses = new double[tm.keySet().size()];
        int[] intensities = new int[tm.keySet().size()];
        int i = 0;
        for (Float key : tm.keySet()) {
            masses[i] = key;
            intensities[i] = tm.get(key);
            i++;
        }
        return new Tuple2D<>(masses, intensities);
    }

    /**
     * <p>getHeader.</p>
     *
     * @param f a {@link java.io.File} object.
     * @param normalizeColumnNames a boolean.
     * @return a {@link java.util.LinkedHashSet} object.
     */
    public static LinkedHashSet<String> getHeader(File f, boolean normalizeColumnNames) {
        LinkedHashSet<String> globalHeader = new LinkedHashSet<>();
        ArrayList<String> header = null;
        String fileName = f.getName().substring(0, f.getName().lastIndexOf(
                "."));
        //log.info("Processing report " + fileName);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line = "";
            int lineCount = 0;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    String[] lineArray = splitLine(line, FIELD_SEPARATOR, QUOTATION_CHARACTER);//line.split(String.valueOf(FIELD_SEPARATOR));
                    if (header == null) {
                        if (normalizeColumnNames) {
                            for (int i = 0; i < lineArray.length; i++) {
                                lineArray[i] = lineArray[i].trim().toUpperCase().
                                        replaceAll(" ", "_");
                            }
                        }
                        header = new ArrayList<>(Arrays.asList(
                                lineArray));
                        break;
                    }
                    lineCount++;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ChromaTOFParser.class.getName()).log(
                    Level.SEVERE, null, ex);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ChromaTOFParser.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
        globalHeader.addAll(header);
        return globalHeader;
    }
    /** Constant <code>doubleQuotePattern=""</code> */
    public static final String doubleQuotePattern = "";
    /** Constant <code>msPattern=""</code> */
    public static final String msPattern = "";

    /**
     * <p>splitLine.</p>
     *
     * @param line a {@link java.lang.String} object.
     * @param fieldSeparator a {@link java.lang.String} object.
     * @param quoteSymbol a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     */
    public static String[] splitLine(String line, String fieldSeparator, String quoteSymbol) {
        switch (fieldSeparator) {
            case ",":
                Pattern p = Pattern.compile("((\")([^\"]*)(\"))");
                Matcher m = p.matcher(line);
                List<String> results = new LinkedList<>();
                int match = 1;
                while (m.find()) {
                    results.add(m.group(3).trim());
                }
                Pattern endPattern = Pattern.compile(",([\"]{0,1}([^\"]*)[^\"]{0,1}$)");
                Matcher m2 = endPattern.matcher(line);
                while (m2.find()) {
                    results.add(m2.group(1).trim());
                }
                return results.toArray(new String[results.size()]);
            case "\t":
                return line.replaceAll("\"", "").split("\t");
            default:
                throw new IllegalArgumentException("Field separator " + fieldSeparator + " is not supported, only ',' and '\t' are valid!");
        }
    }

    /**
     * <p>parseBody.</p>
     *
     * @param globalHeader a {@link java.util.LinkedHashSet} object.
     * @param f a {@link java.io.File} object.
     * @param normalizeColumnNames a boolean.
     * @return a {@link java.util.List} object.
     */
    public static List<TableRow> parseBody(LinkedHashSet<String> globalHeader,
            File f, boolean normalizeColumnNames) {
        List<TableRow> body = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));

            String line = "";
            int lineCount = 0;
            List<String> header = null;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    ArrayList<String> lineList = new ArrayList<>(Arrays.asList(splitLine(line, FIELD_SEPARATOR, QUOTATION_CHARACTER)));//.split(String.valueOf(FIELD_SEPARATOR))));
                    if (header == null) {
                        if (normalizeColumnNames) {
                            for (int i = 0; i < lineList.size(); i++) {
                                lineList.set(i, lineList.get(i).trim().toUpperCase().
                                        replaceAll(" ", "_"));
                            }
                        }
                        header = new ArrayList<>(lineList);
                    } else {
                        TableRow tr = new TableRow();
                        for (String headerColumn : globalHeader) {
                            int localIndex = getIndexOfHeaderColumn(header,
                                    headerColumn);
                            if (localIndex >= 0 && localIndex < lineList.size()) {//found column name
                                tr.put(headerColumn, lineList.get(localIndex));
                            } else {//did not find column name
                                tr.put(headerColumn, null);
                            }
                        }
                        body.add(tr);
                    }
                    lineCount++;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ChromaTOFParser.class.getName()).log(Level.SEVERE,
                    null, ex);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ChromaTOFParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return body;
    }

    /**
     * <p>parseReport.</p>
     *
     * @param f a {@link java.io.File} object.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public static Tuple2D<LinkedHashSet<String>, List<TableRow>> parseReport(
            File f) {
        return parseReport(f, true);
    }

    /**
     * <p>parseReport.</p>
     *
     * @param f a {@link java.io.File} object.
     * @param normalizeColumnNames a boolean.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public static Tuple2D<LinkedHashSet<String>, List<TableRow>> parseReport(
            File f, boolean normalizeColumnNames) {
        if (f.getName().toLowerCase().endsWith("csv")) {
//            log.info("CSV Mode");
            ChromaTOFParser.FIELD_SEPARATOR = ",";
            ChromaTOFParser.QUOTATION_CHARACTER = "\"";
        } else if (f.getName().toLowerCase().endsWith("tsv") || f.getName().toLowerCase().endsWith("txt")) {
//            log.info("TSV Mode");
            ChromaTOFParser.FIELD_SEPARATOR = "\t";
            ChromaTOFParser.QUOTATION_CHARACTER = "";
        }
        LinkedHashSet<String> header = getHeader(f, normalizeColumnNames);
        List<TableRow> table = parseBody(header, f, normalizeColumnNames);
        return new Tuple2D<>(header, table);
    }

    /**
     * <p>create1DPeak.</p>
     *
     * @param peakReport a {@link java.io.File} object.
     * @param tr a {@link maltcms.io.csv.chromatof.TableRow} object.
     * @return a {@link maltcms.datastructures.peak.Peak1D} object.
     */
    public static Peak1D create1DPeak(File peakReport, TableRow tr) {
        //log.info("1D chromatogram peak data detected");
        Peak1D p1 = new Peak1D();
        p1.setName(tr.get("NAME"));
        p1.setFile(peakReport.getAbsolutePath());
        p1.setApexTime(parseDouble((tr.get("R.T._(S)"))));
        return p1;
    }

    /**
     * <p>create2DPeak.</p>
     *
     * @param peakReport a {@link java.io.File} object.
     * @param tr a {@link maltcms.io.csv.chromatof.TableRow} object.
     * @param rt1 a double.
     * @param rt2 a double.
     * @return a {@link maltcms.datastructures.peak.Peak2D} object.
     */
    public static Peak2D create2DPeak(File peakReport, TableRow tr, double rt1, double rt2) {
        //log.info("Adding peak "+tr.get("NAME"));
        Peak2D p2 = new Peak2D();
        p2.setName(tr.get("NAME"));
        p2.setFile(peakReport.getAbsolutePath());
        p2.setFirstRetTime(rt1);
        p2.setSecondRetTime(rt2);
        return p2;
    }

    /**
     * <p>parseDoubleArray.</p>
     *
     * @param fieldName a {@link java.lang.String} object.
     * @param row a {@link maltcms.io.csv.chromatof.TableRow} object.
     * @param elementSeparator a {@link java.lang.String} object.
     * @return an array of double.
     */
    public static double[] parseDoubleArray(String fieldName, TableRow row,
            String elementSeparator) {
        if (row.get(fieldName).contains(elementSeparator)) {
            String[] values = row.get(fieldName).split(elementSeparator);
            double[] v = new double[values.length];
            for (int i = 0; i < v.length; i++) {
                v[i] = parseDouble(values[i]);
            }
            return v;
        }
        return new double[]{parseDouble(row.get(fieldName))};
    }

    /**
     * <p>parseDouble.</p>
     *
     * @param fieldName a {@link java.lang.String} object.
     * @param tr a {@link maltcms.io.csv.chromatof.TableRow} object.
     * @return a double.
     */
    public static double parseDouble(String fieldName, TableRow tr) {
//        log.info("Retrieving " + fieldName);
        String value = tr.get(fieldName);
//        log.info("Value: " + value);
        return parseDouble(value);
    }

    /**
     * <p>parseDouble.</p>
     *
     * @param s a {@link java.lang.String} object.
     * @return a double.
     */
    public static double parseDouble(String s) {
        return parseDouble(s, defaultLocale);
    }

    /**
     * <p>parseDouble.</p>
     *
     * @param s a {@link java.lang.String} object.
     * @param locale a {@link java.util.Locale} object.
     * @return a double.
     */
    public static double parseDouble(String s, Locale locale) {
        if (s == null || s.isEmpty()) {
            return Double.NaN;
        }
        try {
            return NumberFormat.getNumberInstance(locale).parse(s).doubleValue();
        } catch (ParseException ex) {
            try {
                return NumberFormat.getNumberInstance(Locale.US).parse(s).
                        doubleValue();
            } catch (ParseException ex1) {
                return Double.NaN;
            }
        }
    }

    /**
     * <p>parseIntegrationStartEnd.</p>
     *
     * @param s a {@link java.lang.String} object.
     * @return a double.
     */
    public static double parseIntegrationStartEnd(String s) {
        if (s == null || s.isEmpty()) {
            return Double.NaN;
        }
        if (s.contains(",")) {
            String[] tokens = s.split(",");
            return parseDouble(tokens[0]);
        }
        return parseDouble(s);
    }
}
