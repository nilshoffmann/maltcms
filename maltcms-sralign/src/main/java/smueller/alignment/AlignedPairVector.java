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

import lombok.extern.slf4j.Slf4j;

// einem OAV ein Alignment
/**
 * <p>AlignedPairVector class.</p>
 *
 * @author Soeren Mueller
 * 
 */
@Slf4j
public class AlignedPairVector implements Cloneable {

    private char a = ' ';
    private char b = ' ';
    private int c;
    private int d;
    private int e;
    private int f;
    private boolean done;

    /**
     * <p>Constructor for AlignedPairVector.</p>
     */
    public AlignedPairVector() {
    }

    /**
     * <p>Constructor for AlignedPairVector.</p>
     *
     * @param done1 a boolean.
     */
    public AlignedPairVector(final boolean done1) {
        this.done = done1;
    }

    /**
     * <p>Constructor for AlignedPairVector.</p>
     *
     * @param a1 a char.
     * @param b1 a char.
     * @param c1 a int.
     * @param d1 a int.
     */
    public AlignedPairVector(final char a1, final char b1, final int c1,
            final int d1) {
        this.a = a1;
        this.b = b1;
        this.c = c1;
        this.d = d1;
    }

    /**
     * <p>Constructor for AlignedPairVector.</p>
     *
     * @param c1 a int.
     * @param d1 a int.
     */
    public AlignedPairVector(final int c1, final int d1) {
        this.c = c1;
        this.d = d1;
    }

    /** {@inheritDoc} */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException cNSE) {
            log.warn("Klonen fehlgeschlagen");
            return null;
        }

    }

    /**
     * <p>Getter for the field <code>a</code>.</p>
     *
     * @return a char.
     */
    public char getA() {
        return this.a;
    }

    /**
     * <p>Getter for the field <code>b</code>.</p>
     *
     * @return a char.
     */
    public char getB() {
        return this.b;
    }

    /**
     * <p>Getter for the field <code>c</code>.</p>
     *
     * @return a int.
     */
    public int getC() {
        return this.c;
    }

    /**
     * <p>Getter for the field <code>d</code>.</p>
     *
     * @return a int.
     */
    public int getD() {
        return this.d;
    }

    /**
     * <p>Getter for the field <code>e</code>.</p>
     *
     * @return a int.
     */
    public int getE() {
        return this.e;
    }

    /**
     * <p>Getter for the field <code>f</code>.</p>
     *
     * @return a int.
     */
    public int getF() {
        return this.f;
    }

    /**
     * <p>isDone.</p>
     *
     * @return a boolean.
     */
    public boolean isDone() {
        return this.done;
    }

    /**
     * <p>Setter for the field <code>a</code>.</p>
     *
     * @param a1 a char.
     */
    public void setA(final char a1) {
        this.a = a1;
    }

    /**
     * <p>Setter for the field <code>b</code>.</p>
     *
     * @param b1 a char.
     */
    public void setB(final char b1) {
        this.b = b1;
    }

    /**
     * <p>Setter for the field <code>c</code>.</p>
     *
     * @param c1 a int.
     */
    public void setC(final int c1) {
        this.c = c1;
    }

    /**
     * <p>Setter for the field <code>d</code>.</p>
     *
     * @param d1 a int.
     */
    public void setD(final int d1) {
        this.d = d1;
    }

    /**
     * <p>Setter for the field <code>done</code>.</p>
     *
     * @param done1 a boolean.
     */
    public void setDone(final boolean done1) {
        this.done = done1;
    }

    /**
     * <p>Setter for the field <code>e</code>.</p>
     *
     * @param e1 a int.
     */
    public void setE(final int e1) {
        this.e = e1;
    }

    /**
     * <p>Setter for the field <code>f</code>.</p>
     *
     * @param f1 a int.
     */
    public void setF(final int f1) {
        this.f = f1;
    }
}
