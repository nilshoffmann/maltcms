/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.io.hdf5.mz5;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;

@Slf4j
@ServiceProvider(service = IDataSource.class)
public class MZ5DataSource implements IDataSource {

    @Override
    public int canRead(IFileFragment ff) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<Array> readAll(IFileFragment f) throws IOException, ResourceNotAvailableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<Array> readIndexed(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Array readSingle(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<IVariableFragment> readStructure(IFileFragment f) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IVariableFragment readStructure(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> supportedFormats() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean write(IFileFragment f) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void configure(Configuration cfg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void configurationChanged(ConfigurationEvent ce) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
//    private final Logger log = Logging.getLogger(this.getClass());
//    private final String[] fileEnding = new String[]{"mzml", "mzml.xml"};
//    @Configurable(name = "var.mass_values", value = "mass_values")
//    private String mass_values = "mass_values";
//    @Configurable(name = "var.intensity_values", value = "intensity_values")
//    private String intensity_values = "intensity_values";
//    @Configurable(name = "var.total_intensity", value = "total_intensity")
//    private String total_intensity = "total_intensity";
//    @Configurable(name = "var.scan_acquisition_time", value = "scan_acquisition_time")
//    private final String scan_acquisition_time = "scan_acquisition_time";
//    @Configurable(name = "var.scan_index", value = "scan_index")
//    private String scan_index = "scan_index";
//    @Configurable(name = "var.mass_range_min", value = "mass_range_min")
//    private String mass_range_min = "mass_range_min";
//    @Configurable(name = "var.mass_range_max", value = "mass_range_max")
//    private String mass_range_max = "mass_range_max";
//    private NetcdfDataSource ndf = null;
//    @Configurable(name = "var.source_files", value = "source_files")
//    private String source_files = "source_files";
//    private static WeakHashMap<IFileFragment, MzMLUnmarshaller> fileToIndex = new WeakHashMap<IFileFragment, MzMLUnmarshaller>();
}
