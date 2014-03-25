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
package net.sf.maltcms.evaluation.spi;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import cross.datastructures.tuple.Tuple2D;
import cross.tools.StringTools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.io.csv.CSVReader;
import net.sf.maltcms.evaluation.api.classification.Category;
import net.sf.maltcms.evaluation.api.classification.Entity;
import net.sf.maltcms.evaluation.api.classification.EntityGroup;
import net.sf.maltcms.evaluation.api.classification.INamedPeakFeatureVector;
import net.sf.maltcms.evaluation.api.classification.IRowIndexNamedPeakFeatureVector;
import net.sf.maltcms.evaluation.spi.classification.ChromaTOFPeakListEntityTable;
import net.sf.maltcms.evaluation.spi.classification.Peak2DFeatureVector;
import net.sf.maltcms.evaluation.spi.classification.PeakRTFeatureVector;
import net.sf.maltcms.evaluation.spi.hohenheim.Eval;
import net.sf.maltcms.evaluation.spi.xcalibur.Chromatogram;
import net.sf.maltcms.evaluation.spi.xcalibur.Peak;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

/**
 * @author Nils Hoffmann
 *
 *
 */
public class EntityGroupBuilder {

    /**
     * Peak association in xcalibur reports is via peak/compound name.
     *
     * @param e
     * @param oc
     * @return list of EntityGroups, possibly not in any particular order
     */
    public List<EntityGroup<IFeatureVector>> buildXcaliburPeakAssociationGroups(Eval e, ObjectContainer oc) {
        //categories
        ObjectSet<Chromatogram> c = e.getChromatograms(oc);
        Category[] cats = new Category[c.size()];
        HashMap<String, EntityGroup<IFeatureVector>> peakNameToEntityGroup = new HashMap<>();
        for (int i = 0; i < cats.length; i++) {
            cats[i] = new Category(c.get(i).getName());
            ObjectSet<Peak> os = e.getPeaksForChromatogramByCreator(oc, c.get(i), "xcalibur");
            for (Peak p : os) {
                Entity<IFeatureVector> ent = new Entity<IFeatureVector>(p, cats[i], p.getName());
                if (peakNameToEntityGroup.containsKey(p.getName())) {
                    EntityGroup<IFeatureVector> eg = peakNameToEntityGroup.get(p.getName());
                    eg.addEntity(cats[i], ent);
                } else {
                    EntityGroup<IFeatureVector> eg = new EntityGroup<>(ent);
                    peakNameToEntityGroup.put(p.getName(), eg);
                }
            }
        }
        ArrayList<EntityGroup<IFeatureVector>> al = new ArrayList<>(peakNameToEntityGroup.values());
        return al;
    }

    /**
     * Peak association in csv files is via row, header has names of files, used
     * as categories Format: FILE1\tFILE2\tFILE3... FEAT1_1\tFEAT2_1\tFEAT3_1...
     * FEAT1_2\tFEAT2_2\tFEAT3_2... ... where FEATY_X is the value of the
     * grouped feature,e.g. class label, time point or comparable things
     *
     * @param par
     * @return list of EntityGroups in order of row appearance in source file
     * par
     */
    public List<EntityGroup<PeakRTFeatureVector>> buildCSVPeakAssociationGroups(File par) {
        CSVReader csvr = new CSVReader();
        csvr.setFirstLineHeaders(true);
        try {
            Tuple2D<Vector<Vector<String>>, Vector<String>> table = csvr.read(new FileInputStream(par));
            Category[] c = new Category[table.getSecond().size()];
            for (int j = 0; j < c.length; j++) {
                String cstr = table.getSecond().get(j);
                cstr = cstr.replaceAll("\"", "");
                cstr = cstr.replaceAll("\'", "");
                c[j] = new Category(cstr);
            }
            //System.out.println("Using categories: "+Arrays.toString(c));
            int nrows = table.getFirst().size();
            ArrayList<EntityGroup<PeakRTFeatureVector>> al = new ArrayList<>();
            for (int i = 0; i < nrows; i++) {
                Entity<PeakRTFeatureVector>[] es = new Entity[c.length];
                for (int j = 0; j < es.length; j++) {
                    //System.out.println("Parsing cell "+i+" "+j);
                    double val = Double.NaN;
                    String cell = table.getFirst().get(i).get(j);
                    try {
                        val = Double.parseDouble(cell);
                    } catch (NumberFormatException nfe) {
                        //System.out.println("NumberFormatException on parsing: "+cell+" converting to "+val);
                    }
                    es[j] = new Entity<>(new PeakRTFeatureVector(val, Double.NaN), c[j], "" + i);
                }
                EntityGroup<PeakRTFeatureVector> eg = new EntityGroup<>(es);
                al.add(eg);
            }
            return al;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * Peak association in csv files is via row, header has names of files, used
     * as categories Format: FILE1\tFILE2\tFILE3... FEAT1_1\tFEAT2_1\tFEAT3_1...
     * FEAT1_2\tFEAT2_2\tFEAT3_2... ... where FEATY_X is the value of the
     * grouped feature,e.g. class label, time point or comparable things
     *
     * @param par
     * @param plet
     * @return list of EntityGroups in order of row appearance in source file
     * par
     */
    public List<EntityGroup<INamedPeakFeatureVector>> buildCSVPeak2DAssociationGroups(File par, ChromaTOFPeakListEntityTable<INamedPeakFeatureVector> plet) {
        CSVReader csvr = new CSVReader();
        csvr.setFirstLineHeaders(true);
        try {
            Tuple2D<Vector<Vector<String>>, Vector<String>> table = csvr.read(new FileInputStream(par));
            Category[] c = new Category[table.getSecond().size()];
            for (int j = 0; j < c.length; j++) {
                String cstr = table.getSecond().get(j);
                cstr = cstr.replaceAll("\"", "");
                cstr = cstr.replaceAll("\'", "");
                c[j] = new Category(cstr);
            }
            //System.out.println("Using categories: "+Arrays.toString(c));
            int nrows = table.getFirst().size();
            ArrayList<EntityGroup<INamedPeakFeatureVector>> al = new ArrayList<>();
            for (int i = 0; i < nrows; i++) {
                Entity<INamedPeakFeatureVector>[] es = new Entity[c.length];
                for (int j = 0; j < es.length; j++) {
                    //System.out.println("Parsing cell "+i+" "+j);
                    int rowIndex = -1;
                    String cell = table.getFirst().get(i).get(j);
//					System.out.println("Cell: "+cell);
                    try {
                        rowIndex = Integer.parseInt(cell);
                    } catch (NumberFormatException nfe) {
                        //System.out.println("NumberFormatException on parsing: "+cell+" converting to "+val);
                    }
//					System.out.println("RowIndex: "+rowIndex);
                    Entity<INamedPeakFeatureVector> e = new Entity(new Peak2DFeatureVector(null, rowIndex, Double.NaN, Double.NaN, Double.NaN), c[j], "" + i);
                    List<Entity<INamedPeakFeatureVector>> matches = plet.findMatching(e, Peak2DFeatureVector.FEATURE.ROWINDEX.name());
                    if (!matches.isEmpty()) {
                        Entity<INamedPeakFeatureVector> matching = matches.get(0);
//						System.out.println("Found matching entity: "+matching);
                        es[j] = matching;
                    } else {
                        es[j] = new Entity<INamedPeakFeatureVector>(new Peak2DFeatureVector(null, -1, Double.NaN, Double.NaN, Double.NaN), c[j], "" + i);;
                    }

                }
                EntityGroup<INamedPeakFeatureVector> eg = new EntityGroup<>(es);
                al.add(eg);
            }
            return al;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public void addGroup(List<EntityGroup<INamedPeakFeatureVector>> l, Entity<INamedPeakFeatureVector> lhs, Entity<INamedPeakFeatureVector> rhs) {
        int lhsRowIndex = ((IRowIndexNamedPeakFeatureVector) lhs.getFeatureVector()).getRowIndex();
        int rhsRowIndex = ((IRowIndexNamedPeakFeatureVector) rhs.getFeatureVector()).getRowIndex();

        for (EntityGroup<INamedPeakFeatureVector> eg : l) {
            Entity<INamedPeakFeatureVector> lhsTarget = eg.getEntityForCategory(lhs.getCategory());
            if (lhsTarget != null) {
                if (lhsTarget.getFeatureVector() instanceof IRowIndexNamedPeakFeatureVector) {
                    IRowIndexNamedPeakFeatureVector iri = (IRowIndexNamedPeakFeatureVector) lhsTarget.getFeatureVector();
                    if (iri.getRowIndex() == lhsRowIndex) {
                        //contained, so we can add rhs to the group
                        eg.addEntity(rhs.getCategory(), rhs);
                        return;
                    }
                } else {
                    throw new IllegalArgumentException("IFeatureVector must implement IRowIndexNamedPeakFeatureVector");
                }
            }
            Entity<INamedPeakFeatureVector> rhsTarget = eg.getEntityForCategory(rhs.getCategory());
            if (rhsTarget != null) {
                if (rhsTarget.getFeatureVector() instanceof IRowIndexNamedPeakFeatureVector) {
                    IRowIndexNamedPeakFeatureVector iri = (IRowIndexNamedPeakFeatureVector) rhsTarget.getFeatureVector();
                    if (iri.getRowIndex() == rhsRowIndex) {
                        //contained, so we can add rhs to the group
                        eg.addEntity(lhs.getCategory(), lhs);
                        return;
                    }
                } else {
                    throw new IllegalArgumentException("IFeatureVector must implement IRowIndexNamedPeakFeatureVector");
                }
            }
        }
        //otherwise we return a new entity group
        l.add(new EntityGroup<>(lhs, rhs));
    }

    /**
     * Peak association in csv files is via row, header has names of files, used
     * as categories Format: FILE1\tFILE2\tFILE3... FEAT1_1\tFEAT2_1\tFEAT3_1...
     * FEAT1_2\tFEAT2_2\tFEAT3_2... ... where FEATY_X is the value of the
     * grouped feature,e.g. class label, time point or comparable things
     *
     * @param plet
     * @param pal
     * @return list of EntityGroups in order of row appearance in source files
     * pal
     */
    public List<EntityGroup<INamedPeakFeatureVector>> buildMSPAPeak2DAssociationGroups(File baseDir, ChromaTOFPeakListEntityTable<INamedPeakFeatureVector> plet) {
        ArrayList<EntityGroup<INamedPeakFeatureVector>> al = new ArrayList<>();
        List<File> pal = new ArrayList<>(FileUtils.listFiles(baseDir, FileFilterUtils.prefixFileFilter("pa-"), FileFilterUtils.directoryFileFilter()));
        Collections.sort(pal, new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                String name1 = StringTools.removeFileExt(o1.getName());
                String name2 = StringTools.removeFileExt(o2.getName());
                int suff1 = Integer.parseInt(name1.substring(name1.indexOf("-") + 1));
                int suff2 = Integer.parseInt(name2.substring(name2.indexOf("-") + 1));
                return Integer.compare(suff1, suff2);
            }
        });
//		System.out.println("Entities for: "+baseDir.getName());
        Set<Category> orderedSet = new LinkedHashSet<>();
//		Map<Category, Map<Integer, Entity<INamedPeakFeatureVector>>> map = new LinkedHashMap<Category, Map<Integer, Entity<INamedPeakFeatureVector>>>();
        for (File f : pal) {
//			System.out.println("Processing file "+f.getName());
            CSVReader csvr = new CSVReader();
            csvr.setFirstLineHeaders(true);
            try {
                Tuple2D<Vector<Vector<String>>, Vector<String>> table = csvr.read(new FileInputStream(f));
                Category[] c = new Category[table.getSecond().size()];
                for (int j = 0; j < c.length; j++) {
                    String cstr = table.getSecond().get(j);
                    cstr = cstr.replaceAll("\"", "");
                    cstr = cstr.replaceAll("\'", "");
                    c[j] = new Category(cstr);
                    orderedSet.add(c[j]);
                }
                //System.out.println("Using categories: "+Arrays.toString(c));
                int nrows = table.getFirst().size();
                for (int i = 0; i < nrows; i++) {
                    List<Entity<INamedPeakFeatureVector>> entityGroup = new LinkedList<>();
                    Vector<String> row = table.getFirst().get(i);
//					System.out.println("Row "+i+" = "+row);
                    for (int j = 0; j < row.size(); j++) {
                        //System.out.println("Parsing cell "+i+" "+j);
                        int rowIndex = -1;
                        String cell = row.get(j);
                        //					System.out.println("Cell: "+cell);
                        try {
                            rowIndex = Integer.parseInt(cell);
                        } catch (NumberFormatException nfe) {
                            //System.out.println("NumberFormatException on parsing: "+cell+" converting to "+val);
                        }
//						System.out.println("Peak RowIndex: "+rowIndex);
                        Entity<INamedPeakFeatureVector> e = new Entity(new Peak2DFeatureVector(null, rowIndex, Double.NaN, Double.NaN, Double.NaN), c[j], "" + i);
                        List<Entity<INamedPeakFeatureVector>> matches = plet.findMatching(e, Peak2DFeatureVector.FEATURE.ROWINDEX.name());
                        if (!matches.isEmpty()) {
                            Entity<INamedPeakFeatureVector> matching = matches.get(0);
//							System.out.println("Found matching entity: "+matching+" from category "+matching.getCategory());
                            entityGroup.add(matching);
                        } else {
                            e = new Entity<INamedPeakFeatureVector>(new Peak2DFeatureVector(null, -1, Double.NaN, Double.NaN, Double.NaN), c[j], "" + i);
                            entityGroup.add(e);
                        }
                    }
                    if (entityGroup.size() != 2) {
                        throw new IllegalArgumentException();
                    }
                    addGroup(al, entityGroup.get(0), entityGroup.get(1));
//					System.out.println(entityGroup.get(0).getCategory()+"\t"+entityGroup.get(1).getCategory());
//					System.out.println(entityGroup);
                }

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
//		for(Category c:orderedSet) {
////			System.out.print(c+"\t");
//		}
//		System.out.println("");
        for (EntityGroup<INamedPeakFeatureVector> group : al) {
            for (Category c : orderedSet) {
                if (group.getEntityForCategory(c) == null) {
                    group.addEntity(c, new Entity<INamedPeakFeatureVector>(new Peak2DFeatureVector(null, -1, Double.NaN, Double.NaN, Double.NaN), c, "NA"));
//					System.out.print("-\t");
                }
//				int rowIndex = ((Peak2DFeatureVector)group.getEntityForCategory(c).getFeatureVector()).getRowIndex();
//				System.out.print((rowIndex==-1?"-":rowIndex)+"\t");
            }
//			System.out.println("");
        }
        return al;
    }

    /**
     * Peak association in csv files via column, first element in row has name
     * of file Format: FILE1\tFEAT1_1\tFEAT2_1... FILE2\tFEAT1_2\tFEAT2_2...
     * FILE3\tFEAT1_3\tFEAT2_3... ... where FEATY_X is the value of the grouped
     * feature,e.g. class label, time point or comparable things
     *
     * @param par
     * @return list of EntityGroups in order of row appearance in source file
     * par
     */
    public List<EntityGroup<PeakRTFeatureVector>> buildCSVTablePeakAssociationGroups(File par) {
        CSVReader csvr = new CSVReader();
        csvr.setFirstLineHeaders(false);
        try {
            Tuple2D<Vector<Vector<String>>, Vector<String>> table = csvr.read(new FileInputStream(par));
            int nrows = table.getFirst().size();
            Category[] c = new Category[nrows];
            for (int i = 0; i < nrows; i++) {
                String cstr = table.getFirst().get(i).get(0);
                cstr = cstr.replaceAll("\"", "");
                cstr = cstr.replaceAll("\'", "");
                c[i] = new Category(cstr);
            }
            //System.out.println("Using categories: "+Arrays.toString(c));
            ArrayList<EntityGroup<PeakRTFeatureVector>> al = new ArrayList<>();
            int ncols = table.getFirst().get(0).size() - 1;
            for (int j = 0; j < ncols; j++) {
                Entity<PeakRTFeatureVector>[] es = new Entity[nrows];
                for (int i = 0; i < nrows; i++) {
                    double val = Double.NaN;
                    String cell = table.getFirst().get(i).get(j + 1);
                    try {
                        val = Double.parseDouble(cell) / 60.0d;
                    } catch (NumberFormatException nfe) {
                        //System.out.println("NumberFormatException on parsing: "+cell+" converting to "+val);
                    }
                    es[i] = new Entity<>(new PeakRTFeatureVector(val, Double.NaN), c[i], "" + i);
                }
                EntityGroup<PeakRTFeatureVector> eg = new EntityGroup<>(es);
                al.add(eg);
            }
            return al;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
