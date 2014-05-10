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
package maltcms.datastructures.peak;

import maltcms.datastructures.ms.*;
import cross.exception.ResourceNotAvailableException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
public class Peak1DTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    public List<Peak1D> createPeaks() {
        Peak1D[] scans = {
            //default scan
            new Peak1D(35, 40, 45),
            new Peak1D(89, 255, 352),
            new Peak1D(830, 1240, 2241)
        };
        return Arrays.asList(scans);
    }

    /**
     * Test of getFeature method, of class Peak1D.
     */
    @Test
    public void testGetFeature() {
        for (Peak1D goodScan : createPeaks()) {
            for (String featureName : goodScan.getFeatureNames()) {
                switch (featureName) {
                    case "PeakAnnotations":
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
        }
    }

    /**
     * Test of getFeatureNames method, of class Peak1D.
     */
    @Test
    public void testGetFeatureNames() {
        Peak1D scan = createPeaks().get(0);
        String[] members = new String[]{
            "StartIndex",
            "ApexIndex",
            "StopIndex",
            "ApexIntensity",
            "StartTime",
            "StopTime",
            "ApexTime",
            "Area",
            "NormalizedArea",
            "Mw",
            "ExtractedIonCurrent",
            "Snr",
            "File",
            "PeakType",
            "NormalizationMethods",
            "Name",
            "PeakAnnotations",
            "BaselineStartTime",
            "BaselineStopTime",
            "BaselineStartValue",
            "BaselineStopValue",
            "UniqueId"};
        Arrays.sort(members);
        List<String> c = scan.getFeatureNames();
        Collections.sort(c);
        for (int i = 0; i < members.length; i++) {
            Assert.assertEquals(c.get(i), members[i]);
        }
    }
}
