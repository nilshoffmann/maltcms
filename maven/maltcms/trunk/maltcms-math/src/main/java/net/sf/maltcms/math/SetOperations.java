/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
