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

import cross.datastructures.cache.ehcache.AutoRetrievalEhcacheDelegate;
import cross.datastructures.cache.ehcache.EhcacheDelegate;
import cross.datastructures.cache.db4o.Db4oCacheManager;
import cross.datastructures.cache.ehcache.VariableFragmentArrayCache;
import cross.datastructures.cache.softReference.SoftReferenceCacheManager;
import cross.datastructures.fragments.IVariableFragment;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import net.sf.ehcache.store.DiskStore;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class CacheFactory {

    private static File cacheDirectory = new File(System.getProperty("java.io.tmpdir"));
    private static CacheType fragmentCacheType = CacheType.EHCACHE;

    /**
     * Set the cache location for all NEWLY created caches.
     *
     * @param f
     */
    public static void setCacheDirectory(File f) {
        CacheFactory.cacheDirectory = f;
    }

    public static void setDefaultFragmentCacheType(CacheType fragmentCacheType) {
        CacheFactory.fragmentCacheType = fragmentCacheType;
    }

    public static void removeCache(String cacheName) {
        try {
            CacheManager.getInstance().removeCache(cacheName);
        } catch (IllegalStateException ise) {
            log.warn("Failed to remove cache " + cacheName, ise.getLocalizedMessage());
        }
    }

    public static void removeDb4oCache(File basedir, ICacheDelegate delegate) {
        try {
            Db4oCacheManager.getInstance(basedir).remove(delegate);
        } catch (Exception ise) {
            log.warn("Failed to remove cache " + delegate.getName(), ise.getLocalizedMessage());
        }
    }

    public static ICacheDelegate<IVariableFragment, List<Array>> createFragmentCache(File cacheDir, String cacheName) {
        return createFragmentCache(cacheDir, cacheName, fragmentCacheType);
    }

    public static ICacheDelegate<IVariableFragment, List<Array>> createFragmentCache(String cacheName) {
        return createFragmentCache(cacheDirectory, cacheName);
    }

    public static ICacheDelegate<IVariableFragment, List<Array>> createFragmentCache(File cacheDir, String cacheName, CacheType cacheType) {
        switch (cacheType) {
            case DB4O:
                File cacheLocation = new File(cacheDir, cacheName);
                log.debug("Using db4o cache {}", cacheLocation.getAbsolutePath());
//                if(cacheLocation.getParentFile().isDirectory()) {
                return createDb4oDefaultCache(cacheDir, cacheName);
//                }else{
//                    throw new IllegalArgumentException("DB4o cache name must be an absolute file name!");
//                }
            case EHCACHE:
                log.debug("Using ehcache {}", cacheName);
                return createDefaultFragmentCache(cacheDir, cacheName);
            case SOFT:
                return SoftReferenceCacheManager.getInstance().getCache(cacheName);
            default:
                throw new IllegalStateException("Unknown enum value: " + cacheType);
        }
    }

    public static ICacheDelegate<IVariableFragment, List<Array>> createFragmentCache(String cacheName, CacheType cacheType) {
        return createFragmentCache(cacheDirectory, cacheName, cacheType);
    }

    public static ICacheDelegate<IVariableFragment, List<Array>> createDefaultFragmentCache(File cacheDir, String cacheName) {
        CacheManager cm = CacheManager.getInstance();
        Ehcache cache = cm.addCacheIfAbsent(cacheName);
        ICacheDelegate<IVariableFragment, List<Array>> ed = new VariableFragmentArrayCache(cache);
        CacheConfiguration cc = cache.getCacheConfiguration();
        cc.setMaxElementsInMemory(10);
//        cc.setEternal(true);
        cc.overflowToDisk(true);
        cc.maxElementsOnDisk(1000000000);
        cc.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU);
//        DiskStore ds = DiskStore.create(cache, cacheDir.getAbsolutePath());
        return ed;
    }
    
    public static <K, V> ICacheDelegate<K, V> createDefaultCache(File cacheDir, String cacheName) {
        CacheManager cm = CacheManager.getInstance();
        Ehcache cache = cm.addCacheIfAbsent(cacheName);
        EhcacheDelegate<K, V> ed = new EhcacheDelegate<K, V>(cache);
        CacheConfiguration cc = cache.getCacheConfiguration();
        cc.setMaxElementsInMemory(10000);
//        cc.setEternal(true);
        cc.overflowToDisk(true);
        cc.maxElementsOnDisk(1000000000);
        cc.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU);
        DiskStore ds = DiskStore.create(cache, cacheDir.getAbsolutePath());
        return ed;
    }

    public static <K, V> ICacheDelegate<K, V> createDefaultCache(String cacheName) {
        return createDefaultCache(cacheDirectory, cacheName);
    }

    public static <K, V> ICacheDelegate<K, V> createVolatileCache(String cacheName, long timeToIdle, long timeToLive) {
        return createVolatileCache(cacheName, timeToIdle, timeToLive, 20, new CacheEventListener[0]);
    }
    
    public static <K, V> ICacheDelegate<K, V> createVolatileCache(String cacheName, long timeToIdle, long timeToLive, int maxElementsInMemory, CacheEventListener... cacheEventListener) {
        CacheManager cm = CacheManager.getInstance();
        Ehcache cache = cm.addCacheIfAbsent(cacheName);
        EhcacheDelegate<K, V> ed = new EhcacheDelegate<K, V>(cache);
        CacheConfiguration cc = cache.getCacheConfiguration();
        cc.setEternal(false);
        cc.setMaxElementsInMemory(maxElementsInMemory);
        cc.setTimeToIdleSeconds(timeToIdle);
        cc.setTimeToLiveSeconds(timeToLive);
        cc.overflowToDisk(false);
        cc.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU);
        for(CacheEventListener listener:cacheEventListener) {
            cache.getCacheEventNotificationService().registerListener(listener);
        }
        return ed;
    }

    public static <K, V> ICacheDelegate<K, V> createAutoRetrievalCache(String cacheName, ICacheElementProvider<K, V> provider) {
        CacheManager cm = CacheManager.getInstance();
        return new AutoRetrievalEhcacheDelegate<K, V>(cm.addCacheIfAbsent(cacheName), provider);
    }

    public static <K, V> ICacheDelegate<K, V> createVolatileAutoRetrievalCache(String cacheName, ICacheElementProvider<K, V> provider, long timeToIdle, long timeToLive) {
        CacheManager cm = CacheManager.getInstance();
        AutoRetrievalEhcacheDelegate<K, V> ared = new AutoRetrievalEhcacheDelegate<K, V>(cm.addCacheIfAbsent(cacheName), provider);
        CacheConfiguration cc = ared.getCache().getCacheConfiguration();
        cc.setEternal(false);
        cc.setTimeToIdleSeconds(timeToIdle);
        cc.setTimeToLiveSeconds(timeToLive);
        return ared;
    }

    public static Ehcache getCacheFor(String cacheName) {
        return CacheManager.getInstance().getEhcache(cacheName);
    }

    public static <K, V> ICacheDelegate<K, V> createDb4oDefaultCache(File basedir, String cacheName) {
        Db4oCacheManager dbcm = Db4oCacheManager.getInstance(basedir);
        return dbcm.getCache(cacheName);
    }

    /**
     *
     * @param <K>
     * @param <V>
     * @param basedir
     * @param cacheName
     * @param comparator
     * @return
     */
    public static <K, V> ICacheDelegate<K, V> createDb4oSortedCache(File basedir, String cacheName, Comparator<K> comparator) {
        Db4oCacheManager dbcm = Db4oCacheManager.getInstance(basedir);
        return dbcm.getCache(cacheName, comparator);
    }
}
