/**
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
 */
/*
 * 
 *
 * $Id$
 */
package cross.datastructures.fragments;

import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import cross.Factory;
import cross.IConfigurable;
import cross.annotations.Configurable;
import cross.exception.ResourceNotAvailableException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of a cached list for indexed data access. Supports 
 * read-only actions only! All other options that attempt to modify the list 
 * (add,subset,remove) will throw an exception.
 * 
 * @TODO A future version of this will be generic, using a DataProvider to load
 *       T instances.
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
public class CachedList implements List<ucar.ma2.Array>, IConfigurable {

    private class SRefA extends SoftReference<Array> {

        private final Integer key;

        private SRefA(final Integer key, final Array value,
                final ReferenceQueue<Array> rq) {
            super(value, rq);
            this.key = key;
        }
    }

    public static CachedList getList(final IVariableFragment ivf) {
        return CachedList.getList(ivf, 0, -1);
    }

    public static CachedList getList(final IVariableFragment ivf,
            final int offset, final int length) {
        final String clclass = Factory.getInstance().getConfiguration().
                getString("cross.datastructures.fragments.cachedListImpl",
                "cross.datastructures.fragments.CachedList");
        final CachedList cl = Factory.getInstance().getObjectFactory().
                instantiate(clclass, CachedList.class);
        cl.setVariableFragment(ivf);
        cl.init(offset, length);
        return cl;
    }
    private IVariableFragment ivf = null;
    private final HashMap<Integer, SRefA> cache = new HashMap<Integer, SRefA>();
    @Configurable
    private int cacheSize = 512;
    @Configurable
    private boolean prefetchOnMiss = false;
    private final LinkedList<Integer> lru = new LinkedList<Integer>();
    private final ReferenceQueue<Array> rq = new ReferenceQueue<Array>();
    private int size = -1;
    private int offset = 0;
    private int cacheHit = 0;
    private int cacheMiss = 0;
    private int cacheGCed = 0;
    private int cacheLRU = 0;
    private int cacheLRUPURGELAST = 0;
    private int cacheSoftRefRemoved = 0;

    public boolean add(final Array arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void add(final int arg0, final Array arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addAll(final Collection<? extends Array> arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addAll(final int arg0, final Collection<? extends Array> arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void addToCache(final Integer key, final Array a) {
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
        log.debug("Number of referenced Elements: {}",
                this.cache.size());
    }

    public void clear() {
        this.lru.clear();
        updateQueue();
        this.cache.clear();
    }

    public void configure(final Configuration cfg) {
        this.prefetchOnMiss = cfg.getBoolean(this.getClass().getName()
                + ".prefetchOnMiss", false);
        this.cacheSize = cfg.getInt(this.getClass().getName() + ".cacheSize",
                1024);
    }

    public boolean contains(final Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean containsAll(final Collection<?> arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Array get(final int arg0) {
        final int arg = arg0;
        if ((arg < 0) || (arg > this.size - 1)) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + arg0);
        }
        final Integer key = Integer.valueOf(arg);
        Array a = null;
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
                a = load(arg);
                addToCache(key, a);
            }
        } else {
            this.cacheMiss++;
            if (this.prefetchOnMiss) {
                final int upperBound = Math.min(this.size, this.cacheSize);
                log.info("Prefetching: from {} to {}",
                        arg0, arg0 + upperBound);
                final List<Array> l = load(arg0, Math.min(
                        arg0 + upperBound - 1, this.size - 1));
                for (int i = 0; i < l.size(); i++) {
                    addToCache(Integer.valueOf(arg0 + i), l.get(i));
                }
                a = l.get(0);
            } else {
                a = load(arg);
                addToCache(key, a);
            }
        }
        updateQueue();
        log.debug(
                "CACHE ACCESS: HITS=" + this.cacheHit + " MISSES="
                + this.cacheMiss + " GCED=" + this.cacheGCed
                + " LRUED=" + this.cacheLRU + " LRUPURGED="
                + this.cacheLRUPURGELAST);
        return a;
    }

    public int getCacheSize() {
        return this.cacheSize;
    }

    public int indexOf(final Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void init(final int offset, final int size) {
        try {
            this.size = Factory.getInstance().getDataSourceFactory().
                    getDataSourceFor(this.ivf.getParent()).readStructure(
                    this.ivf.getIndex()).getDimensions()[0].getLength();
        } catch (final IOException ex) {
            log.warn(ex.getLocalizedMessage());
        } catch (final ResourceNotAvailableException ex) {
            log.warn(ex.getLocalizedMessage());
        }
        if ((offset > 0) && (size >= 0)) {
            this.size = Math.min(this.size, size);
            this.offset = offset;
        }
    }

    public boolean isEmpty() {
        return this.size==0;
    }

    public boolean isPrefetchOnMiss() {
        return this.prefetchOnMiss;
    }

    public Iterator<Array> iterator() {
        return new Iterator<Array>() {

            private int start = 0;
            private final int end = size();

            public boolean hasNext() {
                return (this.start < this.end);
            }

            public Array next() {
                return get(this.start++);
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public int lastIndexOf(final Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ListIterator<Array> listIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ListIterator<Array> listIterator(final int arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private Array load(final int idx) throws ResourceNotAvailableException {
        return load(idx, idx).get(0);
    }

    private List<Array> load(final int from, final int to)
            throws ResourceNotAvailableException {
        try {
            // keep range as is since we still reference original data
            final Range[] r = new Range[]{new Range(from + this.offset, to
                + this.offset)};
            final IVariableFragment index = this.ivf.getIndex();
            index.setRange(r);
            // read array
            final List<Array> a = Factory.getInstance().getDataSourceFactory().
                    getDataSourceFor(this.ivf.getParent()).readIndexed(
                    this.ivf);
            return a;
        } catch (final IOException ex) {
            throw new ResourceNotAvailableException(ex);
        } catch (final ResourceNotAvailableException ex) {
            throw new ResourceNotAvailableException(ex);
        } catch (final InvalidRangeException ex) {
            throw new ResourceNotAvailableException(ex);
        }
    }

    public Array remove(final int arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean remove(final Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeAll(final Collection<?> arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean retainAll(final Collection<?> arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Array set(final int arg0, final Array arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setCacheSize(final int cachesize) {
        this.cacheSize = cachesize;
    }

    public void setPrefetchOnMiss(final boolean prefetchOnMiss) {
        this.prefetchOnMiss = prefetchOnMiss;
    }

    public void setVariableFragment(final IVariableFragment ivf) {
        this.ivf = ivf;
    }

    @Override
    public int size() {
        return this.size;
    }

    public List<Array> subList(final int arg0, final int arg1) {
        return CachedList.getList(this.ivf, arg0, arg1 - arg0);
    }

    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T> T[] toArray(final T[] arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void updateQueue() {
        SRefA sv;
        while ((sv = (SRefA) this.rq.poll()) != null) {
            try {
                this.rq.remove(sv.key); // remove the SoftReference
                this.cache.remove(sv.key);
                this.cacheSoftRefRemoved++;
            } catch (final IllegalArgumentException ex) {
                log.warn(ex.getLocalizedMessage());
            } catch (final InterruptedException ex) {
                log.warn(ex.getLocalizedMessage());
            }
        }
    }
}
