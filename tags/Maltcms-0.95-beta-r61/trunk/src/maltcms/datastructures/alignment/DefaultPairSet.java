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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import maltcms.datastructures.ms.IAnchor;

import org.slf4j.Logger;

import cross.Logging;
import cross.datastructures.tuple.Tuple2D;

/**
 * Implementation of a pairset for anything implementing
 * {@link maltcms.datastructures.alignment.DefaultPairSet}.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 * @param <T>
 */
public class DefaultPairSet<T extends IAnchor> implements
        Iterable<Tuple2D<T, T>> {
	Logger log = Logging.getLogger(this.getClass());

	List<Tuple2D<T, T>> al = new ArrayList<Tuple2D<T, T>>();

	public DefaultPairSet(final List<T> a1, final List<T> a2) {
		// EvalTools.eqI(a1.size(),a2.size());
		prepareWithSet(a1, a2, this.al);
	}

	public List<Tuple2D<Integer, Integer>> getCorrespondingScans() {
		final ArrayList<Tuple2D<Integer, Integer>> al1 = new ArrayList<Tuple2D<Integer, Integer>>(
		        getSize());
		for (final Tuple2D<T, T> t : this) {
			al1.add(new Tuple2D<Integer, Integer>(t.getFirst().getScanIndex(),
			        t.getSecond().getScanIndex()));
		}
		return al1;
	}

	public int getSize() {
		return this.al.size();
	}

	public Iterator<Tuple2D<T, T>> iterator() {
		return this.al.iterator();
	}

	protected void prepareWithSet(final List<T> a1, final List<T> a2,
	        final List<Tuple2D<T, T>> al1) {
		final HashMap<String, T> s1 = new HashMap<String, T>();
		this.log.debug("Number of anchors: " + al1.size());
		for (final T a : a1) {
			this.log.debug("Adding lhs anchor with retention index "
			        + a.toString() + " scan index: " + a.getScanIndex());
			s1.put(a.toString(), a);
		}
		for (final T b : a2) {
			this.log.debug("Adding rhs anchor with retention index "
			        + b.toString() + " scan index: " + b.getScanIndex());
			if (s1.containsKey(b.toString())) {
				this.log.debug("IAnchor matches for ("
				        + s1.get(b.toString()).getScanIndex() + "<->"
				        + b.getScanIndex() + ")");
				al1.add(new Tuple2D<T, T>(s1.get(b.toString()), b));
			}
		}
		int prevx = -1;
		int prevy = -1;
		ArrayList<Integer> conflicts = new ArrayList<Integer>();
		for(int i = 0;i<al1.size();i++) {
			Tuple2D<T,T> tpl = al1.get(i);
			//if current indices are lower, we must remove the last anchor
			if(i>0 && (tpl.getFirst().getScanIndex()<=prevx || tpl.getSecond().getScanIndex()<=prevy)) {
				conflicts.add(Integer.valueOf(i-1));
				conflicts.add(Integer.valueOf(i));
				this.log.info("Removing conflicting anchors at {},{} and {},{}",new Object[]{al1.get(i-1).getFirst().getScanIndex(),al1.get(i-1).getSecond().getScanIndex(),al1.get(i).getFirst(),al1.get(i).getSecond()});
			}else{
				prevx = tpl.getFirst().getScanIndex();
				prevy = tpl.getSecond().getScanIndex();
			}
		}
		int cnt = 0;
		for(Integer itg:conflicts) {
			al1.remove(itg.intValue()-cnt);
			cnt++;
		}
		this.log.info("Retaining {} anchors." + al1.size());
		for (final Tuple2D<T, T> t : al1) {
			final T a = t.getFirst();
			final T b = t.getSecond();
			// System.out.println(a.getName()+"
			// "+a.getScanIndex()+"<->"+b.getName()+" "+b.getScanIndex());
			this.log.debug(a.getName() + " " + a.getScanIndex() + ":"
			        + b.getName() + " " + b.getScanIndex());
		}
	}

}
