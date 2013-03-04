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

/**
 * Typed specialization of Tuple2D for Double, providing additional methods for
 * arithmetic with Tuple2DD.
 *
 * @author Nils Hoffmann
 *
 */
public class Tuple2DD extends Tuple2D<Double, Double> {

    public static Tuple2D<Double, Double> diff(
            final Tuple2D<Double, Double> t1, final Tuple2D<Double, Double> t2) {
        return new Tuple2DD(t1.getFirst() - t2.getFirst(), t1.getSecond()
                - t2.getSecond());
    }

    public static Double dot(final Tuple2D<Double, Double> t1,
            final Tuple2D<Double, Double> t2) {
        return t1.getFirst() * t2.getFirst() + t1.getSecond() * t2.getSecond();
    }

    public static Double len(final Tuple2D<Double, Double> t1) {
        return 1.0f / Math.sqrt(Tuple2DD.dot(t1, t1));
    }

    public static Tuple2D<Double, Double> mult(
            final Tuple2D<Double, Double> l1, final Double mult) {
        return new Tuple2DD(l1.getFirst() * mult, l1.getSecond() * mult);
    }

    public static Tuple2D<Double, Double> trans(final Tuple2D<Double, Double> t1) {
        return new Tuple2DD(t1.getSecond(), t1.getFirst());
    }

    public Tuple2DD(final Double t1, final Double t2) {
        super(t1, t2);
    }
}
