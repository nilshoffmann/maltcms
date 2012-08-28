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
package net.sf.maltcms.apps;

import cross.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflowFileResult;
import cross.datastructures.workflow.IWorkflowResult;
import java.awt.geom.Area;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import maltcms.commands.distances.dtw.ADynamicTimeWarp;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.constraint.ConstraintFactory;
import maltcms.datastructures.feature.FeatureVectorFactory;
import maltcms.commands.distances.dtwng.AlignmentFactory;
import maltcms.commands.distances.dtwng.FeatureVectorDtwSimilarity;
import maltcms.math.functions.similarities.ArrayCos;
import maltcms.experimental.operations.EditDistance;
import maltcms.commands.distances.dtwng.IAlignment;
import maltcms.commands.distances.dtwng.ThreePredecessorsOptimization;
import maltcms.math.functions.DtwTimePenalizedPairwiseSimilarity;
import maltcms.math.functions.similarities.GaussianDifferenceSimilarity;
import org.apache.commons.configuration.CompositeConfiguration;
import ucar.ma2.Array;

/**
 *
 * @author nilshoffmann
 */
public class DTWNew {

    public static void main(String[] args) {
        //i "input.basedir"
        //o "output.basedir"
        //f "input.dataInfo
        Maltcms m = Maltcms.getInstance();
        // set up Maltcms configuration
        CompositeConfiguration cfg = m.parseCommandLine(args);
        cfg.setProperty("maltcms.ui.charts.PlotRunner.headless", Boolean.valueOf(false));
        cfg.setProperty(
                "cross.datastructures.fragments.VariableFragment.useCachedList",
                Boolean.valueOf(false));
        cfg.setProperty(
                "maltcms.datastructures.fragments.PairwiseAlignment.normalizeAlignmentValueByMapWeights",
                Boolean.valueOf(true));
        String pipeline = "maltcms.commands.fragments.preprocessing.DefaultVarLoader,maltcms.commands.fragments.preprocessing.DenseArrayProducer";
        cfg.setProperty("pipeline", pipeline);
        cfg.setProperty("cross.io.IDataSource", Arrays.asList(new String[]{"maltcms.io.andims.NetcdfDataSource"}));
        // cfg.setProperty("alignment.normalizeAlignmentValueByMapWeights",
        // true);
        cfg.setProperty("alignment.save.cumulative.distance.matrix", true);
        cfg.setProperty("alignment.save.pairwise.distance.matrix", true);
        cfg.setProperty("alignment.algorithm.distance",
                "maltcms.commands.distances.ArrayCos");
        cfg.setProperty("alignment.algorithm.windowsize", 0.05);
        cfg.setProperty(
                "maltcms.commands.distances.ArrayCos.compression_weight", 1.0);
        cfg.setProperty("maltcms.commands.distances.ArrayCos.expansion_weight",
                1.0);
        cfg.setProperty("maltcms.commands.distances.ArrayCos.diagonal_weight",
                1.0);
        // cfg.save(new BufferedOutputStream(System.out));
        cross.Factory.getInstance().configure(cfg);
        System.out.println("Preparing command sequence");
        ICommandSequence cp = Factory.getInstance().createCommandSequence();

        System.out.println("Running commands");
        TupleND<IFileFragment> res = null;
        while (cp.hasNext()) {
            res = cp.next();
        }
        System.out.println("Finished running commands");
        IFileFragment iff3 = res.get(0);
        IFileFragment iff4 = res.get(1);

        System.out.println("Creating feature vectors");
        // prepare feature vectors
        FeatureVectorFactory fvf = FeatureVectorFactory.getInstance();
        iff3.getChild("binned_intensity_values").setIndex(
                iff3.getChild("binned_scan_index"));
        List<Array> bi1 = iff3.getChild("binned_intensity_values").getIndexedArray();
        System.out.println("Length of binned intensity values: " + bi1.size());

        //
        List<IFeatureVector> l1 = fvf.createBinnedMSFeatureVectorList(iff3,
                true);
        List<IFeatureVector> l2 = fvf.createBinnedMSFeatureVectorList(iff4,
                true);

        System.out.println("Preparing alignment");
        // prepare alignment
        AlignmentFactory af = new AlignmentFactory();
        FeatureVectorDtwSimilarity fvds = new FeatureVectorDtwSimilarity();
        DtwTimePenalizedPairwiseSimilarity dtwtpps = new DtwTimePenalizedPairwiseSimilarity();
        ArrayCos ac = new ArrayCos();
        dtwtpps.setDenseMassSpectraSimilarity(ac);
        dtwtpps.setRetentionTimeSimilarity(new GaussianDifferenceSimilarity());
        fvds.setScoreFunction(dtwtpps);
        Area constraints = ConstraintFactory.getInstance().createBandConstraint(0, 0, l1.size(), l2.size(), 0.05);
        ThreePredecessorsOptimization tpo = new ThreePredecessorsOptimization();
        dtwtpps.setMatchWeight(2.25d);
        tpo.setWeight(ThreePredecessorsOptimization.State.N.name(), dtwtpps.getExpansionWeight());
        tpo.setWeight(ThreePredecessorsOptimization.State.NW.name(), dtwtpps.getMatchWeight());
        tpo.setWeight(ThreePredecessorsOptimization.State.W.name(), dtwtpps.getCompressionWeight());
        IAlignment ia = af.getDTWInstance(Factory.getInstance().getObjectFactory().instantiate(
                ThreePredecessorsOptimization.class), fvds,
                constraints);
        // set alignment properties
        ia.setLeftHandSideId(iff3.getName());
        ia.setRightHandSideId(iff4.getName());
        System.out.println("Calculating alignment");
        // apply and retrieve score
        double v = ia.apply(l1, l2);
        // retrieve map
//        List<Point> l = ia.getMap();
        String s1 = ia.getOptimizationFunction().getOptimalOperationSequenceString();
//        System.out.println(s1);
        System.out.println("Done!");
        System.out.println(v);
        System.out.println(ia.getOptimizationFunction().getOptimalValue());
        IFileFragment ares = Factory.getInstance().getFileFragmentFactory().create(iff3, iff4, cp.getWorkflow().getOutputDirectory(ia));
        ia.modify(ares);
        ares.save();
        ADynamicTimeWarp adtw = Factory.getInstance().getObjectFactory().instantiate(
                "maltcms.commands.distances.dtw.MZIDynamicTimeWarp",
                ADynamicTimeWarp.class);
        adtw.setWorkflow(cp.getWorkflow());
        IFileFragment iff = adtw.apply(iff3, iff4);
        iff.clearArrays();
        List<IWorkflowResult> l = cp.getWorkflow().getResultsOfType("_path-symbolic.txt");
//        List<Tuple2DI> ll = MaltcmsTools.getWarpPath(iff);

//        List<Point> pl2 = PathTools.toPointList(ll);
//        if (pl2.equals(l)) {
//            System.out.println("Both alignments return the same result!");
//        }else{
//            System.out.println("Both alignments return different results!");
//        }
        //System.out.println("New: " + l);
        //System.out.println("Old: " + pl2);
        System.out.println("New value: " + v);
        System.out.println("Old value: " + adtw.getResult().get());
        String s2 = getStringFromFile(((IWorkflowFileResult) l.get(0)).getFile());
        EditDistance ed = new EditDistance();
        double d = ed.getDistance(s1, s2);
        System.out.println("Edit distance: " + d);

    }

    public static String getStringFromFile(File f) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException ex) {
            Logger.getLogger(DTWNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
}
