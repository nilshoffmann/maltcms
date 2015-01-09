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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.datastructures.cache.SerializableArray;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.tools.MathTools;
import java.util.ArrayList;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;

/**
 * <p>
 * Peak1DGroup class.</p>
 *
 * @author Nils Hoffmann
 *
 */
@Data
public class Peak2DGroup implements Iterable<Peak2D> {

    /**
     *
     */
    private static final long serialVersionUID = 5613625440101236042L;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TreeSet<Peak2D> peakSet;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private SerializableArray massValues;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private SerializableArray intensityValues;
    private int apexIndex = -1;
    private int startIndex = -1;
    private int stopIndex = -1;
    private double apexTime = Double.NaN;
    private double startTime = Double.NaN;
    private double stopTime = Double.NaN;
    private double apexIntensity = Double.NaN;
    private double area = Double.NaN;

    
     /**
     * <p>
     * Constructor for Peak2DGroup.</p>
     *
     * @param massValues the mass values.
     * @param intensityValues the intensity values.
     * @param peaks the {@link maltcms.datastructures.peak.Peak2D} objects of this group.
     */
    public Peak2DGroup(Array massValues, Array intensityValues, Peak2D ... peaks) {
        this(peaks);
        this.massValues = new SerializableArray(massValues);
        this.intensityValues = new SerializableArray(intensityValues);
    }

    /**
     * <p>
     * Constructor for Peak2DGroup.</p>
     *
     * @param p a {@link maltcms.datastructures.peak.Peak2D} object.
     */
    public Peak2DGroup(Peak2D... p) {
        this.peakSet = new TreeSet<>(new Peak2DComparator());
        this.peakSet.addAll(Arrays.asList(p));
        int startIndex = Integer.MAX_VALUE;
        int stopIndex = Integer.MIN_VALUE;
        int[] apexIndex = new int[p.length];
        double[] apexTime = new double[p.length];
        double startTime = Double.POSITIVE_INFINITY;
        double stopTime = Double.NEGATIVE_INFINITY;
        double area = 0.0d;
        double apexIntensity = 0.0d;
        double mw = Double.POSITIVE_INFINITY;
        int i = 0;
        for (Peak2D peak : peakSet) {
            if (peak.getStartTime() < startTime) {
                startTime = peak.getStartTime();
            }
            if (peak.getStopTime() > stopTime) {
                stopTime = peak.getStopTime();
            }
            if (peak.getStartIndex() < startIndex) {
                startIndex = peak.getStartIndex();
            }
            if (peak.getStopIndex() > stopIndex) {
                stopIndex = peak.getStopIndex();
            }
            if (peak.getMw() < mw) {
                mw = peak.getMw();
            }
            area += peak.getArea();
            apexIntensity += peak.getApexIntensity();
            apexIndex[i] = peak.getApexIndex();
            apexTime[i] = peak.getApexTime();
            i++;
        }
        setApexIndex((int) MathTools.median(apexIndex));
        setStartIndex(startIndex);
        setStopIndex(stopIndex);
        setApexTime(MathTools.median(apexTime));
        setStartTime(startTime);
        setStopTime(stopTime);
        setApexIntensity(apexIntensity);
        setArea(area);
    }

    /**
     * <p>
     * getMassSpectrum.</p>
     *
     * @param index a int.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public Tuple2D<Array, Array> getMassSpectrum(int index) {
        int i = 0;
        int globalStartIdx = getStartIndex();
        int globalStopIdx = getStopIndex();
        EvalTools.inRangeI(globalStartIdx, globalStopIdx, index, this);
        if (massValues == null || intensityValues == null) {
            int iPrime = globalStartIdx + index;
            ArrayDouble.D1 masses = new ArrayDouble.D1(peakSet.size());
            ArrayDouble.D1 intensities = new ArrayDouble.D1(peakSet.size());
            for (Peak2D peak : peakSet) {
                int startIdx = peak.getStartIndex();
                int localIndex = (iPrime - startIdx);
                double[] eic = peak.getExtractedIonCurrent();
                if (localIndex >= 0 && localIndex < eic.length) { //peak starts later
                    double value = eic[localIndex];
                    //set intensity only if peak has intensity at given scan
                    intensities.set(i, value);
                }
                //set mass in any case
                masses.set(i, peak.getMw());
                i++;
            }
            return new Tuple2D<Array, Array>(masses, intensities);
        } else {
            return new Tuple2D<>(massValues.getArray(), intensityValues.getArray());
        }
    }

    /**
     * <p>
     * getMassSpectrum.</p>
     *
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public Tuple2D<Array, Array> getMassSpectrum() {
        return getMassSpectrum(getApexIndex());
    }

    /**
     * <p>
     * getMassSpectra.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Tuple2D<Array, Array>> getMassSpectra() {
        List<Tuple2D<Array, Array>> massSpectra = new ArrayList<>();
        if (massValues != null && intensityValues != null) {
            massSpectra.add(new Tuple2D<>(
                massValues.getArray(), 
                intensityValues.getArray())
            );
            return massSpectra;
        }
        for (int j = 0; j <= (getStopIndex() - getStartIndex()); j++) {
            massSpectra.add(getMassSpectrum(j));
        }
        return massSpectra;
    }

    private final class Peak2DComparator implements Comparator<Peak2D> {

        /*
         * (non-Javadoc)
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Peak2D o1, Peak2D o2) {
            //compare start masses
            if (o1.getMw() < o2.getMw()) {
                return -1;
            } else if (o1.getMw() > o2.getMw()) {
                return 1;
            } else {
                //compare apex times
                if (o1.getApexTime() < o2.getApexTime()) {
                    return -1;
                } else if (o1.getApexTime() > o2.getApexTime()) {
                    return 1;
                } else {
                    //compare start times
                    if (o1.getStartTime() < o2.getStartTime()) {
                        return -1;
                    } else if (o1.getStartTime() > o2.getStartTime()) {
                        return 1;
                    } else {
                        //compare stop times
                        if (o1.getStopTime() < o2.getStopTime()) {
                            return -1;
                        } else if (o1.getStopTime() > o2.getStopTime()) {
                            return 1;
                        } else {
                            //compare apex intensities
                            if (o1.getApexIntensity() < o2.getApexIntensity()) {
                                return -1;
                            } else if (o1.getApexIntensity() > o2.getApexIntensity()) {
                                return 1;
                            } else {
                                //compare areas
                                if (o1.getArea() < o2.getArea()) {
                                    return -1;
                                } else if (o1.getArea() > o2.getArea()) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Peak2D> iterator() {
        return Collections.unmodifiableSet(this.peakSet).iterator();
    }
}
