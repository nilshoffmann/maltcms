/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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

import java.awt.Point;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import maltcms.tools.ArrayTools2;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import cross.annotations.RequiresVariables;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is a dataholder for mass spectra. This class will cache all mass
 * spectra which were read an hold a {@link SoftReference} to it.
 *
 * TODO: Change to {@link AScanLineCache}
 *
 * @author Mathias Wilhelm
 */
@RequiresVariables(names = {"var.mass_values", "var.intensity_values",
    "var.scan_index", "var.mass_range_min", "var.mass_range_max",
    "var.modulation_time", "var.scan_rate"})
@Slf4j
public class SparseScanLineCache implements IScanLine {

    private String intensityValuesVar = "intensity_values";
    private String scanIndexVar = "scan_index";
    private String massValuesVar = "mass_values";
    // private String minRangeVar = "mass_range_min";
    private String maxRangeVar = "mass_range_max";
    private String modulationVar = "modulation_time";
    private String scanRateVar = "scan_rate";
    private String secondColumnIndexVar = "second_column_scan_index";
    private List<List<Integer>> xyToScanIndexMap;
    private IVariableFragment scanIndex = null;
    private IVariableFragment massValues = null;
    private IVariableFragment massIntensities = null;
    private final IFileFragment iff;
    private int lastIndex = -1;
    private final int scansPerModulation;
    private int binSize = -1;
    // private final HashMap<Integer, SoftReference<Tuple2D<List<Array>,
    // List<Array>>>> cache = new HashMap<Integer,
    // SoftReference<Tuple2D<List<Array>, List<Array>>>>();
    private final HashMap<Integer, SoftReference<List<Tuple2D<Array, Array>>>> cache = new HashMap<Integer, SoftReference<List<Tuple2D<Array, Array>>>>();
    private int cachemiss = 0;
    private int cachehit = 0;
    private int loads = 0;
    private int loadnew = 0;
    // private long time = 0L;
    private boolean cacheModulations = true;

    /**
     * This constructor will automatically set the number of scans per
     * modulation and the last index.
     *
     * @param iff1 file fragment
     */
    protected SparseScanLineCache(final IFileFragment iff1) {
        this.iff = iff1;
        final double modulation = this.iff.getChild(this.modulationVar).getArray().getDouble(Index.scalarIndexImmutable);
        final double scanRate = this.iff.getChild(this.scanRateVar).getArray().getDouble(Index.scalarIndexImmutable);
        this.scansPerModulation = (int) (modulation * scanRate);
        estimateBinSize();
        estimateLastIndex();
        setUpxyIndexMap();
    }

    /**
     * Default constructor.
     *
     * @param iff1 file fragment
     * @param scansPerModulation1 scans per modulation
     * @param lastIndex1 index of the last scan
     */
    protected SparseScanLineCache(final IFileFragment iff1,
            final int scansPerModulation1, final int lastIndex1) {
        this.iff = iff1;
        this.scansPerModulation = scansPerModulation1;
        this.lastIndex = lastIndex1;
        setUpxyIndexMap();
    }

    private void setUpxyIndexMap() {
        this.xyToScanIndexMap = new ArrayList<List<Integer>>();
        final Array secondScanIndex = this.iff.getChild(this.secondColumnIndexVar).getArray();
        final IndexIterator iter = secondScanIndex.getIndexIterator();
        int lastScanIndex = iter.getIntNext();
        while (iter.hasNext()) {
            List<Integer> tmpList = new ArrayList<Integer>();
            final int currentScanIndex = iter.getIntNext();
            for (; lastScanIndex < currentScanIndex; lastScanIndex++) {
                tmpList.add(lastScanIndex);
            }
            this.xyToScanIndexMap.add(tmpList);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        this.massValuesVar = cfg.getString("var.mass_values", "mass_values");
        this.intensityValuesVar = cfg.getString("var.intensity_values",
                "intensity_values");
        this.scanIndexVar = cfg.getString("var.scan_index", "scan_index");
        // this.minRangeVar = cfg
        // .getString("var.mass_range_min", "mass_range_min");
        this.maxRangeVar = cfg.getString("var.mass_range_max", "mass_range_max");
        this.modulationVar = cfg.getString("var.modulation_time",
                "modulation_time");
        this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
    }

    /**
     * Will compute the max range of an chromatogram and sets the bin size to
     * max range + 1.
     */
    private void estimateBinSize() {
        final Array maxRange = this.iff.getChild(this.maxRangeVar).getArray();
        final IndexIterator iter = maxRange.getIndexIterator();
        while (iter.hasNext()) {
            final int currentMaxRange = iter.getIntNext();
            if (this.binSize < currentMaxRange) {
                this.binSize = currentMaxRange;
            }
        }
        this.binSize++;
    }

    /**
     * Uses the scan_index shape to estimate the last index.
     */
    private void estimateLastIndex() {
        this.lastIndex = this.iff.getChild(this.scanIndexVar).getArray().getShape()[0];
    }

    /**
     * Getter.
     *
     * @return bin size
     */
    public int getBinsSize() {
        return this.binSize;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public boolean getCacheModulation() {
        return this.cacheModulations;
    }

    /**
     * Getter.
     *
     * @return last index
     */
    public int getLastIndex() {
        return this.lastIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Array getMassSpectra(final int x, final int y) {
        try {
            if ((x >= 0) && (y >= 0) && (y < this.scansPerModulation)
                    && (x < this.getScanLineCount())) {
                return getScanlineMS(x).get(y);
            } else {
                return null;
            }
        } catch (final IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Array getMassSpectra(final Point p) {
        return getMassSpectra(p.x, p.y);
    }

    /**
     * Returns normalized Arrays.
     *
     * @param massValuesA mass values
     * @param massIntensitiesA mass intensities
     * @return length normalized array
     */
    private List<Array> getNormalizedArray(final List<Tuple2D<Array, Array>> mss) {
        if (mss != null) {
            final List<Array> normalized = new ArrayList<Array>();
            for (Tuple2D<Array, Array> ms : mss) {
                normalized.add(ArrayTools2.normalize(ms.getFirst(), ms.getSecond(), this.binSize, this.log));
            }
            return normalized;
        }
        return null;
    }

    /**
     * Getter.
     *
     * @return scan line count
     */
    public int getScanLineCount() {
        return (this.lastIndex / this.scansPerModulation);
    }

    /**
     * Getter.
     *
     * @param x scan line number
     * @return complete ms list of this scan line
     */
    public List<Array> getScanlineMS(final int x) {
        return getNormalizedArray(getScanlineSparseMS(x));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getScansPerModulation() {
        return this.scansPerModulation;
    }

    /**
     * Getter. Will load the scanline from a given {@link IFileFragment}.
     *
     * @param x scan line number
     * @return complete ms list of the scan line
     */
    private synchronized List<Tuple2D<Array, Array>> loadScanline(final int x) {
        try {
//            int maxindex = ((x + 1) * this.scansPerModulation) - 1;
            int last = this.xyToScanIndexMap.get(x).size() - 1;
            int maxindex = this.xyToScanIndexMap.get(x).get(last);
            if (maxindex > this.lastIndex) {
                maxindex = this.lastIndex;
            }
//            final Range range = new Range(x * this.scansPerModulation, maxindex);
            final Range range = new Range(this.xyToScanIndexMap.get(x).get(0), maxindex);
            final Range[] r = new Range[]{range};
            this.scanIndex.setRange(r);
            this.massValues.setIndex(this.scanIndex);
            this.massIntensities.setIndex(this.scanIndex);

            // this.massValues.setIndexedArray(null);
            // this.massIntensities.setIndexedArray(null);
            final List<Array> massValuesA = this.massValues.getIndexedArray();
            final List<Array> massIntensitiesA = this.massIntensities.getIndexedArray();

            final List<Tuple2D<Array, Array>> sl = new ArrayList<Tuple2D<Array, Array>>();
            for (int i = 0; i < massValuesA.size()
                    & i < massIntensitiesA.size(); i++) {
                sl.add(new Tuple2D<Array, Array>(massValuesA.get(i),
                        massIntensitiesA.get(i)));
            }

            if (this.cacheModulations) {
                final SoftReference<List<Tuple2D<Array, Array>>> sr = new SoftReference<List<Tuple2D<Array, Array>>>(
                        sl);
                this.cache.put(Integer.valueOf(x), sr);
            }
            this.loadnew++;
            return sl;
        } catch (final InvalidRangeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Setter.
     *
     * @param size bin size
     */
    public void setBinSize(final int size) {
        this.binSize = size;
    }

    /**
     * {@inheritDoc}
     */
    public void setCacheModulations(final boolean cacheMod) {
        this.cacheModulations = cacheMod;
    }

    /**
     * Setter.
     *
     * @param index last index
     */
    public void setLastIndex(final int index) {
        this.lastIndex = index;
    }

    /**
     * Will view some statistical information about the cache usage.
     */
    public void showStat() {
        this.log.info("Statistic for ScanLineCache:");
        this.log.info("	Loads    : {}", this.loads);
        this.log.info("	New      : {}", this.loadnew);
        this.log.info("	Cached   : {}", this.cachehit);
        this.log.info("	Cachemiss: {}", this.cachemiss);
    }

    @Override
    public List<Tuple2D<Array, Array>> getScanlineSparseMS(int x) {
        this.loads++;
        if (this.scanIndex == null) {
            // TODO second_scan_index anstatt?
            this.scanIndex = this.iff.getChild(this.scanIndexVar);
        }
        if (this.massValues == null) {
            this.massValues = this.iff.getChild(this.massValuesVar, true);
        }
        if (this.massIntensities == null) {
            this.massIntensities = this.iff.getChild(this.intensityValuesVar,
                    true);
        }
        final Integer scan = Integer.valueOf(x);
        if (this.cache.containsKey(scan)) {
            if (this.cache.get(scan).get() != null) {
                this.cachehit++;
                return this.cache.get(scan).get();
            }
            this.cachemiss++;
        }
        return loadScanline(x);
    }

    @Override
    public Tuple2D<Array, Array> getSparseMassSpectra(int x, int y) {
        try {
            if ((x >= 0) && (y >= 0) && (y < this.scansPerModulation)
                    && (x < this.getScanLineCount())) {
                return getScanlineSparseMS(x).get(y);
            } else {
                return null;
            }
        } catch (final IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public Tuple2D<Array, Array> getSparseMassSpectra(Point p) {
        return getSparseMassSpectra(p.x, p.y);
    }

    @Override
    public Point mapIndex(int scanIndex) {
        int x = scanIndex / getScansPerModulation();
        int y = scanIndex % getScansPerModulation();
        return new Point(x, y);
    }

    @Override
    public int mapPoint(int x, int y) {
        return (x * getScansPerModulation()) + y;
    }

    @Override
    public int mapPoint(Point p) {
        return mapPoint(p.x, p.y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
    }
}
