/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.datastructures.ms;

import cross.cache.ICacheDelegate;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IFragment;
import cross.datastructures.fragments.IGroupFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.exception.ResourceNotAvailableException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.configuration.Configuration;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 * Concrete implementation of an Experiment containing a 1-dimensional
 * chromatogram.
 *
 * @author Nils Hoffmann
 *
 */
public class Experiment1D implements IExperiment1D {

    private IFileFragment ff = null;
    private IChromatogram1D chrom = null;
    private HashMap<String, String> metaData = null;

    public Experiment1D() {
        super();
    }

    public Experiment1D(final IFileFragment ff1) {
        this.ff = ff1;
    }

    @Override
    public IVariableFragment addChild(String name) {
        return this.ff.addChild(name);
    }

    /**
     * @param fragments
     * @see
     * cross.datastructures.fragments.IFileFragment#addChildren(cross.datastructures.fragments.IVariableFragment[])
     */
    public void addChildren(final IVariableFragment... fragments) {
        this.ff.addChildren(fragments);
    }

    /**
     * @param dims1
     * @see
     * cross.datastructures.fragments.IFileFragment#addDimensions(ucar.nc2.Dimension[])
     */
    public void addDimensions(final Dimension... dims1) {
        this.ff.addDimensions(dims1);
    }

    /**
     * @param c
     * @see
     * cross.datastructures.fragments.IFileFragment#addSourceFile(java.util.Collection)
     */
    public void addSourceFile(final Collection<IFileFragment> c) {
        this.ff.addSourceFile(c);
    }

    /**
     * @param ff
     * @see
     * cross.datastructures.fragments.IFileFragment#addSourceFile(cross.datastructures.fragments.IFileFragment[])
     */
    public void addSourceFile(final IFileFragment... ff) {
        this.ff.addSourceFile(ff);
    }

    /**
     * @param e
     * @see
     * cross.datastructures.fragments.IFileFragment#appendXML(org.jdom.Element)
     */
    public void appendXML(final Element e) {
        this.ff.appendXML(e);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#clearArrays()
     */
    @Override
    public void clearArrays() throws IllegalStateException {
        this.ff.clearArrays();
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#clearDimensions()
     */
    @Override
    public void clearDimensions() {
        this.ff.clearDimensions();
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @see
     * cross.datastructures.fragments.IFragment#compare(cross.datastructures.fragments.IFragment,
     * cross.datastructures.fragments.IFragment)
     */
    public int compare(final IFragment arg0, final IFragment arg1) {
        return this.ff.compare(arg0, arg1);
    }

    /**
     * @param arg0
     * @return
     * @see cross.datastructures.fragments.IFragment#compareTo(java.lang.Object)
     */
    public int compareTo(final Object arg0) {
        return this.ff.compareTo(arg0);
    }

    @Override
    public void configure(final Configuration cfg) {
    }

    /**
     * @param obj
     * @return
     * @see java.util.Comparator#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        return this.ff.equals(obj);
    }

    /**
     * @return @see
     *         cross.datastructures.fragments.IFileFragment#getAbsolutePath()
     */
    public String getAbsolutePath() {
        return this.ff.getAbsolutePath();
    }

    /**
     * @param a
     * @return
     * @see
     * cross.datastructures.fragments.IFragment#getAttribute(ucar.nc2.Attribute)
     */
    public Attribute getAttribute(final Attribute a) {
        return this.ff.getAttribute(a);
    }

    /**
     * @param name
     * @return
     * @see
     * cross.datastructures.fragments.IFragment#getAttribute(java.lang.String)
     */
    public Attribute getAttribute(final String name) {
        return this.ff.getAttribute(name);
    }

    /**
     * @return @see cross.datastructures.fragments.IFragment#getAttributes()
     */
    public List<Attribute> getAttributes() {
        return this.ff.getAttributes();
    }

    @Override
    public ICacheDelegate<IVariableFragment, List<Array>> getCache() {
        return this.ff.getCache();
    }

    /**
     * @param varname
     * @return
     * @see
     * cross.datastructures.fragments.IGroupFragment#getChild(java.lang.String)
     */
    public IVariableFragment getChild(final String varname) {
        return this.ff.getChild(varname);
    }

    /**
     * @param varname
     * @param loadStructureOnly
     * @return
     * @see cross.datastructures.fragments.IGroupFragment#getChild(String)
     */
    public IVariableFragment getChild(final String varname,
        final boolean loadStructureOnly) {
        return this.ff.getChild(varname, loadStructureOnly);
    }

    public IChromatogram1D getChromatogram() {
        return this.chrom;
    }

    public IFileFragment getFileFragment() {
        return this.ff;
    }

    /**
     * @return @see cross.datastructures.fragments.IFileFragment#getID()
     */
    public long getID() {
        return this.ff.getID();
    }

    @Override
    public List<IVariableFragment> getImmediateChildren() {
        return this.ff.getImmediateChildren();
    }

    public HashMap<String, String> getMetadata() {
        if (this.metaData == null) {
            this.metaData = new HashMap<String, String>();
        }
        return this.metaData;
    }

    /**
     * @return @see cross.datastructures.fragments.IFileFragment#getName()
     */
    public String getName() {
        return this.ff.getName();
    }

    /**
     * @return @see cross.datastructures.fragments.IFileFragment#getParent()
     */
    public IGroupFragment getParent() {
        return this.ff.getParent();
    }

    /**
     * @return @see cross.datastructures.fragments.IFileFragment#getSize()
     */
    public int getSize() {
        return this.ff.getSize();
    }

    /**
     * @return @see
     *         cross.datastructures.fragments.IFileFragment#getSourceFiles()
     */
    public Collection<IFileFragment> getSourceFiles() {
        return this.ff.getSourceFiles();
    }

    /**
     * @return @see cross.datastructures.fragments.IFragment#getStats()
     */
    public StatsMap getStats() {
        return this.ff.getStats();
    }

    /**
     * @param a
     * @return
     * @see
     * cross.datastructures.fragments.IFragment#hasAttribute(ucar.nc2.Attribute)
     */
    public boolean hasAttribute(final Attribute a) {
        return this.ff.hasAttribute(a);
    }

    /**
     * @param name
     * @return
     * @see
     * cross.datastructures.fragments.IFragment#hasAttribute(java.lang.String)
     */
    public boolean hasAttribute(final String name) {
        return this.ff.hasAttribute(name);
    }

    /**
     * @param vf
     * @return
     * @see
     * cross.datastructures.fragments.IGroupFragment#hasChild(cross.datastructures.fragments.IVariableFragment)
     */
    public boolean hasChild(final IVariableFragment vf) {
        return this.ff.hasChild(vf);
    }

    /**
     * @param varname
     * @return
     * @see
     * cross.datastructures.fragments.IGroupFragment#hasChild(java.lang.String)
     */
    public boolean hasChild(final String varname) {
        return this.ff.hasChild(varname);
    }

    /**
     * @param vf
     * @return
     * @see
     * cross.datastructures.fragments.IFileFragment#hasChildren(cross.datastructures.fragments.IVariableFragment[])
     */
    public boolean hasChildren(final IVariableFragment... vf) {
        return this.ff.hasChildren(vf);
    }

    /**
     * @param s
     * @return
     * @see
     * cross.datastructures.fragments.IFileFragment#hasChildren(java.lang.String[])
     */
    public boolean hasChildren(final String... s) {
        return this.ff.hasChildren(s);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#isModified()
     */
    @Override
    public boolean isModified() {
        return this.ff.isModified();
    }

    /**
     * @return @see cross.datastructures.fragments.IFileFragment#iterator()
     */
    public Iterator<IVariableFragment> iterator() {
        return this.ff.iterator();
    }

    /**
     * @return @see cross.datastructures.fragments.IGroupFragment#nextGID()
     */
    public long nextGID() {
        return this.ff.nextGID();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException,
        ClassNotFoundException {
        this.ff.readExternal(in);
    }

    /**
     * @param variableFragment
     * @see
     * cross.datastructures.fragments.IFileFragment#removeChild(cross.datastructures.fragments.IVariableFragment)
     */
    public void removeChild(final IVariableFragment variableFragment) {
        this.ff.removeChild(variableFragment);
    }

    /**
     * @param ff
     * @see
     * cross.datastructures.fragments.IFileFragment#removeSourceFile(cross.datastructures.fragments.IFileFragment)
     */
    public void removeSourceFile(final IFileFragment ff) {
        this.ff.removeSourceFile(ff);
    }

    /**
     *
     * @see cross.datastructures.fragments.IFileFragment#removeSourceFiles()
     */
    public void removeSourceFiles() {
        this.ff.removeSourceFiles();
    }

    /**
     * @return @see cross.datastructures.fragments.IFileFragment#save()
     */
    public boolean save() {
        return this.ff.save();
    }

    /**
     * @param a
     * @see
     * cross.datastructures.fragments.IFragment#setAttributes(ucar.nc2.Attribute[])
     */
    public void setAttributes(final Attribute... a) {
        this.ff.setAttributes(a);
    }

    public void setChromatogram(final IChromatogram1D c) {
        this.chrom = c;
    }

    /**
     * @param f1
     * @see cross.datastructures.fragments.IFileFragment#setFile(java.io.File)
     */
    public void setFile(final File f1) {
        this.ff.setFile(f1);
    }

    /**
     * @param file
     * @see
     * cross.datastructures.fragments.IFileFragment#setFile(java.lang.String)
     */
    public void setFile(final String file) {
        this.ff.setFile(file);
    }

    public void setFileFragment(final IFileFragment ff1) {
        this.ff = ff1;
    }

    /**
     * @param id
     * @see cross.datastructures.fragments.IGroupFragment#setID(long)
     */
    public void setID(final long id) {
        this.ff.setID(id);
    }

    public void setMetadata(final String key, final String value) {
        this.metaData.put(key, value);
    }

    /**
     * @param stats1
     * @see
     * cross.datastructures.fragments.IFragment#setStats(cross.datastructures.StatsMap)
     */
    public void setStats(final StatsMap stats1) {
        this.ff.setStats(stats1);
    }

    /**
     * @return @see cross.datastructures.fragments.IFileFragment#toString()
     */
    @Override
    public String toString() {
        return this.ff.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        this.ff.writeExternal(out);
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.ms.IExperiment#getMetadata(java.lang.String)
     */
    @Override
    public String getMetadata(String key) throws ResourceNotAvailableException {
        if (this.metaData.containsKey(key)) {
            return this.metaData.get(key);
        }
        throw new ResourceNotAvailableException("Metadata key " + key
            + " is unknown!");
    }

    @Override
    public void addAttribute(Attribute atrbt) {
        this.ff.addAttribute(atrbt);
    }

    @Override
    public Set<Dimension> getDimensions() {
        return this.ff.getDimensions();
    }

    @Override
    public URI getUri() {
        return this.ff.getUri();
    }

    @Override
    public void setCache(ICacheDelegate<IVariableFragment, List<Array>> cache) {
        this.ff.setCache(cache);
    }

    @Override
    public void readStructure() {
        this.ff.readStructure();
    }
}
