/*
 * $license$
 *
 * $Id$
 */
package cross.datastructures.ehcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class CacheFactory<K,V> {
    
    public ICacheDelegate<K,V> createDefaultCache(String cacheName) {
        CacheManager cm = CacheManager.getInstance();
        cm.addCacheIfAbsent(cacheName);
        return new EhcacheDelegate<K, V>(cacheName, cm);
    }
    
    public ICacheDelegate<K,V> createAutoRetrievalCache(String cacheName, ICacheElementProvider<K,V> provider) {
        CacheManager cm = CacheManager.getInstance();
        cm.addCacheIfAbsent(cacheName);
        return new AutoRetrievalEhcacheDelegate<K, V>(cacheName, cm, provider);
    }
    
    public static Ehcache getCacheFor(String cacheName) {
        return CacheManager.getInstance().getEhcache(cacheName);
    }
    
}
