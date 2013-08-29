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

import cross.cache.ICacheDelegate;
import cross.cache.CacheType;
import cross.datastructures.collections.CachedReadWriteList;
import cross.datastructures.fragments.IVariableFragment;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import ucar.ma2.Array;

/**
 *
 * Implementation of a cache delegate for typed caches backed by <a
 * href="http://www.ehcache.org">ehcache</a>.
 *
 * Please note that Ehcache only allows Serializable objects to be externalized
 * to disk, should the in-memory cache overflow.
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class VariableFragmentArrayCache implements ICacheDelegate<IVariableFragment, List<Array>> {

	private final String cacheName;
	private final Ehcache cache;
	private final Map<IVariableFragment, List<Array>> keys;

	public VariableFragmentArrayCache(final Ehcache cache) {
		this.cache = cache;
		this.cacheName = cache.getName();
		this.keys = new HashMap<IVariableFragment, List<Array>>();
	}

	@Override
	public Set<IVariableFragment> keys() {
		return this.keys.keySet();
	}

	@Override
	public void put(final IVariableFragment key, final List<Array> value) {
		if (key instanceof Serializable) {
			log.debug("key {} is serializable", key);
		}
		if (value == null) {
			getCache().put(new Element(getVariableFragmentId(key), null));
		} else if (value instanceof CachedReadWriteList) {
			//store this seperately, since it maintains its own cache
			keys.put(key, (List<Array>) value);
		} else {
			if (!(value instanceof Serializable)) {
				throw new IllegalArgumentException("Value must be serializable!");
			}
			try {
				List c = (List) value;
				if (c.size() > 0) {
					//System.out.println("Converting array to serializable array for " + key);
					List l = new ArrayList<SerializableArray>(c.size());
					for (Object object : c) {
						l.add(new SerializableArray((Array) object));
					}
					getCache().put(new Element(getVariableFragmentId(key), (Serializable) l));
				}
				keys.put(key, null);
			} catch (IllegalStateException se) {
				log.warn("Failed to add element to cache: " + getVariableFragmentId(key), se);
			}
		}
	}

	public String getVariableFragmentId(IVariableFragment key) {
		return key.getParent().getName() + ">" + key.getName();
	}

	@Override
	public List<Array> get(final IVariableFragment key) {
		if (keys.get(key) != null) {
			return keys.get(key);
		} else {
			try {
				Element element = getCache().get(getVariableFragmentId(key));
				if (element != null) {
					List<SerializableArray> c = (List<SerializableArray>) element.getValue();
					if (c != null && c.size() > 0) {
						//System.out.println("Converting serializable array to array for " + key);
						List<Array> l = new ArrayList<Array>(c.size());
						for (Object object : c) {
							l.add(((SerializableArray) object).getArray());
						}
						return l;
					}
				}
				return null;
			} catch (IllegalStateException se) {
				log.warn("Failed to get element from cache: " + getVariableFragmentId(key), se);
				return null;
			}
		}
	}

	@Override
	public void close() {
		for(IVariableFragment key:keys.keySet()) {
			cache.remove(key);
		}
//		cache.dispose();
	}

	public Ehcache getCache() {
		if (cache.getStatus() != Status.STATUS_ALIVE) {
			cache.dispose();
			if(cache.getStatus() == Status.STATUS_UNINITIALISED) {
				cache.initialise();
			}
		}
		return cache;
	}

	@Override
	public String getName() {
		return cacheName;
	}

	@Override
	public CacheType getCacheType() {
		return CacheType.EHCACHE;
	}
}
