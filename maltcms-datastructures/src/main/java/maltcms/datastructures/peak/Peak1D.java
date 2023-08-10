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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;

import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.peak.annotations.PeakAnnotation;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import maltcms.tools.ArrayTools;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;

/**
 * Peak1D models a standard 1D chromatographic peak.
 *
 * If you want to model mass spectral peaks over time, use a Peak1DGroup
 * instance.
 *
 * Peak1D objects can be compared using <code>equals</code>. Each peak
 * additionally has a random unique id, that can be retrieved using
 * <code>getUniqueId()</code>.
 *
 * @author Nils Hoffmann
 */
@Data
@EqualsAndHashCode(exclude = "uniqueId")

public class Peak1D implements Serializable, IFeatureVector, Iterable<Peak1D> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Peak1D.class);

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
    private List<PeakAnnotation> peakAnnotations = Collections.emptyList();
    private double baselineStartTime = Double.NaN;
    private double baselineStopTime = Double.NaN;
    private double baselineStartValue = Double.NaN;
    private double baselineStopValue = Double.NaN;
    private int index = -1;
    private final UUID uniqueId;

    public static class Peak1DBuilder {

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
        private List<PeakAnnotation> peakAnnotations = Collections.emptyList();
        private double baselineStartTime = Double.NaN;
        private double baselineStopTime = Double.NaN;
        private double baselineStartValue = Double.NaN;
        private double baselineStopValue = Double.NaN;
        private int index = -1;
        private UUID uniqueId = UUID.randomUUID();

        private Peak1DBuilder() {
        }

        public Peak1DBuilder startIndex(final int value) {
            this.startIndex = value;
            return this;
        }

        public Peak1DBuilder apexIndex(final int value) {
            this.apexIndex = value;
            return this;
        }

        public Peak1DBuilder stopIndex(final int value) {
            this.stopIndex = value;
            return this;
        }

        public Peak1DBuilder apexIntensity(final double value) {
            this.apexIntensity = value;
            return this;
        }

        public Peak1DBuilder startTime(final double value) {
            this.startTime = value;
            return this;
        }

        public Peak1DBuilder stopTime(final double value) {
            this.stopTime = value;
            return this;
        }

        public Peak1DBuilder apexTime(final double value) {
            this.apexTime = value;
            return this;
        }

        public Peak1DBuilder area(final double value) {
            this.area = value;
            return this;
        }

        public Peak1DBuilder normalizedArea(final double value) {
            this.normalizedArea = value;
            return this;
        }

        public Peak1DBuilder mw(final double value) {
            this.mw = value;
            return this;
        }

        public Peak1DBuilder extractedIonCurrent(final double[] value) {
            this.extractedIonCurrent = value;
            return this;
        }

        public Peak1DBuilder snr(final double value) {
            this.snr = value;
            return this;
        }

        public Peak1DBuilder file(final String value) {
            this.file = value;
            return this;
        }

        public Peak1DBuilder peakType(final PeakType value) {
            this.peakType = value;
            return this;
        }

        public Peak1DBuilder normalizationMethods(final String[] value) {
            this.normalizationMethods = value;
            return this;
        }

        public Peak1DBuilder name(final String value) {
            this.name = value;
            return this;
        }

        public Peak1DBuilder peakAnnotations(final List<PeakAnnotation> value) {
            this.peakAnnotations = value;
            return this;
        }

        public Peak1DBuilder baselineStartTime(final double value) {
            this.baselineStartTime = value;
            return this;
        }

        public Peak1DBuilder baselineStopTime(final double value) {
            this.baselineStopTime = value;
            return this;
        }

        public Peak1DBuilder baselineStartValue(final double value) {
            this.baselineStartValue = value;
            return this;
        }

        public Peak1DBuilder baselineStopValue(final double value) {
            this.baselineStopValue = value;
            return this;
        }

        public Peak1DBuilder index(final int value) {
            this.index = value;
            return this;
        }

        public Peak1DBuilder uniqueId(final UUID value) {
            this.uniqueId = value;
            return this;
        }

        public Peak1D build() {
            return new maltcms.datastructures.peak.Peak1D(startIndex, apexIndex, stopIndex, apexIntensity, startTime, stopTime, apexTime, area, normalizedArea, mw, extractedIonCurrent, snr, file, peakType, normalizationMethods, name, peakAnnotations, baselineStartTime, baselineStopTime, baselineStartValue, baselineStopValue, index, uniqueId);
        }
    }

    public static Peak1D.Peak1DBuilder builder1D() {
        return new Peak1D.Peak1DBuilder();
    }

    private Peak1D(final int startIndex, final int apexIndex, final int stopIndex, final double apexIntensity, final double startTime, final double stopTime, final double apexTime, final double area, final double normalizedArea, final double mw, final double[] extractedIonCurrent, final double snr, final String file, final PeakType peakType, final String[] normalizationMethods, final String name, final List<PeakAnnotation> peakAnnotations, final double baselineStartTime, final double baselineStopTime, final double baselineStartValue, final double baselineStopValue, final int index, final UUID uniqueId) {
        this.startIndex = startIndex;
        this.apexIndex = apexIndex;
        this.stopIndex = stopIndex;
        this.apexIntensity = apexIntensity;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.apexTime = apexTime;
        this.area = area;
        this.normalizedArea = normalizedArea;
        this.mw = mw;
        this.extractedIonCurrent = extractedIonCurrent;
        this.snr = snr;
        this.file = file;
        this.peakType = peakType;
        this.normalizationMethods = normalizationMethods;
        this.name = name;
        this.peakAnnotations = peakAnnotations;
        this.baselineStartTime = baselineStartTime;
        this.baselineStopTime = baselineStopTime;
        this.baselineStartValue = baselineStartValue;
        this.baselineStopValue = baselineStopValue;
        this.index = index;
        this.uniqueId = uniqueId;
    }

    /**
     * Creates a new Peak1D object from the given values. It is generally
     * advised to use the {@link Peak1DBuilder}, obtainable from
     * {@link Peak1D#builder1D()}.
     *
     * @param index, default value: -1.
     * @param startIndex, default value: -1.
     * @param apexIndex, default value: -1.
     * @param stopIndex, default value: -1.
     * @param apexIntensity, default value: NaN.
     * @param area, default value: NaN.
     * @param normalizedArea, default value: NaN.
     * @param normalizationMethods, default value: &#123;"None"&#125;.
     * @param startTime, default value: NaN.
     * @param stopTime, default value: NaN.
     * @param apexTime, default value: NaN.
     * @param baselineStartTime, default value: NaN.
     * @param baselineStopTime, default value: NaN.
     * @param baselineStartValue, default value: NaN.
     * @param baselineStopValue, default value: NaN.
     * @param mw, default value: NaN.
     * @param extractedIonCurrent, default value: undefined/null.
     * @param snr, default value: NaN.
     * @param file, default value: "".
     * @param peakType, default value: PeakType.UNDEFINED.
     * @param name, default value: "".
     * @param peakAnnotations, default value: Collections.emptyList().
     * @param uniqueId
     *
     */
//    @Peak1DBuilder
    public Peak1D(int index, int startIndex, int apexIndex, int stopIndex, double apexIntensity, double area, double normalizedArea, String[] normalizationMethods, double startTime, double stopTime, double apexTime, double baselineStartTime, double baselineStopTime, double baselineStartValue, double baselineStopValue, double mw, double[] extractedIonCurrent, double snr, String file, PeakType peakType, String name, List<PeakAnnotation> peakAnnotations, UUID uniqueId) {
        this.index = index;
        this.startIndex = startIndex;
        this.apexIndex = apexIndex;
        this.stopIndex = stopIndex;
        this.apexIntensity = apexIntensity;
        this.area = area;
        this.normalizedArea = normalizedArea;
        this.normalizationMethods = normalizationMethods;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.apexTime = apexTime;
        this.baselineStartTime = baselineStartTime;
        this.baselineStopTime = baselineStopTime;
        this.baselineStartValue = baselineStartValue;
        this.baselineStopValue = baselineStopValue;
        this.mw = mw;
        this.extractedIonCurrent = extractedIonCurrent;
        this.snr = snr;
        this.file = file;
        this.peakType = peakType == null ? PeakType.UNDEFINED : peakType;
        this.name = name;
        this.peakAnnotations = peakAnnotations;
        this.uniqueId = uniqueId;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public Array getFeature(String name) {
        switch (name) {
            case "ExtractedIonCurrent":
                if (extractedIonCurrent != null) {
                    return Array.factory(extractedIonCurrent);
                } else {
                    return Array.factory(new double[]{Double.NaN});
                }
            case "NormalizationMethods":
                if (normalizationMethods != null) {
                    return Array.makeArray(DataType.STRING, normalizationMethods);
                } else {
                    return Array.makeArray(DataType.STRING, new String[]{"None"});
                }
            case "PeakAnnotations":
                throw new ResourceNotAvailableException("PeakAnnotations can not be accessed via getFeature()");
            case "PeakType":
                if (peakType == null) {
                    return Array.makeArray(DataType.STRING, new String[]{PeakType.UNDEFINED.name()});
                } else {
                    return Array.makeArray(DataType.STRING, new String[]{peakType.name()});
                }
            case "UniqueId":
                return Array.makeArray(DataType.STRING, new String[]{uniqueId.toString()});
            case "File":
                return Array.makeArray(DataType.STRING, new String[]{file});
            case "Name":
                return Array.makeArray(DataType.STRING, new String[]{name});
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
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getFeatureNames() {
        PublicMemberGetters<Peak1D> pmg = new PublicMemberGetters<>(
                getClass(), "Feature", "FeatureNames");
        return Arrays.asList(pmg.getGetterNames());
    }

    /**
     * {@inheritDoc}
     */
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
     * <p>
     * fromFragment.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param peakVariable the peak variable to read, e.g. 'tic_peaks' or
     * 'eic_peaks'.
     * @return a {@link java.util.List} object containing the loaded peaks.
     * @throws ResourceNotAvailableException if a variable can not be retrieved
     * from the file fragment
     */
    public static List<Peak1D> fromFragment(IFileFragment ff, String peakVariable) throws ResourceNotAvailableException {
        IVariableFragment peaks = ff.getChild(peakVariable);
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
        Array peakHeight = null;
        try {
            peakHeight = ff.getChild("peak_height").getArray();
        } catch (ResourceNotAvailableException rnae) {
            peakHeight = Array.factory(area.getElementType(), area.getShape());
            ArrayTools.fill(peakHeight, Double.NaN);
        }
        ArrayChar.D2 peakType = (ArrayChar.D2) ff.getChild("peak_type").getArray();
        Array peakDetectionChannel = ff.getChild("peak_detection_channel").getArray();
        ArrayInt.D1 peakPositions = (ArrayInt.D1) peaks.getArray();
        ArrayList<Peak1D> peaklist = new ArrayList<>(peakPositions.getShape()[0]);
        for (int i = 0; i < peakPositions.getShape()[0]; i++) {
            Peak1DBuilder builder = Peak1D.builder1D();
            if (normalizationMethods.isEmpty()) {
                builder.normalizationMethods(new String[]{"None"});
            } else {
                builder.normalizationMethods(normalizationMethods.toArray(new String[normalizationMethods.size()]));
            }
            builder.
                    index(i).
                    file(sourceFileName.getString(0)).
                    name(peakNames.getString(i)).
                    apexTime(apexRt.getDouble(i)).
                    startTime(startRT.getDouble(i)).
                    stopTime(stopRT.getDouble(i)).
                    area(area.getDouble(i)).
                    apexIntensity(peakHeight.getDouble(i)).
                    normalizedArea(normalizedArea.getDouble(i)).
                    baselineStartTime(baseLineStartRT.getDouble(i)).
                    baselineStopTime(baseLineStopRT.getDouble(i)).
                    baselineStartValue(baseLineStartValue.getDouble(i)).
                    baselineStopValue(baseLineStopValue.getDouble(i)).
                    apexIndex(peakPositions.getInt(i)).
                    startIndex(peakStartIndex.getInt(i)).
                    stopIndex(peakEndIndex.getInt(i)).
                    snr(snr.getDouble(i)).
                    peakType(PeakType.valueOf(peakType.getString(i))).
                    mw(peakDetectionChannel.getDouble(i));
            peaklist.add(builder.build());
        }
        return peaklist;
    }

    /**
     * <p>
     * fromFragment. Tries to retrieve 'tic_peaks' variable first, falling back
     * to 'eic_peaks' if that the other is not available. If 'eic_peaks' is also
     * not available, returns an empty list.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Peak1D> fromFragment(IFileFragment ff) {
        try {
            return fromFragment(ff, "tic_peaks");
        } catch (ResourceNotAvailableException rnae) {
            log.warn("Could not retrieve 'tic_peaks' from " + ff.getName() + "! Trying 'eic_peaks'!");
            try {
                return fromFragment(ff, "eic_peaks");
            } catch (ResourceNotAvailableException rnae2) {
                return Collections.emptyList();
            }
        }
    }

    /**
     * <p>
     * append.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param peakNormalizers a {@link java.util.List} object.
     * @param peaklist a {@link java.util.List} object.
     * @param peakVarName a {@link java.lang.String} object.
     */
    public static void append(IFileFragment ff, List<IPeakNormalizer> peakNormalizers, List<Peak1D> peaklist, String peakVarName) {
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
            IVariableFragment peakHeight = new VariableFragment(ff, "peak_height");
            peakHeight.setDimensions(new Dimension[]{peak_number});
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
            String[] normalizationMethodsString = new String[Math.max(1, peakNormalizers.size())];
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
            ArrayDouble.D1 peakHeightArray = new ArrayDouble.D1(peaklist.size());
            peakSourceFileArray.setString(0, ff.getName());
            if (peakNormalizers.isEmpty()) {
                normalizationMethodArray.setString(0, "None");
                normalizationMethodsString[0] = "None";
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
                    names.setString(i, "");
                } else {
                    names.setString(i, name);
                }
                if (p.getPeakType() == null) {
                    p.setPeakType(PeakType.UNDEFINED);
                }
                peakTypeArray.setString(i, p.getPeakType().name());
                snrArray.set(i, p.getSnr());
                peakDetectionChannelArray.set(i, p.getMw());
                apexRT.setDouble(i, p.getApexTime());
                startRT.setDouble(i, p.getStartTime());
                stopRT.setDouble(i, p.getStopTime());
                area.setDouble(i, p.getArea());
                peakHeightArray.setDouble(i, p.getApexIntensity());
                bstartRT.setDouble(i, p.getStartTime());
                bstartV.setDouble(i, p.getBaselineStartValue());
                bstopRT.setDouble(i, p.getStopTime());
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
            peakHeight.setArray(peakHeightArray);
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

}
