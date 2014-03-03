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
package maltcms.test;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import cross.test.SetupLogging;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.junit.Assert;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class ZipResourceExtractorTest {

    @Rule
    public TemporaryFolder tf = new TemporaryFolder();
    @Rule
    public SetupLogging sl = new SetupLogging();

    @Test
    public void testExtract1D() throws IOException {
        File outputFolder = tf.newFolder(
                "cdf");
        File outputFile = ZipResourceExtractor.extract("/cdf/1D/glucoseA.cdf.gz", outputFolder);
        File unzippedFile = new File(outputFolder, "glucoseA.cdf");
        Assert.assertTrue(outputFile.getAbsolutePath().equals(unzippedFile.getAbsolutePath()));
        Assert.assertTrue(unzippedFile.exists());
        log.info("File exists");
        Assert.assertTrue(unzippedFile.length() > 0);
        log.info("File size is != 0: {}", unzippedFile.length());
    }

    @Test
    public void testExtractZip() throws IOException {
        File outputFolder = tf.newFolder(
                "xls");
        //zip archives must contain a folder with their name, e.g. 'Ap 601.D'
        File outputFile = ZipResourceExtractor.extract("/xls/agilent/Ap 601.D.zip", outputFolder);
        File unzippedFile = new File(outputFolder, "Ap 601.D");
        log.info(outputFile.getAbsolutePath());
        log.info(unzippedFile.getAbsolutePath());
        Assert.assertTrue(outputFile.getAbsolutePath().equals(unzippedFile.getAbsolutePath()));
        Assert.assertTrue(unzippedFile.exists());
        log.info("File exists");
        Assert.assertTrue(unzippedFile.isDirectory());
        log.info("File is directory");
        Assert.assertTrue(unzippedFile.length() >= 0);
        log.info("File size is >= 0: {}", unzippedFile.length());
        File reportFile = new File(unzippedFile, "REPORT01.xls");
        Assert.assertTrue(reportFile.exists());
        log.info("Report file exists!");
        Assert.assertTrue(reportFile.isFile());
        log.info("Report file is file!");
    }
//    @Test
//    public void testExtract2D() throws IOException {
//        File outputFolder = tf.newFolder(
//                "cdf");
//        File outputFile = ZipResourceExtractor.extract("/cdf/2D/090306_37_FAME_Standard_1.cdf.gz", outputFolder);
//        File unzippedFile = new File(outputFolder, "090306_37_FAME_Standard_1.cdf");
//        Assert.assertTrue(outputFile.getAbsolutePath().equals(unzippedFile.getAbsolutePath()));
//        Assert.assertTrue(unzippedFile.exists());
//        log.info("File exists");
//        Assert.assertTrue(unzippedFile.length() > 0);
//        log.info("File size is != 0: {}", unzippedFile.length());
//    }
}
