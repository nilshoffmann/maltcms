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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.io.csv.chromatof.ChromaTOFParser;
import maltcms.io.csv.chromatof.ChromaTOFParser.ColumnName;
import maltcms.io.csv.chromatof.ChromaTOFParser.TableColumn;
import maltcms.io.csv.chromatof.TableRow;
import net.sf.maltcms.evaluation.api.classification.Category;
import net.sf.maltcms.evaluation.api.classification.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.MAMath;

/**
 * <p>
 * ChromaTOFPeakListEntityTable class.</p>
 *
 * @author Nils Hoffmann
 * @param <T> the entity {@link maltcms.datastructures.array.IFeatureVector} type.
 *
 */
public class ChromaTOFPeakListEntityTable<T extends IFeatureVector> {

    private static final Logger log = LoggerFactory.getLogger(net.sf.maltcms.evaluation.api.ClassificationPerformanceTest.class);

    private final Map<Category, List<Entity<T>>> peakTableMap = new LinkedHashMap<>();

    /**
     * <p>
     * Constructor for ChromaTOFPeakListEntityTable.</p>
     *
     * @param peakLists a {@link java.io.File} object.
     */
    public ChromaTOFPeakListEntityTable(File... peakLists) {
        for (File f : peakLists) {
            ChromaTOFParser parser = ChromaTOFParser.create(f, true, Locale.US);
            Tuple2D<LinkedHashSet<TableColumn>, List<TableRow>> t = ChromaTOFParser.parseReport(parser, f, true);
            Category c = new Category(StringTools.removeFileExt(f.getName()));
            peakTableMap.put(c, buildEntities(c, t, parser));
        }
//		log.info("Holding "+peakTableMap.keySet().size()+" categories: "+peakTableMap.keySet());
    }

    /**
     * <p>
     * getCategories.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Category> getCategories() {
        return peakTableMap.keySet();
    }

    /**
     * <p>
     * buildEntities.</p>
     *
     * @param c a {@link net.sf.maltcms.evaluation.api.classification.Category}
     * object.
     * @param t a {@link cross.datastructures.tuple.Tuple2D} object.
     * @param parser a {@link ChromaTOFParser} object.
     * @return a {@link java.util.List} object.
     */
    public final List<Entity<T>> buildEntities(Category c, Tuple2D<LinkedHashSet<TableColumn>, List<TableRow>> t, ChromaTOFParser parser) {
        List<Entity<T>> l = new ArrayList<>(t.getSecond().size());
        int rowIndex = 0;
        Set<Double> areaSet = new HashSet<>();
        MultiMap<String, Entity<T>> mm = new MultiMap<>();
        for (TableRow tr : t.getSecond()) {
            if (log.isDebugEnabled()) {
                log.debug("Row data for row index: " + rowIndex + " = " + tr.toString());
            }
            IFeatureVector ifc = null;
            double area = Double.NaN;
            if (!tr.getColumnForName(ColumnName.RETENTION_TIME_SECONDS).equals(TableColumn.NIL)) {
                //fused RT mode
                String rt = tr.getValueForName(ColumnName.RETENTION_TIME_SECONDS);
//				log.info("Fused RT");
                //log.info("Retention Time: "+rt);
                if (rt.contains(",")) {//2D mode
//					log.info("2D chromatogram peak data detected");
                    String[] rts = rt.split(",");
                    double rt1 = parser.parseDouble(rts[0].trim());
                    double rt2 = parser.parseDouble(rts[1].trim());
                    area = parser.parseDouble(tr.getValueForName(ColumnName.AREA));
                    ifc = new Peak2DFeatureVector(tr.getValueForName(ColumnName.NAME), rowIndex, rt1, rt2, area);

                } else {
                    double rtv = parser.parseDouble(rt.trim());
                    area = parser.parseDouble(tr.getValueForName(ColumnName.AREA));
                    ifc = new Peak1DFeatureVector(tr.getValueForName(ColumnName.NAME), rowIndex, rtv, area);
                }
            } else {
                if (!tr.getColumnForName(ColumnName.FIRST_DIMENSION_TIME_SECONDS).equals(TableColumn.NIL) && !tr.getColumnForName(ColumnName.SECOND_DIMENSION_TIME_SECONDS).equals(TableColumn.NIL)) {
//					log.info("Separate RT 2D chromatogram peak data detected");
                    double rt1 = parser.parseDouble(tr.getValueForName(ColumnName.FIRST_DIMENSION_TIME_SECONDS));
                    double rt2 = parser.parseDouble(tr.getValueForName(ColumnName.SECOND_DIMENSION_TIME_SECONDS));
                    area = parser.parseDouble(tr.getValueForName(ColumnName.AREA));
                    ifc = new Peak2DFeatureVector(tr.getValueForName(ColumnName.NAME), rowIndex, rt1, rt2, area);
                } else {
                    log.info("Skipping unparseable row!");
                }
            }
            if (!areaSet.contains(area)) {
                Entity e = new Entity(ifc, c, tr.getValueForName(ColumnName.NAME));
                l.add(e);
                areaSet.add(area);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping peak with identical area!");
                }
            }
            rowIndex++;
        }
        return l;
    }

    /**
     * <p>
     * getEntities.</p>
     *
     * @param category a
     * {@link net.sf.maltcms.evaluation.api.classification.Category} object.
     * @return a {@link java.util.List} object.
     */
    public List<Entity<T>> getEntities(Category category) {
        if (log.isDebugEnabled()) {
            log.debug("Table contains " + peakTableMap.keySet() + " categories!");
            for (Category c : peakTableMap.keySet()) {
                log.debug("Category " + c.getName() + " contains " + peakTableMap.get(c).size() + " entities!");
            }
        }
        List<Entity<T>> l = peakTableMap.get(category);
        if (l == null) {
            return Collections.emptyList();
        }
        return l;
    }

    /**
     * <p>
     * findMatching.</p>
     *
     * @param query a
     * {@link net.sf.maltcms.evaluation.api.classification.Entity} object.
     * @param feature a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<Entity<T>> findMatching(Entity<T> query, String feature) {
        Category c = query.getCategory();
        List<Entity<T>> l = getEntities(c);
        List<Entity<T>> results = new LinkedList<>();
        Array queryA = query.getFeatureVector().getFeature(feature);
//		log.info("Query for category: "+c+" = "+query);
//		log.info("Categories in table: "+peakTableMap.keySet());
//		log.info("Retrieved "+l.size()+" entities from table for category!");
        for (Entity<T> e : l) {
            Array refA = e.getFeatureVector().getFeature(feature);
//			log.info("Candidate: "+e);
            if (refA != null) {
                if (MAMath.isEqual(queryA, refA)) {
                    results.add(e);
                }
            }
        }
        return results;
    }
}
