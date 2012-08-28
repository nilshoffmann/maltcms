/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.db.search.api.similarities;

import com.db4o.query.Predicate;
import cross.datastructures.cache.CacheFactory;
import cross.datastructures.cache.ICacheDelegate;
import cross.datastructures.tuple.Tuple2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.ms.IScan;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import net.sf.maltcms.db.search.api.IMatchPredicate;
import ucar.ma2.Array;

/**
 *
 * @author nilshoffmann
 */
public abstract class AMetabolitePredicate extends Predicate<IMetabolite> implements IMatchPredicate<IMetabolite> {

    private double scoreThreshold;
    private int maxHits = 1;
    private IScan scan;
    private List<Double> maskedMasses;
    private TreeMap<Double, Integer> spectrum;
    private TreeSet<Tuple2D<Double, IMetabolite>> scoreMap = null;

    public TreeSet<Tuple2D<Double, IMetabolite>> getScoreMap() {
        if (scoreMap == null) {
            scoreMap = new TreeSet<Tuple2D<Double, IMetabolite>>(getComparator());
        }
        return scoreMap;
    }

    public abstract Comparator<Tuple2D<Double, IMetabolite>> getComparator();
    private ICacheDelegate<IMetabolite, TreeMap<Double, Integer>> cache = CacheFactory.createDefaultCache(getClass().getName());

    public Collection<Tuple2D<Double, IMetabolite>> getMetabolites() {
        ArrayList<Tuple2D<Double, IMetabolite>> results = new ArrayList<Tuple2D<Double, IMetabolite>>();
        System.out.println("Adding " + getScoreMap().size() + " hits!");
        Iterator<Tuple2D<Double, IMetabolite>> iter = getScoreMap().iterator();
        while (iter.hasNext()) {
            results.add(iter.next());
        }
        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.subList(0, Math.min(maxHits, results.size()));
    }

    public Array filterMaskedMasses(Array masses, Array intensities) {
        if (getMaskedMasses() == null || getMaskedMasses().isEmpty()) {
            return intensities;
        }
        List<Integer> l1 = MaltcmsTools.findMaskedMasses(masses, getMaskedMasses(), 0);
        return ArrayTools.filterIndices(intensities, l1, 0.0d);
    }

    public List<Double> getMaskedMasses() {
        return maskedMasses;
    }

    public void setMaskedMasses(List<Double> masses) {
        this.maskedMasses = masses;
    }

    public int getMaxHits() {
        return maxHits;
    }

    public void setMaxHits(int maxHits) {
        this.maxHits = maxHits;
    }

    public double getScoreThreshold() {
        return scoreThreshold;
    }

    public void setScoreThreshold(double scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    public IScan getScan() {
        return scan;
    }

    public void setScan(IScan scan) {
        this.scan = scan;
        spectrum = new TreeMap<Double, Integer>();
        for (int i = 0; i < scan.getMasses().getShape()[0]; i++) {
            spectrum.put(scan.getMasses().getDouble(i), scan.getIntensities().getInt(i));
        }
    }

    public TreeMap<Double, Integer> getSpectrum() {
        return this.spectrum;
    }

    public ICacheDelegate<IMetabolite, TreeMap<Double, Integer>> getCache() {
        return cache;
    }

    public abstract AMetabolitePredicate copy();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
