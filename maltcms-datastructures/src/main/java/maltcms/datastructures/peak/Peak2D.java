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
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ResourceNotAvailableException;
import cross.tools.PublicMemberGetters;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.ms.Metabolite;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
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
@EqualsAndHashCode(callSuper = true, exclude = {
    "firstScanIndex", "secondScanIndex", "peakArea", "reference", "names", "identification"})
@Data
public class Peak2D extends Peak1D implements Serializable {

    private PeakArea2D peakArea = null;
    private double firstRetTime = -1.0d;
    private double secondRetTime = -1.0d;
    
    public Peak2D() {
        
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
     * main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        Peak2D p = new Peak2D();
        p.setApexIndex(50);
        p.setStartIndex(30);
        p.setStopIndex(80);
        p.setFirstRetTime(380);
        p.setSecondRetTime(3.12);
        log.info("ApexIndex: {}", p.getFeature("ApexIndex"));
        log.info("StartIndex: {}", p.getFeature("StartIndex"));
        log.info("StopIndex: {}", p.getFeature("StopIndex"));
        log.info("FirstRetTime: {}", p.getFeature("FirstRetTime"));
        log.info("SecondRetTime: {}", p.getFeature("SecondRetTime"));
        // log.info(p.getFeature("PeakArea"));
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
            Peak2D p = new Peak2D();
            if (normalizationMethods.isEmpty()) {
                p.setNormalizationMethods(new String[]{"None"});
            } else {
                p.setNormalizationMethods(normalizationMethods.toArray(new String[normalizationMethods.size()]));
            }
            p.setFile(sourceFileName.getString(0));
            p.setName(peakNames.getString(i));
            p.setApexTime(apexRt.getDouble(i));
            p.setStartTime(startRT.getDouble(i));
            p.setFirstRetTime(firstColumnRT.getDouble(i));
            p.setSecondRetTime(secondColumnRT.getDouble(i));
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
                peakTypeArray.setString(i, p.getPeakType().name());
                snrArray.set(i, p.getSnr());
                peakDetectionChannelArray.set(i, p.getMw());
                apexRT.setDouble(i, p.getApexTime());
                apexRT1D.setDouble(i, p.getFirstRetTime());
                apexRT2D.setDouble(i, p.getSecondRetTime());
                startRT.setDouble(i, p.getStartTime());
                stopRT.setDouble(i, p.getStopTime());
                area.setDouble(i, p.getArea());
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
