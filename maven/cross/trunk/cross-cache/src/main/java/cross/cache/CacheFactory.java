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
package cross.cache;

import cross.cache.ehcache.AutoRetrievalEhcacheDelegate;
import cross.cache.ehcache.EhcacheDelegate;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheWriterConfiguration;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.store.DiskStore;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * Static utility class for creation and retrieval of various pre-configured
 * caches.
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class CacheFactory {

	private static File cacheDirectory = new File(System.getProperty("java.io.tmpdir"));

	/**
	 * Set the cache location for all NEWLY created caches.
	 *
	 * @param f
	 */
	public static void setCacheDirectory(File f) {
		CacheFactory.cacheDirectory = f;
	}

	public static void removeCache(String cacheName) {
		try {
			CacheManager.getInstance().removeCache(cacheName);
		} catch (IllegalStateException ise) {
			log.warn("Failed to remove cache " + cacheName, ise.getLocalizedMessage());
		}
	}

	public static <K, V> ICacheDelegate<K, V> createDefaultCache(File cacheDir, String cacheName, int maxElementsInMemory) {
		CacheManager cm = CacheManager.getInstance();
		boolean preExists = cm.cacheExists(cacheName);
		Ehcache cache = cm.addCacheIfAbsent(cacheName);
		EhcacheDelegate<K, V> ed = new EhcacheDelegate<K, V>(cache);
		if (!preExists) {
			CacheConfiguration cc = cache.getCacheConfiguration();
			cc.setMaxElementsInMemory(maxElementsInMemory);
//        cc.setEternal(true);
			cc.overflowToDisk(true);
			cc.maxElementsOnDisk(Integer.MAX_VALUE);
			cc.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU);
			DiskStore ds = DiskStore.create(cache, cacheDir.getAbsolutePath());
		}
		return ed;
	}

	public static <K, V> ICacheDelegate<K, V> createDefaultCache(File cacheDir, String cacheName) {
		return createDefaultCache(cacheDir, cacheName, 100);
	}

	public static <K, V> ICacheDelegate<K, V> createDefaultCache(String cacheName) {
		return createDefaultCache(cacheDirectory, cacheName);
	}

	public static <K, V> ICacheDelegate<K, V> createDefaultCache(String cacheName, int maxElementsInMemory) {
		return createDefaultCache(cacheDirectory, cacheName, maxElementsInMemory);
	}

	public static <K, V> ICacheDelegate<K, V> createVolatileCache(String cacheName, long timeToIdle, long timeToLive) {
		return createVolatileCache(cacheName, timeToIdle, timeToLive, 20, new CacheEventListener[0]);
	}

	public static <K, V> ICacheDelegate<K, V> createVolatileCache(String cacheName, long timeToIdle, long timeToLive, int maxElementsInMemory) {
		return createVolatileCache(cacheName, timeToIdle, timeToLive, maxElementsInMemory, new CacheEventListener[0]);
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
		for (CacheEventListener listener : cacheEventListener) {
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
}
