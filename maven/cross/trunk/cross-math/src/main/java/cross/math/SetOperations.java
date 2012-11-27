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
import java.util.HashSet;
import java.util.Set;

/**
 * Utilitiy methods for sets with common names.
 *
 * @author Nils Hoffmann
 */
public class SetOperations {

    public static <T> Set<T> newSet(Collection<? extends T> c) {
        return new HashSet<T>(c);
    }
    
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
        return union(complement(a, b), complement(b, a));
    }
}
