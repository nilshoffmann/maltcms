/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.math;

import java.util.Iterator;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public class Partition implements Iterator<Integer> {

    private final int size;
    private int count = 0;
    private int max;
    private Partition neighbor = null;

    public Partition(int size) {
        this.size = size;
        this.max = this.size;
    }

    public Partition(Partition neighbor, int size) {
        this(size);
        this.neighbor = neighbor;
    }

    @Override
    public boolean hasNext() {
        if (this.count < max) {
            return true;
        }
        return false;
    }

    @Override
    public Integer next() {
        //overflow/carry to next neighbor
        if (count + 1 == this.max) {
            //this.max--;
            this.count = 0;
            if (this.neighbor != null) {
                this.neighbor.next();
            }
            return this.count;
        } else {//increase counter
            return Integer.valueOf(count++);
        }
    }

    public Integer current() {
        return this.count;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int size() {
        return this.size;
    }
    
    public void reset() {
        this.count = 0;
    }
}
