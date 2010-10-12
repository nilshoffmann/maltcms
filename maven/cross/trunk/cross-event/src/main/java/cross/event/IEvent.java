/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
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
 * 
 * $Id: IEvent.java 43 2009-10-16 17:22:55Z nilshoffmann $
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
