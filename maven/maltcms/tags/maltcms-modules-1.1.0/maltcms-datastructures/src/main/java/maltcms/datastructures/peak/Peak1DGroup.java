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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.annotations.NoFeature;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.tools.MathTools;
import java.util.ArrayList;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
//@Data
public class Peak1DGroup extends Peak1D {

    /**
     * 
     */
    private static final long serialVersionUID = 5613625440101236042L;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TreeSet<Peak1D> peakSet;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Tuple2D<Array, Array> apexMassSpectrum;

    public Peak1DGroup(IFileFragment fragment, int scanIndex, boolean raw) {
        Tuple2D<Array, Array> ms = null;
        if (raw) {
            ms = MaltcmsTools.getMS(fragment, scanIndex);
        } else {
            ms = MaltcmsTools.getBinnedMS(fragment, scanIndex);
        }
        apexMassSpectrum = ms;
        MinMax mm = MAMath.getMinMax(ms.getFirst());
        Peak1D peak = PeakFactory.createPeak1DTic(fragment, scanIndex, scanIndex, scanIndex);
        setApexIndex(peak.getApexIndex());
        setStartIndex(peak.getStartIndex());
        setStopIndex(peak.getStopIndex());
        setApexTime(peak.getApexTime());
        setStartTime(peak.getStartTime());
        setStopTime(peak.getStopTime());
        setApexIntensity(peak.getApexIntensity());
        setArea(peak.getArea());
        setStartMass(mm.min);
        setStopMass(mm.max);
    }

    public Peak1DGroup(Peak1D... p) {
        this.peakSet = new TreeSet<Peak1D>(new Peak1DComparator());
        this.peakSet.addAll(Arrays.asList(p));
        int startIndex = Integer.MAX_VALUE;
        int stopIndex = Integer.MIN_VALUE;
        int[] apexIndex = new int[p.length];
        double[] apexTime = new double[p.length];
        double startTime = Double.POSITIVE_INFINITY;
        double stopTime = Double.NEGATIVE_INFINITY;
        double area = 0.0d;
        double apexIntensity = 0.0d;
        double startMass = Double.POSITIVE_INFINITY;
        double stopMass = Double.NEGATIVE_INFINITY;
        int i = 0;
        for (Peak1D peak : peakSet) {
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
            if (peak.getStartMass() < startMass) {
                startMass = peak.getStartMass();
            }
            if (peak.getStopMass() > stopMass) {
                stopMass = peak.getStopMass();
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
        setStartMass(startMass);
        setStopMass(stopMass);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
     */
    @Override
    public Array getFeature(String name) {
        return super.getFeature(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.datastructures.array.IFeatureVector#getFeatureNames()
     */
    @Override
    public List<String> getFeatureNames() {
        return super.getFeatureNames();
    }

    public Tuple2D<Array, Array> getMassSpectrum(int index) {
        int i = 0;
        int globalStartIdx = getStartIndex();
        int globalStopIdx = getStopIndex();
        EvalTools.inRangeI(globalStartIdx, globalStopIdx, index, this);
        if (apexMassSpectrum == null) {
            int iPrime = globalStartIdx + index;
            ArrayDouble.D1 masses = new ArrayDouble.D1(peakSet.size());
            ArrayDouble.D1 intensities = new ArrayDouble.D1(peakSet.size());
            for (Peak1D peak : peakSet) {
                int startIdx = peak.getStartIndex();
                int localIndex = (iPrime - startIdx);
                double[] eic = peak.getExtractedIonCurrent();
                if (localIndex >= 0 && localIndex < eic.length) { //peak starts later
                    double value = eic[localIndex];
                    //set intensity only if peak has intensity at given scan
                    intensities.set(i, value);
                }
                //set mass in any case
                masses.set(i, peak.getStartMass());
                i++;
            }
            return new Tuple2D<Array, Array>(masses, intensities);
        } else {
            return apexMassSpectrum;
        }
    }

    public Tuple2D<Array, Array> getMassSpectrum() {
        return getMassSpectrum(getApexIndex());
    }

    public List<Tuple2D<Array, Array>> getMassSpectra() {
        List<Tuple2D<Array, Array>> massSpectra = new ArrayList<Tuple2D<Array, Array>>();
        if (apexMassSpectrum != null) {
            massSpectra.add(apexMassSpectrum);
            return massSpectra;
        }
        for (int j = 0; j <= (getStopIndex() - getStartIndex()); j++) {
            massSpectra.add(getMassSpectrum(j));
        }
        return massSpectra;
    }

    private final class Peak1DComparator implements Comparator<Peak1D> {

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Peak1D o1, Peak1D o2) {
            //compare start masses
            if (o1.getStartMass() < o2.getStartMass()) {
                return -1;
            } else if (o1.getStartMass() > o2.getStartMass()) {
                return 1;
            } else {
                //compare stop masses
                if (o1.getStopMass() < o2.getStopMass()) {
                    return -1;
                } else if (o1.getStopMass() > o2.getStopMass()) {
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
    }

//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        Iterator<Peak1D> p = this.peakSet.iterator();
//        while (p.hasNext()) {
//            sb.append(p.next() + "\n");
//        }
//        return sb.toString();
//    }
    @Override
    @NoFeature
    public Iterator<Peak1D> iterator() {
        return Collections.unmodifiableSet(this.peakSet).iterator();
    }
//    public static void main(String[] args) {
//        Peak1D p1 = new Peak1D(30, 35, 40, 213.932, 144);
//        p1.setMw(58.32);
//        Peak1D p2 = new Peak1D(28, 35, 42, 3134.932, 1567);
//        p2.setMw(73.923);
//        Peak1D p3 = new Peak1D(26, 35, 41, 252.932, 235);
//        p3.setMw(314.123);
//        Peak1D p4 = new Peak1D(30, 35, 40, 2367.932, 1267);
//        p4.setMw(124.25);
//        Peak1DGroup pg = new Peak1DGroup(p1, p2, p3, p4);
//        System.out.println(pg.getFeatureNames());
//        System.out.println(pg.getFeature("Masses"));
//        System.out.println(pg.getFeature("Intensities"));
//        // System.out.println(pg.getFeature("Peaks"));
//        // System.out.println(pg.getFeature("FeatureNames"));
//        for (Peak1D p : pg) {
//            System.out.print(p);
//        }
//
//        Peak2D p21 = new Peak2D();
//        p21.setApexIndex(50);
//        p21.setStartIndex(30);
//        p21.setStopIndex(80);
//        p21.setFirstRetTime(380);
//        p21.setSecondRetTime(3.12);
//        p21.setMw(123.23);
//        // System.out.println(p21.getFeature("ApexIndex"));
//        // System.out.println(p21.getFeature("StartIndex"));
//        // System.out.println(p21.getFeature("StopIndex"));
//        // System.out.println(p21.getFeature("FirstRetTime"));
//        // System.out.println(p21.getFeature("SecondRetTime"));
//        Peak2D p22 = new Peak2D();
//        p22.setApexIndex(50);
//        p22.setStartIndex(30);
//        p22.setStopIndex(80);
//        p22.setFirstRetTime(380);
//        p22.setSecondRetTime(3.12);
//        p22.setMw(456.014);
//
//        Peak1DGroup pg2 = new Peak1DGroup(p21, p22);
//        System.out.println(pg2.getFeatureNames());
//        System.out.println(pg2.getFeature("Masses"));
//        System.out.println(pg2.getFeature("Intensities"));
//        // System.out.println(pg.getFeature("Peaks"));
//        // System.out.println(pg.getFeature("FeatureNames"));
//        for (Peak1D p : pg2) {
//            System.out.print(p);
//        }
//    }
}