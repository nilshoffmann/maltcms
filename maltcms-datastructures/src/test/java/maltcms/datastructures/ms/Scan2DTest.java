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

import cross.exception.ResourceNotAvailableException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 *
 * @author Nils Hoffmann
 */
public class Scan2DTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     *
     * @param a
     * @param b
     */
    public void checkArraysEqual(Array a, Array b) {
        Assert.assertNotNull(a);
        Assert.assertNotNull(b);
        int[] shape1 = a.getShape();
        int[] shape2 = b.getShape();
        Assert.assertEquals(shape1.length, shape2.length);
        for(int i = 0; i< shape1.length; i++) {
            Assert.assertEquals(shape1[i], shape2[i]);
        }
        junit.framework.Assert.assertEquals(a.getElementType(), b.getElementType());
        IndexIterator itera = a.getIndexIterator();
        IndexIterator iterb = b.getIndexIterator();
        while (itera.hasNext() && iterb.hasNext()) {
            junit.framework.Assert.assertEquals(itera.getObjectNext(), iterb.getObjectNext());
        }
    }

    public List<IScan> createScans() {
        IScan[] scans = {
            //default scan
            new Scan2D(
            Array.factory(new double[]{50.213, 58.997, 82.786}),
            Array.factory(new int[]{9870, 988, 76234}),
            0,
            782.24, -1, -1, 21.532, 3.13),
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
                for (String featureName : scan.getFeatureNames()) {
                    switch (featureName) {
                        case "precursor_charge":
                        case "precursor_mz":
                        case "precursor_intensity":
                            try {
                                Array a = o.getFeature(featureName);
                            } catch (ResourceNotAvailableException rnae) {

                            }
                            break;
                        default:
                            Array a = scan.getFeature(featureName);
                            Array b = o.getFeature(featureName);
                            checkArraysEqual(a, b);
                    }
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
                Assert.assertNotEquals(lhs.getFirstColumnScanAcquisitionTime(), 0.0, 0.0);
                Assert.assertNotEquals(rhs.getFirstColumnScanAcquisitionTime(), 0.0, 0.0);
                Assert.assertEquals(lhs.getFirstColumnScanAcquisitionTime(), rhs.getFirstColumnScanAcquisitionTime(), 0.0);
                Assert.assertNotEquals(lhs.getSecondColumnScanAcquisitionTime(), 0.0, 0.0);
                Assert.assertNotEquals(rhs.getSecondColumnScanAcquisitionTime(), 0.0, 0.0);
                Assert.assertEquals(lhs.getSecondColumnScanAcquisitionTime(), rhs.getSecondColumnScanAcquisitionTime(), 0.0);
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
