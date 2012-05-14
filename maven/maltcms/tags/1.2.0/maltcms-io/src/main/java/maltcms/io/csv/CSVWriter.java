/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.io.csv;

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

import maltcms.datastructures.array.IArrayD2Double;

import org.jdom.Element;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.ArrayChar.StringIterator;
import cross.Factory;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.NotImplementedException;
import cross.datastructures.tools.FileTools;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides various methods to write arrays and other data to csv files.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
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
    @Override
    public void appendXML(final Element e) {
        throw new NotImplementedException();
    }

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
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }

    public File writeAlignmentPath(final String path, final String filename,
            final List<Tuple2DI> map, final IArrayD2Double dist,
            final String refname, final String queryname, final String value,
            final String symbolicPath) {
        final File f = createFile(path, filename, WorkflowSlot.ALIGNMENT);
        // appendToWorkflow(f, WorkflowSlot.ALIGNMENT);
        log.info("Value of [0][0] = {}", dist.get(0, 0));
        try {
            final PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)));
            final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                    Locale.US);
            df.applyPattern("0.0000");
            bw.append(refname + this.fieldSeparator + queryname
                    + this.fieldSeparator + value + this.fieldSeparator
                    + "symbol");
            bw.println();
            log.info("Shape of matrix rows {} cols {}", dist.rows(), dist.
                    columns());
            for (int i = 0; i < map.size(); i++) {
                final Tuple2DI t = map.get(i);
                log.debug("Getting path value for {},{}", t.getFirst(), t.
                        getSecond());
                bw.append(t.getFirst()
                        + this.fieldSeparator
                        + t.getSecond()
                        + this.fieldSeparator
                        + dist.get(t.getFirst(), t.getSecond())
                        + ((symbolicPath != null) ? this.fieldSeparator
                        + symbolicPath.charAt(i) : ""));
                bw.println();
                // if (writeBlockNewline) {
                // bw.println();
                // }

                // bw.flush();
            }
            bw.flush();
            bw.close();
            log.info(map.size() + " records written to file "
                    + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public File writeArray(final String path, final String filename,
            final Array vals) {
        final File f = createFile(path, filename, WorkflowSlot.FILEIO);
        try {
            final PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)));
            final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                    Locale.US);
            df.applyPattern("0.0000");
            // double minThreshold = 0.000;
            final IndexIterator ii = vals.getIndexIterator();
            int i = 0;
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
            bw.close();
            log.info(i + " records written to file " + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    public File writeArray2D(final String path, final String filename,
            final ArrayDouble.D2 arr) {

        // }
        final boolean writeBlockNewline = Factory.getInstance().getConfiguration().
                getBoolean("csvwriter.writeBlockNewLine",
                false);
        final File f = createFile(path, filename, WorkflowSlot.FILEIO);
        try {
            final PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)));
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
            bw.close();
            log.info(arr.getShape()[0] + "*" + arr.getShape()[1]
                    + " records written to file " + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

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
            final PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)));
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
            bw.close();
            log.info(arr.getShape()[0] + "*" + arr.getShape()[1]
                    + " records written to file " + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    public File writeArrayListOfArrays(final String path,
            final String filename, final List<Array> values) {
        final boolean writeBlockNewline = Factory.getInstance().getConfiguration().
                getBoolean("csvwriter.writeBlockNewLine",
                false);
        final int maxscans = values.size();
        final File f = createFile(path, filename, WorkflowSlot.FILEIO);
        try {
            final PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)));
            final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                    Locale.US);
            df.applyPattern("0.0000");
            final double minThreshold = 0.001;
            for (int i = 0; i < maxscans; i++) {
                System.out.println((i + 1) + "/" + maxscans);
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
            bw.close();
            log.info(maxscans + " records written to file "
                    + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

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
            final PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)));
            final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                    Locale.US);
            df.applyPattern("0.0000");
            final double minThreshold = 0.001;
            for (int i = 0; i < maxscans; i++) {
                System.out.println((i + 1) + "/" + maxscans);
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
            bw.close();
            log.info(maxscans + " records written to file "
                    + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

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

    public File writeStatsMap(final IFileFragment f, final StatsMap sm) {
        final File file = new File(f.getAbsolutePath());
        return writeStatsMap(file.getParent(), file.getName(), sm);
    }

    public File writeStatsMap(final String path, final String filename,
            final StatsMap sm) {
        final File f = createFile(path, filename, WorkflowSlot.STATISTICS);
        try {
            final PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)));
            final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                    Locale.US);
            df.applyPattern("0.0000");
            final String header = sm.getLabel();
            bw.append("label\t");
            // if(header!=null) {
            // bw.append(header+this.fieldSeparator);
            // }
            final LinkedList<String> ll = new LinkedList<String>(sm.keySet());
            Collections.sort(ll);
            for (final String str : ll) {
                bw.append(str + this.fieldSeparator);
            }
            bw.println();
            bw.flush();
            int i = 0;
            bw.append(header + this.fieldSeparator);
            for (final String str : ll) {
                final double value = sm.get(str);
                bw.append(df.format(value) + this.fieldSeparator);
                i++;
            }
            bw.println();
            bw.flush();
            bw.close();
            log.info(i + " stats written to file " + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public File writeStatsMaps(final String path, final String filename,
            final StatsMap... sms) {
        final File f = createFile(path, filename, WorkflowSlot.STATISTICS);
        try {
            final PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)));
            final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                    Locale.US);
            df.applyPattern("0.0000");
            boolean first = true;
            for (final StatsMap sm : sms) {
                final LinkedList<String> ll = new LinkedList<String>(sm.keySet());
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
            bw.close();
            log.info(sms.length + " records written to file "
                    + f.getAbsolutePath());
            return f;
        } catch (final IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    public File writeTableByCols(final String path, final String filename,
            final List<List<String>> table, final WorkflowSlot ws) {
        final File f = createFile(path, filename, ws);
        try {
            final PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)));
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
            bw.close();
            return f;
        } catch (final IOException ioe) {
            log.error(ioe.getLocalizedMessage());
        }
        return null;
    }

    public File writeTableByRows(final String path, final String filename,
            final List<List<String>> table, final WorkflowSlot ws) {
        final File f = createFile(path, filename, ws);
        try {
            final PrintWriter bw = new PrintWriter(new BufferedWriter(
                    new FileWriter(f)));
            // rows
            for (int i = 0; i < table.size(); i++) {
                // columns
                final StringBuffer row = new StringBuffer();
                for (int j = 0; j < table.get(i).size(); j++) {
                    row.append(table.get(i).get(j)
                            + ((j < table.get(i).size() - 1) ? this.fieldSeparator
                            : "\n"));
                }
                bw.write(row.toString());
            }
            bw.flush();
            bw.close();
            return f;
        } catch (final IOException ioe) {
            log.error(ioe.getLocalizedMessage());
        }
        return null;
    }

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

    public void writeLine(final PrintWriter pw, final List<String> line) {
        final StringBuffer row = new StringBuffer();
        for (int j = 0; j < line.size(); j++) {
            row.append(line.get(j)
                    + ((j < line.size() - 1) ? this.fieldSeparator : "\n"));
        }
        pw.write(row.toString());
    }
}