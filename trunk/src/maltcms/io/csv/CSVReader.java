/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */

package maltcms.io.csv;

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
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;

import cross.Logging;
import cross.datastructures.tuple.Tuple2D;

/**
 * Configurable Reader for CSV-like files.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class CSVReader {

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
			final String[] keys = hm.keySet().toArray(new String[] {});
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

	private final Logger log = Logging.getLogger(this);

	private final Vector<String> skippedLines = new Vector<String>();

	/**
	 * @param line
	 * @return
	 */
	private Vector<String> addColumns(final String line) {
		final Vector<String> lv = new Vector<String>();
		this.log.debug("Adding columns of " + line);
		for (final String s : split(line, this.fieldSeparator)) {
			lv.add(s);
		}
		return lv;
	}

	public void addLine(final Vector<Vector<String>> rows, final String line) {
		this.log.debug("Adding line " + line);
		final Vector<String> lv = addColumns(line);
		rows.add(lv);
	}

	public HashMap<String, Vector<String>> getColumns(
	        final Tuple2D<Vector<Vector<String>>, Vector<String>> t) {
		final int columns = t.getFirst().size();
		Vector<String> headers = t.getSecond();
		if ((headers == null) || headers.isEmpty()) {
			this.log.debug("Headers empty, generating default ones");
			headers = new Vector<String>(columns);
			for (int i = 0; i < columns; i++) {
				headers.add("" + i);
			}
		}
		final HashMap<String, Vector<String>> hm = new HashMap<String, Vector<String>>();
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
					colv = new Vector<String>();
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

	public String getComment() {
		return this.comment;
	}

	public String getFieldSeparator() {
		return this.fieldSeparator;
	}

	public String getSkip() {
		return this.skip;
	}

	public Vector<String> getSkippedLines() {
		return this.skippedLines;
	}

	public boolean isFirstLineHeaders() {
		return this.firstLineHeaders;
	}

	public boolean isSkipCommentLines() {
		return this.skipCommentLines;
	}

	public Tuple2D<Vector<Vector<String>>, Vector<String>> read(
	        final InputStream is) {

		try {
			final BufferedReader br = new BufferedReader(new InputStreamReader(
			        is));
			String line = "";
			final Vector<Vector<String>> rows = new Vector<Vector<String>>();
			Vector<String> headers = new Vector<String>();
			this.skippedLines.clear();
			int cnt = 0;
			int content = 0;
			int skipped = 0;
			int comment1 = 0;

			while ((line = br.readLine()) != null) {
				this.log.debug("Parsing row: {}", cnt);
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
			br.close();
			this.log.debug("Read {} lines, skipped {}, comments {}",
			        new Object[] { cnt, skipped, comment1 });
			return new Tuple2D<Vector<Vector<String>>, Vector<String>>(rows,
			        headers);
		} catch (final ArrayIndexOutOfBoundsException aie) {
			this.log.error("Can not parse input, check file syntax!");
		} catch (final FileNotFoundException e) {
			this.log.error(e.getLocalizedMessage());
		} catch (final IOException e) {
			this.log.error(e.getLocalizedMessage());
		}
		return new Tuple2D<Vector<Vector<String>>, Vector<String>>(
		        new Vector<Vector<String>>(), new Vector<String>());
	}

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

		return new Tuple2D<Vector<Vector<String>>, Vector<String>>(
		        new Vector<Vector<String>>(), new Vector<String>());
	}

	public void setComment(final String comment1) {
		this.comment = comment1;
	}

	public void setFieldSeparator(final String sep) {
		this.fieldSeparator = sep;
	}

	public void setFirstLineHeaders(final boolean firstLineHeaders1) {
		this.firstLineHeaders = firstLineHeaders1;
	}

	public void setSkip(final String skip1) {
		this.skip = skip1;
	}

	public void setSkipCommentLines(final boolean skipCommentLines1) {
		this.skipCommentLines = skipCommentLines1;
	}

	public String[] split(final String s, final String split_token) {
		return s.split(split_token);
	}

}
