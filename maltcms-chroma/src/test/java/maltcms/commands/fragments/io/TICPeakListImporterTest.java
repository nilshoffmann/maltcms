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
package maltcms.commands.fragments.io;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.workflow.IWorkflow;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import maltcms.commands.fragments.peakfinding.TICPeakFinderTest;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ZipResourceExtractor;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
public class TICPeakListImporterTest extends AFragmentCommandTest {

    /**
     *
     */
    @Test
    public void testTicPeakListImporter() throws IOException {
        File dataFolder = tf.newFolder("chromaTestData");
        File inputFile1 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseA.cdf.gz", dataFolder);
        File ticPeakFile = new File(dataFolder, "glucoseA.txt");
        FileUtils.write(ticPeakFile,
            "STUFF1\tSCAN\tOTHERSTUFF\n" +
            "214\t5\t5137\n" +
            "876\t10\t7681\n" +
            "13\t862\t89791\n",
            Charset.forName("UTF-8")
        );
        //5, 10, 13
        File outputBase = tf.newFolder(TICPeakFinderTest.class.getName());
        List<IFragmentCommand> commands = new ArrayList<>();
        TICPeakListImporter tpli = new TICPeakListImporter();
        ArrayList<String> filesToRead = new ArrayList<String>();
        filesToRead.add(ticPeakFile.getAbsolutePath());
        tpli.setFilesToRead(filesToRead);
        commands.add(tpli);
        IWorkflow w = createWorkflow(outputBase, commands, Arrays.asList(
                inputFile1));
        IFileFragment f = testWorkflow(w).get(0);
        Array ticPeaks = f.getChild("tic_peaks").getArray();
        Array refTicPeaks = Array.factory(new int[]{5, 10, 862});
        Assert.assertEquals(3, ticPeaks.getShape()[0]);
        for (int i = 0; i < ticPeaks.getShape()[0]; i++) {
            Assert.assertEquals(refTicPeaks.getInt(i), ticPeaks.getInt(i));
        }
    }
    
    /**
     *
     */
    @Test
    public void testTicPeakListImporterWithOffset() throws IOException {
        File dataFolder = tf.newFolder("chromaTestData");
        File inputFile1 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseA.cdf.gz", dataFolder);
        int offset = -1;
        File ticPeakFile = new File(dataFolder, "glucoseA.txt");
        FileUtils.write(ticPeakFile,
            "STUFF1\tSCAN\tOTHERSTUFF\n" +
            "214\t5\t5137\n" +
            "876\t10\t7681\n" +
            "13\t862\t89791\n",
            Charset.forName("UTF-8")
        );
        //5, 10, 13
        File outputBase = tf.newFolder(TICPeakFinderTest.class.getName());
        List<IFragmentCommand> commands = new ArrayList<>();
        TICPeakListImporter tpli = new TICPeakListImporter();
        tpli.setScanIndexOffset(offset);
        ArrayList<String> filesToRead = new ArrayList<String>();
        filesToRead.add(ticPeakFile.getAbsolutePath());
        tpli.setFilesToRead(filesToRead);
        commands.add(tpli);
        IWorkflow w = createWorkflow(outputBase, commands, Arrays.asList(
                inputFile1));
        IFileFragment f = testWorkflow(w).get(0);
        Array ticPeaks = f.getChild("tic_peaks").getArray();
        Array refTicPeaks = Array.factory(new int[]{4, 9, 861});
        Assert.assertEquals(3, ticPeaks.getShape()[0]);
        for (int i = 0; i < ticPeaks.getShape()[0]; i++) {
            Assert.assertEquals(refTicPeaks.getInt(i), ticPeaks.getInt(i));
        }
    }
}
