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
package maltcms.commands.fragments2d.peakfinding.output;

import cross.annotations.Configurable;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments2d.peakfinding.Reliability;
import maltcms.commands.fragments2d.peakfinding.bbh.IBidirectionalBestHit;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.ms.Metabolite2D;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.PeakArea2D;
import maltcms.io.csv.CSVWriter;
import maltcms.tools.MaltcmsTools;
import org.apache.commons.configuration.Configuration;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.MAMath;

/**
 * Implementation of an peak exporter.
 *
 * @author Mathias Wilhelm
 */
@Slf4j
@Data
public class PeakExporter implements IPeakExporter {

    private IWorkflow workflow;
    // @SuppressWarnings("unchecked")
    // private Class caller = this.getClass();
    @Configurable(value = "false", type = boolean.class)
    private boolean compareAllAgainstAll = false;
    @Configurable(value = "#0.00", type = String.class)
    private String formatString = "#0.00";
    private NumberFormat formatter = new DecimalFormat(this.formatString,
            DecimalFormatSymbols.getInstance(Locale.US));

    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
//        this.formatString = cfg.getString(this.getClass().getName()
//                + ".formatString", "#0.00");
//        this.formatter = new DecimalFormat(this.formatString,
//                DecimalFormatSymbols.getInstance(Locale.US));
//        this.compareAllAgainstAll = cfg.getBoolean(this.getClass().getName()
//                + ".compareAllAgainstAll", false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportBBHInformation(final List<List<Point>> bidiBestHitList,
            List<List<Peak2D>> peaklist, final IBidirectionalBestHit bbh,
            final List<String> names, final List<Reliability> reliabilities) {
        final List<List<String>> table = new ArrayList<>();
        if (bidiBestHitList.size() > 0) {
            table.add(new ArrayList<String>());
            table.get(0).add("Name");
            for (int i = 1; i < bidiBestHitList.get(0).size() + 1; i++) {
                table.add(new ArrayList<String>());
                // table.get(i).add(t.get(i - 1).getName());
                table.get(i).add(names.get(i - 1));
            }
            for (final List<Point> l : bidiBestHitList) {
                String name = "Unkown";
                for (final Point p : l) {
                    if (p.x != -1) {
                        // table.get(p.y + 1).add(
                        // this.formatter.format(bbh.getPeakLists().get(
                        // p.y).get(p.x).getPeakArea()
                        // .getAreaIntensity())
                        // + "(" + p.x + ")");
                        table.get(p.y + 1).add(
                                this.formatter.format(peaklist.get(p.y).get(p.x).
                                        getPeakArea().getAreaIntensity()));
//                                                                p.x + "");
                        if (name.equals("Unkown")) {
                            name = peaklist.get(p.y).get(p.x).getName();
                        }
                    } else {
                        // table.get(p.y + 1).add("-(-)");
                        table.get(p.y + 1).add("-1.0");
                    }
                }
                table.get(0).add(name);
            }
            List<String> relis = new ArrayList<>();
            relis.add("Reliability");
            for (List<Point> bidiBestHitList1 : bidiBestHitList) {
                relis.add("0.0d");
            }
            table.add(relis);

            final CSVWriter csvw = new CSVWriter();
            csvw.setWorkflow(this.workflow);
            csvw.writeTableByCols(this.workflow.getOutputDirectory(this).
                    getAbsolutePath(), "bidibestHits.csv", table,
                    WorkflowSlot.PEAKFINDING);
        } else {
            this.log.error("Bidirectional best hit list ist empty");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportBBHMultipleAlignmentRT(
            final List<List<Point>> bidiBestHitList,
            List<List<Peak2D>> peaklist, final IBidirectionalBestHit bbh,
            final List<String> names, final List<Reliability> reliabilities) {
        final List<List<String>> table = new ArrayList<>();
        NumberFormat formatter = new DecimalFormat("#0.0000",
                DecimalFormatSymbols.getInstance(Locale.US));
        if (bidiBestHitList.size() > 0) {
            for (int i = 0; i < bidiBestHitList.get(0).size(); i++) {
                table.add(new ArrayList<String>());
                table.get(i).add(names.get(i));
            }
            for (final List<Point> l : bidiBestHitList) {
                for (final Point p : l) {
                    if (p.x != -1) {
                        table.get(p.y).add(
                                formatter.format(peaklist.get(p.y).get(p.x).
                                        getRetentionTime()));
                    } else {
                        // table.get(p.y + 1).add("-(-)");
                        table.get(p.y).add("NA");
                    }
                }
            }

            final CSVWriter csvw = new CSVWriter();
            csvw.setWorkflow(this.workflow);
            csvw.writeTableByCols(this.workflow.getOutputDirectory(this).
                    getAbsolutePath(), "multiple-alignmentRT.csv", table,
                    WorkflowSlot.PEAKFINDING);
        } else {
            this.log.error("Bidirectional best hit list ist empty");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportDetailedBBHInformation(
            final List<List<Point>> bidiBestHitList,
            final List<List<Peak2D>> peaklists,
            final IBidirectionalBestHit bbh,
            final List<String> chromatogramNames,
            final List<Reliability> reliabilities, final String oFilename) {

        // Setting up metatable
        final List<List<String>> metaTable = new ArrayList<>();

        int diffStart = 1;
        // creating name field
        final List<String> name = new ArrayList<>();
        name.add("Name");
        metaTable.add(name);
        // creating value fields
        for (Datatypes t : Datatypes.values()) {
            for (String filename : chromatogramNames) {
                final List<String> tmp = new ArrayList<>();
                tmp.add(t.toString() + "-" + filename);
                metaTable.add(tmp);
                diffStart++;
            }
        }
        int specC = 0;
        // creating special value fields
        for (SpecialDatatypes t : SpecialDatatypes.values()) {
            specC = 0;
            for (int i = 0; i < chromatogramNames.size(); i++) {
                for (int j = i + 1; j < chromatogramNames.size(); j++) {
                    final List<String> tmp = new ArrayList<>();
                    tmp.add(t.toString() + "-" + chromatogramNames.get(i) + "_"
                            + chromatogramNames.get(j));
                    metaTable.add(tmp);
                    specC++;
                }
                if (!this.compareAllAgainstAll) {
                    break;
                }
            }
        }
        // creating mean and group field
        // final List<String> meanG = new ArrayList<String>();
        // meanG.add("Mean_TIC");
        // metaTable.add(meanG);
        final List<String> group = new ArrayList<>();
        group.add("Group");
        metaTable.add(group);

        // setting up content
        boolean complete = true;
        int groupC = 0;
        Peak2D peak;
        // double mean = 0.0d;
        for (List<Point> list : bidiBestHitList) {
            complete = true;
            groupC++;
            // checking, wether a bbh group is complete or not
            for (Point p : list) {
                if (p.x == -1) {
                    complete = false;
                    break;
                }
            }
            if (complete) {
                // mean = 0.0d;
                // export normal value fields
                for (Point p : list) {
                    int i = p.y + 1;
                    peak = peaklists.get(p.y).get(p.x);
                    for (Datatypes t : Datatypes.values()) {
                        switch (t) {
                            case LocalIndex:
                                metaTable.get(i).add(peak.getIndex() + "");
                                break;
                            case ScanIndex:
                                metaTable.get(i).add(peak.getScanIndex() + "");
                                break;
                            case PeakArea:
                                // mean += peak.getPeakArea().getAreaIntensity();
                                metaTable.get(i).add(
                                        this.formatter.format(peak.getPeakArea().
                                                getAreaIntensity()));
                                break;
                            case LogPeakArea:
                                metaTable.get(i).add(
                                        this.formatter.format(Math.log(peak.
                                                        getPeakArea().getAreaIntensity())));
                                break;
                            case RetT1:
                                metaTable.get(i).add(
                                        this.formatter.format(peak.
                                                getFirstRetTime()));
                                break;
                            case RetT2:
                                metaTable.get(i).add(
                                        this.formatter.format(peak.
                                                getSecondRetTime()));
                                break;
                            default:
                                break;
                        }
                        i = i + chromatogramNames.size();
                    }
                    // creating static content
                    if (p.y == 0) {
                        metaTable.get(0).add(
                                peaklists.get(0).get(p.x).getName());
                        metaTable.get(metaTable.size() - 1).add(groupC + "");
                        // metaTable.get(metaTable.size() - 2).add(
                        // this.formatter.format(mean
                        // / (double) chromatogramNames.size()));
                    }
                }
                // export special value fields
                double v = 0;
                Peak2D peak1, peak2;
                int k = 0, l = 0;
                for (int i = 0; i < list.size(); i++) {
                    for (int j = i + 1; j < list.size(); j++) {
                        peak1 = peaklists.get(list.get(i).y).get(list.get(i).x);
                        peak2 = peaklists.get(list.get(j).y).get(list.get(j).x);
                        k = 0;
                        for (SpecialDatatypes t : SpecialDatatypes.values()) {
                            switch (t) {
                                case PeakAreaRatio:
                                    v = peak1.getPeakArea().getAreaIntensity()
                                            / peak2.getPeakArea().
                                            getAreaIntensity();
                                    metaTable.get(diffStart + k * specC + l).add(
                                            this.formatter.format(v));
                                    break;
                                case DeltaPeakArea:
                                    v = peak1.getPeakArea().getAreaIntensity()
                                            - peak2.getPeakArea().
                                            getAreaIntensity();
                                    metaTable.get(diffStart + k * specC + l).add(
                                            this.formatter.format(v));
                                    break;
                                case LogDeltaPeakArea:
                                    boolean n = false;
                                    v = peak1.getPeakArea().getAreaIntensity()
                                            - peak2.getPeakArea().
                                            getAreaIntensity();
                                    if (v < 0) {
                                        n = true;
                                        v = Math.abs(v);
                                    }
                                    if (v >= 0) {
                                        v = Math.log(v);
                                    }
                                    if (n) {
                                        v = v * (-1.0d);
                                    }
                                    metaTable.get(diffStart + k * specC + l).add(
                                            this.formatter.format(v));
                                    break;
                                case Similarity:
                                    v = bbh.sim(peak1, peak2);
                                    metaTable.get(diffStart + k * specC + l).add(
                                            this.formatter.format(v));
                                    break;
                                default:
                                    break;
                            }
                            k++;
                        }
                        l++;
                    }
                    if (!this.compareAllAgainstAll) {
                        break;
                    }
                }
            }
        }

        // for (List<String> content : metaTable) {
        // this.log.info("{}:{}", content.get(0), content.size());
        // }
        // writing file
        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(this.workflow);
        csvw.writeTableByCols(this.workflow.getOutputDirectory(this).
                getAbsolutePath(), oFilename, metaTable,
                WorkflowSlot.PEAKFINDING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportPeakOccurrenceMap(
            final List<List<Point>> bidiBestHitList,
            final List<List<Peak2D>> peaklists,
            final IBidirectionalBestHit bbh,
            final List<String> chromatogramNames, final String oFilename) {
        final List<List<String>> metaTable = new ArrayList<>();

        final List<String> header = new ArrayList<>();
        header.add("Filename");
        header.add("GroupID");
        header.add("RT1[s]");
        header.add("RT2[s]");
        header.add("Area[TIC]");
        header.add("Apex[TIC]");
        metaTable.add(header);

        List<String> row;
        Peak2D peak;
        for (int i = 0; i < bidiBestHitList.size(); i++) {
            for (Point p : bidiBestHitList.get(i)) {
                if (p.x != -1) {
                    // peakmatch
                    row = new ArrayList<>();
                    row.add(chromatogramNames.get(p.y));
                    row.add(i + "");
                    peak = peaklists.get(p.y).get(p.x);
                    row.add(this.formatter.format(peak.getFirstRetTime()));
                    row.add(this.formatter.format(peak.getSecondRetTime()));
                    row.add(this.formatter.format(peak.getPeakArea().
                            getAreaIntensity()));
                    row.add(this.formatter.format(peak.getPeakArea().
                            getSeedIntensity()));
                    metaTable.add(row);
                } else {
                    // no peak match
                }
            }
        }

        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(this.workflow);
        csvw.writeTableByRows(this.workflow.getOutputDirectory(this).
                getAbsolutePath(), oFilename, metaTable,
                WorkflowSlot.PEAKFINDING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportDetailedPeakInformation(final String name,
            final List<Peak2D> ps) {
        final Map<Integer, List<Peak2D>> table = new HashMap<>();
        Map<Integer, Double> intensities;
        List<Peak2D> list;
        for (final Peak2D p : ps) {
            intensities = p.getPeakArea().getAreaIntensities();
            for (final Integer mass : intensities.keySet()) {
                if (!table.containsKey(mass)) {
                    list = new ArrayList<>();
                    list.add(p);
                    table.put(mass, list);
                } else {
                    table.get(mass).add(p);
                }
            }
        }

        final List<Integer> masses = new ArrayList<>(table.keySet());
        Collections.sort(masses, new Comparator<Integer>() {
            @Override
            public int compare(final Integer o1, final Integer o2) {
                return Double.compare(o1, o2);
            }
        });

        final List<List<String>> printTable = new ArrayList<>();
        List<String> row = new ArrayList<>();
        row.add("");
        for (final Integer mass : masses) {
            row.add(mass + "");
        }
        printTable.add(row);

        for (final Peak2D p : ps) {
            row = new ArrayList<>();
            row.add(p.getIndex() + "");
            for (Integer masse : masses) {
                row.add("0.0");
            }
            for (final Entry<Integer, Double> e : p.getPeakArea().
                    getAreaIntensities().entrySet()) {
                row.set(masses.indexOf(e.getKey()) + 1, this.formatter.format(e.
                        getValue())
                        + "");
            }
            printTable.add(row);
        }

        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(this.workflow);
        csvw.writeTableByRows(this.workflow.getOutputDirectory(this).
                getAbsolutePath(), name + "_peakAreas.csv", printTable,
                WorkflowSlot.PEAKFINDING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportPeakInformation(final String name, final List<Peak2D> ps) {
//		final List<Peak2D> peaklist = new ArrayList<Peak2D>();
        final List<List<String>> table = new ArrayList<>();
//		final Collection<String> peakNames = new ArrayList<String>();
//		int maxLength = 10;
        List<String> row = new ArrayList<>();
        row.add("Index");
        row.add("ScanIndex");
        row.add("RT1[s]");
        row.add("RT2[s]");
        row.add("Apex[TIC]");
        row.add("Area[TIC]");
        row.add("Area[no.]");
        row.add("Name");
        row.add("Sim");
        table.add(row);
        PeakArea2D s;
        Peak2D peak;
        for (Peak2D p : ps) {
            peak = p;
            s = peak.getPeakArea();
            row = new ArrayList<>();
            row.add(ps.indexOf(peak) + "");
            row.add(peak.getScanIndex() + "");
            row.add(String.format(Locale.US, "%10.4f", peak.getFirstRetTime())
                    + "");
            row.add(String.format(Locale.US, "%10.4f", peak.getSecondRetTime())
                    + "");
            row.add(String.format(Locale.US, "%10.4f", s.getSeedIntensity())
                    + "");
            row.add(String.format(Locale.US, "%10.4f", s.getAreaIntensity())
                    + "");
            row.add(s.size() + "");
//			if (peak.getName().length() > maxLength) {
//				maxLength = peak.getName().length();
//			}
            row.add(peak.getName());
//			peakNames.add(peak.getName());
            row.add(peak.getSim() + "");
//			peaklist.add(peak);
            table.add(row);
        }

        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(this.workflow);
        csvw.writeTableByRows(this.workflow.getOutputDirectory(this).
                getAbsolutePath(), name + "_peaklist.csv", table,
                WorkflowSlot.PEAKFINDING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportPeaksToMSP(final String name, final List<Peak2D> ps,
            final IScanLine isl) {
        File outputDir = this.workflow.getOutputDirectory(this);
        File fname = new File(outputDir, name);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        int cnt = 1;
        try {
            BufferedWriter dos = new BufferedWriter(new FileWriter(fname));
            for (Peak2D p : ps) {
                String pname = "CHROMA4D_" + StringTools.removeFileExt(name)
                        + "_" + p.getIndex();
                // Tuple2D<Array, Array> t = isl.getSparseMassSpectra(p
                // .getPeakArea().getSeedPoint());
                Array denseIntensities = p.getPeakArea().getSeedMS();

                final ArrayInt.D1 mz = new ArrayInt.D1(
                        denseIntensities.getShape()[0]);
                for (int i = 0; i < denseIntensities.getShape()[0]; i++) {
                    mz.set(i, i);
                }
                Tuple2D<Array, Array> t = new Tuple2D<Array, Array>(mz,
                        denseIntensities);

                // if (t == null) {
                // this.log.info("Calling with {},{} returned null.", p
                // .getPeakArea().getSeedPoint().x, p.getPeakArea()
                // .getSeedPoint().y);
                // }
                if (t != null && t.getFirst() != null && t.getSecond() != null) {
                    int mw = (int) MaltcmsTools.getMaxMass(t.getFirst(), t.
                            getSecond());
                    ArrayDouble.D1 masses = new ArrayDouble.D1(t.getFirst().
                            getShape()[0]);
                    ArrayInt.D1 intensities = new ArrayInt.D1(t.getSecond().
                            getShape()[0]);
                    MAMath.copyDouble(masses, t.getFirst());
                    MAMath.copyInt(intensities, t.getSecond());
                    Metabolite2D m = new Metabolite2D(pname, pname,
                            "CHROMA4D-ID", cnt, "", "", this.workflow.
                            getStartupDate().toString(), 0, p.getFirstRetTime(),
                            "min", mw, "", p.getName(),
                            masses, intensities, 0, p.getSecondRetTime(), "sec");
                    try {
                        dos.write(m.toString());
                        dos.newLine();
                        dos.flush();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    cnt++;
                } else {
                    this.log.error(
                            "Can not export peak {}, because MS is null", cnt);
                }
            }
            try {
                dos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportPeakNames(final List<Peak2D> peaklist,
            final String chomatogramName) {
        final List<List<String>> metaTable = new ArrayList<>();

        final List<String> header = new ArrayList<>();
        header.add("PeakID");
        header.add("Identifications");

        List<String> entry;
        String value;
        int i = 0;
        for (Peak2D p : peaklist) {
            entry = new ArrayList<>();
            entry.add(i + "");
            value = "";
            for (Tuple2D<Double, IMetabolite> r : p.getNames()) {
                value += r.getSecond().getName() + "(" + r.getFirst() + "), ";
            }
            entry.add(value);
            metaTable.add(entry);
            i++;
        }

        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(this.workflow);
        csvw.writeTableByRows(this.workflow.getOutputDirectory(this).
                getAbsolutePath(), chomatogramName + "_peakIdentifications",
                metaTable,
                WorkflowSlot.PEAKFINDING);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setCaller(final Class nCaller) {
        if (nCaller != null) {
            // this.caller = nCaller;
        }
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.FILEIO;
    }

    @Override
    public void appendXML(Element e) {
    }

    // each datatyp will appear only once for each chromatogram
    private enum Datatypes {

        LocalIndex, ScanIndex, PeakArea, LogPeakArea, RetT1, RetT2;
    }

    // depending on compareAllAgainstAll, this values will appear
    // multiple(range[n-1),n!/2])
    private enum SpecialDatatypes {

        PeakAreaRatio, DeltaPeakArea, LogDeltaPeakArea, Similarity;
    }
}
