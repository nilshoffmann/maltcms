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

import ucar.ma2.Array;

// Erzeugt sortierte Java Arrays aus ma2 Arrays
/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class SortedJavArrays {

    private double[] sortedjavarray1;
    private double[] sortedjavarray2;

    /**
     *
     * @return
     */
    public double[] getSortedjavarray1() {
        return this.sortedjavarray1;
    }

    /**
     *
     * @return
     */
    public double[] getSortedjavarray2() {
        return this.sortedjavarray2;
    }

    /**
     *
     * @param sortedjavarray11
     */
    public void setSortedjavarray1(final double[] sortedjavarray11) {
        this.sortedjavarray1 = sortedjavarray11;
    }

    /**
     *
     * @param sortedjavarray21
     */
    public void setSortedjavarray2(final double[] sortedjavarray21) {
        this.sortedjavarray2 = sortedjavarray21;
    }

    /**
     *
     * @param a1
     * @param b1
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
