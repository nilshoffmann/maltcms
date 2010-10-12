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
 * $Id$
 */

package maltcms.datastructures.ms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import cross.tools.ArrayTools;
import cross.tools.EvalTools;

/**
 * Concrete Implementation of a 1-dimensional chromatogram.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Chromatogram1D implements IChromatogram1D, Iterable<Scan1D> {

	private List<Scan1D> scans;

	private ArrayDouble.D1 tic;

	private List<Array> masses;

	private List<Array> intensities;

	private IExperiment1D parent;

	private Array scanAcquisitionTime;

	private final String scanAcquisitionTimeUnit = "seconds";

	public Chromatogram1D(final IExperiment1D e) {
		this.parent = e;
	}

	public Chromatogram1D(final IExperiment1D e, final List<Array> masses1,
	        final List<Array> intensities1, final Array scanAcquisitionTime1,
	        final String scanAcquisitionTimeUnit1) {
		EvalTools.notNull(new Object[] { e, masses1, intensities1,
		        scanAcquisitionTime1 }, this);
		this.parent = e;
		this.scanAcquisitionTime = scanAcquisitionTime1;
		this.intensities = intensities1;
		this.masses = masses1;
		setScans(buildScans(this.masses, this.intensities,
		        this.scanAcquisitionTime));
		this.tic = ArrayTools.integrate(this.intensities);
	}

	protected List<Scan1D> buildScans(final List<Array> masses1,
	        final List<Array> intensities1, final Array scanAcquisitionTime1) {
		EvalTools.eqI(masses1.size(), intensities1.size(), this);
		final ArrayList<Scan1D> al = new ArrayList<Scan1D>(masses1.size());
		final Index idx = scanAcquisitionTime1.getIndex();
		for (int i = 0; i < masses1.size(); i++) {
			idx.set(i);
			al.add(new Scan1D(masses1.get(i), intensities1.get(i), i,
			        scanAcquisitionTime1.getDouble(idx)));
		}
		return al;
	}

	@Override
	public void configure(final Configuration cfg) {

	}

	public IExperiment1D getExperiment() {
		return this.parent;
	}

	public int getIntegratedIntensity(final int scan) {
		final Index i = this.tic.getIndex();
		return this.tic.getInt(i.set(scan));
	}

	public List<Array> getIntensities() {
		return this.intensities;
	}

	public List<Array> getMasses() {
		return this.masses;
	}

	@Override
	public Scan1D getScan(final int scan) {
		return this.scans.get(scan);
	}

	@Override
	public String getScanAcquisitionTimeUnit() {
		return this.scanAcquisitionTimeUnit;
	}

	public List<Scan1D> getScans() {
		return this.scans;
	}

	/**
	 * This iterator acts on the underlying collection of scans in
	 * Chromatogram1D, so be careful with concurrent access / modification!
	 */
	@Override
	public Iterator<Scan1D> iterator() {

		final Iterator<Scan1D> iter = new Iterator<Scan1D>() {

			private int currentPos = -1;

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

	@Override
	public void setExperiment(final IExperiment e) {
		if (e instanceof IExperiment1D) {
			this.parent = (IExperiment1D) e;
		}
		throw new IllegalArgumentException(
		        "Parameter must be of type IExperiment1D!");
	}

	public void setExperiment(final IExperiment1D e) {
		this.parent = e;
	}

	private void setScans(final List<Scan1D> scans1) {
		this.scans = scans1;
	}

}
