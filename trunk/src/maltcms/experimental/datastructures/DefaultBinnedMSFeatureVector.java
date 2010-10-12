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
package maltcms.experimental.datastructures;

import java.util.Arrays;
import java.util.List;

import maltcms.datastructures.array.IFeatureVector;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;

/**
 * This feature vector retrieves mass and intensity values for this ms from
 * directly referenced (possibly cached) lists.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public class DefaultBinnedMSFeatureVector implements IFeatureVector {

	/**
     * 
     */
	private static final long serialVersionUID = 4737942765869140810L;
	private final List<Array> binned_mass_values;
	private final List<Array> binned_intensity_values;
	private ArrayDouble.D0 sat = null;
	private ArrayDouble.D0 tic = null;
	private final int i;
	private final IFileFragment iff;

	public DefaultBinnedMSFeatureVector(IFileFragment iff, int i,
	        Tuple2D<List<Array>, List<Array>> t) {
		this.iff = iff;
		this.binned_mass_values = t.getFirst();
		this.binned_intensity_values = t.getSecond();
		// this.sat.set(MaltcmsTools.getScanAcquisitionTime(iff, i));
		// this.tic.set(MaltcmsTools.getTIC(iff, i));
		this.i = i;
	}

	@Override
	public Array getFeature(String string) {
		if (string.equals("binned_mass_values")) {
			return this.binned_mass_values.get(i);
		} else if (string.equals("binned_intensity_values")) {
			return this.binned_intensity_values.get(i);
		} else if (string.equals("total_intensity")) {
			if (this.tic == null) {
				this.tic = new ArrayDouble.D0();
				this.tic.set(MaltcmsTools.getTIC(this.iff, this.i));
			}
			return this.tic;
		} else if (string.equals("scan_acquisition_time")) {
			if (this.sat == null) {
				this.sat = new ArrayDouble.D0();
				this.sat.set(MaltcmsTools.getScanAcquisitionTime(this.iff,
				        this.i));
			}
			return this.sat;
		}
		throw new IllegalArgumentException("Unknown feature name: " + string);
	}

	@Override
	public List<String> getFeatureNames() {
		return Arrays.asList("binned_mass_values", "binned_intensity_values",
		        "total_intensity", "scan_acquisition_time");
	}

}
