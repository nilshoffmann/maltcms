/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: CachedList.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */
package net.sf.maltcms.datastructures;

import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Implementation of a cached list for indexed data access.
 * 
 * @TODO A future version of this will be generic, using a DataProvider to load
 *       T instances.
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class CachedLazyList<T> implements List<T> {

    private class SRefA extends SoftReference<T> {

        private final Integer key;

        private SRefA(final Integer key, final T value,
                final ReferenceQueue<T> rq) {
            super(value, rq);
            this.key = key;
        }
    }

    public static <T> CachedLazyList<T> getList(final IElementProvider<T> ivf) {
        return CachedLazyList.getList(ivf, 0, -1);
    }

    public static <T> CachedLazyList<T> getList(final IElementProvider<T> ivf,
            final int offset, final int length) {
        CachedLazyList<T> cl = new CachedLazyList<T>(ivf);
//        cl.setElementProvider(ivf);
        cl.init(offset, length);
        return cl;
    }
    private IElementProvider<T> ivf = null;
    private final HashMap<Integer, SRefA> cache = new HashMap<Integer, SRefA>();
    private int cacheSize = 512;
    private boolean prefetchOnMiss = false;
    private final LinkedList<Integer> lru = new LinkedList<Integer>();
    private final ReferenceQueue<T> rq = new ReferenceQueue<T>();
    private int size = -1;
    private int offset = 0;
    private int cacheHit = 0;
    private int cacheMiss = 0;
    private int cacheGCed = 0;
    private int cacheLRU = 0;
    private int cacheLRUPURGELAST = 0;
    private int cacheSoftRefRemoved = 0;
    
    public CachedLazyList(IElementProvider<T> iep) {
        this.ivf = iep;
    }

    public void setElementProvider(IElementProvider<T> iep) {
        this.ivf = iep;
    }

    @Override
    public boolean add(final T arg0) {
        throw new UnsupportedOperationException("Can not modify read-only list!");
    }

    @Override
    public void add(final int arg0, final T arg1) {
        throw new UnsupportedOperationException("Can not modify read-only list!");
    }

    @Override
    public boolean addAll(final Collection<? extends T> arg0) {
        throw new UnsupportedOperationException("Can not modify read-only list!");
    }

    @Override
    public boolean addAll(final int arg0, final Collection<? extends T> arg1) {
        throw new UnsupportedOperationException("Can not modify read-only list!");
    }

    private void addToCache(final Integer key, final T a) {
        // create SoftReference with index arg as key
        final SRefA sr = new SRefA(key, a, this.rq);
        this.cache.put(key, sr);
        this.cacheLRU++;
        // Array has not been gc'ed, so add to lru cache (hard reference)
        this.lru.addFirst(key);
        // If we hold too many elements in the lru cache, release the
        // oldest element
        while (this.lru.size() > this.cacheSize) {
            this.cacheLRUPURGELAST++;
            // remove hard reference from lru and from HashMap
            final Integer keyr = this.lru.removeLast();
            this.cache.remove(keyr);
        }
    }

    @Override
    public void clear() {
        this.lru.clear();
        updateQueue();
        this.cache.clear();
    }

    @Override
    public boolean contains(final Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsAll(final Collection<?> arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T get(final int arg0) {
        final int arg = arg0;
        if ((arg < 0) || (arg > this.size - 1)) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + arg0);
        }
        final Integer key = Integer.valueOf(arg);
        T a = null;
        // Lookup SoftReference to array in hashmap
        final SRefA aref = this.cache.get(key);
        // Reference for key exists
        if (aref != null) {
            this.cacheHit++;
            // retrieve referenced array
            a = aref.get();
            if (a == null) {
                this.cacheGCed++;
                // SoftReference was last reference to array. Array was
                // garbage collected
                this.cache.remove(key);
                a = ivf.get(arg);
                addToCache(key, a);
            }
        } else {
            this.cacheMiss++;
            if (this.prefetchOnMiss) {
                final int upperBound = Math.min(this.size, this.cacheSize);
//				Logging.getLogger(this).info("Prefetching: from {} to {}",
//				        arg0, arg0 + upperBound);
                final List<T> l = ivf.get(arg0, Math.min(
                        arg0 + upperBound - 1, this.size - 1));
                for (int i = 0; i < l.size(); i++) {
                    addToCache(Integer.valueOf(arg0 + i), l.get(i));
                }
                a = l.get(0);
            } else {
                a = ivf.get(arg);
                addToCache(key, a);
            }
        }
        updateQueue();
//		Logging.getLogger(this).debug(
//		        "CACHE ACCESS: HITS=" + this.cacheHit + " MISSES="
//		                + this.cacheMiss + " GCED=" + this.cacheGCed
//		                + " LRUED=" + this.cacheLRU + " LRUPURGED="
//		                + this.cacheLRUPURGELAST);
        return a;
    }

    public int getCacheSize() {
        return this.cacheSize;
    }

    @Override
    public int indexOf(final Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void init(final int offset, final int size) {
        this.size = this.ivf.size();
        if ((offset > 0) && (size >= 0)) {

            this.size = Math.min(this.size, size);
            System.out.println("Size of cached list: " + this.size);
            this.offset = offset;
        } else {
//            throw new IllegalArgumentException("Offset and size must be greater than 0!");
        }
    }

    @Override
    public boolean isEmpty() {
        return this.ivf.size() == 0;
    }

    public boolean isPrefetchOnMiss() {
        return this.prefetchOnMiss;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private int start = 0;
            private final int end = size();

            @Override
            public boolean hasNext() {
                return (this.start < this.end);
            }

            @Override
            public T next() {
                return get(this.start++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    @Override
    public int lastIndexOf(final Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<T> listIterator(final int arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T remove(final int arg0) {
        throw new UnsupportedOperationException("Can not modify read-only list!");
    }

    @Override
    public boolean remove(final Object arg0) {
        throw new UnsupportedOperationException("Can not modify read-only list!");
    }

    @Override
    public boolean removeAll(final Collection<?> arg0) {
        throw new UnsupportedOperationException("Can not modify read-only list!");
    }

    @Override
    public boolean retainAll(final Collection<?> arg0) {
        throw new UnsupportedOperationException("Can not modify read-only list!");
    }

    @Override
    public T set(final int arg0, final T arg1) {
        throw new UnsupportedOperationException("Can not modify read-only list!");
    }

    public void setCacheSize(final int cachesize) {
        this.cacheSize = cachesize;
    }

    public void setPrefetchOnMiss(final boolean prefetchOnMiss) {
        this.prefetchOnMiss = prefetchOnMiss;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public List<T> subList(final int arg0, final int arg1) {
        return CachedLazyList.getList(this.ivf, arg0, arg1 - arg0);
    }

    @Override
    public Object[] toArray() {
//        Object[] o = new Object[];
        Object[] result = new Object[size];
        for (int i = 0;i<size;i++) {
            result[i] = get(i);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        }
        Object[] result = a;
        for (int i = 0;i<size;i++) {
            result[i] = get(i);
        }

        if (a.length > size) {
            a[size] = null;
        }

        return a;
    }

    private void updateQueue() {
        SRefA sv;
        while ((sv = (SRefA) this.rq.poll()) != null) {
            try {
                this.rq.remove(sv.key); // remove the SoftReference
                this.cache.remove(sv.key);
                this.cacheSoftRefRemoved++;
            } catch (final IllegalArgumentException ex) {
//				Logging.getLogger(this).warn(ex.getLocalizedMessage());
            } catch (final InterruptedException ex) {
                Thread.interrupted();
//				Logging.getLogger(this).warn(ex.getLocalizedMessage());
            }
        }
    }
}
