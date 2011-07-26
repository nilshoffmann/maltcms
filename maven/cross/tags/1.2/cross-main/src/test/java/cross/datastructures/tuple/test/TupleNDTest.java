/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
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
 * $Id: TupleNDTest.java 43 2009-10-16 17:22:55Z nilshoffmann $
 */

package cross.datastructures.tuple.test;

import java.util.Arrays;
import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.TestCase;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;

public class TupleNDTest extends TestCase {

	private TupleND<Integer> tnd;

	public TupleNDTest(final String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testAddPairs() {

	}

	public void testGetNumberOfPairs() {
		final Integer[] ints = new Integer[7];
		for (int i = 0; i < 7; i++) {
			ints[i] = i;
		}
		this.tnd = new TupleND<Integer>(ints);
		Assert.assertEquals(this.tnd.getNumberOfPairs(), 7 * 6 / 2);
	}

	public void testGetPairs() {
		final Integer[] ints = new Integer[7];
		for (int i = 0; i < 7; i++) {
			ints[i] = i;
		}
		this.tnd = new TupleND<Integer>(ints);
		final Iterator<Tuple2D<Integer, Integer>> iter = this.tnd.getPairs()
		        .iterator();
		while (iter.hasNext()) {
			final Tuple2D<Integer, Integer> t = iter.next();
			System.out.println("Pair: " + t.getFirst() + " " + t.getSecond());
		}

		Assert.assertEquals(1, 1);
	}

	public void testTupleNDCollectionOfT() {

		final Integer[] ints = new Integer[7];
		for (int i = 0; i < 7; i++) {
			ints[i] = i;
		}
		this.tnd = new TupleND<Integer>(Arrays.asList(ints));
		Assert.assertEquals(this.tnd.getSize(), 7);
	}

	public void testTupleNDTArray() {
		final Integer[] ints = new Integer[7];
		for (int i = 0; i < 7; i++) {
			ints[i] = i;
		}
		this.tnd = new TupleND<Integer>(ints);
		Assert.assertEquals(this.tnd.getSize(), 7);
	}

}
