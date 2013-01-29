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
package maltcms.commands.fragments.alignment.peakCliqueAlignment.peakFactory;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import lombok.Data;
import maltcms.datastructures.peak.Peak;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 *
 * @author Nils Hoffmann
 */
@Data
public class Peak1DFactory implements IPeakFactory {

    private String peakAreaVar = "peak_area";
    private String scanAcquisitionTimeVar = "scan_acquisition_time";

    @Override
    public IPeakFactoryImpl createInstance(IFileFragment sourceFile, boolean storeOnlyBestSimilarites, Tuple2D<Double, Double> minMaxMassRange, int size, double massBinResolution, boolean useSparseArrays, boolean savePeakSimilarities) {
        return new Peak1DFactoryImpl(sourceFile, storeOnlyBestSimilarites, minMaxMassRange, size, massBinResolution, useSparseArrays, savePeakSimilarities);
    }

    @Data
    private class Peak1DFactoryImpl implements IPeakFactoryImpl {

        private final IFileFragment sourceFile;
        private final boolean storeOnlyBestSimilarites;
        private final Tuple2D<Double, Double> minMaxMassRange;
        private final int size;
        private final double massBinResolution;
        private final boolean useSparseArrays;
        private final boolean savePeakSimilarities;
        private final Array peakAreaArray;
        private final Array satArray;

        public Peak1DFactoryImpl(IFileFragment sourceFile, boolean storeOnlyBestSimilarites, Tuple2D<Double, Double> minMaxMassRange, int size, double massBinResolution, boolean useSparseArrays, boolean savePeakSimilarities) {
            this.sourceFile = sourceFile;
            this.storeOnlyBestSimilarites = storeOnlyBestSimilarites;
            this.minMaxMassRange = minMaxMassRange;
            this.size = size;
            this.massBinResolution = massBinResolution;
            this.useSparseArrays = useSparseArrays;
            this.savePeakSimilarities = savePeakSimilarities;
            this.peakAreaArray = sourceFile.getChild(peakAreaVar).getArray();
            this.satArray = sourceFile.getChild(scanAcquisitionTimeVar).getArray();
        }

        @Override
        public Peak create(int peakIndex, int globalIndex) {
            Array a = new ArrayDouble.D1(1);
            a.setDouble(0,peakAreaArray.getDouble(globalIndex));
            Peak p = new Peak(globalIndex, a,
                    satArray.getDouble(globalIndex), sourceFile.getName(), savePeakSimilarities);
            return p;
        }
    }
}
