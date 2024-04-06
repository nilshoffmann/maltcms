/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
package maltcms.io.csv.fsa;

import cross.Factory;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.NotImplementedException;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.tools.StringTools;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import maltcms.io.andims.NetcdfDataSource;
import maltcms.io.csv.CSVReader;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.openide.util.lookup.ServiceProvider;
import static org.slf4j.LoggerFactory.getLogger;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriter;

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
 * @author Nils Hoffmann
 * 
 */

@ServiceProvider(service = IDataSource.class)
public class FsaTxtDataSource implements IDataSource {
    
    private static final org.slf4j.Logger log = getLogger(FsaTxtDataSource.class);

    private final String[] fileEnding = new String[]{"fsa"};
    private List<String> scanDimensionVars = Collections.emptyList();
    private String scanDimensionName = "scan_number";
    private List<String> pointDimensionVars = Collections.emptyList();
    private String pointDimensionName = "point_number";
    private NetcdfDataSource ndf = null;
    private int dataFieldToRead = 3;

    private Dimension addDimension(final NetcdfFileWriter nfw,
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
            Group rootGroup = nfw.addGroup(null, "variables");
            d = nfw.addDimension(dimname, element.getLength(), element.isUnlimited(), element.
                    isVariableLength());
            dimensions.put(dimname, d);
        }
        return d;
    }

    /** {@inheritDoc} */
    @Override
    public int canRead(final IFileFragment ff) {
        final int dotindex = ff.getName().indexOf(".");
        if (dotindex == -1) {
            throw new RuntimeException("Could not determine File extension of "
                    + ff);
        }
        final String fileending = ff.getName().substring(dotindex + 1);
        // log.info("fileending: "+fileending);
        for (final String s : this.fileEnding) {
            if (s.equalsIgnoreCase(fileending)) {
                return 1;
            }
        }
        log.debug("no!");
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void configurationChanged(final ConfigurationEvent arg0) {
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public ArrayList<Array> readAll(final IFileFragment f) throws IOException,
            FileNotFoundException, ResourceNotAvailableException {
        final ArrayList<IVariableFragment> al = readStructure(f);
        final ArrayList<Array> ral = new ArrayList<>(al.size());
        for (final IVariableFragment vf : al) {
            final Array a = readSingle(vf);
            ral.add(a);
        }
        return ral;
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<Array> readIndexed(final IVariableFragment f)
            throws IOException, FileNotFoundException,
            ResourceNotAvailableException {
        throw new NotImplementedException();
    }

    /** {@inheritDoc} */
    @Override
    public Array readSingle(final IVariableFragment f) throws IOException,
            ResourceNotAvailableException, FileNotFoundException {
        log.debug("Reading single of {}, child of {}", f.toString(),
                f.getParent().toString());
        log.debug("{}", f.getParent().toString());
        switch (f.getName()) {
            case "total_intensity": {
                Array data = getDataEntry(f.getParent().getUri(),
                        dataFieldToRead);
                f.setDimensions(new Dimension[]{new Dimension(scanDimensionName,
                    data.getShape()[0])});
                return data;
            }
            case "scan_index": {
                Array data = getDataEntry(f.getParent().getUri(),
                        dataFieldToRead);
                Array scanIndex = new ArrayInt.D1(data.getShape()[0], false);
                for (int i = 0; i < data.getShape()[0]; i++) {
                    scanIndex.setInt(i, i);
                }
                f.setDimensions(new Dimension[]{new Dimension(scanDimensionName,
                    scanIndex.getShape()[0])});
                return scanIndex;
            }
        }
        throw new ResourceNotAvailableException("Could not retrieve array for fragment " + f.
                getName());
    }

    /**
     * <p>read.</p>
     *
     * @param csvr a {@link maltcms.io.csv.CSVReader} object.
     * @param inputFileName a {@link java.lang.String} object.
     * @return a {@link maltcms.io.csv.CSVReader} object.
     * @throws java.io.IOException if any.
     */
    protected CSVReader read(CSVReader csvr, String inputFileName) throws IOException {
        String fileExtension = StringTools.getFileExtension(inputFileName).
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

    /** {@inheritDoc} */
    @Override
    public ArrayList<IVariableFragment> readStructure(final IFileFragment f)
            throws IOException, FileNotFoundException {
        log.debug("Reading structure of {}", f.toString());
        final ArrayList<IVariableFragment> al = new ArrayList<>();
        for (IVariableFragment iv : f) {
            try {
                al.add(readStructure(iv));
            } catch (ResourceNotAvailableException rnae) {
                log.error("Resource not available: {}", rnae.getLocalizedMessage());
            }
        }
        return al;
    }

    /** {@inheritDoc} */
    @Override
    public IVariableFragment readStructure(final IVariableFragment f)
            throws IOException, FileNotFoundException,
            ResourceNotAvailableException {
        log.debug("Reading structure of {}", f.toString());
        Array a = readSingle(f);
//        f.setArray(a);
        return f;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> supportedFormats() {
        return Arrays.asList(this.fileEnding);
    }

    /**
     * <p>getDataEntry.</p>
     *
     * @param f a {@link java.net.URI} object.
     * @param id a int.
     * @return a {@link ucar.ma2.Array} object.
     */
    protected Array getDataEntry(URI f, int id) {
        try {
            CSVReader csvr = read(new CSVReader(), FileTools.getFilename(f));
            Tuple2D<Vector<Vector<String>>, Vector<String>> table = csvr.read(f.toURL().openStream());
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
                toString());
    }

    /** {@inheritDoc} */
    @Override
    public boolean write(final IFileFragment f) {
        EvalTools.notNull(this.ndf, this);
        // TODO Implement real write support
        this.log.info("Saving {} with CsvDataSource", f.getUri());
        this.log.info("Changing output file from: {}", f.toString());
        File file = new File(f.getUri());
        String filename = StringTools.removeFileExt(file.getAbsolutePath());
        filename += ".cdf";
        f.setFile(filename);
        f.addSourceFile(new FileFragment(f.getUri()));
        log.info("To: {}", filename);
        return Factory.getInstance().getDataSourceFactory().getDataSourceFor(f).write(f);
    }
}
