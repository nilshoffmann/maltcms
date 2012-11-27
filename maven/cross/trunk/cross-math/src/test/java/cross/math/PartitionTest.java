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

import junit.framework.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nils Hoffmann
 */
public class PartitionTest {

    public PartitionTest() {
    }

    /**
     * Test of iteration of class Partition.
     */
    @Test
    public void testIteration() {
        Partition p = new Partition(5);
        for (int i = 0; i < p.size()-1; i++) {
            Assert.assertTrue(p.hasNext());
            Assert.assertEquals(i, p.next().intValue());
        }
    }

    /**
     * Test of remove method, of class Partition.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        Partition p = new Partition(11);
        p.remove();
    }

    /**
     * Test of size method, of class Partition.
     */
    @Test
    public void testSize() {
        Partition p = new Partition(11);
        Assert.assertEquals(11, p.size());
    }
}
