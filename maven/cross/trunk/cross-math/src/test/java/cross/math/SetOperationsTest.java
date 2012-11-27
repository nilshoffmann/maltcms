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
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Nils Hoffmann
 */
public class SetOperationsTest {
    
    public SetOperationsTest() {
    }

    /**
     * Test of union method, of class SetOperations.
     */
    @Test
    public void testUnion() {
        Set<Integer> a = new HashSet<Integer>(Arrays.asList(5,21,45,1));
        Set<Integer> b = new HashSet<Integer>(Arrays.asList(5,26,43,1));
        Set<Integer> union = new HashSet<Integer>(Arrays.asList(5,21,26,43,45,1));
        Set<Integer> result = SetOperations.union(a, b);
        Assert.assertEquals(union, result);
    }

    /**
     * Test of intersection method, of class SetOperations.
     */
    @Test
    public void testIntersection() {
        Set<Integer> a = new HashSet<Integer>(Arrays.asList(5,21,45,1));
        Set<Integer> b = new HashSet<Integer>(Arrays.asList(5,26,43,1));
        Set<Integer> intersection = new HashSet<Integer>(Arrays.asList(1,5));
        Set<Integer> result = SetOperations.intersection(a, b);
        Assert.assertEquals(intersection, result);
    }

    /**
     * Test of complement method, of class SetOperations.
     */
    @Test
    public void testComplement() {
        Set<Integer> a = new HashSet<Integer>(Arrays.asList(5,21,45,1));
        Set<Integer> b = new HashSet<Integer>(Arrays.asList(5,26,43,1));
        Set<Integer> complementA = new HashSet<Integer>(Arrays.asList(21,45));
        Set<Integer> complementB = new HashSet<Integer>(Arrays.asList(26,43));
        Set<Integer> resultA = SetOperations.complement(a, b);
        Set<Integer> resultB = SetOperations.complement(b, a);
        Assert.assertEquals(complementA, resultA);
        Assert.assertEquals(complementB, resultB);
    }

    /**
     * Test of symmetricDifference method, of class SetOperations.
     */
    @Test
    public void testSymmetricDifference() {
        Set<Integer> a = new HashSet<Integer>(Arrays.asList(5,21,45,1));
        Set<Integer> b = new HashSet<Integer>(Arrays.asList(5,26,43,1));
        Set<Integer> symmetricDiff = new HashSet<Integer>(Arrays.asList(21,45,26,43));
        Set<Integer> result = SetOperations.symmetricDifference(a, b);
        Set<Integer> resultB = SetOperations.complement(b, a);
        Assert.assertEquals(symmetricDiff, result);
    }
}
