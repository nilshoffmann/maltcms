/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.datastructures.peak;

import maltcms.datastructures.peak.annotations.PeakAnnotation;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.FragmentTools;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import maltcms.datastructures.array.IFeatureVector;
import maltcms.tools.PublicMemberGetters;
import maltcms.tools.ArrayTools;
import ucar.ma2.Array;
import cross.exception.ResourceNotAvailableException;
import java.util.*;
import lombok.Data;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import ucar.ma2.*;
import ucar.nc2.Dimension;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 * Peak1D models a standard 1D chromatographic peak. If you want to model mass
 * spectral peaks over time, use a Peak1DGroup instance.
 *
 */
@Data
public class Peak1D implements Serializable, IFeatureVector, Iterable<Peak1D> {

    /**
     *
     */
    private static final long serialVersionUID = -8878754902179218064L;
    private int startIndex = -1;
    private int apexIndex = -1;
    private int stopIndex = -1;
    private double apexIntensity = Double.NaN;
    private double startTime = Double.NaN;
    private double stopTime = Double.NaN;
    private double apexTime = Double.NaN;
    private double area = Double.NaN;
    private double normalizedArea = Double.NaN;
    private double mw = Double.NaN;
    private double[] extractedIonCurrent;
    private double snr = Double.NaN;
    private String file = "";
    private PeakType peakType = PeakType.UNDEFINED;
    private String[] normalizationMethods = {"None"};
    private String name = "";
    private List<PeakAnnotation> peakAnnotations;
    private double baselineStartTime = Double.NaN;
    private double baselineStopTime = Double.NaN;
    private double baselineStartValue = Double.NaN;
    private double baselineStopValue = Double.NaN;

    public Peak1D() {
    }

    public Peak1D(int startIndex, int apexIndex, int stopIndex) {
        this();
        this.startIndex = startIndex;
        this.apexIndex = apexIndex;
        this.stopIndex = stopIndex;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
     */
    @Override
    public Array getFeature(String name) {
        PublicMemberGetters<Peak1D> pmg = new PublicMemberGetters<Peak1D>(
                getClass());
        Method m = pmg.getMethodForGetterName(name);
        if (m == null) {
            throw new ResourceNotAvailableException(
                    "Could not find compatible method for feature with name : "
                    + name);
        }
        try {
            Object o = m.invoke(this, new Object[]{});
            // if (o.getClass().isPrimitive()) {
            // throw new NotImplementedException();
            // }
            if (o == null) {
                throw new ResourceNotAvailableException(
                        "Can not create array representation of object for method: "
                        + name);
            }
            if (o instanceof Array) {
                return (Array) o;
            }
            return ArrayTools.factoryScalar(o);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        throw new ResourceNotAvailableException(
                "Could not find compatible method for feature with name : "
                + name);
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IFeatureVector#getFeatureNames()
     */
    @Override
    public List<String> getFeatureNames() {
        PublicMemberGetters<Peak1D> pmg = new PublicMemberGetters<Peak1D>(
                getClass(), "Feature", "FeatureNames");
        return Arrays.asList(pmg.getGetterNames());
    }

//	public static void main(String[] args) {
//		Peak1D p = new Peak1D(30, 35, 40, 123124.932, 12354);
//		System.out.println(p.getFeature("StartIndex"));
//		System.out.println(p.getFeature("StopIndex"));
//		System.out.println(p.getFeature("Area"));
//		System.out.println(p.getFeature("Intensity"));
//	}
    @Override
    public Iterator<Peak1D> iterator() {
        final Peak1D thisPeak = this;
        return new Iterator<Peak1D>() {

            private Peak1D peak = thisPeak;
            private boolean hasNext = true;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public Peak1D next() {
                Peak1D returnedPeak = this.peak;
                this.peak = null;
                hasNext = false;
                return returnedPeak;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public static List<Peak1D> fromFragment(IFileFragment ff) {

        final IVariableFragment peaks = ff.getChild(
                "tic_peaks");
        ArrayChar.D2 sourceFileName = (ArrayChar.D2) ff.getChild("peak_source_file").getArray();
        ArrayChar.D2 peakNames = (ArrayChar.D2) ff.getChild("peak_name").getArray();
        Array apexRt = ff.getChild(
                "peak_retention_time").getArray();
        Array startRT = ff.getChild(
                "peak_start_time").getArray();
        Array stopRT = ff.getChild(
                "peak_end_time").getArray();
        Array area = ff.getChild("peak_area").getArray();
        Array normalizedArea = ff.getChild("peak_area_normalized").getArray();
        Collection<String> normalizationMethods = FragmentTools.getStringArray(ff, "peak_area_normalization_methods");
        Array baseLineStartRT = ff.getChild("baseline_start_time").getArray();
        Array baseLineStopRT = ff.getChild("baseline_stop_time").getArray();
        Array baseLineStartValue = ff.getChild("baseline_start_value").getArray();
        Array baseLineStopValue = ff.getChild("baseline_stop_value").getArray();
        Array peakStartIndex = ff.getChild("peak_start_index").getArray();
        Array peakEndIndex = ff.getChild("peak_end_index").getArray();
        Array snr = ff.getChild("peak_signal_to_noise").getArray();
        ArrayChar.D2 peakType = (ArrayChar.D2) ff.getChild("peak_type").getArray();
        Array peakDetectionChannel = ff.getChild("peak_detection_channel").getArray();
        ArrayInt.D1 peakPositions = (ArrayInt.D1) peaks.getArray();
        ArrayList<Peak1D> peaklist = new ArrayList<Peak1D>(peakPositions.getShape()[0]);
        for (int i = 0; i < peakPositions.getShape()[0]; i++) {
            Peak1D p = new Peak1D();
            p.setNormalizationMethods(normalizationMethods.toArray(new String[normalizationMethods.size()]));
            p.setFile(sourceFileName.getString(0));
            p.setName(peakNames.getString(i));
            p.setApexTime(apexRt.getDouble(i));
            p.setStartTime(startRT.getDouble(i));
            p.setStopTime(stopRT.getDouble(i));
            p.setArea(area.getDouble(i));
            p.setNormalizedArea(normalizedArea.getDouble(i));
            p.setBaselineStartTime(baseLineStartRT.getDouble(i));
            p.setBaselineStopTime(baseLineStopRT.getDouble(i));
            p.setBaselineStartValue(baseLineStartValue.getDouble(i));
            p.setBaselineStopValue(baseLineStopValue.getDouble(i));
            p.setApexIndex(peakPositions.getInt(i));
            p.setStartIndex(peakStartIndex.getInt(i));
            p.setStopIndex(peakEndIndex.getInt(i));
            p.setSnr(snr.getDouble(i));
            p.setPeakType(PeakType.valueOf(peakType.getString(i)));
            p.setMw(peakDetectionChannel.getDouble(i));
            peaklist.add(p);
            i++;
        }
        return peaklist;
    }

    public static void append(IFileFragment ff, List<IPeakNormalizer> peakNormalizers, List<Peak1D> peaklist, Array filteredTrace, String peakVarName, String filteredTraceVarName) {
        if (!peaklist.isEmpty()) {
            final IVariableFragment peaks = new VariableFragment(ff,
                    peakVarName);
            final Dimension peak_number = new Dimension("peak_number",
                    peaklist.size(), true, false, false);
            peaks.setDimensions(new Dimension[]{peak_number});
            Array tic = ff.getChild("total_intensity").getArray();
            final IVariableFragment mai = new VariableFragment(ff,
                    filteredTraceVarName);
            mai.setArray(filteredTrace);
            IVariableFragment peakSourceFile = new VariableFragment(ff, "peak_source_file");
            IVariableFragment peakNames = new VariableFragment(ff, "peak_name");
            IVariableFragment peakStartIndex = new VariableFragment(ff, "peak_start_index");
            IVariableFragment peakEndIndex = new VariableFragment(ff, "peak_end_index");
            IVariableFragment peakRT = new VariableFragment(ff,
                    "peak_retention_time");
            IVariableFragment peakStartRT = new VariableFragment(ff,
                    "peak_start_time");
            IVariableFragment peakStopRT = new VariableFragment(ff,
                    "peak_end_time");
            IVariableFragment snr = new VariableFragment(ff, "peak_signal_to_noise");
            IVariableFragment peakType = new VariableFragment(ff, "peak_type");
            IVariableFragment peakDetectionChannel = new VariableFragment(ff, "peak_detection_channel");
            IVariableFragment peakArea = new VariableFragment(ff, "peak_area");
            IVariableFragment peakNormalizedArea = new VariableFragment(ff, "peak_area_normalized");
            IVariableFragment peakNormalizationMethod = new VariableFragment(ff, "peak_area_normalization_methods");
            IVariableFragment baseLineStartRT = new VariableFragment(ff, "baseline_start_time");
            IVariableFragment baseLineStopRT = new VariableFragment(ff, "baseline_stop_time");
            IVariableFragment baseLineStartValue = new VariableFragment(ff, "baseline_start_value");
            IVariableFragment baseLineStopValue = new VariableFragment(ff, "baseline_stop_value");
            Array peakPositions = new ArrayInt.D1(peaklist.size());
            ArrayInt.D1 peakStartIndexArray = new ArrayInt.D1(peaklist.size());
            ArrayInt.D1 peakEndIndexArray = new ArrayInt.D1(peaklist.size());
            ArrayChar.D2 names = cross.datastructures.tools.ArrayTools.createStringArray(
                    peaklist.size(), 1024);
            ArrayChar.D2 peakTypeArray = cross.datastructures.tools.ArrayTools.createStringArray(
                    peaklist.size(), 64);
            ArrayChar.D2 peakSourceFileArray = cross.datastructures.tools.ArrayTools.createStringArray(
                    1, 1024);
            ArrayChar.D2 normalizationMethodArray = cross.datastructures.tools.ArrayTools.createStringArray(
                    Math.max(1, peakNormalizers.size()), 1024);
            ArrayDouble.D1 apexRT = new ArrayDouble.D1(peaklist.size());
            ArrayDouble.D1 startRT = new ArrayDouble.D1(peaklist.size());
            ArrayDouble.D1 snrArray = new ArrayDouble.D1(peaklist.size());
            ArrayDouble.D1 peakDetectionChannelArray = new ArrayDouble.D1(peaklist.size());
            ArrayDouble.D1 stopRT = new ArrayDouble.D1(peaklist.size());
            ArrayDouble.D1 bstartRT = new ArrayDouble.D1(peaklist.size());
            ArrayDouble.D1 bstopRT = new ArrayDouble.D1(peaklist.size());
            ArrayDouble.D1 bstartV = new ArrayDouble.D1(peaklist.size());
            ArrayDouble.D1 bstopV = new ArrayDouble.D1(peaklist.size());
            ArrayDouble.D1 area = new ArrayDouble.D1(peaklist.size());
            ArrayDouble.D1 areaNormalized = new ArrayDouble.D1(peaklist.size());
            peakSourceFileArray.setString(0, ff.getName());
            if (peakNormalizers.isEmpty()) {
                normalizationMethodArray.setString(0, "None");
            } else {
                for (int i = 0; i < peakNormalizers.size(); i++) {
                    normalizationMethodArray.setString(i, peakNormalizers.get(i).getNormalizationName());
                }
            }
            int i = 0;
            for (Peak1D p : peaklist) {
                String name = p.getName();
                if (name == null || name.isEmpty()) {
                    names.setString(i, "" + i);
                } else {
                    names.setString(i, name);
                }
                peakTypeArray.setString(i, p.getPeakType().name());
                snrArray.set(i, p.getSnr());
                peakDetectionChannelArray.set(i, p.getMw());
                apexRT.setDouble(i, p.getApexTime());
                startRT.setDouble(i, p.getStartTime());
                stopRT.setDouble(i, p.getStopTime());
                area.setDouble(i, p.getArea());
                double normalizedArea = p.getArea();
                for (IPeakNormalizer normalizer : peakNormalizers) {
                    normalizedArea *= normalizer.getNormalizationFactor(ff, p);
                }
                areaNormalized.setDouble(i, normalizedArea);
                bstartRT.setDouble(i, p.getStartTime());
                if (p.getStartIndex() >= 0) {
                    bstartV.setDouble(i, tic.getDouble(p.getStartIndex()) - filteredTrace.getDouble(p.getStartIndex()));
                } else {
                    bstartV.setDouble(i, Double.NaN);
                }
                bstopRT.setDouble(i, p.getStopTime());
                if (p.getStopIndex() >= 0) {
                    bstopV.setDouble(i, tic.getDouble(p.getStopIndex()) - filteredTrace.getDouble(p.getStopIndex()));
                } else {
                    bstopV.setDouble(i, Double.NaN);
                }
                peakPositions.setInt(i, p.getApexIndex());
                peakStartIndexArray.setInt(i, p.getStartIndex());
                peakEndIndexArray.setInt(i, p.getStopIndex());
                i++;
            }
            peakSourceFile.setArray(peakSourceFileArray);
            peaks.setArray(peakPositions);
            peakStartIndex.setArray(peakStartIndexArray);
            peakEndIndex.setArray(peakEndIndexArray);
            peakNames.setArray(names);
            peakType.setArray(peakTypeArray);
            snr.setArray(snrArray);
            peakDetectionChannel.setArray(peakDetectionChannelArray);
            peakRT.setArray(apexRT);
            peakStartRT.setArray(startRT);
            peakStopRT.setArray(stopRT);
            peakArea.setArray(area);
            baseLineStartRT.setArray(bstartRT);
            baseLineStopRT.setArray(bstopRT);
            baseLineStartValue.setArray(bstartV);
            baseLineStopValue.setArray(bstopV);
            peakNormalizedArea.setArray(areaNormalized);
            peakNormalizationMethod.setArray(normalizationMethodArray);
        }
    }
}
