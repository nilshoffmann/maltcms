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
package net.sf.maltcms.db.search.spi.similarities;

import cross.datastructures.tuple.Tuple2D;
import maltcms.math.functions.similarities.ArrayWeightedCosine;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;

/**
 * <p>AMDISMSSimilarity class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class AMDISMSSimilarity {

    private final ArrayWeightedCosine awc = new ArrayWeightedCosine();

//    private Array getIndexSubset(Array a, BitSet bs) {
//        Array ret = Array.factory(a.getElementType(),
//                new int[]{bs.cardinality()});
//        int idx = 0;
//        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
//            ret.setDouble(idx, a.getDouble(i));
//            idx++;
//        }
//        return ret;
//    }
//
//    private void fillBitSet(Array t, double threshold, BitSet tpeaks) {
//        IndexIterator iter1 = t.getIndexIterator();
//        int idx = 0;
//        while (iter1.hasNext()) {
//            double val = iter1.getDoubleNext();
//            if (val > threshold) {
//                tpeaks.set(idx);
//            }
//            idx++;
//        }
//    }
//
//    public Double applyOnShared(Array mra, Array ira, Array mqa, Array iqa,
//            double massExponent, double intensityExponent) {
//
//
////        BitSet libraryPeaks = new BitSet();
////        fillBitSet(ira, 0.0, libraryPeaks);
////        BitSet unknownPeaks = new BitSet();
////        fillBitSet(iqa, 0.0, unknownPeaks);
////
//////        BitSet unionPeaks = new BitSet();
//////        unionPeaks.or(libraryPeaks);
//////        unionPeaks.or(unknownPeaks);
////
////        BitSet intersecPeaks = new BitSet();
////        intersecPeaks.and(unknownPeaks);
////        intersecPeaks.and(libraryPeaks);
//
//        return getMixedValue(mra, ira, mqa,
//                iqa, massExponent,
//                intensityExponent);
////        return getMixedValue(getIndexSubset(mra, intersecPeaks), getIndexSubset(
////                ira, intersecPeaks), getIndexSubset(mqa, intersecPeaks),
////                getIndexSubset(iqa, intersecPeaks), massExponent,
////                intensityExponent);
//    }
//
//    public double getMixedValue(Array mra, Array ira, Array mqa, Array iqa,
//            double massExponent, double intensityExponent) {
//        double mixedValue = getPairedProductSumOfSquares(mra, ira, mqa, iqa,
//                massExponent, intensityExponent);
////        double refValue = getPairedProductSumOfSquares(mra, ira, mra, ira,
////                massExponent, intensityExponent);
////        double queryValue = getPairedProductSumOfSquares(mqa, iqa, mqa, iqa,
////                massExponent, intensityExponent);
//
////        return mixedValue / (refValue * queryValue);
//        double refValue = ArrayTools.integrate(ArrayTools.mult(ArrayTools.pow(
//                mra, 2 * massExponent), ArrayTools.pow(ira,
//                2 * intensityExponent)));
//        double queryValue = ArrayTools.integrate(ArrayTools.mult(ArrayTools.pow(
//                mqa, 2 * massExponent), ArrayTools.pow(iqa,
//                2 * intensityExponent)));
//        return mixedValue / refValue * queryValue;
//    }
    /**
     * <p>apply.</p>
     *
     * @param referenceMassSpectrum a {@link cross.datastructures.tuple.Tuple2D} object.
     * @param queryMassSpectrum a {@link cross.datastructures.tuple.Tuple2D} object.
     * @return a double.
     */
    public double apply(Tuple2D<Array, Array> referenceMassSpectrum,
            Tuple2D<Array, Array> queryMassSpectrum) {
//        double massExponent = 1.0;
//        double intensityExponent = 0.5;

        double resolution = 1.0;

        MinMax mm1 = MAMath.getMinMax(referenceMassSpectrum.getFirst());
        MinMax mm2 = MAMath.getMinMax(queryMassSpectrum.getFirst());
        // Union, greatest possible interval
        double max = Math.max(mm1.max, mm2.max);
        double min = Math.min(mm1.min, mm2.min);
        int bins = MaltcmsTools.getNumberOfIntegerMassBins(min, max, resolution);

        ArrayDouble.D1 ira = null, iqa = null;
        ArrayDouble.D1 mra = new ArrayDouble.D1(bins);
        ira = new ArrayDouble.D1(bins);
        ArrayTools.createDenseArray(referenceMassSpectrum.getFirst(),
                referenceMassSpectrum.getSecond(),
                new Tuple2D<Array, Array>(mra, ira), ((int) Math.floor(min)),
                ((int) Math.ceil(max)), bins,
                resolution, 0.0d);
//		}
        ArrayDouble.D1 mqa = new ArrayDouble.D1(bins);
        iqa = new ArrayDouble.D1(bins);
        ArrayTools.createDenseArray(queryMassSpectrum.getFirst(),
                queryMassSpectrum.getSecond(),
                new Tuple2D<Array, Array>(mqa, iqa),
                ((int) Math.floor(min)), ((int) Math.ceil(max)), bins,
                resolution, 0.0d);

//        double maxS1 = MAMath.getMaximum(ira);
//        ira = (ArrayDouble.D1) ArrayTools.mult(ira, 1.0d / maxS1);
//        double maxS2 = MAMath.getMaximum(iqa);
//        iqa = (ArrayDouble.D1) ArrayTools.mult(iqa, 1.0d / maxS2);
        return awc.apply(mqa, iqa);
//        return applyOnShared(mra, ira, mqa, iqa, massExponent, intensityExponent);
    }

//    public double getPairedProductSumOfSquares(Array mra, Array ira, Array mqa,
//            Array iqa, double massExponent, double intensityExponent) {
//        double commonProduct = 0;
//
//        for (int i = 0; i < mra.getShape()[0]; i++) {
//            double mr = mra.getDouble(i);
//            double mq = mqa.getDouble(i);
//            double ir = ira.getDouble(i);
//            double iq = iqa.getDouble(i);
//            commonProduct += Math.pow(Math.pow(mr * mq, massExponent) * Math.pow(
//                    ir * iq, intensityExponent), 2.0);
//        }
//        return commonProduct;
//    }
}
