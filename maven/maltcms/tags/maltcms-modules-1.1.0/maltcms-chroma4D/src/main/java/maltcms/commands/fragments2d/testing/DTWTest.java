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
package maltcms.commands.fragments2d.testing;

import java.awt.Point;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.constraint.ConstraintFactory;
import maltcms.datastructures.feature.FeatureVectorFactory;
import maltcms.commands.distances.dtwng.AlignmentFactory;
import maltcms.commands.distances.dtwng.IAlignment;
import maltcms.commands.distances.dtwng.ThreePredecessorsOptimization;

import org.apache.commons.configuration.CompositeConfiguration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import net.sf.maltcms.apps.Maltcms;
import cross.Factory;
import maltcms.commands.distances.dtwng.FeatureVectorDtwSimilarity;
import maltcms.math.functions.DtwPairwiseSimilarity;
import maltcms.math.functions.similarities.ArrayCos;

public class DTWTest {

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

        final List<Array> la1 = new ArrayList<Array>();
        la1.add(d1);
        la1.add(d2);
        la1.add(d2);
        la1.add(d2);
        la1.add(d2);
//		la1.add(d1);
//		la1.add(d1);
//		la1.add(d1);
        final List<Array> la2 = new ArrayList<Array>();
        la2.add(d1);
        la2.add(d2);
        la2.add(d2);
        la2.add(d2);

        System.out.println("Creating feature vectors");
        // prepare feature vectors
        FeatureVectorFactory fvf = FeatureVectorFactory.getInstance();
        List<IFeatureVector> l1 = fvf.createFeatureVectorList(la1);
        List<IFeatureVector> l2 = fvf.createFeatureVectorList(la2);

        System.out.println("Preparing alignment");
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
        System.out.println("Calculating alignment");
        // apply and retrieve score
        double v = ia.apply(l1, l2);
        // retrieve map
        List<Point> l = ia.getMap();
        System.out.println("");
        System.out.println(ia.getOptimizationFunction().
                getOptimalOperationSequenceString());
        System.out.println("Done!");
        System.out.println(v);
        System.out.println(ia.getOptimizationFunction().getOptimalValue());
        // IFileFragment ares = Factory.getInstance().getFileFragmentFactory()
        // .createFragment(iff3, iff4, DTW.class,
        // cp.getWorkflow().getStartupDate());
        // ia.decorate(ares);
        // ares.save();

    }
}
