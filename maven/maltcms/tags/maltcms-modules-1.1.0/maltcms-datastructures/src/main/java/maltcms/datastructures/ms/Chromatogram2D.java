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
package maltcms.datastructures.ms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;

/**
 * Concrete Implementation of a 1-dimensional chromatogram.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Chromatogram2D implements IChromatogram2D {

    private IFileFragment parent;
    private IScanLine isl;
    private final String scanAcquisitionTimeUnit = "seconds";
    private final String secondColumnScanAcquisitionTimeUnit = "seconds";
    private double md = -1;
    private final int spm;
    private final int slc;
    private double satOffset = 0;

    public Chromatogram2D(final IFileFragment e) {
        this.parent = e;
        this.isl = ScanLineCacheFactory.getScanLineCache(e);
        this.spm = this.isl.getScansPerModulation();
        this.slc = this.isl.getScanLineCount();
        this.satOffset = e.getChild("scan_acquisition_time").getArray().
                getDouble(0);
        this.md = getModulationDuration();
    }

    @Override
    public IFileFragment getParent() {
        return this.parent;
    }

    @Override
    public int getNumberOf2DScans() {
        return this.isl.getScanLineCount() * this.isl.getScansPerModulation();
    }

    @Override
    /**
     *  @param firstColumnScanIndex
     *  @param secondColumnScanIndex
     */
    public IScan2D getScan2D(final int firstColumnScanIndex,
            final int secondColumnScanIndex) {
        return buildScan((firstColumnScanIndex * isl.getScansPerModulation())
                + secondColumnScanIndex);
    }

    /**
     * Call for explicit access to the underlying IScanLine implementation.
     *
     * @return
     */
    public IScanLine getScanLineImpl() {
        return this.isl;
    }

    protected IScan2D buildScan(int i) {
        Point p = this.isl.mapIndex(i);
        final Tuple2D<Array, Array> t = this.isl.getSparseMassSpectra(p.x, p.y);
        double sat1 = satOffset + (p.x * getModulationDuration());
        double sat = MaltcmsTools.getScanAcquisitionTime(this.parent, i);
        double sat2 = sat - sat1;
        final Scan2D s = new Scan2D(t.getFirst(), t.getSecond(), i, sat, p.x,
                p.y, sat1, sat2);
        return s;
    }

    @Override
    public void configure(final Configuration cfg) {
    }

    public List<Array> getIntensities() {
        return MaltcmsTools.getMZIs(this.parent).getSecond();
    }

    public List<Array> getMasses() {
        return MaltcmsTools.getMZIs(this.parent).getFirst();
    }

    /**
     * @param scan
     *            scan index to load
     */
    @Override
    public IScan2D getScan(final int scan) {
        return buildScan(scan);
    }

    @Override
    public String getScanAcquisitionTimeUnit() {
        return this.scanAcquisitionTimeUnit;
    }

    public List<IScan2D> getScans() {
        ArrayList<IScan2D> al = new ArrayList<IScan2D>();
        for (int i = 0; i < getNumberOfScans(); i++) {
            al.add(buildScan(i));
        }
        return al;
    }

    /**
     * This iterator acts on the underlying collection of scans in
     * Chromatogram1D, so be careful with concurrent access / modification!
     */
    @Override
    public Iterator<IScan2D> iterator() {

        final Iterator<IScan2D> iter = new Iterator<IScan2D>() {

            private int currentPos = 0;

            @Override
            public boolean hasNext() {
                if (this.currentPos < getScans().size() - 1) {
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
    @Override
    public Array getScanAcquisitionTime() {
        return this.parent.getChild("scan_acquisition_time").getArray();
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.ms.IChromatogram#getNumberOfScans()
     */
    @Override
    public int getNumberOfScans() {
        return MaltcmsTools.getNumberOfScans(this.parent);
    }

    public Array getIntensities(int globalScan, int localScan) {
        return getIntensities().get(this.isl.mapPoint(globalScan, localScan));
    }

    public Array getMasses(int globalScan, int localScan) {
        return getMasses().get(this.isl.mapPoint(globalScan, localScan));
    }

    @Override
    public Point getPointFor(int scan) {
        return this.isl.mapIndex(scan);
    }

    @Override
    public Point getPointFor(double scan_acquisition_time) {
        return getPointFor(getIndexFor(scan_acquisition_time));
    }

    @Override
    public int getIndexFor(double scan_acquisition_time) {
        double[] d = (double[]) getScanAcquisitionTime().get1DJavaArray(
                double.class);
        int idx = Arrays.binarySearch(d, scan_acquisition_time);
        if (idx >= 0) {// exact hit
            return idx;
        } else {// imprecise hit, find closest element
            double current = d[Math.max(d.length - 1, (-idx) + 1)];
            double next = d[Math.max(d.length - 1, (-idx) + 2)];
            if (Math.abs(scan_acquisition_time - current) < Math.abs(
                    scan_acquisition_time - next)) {
                return (-idx) + 1;
            } else {
                return (-idx) + 2;
            }
        }
    }

    @Override
    public String getSecondColumnScanAcquisitionTimeUnit() {
        return this.secondColumnScanAcquisitionTimeUnit;
    }

    @Override
    public int getNumberOfModulations() {
        return this.isl.getScanLineCount();
    }

    @Override
    public int getNumberOfScansPerModulation() {
        return this.isl.getScansPerModulation();
    }

    @Override
    public double getModulationDuration() {
        if (this.md == -1) {
            Array sat = this.parent.getChild("scan_acquisition_time").getArray();
            double t1 = sat.getDouble(getNumberOfScansPerModulation());
            this.md = t1 - this.satOffset;
        }
        return this.md;
    }
}
