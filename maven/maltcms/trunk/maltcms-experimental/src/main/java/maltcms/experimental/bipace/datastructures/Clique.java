/**
 * 
 */
package maltcms.experimental.bipace.datastructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import maltcms.commands.ArrayStatsMap;
import maltcms.datastructures.array.IFeatureVector;


import maltcms.datastructures.array.IMutableFeatureVector;
import maltcms.experimental.bipace.datastructures.api.IClique;
import maltcms.experimental.bipace.datastructures.api.ICliqueMemberCriterion;
import maltcms.experimental.bipace.datastructures.api.ICliqueUpdater;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
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

    @Override
    public long getID() {
        return this.id;
    }

    @Override
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

    @Override
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
    @Override
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
    @Override
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

    @Override
    public List<T> getFeatureVectorList() {
        List<T> peaks = new ArrayList<T>(this.clique);
        Collections.sort(peaks, comp);
        return peaks;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Clique<T> other = (Clique<T>) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.icmc != other.icmc && (this.icmc == null || !this.icmc.equals(other.icmc))) {
            return false;
        }
        if (this.icu != other.icu && (this.icu == null || !this.icu.equals(other.icu))) {
            return false;
        }
        if (this.comp != other.comp && (this.comp == null || !this.comp.equals(other.comp))) {
            return false;
        }
        return true;
    }

    @Override
    public int size() {
        return this.clique.size();
    }
}
