/*
 * $license$
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
