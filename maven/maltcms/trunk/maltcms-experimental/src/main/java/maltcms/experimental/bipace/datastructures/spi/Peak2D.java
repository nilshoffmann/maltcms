/*
 * $license$
 *
 * $Id$
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
        super(name, file, scanIndex, msIntensities, scan_acquisition_time);
    }
    
    public Peak2D(String name, IFileFragment file, int scanIndex,
            Array msIntensities, double scan_acquisition_time, double rt1, double rt2) {
        super(name, file, scanIndex, msIntensities, scan_acquisition_time);
        this.retentionTime1 = rt1;
        this.retentionTime2 = rt2;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Peak 2D at position ").append(getScanIndex()).
                append(" and rt: ").append(getScanAcquisitionTime()).
                append(" with rt1=").append(getRetentionTime1()).
                append(" and rt2=").append(getRetentionTime2()).
                append(" in file ").append(getAssociation().
                getName());
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