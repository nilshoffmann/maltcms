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
import org.jdom2.Element;
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

    /**
     * <p>Constructor for Experiment1D.</p>
     */
    public Experiment1D() {
        super();
    }

    /**
     * <p>Constructor for Experiment1D.</p>
     *
     * @param ff1 a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public Experiment1D(final IFileFragment ff1) {
        this.ff = ff1;
    }

    /** {@inheritDoc} */
    @Override
    public IVariableFragment addChild(String name) {
        return this.ff.addChild(name);
    }

    /** {@inheritDoc} */
    @Override
    public void addChildren(final IVariableFragment... fragments) {
        this.ff.addChildren(fragments);
    }

    /** {@inheritDoc} */
    @Override
    public void addDimensions(final Dimension... dims1) {
        this.ff.addDimensions(dims1);
    }

    /** {@inheritDoc} */
    @Override
    public void addSourceFile(final Collection<IFileFragment> c) {
        this.ff.addSourceFile(c);
    }

    /** {@inheritDoc} */
    @Override
    public void addSourceFile(final IFileFragment... ff) {
        this.ff.addSourceFile(ff);
    }

    /** {@inheritDoc} */
    @Override
    public void appendXML(final Element e) {
        this.ff.appendXML(e);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#clearArrays()
     */
    /** {@inheritDoc} */
    @Override
    public void clearArrays() throws IllegalStateException {
        this.ff.clearArrays();
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#clearDimensions()
     */
    /** {@inheritDoc} */
    @Override
    public void clearDimensions() {
        this.ff.clearDimensions();
    }

    /** {@inheritDoc} */
    @Override
    public int compare(final IFragment arg0, final IFragment arg1) {
        return this.ff.compare(arg0, arg1);
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(final Object arg0) {
        return this.ff.compareTo(arg0);
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        return this.ff.equals(obj);
    }

    /** {@inheritDoc} */
    @Override
    public String getAbsolutePath() {
        return this.ff.getAbsolutePath();
    }

    /** {@inheritDoc} */
    @Override
    public Attribute getAttribute(final Attribute a) {
        return this.ff.getAttribute(a);
    }

    /** {@inheritDoc} */
    @Override
    public Attribute getAttribute(final String name) {
        return this.ff.getAttribute(name);
    }

    /** {@inheritDoc} */
    @Override
    public List<Attribute> getAttributes() {
        return this.ff.getAttributes();
    }

    /** {@inheritDoc} */
    @Override
    public ICacheDelegate<IVariableFragment, List<Array>> getCache() {
        return this.ff.getCache();
    }

    /** {@inheritDoc} */
    @Override
    public IVariableFragment getChild(final String varname) {
        return this.ff.getChild(varname);
    }

    /** {@inheritDoc} */
    @Override
    public IVariableFragment getChild(final String varname,
            final boolean loadStructureOnly) {
        return this.ff.getChild(varname, loadStructureOnly);
    }

    /** {@inheritDoc} */
    @Override
    public IChromatogram1D getChromatogram() {
        return this.chrom;
    }

    /** {@inheritDoc} */
    @Override
    public IFileFragment getFileFragment() {
        return this.ff;
    }

    /** {@inheritDoc} */
    @Override
    public long getID() {
        return this.ff.getID();
    }

    /** {@inheritDoc} */
    @Override
    public List<IVariableFragment> getImmediateChildren() {
        return this.ff.getImmediateChildren();
    }

    /** {@inheritDoc} */
    @Override
    public HashMap<String, String> getMetadata() {
        if (this.metaData == null) {
            this.metaData = new HashMap<>();
        }
        return this.metaData;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return this.ff.getName();
    }

    /** {@inheritDoc} */
    @Override
    public IGroupFragment getParent() {
        return this.ff.getParent();
    }

    /** {@inheritDoc} */
    @Override
    public int getSize() {
        return this.ff.getSize();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<IFileFragment> getSourceFiles() {
        return this.ff.getSourceFiles();
    }

    /** {@inheritDoc} */
    @Override
    public StatsMap getStats() {
        return this.ff.getStats();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasAttribute(final Attribute a) {
        return this.ff.hasAttribute(a);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasAttribute(final String name) {
        return this.ff.hasAttribute(name);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChild(final IVariableFragment vf) {
        return this.ff.hasChild(vf);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChild(final String varname) {
        return this.ff.hasChild(varname);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChildren(final IVariableFragment... vf) {
        return this.ff.hasChildren(vf);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChildren(final String... s) {
        return this.ff.hasChildren(s);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#isModified()
     */
    /** {@inheritDoc} */
    @Override
    public boolean isModified() {
        return this.ff.isModified();
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<IVariableFragment> iterator() {
        return this.ff.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public long nextGID() {
        return this.ff.nextGID();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    /** {@inheritDoc} */
    @Override
    public void readExternal(final ObjectInput in) throws IOException,
            ClassNotFoundException {
        this.ff.readExternal(in);
    }

    /** {@inheritDoc} */
    @Override
    public void removeChild(final IVariableFragment variableFragment) {
        this.ff.removeChild(variableFragment);
    }

    /** {@inheritDoc} */
    @Override
    public void removeSourceFile(final IFileFragment ff) {
        this.ff.removeSourceFile(ff);
    }

    /** {@inheritDoc} */
    @Override
    public void removeSourceFiles() {
        this.ff.removeSourceFiles();
    }

    /** {@inheritDoc} */
    @Override
    public boolean save() {
        return this.ff.save();
    }

    /** {@inheritDoc} */
    @Override
    public void setAttributes(final Attribute... a) {
        this.ff.setAttributes(a);
    }

    /** {@inheritDoc} */
    @Override
    public void setChromatogram(final IChromatogram1D c) {
        this.chrom = c;
    }

    /** {@inheritDoc} */
    @Override
    public void setFile(final File f1) {
        this.ff.setFile(f1);
    }

    /** {@inheritDoc} */
    @Override
    public void setFile(final String file) {
        this.ff.setFile(file);
    }

    /** {@inheritDoc} */
    @Override
    public void setFileFragment(final IFileFragment ff1) {
        this.ff = ff1;
    }

    /** {@inheritDoc} */
    @Override
    public void setID(final long id) {
        this.ff.setID(id);
    }

    /** {@inheritDoc} */
    @Override
    public void setMetadata(final String key, final String value) {
        this.metaData.put(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public void setStats(final StatsMap stats1) {
        this.ff.setStats(stats1);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.ff.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    /** {@inheritDoc} */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        this.ff.writeExternal(out);
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.ms.IExperiment#getMetadata(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public String getMetadata(String key) throws ResourceNotAvailableException {
        if (this.metaData.containsKey(key)) {
            return this.metaData.get(key);
        }
        throw new ResourceNotAvailableException("Metadata key " + key
                + " is unknown!");
    }

    /** {@inheritDoc} */
    @Override
    public void addAttribute(Attribute atrbt) {
        this.ff.addAttribute(atrbt);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Dimension> getDimensions() {
        return this.ff.getDimensions();
    }

    /** {@inheritDoc} */
    @Override
    public URI getUri() {
        return this.ff.getUri();
    }

    /** {@inheritDoc} */
    @Override
    public void setCache(ICacheDelegate<IVariableFragment, List<Array>> cache) {
        this.ff.setCache(cache);
    }

    /** {@inheritDoc} */
    @Override
    public void readStructure() {
        this.ff.readStructure();
    }
}
