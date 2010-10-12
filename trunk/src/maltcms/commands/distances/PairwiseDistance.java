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

package maltcms.commands.distances;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import maltcms.datastructures.array.IArrayD2Double;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.Factory;
import cross.IConfigurable;
import cross.Logging;
import cross.annotations.Configurable;

/**
 * Class used to calculate a pairwise score or cost between mass-spectra.
 * Objects of this class are used in
 * {@link maltcms.commands.distances.dtw.ADynamicTimeWarp} to calculate the
 * distance/similiarity between scans, as configured.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class PairwiseDistance implements IConfigurable {

	public class PartitionCalculator implements Callable<Integer>,
	        IConfigurable {

		private Rectangle shape = null;
		private IArrayD2Double pa = null;
		private IArrayDoubleComp costFunction = null;
		private final Logger log = Logging.getLogger(this);
		private final ArrayDouble.D1 satRef, satQuery;
		private final List<Array> ref, query;

		public PartitionCalculator(final Rectangle shape1,
		        final IArrayD2Double pa1, final ArrayDouble.D1 satRef1,
		        final ArrayDouble.D1 satQuery1, final List<Array> ref1,
		        final List<Array> query1) {
			this.shape = shape1;
			this.pa = pa1;
			this.satRef = satRef1;
			this.satQuery = satQuery1;
			this.ref = ref1;
			this.query = query1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public Integer call() throws Exception {
			final Area a = this.pa.getShape();
			if (a.intersects(this.shape)) {
				this.log.debug("Bounds before intersection: {}", this.shape);
				final Area b = new Area(this.shape);
				b.intersect(a);
				final Rectangle r = b.getBounds();
				this.log.debug("Bounds after intersection: {}", r);
				int counter = 0;
				for (int i = r.y; i < r.y + r.height; i++) {
					final int[] bounds = this.pa.getColumnBounds(i);
					for (int j = bounds[0]; j < bounds[0] + bounds[1]; j++) {
						this.pa.set(i, j, this.costFunction.apply(i, j,
						        this.satRef.get(i), this.satQuery.get(j),
						        this.ref.get(i), this.query.get(j)));
						counter++;
					}
				}
				return new Integer(counter);
			} else {
				this.log
				        .debug(
				                "Job outside of defined bounds on PartitionedArray for rectangle {}",
				                this.shape);
			}

			return new Integer(0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @seecross.IConfigurable#configure(org.apache.commons.configuration.
		 * Configuration)
		 */
		@Override
		public void configure(final Configuration cfg) {
			final String aldist = "maltcms.commands.distances.ArrayLp";
			this.costFunction = Factory.getInstance().getObjectFactory()
			        .instantiate(
			                cfg.getString("alignment.algorithm.distance",
			                        aldist), IArrayDoubleComp.class);
			this.log.debug("Using {}", this.costFunction.getClass().getName());

		}

	}

	private final Logger log = Logging.getLogger(this.getClass());

	@Configurable(name = "alignment.algorithm.distance")
	private IArrayDoubleComp costFunction = null;

	@Configurable(name = "cross.Factory.maxthreads")
	private final int nthreads = 1;

	private IArrayD2Double pad = null;

	private void calcPWD(final Executor e,
	        final Collection<Callable<Integer>> solvers,
	        final IArrayD2Double pairwiseDistance) throws InterruptedException,
	        ExecutionException {
		final CompletionService<Integer> ecs = new ExecutorCompletionService<Integer>(
		        e);
		for (final Callable<Integer> s : solvers) {
			ecs.submit(s);
		}
		final int n = solvers.size();
		int cnt = 0;
		for (int i = 0; i < n; ++i) {
			final Integer r = ecs.take().get();
			if (r > 0) {
				cnt += r;
				// log.info("Calculated {}/{} similarities/distances",cnt,
				// pairwiseDistance.getNumberOfStoredElements());
			}
		}
		this.log.info("Calculated {}/{} similarities/distances", cnt,
		        pairwiseDistance.getNumberOfStoredElements());
	}

	/**
	 * This method can be called for precalculation of all
	 * distances/similarites. Values are set within the paramter ArrayDouble.D2
	 * d and can be retrieved from it.
	 * 
	 * @param d
	 * @param ref
	 * @param query
	 */
	public void calculatePairwiseDistances(final IArrayD2Double d,
	        final ArrayDouble.D1 satRef1, final ArrayDouble.D1 satQuery1,
	        final List<Array> ref, final List<Array> query) {
		if (this.nthreads == 1) {
			this.log
			        .info("Skipping precalculation, only one thread available!");
			return;
		}

		final ArrayDouble.D1 satRef = (satRef1 == null) ? new ArrayDouble.D1(
		        ref.size()) : satRef1;
		final ArrayDouble.D1 satQuery = (satQuery1 == null) ? new ArrayDouble.D1(
		        query.size())
		        : satQuery1;

		final int rows = ref.size();
		final int cols = query.size();
		final float nelements = (rows * cols);
		// float onebynelemens = 1.0f / nelements;
		this.log.info("Calculating {} elements", nelements);
		int i = 0;
		int j = 0;
		this.pad = d;
		final ExecutorService es = Executors.newFixedThreadPool(this.nthreads);
		// int threadpoolsize = Math.max(1, Math.min(50, Math.max(sx, sy)/10));
		// ExecutorService es = Executors.newFixedThreadPool(nthreads);
		this.log.info("Precalculating the distance matrix with {} thread(s)!",
		        this.nthreads);
		final int tilesRows = 2;
		final int tilesCols = 2;
		final int tileSizeRows = (int) Math.ceil((float) rows
		        / (float) tilesRows);
		final int tileSizeCols = (int) Math.ceil((float) cols
		        / (float) tilesCols);
		final ArrayList<Callable<Integer>> solvers = new ArrayList<Callable<Integer>>();
		for (i = 0; i < tilesRows; i++) {
			for (j = 0; j < tilesCols; j++) {
				final Rectangle r = new Rectangle(i * tileSizeCols, j
				        * tileSizeRows, tileSizeCols, tileSizeRows);
				this.log.debug("Creating rectangle: {}", r);
				final PartitionCalculator pc = new PartitionCalculator(r,
				        this.pad, satRef, satQuery, ref, query);
				pc.configure(Factory.getInstance().getConfiguration());
				solvers.add(pc);
			}
		}
		try {
			calcPWD(es, solvers, this.pad);
		} catch (final InterruptedException e) {
			this.log.error(e.getLocalizedMessage());
		} catch (final ExecutionException e) {
			this.log.error(e.getLocalizedMessage());
		}
	}

	@Override
	public void configure(final Configuration cfg) {
		final String aldist = "maltcms.commands.distances.ArrayLp";
		this.costFunction = Factory.getInstance().getObjectFactory()
		        .instantiate(
		                cfg.getString("alignment.algorithm.distance", aldist),
		                IArrayDoubleComp.class);
		this.log.info("Using {}", this.costFunction.getClass().getName());

		// this.nthreads = cfg.getInt("cross.Factory.maxthreads", 1);
	}

	/**
	 * Returns the IArrayComp Object used to calculate pairwise
	 * distances/similarities.
	 * 
	 * @return
	 */
	public IArrayComp<Array, Double> getDistance() {
		return this.costFunction;
	}

	public double getDistance(final int i, final int j, final double time_i,
	        final double time_j, final Array a, final Array b) {
		double d = 0.0d;
		if (this.pad == null) {
			d = this.costFunction.apply(i, j, time_i, time_j, a, b);
		} else {
			return this.pad.get(i, j);
		}
		return d;
	}

}
