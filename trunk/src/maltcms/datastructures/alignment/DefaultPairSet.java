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

import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
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

	@Configurable
	private int minScansBetweenAnchors = 1;

	public int getMinScansBetweenAnchors() {
		return minScansBetweenAnchors;
	}

	public void setMinScansBetweenAnchors(int minScansBetweenAnchors) {
		this.minScansBetweenAnchors = minScansBetweenAnchors;
	}

	public DefaultPairSet(final List<T> a1, final List<T> a2) {
		this.minScansBetweenAnchors = Factory.getInstance().getConfiguration()
		        .getInt(this.getClass().getName() + ".minScansBetweenAnchors",
		                1);
		// EvalTools.eqI(a1.size(),a2.size());
		this.al = prepareWithSet(a1, a2);
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

	protected List<Tuple2D<T, T>> prepareWithSet(final List<T> a1,
	        final List<T> a2) {
		final List<Tuple2D<T, T>> pairedAnchors = new ArrayList<Tuple2D<T, T>>();
		final HashMap<String, T> s1 = new HashMap<String, T>();
		this.log.debug("Number of anchors: " + pairedAnchors.size());
		for (final T a : a1) {
			this.log.info("Adding lhs anchor with retention index "
			        + a.toString() + " scan index: " + a.getScanIndex());
			s1.put(a.toString(), a);
		}
		for (final T b : a2) {
			this.log.info("Adding rhs anchor with retention index "
			        + b.toString() + " scan index: " + b.getScanIndex());
			if (s1.containsKey(b.toString())) {
				this.log.info("IAnchor matches for ("
				        + s1.get(b.toString()).getScanIndex() + "<->"
				        + b.getScanIndex() + ")");
				pairedAnchors.add(new Tuple2D<T, T>(s1.get(b.toString()), b));
			}
		}

		final List<Tuple2D<T, T>> validAnchors = pairedAnchors;// checkConsistency(pairedAnchors);
		this.log.info("Retaining {} anchors.", validAnchors.size());
		for (final Tuple2D<T, T> t : validAnchors) {
			final T a = t.getFirst();
			final T b = t.getSecond();
			// System.out.println(a.getName()+"
			// "+a.getScanIndex()+"<->"+b.getName()+" "+b.getScanIndex());
			this.log.info(a.getName() + " " + a.getScanIndex() + ":"
			        + b.getName() + " " + b.getScanIndex());
		}
		return validAnchors;
	}

	public List<Tuple2D<T, T>> checkConsistency(
	        List<Tuple2D<T, T>> pairedAnchors) {
		int prevx = -1;
		int prevy = -1;
		final ArrayList<Tuple2D<T, T>> validAnchors = new ArrayList<Tuple2D<T, T>>();
		for (int i = 0; i < pairedAnchors.size(); i++) {
			final Tuple2D<T, T> tpl = pairedAnchors.remove(i);
			// if current indices are lower, we must remove the last anchor
			// allowed configurations:
			// Let u be the current anchor's row and let u' be the previous
			// anchor's
			// row
			// and let v be the current anchor's col and let v' be the previous
			// anchor's col
			// then (u-u'>1 && v-v'>1) is a valid configuration. If either
			// fails, u,v is not a valid
			// anchor and must be removed.
			// the first anchor is always valid

			if (i == 0) {
				prevx = tpl.getFirst().getScanIndex();
				prevy = tpl.getSecond().getScanIndex();
				// add first tuple to valid anchors
				validAnchors.add(tpl);
			}
			// next cases, check, whether current anchor is valid w.r.t.
			// previous anchor
			if ((i > 0) && i < pairedAnchors.size() - 1) {
				if ((tpl.getFirst().getScanIndex() - prevx) > 1
				        && (tpl.getSecond().getScanIndex() - prevy) > 1) {
					validAnchors.add(tpl);
					this.log.info("Keeping valid anchor at {},{}", tpl
					        .getFirst().getScanIndex(), tpl.getSecond()
					        .getScanIndex());
					prevx = tpl.getFirst().getScanIndex();
					prevy = tpl.getSecond().getScanIndex();
				} else {
					this.log.info("Removing invalid anchor at {},{}", tpl
					        .getFirst().getScanIndex(), tpl.getSecond()
					        .getScanIndex());
					prevx = validAnchors.get(validAnchors.size() - 1)
					        .getFirst().getScanIndex();
					prevy = validAnchors.get(validAnchors.size() - 1)
					        .getSecond().getScanIndex();
				}
			}
			// end case, remove preceding anchor, if it violates
			// our monotonicity assumption
			if ((i == pairedAnchors.size() - 1)) {
				if ((tpl.getFirst().getScanIndex() - prevx) > 1
				        && (tpl.getSecond().getScanIndex() - prevy) > 1) {
					validAnchors.add(tpl);
					this.log.info("Keeping valid anchor at {},{}", tpl
					        .getFirst().getScanIndex(), tpl.getSecond()
					        .getScanIndex());
					prevx = tpl.getFirst().getScanIndex();
					prevy = tpl.getSecond().getScanIndex();
				} else {
					validAnchors.remove(validAnchors.size() - 1);
					validAnchors.add(tpl);
					this.log.info("Removing anchor before final anchor");
					prevx = tpl.getFirst().getScanIndex();
					prevy = tpl.getSecond().getScanIndex();
				}
			}
		}
		return validAnchors;
	}

}
