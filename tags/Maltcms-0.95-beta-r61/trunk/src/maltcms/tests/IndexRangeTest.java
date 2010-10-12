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

import java.util.ArrayList;
import java.util.LinkedList;

import junit.framework.TestCase;
import maltcms.datastructures.constraint.IndexConstraintSet;
import maltcms.datastructures.constraint.RetentionIndexConstraint;

/**
 * Test IndexRange.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class IndexRangeTest extends TestCase {

	private ArrayList<Integer> risx = null;

	private ArrayList<Integer> risy = null;

	public IndexRangeTest(final String arg0) {
		super(arg0);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.risx = new ArrayList<Integer>();
		this.risx.add(10);
		this.risx.add(22);
		this.risx.add(30);
		this.risx.add(50);
		this.risy = new ArrayList<Integer>();
		this.risy.add(12);
		this.risy.add(20);
		this.risy.add(29);
		this.risy.add(46);
	}

	// public void testAllowed() {
	// fail("Not yet implemented");
	// }
	//
	// public void testGetLowerBound() {
	// fail("Not yet implemented");
	// }
	//
	// public void testGetUpperBound() {
	// fail("Not yet implemented");
	// }
	//
	// public void testGetAxis() {
	// fail("Not yet implemented");
	// }

	public void testNext() {
		final LinkedList<ArrayList<Integer>> ll = new LinkedList<ArrayList<Integer>>();
		ll.add(this.risx);
		ll.add(this.risy);
		final RetentionIndexConstraint ric = new RetentionIndexConstraint(
		        new Integer[] { 55, 50 }, ll);
		final IndexConstraintSet icsx = new IndexConstraintSet(new Integer[] {
		        55, 50 }, true, ric);
		Integer ilb = icsx.getLowerBound(0);
		Integer iub = icsx.getUpperBound(0);
		Integer jlb = icsx.getLowerBound(1);
		Integer jub = icsx.getUpperBound(1);
		for (; ilb < iub; ilb++) {
			for (; jlb < jub; jlb++) {
				// System.out.println(i+" "+j);
				if (icsx.next(ilb, jlb)) {
					ilb = icsx.getLowerBound(0);
					iub = icsx.getUpperBound(0);
					jlb = icsx.getLowerBound(1);
					jub = icsx.getUpperBound(1);
				}
			}
		}
	}

}
