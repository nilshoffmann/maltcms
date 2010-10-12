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

import ucar.ma2.Array;

/**
 * Implementation of a 1-dimensional scan.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Scan1D implements IScan {

	private int scanNumber = -1;
	private double scanAcquisitionTime = -1.0d;
	private Array masses = null;
	private Array intensities = null;

	public Scan1D() {
	}

	public Scan1D(final Array masses1, final Array intensities1,
	        final int scanNumber1, final double scanAcquisitionTime1) {
		this.masses = masses1;
		this.intensities = intensities1;
		this.scanNumber = scanNumber1;
		this.scanAcquisitionTime = scanAcquisitionTime1;
	}

	@Override
	public Array getIntensities() {
		return this.intensities;
	}

	@Override
	public Array getMasses() {
		return this.masses;
	}

	@Override
	public double getScanAcquisitionTime() {
		return this.scanAcquisitionTime;
	}

	@Override
	public int getScanNumber() {
		return this.scanNumber;
	}

}
