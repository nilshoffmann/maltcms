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
package cross.datastructures.ehcache.db4o;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.config.annotations.Indexed;
import com.db4o.query.Predicate;
import com.db4o.ta.Activatable;
import cross.datastructures.ehcache.ICacheDelegate;
import java.util.Comparator;

/**
 * Implementation of a cache delegate for typed caches backed by
 * <a href="http://www.db4o.com/">db4o</a>.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class Db4oCacheDelegate<K, V> implements ICacheDelegate<K, V> {

    private final String cacheName;
    private final ObjectContainer container;
    private final Comparator<K> comparator;

    public Db4oCacheDelegate(final String cacheName,
            final ObjectContainer container, final Comparator<K> comparator) {
        this.cacheName = cacheName;
        this.container = container;
        if(comparator==null) {
            this.comparator = new Comparator<K>() {

                @Override
                public int compare(K t, K t1) {
                    return 0;
                }
            };
        }else{
            this.comparator = comparator;
        }
        
    }

    @Override
    public void close() {
        container.commit();
        container.close();
    }
    
    @Override
    public void put(final K key, final V value) {
        container.store(new TypedEntry<K, V>(key, value));
    }

    @Override
    public V get(final K key) {
        ObjectSet<TypedEntry<K,V>> os = container.query(new TypedEntryPredicate<K, V>(key),new TypedEntryComparator<K, V>(comparator));
        if(os.size()>1) {
            throw new IllegalStateException("Cache contains more than one element for key: "+key);
        }
        if(os.isEmpty()) {
            return null;
        }
        return os.get(0).getValue();
    }

    @Override
    public String getName() {
        return cacheName;
    }
    
    public class TypedEntryComparator<K,V> implements Comparator<TypedEntry<K,V>> {
        
        private Comparator<K> comparator;
        
        public TypedEntryComparator(Comparator<K> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(TypedEntry<K, V> t, TypedEntry<K, V> t1) {
            return this.comparator.compare(t.getKey(), t1.getKey());
        }
        
    }
    
    public class TypedEntryPredicate<K,V> extends Predicate<TypedEntry<K,V>> {

        private final K key;
        
        public TypedEntryPredicate(K key) {
            this.key = key;
        }
        
        @Override
        public boolean match(TypedEntry<K,V> et) {
            return et.getKey().equals(key);
        }     
    }

    public class TypedEntry<K, V> implements Activatable {

        private transient Activator activator;

        @Override
        public void bind(Activator activator) {
            if (this.activator == activator) {
                return;
            }
            if (activator != null && null != this.activator) {
                throw new IllegalStateException(
                        "Object can only be bound to one activator");
            }
            this.activator = activator;
        }

        @Override
        public void activate(ActivationPurpose activationPurpose) {
            if (null != activator) {
                activator.activate(activationPurpose);
            }
        }
        @Indexed
        private K key;
        private V value;

        public TypedEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            activate(ActivationPurpose.READ);
            return key;
        }

        public void setKey(K key) {
            activate(ActivationPurpose.WRITE);
            this.key = key;
        }

        public V getValue() {
            activate(ActivationPurpose.READ);
            return value;
        }

        public void setValue(V value) {
            activate(ActivationPurpose.WRITE);
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TypedEntry<K, V> other = (TypedEntry<K, V>) obj;
            if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
                return false;
            }
            if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + (this.key != null ? this.key.hashCode() : 0);
            hash = 41 * hash + (this.value != null ? this.value.hashCode() : 0);
            return hash;
        }
    }
}
