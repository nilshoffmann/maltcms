/*
 * $license$
 *
 * $Id$
 */

package cross.event;

/**
 * Event of Type V, allows access to EventSource and time of Event.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 * @param <V>
 */
public interface IEvent<V> {

	public V get();

	public String getEventName();

	public IEventSource<V> getSource();

	public long getWhen();

}
