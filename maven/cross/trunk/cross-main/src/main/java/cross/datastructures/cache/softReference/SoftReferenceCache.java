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
package cross.datastructures.cache.softReference;

import cross.datastructures.cache.CacheType;
import cross.datastructures.cache.ICacheDelegate;
import java.lang.ref.SoftReference;
import java.util.HashMap;

/**
 *
 * @author Nils Hoffmann
 */
public class SoftReferenceCache<K, V> implements ICacheDelegate<K, V> {

    private final HashMap<K, SoftReference<? extends V>> map;
    private final String name;
//    private final ReferenceQueue<V> referenceQueue;

    public SoftReferenceCache(String name) {
        this.name = name;
        this.map = new HashMap<K, SoftReference<? extends V>>();
//        this.referenceQueue = new ReferenceQueue<V>();
    }

    @Override
    public void put(K key, V value) {
        map.put(key, new SoftReference<V>(value));//,this.referenceQueue));
    }

    @Override
    public V get(K key) {
        SoftReference<? extends V> softReference = map.get(key);
        //soft reference is null, no entry for key
        if (softReference == null) {
            return null;
        }
        V value = softReference.get();
        if (value == null) {
            //value was garbage collected
            map.remove(key);
        }
        return value;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void close() {
        this.map.clear();
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.SOFT;
    }
}
