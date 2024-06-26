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
package maltcms.db.predicates.metabolite;

import com.db4o.query.Predicate;
import cross.datastructures.tuple.Tuple2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.ms.IScan;
import maltcms.datastructures.ms.Scan1D;
import maltcms.math.functions.IArraySimilarity;
import maltcms.math.functions.similarities.ArrayCos;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;

@Data
/**
 * <p>MetaboliteSimilarity class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@EqualsAndHashCode(callSuper = true)
public class MetaboliteSimilarity extends Predicate<IMetabolite> {

    //properties
    private IArraySimilarity similarityFunction = new ArrayCos();
    private double resolution = 1.0d;
    private double scoreThreshold = 0.6d;
    private double massThreshold = 0.01;
    private int numberOfHitsToReturn = 1;
    private boolean normalize = false;
    private IScan scan;
    //internal state
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private double lastMin = Double.POSITIVE_INFINITY,
            lastMax = Double.NEGATIVE_INFINITY;
    private final List<Tuple2D<Double, IMetabolite>> matches = new ArrayList<>();
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

    /**
     * <p>Constructor for MetaboliteSimilarity.</p>
     *
     * @param scan a {@link maltcms.datastructures.ms.IScan} object.
     * @param scoreThreshold a double.
     * @param maxHits a int.
     * @param normalize a boolean.
     */
    public MetaboliteSimilarity(IScan scan, double scoreThreshold,
            int maxHits, boolean normalize) {
        this.scan = scan;
        this.scoreThreshold = scoreThreshold;
        this.numberOfHitsToReturn = maxHits;
        this.normalize = normalize;
    }

    /**
     * <p>Constructor for MetaboliteSimilarity.</p>
     *
     * @param masses a {@link ucar.ma2.Array} object.
     * @param intensities a {@link ucar.ma2.Array} object.
     * @param scoreThreshold a double.
     * @param maxHits a int.
     * @param normalize a boolean.
     */
    public MetaboliteSimilarity(Array masses, Array intensities,
            double scoreThreshold,
            int maxHits, boolean normalize) {
        this(new Scan1D(masses, intensities, -1,
                Double.NaN), scoreThreshold, maxHits,
                normalize);
    }

    /**
     * <p>Constructor for MetaboliteSimilarity.</p>
     *
     * @param metabolite a {@link maltcms.datastructures.ms.IMetabolite} object.
     * @param scoreThreshold a double.
     * @param maxHits a int.
     * @param normalize a boolean.
     */
    public MetaboliteSimilarity(IMetabolite metabolite, double scoreThreshold,
            int maxHits, boolean normalize) {
        this(new Scan1D(metabolite.getMassSpectrum().getFirst(), metabolite.
                getMassSpectrum().getSecond(), metabolite.getScanIndex(),
                metabolite.getRetentionTime()), scoreThreshold, maxHits,
                normalize);
    }

    /**
     * <p>Getter for the field <code>matches</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Tuple2D<Double, IMetabolite>> getMatches() {
        Collections.sort(this.matches, comparator);
        return this.matches;
    }

    /**
     * <p>similarity.</p>
     *
     * @param massesRef a {@link ucar.ma2.Array} object.
     * @param intensitiesRef a {@link ucar.ma2.Array} object.
     * @param massesQuery a {@link ucar.ma2.Array} object.
     * @param intensitiesQuery a {@link ucar.ma2.Array} object.
     * @return a double.
     */
    protected double similarity(Array massesRef, Array intensitiesRef,
            Array massesQuery, Array intensitiesQuery) {
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

        //normalization
        if (normalize) {
            double maxS2 = MAMath.getMaximum(s2);
            s2 = (ArrayDouble.D1) ArrayTools.mult(s2, 1.0d / maxS2);
        }
//        double commonMasses = 0.0d;
//        double matchMW = 0.0d;
//        for (int i = 0; i < dmasses1.getShape()[0]; i++) {
//            double mass = dmasses1.getDouble(i);
//            if (s1.getDouble(i) != 0 && s2.getDouble(i) != 0) {
//                commonMasses++;
//            }
//        }
        //FIXME try whether it makes a difference if the minimal interval of overlap
        //is used
//        double relativeCommonMasses = (commonMasses) / (double) bins;
        double d = this.similarityFunction.apply(s1, s2);
        return d;
    }

    /** {@inheritDoc} */
    @Override
    public boolean match(IMetabolite et) {
        Tuple2D<ArrayDouble.D1, ArrayInt.D1> etMs = et.getMassSpectrum();
        double sim = similarity(scan.getMasses(), scan.getIntensities(), etMs.
                getFirst(), etMs.getSecond());
        if (sim >= scoreThreshold) {
            if (matches.size() == numberOfHitsToReturn) {
                Collections.sort(matches, comparator);
                Tuple2D<Double, IMetabolite> tple = new Tuple2D<>(
                        sim, et);
                int idx = Collections.binarySearch(matches, tple, comparator);
                if (idx >= 0) {
                    matches.add(idx, tple);
                    matches.remove(matches.size() - 1);
                } else {
                    int insertionPoint = ((-1) * idx) + 1;
                    if (insertionPoint != matches.size()) {
                        matches.add(insertionPoint, tple);
                        matches.remove(matches.size() - 1);
                    }
                }
            } else {
                matches.add(new Tuple2D<>(sim, et));
                Collections.sort(matches, comparator);
            }

            return true;
        }
        return false;
    }
}
