/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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

import cross.tools.MathTools;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import static net.sf.maltcms.evaluation.spi.classification.PerformanceMetrics.Vars.*;
import java.util.Set;
import maltcms.datastructures.array.IFeatureVector;
import net.sf.maltcms.evaluation.api.classification.Category;
import net.sf.maltcms.evaluation.api.classification.Entity;
import net.sf.maltcms.evaluation.api.classification.EntityGroup;
import net.sf.maltcms.evaluation.api.classification.EntityGroupList;
import net.sf.maltcms.evaluation.api.classification.IFeatureVectorComparator;
import net.sf.maltcms.evaluation.api.classification.PeakRTFeatureVectorComparator;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Nils Hoffmann
 */
public class PairwiseClassificationPerformanceTest<T extends IFeatureVector> {

	private final List<EntityGroup> groundTruth;
	private final int numberOfGroundTruthEntities;
	private final IFeatureVectorComparator ifvc;

	public PairwiseClassificationPerformanceTest(List<EntityGroup> groundTruth, IFeatureVectorComparator ifvc) {
		this.groundTruth = groundTruth;
		int nent = 0;
		for (EntityGroup eg : this.groundTruth) {
			nent += eg.getCategories().size();
		}
		this.numberOfGroundTruthEntities = nent;
		this.ifvc = ifvc;
	}

	public static Map<String, EntityGroupList> createPairMap(EntityGroupList entities) {
		Map<String, EntityGroupList> m = new LinkedHashMap<String, EntityGroupList>();
		int cnt = 0;
		int size = entities.getCategoriesSize() * (entities.getCategoriesSize() - 1) / 2;
		Category[] categories = entities.getCategories().toArray(new Category[entities.getCategoriesSize()]);
		for (int i = 0; i < entities.getCategoriesSize() - 1; i++) {
			for (int j = i + 1; j < entities.getCategoriesSize(); j++) {
				System.out.println("Adding pair " + (cnt + 1) + " of " + size);
				m.put(i + "-" + j, entities.getSubList(categories[i], categories[j]));
				cnt++;
			}
		}
		return m;
	}

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

		EntityGroupList egl = new EntityGroupList(cats);
		for (int i = 0; i < gt.length; i++) {
			Entity[] e = new Entity[gt[i].length];
			for (int j = 0; j < e.length; j++) {
				e[j] = new Entity(new PeakRTFeatureVector(gt[i][j]), cats[j], "gt" + (i + 1));
			}
			EntityGroup eg = new EntityGroup(e);
			egl.add(eg);
		}

		double[][] data = new double[6][];
		data[0] = new double[]{1.421, 1.435, 1.446};
		data[1] = new double[]{Double.NaN, 2.207, 2.075};
		data[2] = new double[]{4.622, 4.581, 4.586};
		data[3] = new double[]{5.31, 5.27, 5.5};
		data[4] = new double[]{6.34999, 6.321, Double.NaN};
		data[5] = new double[]{7.23, Double.NaN, 7.12};

		EntityGroupList datal = new EntityGroupList(cats);
		for (int i = 0; i < data.length; i++) {
			Entity[] e = new Entity[data[i].length];
			for (int j = 0; j < e.length; j++) {
				e[j] = new Entity(new PeakRTFeatureVector(data[i][j]), cats[j], "data" + (i + 1));
			}
			EntityGroup eg = new EntityGroup(e);
			datal.add(eg);
		}

		Map<String, EntityGroupList> refMap = createPairMap(egl);
		Map<String, EntityGroupList> resultMap = createPairMap(datal);
		String toolName = "testTool";
		MultiMap<PerformanceMetrics.Vars, Number> metricsMap = new MultiMap<PerformanceMetrics.Vars, Number>();
		for (String s : resultMap.keySet()) {
			EntityGroupList egl1 = refMap.get(s);
			EntityGroupList resultMap1 = resultMap.get(s);
			ClassificationPerformanceTest<PeakRTFeatureVector> cpt = new ClassificationPerformanceTest<PeakRTFeatureVector>(egl1, new PeakRTFeatureVectorComparator(0.02));
			PerformanceMetrics pm = cpt.performTest(egl1.getCategories().toString(), resultMap1);
			metricsMap.put(TP, pm.getTp());
			metricsMap.put(FP, pm.getFp());
			metricsMap.put(TN, pm.getTn());
			metricsMap.put(FN, pm.getFn());
			metricsMap.put(F1, pm.getF1());
//			System.out.println(pm);
		}
		System.out.println("Pairwise evaluation:");
		for (PerformanceMetrics.Vars var : metricsMap.keySet()) {
			double[] values = toArray(metricsMap.get(var));
			DescriptiveStatistics ds = new DescriptiveStatistics(values);
			System.out.println(var.toString() + ": totalValue=" + ds.getSum() + "; min=" + ds.getMin() + "; max=" + ds.getMax() + "; mean=" + ds.getMean() + "+/-" + ds.getStandardDeviation());
		}
		ClassificationPerformanceTest<PeakRTFeatureVector> cpt = new ClassificationPerformanceTest<PeakRTFeatureVector>(egl, new PeakRTFeatureVectorComparator(0.02));
		PerformanceMetrics pm = cpt.performTest(toolName, datal);
		MultiMap<PerformanceMetrics.Vars, Number> metricsMap2 = new MultiMap<PerformanceMetrics.Vars, Number>();
		metricsMap2.put(TP, pm.getTp());
		metricsMap2.put(FP, pm.getFp());
		metricsMap2.put(TN, pm.getTn());
		metricsMap2.put(FN, pm.getFn());
		metricsMap2.put(F1, pm.getF1());
		System.out.println("Row-wise evaluation:");
		for (PerformanceMetrics.Vars var : metricsMap2.keySet()) {
			double[] values = toArray(metricsMap2.get(var));
			DescriptiveStatistics ds = new DescriptiveStatistics(values);
			System.out.println(var.toString() + ": totalValue=" + ds.getSum()+"; min=" + ds.getMin() + "; max=" + ds.getMax() + "; mean=" + ds.getMean() + "+/-" + ds.getStandardDeviation());
		}
//		System.out.println(cpt.performTest("test", datal));
	}

	public static double[] toArray(Collection<Number> c) {
		double[] values = new double[c.size()];
		Number[] numbers = c.toArray(new Number[c.size()]);
		for (int i = 0; i < values.length; i++) {
			values[i] = numbers[i].doubleValue();
		}
		return values;
	}

	public String getPairwisePerformanceMetrics() {
		return null;
//		cscore <- function(d1,d2){
//    d1.name = as.character(d1$name)
//    d2.name = as.character(d2$name)
//	total number of matching rows between 1 and 2 (identical name)
//    total.pos = length(na.omit(match(d1.name,d2.name))) 
// total number of peaks with the same name in common, these should be found	
//	product of names in 1 and names in 2 (omitting empty rows)
// total number of peaks that could be assigned, but would score as wrong assignments
//    total.neg = length(d1.name)*length(d2.name) - total.pos
//
//	select only peaks that have a match in ref
//    d1 = d1[d1$nflag!=0,] # ref
//	select only peaks that have a match in target
//    d2 = d2[d2$nflag!=0,] # tar
//    d1 = d1[order(d1$nflag),]
//    d2 = d2[order(d2$nflag),]
//    d1$name = as.character(d1$name)
//    d2$name = as.character(d2$name)
//	total number of matches (including TP, FP matches) from d1 into d2
//    total.match = dim(d1)[1]
//    t.p = 0
//    for(i in 1:total.match){
//			count true positives
//        if(d1$name[i] == d2$name[i]){
//            t.p = t.p + 1
//        }
//    }
// FP = #MATCHES - TP
//    f.p = total.match - t.p
// FN = #POSITIVES - TP
//    f.n = total.pos - t.p
// TN = #NEGATIVES - FP
//    t.n = total.neg - f.p
//	  total.pos <- t.p + f.n		
//    t.p.r = t.p/total.pos
//    p.p.v = t.p/(t.p+f.p)
//    f1 = 2*t.p.r*p.p.v/(t.p.r+p.p.v)
//
//    rlt=c(t.p.r,p.p.v,t.p,f.p,f.n,t.n,f1)
//    rlt
//}
	}

	public List<PerformanceMetrics> performTest(String toolname, List<EntityGroup> testGroup) throws IllegalArgumentException {
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

		HashMap<EntityGroup, EntityGroupClassificationResult> gtToClsRes = new LinkedHashMap<EntityGroup, EntityGroupClassificationResult>();

		int M = 0;
		for (EntityGroup eg : testGroup) {
			M += eg.getCategories().size();
		}

		int K = testGroup.get(0).getCategories().size();

		int cnt = 0;

		//log.debug("Matching " + M + " entities against ground truth!");
		for (EntityGroup tgEg : testGroup) {
			//log.debug("Entity group " + (++cnt) + "/" + testGroup.size());
			//find the ground truth group, which has the highest tp1+tn1 number
			//may be null if no tp1 and/or tn1 hits are found
			EntityGroupClassificationResult gtg = findBest(tgEg, this.groundTruth);

			if (gtg != null) {
				//log.debug("GT group: \n" + gtg.getGroundTruthEntityGroup());
				//log.debug("Best tool group: \n" + gtg.getToolEntityGroup());
				EntityGroup gtEntityGroup = gtg.getGroundTruthEntityGroup();
				if (gtToClsRes.containsKey(gtEntityGroup)) {
					//System.err.println("Warning: GT EntityGroup already assigned!");
					//test for reassignment
					EntityGroupClassificationResult other = gtToClsRes.get(gtEntityGroup);
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
		HashSet<EntityGroup> matchedToolGroups = new LinkedHashSet<EntityGroup>();
		int tp = 0, tn = 0, fp = 0, fn = 0;
		double dist = 0;
		for (EntityGroup gtGroup : gtToClsRes.keySet()) {
			EntityGroupClassificationResult egcr = gtToClsRes.get(gtGroup);
			tp += egcr.getTp();
			tn += egcr.getTn();
			fp += egcr.getFp();
			fn += egcr.getFn();
			dist += egcr.getDist();
			EntityGroup toolGroup = gtToClsRes.get(gtGroup).getToolEntityGroup();
			matchedToolGroups.add(toolGroup);
		}
		HashSet<EntityGroup> unmatchedToolGroups = new LinkedHashSet<EntityGroup>(testGroup);
		unmatchedToolGroups.removeAll(matchedToolGroups);

		HashSet<EntityGroup> matchedGTGroups = new LinkedHashSet<EntityGroup>(gtToClsRes.keySet());
		HashSet<EntityGroup> unmatchedGTGroups = new LinkedHashSet<EntityGroup>(groundTruth);
		unmatchedGTGroups.removeAll(matchedGTGroups);

		PerformanceMetrics pm = new PerformanceMetrics(toolname, tp, fp, tn, fn, N, M, K, dist, unmatchedToolGroups, unmatchedGTGroups, gtToClsRes);
		return Arrays.asList(pm);
	}

	/**
	 * We expect to find at least one positive assignment in a group. To rank
	 * the groups, we focus on TP and TN first.
	 *
	 * @param testGroup
	 * @param groundTruth
	 * @return
	 */
	public EntityGroupClassificationResult findBest(EntityGroup testGroup, List<EntityGroup> groundTruth, Category... categories) {
		//int tmpCorrect = 1;

		//int tmpFalse = categories.size() - 1;
		//int tpOpt = 0, fpOpt = categories.size(), tnOpt = 0, fnOpt = categories.size();
		//double minDist = Double.POSITIVE_INFINITY;
		EntityGroupClassificationResult bestGroup = null;
		for (EntityGroup groundTruthEntityGroup : groundTruth) {
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
				Entity gtEntity = groundTruthEntityGroup.getEntityForCategory(c);
				Entity testEntity = testGroup.getEntityForCategory(c);
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
				EntityGroupClassificationResult egcr = new EntityGroupClassificationResult(testGroup, groundTruthEntityGroup, tp, tn, fp, fn, dist);
				//System.out.println(egcr);
				if (bestGroup == null) {
					bestGroup = egcr;
				} else {
					try {
						int comp = egcr.compareTo(bestGroup);
						//System.out.println("CompareTo returned: " + comp);
						if (comp > 0) {
							bestGroup = egcr;
						} else if (comp == 0) {
							//System.err.println("Warning: Entity groups are equal!");
						}
					} catch (IllegalArgumentException iae) {
						//System.err.println("IllegalArgumentException: " + iae.getLocalizedMessage());
					}
				}
			}
		}
		return bestGroup;
	}

	public boolean checkCategories(List<EntityGroup> gt, List<EntityGroup> testGroup) {
		boolean check = false;
		int ncat = -1;
		//test all pairs
		for (EntityGroup eg : gt) {
			if (ncat == -1) {
				ncat = eg.getCategories().size();
			} else {
				if (ncat != eg.getCategories().size()) {
					//log.warn("Number of categories in ground truth differs! Check rows!");
				}
			}
			for (EntityGroup tg : testGroup) {
				if (ncat != tg.getCategories().size()) {
					//log.warn("Number of categories in test group differs! Check rows!");
				}
				check = checkCategories(eg, tg);
			}
		}
		return check;
	}

	public boolean checkCategories(EntityGroup gt, EntityGroup testGroup) {
		//categories need to be the same
		Set<Category> gtCats = gt.getCategories();
		Set<Category> tgCats = testGroup.getCategories();
		//test if both are subsets of each other => sets are equal
		if (gtCats.containsAll(tgCats) && tgCats.containsAll(gtCats)) {
			return true;
		}
		//log.warn("Categories differ between ground truth and test group!");
		List<Category> gtList = new ArrayList<Category>(gtCats);
		Collections.sort(gtList);
		List<Category> tgList = new ArrayList<Category>(gtList);
		Collections.sort(tgList);
		//log.warn("GroundTruth: " + gtList);
		//log.warn("TestGroup:" + tgList);
		return false;
	}
}
