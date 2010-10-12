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
 * $Id: FragmentXMLSerializer.java 116 2010-06-17 08:46:30Z nilshoffmann $
 */

package cross.io.xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import cross.Factory;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.exception.NotImplementedException;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.io.misc.Base64;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;

/**
 * Serializes a FileFragment and it's children structurally, with array data,
 * but array data is currently experimental.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class FragmentXMLSerializer implements IDataSource {

	private enum Mode {
		UNDEF, DOUBLE, FLOAT, LONG, INTEGER, BYTE, SHORT, BOOLEAN, OBJECT, STRING
	}

	private final Logger log = Logging.getLogger(this.getClass());;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.io.IDataSource#canRead(cross.datastructures.fragments.IFileFragment
	 * )
	 */
	@Override
	public int canRead(final IFileFragment ff) {
		if (ff.getAbsolutePath().endsWith("maltcms.xml")) {
			return 1;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.configuration.event.ConfigurationListener#
	 * configurationChanged
	 * (org.apache.commons.configuration.event.ConfigurationEvent)
	 */
	@Override
	public void configurationChanged(final ConfigurationEvent arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.io.IDataSource#configure(org.apache.commons.configuration.Configuration
	 * )
	 */
	@Override
	public void configure(final Configuration configuration) {
		// TODO Auto-generated method stub

	}

	public IFileFragment deserialize(final String location) throws IOException {
		final File f = new File(location);
		this.log.info("Deserializing {}", f.getAbsolutePath());
		final SAXBuilder parser = new SAXBuilder();
		IFileFragment ff = null;
		try {
			final Document doc = parser.build(new BufferedInputStream(
			        new FileInputStream(f)));
			this.log.debug("{}", doc.toString());
			final Element root = doc.getRootElement();
			ff = handleFile(root);

		} catch (final JDOMException e) {
			throw new IOException(e.fillInStackTrace());
		}
		return ff;
	}

	protected void handleAttributes(final IFragment f, final Element group) {
		final Element attributes = group.getChild("attributes");
		if (attributes != null) {
			int size = attributes.getChildren("attribute").size();
			try {
				size = attributes.getAttribute("size").getIntValue();
			} catch (final DataConversionException e) {
				this.log.error(e.getLocalizedMessage());
			}
			final ucar.nc2.Attribute[] attribA = new ucar.nc2.Attribute[size];
			int cnt = 0;
			final List<?> l = attributes.getChildren("attribute");
			for (final Object o : l) {
				final Element attribute = (Element) o;
				final String name = attribute.getAttributeValue("name");
				final String value = attribute.getAttributeValue("value");
				attribA[cnt++] = new ucar.nc2.Attribute(name, value);
			}
			f.setAttributes(attribA);
		}
	}

	/**
	 * @param name
	 * @param dims
	 * @param data
	 * @return
	 */
	private Array handleData(final String name, final Dimension[] dims,
	        final Element data) {
		EvalTools.notNull(dims, this);
		final String dec = new String(Base64
		        .decode(data.getText(), Base64.GZIP));
		final StreamTokenizer st = new StreamTokenizer(new StringReader(dec));
		final NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		final int[] shape = new int[dims.length];
		int d = 0;
		for (final Dimension dim : dims) {
			shape[d++] = dim.getLength();
		}
		Array a = null;
		IndexIterator idx = null;
		int tok = -1;
		// log.info("DataType of array: {}",
		// dt.getPrimitiveClassType().getName());
		Mode m = Mode.UNDEF;
		Object o = null;
		try {
			while ((tok = st.nextToken()) != StreamTokenizer.TT_EOL) {
				if (tok == StreamTokenizer.TT_WORD) {

					if (m == Mode.UNDEF) {
						try {
							o = nf.parse(st.sval);

							if (o instanceof Double) {
								m = Mode.DOUBLE;
								a = Array.factory(DataType.DOUBLE, shape);
							} else if (o instanceof Float) {
								m = Mode.FLOAT;
								a = Array.factory(DataType.FLOAT, shape);
							} else if (o instanceof Long) {
								m = Mode.LONG;
								a = Array.factory(DataType.LONG, shape);
							} else if (o instanceof Integer) {
								m = Mode.INTEGER;
								a = Array.factory(DataType.INT, shape);
							} else if (o instanceof Byte) {
								m = Mode.BYTE;
								a = Array.factory(DataType.BYTE, shape);
							} else if (o instanceof Short) {
								m = Mode.SHORT;
								a = Array.factory(DataType.SHORT, shape);
							}
						} catch (final ParseException pe) {
							if (st.sval.equalsIgnoreCase("true")
							        || st.sval.equalsIgnoreCase("false")) {
								m = Mode.BOOLEAN;
								a = Array.factory(DataType.BOOLEAN, shape);
							} else {
								m = Mode.STRING;
								a = Array.factory(DataType.STRING, shape);
							}
						}
					} else {
						if (idx == null) {
							idx = a.getIndexIterator();
						}
						switch (m) {
							case DOUBLE: {
								idx.setDoubleNext((Double) o);
								break;
							}
							case FLOAT: {
								idx.setFloatNext((Float) o);
								break;
							}
							case INTEGER: {
								idx.setIntNext((Integer) o);
								break;
							}
							case LONG: {
								idx.setLongNext((Long) o);
								break;
							}
							case BYTE: {
								idx.setByteNext((Byte) o);
								break;
							}
							case SHORT: {
								idx.setShortNext((Short) o);
								break;
							}
							case BOOLEAN: {
								idx.setBooleanNext(Boolean
								        .parseBoolean(st.sval));
								break;
							}
							case STRING: {
								idx.setObjectNext(st.sval);
								break;
							}
							case OBJECT: {
								throw new IllegalArgumentException(
								        "Could not handle type");
							}
						}
					}
				}
			}
		} catch (final IOException e) {
			this.log.warn("Could not read data for {}", name);
		}
		return a;
	}

	protected IFileFragment handleFile(final Element root) {
		final Element file = root.getChild("file");
		final Attribute filename1 = file.getAttribute("filename");
		final Attribute dirname = file.getAttribute("dirname");
		// Attribute resourceLocation = file.getAttribute("resourceLocation");
		this.log.debug("Associated file is {} {}", dirname.getValue(),
		        filename1.getValue());
		final File f = new File(dirname.getValue(), filename1.getValue());
		final IFileFragment ff1 = Factory.getInstance()
		        .getFileFragmentFactory().create(f);
		handleAttributes(ff1, file);
		final List<?> l = file.getChildren("namedGroup");
		final HashSet<String> idxVars = new HashSet<String>();
		// first pass: read all non indexed variables first
		for (final Object o : l) {
			final Element group = (Element) o;
			final String varname = group.getAttribute("name").getValue();
			final Element idxVar = group.getChild("indexVariable");
			if (idxVar != null) {
				idxVars.add(varname);
			} else {
				final IVariableFragment ngf = handleVariable(ff1, group);
				EvalTools.notNull(ngf, this);
			}
		}
		// second pass: initialize all indexed variables and set indices
		for (final Object o : l) {
			final Element group = (Element) o;
			final String idxVarName = group.getChild("indexVariable")
			        .getAttribute("name").getName();
			final IVariableFragment ngf = handleVariable(ff1, group);
			ngf.setIndex(ff1.getChild(idxVarName));
			EvalTools.notNull(ngf, this);
		}
		return ff1;
	}

	protected IVariableFragment handleVariable(final IFileFragment parent,
	        final Element var) {
		final String varname = var.getAttribute("name").getValue();
		if (parent.hasChild(varname)) {
			return parent.getChild(varname);
		}
		final IVariableFragment vf = parseVariable(parent, var);
		return vf;
	}

	protected IVariableFragment parseVariable(final IFileFragment parent,
	        final Element var) {
		final String name = var.getAttribute("name").getValue();
		final String dataType = var.getAttribute("dataType").getValue();
		DataType dt = DataType.getType(dataType);
		if (dt == null) {
			dt = DataType.DOUBLE;
		}

		final Dimension[] dims = parseVariableDimensions(var);

		final Element ranges = var.getChild("ranges");
		final List<?> l = ranges.getChildren("range");
		final Range[] rangeA = new Range[l.size()];
		int i = 0;
		for (final Object o : l) {
			final Element range = (Element) o;
			Range r = null;
			if (range != null) {
				final Attribute r_name = range.getAttribute("name");
				final Attribute r_first = range.getAttribute("first");
				final Attribute r_stride = range.getAttribute("stride");
				final Attribute r_last = range.getAttribute("last");
				try {
					r = new Range(r_name.getValue(), r_first.getIntValue(),
					        r_last.getIntValue(), r_stride.getIntValue());
				} catch (final DataConversionException e) {
					this.log.error(e.getLocalizedMessage());
				} catch (final InvalidRangeException e) {
					this.log.error(e.getLocalizedMessage());
				}
				// if (r_name != null) {
				// r.setName(r_name.getValue());
				// }

				rangeA[i++] = r;
			}
		}

		final IVariableFragment vf = new VariableFragment(parent, name);
		vf.setDimensions(dims);
		vf.setDataType(dt);
		vf.setRange(rangeA);
		handleAttributes(vf, var);
		final Element data = var.getChild("data");
		Array a = null;
		if (data != null) {
			a = handleData(name, dims, data);
			vf.setArray(a);
		}
		// VariableFragment vf =
		// FragmentTools.getVariable(parent,name,null,dims,dt,r);

		return vf;
	}

	/**
	 * @param var
	 * @return
	 */
	private Dimension[] parseVariableDimensions(final Element var) {
		final List<?> dimensions = var.getChildren("dimension");
		Dimension[] dims = null;
		if ((dimensions != null) && (dimensions.size() > 0)) {
			dims = new Dimension[dimensions.size()];
			int i = 0;
			for (final Object o : dimensions) {
				final Element dim = (Element) o;
				final Attribute d_length = dim.getAttribute("length");
				// Attribute d_id = dim.getAttribute("id");
				final Attribute d_name = dim.getAttribute("name");
				final Attribute d_shared = dim.getAttribute("shared");
				final Attribute d_unlimited = dim.getAttribute("unlimited");
				final Attribute d_variableLength = dim
				        .getAttribute("variableLength");
				try {
					dims[i++] = new Dimension(d_name.getValue(), d_length
					        .getIntValue(), d_shared.getBooleanValue(),
					        d_unlimited.getBooleanValue(), d_variableLength
					                .getBooleanValue());
				} catch (final DataConversionException e) {
					this.log.error(e.getLocalizedMessage());
				}
			}
		}
		return dims;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.io.IDataSource#readAll(cross.datastructures.fragments.IFileFragment
	 * )
	 */
	@Override
	public ArrayList<Array> readAll(final IFileFragment f) throws IOException,
	        ResourceNotAvailableException {
		final File file = FileTools.getFile(f);
		this.log.info("Deserializing {}", file.getAbsolutePath());
		final SAXBuilder parser = new SAXBuilder();
		try {
			final Document doc = parser.build(new BufferedInputStream(
			        new FileInputStream(file)));
			final Element root = doc.getRootElement();
			final List<?> l = root.getChildren("namedGroup");
			final ArrayList<Array> al = new ArrayList<Array>();
			for (final Object o : l) {
				final Element group = (Element) o;
				final String name = group.getAttribute("name").getValue();
				final Dimension[] d = parseVariableDimensions(group);
				al.add(handleData(name, d, group.getChild("data")));
			}
			return al;
		} catch (final JDOMException e) {
			this.log.error(e.getLocalizedMessage());
		}
		return new ArrayList<Array>(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.io.IDataSource#readIndexed(cross.datastructures.fragments.
	 * IVariableFragment)
	 */
	@Override
	public ArrayList<Array> readIndexed(final IVariableFragment f)
	        throws IOException, ResourceNotAvailableException {
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.io.IDataSource#readSingle(cross.datastructures.fragments.
	 * IVariableFragment)
	 */
	@Override
	public Array readSingle(final IVariableFragment f) throws IOException,
	        ResourceNotAvailableException {
		final File file = FileTools.getFile(f.getParent());
		this.log.info("Deserializing {}", file.getAbsolutePath());
		final SAXBuilder parser = new SAXBuilder();
		try {
			final Document doc = parser.build(new BufferedInputStream(
			        new FileInputStream(file)));
			final Element root = doc.getRootElement();
			final List<?> l = root.getChildren("namedGroup");
			for (final Object o : l) {
				final Element group = (Element) o;
				final String name = group.getAttribute("name").getValue();
				if (name.equals(f.getVarname())) {
					return handleData(name, f.getDimensions(), group
					        .getChild("data"));
				}
			}

		} catch (final JDOMException e) {
			this.log.error(e.getLocalizedMessage());
		}
		throw new ResourceNotAvailableException("Could not locate variable "
		        + f.getVarname() + " in file " + file.getAbsolutePath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.io.IDataSource#readStructure(cross.datastructures.fragments.
	 * IFileFragment)
	 */
	@Override
	public ArrayList<IVariableFragment> readStructure(final IFileFragment f)
	        throws IOException {
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.io.IDataSource#readStructure(cross.datastructures.fragments.
	 * IVariableFragment)
	 */
	@Override
	public IVariableFragment readStructure(final IVariableFragment f)
	        throws IOException, ResourceNotAvailableException {
		throw new NotImplementedException();
	}

	public String serialize(final IFileFragment iff) throws IOException {
		final String filename = iff.getAbsolutePath().substring(0,
		        iff.getAbsolutePath().lastIndexOf("."))
		        + ".maltcms.xml";
		final Element maltcms = new Element("maltcms");
		final Document doc = new Document(maltcms);
		iff.appendXML(maltcms);
		final XMLOutputter outp = new XMLOutputter();
		outp.output(doc, new BufferedOutputStream(new FileOutputStream(
		        new File(filename))));
		return filename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.io.IDataSource#supportedFormats()
	 */
	@Override
	public List<String> supportedFormats() {
		return Arrays.asList(new String[] { "maltcms.xml" });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.io.IDataSource#write(cross.datastructures.fragments.IFileFragment)
	 */
	@Override
	public boolean write(final IFileFragment f) {
		try {
			serialize(f);
			return true;
		} catch (final IOException ioex) {
			this.log.error("{}", ioex.getLocalizedMessage());
			return false;
		}
	}

	// protected IndexFragment handleIndex(FileFragment parent, VariableFragment
	// vf, Element indexVar) {
	// Attribute i_name = indexVar.getAttribute("name");
	// }

	// protected NamedGroupFragment handleGroup(FileFragment parent, Element
	// group) {
	// Attribute gID = group.getAttribute("groupID");
	// Attribute name = group.getAttribute("name");
	// try {
	// long gid = gID.getLongValue();
	// NamedGroupFragment ngf = new
	// NamedGroupFragment(ff,(name==null?""+gid:name.getValue()));
	// handleAttributes(ngf,group);
	// List vars = group.getChildren("variable");
	// for(Object var:vars) {
	// Element variable = (Element)var;
	// handleVariable(parent,ngf,group,variable);
	// }
	// return ngf;
	// } catch (DataConversionException e1) {
	// log.error(e1.getLocalizedMessage());
	// }
	// return null;
	// }

}
