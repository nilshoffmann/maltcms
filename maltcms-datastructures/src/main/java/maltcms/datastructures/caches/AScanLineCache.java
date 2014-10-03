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
package maltcms.datastructures.caches;

import cross.annotations.RequiresVariables;
import cross.datastructures.fragments.IFileFragment;
import java.awt.Point;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;

/**
 * This class is a dataholder for mass spectra. This class will cache all mass
 * spectra which were read an hold a {@link java.lang.ref.SoftReference} to it.
 *
 * @author Mathias Wilhelm
 * 
 */
@RequiresVariables(names = {"var.mass_values", "var.intensity_values",
    "var.scan_index", "var.mass_range_min", "var.mass_range_max",
    "var.modulation_time", "var.scan_rate"})
@Slf4j
public abstract class AScanLineCache implements IScanLine {

    /** {@inheritDoc} */
    @Override
    public Point mapIndex(int scanIndex) {
        int x = scanIndex / getScansPerModulation();
        int y = scanIndex % getScansPerModulation();
        return new Point(x, y);
    }

    /** {@inheritDoc} */
    @Override
    public int mapPoint(int x, int y) {
        return (x * getScansPerModulation()) + y;
    }

    /** {@inheritDoc} */
    @Override
    public int mapPoint(Point p) {
        return mapPoint(p.x, p.y);
    }
    private String scanIndexVar = "scan_index";
    private String maxRangeVar = "mass_range_max";
    private String modulationVar = "modulation_time";
    private String scanRateVar = "scan_rate";
    private IFileFragment iff;
    private int lastIndex = -1;
    private int scansPerModulation;
    private int binSize = -1;
    private boolean cacheModulations = true;

    /**
     * This constructor will automatically set the number of scans per
     * modulation and the last index.
     *
     * @param iff1 file fragment
     */
    protected AScanLineCache(final IFileFragment iff1) {
        log.info("Initing AScanLineCache!");
        this.iff = iff1;
        final int modulation = this.iff.getChild(this.modulationVar).getArray()
                .getInt(Index.scalarIndexImmutable);
        final int scanRate = this.iff.getChild(this.scanRateVar).getArray()
                .getInt(Index.scalarIndexImmutable);
        this.scansPerModulation = modulation * scanRate;
        log.info("Estimating bin size!");
        estimateBinSize();
        log.info("Estimating last index!");
        estimateLastIndex();
    }

    /**
     * {@inheritDoc}
     *
     * Setter.
     */
    @Override
    public void setBinSize(final int size) {
        this.binSize = size;
    }

    /**
     * {@inheritDoc}
     *
     * Getter.
     */
    @Override
    public int getBinsSize() {
        return this.binSize;
    }

    /**
     * Will compute the max range of an chromatogram and sets the bin size to
     * max range + 1.
     */
    private void estimateBinSize() {
        // Tuple2D<Double, Double> t =
        // MaltcmsTools.getMinMaxMassRange(this.iff);
        final Array maxRange = this.iff.getChild(this.maxRangeVar).getArray();
        final IndexIterator iter = maxRange.getIndexIterator();
        while (iter.hasNext()) {
            final int currentMaxRange = iter.getIntNext();
            if (this.binSize < currentMaxRange) {
                this.binSize = currentMaxRange;
            }
        }
        // this.binSize = t.getSecond().intValue();
        this.binSize++;
    }

    /**
     * {@inheritDoc}
     *
     * Getter.
     */
    @Override
    public int getLastIndex() {
        return this.lastIndex;
    }

    /**
     * {@inheritDoc}
     *
     * Setter.
     */
    @Override
    public void setLastIndex(final int index) {
        this.lastIndex = index;
    }

    /**
     * Uses the scan_index shape to estimate the last index.
     */
    private void estimateLastIndex() {
        this.lastIndex = this.iff.getChild(this.scanIndexVar).getArray()
                .getShape()[0] - 1;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        this.scanIndexVar = cfg.getString("var.scan_index", "scan_index");
        this.maxRangeVar = cfg
                .getString("var.mass_range_max", "mass_range_max");
        this.modulationVar = cfg.getString("var.modulation_time",
                "modulation_time");
        this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
    }

    /**
     * {@inheritDoc}
     *
     * Will view some statistical information about the cache usage.
     */
    @Override
    public void showStat() {
    }

    /**
     * {@inheritDoc}
     *
     * Getter.
     */
    @Override
    public int getScanLineCount() {
        return (int) (this.lastIndex / this.scansPerModulation);
    }

    /** {@inheritDoc} */
    @Override
    public Array getMassSpectrum(final Point p) {
        return getMassSpectrum(p.x, p.y);
    }

    /** {@inheritDoc} */
    @Override
    public void setCacheModulations(final boolean cacheMod) {
        this.cacheModulations = cacheMod;
    }

    /** {@inheritDoc} */
    @Override
    public boolean getCacheModulation() {
        return this.cacheModulations;
    }

    /** {@inheritDoc} */
    @Override
    public int getScansPerModulation() {
        return this.scansPerModulation;
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
    }
}
