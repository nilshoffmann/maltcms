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
package maltcms.datastructures.ms;

import cross.Factory;
import cross.annotations.Configurable;
import cross.datastructures.fragments.CachedList;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableVariableFragment2;
import cross.datastructures.fragments.VariableFragment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.tools.MaltcmsTools;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;

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
    @Configurable(name = "var.scan_acquisition_time")
    private String scan_acquisition_time_var = "scan_acquisition_time";
    private final List<Array> massValues;
    private final List<Array> intensityValues;

    public Chromatogram1D(final IFileFragment e) {
        this.parent = e;
        final String mz = Factory.getInstance().getConfiguration().getString(
                "var.mass_values", "mass_values");
        final String intens = Factory.getInstance().getConfiguration().getString(
                "var.intensity_values", "intensity_values");
        final String scan_index = Factory.getInstance().getConfiguration().
                getString("var.scan_index", "scan_index");
        final IVariableFragment index = e.getChild(scan_index);
        int scans = MaltcmsTools.getNumberOfScans(e);
        final IVariableFragment mzV = e.getChild(mz);
        mzV.setIndex(index);
        activateCache(mzV);
        massValues = mzV.getIndexedArray();
        setPrefetchSize(scans, massValues);
        final IVariableFragment iV = e.getChild(intens);
        iV.setIndex(index);
        activateCache(iV);
        intensityValues = iV.getIndexedArray();
        setPrefetchSize(scans, intensityValues);
    }

    protected void setPrefetchSize(int scans, List<Array> list) {
        if (list instanceof CachedList) {
            ((CachedList) list).setCacheSize(scans / 10);
            ((CachedList) list).setPrefetchOnMiss(true);
        }
    }

    protected void activateCache(IVariableFragment ivf) {
        if (ivf instanceof ImmutableVariableFragment2) {
            ((ImmutableVariableFragment2) ivf).setUseCachedList(true);
        }
        if (ivf instanceof VariableFragment) {
            ((VariableFragment) ivf).setUseCachedList(true);
        }
    }

    protected Scan1D buildScan(int i) {
        final Array masses = massValues.get(i);
        final Array intens = intensityValues.get(i);
        final Scan1D s = new Scan1D(masses, intens, i,
                MaltcmsTools.getScanAcquisitionTime(this.parent, i));
        return s;
    }

    @Override
    public void configure(final Configuration cfg) {
        this.scan_acquisition_time_var = cfg.getString(
                "var.scan_acquisition_time", "scan_acquisition_time");
    }

    @Override
    public List<Array> getIntensities() {
        return intensityValues;
    }

    @Override
    public List<Array> getMasses() {
        return massValues;
    }

    /**
     * @param scan scan index to load
     */
    @Override
    public Scan1D getScan(final int scan) {
        return buildScan(scan);
    }

    @Override
    public String getScanAcquisitionTimeUnit() {
        return this.scanAcquisitionTimeUnit;
    }

    public List<Scan1D> getScans() {
        ArrayList<Scan1D> al = new ArrayList<Scan1D>();
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
    public Iterator<IScan1D> iterator() {

        final Iterator<IScan1D> iter = new Iterator<IScan1D>() {
            private int currentPos = 0;

            @Override
            public boolean hasNext() {
                if (this.currentPos < getScans().size() - 1) {
                    return true;
                }
                return false;
            }

            @Override
            public IScan1D next() {
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
    public int getIndexFor(double scan_acquisition_time) throws ArrayIndexOutOfBoundsException{
        double[] d = (double[]) getScanAcquisitionTime().get1DJavaArray(
                double.class);
        int idx = Arrays.binarySearch(d, scan_acquisition_time);
		if (idx >= 0) {// exact hit
			log.info("sat {}, scan_index {}",
                    scan_acquisition_time, idx);
			return idx;
		} else {// imprecise hit, find closest element
			int insertionPosition = (-idx)-1;
			if(insertionPosition<0) {
				throw new ArrayIndexOutOfBoundsException("Insertion index is out of bounds! "+insertionPosition+"<"+0);
			}
			if(insertionPosition>=d.length) {
				throw new ArrayIndexOutOfBoundsException("Insertion index is out of bounds! "+insertionPosition+">="+d.length);
			}
//			System.out.println("Would insert before "+insertionPosition);
			double current = d[Math.min(d.length - 1, insertionPosition)];
//			System.out.println("Value at insertion position: "+current);
			double previous = d[Math.max(0, insertionPosition-1)];
//			System.out.println("Value before insertion position: "+previous);
			if (Math.abs(scan_acquisition_time - previous) <= Math.abs(
					scan_acquisition_time - current)) {
				int index = Math.max(0, insertionPosition-1);
//				System.out.println("Returning "+index);
				return index;
			} else {
//				System.out.println("Returning "+insertionPosition);
				return insertionPosition;
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
