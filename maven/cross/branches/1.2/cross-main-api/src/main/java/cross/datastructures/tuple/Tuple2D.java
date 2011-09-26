/*
 * 
 *
 * $Id$
 */

package cross.datastructures.tuple;

import java.io.Serializable;

/**
 * Class representing a tuple of two elements with Types T and U.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Tuple2D<T, U> implements Serializable {

	/**
     * 
     */
	private static final long serialVersionUID = 5350596008036216528L;

	protected T first;

	protected U second;

	public Tuple2D(final T first1, final U second1) {
		this.first = first1;
		this.second = second1;
	}

	public T getFirst() {
		return this.first;
	}

	public U getSecond() {
		return this.second;
	}

	public void setFirst(final T t) {
		this.first = t;
	}

	public void setSecond(final U u) {
		this.second = u;
	}

	@Override
	public String toString() {
		return "( " + this.first.toString() + "; " + this.second.toString()
		        + " )";
	}

}
