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
 * $Id$
 */

package cross.datastructures.tuple;

/**
 * Class representing a tuple of two elements with Types T and U.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Tuple2D<T, U> {

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
		return "( " + this.first + "; " + this.second + " )";
	}

}
