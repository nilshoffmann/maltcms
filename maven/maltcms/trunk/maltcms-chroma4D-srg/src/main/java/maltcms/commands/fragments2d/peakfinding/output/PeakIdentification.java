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
package maltcms.commands.fragments2d.peakfinding.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.peak.Peak2D;
import maltcms.db.MetaboliteQueryDB;
import maltcms.tools.ArrayTools2;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;

import com.db4o.ObjectSet;

import cross.annotations.Configurable;
import cross.datastructures.tuple.Tuple2D;
import java.util.concurrent.ExecutionException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.db.QueryCallable;
import maltcms.db.predicates.metabolite.DBMatch;
import maltcms.db.predicates.metabolite.MetaboliteSimilarity;
import maltcms.math.functions.IArraySimilarity;
import maltcms.math.functions.similarities.ArrayCos;

/**
 * Will do the identification.
 *
 * @author Mathias Wilhelm
 */
@Slf4j
@Data
public class PeakIdentification implements IPeakIdentification {

    private MetaboliteQueryDB mqdb;
//    private ObjectContainer oc;
//    private List<ObjectContainer> ocl = new ArrayList<ObjectContainer>();
    @Configurable(value = "false", type = boolean.class)
    private boolean doSearch = false;
    @Configurable(type = String.class)
    private String dbFile = null;
    private List<String> dbFiles = new ArrayList<String>();
    @Configurable(name = "dbThreshold", value = "0.9d", type = double.class)
    private double threshold = 0.08d;
    @Configurable(name = "kBest", value = "1", type = int.class)
    private int k = 1;
    private boolean dbAvailable = true;
    private IArraySimilarity similarity = new ArrayCos();
    private List<Integer> masqMasses = new ArrayList<Integer>();
//    private ObjectSet<IMetabolite> dbMetabolites = null;
    private List<ObjectSet<IMetabolite>> dbMetabolitesList = new ArrayList<ObjectSet<IMetabolite>>();

    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        // this.doDBSearch = cfg.getBoolean(this.getClass().getName()
        // + ".doDBSearch", false);
        this.dbFile = cfg.getString(this.getClass().getName() + ".dbFile", null);
        for (String single : cfg.getStringArray(
                this.getClass().getName() + ".dbFile")) {
            this.dbFiles.add(single);
        }
        this.threshold = cfg.getDouble(this.getClass().getName()
                + ".dbThreshold", 0.8d);
        this.doSearch = cfg.getBoolean(this.getClass().getName() + ".doSearch",
                false);
        this.k = cfg.getInteger(this.getClass().getName() + ".kBest", 1);
        for (String single : cfg.getStringArray(this.getClass().getName()
                + ".masq")) {
            try {
                masqMasses.add(Integer.parseInt(single));
            } catch (NumberFormatException e) {
                log.info("Can not parse " + single + " to an integer");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(final Peak2D peak) {
        if (this.dbFiles.isEmpty()) {
            this.dbAvailable = false;
        }
        if (this.dbAvailable && this.doSearch) {
//            if (this.ocl.isEmpty()) {
            List<DBMatch> hits = new ArrayList<DBMatch>();
            for (String dbf : this.dbFiles) {
                final ArrayDouble.D1 massValues = new ArrayDouble.D1(peak.
                        getPeakArea().getSeedMS().getShape()[0]);
                final IndexIterator iter = massValues.getIndexIterator();
                int c = 0;
                while (iter.hasNext()) {
                    iter.setIntNext(c++);
                }
                Array intensityValues = prepareMS(peak.getPeakArea().getSeedMS());
                MetaboliteSimilarity ms = new MetaboliteSimilarity(massValues,
                        intensityValues,
                        threshold, k, false);
                ms.setSimilarityFunction(similarity);
                MetaboliteQueryDB mqdb = new MetaboliteQueryDB(dbf, ms);
                QueryCallable<IMetabolite> qc = mqdb.getCallable();
                ObjectSet<IMetabolite> osRes = null;
                try {
                    osRes = qc.call();
                    log.info("Received {} hits from ObjectSet!",
                            osRes.size());
                    for (Tuple2D<Double, IMetabolite> t : ms.getMatches()) {
                        hits.add(new DBMatch(dbf, t.getFirst(), t.getSecond()));
                    }
                    qc.terminate();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            Collections.sort(hits);
//            }

            if (hits != null) {
                peak.setNames(DBMatch.asMatchList(hits));
            }
        }
    }

    private Array prepareMS(Array ms) {
        ms = ArrayTools2.createIntegerArray(ms, this.masqMasses);
        return ms;
    }
}
