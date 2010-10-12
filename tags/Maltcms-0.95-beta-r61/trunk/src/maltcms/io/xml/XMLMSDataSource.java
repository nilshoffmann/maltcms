/**
 * 
 */
package maltcms.io.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;

import ucar.ma2.Array;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public class XMLMSDataSource implements IDataSource {

	/* (non-Javadoc)
	 * @see cross.io.IDataSource#canRead(cross.datastructures.fragments.IFileFragment)
	 */
	@Override
	public int canRead(IFileFragment ff) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see cross.io.IDataSource#configure(org.apache.commons.configuration.Configuration)
	 */
	@Override
	public void configure(Configuration configuration) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see cross.io.IDataSource#readAll(cross.datastructures.fragments.IFileFragment)
	 */
	@Override
	public ArrayList<Array> readAll(IFileFragment f) throws IOException,
	        ResourceNotAvailableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see cross.io.IDataSource#readIndexed(cross.datastructures.fragments.IVariableFragment)
	 */
	@Override
	public ArrayList<Array> readIndexed(IVariableFragment f)
	        throws IOException, ResourceNotAvailableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see cross.io.IDataSource#readSingle(cross.datastructures.fragments.IVariableFragment)
	 */
	@Override
	public Array readSingle(IVariableFragment f) throws IOException,
	        ResourceNotAvailableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see cross.io.IDataSource#readStructure(cross.datastructures.fragments.IFileFragment)
	 */
	@Override
	public ArrayList<IVariableFragment> readStructure(IFileFragment f)
	        throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see cross.io.IDataSource#readStructure(cross.datastructures.fragments.IVariableFragment)
	 */
	@Override
	public IVariableFragment readStructure(IVariableFragment f)
	        throws IOException, ResourceNotAvailableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see cross.io.IDataSource#supportedFormats()
	 */
	@Override
	public List<String> supportedFormats() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see cross.io.IDataSource#write(cross.datastructures.fragments.IFileFragment)
	 */
	@Override
	public boolean write(IFileFragment f) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.configuration.event.ConfigurationListener#configurationChanged(org.apache.commons.configuration.event.ConfigurationEvent)
	 */
	@Override
	public void configurationChanged(ConfigurationEvent arg0) {
		// TODO Auto-generated method stub

	}

}
