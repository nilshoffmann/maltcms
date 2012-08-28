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
