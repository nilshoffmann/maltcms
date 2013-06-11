/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.IndexIterator;

/**
 * Implementation of a 1-dimensional scan.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
public class Scan1D implements IScan1D, Externalizable {

    /**
     *
     */
    private static final long serialVersionUID = 2937381605461829269L;
    private ArrayInt.D0 scanNumber = new ArrayInt.D0();
    private ArrayDouble.D0 scanAcquisitionTime = new ArrayDouble.D0();
    private ArrayDouble.D0 total_intensity = new ArrayDouble.D0();
    private Array masses = null;
    private Array intensities = null;
    private UUID uniqueId = UUID.randomUUID();

    public Scan1D(final Array masses1, final Array intensities1,
            final int scanNumber1, final double scanAcquisitionTime1) {
        this.masses = masses1;
        this.intensities = intensities1;
        this.scanNumber.set(scanNumber1);
        this.scanAcquisitionTime.set(scanAcquisitionTime1);
        this.total_intensity.set(integrate(this.intensities));
    }

    @Override
    public Array getFeature(final String name) {
        if (name.equals("mass_values")) {
            return this.masses;
        } else if (name.equals("intensity_values")) {
            return this.intensities;
        } else if (name.equals("scan_index")) {
            return this.scanNumber;
        } else if (name.equals("scan_acquisition_time")) {
            return this.scanAcquisitionTime;
        } else if (name.equals("total_intensity")) {
            return this.total_intensity;
        }
        throw new IllegalArgumentException("Feature name " + name + " unknown!");
    }

    @Override
    public List<String> getFeatureNames() {
        return Arrays.asList(new String[]{"mass_values", "intensity_values",
                    "scan_index", "scan_acquisition_time", "total_intensity"});
    }

    @Override
    public Array getIntensities() {
        return this.intensities;
    }

    @Override
    public Array getMasses() {
        return this.masses;
    }

    @Override
    public double getScanAcquisitionTime() {
        return this.scanAcquisitionTime.get();
    }

    @Override
    public int getScanIndex() {
        return this.scanNumber.get();
    }

    @Override
    public double getTotalIntensity() {
        return this.total_intensity.get();
    }

    private double integrate(final Array intensities) {
        double d = 0;
        final IndexIterator ii = this.intensities.getIndexIterator();
        while (ii.hasNext()) {
            d += ii.getDoubleNext();
        }
        return d;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(uniqueId);
		SerializableArray scanNumberArray = new SerializableArray(scanNumber);
		scanNumberArray.writeExternal(out);
		SerializableArray scanAcquisitionTimeArray = new SerializableArray(scanNumber);
		scanAcquisitionTimeArray.writeExternal(out);
		SerializableArray totalIntensityArray = new SerializableArray(scanNumber);
		totalIntensityArray.writeExternal(out);
		SerializableArray massesArray = new SerializableArray(masses);
		massesArray.writeExternal(out);
		SerializableArray intensitiesArray = new SerializableArray(intensities);
		intensitiesArray.writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		uniqueId = (UUID)in.readObject();
		SerializableArray scanNumberArray = (SerializableArray)in.readObject();
		scanNumber = (ArrayInt.D0)scanNumberArray.getArray();
		SerializableArray scanAcquisitionTimeArray = (SerializableArray)in.readObject();
		scanAcquisitionTime = (ArrayDouble.D0)scanAcquisitionTimeArray.getArray();
		SerializableArray totalIntensityArray = (SerializableArray)in.readObject();
		total_intensity = (ArrayDouble.D0)totalIntensityArray.getArray();
		SerializableArray massesArray = (SerializableArray)in.readObject();
		masses = (Array)massesArray.getArray();
		SerializableArray intensitiesArray = (SerializableArray)in.readObject();
		intensities = (Array)intensitiesArray.getArray();
	}
}
