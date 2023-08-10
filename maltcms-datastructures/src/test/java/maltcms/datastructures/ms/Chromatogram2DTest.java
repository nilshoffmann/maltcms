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
package maltcms.datastructures.ms;

import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.test.LogMethodName;
import cross.test.SetupLogging;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import maltcms.datastructures.caches.SparseScanLineCache;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */

public class Chromatogram2DTest {
        
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Chromatogram2DTest.class);

    /**
     *
     */
    @Rule
    public TemporaryFolder tf = new TemporaryFolder();
    @Rule
    public SetupLogging sl = new SetupLogging();
    @Rule
    public LogMethodName lmn = new LogMethodName();

    /**
     *
     */
    @Test
    public void testRTtoIndexAssignmentInRange() throws IOException {
        File file = tf.newFile("testFragment.cdf");
        Chromatogram2D chrom = createTestChromatogram2D(file);
        double[] searchTimes = (double[]) chrom.getParent().getChild("scan_acquisition_time").getArray().get1DJavaArray(double.class);
        for (int i = 0; i < searchTimes.length; i++) {
            double time = searchTimes[i];
            try {
                int idx = chrom.getIndexFor(time);
                Assert.assertEquals(i, idx);
            } catch (ArrayIndexOutOfBoundsException ex) {

            }
        }
    }

    private Chromatogram2D createTestChromatogram2D(File file) {
        FileFragment f = new FileFragment(file);
        f.addChild("first_column_elution_time").setArray(
                Array.factory(new double[]{240, 240, 245, 245, 250, 255, 255})
        );
        f.addChild("second_column_elution_time").setArray(
                Array.factory(new double[]{0, 2.0, 0.25, 3.2, 4.6, 3.5, 4.9})
        );
        IVariableFragment sat = f.addChild("scan_acquisition_time");
        double[] sats = new double[]{240 + 0, 240 + 2.0, 245 + 0.25, 245 + 3.2, 250 + 4.6, 255 + 3.5, 255 + 4.9};
        sat.setArray(Array.factory(sats));
        IVariableFragment modTime = f.addChild("modulation_time");
        modTime.setArray(Array.factory(new double[]{5.0d}));
        IVariableFragment scanRate = f.addChild("scan_rate");
        scanRate.setArray(Array.factory(new double[]{100.0d}));
        IVariableFragment si = f.addChild("scan_index");
        int[] sis = new int[]{0, 1, 2, 3, 4, 5, 6};
        si.setArray(Array.factory(sis));
        IVariableFragment ms = f.addChild("mass_values");
        double[] mvs = new double[]{74.241, 74.521, 70.4214, 75.869, 90.421, 61.515, 89.124};
        ms.setArray(Array.factory(mvs));
        IVariableFragment is = f.addChild("intensity_values");
        int[] ivs = new int[]{896, 89613, 8979694, 78585, 89563, 56704, 76124};
        is.setArray(Array.factory(ivs));
        f.addChild("total_intensity").setArray(Array.factory(ivs));
        f.save();
        Chromatogram2D chrom = new Chromatogram2D(f);
        return chrom;
    }

    private Chromatogram2D createDenseTestChromatogram2D(File file) {
        FileFragment f = new FileFragment(file);
        f.addChild("first_column_elution_time").setArray(
                Array.factory(new double[]{240, 240, 245, 245, 250, 250, 255, 255})
        );
        f.addChild("second_column_elution_time").setArray(
                Array.factory(new double[]{0, 2.5, 0, 2.5, 0, 2.5, 0, 2.5})
        );
        IVariableFragment sat = f.addChild("scan_acquisition_time");
        double[] sats = new double[]{240 + 0, 240 + 2.5, 245 + 0, 245 + 2.5, 250 + 0, 250 + 2.5, 255 + 0, 255 + 2.5};
        sat.setArray(Array.factory(sats));
        IVariableFragment modTime = f.addChild("modulation_time");
        modTime.setArray(Array.factory(new double[]{5.0d}));
        IVariableFragment scanRate = f.addChild("scan_rate");
        scanRate.setArray(Array.factory(new double[]{0.4}));
        IVariableFragment si = f.addChild("scan_index");
        int[] sis = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
        si.setArray(Array.factory(sis));
        IVariableFragment ms = f.addChild("mass_values");
        double[] mvs = new double[]{74.241, 74.521, 70.4214, 75.869, 90.421, 61.515, 89.124, 98.872};
        ms.setArray(Array.factory(mvs));
        IVariableFragment is = f.addChild("intensity_values");
        int[] ivs = new int[]{896, 89613, 8979694, 78585, 89563, 56704, 76124, 962132};
        is.setArray(Array.factory(ivs));
        f.addChild("total_intensity").setArray(Array.factory(ivs));
        f.save();
        Chromatogram2D chrom = new Chromatogram2D(f);
        return chrom;
    }

    /**
     *
     */
    @Test
    public void testRTtoIndexAssignmentOutOfRange() throws IOException {
        File file = tf.newFile("testFragment.cdf");
        Chromatogram2D chrom = createTestChromatogram2D(file);
        double[] searchTimes = new double[]{240 - 1, 240 + 2.2, 245 - 0.25, 245 + 3.0, 250 + 4.7, 255 + 3.2, 255 + 6};
        int[] expectedIndices = new int[]{0, 1, 2, 3, 4, 5, 7};
        for (int i = 0; i < expectedIndices.length; i++) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Start Test {0}/{1}", new Object[]{i + 1, expectedIndices.length});
            double time = searchTimes[i];
            int expectedIndex = expectedIndices[i];
            if (i == expectedIndices.length - 1) {
                try {
                    Assert.assertEquals(expectedIndex, chrom.getIndexFor(time));
                    Assert.fail();
                } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                    //expected exception
                }
            } else {
                Assert.assertEquals(expectedIndex, chrom.getIndexFor(time));
            }
            Logger.getLogger(getClass().getName()).log(Level.INFO, "End Test {0}/{1}", new Object[]{i + 1, expectedIndices.length});
        }
    }

    @Test
    public void testBuildScan() throws IOException {
        sl.setLogLevel(Chromatogram2DTest.class, Level.INFO.toString());
        sl.setLogLevel(SparseScanLineCache.class, Level.INFO.toString());
        File file = tf.newFile("testFragment.cdf");
        Chromatogram2D chrom = createDenseTestChromatogram2D(file);
        int[] expectedIndices = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
        int[] firstColumnIndices = new int[]{0, 0, 1, 1, 2, 2, 3, 3};
        int[] secondColumnIndices = new int[]{0, 1, 0, 1, 0, 1, 0, 1};
        for (int i = 0; i < expectedIndices.length; i++) {
            //currently uses scan line cache, which requires a dense chromatogram
            log.info("Checking index {}", i);
            IScan2D scan = chrom.buildScan(i);
            log.info("Retrieved scan {}", scan);
            Assert.assertEquals(i, scan.getScanIndex());
            Assert.assertEquals(chrom.getScanAcquisitionTime().getDouble(i), scan.getScanAcquisitionTime(), 0.0d);
            Assert.assertEquals(chrom.getParent().getChild("first_column_elution_time").getArray().getDouble(i), scan.getFirstColumnScanAcquisitionTime(), 0.0d);
            Assert.assertEquals(chrom.getParent().getChild("second_column_elution_time").getArray().getDouble(i), scan.getSecondColumnScanAcquisitionTime(), 0.0d);
            Assert.assertEquals(firstColumnIndices[i], scan.getFirstColumnScanIndex());
            Assert.assertEquals(secondColumnIndices[i], scan.getSecondColumnScanIndex());
        }
    }
}
