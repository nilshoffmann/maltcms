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
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import java.util.List;
import lombok.Data;
import maltcms.datastructures.array.Sparse;
import maltcms.datastructures.peak.IPeak;
import maltcms.datastructures.peak.PeakNG;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 *
 * @author Nils Hoffmann
 */
public class Peak1DMSFactory implements IPeakFactory {

    private String massesVar = "mass_values";
    private String scanIndexVar = "scan_index";
    private String intensitiesVar = "intensity_values";
    private String binnedScanIndexVar = "binned_scan_index";
    private String binnedMassesVar = "binned_mass_values";
    private String binnedIntensitiesVar = "binned_intensity_values";
    private String scanAcquisitionTimeVar = "scan_acquisition_time";

    @Override
    public IPeakFactoryImpl createInstance(IFileFragment sourceFile, boolean storeOnlyBestSimilarites, Tuple2D<Double, Double> minMaxMassRange, int size, double massBinResolution, boolean useSparseArrays, boolean savePeakSimilarities) {
        return new Peak1DMSFactoryImpl(sourceFile, storeOnlyBestSimilarites, minMaxMassRange, size, massBinResolution, useSparseArrays, savePeakSimilarities);
    }

    @Data
    private class Peak1DMSFactoryImpl implements IPeakFactoryImpl {

        private final IFileFragment sourceFile;
        private final boolean storeOnlyBestSimilarites;
        private final Tuple2D<Double, Double> minMaxMassRange;
        private final int size;
        private final double massBinResolution;
        private final boolean useSparseArrays;
        private final boolean savePeakSimilarities;
        private final List<Array> indexedMassValues;
        private final List<Array> indexedIntensityValues;
        private final Array satArray;

        public Peak1DMSFactoryImpl(IFileFragment sourceFile, boolean storeOnlyBestSimilarites, Tuple2D<Double, Double> minMaxMassRange, int size, double massBinResolution, boolean useSparseArrays, boolean savePeakSimilarities) {
            this.sourceFile = sourceFile;
            this.storeOnlyBestSimilarites = storeOnlyBestSimilarites;
            this.minMaxMassRange = minMaxMassRange;
            this.size = size;
            this.massBinResolution = massBinResolution;
            this.useSparseArrays = useSparseArrays;
            this.savePeakSimilarities = savePeakSimilarities;
            if(useSparseArrays) {
                IVariableFragment scanIndex = sourceFile.getChild(scanIndexVar);
                IVariableFragment masses = sourceFile.getChild(massesVar);
                masses.setIndex(scanIndex);
                IVariableFragment intens = sourceFile.getChild(intensitiesVar);
                intens.setIndex(scanIndex);
                indexedMassValues = masses.getIndexedArray();
                indexedIntensityValues = intens.getIndexedArray();
            }else{
                IVariableFragment scanIndex = sourceFile.getChild(binnedScanIndexVar);
                IVariableFragment masses = sourceFile.getChild(binnedMassesVar);
                masses.setIndex(scanIndex);
                IVariableFragment intens = sourceFile.getChild(binnedIntensitiesVar);
                intens.setIndex(scanIndex);
                indexedMassValues = masses.getIndexedArray();
                indexedIntensityValues = intens.getIndexedArray();
            }
            satArray = sourceFile.getChild(scanAcquisitionTimeVar).getArray();
        }

        @Override
        public IPeak create(int peakIndex, int scanIndex) {
            IPeak p;
            if (useSparseArrays) {
                ArrayDouble.D1 sparse = new Sparse(indexedMassValues.get(scanIndex), indexedIntensityValues.get(scanIndex),
                        (int) Math.floor(minMaxMassRange.getFirst()), (int) Math.ceil(minMaxMassRange.getSecond()),
                        size, massBinResolution);
                p = new PeakNG(scanIndex, sparse,
                        satArray.getDouble(scanIndex), sourceFile.getName(), storeOnlyBestSimilarites);
            } else {
                p = new PeakNG(scanIndex, indexedIntensityValues.get(scanIndex),
                        satArray.getDouble(scanIndex), sourceFile.getName(), storeOnlyBestSimilarites);
            }
            p.setPeakIndex(peakIndex);
            return p;
        }
    }
}
