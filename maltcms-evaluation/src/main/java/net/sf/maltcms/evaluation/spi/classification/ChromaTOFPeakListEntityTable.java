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

import cross.datastructures.tuple.Tuple2D;
import cross.tools.StringTools;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.io.csv.chromatof.ChromaTOFParser;
import maltcms.io.csv.chromatof.TableRow;
import net.sf.maltcms.evaluation.api.classification.Category;
import net.sf.maltcms.evaluation.api.classification.Entity;
import ucar.ma2.Array;
import ucar.ma2.MAMath;

/**
 *
 * @author Nils Hoffmann
 */
public class ChromaTOFPeakListEntityTable<T extends IFeatureVector> {

    private final Map<Category, List<Entity<T>>> peakTableMap = new LinkedHashMap<Category, List<Entity<T>>>();

    public ChromaTOFPeakListEntityTable(File... peakLists) {
        for (File f : peakLists) {
            Tuple2D<LinkedHashSet<String>, List<TableRow>> t = ChromaTOFParser.parseReport(f, true);
            Category c = new Category(StringTools.removeFileExt(f.getName()));
            peakTableMap.put(c, buildEntities(c, t));
        }
//		System.out.println("Holding "+peakTableMap.keySet().size()+" categories: "+peakTableMap.keySet());
    }

    public Collection<Category> getCategories() {
        return peakTableMap.keySet();
    }

    public final List<Entity<T>> buildEntities(Category c, Tuple2D<LinkedHashSet<String>, List<TableRow>> t) {
        List<Entity<T>> l = new ArrayList<Entity<T>>(t.getSecond().size());
        int rowIndex = 0;
        Set<Double> areaSet = new HashSet<Double>();
        MultiMap<String, Entity<T>> mm = new MultiMap<String, Entity<T>>();
        for (TableRow tr : t.getSecond()) {
            //System.out.println("Parsing row "+(index+1)+"/"+report.getSecond().size());
//			System.out.println("Row data for row index: "+rowIndex+" = "+tr.toString());
            IFeatureVector ifc = null;
            double area = Double.NaN;
            if (tr.containsKey("R.T._(S)")) {
                //fused RT mode
                String rt = tr.get("R.T._(S)");
//				System.out.println("Fused RT");
                //System.out.println("Retention Time: "+rt);
                if (rt.contains(",")) {//2D mode
//					System.out.println("2D chromatogram peak data detected");
                    String[] rts = rt.split(",");
                    double rt1 = ChromaTOFParser.parseDouble(rts[0].trim());
                    double rt2 = ChromaTOFParser.parseDouble(rts[1].trim());
                    area = ChromaTOFParser.parseDouble(tr.get("AREA"));
                    ifc = new Peak2DFeatureVector(tr.get("NAME"), rowIndex, rt1, rt2, area);

                } else {
                    double rtv = ChromaTOFParser.parseDouble(rt.trim());
                    area = ChromaTOFParser.parseDouble(tr.get("AREA"));
                    ifc = new Peak1DFeatureVector(tr.get("NAME"), rowIndex, rtv, area);
                }
            } else {
                if (tr.containsKey("1ST_DIMENSION_TIME_(S)") && tr.containsKey("2ND_DIMENSION_TIME_(S)")) {
//					System.out.println("Separate RT 2D chromatogram peak data detected");
                    double rt1 = ChromaTOFParser.parseDouble(tr.get("1ST_DIMENSION_TIME_(S)"));
                    double rt2 = ChromaTOFParser.parseDouble(tr.get("2ND_DIMENSION_TIME_(S)"));
                    area = ChromaTOFParser.parseDouble(tr.get("AREA"));
                    ifc = new Peak2DFeatureVector(tr.get("NAME"), rowIndex, rt1, rt2, area);
                } else {
                    System.out.println("Skipping unparseable row!");
                }
            }
            if (!areaSet.contains(area)) {
                Entity e = new Entity(ifc, c, tr.get("NAME"));
                l.add(e);
                areaSet.add(area);
            } else {
//				System.out.println("Skipping peak with identical area!");
            }
            rowIndex++;
        }
        return l;
    }

    public List<Entity<T>> getEntities(Category category) {
//		System.out.println("Table contains "+peakTableMap.keySet()+" categories!");
//		for(Category c:peakTableMap.keySet()) {
////			System.out.println("Category "+c.getName()+" contains "+peakTableMap.get(c).size()+" entities!");
//		}
        List<Entity<T>> l = peakTableMap.get(category);
        if (l == null) {
            return Collections.emptyList();
        }
        return l;
    }

    public List<Entity<T>> findMatching(Entity<T> query, String feature) {
        Category c = query.getCategory();
        List<Entity<T>> l = getEntities(c);
        List<Entity<T>> results = new LinkedList<Entity<T>>();
        Array queryA = query.getFeatureVector().getFeature(feature);
//		System.out.println("Query for category: "+c+" = "+query);
//		System.out.println("Categories in table: "+peakTableMap.keySet());
//		System.out.println("Retrieved "+l.size()+" entities from table for category!");
        for (Entity<T> e : l) {
            Array refA = e.getFeatureVector().getFeature(feature);
//			System.out.println("Candidate: "+e);
            if (refA != null) {
                if (MAMath.isEqual(queryA, refA)) {
                    results.add(e);
                }
            }
        }
        return results;
    }
}
