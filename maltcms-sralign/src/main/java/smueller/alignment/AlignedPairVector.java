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
package smueller.alignment;

// Speichert alignierte Paare und deren Position, zusammen bilden die Objekte in
// einem OAV ein Alignment
/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class AlignedPairVector implements Cloneable {

    private char a = ' ';
    private char b = ' ';
    private int c;
    private int d;
    private int e;
    private int f;
    private boolean done;

    /**
     *
     */
    public AlignedPairVector() {
    }

    /**
     *
     * @param done1
     */
    public AlignedPairVector(final boolean done1) {
        this.done = done1;
    }

    /**
     *
     * @param a1
     * @param b1
     * @param c1
     * @param d1
     */
    public AlignedPairVector(final char a1, final char b1, final int c1,
        final int d1) {
        this.a = a1;
        this.b = b1;
        this.c = c1;
        this.d = d1;
    }

    /**
     *
     * @param c1
     * @param d1
     */
    public AlignedPairVector(final int c1, final int d1) {
        this.c = c1;
        this.d = d1;
    }

    /**
     *
     * @return
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException cNSE) {
            System.err.println("Klonen fehlgeschlagen");
            return null;
        }

    }

    /**
     *
     * @return
     */
    public char getA() {
        return this.a;
    }

    /**
     *
     * @return
     */
    public char getB() {
        return this.b;
    }

    /**
     *
     * @return
     */
    public int getC() {
        return this.c;
    }

    /**
     *
     * @return
     */
    public int getD() {
        return this.d;
    }

    /**
     *
     * @return
     */
    public int getE() {
        return this.e;
    }

    /**
     *
     * @return
     */
    public int getF() {
        return this.f;
    }

    /**
     *
     * @return
     */
    public boolean isDone() {
        return this.done;
    }

    /**
     *
     * @param a1
     */
    public void setA(final char a1) {
        this.a = a1;
    }

    /**
     *
     * @param b1
     */
    public void setB(final char b1) {
        this.b = b1;
    }

    /**
     *
     * @param c1
     */
    public void setC(final int c1) {
        this.c = c1;
    }

    /**
     *
     * @param d1
     */
    public void setD(final int d1) {
        this.d = d1;
    }

    /**
     *
     * @param done1
     */
    public void setDone(final boolean done1) {
        this.done = done1;
    }

    /**
     *
     * @param e1
     */
    public void setE(final int e1) {
        this.e = e1;
    }

    /**
     *
     * @param f1
     */
    public void setF(final int f1) {
        this.f = f1;
    }
}
