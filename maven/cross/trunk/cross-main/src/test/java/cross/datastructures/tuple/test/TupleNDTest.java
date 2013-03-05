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
package cross.datastructures.tuple.test;

import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import java.util.Arrays;
import java.util.Iterator;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class TupleNDTest {

    @Test
    public void testGetNumberOfPairs() {
        final Integer[] ints = new Integer[7];
        for (int i = 0; i < 7; i++) {
            ints[i] = i;
        }
        TupleND<Integer> tnd = new TupleND<Integer>(ints);
        Assert.assertEquals(tnd.getNumberOfPairs(), 7 * 6 / 2);
    }

    @Test
    public void testGetPairs() {
        final Integer[] ints = new Integer[7];
        for (int i = 0; i < 7; i++) {
            ints[i] = i;
        }
        TupleND<Integer> tnd = new TupleND<Integer>(ints);
        final Iterator<Tuple2D<Integer, Integer>> iter = tnd.getPairs()
                .iterator();
        while (iter.hasNext()) {
            final Tuple2D<Integer, Integer> t = iter.next();
            log.info("Pair: {},{}", t.getFirst(), t.getSecond());
        }

        Assert.assertEquals(1, 1);
    }

    @Test
    public void testTupleNDCollectionOfT() {

        final Integer[] ints = new Integer[7];
        for (int i = 0; i < 7; i++) {
            ints[i] = i;
        }
        TupleND<Integer> tnd = new TupleND<Integer>(Arrays.asList(ints));
        Assert.assertEquals(tnd.getSize(), 7);
    }

    @Test
    public void testTupleNDTArray() {
        final Integer[] ints = new Integer[7];
        for (int i = 0; i < 7; i++) {
            ints[i] = i;
        }
        TupleND<Integer> tnd = new TupleND<Integer>(ints);
        Assert.assertEquals(tnd.getSize(), 7);
    }
}
