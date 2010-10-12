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

package cross.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationListener;

import ucar.ma2.Array;
import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.exception.ResourceNotAvailableException;

/**
 * Interface describing access to a IDataSource, which could be either a file on
 * a local system, a network resource or a database connection.
 * 
 * Implementing classes are required to allow reading of the structure of a data
 * source, represented as IVariableFragments, describing the dimensions, data
 * type, name and other attributes of each variable. This allows for decoupling
 * of reading the actual data and/or only reading the associated information.
 * Use a IVariableFragment to retrieve the associated data, or use the
 * IFileFragment, describing the source, to retrieve all associated data at once
 * via {@link cross.tools.FragmentTools}.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IDataSource extends IConfigurable, ConfigurationListener {

	public int canRead(IFileFragment ff);

	public void configure(Configuration configuration);

	public ArrayList<Array> readAll(IFileFragment f) throws IOException,
	        ResourceNotAvailableException;

	public ArrayList<Array> readIndexed(IVariableFragment f)
	        throws IOException, ResourceNotAvailableException;// f

	public Array readSingle(IVariableFragment f) throws IOException,
	        ResourceNotAvailableException;

	// needs
	// to
	// have
	// an
	// index
	// defined
	// public boolean structureWrite(FileFragment f,
	// ArrayList<Tuple2D<VariableFragment,Array>> a);

	// public ArrayList<VariableFragment> structureReadAll(FileFragment f);
	public ArrayList<IVariableFragment> readStructure(IFileFragment f)
	        throws IOException;

	public IVariableFragment readStructure(IVariableFragment f)
	        throws IOException, ResourceNotAvailableException;

	public List<String> supportedFormats();

	public boolean write(IFileFragment f);
}
