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

import cross.datastructures.StatsMap;
import cross.cache.ICacheDelegate;
import cross.datastructures.tools.FileTools;
import cross.exception.ResourceNotAvailableException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;
import java.util.*;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 * Immutable Variant of a FileFragment. All set operations will throw
 * UnsupportedOperationException. All other operations delegate to an instance
 * of IFileFragment, which is provided to the constructor.
 *
 * @author Nils Hoffmann
 *
 */
public class ImmutableFileFragment implements IFileFragment {

    private IFileFragment frag = null;

    public ImmutableFileFragment(final URI uri) {
//        if (FileFragment.hasFragment(uri)) {
//            this.frag = FileFragment.getFragment(uri);
//        } else {
            this.frag = new FileFragment(uri);
//        }
    }

    public ImmutableFileFragment(final File f) {
//        if (FileFragment.hasFragment(f.toURI())) {
//            this.frag = FileFragment.getFragment(f.toURI());
//        } else {
            this.frag = new FileFragment(f);
//        }
    }

    public ImmutableFileFragment(final File basedir, final String name) {
        this(new File(basedir, name));
    }

    public ImmutableFileFragment(final IFileFragment f) {
        this.frag = f;
    }

    @Override
    public IVariableFragment addChild(String name) {
        return this.frag.addChild(name);
    }

    /**
     * @param fragments
     * @see
     * cross.datastructures.fragments.IFileFragment#addChildren(cross.datastructures.fragments.IVariableFragment[])
     */
    @Override
    public void addChildren(final IVariableFragment... fragments) {
        this.frag.addChildren(fragments);
    }

    /**
     * @param dims1
     * @see
     * cross.datastructures.fragments.IFileFragment#addDimensions(ucar.nc2.Dimension[])
     */
    @Override
    public void addDimensions(final Dimension... dims1) {
        this.frag.addDimensions(dims1);
    }

    /**
     * @param c
     * @see
     * cross.datastructures.fragments.IFileFragment#addSourceFile(java.util.Collection)
     */
    @Override
    public void addSourceFile(final Collection<IFileFragment> c) {
        this.frag.addSourceFile(c);
    }

    /**
     * @param ff
     * @see
     * cross.datastructures.fragments.IFileFragment#addSourceFile(cross.datastructures.fragments.IFileFragment[])
     */
    @Override
    public void addSourceFile(final IFileFragment... ff) {
        this.frag.addSourceFile(ff);
    }

    /**
     * @param e
     * @see
     * cross.datastructures.fragments.IFileFragment#appendXML(org.jdom.Element)
     */
    @Override
    public void appendXML(final Element e) {
        this.frag.appendXML(e);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#clearArrays()
     */
    @Override
    public void clearArrays() throws IllegalStateException {
        this.frag.clearArrays();
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @see
     * cross.datastructures.fragments.IFragment#compare(cross.datastructures.fragments.IFragment,
     * cross.datastructures.fragments.IFragment)
     */
    @Override
    public int compare(final IFragment arg0, final IFragment arg1) {
        return this.frag.compare(arg0, arg1);
    }

    /**
     * @param arg0
     * @return
     * @see cross.datastructures.fragments.IFragment#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final Object arg0) {
        return this.frag.compareTo(arg0);
    }

    /**
     * @param obj
     * @return
     * @see java.util.Comparator#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        return this.frag.equals(obj);
    }

//    /**
//     * @return @see
//     * cross.datastructures.fragments.IFileFragment#getAbsolutePath()
//     */
    @Override
    public String getAbsolutePath() {
        return this.frag.getAbsolutePath();
    }

    /**
     * @param a
     * @return
     * @see
     * cross.datastructures.fragments.IFragment#getAttribute(ucar.nc2.Attribute)
     */
    @Override
    public Attribute getAttribute(final Attribute a) {
        return this.frag.getAttribute(a);
    }

    /**
     * @param name
     * @return
     * @see
     * cross.datastructures.fragments.IFragment#getAttribute(java.lang.String)
     */
    @Override
    public Attribute getAttribute(final String name) {
        return this.frag.getAttribute(name);
    }

    /**
     * @return @see cross.datastructures.fragments.IFragment#getAttributes()
     */
    @Override
    public List<Attribute> getAttributes() {
        return this.frag.getAttributes();
    }

    /**
     *
     * @return @see cross.datastructures.fragments.IFragment#getCache()
     */
    @Override
    public ICacheDelegate<IVariableFragment, List<Array>> getCache() {
        return this.frag.getCache();
    }

    /**
     * @param varname
     * @return
     * @throws ResourceNotAvailableException
     * @see
     * cross.datastructures.fragments.IFileFragment#getChild(java.lang.String)
     */
    @Override
    public IVariableFragment getChild(final String varname)
            throws ResourceNotAvailableException {
        return this.frag.getChild(varname, false);
    }

    /**
     * @param varname
     * @param loadStructureOnly
     * @return
     * @throws ResourceNotAvailableException
     * @see
     * {@link cross.datastructures.fragments.IFileFragment#getChild(String, boolean)}
     */
    @Override
    public IVariableFragment getChild(final String varname,
            final boolean loadStructureOnly)
            throws ResourceNotAvailableException {
        return this.frag.getChild(varname, loadStructureOnly);
    }

    /**
     *
     * @return @see
     * {@link cross.datastructures.fragments.IFileFragment#getImmediateChildren()}
     */
    @Override
    public List<IVariableFragment> getImmediateChildren() {
        return this.frag.getImmediateChildren();
    }

    /**
     * @return @see cross.datastructures.fragments.IFileFragment#getID()
     */
    @Override
    public long getID() {
        return this.frag.getID();
    }

    /**
     * @return @see cross.datastructures.fragments.IFileFragment#getName()
     */
    @Override
    public String getName() {
        return this.frag.getName();
    }

    /**
     * @return @see cross.datastructures.fragments.IFileFragment#getParent()
     */
    @Override
    public IGroupFragment getParent() {
        return this.frag.getParent();
    }

    /**
     * @return @see cross.datastructures.fragments.IFileFragment#getSize()
     */
    @Override
    public int getSize() {
        return this.frag.getSize();
    }

    /**
     * @return @see
     * cross.datastructures.fragments.IFileFragment#getSourceFiles()
     */
    @Override
    public Collection<IFileFragment> getSourceFiles() {
        final Collection<IFileFragment> c = this.frag.getSourceFiles();
        final ArrayList<IFileFragment> cret = new ArrayList<IFileFragment>();
        for (final IFileFragment ifrg : c) {
            cret.add(new ImmutableFileFragment(ifrg));
        }
        return cret;
    }

    /**
     * @return @see cross.datastructures.fragments.IFragment#getStats()
     */
    @Override
    public StatsMap getStats() {
        return this.frag.getStats();
    }

    /**
     * @param a
     * @return
     * @see
     * cross.datastructures.fragments.IFragment#hasAttribute(ucar.nc2.Attribute)
     */
    @Override
    public boolean hasAttribute(final Attribute a) {
        return this.frag.hasAttribute(a);
    }

    /**
     * @param name
     * @return
     * @see
     * cross.datastructures.fragments.IFragment#hasAttribute(java.lang.String)
     */
    @Override
    public boolean hasAttribute(final String name) {
        return this.frag.hasAttribute(name);
    }

    /**
     * @param vf
     * @return
     * @see
     * cross.datastructures.fragments.IGroupFragment#hasChild(cross.datastructures.fragments.IVariableFragment)
     */
    @Override
    public boolean hasChild(final IVariableFragment vf) {
        return this.frag.hasChild(vf);
    }

    /**
     * @param varname
     * @return
     * @see
     * cross.datastructures.fragments.IGroupFragment#hasChild(java.lang.String)
     */
    @Override
    public boolean hasChild(final String varname) {
        return this.frag.hasChild(varname);
    }

    /**
     * @param vf
     * @return
     * @see
     * cross.datastructures.fragments.IFileFragment#hasChildren(cross.datastructures.fragments.IVariableFragment[])
     */
    @Override
    public boolean hasChildren(final IVariableFragment... vf) {
        return this.frag.hasChildren(vf);
    }

    /**
     * @param s
     * @return
     * @see
     * cross.datastructures.fragments.IFileFragment#hasChildren(java.lang.String[])
     */
    @Override
    public boolean hasChildren(final String... s) {
        return this.frag.hasChildren(s);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#isModified()
     */
    @Override
    public boolean isModified() {
        return false;
    }

    /**
     * @return @see cross.datastructures.fragments.IFileFragment#iterator()
     */
    @Override
    public Iterator<IVariableFragment> iterator() {
        final ArrayList<IVariableFragment> al = new ArrayList<IVariableFragment>();
        final Iterator<IVariableFragment> iter = this.frag.iterator();
        while (iter.hasNext()) {
            al.add(new ImmutableVariableFragment(iter.next()));
        }
        return al.iterator();
    }

    /**
     * @return @see cross.datastructures.fragments.IGroupFragment#nextGID()
     */
    @Override
    public long nextGID() {
        return this.frag.nextGID();
    }

    @Override
    public void readStructure() {
        this.frag.readStructure();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException,
            ClassNotFoundException {
        Object o = in.readObject();
        if (o instanceof Long) {
            setID(((Long) o).longValue());
        }
        o = in.readObject();
        if (o instanceof String) {
            this.frag.setFile(URI.create(FileTools.escapeUri((String) o)).toString());
        }
        in.close();
    }

    /**
     * @param variableFragment
     * @see
     * cross.datastructures.fragments.IFileFragment#removeChild(cross.datastructures.fragments.IVariableFragment)
     */
    @Override
    public void removeChild(final IVariableFragment variableFragment) {
        this.frag.removeChild(variableFragment);
    }

    /*
     * (non-Javadoc)
     *
     * @seecross.datastructures.fragments.IFileFragment#removeSourceFile(cross.
     * datastructures.fragments.IFileFragment)
     */
    @Override
    public void removeSourceFile(final IFileFragment ff) {
        this.frag.removeSourceFile(ff);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#removeSourceFiles()
     */
    @Override
    public void removeSourceFiles() {
        this.frag.removeSourceFiles();
    }

    /**
     * @return @see cross.datastructures.fragments.IFileFragment#save()
     */
    @Override
    public boolean save() {
        throw new UnsupportedOperationException("Can not save immutable fragment!");
//        return this.frag.save();
    }

    /**
     * @param a
     * @see
     * cross.datastructures.fragments.IFragment#setAttributes(ucar.nc2.Attribute[])
     */
    @Override
    public void setAttributes(final Attribute... a) {
        this.frag.setAttributes(a);
    }

    @Override
    public void addAttribute(Attribute a) {
        this.frag.addAttribute(a);
    }

    /**
     * Sets the array cache of this FileFragment as specified if the current
     * cache has not yet been initialized (is null). Throws an
     *
     * @see IllegalStateException otherwise to prevent loss of cached data.
     *
     * @throws IllegalStateException
     */
    @Override
    public void setCache(ICacheDelegate<IVariableFragment, List<Array>> persistentCache) {
        this.frag.setCache(persistentCache);
    }

    /**
     * @param f1
     * @see cross.datastructures.fragments.IFileFragment#setFile(java.io.File)
     */
    @Override
    public void setFile(final File f1) {
        this.frag.setFile(f1);
    }

    /**
     * @param file
     * @see
     * cross.datastructures.fragments.IFileFragment#setFile(java.lang.String)
     */
    @Override
    public void setFile(final String file) {
        this.frag.setFile(file);
    }

    /**
     * @param id
     * @see cross.datastructures.fragments.IGroupFragment#setID(long)
     */
    @Override
    public void setID(final long id) {
        this.frag.setID(id);
    }

    /**
     * @param stats1
     * @see
     * cross.datastructures.fragments.IFragment#setStats(cross.datastructures.StatsMap)
     */
    @Override
    public void setStats(final StatsMap stats1) {
        this.frag.setStats(stats1);
    }

    /**
     * @return @see cross.datastructures.fragments.IFileFragment#toString()
     */
    @Override
    public String toString() {
        return this.frag.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    /**
     *
     * @param out
     * @throws IOException
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // store id
        out.writeObject(Long.valueOf(getID()));
        // store path to storage
        out.writeObject(getUri());
        out.flush();
        out.close();
    }

    /**
     * The registered dimensions of this FileFragment as an unmodifiable Set.
     *
     * @return
     */
    @Override
    public Set<Dimension> getDimensions() {
        return this.frag.getDimensions();
    }

    @Override
    public void clearDimensions() {
        this.frag.clearDimensions();
    }

    @Override
    public URI getUri() {
        return this.frag.getUri();
    }
}
