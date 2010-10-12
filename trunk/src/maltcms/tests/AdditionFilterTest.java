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

import junit.framework.Assert;
import junit.framework.TestCase;
import maltcms.commands.filters.AElementFilter;
import maltcms.commands.filters.array.AdditionFilter;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;

/**
 * Test AdditionFilter.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class AdditionFilterTest extends TestCase {

	private Array a = null;

	private AdditionFilter af = null;

	public AdditionFilterTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.a = new ArrayDouble(new int[25]);
		final IndexIterator iter = this.a.getIndexIteratorFast();
		while (iter.hasNext()) {
			iter.setDoubleNext(Math.random());
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testFilter() {
		this.af = new AdditionFilter(1.0d);
		final Array b = this.af.apply(new Array[] { this.a.copy() })[0];
		final IndexIterator ia = this.a.getIndexIterator();
		final IndexIterator ib = b.getIndexIterator();
		while (ia.hasNext() && ib.hasNext()) {
			final double diff = Math.abs(ia.getDoubleNext()
			        - ib.getDoubleNext());
			Assert.assertEquals(1.0d, diff, 10e-8);
		}
	}

	public void testSetFilter() {
		this.af = new AdditionFilter(1.0d);
		this.af.setFilter(new AElementFilter() {

			public Double apply(final Double t) {
				return 2.0d;
			}

			@Override
			public void configure(final Configuration cfg) {

			}

		});
		final Array b = this.af.apply(new Array[] { this.a.copy() })[0];
		final IndexIterator ia = this.a.getIndexIterator();
		final IndexIterator ib = b.getIndexIterator();
		while (ia.hasNext() && ib.hasNext()) {
			final double diff = Math.abs(ia.getDoubleNext()
			        - ib.getDoubleNext());
			Assert.assertEquals(2.0d, diff, 10e-8);
		}
	}

}
