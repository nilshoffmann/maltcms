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

package maltcms.tests;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.exception.NotImplementedException;

/**
 * Test MultiplicationFilter.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
// public class MultiplicationFilterTest extends TestCase {
public class MultiplicationFilterTest {

	public static double addNormalNoise(final double x, final double mean,
	        final double stddev) {
		return x
		        + (1.0 / (stddev + Math.sqrt(2.0 * Math.PI)) * Math.exp(-Math
		                .pow((Math.random() - mean), 2.0d)
		                / 2.0d * Math.pow(stddev, 2.0d)));
	}

	public static void main(final String[] args) {
		final MultiplicationFilterTest mft = new MultiplicationFilterTest(
		        "MultipTest");
		mft.testFilter();
	}

	private IVariableFragment a = null;

	private IVariableFragment b = null;

	private IFileFragment fa = null, fb = null;

	public MultiplicationFilterTest(final String name) {
		// super(name);
		final Array a1 = new ArrayDouble.D1(632);
		final Array b1 = new ArrayDouble.D1(643);
		this.fa = FileFragmentFactory.getInstance()
		        .getFragment("random1", null);
		this.fb = FileFragmentFactory.getInstance()
		        .getFragment("random2", null);
		this.a = new VariableFragment(this.fa, "rnd_intensity");
		this.a.setArray(a1);
		this.b = new VariableFragment(this.fb, "rnd_intensity");
		this.b.setArray(b1);
		IndexIterator iter = a1.getIndexIterator();
		while (iter.hasNext()) {
			final double d = Math.random();
			iter.setDoubleNext(d
			        + MultiplicationFilterTest.addNormalNoise(d, 0.0d, 1.0d));
		}
		iter = b1.getIndexIterator();
		while (iter.hasNext()) {
			final double d = Math.random();
			iter.setDoubleNext(d
			        + MultiplicationFilterTest.addNormalNoise(d, 0.0d, 1.0d));
		}
	}

	public void testFilter() {
		throw new NotImplementedException();
	}

	public void testSetFilter() {
		throw new NotImplementedException();
	}

}
