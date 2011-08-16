/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.datastructures.peak;

import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.tools.MathTools;
import java.awt.Point;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import maltcms.datastructures.ms.IChromatogram2D;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author nils
 */
public class PeakFactory {
    
    public static Peak1D createPeak1DEicRaw(File file, int startIndex, int apexIndex, int stopIndex, double startMass, double stopMass) {
        return createPeak1DEicRaw(new FileFragment(file), startIndex, apexIndex, stopIndex, startMass, stopMass);
    }
    
    public static Peak1D createPeak1DEicRaw(IFileFragment fragment, int startIndex, int apexIndex, int stopIndex, double startMass, double stopMass) {
        Peak1D peak = new Peak1D();
        peak.setPeakType(PeakType.EIC_RAW);
        peak.setExtractedIonCurrent((double[])MaltcmsTools.getEIC(fragment, startMass, stopMass, false, false, startIndex, stopIndex-startIndex).get1DJavaArray(double.class));
        peak.setArea(MathTools.sum(peak.getExtractedIonCurrent()));
        peak.setApexIntensity(peak.getExtractedIonCurrent()[apexIndex-startIndex]);
        peak.setFile(fragment.getAbsolutePath());
        peak.setStartIndex(startIndex);
        peak.setApexIndex(apexIndex);
        peak.setStopIndex(stopIndex);
        peak.setStartTime(MaltcmsTools.getScanAcquisitionTime(fragment, startIndex));
        peak.setApexTime(MaltcmsTools.getScanAcquisitionTime(fragment, apexIndex));
        peak.setStopTime(MaltcmsTools.getScanAcquisitionTime(fragment, stopIndex));
        return peak;
    }
    
    public static Peak1D createPeak1DEicBinned(File file, int startIndex, int apexIndex, int stopIndex, double startMass, double stopMass) {
        return createPeak1DEicBinned(new FileFragment(file), startIndex, apexIndex, stopIndex, startMass, stopMass);
    }
    
    public static Peak1D createPeak1DEicBinned(IFileFragment fragment, int startIndex, int apexIndex, int stopIndex, double startMass, double stopMass) {
        Peak1D peak = new Peak1D();
        peak.setPeakType(PeakType.EIC_BINNED);
        peak.setExtractedIonCurrent((double[])MaltcmsTools.getBinnedEIC(fragment, startMass, stopMass, false, false, startIndex, stopIndex-startIndex).get1DJavaArray(double.class));
        peak.setArea(MathTools.sum(peak.getExtractedIonCurrent()));
        peak.setApexIntensity(peak.getExtractedIonCurrent()[apexIndex-startIndex]);
        peak.setFile(fragment.getAbsolutePath());
        peak.setStartIndex(startIndex);
        peak.setApexIndex(apexIndex);
        peak.setStopIndex(stopIndex);
        peak.setStartTime(MaltcmsTools.getScanAcquisitionTime(fragment, startIndex));
        peak.setApexTime(MaltcmsTools.getScanAcquisitionTime(fragment, apexIndex));
        peak.setStopTime(MaltcmsTools.getScanAcquisitionTime(fragment, stopIndex));
        return peak;
    }
    
    public static Peak1D createPeak1DTic(File file, int startIndex, int apexIndex, int stopIndex) {
        return createPeak1DTic(new FileFragment(file), startIndex, apexIndex, stopIndex);
    }
    
    public static Peak1D createPeak1DTic(IFileFragment fragment, int startIndex, int apexIndex, int stopIndex) {
        Peak1D peak = new Peak1D();
        peak.setPeakType(PeakType.TIC);
        try {
            peak.setExtractedIonCurrent((double[])fragment.getChild("total_intensity").getArray().section(new int[]{startIndex}, new int[]{stopIndex-startIndex}).get1DJavaArray(double.class));
        } catch (InvalidRangeException ex) {
            Logger.getLogger(PeakFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        peak.setArea(MathTools.sum(peak.getExtractedIonCurrent()));
        peak.setApexIntensity(peak.getExtractedIonCurrent()[apexIndex-startIndex]);
        peak.setFile(fragment.getAbsolutePath());
        peak.setStartIndex(startIndex);
        peak.setApexIndex(apexIndex);
        peak.setStopIndex(stopIndex);
        peak.setStartTime(MaltcmsTools.getScanAcquisitionTime(fragment, startIndex));
        peak.setApexTime(MaltcmsTools.getScanAcquisitionTime(fragment, apexIndex));
        peak.setStopTime(MaltcmsTools.getScanAcquisitionTime(fragment, stopIndex));
        return peak;
    }
    
    public static Peak2D createPeak2DTic(IChromatogram2D chromatogram2D, PeakArea2D peakArea2D) {
        Peak2D peak2D = new Peak2D();
        peak2D.setPeakType(PeakType.TIC);
        for(Point point:peakArea2D) {
            chromatogram2D.getScan2D(point.x, point.y);
        }
        peak2D.setExtractedIonCurrent(new double[]{chromatogram2D.getScan2D(p.x, p.y).getTotalIntensity()});
        peak2D.setArea(peak2D.getExtractedIonCurrent()[0]);
        peak2D.setA;
    }
    
}
