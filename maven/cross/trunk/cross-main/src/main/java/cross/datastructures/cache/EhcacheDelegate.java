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
package cross.datastructures.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

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
public class EhcacheDelegate<K, V> implements ICacheDelegate<K, V> {

    private final String cacheName;
    private final CacheManager cacheManager;

    public EhcacheDelegate(final String cacheName,
            final CacheManager cacheManager) {
        this.cacheName = cacheName;
        this.cacheManager = cacheManager;
    }

    @Override
    public void put(final K key, final V value) {
        getCache().put(new Element(key, value));
    }

    @Override
    public V get(final K key) {
        Element element = getCache().get(key);
        if (element != null) {
            return (V) element.getValue();
        }
        return null;
    }

    @Override
    public void close() {
        cacheManager.getEhcache(cacheName).dispose();
    }

    public Ehcache getCache() {
        return cacheManager.getEhcache(cacheName);
    }

    @Override
    public String getName() {
        return cacheName;
    }
}
