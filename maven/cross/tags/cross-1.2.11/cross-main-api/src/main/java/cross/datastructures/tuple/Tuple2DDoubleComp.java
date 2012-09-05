/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.datastructures.tuple;

import java.util.Comparator;

/**
 * Comparator which can be used to sort collections of Tuple2D<Double,Double>
 * based on the first element of the tuple.
 *
 * @author Nils Hoffmann
 *
 *
 */
public class Tuple2DDoubleComp implements Comparator<Tuple2D<Double, Double>> {

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(final Tuple2D<Double, Double> o1,
            final Tuple2D<Double, Double> o2) {
        if (o1.getFirst() > o2.getFirst()) {
            return 1;
        } else if (o1.getFirst() < o2.getFirst()) {
            return -1;
        }
        return 0;
    }
}
