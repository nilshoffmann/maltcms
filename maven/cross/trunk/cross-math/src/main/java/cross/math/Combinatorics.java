/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.math;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import cross.datastructures.collections.CachedLazyList;
import cross.datastructures.collections.IElementProvider;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class with methods for combinatorics.
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class Combinatorics {

    private static double[] faculty = null;

    public static String[][] toStringArray(Map<String, List<String>> lh) {
        String[][] s = new String[lh.size()][];
        List<String> keys = new LinkedList<String>(lh.keySet());
        for (int i = 0; i < s.length; i++) {
            List<String> ls = lh.get(keys.get(i));
            s[i] = ls.toArray(new String[ls.size()]);
        }
        return s;
    }

    public static List<Object[]> toObjectArray(Map<String, ?> lh) {
        List<Object[]> s = new LinkedList<Object[]>();
        List<String> keys = new LinkedList<String>(lh.keySet());
        for (int i = 0; i < lh.size(); i++) {
            Object ls = lh.get(keys.get(i));
            if (ls instanceof Collection) {
                Collection<?> list = (Collection<?>) ls;
                s.add(list.toArray(new Object[list.size()]));
            } else {
                s.add(new Object[]{ls});
            }
        }
        return s;
    }

    /**
     * Returns a lazily instantiated list (allows for an almost arbitrary number
     * of combinations< {@code Integer.maxValue()}) of all unique object
     * combinations, s.t. for (a,b)=(b,a) only (a,b) is returned. The list is
     * backed by a {@see CombinationProvider} wrapping a {@see
     * CombinationIterator} to feed a {@see CachedLazyList}.
     *

     *
     * @param data
     * @return
     */
    public static List<Object[]> getKPartiteChoices(List<Object[]> data) {
        //element counter
//        int nelements = 0;
        int[] partitionSize = new int[data.size()];
        Partition[] parts = new Partition[data.size()];
        // store partition size
        // and partition
        // allowed combinations are enumerated by
        // treating each individual partition p as a 
        // base |p| number register. 
        // While enumerating, the current value of the right-most
        // partition/register is increased until its maximum is reached.
        // It then carries over to the next neighbor, who is also increased.
        // The counting continues until the maximum number of possible choices
        // is reached, which is \PI_{i=0}^{k}|p_{i}| (the product of all parition
        // sizes)
        for (int i = 0; i < data.size(); i++) {
            partitionSize[i] = data.get(i).length;
//            nelements += partitionSize[i];
            if (i > 0) {
                parts[i] = new Partition(parts[i - 1], partitionSize[i]);
            } else {
                parts[i] = new Partition(partitionSize[i]);
            }
            log.debug("|Partition {}| = {}; contents = {}", new Object[]{i, partitionSize[i], Arrays.toString(data.get(i))});
        }

        CombinationIterator pi = new CombinationIterator(parts);
        log.info("No. of choices: {}", pi.size());
        // list holding returned choices
        IElementProvider<Object[]> iep = new CombinationProvider(pi, data);
        List<Object[]> l = CachedLazyList.getList(iep);//new ArrayList<Object[]>();
        return l;
    }

    /**
     * Faculty function. Below 171!, the exact value is returned. Otherwise, the
     * approximate faculty is returned faculty(n) := (n+0.5)*ln(n) - n +
     * ln(2PI)/2
     *
     * @param n
     * @return
     */
    public static double faculty(int n) {
        if (n >= 171) {
            return (n + 0.5d) * Math.log(n) - n + Math.log(2 * Math.PI) / 2.0d;
        }
        if (faculty == null) {
            faculty = new double[171];
            faculty[0] = 1;
            for (int i = 1; i < faculty.length; i++) {
                faculty[i] = faculty[i - 1] * i;
                // if(faculty[i]==Double.POSITIVE_INFINITY){
                // System.out.println(i+" "+faculty[i]);
                // }
            }
        }
        return faculty[n];
    }
}
