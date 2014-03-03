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
package smueller.datastructure;

import java.util.Arrays;
import ucar.ma2.Array;

// Erzeugt sortierte Java Arrays aus ma2 Arrays
/**
 *
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 *
 */
public class SortedJavArrays {

    private double[] sortedjavarray1;

    private double[] sortedjavarray2;

    /**
     *
     *
     *
     * @return
     *
     */
    public double[] getSortedjavarray1() {

        return this.sortedjavarray1;

    }

    /**
     *
     *
     *
     * @return
     *
     */
    public double[] getSortedjavarray2() {

        return this.sortedjavarray2;

    }

    /**
     *
     *
     *
     * @param sortedjavarray11
     *
     */
    public void setSortedjavarray1(final double[] sortedjavarray11) {

        this.sortedjavarray1 = sortedjavarray11;

    }

    /**
     *
     *
     *
     * @param sortedjavarray21
     *
     */
    public void setSortedjavarray2(final double[] sortedjavarray21) {

        this.sortedjavarray2 = sortedjavarray21;

    }

    /**
     *
     *
     *
     * @param a1
     *
     * @param b1
     *
     */
    public void sort(final Array a1, final Array b1) {

        final Array a = a1.copy();

        this.sortedjavarray1 = (double[]) a.copyTo1DJavaArray();

        Arrays.sort(this.sortedjavarray1);

        final Array b = b1.copy();

        this.sortedjavarray2 = (double[]) b.copyTo1DJavaArray();

        Arrays.sort(this.sortedjavarray2);

    }

}
