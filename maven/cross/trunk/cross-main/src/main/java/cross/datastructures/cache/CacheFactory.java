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

import cross.datastructures.cache.FragmentCacheType;
import cross.datastructures.cache.ICacheDelegate;
import cross.datastructures.cache.ICacheElementProvider;
import cross.datastructures.cache.ehcache.AutoRetrievalEhcacheDelegate;
import cross.datastructures.cache.ehcache.EhcacheDelegate;
import cross.Factory;
import cross.datastructures.cache.db4o.Db4oCacheManager;
import java.io.File;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class CacheFactory {

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
    
    public static <K, V> ICacheDelegate<K, V> createFragmentCache(String cacheName) {
        String type = Factory.getInstance().getConfiguration().getString(CacheFactory.class.getName()+".fragmentCacheType","EHCACHE");
        return createFragmentCache(cacheName, FragmentCacheType.valueOf(type));
    }
    
    public static <K, V> ICacheDelegate<K, V> createFragmentCache(String cacheName, FragmentCacheType cacheType) {
        switch(cacheType) {
            case DB4O:
                File cacheLocation = new File(cacheName);
                log.debug("Using db4o cache {}",cacheLocation.getAbsolutePath());
                if(cacheLocation.getParentFile().isDirectory()) {
                    return createDb4oDefaultCache(cacheLocation.getParentFile(), cacheLocation.getName());
                }else{
                    throw new IllegalArgumentException("DB4o cache name must be an absolute file name!");
                }
            case EHCACHE:
                log.debug("Using ehcache {}",cacheName);
                return createDefaultCache(cacheName);
            default:
                throw new IllegalStateException("Unknown enum value: "+cacheType);
        }
    }

    public static <K, V> ICacheDelegate<K, V> createDefaultCache(String cacheName) {
        CacheManager cm = CacheManager.getInstance();
        cm.addCacheIfAbsent(cacheName);
        EhcacheDelegate<K, V> ed = new EhcacheDelegate<K, V>(cacheName, cm);
        CacheConfiguration cc = ed.getCache().getCacheConfiguration();
        cc.setEternal(true);
        cc.setTimeToIdleSeconds(-1);
        cc.setTimeToIdleSeconds(-1);
        return ed;
    }

    public static <K, V> ICacheDelegate<K, V> createVolatileCache(String cacheName, long timeToIdle, long timeToLive) {
        EhcacheDelegate<K, V> ed = (EhcacheDelegate<K, V>) createDefaultCache(cacheName);
        CacheConfiguration cc = ed.getCache().getCacheConfiguration();
        cc.setEternal(false);
        cc.setTimeToIdleSeconds(timeToIdle);
        cc.setTimeToLiveSeconds(timeToLive);
        return ed;
    }

    public static <K, V> ICacheDelegate<K, V> createAutoRetrievalCache(String cacheName, ICacheElementProvider<K, V> provider) {
        CacheManager cm = CacheManager.getInstance();
        cm.addCacheIfAbsent(cacheName);
        return new AutoRetrievalEhcacheDelegate<K, V>(cacheName, cm, provider);
    }

    public static <K, V> ICacheDelegate<K, V> createVolatileAutoRetrievalCache(String cacheName, ICacheElementProvider<K, V> provider, long timeToIdle, long timeToLive) {
        CacheManager cm = CacheManager.getInstance();
        cm.addCacheIfAbsent(cacheName);
        AutoRetrievalEhcacheDelegate<K, V> ared = new AutoRetrievalEhcacheDelegate<K, V>(cacheName, cm, provider);
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
