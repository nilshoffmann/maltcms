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
package smueller.datastructure;

import cross.Factory;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2DI;
import cross.datastructures.workflow.IWorkflow;
import cross.tools.StringTools;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import maltcms.datastructures.array.ArrayFactory;
import maltcms.datastructures.fragments.PairwiseAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smueller.SymbolicRepresentationAlignment;
import smueller.alignment.Alignment;
import smueller.alignment.OptimalAlignmentVector;
import smueller.tools.SymbolConvert;
import ucar.ma2.Array;

// Speichern der optimalen Alignments in einem vom User gewählten Format
/**
 * <p>AlignmentOutput class.</p>
 *
 * @author Soeren Mueller
 * 
 */

public class AlignmentOutput {
    
    private static final Logger log = LoggerFactory.getLogger(AlignmentOutput.class);  

    private FileWriter outputStream = null;
    private FileWriter outputStream2 = null;
    private ArrayList<Tuple2DI> al;
    private PairwiseAlignment result = null;
    private IWorkflow iw;

    private void createPairwiseAlignment(final String a, final String b,
            final Alignment ali, final int[][] test, final double value) {
        this.al = getAsPairList(test);
        final PairwiseAlignment pa = new PairwiseAlignment();
        pa.setFileFragments(ali.getRef(), ali.getQuery(), this.getClass());
        pa.setNumberOfScansReference(a.length());
        pa.setNumberOfScansQuery(b.length());
        final double[][] matrx = ali.getMatrix();
        // log.info(Arrays.deepToString(matrx));
        log.info("MATRX {}x{}", matrx.length,
                matrx[0].length);
        EvalTools.notNull(matrx, this);
        final ArrayFactory f = Factory.getInstance().getObjectFactory()
                .instantiate(ArrayFactory.class);
        pa.setAlignment(f.create(Array.factory(matrx)));
        final double[][] pwdm = ali.getPairwiseDistance();
        // log.info(Arrays.deepToString(pwdm));
        log.info("PWDM {}x{}", pwdm.length, pwdm[0].length);
        EvalTools.notNull(pwdm, this);
        pa.setPairwiseDistances(f.create(Array.factory(pwdm)));
        pa.setIsMinimizing(true);
        pa.setResult(value);
        // pa.
        // IFileFragment ff = FragmentTools.createFragment(ali.getRef(),
        // ali.getQuery(), this.getClass());

        pa.setPath(this.al);
        // maltcms.tools.PathTools.getFragments(pa.provideFileFragment(), al);

        // ArrayDouble.D0 distance = new ArrayDouble.D0();
        // distance.set(matrix[(a.length() - 1)][(b.length() - 1)]);
        // IVariableFragment dist = new VariableFragment(ff, "distance");
        // dist.setArray(distance);
        // FragmentTools.createString(ff, "array_comp",
        // "custom_affine_gap_cost");
        // MaltcmsTools.createString(ff, "query_file",ali.getQuery() );
        this.result = pa;
    }

    private int[][] erstelleGrossenPfad(final String a, final String b,
            final String c, final String d, final int aoriglen,
            final int boriglen) {
        final int fenstergr = SymbolicRepresentationAlignment.getFenstergr();
        final int u = a.length();
        final int v = b.length();
        int i = 0;
        int j = 0;
        int y = 0;
        int z = 0;
        log.info(a);
        log.info(b);
        final int[][] pfad = new int[aoriglen][boriglen];
        System.out
                .println("Grosser Pfad " + pfad.length + " " + pfad[0].length);
        try {
            while ((y < (u)) && (z < (v))) {

                if (a.charAt(y) == '-') {
                    y++;
                    z++;
                    // i++;
                    // if(i<(aoriglen)) {
                    for (int fr = 0; fr < fenstergr; fr++) {
                        // log.info("VGAP at ["+i+"]["+j+"]");
                        if (i < aoriglen - 1) {
                            pfad[i][j] = 1;
                            i++;
                        }
                    }
                    // }

                } else if (b.charAt(z) == '-') {
                    y++;
                    z++;
                    // j++;
                    for (int fr = 0; fr < fenstergr; fr++) {
                        // log.info("HGAP at ["+i+"]["+j+"]");
                        if (j < boriglen - 1) {
                            pfad[i][j] = 1;
                            j++;
                        }
                    }

                } else if ((a.charAt(y) != '-') && (b.charAt(z) != '-')) {
                    y++;
                    z++;
                    // i++;
                    // j++;
                    for (int fr = 0; fr < fenstergr; fr++) {
                        pfad[i][j] = 1;
                        // log.info("MATCH at ["+i+"]["+j+"]");
                        if (i < aoriglen - 1) {
                            i++;
                        }
                        if (j < boriglen - 1) {
                            j++;
                        }
                    }

                } else {
                    log.info("Uncaught case!");
                }
            }
        } catch (final ArrayIndexOutOfBoundsException aae) {
            log.info("Caught exception i=" + i + " j=" + j);
        }
        if (i < aoriglen - 1) {
            for (; i < aoriglen; i++) {
                pfad[i][j] = 1;
            }
        }
        if (j < boriglen - 1) {
            for (; j < boriglen; j++) {
                pfad[i][j] = 1;
            }
        }
        log.info("i=" + i + " j=" + j);
        return pfad;

    }

    // Backtracking Pfad visualisieren
    private int[][] erstellepfad(final String a, final String b,
            final String c, final String d) {
        final int u = c.length();
        final int v = d.length();
        int i = 0;
        int j = 0;
        int y = 0;
        int z = 0;
        final int[][] pfad = new int[u][v];
        while ((i < u - 1) || (j < v - 1)) {
            if (a.charAt(y) == '-') {
                y++;
                z++;
                i++;
                pfad[i][j] = 1;
            } else if (b.charAt(z) == '-') {
                y++;
                z++;
                j++;
                pfad[i][j] = 1;
            } else if ((a.charAt(y) != '-') && (b.charAt(z) != '-')) {
                y++;
                z++;
                i++;
                j++;
                pfad[i][j] = 1;
            }
        }
        log.info("i:" + i);
        log.info("j:" + j);
        return pfad;

    }

    /**
     * <p>Getter for the field <code>al</code>.</p>
     *
     * @return a {@link java.util.ArrayList} object.
     */
    public ArrayList<Tuple2DI> getAl() {
        return this.al;
    }

    /**
     * <p>getAsPairList.</p>
     *
     * @param path an array of int.
     * @return a {@link java.util.ArrayList} object.
     */
    public ArrayList<Tuple2DI> getAsPairList(final int[][] path) {
        this.al = new ArrayList<>(path.length);
        for (int i = 0; i < path.length; i++) {
            for (int j = 0; j < path[0].length; j++) {
                if (path[i][j] == 1) {
                    this.al.add(new Tuple2DI(i, j));
                }
            }
        }
        return this.al;
    }

    /**
     * <p>Getter for the field <code>result</code>.</p>
     *
     * @return a {@link maltcms.datastructures.fragments.PairwiseAlignment} object.
     */
    public PairwiseAlignment getResult() {
        return this.result;
    }

    // Speichern der Files
    /**
     * <p>writefile.</p>
     *
     * @param a
     * @param all
     * @param alnumber
     * @param ali
     * @param aoriglen
     * @param b
     * @param boriglen
     * @param all a {@link java.util.Vector} object.
     * @param matrix an array of double.
     * @param format a {@link java.lang.String} object.
     * @param dima an array of double.
     * @param location a {@link java.lang.String} object.
     * @param alnumber a int.
     * @param ali a {@link smueller.alignment.Alignment} object.
     * @param aoriglen a int.
     * @param boriglen a int.
     * @param iw a {@link cross.datastructures.workflow.IWorkflow} object.
     */
    public void writefile(final String a, final String b,
            final Vector<OptimalAlignmentVector> all, final double[][] matrix,
            final String format, final double[][] dima, final String location,
            final int alnumber, final Alignment ali, final int aoriglen,
            final int boriglen, final IWorkflow iw) {
        this.iw = iw;
        Date datum;
        datum = new Date();
        final Locale loc = new Locale(location);
        Locale.setDefault(loc);
        final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_HH-mm",
                Locale.getDefault());
        final String localeString = sdf.format(datum);
        int identity = 0;
        int similarity = 0;
        int gaps = 0;
        String str1 = "";
        String str2 = "";
        int end1 = 0;
        int end2 = 0;
        int start1 = 1;
        int start2 = 1;
        final String refname = StringTools
                .removeFileExt(ali.getRef().getName());
        final String queryname = StringTools.removeFileExt(ali.getQuery()
                .getName());
        final String refloc = ali.getRef().getUri().toString();
        final String queryloc = ali.getQuery().getUri().toString();

        if (format.equals("txt")) {
            try {
                final File out = new File(iw.getOutputDirectory(this), refname
                        + "-vs-" + queryname + ".txt");
                this.outputStream = new FileWriter(out);
                this.outputStream.write(refloc + " in Stringrep:");
                this.outputStream.write(a);
                this.outputStream.write("\n");
                this.outputStream.write("\n");
                this.outputStream.write(queryloc + " in Stringrep:");
                this.outputStream.write(b);
                this.outputStream.write("\n");
                this.outputStream.write("\n");
                this.outputStream
                        .write("Die zur Datenreduktion verwendete Fenstergroesse betrug:"
                                + SymbolicRepresentationAlignment
                                .getFenstergr());
                this.outputStream.write("\n");
                this.outputStream.write("Die Alphabetgroesse betraegt:"
                        + SymbolicRepresentationAlignment.getAlphabetgr());
                this.outputStream.write("\n");
                this.outputStream.write("Es existieren " + all.size()
                        + " optimale Alignments mit Distanz: "
                        + matrix[(a.length() - 1)][(b.length() - 1)]);
                this.outputStream.write("\n");
                this.outputStream.write("\n");
                this.outputStream.write("\n");
                for (int p = 0; p < all.size(); p++) {
                    this.outputStream.write("Alignment Nummer: " + (p + 1));
                    this.outputStream.write("\n");
                    final OptimalAlignmentVector holeAlignment = all.get(p);
                    for (int q = holeAlignment.countAlChars() - 1; q >= 0; q--) {
                        str1 += holeAlignment.getCharPair(q).getA();
                        str2 += holeAlignment.getCharPair(q).getB();
                    }
                    this.outputStream.write(str1);
                    this.outputStream.write("\n");
                    for (int c = 0; c < str1.length(); c++) {

                        final char v = str1.charAt(c);
                        final char w = str2.charAt(c);
                        if ((v != '_') && (w != '_')) {
                            this.outputStream.write("|");
                        } else {
                            this.outputStream.write(".");
                        }

                    }
                    final int[][] test = erstelleGrossenPfad(str1, str2, a, b,
                            aoriglen, boriglen);
                    createPairwiseAlignment(a, b, ali, test,
                            matrix[(a.length() - 1)][(b.length() - 1)]);
                    this.outputStream.write("\n");
                    this.outputStream.write(str2);
                    this.outputStream.write("\n");
                    this.outputStream.write("\n");
                    str1 = "";
                    str2 = "";
                }

                this.outputStream.close();
            } catch (final IOException e) {
                e.printStackTrace(System.err);
                // log.warn("IOException");
            }
        }
        if (format.equals("fasta")) {
            try {
                final File out = new File(iw.getOutputDirectory(this), refname
                        + "-vs-" + queryname + ".fasta");
                this.outputStream = new FileWriter(out);
                for (int p = 0; p < all.size(); p++) {
                    this.outputStream.write(">" + refloc
                            + " of optimal Alignment  " + (p + 1));
                    this.outputStream.write("\n");
                    final OptimalAlignmentVector holeAlignment = all.get(p);
                    for (int q = holeAlignment.countAlChars() - 1; q >= 0; q--) {
                        str1 += holeAlignment.getCharPair(q).getA();
                        str2 += holeAlignment.getCharPair(q).getB();
                    }
                    final int[][] test = erstelleGrossenPfad(str1, str2, a, b,
                            aoriglen, boriglen);
                    createPairwiseAlignment(a, b, ali, test,
                            matrix[(a.length() - 1)][(b.length() - 1)]);
                    this.outputStream.write(str1);
                    this.outputStream.write("\n");
                    this.outputStream.write(">" + queryloc
                            + " of optimal Alignment  " + (p + 1));
                    this.outputStream.write("\n");
                    this.outputStream.write(str2);
                    this.outputStream.write("\n");
                    this.outputStream.write("\n");
                    str1 = "";
                    str2 = "";
                }

                this.outputStream.close();
            } catch (final IOException e) {
                e.printStackTrace(System.err);
                // log.warn("IOException");
            }
        }
        if (format.equals("pair")) {
            try {
                final File out = new File(iw.getOutputDirectory(this), refname
                        + "-vs-" + queryname + alnumber + ".pair");
                this.outputStream = new FileWriter(out);
                final OptimalAlignmentVector holeAlignment = all.get(alnumber);
                for (int q = holeAlignment.countAlChars() - 1; q >= 0; q--) {
                    str1 += holeAlignment.getCharPair(q).getA();
                    str2 += holeAlignment.getCharPair(q).getB();
                    if (holeAlignment.getCharPair(q).getA() == (holeAlignment
                            .getCharPair(q).getB())) {
                        identity++;
                    } else if ((holeAlignment.getCharPair(q).getA() != '-')
                            && (holeAlignment.getCharPair(q).getB() != '-')) {
                        similarity++;
                    } else if (holeAlignment.getCharPair(q).getA() == '-') {
                        gaps++;
                    }

                }
                similarity += identity;

                this.outputStream
                        .write("########################################");
                this.outputStream.write("\n");
                this.outputStream.write("# Program: maltcms-red");
                this.outputStream.write("\n");
                this.outputStream.write("# Rundate:  " + localeString);
                this.outputStream.write("\n");
                this.outputStream.write("# Report_file:  " + "pairalignment"
                        + alnumber + ".txt");
                this.outputStream.write("\n");
                this.outputStream
                        .write("########################################");
                this.outputStream.write("\n");
                this.outputStream
                        .write("#=======================================");
                this.outputStream.write("\n");
                this.outputStream.write("#");
                this.outputStream.write("\n");
                this.outputStream.write("# Aligned_sequences: 2");
                this.outputStream.write("\n");
                this.outputStream.write("# 1: " + refloc);
                this.outputStream.write("\n");
                this.outputStream.write("# 2: " + queryloc);
                this.outputStream.write("\n");
                this.outputStream.write("# Matrix: dynamic computation");
                this.outputStream.write("\n");
                this.outputStream.write("# Gap_penalty: "
                        + SymbolicRepresentationAlignment.getGapinit());
                this.outputStream.write("\n");
                this.outputStream
                        .write("# Extend_penalty: based on char, look up in txt");
                this.outputStream.write("\n");
                this.outputStream.write("#");
                this.outputStream.write("\n");
                this.outputStream.write("# Length: "
                        + Math.max(a.length() - 1, b.length() - 1));
                this.outputStream.write("\n");
                this.outputStream.write("# Identity:   "
                        + identity
                        + "/"
                        + Math.max(a.length() - 1, b.length() - 1)
                        + "("
                        + ((double) identity / (double) Math.max(
                                a.length() - 1, b.length() - 1)) * 100 + "%)");
                this.outputStream.write("\n");
                this.outputStream.write("# Similarity: "
                        + similarity
                        + "/"
                        + Math.max(a.length() - 1, b.length() - 1)
                        + "("
                        + ((double) similarity / (double) Math.max(
                                a.length() - 1, b.length() - 1)) * 100 + "%)");
                this.outputStream.write("\n");
                this.outputStream.write("# Gaps:       "
                        + gaps
                        + "/"
                        + Math.max(a.length() - 1, b.length() - 1)
                        + "("
                        + ((double) gaps / (double) Math.max(a.length() - 1, b
                                .length() - 1)) * 100 + "%)");
                this.outputStream.write("\n");
                this.outputStream.write("# Distance: "
                        + matrix[(a.length() - 1)][(b.length() - 1)]);
                this.outputStream.write("\n");
                this.outputStream.write("#");
                this.outputStream.write("\n");
                this.outputStream.write("#");
                this.outputStream.write("\n");
                this.outputStream
                        .write("#=======================================");
                this.outputStream.write("\n");
                this.outputStream.write("\n");
                // FileTools.prependDefaultDirs("pairlist.cdf",
                // SymbolicRepresentationAlignment.class).getAbsolutePath()
                final int[][] test = erstelleGrossenPfad(str1, str2, a, b,
                        aoriglen, boriglen);
                createPairwiseAlignment(a, b, ali, test,
                        matrix[(a.length() - 1)][(b.length() - 1)]);

                final String[] str1ar = new String[str1.length() / 50 + 1];
                for (int s = 0; s <= (double) str1.length() / 50; s++) {
                    if (str1.length() > (s + 1) * 50) {
                        str1ar[s] = str1.substring(s * 50, (s + 1) * 50);
                    } else {
                        str1ar[s] = str1.substring(s * 50, str2.length());
                    }
                }
                final String[] str2ar = new String[(str2.length() / 50) + 1];
                for (int g = 0; g <= (double) str2.length() / 50; g++) {
                    if (str2.length() > (g + 1) * 50) {
                        str2ar[g] = str2.substring(g * 50, (g + 1) * 50);
                    } else {
                        str2ar[g] = str2.substring(g * 50, str2.length());
                    }
                }
                EvalTools.notNull(str2ar, this);
                EvalTools.notNull(str1ar, this);
                for (int y = 0; y < str2ar.length; y++) {
                    end1 += str1ar[y].length()
                            - smueller.tools.ArrayTools.countChar(str1ar[y],
                                    '-');
                    if (start1 < 10) {
                        this.outputStream.write("Sequ1name       " + start1
                                + " " + str1ar[y] + "     " + end1);
                    } else if (start1 < 100) {
                        this.outputStream.write("Sequ1name      " + start1
                                + " " + str1ar[y] + "     " + end1);
                    } else if (start1 < 1000) {
                        this.outputStream.write("Sequ1name     " + start1 + " "
                                + str1ar[y] + "     " + end1);
                    }
                    this.outputStream.write("\n");
                    this.outputStream.write("                  ");
                    start1 = end1 + 1;
                    for (int c = 0; c < str2ar[y].length(); c++) {

                        final char v = str1ar[y].charAt(c);
                        final char w = str2ar[y].charAt(c);
                        if ((v != '-') && (w != '-')) {
                            this.outputStream.write("|");
                        } else {
                            this.outputStream.write(" ");
                        }

                    }
                    this.outputStream.write("\n");
                    end2 += str2ar[y].length()
                            - smueller.tools.ArrayTools.countChar(str2ar[y],
                                    '-');
                    if (start2 < 10) {
                        this.outputStream.write("Sequ2name       " + start2
                                + " " + str2ar[y] + "     " + end2);
                    } else if (start2 < 100) {
                        this.outputStream.write("Sequ2name      " + start2
                                + " " + str2ar[y] + "     " + end2);
                    } else if (start2 < 1000) {
                        this.outputStream.write("Sequ2name     " + start2 + " "
                                + str2ar[y] + "     " + end2);
                    }
                    this.outputStream.write("\n");
                    this.outputStream.write("\n");
                    start2 = end2 + 1;
                }

                this.outputStream.write("\n");
                this.outputStream
                        .write("#---------------------------------------");
                this.outputStream.write("\n");
                this.outputStream
                        .write("#---------------------------------------");
                this.outputStream.write("\n");

                this.outputStream.close();
                final File out2 = new File(iw.getOutputDirectory(this), refname
                        + "-vs-" + queryname + "-pairalignment" + alnumber
                        + ".txt");
                this.outputStream2 = new FileWriter(out2);
                this.outputStream2.write("Distance Matrix:");
                this.outputStream2.write("\n");
                this.outputStream2.write("\n");
                for (int k = 0; k < SymbolConvert.getAlphabet().length(); k++) {
                    this.outputStream2.write("	"
                            + SymbolConvert.getAlphabet().charAt(k));
                }
                this.outputStream2.write("\n");
                // for (int i = 0; i < SymbolicRepresentationAlignment
                // .getDistmatrix().getDistmat().length; i++) {
                // outputStream2.write(SymbolConvert.getAlphabet().charAt(i)
                // + "	");
                // for (int j = 0; j < SymbolicRepresentationAlignment
                // .getDistmatrix().getDistmat().length; j++) {
                // outputStream2.write(SymbolicRepresentationAlignment
                // .getDistmatrix().getDistmat()[i][j]
                // + "	");
                // }
                // outputStream2.write("\n");
                // }
                this.outputStream2.write("\n");
                this.outputStream2.write("Graph:");
                this.outputStream2.write("\n");
                this.outputStream2.write(" ");
                for (int k = 0; k < b.length(); k++) {
                    this.outputStream2.write("	" + b.charAt(k));
                }
                this.outputStream2.write("\n");
                for (int i = 0; i < a.length(); i++) {
                    this.outputStream2.write(a.charAt(i) + "");
                    for (int j = 0; j < b.length(); j++) {
                        this.outputStream2.write("	"
                                + SymbolicRepresentationAlignment.getAl()
                                .getMatrix()[i][j]);
                    }
                    this.outputStream2.write("\n");
                }
                this.outputStream2.write("\n");

                this.outputStream2.write("Backtracking Path:");

                this.outputStream2.write("\n");
                this.outputStream2.write(" ");

                for (int k = 0; k < b.length(); k++) {
                    this.outputStream2.write(" " + b.charAt(k));
                }
                this.outputStream2.write("\n");
                for (int i = 0; i < a.length(); i++) {
                    this.outputStream2.write(a.charAt(i) + "");
                    for (int j = 0; j < b.length(); j++) {
                        this.outputStream2.write(" " + test[i][j]);
                    }
                    this.outputStream2.write("\n");
                }
                this.outputStream2.write("\n");

                this.outputStream2.close();

            } catch (final IOException e) {
                e.printStackTrace(System.err);
                // log.warn("IOException");
            }
        }
    }
}
