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
 * $Id: Chromatogram1D.java 115 2010-04-23 15:42:15Z nilshoffmann $
 */
package maltcms.datastructures.ms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.Logging;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import java.util.WeakHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Concrete Implementation of a 1-dimensional chromatogram.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
public class Chromatogram1D implements IChromatogram1D {

    private IFileFragment parent;
    private final String scanAcquisitionTimeUnit = "seconds";
    private final WeakHashMap<Integer, Scan1D> scanCache = new WeakHashMap<Integer, Scan1D>();
    @Configurable(name = "var.scan_acquisition_time")
    private String scan_acquisition_time_var = "scan_acquisition_time";

    public Chromatogram1D(final IFileFragment e) {
        this.parent = e;
    }

    protected Scan1D acquireFromCache(int i) {
        Scan1D scan = scanCache.get(Integer.valueOf(i));
        if (scan != null) {
            log.debug("Retrieved scan {} from cache",i);
            return scan;
        }
        final Tuple2D<Array, Array> t = MaltcmsTools.getMS(this.parent, i);
        final Scan1D s = new Scan1D(t.getFirst(), t.getSecond(), i,
                MaltcmsTools.getScanAcquisitionTime(this.parent, i));
        scanCache.put(Integer.valueOf(i),s);
        log.debug("Created scan {}",i);
        return s;

    }

    @Override
    public void configure(final Configuration cfg) {
        this.scan_acquisition_time_var = cfg.getString(
                "var.scan_acquisition_time", "scan_acquisition_time");
    }

    @Override
    public List<Array> getIntensities() {
        return MaltcmsTools.getMZIs(this.parent).getSecond();
    }

    @Override
    public List<Array> getMasses() {
        return MaltcmsTools.getMZIs(this.parent).getFirst();
    }

    /**
     * @param scan
     *            scan index to load
     */
    @Override
    public Scan1D getScan(final int scan) {
        return acquireFromCache(scan);
    }

    @Override
    public String getScanAcquisitionTimeUnit() {
        return this.scanAcquisitionTimeUnit;
    }

    public List<Scan1D> getScans() {
        ArrayList<Scan1D> al = new ArrayList<Scan1D>();
        for (int i = 0; i < getNumberOfScans(); i++) {
            al.add(acquireFromCache(i));
        }
        return al;
    }

    /**
     * This iterator acts on the underlying collection of scans in
     * Chromatogram1D, so be careful with concurrent access / modification!
     */
    @Override
    public Iterator<Scan1D> iterator() {

        final Iterator<Scan1D> iter = new Iterator<Scan1D>() {

            private int currentPos = 0;

            @Override
            public boolean hasNext() {
                if (this.currentPos < getScans().size() - 1) {
                    return true;
                }
                return false;
            }

            @Override
            public Scan1D next() {
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

    public void setExperiment(final IExperiment1D e) {
        this.parent = e;
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.datastructures.ms.IChromatogram#getScanAcquisitionTime()
     */
    @Override
    public Array getScanAcquisitionTime() {
        return this.parent.getChild(this.scan_acquisition_time_var).getArray();
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
            double current = d[Math.min(d.length - 1, (-idx) + 1)];
            double previous = d[Math.max(0, (-idx))];
            if (Math.abs(scan_acquisition_time - previous) < Math.abs(scan_acquisition_time - current)) {
                log.info("sat {}, scan_index {}",
                        scan_acquisition_time, (-idx) + 1);
                return (-idx) + 1;
            } else {
                log.info("sat {}, scan_index {}",
                        scan_acquisition_time, -idx);
                return (-idx);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.datastructures.ms.IChromatogram#getParent()
     */
    @Override
    public IFileFragment getParent() {
        return this.parent;
    }
}
