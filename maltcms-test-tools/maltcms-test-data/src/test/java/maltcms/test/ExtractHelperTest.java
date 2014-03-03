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
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Nils Hoffmann
 */
public class ExtractHelperTest {

    @Rule
    public TemporaryFolder tf = new TemporaryFolder();

    @Test
    public void testExtractHelper() throws IOException {
        //clear possibly existing paths
        ExtractHelper.typeToPaths.clear();
        //manually add only one file per type category
        ExtractHelper.typeToPaths.put(ExtractHelper.FType.CDF_1D, new String[]{
                    "/cdf/1D/glucoseA.cdf.gz"
                });
        ExtractHelper.typeToPaths.put(ExtractHelper.FType.MZML, new String[]{
                    "/mzML/MzMLFile_PDA.mzML.xml.gz"});
        ExtractHelper.typeToPaths.put(ExtractHelper.FType.MZDATA, new String[]{
                    "/mzData/tiny1.mzData1.05.mzData.xml.gz"
                });
        ExtractHelper.typeToPaths.put(ExtractHelper.FType.MZXML, new String[]{
                    "/mzXML/tiny1.mzXML3.0.mzXML.gz"});
        ExtractHelper.typeToPaths.put(ExtractHelper.FType.MZ5, new String[]{
                    "/mz5/small_raw.mz5.gz"});
        //CDF 1D
        ExtractHelper.FType type = ExtractHelper.FType.CDF_1D;
        File[] files = ExtractHelper.extractAllForType(tf.newFolder(type.name()), type);
        for (File f : files) {
            Assert.assertTrue(f.exists() && f.isFile());
            Assert.assertTrue(f.length() > 0);
        }
        //MZML
        type = ExtractHelper.FType.MZML;
        files = ExtractHelper.extractAllForType(tf.newFolder(type.name()), type);
        for (File f : files) {
            Assert.assertTrue(f.exists() && f.isFile());
            Assert.assertTrue(f.length() > 0);
        }
        //MZXML
        type = ExtractHelper.FType.MZXML;
        files = ExtractHelper.extractAllForType(tf.newFolder(type.name()), type);
        for (File f : files) {
            Assert.assertTrue(f.exists() && f.isFile());
            Assert.assertTrue(f.length() > 0);
        }
        //MZDATA
        type = ExtractHelper.FType.MZDATA;
        files = ExtractHelper.extractAllForType(tf.newFolder(type.name()), type);
        for (File f : files) {
            Assert.assertTrue(f.exists() && f.isFile());
            Assert.assertTrue(f.length() > 0);
        }
        //MZ5
        type = ExtractHelper.FType.MZ5;
        files = ExtractHelper.extractAllForType(tf.newFolder(type.name()), type);
        for (File f : files) {
            Assert.assertTrue(f.exists() && f.isFile());
            Assert.assertTrue(f.length() > 0);
        }
    }
}
