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
 * Typed specialization of Tuple2D for Float, providing additional methods for
 * arithmetic with Tuple2DF.
 *
 * @author Nils Hoffmann
 *
 */
public class Tuple2DF extends Tuple2D<Float, Float> {

    public static Tuple2D<Float, Float> diff(final Tuple2D<Float, Float> t1,
            final Tuple2D<Float, Float> t2) {
        return new Tuple2DF(t1.getFirst() - t2.getFirst(), t1.getSecond()
                - t2.getSecond());
    }

    public static Float dot(final Tuple2D<Float, Float> t1,
            final Tuple2D<Float, Float> t2) {
        return t1.getFirst() * t2.getFirst() + t1.getSecond() * t2.getSecond();
    }

    public static Float len(final Tuple2D<Float, Float> t1) {
        return 1.0f / (float) Math.sqrt(Tuple2DF.dot(t1, t1));
    }

    public static Tuple2D<Float, Float> mult(final Tuple2D<Float, Float> l1,
            final Float mult) {
        return new Tuple2DF(l1.getFirst() * mult, l1.getSecond() * mult);
    }

    public static Tuple2D<Float, Float> trans(final Tuple2D<Float, Float> t1) {
        return new Tuple2DF(t1.getSecond(), t1.getFirst());
    }

    public Tuple2DF(final Float t1, final Float t2) {
        super(t1, t2);
    }
}
