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
