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
import maltcms.datastructures.peak.Peak1D;
import maltcms.datastructures.peak.Peak2D;

/**
 *
 * @author Nils Hoffmann
 */
public class ChromaTOFParser {

    public static String FIELD_SEPARATOR = "\t";
    public static String QUOTATION_CHARACTER = "\"";
    public static Locale defaultLocale = Locale.getDefault();

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
                        //                        System.out.println(
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

    public static Tuple2D<double[], int[]> convertMassSpectrum(
            String massSpectrum) {
        if (massSpectrum == null) {
            System.err.println("Warning: mass spectral data was null!");
            return new Tuple2D<>(new double[0], new int[0]);
        }
        String[] mziTuples = massSpectrum.split(" ");
        TreeMap<Float, Integer> tm = new TreeMap<>();
        for (String tuple : mziTuples) {
            if (tuple.contains(":")) {
                String[] tplArray = tuple.split(":");
                tm.put(Float.valueOf(tplArray[0]), Integer.valueOf(tplArray[1]));
            } else {
                System.err.println(
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

    public static LinkedHashSet<String> getHeader(File f, boolean normalizeColumnNames) {
        LinkedHashSet<String> globalHeader = new LinkedHashSet<>();
        ArrayList<String> header = null;
        String fileName = f.getName().substring(0, f.getName().lastIndexOf(
                "."));
        //System.out.println("Processing report " + fileName);
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
    public static final String doubleQuotePattern = "";
    public static final String msPattern = "";

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

    public static Tuple2D<LinkedHashSet<String>, List<TableRow>> parseReport(
            File f) {
        return parseReport(f, true);
    }

    public static Tuple2D<LinkedHashSet<String>, List<TableRow>> parseReport(
            File f, boolean normalizeColumnNames) {
        if (f.getName().toLowerCase().endsWith("csv")) {
//            System.out.println("CSV Mode");
            ChromaTOFParser.FIELD_SEPARATOR = ",";
            ChromaTOFParser.QUOTATION_CHARACTER = "\"";
        } else if (f.getName().toLowerCase().endsWith("tsv") || f.getName().toLowerCase().endsWith("txt")) {
//            System.out.println("TSV Mode");
            ChromaTOFParser.FIELD_SEPARATOR = "\t";
            ChromaTOFParser.QUOTATION_CHARACTER = "";
        }
        LinkedHashSet<String> header = getHeader(f, normalizeColumnNames);
        List<TableRow> table = parseBody(header, f, normalizeColumnNames);
        return new Tuple2D<>(header, table);
    }

    public static Peak1D create1DPeak(File peakReport, TableRow tr) {
        //System.out.println("1D chromatogram peak data detected");
        Peak1D p1 = new Peak1D();
        p1.setName(tr.get("NAME"));
        p1.setFile(peakReport.getAbsolutePath());
        p1.setApexTime(parseDouble((tr.get("R.T._(S)"))));
        return p1;
    }

    public static Peak2D create2DPeak(File peakReport, TableRow tr, double rt1, double rt2) {
        //System.out.println("Adding peak "+tr.get("NAME"));
        Peak2D p2 = new Peak2D();
        p2.setName(tr.get("NAME"));
        p2.setFile(peakReport.getAbsolutePath());
        p2.setFirstRetTime(rt1);
        p2.setSecondRetTime(rt2);
        return p2;
    }

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

    public static double parseDouble(String fieldName, TableRow tr) {
//        System.out.println("Retrieving " + fieldName);
        String value = tr.get(fieldName);
//        System.out.println("Value: " + value);
        return parseDouble(value);
    }

    public static double parseDouble(String s) {
        return parseDouble(s, defaultLocale);
    }

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
