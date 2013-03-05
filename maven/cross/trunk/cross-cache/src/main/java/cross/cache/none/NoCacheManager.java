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
package cross.cache.none;

import cross.cache.ICacheDelegate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CacheManager that manages {@link NoCache} instances for names.
 * 
 * @author Nils Hoffmann
 */
public class NoCacheManager {

    private static Map<String, NoCache> caches = new ConcurrentHashMap<String, NoCache>();
    private static NoCacheManager instance;

    private NoCacheManager() {
        super();
    }

    public static NoCacheManager getInstance() {
        if (NoCacheManager.instance == null) {
            NoCacheManager.instance = new NoCacheManager();
        }
        return NoCacheManager.instance;
    }

    public <K, V> ICacheDelegate<K, V> getCache(String name) {
        NoCache<K, V> delegate = caches.get(name);
        if (delegate == null) {
            delegate = new NoCache<K, V>(name);
            caches.put(name, delegate);
        }
        return delegate;
    }

    public <K, V> void remove(ICacheDelegate<K, V> delegate) {
        NoCache<K, V> cache = caches.get(delegate.getName());
        if (cache != null) {
            cache.close();
            caches.remove(delegate.getName());
        }
    }
}
