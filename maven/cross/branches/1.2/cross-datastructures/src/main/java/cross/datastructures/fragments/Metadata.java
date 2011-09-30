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

package cross.datastructures.fragments;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ucar.nc2.Attribute;

/**
 * Objects of this type hold netcdf associated metadata as Attributes,
 * accessible by name.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Metadata implements Iterable<Attribute> {

	protected HashMap<String, Attribute> hm = new HashMap<String, Attribute>();

	public Metadata() {

	}

	public Metadata(final List<Attribute> l) {
		this();
		for (final Attribute a : l) {
			add(a);
		}
	}

	public void add(final Attribute a) {
		final Attribute b = new Attribute(a.getName(), a);
		this.hm.put(b.getName(), b);
	}

	public Collection<Attribute> asCollection() {
		return this.hm.values();
	}

	public Attribute get(final String name) {
		return this.hm.get(name);
	}

	public boolean has(final String name) {
		return this.hm.containsKey(name);
	}

	@Override
	public Iterator<Attribute> iterator() {
		return this.hm.values().iterator();
	}

	public Collection<String> keySet() {
		return this.hm.keySet();
	}

}
