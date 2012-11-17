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

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.exception.ResourceNotAvailableException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
public class MockDatasource implements IDataSource {

    @Override
    public int canRead(IFileFragment ff) {
        return 1;
    }

    @Override
    public ArrayList<Array> readAll(IFileFragment f) throws IOException, ResourceNotAvailableException {
        return new ArrayList<Array>(0);
    }

    @Override
    public ArrayList<Array> readIndexed(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        throw new ResourceNotAvailableException("No such indexed variable: "+f.getName());
    }

    @Override
    public Array readSingle(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        throw new ResourceNotAvailableException("No such variable: "+f.getName());
    }

    @Override
    public ArrayList<IVariableFragment> readStructure(IFileFragment f) throws IOException {
        return new ArrayList<IVariableFragment>(0);
    }

    @Override
    public IVariableFragment readStructure(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        return f;
    }

    @Override
    public List<String> supportedFormats() {
        return Arrays.asList("nc", "nc.gz", "nc.z", "nc.zip", "nc.gzip", "nc.bz2", "cdf", "cdf.gz", "cdf.z", "cdf.zip", "cdf.gzip", "cdf.bz2");
    }

    @Override
    public boolean write(IFileFragment f) {
        return true;
    }

    @Override
    public void configure(Configuration cfg) {
        
    }

    @Override
    public void configurationChanged(ConfigurationEvent ce) {
        
    }
    
}
