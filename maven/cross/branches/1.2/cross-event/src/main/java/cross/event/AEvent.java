/**
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 *
 * This file is part of Cross/Maltcms.
 *
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 */
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
