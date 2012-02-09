/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package cross.datastructures.tuple.test;

import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import java.util.Arrays;
import java.util.Iterator;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class TupleNDTest {

        @Test
	public void testGetNumberOfPairs() {
		final Integer[] ints = new Integer[7];
		for (int i = 0; i < 7; i++) {
			ints[i] = i;
		}
		TupleND<Integer> tnd = new TupleND<Integer>(ints);
		Assert.assertEquals(tnd.getNumberOfPairs(), 7 * 6 / 2);
	}

        @Test
	public void testGetPairs() {
		final Integer[] ints = new Integer[7];
		for (int i = 0; i < 7; i++) {
			ints[i] = i;
		}
		TupleND<Integer> tnd = new TupleND<Integer>(ints);
		final Iterator<Tuple2D<Integer, Integer>> iter = tnd.getPairs()
		        .iterator();
		while (iter.hasNext()) {
			final Tuple2D<Integer, Integer> t = iter.next();
			log.info("Pair: {},{}",t.getFirst(),t.getSecond());
		}

		Assert.assertEquals(1, 1);
	}

        @Test
	public void testTupleNDCollectionOfT() {

		final Integer[] ints = new Integer[7];
		for (int i = 0; i < 7; i++) {
			ints[i] = i;
		}
		TupleND<Integer> tnd = new TupleND<Integer>(Arrays.asList(ints));
		Assert.assertEquals(tnd.getSize(), 7);
	}

        @Test
	public void testTupleNDTArray() {
		final Integer[] ints = new Integer[7];
		for (int i = 0; i < 7; i++) {
			ints[i] = i;
		}
		TupleND<Integer> tnd = new TupleND<Integer>(ints);
		Assert.assertEquals(tnd.getSize(), 7);
	}

}
