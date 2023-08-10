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
package smueller.tools;

import cross.tools.MathTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smueller.SymbolicRepresentationAlignment;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

// Statische Methode, die zur Standardisiereung be�tigt werden
/**
 * <p>Standardizer class.</p>
 *
 * @author Soeren Mueller
 * 
 */

public class Standardizer {
    
    private static final Logger log = LoggerFactory.getLogger(Standardizer.class);
    
    private double median;
    private double min;
    private double max;

    // Abziehen des Median, teilen durch Standardabweichung f�r alle
    // Arraywerte,
    // addiert minimum um im pos. wertebereich zu bleiben
    /**
     * <p>cleanbase.</p>
     *
     * @param a1 a {@link ucar.ma2.Array} object.
     * @param median1 a double.
     * @param deviation a double.
     * @return a {@link ucar.ma2.Array} object.
     */
    public Array cleanbase(final Array a1, final double median1,
            final double deviation) {
        final Array a = a1.copy();
        final IndexIterator ii4 = a.getIndexIterator();
        final IndexIterator ii5 = a.getIndexIterator();
        while (ii4.hasNext()) {

            final double save = ((ii4.getDoubleNext() - median1));
            ii4.setDoubleCurrent(save);
        }
        final double min2 = smueller.tools.ArrayTools.calcmin(a);
        if (min2 < 0) {
            while (ii5.hasNext()) {
                final double save = ii5.getDoubleNext() + Math.abs(min2);
                ii5.setDoubleCurrent(save);
            }
        }

        return a;
    }

    /**
     * <p>cleanbaseline.</p>
     *
     * @param a1 a {@link ucar.ma2.Array} object.
     * @return a {@link ucar.ma2.Array} object.
     */
    public Array cleanbaseline(final Array a1) {
        final Array a = a1.copy();
        // Globale Medianberechnung, verbraucht viel zus�tzl. Speicher, weil
        // in
        // Java Array kopiert werden muss.
        SymbolicRepresentationAlignment.getSorti().setSortedjavarray1(
                (double[]) a.copyTo1DJavaArray());
        this.median = MathTools.median(SymbolicRepresentationAlignment
                .getSorti().getSortedjavarray1());
        SymbolicRepresentationAlignment.getSorti().setSortedjavarray1(null);
        final double standarddev = standardDeviation(a, this.median);
        Array b = a;
        b = cleanbase(a, this.median, standarddev);
        return b;
    }

    /**
     * <p>Getter for the field <code>max</code>.</p>
     *
     * @return a double.
     */
    public double getMax() {
        return this.max;
    }

    /**
     * <p>Getter for the field <code>median</code>.</p>
     *
     * @return a double.
     */
    public double getMedian() {
        return this.median;
    }

    /**
     * <p>Getter for the field <code>min</code>.</p>
     *
     * @return a double.
     */
    public double getMin() {
        return this.min;
    }

    // zieht logarithmus
    /**
     * <p>logData.</p>
     *
     * @param a1 a {@link ucar.ma2.Array} object.
     * @return a {@link ucar.ma2.Array} object.
     */
    public Array logData(final Array a1) {
        final Array a = a1.copy();
        final IndexIterator ii4 = a.getIndexIterator();
        while (ii4.hasNext()) {
            final double save = (Math.log(ii4.getDoubleNext()));
            ii4.setDoubleCurrent(save);
            // log.info("Wert " + " :" + save);
        }
        return a;
    }

    // Berechnung des globalen Mittelwertes
    /**
     * <p>mean.</p>
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @return a double.
     */
    public double mean(final Array a) {
        final IndexIterator ii1 = a.getIndexIterator();
        double i = 0;
        while (ii1.hasNext()) {
            i = i + ii1.getDoubleNext();
        }
        i = i / a.getSize();
        return i;
    }

    // setzt alle negativen Arraywerte auf 0
    /**
     * <p>nullData.</p>
     *
     * @param a1 a {@link ucar.ma2.Array} object.
     * @return a {@link ucar.ma2.Array} object.
     */
    public Array nullData(final Array a1) {
        final Array a = a1.copy();
        final IndexIterator ii4 = a.getIndexIterator();
        while (ii4.hasNext()) {
            final double save = ii4.getDoubleNext();
            if (save > 0) {
                ii4.setDoubleCurrent(save);
            } else {
                ii4.setDoubleCurrent(0);
                // log.info("Wert " + " :" + save);
            }
        }
        return a;
    }

    // Standardisierung, skalieren auf Wertebereich 0-1
    /**
     * <p>scale.</p>
     *
     * @param a1 a {@link ucar.ma2.Array} object.
     * @return a {@link ucar.ma2.Array} object.
     */
    public Array scale(final Array a1) {
        final Array a = a1.copy();
        final IndexIterator ii4 = a.getIndexIterator();
        this.max = smueller.tools.ArrayTools.calcmax(a);
        this.min = smueller.tools.ArrayTools.calcmin(a);

        int counter = 0;
        final double minmax = 1 / (this.max - this.min);
        log.info("Larifari" + Math.round(minmax) + "Min" + this.min
                + "max" + this.max);
        while (ii4.hasNext()) {
            final double save = ((ii4.getDoubleNext() - this.min) * minmax);
            ii4.setDoubleCurrent(save);
            counter++;
            // log.info("Wert " + counter + " :" + save);

        }
        return a;
    }

    // Berechnung der Standardabweichung
    /**
     * <p>standardDeviation.</p>
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param meanvalue a double.
     * @return a double.
     */
    public double standardDeviation(final Array a, final double meanvalue) {
        final IndexIterator ii2 = a.getIndexIterator();
        double i = 0;
        double counter = 0;
        while (ii2.hasNext()) {
            final double calc = ii2.getDoubleNext();
            i = i + ((calc - meanvalue) * (calc - meanvalue));
            counter++;
        }

        counter = 1 / (counter - 1);
        i = (i * counter);
        i = Math.sqrt(i);
        return i;
    }
}
