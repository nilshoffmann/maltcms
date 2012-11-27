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
package cross.math;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nils Hoffmann
 */
public class CombinationIteratorTest {

    /**
     * Test of next method, of class CombinationIterator.
     */
    @Test
    public void testNext() {
        Partition p = new Partition(2);
        Partition q = new Partition(p, 3);
        CombinationIterator ci = new CombinationIterator(p, q);
//        System.out.println("Combinations: " + ci.size());
        Assert.assertEquals(6,ci.size());
        int[][] combs = new int[(int) ci.size()][];
        combs[0] = new int[]{0, 0};
        combs[1] = new int[]{0, 1};
        combs[2] = new int[]{0, 2};
        combs[3] = new int[]{1, 0};
        combs[4] = new int[]{1, 1};
        combs[5] = new int[]{1, 2};
        
//        System.out.println(Arrays.deepToString(combs));
        int counter = 0;
        ci.reset();
        for (int i = 0; i < ci.size(); i++) {
//            System.out.println("Combination "+i);
            int[] a = ci.next();
//            System.out.println(Arrays.toString(a));
            Assert.assertArrayEquals(combs[counter], a);
            counter++;
        }
    }

}
