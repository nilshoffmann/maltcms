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
 * $Id: CallableCompletionService.java 73 2009-12-16 08:45:14Z nilshoffmann $
 */
package cross.datastructures.threads;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;

/**
 * Allows the creation of a CompletionService which can create an iterator over
 * the results of the Callables results. There is no guaranteed order in which
 * results are returned, so you will need to provide the callable with some kind
 * of id to associate it later on.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class CallableCompletionService<T> implements Iterable<T> {

	private class ResultIterator implements Iterator<T> {

		private int elements = 0;

		private final CompletionService<T> ecs;

		public ResultIterator(final CompletionService<T> ecs, final int elements) {
			this.ecs = ecs;
			this.elements = elements;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			if (this.elements > 0) {
				return true;
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		@Override
		public T next() {
			try {
				final T t = this.ecs.take().get();
				this.elements--;
				return t;
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private final CompletionService<T> ecs;

	private int callables = 0;

	/**
	 * Initializes this CallableCompletionService to use the specified Executor
	 * for execution of tasks.
	 * 
	 * @param e
	 */
	public CallableCompletionService(final Executor e) {
		this.ecs = new ExecutorCompletionService<T>(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new ResultIterator(this.ecs, this.callables);
	}

	/**
	 * Submits a collection of Callables to this objects CompletionService. This
	 * method returns immediately. Use the iterator provided to receive results
	 * in undetermined order.
	 * 
	 * @param c
	 */
	public void submit(final Collection<Callable<T>> c) {
		this.callables = c.size();
		for (final Callable<T> s : c) {
			this.ecs.submit(s);
		}
	}

}
