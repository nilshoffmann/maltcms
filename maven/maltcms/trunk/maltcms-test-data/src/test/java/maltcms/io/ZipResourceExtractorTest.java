/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.io;

import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.junit.Assert;

/**
 *
 * @author nilshoffmann
 */
public class ZipResourceExtractorTest {
    
    @Rule
    public TemporaryFolder tf = new TemporaryFolder();
    
    @Test
    public void testExtract1D() {
        File outputFolder = tf.newFolder(
                "cdf");
        File outputFile = ZipResourceExtractor.extract("/cdf/1D/glucoseA.cdf.gz", outputFolder);
        File unzippedFile = new File(outputFolder,"glucoseA.cdf");
        Assert.assertTrue(outputFile.getAbsolutePath().equals(unzippedFile.getAbsolutePath()));
        Assert.assertTrue(unzippedFile.exists());
        System.out.println("File exists");
        Assert.assertTrue(unzippedFile.length()>0);
        System.out.println("File size is != 0: "+unzippedFile.length());
    }
    
    @Test
    public void testExtract2D() {
        File outputFolder = tf.newFolder(
                "cdf");
        File outputFile = ZipResourceExtractor.extract("/cdf/2D/090306_37_FAME_Standard_1.cdf.gz", outputFolder);
        File unzippedFile = new File(outputFolder,"090306_37_FAME_Standard_1.cdf");
        Assert.assertTrue(outputFile.getAbsolutePath().equals(unzippedFile.getAbsolutePath()));
        Assert.assertTrue(unzippedFile.exists());
        System.out.println("File exists");
        Assert.assertTrue(unzippedFile.length()>0);
        System.out.println("File size is != 0: "+unzippedFile.length());
    }

}
