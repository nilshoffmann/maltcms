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

import maltcms.io.csv.ParserUtilities;
import cross.datastructures.tuple.Tuple2D;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Value;
import maltcms.datastructures.peak.Peak1D;
import maltcms.datastructures.peak.Peak2D;
import maltcms.io.csv.ParserUtilities;

/**
 * <p>
 * ChromaTOFParser class.</p>
 *
 * @author Nils Hoffmann
 *
 */
@Value
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

    public Tuple2D<double[], int[]> convertMassSpectrum(
            String massSpectrum) {
        if (massSpectrum == null) {
            Logger.getLogger(ChromaTOFParser.class.getName()).warning("Warning: mass spectral data was null!");
            return new Tuple2D<>(new double[0], new int[0]);
        }
        String[] mziTuples = massSpectrum.split(" ");
        TreeMap<Float, Integer> tm = new TreeMap<>();
        for (String tuple : mziTuples) {
            if (tuple.contains(":")) {
                String[] tplArray = tuple.split(":");
                tm.put(Float.valueOf(tplArray[0]), Integer.valueOf(tplArray[1]));
            } else {
                Logger.getLogger(ChromaTOFParser.class.getName()).log(
                        Level.WARNING, "Warning: encountered strange tuple: {0} within ms: {1}", new Object[]{tuple, massSpectrum});
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

    public static Tuple2D<LinkedHashSet<String>, List<TableRow>> parseReport(ChromaTOFParser parser, File f, boolean normalizeColumnNames) {
        LinkedHashSet<String> header = parser.getParserUtils().getHeader(f, normalizeColumnNames, parser.getFieldSeparator(), parser.getQuotationCharacter());
        List<TableRow> table = parser.getParserUtils().parseBody(header, f, normalizeColumnNames, parser.getFieldSeparator(), parser.getQuotationCharacter());
        return new Tuple2D<>(header, table);
    }

    public static Tuple2D<LinkedHashSet<String>, List<TableRow>> parseReport(
            File f, Locale locale) {
        return parseReport(f, true, locale);
    }

    public static Tuple2D<LinkedHashSet<String>, List<TableRow>> parseReport(
            File f, boolean normalizeColumnNames, Locale locale) {
        ChromaTOFParser parser = create(f, normalizeColumnNames, locale);
        return parseReport(parser, f, normalizeColumnNames);
    }

    public static ChromaTOFParser create(File f, boolean normalizeColumnNames, Locale locale) {
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

    public Peak1D create1DPeak(File peakReport, TableRow tr) {
        //System.out.println("1D chromatogram peak data detected");
        Peak1D p1 = new Peak1D();
        p1.setName(tr.get("NAME"));
        p1.setFile(peakReport.getAbsolutePath());
        p1.setApexTime(parseDouble((tr.get("R.T._(S)"))));
        return p1;
    }

    public Peak2D create2DPeak(File peakReport, TableRow tr, double rt1, double rt2) {
        //System.out.println("Adding peak "+tr.get("NAME"));
        Peak2D p2 = new Peak2D();
        p2.setName(tr.get("NAME"));
        p2.setFile(peakReport.getAbsolutePath());
        p2.setFirstRetTime(rt1);
        p2.setSecondRetTime(rt2);
        return p2;
    }

    public double[] parseDoubleArray(String fieldName, TableRow row,
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

    public double parseDouble(String fieldName, TableRow tr) {
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
}
