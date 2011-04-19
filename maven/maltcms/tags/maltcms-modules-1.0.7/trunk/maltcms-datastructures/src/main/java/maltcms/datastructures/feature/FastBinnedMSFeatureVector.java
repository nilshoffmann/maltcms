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
 * $Id: FastBinnedMSFeatureVector.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */
package maltcms.datastructures.feature;

import java.util.Arrays;
import java.util.List;

import maltcms.datastructures.array.IFeatureVector;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;

/**
 * This feature vector keeps direct copies of mass and intensity values, as
 * opposed to DefaultBinnedMSFeatureVector, which requests them possibly from a
 * CachedList backed implementation.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public class FastBinnedMSFeatureVector implements IFeatureVector {

	/**
     * 
     */
	private static final long serialVersionUID = 2026476569387259118L;

	private final Array binnedMasses;

	private final Array binnedIntens;

	private final ArrayDouble.D0 sat;

	private final ArrayDouble.D0 tic;

	public FastBinnedMSFeatureVector(IFileFragment iff, int i) {// ArrayDouble.D0
		// tic,
		// ArrayDouble.D0
		// scanAcquisition,
		// Array
		// binnedMasses,
		// Array
		// binnedIntens)
		// {
		Tuple2D<Array, Array> t = MaltcmsTools.getBinnedMS(iff, i);
		ArrayDouble.D0 sata = new ArrayDouble.D0();
		sata.set(MaltcmsTools.getScanAcquisitionTime(iff, i));
		ArrayDouble.D0 tica = new ArrayDouble.D0();
		tica.set(MaltcmsTools.getTIC(iff, i));
		this.tic = tica;
		this.sat = sata;
		this.binnedMasses = t.getFirst();
		this.binnedIntens = t.getSecond();
	}

	@Override
	public Array getFeature(String string) {
		if (string.equals("binned_mass_values")) {
			return this.binnedMasses;
		} else if (string.equals("binned_intensity_values")) {
			return this.binnedIntens;
		} else if (string.equals("total_intensity")) {
			return this.tic;
		} else if (string.equals("scan_acquisition_time")) {
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
