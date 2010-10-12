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

package maltcms.commands.filters.array;

import maltcms.commands.filters.AElementFilter;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.commands.ICommand;

/**
 * AArrayFilter applicable to Arrays of Array objects, returning Arrays of Array
 * objects. AArrayFilter Objects can be nested to allow hierarchical processing
 * of Arrays.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public abstract class AArrayFilter implements ICommand<Array[], Array[]> {

	protected transient AElementFilter ef = null;

	protected transient AArrayFilter parent = null;

	public AArrayFilter() {

	}

	public AArrayFilter(final AArrayFilter af) {
		this();
		this.parent = af;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.ucar.ma2.Filter#filter(java.lang.Object)
	 */
	public Array[] apply(final Array[] a) {
		return this.parent == null ? a : this.parent.apply(a);
	}

	public void configure(final Configuration cfg) {

	}

	public AElementFilter getFilter() {
		return this.ef;
	}

	public void setFilter(final AElementFilter ef1) {
		this.ef = ef1;
	}

}
