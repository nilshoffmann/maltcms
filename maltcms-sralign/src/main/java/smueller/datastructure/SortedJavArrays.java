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
import ucar.ma2.Array;

// Erzeugt sortierte Java Arrays aus ma2 Arrays
/**
 * <p>SortedJavArrays class.</p>
 *
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 * @version $Id: $Id
 */
public class SortedJavArrays {

    private double[] sortedjavarray1;

    private double[] sortedjavarray2;

    /**
     * <p>Getter for the field <code>sortedjavarray1</code>.</p>
     *
     * @return an array of double.
     */
    public double[] getSortedjavarray1() {

        return this.sortedjavarray1;

    }

    /**
     * <p>Getter for the field <code>sortedjavarray2</code>.</p>
     *
     * @return an array of double.
     */
    public double[] getSortedjavarray2() {

        return this.sortedjavarray2;

    }

    /**
     * <p>Setter for the field <code>sortedjavarray1</code>.</p>
     *
     * @param sortedjavarray11 an array of double.
     */
    public void setSortedjavarray1(final double[] sortedjavarray11) {

        this.sortedjavarray1 = sortedjavarray11;

    }

    /**
     * <p>Setter for the field <code>sortedjavarray2</code>.</p>
     *
     * @param sortedjavarray21 an array of double.
     */
    public void setSortedjavarray2(final double[] sortedjavarray21) {

        this.sortedjavarray2 = sortedjavarray21;

    }

    /**
     * <p>sort.</p>
     *
     * @param a1 a {@link ucar.ma2.Array} object.
     * @param b1 a {@link ucar.ma2.Array} object.
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
