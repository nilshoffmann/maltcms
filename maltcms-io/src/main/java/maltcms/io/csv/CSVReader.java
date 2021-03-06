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

import cross.datastructures.tuple.Tuple2D;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import lombok.extern.slf4j.Slf4j;

/**
 * Configurable Reader for CSV-like files.
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
public class CSVReader {

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(final String[] args) {
        final JFrame jf = new JFrame();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JTabbedPane jtp = new JTabbedPane();
        jf.add(jtp);
        for (final String s : args) {
            final CSVReader cr = new CSVReader();
            final Tuple2D<Vector<Vector<String>>, Vector<String>> t = cr
                    .read(s);
            final HashMap<String, Vector<String>> hm = cr.getColumns(t);
            final DefaultTableModel dtm = new DefaultTableModel();
            final String[] keys = hm.keySet().toArray(new String[]{});
            Arrays.sort(keys);
            for (final String key : keys) {
                dtm.addColumn(key, hm.get(key));
            }
            final JPanel jp = new JPanel();
            final JTable jt = new JTable(dtm);
            jp.add(new JScrollPane(jt), BorderLayout.CENTER);
            final JList jl = new JList(cr.getSkippedLines());
            jp.add(new JScrollPane(jl), BorderLayout.EAST);
            jtp.addTab(s, jp);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jf.setVisible(true);
                jf.pack();
            }
        });
    }
    private String fieldSeparator = "\t";
    private String comment = "#";
    private String skip = ">";
    private boolean skipCommentLines = true;
    private boolean firstLineHeaders = true;
    private final Vector<String> skippedLines = new Vector<>();

    /**
     * @param line
     * @return
     */
    private Vector<String> addColumns(final String line) {
        final Vector<String> lv = new Vector<>();
        this.log.debug("Adding columns of " + line);
        for (final String s : split(line, this.fieldSeparator)) {
            lv.add(s);
        }
        return lv;
    }

    /**
     * <p>addLine.</p>
     *
     * @param rows a {@link java.util.Vector} object.
     * @param line a {@link java.lang.String} object.
     */
    public void addLine(final Vector<Vector<String>> rows, final String line) {
        this.log.debug("Adding line " + line);
        final Vector<String> lv = addColumns(line);
        rows.add(lv);
    }

    /**
     * <p>getColumns.</p>
     *
     * @param t a {@link cross.datastructures.tuple.Tuple2D} object.
     * @return a {@link java.util.HashMap} object.
     */
    public HashMap<String, Vector<String>> getColumns(
            final Tuple2D<Vector<Vector<String>>, Vector<String>> t) {
        final int columns = t.getFirst().size();
        Vector<String> headers = t.getSecond();
        if ((headers == null) || headers.isEmpty()) {
            this.log.debug("Headers empty, generating default ones");
            headers = new Vector<>(columns);
            for (int i = 0; i < columns; i++) {
                headers.add("" + i);
            }
        }
        final HashMap<String, Vector<String>> hm = new LinkedHashMap<>();
        int column = 0;
        int rows = 0;
        // proceed row-wise
        for (final Vector<String> v : t.getFirst()) {
            this.log.debug("Converting row {}", rows++);
            // process strings in row
            for (final String s : v) {
                this.log.debug(s);
                Vector<String> colv = null;
                if (!hm.containsKey(headers.get(column))) {
                    colv = new Vector<>();
                    colv.add(s);
                    hm.put(headers.get(column), colv);
                } else {
                    colv = hm.get(headers.get(column));
                    colv.add(s);
                }
                column++;
            }
            column = 0;
        }
        this.log.debug(hm.toString());
        return hm;
    }

    /**
     * <p>Getter for the field <code>comment</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * <p>Getter for the field <code>fieldSeparator</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFieldSeparator() {
        return this.fieldSeparator;
    }

    /**
     * <p>Getter for the field <code>skip</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSkip() {
        return this.skip;
    }

    /**
     * <p>Getter for the field <code>skippedLines</code>.</p>
     *
     * @return a {@link java.util.Vector} object.
     */
    public Vector<String> getSkippedLines() {
        return this.skippedLines;
    }

    /**
     * <p>isFirstLineHeaders.</p>
     *
     * @return a boolean.
     */
    public boolean isFirstLineHeaders() {
        return this.firstLineHeaders;
    }

    /**
     * <p>isSkipCommentLines.</p>
     *
     * @return a boolean.
     */
    public boolean isSkipCommentLines() {
        return this.skipCommentLines;
    }

    /**
     * <p>read.</p>
     *
     * @param is a {@link java.io.InputStream} object.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public Tuple2D<Vector<Vector<String>>, Vector<String>> read(
            final InputStream is) {

        try {
            final Vector<Vector<String>> rows;
            Vector<String> headers;
            int cnt;
            int skipped;
            int comment1;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    is))) {
                String line = "";
                rows = new Vector<>();
                headers = new Vector<>();
                this.skippedLines.clear();
                cnt = 0;
                int content = 0;
                skipped = 0;
                comment1 = 0;
                while ((line = br.readLine()) != null) {
                    this.log.debug("Parsing row: {}", cnt);
                    if (line.trim().isEmpty()) {
                        this.log.debug("Skipping empty line!");
                    } else {
                        if (line.startsWith(this.comment)) {
                            if (this.skipCommentLines) {
                                this.log.debug("Skipping comment row: {} = {}", cnt,
                                        line);
                                // skip to next line
                                comment1++;
                                cnt++;
                                continue;
                            } else {
                                // Add row if comments should not be skipped
                                this.log.debug("Adding row: {}", cnt);
                                addLine(rows, line);
                                cnt++;// increase counter only on added lines
                            }
                        } else {
                            // Skip row
                            if (line.startsWith(this.skip)) {
                                this.log.debug("Skipping row: {} = {}", cnt, line);
                                this.skippedLines.add(line);
                                skipped++;
                                cnt++;
                                continue;
                            }
                            // we haven't seen any content yet, so suspect the next line
                            // to contain column labels
                            if (this.firstLineHeaders && (content == 0)) {
                                this.log.debug("Adding headers: {}", cnt);
                                headers = addColumns(line);
                                cnt++;
                                this.firstLineHeaders = false;
                            } else {// add content row
                                this.log.debug("Adding row: {}", cnt);
                                addLine(rows, line);
                                content++;
                                cnt++;
                            }
                        }
                    }
                }
            }
            this.log.debug("Read {} lines, skipped {}, comments {}",
                    new Object[]{cnt, skipped, comment1});
            return new Tuple2D<>(rows,
                    headers);
        } catch (final ArrayIndexOutOfBoundsException aie) {
            this.log.error("Can not parse input, check file syntax!");
        } catch (final FileNotFoundException e) {
            this.log.error(e.getLocalizedMessage());
        } catch (final IOException e) {
            this.log.error(e.getLocalizedMessage());
        }
        return new Tuple2D<>(
                new Vector<Vector<String>>(), new Vector<String>());
    }

    /**
     * <p>read.</p>
     *
     * @param url a {@link java.lang.String} object.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public Tuple2D<Vector<Vector<String>>, Vector<String>> read(final String url) {
        if (new File(url).exists()) {
            this.log.info("Reading from file {}", url);
            FileInputStream fis;
            try {
                fis = new FileInputStream(url);
                return read(fis);
            } catch (final FileNotFoundException e) {
                this.log.error(e.getLocalizedMessage());
            }
        } else {
            final InputStream is = this.getClass().getClassLoader()
                    .getResourceAsStream(url);
            if (is != null) {
                return read(is);
            } else {
                this.log.warn("Could not retrieve resource as stream: {}", url);
            }
        }

        return new Tuple2D<>(
                new Vector<Vector<String>>(), new Vector<String>());
    }

    /**
     * <p>Setter for the field <code>comment</code>.</p>
     *
     * @param comment1 a {@link java.lang.String} object.
     */
    public void setComment(final String comment1) {
        this.comment = comment1;
    }

    /**
     * <p>Setter for the field <code>fieldSeparator</code>.</p>
     *
     * @param sep a {@link java.lang.String} object.
     */
    public void setFieldSeparator(final String sep) {
        this.fieldSeparator = sep;
    }

    /**
     * <p>Setter for the field <code>firstLineHeaders</code>.</p>
     *
     * @param firstLineHeaders1 a boolean.
     */
    public void setFirstLineHeaders(final boolean firstLineHeaders1) {
        this.firstLineHeaders = firstLineHeaders1;
    }

    /**
     * <p>Setter for the field <code>skip</code>.</p>
     *
     * @param skip1 a {@link java.lang.String} object.
     */
    public void setSkip(final String skip1) {
        this.skip = skip1;
    }

    /**
     * <p>Setter for the field <code>skipCommentLines</code>.</p>
     *
     * @param skipCommentLines1 a boolean.
     */
    public void setSkipCommentLines(final boolean skipCommentLines1) {
        this.skipCommentLines = skipCommentLines1;
    }

    /**
     * <p>split.</p>
     *
     * @param s a {@link java.lang.String} object.
     * @param split_token a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] split(final String s, final String split_token) {
        return s.split(split_token);
    }
}
