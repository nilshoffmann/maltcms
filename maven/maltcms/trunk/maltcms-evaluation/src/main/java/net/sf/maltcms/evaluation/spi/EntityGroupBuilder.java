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
package net.sf.maltcms.evaluation.spi;

import net.sf.maltcms.evaluation.spi.hohenheim.Eval;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;


import cross.datastructures.tuple.Tuple2D;
import maltcms.io.csv.CSVReader;
import net.sf.maltcms.evaluation.api.classification.Category;
import net.sf.maltcms.evaluation.api.classification.Entity;
import net.sf.maltcms.evaluation.api.classification.EntityGroup;
import net.sf.maltcms.evaluation.spi.classification.PeakRTFeatureVector;
import net.sf.maltcms.evaluation.spi.xcalibur.Chromatogram;
import net.sf.maltcms.evaluation.spi.xcalibur.Peak;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
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
    public List<EntityGroup> buildXcaliburPeakAssociationGroups(Eval e, ObjectContainer oc) {
        //categories
        ObjectSet<Chromatogram> c = e.getChromatograms(oc);
        Category[] cats = new Category[c.size()];
        HashMap<String, EntityGroup> peakNameToEntityGroup = new HashMap<String, EntityGroup>();
        for (int i = 0; i < cats.length; i++) {
            cats[i] = new Category(c.get(i).getName());
            ObjectSet<Peak> os = e.getPeaksForChromatogramByCreator(oc, c.get(i), "xcalibur");
            for (Peak p : os) {
                Entity ent = new Entity(p, cats[i], p.getName());
                if (peakNameToEntityGroup.containsKey(p.getName())) {
                    EntityGroup eg = peakNameToEntityGroup.get(p.getName());
                    eg.addEntity(cats[i], ent);
                } else {
                    EntityGroup eg = new EntityGroup(ent);
                    peakNameToEntityGroup.put(p.getName(), eg);
                }
            }
        }
        ArrayList<EntityGroup> al = new ArrayList<EntityGroup>(peakNameToEntityGroup.values());
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
    public List<EntityGroup> buildCSVPeakAssociationGroups(File par) {
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
            ArrayList<EntityGroup> al = new ArrayList<EntityGroup>();
            for (int i = 0; i < nrows; i++) {
                Entity[] es = new Entity[c.length];
                for (int j = 0; j < es.length; j++) {
                    //System.out.println("Parsing cell "+i+" "+j);
                    double val = Double.NaN;
                    String cell = table.getFirst().get(i).get(j);
                    try {
                        val = Double.parseDouble(cell);
                    } catch (NumberFormatException nfe) {
                        //System.out.println("NumberFormatException on parsing: "+cell+" converting to "+val);
                    }
                    es[j] = new Entity(new PeakRTFeatureVector(val), c[j], "" + i);
                }
                EntityGroup eg = new EntityGroup(es);
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
     * Peak association in csv files via column, first element in row has name
     * of file Format: FILE1\tFEAT1_1\tFEAT2_1... FILE2\tFEAT1_2\tFEAT2_2...
     * FILE3\tFEAT1_3\tFEAT2_3... ... where FEATY_X is the value of the grouped
     * feature,e.g. class label, time point or comparable things
     *
     * @param par
     * @return list of EntityGroups in order of row appearance in source file
     * par
     */
    public List<EntityGroup> buildCSVTablePeakAssociationGroups(File par) {
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
            ArrayList<EntityGroup> al = new ArrayList<EntityGroup>();
            int ncols = table.getFirst().get(0).size() - 1;
            for (int j = 0; j < ncols; j++) {
                Entity[] es = new Entity[nrows];
                for (int i = 0; i < nrows; i++) {
                    double val = Double.NaN;
                    String cell = table.getFirst().get(i).get(j + 1);
                    try {
                        val = Double.parseDouble(cell) / 60.0d;
                    } catch (NumberFormatException nfe) {
                        //System.out.println("NumberFormatException on parsing: "+cell+" converting to "+val);
                    }
                    es[i] = new Entity(new PeakRTFeatureVector(val), c[i], "" + i);
                }
                EntityGroup eg = new EntityGroup(es);
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
