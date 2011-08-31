/*
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
 * 
 * $Id: NamedGroupFragment.java 43 2009-10-16 17:22:55Z nilshoffmann $
 */

package cross.datastructures.fragments;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.jdom.Element;
import org.slf4j.Logger;

import cross.Logging;

/**
 * Represents a VariableFragment group with a given (unique) name.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class NamedGroupFragment extends Fragment implements IGroupFragment {

	// protected static long GROUPID = 0;
	private final Logger log = Logging.getLogger(this.getClass());

	private ConcurrentHashMap<String, IVariableFragment> children = null;

	private String name = "";

	private long id = -1;

	private final IGroupFragment parentGroup;

	public NamedGroupFragment(final IGroupFragment parent, final String name1) {
		this.children = new ConcurrentHashMap<String, IVariableFragment>();
		this.parentGroup = parent;
		this.id = parent.nextGID();
		this.name = (name1 == null) ? "" + this.id : name1;
	}

	/**
	 * Add a number of children.
	 * 
	 * @param fragments
	 */
	public synchronized void addChildren(final IVariableFragment... fragments) {
		for (final IVariableFragment vf : fragments) {
			this.log.debug("Adding " + vf.getVarname() + " to group "
			        + getName());
			this.children.put(vf.getVarname(), vf);
		}
	}

	@Override
	public void appendXML(final Element e) {
		this.log.debug("Appending xml for named group " + getName());
		final Element group = new Element("namedGroup");
		super.appendAttributes(group);
		group.setAttribute("size", "" + this.children.size());
		group.setAttribute("groupID", "" + this.id);
		e.addContent(group);
		for (final IVariableFragment vf : this.children.values()) {
			vf.appendXML(group);
		}

	}

	public IVariableFragment getChild(final String varname) {
		if (hasChild(varname)) {
			return this.children.get(varname);
		} else {
			throw new IllegalArgumentException("No child with name " + varname
			        + "!");
		}

	}

	public String getName() {
		return this.name;
	}

	public IGroupFragment getParent() {
		return this.parentGroup;
	}

	public int getSize() {
		return this.children.size();
	}

	public boolean hasChild(final IVariableFragment vf) {
		return hasChild(vf.getVarname());
	}

	public boolean hasChild(final String varname) {
		return this.children.containsKey(varname);
	}

	public Iterator<IVariableFragment> iterator() {
		return this.children.values().iterator();
	}

	public long nextGID() {
		return this.parentGroup.nextGID();
	}

	public void setID(final long id1) {
		if (this.id == -1) {
			this.id = id1;
		}
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		for (final IVariableFragment vf : this.children.values()) {
			sb.append(vf.getVarname());
		}
		return "Members of group: " + this.name + "\n" + sb.toString();
	}

}