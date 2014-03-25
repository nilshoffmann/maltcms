/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2013, The authors of Maltcms. All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
public class Scan2DTest extends Scan1DTest {

    @Override
    public List<IScan> createScans() {
        IScan[] scans = {
            //default scan
            new Scan2D(
            Array.factory(new double[]{50.213, 58.997, 82.786}),
            Array.factory(new int[]{9870, 988, 76234}),
            0,
            782.24),
            new Scan2D(
            Array.factory(new double[]{50.213, 58.997, 82.786}),
            Array.factory(new int[]{9870, 988, 76234}),
            1,
            783.24, -1, -1, 23.123, 1.24, (short) 1),
            new Scan2D(
            Array.factory(new double[]{14.57, 24.13, 42.778}),
            Array.factory(new int[]{972, 2788, 145}),
            2,
            783.24, -1, -1, 23.123, 1.24, (short) 2, 1, 82.786, 76234)
        };
        return Arrays.asList(scans);
    }

    /**
     * Test of getFeatureNames method, of class Scan2D.
     */
    @Test
    @Override
    public void testGetFeatureNames() {
        IScan scan = createScans().get(0);
        Assert.assertEquals(scan.getFeatureNames(), Arrays.asList(
                new String[]{"mass_values", "intensity_values",
                    "scan_index", "scan_acquisition_time", "total_intensity",
                    "first_column_scan_index", "second_column_scan_index",
                    "first_column_elution_time", "second_column_elution_time",
                    "ms_level", "precursor_charge", "precursor_mz",
                    "precursor_intensity"}));
    }

    /**
     * Test of externalization of class Scan2D.
     */
    @Test
    @Override
    public void testReadWriteExternal() throws Exception {
        List<IScan> scans = createScans();
        for (IScan scan : scans) {
            File f = temporaryFolder.newFile();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
                oos.writeObject(scan);
            } catch (IOException ioex) {
            }
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(f));
                IScan o = (IScan) ois.readObject();
                for (String feature : scan.getFeatureNames()) {
                    Assert.assertEquals(scan.getFeature(feature), o.getFeature(feature));
                }
                Assert.assertEquals(scan.getUniqueId(), o.getUniqueId());
                Scan2D lhs = (Scan2D) scan;
                Scan2D rhs = (Scan2D) o;
                Assert.assertEquals(lhs.getPrecursorCharge(), rhs.getPrecursorCharge(), 0.0);
                Assert.assertEquals(lhs.getPrecursorIntensity(), rhs.getPrecursorIntensity(), 0.0);
                Assert.assertEquals(lhs.getPrecursorMz(), rhs.getPrecursorMz(), 0.0);
                Assert.assertEquals(lhs.getScanAcquisitionTime(), rhs.getScanAcquisitionTime(), 0.0);
                Assert.assertEquals(lhs.getTotalIntensity(), rhs.getTotalIntensity(), 0.0);
                Assert.assertEquals(lhs.getScanIndex(), rhs.getScanIndex());
                Assert.assertEquals(lhs.getFirstColumnScanAcquisitionTime(), rhs.getFirstColumnScanAcquisitionTime());
                Assert.assertEquals(lhs.getSecondColumnScanAcquisitionTime(), rhs.getSecondColumnScanAcquisitionTime());
                Assert.assertEquals(lhs.getFirstColumnScanIndex(), rhs.getFirstColumnScanIndex());
                Assert.assertEquals(lhs.getSecondColumnScanIndex(), rhs.getSecondColumnScanIndex());

            } catch (IOException ioex) {

            } finally {
                if (ois != null) {
                    ois.close();
                }
            }
        }
    }
}
