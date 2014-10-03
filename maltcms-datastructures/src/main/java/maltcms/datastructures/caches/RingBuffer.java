/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.datastructures.caches;

import java.util.LinkedList;

/**
 * Implementation of a RingBuffer datastructure e.g. for caches with fixed
 * capacity and automatic remove oldest entry semantics.
 *
 * @author Nils Hoffmann
 * 
 */
public class RingBuffer<T> {

    private final LinkedList<T> buffer;
    private int capacity = 0;

    /**
     * <p>Constructor for RingBuffer.</p>
     *
     * @param capacity a int.
     */
    public RingBuffer(int capacity) {
        this.buffer = new LinkedList<>();
        this.capacity = capacity;
    }

    /**
     * <p>oldest.</p>
     *
     * @return a T object.
     */
    public T oldest() {
        return buffer.peekLast();
    }

    /**
     * <p>current.</p>
     *
     * @return a T object.
     */
    public T current() {
        return buffer.peekFirst();
    }

    /**
     * <p>previous.</p>
     *
     * @return a T object.
     */
    public T previous() {
        return buffer.get(1);
    }

    /**
     * <p>push.</p>
     *
     * @param d a T object.
     * @return a T object.
     */
    public T push(T d) {
        buffer.addFirst(d);
        if (buffer.size() == this.capacity + 1) {
            return buffer.pollLast();
        }
        return buffer.peekLast();
    }

    /**
     * <p>Getter for the field <code>buffer</code>.</p>
     *
     * @param t an array of T objects.
     * @return an array of T objects.
     */
    public T[] getBuffer(T[] t) {
        return this.buffer.toArray(t);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(buffer.size());
        for (T buffer1 : buffer) {
            sb.append(buffer1 + " ");
        }
        return sb.toString();
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        RingBuffer<Double> rb = new RingBuffer<>(3);
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
