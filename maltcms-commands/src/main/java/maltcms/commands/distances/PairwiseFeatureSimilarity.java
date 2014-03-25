/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.commands.distances;

import cross.Factory;
import cross.IConfigurable;
import cross.annotations.Configurable;
import java.awt.Rectangle;
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
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.array.IArrayD2Double;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 * Class used to calculate a pairwise score or cost between mass-spectra.
 * Objects of this class are used in
 * {@link maltcms.commands.distances.dtw.ADynamicTimeWarp} to calculate the
 * distance/similiarity between scans, as configured.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
@Data
public class PairwiseFeatureSimilarity implements IConfigurable {

    /**
     *
     */
    private static final long serialVersionUID = -2087449317783907180L;
    @Configurable(name = "alignment.algorithm.distance")
    private IDtwSimilarityFunction similarityFunction = null;
    @Configurable(name = "cross.Factory.maxthreads")
    private int nthreads = 1;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private IArrayD2Double pad = null;

    private void calcPWD(final Executor e,
            final Collection<Callable<Integer>> solvers,
            final IArrayD2Double pairwiseDistance) throws InterruptedException,
            ExecutionException {
        final CompletionService<Integer> ecs = new ExecutorCompletionService<>(
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
        log.info("Calculated {}/{} similarities/distances", cnt,
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
            log.info("Skipping precalculation, only one thread available!");
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
        log.info("Calculating {} elements", nelements);
        int i = 0;
        int j = 0;
        this.pad = d;
        final ExecutorService es = Executors.newFixedThreadPool(this.nthreads);
        // int threadpoolsize = Math.max(1, Math.min(50, Math.max(sx, sy)/10));
        // ExecutorService es = Executors.newFixedThreadPool(nthreads);
        log.info("Precalculating the distance matrix with {} thread(s)!",
                this.nthreads);
        final int tilesRows = 2;
        final int tilesCols = 2;
        final int tileSizeRows = (int) Math.ceil((float) rows
                / (float) tilesRows);
        final int tileSizeCols = (int) Math.ceil((float) cols
                / (float) tilesCols);
        final ArrayList<Callable<Integer>> solvers = new ArrayList<>();
        for (i = 0; i < tilesRows; i++) {
            for (j = 0; j < tilesCols; j++) {
                final Rectangle r = new Rectangle(i * tileSizeCols, j
                        * tileSizeRows, tileSizeCols, tileSizeRows);
                log.debug("Creating rectangle: {}", r);
                final PartitionCalculator pc = new PartitionCalculator(r,
                        this.pad, satRef, satQuery, ref, query);
                pc.configure(Factory.getInstance().getConfiguration());
                solvers.add(pc);
            }
        }
        try {
            calcPWD(es, solvers, this.pad);
        } catch (final InterruptedException | ExecutionException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     *
     * @param cfg
     */
    @Override
    public void configure(final Configuration cfg) {
//        final String aldist = "maltcms.commands.distances.ArrayLp";
//        this.costFunction = Factory.getInstance().getObjectFactory().instantiate(
//                cfg.getString("alignment.algorithm.distance", aldist),
//                IDtwSimilarityFunction.class);
//        log.info("Using {}", this.costFunction.getClass().getName());
        // this.nthreads = cfg.getInt("cross.Factory.maxthreads", 1);
    }

    /**
     * Returns the IDtwSimilarityFunction Object used to calculate pairwise
     * distances/similarities.
     *
     * @return
     */
//    public IDtwSimilarityFunction getDistance() {
//        return this.similarityFunction;
//    }
    public double getDistance(final int i, final int j, final double time_i,
            final double time_j, final Array a, final Array b) {
        double d = 0.0d;
        if (this.pad == null) {
            d = this.similarityFunction.apply(i, j, time_i, time_j, a, b);
        } else {
            return this.pad.get(i, j);
        }
        return d;
    }
}
