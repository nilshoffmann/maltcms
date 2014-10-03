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
package maltcms.datastructures.ms;

import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ResourceNotAvailableException;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.tools.MaltcmsTools;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;
import ucar.ma2.MAMath;

/**
 * Concrete Implementation of a 1-dimensional chromatogram.
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
public class Chromatogram2D implements IChromatogram2D {

    private IFileFragment parent;
    private IScanLine isl;
    private final String scanAcquisitionTimeUnit = "seconds";
    private final String secondColumnScanAcquisitionTimeUnit = "seconds";
    @Configurable(name = "var.scan_acquisition_time")
    private String scan_acquisition_time_var = "scan_acquisition_time";
    private final IVariableFragment scanAcquisitionTimeVar;
    private double md = -1;
    private final int spm;
    private final int slc;
    private double satOffset = 0;
    private Tuple2D<Double, Double> massRange;
    private Tuple2D<Double, Double> timeRange;
    private int numberOfScans = -1;
    private Array msLevel;
    private Map<Short, List<Integer>> msScanMap;

    /**
     * <p>Constructor for Chromatogram2D.</p>
     *
     * @param e a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public Chromatogram2D(final IFileFragment e) {
        this.parent = e;
        Tuple2D<Double, Double> massRange = getMassRange();
        ScanLineCacheFactory.setMinMass(massRange.getFirst());
        ScanLineCacheFactory.setMaxMass(massRange.getSecond());
        this.isl = ScanLineCacheFactory.getScanLineCache(e);
        this.spm = this.isl.getScansPerModulation();
        this.slc = this.isl.getScanLineCount();
        this.satOffset = e.getChild("scan_acquisition_time").getArray().
                getDouble(0);
        this.md = getModulationDuration();
        this.scanAcquisitionTimeVar = e.getChild(scan_acquisition_time_var);
        try {
            IVariableFragment msLevelVar = this.parent.getChild("ms_level");
            msLevel = msLevelVar.getArray();
            msScanMap = new TreeMap<>();
            for (int i = 0; i < msLevel.getShape()[0]; i++) {
                Short msLevelValue = msLevel.getShort(i);
                if (msScanMap.containsKey(msLevelValue)) {
                    List<Integer> scanToScan = msScanMap.get(msLevelValue);
                    scanToScan.add(i);
                } else {
                    List<Integer> scanToScan = new ArrayList<>();
                    scanToScan.add(scanToScan.size(), i);
                    msScanMap.put(msLevelValue, scanToScan);
                }
            }
        } catch (ResourceNotAvailableException rnae) {
            System.out.println("Chromatogram has no ms_level variable, assuming all scans are MS1!");
        }
    }

    /** {@inheritDoc} */
    @Override
    public IFileFragment getParent() {
        return this.parent;
    }

    /** {@inheritDoc} */
    @Override
    public int getNumberOf2DScans() {
        return this.isl.getScanLineCount() * this.isl.getScansPerModulation();
    }

     /** {@inheritDoc} */
    @Override
    public IScan2D getScan2D(final int firstColumnScanIndex,
            final int secondColumnScanIndex) {
        return buildScan((firstColumnScanIndex * isl.getScansPerModulation())
                + secondColumnScanIndex);
    }

    /**
     * Call for explicit access to the underlying IScanLine implementation.
     *
     * @return a {@link maltcms.datastructures.caches.IScanLine} object.
     */
    public IScanLine getScanLineImpl() {
        return this.isl;
    }

    /**
     * <p>buildScan.</p>
     *
     * @param i a int.
     * @return a {@link maltcms.datastructures.ms.IScan2D} object.
     */
    protected IScan2D buildScan(int i) {
        Point p = this.isl.mapIndex(i);
        final Tuple2D<Array, Array> t = this.isl.getSparseMassSpectrum(p.x, p.y);
        double sat1 = satOffset + (p.x * getModulationDuration());
        double sat = MaltcmsTools.getScanAcquisitionTime(this.parent, i);
        double sat2 = sat - sat1;
        short scanMsLevel = 1;
        if (msLevel != null) {
            scanMsLevel = msLevel.getByte(i);
        }
        final Scan2D s = new Scan2D(t.getFirst(), t.getSecond(), i, sat, p.x,
                p.y, sat1, sat2, scanMsLevel);
        return s;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
    }

    /** {@inheritDoc} */
    @Override
    public Tuple2D<Double, Double> getTimeRange() {
        if (timeRange == null) {
            MAMath.MinMax satMM = MAMath.getMinMax(scanAcquisitionTimeVar.getArray());
            timeRange = new Tuple2D<>(satMM.min, satMM.max);
        }
        return timeRange;
    }

    /** {@inheritDoc} */
    @Override
    public Tuple2D<Double, Double> getMassRange() {
        if (massRange == null) {
            massRange = MaltcmsTools.getMinMaxMassRange(parent);
        }
        return massRange;
    }

    /** {@inheritDoc} */
    @Override
    public List<Array> getIntensities() {
        return MaltcmsTools.getMZIs(this.parent).getSecond();
    }

    /** {@inheritDoc} */
    @Override
    public List<Array> getMasses() {
        return MaltcmsTools.getMZIs(this.parent).getFirst();
    }

    /** {@inheritDoc} */
    @Override
    public IScan2D getScan(final int scan) {
        return buildScan(scan);
    }

    /** {@inheritDoc} */
    @Override
    public String getScanAcquisitionTimeUnit() {
        return this.scanAcquisitionTimeUnit;
    }

    /**
     * <p>getScans.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<IScan2D> getScans() {
        ArrayList<IScan2D> al = new ArrayList<>();
        for (int i = 0; i < getNumberOfScans(); i++) {
            al.add(buildScan(i));
        }
        return al;
    }

    /**
     * {@inheritDoc}
     *
     * This iterator acts on the underlying collection of scans in
     * Chromatogram1D, so be careful with concurrent access / modification!
     */
    @Override
    public Iterator<IScan2D> iterator() {

        final Iterator<IScan2D> iter = new Iterator<IScan2D>() {
            private int currentPos = 0;

            @Override
            public boolean hasNext() {
                if (this.currentPos < getNumberOfScans() - 1) {
                    return true;
                }
                return false;
            }

            @Override
            public IScan2D next() {
                return getScan(this.currentPos++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "Can not remove scans with iterator!");
            }
        };
        return iter;
    }

    /**
     * <p>setExperiment.</p>
     *
     * @param e a {@link maltcms.datastructures.ms.IExperiment} object.
     */
    public void setExperiment(final IExperiment e) {
        if (e instanceof IExperiment2D) {
            this.parent = (IExperiment2D) e;
        }
        throw new IllegalArgumentException(
                "Parameter must be of type IExperiment2D!");
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.ms.IChromatogram#getScanAcquisitionTime()
     */
    /** {@inheritDoc} */
    @Override
    public Array getScanAcquisitionTime() {
        return this.parent.getChild("scan_acquisition_time").getArray();
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.ms.IChromatogram#getNumberOfScans()
     */
    /** {@inheritDoc} */
    @Override
    public int getNumberOfScans() {
        if (numberOfScans == -1) {
            numberOfScans = MaltcmsTools.getNumberOfScans(this.parent);
        }
        return numberOfScans;
    }

    /**
     * <p>getIntensities.</p>
     *
     * @param globalScan a int.
     * @param localScan a int.
     * @return a {@link ucar.ma2.Array} object.
     */
    public Array getIntensities(int globalScan, int localScan) {
        return getIntensities().get(this.isl.mapPoint(globalScan, localScan));
    }

    /**
     * <p>getMasses.</p>
     *
     * @param globalScan a int.
     * @param localScan a int.
     * @return a {@link ucar.ma2.Array} object.
     */
    public Array getMasses(int globalScan, int localScan) {
        return getMasses().get(this.isl.mapPoint(globalScan, localScan));
    }

    /** {@inheritDoc} */
    @Override
    public Point getPointFor(int scan) {
        return this.isl.mapIndex(scan);
    }

    /** {@inheritDoc} */
    @Override
    public Point getPointFor(double scan_acquisition_time) {
        return getPointFor(getIndexFor(scan_acquisition_time));
    }

    /** {@inheritDoc} */
    @Override
    public int getIndexFor(double scan_acquisition_time) {
        double[] d = (double[]) getScanAcquisitionTime().get1DJavaArray(
                double.class);
        int idx = Arrays.binarySearch(d, scan_acquisition_time);
        if (idx >= 0) {// exact hit
            log.info("sat {}, scan_index {}",
                    scan_acquisition_time, idx);
            return idx;
        } else {// imprecise hit, find closest element
            int insertionPosition = (-idx) - 1;
            if (insertionPosition < 0) {
                throw new ArrayIndexOutOfBoundsException("Insertion index is out of bounds! " + insertionPosition + "<" + 0);
            }
            if (insertionPosition >= d.length) {
                throw new ArrayIndexOutOfBoundsException("Insertion index is out of bounds! " + insertionPosition + ">=" + d.length);
            }
//			System.out.println("Would insert before "+insertionPosition);
            double current = d[Math.min(d.length - 1, insertionPosition)];
//			System.out.println("Value at insertion position: "+current);
            double previous = d[Math.max(0, insertionPosition - 1)];
//			System.out.println("Value before insertion position: "+previous);
            if (Math.abs(scan_acquisition_time - previous) <= Math.abs(
                    scan_acquisition_time - current)) {
                int index = Math.max(0, insertionPosition - 1);
//				System.out.println("Returning "+index);
                return index;
            } else {
//				System.out.println("Returning "+insertionPosition);
                return insertionPosition;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getSecondColumnScanAcquisitionTimeUnit() {
        return this.secondColumnScanAcquisitionTimeUnit;
    }

    /** {@inheritDoc} */
    @Override
    public int getNumberOfModulations() {
        return this.isl.getScanLineCount();
    }

    /** {@inheritDoc} */
    @Override
    public int getNumberOfScansPerModulation() {
        return this.isl.getScansPerModulation();
    }

    /** {@inheritDoc} */
    @Override
    public double getModulationDuration() {
        if (this.md == -1) {
            Array sat = this.parent.getChild("scan_acquisition_time").getArray();
            double t1 = sat.getDouble(getNumberOfScansPerModulation());
            this.md = t1 - this.satOffset;
        }
        return this.md;
    }

    /** {@inheritDoc} */
    @Override
    public int getNumberOfScansForMsLevel(short msLevelValue) {
        if (msLevelValue == (short) 1 && msScanMap == null) {
            return getNumberOfScans();
        }
        return msScanMap.get(msLevelValue).size();
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<IScan2D> subsetByMsLevel(final short msLevel) {
        Iterable<IScan2D> iterable = new Iterable<IScan2D>() {

            @Override
            public Iterator<IScan2D> iterator() {
                return new Scan2DIterator(msLevel);
            }
        };
        return iterable;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Short> getMsLevels() {
        if (msScanMap == null) {
            return Arrays.asList((short) 1);
        }
        List<Short> l = new ArrayList<>(msScanMap.keySet());
        Collections.sort(l);
        return l;
    }

    /** {@inheritDoc} */
    @Override
    public IScan2D getScanForMsLevel(int i, short level) {
        if (level == (short) 1 && msScanMap == null) {
            return getScan(i);
        }
        if (msScanMap == null) {
            throw new ResourceNotAvailableException("No ms fragmentation level available for chromatogram " + getParent().getName());
        }
        return getScan(msScanMap.get(level).get(i));
    }

    /** {@inheritDoc} */
    @Override
    public List<Integer> getIndicesOfScansForMsLevel(short level) {
        if (level == (short) 1 && msScanMap == null) {
            int scans = getNumberOfScansForMsLevel((short) 1);
            ArrayList<Integer> indices = new ArrayList<>(scans);
            for (int i = 0; i < scans; i++) {
                indices.add(i);
            }
            return indices;
        }
        if (msScanMap == null) {
            throw new ResourceNotAvailableException("No ms fragmentation level available for chromatogram " + getParent().getName());
        }
        return Collections.unmodifiableList(msScanMap.get(level));
    }

    private class Scan2DIterator implements Iterator<IScan2D> {

        private final int maxScans;
        private int scan = 0;
        private short msLevel = 1;

        public Scan2DIterator(short msLevel) {
            maxScans = getNumberOfScansForMsLevel(msLevel);
            this.msLevel = msLevel;
        }

        @Override
        public boolean hasNext() {
            return scan < maxScans - 1;
        }

        @Override
        public IScan2D next() {
            return getScanForMsLevel(scan++, msLevel);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
