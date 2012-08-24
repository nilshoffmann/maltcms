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
package smueller.datastructure;

import java.util.Arrays;

/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class DistanceMatrix {

    protected double[][] distmat;

    // Berechnung der Distanzmatrix basierend auf den Breakpoints,
    // Differenz der Mittelwerte der jeweiligen Bins bildet Distanz
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

    public double[][] getDistmat() {
        return this.distmat;
    }
}
