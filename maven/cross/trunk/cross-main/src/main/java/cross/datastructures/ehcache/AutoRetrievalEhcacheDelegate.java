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
package cross.datastructures.ehcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * Transparent cache, which also knows how to create objects of the given type
 * via the
 *
 * @{link cross.datastructures.ehcache.ICacheElementProvider}, if their key is
 * not present in the in-memory cache.
 *
 * Please note that Ehcache only allows Serializable objects to be externalized
 * to disk, should the in-memory cache overflow.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
class AutoRetrievalEhcacheDelegate<K, V> implements ICacheDelegate<K, V> {

    private final String cacheName;
    private final CacheManager cacheManager;
    private final ICacheElementProvider<K, V> provider;

    public AutoRetrievalEhcacheDelegate(String cacheName, CacheManager cm,
            ICacheElementProvider<K, V> provider) {
        this.cacheName = cacheName;
        this.cacheManager = cm;
        this.provider = provider;
    }

    @Override
    public void put(final K key, final V value) {
        getCache().put(new Element(key, value));
    }

    @Override
    public V get(final K key) {
        Element element = getCache().get(key);
        V v = null;
        if (element != null) {
            v = (V) element.getValue();
            if(v != null) {
                return v;
            }
        }
        v = provider.provide(key);
        put(key, v);
        return v;

    }

    public Ehcache getCache() {
        return cacheManager.getEhcache(cacheName);
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public void close() {
        cacheManager.getCache(cacheName).dispose();
    }
}
