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
import maltcms.datastructures.ms.Metabolite2D;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.PeakArea2D;
import maltcms.datastructures.peak.annotations.PeakAnnotation;
import maltcms.io.csv.CSVWriter;
import maltcms.tools.MaltcmsTools;
import org.apache.commons.configuration.Configuration;
import org.jdom.Element;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.MAMath;

/**
 * Implementation of an peak exporter.
 *
 * @author Mathias Wilhelm
 *
 */
@Slf4j
@Data
@ServiceProvider(service = IPeakExporter.class)
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
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
        row.add("Database");
        row.add("MS Similarity");
        table.add(row);
        PeakArea2D s;
        Peak2D peak;
        for (Peak2D p : ps) {
            peak = p;
            s = peak.getPeakArea();
            row = new ArrayList<>();
            row.add(ps.indexOf(peak) + "");
            row.add(peak.getApexIndex() + "");
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
            List<PeakAnnotation> peakAnnotations = peak.getPeakAnnotations();
            if (peakAnnotations.isEmpty()) {
                row.add("Unknown");
//			peakNames.add(peak.getName());
                row.add(Double.NaN+"");
                row.add("NA");
                row.add("NA");
            } else {
                row.add(peak.getPeakAnnotations().get(0).getMetabolite().getName());
//			peakNames.add(peak.getName());
                row.add(String.format(Locale.US, "%10.4f", peak.getPeakAnnotations().get(0).getScore()+""));
                row.add(peak.getPeakAnnotations().get(0).getDatabase());
                row.add(peak.getPeakAnnotations().get(0).getSimilarityFunction());
            }
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
    public void exportPeaksToMSP(final String name, final List<Peak2D> ps) {
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
                        + "_" + cnt;
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
                            getStartupDate().toString(), 0, p.getFirstRetTime()/60.0d,
                            "min", mw, "", p.getName(),
                            masses, intensities, 0, p.getSecondRetTime(), "sec");
                    try {
                        dos.write(m.toString());
                        dos.newLine();
                        dos.flush();
                    } catch (IOException e) {

                        log.warn(e.getLocalizedMessage());
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

                log.warn(e.getLocalizedMessage());
            }
        } catch (FileNotFoundException e) {

            log.warn(e.getLocalizedMessage());
        } catch (IOException e) {

            log.warn(e.getLocalizedMessage());
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
        metaTable.add(header);
        List<String> entry;
        String value;
        int i = 0;
        for (Peak2D p : peaklist) {
            entry = new ArrayList<>();
            entry.add(i + "");
            value = "";
            for (PeakAnnotation r : p.getPeakAnnotations()) {
                value += r.getMetabolite().getName() + "(" + r.getScore()+ "), ";
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

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.FILEIO;
    }

    /**
     * {@inheritDoc}
     */
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
