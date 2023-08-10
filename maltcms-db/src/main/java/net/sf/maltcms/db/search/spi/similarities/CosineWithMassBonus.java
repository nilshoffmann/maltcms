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
import java.util.Collections;
import java.util.Comparator;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.math.functions.IArraySimilarity;
import maltcms.math.functions.similarities.ArrayCos;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import net.sf.maltcms.db.search.api.similarities.AMetabolitePredicate;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;

/**
 * <p>CosineWithMassBonus class.</p>
 *
 * @author Nils Hoffmann
 * 
 */

@ServiceProvider(service = AMetabolitePredicate.class)
public class CosineWithMassBonus extends AMetabolitePredicate {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CosineWithMassBonus.class);

    boolean toggle = true;
    private IArraySimilarity iadc = new ArrayCos();
    private double resolution = 1.0d;
    private double lastMin = Double.POSITIVE_INFINITY,
            lastMax = Double.NEGATIVE_INFINITY;
    private boolean normalize = true;
    private final Comparator<Tuple2D<Double, IMetabolite>> comparator = Collections.
            reverseOrder(new Comparator<Tuple2D<Double, IMetabolite>>() {
                @Override
                public int compare(Tuple2D<Double, IMetabolite> t,
                        Tuple2D<Double, IMetabolite> t1) {
                    if (t.getFirst() > t1.getFirst()) {
                        return 1;
                    } else if (t.getFirst() < t1.getFirst()) {
                        return -1;
                    }
                    return 0;
                }
            });
    private double threshold = 1.0;

    /**
     * <p>isNormalize.</p>
     *
     * @return a boolean.
     */
    public boolean isNormalize() {
        return normalize;
    }

    /**
     * <p>Setter for the field <code>normalize</code>.</p>
     *
     * @param normalize a boolean.
     */
    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    /**
     * <p>Getter for the field <code>resolution</code>.</p>
     *
     * @return a double.
     */
    public double getResolution() {
        return resolution;
    }

    /**
     * <p>Setter for the field <code>resolution</code>.</p>
     *
     * @param resolution a double.
     */
    public void setResolution(double resolution) {
        this.resolution = resolution;
    }

    /**
     * <p>Constructor for CosineWithMassBonus.</p>
     */
    public CosineWithMassBonus() {
    }

    /** {@inheritDoc} */
    @Override
    public AMetabolitePredicate copy() {
        CosineWithMassBonus ms = new CosineWithMassBonus();
        ms.setResolution(resolution);
//        ms.setThreshold(threshold);
        ms.setMaxHits(getMaxHits());
        ms.setNormalize(normalize);
        ms.setScoreThreshold(getScoreThreshold());
//        ms.setScan(getScan());
        ms.setMaskedMasses(getMaskedMasses());
        return ms;
    }

    /**
     * <p>similarity.</p>
     *
     * @param massesRef a {@link ucar.ma2.Array} object.
     * @param intensitiesRef a {@link ucar.ma2.Array} object.
     * @param massesQuery a {@link ucar.ma2.Array} object.
     * @param intensitiesQuery a {@link ucar.ma2.Array} object.
     * @param mw a double.
     * @return a double.
     */
    protected double similarity(Array massesRef, Array intensitiesRef,
            Array massesQuery, Array intensitiesQuery, double mw) {
        //return new AMDISMSSimilarity().apply(new Tuple2D<Array,Array>(massesRef, intensitiesRef), new Tuple2D<Array,Array>(massesQuery,intensitiesQuery),mw);
        MinMax mm1 = MAMath.getMinMax(massesRef);
        MinMax mm2 = MAMath.getMinMax(massesQuery);
        // Union, greatest possible interval
        double max = Math.max(mm1.max, mm2.max);
        double min = Math.min(mm1.min, mm2.min);
        int bins = MaltcmsTools.getNumberOfIntegerMassBins(min, max, resolution);

        ArrayDouble.D1 s1 = null, s2 = null;
        ArrayDouble.D1 dmasses1 = new ArrayDouble.D1(bins);
        s1 = new ArrayDouble.D1(bins);
        ArrayTools.createDenseArray(massesRef, intensitiesRef,
                new Tuple2D<Array, Array>(dmasses1, s1), ((int) Math.floor(min)),
                ((int) Math.ceil(max)), bins,
                resolution, 0.0d);
        s1 = (ArrayDouble.D1) filterMaskedMasses(dmasses1, s1);
//		}
        //normalization to 0..1
        if (normalize) {
            double maxS1 = MAMath.getMaximum(s1);
            s1 = (ArrayDouble.D1) ArrayTools.mult(s1, 1.0d / maxS1);
        }
        ArrayDouble.D1 dmasses2 = new ArrayDouble.D1(bins);
        s2 = new ArrayDouble.D1(bins);
        ArrayTools.createDenseArray(massesQuery, intensitiesQuery,
                new Tuple2D<Array, Array>(dmasses2, s2),
                ((int) Math.floor(min)), ((int) Math.ceil(max)), bins,
                resolution, 0.0d);
        s2 = (ArrayDouble.D1) filterMaskedMasses(dmasses2, s2);
        //normalization
        if (normalize) {
            double maxS2 = MAMath.getMaximum(s2);
            s2 = (ArrayDouble.D1) ArrayTools.mult(s2, 1.0d / maxS2);
        }
        double commonMasses = 0.0d;
        double matchMW = 0.0d;
        for (int i = 0; i < dmasses1.getShape()[0]; i++) {
            double mass = dmasses1.getDouble(i);
            if (s1.getDouble(i) != 0 && s2.getDouble(i) != 0) {
                double percentDev = 0.10;
                double val = Math.abs(s1.getDouble(i) - s2.getDouble(
                        i)) / Math.max(s1.getDouble(i), s2.getDouble(
                                        i));
                if (Math.abs(mass - mw) < threshold && val <= percentDev) {
                    matchMW = 1.0;
                }
                commonMasses++;
            }
        }
        //FIXME try whether it makes a difference if the minimal interval of overlap
        //is used
        double relativeCommonMasses = (commonMasses) / (double) bins;
        double d = this.iadc.apply(s1, s2);
        return (d + matchMW);
    }

    /** {@inheritDoc} */
    @Override
    public boolean match(IMetabolite et) {
        Tuple2D<ArrayDouble.D1, ArrayInt.D1> etMs = et.getMassSpectrum();
        double sim = similarity(getScan().getMasses(),
                getScan().getIntensities(), etMs.getFirst(), etMs.getSecond(),
                et.getMW());
        log.info("Similarity score: " + sim);
        if (sim >= getScoreThreshold()) {
            Tuple2D<Double, IMetabolite> tple = new Tuple2D<>(
                    sim, et);
            getScoreMap().add(tple);

            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Comparator<Tuple2D<Double, IMetabolite>> getComparator() {
        return this.comparator;
    }
}
