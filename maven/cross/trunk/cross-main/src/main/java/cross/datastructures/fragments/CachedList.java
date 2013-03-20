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
package cross.datastructures.fragments;

import cross.Factory;
import cross.IConfigurable;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.exception.ResourceNotAvailableException;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;

/**
 * Implementation of a cached list for indexed data access.
 *
 * @TODO A future version of this will be generic, using a DataProvider to load
 * T instances.
 *
 * @author Nils Hoffmann
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
        final String clclass = Factory.getInstance().getConfiguration().getString("cross.datastructures.fragments.cachedListImpl",
                "cross.datastructures.fragments.CachedList");
        final CachedList cl = Factory.getInstance().getObjectFactory().instantiate(clclass, CachedList.class);
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

    @Override
    public boolean add(final Array arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(final int arg0, final Array arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAll(final Collection<? extends Array> arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
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

    @Override
    public void clear() {
        this.lru.clear();
        updateQueue();
        this.cache.clear();
    }

    @Override
    public void configure(final Configuration cfg) {
        this.prefetchOnMiss = cfg.getBoolean(this.getClass().getName()
                + ".prefetchOnMiss", false);
        this.cacheSize = cfg.getInt(this.getClass().getName() + ".cacheSize",
                1024);
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
    public Array get(final int arg0) {
        final int arg = arg0;
        if ((arg < 0) || (arg < offset) || (arg > this.size - 1)) {
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
                final List<Array> l = load(arg0, Math.max(arg0,Math.min(
                        arg0 + upperBound - 1, this.size - 1)));
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

    @Override
    public int indexOf(final Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void init(final int offset, final int size) {
        try {
            this.size = Factory.getInstance().getDataSourceFactory().getDataSourceFor(this.ivf.getParent()).readStructure(
                    this.ivf.getIndex()).getDimensions()[0].getLength();
        } catch (final IOException ex) {
            log.warn(ex.getLocalizedMessage());
        } catch (final ResourceNotAvailableException ex) {
            log.warn(ex.getLocalizedMessage());
        }
        if ((offset >= 0 && offset < size) && (size > 0)) {
            this.offset = offset;
            this.size = Math.min(this.size, this.offset + size);
        }
    }

    @Override
    public boolean isEmpty() {
        if (this.size < 1) {
            return true;
        }
        return false;
    }

    public boolean isPrefetchOnMiss() {
        return this.prefetchOnMiss;
    }

    @Override
    public Iterator<Array> iterator() {
        return new Iterator<Array>() {
            private int start = offset;
            private final int end = size();

            @Override
            public boolean hasNext() {
                return (this.start < this.end);
            }

            @Override
            public Array next() {
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
    public ListIterator<Array> listIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<Array> listIterator(final int arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private Array load(final int idx) throws ResourceNotAvailableException {
        return load(idx, idx).get(0);
    }

    private List<Array> load(final int from, final int to)
            throws ResourceNotAvailableException {
        Range[] originalRange = this.ivf.getIndex().getRange();
		EvalTools.geq(from, to, this);
        try {
            // keep range as is since we still reference original data
            final Range[] r = new Range[]{new Range(from + this.offset, to
                + this.offset)};
            final IVariableFragment index = this.ivf.getIndex();
            index.setRange(r);
            // read array
            final List<Array> a = Factory.getInstance().getDataSourceFactory().getDataSourceFor(this.ivf.getParent()).readIndexed(
                    this.ivf);

            return a;
        } catch (final IOException ex) {
            throw new ResourceNotAvailableException(ex);
        } catch (final ResourceNotAvailableException ex) {
            throw new ResourceNotAvailableException(ex);
        } catch (final InvalidRangeException ex) {
            throw new ResourceNotAvailableException(ex);
        } finally {
            //restore original range
            this.ivf.getIndex().setRange(originalRange);
        }
    }

    /**
     *
     * @param arg0
     * @return
     */
    @Override
    public Array remove(final int arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param arg0
     * @return
     */
    @Override
    public boolean remove(final Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param arg0
     * @return
     */
    @Override
    public boolean removeAll(final Collection<?> arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param arg0
     * @return
     */
    @Override
    public boolean retainAll(final Collection<?> arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param arg0
     * @param arg1
     * @return
     */
    @Override
    public Array set(final int arg0, final Array arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param cachesize
     */
    public void setCacheSize(final int cachesize) {
        this.cacheSize = cachesize;
    }

    /**
     *
     * @param prefetchOnMiss
     */
    public void setPrefetchOnMiss(final boolean prefetchOnMiss) {
        this.prefetchOnMiss = prefetchOnMiss;
    }

    /**
     *
     * @param ivf
     */
    public void setVariableFragment(final IVariableFragment ivf) {
        this.ivf = ivf;
    }

    /**
     *
     * @return
     */
    @Override
    public int size() {
        return this.size;
    }

    /**
     *
     * @param arg0
     * @param arg1
     * @return
     */
    @Override
    public List<Array> subList(final int arg0, final int arg1) {
        return CachedList.getList(this.ivf, arg0, arg1 - arg0);
    }

    /**
     *
     * @return
     */
    @Override
    public Object[] toArray() {
        return toArray(new Array[size()]);
    }

    /**
     *
     * @param <T>
     * @param arg0
     * @return
     */
    @Override
    public <T> T[] toArray(final T[] arg0) {
        Array[] arrays = null;
        if (arg0 == null || arg0.length == 0 || arg0.length != size()) {
            arrays = new Array[size()];
        } else {
            arrays = (Array[]) arg0;
        }
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = get(i);
        }
        return (T[]) arrays;
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
