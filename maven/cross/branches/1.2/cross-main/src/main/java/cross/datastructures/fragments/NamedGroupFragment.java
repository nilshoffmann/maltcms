/*
 * 
 *
 * $Id$
 */

package cross.datastructures.fragments;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;

/**
 * Represents a VariableFragment group with a given (unique) name.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
public class NamedGroupFragment extends Fragment implements IGroupFragment {

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
