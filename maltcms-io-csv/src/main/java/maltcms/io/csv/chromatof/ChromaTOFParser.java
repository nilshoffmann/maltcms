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
package maltcms.io.csv.chromatof;

import cross.datastructures.tuple.Tuple2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import maltcms.io.csv.ParserUtilities;

/**
 * <p>
 * ChromaTOFParser class.</p>
 *
 * @author Nils Hoffmann
 *
 */
@Value
@Slf4j
public class ChromaTOFParser {
    
    public static String FIELD_SEPARATOR_TAB = "\t";
    public static String FIELD_SEPARATOR_COMMA = ",";
    public static String FIELD_SEPARATOR_SEMICOLON = ";";
    
    public static String QUOTATION_CHARACTER_DOUBLETICK = "\"";
    public static String QUOTATION_CHARACTER_NONE = "";
    public static String QUOTATION_CHARACTER_SINGLETICK = "\'";
    
    private final String fieldSeparator;
    private final String quotationCharacter;
    private final Locale locale;
    private final ParserUtilities parserUtils = new ParserUtilities();
    
    public enum Mode {

        /**
         * The default mode for 1D GC-MS data.
         */
        RT_1D,
        /**
         * The mode for 2D GC-MS data with combined retention times.
         * {@literal 'R.T. (s)'} contains two retention times, separated by a
         * comma.
         */
        RT_2D_FUSED,
        /**
         * The mode for 2D GC-MS data with separate retention times. '1st
         * Dimension Time (s)' and '2nd Dimension Time (s)' contain the two
         * retention times.
         */
        RT_2D_SEPARATE
    }
    
    public enum ColumnName {

        /**
         * Putative name. Original column name: "Name"
         */
        NAME("Name"),
        /**
         * Retention Time in seconds. Original column name:
         * {@literal "R.T. (s)"}
         */
        RETENTION_TIME_SECONDS("R.T. (s)"),
        /**
         * The first column retention time of a GCxGC peak. Original column
         * name: "1st Dimension Time (s)"
         */
        FIRST_DIMENSION_TIME_SECONDS("1st Dimension Time (s)"),
        /**
         * The second column retention time of a GCxGC peak. Original column
         * name: "2nd Dimension Time (s)"
         */
        SECOND_DIMENSION_TIME_SECONDS("2nd Dimension Time (s)"),
        /**
         * The peak type. Original column name: "Type"
         */
        TYPE("Type"),
        /**
         * The unique mass. Original column name: "UniqueMass"
         */
        UNIQUE_MASS("UniqueMass"),
        /**
         * The masses used for area quantification. Original column name: "Quant
         * Masses"
         */
        QUANT_MASSES("Quant Masses"),
        /**
         * The single mass used for area quantification. Original column name:
         * "Quant Mass"
         */
        QUANT_MASS("Quant Mass"),
        /**
         * The Signal-to-Noise ratio of the quantification masses' signals.
         * Original column name: "Quant S/N"
         */
        QUANT_SN("Quant S/N"),
        /**
         * The concentration of the peak. Original column name: "Concentration"
         */
        CONCENTRATION("Concentration"),
        /**
         * The sample concentration. Original column name: "Sample
         * Concentration"
         */
        SAMPLE_CONCENTRATION("Sample Concentration"),
        /**
         * The match. Original column name: "Match"
         */
        MATCH("Match"),
        /**
         * Then quantified area. Original column name: "Area"
         */
        AREA("Area"),
        /**
         * The putative sum formula. Original column name: "Formula"
         */
        FORMULA("Formula"),
        /**
         * The chemical abstracts service number of the putative identification.
         * Original column name: "CAS"
         */
        CAS("CAS"),
        /**
         * The match similarity (0-999) of the putative identification. Original
         * column name: "Similarity"
         */
        SIMILARITY("Similarity"),
        /**
         * The reverse match similarity (0-999). Original column name: "Reverse"
         */
        REVERSE("Reverse"),
        /**
         * The probability. Original column name: "Probability"
         */
        PROBABILITY("Probability"),
        /**
         * The purity. Original column name: "Purity"
         */
        PURITY("Purity"),
        /**
         * Free form concerns. Original column name: "Concerns"
         */
        CONCERNS("Concerns"),
        /**
         * The Signal-to-Noise ratio of the actual signal. Original column name:
         * "S/N"
         */
        SIGNAL_TO_NOISE("S/N"),
        /**
         * The modified baseline. Original column name: "BaselineModified"
         */
        BASELINE_MODIFIED("BaselineModified"),
        /**
         * The quantification. Original column name: "Quantification"
         */
        QUANTIFICATION("Quantification"),
        /**
         * The full width at half height characterizes the peak elongation.
         * Original column name: "Full Width at Half Height"
         */
        FULL_WIDTH_AT_HALF_HEIGHT("Full Width at Half Height"),
        /**
         * The start of area integration. Original column name:
         * "IntegrationBegin"
         */
        INTEGRATION_BEGIN("IntegrationBegin"),
        /**
         * The end of area integration. Original column name: "IntegrationEnd"
         */
        INTEGRATION_END("IntegrationEnd"),
        /**
         * The name of the first database match. Original column name: "Hit 1
         * Name"
         */
        HIT_1_NAME("Hit 1 Name"),
        /**
         * The similarity of the first database match. Original column name:
         * "Hit 1 Similarity"
         */
        HIT_1_SIMILARITY("Hit 1 Similarity"),
        /**
         * The reverse similarity of the first database match. Original column
         * name: "Hit 1 Reverse"
         */
        HIT_1_REVERSE("Hit 1 Reverse"),
        /**
         * The probability of the first database match. Original column name:
         * "Hit 1 Probability"
         */
        HIT_1_PROBABILITY("Hit 1 Probability"),
        /**
         * The CAS id of the first database match. Original column name: "Hit 1
         * CAS"
         */
        HIT_1_CAS("Hit 1 CAS"),
        /**
         * The library name of the database. Original column name: "Hit 1
         * Library"
         */
        HIT_1_LIBRARY("Hit 1 Library"),
        /**
         * The native id of the first match. Original column name: "Hit 1 Id"
         */
        HIT_1_ID("Hit 1 Id"),
        /**
         * The sum formula of the first match. Original column name: "Hit 1
         * Formula"
         */
        HIT_1_FORMULA("Hit 1 Formula"),
        /**
         * The molecular weight of the first match. Original column name: "Hit 1
         * Weight"
         */
        HIT_1_WEIGHT("Hit 1 Weight"),
        /**
         * The contributor of the first match. Original column name: "Hit 1
         * Contributor"
         */
        HIT_1_CONTRIBUTOR("Hit 1 Contributor"),
        /**
         * The spectrum of the match. Original column name: "Spectra"
         */
        SPECTRA("Spectra");
        
        private final String originalName;
        
        private ColumnName(String originalName) {
            this.originalName = originalName;
        }
        
        @Override
        public String toString() {
            return originalName;
        }
        
        public static ColumnName fromString(String name) {
            switch (name) {
                case "Name":
                case "NAME":
                    return NAME;
                case "R.T. (s)":
                case "R.T._(S)":
                    return RETENTION_TIME_SECONDS;
                case "1st Dimension Time (s)":
                case "1ST_DIMENSION_TIME_(S)":
                    return FIRST_DIMENSION_TIME_SECONDS;
                case "2nd Dimension Time (s)":
                case "2ND_DIMENSION_TIME_(S)":
                    return SECOND_DIMENSION_TIME_SECONDS;
                case "Type":
                case "TYPE":
                    return TYPE;
                case "UniqueMass":
                case "UNIQUEMASS":
                    return UNIQUE_MASS;
                case "Concentration":
                case "CONCENTRATION":
                    return CONCENTRATION;
                case "Sample Concentration":
                case "SAMPLE_CONCENTRATION":
                    return SAMPLE_CONCENTRATION;
                case "Match":
                case "MATCH":
                    return MATCH;
                case "Quant Masses":
                case "QUANT_MASSES":
                    return QUANT_MASSES;
                case "Quant S/N":
                case "QUANT_S/N":
                    return QUANT_SN;
                case "Quant Mass":
                case "QUANT_MASS":
                    return QUANT_MASS;
                case "Area":
                case "AREA":
                    return AREA;
                case "Formula":
                case "FORMULA":
                    return FORMULA;
                case "Cas":
                case "CAS":
                    return CAS;
                case "Similarity":
                case "SIMILARITY":
                    return SIMILARITY;
                case "Reverse":
                case "REVERSE":
                    return REVERSE;
                case "Probability":
                case "PROBABILITY":
                    return PROBABILITY;
                case "Purity":
                case "PURITY":
                    return PURITY;
                case "Concerns":
                case "CONCERNS":
                    return CONCERNS;
                case "s/n":
                case "S/N":
                    return SIGNAL_TO_NOISE;
                case "BaselineModified":
                case "BASELINEMODIFIED":
                    return BASELINE_MODIFIED;
                case "Quantification":
                case "QUANTIFICATION":
                    return QUANTIFICATION;
                case "Full Width at Half Height":
                case "FULL_WIDTH_AT_HALF_HEIGHT":
                    return FULL_WIDTH_AT_HALF_HEIGHT;
                case "IntegrationBegin":
                case "INTEGRATIONBEGIN":
                    return INTEGRATION_BEGIN;
                case "IntegrationEnd":
                case "INTEGRATIONEND":
                    return INTEGRATION_END;
                case "Hit 1 Name":
                case "HIT_1_NAME":
                    return HIT_1_NAME;
                case "Hit 1 Similarity":
                case "HIT_1_SIMILARITY":
                    return HIT_1_SIMILARITY;
                case "Hit 1 Reverse":
                case "HIT_1_REVERSE":
                    return HIT_1_REVERSE;
                case "Hit 1 Probability":
                case "HIT_1_PROBABILITY":
                    return HIT_1_PROBABILITY;
                case "Hit 1 CAS":
                case "HIT_1_CAS":
                    return HIT_1_CAS;
                case "Hit 1 Library":
                case "HIT_1_LIBRARY":
                    return HIT_1_LIBRARY;
                case "Hit 1 Id":
                case "HIT_1_ID":
                    return HIT_1_ID;
                case "Hit 1 Formula":
                case "HIT_1_FORMULA":
                    return HIT_1_FORMULA;
                case "Hit 1 Weight":
                case "HIT_1_WEIGHT":
                    return HIT_1_WEIGHT;
                case "Hit 1 Contributor":
                case "HIT_1_CONTRIBUTOR":
                    return HIT_1_CONTRIBUTOR;
                case "Spectra":
                case "SPECTRA":
                    return SPECTRA;
                default:
                    throw new IllegalArgumentException("Unsupported column name '" + name + "'");
            }
        }
    };
    
    public static Tuple2D<LinkedHashSet<ChromaTOFParser.ColumnName>, List<TableRow>> parseReport(ChromaTOFParser parser, File f, boolean normalizeColumnNames) {
        LinkedHashSet<ChromaTOFParser.ColumnName> header = parser.parseHeader(f, normalizeColumnNames, parser.getFieldSeparator(), parser.getQuotationCharacter());
        List<TableRow> table = parser.parseBody(header, f, normalizeColumnNames, parser.getFieldSeparator(), parser.getQuotationCharacter());
        return new Tuple2D<>(header, table);
    }
    
    public static Tuple2D<LinkedHashSet<ChromaTOFParser.ColumnName>, List<TableRow>> parseReport(
            File f, Locale locale) {
        return parseReport(f, true, locale);
    }
    
    public static Tuple2D<LinkedHashSet<ChromaTOFParser.ColumnName>, List<TableRow>> parseReport(
            File f, boolean normalizeColumnNames, Locale locale) {
        ChromaTOFParser parser = create(f, normalizeColumnNames, locale);
        return parseReport(parser, f, normalizeColumnNames);
    }
    
    public static ChromaTOFParser create(File f, boolean normalizeColumnNames, Locale locale) throws IllegalArgumentException {
        ChromaTOFParser parser;
        if (f.getName().toLowerCase().endsWith("csv")) {
            parser = new ChromaTOFParser(FIELD_SEPARATOR_COMMA, QUOTATION_CHARACTER_DOUBLETICK, locale);
        } else if (f.getName().toLowerCase().endsWith("tsv") || f.getName().toLowerCase().endsWith("txt")) {
            parser = new ChromaTOFParser(FIELD_SEPARATOR_TAB, QUOTATION_CHARACTER_NONE, locale);
        } else {
            throw new IllegalArgumentException("Unsupported file extension '" + f.getName().toLowerCase() + "'! Supported are '.csv', '.tsv', '.txt'.");
        }
        return parser;
    }
    
    public double[] parseDoubleArray(ColumnName fieldName, TableRow row,
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
    
    public double parseDouble(ColumnName fieldName, TableRow tr) {
        return parseDouble(tr.get(fieldName));
    }
    
    public double parseDouble(String s) {
        return parserUtils.parseDouble(s, locale);
    }
    
    public double parseIntegrationStartEnd(String s) {
        if (s == null || s.isEmpty()) {
            return Double.NaN;
        }
        if (s.contains(",")) {
            String[] tokens = s.split(",");
            return parseDouble(tokens[0]);
        }
        return parseDouble(s);
    }
    
    public Mode getMode(List<TableRow> body) {
        for (TableRow tr : body) {
            if (tr.containsKey(ChromaTOFParser.ColumnName.RETENTION_TIME_SECONDS)) {
                //fused RT mode
                String rt = tr.get(ChromaTOFParser.ColumnName.RETENTION_TIME_SECONDS);
                if (rt.contains(",")) {//2D mode
                    return ChromaTOFParser.Mode.RT_2D_FUSED;
                } else {
                    return ChromaTOFParser.Mode.RT_1D;
                }
            } else {
                if (tr.containsKey(ChromaTOFParser.ColumnName.FIRST_DIMENSION_TIME_SECONDS) && tr.containsKey(ChromaTOFParser.ColumnName.SECOND_DIMENSION_TIME_SECONDS)) {
                    return ChromaTOFParser.Mode.RT_2D_SEPARATE;
                }
            }
        }
        return ChromaTOFParser.Mode.RT_1D;
    }

    /**
     * Parse the header of the given file.
     *
     * @param f the file to parse.
     * @param normalizeColumnNames if true, column names are capitalized and
     * spaces are replaced by '_'.
     * @param fieldSeparator the field separator to use, e.g. '\t', ',', or ';'.
     * @param quotationCharacter the quotation character to use, e.g. '"', '',
     * or '''.
     * @return the set of unique column names in order of appearance.
     */
    public LinkedHashSet<ChromaTOFParser.ColumnName> parseHeader(File f, boolean normalizeColumnNames, String fieldSeparator, String quotationCharacter) {
        LinkedHashSet<ChromaTOFParser.ColumnName> globalHeader = new LinkedHashSet<>();
        ArrayList<String> header = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line = "";
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    String[] lineArray = splitLine(line, fieldSeparator, quotationCharacter);//line.split(String.valueOf(FIELD_SEPARATOR));
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
                }
            }
        } catch (IOException ex) {
            log.warn("Caught an IO Exception while reading file " + f, ex);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                log.warn("Caught an IO Exception while trying to close stream of file " + f, ex);
            }
        }
        for (String str : header) {
            try {
                globalHeader.add(ChromaTOFParser.ColumnName.fromString(str));
            } catch (IllegalArgumentException iae) {
                log.warn("Unsupported column name '{}'", str);
            }
        }
        return globalHeader;
    }

    /**
     * Parse the header of the given file.
     *
     * @param f
     * @param normalizeColumnNames
     * @param fieldSeparator
     * @param quotationCharacter
     * @return
     * @deprecated use {@link #parseHeader(java.io.File, boolean, java.lang.String, java.lang.String)
     * }
     */
    public LinkedHashSet<ChromaTOFParser.ColumnName> getHeader(File f, boolean normalizeColumnNames, String fieldSeparator, String quotationCharacter) {
        return parseHeader(f, normalizeColumnNames, fieldSeparator, quotationCharacter);
    }
    
    public String[] splitLine(String line, String fieldSeparator, String quoteSymbol) {
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
    
    public List<TableRow> parseBody(LinkedHashSet<ChromaTOFParser.ColumnName> globalHeader,
            File f, boolean normalizeColumnNames, String fieldSeparator, String quotationCharacter) {
        List<TableRow> body = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line = "";
            List<ChromaTOFParser.ColumnName> header = null;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    ArrayList<String> lineList = new ArrayList<>(Arrays.asList(splitLine(line, fieldSeparator, quotationCharacter)));//.split(String.valueOf(FIELD_SEPARATOR))));
                    if (header == null) {
                        if (normalizeColumnNames) {
                            for (int i = 0; i < lineList.size(); i++) {
                                lineList.set(i, lineList.get(i).trim().toUpperCase().
                                        replaceAll(" ", "_"));
                            }
                        }
                        header = new ArrayList<>();
                        for (String str : lineList) {
                            header.add(ChromaTOFParser.ColumnName.fromString(str));
                        }
                    } else {
                        TableRow tr = new TableRow();
                        for (ChromaTOFParser.ColumnName headerColumn : globalHeader) {
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
                }
            }
        } catch (IOException ex) {
            log.warn("Caught an IO Exception while reading file " + f, ex);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                log.warn("Caught an IO Exception while trying to close stream of file " + f, ex);
            }
        }
        return body;
    }
    
    public int getIndexOfHeaderColumn(List<ChromaTOFParser.ColumnName> header,
            ChromaTOFParser.ColumnName columnName) {
        int idx = 0;
        for (ChromaTOFParser.ColumnName str : header) {
            if (str == columnName) {
                return idx;
            }
//            if (str.equalsIgnoreCase(columnName)) {
//                return idx;
//            }
            idx++;
        }
        return -1;
    }
}
