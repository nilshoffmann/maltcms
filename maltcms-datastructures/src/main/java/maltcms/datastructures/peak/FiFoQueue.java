/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
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
 * @author Mathias Wilhelm
 * @param <E> queue element type
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
