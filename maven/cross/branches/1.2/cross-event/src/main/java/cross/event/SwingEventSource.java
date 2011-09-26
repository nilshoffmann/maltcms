/*
 * 
 *
 * $Id$
 */

package cross.event;

import java.util.LinkedHashSet;

import javax.swing.SwingUtilities;

/**
 * EventSource of Type V. Notifications of Listeners are performed
 * asynchronously on Swing's EventDispatchThread.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
public class SwingEventSource<V> implements IEventSource<V> {

	private final LinkedHashSet<IListener<IEvent<V>>> listenerMap;

	public SwingEventSource() {
		this.listenerMap = new LinkedHashSet<IListener<IEvent<V>>>();
	}

	public void addListener(final IListener<IEvent<V>> l) {
		if (this.listenerMap.contains(l)) {
			// System.out.println("IListener already known, ignoring!");
		} else {
			// System.out.println("Adding listener!");
			this.listenerMap.add(l);
		}
	}

	public void fireEvent(final IEvent<V> e) {
		for (final IListener<IEvent<V>> lst : this.listenerMap) {
			Runnable r = new Runnable() {

				public void run() {
					lst.listen(e);
				}

			};
			SwingUtilities.invokeLater(r);
		}
	}

	public void removeListener(final IListener<IEvent<V>> l) {
		if (this.listenerMap.contains(l)) {
			this.listenerMap.remove(l);
		}
	}
}
