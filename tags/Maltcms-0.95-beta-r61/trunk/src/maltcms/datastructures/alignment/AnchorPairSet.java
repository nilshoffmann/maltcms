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

package maltcms.datastructures.alignment;

import java.util.List;

import maltcms.datastructures.ms.IAnchor;
import maltcms.datastructures.ms.RetentionInfo;

import org.slf4j.Logger;

import cross.Logging;
import cross.datastructures.tuple.Tuple2D;

/**
 * Represents a set of matched pairs of anchors.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class AnchorPairSet extends DefaultPairSet<IAnchor> {

	Logger log1 = Logging.getLogger(this.getClass());

	public AnchorPairSet(final List<IAnchor> a1, final List<IAnchor> a2,
	        final int rows, final int cols) {
		super(a1, a2);
		// System.out.println(a1.size()+" "+a2.size());
		// EvalTools.eqI(a1.size(),a2.size());
		// prepareWithSet(a1, a2, width, height, this.al);
		checkZeroAndEndPresent(rows, cols, this.al);
	}

	//
	// protected void prepareWithSet(ArrayList<IAnchor> a1, ArrayList<IAnchor>
	// a2,
	// int width, int height, ArrayList<Tuple2D<IAnchor, IAnchor>> al) {
	// HashMap<String, IAnchor> s1 = new HashMap<String, IAnchor>();
	// System.out.println("Number of anchors: "+al.size());
	// for (IAnchor a : a1) {
	// System.out.println("Adding lhs anchor with retention index
	// "+a.toString()+
	// " scan index: "+a.getScanIndex());
	// s1.put(a.toString(), a);
	// }
	// for (IAnchor b : a2) {
	// System.out.println("Adding rhs anchor with retention index
	// "+b.toString()+
	// " scan index: "+b.getScanIndex());
	// if (s1.containsKey(b.toString())) {
	// System.out.println("IAnchor matches for ("+s1.get(b.toString()).
	// getScanIndex()+"<->"+b.getScanIndex()+")");
	// al.add(new Tuple2D<IAnchor, IAnchor>(s1.get(b.toString()), b));
	// }
	// }
	// System.out.println("Number of anchors: "+al.size());
	// checkZeroAndEndPresent(width, height, al);
	// for (Tuple2D<IAnchor, IAnchor> t : al) {
	// IAnchor a = t.getFirst();
	// IAnchor b = t.getSecond();
	// // System.out.println(a.getName()+"
	// // "+a.getScanIndex()+"<->"+b.getName()+" "+b.getScanIndex());
	// this.log.info(a.getName() + " " + a.getScanIndex() + ":" + b.getName()
	// + " " + b.getScanIndex());
	// }
	// }
	//
	// // protected void prepare(ArrayList<IAnchor> a1, ArrayList<IAnchor> a2) {
	// // Collections.sort(a1);
	// // Collections.sort(a2);
	// // this.al = new ArrayList<Tuple2D<IAnchor, IAnchor>>();
	// // for (int i = 0; i < a1.size(); i++) {
	// // IAnchor ri1 = a1.remove(i);
	// // int res = Collections.binarySearch(a2, ri1);
	// // if (res >= 0) {
	// // this.log.debug("Found match at position " + res);
	// // this.al.add(new Tuple2D<IAnchor, IAnchor>(ri1, a2.remove(res)));
	// // } else {
	// // this.log.debug("Could not find a match!");
	// // }
	// // }
	// // this.log.debug("{}", this.al);
	// // }
	//
	// public Iterator<Tuple2D<IAnchor, IAnchor>> iterator() {
	// return this.al.iterator();
	// }
	//
	// public int getSize() {
	// return this.al.size();
	// }
	//
	// public List<Tuple2D<Integer, Integer>> getCorrespondingScans() {
	// ArrayList<Tuple2D<Integer, Integer>> al = new ArrayList<Tuple2D<Integer,
	// Integer>>(
	// getSize());
	// for (Tuple2D<IAnchor, IAnchor> t : this) {
	// al.add(new Tuple2D<Integer, Integer>(t.getFirst().getScanIndex(), t
	// .getSecond().getScanIndex()));
	// }
	// return al;
	// }

	/**
	 * Ensure, that (0,0) and (m-1,n-1) are included as virtual anchors.
	 */
	protected void checkZeroAndEndPresent(final int virtual_width,
	        final int virtual_height, final List<Tuple2D<IAnchor, IAnchor>> al1) {
		final IAnchor a1 = new RetentionInfo();
		a1.setName("START");
		a1.setScanIndex(0);
		final IAnchor b1 = new RetentionInfo();
		b1.setName("START");
		b1.setScanIndex(0);
		final IAnchor a2 = new RetentionInfo();
		a2.setName("END");
		a2.setScanIndex(virtual_width - 1);
		final IAnchor b2 = new RetentionInfo();
		b2.setName("END");
		b2.setScanIndex(virtual_height - 1);
		if (al1.isEmpty()) {
			this.log1.debug("Only adding start and end anchors!");
			// System.out.println("Only adding start and end anchors!");
			al1.add(0, new Tuple2D<IAnchor, IAnchor>(a1, b1));
			al1.add(new Tuple2D<IAnchor, IAnchor>(a2, b2));
			return;
		}
		// int k = 0;
		Tuple2D<IAnchor, IAnchor> t = al1.get(0);
		if ((t.getFirst().getScanIndex() > 0)
		        && (t.getSecond().getScanIndex() > 0)) {// if
			// there
			// is
			// no
			// tuple
			// 0,0
			this.log1.debug("Adding start anchor!");
			// System.out.println("Adding start anchor!");
			al1.add(0, new Tuple2D<IAnchor, IAnchor>(a1, b1));
		} else {
			Integer a = t.getFirst().getScanIndex();
			Integer b = t.getSecond().getScanIndex();
			if (a < 0) {
				a = 0;
			}
			if (b < 0) {
				b = 0;
			}
			this.log1.debug("Adding other anchor at " + a + "," + b);
			final IAnchor a3 = new RetentionInfo();
			a3.setName("ANCHOR");
			a3.setScanIndex(a);
			final IAnchor b3 = new RetentionInfo();
			b3.setName("ANCHOR");
			b3.setScanIndex(b);
			al1.set(0, new Tuple2D<IAnchor, IAnchor>(a3, b3));
		}

		t = al1.get(al1.size() - 1);
		if ((t.getFirst().getScanIndex() < virtual_width)
		        && (t.getSecond().getScanIndex() < virtual_height)) {
			this.log1.debug("Adding end anchor");
			// System.out.println("Adding end anchor!");
			al1.add(new Tuple2D<IAnchor, IAnchor>(a2, b2));
		} else {
			Integer a = t.getFirst().getScanIndex();
			Integer b = t.getSecond().getScanIndex();
			if (a > virtual_width) {
				a = virtual_width - 1;
			}
			if (b > virtual_height) {
				b = virtual_height - 1;
			}
			this.log1.debug("Adding other anchor at {},{}", a, b);
			final IAnchor a4 = new RetentionInfo();
			a4.setName("ANCHOR");
			a4.setScanIndex(a);
			final IAnchor b4 = new RetentionInfo();
			b4.setName("ANCHOR");
			b4.setScanIndex(b);
			al1.set(al1.size(), new Tuple2D<IAnchor, IAnchor>(a4, b4));
		}
	}

}
