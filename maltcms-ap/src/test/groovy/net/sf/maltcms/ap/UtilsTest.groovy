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

package net.sf.maltcms.ap

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

/**
 *
 * @author Nils Hoffmann
 */
class UtilsTest {

    @Test
    public void testIsDouble() {
        assertTrue new Utils().isDouble("989.24")
        assertTrue new Utils().isDouble("98")
    }
    
    @Test
    public void testIsInteger() {
        assertTrue new Utils().isInteger("879")
        assertFalse new Utils().isInteger("897.35")
    }

    @Test
    public void testConvString() {
        assertEquals("defaultValue",new Utils().convString(null, "defaultValue"))
        assertEquals("b",new Utils().convString("b", "defaultValue"))
    }

    @Test
    public void testConvDouble() {
        assertEquals(0.0d, new Utils().convDouble(null, 0.0d), 0.0d)
        assertEquals(Double.NaN, new Utils().convDouble("NaN", 0.0d), 0.0d)
        assertEquals(Double.NEGATIVE_INFINITY, new Utils().convDouble("-Infinity", 0.0d), 0.0d)
        assertEquals(Double.POSITIVE_INFINITY, new Utils().convDouble("Infinity", 0.0d), 0.0d)
        assertEquals(89123.124, new Utils().convDouble("89123.124", 0.0d), 0.0d)
    }
    
    @Test
    public void testConvInteger() {
        assertEquals(0, new Utils().convInteger(null, 0))
        assertEquals(23, new Utils().convInteger("23", 0))
    }
    
    @Test
    public void testConvBoolean() {
        assertEquals(true, new Utils().convBoolean(null, true))
        assertEquals(false, new Utils().convBoolean("false", true))
    }
}
