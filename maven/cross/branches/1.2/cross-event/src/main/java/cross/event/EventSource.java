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

import java.util.LinkedHashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EventSource of Type V, where V can be any Object produced by the EventSource.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 * @param <V>
 */
public class EventSource<V> implements IEventSource<V> {

	private final LinkedHashSet<IListener<IEvent<V>>> listenerMap;

	private ExecutorService es = Executors.newCachedThreadPool();

	public EventSource(int nThreads) {
		this();
		this.es = Executors.newFixedThreadPool(nThreads);
	}

	public EventSource() {
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
			es.submit(new Runnable() {

				public void run() {
					lst.listen(e);
				}

			});
		}
	}

	public void removeListener(final IListener<IEvent<V>> l) {
		if (this.listenerMap.contains(l)) {
			this.listenerMap.remove(l);
		}
	}

}
