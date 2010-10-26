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
 * $Id$
 */
package maltcms.experimental.operations;

import maltcms.commands.distances.ArrayCos;
import maltcms.commands.distances.ArrayDot;
import maltcms.commands.distances.IArrayDoubleComp;
import maltcms.datastructures.array.IFeatureVector;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.exception.NotImplementedException;

public class CosineTic extends TwoFeatureVectorOperation {

	private String varname1 = "FEATURE0";
	private String varname2 = "FEATURE1";

	private IArrayDoubleComp sf1 = new ArrayCos();
	private IArrayDoubleComp sf2 = new ArrayDot();

	public double apply(final Array a, final Array b, final Array c,
			final Array d) {
		final Double s1 = this.sf1.apply(0, 0, 0, 0, a, b);
		final Double s2 = this.sf2.apply(0, 0, 0, 0, c, d);
		final Double s = s1 * s2;
		System.out.println(s1 + "-" + s2 + "-" + s);
		return s;
	}

	@Override
	public boolean isMinimize() {
		if (this.sf1.minimize() != this.sf2.minimize()
				|| this.sf1.minimize() == true) {
			throw new NotImplementedException(
					"FU - Incompatibel score functions");
		}
		return false;
	}

	@Override
	public double apply(final IFeatureVector f1, final IFeatureVector f2) {
		final Array a = f1.getFeature(this.varname1);
		final Array b = f2.getFeature(this.varname1);
		final Array c = f1.getFeature(this.varname2);
		final Array d = f2.getFeature(this.varname2);
		return apply(a, b, c, d);
	}

	@Override
	public void configure(Configuration cfg) {
		super.configure(cfg);
	}

	public void setFeatureVarName1(String varname) {
		this.varname1 = varname;
	}

	public void setFeatureVarName2(String varname) {
		this.varname2 = varname;
	}

}
