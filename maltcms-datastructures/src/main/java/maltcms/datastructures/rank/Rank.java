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
package maltcms.datastructures.rank;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import maltcms.datastructures.ridge.Ridge;

/**
 * <p>Rank class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class Rank<T extends Ridge> implements Comparable<Rank<? extends T>> {

    private final HashMap<String, Double> featureToRank = new LinkedHashMap<>();
    private final T t;

    /**
     * <p>Constructor for Rank.</p>
     *
     * @param t a T object.
     */
    public Rank(T t) {
        this.t = t;
        //addRank("cwtResponse",t.getRidgePoints().get(0).getSecond());
    }

    /**
     * <p>getRidge.</p>
     *
     * @return a T object.
     */
    public T getRidge() {
        return this.t;
    }

    /**
     * <p>addRank.</p>
     *
     * @param feature a {@link java.lang.String} object.
     * @param rank a double.
     */
    public void addRank(String feature, double rank) {
        featureToRank.put(feature, rank);
    }

    /**
     * <p>getRank.</p>
     *
     * @param feature a {@link java.lang.String} object.
     * @return a {@link java.lang.Double} object.
     */
    public Double getRank(String feature) {
        return featureToRank.get(feature);
    }

    /**
     * <p>getFeatures.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getFeatures() {
        return featureToRank.keySet();
    }

    /**
     * <p>getCommonFeatures.</p>
     *
     * @param r a {@link maltcms.datastructures.rank.Rank} object.
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getCommonFeatures(Rank<? extends T> r) {
        HashSet<String> hs = new HashSet<>(featureToRank.keySet());
        hs.retainAll(r.getFeatures());
        return hs;
    }

    /**
     * <p>getFeatureRanks.</p>
     *
     * @param r a {@link maltcms.datastructures.rank.Rank} object.
     * @return an array of double.
     */
    public double[] getFeatureRanks(Rank<? extends T> r) {
        Set<String> features = getCommonFeatures(r);
        double[] vals = new double[2];
        for (String s : features) {
            vals[0] += getRank(s);
            vals[1] += r.getRank(s);
        }
        return vals;
    }

    /**
     * <p>compareRanks.</p>
     *
     * @param feature a {@link java.lang.String} object.
     * @param r a {@link maltcms.datastructures.rank.Rank} object.
     * @return a int.
     */
    public int compareRanks(String feature, Rank<? extends T> r) {
        double[] vals = new double[2];
        Set<String> features = getCommonFeatures(r);
        if (features.isEmpty()) {
            return -1;
        }
        vals[0] = getRank(feature);
        vals[1] = r.getRank(feature);
        return Double.compare(vals[0], vals[1]);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key : getFeatures()) {
            sb.append("[" + key + ":" + getRank(key) + "]");
        }
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Rank<? extends T> o) {
        List<String> keys = new LinkedList<>(featureToRank.keySet());
        Collections.reverse(keys);
        for (String key : keys) {
            int cmp = compareRanks(key, o);
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    public static class ResponseComparator<T extends Ridge> implements Comparator<Rank<? extends T>> {

        @Override
        public int compare(Rank<? extends T> o1, Rank<? extends T> o2) {
            return o1.getRidge().compareTo(o2.getRidge());
        }
    }
}
