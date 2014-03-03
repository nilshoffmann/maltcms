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

import java.util.Arrays;

/**
 *
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 *
 */
public class DistanceMatrix {

    /**
     *
     *
     *
     */
    protected double[][] distmat;

    // Berechnung der Distanzmatrix basierend auf den Breakpoints,
    // Differenz der Mittelwerte der jeweiligen Bins bildet Distanz
    /**
     *
     *
     *
     * @param bp
     *
     */
    public DistanceMatrix(final double[] bp) {

        this.distmat = new double[bp.length][bp.length];

        final double[] bpcopy = bp.clone();

        Arrays.sort(bpcopy);

        final int l = bpcopy.length - 2;

        for (int i = 1; i <= l + 1; i++) {

            for (int j = 1; j <= l + 1; j++) {

                if (i - j == 0) {

                    this.distmat[i][j] = 0.0;

                } else {

                    this.distmat[i][j] = Math
                        .round(Math.abs(((bpcopy[i - 1] + bpcopy[i]) / 2)
                                - ((bpcopy[j - 1] + bpcopy[j]) / 2)) * 3000) / 100.00;

                }

            }

        }

        // Gapkosten eintragen, direkte Nachbarn sollen erlaubt sein zu alignen,
        // sonst soll gap gesetzt werden
        for (int o = 1; o < this.distmat.length - 1; o++) {

            this.distmat[o][0] = Math
                .round((this.distmat[o + 1][o] + 0.01) * 100) / 100.00;

            this.distmat[0][o] = Math
                .round((this.distmat[o + 1][o] + 0.01) * 100) / 100.00;

        }

        this.distmat[this.distmat.length - 1][0] = this.distmat[this.distmat.length - 2][this.distmat.length - 1] + 0.02;

        this.distmat[0][this.distmat.length - 1] = this.distmat[this.distmat.length - 2][this.distmat.length - 1] + 0.02;

        /*

         * for(int i=0;i<distmat.length;i++){ for(int j=0;j<distmat.length;j++){

         * System.out.print(distmat[i][j]+" "); } System.out.println(); }

         */
    }

    /**
     *
     *
     *
     * @return
     *
     */
    public double[][] getDistmat() {

        return this.distmat;

    }

}
