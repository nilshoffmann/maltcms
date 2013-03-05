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

import java.util.ArrayList;
import java.util.List;
import cross.datastructures.collections.IElementProvider;
import cross.math.CombinationIterator;

/**
 * Implementation of {@link IElementProvider} for
 * <code>Object[]</code>. Use this class in ascending iteration order 
 * to achieve the best performance.
 *
 * @author Nils Hoffmann
 *
 */
public class CombinationProvider implements IElementProvider<Object[]> {

    private CombinationIterator ci;
    private List<Object[]> data;
    private long lastIdx = -1;

    public CombinationProvider(CombinationIterator ci, List<Object[]> data) {
        this.ci = ci;
        this.data = data;
    }

    /* (non-Javadoc)
     * @see net.sf.maltcms.datastructures.IElementProvider#size()
     */
    @Override
    public int size() {
        return (int) ci.size();
    }

    /* (non-Javadoc)
     * @see net.sf.maltcms.datastructures.IElementProvider#get(int)
     */
    @Override
    public Object[] get(int idx) {
        return get((long) idx);
    }

    /* (non-Javadoc)
     * @see net.sf.maltcms.datastructures.IElementProvider#get(int, int)
     */
    @Override
    public List<Object[]> get(int start, int stop) {
        return get((int) start, (int) stop);
    }

    @Override
    public void reset() {
        lastIdx = -1;
        ci.reset();
    }

    @Override
    public long sizeLong() {
        return ci.size();
    }

    @Override
    public Object[] get(long idx) {
        if (idx > size()) {
            throw new IndexOutOfBoundsException();
        }
//        System.out.println("LastIdx: "+lastIdx+" currentIdx: "+idx);
        if (Math.abs(idx - lastIdx) > 1 || idx <= lastIdx) {
            ci.reset();
            for (int i = 0; i < idx; i++) {
                get(i);
            }
            //throw new IllegalArgumentException("Random access is not implemented!");
        }
        int[] elementCombination = ci.next();
        lastIdx = idx;
//         = ci.next();
        Object[] s = new Object[elementCombination.length];
        for (int i = 0; i < elementCombination.length; i++) {
            s[i] = data.get(i)[elementCombination[i]];
        }
        return s;
    }

    @Override
    public List<Object[]> get(long start, long stop) {
        List<Object[]> l = new ArrayList<Object[]>();
        for (long i = start; i <= stop; i++) {
            l.add(get(i));
        }
        return l;
    }
}
