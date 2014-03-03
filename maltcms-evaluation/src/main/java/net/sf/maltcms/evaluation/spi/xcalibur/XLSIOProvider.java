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
package net.sf.maltcms.evaluation.spi.xcalibur;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * Based on ExcelTools by Alexander Bunkowski. Uses Jexcel API,
 * http://jexcelapi.sourceforge.net/
 *
 * @author Nils Hoffmann
 *
 *
 */
public class XLSIOProvider {

    public static Vector<String> getCompoundNames(Workbook w) {
        Vector<String> compounds = new Vector<String>();
        if (w != null) {
            for (int i = 0; i < w.getNumberOfSheets(); i++) {
                final Sheet sheet = w.getSheet(i);
                Cell c = sheet.findCell("Component Name");
                String compname = getCellContent(sheet.getCell(c.getColumn(), c.getRow() + 1));
                if (!compname.isEmpty()) {
                    compounds.add(compname);
                }
            }
        }
        return compounds;
    }

    public static Vector<Peak> getPeaks(Workbook w, int rows, Creator creator, ObjectContainer oc) {
        Vector<Peak> peaks = new Vector<Peak>();
        if (w != null) {
            for (int i = 0; i < w.getNumberOfSheets(); i++) {
                final Sheet sheet = w.getSheet(i);
                //System.out.println("Parsing sheet "+sheet.getName());
                Cell c = sheet.findCell("Component Name");
                String compname = getCellContent(sheet.getCell(c.getColumn(), c.getRow() + 1));
                List<Integer> mws = getCharacteristicMass(compname);
                double[] mwsd = new double[mws.size()];
                for (int j = 0; j < mwsd.length; j++) {
                    mwsd[j] = mws.get(j);
                }
                Cell file = sheet.findCell("Filename", 0, 0, 100, 5, false);
                int headerRow = file.getRow();
                //System.out.println("Header info in row: "+headerRow);
                Cell area = sheet.findCell("Area", 0, headerRow, 100, headerRow, false);
                //System.out.println("Area column: "+area.getColumn()+" row: "+headerRow);
                int startRow = headerRow + 1;
                Cell height = sheet.findCell("Height", 0, headerRow, 100, headerRow, false);
                //System.out.println("Height column: "+height.getColumn()+" row: "+headerRow);
                Cell rt = sheet.findCell("RT", 0, headerRow, 100, headerRow, false);
                //System.out.println("RT column: "+rt.getColumn()+" row: "+headerRow);
                Cell startt = sheet.findCell("Start Time", 0, headerRow, 100, headerRow, false);
                //System.out.println("Starttime column: "+startt.getColumn()+" row: "+headerRow);
                Cell endt = sheet.findCell("End Time", 0, headerRow, 100, headerRow, false);
                //System.out.println("Endtime column: "+endt.getColumn()+" row: "+headerRow);
                //System.out.println("Parsing "+rows+" peaks from row "+startRow);
                for (int j = startRow; j < (startRow + rows); j++) {
                    //System.out.println(toString(sheet.getRow(j)));
                    Cell areaV = sheet.getCell(area.getColumn(), j);
                    Cell heightV = sheet.getCell(height.getColumn(), j);
                    Cell rtV = sheet.getCell(rt.getColumn(), j);
                    Cell starttV = sheet.getCell(startt.getColumn(), j);
                    Cell endtV = sheet.getCell(endt.getColumn(), j);
                    Cell fileV = sheet.getCell(file.getColumn(), j);
                    //System.out.println("Area: "+areaV.getContents()+" height: "+heightV.getContents()+" rt: "+rtV.getContents()+" startrt: "+starttV.getContents()+" stoprt: "+endtV.getContents());
                    if (!getCellContent(fileV).isEmpty()) {
                        Chromatogram chrom = new Chromatogram(getCellContent(fileV));
                        ObjectSet<Chromatogram> os = oc.queryByExample(chrom);
                        if (os.isEmpty()) {
                            oc.store(chrom);
                        } else {
                            chrom = os.get(0);
                        }

                        Peak p = new Peak(creator, chrom, compname, getContent(rtV), getContent(starttV), getContent(endtV), mwsd, getContent(areaV), getContent(heightV), RTUnit.Minutes);
                        if (oc.queryByExample(p).isEmpty()) {
                            oc.store(p);
                        }
                        peaks.add(p);
                    }
                }
            }
        }
        return peaks;
    }

    public static String toString(Cell[] c) {
        StringBuilder sb = new StringBuilder();
        for (Cell cell : c) {
            sb.append(cell.getContents() + "\t");
        }
        return sb.toString();
    }

    public static double getContent(Cell c) {
        String s = getCellContent(c);
        if (s.isEmpty() || s.equals("N/A") || s.equals("N/F") || s.equals("Peak Not Found")) {
            return Double.NaN;
        }
        s = s.replace(",", ".");
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException nfe) {
            System.err.println(nfe);
            return Double.NaN;
        }
    }

    public static String getCellContent(Cell c) {
        return c.getContents().trim();
    }

    public static Workbook getWorkbook(File f) {
        try {
            Workbook w = Workbook.getWorkbook(f);
            return w;
        } catch (BiffException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static int getAreaColumn(Sheet s) {
        return s.findCell("Area").getColumn();
    }

    public static int getHeightColumn(Sheet s) {
        return s.findCell("Height").getColumn();
    }

    public static int getRTColumn(Sheet s) {
        return s.findCell("RT").getColumn();
    }

    public static int getStartTimeColumn(Sheet s) {
        return s.findCell("Start Time").getColumn();
    }

    public static int getEndTimeColumn(Sheet s) {
        return s.findCell("End Time").getColumn();
    }

    public static Vector<String> getFilenames(Workbook w) {
        Vector<String> filenames = new Vector<String>();
        if (w != null) {
            Sheet s = w.getSheet(0);
            Cell c = s.findCell("Filename");
            int col = c.getColumn();
            int row = c.getRow();
            Cell[] cells = s.getColumn(col);
            for (int i = row + 1; i < cells.length; i++) {
                Cell cell = cells[i];
                if (!cell.getContents().trim().isEmpty()) {
                    //System.out.println("Cell "+i+"="+cell.getContents());
                    filenames.add(cell.getContents());
                } else {
                }
            }
        }
        return filenames;
    }

    public static void main(String[] args) {
        for (String arg : args) {
            File f = new File(arg);
            Workbook w = getWorkbook(f);
            Vector<String> filenames = getFilenames(w);
            Vector<String> compounds = getCompoundNames(w);
            System.out.println("File " + f.getAbsolutePath() + " contains the following records: ");
            System.out.println("Chromatograms: " + filenames);
            System.out.println("Compounds: " + compounds);
            int cnt = 0;
            for (String s : compounds) {
                if (!s.isEmpty()) {
                    System.out.println((cnt + 1) + "/" + compounds.size() + ": Characteristic mass(es) for " + s + "=" + getCharacteristicMass(s));
                    cnt++;
                }
            }
        }
    }

    public static List<Integer> getCharacteristicMass(String s) {
        final String name = s;
        int sidx = 0;
        int eidx = name.length() - 1;
        if (name.contains("(") || name.contains(")")) {
            sidx = name.indexOf("(") + 1;
            eidx = name.indexOf(")");
        } else if (name.contains("[") || name.contains("]")) {
            sidx = name.indexOf("[") + 1;
            eidx = name.indexOf("]");
        }
        String substring = "";
        try {
            int sidx2 = Math.max(0, sidx);
            int eidx2 = Math.min(name.length(), eidx);
            //System.out.println("sidx2: "+sidx2+" eidx2: "+eidx2);
            substring = name.substring(sidx2, eidx == -1 ? name.length() - 1 : eidx2);
        } catch (StringIndexOutOfBoundsException sie) {
            System.err.println("Exception while trying to subset: " + name + " from " + sidx + " to " + eidx);
        }
        List<Integer> l = new ArrayList<Integer>();
        if (substring.contains(",")) {
            String[] str = substring.split(",");
            for (String sps : str) {
                try {
                    int val = Integer.parseInt(sps);
                    l.add(val);
                } catch (NumberFormatException nfe) {
                    System.err.println("Could not parse as integer: " + substring);
                    //l.add(-1);
                }
                //l.add(Integer.parseInt(sps));
            }
        } else {
            try {
                int val = Integer.parseInt(substring);
                l.add(val);
            } catch (NumberFormatException nfe) {
                System.err.println("Could not parse as integer: " + substring);
                //l.add(-1);
            }

        }
        Collections.sort(l);
        return l;
    }

    public static Vector<String[]> importXLS(File f, int sheetNo) {
        Vector<String[]> data = new Vector<String[]>();
        if (f != null) {
            Workbook workbook = null;
            try {
                workbook = Workbook.getWorkbook(f);
                Sheet sheet = workbook.getSheet(sheetNo);
                int anz = sheet.getRows();
                for (int i = 0; i < anz; i++) {
                    try {
                        Cell[] cells = sheet.getRow(i);
                        String[] s = new String[cells.length];
                        for (int j = 0; j < cells.length; j++) {
                            try {
                                s[j] = cells[j].getContents().replace(",", ".");
                            } catch (Exception ex) {
                                s[j] = "";
                            }
                        }
                        data.add(s);
                    } catch (Exception ex) {
                    }
                }
                workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static void exportXLS(Vector<String[]> content, File file) {
        try {
            WritableWorkbook workbook = Workbook.createWorkbook(file);

            WritableSheet sheet = workbook.createSheet("Sheet", 0);
            for (int i = 0; i < content.size(); i++) {
                String[] strings = content.elementAt(i);
                for (int j = 0; j < strings.length; j++) {
                    String string = strings[j];
                    Label label = new Label(j, i, string);
                    sheet.addCell(label);
                }
            }
            workbook.write();
            workbook.close();
        } catch (Exception ex) {
            // Logger.getLogger(XLSIOProvider.class.getName()).log(Level.SEVERE,
            // null, ex);
        }
    }

    public static void exportXLS(JTable table, File file) {
        Vector<String[]> v = new Vector<String[]>();

        String[] head = new String[table.getTableHeader().getColumnModel()
            .getColumnCount()];
        for (int i = 0; i < head.length; i++) {
            head[i] = table.getTableHeader().getColumnModel().getColumn(i)
                .getHeaderValue().toString();
        }
        v.add(head);

        int rows = table.getModel().getRowCount();
        int clms = table.getModel().getColumnCount();

        for (int i = 0; i < rows; i++) {
            String[] s = new String[clms];
            for (int j = 0; j < clms; j++) {
                s[j] = (String) table.getModel().getValueAt(i, j);
            }
            v.add(s);
        }
        exportXLS(v, file);
    }

    public static void exportXLS(JTable table) {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.toString().endsWith(".xls")) {
                file = new File(file.getPath() + ".xls");
            }
            // ExcelTools.exportXLS(table, file);
        }

    }
}
