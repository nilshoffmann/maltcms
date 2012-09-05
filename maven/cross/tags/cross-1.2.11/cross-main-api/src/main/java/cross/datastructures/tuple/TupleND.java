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
package cross.datastructures.tuple;

import java.io.Serializable;
import java.util.*;

/**
 * Abstract class for N-Tuples of type T, providing different utility methods,
 * to return all non-identical pairs (Ai,Aj), i!=j. (Ai,Aj) = (Aj,Ai) => i<j =>
 * (Ai,Aj) is preferred.
 *
 * @author Nils Hoffmann
 *
 * @param <T>
 */
public class TupleND<T extends Serializable> implements Collection<T>,
        Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3369621175169080132L;
    private final List<T> c;

    public TupleND(final Collection<T> c1) {
        this.c = new ArrayList<T>(c1);
    }

    public TupleND() {
        this.c = new ArrayList<T>();
    }

    public TupleND(final T... ts) {
        this.c = new ArrayList<T>(ts.length);
        for (final T t : ts) {
            this.c.add(t);
        }
    }

    @Override
    public synchronized boolean add(final T e) {
        return this.c.add(e);
    }

    @Override
    public synchronized boolean addAll(final Collection<? extends T> c1) {
        return this.c.addAll(c1);
    }

    @Override
    public void clear() {
        this.c.clear();
    }

    @Override
    public boolean contains(final Object o) {
        return this.c.contains(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c1) {
        return this.c.containsAll(c1);
    }

    public T get(final int n) {
        return this.c.get(n);
    }

    public Iterator<T> getIterator() {
        return this.c.iterator();
    }

    public ListIterator<T> getListIterator() {
        return this.c.listIterator();
    }

    public ListIterator<T> getListIterator(final int index) {
        return this.c.listIterator(index);
    }

    public int getNumberOfPairs() {
        return getSize() * (getSize() - 1) / 2;
    }

    public List<Tuple2D<T, T>> getPairs() {
        final ArrayList<Tuple2D<T, T>> al = new ArrayList<Tuple2D<T, T>>(
                getNumberOfPairs());
        // int size = getNumberOfPairs();
        int cnt = 1;
        for (int i = 0; i < this.c.size() - 1; i++) {
            for (int j = i + 1; j < this.c.size(); j++) {
                // System.out.println("Adding pair " + cnt + " of " + size);
                al.add(new Tuple2D<T, T>(this.c.get(i), this.c.get(j)));
                cnt++;
            }
        }

        return Collections.unmodifiableList(al);
    }

    public List<Tuple2D<T, T>> getPairsWithFirstElement() {
        final T first = this.c.get(0);
        final ArrayList<Tuple2D<T, T>> al = new ArrayList<Tuple2D<T, T>>(this.c
                .size() - 1);
        // int size = this.c.size() - 1;
        for (int i = 1; i < this.c.size(); i++) {
            // System.out.println("Adding pair " + i + " of " + size);
            al.add(new Tuple2D<T, T>(first, this.c.get(i)));
        }

        return Collections.unmodifiableList(al);
    }

    public List<Tuple2D<T, T>> getPairsWithLastElement() {
        final T last = this.c.get(this.c.size() - 1);
        final ArrayList<Tuple2D<T, T>> al = new ArrayList<Tuple2D<T, T>>(this.c
                .size() - 1);
        final int size = this.c.size() - 1;
        for (int i = 0; i < size; i++) {
            // System.out.println("Adding pair " + i + " of " + size);
            al.add(new Tuple2D<T, T>(last, this.c.get(i)));
        }

        return Collections.unmodifiableList(al);
    }

    public int getSize() {
        return this.c.size();
    }

    @Override
    public boolean isEmpty() {
        return this.c.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return getIterator();
    }

    public T remove(final int n) {
        return this.c.remove(n);
    }

    @Override
    public boolean remove(final Object o) {
        return this.c.remove(o);
    }

    @Override
    public boolean removeAll(final Collection<?> c1) {
        return this.c.removeAll(c1);
    }

    @Override
    public boolean retainAll(final Collection<?> c1) {
        return this.c.retainAll(c1);
    }

    public void setPairs(final Collection<Tuple2D<T, T>> coll) {
//		System.out.println("Clearing collection!");
        this.c.clear();
        final Iterator<Tuple2D<T, T>> iter = coll.iterator();
        final HashSet<T> hm = new HashSet<T>();
//		System.out.println("Adding new Pairs!");
        while (iter.hasNext()) {
            final Tuple2D<T, T> tuple = iter.next();
            if (!hm.contains(tuple.getFirst())) {
                this.c.add(tuple.getFirst());
                hm.add(tuple.getFirst());
            }
            if (!hm.contains(tuple.getSecond())) {
                this.c.add(tuple.getSecond());
                hm.add(tuple.getSecond());
            }
        }
    }

    @Override
    public int size() {
        return this.getSize();
    }

    @Override
    public Object[] toArray() {
        return this.c.toArray();
    }

    @Override
    public <T1> T1[] toArray(final T1[] a) {
        return this.c.toArray(a);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int last = size() - 1;
        int i = 0;
        for (T t : this) {
            sb.append(t.toString() + (i == last ? "" : ", "));
            i++;
        }
        sb.append("]");
        return sb.toString();
    }
}
