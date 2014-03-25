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
 * This feature vector keeps direct copies of mass and intensity values, as
 * opposed to DefaultBinnedMSFeatureVector, which requests them possibly from a
 * CachedList backed implementation.
 *
 * @author Nils Hoffmann
 */
public class FastBinnedMSFeatureVector implements IFeatureVector {

    /**
     *
     */
    private static final long serialVersionUID = 2026476569387259118L;
    private final Array binnedMasses;
    private final Array binnedIntens;
    private final ArrayDouble.D0 sat;
    private final ArrayDouble.D0 tic;
    private UUID uniqueId = UUID.randomUUID();

    public FastBinnedMSFeatureVector(IFileFragment iff, int i) {// ArrayDouble.D0
        // tic,
        // ArrayDouble.D0
        // scanAcquisition,
        // Array
        // binnedMasses,
        // Array
        // binnedIntens)
        // {
        Tuple2D<Array, Array> t = MaltcmsTools.getBinnedMS(iff, i);
        ArrayDouble.D0 sata = new ArrayDouble.D0();
        sata.set(MaltcmsTools.getScanAcquisitionTime(iff, i));
        ArrayDouble.D0 tica = new ArrayDouble.D0();
        tica.set(MaltcmsTools.getTIC(iff, i));
        this.tic = tica;
        this.sat = sata;
        this.binnedMasses = t.getFirst();
        this.binnedIntens = t.getSecond();
    }

    @Override
    public Array getFeature(String string) {
        switch (string) {
            case "binned_mass_values":
                return this.binnedMasses;
            case "binned_intensity_values":
                return this.binnedIntens;
            case "total_intensity":
                return this.tic;
            case "scan_acquisition_time":
                return this.sat;
        }
        throw new IllegalArgumentException("Unknown feature name: " + string);

    }

    @Override
    public List<String> getFeatureNames() {
        return Arrays.asList("binned_mass_values", "binned_intensity_values",
                "total_intensity", "scan_acquisition_time");
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

}
