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
package maltcms.experimental.bipace.datastructures.spi;

import cross.datastructures.fragments.IFileFragment;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import maltcms.datastructures.peak.Peak;
import ucar.ma2.Array;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class Peak2D extends Peak {

    @Getter
    @Setter
    private double retentionTime1 = Double.NaN;
    @Getter
    @Setter
    private double retentionTime2 = Double.NaN;

    public Peak2D(String name, IFileFragment file, int scanIndex,
            Array msIntensities, double scan_acquisition_time) {
        super(scanIndex, msIntensities, scan_acquisition_time,file.getName(),true);
        setName(name);
    }
    
    public Peak2D(String name, IFileFragment file, int scanIndex,
            Array msIntensities, double scan_acquisition_time, double rt1, double rt2) {
        super(scanIndex, msIntensities, scan_acquisition_time,file.getName(),true);
        this.retentionTime1 = rt1;
        this.retentionTime2 = rt2;
        setName(name);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Peak 2D at position ").append(getScanIndex()).
                append(" and rt: ").append(getScanAcquisitionTime()).
                append(" with rt1=").append(getRetentionTime1()).
                append(" and rt2=").append(getRetentionTime2()).
                append(" in file ").append(getAssociation());
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
     */
    @Override
    public Array getFeature(String name) {
        if(name.equals("first_column_time")) {
            return Array.factory(retentionTime1);
        }else if(name.equals("second_column_time")) {
            return Array.factory(retentionTime2);
        }
        return super.getFeature(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IFeatureVector#getFeatureNames()
     */
    @Override
    public List<String> getFeatureNames() {
        return Arrays.asList("first_column_time","second_column_time","scan_acquisition_time", "scan_index",
                "binned_intensity_values");
    }
}
