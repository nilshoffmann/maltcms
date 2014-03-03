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
package maltcms.datastructures.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.array.IMutableFeatureVector;

/**
 * @author Nils Hoffmann
 *
 *
 */
@Slf4j
public class Clique<T extends IFeatureVector> implements IClique<T> {

    private static long CLIQUEID = 0;
    private long id = -1;
    private HashSet<T> clique = new HashSet<T>();
    private T centroid = null;
    private ICliqueMemberCriterion<T> icmc;
    private ICliqueUpdater<T> icu;
    private Comparator<T> comp;
    private IMutableFeatureVector statsMap = new ArrayStatsMap();

    @Override
    public IMutableFeatureVector getArrayStatsMap() {
        return statsMap;
    }

    public Clique(Comparator<T> comp, ICliqueMemberCriterion<T> icmc,
        ICliqueUpdater<T> icu) {
        this.id = ++CLIQUEID;
        this.comp = comp;
        this.icmc = icmc;
        this.icu = icu;
    }

    public long getID() {
        return this.id;
    }

    public boolean add(T p) throws IllegalArgumentException {
        if (clique.contains(p)) {
            log.debug("Peak {} already contained in clique!", p);
            return false;
        } else {
            // if (clique.isEmpty()) {

            // check bidi best hit assumption
            // bail out if assumption fails!
            if (!icmc.shouldBeMemberOf(this, p)) {
                return false;
            }
            icu.update(this, p);
            log.debug("Adding element {} to clique", p);
            clique.add(p);
            icu.setCentroid(this);
            return true;

        }
    }

    public void clear() {
        centroid = null;
        clique.clear();
    }

    // public BoxAndWhiskerItem createRTBoxAndWhisker() {
    // List<Double> l = new ArrayList<Double>();
    // for (IFeatureVector p : this.clique) {
    // l.add(centroid.sat - p.sat);
    // }
    // return BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(l);
    // }
    //
    // public BoxAndWhiskerItem createApexTicBoxAndWhisker() {
    // List<Double> l = new ArrayList<Double>();
    // for (IFeatureVector p : this.clique) {
    // l.add(Math.log(ArrayTools.integrate(centroid.msIntensities))
    // - ArrayTools.integrate(p.msIntensities));
    // }
    // return BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(l);
    // }
    public void setCentroid(T ifv) {
        this.centroid = ifv;
    }

    // public double getCliqueRTVariance() {
    // return this.cliqueVar;
    // }
    //
    // public double getCliqueRTMean() {
    // return this.cliqueMean;
    // }
    public T getCliqueCentroid() {
        return this.centroid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.centroid != null) {
            sb.append("Center: " + this.centroid.toString() + "\n");
        } else {
            sb.append("Center: null\n");
        }
        for (IFeatureVector p : this.clique) {
            if (p != null) {
                sb.append(p.toString());
            } else {
                sb.append("null");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public List<T> getFeatureVectorList() {
        List<T> peaks = new ArrayList<T>(this.clique);
        Collections.sort(peaks, comp);
        return peaks;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public int size() {
        return this.clique.size();
    }
}
