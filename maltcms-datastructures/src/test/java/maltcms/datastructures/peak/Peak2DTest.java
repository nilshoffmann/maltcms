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

import cross.datastructures.fragments.FileFragment;
import cross.exception.ResourceNotAvailableException;
import cross.test.LogMethodName;
import cross.test.SetupLogging;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.Peak2D.Peak2DBuilder;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import maltcms.tools.ArrayTools;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class Peak2DTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public SetupLogging sl = new SetupLogging();
    @Rule
    public LogMethodName lmn = new LogMethodName();

    public List<Peak2D> createPeaks() {
        Peak2DBuilder builder1 = Peak2D.builder2D();
        builder1.
            index(0).
            startIndex(35).
            apexIndex(40).
            stopIndex(45).
            firstRetTime(3400.0d).
            secondRetTime(2.21d);
        PeakArea2D peakArea1 = new PeakArea2D(new Point(5,200), ArrayTools.randomUniform(100, 50, 100000), 9869123.987, 40, 500);
        builder1.
            peakArea(peakArea1).
            area(peakArea1.getAreaIntensity());
        Peak2D p1 = builder1.build();
        Peak2DBuilder builder2 = Peak2D.builder2D();
        builder2.
            index(1).
            startIndex(57).
            apexIndex(60).
            stopIndex(64).
            firstRetTime(3787.0d).
            secondRetTime(1.752d);
        PeakArea2D peakArea2 = new PeakArea2D(new Point(7,141), ArrayTools.randomUniform(100, 50, 100000), 21415.125, 50, 500);
        builder2.
            peakArea(peakArea2).
            area(peakArea2.getAreaIntensity());
        Peak2D p2 = builder2.build();
        Peak2DBuilder builder3 = Peak2D.builder2D();
        builder3.
            index(2).
            startIndex(87).
            apexIndex(92).
            stopIndex(110).
            firstRetTime(5232.0d).
            secondRetTime(3.75d);
        PeakArea2D peakArea3 = new PeakArea2D(new Point(14,345), ArrayTools.randomUniform(100, 50, 100000), 567613.155, 50, 500);
        builder3.
            peakArea(peakArea3).
            area(peakArea3.getAreaIntensity());
        Peak2D p3 = builder3.build();
        Peak2D[] scans = {
            p1,
            p2,
            p3
        };
        return Arrays.asList(scans);
    }

    /**
     * Test of getFeature method, of class Peak2D.
     */
    @Test
    public void testGetFeature() {
        for (Peak2D goodScan : createPeaks()) {
            for (String featureName : goodScan.getFeatureNames()) {
                log.info("Retrieving feature {}", featureName);
                switch (featureName) {
                    case "FirstScanIndex":
                    case "SecondScanIndex":
                    case "Identification":
                    case "PeakArea":
                    case "Reference":
                    case "Names":
                        log.info("Skipping PeakArea2D specific feature " + featureName);
                        break;
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
     * Test of getFeatureNames method, of class Peak2D.
     */
    @Test
    public void testGetFeatureNames() {
        Peak2D scan = createPeaks().get(0);
        String[] members = new String[]{
            "ApexIndex",
            "ApexIntensity",
            "ApexTime",
            "Area",
            "BaselineStartTime",
            "BaselineStartValue",
            "BaselineStopTime",
            "BaselineStopValue",
            "ExtractedIonCurrent",
            "File",
            "FirstRetTime",
            "FirstScanIndex",
            "Index",
            "Mw",
            "Name",
            "NormalizationMethods",
            "NormalizedArea",
            "PeakAnnotations",
            "PeakArea",
            "PeakType",
            "RetentionTime",
            "SecondRetTime",
            "SecondScanIndex",
            "Snr",
            "StartIndex",
            "StartTime",
            "StopIndex",
            "StopTime",
            "UniqueId"
        };
        Arrays.sort(members);
        List<String> c = scan.getFeatureNames();
        Collections.sort(c);
        for (int i = 0; i < c.size(); i++) {
            log.info("feature name: {}", c.get(i));
            Assert.assertEquals(c.get(i), members[i]);
        }
    }

    @Test
    public void testSave() throws IOException {
        File outputDir = temporaryFolder.newFolder("Peak2DExportTest");
        FileFragment f = new FileFragment(outputDir, "peak2Dtest.cdf");
        List<Peak2D> peaks = createPeaks();
        for (Peak2D peak : peaks) {
            peak.setFile(f.getName());
        }
        Peak2D.append2D(f, new LinkedList<IPeakNormalizer>(), peaks, "tic_peaks");
        f.save();
        List<Peak2D> peaksRestored = Peak2D.fromFragment2D(f, "tic_peaks");
        Assert.assertEquals(peaks.size(), peaksRestored.size());
        for (int i = 0; i < peaks.size(); i++) {
            Peak2D original = peaks.get(i);
            Peak2D restored = peaksRestored.get(i);
            Assert.assertEquals(original, restored);
            Assert.assertEquals(original.getIndex(), restored.getIndex());
        }
    }
}
