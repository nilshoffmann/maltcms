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
package maltcms.datastructures.feature;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 * This feature vector retrieves mass and intensity values for this ms from
 * directly referenced (possibly cached) lists.
 *
 * @author Nils Hoffmann
 * 
 */
public class DefaultBinnedMSFeatureVector implements IFeatureVector {

    /**
     *
     */
    private static final long serialVersionUID = 4737942765869140810L;
    private final List<Array> binned_mass_values;
    private final List<Array> binned_intensity_values;
    private ArrayDouble.D0 sat = null;
    private ArrayDouble.D0 tic = null;
    private final int i;
    private final IFileFragment iff;
    private UUID uniqueId = UUID.randomUUID();

    /**
     * <p>Constructor for DefaultBinnedMSFeatureVector.</p>
     *
     * @param iff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param i a int.
     * @param t a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public DefaultBinnedMSFeatureVector(IFileFragment iff, int i,
            Tuple2D<List<Array>, List<Array>> t) {
        this.iff = iff;
        this.binned_mass_values = t.getFirst();
        this.binned_intensity_values = t.getSecond();
        // this.sat.set(MaltcmsTools.getScanAcquisitionTime(iff, i));
        // this.tic.set(MaltcmsTools.getTIC(iff, i));
        this.i = i;
    }

    /** {@inheritDoc} */
    @Override
    public Array getFeature(String string) {
        switch (string) {
            case "binned_mass_values":
                return this.binned_mass_values.get(i);
            case "binned_intensity_values":
                return this.binned_intensity_values.get(i);
            case "total_intensity":
                if (this.tic == null) {
                    this.tic = new ArrayDouble.D0();
                    this.tic.set(MaltcmsTools.getTIC(this.iff, this.i));
                }
                return this.tic;
            case "scan_acquisition_time":
                if (this.sat == null) {
                    this.sat = new ArrayDouble.D0();
                    this.sat.set(MaltcmsTools.getScanAcquisitionTime(this.iff,
                            this.i));
                }
                return this.sat;
        }
        throw new IllegalArgumentException("Unknown feature name: " + string);
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getFeatureNames() {
        return Arrays.asList("binned_mass_values", "binned_intensity_values",
                "total_intensity", "scan_acquisition_time");
    }

    /** {@inheritDoc} */
    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }
}
