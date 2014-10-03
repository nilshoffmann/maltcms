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
import cross.exception.ResourceNotAvailableException;
import java.awt.Point;
import java.util.List;
import lombok.Data;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.IBipacePeak;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.Peak2D;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.tools.ArrayTools;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.Sparse;

/**
 * <p>Peak2DMSFactory class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Data
@ServiceProvider(service = IPeakFactory.class)
public class Peak2DMSFactory implements IPeakFactory {

    private String massesVar = "mass_values";
    private String scanIndexVar = "scan_index";
    private String intensitiesVar = "intensity_values";
    private String binnedScanIndexVar = "binned_scan_index";
    private String binnedMassesVar = "binned_mass_values";
    private String binnedIntensitiesVar = "binned_intensity_values";
    private String firstColumnElutionTimeVar = "first_column_elution_time";
    private String secondColumnElutionTimeVar = "second_column_elution_time";
    private String scanAcquisitionTimeVar = "scan_acquisition_time";

    /** {@inheritDoc} */
    @Override
    public IPeakFactoryImpl createInstance(IFileFragment sourceFile, Tuple2D<Double, Double> minMaxMassRange, int size, double massBinResolution, boolean useSparseArrays, int associationId) {
        return new Peak2DMSFactoryImpl(sourceFile, minMaxMassRange, size, massBinResolution, useSparseArrays, associationId);
    }

    @Data
    private class Peak2DMSFactoryImpl implements IPeakFactoryImpl {

        private final IFileFragment sourceFile;
        private MsProvider provider;

        public Peak2DMSFactoryImpl(IFileFragment sourceFile, Tuple2D<Double, Double> minMaxMassRange, int size, double massBinResolution, boolean useSparseArrays, int associationId) {
            this.sourceFile = new FileFragment(sourceFile.getUri());
            ScanLineCacheFactory.setMinMass(minMaxMassRange.getFirst());
            ScanLineCacheFactory.setMaxMass(minMaxMassRange.getSecond());
            try {
                IVariableFragment modulationTime = sourceFile.getChild("modulation_time");
                if (useSparseArrays) {
                    provider = new ScanLineSparseProvider(this.sourceFile, associationId, massBinResolution, size, minMaxMassRange);
                } else {
                    provider = new ScanLineDenseProvider(this.sourceFile, associationId, massBinResolution, size, minMaxMassRange);
                }
            } catch (ResourceNotAvailableException rnae) {
                if (useSparseArrays) {
                    provider = new DefaultSparseProvider(this.sourceFile, associationId, massBinResolution, size, minMaxMassRange);
                } else {
                    provider = new DefaultDenseProvider(this.sourceFile, associationId, massBinResolution, size, minMaxMassRange);
                }
            }
        }

        @Override
        public IBipacePeak create(int peakIndex, int scanIndex) {
            Peak2D p = provider.provide(scanIndex);
            p.setPeakIndex(peakIndex);
            return p;
        }
    }

    private abstract class MsProvider {

        final IFileFragment sourceFile;
        final int associationId;
        final double massBinResolution;
        final int size;
        final Tuple2D<Double, Double> minMaxMassRange;

        MsProvider(IFileFragment sourceFile, int associationId, double massBinResolution, int size, Tuple2D<Double, Double> minMaxMassRange) {
            this.sourceFile = sourceFile;
            this.associationId = associationId;
            this.massBinResolution = massBinResolution;
            this.size = size;
            this.minMaxMassRange = minMaxMassRange;
        }

        abstract Peak2D provide(int scanIndex);
    }

    private class DefaultSparseProvider extends MsProvider {

        final List<Array> indexedMassValues;
        final List<Array> indexedIntensityValues;
        final Array satArray;
        final Array fctArray;
        final Array sctArray;

        DefaultSparseProvider(IFileFragment sourceFile, int associationId, double massBinResolution, int size, Tuple2D<Double, Double> minMaxMassRange) {
            super(sourceFile, associationId, massBinResolution, size, minMaxMassRange);
            IVariableFragment scanIndex = sourceFile.getChild(scanIndexVar);
            IVariableFragment masses = sourceFile.getChild(massesVar);
            masses.setIndex(scanIndex);
            IVariableFragment intens = sourceFile.getChild(intensitiesVar);
            intens.setIndex(scanIndex);
            indexedMassValues = masses.getIndexedArray();
            indexedIntensityValues = intens.getIndexedArray();
            satArray = sourceFile.getChild(scanAcquisitionTimeVar).getArray();
            fctArray = sourceFile.getChild(firstColumnElutionTimeVar).getArray();
            sctArray = sourceFile.getChild(secondColumnElutionTimeVar).getArray();
        }

        @Override
        Peak2D provide(int scanIndex) {
            Array sparse = new Sparse(indexedMassValues.get(scanIndex), indexedIntensityValues.get(scanIndex),
                    (int) Math.floor(minMaxMassRange.getFirst()), (int) Math.ceil(minMaxMassRange.getSecond()),
                    size, massBinResolution);
            Peak2D p = new Peak2D(scanIndex, sparse,
                    satArray.getDouble(scanIndex), sourceFile.getName(), associationId);
            p.setFirstColumnElutionTime(fctArray.getFloat(scanIndex));
            p.setSecondColumnElutionTime(sctArray.getFloat(scanIndex));
            return p;
        }
    }

    private class ScanLineSparseProvider extends MsProvider {

        final IScanLine scanLineCache;
        final Array satArray;
        final Array fctArray;
        final Array sctArray;

        ScanLineSparseProvider(IFileFragment sourceFile, int associationId, double massBinResolution, int size, Tuple2D<Double, Double> minMaxMassRange) {
            super(sourceFile, associationId, massBinResolution, size, minMaxMassRange);
            scanLineCache = ScanLineCacheFactory.getSparseScanLineCache(sourceFile);
            satArray = sourceFile.getChild(scanAcquisitionTimeVar).getArray();
            fctArray = sourceFile.getChild(firstColumnElutionTimeVar).getArray();
            sctArray = sourceFile.getChild(secondColumnElutionTimeVar).getArray();
        }

        @Override
        Peak2D provide(int scanIndex) {
            Point pt = scanLineCache.mapIndex(scanIndex);
            Tuple2D<Array, Array> t = scanLineCache.getSparseMassSpectrum(pt);
            Array sparse = new Sparse(t.getFirst(), t.getSecond(),
                    (int) Math.floor(minMaxMassRange.getFirst()), (int) Math.ceil(minMaxMassRange.getSecond()),
                    size, massBinResolution);
            Peak2D p = new Peak2D(scanIndex, sparse,
                    satArray.getDouble(scanIndex), sourceFile.getName(), associationId);
            p.setFirstColumnElutionTime(fctArray.getFloat(scanIndex));
            p.setSecondColumnElutionTime(sctArray.getFloat(scanIndex));
            return p;
        }
    }

    private class DefaultDenseProvider extends MsProvider {

        final List<Array> indexedMassValues;
        final List<Array> indexedIntensityValues;
        final Array satArray;
        final Array fctArray;
        final Array sctArray;

        DefaultDenseProvider(IFileFragment sourceFile, int associationId, double massBinResolution, int size, Tuple2D<Double, Double> minMaxMassRange) {
            super(sourceFile, associationId, massBinResolution, size, minMaxMassRange);
            IVariableFragment scanIndex = sourceFile.getChild(binnedScanIndexVar);
            IVariableFragment masses = sourceFile.getChild(binnedMassesVar);
            masses.setIndex(scanIndex);
            IVariableFragment intens = sourceFile.getChild(binnedIntensitiesVar);
            intens.setIndex(scanIndex);
            indexedMassValues = masses.getIndexedArray();
            indexedIntensityValues = intens.getIndexedArray();
            satArray = sourceFile.getChild(scanAcquisitionTimeVar).getArray();
            fctArray = sourceFile.getChild(firstColumnElutionTimeVar).getArray();
            sctArray = sourceFile.getChild(secondColumnElutionTimeVar).getArray();
        }

        @Override
        Peak2D provide(int scanIndex) {
            Peak2D p = new Peak2D(scanIndex, indexedIntensityValues.get(scanIndex),
                    satArray.getDouble(scanIndex), sourceFile.getName(), associationId);
            return p;
        }
    }

    private class ScanLineDenseProvider extends MsProvider {

        final IScanLine scanLineCache;
        final Array satArray;
        final Array fctArray;
        final Array sctArray;

        ScanLineDenseProvider(IFileFragment sourceFile, int associationId, double massBinResolution, int size, Tuple2D<Double, Double> minMaxMassRange) {
            super(sourceFile, associationId, massBinResolution, size, minMaxMassRange);
            scanLineCache = ScanLineCacheFactory.getSparseScanLineCache(sourceFile);
            satArray = sourceFile.getChild(scanAcquisitionTimeVar).getArray();
            fctArray = sourceFile.getChild(firstColumnElutionTimeVar).getArray();
            sctArray = sourceFile.getChild(secondColumnElutionTimeVar).getArray();
        }

        @Override
        Peak2D provide(int scanIndex) {
            Point pt = scanLineCache.mapIndex(scanIndex);
            Tuple2D<Array, Array> t = scanLineCache.getSparseMassSpectrum(pt);
            Array mz = Array.factory(t.getFirst().getElementType(), new int[]{size});
            Array intens = Array.factory(t.getSecond().getElementType(), new int[]{size});
            ArrayTools.createDenseArray(t.getFirst(), t.getSecond(), new Tuple2D<>(mz, intens), ((int) Math.floor(minMaxMassRange.getFirst())), ((int) Math.ceil(minMaxMassRange.getSecond())), size, massBinResolution, 0.0d);
            Peak2D p = new Peak2D(scanIndex, intens,
                    satArray.getDouble(scanIndex), sourceFile.getName(), associationId);
            return p;
        }
    }
}
