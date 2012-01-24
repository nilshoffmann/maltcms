/*
 * $license$
 *
 * $Id$
 */
package cross.datastructures.ehcache;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public interface ICacheDelegate<K, V> {

    void put(K key, V value);

    V get(K key);

    String getName();
    
}
