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
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
//import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author Nils Hoffmann
 */
public class CombinatoricsTest {

    /**
     * Test of faculty method, of class Combinatorics.
     */
    @Test
    public void testFaculty() {
        double f = 1 * 2 * 3 * 4 * 5 * 6 * 7;
        Assert.assertEquals(Combinatorics.faculty(7), f, 0.0f);
    }
}
