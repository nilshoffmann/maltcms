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
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.util.Arrays.sort;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import javax.swing.table.DefaultTableModel;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Configurable Reader for CSV-like files.
 *
 * @author Nils Hoffmann
 * 
 */

public class CSVReader {
    
    private static final org.slf4j.Logger log = getLogger(CSVReader.class);

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(final String[] args) {
        final var jf = new JFrame();
        jf.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final var jtp = new JTabbedPane();
        jf.add(jtp);
        for (final String s : args) {
            final var cr = new CSVReader();
            final var t = cr
                    .read(s);
            final var hm = cr.getColumns(t);
            final var dtm = new DefaultTableModel();
            final var keys = hm.keySet().toArray(new String[]{});
            sort(keys);
            for (final String key : keys) {
                dtm.addColumn(key, hm.get(key));
            }
            final var jp = new JPanel();
            final var jt = new JTable(dtm);
            jp.add(new JScrollPane(jt), CENTER);
            final var jl = new JList(cr.getSkippedLines());
            jp.add(new JScrollPane(jl), EAST);
            jtp.addTab(s, jp);
        }
        invokeLater(() -> {
            jf.setVisible(true);
            jf.pack();
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
        final var lv = new Vector<String>();
        log.debug("Adding columns of " + line);
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
        log.debug("Adding line " + line);
        final var lv = addColumns(line);
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
        final var columns = t.getFirst().size();
        var headers = t.getSecond();
        if ((headers == null) || headers.isEmpty()) {
            log.debug("Headers empty, generating default ones");
            headers = new Vector<>(columns);
            for (var i = 0; i < columns; i++) {
                headers.add("" + i);
            }
        }
        final HashMap<String, Vector<String>> hm = new LinkedHashMap<>();
        var column = 0;
        var rows = 0;
        // proceed row-wise
        for (final Vector<String> v : t.getFirst()) {
            log.debug("Converting row {}", rows++);
            // process strings in row
            for (final String s : v) {
                log.debug(s);
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
        log.debug(hm.toString());
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
            try (var br = new BufferedReader(new InputStreamReader(
                    is))) {
                var line = "";
                rows = new Vector<>();
                headers = new Vector<>();
                this.skippedLines.clear();
                cnt = 0;
                var content = 0;
                skipped = 0;
                comment1 = 0;
                while ((line = br.readLine()) != null) {
                    log.debug("Parsing row: {}", cnt);
                    if (line.trim().isEmpty()) {
                        log.debug("Skipping empty line!");
                    } else {
                        if (line.startsWith(this.comment)) {
                            if (this.skipCommentLines) {
                                log.debug("Skipping comment row: {} = {}", cnt,
                                        line);
                                // skip to next line
                                comment1++;
                                cnt++;
                                continue;
                            } else {
                                // Add row if comments should not be skipped
                                log.debug("Adding row: {}", cnt);
                                addLine(rows, line);
                                cnt++;// increase counter only on added lines
                            }
                        } else {
                            // Skip row
                            if (line.startsWith(this.skip)) {
                                log.debug("Skipping row: {} = {}", cnt, line);
                                this.skippedLines.add(line);
                                skipped++;
                                cnt++;
                                continue;
                            }
                            // we haven't seen any content yet, so suspect the next line
                            // to contain column labels
                            if (this.firstLineHeaders && (content == 0)) {
                                log.debug("Adding headers: {}", cnt);
                                headers = addColumns(line);
                                cnt++;
                                this.firstLineHeaders = false;
                            } else {// add content row
                                log.debug("Adding row: {}", cnt);
                                addLine(rows, line);
                                content++;
                                cnt++;
                            }
                        }
                    }
                }
            }
            log.debug("Read {} lines, skipped {}, comments {}",
                    new Object[]{cnt, skipped, comment1});
            return new Tuple2D<>(rows,
                    headers);
        } catch (final ArrayIndexOutOfBoundsException aie) {
            log.error("Can not parse input, check file syntax!");
        } catch (final FileNotFoundException e) {
            log.error(e.getLocalizedMessage());
        } catch (final IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return new Tuple2D<>(
                new Vector<>(), new Vector<>());
    }

    /**
     * <p>read.</p>
     *
     * @param url a {@link java.lang.String} object.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public Tuple2D<Vector<Vector<String>>, Vector<String>> read(final String url) {
        if (new File(url).exists()) {
            log.info("Reading from file {}", url);
            FileInputStream fis;
            try {
                fis = new FileInputStream(url);
                return read(fis);
            } catch (final FileNotFoundException e) {
                log.error(e.getLocalizedMessage());
            }
        } else {
            final var is = this.getClass().getClassLoader()
                    .getResourceAsStream(url);
            if (is != null) {
                return read(is);
            } else {
                log.warn("Could not retrieve resource as stream: {}", url);
            }
        }

        return new Tuple2D<>(
                new Vector<>(), new Vector<>());
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
