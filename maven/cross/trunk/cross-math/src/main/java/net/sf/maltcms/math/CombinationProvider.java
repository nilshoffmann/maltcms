/**
 * 
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
