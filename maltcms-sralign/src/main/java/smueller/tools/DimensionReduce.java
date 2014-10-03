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
package smueller.tools;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import cross.tools.MathTools;

// Reduktion der Daten durch Ueberfuehrung in die PAA Repraesentation
/**
 * <p>DimensionReduce class.</p>
 *
 * @author Soeren Mueller
 * 
 */
public class DimensionReduce {

    /**
     * <p>paa.</p>
     *
     * @param a1 a {@link ucar.ma2.Array} object.
     * @param windowsize a int.
     * @return a {@link ucar.ma2.Array} object.
     */
    public static Array paa(final Array a1, final int windowsize) {
        final Array a = a1.copy();
        final int[] dimension = {((int) a.getSize() / windowsize) + 1};
        final Array reduced = Array.factory(a.getElementType(), dimension);
        final IndexIterator ii4 = a.getIndexIterator();
        final IndexIterator red = reduced.getIndexIterator();
        double save = 0;
        int counter;
        final double[] window = new double[windowsize];

        // Setzt Fenster der Gr��e w �ber Daten, und findet Median darin.
        // Dieser
        // ist neuer Wert im reduzierten Array
        while (ii4.hasNext()) {
            for (counter = 0; counter < windowsize; counter++) {
                if (ii4.hasNext()) {
                    window[counter] = ii4.getDoubleNext();
                } else {
                    counter = windowsize;
                }
            }
            save = MathTools.median(window);
            counter = 0;
            red.setDoubleNext(save);
            save = 0;
        }

        return reduced;
    }
}
