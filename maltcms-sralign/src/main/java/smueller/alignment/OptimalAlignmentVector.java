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

import java.util.Vector;

// Enth�lt optimales Alignment
/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class OptimalAlignmentVector implements Cloneable {

    private final Vector<AlignedPairVector> align = new Vector<AlignedPairVector>();

    /**
     *
     */
    public OptimalAlignmentVector() {
    }

    /**
     *
     * @param a
     */
    public void addAlChars(final boolean a) {
        final AlignedPairVector ss = new AlignedPairVector(a);
        this.align.add(ss);
    }

    /**
     *
     * @param a
     * @param b
     */
    public void addAlChars(final char a, final char b) {
        final AlignedPairVector ss = new AlignedPairVector(a, b);
        this.align.add(ss);
    }

    /**
     *
     * @param a
     * @param b
     * @param c
     * @param d
     */
    public void addAlChars(final char a, final char b, final int c, final int d) {
        final AlignedPairVector ss = new AlignedPairVector(a, b, c, d);
        this.align.add(ss);
    }

    /**
     *
     * @param ss
     */
    public void addALChars(final AlignedPairVector ss) {
        this.align.add(ss);
    }

    /**
     *
     * @param a
     * @param b
     */
    public void changeAlChars(final int a, final int b) {
        final AlignedPairVector ss = new AlignedPairVector();
        getCharPair(countAlChars() - 1);
        ss.setC(a);
        ss.setD(b);
    }

    /**
     *
     * @return
     */
    @Override
    public Object clone() {

        final OptimalAlignmentVector cloneAlignme = new OptimalAlignmentVector();
        for (int i = 0; i < this.countAlChars(); i++) {
            cloneAlignme.addALChars((AlignedPairVector) this.getCharPair(i)
                .clone());
            // System.out.println(11);
        }
        return cloneAlignme;

    }

    /**
     *
     * @return
     */
    public int countAlChars() {
        return this.align.size();
    }

    /**
     *
     * @param i
     * @return
     */
    public AlignedPairVector getCharPair(final int i) {
        return this.align.get(i);
    }
}
