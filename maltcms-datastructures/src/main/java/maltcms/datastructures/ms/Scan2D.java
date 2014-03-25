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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.List;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;

/**
 * Specialization of Scan1D, adding second column times and indices, indicating
 * the modulation periods scans belong to.
 *
 * @author Nils Hoffmann
 *
 *
 */
public class Scan2D extends Scan1D implements IScan2D {

    /**
     *
     */
    private static final long serialVersionUID = -5785157824205744680L;
    private double fctime = -1, sctime = -1;
    private int fcind = -1, scind = -1;

    public Scan2D(Array masses1, Array intensities1, int scanNumber1,
            double scanAcquisitionTime1) {
        super(masses1, intensities1, scanNumber1, scanAcquisitionTime1);
    }

    public Scan2D(Array masses1, Array intensities1, int scanNumber1,
            double scanAcquisitionTime1, int idx1, int idx2, double rt1,
            double rt2) {
        this(masses1, intensities1, scanNumber1, scanAcquisitionTime1);
        this.fcind = idx1;
        this.scind = idx2;
        this.fctime = rt1;
        this.sctime = rt2;
    }

    public Scan2D(Array masses1, Array intensities1, int scanNumber1,
            double scanAcquisitionTime1, int idx1, int idx2, double rt1,
            double rt2, short msLevel) {
        super(masses1, intensities1, scanNumber1, scanAcquisitionTime1, msLevel);
        this.fcind = idx1;
        this.scind = idx2;
        this.fctime = rt1;
        this.sctime = rt2;
    }

    public Scan2D(Array masses1, Array intensities1, int scanNumber1,
            double scanAcquisitionTime1, int idx1, int idx2, double rt1,
            double rt2, short msLevel, final int precursorCharge, final double precursorMz, final double precursorIntensity) {
        super(masses1, intensities1, scanNumber1, scanAcquisitionTime1, msLevel, precursorCharge, precursorMz, precursorIntensity);
        this.fcind = idx1;
        this.scind = idx2;
        this.fctime = rt1;
        this.sctime = rt2;
    }

    public Scan2D(Array masses1, Array intensities1, int scanNumber1,
            double scanAcquisitionTime1, double rt1,
            double rt2, short msLevel, final int precursorCharge, final double precursorMz, final double precursorIntensity) {
        super(masses1, intensities1, scanNumber1, scanAcquisitionTime1, msLevel, precursorCharge, precursorMz, precursorIntensity);
        this.fctime = rt1;
        this.sctime = rt2;
    }

    @Override
    public int getSecondColumnScanIndex() {
        return this.scind;
    }

    @Override
    public double getSecondColumnScanAcquisitionTime() {
        return this.sctime;
    }

    @Override
    public void setSecondColumnScanIndex(final int a) {
        this.scind = a;
    }

    @Override
    public void setSecondColumnScanAcquisitionTime(final double sat) {
        this.sctime = sat;
    }

    @Override
    public int getFirstColumnScanIndex() {
        return this.fcind;
    }

    @Override
    public double getFirstColumnScanAcquisitionTime() {
        return this.fctime;
    }

    @Override
    public void setFirstColumnScanIndex(final int a) {
        this.fcind = a;
    }

    @Override
    public void setFirstColumnScanAcquisitionTime(final double sat) {
        this.fctime = sat;
    }

    @Override
    public Array getFeature(final String name) {
        switch (name) {
            case "first_column_scan_index": {
                ArrayInt.D0 a = new ArrayInt.D0();
                a.set(this.fcind);
                return a;
            }
            case "second_column_scan_index": {
                ArrayInt.D0 a = new ArrayInt.D0();
                a.set(this.scind);
                return a;
            }
            case "first_column_time":
            case "first_column_elution_time": {
                ArrayDouble.D0 a = new ArrayDouble.D0();
                a.set(this.fctime);
                return a;
            }
            case "second_column_time":
            case "second_column_elution_time": {
                ArrayDouble.D0 a = new ArrayDouble.D0();
                a.set(this.sctime);
                return a;
            }
            default:
                return super.getFeature(name);
        }
    }

    @Override
    public List<String> getFeatureNames() {
        return Arrays.asList(new String[]{"mass_values", "intensity_values",
            "scan_index", "scan_acquisition_time", "total_intensity",
            "first_column_scan_index", "second_column_scan_index",
            "first_column_elution_time", "second_column_elution_time",
            "ms_level", "precursor_charge", "precursor_mz",
            "precursor_intensity"});
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(fcind);
        out.writeInt(scind);
        out.writeDouble(fctime);
        out.writeDouble(sctime);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        fcind = in.readInt();
        scind = in.readInt();
        fctime = in.readDouble();
        sctime = in.readDouble();
    }

}
