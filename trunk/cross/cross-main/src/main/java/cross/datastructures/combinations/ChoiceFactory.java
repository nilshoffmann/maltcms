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
package cross.datastructures.combinations;

import cross.datastructures.collections.CachedLazyList;
import cross.datastructures.collections.IElementProvider;
import cross.math.CombinationIterator;
import cross.math.Partition;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class ChoiceFactory {
    /**
     * Returns a lazily instantiated list (allows for an almost arbitrary number
     * of combinations< {@link Integer.maxValue()}) of all unique object
     * combinations, s.t. for (a,b)=(b,a) only (a,b) is returned. The list is
     * backed by a {@link CombinationProvider} wrapping a {@link
     * CombinationIterator} to feed a {@link CachedLazyList}.
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
//            ChoiceFactory.log.debug("|Partition {}| = {}; contents = {}", new Object[]{i, partitionSize[i], Arrays.toString(data.get(i))});
        }

        CombinationIterator pi = new CombinationIterator(parts);
//        ChoiceFactory.log.info("No. of choices: {}", pi.size());
        // list holding returned choices
        IElementProvider<Object[]> iep = new CombinationProvider(pi, data);
        List<Object[]> l = CachedLazyList.getList(iep);//new ArrayList<Object[]>();
        return l;
    }
}
