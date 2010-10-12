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
package maltcms.commands.fragments2d.peakfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.peak.Peak2D;
import maltcms.db.MetaboliteQueryDB;
import maltcms.db.QueryCallable;
import maltcms.db.predicates.metabolite.MScanSimilarityPredicate;
import maltcms.db.similarities.MetaboliteSimilarity;
import maltcms.tools.ArrayTools2;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.IndexIterator;

import com.db4o.ObjectSet;
import com.db4o.ext.Db4oIOException;

import cross.Logging;
import cross.datastructures.tuple.Tuple2D;

/**
 * Will do the identification.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public class PeakIdentification implements IPeakIdentification {

	private final Logger log = Logging.getLogger(this);

	private MetaboliteQueryDB mqdb;

	private boolean doSearch = false;
	private String dbFile = null;
	private boolean dbAvailable = true;
	private double threshold = 0.08d;
	private int k = 1;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Configuration cfg) {
		// this.doDBSearch = cfg.getBoolean(this.getClass().getName()
		// + ".doDBSearch", false);
		this.dbFile = cfg
		        .getString(this.getClass().getName() + ".dbFile", null);
		this.threshold = cfg.getDouble(this.getClass().getName()
		        + ".dbThreshold", 0.8d);
		this.doSearch = cfg.getBoolean(this.getClass().getName() + ".doSearch",
		        false);
		this.k = cfg.getInteger(this.getClass().getName() + ".kBest", 1);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setName(final Peak2D peak) {
		if ((this.dbFile == null) || this.dbFile.isEmpty()) {
			this.dbAvailable = false;
		}
		if (this.dbAvailable && this.doSearch) {
			if (this.mqdb == null) {
				this.mqdb = new MetaboliteQueryDB(this.dbFile, null);
			}
			final ArrayDouble.D1 massValues = new ArrayDouble.D1(peak
			        .getPeakArea().getSeedMS().getShape()[0]);
			final IndexIterator iter = massValues.getIndexIterator();
			int c = 0;
			while (iter.hasNext()) {
				iter.setIntNext(c++);
			}
			final ArrayInt.D1 ms = ArrayTools2.createIntegerArray(peak
			        .getPeakArea().getSeedMS());

			final Tuple2D<Array, Array> query = new Tuple2D<Array, Array>(
			        massValues, ms);

			final MScanSimilarityPredicate ssp = new MScanSimilarityPredicate(
			        query);
			ssp.setThreshold(this.threshold);

			this.mqdb.setPredicate(ssp);
			final QueryCallable<IMetabolite> qc = this.mqdb.getCallable();
			ObjectSet<IMetabolite> osRes = null;

			List<Tuple2D<Double, IMetabolite>> hits = null;
			try {
				osRes = qc.call();

				// this.log.info("Received {} hits from ObjectSet!",
				// osRes.size());

				if ((osRes != null) && (osRes.size() != 0)) {
					final List<Tuple2D<Double, IMetabolite>> l = new ArrayList<Tuple2D<Double, IMetabolite>>();
					final MetaboliteSimilarity mss = new MetaboliteSimilarity();
					for (final IMetabolite im : osRes) {
						l.add(new Tuple2D<Double, IMetabolite>(mss.get(query,
						        im), im));
					}
					if (this.k > 1) {
						final Comparator<Tuple2D<Double, IMetabolite>> comp = new Comparator<Tuple2D<Double, IMetabolite>>() {
							@Override
							public int compare(
							        final Tuple2D<Double, IMetabolite> o1,
							        final Tuple2D<Double, IMetabolite> o2) {
								return o1.getFirst().compareTo(o2.getFirst());
							}
						};
						Collections.sort(l, Collections.reverseOrder(comp));
						hits = l.subList(0, Math.min(l.size(), this.k + 1));
						// for (Tuple2D<Double, IMetabolite> tuple2D : hits) {
						// System.out.println(""
						// + (int) (tuple2D.getFirst() * 1000.0) + ""
						// + im.getRetentionIndex() + "" + im.getFormula()
						// + im.getID());
						// }
					} else {
						double max = Double.NEGATIVE_INFINITY;
						IMetabolite maxMet = null;
						for (final Tuple2D<Double, IMetabolite> t : l) {
							if (max < t.getFirst()) {
								max = t.getFirst();
								maxMet = t.getSecond();
							}
						}
						hits = new ArrayList<Tuple2D<Double, IMetabolite>>();
						hits.add(new Tuple2D<Double, IMetabolite>(max, maxMet));
					}
				}
				qc.terminate();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} catch (final ExecutionException e) {
				e.printStackTrace();
			} catch (final Db4oIOException e) {
				e.printStackTrace();
				this.log.error("Stopping DB search.");
				this.dbAvailable = false;
			} catch (final NullPointerException e) {
				e.printStackTrace();
				this.log.error("Stopping DB search.");
				this.dbAvailable = false;
			} catch (final Exception e) {
				e.printStackTrace();
			}

			if (hits != null) {
				peak.setNames(hits);
			}
		}
	}

}
