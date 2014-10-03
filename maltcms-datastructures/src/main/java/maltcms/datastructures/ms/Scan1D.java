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
package maltcms.datastructures.ms;

import cross.datastructures.cache.SerializableArray;
import cross.datastructures.tools.EvalTools;
import cross.exception.ResourceNotAvailableException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * Implementation of a 1-dimensional scan.
 *
 * @author Nils Hoffmann
 * 
 */
public class Scan1D implements IScan1D {

    /**
     *
     */
    private static final long serialVersionUID = 2937381605461829269L;
//    private ArrayInt.D0 scanNumber = new ArrayInt.D0();
    private int scanNumber = 0;
//    private ArrayDouble.D0 scanAcquisitionTime = new ArrayDouble.D0();
    private double scanAcquisitionTime = Double.NaN;
//    private ArrayDouble.D0 total_intensity = new ArrayDouble.D0();
    private double total_intensity = Double.NaN;
    private transient Array masses = null;
    private transient Array intensities = null;
    private UUID uniqueId = UUID.randomUUID();
    private short msLevel = 1;
    private double precursorCharge = Double.NaN;
    private double precursorMz = Double.NaN;
    private double precursorIntensity = Double.NaN;
//    private ArrayShort.D0 msLevel = new ArrayShort.D0();
//    private ArrayDouble.D0 precursorCharge = null;
//    private ArrayDouble.D0 precursorMz = null;
//    private ArrayDouble.D0 precursorIntensity = null;

    /**
     * <p>Constructor for Scan1D.</p>
     *
     * @since 1.3.2
     */
    public Scan1D() {
        
    }
    
    /**
     * <p>Constructor for Scan1D.</p>
     *
     * @param masses1 a {@link ucar.ma2.Array} object.
     * @param intensities1 a {@link ucar.ma2.Array} object.
     * @param scanNumber1 a int.
     * @param scanAcquisitionTime1 a double.
     */
    public Scan1D(final Array masses1, final Array intensities1,
            final int scanNumber1, final double scanAcquisitionTime1) {
        //enforce equal lengths for masses and intensities
        EvalTools.eqI(masses1.getShape()[0], intensities1.getShape()[0], Scan1D.class);
        this.masses = masses1;
        this.intensities = intensities1;
        EvalTools.geq(-1, scanNumber1, Scan1D.class);
        this.scanNumber = scanNumber1;
//        this.scanNumber.set(scanNumber1);
        //scan acquisition time can be negative sometimes, so no checking required
        this.scanAcquisitionTime = scanAcquisitionTime1;
//        this.scanAcquisitionTime.set(scanAcquisitionTime1);
        this.total_intensity = integrate(this.intensities);
//        this.total_intensity.set(integrate(this.intensities));
    }

    /**
     * <p>Constructor for Scan1D.</p>
     *
     * @param masses1 a {@link ucar.ma2.Array} object.
     * @param intensities1 a {@link ucar.ma2.Array} object.
     * @param scanNumber1 a int.
     * @param scanAcquisitionTime1 a double.
     * @param msLevel a short.
     */
    public Scan1D(final Array masses1, final Array intensities1,
            final int scanNumber1, final double scanAcquisitionTime1, final short msLevel) {
        this(masses1, intensities1, scanNumber1, scanAcquisitionTime1);
        EvalTools.geq(1, msLevel, Scan1D.class);
        this.msLevel = msLevel;
    }

    /**
     * <p>Constructor for Scan1D.</p>
     *
     * @param masses1 a {@link ucar.ma2.Array} object.
     * @param intensities1 a {@link ucar.ma2.Array} object.
     * @param scanNumber1 a int.
     * @param scanAcquisitionTime1 a double.
     * @param msLevel a short.
     * @param precursorCharge a int.
     * @param precursorMz a double.
     * @param precursorIntensity a double.
     * @since 1.3.2
     */
    public Scan1D(final Array masses1, final Array intensities1,
            final int scanNumber1, final double scanAcquisitionTime1, final short msLevel, final int precursorCharge, final double precursorMz, final double precursorIntensity) {
        this(masses1, intensities1, scanNumber1, scanAcquisitionTime1, msLevel);
        //ensure that precursor information is only set with msLevel>=2
        EvalTools.geq(2, msLevel, Scan1D.class);
        this.precursorCharge = precursorCharge;
        this.precursorMz = precursorMz;
        this.precursorIntensity = precursorIntensity;
    }

    /** {@inheritDoc} */
    @Override
    public Array getFeature(final String name) {
        switch (name) {
            case "mass_values":
                return this.masses;
            case "intensity_values":
                return this.intensities;
            case "scan_index":
                return Array.factory(new int[]{this.scanNumber});
            case "scan_acquisition_time":
                return Array.factory(new double[]{this.scanAcquisitionTime});
            case "total_intensity":
                return Array.factory(new double[]{this.total_intensity});
            case "ms_level":
                return Array.factory(new short[]{this.msLevel});
            case "precursor_charge":
                if (Double.isNaN(precursorCharge)) {
                    throw new ResourceNotAvailableException("precursor_charge not available!");
                }
                return Array.factory(new int[]{(int) this.precursorCharge});
            case "precursor_intensity":
                if (Double.isNaN(precursorIntensity)) {
                    throw new ResourceNotAvailableException("precursor_intensity not available!");
                }
                return Array.factory(new double[]{this.precursorIntensity});
            case "precursor_mz":
                if (Double.isNaN(precursorMz)) {
                    throw new ResourceNotAvailableException("precursor_mz not available!");
                }
                return Array.factory(new double[]{this.precursorMz});
        }
        throw new IllegalArgumentException("Feature name " + name + " unknown!");
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getFeatureNames() {
        return Arrays.asList(new String[]{"mass_values", "intensity_values",
            "scan_index", "scan_acquisition_time", "total_intensity", "ms_level", "precursor_charge", "precursor_mz", "precursor_intensity"});
    }

    /** {@inheritDoc} */
    @Override
    public Array getIntensities() {
        return this.intensities;
    }

    /** {@inheritDoc} */
    @Override
    public Array getMasses() {
        return this.masses;
    }

    /** {@inheritDoc} */
    @Override
    public double getScanAcquisitionTime() {
        return this.scanAcquisitionTime;
    }

    /** {@inheritDoc} */
    @Override
    public int getScanIndex() {
        return this.scanNumber;
    }

    /** {@inheritDoc} */
    @Override
    public double getTotalIntensity() {
        return this.total_intensity;
    }

    private double integrate(final Array intensities) {
        double d = 0;
        final IndexIterator ii = intensities.getIndexIterator();
        while (ii.hasNext()) {
            d += ii.getDoubleNext();
        }
        return d;
    }

    /** {@inheritDoc} */
    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(uniqueId);
        out.writeInt(scanNumber);
        out.writeDouble(scanAcquisitionTime);
        out.writeDouble(total_intensity);
        SerializableArray massesArray = new SerializableArray(masses);
        out.writeObject(massesArray);
//        massesArray.writeExternal(out);
        SerializableArray intensitiesArray = new SerializableArray(intensities);
        out.writeObject(intensitiesArray);
//        intensitiesArray.writeExternal(out);
        out.writeShort(msLevel);
        out.writeDouble(precursorCharge);
        out.writeDouble(precursorMz);
        out.writeDouble(precursorIntensity);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        uniqueId = (UUID) in.readObject();
        scanNumber = in.readInt();
        scanAcquisitionTime = in.readDouble();
        total_intensity = in.readDouble();
        SerializableArray massesArray = (SerializableArray) in.readObject();
        masses = (Array) massesArray.getArray();
        SerializableArray intensitiesArray = (SerializableArray) in.readObject();
        intensities = (Array) intensitiesArray.getArray();
        msLevel = in.readShort();
        precursorCharge = in.readDouble();
        precursorMz = in.readDouble();
        precursorIntensity = in.readDouble();
    }

    /** {@inheritDoc} */
    @Override
    public short getMsLevel() {
        return msLevel;
    }

    /** {@inheritDoc} */
    @Override
    public double getPrecursorCharge() {
        return precursorCharge;
    }

    /** {@inheritDoc} */
    @Override
    public double getPrecursorMz() {
        return precursorMz;
    }

    /** {@inheritDoc} */
    @Override
    public double getPrecursorIntensity() {
        return precursorIntensity;
    }
}
