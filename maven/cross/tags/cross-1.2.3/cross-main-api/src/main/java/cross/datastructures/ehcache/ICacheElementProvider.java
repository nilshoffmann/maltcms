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
public interface ICacheElementProvider<K,V> {
    V provide(K key);
}
