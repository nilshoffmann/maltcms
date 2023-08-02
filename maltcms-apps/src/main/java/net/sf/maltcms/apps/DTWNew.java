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
package net.sf.maltcms.apps;

import cross.Factory;
import cross.datastructures.fragments.FileFragmentFactory;
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
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.distances.alignment.EditDistance;
import maltcms.commands.distances.dtw.ADynamicTimeWarp;
import maltcms.commands.distances.dtwng.AlignmentFactory;
import maltcms.commands.distances.dtwng.FeatureVectorDtwSimilarity;
import maltcms.commands.distances.dtwng.IAlignment;
import maltcms.commands.distances.dtwng.ThreePredecessorsOptimization;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.constraint.ConstraintFactory;
import maltcms.datastructures.feature.FeatureVectorFactory;
//import maltcms.experimental.operations.EditDistance;
import maltcms.math.functions.DtwTimePenalizedPairwiseSimilarity;
import maltcms.math.functions.similarities.ArrayCos;
import maltcms.math.functions.similarities.GaussianDifferenceSimilarity;
import org.apache.commons.configuration.CompositeConfiguration;
import ucar.ma2.Array;

/**
 * <p>DTWNew class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
public class DTWNew {

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        //i "input.basedir"
        //o "output.basedir"
        //f "input.dataInfo
        Maltcms m = Maltcms.getInstance();
        // set up Maltcms configuration
        CompositeConfiguration cfg = m.parseCommandLine(args);
        cfg.setProperty("maltcms.ui.charts.PlotRunner.headless", false);
        cfg.setProperty(
                "cross.datastructures.fragments.VariableFragment.useCachedList", false);
        cfg.setProperty(
                "maltcms.datastructures.fragments.PairwiseAlignment.normalizeAlignmentValueByMapWeights", true);
        String pipeline = "maltcms.commands.fragments.preprocessing.DefaultVarLoader,maltcms.commands.fragments.preprocessing.DenseArrayProducer";
        cfg.setProperty("pipeline", pipeline);
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
        Factory f = new Factory();
        f.configure(cfg);
        log.info("Preparing command sequence");
        ICommandSequence cp = f.createCommandSequence();

        log.info("Running commands");
        TupleND<IFileFragment> res = null;
        while (cp.hasNext()) {
            res = cp.next();
        }
        log.info("Finished running commands");
        IFileFragment iff3 = res.get(0);
        IFileFragment iff4 = res.get(1);

        log.info("Creating feature vectors");
        // prepare feature vectors
        FeatureVectorFactory fvf = FeatureVectorFactory.getInstance();
        iff3.getChild("binned_intensity_values").setIndex(
                iff3.getChild("binned_scan_index"));
        List<Array> bi1 = iff3.getChild("binned_intensity_values").getIndexedArray();
        log.info("Length of binned intensity values: " + bi1.size());

        //
        List<IFeatureVector> l1 = fvf.createBinnedMSFeatureVectorList(iff3,
                true);
        List<IFeatureVector> l2 = fvf.createBinnedMSFeatureVectorList(iff4,
                true);

        log.info("Preparing alignment");
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
        log.info("Calculating alignment");
        // apply and retrieve score
        double v = ia.apply(l1, l2);
        // retrieve map
//        List<Point> l = ia.getMap();
        String s1 = ia.getOptimizationFunction().getOptimalOperationSequenceString();
//        log.info(s1);
        log.info("Done!");
        log.info("{}",v);
        log.info("{}",ia.getOptimizationFunction().getOptimalValue());
        IFileFragment ares = new FileFragmentFactory().create(iff3, iff4, cp.getWorkflow().getOutputDirectory(ia));
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
//            log.info("Both alignments return the same result!");
//        }else{
//            log.info("Both alignments return different results!");
//        }
        //log.info("New: " + l);
        //log.info("Old: " + pl2);
        log.info("New value: " + v);
        log.info("Old value: " + adtw.getResult().get());
        String s2 = getStringFromFile(((IWorkflowFileResult) l.get(0)).getFile());
        EditDistance ed = new EditDistance();
        double d = ed.getDistance(s1, s2);
        log.info("Edit distance: " + d);

    }

    /**
     * <p>getStringFromFile.</p>
     *
     * @param f a {@link java.io.File} object.
     * @return a {@link java.lang.String} object.
     */
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
