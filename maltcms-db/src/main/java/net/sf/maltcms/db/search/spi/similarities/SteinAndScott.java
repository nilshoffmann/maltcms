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
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.IMetabolite;
import net.sf.maltcms.db.search.api.similarities.AMetabolitePredicate;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;

/**
 * <p>SteinAndScott class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
@ServiceProvider(service = AMetabolitePredicate.class)
public class SteinAndScott extends AMetabolitePredicate {

    boolean toggle = true;
    private AMDISMSSimilarity iadc = new AMDISMSSimilarity();
    private double resolution = 1.0d;
    private double lastMin = Double.POSITIVE_INFINITY,
            lastMax = Double.NEGATIVE_INFINITY;
    private boolean normalize = true;
    private final Comparator<Tuple2D<Double, IMetabolite>> comparator = Collections.reverseOrder(new Comparator<Tuple2D<Double, IMetabolite>>() {
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
     * <p>Constructor for SteinAndScott.</p>
     */
    public SteinAndScott() {
    }

    /** {@inheritDoc} */
    @Override
    public AMetabolitePredicate copy() {
        SteinAndScott ms = new SteinAndScott();
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
        return iadc.apply(new Tuple2D<>(massesRef, intensitiesRef), new Tuple2D<>(massesQuery, intensitiesQuery));
    }

    /** {@inheritDoc} */
    @Override
    public boolean match(IMetabolite et) {
        Tuple2D<ArrayDouble.D1, ArrayInt.D1> etMs = et.getMassSpectrum();
        Array m1 = getScan().getMasses();
        Array s1 = getScan().getIntensities();

        Array m2 = etMs.getFirst();
        Array s2 = etMs.getSecond();
        s1 = filterMaskedMasses(m1, s1);
        s2 = filterMaskedMasses(m2, s2);
        double sim = similarity(m1, s1, m2, s2,
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
