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
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;

/**
 * Specialization of Scan1D, adding second column times and indices, indicating
 * the modulation periods scans belong to.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Scan2D extends Scan1D {

	private ArrayDouble.D1 sctimes;

	private ArrayInt.D1 scind;

	public Scan2D(final Array masses1, final Array intensities1,
	        final int scanNumber1, final double scanAcquisitionTime1) {
		super(masses1, intensities1, scanNumber1, scanAcquisitionTime1);
	}

	public ArrayInt.D1 getSecondColumnIndices() {
		return this.scind;
	}

	public ArrayDouble.D1 getSecondColumnTimes() {
		return this.sctimes;
	}

	public void setSecondColumnIndices(final ArrayInt.D1 a) {
		this.scind = a;
	}

	public void setSecondColumnTimes(final ArrayDouble.D1 a) {
		this.sctimes = a;
	}

}
