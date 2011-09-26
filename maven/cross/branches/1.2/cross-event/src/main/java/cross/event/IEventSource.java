/*
 * $license$
 *
 * $Id$
 */

package cross.event;

/**
 * Classes representing concrete EventSources must implement this interface.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 * @param <V>
 */
public interface IEventSource<V> {

	public void addListener(IListener<IEvent<V>> l);

	public void fireEvent(IEvent<V> e);

	public void removeListener(IListener<IEvent<V>> l);

}
