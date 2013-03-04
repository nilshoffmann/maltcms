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

import cross.cache.CacheType;
import cross.cache.ICacheDelegate;
import cross.cache.none.NoCacheManager;
import cross.cache.softReference.SoftReferenceCacheManager;
import cross.datastructures.cache.VariableFragmentArrayCache;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class Fragments {
    
    private static CacheType fragmentCacheType = CacheType.NONE;
    
    private static File cacheDirectory = new File(System.getProperty("java.io.tmpdir"));

    /**
     * Set the cache location for all NEWLY created caches.
     *
     * @param f
     */
    public static void setCacheDirectory(File f) {
        Fragments.cacheDirectory = f;
    }
    
    public static void setDefaultFragmentCacheType(CacheType fragmentCacheType) {
        Fragments.fragmentCacheType = fragmentCacheType;
    }
    
    public static ICacheDelegate<IVariableFragment, List<Array>> createFragmentCache(File cacheDir, String cacheName) {
        return createFragmentCache(cacheDir, cacheName, fragmentCacheType);
    }

    public static ICacheDelegate<IVariableFragment, List<Array>> createFragmentCache(String cacheName) {
        return createFragmentCache(cacheDirectory, cacheName);
    }

    public static ICacheDelegate<IVariableFragment, List<Array>> createFragmentCache(File cacheDir, String cacheName, CacheType cacheType) {
        switch (cacheType) {
            case EHCACHE:
                log.debug("Using ehcache {}", cacheName);
                return createDefaultFragmentCache(cacheDir, cacheName);
            case SOFT:
                log.debug("Using soft reference cache {}", cacheName);
                return SoftReferenceCacheManager.getInstance().getCache(cacheName);
            case NONE:
                log.debug("Using hash map cache {}", cacheName);
                return NoCacheManager.getInstance().getCache(cacheName);
            default:
                log.debug("Using hash map cache {}", cacheName);
                return NoCacheManager.getInstance().getCache(cacheName);
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
        cc.setMaxElementsInMemory(100);
//        cc.setEternal(true);
        cc.overflowToDisk(true);
        cc.maxElementsOnDisk(1000000000);
        cc.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU);
//        DiskStore ds = DiskStore.create(cache, cacheDir.getAbsolutePath());
        return ed;
    }
}
