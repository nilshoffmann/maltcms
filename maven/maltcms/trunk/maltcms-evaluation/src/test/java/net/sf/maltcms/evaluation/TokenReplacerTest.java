/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.evaluation;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nilshoffmann
 */
public class TokenReplacerTest {
    
    public TokenReplacerTest() {
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
    
    @Test
    public void testReplacement() {
        String base = "blasdjlaselihda";
        String s = base+"${KEY}";
        String value = "VALUE";
        String s2 = s.replaceAll("\\$\\{KEY\\}", value);
        System.out.println(s2);
        assertEquals(base+value, s2);
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
