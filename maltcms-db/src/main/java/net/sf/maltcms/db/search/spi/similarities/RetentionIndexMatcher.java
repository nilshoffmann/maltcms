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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.IMetabolite;
import net.sf.maltcms.db.search.api.similarities.AMetabolitePredicate;

//@ServiceProvider(service = AMetabolitePredicate.class)
/**
 * <p>RetentionIndexMatcher class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
public class RetentionIndexMatcher extends AMetabolitePredicate {

    private AMetabolitePredicate delegate = new Cosine();
    private double retentionIndex = Double.NaN;
    private List<Tuple2D<Double, IMetabolite>> metabolites = new LinkedList<>();

    /**
     * <p>Getter for the field <code>delegate</code>.</p>
     *
     * @return a {@link net.sf.maltcms.db.search.api.similarities.AMetabolitePredicate} object.
     */
    public AMetabolitePredicate getDelegate() {
        return delegate;
    }

    /**
     * <p>Setter for the field <code>delegate</code>.</p>
     *
     * @param delegate a {@link net.sf.maltcms.db.search.api.similarities.AMetabolitePredicate} object.
     */
    public void setDelegate(AMetabolitePredicate delegate) {
        this.delegate = delegate;
        this.delegate.setScoreThreshold(0.0);
    }

    /**
     * <p>Getter for the field <code>retentionIndex</code>.</p>
     *
     * @return a double.
     */
    public double getRetentionIndex() {
        return retentionIndex;
    }

    /**
     * <p>Setter for the field <code>retentionIndex</code>.</p>
     *
     * @param retentionIndex a double.
     */
    public void setRetentionIndex(double retentionIndex) {
        this.retentionIndex = retentionIndex;
    }

    /** {@inheritDoc} */
    @Override
    public double getScoreThreshold() {
        return delegate.getScoreThreshold();
    }

    /** {@inheritDoc} */
    @Override
    public void setScoreThreshold(double scoreThreshold) {
        delegate.setScoreThreshold(scoreThreshold);
    }

    /** {@inheritDoc} */
    @Override
    public TreeSet<Tuple2D<Double, IMetabolite>> getScoreMap() {
        return delegate.getScoreMap();
    }
//    private List<Tuple2D<Double, IMetabolite>> metToScore = new ArrayList<Tuple2D<Double, IMetabolite>>();
//    private final Comparator<Tuple2D<Double, IMetabolite>> comparator = Collections.
//            reverseOrder(new Comparator<Tuple2D<Double, IMetabolite>>() {
//
//        @Override
//        public int compare(Tuple2D<Double, IMetabolite> t,
//                Tuple2D<Double, IMetabolite> t1) {
//            if (t.getFirst() > t1.getFirst()) {
//                return 1;
//            } else if (t.getFirst() < t1.getFirst()) {
//                return -1;
//            }
//            return 0;
//        }
//    });
    private double window = 10.0;

    /**
     * <p>Getter for the field <code>window</code>.</p>
     *
     * @return a double.
     */
    public double getWindow() {
        return window;
    }

    /**
     * <p>Setter for the field <code>window</code>.</p>
     *
     * @param threshold a double.
     */
    public void setWindow(double threshold) {
        this.window = threshold;
    }

    /**
     * <p>Constructor for RetentionIndexMatcher.</p>
     */
    public RetentionIndexMatcher() {
    }

    /** {@inheritDoc} */
    @Override
    public AMetabolitePredicate copy() {
        RetentionIndexMatcher ms = new RetentionIndexMatcher();
        ms.setWindow(getWindow());
        ms.setMaxHits(getMaxHits());
        ms.setScoreThreshold(getScoreThreshold());
//        ms.setScan(getScan());
        ms.setDelegate(getDelegate());
        ms.setRetentionIndex(getRetentionIndex());
        ms.setMaskedMasses(getMaskedMasses());
        return ms;
    }

    /** {@inheritDoc} */
    @Override
    public boolean match(IMetabolite et) {

        if (Double.isNaN(retentionIndex) || et.getRetentionIndex() == 0.0) {
//            log.info("RI is not a number! Using fallback similarity");
            //fallback
//            return delegate.match(et);
            return false;
        } else {
            //filter
            double delta = Math.abs(retentionIndex - et.getRetentionIndex());
            if (delta <= window) {

//                metabolites.add(new Tuple2D<Double, IMetabolite>(delta, et));
                //FIXME this is a quickfix
                if (delegate.match(et)) {
                    log.info("Candidate above threshold and within window: " + delta);
                    return true;
                }
//                //log.info("Delegate score "+delegate.getClass().getName()+" match: "+match);
//                return true;
//                return true;
            }
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Comparator<Tuple2D<Double, IMetabolite>> getComparator() {
        return delegate.getComparator();
    }
}
