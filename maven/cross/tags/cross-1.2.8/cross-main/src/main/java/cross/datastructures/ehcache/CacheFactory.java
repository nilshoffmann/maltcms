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

import cross.datastructures.ehcache.db4o.Db4oCacheManager;
import java.io.File;
import java.util.Comparator;
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
    
    public ICacheDelegate<K,V> createDb4oDefaultCache(File basedir, String cacheName) {
        Db4oCacheManager dbcm = new Db4oCacheManager(basedir);
        return dbcm.getCache(cacheName);
    }
    
    public ICacheDelegate<K,V> createDb4oSortedCache(File basedir, String cacheName, Comparator<K> comparator) {
        Db4oCacheManager dbcm = new Db4oCacheManager(basedir);
        return dbcm.getCache(cacheName, comparator);
    }
}
