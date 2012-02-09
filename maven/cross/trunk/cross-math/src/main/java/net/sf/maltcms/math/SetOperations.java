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
package net.sf.maltcms.math;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public class SetOperations {

    public static <T> Set<T> union(Set<T> a, Set<T> b) {
        Set<T> union = new HashSet<T>(a);
        union.addAll(b);
        return union;
    }

    public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        Set<T> inters = new HashSet<T>(a);
        inters.retainAll(b);
        return inters;
    }

    public static <T> Set<T> complement(Set<T> a, Set<T> b) {
        Set<T> a1 = new HashSet<T>(a);
        a1.removeAll(b);
        return a1;
    }

    public static <T> Set<T> symmetricDifference(Set<T> a, Set<T> b) {
        return union(complement(a,b),complement(b,a));
    }
}
