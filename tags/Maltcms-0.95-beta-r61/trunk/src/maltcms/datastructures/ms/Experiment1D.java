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

package maltcms.datastructures.ms;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.jdom.Element;
import org.slf4j.Logger;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import cross.Logging;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IFragment;
import cross.datastructures.fragments.IGroupFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.tools.EvalTools;
import cross.tools.FragmentTools;

/**
 * Concrete implementation of an Experiment containing a 1-dimensional
 * chromatogram.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Experiment1D implements IExperiment1D {

	private final Logger log = Logging.getLogger(this);

	private IFileFragment ff = null;

	private IChromatogram1D chrom = null;

	private HashMap<String, String> metaData = null;

	private IVariableFragment masses, intensities, scan_acquisition_time;

	public Experiment1D() {

	}

	public Experiment1D(final IFileFragment ff1) {
		this.ff = ff1;
	}

	/**
	 * @param fragments
	 * @see cross.datastructures.fragments.IFileFragment#addChildren(cross.datastructures.fragments.IVariableFragment[])
	 */
	public void addChildren(final IVariableFragment... fragments) {
		this.ff.addChildren(fragments);
	}

	/**
	 * @param dims1
	 * @see cross.datastructures.fragments.IFileFragment#addDimensions(ucar.nc2.Dimension[])
	 */
	public void addDimensions(final Dimension... dims1) {
		this.ff.addDimensions(dims1);
	}

	/**
	 * @param c
	 * @see cross.datastructures.fragments.IFileFragment#addSourceFile(java.util.Collection)
	 */
	public void addSourceFile(final Collection<IFileFragment> c) {
		this.ff.addSourceFile(c);
	}

	/**
	 * @param ff
	 * @see cross.datastructures.fragments.IFileFragment#addSourceFile(cross.datastructures.fragments.IFileFragment[])
	 */
	public void addSourceFile(final IFileFragment... ff) {
		this.ff.addSourceFile(ff);
	}

	/**
	 * @param e
	 * @see cross.datastructures.fragments.IFileFragment#appendXML(org.jdom.Element)
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

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @see cross.datastructures.fragments.IFragment#compare(cross.datastructures.fragments.IFragment,
	 *      cross.datastructures.fragments.IFragment)
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
	 * @return
	 * @see cross.datastructures.fragments.IFileFragment#getAbsolutePath()
	 */
	public String getAbsolutePath() {
		return this.ff.getAbsolutePath();
	}

	/**
	 * @param a
	 * @return
	 * @see cross.datastructures.fragments.IFragment#getAttribute(ucar.nc2.Attribute)
	 */
	public Attribute getAttribute(final Attribute a) {
		return this.ff.getAttribute(a);
	}

	/**
	 * @param name
	 * @return
	 * @see cross.datastructures.fragments.IFragment#getAttribute(java.lang.String)
	 */
	public Attribute getAttribute(final String name) {
		return this.ff.getAttribute(name);
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IFragment#getAttributes()
	 */
	public List<Attribute> getAttributes() {
		return this.ff.getAttributes();
	}

	/**
	 * @param varname
	 * @return
	 * @see cross.datastructures.fragments.IGroupFragment#getChild(java.lang.String)
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
		if (this.chrom == null) {
			if (this.masses == null) {
				// try {
				this.masses = FragmentTools.getVariable(this.ff, "mass_values",
				        "scan_index");
				// } catch (ResourceNotAvailableException e) {
				// log.error(e.getLocalizedMessage());
				// }
			}
			if (this.intensities == null) {
				// try {
				this.intensities = FragmentTools.getVariable(this.ff,
				        "intensity_values", "scan_index");
				// } catch (ResourceNotAvailableException e) {
				// log.error(e.getLocalizedMessage());
				// }
			}
			if (this.scan_acquisition_time == null) {
				// try {
				this.scan_acquisition_time = this.ff
				        .getChild("scan_acquisition_time");
				// } catch (ResourceNotAvailableException e) {
				// log.error(e.getLocalizedMessage());
				// }
			}
			EvalTools.notNull(this.masses, this);
			this.chrom = new Chromatogram1D(this,
			        this.masses.getIndexedArray(), this.intensities
			                .getIndexedArray(), this.scan_acquisition_time
			                .getArray(), null);
		}
		return this.chrom;
	}

	public IFileFragment getFileFragment() {
		return this.ff;
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IFileFragment#getID()
	 */
	public long getID() {
		return this.ff.getID();
	}

	public HashMap<String, String> getMetadata() {
		if (this.metaData == null) {
			this.metaData = new HashMap<String, String>();
		}
		return this.metaData;
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IFileFragment#getName()
	 */
	public String getName() {
		return this.ff.getName();
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IFileFragment#getParent()
	 */
	public IGroupFragment getParent() {
		return this.ff.getParent();
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IFileFragment#getSize()
	 */
	public int getSize() {
		return this.ff.getSize();
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IFileFragment#getSourceFiles()
	 */
	public Collection<IFileFragment> getSourceFiles() {
		return this.ff.getSourceFiles();
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IFragment#getStats()
	 */
	public StatsMap getStats() {
		return this.ff.getStats();
	}

	/**
	 * @param a
	 * @return
	 * @see cross.datastructures.fragments.IFragment#hasAttribute(ucar.nc2.Attribute)
	 */
	public boolean hasAttribute(final Attribute a) {
		return this.ff.hasAttribute(a);
	}

	/**
	 * @param name
	 * @return
	 * @see cross.datastructures.fragments.IFragment#hasAttribute(java.lang.String)
	 */
	public boolean hasAttribute(final String name) {
		return this.ff.hasAttribute(name);
	}

	/**
	 * @param vf
	 * @return
	 * @see cross.datastructures.fragments.IGroupFragment#hasChild(cross.datastructures.fragments.IVariableFragment)
	 */
	public boolean hasChild(final IVariableFragment vf) {
		return this.ff.hasChild(vf);
	}

	/**
	 * @param varname
	 * @return
	 * @see cross.datastructures.fragments.IGroupFragment#hasChild(java.lang.String)
	 */
	public boolean hasChild(final String varname) {
		return this.ff.hasChild(varname);
	}

	/**
	 * @param vf
	 * @return
	 * @see cross.datastructures.fragments.IFileFragment#hasChildren(cross.datastructures.fragments.IVariableFragment[])
	 */
	public boolean hasChildren(final IVariableFragment... vf) {
		return this.ff.hasChildren(vf);
	}

	/**
	 * @param s
	 * @return
	 * @see cross.datastructures.fragments.IFileFragment#hasChildren(java.lang.String[])
	 */
	public boolean hasChildren(final String... s) {
		return this.ff.hasChildren(s);
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IFileFragment#iterator()
	 */
	public Iterator<IVariableFragment> iterator() {
		return this.ff.iterator();
	}

	public void load() {

	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IGroupFragment#nextGID()
	 */
	public long nextGID() {
		return this.ff.nextGID();
	}

	/**
	 * @param variableFragment
	 * @see cross.datastructures.fragments.IFileFragment#removeChild(cross.datastructures.fragments.IVariableFragment)
	 */
	public void removeChild(final IVariableFragment variableFragment) {
		this.ff.removeChild(variableFragment);
	}

	/**
	 * @param ff
	 * @see cross.datastructures.fragments.IFileFragment#removeSourceFile(cross.datastructures.fragments.IFileFragment)
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
	 * @return
	 * @see cross.datastructures.fragments.IFileFragment#save()
	 */
	public boolean save() {
		return this.ff.save();
	}

	/**
	 * @param a
	 * @see cross.datastructures.fragments.IFragment#setAttributes(ucar.nc2.Attribute[])
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
	 * @see cross.datastructures.fragments.IFileFragment#setFile(java.lang.String)
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
	 * @see cross.datastructures.fragments.IFragment#setStats(cross.datastructures.StatsMap)
	 */
	public void setStats(final StatsMap stats1) {
		this.ff.setStats(stats1);
	}

	public void store() {
		this.ff.save();
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IFileFragment#toString()
	 */
	@Override
	public String toString() {
		return this.ff.toString();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException,
	        ClassNotFoundException {
		this.ff.readExternal(in);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		this.ff.writeExternal(out);
	}

}
