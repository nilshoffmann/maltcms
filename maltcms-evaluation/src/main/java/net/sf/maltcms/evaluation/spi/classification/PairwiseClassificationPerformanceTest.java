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

import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import maltcms.datastructures.array.IFeatureVector;
import net.sf.maltcms.evaluation.api.classification.Category;
import net.sf.maltcms.evaluation.api.classification.Entity;
import net.sf.maltcms.evaluation.api.classification.EntityGroup;
import net.sf.maltcms.evaluation.api.classification.EntityGroupList;
import net.sf.maltcms.evaluation.api.classification.IFeatureVectorComparator;
import net.sf.maltcms.evaluation.api.classification.INamedPeakFeatureVector;
import net.sf.maltcms.evaluation.api.classification.IPerformanceMetrics;
import static net.sf.maltcms.evaluation.api.classification.IPerformanceMetrics.Vars.F1;
import static net.sf.maltcms.evaluation.api.classification.IPerformanceMetrics.Vars.FN;
import static net.sf.maltcms.evaluation.api.classification.IPerformanceMetrics.Vars.FP;
import static net.sf.maltcms.evaluation.api.classification.IPerformanceMetrics.Vars.TN;
import static net.sf.maltcms.evaluation.api.classification.IPerformanceMetrics.Vars.TP;
import net.sf.maltcms.evaluation.api.classification.PeakNameFeatureVectorComparator;
import net.sf.maltcms.evaluation.api.classification.PeakRowIndexFeatureVectorComparator;
import net.sf.maltcms.evaluation.spi.EntityGroupBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>PairwiseClassificationPerformanceTest class.</p>
 *
 * @author Nils Hoffmann
 * @param <T> the named feature vector type for this test
 * 
 */

public class PairwiseClassificationPerformanceTest<T extends INamedPeakFeatureVector> {
    
    private static final Logger log = LoggerFactory.getLogger(net.sf.maltcms.evaluation.api.ClassificationPerformanceTest.class);
    
    private final EntityGroupList<T> groundTruth;
    private final int numberOfGroundTruthEntities;
    private final IFeatureVectorComparator<T> ifvc;
    private final ChromaTOFPeakListEntityTable<T> peakTable;

    /**
     * <p>Constructor for PairwiseClassificationPerformanceTest.</p>
     *
     * @param peakTable a {@link net.sf.maltcms.evaluation.spi.classification.ChromaTOFPeakListEntityTable} object.
     * @param groundTruth a {@link net.sf.maltcms.evaluation.api.classification.EntityGroupList} object.
     * @param ifvc a {@link net.sf.maltcms.evaluation.api.classification.IFeatureVectorComparator} object.
     */
    public PairwiseClassificationPerformanceTest(ChromaTOFPeakListEntityTable<T> peakTable, EntityGroupList<T> groundTruth, IFeatureVectorComparator<T> ifvc) {
        this.peakTable = peakTable;
        this.groundTruth = groundTruth;
        int nent = 0;
        for (EntityGroup<T> eg : this.groundTruth) {
            nent += eg.getCategories().size();
        }
        this.numberOfGroundTruthEntities = nent;
        this.ifvc = ifvc;
    }

    /**
     * <p>createMspaPairMap.</p>
     *
     * @param entities a {@link net.sf.maltcms.evaluation.api.classification.EntityGroupList} object.
     * @param <T> a T object.
     * @return a {@link java.util.Map} object.
     */
    public static <T extends IFeatureVector> Map<String, EntityGroupList<T>> createMspaPairMap(EntityGroupList<T> entities) {
        Map<String, EntityGroupList<T>> m = new LinkedHashMap<>();
        int cnt = 0;
        int size = entities.getCategoriesSize() - 1;
        Category[] categories = entities.getCategories().toArray(new Category[entities.getCategoriesSize()]);
        for (int i = 0; i < entities.getCategoriesSize() - 1; i++) {
//			log.info("Adding pair " + (cnt + 1) + " of " + size);
            m.put(i + "-" + (i + 1), entities.getSubList(categories[i], categories[(i + 1)]));
            cnt++;
        }
        return m;
    }

    /**
     * <p>createPairMap.</p>
     *
     * @param entities a {@link net.sf.maltcms.evaluation.api.classification.EntityGroupList} object.
     * @param <T> a T object.
     * @return a {@link java.util.Map} object.
     */
    public static <T extends IFeatureVector> Map<String, EntityGroupList<T>> createPairMap(EntityGroupList<T> entities) {
        Map<String, EntityGroupList<T>> m = new LinkedHashMap<>();
        int cnt = 0;
        int size = entities.getCategoriesSize() * (entities.getCategoriesSize() - 1) / 2;
        Category[] categories = entities.getCategories().toArray(new Category[entities.getCategoriesSize()]);
        for (int i = 0; i < entities.getCategoriesSize() - 1; i++) {
            for (int j = i + 1; j < entities.getCategoriesSize(); j++) {
//				log.info("Adding pair " + (cnt + 1) + " of " + size);
                m.put(i + "-" + j, entities.getSubList(categories[i], categories[j]));
                cnt++;
            }
        }
        return m;
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        EntityGroupBuilder egb = new EntityGroupBuilder();
        File[] files = FileUtils.listFiles(new File("/home/hoffmann/Uni/projects/ChromA4DPaper/evaluation2/mSPA/data/mSPA_Dataset_I/"), new String[]{"csv"}, false).toArray(new File[0]);
        ChromaTOFPeakListEntityTable<INamedPeakFeatureVector> t = new ChromaTOFPeakListEntityTable<>(files);
        List<EntityGroup<INamedPeakFeatureVector>> ref = egb.buildCSVPeak2DAssociationGroups(new File("/home/hoffmann/Uni/projects/ChromA4DPaper/evaluation2/mSPA/groundTruth/mSPA_Dataset_I/reference-alignment.txt"), t);
        EntityGroupList<INamedPeakFeatureVector> referenceGroups = new EntityGroupList<>(ref.get(0).getCategories().toArray(new Category[0]));
        referenceGroups.addAll(ref);
        Map<String, List<EntityGroup<INamedPeakFeatureVector>>> tools = new LinkedHashMap<>();
        ///home/hoffmann/Uni/projects/ChromA4DPaper/evaluation2/results/chlamy_Dataset_I/mspa/b1cf508f-fd7a-37b8-bb13-848ca9f2b0f0/PAM/1
        tools.put("mSPA", egb.buildMSPAPeak2DAssociationGroups(new File("/home/hoffmann/Uni/projects/ChromA4DPaper/evaluation2/results/mSPA_Dataset_I_short/mspa/847570cb-09d1-3411-a74c-8f1b2697baed/PAM/1/"), t));
        tools.put("SWPA", egb.buildMSPAPeak2DAssociationGroups(new File("/home/hoffmann/Uni/projects/ChromA4DPaper/evaluation2/results/mSPA_Dataset_I_short/swpa/dd1978d5-be59-3dcf-ad8a-56002c341bad/SWRE/1/"), t));
        tools.put("BiPACE 2D", egb.buildCSVPeak2DAssociationGroups(new File("/home/hoffmann/Uni/projects/ChromA4DPaper/evaluation2/results/mSPA_Dataset_I_short/bipace2D/5c4d6c12-72e7-3d89-8d4f-ef2117d06df7/0_PeakCliqueAlignment/multiple-alignment.csv"), t));
        //guineu 9a34522f-7fed-39dc-9420-dcc32ccaf3b5
        tools.put("Guineu", egb.buildCSVPeak2DAssociationGroups(new File("/home/hoffmann/Uni/projects/ChromA4DPaper/evaluation2/results/mSPA_Dataset_I_short/guineu/0e029001-7d61-3b29-8d0e-20f05c413ded/multiple-alignment.csv"), t));
        for (String key : tools.keySet()) {
            log.info("Tool: " + key);
            EntityGroupList<INamedPeakFeatureVector> toolGroups = new EntityGroupList<>(tools.get(key).get(0).getCategories().toArray(new Category[0]));
            toolGroups.addAll(tools.get(key));
//		Category c1 = new Category("c1");
//		Category c2 = new Category("c2");
//		Category c3 = new Category("c3");
//		Category[] cats = new Category[]{c1, c2, c3};
//
//		double[][] gt = new double[6][];
//		gt[0] = new double[]{1, 1, 2};
//		gt[1] = new double[]{Double.NaN, 2, 3};
//		gt[2] = new double[]{4, 4, 4};
//		gt[3] = new double[]{6, 6, Double.NaN};
//		gt[4] = new double[]{7, 7, 8};
//		gt[5] = new double[]{5, 8, 5};
//
//		EntityGroupList egl = new EntityGroupList(cats);
//		for (int i = 0; i < gt.length; i++) {
//			Entity[] e = new Entity[gt[i].length];
//			for (int j = 0; j < e.length; j++) {
//				e[j] = new Entity(new PeakRTFeatureVector(gt[i][j]), cats[j], "gt" + (i + 1));
//			}
//			EntityGroup eg = new EntityGroup(e);
//			egl.add(eg);
//		}
//
//		double[][] data = new double[6][];
//		data[0] = new double[]{1, 1, 1}; //1 FP, 2 TP
//		data[1] = new double[]{Double.NaN, 2, 3}; // 1 TN, 2 TP
//		data[2] = new double[]{4, 4, 4}; // 3 TP
//		data[3] = new double[]{6, Double.NaN, 6}; // 1 TP, 1 FN, 1 FP
//		data[4] = new double[]{7, 7, Double.NaN}; // 2 TP, 1 FN
//		data[5] = new double[]{Double.NaN, Double.NaN, 5}; // 2 FN, 1 TP
//		/*
//		 * expected result row-wise:
//		 *		c1 c2 c3
//		 * 0:	TP TP FP
//		 * 1:	TN TP TP
//		 * 2:	TP TP TP
//		 * 3:	TP FN FP
//		 * 4:	TP TP FN
//		 * 5:	FN FN TP
//		 *
//		 * TP: 2 + 2 + 3 + 1 + 2 + 1 = 11 TP
//		 * FP: 1 + 1 = 2 FP
//		 * TN: 1 = 1 TN
//		 * FN: 1 + 1 + 2 = 4 FN
//		 *
//		 * expected result pair-wise:
//		 *     c1-c2 c2-c3 c1-c3
//		 * 0:  TP TP TP FP TP FP
//		 * 1:  TN TP TP TP TN TP
//		 * 2:  TP TP TP TP TP TP
//		 * 3:  TP FN FN FP TP FP
//		 * 4:  TP TP TP FN TP FN
//		 * 5:  FN FN FN TP FN TP
//		 *
//		 * TP: 8     7     7	= 21 TP
//		 * FP: 0     2     2	= 4 FP
//		 * TN: 1     0     1		= 2 TN
//		 * FN: 3     2     2	= 7 FN
//		 */
//		EntityGroupList datal = new EntityGroupList(cats);
//		for (int i = 0; i < data.length; i++) {
//			Entity[] e = new Entity[data[i].length];
//			for (int j = 0; j < e.length; j++) {
//				e[j] = new Entity(new PeakRTFeatureVector(data[i][j]), cats[j], "data" + (i + 1));
//			}
//			EntityGroup eg = new EntityGroup(e);
//			datal.add(eg);
//		}

            PairwiseClassificationPerformanceTest<INamedPeakFeatureVector> cpt = new PairwiseClassificationPerformanceTest<>(t, referenceGroups, new PeakNameFeatureVectorComparator());
            List<PairwisePerformanceMetrics> pm = cpt.performTest(key, toolGroups);
            MultiMap<IPerformanceMetrics.Vars, Number> metricsMap = new MultiMap<>();
            log.info("Pairwise evaluation:");
            log.info("INSTANCE\tTP\tFP\tTN\tFN\tF1");
            for (PairwisePerformanceMetrics metrics : pm) {
//			log.info(metrics);
                metricsMap.put(TP, metrics.getTp());
                metricsMap.put(FP, metrics.getFp());
                metricsMap.put(TN, metrics.getTn());
                metricsMap.put(FN, metrics.getFn());
                metricsMap.put(F1, metrics.getF1());
                log.info(metrics.getInstanceName() + "\t" + metrics.getTp() + "\t" + metrics.getFp() + "\t" + metrics.getTn() + "\t" + metrics.getFn() + "\t" + metrics.getF1());
            }
            for (IPerformanceMetrics.Vars var : metricsMap.keySet()) {
                double[] values = toArray(metricsMap.get(var));
                DescriptiveStatistics ds = new DescriptiveStatistics(values);
                log.info(var.toString() + ": totalValue=" + ds.getSum() + "; min=" + ds.getMin() + "; max=" + ds.getMax() + "; mean=" + ds.getMean() + "+/-" + ds.getStandardDeviation());
            }
            ClassificationPerformanceTest<INamedPeakFeatureVector> rpt = new ClassificationPerformanceTest<>(referenceGroups, new PeakRowIndexFeatureVectorComparator<INamedPeakFeatureVector>());
            PerformanceMetrics<INamedPeakFeatureVector> rpm = rpt.performTest("testTool", toolGroups);
            MultiMap<PerformanceMetrics.Vars, Number> metricsMap2 = new MultiMap<>();
            metricsMap2.put(TP, rpm.getTp());
            metricsMap2.put(FP, rpm.getFp());
            metricsMap2.put(TN, rpm.getTn());
            metricsMap2.put(FN, rpm.getFn());
            metricsMap2.put(F1, rpm.getF1());
            log.info("Row-wise evaluation:");
            for (PerformanceMetrics.Vars var : metricsMap2.keySet()) {
                double[] values = toArray(metricsMap2.get(var));
                DescriptiveStatistics ds = new DescriptiveStatistics(values);
                log.info(var.toString() + ": totalValue=" + ds.getSum() + "; min=" + ds.getMin() + "; max=" + ds.getMax() + "; mean=" + ds.getMean() + "+/-" + ds.getStandardDeviation());
            }
        }
//		log.info(cpt.performTest("test", datal));
    }

    /**
     * <p>toArray.</p>
     *
     * @param c a {@link java.util.Collection} object.
     * @return an array of double.
     */
    public static double[] toArray(Collection<Number> c) {
        double[] values = new double[c.size()];
        Number[] numbers = c.toArray(new Number[c.size()]);
        for (int i = 0; i < values.length; i++) {
            values[i] = numbers[i].doubleValue();
        }
        return values;
    }

    /**
     * <p>getPairwisePerformanceMetrics.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPairwisePerformanceMetrics() {
        return null;

    }

    /**
     * <p>getNumberOfNonNulls.</p>
     *
     * @param l a {@link java.util.List} object.
     * @return a int.
     */
    public int getNumberOfNonNulls(List<Entity<T>> l) {
        int n = 0;
        for (Entity<T> e : l) {
//			log.info("EG: "+eg);
            if (e.getFeatureVector().getName() != null) {
                n++;
            }
        }
        return n;
    }

    /**
     * <p>performPairTest.</p>
     *
     * @param toolName a {@link java.lang.String} object.
     * @param instanceName a {@link java.lang.String} object.
     * @param tool a {@link net.sf.maltcms.evaluation.api.classification.EntityGroupList} object.
     * @param comparisonFeatureName a {@link java.lang.String} object.
     * @return a {@link net.sf.maltcms.evaluation.spi.classification.PairwisePerformanceMetrics} object.
     */
    public PairwisePerformanceMetrics performPairTest(String toolName, String instanceName, EntityGroupList<T> tool, String comparisonFeatureName) {

//		d1.name = as.character(d1$name)
//    d2.name = as.character(d2$name)
//    # match names of d1 to names of d2, should be unique, maximum length = |d1|
//    total.pos = length(na.omit(match(d1.name,d2.name)))
//    total.neg = length(d1.name)*length(d2.name) - total.pos
//
//    d1 = d1[d1$nflag!=0,] # ref
//    d2 = d2[d2$nflag!=0,] # tar
//    d1 = d1[order(d1$nflag),]
//    d2 = d2[order(d2$nflag),]
//nflag refers to the index of the match in the other peak list
//    d1$name = as.character(d1$name)
//    d2$name = as.character(d2$name)
//    total.match = dim(d1)[1]
//number of peaks in d1
//    t.p = 0
//    for(i in 1:total.match){
//        if(d1$name[i] == d2$name[i]){
//            t.p = t.p + 1
//        }
//    }
//    f.p = total.match - t.p
//    f.n = total.pos - t.p
//    t.n = total.neg - f.p
//    t.p.r = t.p/total.pos # t.p/t.p+f.n
//    p.p.v = t.p/(t.p+f.p)
//    f1 = 2*t.p.r*p.p.v/(t.p.r+p.p.v)
//
//    rlt=c(t.p.r,p.p.v,t.p,f.p,f.n,t.n,f1)
//    rlt
//		log.info("Instance: " + instanceName);
        //count the number of peaks in the reference
        int totalPos = 0;
        int lhsPeaks = 0;
        int rhsPeaks = 0;
        //check assignments from tool -> Kim et al remove the unmatched peaks before comparing
        //nflag is recreated for each pairwise alignment, based on the original peak lists
        //thus, unassigned peaks are not counted at all!
        TupleND<Category> toolCategories = new TupleND<>(tool.getCategories());
        if (toolCategories.size() != 2) {
            throw new IllegalArgumentException("Can only process category pairs!");
        }

        for (Tuple2D<Category, Category> t : toolCategories.getPairs()) {
            List<Entity<T>> lhsEntities = groundTruth.getEntities(t.getFirst());
            List<Entity<T>> rhsEntities = groundTruth.getEntities(t.getSecond());
            lhsPeaks += getNumberOfNonNulls(lhsEntities);
            rhsPeaks += getNumberOfNonNulls(rhsEntities);
            //log.info("Lhs Peaks: " + lhsPeaks + " Rhs Peaks: " + rhsPeaks);
            for (EntityGroup<T> lhs : groundTruth.getSubList(t.getFirst(), t.getSecond())) {
                int nonNulls = getNumberOfNonNulls(lhs.getEntities());
                if (nonNulls == 2) {
                    totalPos++;
                }
//				String lhsName = lhs.getFeatureVector().getName();
//				for (Entity<T> rhs : groundTruth.getEntities(t.getSecond())) {
//					String rhsName = rhs.getFeatureVector().getName();
//					if (lhsName != null && rhsName != null) {
////						if (lhsName.equals(rhsName)) {
////							log.info(lhsName + "==" + rhsName);
//							totalPos++;
////							break;
////						}
//					}
//				}
            }
            //log.info("Matched peaks: "+totalPos);
        }
        int totalMatch = 0;
        int nentities = lhsPeaks + rhsPeaks;
        int totalNeg = (lhsPeaks * rhsPeaks) - totalPos;
//		log.info("number of peaks: " + nentities + " total.pos=" + totalPos + " total.neg=" + totalNeg + "; totalNeg+totalPos=" + (totalNeg + totalPos));
        int tp = 0;
        for (Tuple2D<Category, Category> c : toolCategories.getPairs()) {
//			log.info("Comparing category " + c.getFirst() + " with " + c.getSecond());
            for (EntityGroup<T> tgEg : tool) {
                Entity<T> lhsEnt = tgEg.getEntityForCategory(c.getFirst());
                Entity<T> rhsEnt = tgEg.getEntityForCategory(c.getSecond());
                if (groundTruth.containsEntity(lhsEnt, comparisonFeatureName) && groundTruth.containsEntity(rhsEnt, comparisonFeatureName)) {
                    String lhsName = lhsEnt.getFeatureVector().getName();
                    String rhsName = rhsEnt.getFeatureVector().getName();
                    //we skip single and double gaps
                    if (lhsName != null && rhsName != null) {
                        totalMatch++;
                    }
                    if (lhsName != null && rhsName != null) {
                        if (lhsName.equals(rhsName)) {
//							log.info("TP: "+lhsEnt.getFeatureVector()+" -/- "+rhsEnt.getFeatureVector());
                            //						log.info("TP: " + lhsName + "\t" + rhsName);
                            //						log.info("TP");
                            tp++;
                        } else {
//							log.info("FP: "+lhsEnt.getFeatureVector()+" -/- "+rhsEnt.getFeatureVector());
                            //						log.info("FP: " + lhsName + "\t" + rhsName);
                        }
                    }
                }
            }
        }

        int fn = totalPos - tp;
        int fp = totalMatch - tp;
        int tn = totalNeg - fp;
//		log.info("Total match: " + totalMatch + " TP: " + tp + " FP: " + fp + " total pos: " + totalPos + " FN: " + fn + " total neg: " + totalNeg + " TN: " + tn);
        if ((totalPos + totalNeg) != (tp + fp + tn + fn)) {
            throw new IllegalArgumentException("Sum of positives+negatives!=tp+fp+tn+fn");
        }
        PairwisePerformanceMetrics ppm = new PairwisePerformanceMetrics(toolName, instanceName, tp, fp, tn, fn);
        return ppm;
    }

    /**
     * <p>performTest.</p>
     *
     * @param toolName a {@link java.lang.String} object.
     * @param toolGroups a {@link net.sf.maltcms.evaluation.api.classification.EntityGroupList} object.
     * @return a {@link java.util.List} object.
     * @throws java.lang.IllegalArgumentException if any.
     */
    public List<PairwisePerformanceMetrics> performTest(String toolName, EntityGroupList<T> toolGroups) throws IllegalArgumentException {
        //log.debug("Performing classification performance test for " + toolname);
        if (!checkCategories(this.groundTruth, toolGroups)) {
            throw new IllegalArgumentException("Could not match categories to ground truth for tool: " + toolName + "!");
        }

//		Map<String, EntityGroupList<T>> refMap = createMspaPairMap(this.groundTruth);
        Map<String, EntityGroupList<T>> resultMap = createPairMap(toolGroups);
        MultiMap<IPerformanceMetrics.Vars, Number> metricsMap = new MultiMap<>();
        List<PairwisePerformanceMetrics> pml = new ArrayList<>();
        for (String s : resultMap.keySet()) {
//			EntityGroupList<T> ref1 = refMap.get(s);
            EntityGroupList<T> result1 = resultMap.get(s);
            PairwisePerformanceMetrics pm = performPairTest(toolName, result1.getCategories().toString(), result1, "ROWINDEX");
            metricsMap.put(TP, pm.getTp());
            metricsMap.put(FP, pm.getFp());
            metricsMap.put(TN, pm.getTn());
            metricsMap.put(FN, pm.getFn());
            metricsMap.put(F1, pm.getF1());
            pml.add(pm);
//			log.info(pm);
        }
        return pml;
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
                    //log.warn("Number of toolCategories in ground truth differs! Check rows!");
                }
            }
            for (EntityGroup<T> tg : testGroup) {
                if (ncat != tg.getCategories().size()) {
                    //log.warn("Number of toolCategories in test group differs! Check rows!");
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
