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
package maltcms.datastructures.caches;

import java.util.LinkedList;

/**
 * Implementation of a RingBuffer datastructure e.g. for caches with fixed
 * capacity and automatic remove oldest entry semantics.
 *
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 */
public class RingBuffer<T> {

    private final LinkedList<T> buffer;
    private int capacity = 0;

    public RingBuffer(int capacity) {
        this.buffer = new LinkedList<T>();
        this.capacity = capacity;
    }

    public T oldest() {
        return buffer.peekLast();
    }

    public T current() {
        return buffer.peekFirst();
    }

    public T previous() {
        return buffer.get(1);
    }

    public T push(T d) {
        buffer.addFirst(d);
        if (buffer.size() == this.capacity + 1) {
            return buffer.pollLast();
        }
        return buffer.peekLast();
    }

    public T[] getBuffer(T[] t) {
        return this.buffer.toArray(t);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(buffer.size());
        for (int i = 0; i < buffer.size(); i++) {
            sb.append(buffer.get(i) + " ");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        RingBuffer<Double> rb = new RingBuffer<Double>(3);
        for (int i = 0; i < 10; i++) {
            rb.push(Double.valueOf(i + 1));
            if (i >= 3) {
                System.out.println("Current: " + rb.current() + " previous: "
                        + rb.previous() + " oldest: " + rb.oldest());
                System.out.println(rb);
            }
        }
    }
}
