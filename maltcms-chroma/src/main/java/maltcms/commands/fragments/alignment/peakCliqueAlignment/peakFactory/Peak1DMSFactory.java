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
package maltcms.commands.fragments.alignment.peakCliqueAlignment.peakFactory;

import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import java.util.List;
import lombok.Data;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.IBipacePeak;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.PeakNG;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.Sparse;

/**
 * <p>Peak1DMSFactory class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Data
@ServiceProvider(service=IPeakFactory.class)
public class Peak1DMSFactory implements IPeakFactory {

    private String massesVar = "mass_values";
    private String scanIndexVar = "scan_index";
    private String intensitiesVar = "intensity_values";
    private String binnedScanIndexVar = "binned_scan_index";
    private String binnedMassesVar = "binned_mass_values";
    private String binnedIntensitiesVar = "binned_intensity_values";
    private String scanAcquisitionTimeVar = "scan_acquisition_time";

    /** {@inheritDoc} */
    @Override
    public IPeakFactoryImpl createInstance(IFileFragment sourceFile, Tuple2D<Double, Double> minMaxMassRange, int size, double massBinResolution, boolean useSparseArrays, int associationId) {
        return new Peak1DMSFactoryImpl(sourceFile, minMaxMassRange, size, massBinResolution, useSparseArrays, associationId);
    }

    @Data
    private class Peak1DMSFactoryImpl implements IPeakFactoryImpl {

        private final IFileFragment sourceFile;
        private final Tuple2D<Double, Double> minMaxMassRange;
        private final int size;
        private final double massBinResolution;
        private final boolean useSparseArrays;
        private final List<Array> indexedMassValues;
        private final List<Array> indexedIntensityValues;
        private final Array satArray;
        private final int associationId;

        public Peak1DMSFactoryImpl(IFileFragment sourceFile, Tuple2D<Double, Double> minMaxMassRange, int size, double massBinResolution, boolean useSparseArrays, int associationId) {
            this.sourceFile = new FileFragment(sourceFile.getUri());
            this.minMaxMassRange = minMaxMassRange;
            this.size = size;
            this.massBinResolution = massBinResolution;
            this.useSparseArrays = useSparseArrays;
            if (useSparseArrays) {
                IVariableFragment scanIndex = sourceFile.getChild(scanIndexVar);
                IVariableFragment masses = sourceFile.getChild(massesVar);
                masses.setIndex(scanIndex);
                IVariableFragment intens = sourceFile.getChild(intensitiesVar);
                intens.setIndex(scanIndex);
                indexedMassValues = masses.getIndexedArray();
                indexedIntensityValues = intens.getIndexedArray();
            } else {
                IVariableFragment scanIndex = sourceFile.getChild(binnedScanIndexVar);
                IVariableFragment masses = sourceFile.getChild(binnedMassesVar);
                masses.setIndex(scanIndex);
                IVariableFragment intens = sourceFile.getChild(binnedIntensitiesVar);
                intens.setIndex(scanIndex);
                indexedMassValues = masses.getIndexedArray();
                indexedIntensityValues = intens.getIndexedArray();
            }
            satArray = sourceFile.getChild(scanAcquisitionTimeVar).getArray();
            this.associationId = associationId;
        }

        @Override
        public IBipacePeak create(int peakIndex, int scanIndex) {
            IBipacePeak p;
            if (useSparseArrays) {
                Array sparse = new Sparse(indexedMassValues.get(scanIndex), indexedIntensityValues.get(scanIndex),
                        (int) Math.floor(minMaxMassRange.getFirst()), (int) Math.ceil(minMaxMassRange.getSecond()),
                        size, massBinResolution);
                p = new PeakNG(scanIndex, sparse,
                        satArray.getDouble(scanIndex), sourceFile.getName(), associationId);
            } else {
                p = new PeakNG(scanIndex, indexedIntensityValues.get(scanIndex),
                        satArray.getDouble(scanIndex), sourceFile.getName(), associationId);
            }
            p.setPeakIndex(peakIndex);
            return p;
        }
    }
}
