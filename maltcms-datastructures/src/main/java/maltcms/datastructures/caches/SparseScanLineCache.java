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
import cross.cache.CacheFactory;
import cross.cache.ICacheDelegate;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.NotImplementedException;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;
import java.awt.Point;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.tools.ArrayTools2;
import maltcms.tools.MaltcmsTools;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Dimension;

/**
 * This class is a dataholder for mass spectra. This class will cache all mass
 * spectra which were read an hold a {@link java.lang.ref.SoftReference} to it.
 *
 * TODO: Change to {@link maltcms.datastructures.caches.AScanLineCache}
 *
 * @author Mathias Wilhelm
 *
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
//    private String maxRangeVar = "mass_range_max";
    private String modulationVar = "modulation_time";
    private String scanRateVar = "scan_rate";
    private String totalIntensityVar = "total_intensity";
    private String secondColumnIndexVar = "second_column_scan_index";
    private List<List<Integer>> xyToScanIndexMap;
    private IVariableFragment scanIndex = null;
    private IVariableFragment massValues = null;
    private IVariableFragment massIntensities = null;
    private final IFileFragment iff;
    private int lastIndex = -1;
    private final int scansPerModulation;
    private final double minMass;
    private final double maxMass;
    private final double massResolution;
    private final ICacheDelegate<Integer, List<Tuple2D<Array, Array>>> cache;
    private final ICacheDelegate<Integer, Array> normalizedArrayCache;// private long time = 0L;
    private boolean cacheModulations = true;

    /**
     * This constructor will automatically set the number of scans per
     * modulation and the last index.
     *
     * @param iff1 file fragment
     * @param minMass a double.
     * @param maxMass a double.
     * @param massResolution a double.
     */
    protected SparseScanLineCache(final IFileFragment iff1, double minMass, double maxMass, double massResolution) {
        this.iff = iff1;
        final double modulation = this.iff.getChild(this.modulationVar).getArray().getDouble(Index.scalarIndexImmutable);
        final double scanRate = this.iff.getChild(this.scanRateVar).getArray().getDouble(Index.scalarIndexImmutable);
        this.scansPerModulation = (int) (modulation * scanRate);
        estimateLastIndex();
        setUpxyIndexMap();
        int scanLinesToCache = 50;
        int normalizedArraysToCache = scanLinesToCache * scansPerModulation;
        String cacheName = StringTools.removeFileExt(iff1.getName()) + "-scanLineCache";
        cache = CacheFactory.createVolatileCache(cacheName, 30, 60, scanLinesToCache);
        String arrayCacheName = StringTools.removeFileExt(iff1.getName()) + "-normalizedArrayCache";
        normalizedArrayCache = CacheFactory.createVolatileCache(arrayCacheName, 30, 60, normalizedArraysToCache);
        this.minMass = minMass;
        this.maxMass = maxMass;
        this.massResolution = massResolution;
    }

    /**
     * Default constructor.
     *
     * @param iff1 file fragment
     * @param scansPerModulation1 scans per modulation
     * @param lastIndex1 index of the last scan
     * @param minMass a double.
     * @param maxMass a double.
     * @param massResolution a double.
     */
    protected SparseScanLineCache(final IFileFragment iff1,
            final int scansPerModulation1, final int lastIndex1, final double minMass, final double maxMass, final double massResolution) {
        this.iff = iff1;
        this.scansPerModulation = scansPerModulation1;
        this.lastIndex = lastIndex1;
        setUpxyIndexMap();
        String cacheName = StringTools.removeFileExt(iff1.getName()) + "-scanLineCache";
        cache = CacheFactory.createVolatileCache(cacheName, 60, 120, 200);
        String arrayCacheName = StringTools.removeFileExt(iff1.getName()) + "-normalizedArrayCache";
        normalizedArrayCache = CacheFactory.createVolatileCache(arrayCacheName, 60, 120, 5000);
        this.minMass = minMass;
        this.maxMass = maxMass;
        this.massResolution = massResolution;
    }

    private Array createUpxyIndexMap(IFileFragment iff) {
        final Integer scanspermodulation = getScansPerModulation();
        int nscans = iff.getChild(totalIntensityVar, true).getDimensions()[0].getLength();
        Integer modulationCnt = nscans/scanspermodulation + nscans%scanspermodulation==0?0:1;
        final ArrayInt.D1 secondColumnIndex = new ArrayInt.D1(modulationCnt);
        for (int i = 0; i < modulationCnt; i++) {
            secondColumnIndex.set(i, scanspermodulation * i);
        }
        final IVariableFragment index2dvar = new VariableFragment(iff,
                this.secondColumnIndexVar);
        index2dvar.setArray(secondColumnIndex);
        index2dvar.setDimensions(new Dimension[]{new Dimension(
            "modulation_index_0", modulationCnt, true)});
        return secondColumnIndex;
    }

    private void setUpxyIndexMap() {
        this.xyToScanIndexMap = new ArrayList<>();
        Array secondScanIndex = null;
        try {
            secondScanIndex = this.iff.getChild(this.secondColumnIndexVar).getArray();
        } catch (ResourceNotAvailableException rnae) {
            secondScanIndex = createUpxyIndexMap(this.iff);
        }
        final IndexIterator iter = secondScanIndex.getIndexIterator();
        int lastScanIndex = iter.getIntNext();
        while (iter.hasNext()) {
            List<Integer> tmpList = new ArrayList<>();
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
        this.modulationVar = cfg.getString("var.modulation_time",
                "modulation_time");
        this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
    }

    /**
     * Uses the scan_index shape to estimate the last index.
     */
    private void estimateLastIndex() {
        this.lastIndex = this.iff.getChild(this.scanIndexVar).getArray().getShape()[0];
    }

    /**
     * {@inheritDoc}
     *
     * Getter.
     */
    @Override
    public int getBinsSize() {
        return MaltcmsTools.getNumberOfIntegerMassBins(minMass, maxMass, massResolution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCacheModulation() {
        return this.cacheModulations;
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
     */
    @Override
    public Array getMassSpectrum(final int x, final int y) {
        try {
            if ((x >= 0) && (y >= 0) && (y < this.scansPerModulation)
                    && (x < this.getScanLineCount())) {
                return getScanlineMS(x).get(y);
            } else {
                log.error("Tried to access mass spectrum outside of bounds at x=" + x + " y=" + y);
                return null;
            }
        } catch (final IndexOutOfBoundsException e) {
            log.error("Tried to access mass spectrum outside of bounds at x=" + x + " y=" + y, e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Array getMassSpectrum(final Point p) {
        return getMassSpectrum(p.x, p.y);
    }

    /**
     * Returns normalized Arrays.
     *
     * @param massValuesA mass values
     * @param massIntensitiesA mass intensities
     * @return length normalized array
     */
    private List<Array> getNormalizedArray(final List<Tuple2D<Array, Array>> mss, final int scanLine) {
        if (mss.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Array> normalized = new ArrayList<>();
        int i = 0;
        for (Tuple2D<Array, Array> ms : mss) {
            int idx = mapPoint(scanLine, i);
            Array normalizedArray = normalizedArrayCache.get(idx);
            if (normalizedArray == null) {
                normalizedArray = ArrayTools2.normalize(ms.getFirst(), ms.getSecond(), massResolution, this.log, minMass, maxMass);
                normalizedArrayCache.put(idx, normalizedArray);
            }
            normalized.add(normalizedArray);
            i++;
        }
        return normalized;
    }

    /**
     * {@inheritDoc}
     *
     * Getter.
     */
    @Override
    public int getScanLineCount() {
        return (this.lastIndex / this.scansPerModulation);
    }

    /**
     * {@inheritDoc}
     *
     * Getter.
     */
    @Override
    public List<Array> getScanlineMS(final int x) {
        return getNormalizedArray(getScanlineSparseMS(x), x);
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
        if (x > this.xyToScanIndexMap.size() - 1) {
            return Collections.emptyList();
        }
        try {
            int last = this.xyToScanIndexMap.get(x).size() - 1;
            int maxindex = this.xyToScanIndexMap.get(x).get(last);
            if (maxindex > this.lastIndex) {
                maxindex = this.lastIndex;
            }
            final Range range = new Range(this.xyToScanIndexMap.get(x).get(0), maxindex);
            final Range[] r = new Range[]{range};
            this.scanIndex.setRange(r);
            this.massValues.setIndex(this.scanIndex);
            this.massIntensities.setIndex(this.scanIndex);

            final List<Array> massValuesA = this.massValues.getIndexedArray();
            final List<Array> massIntensitiesA = this.massIntensities.getIndexedArray();

            final List<Tuple2D<Array, Array>> sl = new ArrayList<>();
            for (int i = 0; i < massValuesA.size()
                    & i < massIntensitiesA.size(); i++) {
                sl.add(new Tuple2D<>(massValuesA.get(i),
                        massIntensitiesA.get(i)));
            }

            if (this.cacheModulations) {
                this.cache.put(x, sl);
            }
            return sl;
        } catch (final InvalidRangeException | IndexOutOfBoundsException e) {
            log.error("Caught an exception while trying to access scan line " + x, e);
        }

        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     *
     * Setter.
     */
    @Override
    public void setBinSize(final int size) {
        throw new NotImplementedException("This method is deprecated!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCacheModulations(final boolean cacheMod) {
        this.cacheModulations = cacheMod;
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
     * {@inheritDoc}
     *
     * Will view some statistical information about the cache usage.
     */
    @Override
    public void showStat() {
//		this.log.info("Statistic for ScanLineCache:");
//		this.log.info("	Loads    : {}", this.loads);
//		this.log.info("	New      : {}", this.loadnew);
//		this.log.info("	Cached   : {}", this.cachehit);
//		this.log.info("	Cachemiss: {}", this.cachemiss);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tuple2D<Array, Array>> getScanlineSparseMS(int x) {
        if (this.scanIndex == null) {
            this.scanIndex = this.iff.getChild(this.scanIndexVar);
        }
        if (this.massValues == null) {
            this.massValues = this.iff.getChild(this.massValuesVar, true);
        }
        if (this.massIntensities == null) {
            this.massIntensities = this.iff.getChild(this.intensityValuesVar,
                    true);
        }
        final Integer scan = x;
        List<Tuple2D<Array, Array>> t = this.cache.get(scan);
        if (t != null && !t.isEmpty()) {
            log.debug("Retrieved ms from cache!");
            return t;
        }
        return loadScanline(x);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tuple2D<Array, Array> getSparseMassSpectrum(int x, int y) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Tuple2D<Array, Array> getSparseMassSpectrum(Point p) {
        return getSparseMassSpectrum(p.x, p.y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point mapIndex(int scanIndex) {
        int x = scanIndex / getScansPerModulation();
        int y = scanIndex % getScansPerModulation();
        return new Point(x, y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int mapPoint(int x, int y) {
        return (x * getScansPerModulation()) + y;
    }

    /**
     * {@inheritDoc}
     */
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
