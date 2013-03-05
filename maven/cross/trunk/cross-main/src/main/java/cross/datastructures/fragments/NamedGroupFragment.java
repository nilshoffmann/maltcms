/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.datastructures.fragments;

import cross.datastructures.fragments.IGroupFragment;
import cross.datastructures.fragments.IVariableFragment;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;

/**
 * Represents a VariableFragment group with a given (unique) name.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class NamedGroupFragment extends Fragment implements IGroupFragment {

    // protected static long GROUPID = 0;
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
    @Override
    public synchronized void addChildren(final IVariableFragment... fragments) {
        for (final IVariableFragment vf : fragments) {
            log.debug("Adding " + vf.getName() + " to group "
                    + getName());
            this.children.put(vf.getName(), vf);
        }
    }

    @Override
    public void appendXML(final Element e) {
        log.debug("Appending xml for named group " + getName());
        final Element group = new Element("namedGroup");
        super.appendAttributes(group);
        group.setAttribute("size", "" + this.children.size());
        group.setAttribute("groupID", "" + this.id);
        e.addContent(group);
        for (final IVariableFragment vf : this.children.values()) {
            vf.appendXML(group);
        }

    }

    @Override
    public IVariableFragment getChild(final String varname) {
        if (hasChild(varname)) {
            return this.children.get(varname);
        } else {
            throw new IllegalArgumentException("No child with name " + varname
                    + "!");
        }

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public IGroupFragment getParent() {
        return this.parentGroup;
    }

    @Override
    public int getSize() {
        return this.children.size();
    }

    @Override
    public boolean hasChild(final IVariableFragment vf) {
        return hasChild(vf.getName());
    }

    @Override
    public boolean hasChild(final String varname) {
        return this.children.containsKey(varname);
    }

    @Override
    public Iterator<IVariableFragment> iterator() {
        return this.children.values().iterator();
    }

    @Override
    public long nextGID() {
        return this.parentGroup.nextGID();
    }

    @Override
    public void setID(final long id1) {
        if (this.id == -1) {
            this.id = id1;
        }
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        for (final IVariableFragment vf : this.children.values()) {
            sb.append(vf.getName());
        }
        return "Members of group: " + this.name + "\n" + sb.toString();
    }
}
