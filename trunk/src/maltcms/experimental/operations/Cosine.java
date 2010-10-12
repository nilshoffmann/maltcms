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
package maltcms.experimental.operations;

import maltcms.datastructures.array.IFeatureVector;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.MAVector;
import cross.annotations.RequiresVariables;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
@RequiresVariables(names = { "var.binned_intensity_values" })
public class Cosine extends TwoFeatureVectorOperation {

	private String varname = "binned_intensity_values";

	public double apply(Array a, Array b) {
		final MAVector ma1 = new MAVector(a);
		final MAVector ma2 = new MAVector(b);
		return ma1.cos(ma2);
	}

	@Override
	public boolean isMinimize() {
		return false;
	}

	@Override
	public double apply(IFeatureVector f1, IFeatureVector f2) {
		Array a = f1.getFeature(varname);
		Array b = f2.getFeature(varname);
		return apply(a, b);
	}

	@Override
	public void configure(Configuration cfg) {
		super.configure(cfg);
		this.varname = cfg.getString("var.binned_intensity_values",
				"binned_intensity_values");
	}

	public void setFeatureVarName(String varname) {
		this.varname = varname;
	}

}
