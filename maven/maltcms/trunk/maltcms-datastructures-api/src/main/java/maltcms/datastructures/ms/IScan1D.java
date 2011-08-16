/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.datastructures.ms;

import ucar.ma2.Array;

/**
 *
 * @author nilshoffmann
 */
public interface IScan1D extends IScan {

    @Override
    Array getIntensities();

    @Override
    Array getMasses();

    @Override
    double getScanAcquisitionTime();

    @Override
    int getScanIndex();

    @Override
    double getTotalIntensity();
}
