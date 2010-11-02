/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
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
 * $Id: AScanLineCache.java 135 2010-07-05 08:04:51Z nilshoffmann $
 */
package maltcms.datastructures.caches;

import java.awt.Point;
import java.lang.ref.SoftReference;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import cross.annotations.RequiresVariables;
import cross.datastructures.fragments.IFileFragment;

/**
 * This class is a dataholder for mass spectra. This class will cache all mass
 * spectra which were read an hold a {@link SoftReference} to it.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@RequiresVariables(names = { "var.mass_values", "var.intensity_values",
        "var.scan_index", "var.mass_range_min", "var.mass_range_max",
        "var.modulation_time", "var.scan_rate" })
public abstract class AScanLineCache implements IScanLine {

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
	 * @param iff1
	 *            file fragment
	 */
	protected AScanLineCache(final IFileFragment iff1) {
		System.out.println("Initing AScanLineCache!");
		this.iff = iff1;
		final int modulation = this.iff.getChild(this.modulationVar).getArray()
		        .getInt(Index.scalarIndexImmutable);
		final int scanRate = this.iff.getChild(this.scanRateVar).getArray()
		        .getInt(Index.scalarIndexImmutable);
		this.scansPerModulation = modulation * scanRate;
		System.out.println("Estimating bin size!");
		estimateBinSize();
		System.out.println("Estimating last index!");
		estimateLastIndex();
	}

	/**
	 * Setter.
	 * 
	 * @param size
	 *            bin size
	 */
	public void setBinSize(final int size) {
		this.binSize = size;
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
	 * Getter.
	 * 
	 * @return last index
	 */
	public int getLastIndex() {
		return this.lastIndex;
	}

	/**
	 * Setter.
	 * 
	 * @param index
	 *            last index
	 */
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

	/**
	 * {@inheritDoc}
	 */
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
	 * Will view some statistical information about the cache usage.
	 */
	public void showStat() {
	}

	/**
	 * Getter.
	 * 
	 * @return scan line count
	 */
	public int getScanLineCount() {
		return (int) (this.lastIndex / this.scansPerModulation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Array getMassSpectra(final Point p) {
		return getMassSpectra(p.x, p.y);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCacheModulations(final boolean cacheMod) {
		this.cacheModulations = cacheMod;
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
	 * {@inheritDoc}
	 */
	@Override
	public int getScansPerModulation() {
		return this.scansPerModulation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {

	}

}
