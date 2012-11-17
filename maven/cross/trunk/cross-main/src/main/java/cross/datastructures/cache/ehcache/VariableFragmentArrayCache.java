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
package cross.datastructures.cache.ehcache;

import cross.datastructures.cache.CacheType;
import cross.datastructures.cache.ICacheDelegate;
import cross.datastructures.fragments.IVariableFragment;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import ucar.ma2.Array;

/**
 *
 * Implementation of a cache delegate for typed caches backed by <a
 * href="http://www.ehcache.org">ehcache</a>.
 *
 * Please note that Ehcache only allows Serializable objects to be externalized
 * to disk, should the in-memory cache overflow.
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class VariableFragmentArrayCache<K, V> implements ICacheDelegate<IVariableFragment, List<Array>> {

    private final String cacheName;
    private final Ehcache cache;

    public VariableFragmentArrayCache(final Ehcache cache) {
        this.cache = cache;
        this.cacheName = cache.getName();
    }

    @Override
    public void put(final IVariableFragment key, final List<Array> value) {
        if (key instanceof Serializable) {
            log.debug("key {} is serializable", key);
        }
        if (value == null) {
            getCache().put(new Element(getVariableFragmentId(key), null));
        } else {
            if (!(value instanceof Serializable)) {
                throw new IllegalArgumentException("Value must be serializable!");
            }
            try {
                List c = (List) value;
                if (c.size() > 0) {
                    //System.out.println("Converting array to serializable array for " + key);
                    List l = new ArrayList<SerializableArray>(c.size());
                    for (Object object : c) {
                        l.add(new SerializableArray((Array) object));
                    }
                    getCache().put(new Element(getVariableFragmentId(key), (Serializable) l));
                }
            } catch (IllegalStateException se) {
                log.warn("Failed to add element to cache: " + getVariableFragmentId(key), se);
            }
        }
    }

    public String getVariableFragmentId(IVariableFragment key) {
        return key.getParent().getName() + ">" + key.getName();
    }

    @Override
    public List<Array> get(final IVariableFragment key) {
        try {
            Element element = getCache().get(getVariableFragmentId(key));
            if (element != null) {
                List<SerializableArray> c = (List<SerializableArray>) element.getValue();
                if (c != null && c.size() > 0) {
                    //System.out.println("Converting serializable array to array for " + key);
                    List<Array> l = new ArrayList<Array>(c.size());
                    for (Object object : c) {
                        l.add(((SerializableArray) object).getArray());
                    }
                    return l;
                }
            }
            return null;
        } catch (IllegalStateException se) {
            log.warn("Failed to get element from cache: " + getVariableFragmentId(key), se);
            return null;
        }
    }

    @Override
    public void close() {
        cache.dispose();
    }

    public Ehcache getCache() {
        return cache;
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.EHCACHE;
    }
}
