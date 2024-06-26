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
package net.sf.maltcms.evaluation.spi.classification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import maltcms.datastructures.array.IFeatureVector;
import net.sf.maltcms.evaluation.api.classification.Category;
import net.sf.maltcms.evaluation.api.classification.Entity;
import net.sf.maltcms.evaluation.api.classification.EntityGroup;
import net.sf.maltcms.evaluation.api.classification.IFeatureVectorComparator;
import net.sf.maltcms.evaluation.api.classification.PeakRTFeatureVectorComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a classification performance test of a collection of EntityGroup
 * instances as ground truth versus a collection of EntityGroup instances, which
 * are to be evaluated.
 *
 * @author Nils Hoffmann
 * @param <T> the feature vector type for this test
 * 
 */

public class ClassificationPerformanceTest<T extends IFeatureVector> {
    
    private static final Logger log = LoggerFactory.getLogger(net.sf.maltcms.evaluation.api.ClassificationPerformanceTest.class);
    
    private final List<EntityGroup<T>> groundTruth;
    private final int numberOfGroundTruthEntities;
    private final IFeatureVectorComparator<T> ifvc;

    /**
     * <p>Constructor for ClassificationPerformanceTest.</p>
     *
     * @param groundTruth a {@link java.util.List} object.
     * @param ifvc a {@link net.sf.maltcms.evaluation.api.classification.IFeatureVectorComparator} object.
     */
    public ClassificationPerformanceTest(List<EntityGroup<T>> groundTruth, IFeatureVectorComparator<T> ifvc) {
        this.groundTruth = groundTruth;
        int nent = 0;
        for (EntityGroup<T> eg : this.groundTruth) {
            nent += eg.getCategories().size();
        }
        this.numberOfGroundTruthEntities = nent;
        this.ifvc = ifvc;
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        Category c1 = new Category("c1");
        Category c2 = new Category("c2");
        Category c3 = new Category("c3");
        Category[] cats = new Category[]{c1, c2, c3};

        double[][] gt = new double[6][];
        gt[0] = new double[]{1.42, 1.44, 1.45};
        gt[1] = new double[]{Double.NaN, 2.2, 2.1};
        gt[2] = new double[]{4.62, 4.58, 4.6};
        gt[3] = new double[]{6.34, 6.32, Double.NaN};
        gt[4] = new double[]{6.7, 6.65, 6.71};
        gt[5] = new double[]{7.23, 7.12, 7.123};

        List<EntityGroup<PeakRTFeatureVector>> gtl = new ArrayList<>();
        for (int i = 0; i < gt.length; i++) {
            @SuppressWarnings("rawtypes")
			List<Entity<PeakRTFeatureVector>> e = new ArrayList<>(gt[i].length);
            for (int j = 0; j < gt[i].length; j++) {
                e.add(new Entity<>(new PeakRTFeatureVector(gt[i][j], Double.NaN), cats[j], "gt" + (i + 1)));
            }
            EntityGroup<PeakRTFeatureVector> eg = new EntityGroup<>(e);
            gtl.add(eg);
        }

        double[][] data = new double[6][];
        data[0] = new double[]{1.421, 1.435, 1.446};
        data[1] = new double[]{Double.NaN, 2.207, 2.075};
        data[2] = new double[]{4.622, 4.581, 4.586};
        data[3] = new double[]{5.31, 5.27, 5.5};
        data[4] = new double[]{6.34999, 6.321, Double.NaN};
        data[5] = new double[]{7.23, Double.NaN, 7.12};

        List<EntityGroup<PeakRTFeatureVector>> datal = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            List<Entity<PeakRTFeatureVector>>  e = new ArrayList<>(data[i].length);
            for (int j = 0; j < data[i].length; j++) {
                e.add(new Entity<>(new PeakRTFeatureVector(data[i][j], Double.NaN), cats[j], "data" + (i + 1)));
            }
            EntityGroup<PeakRTFeatureVector> eg = new EntityGroup<>(e);
            datal.add(eg);
        }

        ClassificationPerformanceTest<PeakRTFeatureVector> cpt = new ClassificationPerformanceTest<>(gtl, new PeakRTFeatureVectorComparator(0.02));
        log.info("Test Results: {}", cpt.performTest("test", datal));
    }

    /**
     * <p>performTest.</p>
     *
     * @param toolname a {@link java.lang.String} object.
     * @param testGroup a {@link java.util.List} object.
     * @return a {@link net.sf.maltcms.evaluation.spi.classification.PerformanceMetrics} object.
     * @throws java.lang.IllegalArgumentException if any.
     */
    public PerformanceMetrics<T> performTest(String toolname, List<EntityGroup<T>> testGroup) throws IllegalArgumentException {
        //log.debug("Performing classification performance test for " + toolname);
        if (!checkCategories(this.groundTruth, testGroup)) {
            throw new IllegalArgumentException("Could not match categories to ground truth for tool: " + toolname + "!");
        }

        //total number of elements
        int N = this.numberOfGroundTruthEntities;

        //number of different categories/files
        //double M = this.groundTruth.getCategories().size();
        //we need to compare all out testGroups against
        //the ground truth.
        //we first try to find the ground truth EntityGroup, which best matches
        //to a given testGroup entity group
        HashMap<EntityGroup<T>, EntityGroupClassificationResult<T>> gtToClsRes = new LinkedHashMap<>();

        int M = 0;
        for (EntityGroup<?> eg : testGroup) {
            M += eg.getCategories().size();
        }

        int K = testGroup.get(0).getCategories().size();

        int cnt = 0;
        //log.debug("Matching " + M + " entities against ground truth!");
        for (EntityGroup<T> tgEg : testGroup) {
            //log.debug("Entity group " + (++cnt) + "/" + testGroup.size());
            //find the ground truth group, which has the highest tp1+tn1 number
            //may be null if no tp1 and/or tn1 hits are found
            EntityGroupClassificationResult<T> gtg = findBest(tgEg, this.groundTruth);

            if (gtg != null) {
//				log.info("##################");
//				log.info("GT group: \n");
//				log.info(gtg.getGroundTruthEntityGroup());
//                //log.debug("GT group: \n" + gtg.getGroundTruthEntityGroup());
//                //log.debug("Best tool group: \n" + gtg.getToolEntityGroup());
//				log.info("Tool group: \n");
//				log.info(gtg.getToolEntityGroup());
//				log.info("TP: "+gtg.getTp()+" FP: "+gtg.getFp()+" TN: "+gtg.getTn()+" FN: "+gtg.getFn());
//				log.info("##################");
                EntityGroup<T> gtEntityGroup = gtg.getGroundTruthEntityGroup();
                if (gtToClsRes.containsKey(gtEntityGroup)) {
                    //log.warn("Warning: GT EntityGroup already assigned!");
                    //test for reassignment
                    EntityGroupClassificationResult<T> other = gtToClsRes.get(gtEntityGroup);
                    int comp = gtg.compareTo(other);
                    if (comp > 0) {
                        //gtg is better than other
                        //log.debug("Changing assignment for ground truth group " + gtEntityGroup + "\n from group " + other.getToolEntityGroup() + "\n to group: " + gtg.getToolEntityGroup() + "\n");
                        gtToClsRes.put(gtEntityGroup, gtg);
                    } else if (comp < 0) {
                        //other is better, do nothing
                        //log.debug("Retaining assignment");
                    } else {
                        //both are equal, something fishy is happening here!
                        //log.warn("Warning: classification results are equal!");
                    }
                } else {
                    gtToClsRes.put(gtEntityGroup, gtg);
                }
            }
        }
        HashSet<EntityGroup<?>> matchedToolGroups = new LinkedHashSet<>();
        int tp = 0, tn = 0, fp = 0, fn = 0;
        double dist = 0;
        for (EntityGroup<T> gtGroup : gtToClsRes.keySet()) {
            EntityGroupClassificationResult<T> egcr = gtToClsRes.get(gtGroup);
            tp += egcr.getTp();
            tn += egcr.getTn();
            fp += egcr.getFp();
            fn += egcr.getFn();
            dist += egcr.getDist();
            EntityGroup<T> toolGroup = gtToClsRes.get(gtGroup).getToolEntityGroup();
            matchedToolGroups.add(toolGroup);
        }
        HashSet<EntityGroup<T>> unmatchedToolGroups = new LinkedHashSet<>(testGroup);
        unmatchedToolGroups.removeAll(matchedToolGroups);

        HashSet<EntityGroup<T>> matchedGTGroups = new LinkedHashSet<>(gtToClsRes.keySet());
        HashSet<EntityGroup<T>> unmatchedGTGroups = new LinkedHashSet<>(groundTruth);
        unmatchedGTGroups.removeAll(matchedGTGroups);

        PerformanceMetrics<T> pm = new PerformanceMetrics<>(toolname, tp, fp, tn, fn, N, M, K, dist, unmatchedToolGroups, unmatchedGTGroups, gtToClsRes);
        return pm;
    }

    /**
     * We expect to find at least one positive assignment in a group. To rank
     * the groups, we focus on TP and TN first.
     *
     * @param testGroup a {@link net.sf.maltcms.evaluation.api.classification.EntityGroup} object.
     * @param groundTruth a {@link java.util.List} object.
     * @return a {@link net.sf.maltcms.evaluation.spi.classification.EntityGroupClassificationResult} object.
     */
    public EntityGroupClassificationResult<T> findBest(EntityGroup<T> testGroup, List<EntityGroup<T>> groundTruth) {
        //int tmpCorrect = 1;

        Set<Category> categories = testGroup.getCategories();
        //int tmpFalse = categories.size() - 1;
        //int tpOpt = 0, fpOpt = categories.size(), tnOpt = 0, fnOpt = categories.size();
        //double minDist = Double.POSITIVE_INFINITY;
        EntityGroupClassificationResult<T> bestGroup = null;
        for (EntityGroup<T> groundTruthEntityGroup : groundTruth) {
            int tp = 0;
            int tn = 0;
            int fp = 0;
            int fn = 0;
            //the optimal group has only true assignments
            //however, TN are assigned if value NaN is encountered
            //thus, if we choose to use the sum of TP and TN as our indicator,
            //we may get the situation, that we have only TN assignments
            //a complete true negative assignment is not allowed, since such
            //a group would not occur in the ground truth anyway.
            //What we require is at least one TP match or a FP or a FN hit in any of the categories,
            //or any number of TN together with at least one FP or FN hit.
            //Since the group, which scores the highest number of TPs or TNs will get the assignment,
            //we will "optimize" towards the group with the highest TP + TN
            //This approach is thus greedy, but performTest prints a warning,
            //if a group is assigned to a gt group, which has already been assigned before.
            double dist = 0;
            for (Category c : categories) {
                Entity<T> gtEntity = groundTruthEntityGroup.getEntityForCategory(c);
                Entity<T> testEntity = testGroup.getEntityForCategory(c);
                if (this.ifvc.isTP(gtEntity.getFeatureVector(), testEntity.getFeatureVector())) {
                    tp++;
                }
                if (this.ifvc.isTN(gtEntity.getFeatureVector(), testEntity.getFeatureVector())) {
                    tn++;
                }
                if (this.ifvc.isFP(gtEntity.getFeatureVector(), testEntity.getFeatureVector())) {
                    fp++;
                }
                if (this.ifvc.isFN(gtEntity.getFeatureVector(), testEntity.getFeatureVector())) {
                    fn++;
                }
                dist += this.ifvc.getSquaredDiff(gtEntity.getFeatureVector(), testEntity.getFeatureVector());
            }
            dist = Math.sqrt(dist);
            //minimum one tp
            if (tp > 0) {
                EntityGroupClassificationResult<T> egcr = new EntityGroupClassificationResult<>(testGroup, groundTruthEntityGroup, tp, tn, fp, fn, dist);
                //log.info(egcr);
                if (bestGroup == null) {
                    bestGroup = egcr;
                } else {
                    try {
                        int comp = egcr.compareTo(bestGroup);
                        //log.info("CompareTo returned: " + comp);
                        if (comp > 0) {
                            bestGroup = egcr;
                        } else if (comp == 0) {
                            //log.warn("Warning: Entity groups are equal!");
                        }
                    } catch (IllegalArgumentException iae) {
                        //log.warn("IllegalArgumentException: " + iae.getLocalizedMessage());
                    }
                }
            }
        }
        return bestGroup;
    }

    private void printScoreForGroup(EntityGroup<T> gtGroup, EntityGroup<T> testGroup) {
        Set<Category> categories = testGroup.getCategories();
        int tp = 0, tn = 0, fp = 0, fn = 0;
        for (Category c : categories) {
            Entity<T> gtEntity = gtGroup.getEntityForCategory(c);
            Entity<T> testEntity = testGroup.getEntityForCategory(c);
            if (this.ifvc.isTP(gtEntity.getFeatureVector(), testEntity.getFeatureVector())) {
                tp++;
            }
            if (this.ifvc.isTN(gtEntity.getFeatureVector(), testEntity.getFeatureVector())) {
                tn++;
            }
            if (this.ifvc.isFP(gtEntity.getFeatureVector(), testEntity.getFeatureVector())) {
                fp++;
            }
            if (this.ifvc.isFN(gtEntity.getFeatureVector(), testEntity.getFeatureVector())) {
                fn++;
            }
        }
        //log.debug("Score of test group \n" + testGroup.toString() + "\n versus ground truth group \n" + gtGroup.toString() + "\n" + " tp = " + tp + " tn = " + tn + " fp = " + fp + " fn = " + fn);
    }

    private EntityGroup<T> getBetterGroup(EntityGroup<T> gtGroup, EntityGroup<T> testGroup1, EntityGroup<T> testGroup2) {
        int[] cnts1 = getCounts(gtGroup, testGroup1);
        int[] cnts2 = getCounts(gtGroup, testGroup2);
        EntityGroup<T> best = testGroup1;
        if (cnts1[0] > cnts2[0]) {
            return testGroup1;
        } else if (cnts1[0] < cnts2[0]) {
            return testGroup2;
        } else {
            if (cnts1[1] > cnts2[1]) {
                return testGroup1;
            } else if (cnts1[1] < cnts2[1]) {
                return testGroup2;
            } else {
                if (cnts1[2] < cnts2[2]) {
                    return testGroup1;
                } else if (cnts1[2] > cnts2[2]) {
                    return testGroup2;
                } else {
                    if (cnts1[3] < cnts2[3]) {
                        return testGroup1;
                    } else if (cnts1[3] > cnts2[3]) {
                        return testGroup2;
                    } else {
                        Set<Category> categories = gtGroup.getCategories();
                        double dist1 = 0, dist2 = 0;
                        for (Category c : categories) {
                            Entity<T> gtEntity = gtGroup.getEntityForCategory(c);
                            Entity<T> testEntity1 = testGroup1.getEntityForCategory(c);
                            Entity<T> testEntity2 = testGroup2.getEntityForCategory(c);
                            dist1 += this.ifvc.getSquaredDiff(gtEntity.getFeatureVector(), testEntity1.getFeatureVector());
                            dist2 += this.ifvc.getSquaredDiff(gtEntity.getFeatureVector(), testEntity2.getFeatureVector());
                        }
                        if (dist1 < dist2) {
                            return testGroup1;
                        } else if (dist1 > dist2) {
                            return testGroup2;
                        }
                        //log.warn("Group\n" + testGroup1 + "\nand\n" + testGroup2 + "\nboth can not be distinguished versus ground truth group\n" + gtGroup);
                        throw new IllegalArgumentException("Can not decide, which group fits better, check for feature overlap!");
                    }

                }
            }
        }
    }

    /**
     * 0-> tp 1-> tn 2-> fp 3-> fn
     *
     * @return
     */
    private int[] getCounts(EntityGroup<T> gtGroup, EntityGroup<T> testGroup) {
        int tp1 = 0, tn1 = 0, fp1 = 0, fn1 = 0;
        Set<Category> categories = testGroup.getCategories();
        for (Category c : categories) {
            Entity<T> gtEntity = gtGroup.getEntityForCategory(c);
            Entity<T> testEntity = testGroup.getEntityForCategory(c);
            if (this.ifvc.isTP(gtEntity.getFeatureVector(), testEntity.getFeatureVector())) {
                tp1++;
            }
            if (this.ifvc.isTN(gtEntity.getFeatureVector(), testEntity.getFeatureVector())) {
                tn1++;
            }
            if (this.ifvc.isFP(gtEntity.getFeatureVector(), testEntity.getFeatureVector())) {
                fp1++;
            }
            if (this.ifvc.isFN(gtEntity.getFeatureVector(), testEntity.getFeatureVector())) {
                fn1++;
            }
        }
        return new int[]{tp1, tn1, fp1, fn1};
    }

    /**
     * <p>checkCategories.</p>
     *
     * @param gt a {@link java.util.List} object.
     * @param testGroup a {@link java.util.List} object.
     * @return a boolean.
     */
    public boolean checkCategories(List<EntityGroup<T>> gt, List<EntityGroup<T>> testGroup) {
        boolean check = false;
        int ncat = -1;
        //test all pairs
        for (EntityGroup<T> eg : gt) {
            if (ncat == -1) {
                ncat = eg.getCategories().size();
            } else {
                if (ncat != eg.getCategories().size()) {
                    //log.warn("Number of categories in ground truth differs! Check rows!");
                }
            }
            for (EntityGroup<T> tg : testGroup) {
                if (ncat != tg.getCategories().size()) {
                    //log.warn("Number of categories in test group differs! Check rows!");
                }
                check = checkCategories(eg, tg);
            }
        }
        return check;
    }

    /**
     * <p>checkCategories.</p>
     *
     * @param gt a {@link net.sf.maltcms.evaluation.api.classification.EntityGroup} object.
     * @param testGroup a {@link net.sf.maltcms.evaluation.api.classification.EntityGroup} object.
     * @return a boolean.
     */
    public boolean checkCategories(EntityGroup<T> gt, EntityGroup<T> testGroup) {
        //categories need to be the same
        Set<Category> gtCats = gt.getCategories();
        Set<Category> tgCats = testGroup.getCategories();
        //test if both are subsets of each other => sets are equal
        if (gtCats.containsAll(tgCats) && tgCats.containsAll(gtCats)) {
            return true;
        }
        //log.warn("Categories differ between ground truth and test group!");
        List<Category> gtList = new ArrayList<>(gtCats);
        Collections.sort(gtList);
        List<Category> tgList = new ArrayList<>(gtList);
        Collections.sort(tgList);
        //log.warn("GroundTruth: " + gtList);
        //log.warn("TestGroup:" + tgList);
        return false;
    }
}
