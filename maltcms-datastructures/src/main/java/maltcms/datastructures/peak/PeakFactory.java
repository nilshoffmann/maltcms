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
package maltcms.datastructures.peak;

import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.tools.MathTools;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import maltcms.datastructures.peak.Peak1D.Peak1DBuilder;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.InvalidRangeException;

/**
 * <p>
 * PeakFactory class.</p>
 *
 * @author Nils Hoffmann
 *
 */
public class PeakFactory {

    /**
     * <p>
     * createPeak1DEicRaw.</p>
     *
     * @param file a {@link java.io.File} object.
     * @param startIndex a int.
     * @param apexIndex a int.
     * @param stopIndex a int.
     * @param startMass a double.
     * @param stopMass a double.
     * @return a {@link maltcms.datastructures.peak.Peak1D} object.
     */
    public static Peak1D createPeak1DEicRaw(File file, int startIndex, int apexIndex, int stopIndex, double startMass, double stopMass) {
        return createPeak1DEicRaw(new FileFragment(file), startIndex, apexIndex, stopIndex, startMass, stopMass);
    }

    /**
     * <p>
     * createPeak1DEicRaw.</p>
     *
     * @param fragment a {@link cross.datastructures.fragments.IFileFragment}
     * object.
     * @param startIndex a int.
     * @param apexIndex a int.
     * @param stopIndex a int.
     * @param startMass a double.
     * @param stopMass a double.
     * @return a {@link maltcms.datastructures.peak.Peak1D} object.
     */
    public static Peak1D createPeak1DEicRaw(IFileFragment fragment, int startIndex, int apexIndex, int stopIndex, double startMass, double stopMass) {
        Peak1DBuilder builder = Peak1D.builder1D();
        double[] eic = (double[]) MaltcmsTools.getEIC(fragment, startMass, stopMass, false, false, startIndex, stopIndex - startIndex).get1DJavaArray(double.class);
        builder.
            peakType(PeakType.EIC_RAW).
            extractedIonCurrent(eic).
            area(MathTools.sum(eic)).
            apexIntensity(eic[apexIndex - startIndex]).
            file(fragment.getUri().toString()).
            startIndex(startIndex).
            apexIndex(apexIndex).
            stopIndex(stopIndex).
            startTime(MaltcmsTools.getScanAcquisitionTime(fragment, startIndex)).
            apexTime(MaltcmsTools.getScanAcquisitionTime(fragment, apexIndex)).
            stopTime(MaltcmsTools.getScanAcquisitionTime(fragment, stopIndex));
        return builder.build();
    }

//    public static Peak1D createPeak1DEicBinned(File file, int startIndex, int apexIndex, int stopIndex, double startMass, double stopMass) {
//        return createPeak1DEicBinned(new FileFragment(file), startIndex, apexIndex, stopIndex, startMass, stopMass);
//    }
//    public static Peak1D createPeak1DEicBinned(IFileFragment fragment, int startIndex, int apexIndex, int stopIndex, double startMass, double stopMass) {
//        Peak1D peak = new Peak1D();
//        peak.setPeakType(PeakType.EIC_BINNED);
//        peak.setExtractedIonCurrent((double[])MaltcmsTools.getBinnedEIC(fragment, startMass, stopMass, false, false, startIndex, stopIndex-startIndex).get1DJavaArray(double.class));
//        peak.setArea(MathTools.sum(peak.getExtractedIonCurrent()));
//        peak.setApexIntensity(peak.getExtractedIonCurrent()[apexIndex-startIndex]);
//        peak.setFile(fragment.getAbsolutePath());
//        peak.setStartIndex(startIndex);
//        peak.setApexIndex(apexIndex);
//        peak.setStopIndex(stopIndex);
//        peak.setStartTime(MaltcmsTools.getScanAcquisitionTime(fragment, startIndex));
//        peak.setApexTime(MaltcmsTools.getScanAcquisitionTime(fragment, apexIndex));
//        peak.setStopTime(MaltcmsTools.getScanAcquisitionTime(fragment, stopIndex));
//        return peak;
//    }
    /**
     * <p>
     * createPeak1DTic.</p>
     *
     * @param file a {@link java.io.File} object.
     * @param startIndex a int.
     * @param apexIndex a int.
     * @param stopIndex a int.
     * @return a {@link maltcms.datastructures.peak.Peak1D} object.
     */
    public static Peak1D createPeak1DTic(File file, int startIndex, int apexIndex, int stopIndex) {
        return createPeak1DTic(new FileFragment(file), startIndex, apexIndex, stopIndex);
    }

    /**
     * <p>
     * createPeak1DTic.</p>
     *
     * @param fragment a {@link cross.datastructures.fragments.IFileFragment}
     * object.
     * @param startIndex a int.
     * @param apexIndex a int.
     * @param stopIndex a int.
     * @return a {@link maltcms.datastructures.peak.Peak1D} object.
     */
    public static Peak1D createPeak1DTic(IFileFragment fragment, int startIndex, int apexIndex, int stopIndex) {
        Peak1DBuilder builder = Peak1D.builder1D();
        builder.peakType(PeakType.TIC_RAW);
        double[] eic = {0};
        try {
            eic = (double[]) fragment.getChild("total_intensity").getArray().section(new int[]{startIndex}, new int[]{stopIndex - startIndex}).get1DJavaArray(double.class);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(PeakFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        builder.extractedIonCurrent(eic);
        builder.
            area(MathTools.sum(eic)).
            apexIntensity(eic[apexIndex - startIndex]).
            file(fragment.getUri().toString()).
            startIndex(startIndex).
            apexIndex(apexIndex).
            stopIndex(stopIndex).
            startTime(MaltcmsTools.getScanAcquisitionTime(fragment, startIndex)).
            apexTime(MaltcmsTools.getScanAcquisitionTime(fragment, apexIndex)).
            stopTime(MaltcmsTools.getScanAcquisitionTime(fragment, stopIndex));
        return builder.build();
    }
//    public static Peak2D createPeak2DTic(IFileFragment fragment, ) {
//        Peak2D peak2D = new Peak2D();
//        peak2D.setPeakType(PeakType.TIC);
//        for(Point point:peakArea2D) {
//            chromatogram2D.getScan2D(point.x, point.y);
//        }
//        peak2D.setExtractedIonCurrent(new double[]{chromatogram2D.getScan2D(p.x, p.y).getTotalIntensity()});
//        peak2D.setArea(peak2D.getExtractedIonCurrent()[0]);
//        peak2D.setA;
//    }
}
