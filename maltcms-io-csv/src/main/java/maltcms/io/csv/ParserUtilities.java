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
package maltcms.io.csv;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import maltcms.io.csv.chromatof.ChromaTOFParser;
import maltcms.io.csv.chromatof.TableRow;

/**
 *
 * @author Nils Hoffmann
 */
public class ParserUtilities {

    public double parseDouble(String s, Locale locale) {
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

    public LinkedHashSet<String> getHeader(File f, boolean normalizeColumnNames, String fieldSeparator, String quotationCharacter) {
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

    public List<TableRow> parseBody(LinkedHashSet<String> globalHeader,
            File f, boolean normalizeColumnNames, String fieldSeparator, String quotationCharacter) {
        List<TableRow> body = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));

            String line = "";
            int lineCount = 0;
            List<String> header = null;
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
    
    public HashMap<String, String> getFilenameToGroupMap(File f, String fieldSeparator) {
        List<String> header = null;
        HashMap<String, String> filenameToGroupMap = new LinkedHashMap<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line = "";
            int lineCount = 0;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    String[] lineArray = line.split(String.valueOf(fieldSeparator));
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

    public int getIndexOfHeaderColumn(List<String> header,
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
}
