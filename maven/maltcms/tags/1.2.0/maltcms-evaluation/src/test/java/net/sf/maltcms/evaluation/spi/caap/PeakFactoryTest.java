/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.evaluation.spi.caap;

import cross.datastructures.fragments.IFileFragment;
import java.io.File;
import maltcms.io.xml.bindings.openms.featurexml.FeatureMap.FeatureList;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author nilshoffmann
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
