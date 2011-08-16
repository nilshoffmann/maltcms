/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.datastructures.ms;

/**
 *
 * @author nilshoffmann
 */
public interface IScan2D extends IScan1D {

    double getFirstColumnScanAcquisitionTime();

    int getFirstColumnScanIndex();

    double getSecondColumnScanAcquisitionTime();

    int getSecondColumnScanIndex();

    void setFirstColumnScanAcquisitionTime(final double sat);

    void setFirstColumnScanIndex(final int a);

    void setSecondColumnScanAcquisitionTime(final double sat);

    void setSecondColumnScanIndex(final int a);
    
}
