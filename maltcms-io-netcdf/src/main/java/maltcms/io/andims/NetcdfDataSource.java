/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.io.andims;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableVariableFragment2;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.datastructures.tools.EvalTools;
import cross.tools.StringTools;
import java.io.File;
import java.net.URI;
import java.util.LinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Implementation of {@link cross.io.IDataSource} for Netcdf files, following
 * the ANDI-MS/AIA standard.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Slf4j
@ServiceProvider(service = IDataSource.class)
public class NetcdfDataSource implements IDataSource {

	private static int minCachedFiles = 5;
	private static int maxCachedFiles = 10;
	private static int secondsUntilCleanup = 60;
	private final String[] fileEnding = new String[]{"nc", "nc.gz", "nc.z", "nc.zip", "nc.gzip", "nc.bz2", "cdf", "cdf.gz", "cdf.z", "cdf.zip", "cdf.gzip", "cdf.bz2"};
	private boolean updateAttributes = false;
	private boolean useNetcdfFileCache = false;
	private boolean saveNCML;
	private List<String> scanDimensionVars = Collections.emptyList();
	private String scanDimensionName = "scan_number";
	private List<String> pointDimensionVars = Collections.emptyList();
	private String pointDimensionName = "point_number";

	private Dimension addDimension(final NetcdfFileWriteable nfw,
			final HashMap<String, Dimension> dimensions,
			final IVariableFragment vf, final Dimension element) {

		String dimname = element.getName();

		if (this.pointDimensionVars.contains(vf.getName())) {
			dimname = this.pointDimensionName;
			log.debug("Renaming dimension {} to {} for variable {}",
					new Object[]{element.getName(), dimname,
				vf.getName()});
		}

		if (this.scanDimensionVars.contains(vf.getName())) {
			dimname = this.scanDimensionName;
			log.debug("Renaming dimension {} to {} for variable {}",
					new Object[]{element.getName(), dimname,
				vf.getName()});
		}
		Dimension d = addDimension(dimensions, dimname, nfw, element);
		return d;
	}

	private Dimension addDimension(final HashMap<String, Dimension> dimensions, String dimname, final NetcdfFileWriteable nfw, final Dimension element) {
		Dimension d;
		if (dimensions.containsKey(dimname)) {
			log.debug("Dimension {} already known, updating!", dimensions.get(dimname));
			d = dimensions.get(dimname);
			if (d.getLength() != element.getLength()) {
				//TODO improve dimension mapping
				log.debug("Dimensions with identical name are not equal: {}!={}", d, element);
			}
			if (d.isShared() != element.isShared()) {
				if (d.isShared() && !element.isShared()) {
					//do nothing
				} else {
					d.setShared(element.isShared());
				}
			}
			if (d.isUnlimited() != element.isUnlimited()) {
				if (d.isUnlimited() && !element.isUnlimited()) {
					//do nothing
				} else {
					d.setUnlimited(element.isUnlimited());
				}
			}
			if (d.isVariableLength() != element.isVariableLength()) {
				if (d.isVariableLength() && !element.isVariableLength()) {
					//do nothing
				} else {
					d.setVariableLength(element.isVariableLength());
				}
			}
		} else {
			d = nfw.addDimension(dimname, element.getLength(), element.isShared(), element.isUnlimited(), element.isVariableLength());
			dimensions.put(dimname, d);
		}
		return d;
	}

	@Override
	public int canRead(final IFileFragment ff) {
		final int dotindex = ff.getName().lastIndexOf(".");
		if (dotindex == -1) {
			throw new RuntimeException("Could not determine File extension of "
					+ ff);
		}
		final String filename = ff.getName().toLowerCase();
		for (final String s : this.fileEnding) {
			if (filename.endsWith(s)) {
				return 1;
			}
		}
		log.debug("no!");
		return 0;
	}

	@Override
	public void configurationChanged(final ConfigurationEvent arg0) {
		// System.out.println("Configuration changed for property");
		// System.out.println(arg0.getPropertyName()+" =
		// "+arg0.getPropertyValue());
		// int type = arg0.getType();
		// String typeS = "With event type ";
		// switch(type) {
		// case AbstractConfiguration.EVENT_ADD_PROPERTY:
		// typeS+="Add Property";
		// break;
		// case AbstractConfiguration.EVENT_CLEAR_PROPERTY:
		// typeS+="Clear Property";
		// break;
		// case AbstractConfiguration.EVENT_READ_PROPERTY:
		// typeS+="Read Property";
		// break;
		// case AbstractConfiguration.EVENT_SET_PROPERTY:
		// typeS+="Set Property";
		// break;
		//			
		// }
		// System.out.println(typeS);
	}

	@Override
	public void configure(final Configuration configuration) {
		NetcdfDataSource.minCachedFiles = configuration.getInt(
				"ucar.nc2.NetcdfFileCache.minCachedFiles",
				NetcdfDataSource.minCachedFiles);
		NetcdfDataSource.maxCachedFiles = configuration.getInt(
				"ucar.nc2.NetcdfFileCache.maxCachedFiles",
				NetcdfDataSource.maxCachedFiles);
		NetcdfDataSource.secondsUntilCleanup = configuration.getInt(
				"ucar.nc2.NetcdfFileCache.secondsUntilCleanup",
				NetcdfDataSource.secondsUntilCleanup);
		this.updateAttributes = configuration.getBoolean(
				"cross.datastructures.fragments.Fragment.update.attributes",
				true);
		this.useNetcdfFileCache = configuration.getBoolean(
				"ucar.nc2.NetcdfFileCache.use", false);
		this.scanDimensionVars = StringTools.toStringList(configuration.getList(this.getClass().getName() + ".scanDimensionVars"));
		Collections.sort(this.scanDimensionVars);
		this.scanDimensionName = configuration.getString(this.getClass().getName()
				+ ".scanDimensionName", "scan_number");
		this.pointDimensionVars = StringTools.toStringList(configuration.getList(this.getClass().getName() + ".pointDimensionVars"));
		Collections.sort(this.pointDimensionVars);
		this.pointDimensionName = configuration.getString(this.getClass().getName()
				+ ".pointDimensionName", "point_number");
		this.saveNCML = configuration.getBoolean(
				"ucar.nc2.NetcdfFile.saveNCML", false);
		if(useNetcdfFileCache) {
			NetcdfDataset.initNetcdfFileCache(minCachedFiles, maxCachedFiles, secondsUntilCleanup);
			Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook()));
		}
	}
	
	private static class ShutdownHook implements Runnable {

		@Override
		public void run() {
			NetcdfDataset.shutdown();
		}
		
	}

	public IVariableFragment convert(final IFileFragment ff, final Variable v,
			final IVariableFragment ivf) {
		final DataType dt = v.getDataType();
		final List<Dimension> dimensions = v.getDimensions();
		final Dimension[] d = new Dimension[dimensions.size()];
		int i = 0;
		for (final Object o : dimensions) {
			if (o instanceof Dimension) {
				d[i++] = (Dimension) o;
			}
		}
		final String name = v.getName();
		final List<Range> ranges = v.getRanges();
		log.debug("Given Ranges for Variable v: {}", ranges);
		IVariableFragment vf = null;
		if (ivf == null) {
			if (ff.hasChild(name)) {
				vf = ff.getChild(name, true);
			} else {
				vf = new ImmutableVariableFragment2(ff, name);
			}
		} else {
			vf = ivf;
		}
		updateIVariableFragment(v, dt, d, ranges, vf);
		return vf;
	}

	protected NetcdfFile get(final IVariableFragment vf)
			throws ResourceNotAvailableException {

		try {
			final NetcdfFile nf = locateFile(vf);
			if (nf == null) {
				return null;
			}
			return nf;
		} catch (final IOException e) {
			// this is only a problem, when we want to write to a file, but then
			// we create it anyway
			log.debug(e.getLocalizedMessage());
		}
		return null;
	}

	/**
	 * Attributes are small metadata.
	 *
	 * @param ff
	 * @param nf
	 */
	protected void loadAttributes(final IFileFragment ff, final NetcdfFile nf) {
		final List<?> attrs = nf.getGlobalAttributes();
		if (!ff.getAttributes().isEmpty()) {
			log.debug(
					"IFileFragment {} already has Attributes, updating!", ff.getUri());
		}
		if (!attrs.isEmpty()) {
			log.debug("Loading Attributes for {}", ff.getUri());
		}
		for (final Object o : attrs) {
			if (ff.hasAttribute((Attribute) o)) {
				if (this.updateAttributes) {
					log.debug("Updating Attribute {}", ((Attribute) o).getName());
					ff.setAttributes((Attribute) o);
				} else {
					log.debug("Attribute {} already exists, ",
							((Attribute) o).getName());
				}
			} else {
				log.debug("Setting Attribute {}", ((Attribute) o).getName());
				ff.setAttributes((Attribute) o);
			}
		}
	}

	protected synchronized NetcdfFile locateFile(final IFileFragment ff) throws IOException {
		URI u = ff.getUri();
		String filepath = u.toString();
		log.info("Opening netcdf file {}", filepath);
		return NetcdfDataset.acquireFile(filepath, null);
//		return NetcdfFile.open(filepath);
	}

	protected NetcdfFile locateFile(final IVariableFragment f)
			throws IOException, ResourceNotAvailableException {
		NetcdfFile nf = null;
		log.debug("Trying to find {} in {}", f.getName(), f.getParent().getUri());
		try {// Prefer to find in parent of f, if it exists as a file
			nf = locateFile(f.getParent());
			log.debug("Searching for IVariableFragment {} in parent {}",
					f, f.getParent().getUri());
			if (nf.findVariable(f.getName()) != null) {
				log.debug("Found IVariableFragment {} in parent {}", f, f.getParent().getUri());
				return nf;
			} else {
				nf.close();
			}
		} catch (final IOException ioex) {
			if (nf != null) {
				try {
					nf.close();
				} catch (final IOException ioex2) {
					throw ioex2;
				}
			}
			throw ioex;
		}

		for (final IFileFragment ff : f.getParent().getSourceFiles()) {
			try {
				nf = locateFile(ff);
				log.debug(
						"Searching for IVariableFragment {} in source file {}",
						f, ff);
				if (nf.findVariable(f.getName()) != null) {
					log.debug(
							"Found IVariableFragment {} in source file {}", f,
							ff);
					return nf;
				} else {
					nf.close();
				}
			} catch (final IOException ioex2) {
				if (nf != null) {
					try {
						nf.close();
					} catch (final IOException ioex3) {
						throw ioex3;
					}
				}
				throw ioex2;
			}
		}
		throw new ResourceNotAvailableException("Could not find Variable "
				+ f.getName() + " in any associated file!");
	}

	@Override
	public ArrayList<Array> readAll(final IFileFragment f) throws IOException,
			FileNotFoundException, ResourceNotAvailableException {
		final ArrayList<IVariableFragment> al = readStructure(f);
		final ArrayList<Array> ral = new ArrayList<Array>(al.size());
		for (final IVariableFragment vf : al) {
			final Array a = readSingle(vf);
			ral.add(a);
		}
		return ral;
	}

	@Override
	public ArrayList<Array> readIndexed(final IVariableFragment f)
			throws IOException, FileNotFoundException,
			ResourceNotAvailableException {
		log.debug("Reading indexed of {}, child of {}", f.toString(), f.getParent().toString());
		final ArrayList<Array> al = readIndexed2(f);
		return al;
	}

	/**
	 * ReadIndexed: Read a number of (sparse) arrays written in
	 * row-compressed-storage (1D-array of values >0)
	 *
	 * Row-Compressed-Array: DATA Index array: INDEX with length k=|INDEX| =>
	 * Number of arrays
	 *
	 * NOTATION: DATA[FROM,TO,STRIDE], NOTE: TO is inclusive, not up to
	 *
	 * Require: Index containing offsets into DATA, such that INDEX[0]=DATA[0]
	 * and INDEX[i]=start index of array i with 0<=i<k
	 *
	 * Reading an arbitrary array i: IF i+1>=k INDEX[i+1] = |DATA|-1 ELSE a[i] =
	 * DATA[INDEX[i],INDEX[i+1]-1] ENDIF
	 *
	 *
	 * If we only want to read a subsection of DATA, say j scans starting from
	 * scan i, we use Require: NEW_INDEX with length j FOR a=0;a<j;a++ IF a+1 >=
	 * k SCAN[a] = DATA[INDEX[a],|DATA|-1] ELSE SCAN[a] =
	 * DATA[INDEX[a],INDEX[a+1]-1] ENDIF NEW_INDEX[a] = DATA[INDEX[a]]; ENDFOR
	 */
	public ArrayList<Array> readIndexed2(final IVariableFragment f)
			throws IOException, ResourceNotAvailableException,
			FileNotFoundException {
		log.debug("{}", f.getParent().toString());
		// get the associated Netcdf File
		final NetcdfFile nd = get(f);
		if (nd == null) {
			throw new FileNotFoundException("File "
					+ f.getParent().getUri()
					+ " appears not to be a valid cdf file!");
		}
		// Ensure there is an index
		EvalTools.notNull(f.getIndex(), this);// ,f.getIndex().getRange());
		final IVariableFragment index = f.getIndex();
		log.debug("Reading {} with index {}", f.getName(), index.getName());
		// This will be the range of Arrays in the returned ArrayList
		Range[] index_range = index.getRange();
		// Unset the range, so we can read in the full index_array at first
		index.setRange(null);

		// read in the full index_array
		log.debug("Reading index array {}", index);
		Array index_array = readSingle(index);
		switch (DataType.getType(index_array.getElementType())) {
			case LONG:
				log.warn("Index array contains long values, this is currently only supported up to Integer.MAX_VALUE");
				break;
			case INT:
				break;
		}
		// how many scans are stored in the original array/ how many array
		// pointers are contained in index_array?
		final int num_arrays = index_array.getShape()[0];
		// use the default values of index_range
		int index_start = 0;
		if ((index_range != null) && (index_range[0] != null)) {
			index_start = index_range[0].first();
		}
		int index_end = index_array.getShape()[0] - 1;
		if ((index_range != null) && (index_range[0] != null)) {
			index_end = index_range[0].last();
		}
		int index_stride = 1;
		if ((index_range != null) && (index_range[0] != null)) {
			index_stride = index_range[0].stride();
		}

		log.debug("index_start {}, index_end {}, index_stride {}",
				new Object[]{index_start, index_end, index_stride});

		// get information for length of compressed data
		final Variable data_var = nd.findVariable(f.getName());
		if (data_var == null) {
			//nd.close();
			throw new ResourceNotAvailableException("Could not read "
					+ f.getName());
		}
		// get the (first) dimension for that (we expect 1D arrays)
		EvalTools.eqI(1, data_var.getDimensions().size(), this);
		final Dimension data_dim = data_var.getDimension(0);
		f.setDimensions(new Dimension[]{new Dimension(data_dim.getName(),
			data_dim)});

		// absolute array start and end indices in data_array
		int data_start = index_array.getInt(index_start);
		int data_end = index_array.getInt(index_end);
		int data_stride = 1;

		// set stride, if exists
		if ((f.getRange() != null) && (f.getRange()[0] != null)) {
			data_stride = f.getRange()[0].stride();
		}

		log.debug("data_start {}, data_end {}, data_stride {}",
				new Object[]{data_start, data_end, data_stride});

		// Create a new index array, which is zero based
		if (index_range == null) {
			index_range = new Range[1];
		}
		if (index_range[0] == null) {
			try {
				index_range[0] = new Range(0, index_end - index_start,
						index_stride);
				log.debug("index_range[0] = {}", index_range[0]);
			} catch (final InvalidRangeException e) {
				log.error(e.getLocalizedMessage());
			}
		}

		int index_offset = 0;

		// create the ArrayList, which will hold the individual arrays
		final ArrayList<Array> al = new ArrayList<Array>(num_arrays);

		// Iterate over all arrays in range, starting at relative index 0
		// this can be translated to absolute in index_array via
		// index_start+i, where index_start is the positive offset into
		// index_array
		for (int i = 0; i < index_range[0].length(); i += index_stride) {
			// first element of array index_start+i
			data_start = index_array.getInt((index_start + i));
			// if we have reached the last scan start contained in index_array
			// use the length of the data array -1 as absolute end of last array
			if ((i + index_start + 1) == num_arrays) {
				data_end = data_dim.getLength() - 1;
			} else {
				data_end = index_array.getInt((index_start + i + 1)) - 1;
			}
			try {
				log.debug("Reading array {}, from {} to {}", new Object[]{
					i, data_start, data_end});
				// Read from data_start to data_end incl. with data_stride
				final Array ai = data_var.read(data_start + ":" + data_end
						+ ":" + data_stride);
				// Update new index to the changed value
				// new_index.set(i, index_offset);
				// Increase the next start offset by the length of array ai
				// log.debug("new_index[i] = {}, Index offset =
				// {}",i,index_offset);
				index_offset += (data_end - data_start);
				al.add(ai);
			} catch (final InvalidRangeException e) {
				log.error(e.getLocalizedMessage());
			}

		}
		EvalTools.notNull(al, this);
		index.setRange(index_range);
		nd.close();
		return al;
	}

	@Override
	public Array readSingle(final IVariableFragment f) throws IOException,
			ResourceNotAvailableException, FileNotFoundException {
		log.debug("Reading single of {}, child of {}", f.toString(), f.getParent().toString());
		log.debug("{}", f.getParent().toString());
		final NetcdfFile nd = get(f);
		if (nd == null) {
			throw new FileNotFoundException("Could not find physical file for "
					+ f.getParent().getUri());
		}
		EvalTools.notNull(nd, this);
		try {
			if (f.getIndex() != null) {

				log.debug("IVariableFragment "
						+ f.getName()
						+ " has IndexFragment "
						+ f.getIndex().getName()
						+ " set. Ignore if you didn't want to read this Variable indexed!");
			}
			final Variable v = nd.findVariable(f.getName());
			if (v == null) {
				throw new ResourceNotAvailableException("Could not read "
						+ f.getName());
			}

			f.setDataType(v.getDataType());
			// if this
			// fails, look at the commented code below!
			final List<Dimension> dims = v.getDimensions();
			log.debug("Dimensions for variable: {}:{}", f.getName(), dims);
			// final Iterator<Dimension> iter = dims.iterator();
			// final Dimension[] dimensions = new Dimension[dims.size()];
			// int cnt = 0;
			// while (iter.hasNext()) {
			// final Dimension dim = iter.next();
			// dimensions[cnt] = dim;
			// cnt++;
			// }
			f.setDimensions(dims.toArray(new Dimension[]{}));
			Array a = null;
			try {
				final Range[] r = f.getRange();
				if (r != null) {
					final List<Range> l = Arrays.asList(r);
					try {
						log.debug("Using specified range to read partially: {}", l);
						a = v.read(l);
						// if read with ranges is valid: keep ranges as before
						f.setRange(r);
					} catch (final InvalidRangeException e) {
						log.warn("Defined range list {} is invalid for variable {}, falling back to default range defined in file!", new Object[]{l, f.getName()});
						a = v.read();
						// replace ranges with valid ranges from file
						f.setRange(v.getRanges().toArray(new Range[]{}));
					}
				} else {
					log.debug("No range set, reading completely");
					a = v.read();
					List<Range> ranges = v.getRanges();
					log.debug("Ranges from file: {}", ranges);
					// update ranges to those from file
					f.setRange(ranges.toArray(new Range[ranges.size()]));
				}
				nd.close();
				if (a == null) {
					log.debug("a is null", a);
				}
				return a;
			} catch (final IOException e) {
				throw e;
			} finally {
				nd.close();
			}
		} finally {
			nd.close();
		}
	}

	/**
	 *
	 * @param f
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	@Override
	public ArrayList<IVariableFragment> readStructure(final IFileFragment f)
			throws IOException, FileNotFoundException {
		log.debug("Reading structure of {}", f.toString());
		final ArrayList<IVariableFragment> al = new ArrayList<IVariableFragment>();
		final NetcdfFile nd = locateFile(f);
		if (nd == null) {
			throw new FileNotFoundException("Could not find physical file for "
					+ f.getUri());
		}
		try {
			loadAttributes(f, nd);
			final List<?> l = nd.getVariables();
			for (final Object o : l) {
				final Variable v = (Variable) o;
				final IVariableFragment vf = convert(f, v, null);
				final List<?> att = v.getAttributes();
				for (final Object attr : att) {
					vf.setAttributes((ucar.nc2.Attribute) attr);
				}
				al.add(vf);
			}
			log.debug(f.toString());
		} finally {
			nd.close();
			return al;
		}
	}

	/**
	 *
	 * @param f
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ResourceNotAvailableException
	 */
	@Override
	public IVariableFragment readStructure(final IVariableFragment f)
			throws IOException, FileNotFoundException,
			ResourceNotAvailableException {
		log.debug("Reading structure of {}", f.toString());
		final NetcdfFile nd = locateFile(f);
		if (nd == null) {
			throw new FileNotFoundException("Could not find physical file for "
					+ f.getParent().getUri());
		}
		try {
			Variable v = nd.findVariable(f.getName());
			if (v == null) {
				throw new ResourceNotAvailableException("Could not read "
						+ f.getName());
			}
			final IVariableFragment vf = convert(f.getParent(), v, f);
			final List<?> att = v.getAttributes();
			for (final Object attr : att) {
				vf.setAttributes((ucar.nc2.Attribute) attr);
			}

			loadAttributes(f.getParent(), nd);
			log.debug(f.toString());
		} finally {
			nd.close();
			return f;
		}
	}

	/**
	 *
	 * @param parent
	 * @return
	 */
	protected NetcdfFileWriteable structureWrite(final IFileFragment parent) {
		NetcdfFileWriteable nfw = null;
		try {
			String filename = null;
			if (parent.getSize() == 0) {
				log.warn("IFileFragment {} has no children!", parent.getUri());
				return null;
			}
			if (parent.getUri().getScheme().equals("file")) {
				File file = new File(parent.getUri()).getAbsoluteFile();
				file.getParentFile().mkdirs();
				filename = file.getAbsolutePath();
			} else {
				filename = parent.getUri().toString();
			}
			log.debug("Trying to create NetcdfFileWritable {}", filename);
			nfw = NetcdfFileWriteable.createNew(
					filename, false);
			nfw.setLargeFile(true);

			final List<Attribute> globalAttrs = parent.getAttributes();
			for (final Attribute attr : globalAttrs) {
				log.debug("Adding attribute {} to file {}", attr, parent.getUri());
				nfw.addGlobalAttribute(attr);
			}

			final LinkedHashMap<String, Dimension> dimensions = new LinkedHashMap<String, Dimension>();
			for (Dimension dim : parent.getDimensions()) {
				if (dim.getName().startsWith("dimension")) {
					log.debug("Skipping default dimension {}", dim);
				} else {
					log.debug("Adding dimension: {}", dim);
					addDimension(dimensions, dim.getName(), nfw, dim);
				}
				//add all dimensions known to parent file fragment
//                unmappedDimensions.add(dim);
			}

			boolean skipVarForMissingData = false;
			for (final IVariableFragment vf : parent) {
				if (vf.getName().equals("scan_index")) {
					log.debug("{}", vf.getArray());
				}
				if (vf.getParent().getUri().equals(
						parent.getUri())) {
					// HANDLE DIMENSIONS, SHOULD BE DEFINED GLOBALLY
					Dimension[] dim = null;
					if (vf.getDimensions() == null) {
						if (!vf.hasArray()) {
							log.warn(
									"IVariableFragment {} has no array set, skipping!",
									vf);
							skipVarForMissingData = true;
						} else {
							log.warn(
									"IVariableFragment {} has no Dimension info, adding defaults!",
									vf.getName());
							if (vf.getIndex() != null) {
								dim = cross.datastructures.tools.ArrayTools.getDefaultDimensionsForIndexedArray(vf.getIndexedArray());
							} else {
								dim = cross.datastructures.tools.ArrayTools.getDefaultDimensions(vf.getArray());
							}
							final Dimension[] dimC = new Dimension[dim.length];
							int i = 0;
							for (final Dimension element : dim) {
								log.debug("Checking Dimension {}", element);
								dimC[i++] = addDimension(nfw, dimensions, vf,
										element);
							}
							dim = dimC;
						}
					} else {
						if (!vf.hasArray()) {
							log.warn(
									"IVariableFragment {} has no array set, skipping!",
									vf);
							skipVarForMissingData = true;
						} else {
							log.debug("Using Dimensions given by IVariableFragment!");

							dim = vf.getDimensions();
							final Dimension[] dimC = new Dimension[dim.length];
							int i = 0;
							int[] shape = null;
							if (vf.getIndex() != null) {
								shape = cross.datastructures.tools.ArrayTools.getShapeForIndexedArrays(vf.getIndexedArray());
							} else {
								shape = vf.getArray().getShape();
							}
							for (final Dimension element : dim) {
								log.debug("Checking Dimension {}", element);
								if (shape[i] != element.getLength()) {
									//throw new ConstraintViolationException("Dimension and array shape mismatch!: "+element+" vs "+shape[i]);
									log.warn("Correcting dimension {} to length {}!", element.getName(), shape[i]);
									element.setLength(shape[i]);
								}
								dimC[i] = addDimension(nfw, dimensions, vf,
										element);
								i++;
							}
							dim = dimC;
						}
					}
					log.debug("Defined dimensions: {}", Arrays.deepToString(dim));
					if (!skipVarForMissingData) {
						// HANDLE GROUPS
						final Group rootGroup = nfw.getRootGroup();
						// TODO Group handling loses data!!!
						// IGroupFragment parentGroup =
						// t.getFirst().getGroup().getParent();
						// IGroupFragment group = t.getFirst().getGroup();
						final Group variableGroup = rootGroup;
						// if(parentGroup==null) {
						// variableGroup = new
						// Group(nfw,rootGroup,group.getName());
						// nfw.addGroup(rootGroup, variableGroup);
						// } else {
						// Stack<IGroupFragment> s = new
						// Stack<IGroupFragment>();
						// IGroupFragment gf = group;
						// log.debug("Processing group {}",group.getName());
						// while(true) {
						// s.push(parentGroup);
						// log.debug("Current group {}",gf);
						// parentGroup = gf.getParent();
						// if(parentGroup==null) {
						// break;
						// }
						// log.debug("Parent group {}",parentGroup.getName());
						// gf = parentGroup;
						// }
						// while(!s.isEmpty()) {
						// gf = s.pop();
						// variableGroup = new
						// Group(nfw,rootGroup,gf.getName());
						// rootGroup = variableGroup;
						// }
						// }

						// CREATE VARIABLE
						final Variable v = new Variable(nfw, variableGroup,
								null, vf.getName());
						nfw.addVariable(variableGroup, v);

						// SET DIMENSIONS
						final List<Dimension> l = Arrays.asList(dim);
						v.setDimensions(l);
//                        vf.setDimensions(dim);

						// SET DATA TYPE
						DataType dt = null;
						if(vf.getDataType() == null) {
							if(vf.getIndex()!=null) {
								dt = DataType.getType(vf.getIndexedArray().get(0).getElementType());
							}else{
								dt = DataType.getType(vf.getArray().getElementType());
							}
						}else{
							dt = vf.getDataType();
						}
						v.setDataType(dt);

						// SET ATTRIBUTES
						final List<Attribute> attrs = vf.getAttributes();
						for (final Attribute att : attrs) {
							v.addAttribute(att);
						}
						log.debug("Adding variable {}", v.toString());
					}
					skipVarForMissingData = false;
				}
			}
			log.debug("Dimensions: {}", Arrays.deepToString(dimensions.values().toArray(new Dimension[]{})));

			nfw.create();
			return nfw;
		} catch (final IOException e) {
			log.error("Caught IOException while creating netcdf file: ",e);
			if (nfw != null) {
				try {
					nfw.close();
				} catch (IOException ex) {
					log.error("Caught IOException while closing file:",ex);
				}
			}
		}
		return null;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public List<String> supportedFormats() {
		return Arrays.asList(this.fileEnding);
	}

	/**
	 *
	 * @param al
	 * @return
	 */
	protected Dimension[] toDimensionArray(final ArrayList<Dimension> al) {
		Dimension[] dim = new Dimension[al.size()];
		dim = al.toArray(dim);
		return dim;
	}

	private void updateIVariableFragment(final Variable v, final DataType dt,
			final Dimension[] d, final List<Range> ranges,
			final IVariableFragment vf) {
		vf.setDimensions(d);
		vf.setDataType(dt);
		vf.setRange(ranges.toArray(new Range[ranges.size()]));
		final List<?> attrs = v.getAttributes();
		final ucar.nc2.Attribute[] attributes = new ucar.nc2.Attribute[attrs.size()];
		int cnt = 0;
		for (final Object o : attributes) {
			attributes[cnt++] = (ucar.nc2.Attribute) o;
		}
		vf.setAttributes(attributes);
	}

	/**
	 *
	 * @param f
	 * @return
	 */
	@Override
	public boolean write(final IFileFragment f) {
		log.debug("{}", f.toString());
		log.debug("Saving {} with NetcdfDataSource", f.getUri());
		EvalTools.notNull(f, this);
		final NetcdfFileWriteable nfw = structureWrite(f);
		EvalTools.notNull(nfw, this);
		try {
			int cnt = 0;
			String varname = "";
			for (final IVariableFragment vf : f) {
				if (vf.getParent().getUri().equals(f.getUri())) {
					try {
						varname = vf.getName();
						EvalTools.notNull(varname, this);
						if (vf.hasArray()) {
							final Variable v = nfw.findVariable(varname);
							if (v != null) {
								log.debug("Saving variable "
										+ v.getNameAndDimensions());
								if (vf.getIndex() != null) {
									int offset = 0;
									for (Array a : vf.getIndexedArray()) {
										nfw.write(varname, new int[]{offset}, a);
										offset += a.getShape()[0];
									}
								} else {
									nfw.write(varname, vf.getArray());
									nfw.flush();
								}
							}
						}
					} catch (final IOException e) {
						log.warn("IOException while writing variable '{}'", varname);
						log.debug("{}", e.getLocalizedMessage());
						throw new RuntimeException(e);
					} catch (final InvalidRangeException e) {
						log.warn("InvalidRangeException writing variable '{}' with dimensions '{}'", new Object[]{varname, Arrays.toString(vf.getDimensions())});
						log.debug("{}", e.getLocalizedMessage());
					}
					cnt++;
				}
			}
			nfw.finish();
			log.info("Wrote " + cnt + " records to " + nfw.getLocation());
			if (this.saveNCML) {
				final String ncmlFile = StringTools.removeFileExt(nfw.getLocation())
						+ ".ncml";
				try {
					nfw.writeNcML(new BufferedOutputStream(new FileOutputStream(
							ncmlFile)), null);
				} catch (final FileNotFoundException e1) {
					log.error(e1.getLocalizedMessage());
					log.error("{}", e1);
				} catch (final IOException e1) {
					log.error(e1.getLocalizedMessage());
					log.error("{}", e1);
				}
			}
			nfw.flush();
			nfw.close();
			return true;
		} catch (final IOException e) {
			log.error(e.getLocalizedMessage());
			log.error("{}", e);
			if (nfw != null) {
				try {
					nfw.close();
				} catch (final IOException ex) {
					log.error(ex.getLocalizedMessage());
					log.error("{}", ex);
					return false;
				}
			}
			return false;
		}
	}
}
