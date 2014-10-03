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
package maltcms.commands.fragments2d.peakfinding.bbh;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.tools.ArrayTools;
import ucar.ma2.Array;

@Slf4j
/**
 * <p>MissingPeak2D class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Data
public class MissingPeak2D {

    int meanFirstScanIndex, meanSecondScanIndex, maxFirstDelta, maxSecondDelta;
    int averageCount = 0;
    List<Integer> missingInChromatogram = new ArrayList<>();
    Array meanMS = null;

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * <p>Constructor for MissingPeak2D.</p>
     */
    public MissingPeak2D() {
    }

    /**
     * <p>Constructor for MissingPeak2D.</p>
     *
     * @param meanFirstScanIndex a int.
     * @param meanSecondScanIndex a int.
     * @param maxFirstDelta a int.
     * @param maxSecondDelta a int.
     */
    public MissingPeak2D(int meanFirstScanIndex, int meanSecondScanIndex,
            int maxFirstDelta, int maxSecondDelta) {
        this.meanFirstScanIndex = meanFirstScanIndex;
        this.meanSecondScanIndex = meanSecondScanIndex;
        this.maxFirstDelta = maxFirstDelta;
        this.maxSecondDelta = maxSecondDelta;
    }

    /**
     * <p>Getter for the field <code>meanFirstScanIndex</code>.</p>
     *
     * @return a int.
     */
    public int getMeanFirstScanIndex() {
        return meanFirstScanIndex;
    }

    /**
     * <p>Getter for the field <code>meanSecondScanIndex</code>.</p>
     *
     * @return a int.
     */
    public int getMeanSecondScanIndex() {
        return meanSecondScanIndex;
    }

    /**
     * <p>Getter for the field <code>maxFirstDelta</code>.</p>
     *
     * @return a int.
     */
    public int getMaxFirstDelta() {
        return maxFirstDelta;
    }

    /**
     * <p>Getter for the field <code>maxSecondDelta</code>.</p>
     *
     * @return a int.
     */
    public int getMaxSecondDelta() {
        return maxSecondDelta;
    }

    /**
     * <p>addMissingChromatogram.</p>
     *
     * @param i a {@link java.lang.Integer} object.
     */
    public void addMissingChromatogram(Integer i) {
        this.missingInChromatogram.add(i);
    }

    /**
     * <p>getMissingChromatogramList.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Integer> getMissingChromatogramList() {
        return this.missingInChromatogram;
    }

    /**
     * <p>Setter for the field <code>meanFirstScanIndex</code>.</p>
     *
     * @param meanFirstScanIndex a int.
     */
    public void setMeanFirstScanIndex(int meanFirstScanIndex) {
        this.meanFirstScanIndex = meanFirstScanIndex;
    }

    /**
     * <p>Setter for the field <code>meanSecondScanIndex</code>.</p>
     *
     * @param meanSecondScanIndex a int.
     */
    public void setMeanSecondScanIndex(int meanSecondScanIndex) {
        this.meanSecondScanIndex = meanSecondScanIndex;
    }

    /**
     * <p>Setter for the field <code>maxFirstDelta</code>.</p>
     *
     * @param maxFirstDelta a int.
     */
    public void setMaxFirstDelta(int maxFirstDelta) {
        this.maxFirstDelta = maxFirstDelta;
    }

    /**
     * <p>Setter for the field <code>maxSecondDelta</code>.</p>
     *
     * @param maxSecondDelta a int.
     */
    public void setMaxSecondDelta(int maxSecondDelta) {
        this.maxSecondDelta = maxSecondDelta;
    }

    /**
     * <p>Getter for the field <code>averageCount</code>.</p>
     *
     * @return a int.
     */
    public int getAverageCount() {
        return averageCount;
    }

    /**
     * <p>Setter for the field <code>averageCount</code>.</p>
     *
     * @param averageCount a int.
     */
    public void setAverageCount(int averageCount) {
        this.averageCount = averageCount;
    }

    /**
     * <p>getMeanPoint.</p>
     *
     * @return a {@link java.awt.Point} object.
     */
    public Point getMeanPoint() {
        return new Point(this.meanFirstScanIndex, this.meanSecondScanIndex);
    }

    /**
     * <p>addMS.</p>
     *
     * @param ms a {@link ucar.ma2.Array} object.
     */
    public void addMS(Array ms) {
        if (this.meanMS != null) {
            this.meanMS = ArrayTools.sum(this.meanMS, ms);
        } else {
            this.meanMS = ms;
        }
    }

    /**
     * <p>Getter for the field <code>meanMS</code>.</p>
     *
     * @return a {@link ucar.ma2.Array} object.
     */
    public Array getMeanMS() {
        return ArrayTools.mult(this.meanMS, 1.0d / (double) this.averageCount);
    }
}
