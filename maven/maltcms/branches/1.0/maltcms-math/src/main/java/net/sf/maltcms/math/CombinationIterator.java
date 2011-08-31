/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.math;

import java.util.Iterator;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public class CombinationIterator implements Iterator<int[]> {

    private final Partition[] partitions;
    private long size = -1;
    private long cnt = 0;

    public CombinationIterator(Partition... p) {
        this.partitions = p;
        this.size = size();
    }

    @Override
    public boolean hasNext() {
        if (cnt < size) {
            return true;
        }
        return false;
    }

    @Override
    public int[] next() {
        int[] ret = new int[this.partitions.length];
        for (int i = 0; i < ret.length; i++) {
//            Partition p = partitions[i];
            ret[i] = partitions[i].current().intValue();
        }
        partitions[this.partitions.length - 1].next();
        cnt++;
        return ret;
    }

    @Override
    public void remove() {  
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long size() {
//        System.out.print("Size: ");
        if (this.size == -1) {
            long size = 1;
            for (Partition p : partitions) {
//                System.out.print(p.size()+"x");
                size *= p.size();
            }
            this.size = size;
        }
//        System.out.print(" = "+this.size+"\n");
        return size;
    }
    
    public void reset() {
        this.cnt = 0;
        for(Partition p:partitions) {
            p.reset();
        }
    }
}
