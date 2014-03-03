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
package net.sf.maltcms.evaluation.spi.caap;

import cross.datastructures.fragments.IFileFragment;
import java.io.File;
import maltcms.io.xml.bindings.openms.featurexml.FeatureMap.FeatureList;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Nils Hoffmann
 */
public class PeakFactoryTest {

    public PeakFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getFeatureList method, of class PeakFactory.
     */
    @Test
    public void testGetFeatureList() {
        System.out.println("getFeatureList");
        FeatureList result = PeakFactory.getFeatureList(getClass().getResourceAsStream("/M1_1.featureXML"));
        assertNotNull(result);
    }
}
