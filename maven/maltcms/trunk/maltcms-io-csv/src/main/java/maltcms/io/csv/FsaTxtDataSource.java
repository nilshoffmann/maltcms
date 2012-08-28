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
package maltcms.io.csv;

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
import com.db4o.foundation.NotImplementedException;
import cross.Factory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;

import ucar.ma2.Array;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.tools.StringTools;
import java.io.FileInputStream;
import java.util.Vector;
import lombok.extern.slf4j.Slf4j;
import maltcms.io.andims.NetcdfDataSource;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;

/**
 * Implementation of {@link cross.io.IDataSource} for csv files containing
 * variables in columns and values in rows.
 *
 * The following is an example, following the naming scheme of ANDI-MS. The
 * columns will be mapped to variables scan_index, scan_acquisition_time,
 * total_intensity, mass_values and intensity_values. The last two are lists of
 * values with each value separated by a colon character :
 *
 * Not all variables need to be present in a csv file.
 *
 * scan_index scan_acquisition_time total_intensity mass_values intensity_values
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Slf4j
@ServiceProvider(service = IDataSource.class)
public class FsaTxtDataSource implements IDataSource {

    private final String[] fileEnding = new String[]{"fsa"};
    private List<String> scanDimensionVars = Collections.emptyList();
    private String scanDimensionName = "scan_number";
    private List<String> pointDimensionVars = Collections.emptyList();
    private String pointDimensionName = "point_number";
    private NetcdfDataSource ndf = null;
    private int dataFieldToRead = 3;

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

        Dimension d = null;

        if (dimensions.containsKey(dimname)) {
            log.debug("Dimension {} already known!", dimensions.get(dimname));
            d = dimensions.get(dimname);
        } else {

            d = nfw.addDimension(dimname, element.getLength(),
                    element.isShared(), element.isUnlimited(), element.
                    isVariableLength());
            dimensions.put(dimname, d);
        }
        return d;
    }

    @Override
    public int canRead(final IFileFragment ff) {
        final int dotindex = ff.getName().indexOf(".");
        if (dotindex == -1) {
            throw new RuntimeException("Could not determine File extension of "
                    + ff);
        }
        final String fileending = ff.getName().substring(dotindex + 1);
        // System.out.println("fileending: "+fileending);
        for (final String s : this.fileEnding) {
            if (s.equalsIgnoreCase(fileending)) {
                return 1;
            }
        }
        log.debug("no!");
        return 0;
    }

    @Override
    public void configurationChanged(final ConfigurationEvent arg0) {
    }

    @Override
    public void configure(final Configuration configuration) {
        this.scanDimensionVars = StringTools.toStringList(configuration.getList(this.
                getClass().getName() + ".scanDimensionVars"));
        Collections.sort(this.scanDimensionVars);
        this.scanDimensionName = configuration.getString(this.getClass().getName()
                + ".scanDimensionName", "scan_number");
        this.pointDimensionVars = StringTools.toStringList(configuration.getList(this.
                getClass().getName() + ".pointDimensionVars"));
        Collections.sort(this.pointDimensionVars);
        this.pointDimensionName = configuration.getString(this.getClass().
                getName()
                + ".pointDimensionName", "point_number");
        this.dataFieldToRead = configuration.getInt(
                this.getClass().getName() + ".dataFieldToRead", 3);
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
        throw new NotImplementedException();
    }

    @Override
    public Array readSingle(final IVariableFragment f) throws IOException,
            ResourceNotAvailableException, FileNotFoundException {
        log.debug("Reading single of {}, child of {}", f.toString(),
                f.getParent().toString());
        log.debug("{}", f.getParent().toString());
        if (f.getName().equals("total_intensity")) {
            Array data = getDataEntry(new File(f.getParent().getAbsolutePath()),
                    dataFieldToRead);
            f.setDimensions(new Dimension[]{new Dimension(scanDimensionName,
                        data.getShape()[0])});
            return data;
        } else if (f.getName().equals("scan_index")) {
            Array data = getDataEntry(new File(f.getParent().getAbsolutePath()),
                    dataFieldToRead);
            Array scanIndex = new ArrayInt.D1(data.getShape()[0]);
            for (int i = 0; i < data.getShape()[0]; i++) {
                scanIndex.setInt(i, i);
            }
            f.setDimensions(new Dimension[]{new Dimension(scanDimensionName,
                        scanIndex.getShape()[0])});
            return scanIndex;
        }
        throw new ResourceNotAvailableException("Could not retrieve array for fragment " + f.
                getName());
    }

    protected CSVReader read(CSVReader csvr, File inputFile) throws IOException {
        String fileExtension = StringTools.getFileExtension(inputFile.getName()).
                toLowerCase();
        csvr.setComment("#");
        csvr.setFirstLineHeaders(true);

        //tab separated
        if (fileExtension.equals("fsa")) {
            csvr.setFieldSeparator("\t");

        } else {
            throw new IOException(
                    "Unsupported file extension for CsvDataSource: " + fileExtension);
        }
        return csvr;
    }

    @Override
    public ArrayList<IVariableFragment> readStructure(final IFileFragment f)
            throws IOException, FileNotFoundException {
        log.debug("Reading structure of {}", f.toString());
        final ArrayList<IVariableFragment> al = new ArrayList<IVariableFragment>();
        for (IVariableFragment iv : f) {
            try {
                al.add(readStructure(iv));
            } catch (ResourceNotAvailableException rnae) {
                log.error("Resource not available: {}", rnae.getLocalizedMessage());
            }
        }
        return al;
    }

    @Override
    public IVariableFragment readStructure(final IVariableFragment f)
            throws IOException, FileNotFoundException,
            ResourceNotAvailableException {
        log.debug("Reading structure of {}", f.toString());
        Array a = readSingle(f);
//        f.setArray(a);
        return f;
    }

    @Override
    public List<String> supportedFormats() {
        return Arrays.asList(this.fileEnding);
    }

    protected Array getDataEntry(File f, int id) {
        try {
            CSVReader csvr = read(new CSVReader(), f);
            Tuple2D<Vector<Vector<String>>, Vector<String>> table = csvr.read(new FileInputStream(
                    f));
            for (Vector<String> row : table.getFirst()) {
                if (row.get(0).trim().toLowerCase().equals("data")) {
                    //is data row
                    if (Integer.parseInt(row.get(1)) == id) {
                        String rawData = row.get(5);
                        String[] splitData = rawData.trim().split(" ");
                        ArrayDouble.D1 arr = new ArrayDouble.D1(splitData.length);
                        for (int i = 0; i < splitData.length; i++) {
                            arr.setDouble(i, Double.parseDouble(splitData[i]));
                        }
                        return arr;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FsaTxtDataSource.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        throw new ResourceNotAvailableException("Could not retrieve data item " + id + " from file " + f.
                getAbsolutePath());
    }

    @Override
    public boolean write(final IFileFragment f) {
        EvalTools.notNull(this.ndf, this);
        // TODO Implement real write support
        this.log.info("Saving {} with CsvDataSource", f.getAbsolutePath());
        this.log.info("Changing output file from: {}", f.toString());
        final String source_file = f.getAbsolutePath();
        String filename = StringTools.removeFileExt(f.getAbsolutePath());
        filename += ".cdf";
        f.setFile(filename);
        f.addSourceFile(Factory.getInstance().getFileFragmentFactory().create(
                new File(source_file)));
        this.log.info("To: {}", filename);
        return Factory.getInstance().getDataSourceFactory().getDataSourceFor(f).
                write(f);
    }
}