/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
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
 * $Id: FiFoQueue.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */
package maltcms.datastructures.peak;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * This class represents a first in first out queue. The data holder is an
 * {@link ArrayList}.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 * @param <E>
 *            queue element type
 */
public class FiFoQueue<E> implements Queue<E> {

	private ArrayList<E> queue = new ArrayList<E>();

	/**
	 * Default constructor. Initialize the queue.
	 */
	public FiFoQueue() {
		this.queue = new ArrayList<E>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(final E arg0) {
		return this.queue.add(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(final Collection<? extends E> arg0) {
		return this.queue.addAll(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		this.queue = new ArrayList<E>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(final Object arg0) {
		return this.queue.contains(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsAll(final Collection<?> arg0) {
		return this.queue.containsAll(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E element() {
		if (this.queue.size() == 0) {
			throw new NoSuchElementException("Empty Queue");
		}
		return this.queue.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return this.queue.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator() {
		return this.queue.iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean offer(final E arg0) {
		return this.queue.add(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E peek() {
		if (this.queue.size() != 0) {
			return this.queue.get(0);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E poll() {
		if (this.queue.size() != 0) {
			return this.queue.remove(0);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E remove() {
		if (this.queue.size() == 0) {
			throw new NoSuchElementException("Empty Queue");
		}
		return this.queue.remove(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(final Object arg0) {
		return this.queue.remove(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAll(final Collection<?> arg0) {
		return this.queue.removeAll(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean retainAll(final Collection<?> arg0) {
		return this.queue.retainAll(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return this.queue.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray() {
		return this.queue.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T[] toArray(final T[] arg0) {
		return this.queue.toArray(arg0);
	}

}
