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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.annotations.PeakAnnotation;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import maltcms.tools.ArrayTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.nc2.Dimension;

/**
 * Dataholder for all important information about a peak.
 *
 * @author Mathias Wilhelm
 *
 */
@Slf4j
@EqualsAndHashCode(callSuper = true, exclude = {"peakArea"})
@Data
public class Peak2D extends Peak1D implements Serializable {

    public static class Peak2DBuilder {

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
        private PeakArea2D peakArea = null;
        private double firstRetTime = Double.NaN;
        private double secondRetTime = -Double.NaN;

        private Peak2DBuilder() {
        }

        public Peak2DBuilder startIndex(final int value) {
            this.startIndex = value;
            return this;
        }

        public Peak2DBuilder apexIndex(final int value) {
            this.apexIndex = value;
            return this;
        }

        public Peak2DBuilder stopIndex(final int value) {
            this.stopIndex = value;
            return this;
        }

        public Peak2DBuilder apexIntensity(final double value) {
            this.apexIntensity = value;
            return this;
        }

        public Peak2DBuilder startTime(final double value) {
            this.startTime = value;
            return this;
        }

        public Peak2DBuilder stopTime(final double value) {
            this.stopTime = value;
            return this;
        }

        public Peak2DBuilder apexTime(final double value) {
            this.apexTime = value;
            return this;
        }

        public Peak2DBuilder area(final double value) {
            this.area = value;
            return this;
        }

        public Peak2DBuilder normalizedArea(final double value) {
            this.normalizedArea = value;
            return this;
        }

        public Peak2DBuilder mw(final double value) {
            this.mw = value;
            return this;
        }

        public Peak2DBuilder extractedIonCurrent(final double[] value) {
            this.extractedIonCurrent = value;
            return this;
        }

        public Peak2DBuilder snr(final double value) {
            this.snr = value;
            return this;
        }

        public Peak2DBuilder file(final String value) {
            this.file = value;
            return this;
        }

        public Peak2DBuilder peakType(final PeakType value) {
            this.peakType = value;
            return this;
        }

        public Peak2DBuilder normalizationMethods(final String[] value) {
            this.normalizationMethods = value;
            return this;
        }

        public Peak2DBuilder name(final String value) {
            this.name = value;
            return this;
        }

        public Peak2DBuilder peakAnnotations(final List<PeakAnnotation> value) {
            this.peakAnnotations = value;
            return this;
        }

        public Peak2DBuilder baselineStartTime(final double value) {
            this.baselineStartTime = value;
            return this;
        }

        public Peak2DBuilder baselineStopTime(final double value) {
            this.baselineStopTime = value;
            return this;
        }

        public Peak2DBuilder baselineStartValue(final double value) {
            this.baselineStartValue = value;
            return this;
        }

        public Peak2DBuilder baselineStopValue(final double value) {
            this.baselineStopValue = value;
            return this;
        }

        public Peak2DBuilder index(final int value) {
            this.index = value;
            return this;
        }

        public Peak2DBuilder uniqueId(final UUID value) {
            this.uniqueId = value;
            return this;
        }

        public Peak2DBuilder peakArea(final PeakArea2D value) {
            this.peakArea = value;
            return this;
        }

        public Peak2DBuilder firstRetTime(final double value) {
            this.firstRetTime = value;
            return this;
        }

        public Peak2DBuilder secondRetTime(final double value) {
            this.secondRetTime = value;
            return this;
        }

        public Peak2D build() {
            return new maltcms.datastructures.peak.Peak2D(startIndex, apexIndex, stopIndex, apexIntensity, startTime, stopTime, apexTime, area, normalizedArea, mw, extractedIonCurrent, snr, file, peakType, normalizationMethods, name, peakAnnotations, baselineStartTime, baselineStopTime, baselineStartValue, baselineStopValue, index, uniqueId, peakArea, firstRetTime, secondRetTime);
        }

    }

    public static Peak2DBuilder builder2D() {
        return new Peak2DBuilder();
    }

    private Peak2D(final int startIndex, final int apexIndex, final int stopIndex, final double apexIntensity, final double startTime, final double stopTime, final double apexTime, final double area, final double normalizedArea, final double mw, final double[] extractedIonCurrent, final double snr, final String file, final PeakType peakType, final String[] normalizationMethods, final String name, final List<PeakAnnotation> peakAnnotations, final double baselineStartTime, final double baselineStopTime, final double baselineStartValue, final double baselineStopValue, final int index, final UUID uniqueId, final PeakArea2D peakArea, final double firstRetTime, final double secondRetTime) {
        super(index, startIndex, apexIndex, stopIndex, apexIntensity, area, normalizedArea, normalizationMethods, startTime, stopTime, apexTime, baselineStartTime, baselineStopTime, baselineStartValue, baselineStopValue, mw, extractedIonCurrent, snr, file, peakType, name, peakAnnotations, uniqueId);
        this.peakArea = peakArea;
        this.firstRetTime = firstRetTime;
        this.secondRetTime = secondRetTime;
    }

    private PeakArea2D peakArea = null;
    private double firstRetTime = Double.NaN;
    private double secondRetTime = -Double.NaN;

    /**
     * Creates a new Peak2D object from the given values. It is generally
     * advised to use the {@link Peak2DBuilder}, obtainable from
     * {@link Peak22D#builder2D()}.
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
//    @Builder(builderMethodName = "builder2D")
    public Peak2D(int index, int startIndex, int apexIndex, int stopIndex, double apexIntensity, double area, double normalizedArea, String[] normalizationMethods, double startTime, double stopTime, double apexTime, double baselineStartTime, double baselineStopTime, double baselineStartValue, double baselineStopValue, double mw, double[] extractedIonCurrent, double snr, String file, PeakType peakType, String name, List<PeakAnnotation> peakAnnotations, PeakArea2D peakArea, double firstRetTime, double secondRetTime, UUID uniqueId) {
        super(index, startIndex, apexIndex, stopIndex, apexIntensity, area, normalizedArea, normalizationMethods, startTime, stopTime, apexTime, baselineStartTime, baselineStopTime, baselineStartValue, baselineStopValue, mw, extractedIonCurrent, snr, file, peakType, name, peakAnnotations, uniqueId);
        this.peakArea = peakArea;
        this.firstRetTime = firstRetTime;
        this.secondRetTime = secondRetTime;
    }

    /**
     * Getter.
     *
     * @return getFirstRetTime() + getSecondRetTime()
     */
    public double getRetentionTime() {
        return this.firstRetTime + this.secondRetTime;
    }

    /**
     * <p>
     * getFirstScanIndex.</p>
     *
     * @return a int.
     */
    public int getFirstScanIndex() {
        return this.peakArea.getSeedPoint().x;
    }

    /**
     * <p>
     * getSecondScanIndex.</p>
     *
     * @return a int.
     */
    public int getSecondScanIndex() {
        return this.peakArea.getSeedPoint().y;
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
        Set<String> features = new LinkedHashSet<>();
        PublicMemberGetters<Peak1D> pmg1 = new PublicMemberGetters<>(
                getClass(), "Feature", "FeatureNames");
        features.addAll(Arrays.asList(pmg1.getGetterNames()));
        PublicMemberGetters<Peak2D> pmg = new PublicMemberGetters<>(
                getClass(), "Feature", "FeatureNames");
        features.addAll(Arrays.asList(pmg.getGetterNames()));
        log.debug("Feature names: {}", features);
        return new ArrayList<>(features);
    }

    /**
     * <p>
     * fromFragment2D.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param peakVariable the peak variable to read, e.g. 'tic_peaks' or
     * 'eic_peaks'.
     * @return a {@link java.util.List} object containing the loaded peaks.
     * @throws ResourceNotAvailableException if a variable can not be retrieved
     * from the file fragment
     */
    public static List<Peak2D> fromFragment2D(IFileFragment ff, String peakVariable) throws ResourceNotAvailableException {
        IVariableFragment peaks = ff.getChild(peakVariable);
        ArrayChar.D2 sourceFileName = (ArrayChar.D2) ff.getChild("peak_source_file").getArray();
        ArrayChar.D2 peakNames = (ArrayChar.D2) ff.getChild("peak_name").getArray();
        Array apexRt = ff.getChild(
                "peak_retention_time").getArray();
        Array startRT = ff.getChild(
                "peak_start_time").getArray();
        Array stopRT = ff.getChild(
                "peak_end_time").getArray();
        Array firstColumnRT = ff.getChild(
                "peak_first_column_elution_time").getArray();
        Array secondColumnRT = ff.getChild(
                "peak_second_column_elution_time").getArray();
        Array area = ff.getChild("peak_area").getArray();
        Array normalizedArea = ff.getChild("peak_area_normalized").getArray();
        Collection<String> normalizationMethods = FragmentTools.getStringArray(ff, "peak_area_normalization_methods");
        Array peakHeight = null;
        try {
            peakHeight = ff.getChild("peak_height").getArray();
        } catch (ResourceNotAvailableException rnae) {
            peakHeight = Array.factory(area.getElementType(), area.getShape());
            ArrayTools.fill(peakHeight, Double.NaN);
        }
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
        ArrayList<Peak2D> peaklist = new ArrayList<>(peakPositions.getShape()[0]);
        for (int i = 0; i < peakPositions.getShape()[0]; i++) {
            Peak2DBuilder builder = Peak2D.builder2D();
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
                    firstRetTime(firstColumnRT.getDouble(i)).
                    secondRetTime(secondColumnRT.getDouble(i)).
                    stopTime(stopRT.getDouble(i)).
                    area(area.getDouble(i)).
                    normalizedArea(normalizedArea.getDouble(i)).
                    apexIntensity(peakHeight.getDouble(i)).
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
     * append.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param peakNormalizers a {@link java.util.List} object.
     * @param peaklist a {@link java.util.List} object.
     * @param peakVarName a {@link java.lang.String} object.
     */
    public static void append2D(IFileFragment ff, List<IPeakNormalizer> peakNormalizers, List<Peak2D> peaklist, String peakVarName) {
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
            IVariableFragment peakRT1D = new VariableFragment(ff,
                    "peak_first_column_elution_time");
            peakRT1D.setDimensions(new Dimension[]{peak_number});
            IVariableFragment peakRT2D = new VariableFragment(ff,
                    "peak_second_column_elution_time");
            peakRT2D.setDimensions(new Dimension[]{peak_number});
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
            ArrayDouble.D1 apexRT1D = new ArrayDouble.D1(peaklist.size());
            ArrayDouble.D1 apexRT2D = new ArrayDouble.D1(peaklist.size());
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
            for (Peak2D p : peaklist) {
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
                apexRT1D.setDouble(i, p.getFirstRetTime());
                apexRT2D.setDouble(i, p.getSecondRetTime());
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
            peakRT1D.setArray(apexRT1D);
            peakRT2D.setArray(apexRT2D);
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
            for (Peak2D p : peaklist) {
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
