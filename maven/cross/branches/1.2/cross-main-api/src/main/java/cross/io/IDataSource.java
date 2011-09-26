/*
 * 
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

	public ArrayList<Array> readAll(IFileFragment f) throws IOException,
	        ResourceNotAvailableException;

	public ArrayList<Array> readIndexed(IVariableFragment f)
	        throws IOException, ResourceNotAvailableException;// f

	public Array readSingle(IVariableFragment f) throws IOException,
	        ResourceNotAvailableException;

	public ArrayList<IVariableFragment> readStructure(IFileFragment f)
	        throws IOException;

	public IVariableFragment readStructure(IVariableFragment f)
	        throws IOException, ResourceNotAvailableException;

	public List<String> supportedFormats();

	public boolean write(IFileFragment f);
}
