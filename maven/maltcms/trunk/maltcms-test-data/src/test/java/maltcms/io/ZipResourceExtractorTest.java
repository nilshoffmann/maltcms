/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.io;

import maltcms.test.ExtractHelper;
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
    
    @Test
    public void testExtractHelper() {
        ExtractHelper.FType type = ExtractHelper.FType.CDF_1D;
        File[] files = ExtractHelper.extractAllForType(tf.newFolder(type.name()), type);
        for(File f:files) {
            Assert.assertTrue(f.exists() && f.isFile());
            Assert.assertTrue(f.length()>0);
        }
        type = ExtractHelper.FType.MZML;
        files = ExtractHelper.extractAllForType(tf.newFolder(type.name()), type);
        for(File f:files) {
            Assert.assertTrue(f.exists() && f.isFile());
            Assert.assertTrue(f.length()>0);
        }
        type = ExtractHelper.FType.MZXML;
        files = ExtractHelper.extractAllForType(tf.newFolder(type.name()), type);
        for(File f:files) {
            Assert.assertTrue(f.exists() && f.isFile());
            Assert.assertTrue(f.length()>0);
        }
        type = ExtractHelper.FType.MZDATA;
        files = ExtractHelper.extractAllForType(tf.newFolder(type.name()), type);
        for(File f:files) {
            Assert.assertTrue(f.exists() && f.isFile());
            Assert.assertTrue(f.length()>0);
        }
    }

}
