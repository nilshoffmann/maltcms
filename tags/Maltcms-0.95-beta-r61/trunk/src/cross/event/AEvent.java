/**
 * 
 */
package cross.event;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * @param <V>
 * 
 * 
 */
public class AEvent<V> implements IEvent<V> {

	private long ts = System.currentTimeMillis();

	private V v;

	private IEventSource<V> ies;

	public AEvent(V v, IEventSource<V> ies) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.event.IEvent#getEventName()
	 */
	@Override
	public String getEventName() {
		return "EVENT_DEFAULT";
	}

}
