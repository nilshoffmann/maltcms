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
