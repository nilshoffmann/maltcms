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

// Berechnen der Breakpoints, die festlegen in welchem Intervall ein Buchstabe
// zugeordnet wird
import smueller.SymbolicRepresentationAlignment;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * <p>BreakPoints class.</p>
 *
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 * @version $Id: $Id
 */
public class BreakPoints {

    private final double[] common;

    // Berechne gemeinsame Breakpoints
    /**
     * <p>Constructor for BreakPoints.</p>
     *
     * @param c a {@link ucar.ma2.Array} object.
     * @param d a {@link ucar.ma2.Array} object.
     */
    public BreakPoints(final Array c, final Array d) {

        final double[] a = calcbreakpoints(c, SymbolicRepresentationAlignment
                .getSorti().getSortedjavarray1(),
                SymbolicRepresentationAlignment.getAlphabetgr());

        final double[] b = calcbreakpoints(d, SymbolicRepresentationAlignment
                .getSorti().getSortedjavarray2(),
                SymbolicRepresentationAlignment.getAlphabetgr());

        this.common = new double[a.length];

        for (int i = 0; i < this.common.length; i++) {

            this.common[i] = (a[i] + b[i]) / 2;

        }

    }

    /**
     * <p>calcbreakpoints.</p>
     *
     * @param a1 a {@link ucar.ma2.Array} object.
     * @param sortiert an array of double.
     * @param alphabetgr a int.
     * @return an array of double.
     */
    public double[] calcbreakpoints(final Array a1, final double[] sortiert,
            final int alphabetgr) {

        final Array a = a1.copy();

        double total = 0;

        double save = 0;

        final IndexIterator ii1 = a.getIndexIterator();

        // Gesamtintensit�t berechnen
        while (ii1.hasNext()) {

            total = total + ii1.getDoubleNext();

        }

        // Festlegen, bis zu welchem Wert aufsummiert werden muss
        total = (total / (alphabetgr));

        double save2 = total;

        final double[] bps = new double[alphabetgr + 1];

        int pos = 0;

        // Aufsummieren, bis Schwellwert erreicht, Wert zum �berschreiten der
        // Schwelle merken und als Breakpoint sichern
        for (int j = 0; j < sortiert.length; j++) {

            if (save < save2) {

                save = save + sortiert[j];

            } else {

                save = save + sortiert[j];

                save2 = save2 + total;

                bps[pos] = sortiert[j - 1];

                pos++;

            }

        }

        // Wenn Letzter Breakpoint auf Grund zu kleinem Wertebereich nicht mehr
        // gesetzt, dann sorge daf�r dass zumindest 1 Wert
        // den gr��ten Buchstaben bekommt
        if ((bps[alphabetgr - 2] == 0.0) && (bps[alphabetgr - 3] != 0.0)) {

            bps[alphabetgr - 2] = sortiert[sortiert.length - 1] - 0.01;

        }

        // System.out.println("--------");
        // Max und Min den Breakpoints mitgeben
        bps[alphabetgr] = Math.round(sortiert[0] * 100) / 100.00;

        bps[alphabetgr - 1] = Math.round(sortiert[sortiert.length - 1] * 100) / 100.00;

        return bps;

    }

    /**
     * <p>Getter for the field <code>common</code>.</p>
     *
     * @return an array of double.
     */
    public double[] getCommon() {

        return this.common;

    }

}
