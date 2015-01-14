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
package maltcms.commands.fragments2d.io.csv;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import maltcms.datastructures.peak.Peak2D;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ExtractClassPathFiles;
import maltcms.test.ZipResourceExtractor;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Nils Hoffmann
 */
public class GcImagePeak2DImporterTest extends AFragmentCommandTest {

    @Rule
    public ExtractClassPathFiles testFiles = new ExtractClassPathFiles(tf,
            "/cdf/2D/mut_t1_a.cdf.gz");
    
    @Test
    public void testGcImagePeak2DImporter() throws IOException {
        File dataFolder = tf.newFolder("gcImageTestFolder");
        File file = ZipResourceExtractor.extract(
                "/csv/gcimage/reduced/mut_t1_a.csv.gz", dataFolder);

        List<IFragmentCommand> commands = new ArrayList<>();
//        Default2DVarLoader default2DVarLoader = new Default2DVarLoader();
//        default2DVarLoader.setScanRate(100);
//        default2DVarLoader.setModulationTime(5);
//        commands.add(default2DVarLoader);
        GcImagePeak2DImporter importer = new GcImagePeak2DImporter();
        importer.setLocaleString("en_US");
        importer.setQuotationCharacter("\"");
        importer.setReportFiles(Arrays.asList(new String[]{file.getAbsolutePath()}));
        commands.add(importer);
        IWorkflow w = createWorkflow(commands, testFiles.getFiles());
        TupleND<IFileFragment> workflowResults = testWorkflow(w);
        IFileFragment resultFragment = workflowResults.get(0);
        List<Peak2D> peaks = Peak2D.fromFragment2D(resultFragment, "tic_peaks");
        Assert.assertEquals(375, peaks.size());
        Peak2D firstPeak = peaks.get(0);
//        1	M000664_A146007-101-xxx_NA_1470.57_TRUE_VAR5_ALK_Butanoic acid, 4-amino-3-hydroxy- (3TMS)	Group 1			11.5	2.23	12423	12423
        //index is zero based
        Assert.assertEquals(0, firstPeak.getIndex());
        Assert.assertEquals("M000664_A146007-101-xxx_NA_1470.57_TRUE_VAR5_ALK_Butanoic acid, 4-amino-3-hydroxy- (3TMS)", firstPeak.getName());
        Assert.assertEquals(11.5d * 60.d, firstPeak.getFirstRetTime(), 0.0001);
        Assert.assertEquals(2.23d, firstPeak.getSecondRetTime(), 0.0001);
        Assert.assertEquals(12423, firstPeak.getArea(), 0.0001);
        Assert.assertEquals(12423, firstPeak.getApexIntensity(), 0.0001);
    }

}
