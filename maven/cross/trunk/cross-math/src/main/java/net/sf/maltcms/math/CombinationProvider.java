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

import java.util.ArrayList;
import java.util.List;
import net.sf.maltcms.datastructures.IElementProvider;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public class CombinationProvider implements IElementProvider<Object[]> {

    private CombinationIterator ci;
    private List<Object[]> data;
    private int lastIdx = -1;

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
        if(idx>size()) {
            throw new IndexOutOfBoundsException();
        }
//        System.out.println("LastIdx: "+lastIdx+" currentIdx: "+idx);
        if(Math.abs(idx-lastIdx)>1 || idx<=lastIdx) {
            ci.reset();
            for(int i = 0;i<idx;i++) {
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

    /* (non-Javadoc)
     * @see net.sf.maltcms.datastructures.IElementProvider#get(int, int)
     */
    @Override
    public List<Object[]> get(int start, int stop) {
        List<Object[]> l = new ArrayList<Object[]>();
        l.add(get(start));
        return l;
    }

    @Override
    public void reset() {
        lastIdx = -1;
        ci.reset();
    }
}
