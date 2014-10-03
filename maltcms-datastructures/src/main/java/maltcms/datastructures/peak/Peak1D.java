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

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.FragmentTools;
import cross.exception.ResourceNotAvailableException;
import cross.tools.PublicMemberGetters;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.peak.annotations.PeakAnnotation;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import maltcms.tools.ArrayTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;

/**
 * <p>Peak1D class.</p>
 *
 * @author Nils Hoffmann
 *
 * Peak1D models a standard 1D chromatographic peak. If you want to model mass
 * spectral peaks over time, use a Peak1DGroup instance.
 * 
 */
@Data
@Slf4j
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
    private final UUID uniqueId = UUID.randomUUID();

    /**
     * <p>Constructor for Peak1D.</p>
     */
    public Peak1D() {

    }

    /**
     * <p>Constructor for Peak1D.</p>
     *
     * @param startIndex a int.
     * @param apexIndex a int.
     * @param stopIndex a int.
     */
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
    /** {@inheritDoc} */
    @Override
    public Array getFeature(String name) {
        if (name.equals("ExtractedIonCurrent")) {
            if (extractedIonCurrent != null) {
                return Array.factory(extractedIonCurrent);
            } else {
                return Array.factory(new double[]{Double.NaN});
            }
        }
        if (name.equals("NormalizationMethods")) {
            if (normalizationMethods != null) {
                return Array.makeArray(DataType.STRING, normalizationMethods);
            } else {
                return Array.makeArray(DataType.STRING, new String[]{"None"});
            }
        }
        if (name.equals("PeakAnnotations")) {
            throw new ResourceNotAvailableException("PeakAnnotations can not be accessed via getFeature()");
        }
        if (name.equals("PeakType")) {
            if (peakType == null) {
                return Array.makeArray(DataType.STRING, new String[]{PeakType.UNDEFINED.name()});
            } else {
                return Array.makeArray(DataType.STRING, new String[]{peakType.name()});
            }
        }
        if (name.equals("UniqueId")) {
            return Array.makeArray(DataType.STRING, new String[]{uniqueId.toString()});
        }
        PublicMemberGetters<Peak1D> pmg = new PublicMemberGetters<>(
                getClass());
        Method m = pmg.getMethodForGetterName(name);
        if (m == null) {
            throw new ResourceNotAvailableException(
                    "Could not find compatible method for feature with name : "
                    + name);
        }
        try {
            Object o = m.invoke(this, new Object[]{});
            if (o == null) {
                throw new ResourceNotAvailableException(
                        "Can not create array representation of object for method: "
                        + name);
            }
            if (o instanceof Array) {
                return (Array) o;
            }
            return ArrayTools.factoryScalar(o);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            log.warn(e.getLocalizedMessage());
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
    /** {@inheritDoc} */
    @Override
    public List<String> getFeatureNames() {
        PublicMemberGetters<Peak1D> pmg = new PublicMemberGetters<>(
                getClass(), "Feature", "FeatureNames");
        return Arrays.asList(pmg.getGetterNames());
    }

//	public static void main(String[] args) {
//		Peak1D p = new Peak1D(30, 35, 40, 123124.932, 12354);
//		log.info(p.getFeature("StartIndex"));
//		log.info(p.getFeature("StopIndex"));
//		log.info(p.getFeature("Area"));
//		log.info(p.getFeature("Intensity"));
//	}
    /** {@inheritDoc} */
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

    /**
     * <p>fromFragment.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Peak1D> fromFragment(IFileFragment ff) {
        IVariableFragment peaks = null;
        try {
            peaks = ff.getChild(
                    "tic_peaks");
        } catch (ResourceNotAvailableException rnae) {
            peaks = ff.getChild("eic_peaks");
        }
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
        ArrayList<Peak1D> peaklist = new ArrayList<>(peakPositions.getShape()[0]);
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
        }
        return peaklist;
    }

    /**
     * <p>appendEICs.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param peakNormalizers a {@link java.util.List} object.
     * @param peaklist a {@link java.util.List} object.
     * @param peakVarName a {@link java.lang.String} object.
     * @since 1.3.2
     */
    public static void appendEICs(IFileFragment ff, List<IPeakNormalizer> peakNormalizers, List<Peak1D> peaklist, String peakVarName) {
        if (!peaklist.isEmpty()) {
            final IVariableFragment peaks = new VariableFragment(ff,
                    peakVarName);
            final Dimension peak_number = new Dimension("peak_number",
                    peaklist.size(), true, false, false);
            final Dimension peak_source_file_number = new Dimension("peak_source_file_number", 1, true, false, false);
            final Dimension _1024_byte_string = new Dimension("_1024_byte_string", 1024, true, false, false);
            final Dimension _64_byte_string = new Dimension("_64_byte_string", 64, true, false, false);
            final Dimension peak_normalizers = new Dimension("peak_normalizer_count", peakNormalizers.isEmpty() ? 1 : peakNormalizers.size(), true, false, false);
            peaks.setDimensions(new Dimension[]{peak_number});
//			Array tic = ff.getChild("total_intensity").getArray();
            IVariableFragment peakSourceFile = new VariableFragment(ff, "peak_source_file");
            peakSourceFile.setDimensions(new Dimension[]{peak_source_file_number, _1024_byte_string});
            IVariableFragment peakNames = new VariableFragment(ff, "peak_name");
            peakNames.setDimensions(new Dimension[]{peak_number, _1024_byte_string});
            IVariableFragment peakStartIndex = new VariableFragment(ff, "peak_start_index");
            peakStartIndex.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakEndIndex = new VariableFragment(ff, "peak_end_index");
            peakEndIndex.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakRT = new VariableFragment(ff,
                    "peak_retention_time");
            peakRT.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakStartRT = new VariableFragment(ff,
                    "peak_start_time");
            peakStartRT.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakStopRT = new VariableFragment(ff,
                    "peak_end_time");
            peakStopRT.setDimensions(new Dimension[]{peak_number});
            IVariableFragment snr = new VariableFragment(ff, "peak_signal_to_noise");
            snr.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakType = new VariableFragment(ff, "peak_type");
            peakType.setDimensions(new Dimension[]{peak_number, _64_byte_string});
            IVariableFragment peakDetectionChannel = new VariableFragment(ff, "peak_detection_channel");
            peakDetectionChannel.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakArea = new VariableFragment(ff, "peak_area");
            peakArea.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakNormalizedArea = new VariableFragment(ff, "peak_area_normalized");
            peakNormalizedArea.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakNormalizationMethod = new VariableFragment(ff, "peak_area_normalization_methods");
            peakNormalizationMethod.setDimensions(new Dimension[]{peak_normalizers, _1024_byte_string});
            IVariableFragment baseLineStartRT = new VariableFragment(ff, "baseline_start_time");
            baseLineStartRT.setDimensions(new Dimension[]{peak_number});
            IVariableFragment baseLineStopRT = new VariableFragment(ff, "baseline_stop_time");
            baseLineStopRT.setDimensions(new Dimension[]{peak_number});
            IVariableFragment baseLineStartValue = new VariableFragment(ff, "baseline_start_value");
            baseLineStartValue.setDimensions(new Dimension[]{peak_number});
            IVariableFragment baseLineStopValue = new VariableFragment(ff, "baseline_stop_value");
            baseLineStopValue.setDimensions(new Dimension[]{peak_number});
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
            String[] normalizationMethodsString = new String[peakNormalizers.size()];
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
                    normalizationMethodsString[i] = peakNormalizers.get(i).getNormalizationName();
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
                bstartRT.setDouble(i, p.getBaselineStartTime());
                bstopRT.setDouble(i, p.getBaselineStopTime());
                bstartV.setDouble(i, p.getBaselineStartValue());
                bstopV.setDouble(i, p.getBaselineStopValue());
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
            peakNormalizationMethod.setArray(normalizationMethodArray);
            i = 0;
            for (Peak1D p : peaklist) {
                p.setNormalizationMethods(normalizationMethodsString);
                double normalizedArea = p.getArea();
                for (IPeakNormalizer normalizer : peakNormalizers) {
                    normalizedArea *= normalizer.getNormalizationFactor(ff, i);
                }
                areaNormalized.setDouble(i++, normalizedArea);
                p.setNormalizedArea(normalizedArea);
            }
            peakNormalizedArea.setArray(areaNormalized);
        }
    }

    /**
     * <p>append.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param peakNormalizers a {@link java.util.List} object.
     * @param peaklist a {@link java.util.List} object.
     * @param filteredTrace a {@link ucar.ma2.Array} object.
     * @param peakVarName a {@link java.lang.String} object.
     * @param filteredTraceVarName a {@link java.lang.String} object.
     */
    public static void append(IFileFragment ff, List<IPeakNormalizer> peakNormalizers, List<Peak1D> peaklist, Array filteredTrace, String peakVarName, String filteredTraceVarName) {
        if (!peaklist.isEmpty()) {
            final IVariableFragment peaks = new VariableFragment(ff,
                    peakVarName);
            final Dimension peak_number = new Dimension("peak_number",
                    peaklist.size(), true, false, false);
            final Dimension peak_source_file_number = new Dimension("peak_source_file_number", 1, true, false, false);
            final Dimension _1024_byte_string = new Dimension("_1024_byte_string", 1024, true, false, false);
            final Dimension _64_byte_string = new Dimension("_64_byte_string", 64, true, false, false);
            final Dimension peak_normalizers = new Dimension("peak_normalizer_count", peakNormalizers.isEmpty() ? 1 : peakNormalizers.size(), true, false, false);
            peaks.setDimensions(new Dimension[]{peak_number});
            Array tic = ff.getChild("total_intensity").getArray();
            final Dimension scan_number = new Dimension("scan_number", tic.getShape()[0]);
            if (filteredTrace != null && filteredTraceVarName != null) {
                final IVariableFragment mai = new VariableFragment(ff,
                        filteredTraceVarName);
                mai.setDimensions(new Dimension[]{scan_number});
                mai.setArray(filteredTrace);
            }
            IVariableFragment peakSourceFile = new VariableFragment(ff, "peak_source_file");
            peakSourceFile.setDimensions(new Dimension[]{peak_source_file_number, _1024_byte_string});
            IVariableFragment peakNames = new VariableFragment(ff, "peak_name");
            peakNames.setDimensions(new Dimension[]{peak_number, _1024_byte_string});
            IVariableFragment peakStartIndex = new VariableFragment(ff, "peak_start_index");
            peakStartIndex.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakEndIndex = new VariableFragment(ff, "peak_end_index");
            peakEndIndex.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakRT = new VariableFragment(ff,
                    "peak_retention_time");
            peakRT.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakStartRT = new VariableFragment(ff,
                    "peak_start_time");
            peakStartRT.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakStopRT = new VariableFragment(ff,
                    "peak_end_time");
            peakStopRT.setDimensions(new Dimension[]{peak_number});
            IVariableFragment snr = new VariableFragment(ff, "peak_signal_to_noise");
            snr.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakType = new VariableFragment(ff, "peak_type");
            peakType.setDimensions(new Dimension[]{peak_number, _64_byte_string});
            IVariableFragment peakDetectionChannel = new VariableFragment(ff, "peak_detection_channel");
            peakDetectionChannel.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakArea = new VariableFragment(ff, "peak_area");
            peakArea.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakNormalizedArea = new VariableFragment(ff, "peak_area_normalized");
            peakNormalizedArea.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakNormalizationMethod = new VariableFragment(ff, "peak_area_normalization_methods");
            peakNormalizationMethod.setDimensions(new Dimension[]{peak_normalizers, _1024_byte_string});
            IVariableFragment baseLineStartRT = new VariableFragment(ff, "baseline_start_time");
            baseLineStartRT.setDimensions(new Dimension[]{peak_number});
            IVariableFragment baseLineStopRT = new VariableFragment(ff, "baseline_stop_time");
            baseLineStopRT.setDimensions(new Dimension[]{peak_number});
            IVariableFragment baseLineStartValue = new VariableFragment(ff, "baseline_start_value");
            baseLineStartValue.setDimensions(new Dimension[]{peak_number});
            IVariableFragment baseLineStopValue = new VariableFragment(ff, "baseline_stop_value");
            baseLineStopValue.setDimensions(new Dimension[]{peak_number});
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
            String[] normalizationMethodsString = new String[peakNormalizers.size()];
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
                    normalizationMethodsString[i] = peakNormalizers.get(i).getNormalizationName();
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
                bstartRT.setDouble(i, p.getStartTime());
                if (p.getStartIndex() >= 0 && filteredTrace != null) {
                    bstartV.setDouble(i, tic.getDouble(p.getStartIndex()) - filteredTrace.getDouble(p.getStartIndex()));
                } else {
                    bstartV.setDouble(i, Double.NaN);
                }
                bstopRT.setDouble(i, p.getStopTime());
                if (p.getStopIndex() >= 0 && filteredTrace != null) {
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
            peakNormalizationMethod.setArray(normalizationMethodArray);
            i = 0;
            for (Peak1D p : peaklist) {
                p.setNormalizationMethods(normalizationMethodsString);
                double normalizedArea = p.getArea();
                for (IPeakNormalizer normalizer : peakNormalizers) {
                    normalizedArea *= normalizer.getNormalizationFactor(ff, i);
                }
                areaNormalized.setDouble(i++, normalizedArea);
                p.setNormalizedArea(normalizedArea);
            }
            peakNormalizedArea.setArray(areaNormalized);
        }
    }

    /** {@inheritDoc} */
    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }
}
