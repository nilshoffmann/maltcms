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
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
public class Chromatogram1DTest {

    /**
     *
     */
    @Rule
    public TemporaryFolder tf = new TemporaryFolder();

    /**
     *
     */
    @Test
    public void testRTtoIndexAssignmentOutOfRange() throws IOException {
        File file = tf.newFile("testFragment.cdf");
        FileFragment f = new FileFragment(file);
        IVariableFragment sat = f.addChild("scan_acquisition_time");
        double[] sats = new double[]{0.432, 0.442, 0.463, 0.472, 0.491, 1.21, 1.35};
        sat.setArray(Array.factory(sats));
        IVariableFragment si = f.addChild("scan_index");
        int[] sis = new int[]{0, 1, 2, 3, 4, 5, 6};
        si.setArray(Array.factory(sis));
        IVariableFragment ms = f.addChild("mass_values");
        double[] mvs = new double[]{74.241, 74.521, 70.4214, 75.869, 90.421, 61.515, 89.124};
        ms.setArray(Array.factory(mvs));
        IVariableFragment is = f.addChild("intensity_values");
        int[] ivs = new int[]{896, 89613, 8979694, 78585, 89563, 56704, 76124};
        is.setArray(Array.factory(ivs));
        f.save();
        Chromatogram1D chrom = new Chromatogram1D(f);
        double[] searchTimes = new double[]{0.31, 0.442, 1.55};
        for (int i = 0; i < searchTimes.length; i++) {
            double time = searchTimes[i];
            try {
                chrom.getIndexFor(time);
                if(i!=1) {
                    Assert.fail("Access of time out of bound should throw ArrayIndexOutOfBoundsException: time="+time);
                }
            }catch(ArrayIndexOutOfBoundsException ex) {
                    
            }
        }
    }
    
    /**
     *
     */
    @Test
    public void testRTtoIndexAssignmentInRange() throws IOException {
        File file = tf.newFile("testFragment.cdf");
        FileFragment f = new FileFragment(file);
        IVariableFragment sat = f.addChild("scan_acquisition_time");
        double[] sats = new double[]{0.432, 0.442, 0.463, 0.472, 0.491, 1.21, 1.35};
        sat.setArray(Array.factory(sats));
        IVariableFragment si = f.addChild("scan_index");
        int[] sis = new int[]{0, 1, 2, 3, 4, 5, 6};
        si.setArray(Array.factory(sis));
        IVariableFragment ms = f.addChild("mass_values");
        double[] mvs = new double[]{74.241, 74.521, 70.4214, 75.869, 90.421, 61.515, 89.124};
        ms.setArray(Array.factory(mvs));
        IVariableFragment is = f.addChild("intensity_values");
        int[] ivs = new int[]{896, 89613, 8979694, 78585, 89563, 56704, 76124};
        is.setArray(Array.factory(ivs));
        f.save();
        Chromatogram1D chrom = new Chromatogram1D(f);
        double[] searchTimes = new double[]{0.432, 0.477, 0.6, 1.21, 1.35};
        int[] expectedIndices = new int[]{0, 3, 4, 5, sats.length - 1};
        for (int i = 0; i < expectedIndices.length; i++) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Start Test {0}/{1}", new Object[]{i + 1, expectedIndices.length});
            double time = searchTimes[i];
            int expectedIndex = expectedIndices[i];
            Assert.assertEquals(expectedIndex, chrom.getIndexFor(time));//getIndexForRt(time, sats));
            Logger.getLogger(getClass().getName()).log(Level.INFO, "End Test {0}/{1}", new Object[]{i + 1, expectedIndices.length});
        }
    }

    /**
     *
     * @param rt
     * @param rts
     * @return
     */
    public int getIndexForRt(double rt, double[] rts) {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Query value: {0}", rt);
        int idx = Arrays.binarySearch(rts, rt);
        if (idx >= 0) {// exact hit
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Value at index: {0} = {1}", new Object[]{idx, rts[idx]});
            return idx;
        } else {// imprecise hit, find closest element
            int insertionPosition = (-idx) - 1;
            if (insertionPosition < 0) {
                throw new IllegalArgumentException("Insertion index is out of bounds! " + insertionPosition + "<" + 0);
            }
            if (insertionPosition >= rts.length) {
                throw new IllegalArgumentException("Insertion index is out of bounds! " + insertionPosition + ">=" + rts.length);
            }
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Would insert before {0}", insertionPosition);
            double current = rts[Math.min(rts.length - 1, insertionPosition)];
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Value at insertion position: {0}", current);
            double previous = rts[Math.max(0, insertionPosition - 1)];
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Value before insertion position: {0}", previous);
            if (Math.abs(rt - previous) <= Math.abs(
                    rt - current)) {
                int index = Math.max(0, insertionPosition - 1);
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Returning {0}", index);
                return index;
            } else {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Returning {0}", insertionPosition);
                return insertionPosition;
            }
        }
    }
}
