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

import cross.IConfigurable;
import cross.datastructures.tuple.Tuple2D;
import java.awt.Point;
import java.util.List;
import ucar.ma2.Array;

/**
 * Interface for all scan line data holder.
 *
 * @author Mathias Wilhelm
 */
public interface IScanLine extends IConfigurable {

    /**
     * Getter.
     *
     * @return mass bin size
     */
    int getBinsSize();

    /**
     * Getter.
     *
     * @return <code>false</code> if no modulation will be cached
     */
    boolean getCacheModulation();

    /**
     * Getter.
     *
     * @return last scan index
     */
    int getLastIndex();

    /**
     * Getter for one mass spectra. If the requested mass spectra is out of
     * bound, then this method will return null.
     *
     * @param x scanline number
     * @param y offset inside this scanline
     * @return array representing one mass spectrum
     */
    Array getMassSpectrum(final int x, final int y);

    Tuple2D<Array, Array> getSparseMassSpectrum(final int x, final int y);

    /**
     * This Method is a wrapper for getMassPectra(int, int).
     *
     * @param p point of requested mass spectra
     * @return mass spectrum
     */
    Array getMassSpectrum(final Point p);

    Tuple2D<Array, Array> getSparseMassSpectrum(final Point p);

    /**
     * Getter.
     *
     * @return scan line count
     */
    int getScanLineCount();

    /**
     * This method will create a {@link List} of {@link Array} containing the
     * mass spectra of scanline x. All this mass spectra are normalized to one
     * size.
     *
     * @param x scan line number
     * @return list of mass spectra
     */
    List<Array> getScanlineMS(final int x);

    List<Tuple2D<Array, Array>> getScanlineSparseMS(final int x);

    /**
     * Getter.
     *
     * @return scans per modulation
     */
    int getScansPerModulation();

    /**
     * Setter.
     *
     * @param size mass bin size
     */
    @Deprecated
    void setBinSize(final int size);

//	/**
//	 * Set the mass resolution used for retrieving binned arrays.
//	 *
//	 * @param massResolution
//	 */
//	void setMassResolution(final double massResolution);
//
//	/**
//	 * Returns the mass resolution.
//	 *
//	 * @return the mass resolution
//	 */
//	double getMassResolution();
    /**
     * Setter. Default value is
     * <code>true</code>.
     *
     * @param cacheMod <code>true</code> if all modulations should be cached
     */
    void setCacheModulations(final boolean cacheMod);

    /**
     * Setter.
     *
     * @param index last scan index
     */
    void setLastIndex(final int index);

    /**
     * Will show some information about the usage.
     */
    void showStat();

    /**
     * Map a global scan index to a Point of x,y coordinates.
     *
     * @param scanIndex
     * @return
     */
    Point mapIndex(int scanIndex);

    /**
     * Map a Point to a global scan index.
     *
     * @param p
     * @return
     */
    int mapPoint(Point p);

    /**
     * Map two coordinates x,y to a global scan index.
     *
     * @param x
     * @param y
     * @return
     */
    int mapPoint(int x, int y);

    public void clear();
}
