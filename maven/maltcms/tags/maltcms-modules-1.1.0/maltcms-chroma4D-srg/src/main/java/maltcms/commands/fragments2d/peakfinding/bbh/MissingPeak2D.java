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
package maltcms.commands.fragments2d.peakfinding.bbh;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.tools.ArrayTools;
import ucar.ma2.Array;

@Slf4j
@Data
public class MissingPeak2D {

    int meanFirstScanIndex, meanSecondScanIndex, maxFirstDelta, maxSecondDelta;
    int averageCount = 0;
    List<Integer> missingInChromatogram = new ArrayList<Integer>();
    Array meanMS = null;

    @Override
    public String toString() {
        return getClass().getName();
    }

    public MissingPeak2D() {
    }

    public MissingPeak2D(int meanFirstScanIndex, int meanSecondScanIndex,
            int maxFirstDelta, int maxSecondDelta) {
        this.meanFirstScanIndex = meanFirstScanIndex;
        this.meanSecondScanIndex = meanSecondScanIndex;
        this.maxFirstDelta = maxFirstDelta;
        this.maxSecondDelta = maxSecondDelta;
    }

    public int getMeanFirstScanIndex() {
        return meanFirstScanIndex;
    }

    public int getMeanSecondScanIndex() {
        return meanSecondScanIndex;
    }

    public int getMaxFirstDelta() {
        return maxFirstDelta;
    }

    public int getMaxSecondDelta() {
        return maxSecondDelta;
    }

    public void addMissingChromatogram(Integer i) {
        this.missingInChromatogram.add(i);
    }

    public List<Integer> getMissingChromatogramList() {
        return this.missingInChromatogram;
    }

    public void setMeanFirstScanIndex(int meanFirstScanIndex) {
        this.meanFirstScanIndex = meanFirstScanIndex;
    }

    public void setMeanSecondScanIndex(int meanSecondScanIndex) {
        this.meanSecondScanIndex = meanSecondScanIndex;
    }

    public void setMaxFirstDelta(int maxFirstDelta) {
        this.maxFirstDelta = maxFirstDelta;
    }

    public void setMaxSecondDelta(int maxSecondDelta) {
        this.maxSecondDelta = maxSecondDelta;
    }

    public int getAverageCount() {
        return averageCount;
    }

    public void setAverageCount(int averageCount) {
        this.averageCount = averageCount;
    }

    public Point getMeanPoint() {
        return new Point(this.meanFirstScanIndex, this.meanSecondScanIndex);
    }

    public void addMS(Array ms) {
        if (this.meanMS != null) {
            this.meanMS = ArrayTools.sum(this.meanMS, ms);
        } else {
            this.meanMS = ms;
        }
    }

    public Array getMeanMS() {
        return ArrayTools.mult(this.meanMS, 1.0d / (double) this.averageCount);
    }
}
