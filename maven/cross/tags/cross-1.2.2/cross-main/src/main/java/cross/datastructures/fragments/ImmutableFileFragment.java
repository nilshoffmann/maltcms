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
 * $Id: ImmutableFileFragment.java 73 2009-12-16 08:45:14Z nilshoffmann $
 */
/**
 * 
 */
package cross.datastructures.fragments;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import cross.datastructures.StatsMap;
import cross.exception.ResourceNotAvailableException;

/**
 * Immutable Variant of a FileFragment. All set operations will throw
 * UnsupportedOperationException. All other operations delegate to an instance
 * of IFileFragment, which is provided to the constructor.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
public class ImmutableFileFragment implements IFileFragment {

    private IFileFragment frag = null;

    public ImmutableFileFragment(final File f) {
        this.frag = new FileFragment(f);
    }

    public ImmutableFileFragment(final File basedir, final String name) {
        this(new File(basedir, name));
    }

    public ImmutableFileFragment(final IFileFragment f) {
        // EvalTools.notNull(f, this);
        this.frag = f;
    }

    /**
     * @param fragments
     * @see cross.datastructures.fragments.IFileFragment#addChildren(cross.datastructures.fragments.IVariableFragment[])
     */
    public void addChildren(final IVariableFragment... fragments) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param dims1
     * @see cross.datastructures.fragments.IFileFragment#addDimensions(ucar.nc2.Dimension[])
     */
    public void addDimensions(final Dimension... dims1) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param c
     * @see cross.datastructures.fragments.IFileFragment#addSourceFile(java.util.Collection)
     */
    public void addSourceFile(final Collection<IFileFragment> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param ff
     * @see cross.datastructures.fragments.IFileFragment#addSourceFile(cross.datastructures.fragments.IFileFragment[])
     */
    public void addSourceFile(final IFileFragment... ff) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param e
     * @see cross.datastructures.fragments.IFileFragment#appendXML(org.jdom.Element)
     */
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
        throw new IllegalStateException(
                "Can not clear arrays on immutable fragment!");
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @see cross.datastructures.fragments.IFragment#compare(cross.datastructures.fragments.IFragment,
     *      cross.datastructures.fragments.IFragment)
     */
    public int compare(final IFragment arg0, final IFragment arg1) {
        return this.frag.compare(arg0, arg1);
    }

    /**
     * @param arg0
     * @return
     * @see cross.datastructures.fragments.IFragment#compareTo(java.lang.Object)
     */
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

    /**
     * @return
     * @see cross.datastructures.fragments.IFileFragment#getAbsolutePath()
     */
    public String getAbsolutePath() {
        return this.frag.getAbsolutePath();
    }

    /**
     * @param a
     * @return
     * @see cross.datastructures.fragments.IFragment#getAttribute(ucar.nc2.Attribute)
     */
    public Attribute getAttribute(final Attribute a) {
        return this.frag.getAttribute(a);
    }

    /**
     * @param name
     * @return
     * @see cross.datastructures.fragments.IFragment#getAttribute(java.lang.String)
     */
    public Attribute getAttribute(final String name) {
        return this.frag.getAttribute(name);
    }

    /**
     * @return
     * @see cross.datastructures.fragments.IFragment#getAttributes()
     */
    public List<Attribute> getAttributes() {
        return this.frag.getAttributes();
    }

    /**
     * @param varname
     * @return
     * @throws ResourceNotAvailableException
     * @see cross.datastructures.fragments.IFileFragment#getChild(java.lang.String)
     */
    public IVariableFragment getChild(final String varname)
            throws ResourceNotAvailableException {
        return new ImmutableVariableFragment(this.frag.getChild(varname));
    }

    /**
     * @param varname
     * @param loadStructureOnly
     * @return
     * @throws ResourceNotAvailableException
     * @see {@link cross.datastructures.fragments.IFileFragment#getChild(String, boolean)}
     */
    public IVariableFragment getChild(final String varname,
            final boolean loadStructureOnly)
            throws ResourceNotAvailableException {
        return new ImmutableVariableFragment(this.frag.getChild(varname));
    }

    /**
     * @return
     * @see cross.datastructures.fragments.IFileFragment#getID()
     */
    public long getID() {
        return this.frag.getID();
    }

    /**
     * @return
     * @see cross.datastructures.fragments.IFileFragment#getName()
     */
    public String getName() {
        return this.frag.getName();
    }

    /**
     * @return
     * @see cross.datastructures.fragments.IFileFragment#getParent()
     */
    public IGroupFragment getParent() {
        return this.frag.getParent();
    }

    /**
     * @return
     * @see cross.datastructures.fragments.IFileFragment#getSize()
     */
    public int getSize() {
        return this.frag.getSize();
    }

    /**
     * @return
     * @see cross.datastructures.fragments.IFileFragment#getSourceFiles()
     */
    public Collection<IFileFragment> getSourceFiles() {
        final Collection<IFileFragment> c = this.frag.getSourceFiles();
        final ArrayList<IFileFragment> cret = new ArrayList<IFileFragment>();
        for (final IFileFragment ifrg : c) {
            cret.add(new ImmutableFileFragment(ifrg));
        }
        return cret;
    }

    /**
     * @return
     * @see cross.datastructures.fragments.IFragment#getStats()
     */
    public StatsMap getStats() {
        return this.frag.getStats();
    }

    /**
     * @param a
     * @return
     * @see cross.datastructures.fragments.IFragment#hasAttribute(ucar.nc2.Attribute)
     */
    public boolean hasAttribute(final Attribute a) {
        return this.frag.hasAttribute(a);
    }

    /**
     * @param name
     * @return
     * @see cross.datastructures.fragments.IFragment#hasAttribute(java.lang.String)
     */
    public boolean hasAttribute(final String name) {
        return this.frag.hasAttribute(name);
    }

    /**
     * @param vf
     * @return
     * @see cross.datastructures.fragments.IGroupFragment#hasChild(cross.datastructures.fragments.IVariableFragment)
     */
    public boolean hasChild(final IVariableFragment vf) {
        return this.frag.hasChild(vf);
    }

    /**
     * @param varname
     * @return
     * @see cross.datastructures.fragments.IGroupFragment#hasChild(java.lang.String)
     */
    public boolean hasChild(final String varname) {
        return this.frag.hasChild(varname);
    }

    /**
     * @param vf
     * @return
     * @see cross.datastructures.fragments.IFileFragment#hasChildren(cross.datastructures.fragments.IVariableFragment[])
     */
    public boolean hasChildren(final IVariableFragment... vf) {
        return this.frag.hasChildren(vf);
    }

    /**
     * @param s
     * @return
     * @see cross.datastructures.fragments.IFileFragment#hasChildren(java.lang.String[])
     */
    public boolean hasChildren(final String... s) {
        return this.frag.hasChildren(s);
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.fragments.IFileFragment#isModified()
     */
    public boolean isModified() {
        return false;
    }

    /**
     * @return
     * @see cross.datastructures.fragments.IFileFragment#iterator()
     */
    public Iterator<IVariableFragment> iterator() {
        final ArrayList<IVariableFragment> al = new ArrayList<IVariableFragment>();
        final Iterator<IVariableFragment> iter = this.frag.iterator();
        while (iter.hasNext()) {
            al.add(new ImmutableVariableFragment(iter.next()));
        }
        return al.iterator();
    }

    /**
     * @return
     * @see cross.datastructures.fragments.IGroupFragment#nextGID()
     */
    public long nextGID() {
        return this.frag.nextGID();
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
            setFile(new File((String) o));
        }
        in.close();
    }

    /**
     * @param variableFragment
     * @see cross.datastructures.fragments.IFileFragment#removeChild(cross.datastructures.fragments.IVariableFragment)
     */
    public void removeChild(final IVariableFragment variableFragment) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @seecross.datastructures.fragments.IFileFragment#removeSourceFile(cross.
     * datastructures.fragments.IFileFragment)
     */
    @Override
    public void removeSourceFile(final IFileFragment ff) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.fragments.IFileFragment#removeSourceFiles()
     */
    @Override
    public void removeSourceFiles() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return
     * @see cross.datastructures.fragments.IFileFragment#save()
     */
    public boolean save() {
        return this.frag.save();
    }

    /**
     * @param a
     * @see cross.datastructures.fragments.IFragment#setAttributes(ucar.nc2.Attribute[])
     */
    public void setAttributes(final Attribute... a) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param f1
     * @see cross.datastructures.fragments.IFileFragment#setFile(java.io.File)
     */
    public void setFile(final File f1) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param file
     * @see cross.datastructures.fragments.IFileFragment#setFile(java.lang.String)
     */
    public void setFile(final String file) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param id
     * @see cross.datastructures.fragments.IGroupFragment#setID(long)
     */
    public void setID(final long id) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param stats1
     * @see cross.datastructures.fragments.IFragment#setStats(cross.datastructures.StatsMap)
     */
    public void setStats(final StatsMap stats1) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return
     * @see cross.datastructures.fragments.IFileFragment#toString()
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
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // store id
        out.writeObject(Long.valueOf(getID()));
        // store path to storage
        out.writeObject(getAbsolutePath());
        out.flush();
        out.close();
    }
}
