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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;


import maltcms.datastructures.ridge.Ridge;
import org.slf4j.LoggerFactory;

/**
 * <p>RankSorter class.</p>
 *
 * @author Nils Hoffmann
 * 
 */

public class RankSorter {
        
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RankSorter.class);

    private LinkedHashSet<String> features = new LinkedHashSet<>();

    /**
     * <p>Constructor for RankSorter.</p>
     *
     * @param l a {@link java.util.List} object.
     */
    public RankSorter(List<Rank<Ridge>> l) {
        LinkedHashSet<String> union = new LinkedHashSet<>();
        for (Rank<Ridge> r : l) {
            union.addAll(r.getFeatures());
        }
        LinkedHashSet<String> intersection = new LinkedHashSet<>();
        for (Rank<Ridge> r : l) {
            intersection.retainAll(r.getFeatures());
        }
        this.features = intersection;
    }

    /**
     * <p>sort.</p>
     *
     * @param l a {@link java.util.List} object.
     */
    public void sort(List<Rank<Ridge>> l) {
        List<String> ll = new LinkedList<>(this.features);
        //Collections.reverse(ll);
        log.info("Sorting by: ");
        for (String str : ll) {
            log.info(str + " ");
            Collections.sort(l, new RankComparator(str));
        }
    }

    /**
     * <p>sortToOrder.</p>
     *
     * @param features a {@link java.util.List} object.
     * @param l a {@link java.util.List} object.
     */
    public void sortToOrder(List<String> features, List<Rank<Ridge>> l) {
        List<String> ll = new LinkedList<>(features);
        Collections.reverse(ll);
        log.info("Sorting by: ");
        for (String str : ll) {
            log.info(str + " ");
            Collections.sort(l, new RankComparator(str));
        }
    }
}
