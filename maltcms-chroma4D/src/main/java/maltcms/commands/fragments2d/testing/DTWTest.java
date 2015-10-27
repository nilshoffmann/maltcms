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
package maltcms.commands.fragments2d.testing;

import cross.Factory;
import java.awt.Point;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.distances.dtwng.AlignmentFactory;
import maltcms.commands.distances.dtwng.FeatureVectorDtwSimilarity;
import maltcms.commands.distances.dtwng.IAlignment;
import maltcms.commands.distances.dtwng.ThreePredecessorsOptimization;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.constraint.ConstraintFactory;
import maltcms.datastructures.feature.FeatureVectorFactory;
import maltcms.math.functions.DtwPairwiseSimilarity;
import maltcms.math.functions.similarities.ArrayCos;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 * <p>
 * DTWTest class.</p>
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class DTWTest {

    /**
     * <p>
     * main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
//		Maltcms m = Maltcms.getInstance();
//		CompositeConfiguration cfg = m.parseCommandLine(new String[] { "-f",
//				"asd" });
//		cfg.setProperty("maltcms.ui.charts.PlotRunner.headless", Boolean
//				.valueOf(false));
//		cfg
//				.setProperty(
//						"cross.datastructures.fragments.VariableFragment.useCachedList",
//						Boolean.valueOf(false));
//		cfg
//				.setProperty(
//						"maltcms.datastructures.fragments.PairwiseAlignment.normalizeAlignmentValueByMapWeights",
//						Boolean.valueOf(false));
//		String pipeline = "maltcms.commands.fragments.preprocessing.DefaultVarLoader,maltcms.commands.fragments.preprocessing.DenseArrayProducer";
//		cfg.setProperty("pipeline", pipeline);
//		cfg.setProperty("cross.io.IDataSource", Arrays
//				.asList(new String[] { "maltcms.io.andims.NetcdfDataSource" }));
//		cfg.setProperty("alignment.normalizeAlignmentValueByMapWeights", true);
//		// cfg.save(new BufferedOutputStream(System.out));
//		cross.Factory.getInstance().configure(cfg);

        final ArrayDouble.D1 d1 = new ArrayDouble.D1(2);
        d1.set(0, 1);
        final ArrayDouble.D1 d2 = new ArrayDouble.D1(2);
        d2.set(1, 1);
        final ArrayDouble.D1 d3 = new ArrayDouble.D1(2);
        d3.set(0, 1);
        d3.set(1, 1);
        final ArrayDouble.D1 d4 = new ArrayDouble.D1(2);
        // final ArrayDouble.D1 d5 = new ArrayDouble.D1(1);
        // final ArrayDouble.D1 d6 = new ArrayDouble.D1(1);

        final List<Array> la1 = new ArrayList<>();
        la1.add(d1);
        la1.add(d2);
        la1.add(d2);
        la1.add(d2);
        la1.add(d2);
//		la1.add(d1);
//		la1.add(d1);
//		la1.add(d1);
        final List<Array> la2 = new ArrayList<>();
        la2.add(d1);
        la2.add(d2);
        la2.add(d2);
        la2.add(d2);

        log.info("Creating feature vectors");
        // prepare feature vectors
        FeatureVectorFactory fvf = FeatureVectorFactory.getInstance();
        List<IFeatureVector> l1 = fvf.createFeatureVectorList(la1);
        List<IFeatureVector> l2 = fvf.createFeatureVectorList(la2);

        log.info("Preparing alignment");
        // prepare alignment
        AlignmentFactory af = new AlignmentFactory();
        Area constraints = ConstraintFactory.getInstance().createBandConstraint(
                0, 0, l1.size(), l2.size(), 1.0);
        final FeatureVectorDtwSimilarity tfvo = new FeatureVectorDtwSimilarity();
        final DtwPairwiseSimilarity idsf = new DtwPairwiseSimilarity();
        idsf.setDenseMassSpectraSimilarity(new ArrayCos());
        tfvo.setScoreFunction(idsf);
        tfvo.setArrayFeatureName("FEATURE0");

        IAlignment ia = af.getDTWInstance(Factory.getInstance().getObjectFactory().
                instantiate(ThreePredecessorsOptimization.class),
                tfvo, constraints);
        ia.setConstraints(ConstraintFactory.getInstance().createBandConstraint(
                0, 0, la1.size(), la2.size(), 1.0));
        // set alignment properties
        ia.setLeftHandSideId("Left");
        ia.setRightHandSideId("Right");
        log.info("Calculating alignment");
        // apply and retrieve score
        double v = ia.apply(l1, l2);
        // retrieve map
        List<Point> l = ia.getMap();
        log.info("");
        log.info(ia.getOptimizationFunction().
                getOptimalOperationSequenceString());
        log.info("Done!");
        log.info("{}", v);
        log.info("{}", ia.getOptimizationFunction().getOptimalValue());
        // IFileFragment ares = Factory.getInstance().getFileFragmentFactory()
        // .createFragment(iff3, iff4, DTW.class,
        // cp.getWorkflow().getStartupDate());
        // ia.decorate(ares);
        // ares.save();

    }
}
