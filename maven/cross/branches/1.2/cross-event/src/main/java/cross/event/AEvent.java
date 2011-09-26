/*
 * 
 *
 * $Id$
 */

package cross.event;

/**
 * Abstract implementation of a typed Event.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * @param <V>
 * 
 * 
 */
public class AEvent<V> implements IEvent<V> {

	private final long ts = System.currentTimeMillis();

	private final V v;

	private final IEventSource<V> ies;

	private String eventName = "EVENT_DEFAULT";

	public AEvent(final V v, final IEventSource<V> ies, final String eventName) {
		this(v, ies);
		this.eventName = eventName;
	}

	public AEvent(final V v, final IEventSource<V> ies) {
		this.v = v;
		this.ies = ies;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.event.IEvent#get()
	 */
	@Override
	public V get() {
		return this.v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.event.IEvent#getEventName()
	 */
	@Override
	public String getEventName() {
		return this.eventName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.event.IEvent#getSource()
	 */
	@Override
	public IEventSource<V> getSource() {
		return this.ies;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.event.IEvent#getWhen()
	 */
	@Override
	public long getWhen() {
		return this.ts;
	}

}
