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

import cross.Factory;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.NotImplementedException;
import cross.tools.StringTools;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.Element;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayChar.StringIterator;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;

/**
 * Provides various methods to write arrays and other data to csv files.
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
@Data
public class CSVWriter implements IWorkflowElement {

    private String fieldSeparator = "\t";
    private IWorkflow workflow = null;

    private void appendToWorkflow(final File f, final WorkflowSlot ws,
            final IFileFragment... resources) {
        if (getWorkflow() != null) {
            final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
                    new File(f.getAbsolutePath()), this, ws, resources);
            getWorkflow().append(dwr);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.io.misc.IXMLSerializable#appendXML(org.jdom.Element)
     */
    /** {@inheritDoc} */
    @Override
    public void appendXML(final Element e) {
        throw new NotImplementedException();
    }

    /**
     * <p>checkEqualLength.</p>
     *
     * @param a a {@link java.util.List} object.
     * @return a boolean.
     */
    public boolean checkEqualLength(final List<ArrayDouble.D1>... a) {
        List<ArrayDouble.D1> previous = null;
        for (final List<ArrayDouble.D1> arr : a) {
            if (previous == null) {
                previous = arr;
            } else {
                if (checkEqualLength(arr)) {// !MAMath.conformable(previous,arr))
                    // {
                    log.warn("Arrays are not conformable!");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <p>checkEqualLength.</p>
     *
     * @param a a {@link java.util.List} object.
     * @return a boolean.
     */
    public boolean checkEqualLength(final List<ArrayDouble.D1> a) {
        Array previous = null;
        for (final Array arr : a) {
            if (previous == null) {
                previous = arr;
            } else {
                if (!MAMath.conformable(previous, arr)) {
                    return false;
                }
            }
        }
        return true;
    }

    private File createFile(final File f, final WorkflowSlot ws,
            final IFileFragment... resources) {
        return createFile(f.getParent(), f.getName(), ws, resources);
    }

    private File createFile(final String path, final String filename,
            final WorkflowSlot ws, final IFileFragment... resources) {
        File f = FileTools.prepareOutput(path, StringTools.removeFileExt(
                filename), "csv");
        appendToWorkflow(f, ws, resources);
        return f;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /** {@inheritDoc} */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }

    /**
     * <p>writeAlignmentPath.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @param filename a {@link java.lang.String} object.
     * @param map a {@link java.util.List} object.
     * @param distValues an array of double.
     * @param rows a int.
     * @param cols a int.
     * @param refname a {@link java.lang.String} object.
     * @param queryname a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     * @param symbolicPath a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    public File writeAlignmentPath(final String path, final String filename,
            final List<Tuple2DI> map, final double[] distValues, final int rows, final int cols,
            final String refname, final String queryname, final String value,
            final String symbolicPath) {
        final File f = createFile(path, filename, WorkflowSlot.ALIGNMENT);
        // appendToWorkflow(f, WorkflowSlot.ALIGNMENT);
        log.info("Value of [0][0] = {}", distValues[0]);
        try {
            try (PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)))) {
                final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                        Locale.US);
                df.applyPattern("0.0000");
                bw.append(refname + this.fieldSeparator + queryname
                        + this.fieldSeparator + value + this.fieldSeparator
                        + "symbol");
                bw.println();
                log.info("Shape of matrix rows {} cols {}", rows, cols);
                for (int i = 0; i < map.size(); i++) {
                    final Tuple2DI t = map.get(i);
                    log.debug("Getting path value for {},{}", t.getFirst(), t.
                            getSecond());
                    bw.append(t.getFirst()
                            + this.fieldSeparator
                            + t.getSecond()
                            + this.fieldSeparator
                            + distValues[i]
                            + ((symbolicPath != null) ? this.fieldSeparator
                            + symbolicPath.charAt(i) : ""));
                    bw.println();
                    // if (writeBlockNewline) {
                    // bw.println();
                    // }

                    // bw.flush();
                }
                bw.flush();
            }
            log.info(map.size() + " records written to file "
                    + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            
            log.warn(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * <p>writeArray.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @param filename a {@link java.lang.String} object.
     * @param vals a {@link ucar.ma2.Array} object.
     * @return a {@link java.io.File} object.
     */
    public File writeArray(final String path, final String filename,
            final Array vals) {
        final File f = createFile(path, filename, WorkflowSlot.FILEIO);
        try {
            int i;
            try (PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)))) {
                final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                        Locale.US);
                df.applyPattern("0.0000");
                // double minThreshold = 0.000;
                final IndexIterator ii = vals.getIndexIterator();
                i = 0;
                while (ii.hasNext()) {
                    final double value = ii.getDoubleNext();
                    // if (value >= minThreshold) {
                    // bw.append(i + this.fieldSeparator + df.format(value));
                    bw.append(df.format(value));
                    bw.println();
                    i++;
                    // }
                }
                bw.println();
                bw.flush();
            }
            log.info(i + " records written to file " + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * <p>writeArray2DWithHeader.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @param filename a {@link java.lang.String} object.
     * @param arr a {@link ucar.ma2.ArrayDouble.D2} object.
     * @param columnLabels an array of {@link java.lang.String} objects.
     * @return a {@link java.io.File} object.
     */
    public File writeArray2DWithHeader(final String path, final String filename,
            final ArrayDouble.D2 arr, final String[] columnLabels) {

        // }
        final boolean writeBlockNewline = Factory.getInstance().getConfiguration().
                getBoolean("csvwriter.writeBlockNewLine",
                        false);
        EvalTools.eqI(3, columnLabels.length, this);
        final File f = createFile(path, filename, WorkflowSlot.FILEIO);
        try {
            try (PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)))) {
                final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                        Locale.US);
                bw.write(StringTools.join(columnLabels, this.fieldSeparator));
                bw.println();
                for (int i = 0; i < arr.getShape()[0]; i++) {
                    for (int j = 0; j < arr.getShape()[1]; j++) {
                        bw.append(i + this.fieldSeparator + j + this.fieldSeparator
                                + df.format(arr.get(i, j)));
                        bw.println();
                    }

                    if (writeBlockNewline) {
                        bw.println();
                    }

                    bw.flush();
                }
                bw.flush();
            }
            log.info(arr.getShape()[0] + "*" + arr.getShape()[1]
                    + " records written to file " + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            
            log.warn(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * <p>writeArray2D.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @param filename a {@link java.lang.String} object.
     * @param arr a {@link ucar.ma2.ArrayDouble.D2} object.
     * @return a {@link java.io.File} object.
     */
    public File writeArray2D(final String path, final String filename,
            final ArrayDouble.D2 arr) {

        // }
        final boolean writeBlockNewline = Factory.getInstance().getConfiguration().
                getBoolean("csvwriter.writeBlockNewLine",
                        false);
        final File f = createFile(path, filename, WorkflowSlot.FILEIO);
        try {
            try (PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)))) {
                final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                        Locale.US);
                for (int i = 0; i < arr.getShape()[0]; i++) {
                    for (int j = 0; j < arr.getShape()[1]; j++) {
                        bw.append(i + this.fieldSeparator + j + this.fieldSeparator
                                + df.format(arr.get(i, j)));
                        bw.println();
                    }

                    if (writeBlockNewline) {
                        bw.println();
                    }

                    bw.flush();
                }
                bw.flush();
            }
            log.info(arr.getShape()[0] + "*" + arr.getShape()[1]
                    + " records written to file " + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            
            log.warn(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * <p>writeArray2DwithLabels.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @param filename a {@link java.lang.String} object.
     * @param arr a {@link ucar.ma2.ArrayDouble.D2} object.
     * @param labels a {@link ucar.ma2.ArrayChar.D2} object.
     * @param creator a {@link java.lang.Class} object.
     * @param slot a {@link cross.datastructures.workflow.WorkflowSlot} object.
     * @param date a {@link java.util.Date} object.
     * @param resources a {@link cross.datastructures.fragments.IFileFragment} object.
     * @return a {@link java.io.File} object.
     */
    public File writeArray2DwithLabels(final String path,
            final String filename, final ArrayDouble.D2 arr,
            final ArrayChar.D2 labels, final Class<?> creator,
            final WorkflowSlot slot, final Date date,
            final IFileFragment... resources) {
        final File f = createFile(path, filename, slot);
        createFile(f, slot);
        final boolean useFullPath = Factory.getInstance().getConfiguration().
                getBoolean(this.getClass().getName() + ".useFullPathAsLabel",
                        false);
        try {
            f.createNewFile();
            try (PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)))) {
                final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                        Locale.US);
                df.applyPattern("0.0000");
                if (labels != null) {
                    final StringIterator si = labels.getStringIterator();
                    final StringBuffer sbuf = new StringBuffer();
                    sbuf.append(this.fieldSeparator);
                    while (si.hasNext()) {
                        String label = si.next();
                        if (new File(label).exists() && !useFullPath) {
                            label = new File(label).getName();
                        }
                        sbuf.append(label + this.fieldSeparator);
                    }
                    bw.write(sbuf.toString());
                    bw.println();
                }
                StringIterator si = null;
                if (labels != null) {
                    si = labels.getStringIterator();
                }
                for (int i = 0; i < arr.getShape()[0]; i++) {
                    if (si != null) {
                        if (si.hasNext()) {
                            if (useFullPath) {
                                bw.append(si.next() + this.fieldSeparator);
                            } else {
                                final String label = new File(si.next()).getName();
                                bw.append(label + this.fieldSeparator);
                            }
                        }
                    }
                    for (int j = 0; j < arr.getShape()[1]; j++) {
                        bw.append(df.format(arr.get(i, j)) + this.fieldSeparator);
                    }
                    bw.println();
                    bw.flush();
                }
                bw.flush();
            }
            log.info(arr.getShape()[0] + "*" + arr.getShape()[1]
                    + " records written to file " + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * <p>writeArrayListOfArrays.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @param filename a {@link java.lang.String} object.
     * @param values a {@link java.util.List} object.
     * @return a {@link java.io.File} object.
     */
    public File writeArrayListOfArrays(final String path,
            final String filename, final List<Array> values) {
        final boolean writeBlockNewline = Factory.getInstance().getConfiguration().
                getBoolean("csvwriter.writeBlockNewLine",
                        false);
        final int maxscans = values.size();
        final File f = createFile(path, filename, WorkflowSlot.FILEIO);
        try {
            try (PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)))) {
                final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                        Locale.US);
                df.applyPattern("0.0000");
                final double minThreshold = 0.001;
                for (int i = 0; i < maxscans; i++) {
                    log.info((i + 1) + "/" + maxscans);
                    final IndexIterator valind = values.get(i).getIndexIterator();
                    // mzToI.add(new LinkedHashMap<Double,Double>());

                    while (valind.hasNext()) {
                        final double value = valind.getDoubleNext();
                        if (value > minThreshold) {
                            bw.append(i + this.fieldSeparator + df.format(value));
                            bw.println();
                        }
                    }

                    if (writeBlockNewline) {
                        bw.println();
                    }

                    bw.flush();
                }
                bw.flush();
            }
            log.info(maxscans + " records written to file "
                    + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * <p>writeArrayListsOfArrays.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @param filename a {@link java.lang.String} object.
     * @param indices a {@link java.util.List} object.
     * @param values a {@link java.util.List} object.
     * @return a {@link java.io.File} object.
     */
    public File writeArrayListsOfArrays(final String path,
            final String filename, final List<Array> indices,
            final List<Array> values) {
        // Vector<LinkedHashMap<Double, Double>> mzToI = new
        // Vector<LinkedHashMap<Double,Double>>();
        // if(!checkEqualLength(indices))
        // return;
        // if(path==null || path.equals("")) {
        // }
        final boolean writeBlockNewline = Factory.getInstance().getConfiguration().
                getBoolean("csvwriter.writeBlockNewLine",
                        false);
        final int maxscans = indices.size();
        final File f = createFile(path, filename, WorkflowSlot.FILEIO);
        try {
            try (PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)))) {
                final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                        Locale.US);
                df.applyPattern("0.0000");
                final double minThreshold = 0.001;
                for (int i = 0; i < maxscans; i++) {
                    log.info((i + 1) + "/" + maxscans);
                    final IndexIterator inind = indices.get(i).getIndexIterator();
                    final IndexIterator valind = values.get(i).getIndexIterator();
                    // mzToI.add(new LinkedHashMap<Double,Double>());

                    while (inind.hasNext() && valind.hasNext()) {
                        final double index = inind.getDoubleNext();
                        final double value = valind.getDoubleNext();
                        if (value > minThreshold) {
                            bw.append(i + this.fieldSeparator + df.format(index)
                                    + this.fieldSeparator + df.format(value));
                            bw.println();
                        }
                    }

                    if (writeBlockNewline) {
                        bw.println();
                    }

                    bw.flush();
                }
                bw.flush();
            }
            log.info(maxscans + " records written to file "
                    + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            
            log.warn(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * <p>writeOneFilePerArray.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @param filename a {@link java.lang.String} object.
     * @param values a {@link java.util.List} object.
     * @return an array of {@link java.io.File} objects.
     */
    public File[] writeOneFilePerArray(final String path,
            final String filename, final List<Array> values) {
        int i = 0;
        final int cnt = values.size();
        final File[] files = new File[cnt];
        final int digits = ((int) Math.log10(cnt)) + 1;
        final String NUMBERFORMAT = "%0" + digits + "d";
        for (final Array a : values) {
            final StringBuilder sb = new StringBuilder();
            final Formatter formatter = new Formatter(sb);
            formatter.format(NUMBERFORMAT, i);
            final File f = writeArray(path, StringTools.removeFileExt(filename)
                    + "_" + sb.toString(), a);
            files[i++] = f;
        }
        return files;
    }

    /**
     * <p>writeStatsMap.</p>
     *
     * @param f a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param sm a {@link cross.datastructures.StatsMap} object.
     * @return a {@link java.io.File} object.
     */
    public File writeStatsMap(final IFileFragment f, final StatsMap sm) {
        final File file = new File(f.getUri());
        return writeStatsMap(file.getParent(), file.getName(), sm);
    }

    /**
     * <p>writeStatsMap.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @param filename a {@link java.lang.String} object.
     * @param sm a {@link cross.datastructures.StatsMap} object.
     * @return a {@link java.io.File} object.
     */
    public File writeStatsMap(final String path, final String filename,
            final StatsMap sm) {
        final File f = createFile(path, filename, WorkflowSlot.STATISTICS);
        try {
            int i;
            try (PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)))) {
                final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                        Locale.US);
                df.applyPattern("0.0000");
                final String header = sm.getLabel();
                bw.append("label\t");
                // if(header!=null) {
                // bw.append(header+this.fieldSeparator);
                // }
                final LinkedList<String> ll = new LinkedList<>(sm.keySet());
                Collections.sort(ll);
                for (final String str : ll) {
                    bw.append(str + this.fieldSeparator);
                }
                bw.println();
                bw.flush();
                i = 0;
                bw.append(header + this.fieldSeparator);
                for (final String str : ll) {
                    final double value = sm.get(str);
                    bw.append(df.format(value) + this.fieldSeparator);
                    i++;
                }
                bw.println();
                bw.flush();
            }
            log.info(i + " stats written to file " + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            
            log.warn(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * <p>writeStatsMaps.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @param filename a {@link java.lang.String} object.
     * @param sms a {@link cross.datastructures.StatsMap} object.
     * @return a {@link java.io.File} object.
     */
    public File writeStatsMaps(final String path, final String filename,
            final StatsMap... sms) {
        final File f = createFile(path, filename, WorkflowSlot.STATISTICS);
        try {
            try (PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)))) {
                final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                        Locale.US);
                df.applyPattern("0.0000");
                boolean first = true;
                for (final StatsMap sm : sms) {
                    final LinkedList<String> ll = new LinkedList<>(sm.keySet());
                    Collections.sort(ll);
                    if (first) {
                        for (final String str : ll) {
                            bw.append(str + this.fieldSeparator);
                        }
                        first = false;
                        bw.println();
                        bw.flush();
                    }
                    int i = 0;
                    for (final String str : ll) {
                        final double value = sm.get(str);
                        bw.append(df.format(value) + this.fieldSeparator);
                        i++;
                    }
                    bw.println();
                    bw.flush();
                    log.debug(i + " stats written to file "
                            + f.getAbsolutePath());
                }
            }
            log.info(sms.length + " records written to file "
                    + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * <p>writeTableByCols.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @param filename a {@link java.lang.String} object.
     * @param table a {@link java.util.List} object.
     * @param ws a {@link cross.datastructures.workflow.WorkflowSlot} object.
     * @return a {@link java.io.File} object.
     */
    public File writeTableByCols(final String path, final String filename,
            final List<List<String>> table, final WorkflowSlot ws) {
        final File f = createFile(path, filename, ws);
        try {
            // rows
            try (PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)))) {
                // rows
                for (int i = 0; i < table.get(0).size(); i++) {
                    // cols
                    final StringBuffer row = new StringBuffer();
                    for (int j = 0; j < table.size(); j++) {
                        row.append(table.get(j).get(i)
                                + ((j < table.size() - 1) ? this.fieldSeparator
                                : "\n"));
                    }
                    bw.write(row.toString());
                }
                bw.flush();
            }
            return f;
        } catch (final IOException ioe) {
            log.error(ioe.getLocalizedMessage());
        }
        return null;
    }

    /**
     * <p>writeTableByRows.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @param filename a {@link java.lang.String} object.
     * @param table a {@link java.util.List} object.
     * @param ws a {@link cross.datastructures.workflow.WorkflowSlot} object.
     * @return a {@link java.io.File} object.
     */
    public File writeTableByRows(final String path, final String filename,
            final List<List<String>> table, final WorkflowSlot ws) {
        final File f = createFile(path, filename, ws);
        try {
            // rows
            try (PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)))) {
                for (List<String> table1 : table) {
                    // columns
                    final StringBuffer row = new StringBuffer();
                    for (int j = 0; j < table1.size(); j++) {
                        row.append(table1.get(j) + ((j < table1.size() - 1) ? this.fieldSeparator : "\n"));
                    }
                    bw.write(row.toString());
                }
                bw.flush();
            }
            return f;
        } catch (final IOException ioe) {
            log.error(ioe.getLocalizedMessage());
        }
        return null;
    }

    /**
     * <p>createPrintWriter.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @param filename a {@link java.lang.String} object.
     * @param header a {@link java.util.List} object.
     * @param ws a {@link cross.datastructures.workflow.WorkflowSlot} object.
     * @return a {@link java.io.PrintWriter} object.
     */
    public PrintWriter createPrintWriter(final String path,
            final String filename, final List<String> header,
            final WorkflowSlot ws) {
        final File f = createFile(path, filename, ws);
        PrintWriter bw = null;
        try {
            bw = new PrintWriter(new BufferedWriter(new FileWriter(f)));
            // columns
            final StringBuffer row = new StringBuffer();
            for (int j = 0; j < header.size(); j++) {
                row.append(header.get(j)
                        + ((j < header.size() - 1) ? this.fieldSeparator
                        : "\n"));
            }
            bw.write(row.toString());
            return bw;
        } catch (final IOException ioe) {
            log.error(ioe.getLocalizedMessage());
            if (bw != null) {
                bw.close();
            }
        }
        return null;
    }

    /**
     * <p>writeLine.</p>
     *
     * @param pw a {@link java.io.PrintWriter} object.
     * @param line a {@link java.util.List} object.
     */
    public void writeLine(final PrintWriter pw, final List<String> line) {
        final StringBuffer row = new StringBuffer();
        for (int j = 0; j < line.size(); j++) {
            row.append(line.get(j)
                    + ((j < line.size() - 1) ? this.fieldSeparator : "\n"));
        }
        pw.write(row.toString());
    }
}
