/*
 * $license$
 *
 * $Id$
 */
package cross.datastructures.ehcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 *
 * Implementation of a cache delegate for typed caches backed by
 * <a href="http://www.ehcache.org">ehcache</a>.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
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

    
    public Ehcache getCache() {
        return cacheManager.getEhcache(cacheName);
    }

    @Override
    public String getName() {
        return cacheName;
    }

}
