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
package maltcms.experimental.bipace.datastructures.spi;

import cross.datastructures.fragments.IFileFragment;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.PeakNG;
import ucar.ma2.Array;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
public class Peak2D extends PeakNG {

    @Getter
    @Setter
    private double retentionTime1 = Double.NaN;
    @Getter
    @Setter
    private double retentionTime2 = Double.NaN;

    public Peak2D(String name, IFileFragment file, int scanIndex,
            Array msIntensities, double scan_acquisition_time) {
        super(scanIndex, msIntensities, scan_acquisition_time, file.getName(), true);
        setName(name);
    }

    public Peak2D(String name, IFileFragment file, int scanIndex,
            Array msIntensities, double scan_acquisition_time, double rt1, double rt2) {
        super(scanIndex, msIntensities, scan_acquisition_time, file.getName(), true);
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
        if (name.equals("first_column_time")) {
            log.warn("This term has been deprecated, please use first_column_elution_time!");
        }
        if (name.equals("second_column_time")) {
            log.warn("This term has been deprecated, please use second_column_elution_time!");
        }
        if (name.equals("first_column_time") || name.equals("first_column_elution_time")) {
            return Array.factory(retentionTime1);
        } else if (name.equals("second_column_time") || name.equals("second_column_elution_time")) {
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
        return Arrays.asList("first_column_time","first_column_elution_time", "second_column_time","second_column_elution_time", "scan_acquisition_time", "scan_index",
                "binned_intensity_values");
    }
}
