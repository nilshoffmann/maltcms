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

/**
 * <p>NewDistanceMatrix class.</p>
 *
 * @author Soeren Mueller
 * 
 */
public class NewDistanceMatrix extends DistanceMatrix {

    /**
     * <p>Constructor for NewDistanceMatrix.</p>
     *
     * @param bp an array of double.
     */
    public NewDistanceMatrix(final double[] bp) {
        super(bp);
        final int l = bp.length - 2;
        this.distmat = new double[bp.length][bp.length];
        for (int i = 0; i < bp.length; i++) {
            this.distmat[i][0] = 3;
        }
        for (int j = 0; j < bp.length; j++) {
            this.distmat[0][j] = 3;
        }
        for (int i = 1; i <= l + 1; i++) {
            for (int j = 1; j <= l + 1; j++) {

                if (i - j == 0) {
                    this.distmat[i][j] = 0;

                } else if (Math.abs(i - j) == 1) {
                    this.distmat[i][j] = 1;

                } else {
                    this.distmat[i][j] = 10;
                }
            }
        }
    }
}
