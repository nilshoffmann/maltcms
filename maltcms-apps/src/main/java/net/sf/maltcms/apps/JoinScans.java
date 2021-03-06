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
package net.sf.maltcms.apps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * JoinScans class.</p>
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class JoinScans {

    /**
     * <p>
     * createConcatenatedFile.</p>
     *
     * @param filename a {@link java.lang.String} object.
     * @param filenames an array of {@link java.lang.String} objects.
     * @param headers an array of {@link java.lang.String} objects.
     * @param keys a {@link java.util.HashSet} object.
     * @param hm a {@link java.util.HashMap} object.
     */
    public static void createConcatenatedFile(final String filename,
            final String[] filenames, final String[] headers,
            final HashSet<Integer> keys,
            final HashMap<String, HashMap<Integer, Integer>> hm) {
        final File dir = new File(filenames[0]).getParentFile();
        final File f = new File(dir, filename);
        log.info(f.getAbsolutePath());
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(f);
            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(fos))) {
                final StringBuffer sb = new StringBuffer();
                for (int i = 0; i < headers.length; i++) {
                    log.info(headers[i]);
                    if (i == 0) {
                        sb.append(headers[0].split("\t")[0]);
                        sb.append("\t" + headers[0].split("\t")[1]);
                    } else {
                        sb.append("\t" + headers[i].split("\t")[1]);
                    }
                }
                bw.write(sb.toString());
                bw.newLine();
                final ArrayList<Integer> al = new ArrayList<>(keys);
                Collections.sort(al);
                for (final Integer i : al) {
                    final StringBuffer lb = new StringBuffer();
                    lb.append(i);
                    for (String filename1 : filenames) {
                        final HashMap<Integer, Integer> map = hm.get(filename1);
                        final int val = -1;
                        if (map.containsKey(i)) {
                            lb.append("\t" + map.get(i));
                        } else {
                            lb.append("\t" + val);
                        }
                    }
                    bw.write(lb.toString());
                    bw.newLine();
                }
                bw.flush();
            }
        } catch (final FileNotFoundException e) {
            
            log.warn(e.getLocalizedMessage());
        } catch (final IOException e) {
            
            log.warn(e.getLocalizedMessage());
        }
    }

    /**
     * <p>
     * createReducedFile.</p>
     *
     * @param originalFilename a {@link java.lang.String} object.
     * @param header a {@link java.lang.String} object.
     * @param keys a {@link java.util.Set} object.
     * @param map a {@link java.util.HashMap} object.
     */
    public static void createReducedFile(final String originalFilename,
            final String header, final Set<Integer> keys,
            final HashMap<Integer, Integer> map) {
        final File f = new File(originalFilename);
        final String name = f.getName().substring(0, f.getName().length() - 4)
                + "_reduced.txt";
        log.info(name);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(new File(f.getParentFile(), name));
            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(fos))) {
                bw.write(header);
                bw.newLine();
                final ArrayList<Integer> al = new ArrayList<>(keys);
                Collections.sort(al);
                for (final Integer i : al) {
                    bw.write(i + "\t" + map.get(i));
                    bw.newLine();
                }
                bw.flush();
            }
        } catch (final FileNotFoundException e) {
            
            log.warn(e.getLocalizedMessage());
        } catch (final IOException e) {
            
            log.warn(e.getLocalizedMessage());
        }

    }

    /**
     * <p>
     * main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(final String[] args) {
        // arguments are files to join
        final HashSet<Integer> allPeaks = new HashSet<>();
        final HashSet<Integer> retainedPeaks = new HashSet<>();
        final HashMap<String, HashMap<Integer, Integer>> fileRefToQueryMap = new HashMap<>();
        int filecounter = 0;
        final String[] headers = new String[args.length];
        for (final String s : args) {
            try {
                final FileInputStream fis = new FileInputStream(s);
                final BufferedReader br = new BufferedReader(
                        new InputStreamReader(fis));
                String line = "";
                int linecounter = 0;
                final HashMap<Integer, Integer> scanToScan = new HashMap<>();
                fileRefToQueryMap.put(s, scanToScan);
                while ((line = br.readLine()) != null) {
                    if (linecounter == 0) {// header information
                        headers[filecounter] = line;
                    } else {
                        final String[] parts = line.split("\t");
                        if (parts.length != 2) {
                            System.err
                                    .println("Split length is wrong (should be 2), check input!");
                            System.exit(-1);
                        } else {
                            final Integer lhs = Integer.parseInt(parts[0]);
                            final Integer rhs = Integer.parseInt(parts[1]);
                            scanToScan.put(lhs, rhs);
                            allPeaks.add(lhs);
                        }
                    }
                    linecounter++;
                }
                if (filecounter == 0) {
                    retainedPeaks.addAll(scanToScan.keySet());
                } else {
                    retainedPeaks.retainAll(scanToScan.keySet());
                }
                filecounter++;
                JoinScans.printMap(s, scanToScan);
            } catch (final FileNotFoundException e) {
   
                log.warn(e.getLocalizedMessage());
            } catch (final IOException e) {
   
                log.warn(e.getLocalizedMessage());
            }
            JoinScans.printSet(retainedPeaks);

        }
        int i = 0;
        for (final String s : args) {
            JoinScans.createReducedFile(s, headers[i], retainedPeaks,
                    fileRefToQueryMap.get(s));
            i++;
        }

        JoinScans.createConcatenatedFile("peak_map_matrix.txt", args, headers,
                allPeaks, fileRefToQueryMap);
        JoinScans.createConcatenatedFile("peak_map_matrix_cons.txt", args,
                headers, retainedPeaks, fileRefToQueryMap);

    }

    /**
     * <p>
     * printMap.</p>
     *
     * @param filename a {@link java.lang.String} object.
     * @param map a {@link java.util.HashMap} object.
     */
    public static void printMap(final String filename,
            final HashMap<Integer, Integer> map) {
        log.info("Map for " + filename);
        final Set<Integer> s = map.keySet();
        final ArrayList<Integer> al = new ArrayList<>(s);
        Collections.sort(al);
        for (final Integer i : al) {
            log.info(i + ":" + map.get(i));
        }
    }

    /**
     * <p>
     * printSet.</p>
     *
     * @param set a {@link java.util.Set} object.
     */
    public static void printSet(final Set<Integer> set) {
        final ArrayList<Integer> al = new ArrayList<>(set);
        Collections.sort(al);
        for (final Integer i : al) {
            log.info("{}", i);
        }
    }
}
