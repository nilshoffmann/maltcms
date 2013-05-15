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
package cross.io;

import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.exception.ResourceNotAvailableException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.event.ConfigurationListener;
import ucar.ma2.Array;

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
 * @author Nils Hoffmann
 *
 */
public interface IDataSource extends IConfigurable, ConfigurationListener {

	public int canRead(IFileFragment ff);

    public ArrayList<Array> readAll(IFileFragment f) throws IOException,
            ResourceNotAvailableException;

    public ArrayList<Array> readIndexed(IVariableFragment f)
            throws IOException, ResourceNotAvailableException;

    public Array readSingle(IVariableFragment f) throws IOException,
            ResourceNotAvailableException;

    public ArrayList<IVariableFragment> readStructure(IFileFragment f)
            throws IOException;

    public IVariableFragment readStructure(IVariableFragment f)
            throws IOException, ResourceNotAvailableException;

    public List<String> supportedFormats();

    public boolean write(IFileFragment f);
}
