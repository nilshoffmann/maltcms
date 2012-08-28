/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.commands.filters.array;

import cross.datastructures.fragments.ImmutableFileFragment;
import java.io.File;
import cross.io.misc.ZipResourceExtractor;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;

/**
 *
 * @author nilshoffmann
 */
public class SavitzkyGolayFilterTest {

    @Rule
    public TemporaryFolder tf = new TemporaryFolder();

    public SavitzkyGolayFilterTest() {
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
     * Test of apply method, of class SavitzkyGolayFilter.
     */
    @Test
    public void testApply() {
        File outputFolder = tf.newFolder(
                "cdf");
        File outputFile = ZipResourceExtractor.extract("/cdf/1D/glucoseA.cdf.gz", outputFolder);
        File unzippedFile = new File(outputFolder, "glucoseA.cdf");
        ImmutableFileFragment ff = new ImmutableFileFragment(unzippedFile);
        Array tic = ff.getChild("total_intensity").getArray();
        SavitzkyGolayFilter instance = new SavitzkyGolayFilter();
        instance.setWindow(2);
        Array result = instance.apply(tic);

    }
}
