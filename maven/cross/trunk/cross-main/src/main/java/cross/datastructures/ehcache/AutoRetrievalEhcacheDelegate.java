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
        if (element != null) {
            return (V) element.getValue();
        }
        V v = provider.provide(key);
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
}
