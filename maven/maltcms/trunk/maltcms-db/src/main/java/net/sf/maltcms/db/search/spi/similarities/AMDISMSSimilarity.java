/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.db.search.spi.similarities;

import cross.datastructures.tuple.Tuple2D;
import java.util.BitSet;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;

/**
 *
 * @author nilshoffmann
 */
public class AMDISMSSimilarity {

    private Array getIndexSubset(Array a, BitSet bs) {
        Array ret = Array.factory(a.getElementType(),
                new int[]{bs.cardinality()});
        int idx = 0;
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            ret.setDouble(idx, a.getDouble(i));
            idx++;
        }
        return ret;
    }

    private void fillBitSet(Array t, double threshold, BitSet tpeaks) {
        IndexIterator iter1 = t.getIndexIterator();
        int idx = 0;
        while (iter1.hasNext()) {
            double val = iter1.getDoubleNext();
            if (val > threshold) {
                tpeaks.set(idx);
            }
            idx++;
        }
    }

    public Double applyOnShared(Array mra, Array ira, Array mqa, Array iqa,
            double massExponent, double intensityExponent) {


//        BitSet libraryPeaks = new BitSet();
//        fillBitSet(ira, 0.0, libraryPeaks);
//        BitSet unknownPeaks = new BitSet();
//        fillBitSet(iqa, 0.0, unknownPeaks);
//
////        BitSet unionPeaks = new BitSet();
////        unionPeaks.or(libraryPeaks);
////        unionPeaks.or(unknownPeaks);
//
//        BitSet intersecPeaks = new BitSet();
//        intersecPeaks.and(unknownPeaks);
//        intersecPeaks.and(libraryPeaks);

        return getMixedValue(mra, ira, mqa,
                iqa, massExponent,
                intensityExponent);
//        return getMixedValue(getIndexSubset(mra, intersecPeaks), getIndexSubset(
//                ira, intersecPeaks), getIndexSubset(mqa, intersecPeaks),
//                getIndexSubset(iqa, intersecPeaks), massExponent,
//                intensityExponent);
    }

    public double getMixedValue(Array mra, Array ira, Array mqa, Array iqa,
            double massExponent, double intensityExponent) {
        double mixedValue = getPairedProductSumOfSquares(mra, ira, mqa, iqa,
                massExponent, intensityExponent);
//        double refValue = getPairedProductSumOfSquares(mra, ira, mra, ira,
//                massExponent, intensityExponent);
//        double queryValue = getPairedProductSumOfSquares(mqa, iqa, mqa, iqa,
//                massExponent, intensityExponent);

//        return mixedValue / (refValue * queryValue);
        double refValue = ArrayTools.integrate(ArrayTools.mult(ArrayTools.pow(
                mra, 2 * massExponent), ArrayTools.pow(ira,
                2 * intensityExponent)));
        double queryValue = ArrayTools.integrate(ArrayTools.mult(ArrayTools.pow(
                mqa, 2 * massExponent), ArrayTools.pow(iqa,
                2 * intensityExponent)));
        return mixedValue / refValue * queryValue;
    }

    public double apply(Tuple2D<Array, Array> referenceMassSpectrum,
            Tuple2D<Array, Array> queryMassSpectrum) {
        double massExponent = 1.0;
        double intensityExponent = 0.5;

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
        return applyOnShared(mra, ira, mqa, iqa, massExponent, intensityExponent);
    }

    public double getPairedProductSumOfSquares(Array mra, Array ira, Array mqa,
            Array iqa, double massExponent, double intensityExponent) {
        double commonProduct = 0;

        for (int i = 0; i < mra.getShape()[0]; i++) {
            double mr = mra.getDouble(i);
            double mq = mqa.getDouble(i);
            double ir = ira.getDouble(i);
            double iq = iqa.getDouble(i);
            commonProduct += Math.pow(Math.pow(mr * mq, massExponent) * Math.pow(
                    ir * iq, intensityExponent), 2.0);
        }
        return commonProduct;
    }
}
