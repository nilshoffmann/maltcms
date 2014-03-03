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
package smueller.alignment;

import cross.datastructures.fragments.IFileFragment;
import java.util.Vector;

/**
 *
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 *
 */
public class Alignment {

    /**
     *
     *
     *
     */
    protected IFileFragment ref,
        /**
         *
         *
         *
         */
        query;

    /**
     *
     *
     *
     */
    protected double[][] matrix;

    /**
     *
     *
     *
     */
    protected double[][] pwd;

    private double[][] kostenfunktion;

    // 3Dim-Array, welches das Backtracking und finden aller optimalen
    // Alignments erm�glicht
    /**
     *
     *
     *
     */
    protected int[][][] pathway;

    /**
     *
     *
     *
     */
    protected double x;

    /**
     *
     *
     *
     */
    protected double y;

    /**
     *
     *
     *
     */
    protected double z;

    /**
     *
     *
     *
     */
    protected String seq1;

    /**
     *
     *
     *
     */
    protected String seq2;

    int k = 0;

    int counter = 0;

    int alindex1 = 0;

    int alindex2 = 0;

    // Vektor, der alle optimalen Alignments enth�lt. Pro Alignment existiert
    // ein OAV, welcher Aligned Pair Vektoren enth�lt
    Vector<OptimalAlignmentVector> allalignments = new Vector<OptimalAlignmentVector>();

    OptimalAlignmentVector oav1 = new OptimalAlignmentVector();

    OptimalAlignmentVector oav2 = new OptimalAlignmentVector();

    /**
     *
     *
     *
     * @param ref1
     *
     * @param query1
     *
     */
    public Alignment(final IFileFragment ref1, final IFileFragment query1) {

        this.ref = ref1;

        this.query = query1;

    }

    // Gibt alle optimalen Alignments i.d. Konsole aus
    /**
     *
     *
     *
     */
    public void ausgabe() {

        String str1 = "";

        String str2 = "";

        System.out
            .println("Es existieren "
                + this.allalignments.size()
                + " optimale Alignments mit Distanz: "
                + this.matrix[(this.seq1.length() - 1)][(this.seq2
                .length() - 1)]);

        for (int p = 0; p < this.allalignments.size(); p++) {

            System.out.println("Alignment Nummer: " + (p + 1));

            final OptimalAlignmentVector holeAlignment = this.allalignments
                .get(p);

            for (int q = holeAlignment.countAlChars() - 1; q >= 0; q--) {

                str1 += holeAlignment.getCharPair(q).getA();

                str2 += holeAlignment.getCharPair(q).getB();

            }

            System.out.println(str1);

            System.out.println(str2);

            System.out.println("");

            str1 = "";

            str2 = "";

        }

    }

    // Buchstaben Zahlen zuordnen, um die Kostenmatrix ansprechen zu k�nnen
    /**
     *
     *
     *
     * @param a
     *
     * @return
     *
     */
    public int chartoNumber(final char a) {

        int numb = 0;

        if (a == 'a') {

            numb = 1;

        }

        if (a == 'b') {

            numb = 2;

        }

        if (a == 'c') {

            numb = 3;

        }

        if (a == 'd') {

            numb = 4;

        }

        if (a == 'e') {

            numb = 5;

        }

        if (a == 'f') {

            numb = 6;

        }

        if (a == 'g') {

            numb = 7;

        }

        if (a == 'h') {

            numb = 8;

        }

        if (a == 'i') {

            numb = 9;

        }

        if (a == 'k') {

            numb = 10;

        }

        if (a == '-') {

            numb = 0;

        }

        return numb;

    }

    // Klont den aktuellen OAV und und f�gt ihm den Vektor hinzu
    /**
     *
     *
     *
     */
    public void cloneAl() {

        final OptimalAlignmentVector zuKopieren = this.allalignments
            .get(this.counter);

        final OptimalAlignmentVector alKopie = (OptimalAlignmentVector) zuKopieren
            .clone();

        this.allalignments.add(alKopie);

    }

    /**
     *
     *
     *
     * @param u
     *
     * @param v
     *
     */
    public void computeMatrix(final String u, final String v) {

        this.seq1 = u;

        this.seq2 = v;

        final int m = u.length();

        final int n = v.length();

        this.matrix = new double[m][n];

        this.pwd = new double[m][n];

        this.pathway = new int[m][n][3];

        this.matrix[0][0] = 0;

        this.pathway[0][0][0] = 0;

        // 2 Schleien um erste Zeile/Spalte f�llen zu k�nnen
        for (int is = 1; is < n; is++) {

            final double ld = vergleiche((chartoNumber(this.seq2.charAt(is))),
                0);

            this.pwd[0][is] = ld;

            this.matrix[0][is] = Math
                .round((this.matrix[0][is - 1] + ld) * 100) / 100.00;

            this.pathway[0][is][0] = 1;

        }

        for (int ju = 1; ju < m; ju++) {

            final double ld = vergleiche(0,
                (chartoNumber(this.seq1.charAt(ju))));

            this.pwd[ju][0] = ld;

            this.matrix[ju][0] = Math
                .round((this.matrix[ju - 1][0] + ld) * 100) / 100.00;

            this.pathway[ju][0][0] = 2;

        }

        // Matrix von (1,1) bis (m,n) mit Werten f�llen und Pathway f�r
        // Backtracking speichern
        for (int i = 1; i < n; i++) {

            for (int j = 1; j < m; j++) {

                // 3 vorg�nger, die man f�r die Minimierung ben�tigt,
                // holen
                this.z = this.matrix[j - 1][i - 1];

                this.x = this.matrix[j][i - 1];

                this.y = this.matrix[j - 1][i];

                final double lz = vergleiche(
                    (chartoNumber(this.seq1.charAt(j))),
                    (chartoNumber(this.seq2.charAt(i))));

                this.pwd[j][i] = lz;

                final double lx = vergleiche(0, (chartoNumber(this.seq2
                    .charAt(i))));

                final double ly = vergleiche(
                    (chartoNumber(this.seq1.charAt(j))), 0);

                // Werte f�r hor/ver/diag Schritt berechenn
                this.z = Math.round((this.z + lz) * 100) / 100.00;

                this.x = Math.round((this.x + lx) * 100) / 100.00;

                this.y = Math.round((this.y + ly) * 100) / 100.00;

                // Minimalen Weg in Matrix �bernehmen
                this.matrix[j][i] = mini(this.x, this.y, this.z);

                // Pfad, von wo man gekommen ist, f�r Backtracking speichern
                vergleich:

                {

                    if ((this.x == mini(this.x, this.y, this.z))
                        && (this.y == mini(this.x, this.y, this.z))
                        && (this.z == mini(this.x, this.y, this.z))) {

                        this.pathway[j][i][0] = 1;

                        this.pathway[j][i][1] = 2;

                        this.pathway[j][i][2] = 3;

                        break vergleich;

                    }

                    if ((this.x == mini(this.x, this.y, this.z))
                        && (this.y == mini(this.x, this.y, this.z))) {

                        this.pathway[j][i][0] = 1;

                        this.pathway[j][i][1] = 2;

                        break vergleich;

                    }

                    if ((this.y == mini(this.x, this.y, this.z))
                        && (this.z == mini(this.x, this.y, this.z))) {

                        this.pathway[j][i][0] = 2;

                        this.pathway[j][i][1] = 3;

                        break vergleich;

                    }

                    if ((this.x == mini(this.x, this.y, this.z))
                        && (this.z == mini(this.x, this.y, this.z))) {

                        this.pathway[j][i][0] = 1;

                        this.pathway[j][i][1] = 3;

                        break vergleich;

                    }

                    if (this.x == mini(this.x, this.y, this.z)) {

                        this.pathway[j][i][0] = 1;

                    }

                    if (this.y == mini(this.x, this.y, this.z)) {

                        this.pathway[j][i][0] = 2;

                    }

                    if (this.z == mini(this.x, this.y, this.z)) {

                        this.pathway[j][i][0] = 3;

                    }

                }

            }

        }

    }

    // Alle optimalen Alignments erstellen
    /**
     *
     *
     *
     * @param u
     *
     * @param v
     *
     */
    public void createAlignments(final String u, final String v) {

        this.seq1 = u;

        this.seq2 = v;

        final int m = u.length();

        final int n = v.length();

        int i = n - 1;

        int j = m - 1;

        this.allalignments.add(this.oav1);

        align:

        while (true) {

            // wenn es nur eine M�glichkeit gab:
            if (this.pathway[j][i][1] == 0) {

                if (this.pathway[j][i][0] == 1) {

                    this.oav1.addAlChars(this.seq2.charAt(i), '-', j, i);

                    i--;

                    continue align;

                }

                if (this.pathway[j][i][0] == 2) {

                    this.oav1.addAlChars('-', this.seq1.charAt(j), j, i);

                    j--;

                    continue align;

                }

                if (this.pathway[j][i][0] == 3) {

                    this.oav1.addAlChars(this.seq2.charAt(i), this.seq1
                        .charAt(j), j, i);

                    j--;

                    i--;

                    continue align;

                }

                if (this.counter <= this.allalignments.size()) {

                    this.counter++;

                    if (this.counter >= this.allalignments.size()) {

                        break align;

                    }

                    this.oav1 = this.allalignments.get(this.counter);

                    i = this.oav1.getCharPair(this.oav1.countAlChars() - 1)
                        .getD();

                    j = this.oav1.getCharPair(this.oav1.countAlChars() - 1)
                        .getC();

                    continue align;

                }

            }

            // Wenn es 2 M�glichkeiten gab:
            if (this.pathway[j][i][2] == 0) {

                if (this.pathway[j][i][1] == 2) {

                    cloneAl();

                    this.oav2 = this.allalignments.get(this.allalignments
                        .size() - 1);

                    this.oav2.addAlChars(this.seq2.charAt(i), '-', j, i - 1);

                    this.oav1.addAlChars('-', this.seq1.charAt(j), j, i);

                    j--;

                    continue align;

                }

                if (this.pathway[j][i][1] == 3) {

                    cloneAl();

                    this.oav2 = this.allalignments.get(this.allalignments
                        .size() - 1);

                    if (this.pathway[j][i][0] == 1) {

                        this.oav2
                            .addAlChars(this.seq2.charAt(i), '-', j, i - 1);

                    } else {

                        this.oav2
                            .addAlChars('-', this.seq1.charAt(j), j - 1, i);

                    }

                    this.oav1.addAlChars(this.seq2.charAt(i), this.seq1
                        .charAt(j), j, i);

                    j--;

                    i--;

                    continue align;

                }

            }

            // Wenn es 3 M�glichkeiten gab:
            if (this.pathway[j][i][2] != 0) {

                if (this.pathway[j][i][2] == 3) {

                    cloneAl();

                    cloneAl();

                    this.oav2 = this.allalignments.get(this.allalignments
                        .size() - 2);

                    this.oav2.addAlChars(this.seq2.charAt(i), '-', j, i - 1);

                    this.oav2 = this.allalignments.get(this.allalignments
                        .size() - 1);

                    this.oav2.addAlChars('-', this.seq1.charAt(i), j - 1, i);

                    this.oav1.addAlChars(this.seq2.charAt(i), this.seq1
                        .charAt(j), j, i);

                    j--;

                    i--;

                    continue align;

                }

            }

        }

    }

    // Matrix in der Konsole ausgeben
    /**
     *
     *
     *
     */
    public void drawMatrix() {

        final int m = this.seq1.length();

        final int n = this.seq2.length();

        System.out.print(" ");

        for (int k1 = 0; k1 < n; k1++) {

            System.out.print("    " + this.seq2.charAt(k1));

        }

        System.out.println("");

        for (int i = 0; i < m; i++) {

            System.out.print(this.seq1.charAt(i) + "");

            for (int j = 0; j < n; j++) {

                System.out.print(" " + getMatrix()[i][j] + " ");

            }

            System.out.println("");

        }

    }

    // Zeichnet alle 3 Dimensionen der Pfadmatrix
    /**
     *
     *
     *
     */
    public void drawPathway() {

        final int m = this.seq1.length();

        final int n = this.seq2.length();

        System.out.print(" ");

        for (int ebene = 0; ebene < 3; ebene++) {

            System.out.println("Pfadmatrix in der " + ebene + ". Ebene");

            for (int k1 = 0; k1 < n; k1++) {

                System.out.print("  " + this.seq2.charAt(k1));

            }

            System.out.println("");

            for (int i = 0; i < m; i++) {

                System.out.print(this.seq1.charAt(i) + " ");

                for (int j = 0; j < n; j++) {

                    System.out.print(" " + getPathway()[i][j][ebene] + " ");

                }

                System.out.println("");

            }

            System.out.println("");

        }

    }

    /**
     *
     *
     *
     * @return
     *
     */
    public Vector<OptimalAlignmentVector> getAllalignments() {

        return this.allalignments;

    }

    /**
     *
     *
     *
     * @return
     *
     */
    public double[][] getMatrix() {

        return this.matrix;

    }

    /**
     *
     *
     *
     * @return
     *
     */
    public double[][] getPairwiseDistance() {

        return this.pwd;

    }

    /**
     *
     *
     *
     * @return
     *
     */
    public int[][][] getPathway() {

        return this.pathway;

    }

    /**
     *
     *
     *
     * @return
     *
     */
    public IFileFragment getQuery() {

        return this.query;

    }

    /**
     *
     *
     *
     * @return
     *
     */
    public IFileFragment getRef() {

        return this.ref;

    }

    /**
     *
     *
     *
     * @return
     *
     */
    public String getSeq1() {

        return this.seq1;

    }

    /**
     *
     *
     *
     * @return
     *
     */
    public String getSeq2() {

        return this.seq2;

    }

    /**
     *
     *
     *
     * @param a
     *
     * @param b
     *
     * @param c
     *
     * @return
     *
     */
    public double mini(final double a, final double b, final double c) {

        return Math.min(a, Math.min(b, c));

    }

    /**
     *
     *
     *
     * @param kostenfunktion1
     *
     */
    public void setKostenfunktion(final double[][] kostenfunktion1) {

        this.kostenfunktion = kostenfunktion1;

    }

    /**
     *
     *
     *
     * @param query1
     *
     */
    public void setQuery(final IFileFragment query1) {

        this.query = query1;

    }

    /**
     *
     *
     *
     * @param ref1
     *
     */
    public void setRef(final IFileFragment ref1) {

        this.ref = ref1;

    }

    /**
     *
     *
     *
     * @param seq11
     *
     */
    public void setSeq1(final String seq11) {

        this.seq1 = seq11;

    }

    /**
     *
     *
     *
     * @param seq21
     *
     */
    public void setSeq2(final String seq21) {

        this.seq2 = seq21;

    }

    /**
     *
     *
     *
     * @param i
     *
     * @param j
     *
     * @return
     *
     */
    public double vergleiche(final int i, final int j) {

        return this.kostenfunktion[i][j];

    }

}
