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
package maltcms.datastructures.caches;

import java.awt.Point;
import java.util.List;

import ucar.ma2.Array;
import cross.IConfigurable;
import cross.datastructures.tuple.Tuple2D;

/**
 * Interface for all scan line data holder.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
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
	 * @param x
	 *            scanline number
	 * @param y
	 *            mass spectra inside this scanline
	 * @return array representing one mass spectra
	 */
	Array getMassSpectra(final int x, final int y);

	Tuple2D<Array, Array> getSparseMassSpectra(final int x, final int y);

	/**
	 * This Method is a wrapper for getMassPectra(int, int).
	 * 
	 * @param p
	 *            point of requested mass spectra
	 * @return mass spectra
	 */
	Array getMassSpectra(final Point p);

	Tuple2D<Array, Array> getSparseMassSpectra(final Point p);

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
	 * @param x
	 *            scan line number
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
	 * @param size
	 *            mass bin size
	 */
	void setBinSize(final int size);

	/**
	 * Setter. Default value is <code>true</code>.
	 * 
	 * @param cacheMod
	 *            <code>true</code> if all modulations should be cached
	 */
	void setCacheModulations(final boolean cacheMod);

	/**
	 * Setter.
	 * 
	 * @param index
	 *            last scan index
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
