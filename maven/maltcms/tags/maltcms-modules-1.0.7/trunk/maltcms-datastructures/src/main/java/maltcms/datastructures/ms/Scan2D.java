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
 * $Id: Scan2D.java 159 2010-08-31 18:44:07Z nilshoffmann $
 */

package maltcms.datastructures.ms;

import java.util.Arrays;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;

/**
 * Specialization of Scan1D, adding second column times and indices, indicating
 * the modulation periods scans belong to.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 *         FIXME add support for IFeatureVector, extend Scan1D functionality
 * 
 */
public class Scan2D extends Scan1D {

	/**
     * 
     */
	private static final long serialVersionUID = -5785157824205744680L;

	private double fctime = -1, sctime = -1;

	private int fcind = -1, scind = -1;

	public Scan2D(Array masses1, Array intensities1, int scanNumber1,
	        double scanAcquisitionTime1) {
		super(masses1, intensities1, scanNumber1, scanAcquisitionTime1);
	}

        public Scan2D(Array masses1, Array intensities1, int scanNumber1,
	        double scanAcquisitionTime1, int idx1, int idx2, double rt1, double rt2) {
		this(masses1, intensities1, scanNumber1, scanAcquisitionTime1);
                this.fcind = idx1;
                this.scind = idx2;
                this.fctime = rt1;
                this.sctime = rt2;
        }

	public int getSecondColumnScanIndex() {
		return this.scind;
	}

	public double getSecondColumnScanAcquisitionTime() {
		return this.sctime;
	}

	public void setSecondColumnScanIndex(final int a) {
		this.scind = a;
	}

	public void setSecondColumnScanAcquisitionTime(final double sat) {
		this.sctime = sat;
	}

	public int getFirstColumnScanIndex() {
		return this.fcind;
	}

	public double getFirstColumnScanAcquisitionTime() {
		return this.fctime;
	}

	public void setFirstColumnScanIndex(final int a) {
		this.fcind = a;
	}

	public void setFirstColumnScanAcquisitionTime(final double sat) {
		this.fctime = sat;
	}

	@Override
	public Array getFeature(final String name) {
		if (name.equals("first_column_scan_index")) {
			ArrayInt.D0 a = new ArrayInt.D0();
			a.set(this.fcind);
			return a;
		} else if (name.equals("second_column_scan_index")) {
			ArrayInt.D0 a = new ArrayInt.D0();
			a.set(this.scind);
			return a;
		} else if (name.equals("first_column_time")) {
			ArrayDouble.D0 a = new ArrayDouble.D0();
			a.set(this.fctime);
			return a;
		} else if (name.equals("second_column_time")) {
			ArrayDouble.D0 a = new ArrayDouble.D0();
			a.set(this.sctime);
			return a;
		} else {
			return super.getFeature(name);
		}
	}

	@Override
	public List<String> getFeatureNames() {
		return Arrays.asList(new String[] { "mass_values", "intensity_values",
		        "scan_index", "scan_acquisition_time", "total_intensity",
		        "first_column_scan_index", "second_column_scan_index",
		        "first_column_time", "second_column_time" });
	}

}
