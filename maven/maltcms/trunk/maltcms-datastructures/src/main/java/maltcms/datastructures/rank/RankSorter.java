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
package maltcms.datastructures.rank;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import maltcms.datastructures.ridge.Ridge;

public class RankSorter {

    private LinkedHashSet<String> features = new LinkedHashSet<String>();

    public RankSorter(List<Rank<Ridge>> l) {
        LinkedHashSet<String> union = new LinkedHashSet<String>();
        for (Rank<Ridge> r : l) {
            union.addAll(r.getFeatures());
        }
        LinkedHashSet<String> intersection = new LinkedHashSet<String>();
        for (Rank<Ridge> r : l) {
            intersection.retainAll(r.getFeatures());
        }
        this.features = intersection;
    }

    public void sort(List<Rank<Ridge>> l) {
        List<String> ll = new LinkedList<String>(this.features);
        //Collections.reverse(ll);
        System.out.println("Sorting by: ");
        for (String str : ll) {
            System.out.print(str + " ");
            Collections.sort(l, new RankComparator(str));
        }
        System.out.println();
    }

    public void sortToOrder(List<String> features, List<Rank<Ridge>> l) {
        List<String> ll = new LinkedList<String>(features);
        Collections.reverse(ll);
        System.out.println("Sorting by: ");
        for (String str : ll) {
            System.out.print(str + " ");
            Collections.sort(l, new RankComparator(str));
        }
        System.out.println();
    }
}
