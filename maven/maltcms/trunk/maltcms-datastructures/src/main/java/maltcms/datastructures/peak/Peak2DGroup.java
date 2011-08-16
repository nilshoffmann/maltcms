/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: Peak1DGroup.java 159 2010-08-31 18:44:07Z nilshoffmann $
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
import cross.datastructures.tuple.Tuple2D;
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
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@Data
public class Peak2DGroup extends Peak2D {

    /**
     * 
     */
    private static final long serialVersionUID = 5613625440101236042L;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TreeSet<Peak2D> peakSet;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Tuple2D<Array, Array> apexMassSpectrum;
    private PeakArea2D peakArea;

    public Peak2DGroup(Peak2D... p) {
        this.peakSet = new TreeSet<Peak2D>(new Peak2DComparator());
        this.peakSet.addAll(Arrays.asList(p));
    }

    public Peak2DGroup(IFileFragment fragment, PeakArea2D peakArea2D, boolean raw) {
        Tuple2D<Array, Array> ms = null;
        Point seedPoint = peakArea2D.getSeedPoint();
        if (raw) {
            ms = MaltcmsTools.getMS(fragment, );
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
        setPeakArea(peakArea);
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

    public Array getMasses() {
        int size = this.peakSet.size();
        ArrayDouble.D1 masses = new ArrayDouble.D1(size);
        Iterator<Peak2D> miter = this.peakSet.iterator();
        int i = 0;
        while (miter.hasNext()) {
            Peak2D p = miter.next();
            masses.set(i++, p.getMw());
        }
        return masses;
    }

    public Array getIntensities() {
        int size = this.peakSet.size();
        ArrayDouble.D1 intensities = new ArrayDouble.D1(size);
        Iterator<Peak2D> miter = this.peakSet.iterator();
        int i = 0;
        while (miter.hasNext()) {
            Peak1D p = miter.next();
            intensities.set(i++, p.getIntensity());
        }
        return intensities;
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Peak2D> p = this.peakSet.iterator();
        while (p.hasNext()) {
            sb.append(p.next() + "\n");
        }
        return sb.toString();
    }

    @Override
    @NoFeature
    public Iterator<Peak1D> iterator() {
        List<Peak1D> peaks = new ArrayList<Peak1D>(Collections.unmodifiableSet(this.peakSet));
        return peaks.iterator();
    }

    public Iterator<Peak2D> peak2dIterator() {
        return this.peakSet.iterator();
    }
//	public static void main(String[] args) {
//		Peak1D p1 = new Peak1D(30, 35, 40, 213.932, 144);
//		p1.setMw(58.32);
//		Peak1D p2 = new Peak1D(28, 35, 42, 3134.932, 1567);
//		p2.setMw(73.923);
//		Peak1D p3 = new Peak1D(26, 35, 41, 252.932, 235);
//		p3.setMw(314.123);
//		Peak1D p4 = new Peak1D(30, 35, 40, 2367.932, 1267);
//		p4.setMw(124.25);
//		Peak1DGroup pg = new Peak1DGroup(p1, p2, p3, p4);
//		System.out.println(pg.getFeatureNames());
//		System.out.println(pg.getFeature("Masses"));
//		System.out.println(pg.getFeature("Intensities"));
//		// System.out.println(pg.getFeature("Peaks"));
//		// System.out.println(pg.getFeature("FeatureNames"));
//		for (Peak1D p : pg) {
//			System.out.print(p);
//		}
//
//		Peak2D p21 = new Peak2D();
//		p21.setApexIndex(50);
//		p21.setStartIndex(30);
//		p21.setStopIndex(80);
//		p21.setFirstRetTime(380);
//		p21.setSecondRetTime(3.12);
//		p21.setMw(123.23);
//		// System.out.println(p21.getFeature("ApexIndex"));
//		// System.out.println(p21.getFeature("StartIndex"));
//		// System.out.println(p21.getFeature("StopIndex"));
//		// System.out.println(p21.getFeature("FirstRetTime"));
//		// System.out.println(p21.getFeature("SecondRetTime"));
//		Peak2D p22 = new Peak2D();
//		p22.setApexIndex(50);
//		p22.setStartIndex(30);
//		p22.setStopIndex(80);
//		p22.setFirstRetTime(380);
//		p22.setSecondRetTime(3.12);
//		p22.setMw(456.014);
//
//		Peak2DGroup pg2 = new Peak2DGroup(p21, p22);
//		System.out.println(pg2.getFeatureNames());
//		System.out.println(pg2.getFeature("Masses"));
//		System.out.println(pg2.getFeature("Intensities"));
//		System.out.println(pg2.getFeature("FirstRetTime"));
//		System.out.println(pg2.getFeature("SecondRetTime"));
//		// System.out.println(pg.getFeature("Peaks"));
//		// System.out.println(pg.getFeature("FeatureNames"));
//		for (Peak2D p : pg2) {
//			System.out.print(p);
//		}
//	}
}
