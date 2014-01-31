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

import cross.exception.ConstraintViolationException;
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

/**
 *
 * @author Nils Hoffmann
 */
public class Scan1DTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    public List<IScan> createScans() {
        IScan[] scans = {
            //default scan
            new Scan1D(
            Array.factory(new double[]{50.213, 58.997, 82.786}),
            Array.factory(new int[]{9870, 988, 76234}),
            0,
            782.24),
            new Scan1D(
            Array.factory(new double[]{50.213, 58.997, 82.786}),
            Array.factory(new int[]{9870, 988, 76234}),
            1,
            783.24, (short) 1),
            new Scan1D(
            Array.factory(new double[]{14.57, 24.13, 42.778}),
            Array.factory(new int[]{972, 2788, 145}),
            2,
            783.24, (short) 2, 1, 82.786, 76234)
        };
        return Arrays.asList(scans);
    }

    @Test
    public void testConstraintViolation() {
        try {
            Scan1D badScan1 = new Scan1D(
                Array.factory(new double[]{50.213, 58.997, 82.786}),
                Array.factory(new int[]{9870, 988, 76234}),
                -5,
                782.24);
            Assert.fail();
        } catch (ConstraintViolationException cve) {
        }
        try {
            Scan1D badScan2 = new Scan1D(
                Array.factory(new double[]{50.213, 58.997, 82.786}),
                Array.factory(new int[]{9870, 988, 76234}),
                1,
                783.24, (short) 0);
            Assert.fail();
        } catch (ConstraintViolationException cve) {
        }
        try {
            Scan1D badScan3 = new Scan1D(
                Array.factory(new double[]{14.57, 24.13, 42.778}),
                Array.factory(new int[]{972, 2788, 145}),
                2,
                783.24, (short) 1, 1, 82.786, 76234);
            Assert.fail();
        } catch (ConstraintViolationException cve) {
        }
    }

    /**
     * Test of getFeature method, of class Scan1D.
     */
    @Test
    public void testGetFeature() {
        for (IScan goodScan : createScans()) {
            for (String featureName : goodScan.getFeatureNames()) {
                switch (featureName) {
                    case "precursor_charge":
                    case "precursor_mz":
                    case "precursor_intensity":
                        try {
                            Array a = goodScan.getFeature(featureName);
                        } catch (ResourceNotAvailableException rnae) {

                        }
                        break;
                    default:
                        Array a = goodScan.getFeature(featureName);
                        Assert.assertNotNull(a);
                }
            }
            IScan1D scan1D = (IScan1D) goodScan;
            Assert.assertNotNull(scan1D.getIntensities());
            Assert.assertNotNull(scan1D.getMasses());
        }
    }

    /**
     * Test of getFeatureNames method, of class Scan1D.
     */
    @Test
    public void testGetFeatureNames() {
        IScan scan = createScans().get(0);
        Assert.assertEquals(scan.getFeatureNames(), Arrays.asList(new String[]{"mass_values", "intensity_values",
            "scan_index", "scan_acquisition_time", "total_intensity", "ms_level", "precursor_charge", "precursor_mz", "precursor_intensity"}));
    }

    /**
     * Test of getIntensities method, of class Scan1D.
     */
    @Test
    public void testGetIntensities() {
        for (IScan scan : createScans()) {
            Assert.assertNotNull(scan.getIntensities());
        }
    }

    /**
     * Test of getMasses method, of class Scan1D.
     */
    @Test
    public void testGetMasses() {
        for (IScan scan : createScans()) {
            Assert.assertNotNull(scan.getMasses());
        }
    }

    /**
     * Test of getScanAcquisitionTime method, of class Scan1D.
     */
    @Test
    public void testGetScanAcquisitionTime() {
        for (IScan scan : createScans()) {
            Assert.assertFalse(Double.isNaN(scan.getScanAcquisitionTime()));
            Assert.assertFalse(Double.isInfinite(scan.getScanAcquisitionTime()));
        }
    }

    /**
     * Test of getScanIndex method, of class Scan1D.
     */
    @Test
    public void testGetScanIndex() {
        for (IScan scan : createScans()) {
            Assert.assertFalse(scan.getScanIndex() < 0);
        }
    }

    /**
     * Test of getTotalIntensity method, of class Scan1D.
     */
    @Test
    public void testGetTotalIntensity() {
        for (IScan scan : createScans()) {
            Assert.assertFalse(Double.isNaN(scan.getTotalIntensity()));
            Assert.assertFalse(Double.isInfinite(scan.getTotalIntensity()));
        }
    }

    /**
     * Test of getUniqueId method, of class Scan1D.
     */
    @Test
    public void testGetUniqueId() {
        IScan ref = null;
        for (IScan scan : createScans()) {
            if (ref == null) {
                ref = scan;
            } else {
                Assert.assertNotEquals(ref.getUniqueId(), scan.getUniqueId());
            }
        }
    }

    /**
     * Test of externalization of class Scan1D.
     */
    @Test
    public void testReadWriteExternal() throws Exception {
        List<IScan> scans = createScans();
        for (IScan scan : scans) {
            File f = temporaryFolder.newFile();
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(new FileOutputStream(f));
                oos.writeObject(scan);
            } catch (IOException ioex) {
            } finally {
                if (oos != null) {
                    oos.close();
                }
            }
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(f));
                IScan o = (IScan) ois.readObject();
                for (String feature : scan.getFeatureNames()) {
                    Assert.assertEquals(scan.getFeature(feature), o.getFeature(feature));
                }
                Assert.assertEquals(scan.getUniqueId(), o.getUniqueId());
                Scan1D lhs = (Scan1D) scan;
                Scan1D rhs = (Scan1D) o;
                Assert.assertEquals(lhs.getPrecursorCharge(), rhs.getPrecursorCharge(), 0.0);
                Assert.assertEquals(lhs.getPrecursorIntensity(), rhs.getPrecursorIntensity(), 0.0);
                Assert.assertEquals(lhs.getPrecursorMz(), rhs.getPrecursorMz(), 0.0);
                Assert.assertEquals(lhs.getScanAcquisitionTime(), rhs.getScanAcquisitionTime(), 0.0);
                Assert.assertEquals(lhs.getTotalIntensity(), rhs.getTotalIntensity(), 0.0);
                Assert.assertEquals(lhs.getScanIndex(), rhs.getScanIndex());
            } catch (IOException ioex) {

            } finally {
                if (ois != null) {
                    ois.close();
                }
            }
        }
    }

    /**
     * Test of getMsLevel method, of class Scan1D.
     */
    @Test
    public void testGetMsLevel() {
        List<IScan> scans = createScans();
        IScan ms1Scan = scans.get(0);
        Assert.assertTrue(ms1Scan.getMsLevel() == (short) 1);
        IScan ms1Scan2 = scans.get(1);
        Assert.assertTrue(ms1Scan2.getMsLevel() == (short) 1);
        IScan ms2Scan = scans.get(2);
        Assert.assertTrue(ms2Scan.getMsLevel() == (short) 2);
    }

    /**
     * Test of getPrecursorCharge method, of class Scan1D.
     */
    @Test
    public void testGetPrecursorCharge() {
        List<IScan> scans = createScans();
        IScan ms1Scan = scans.get(0);
        try {
            ms1Scan.getFeature("precursor_charge");
        } catch (ResourceNotAvailableException rnae) {

        }
        Assert.assertTrue(Double.isNaN(ms1Scan.getPrecursorCharge()));

        IScan ms1Scan2 = scans.get(1);
        try {
            ms1Scan2.getFeature("precursor_charge");
        } catch (ResourceNotAvailableException rnae) {

        }
        Assert.assertTrue(Double.isNaN(ms1Scan2.getPrecursorCharge()));
        IScan ms2Scan = scans.get(2);
        try {
            ms2Scan.getFeature("precursor_charge");
        } catch (ResourceNotAvailableException rnae) {
            Assert.fail();
        }
        Assert.assertFalse(Double.isNaN(ms2Scan.getPrecursorCharge()));
    }

    /**
     * Test of getPrecursorMz method, of class Scan1D.
     */
    @Test
    public void testGetPrecursorMz() {
        List<IScan> scans = createScans();
        IScan ms1Scan = scans.get(0);
        try {
            ms1Scan.getFeature("precursor_mz");
        } catch (ResourceNotAvailableException rnae) {

        }
        Assert.assertTrue(Double.isNaN(ms1Scan.getPrecursorMz()));

        IScan ms1Scan2 = scans.get(1);
        try {
            ms1Scan2.getFeature("precursor_mz");
        } catch (ResourceNotAvailableException rnae) {

        }
        Assert.assertTrue(Double.isNaN(ms1Scan2.getPrecursorMz()));
        IScan ms2Scan = scans.get(2);
        try {
            ms2Scan.getFeature("precursor_mz");
        } catch (ResourceNotAvailableException rnae) {
            Assert.fail();
        }
        Assert.assertFalse(Double.isNaN(ms2Scan.getPrecursorMz()));
    }

    /**
     * Test of getPrecursorIntensity method, of class Scan1D.
     */
    @Test
    public void testGetPrecursorIntensity() {
        List<IScan> scans = createScans();
        IScan ms1Scan = scans.get(0);
        try {
            ms1Scan.getFeature("precursor_intensity");
        } catch (ResourceNotAvailableException rnae) {

        }
        Assert.assertTrue(Double.isNaN(ms1Scan.getPrecursorIntensity()));

        IScan ms1Scan2 = scans.get(1);
        try {
            ms1Scan2.getFeature("precursor_intensity");
        } catch (ResourceNotAvailableException rnae) {

        }
        Assert.assertTrue(Double.isNaN(ms1Scan2.getPrecursorIntensity()));
        IScan ms2Scan = scans.get(2);
        try {
            ms2Scan.getFeature("precursor_intensity");
        } catch (ResourceNotAvailableException rnae) {
            Assert.fail();
        }
        Assert.assertFalse(Double.isNaN(ms2Scan.getPrecursorIntensity()));
    }

}
