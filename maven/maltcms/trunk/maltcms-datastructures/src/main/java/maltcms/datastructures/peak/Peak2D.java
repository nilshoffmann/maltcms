/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
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
 * $Id: Peak2D.java 155 2010-08-18 14:43:45Z mwilhelm42 $
 */
package maltcms.datastructures.peak;

import java.io.Serializable;

import java.awt.Point;
import java.util.Iterator;
import lombok.Data;

/**
 * Dataholder for all important information about a two dimensional peak.
 * This abstracts from Peak1D by providing information about the peak 
 * in two dimensions. Note that this Peak2D's firstDimensionStartTime, firstDimensionApexTime and firstDimensionStopTime
 * must not differ. They are included due to PeakGroup2D's abstraction, which 
 * may contain a number of Peak2D objects with differing first and second dimension times.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Data
public class Peak2D extends Peak1D implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6343209775465424292L;
    private Point startPosition = new Point(-1,-1);
    private Point apexPosition = new Point(-1,-1);
    private Point stopPosition = new Point(-1,-1);
    private double firstDimensionStartTime = Double.NaN;
    private double firstDimensionApexTime = Double.NaN;
    private double firstDimensionStopTime = Double.NaN;
    
    private double secondDimensionStartTime = Double.NaN;
    private double secondDimensionApexTime = Double.NaN;
    private double secondDimensionStopTime = Double.NaN;

    public Iterator<Peak2D> iterator2D() {
        final Peak2D thisPeak = this;
        return new Iterator<Peak2D>() {
            private Peak2D peak = thisPeak;
            private boolean hasNext = true;
            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public Peak2D next() {
                Peak2D returnedPeak = this.peak;
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
    
    public static void main(String[] args) {
        Peak2D p = new Peak2D();
        p.setStartIndex(30);
        p.setApexIndex(50);
        p.setStopIndex(80);
        p.setFirstDimensionStartTime(380);
        p.setFirstDimensionApexTime(380);
        p.setFirstDimensionStopTime(380);
        p.setSecondDimensionStartTime(390);
        p.setSecondDimensionApexTime(410);
        p.setSecondDimensionStopTime(440);
        System.out.println(p.getFeature("ApexIndex"));
        System.out.println(p.getFeature("StartIndex"));
        System.out.println(p.getFeature("StopIndex"));
        System.out.println(p.getFeature("FirstDimensionStartTime"));
        System.out.println(p.getFeature("FirstDimensionApexTime"));
        System.out.println(p.getFeature("FirstDimensionStopTime"));
        // System.out.println(p.getFeature("PeakArea"));
    }
}
