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
package maltcms.datastructures.alignment;

import cross.Factory;
import cross.annotations.Configurable;
import cross.datastructures.tuple.Tuple2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.IAnchor;

/**
 * Implementation of a pairset for anything implementing
 * {@code maltcms.datastructures.alignment.DefaultPairSet}.
 *
 * @author Nils Hoffmann
 * @param <T>
 * 
 */
@Slf4j
public class DefaultPairSet<T extends IAnchor> implements
        Iterable<Tuple2D<T, T>> {

    List<Tuple2D<T, T>> al = new ArrayList<>();
    @Configurable
    private int minScansBetweenAnchors = 1;

    /**
     * <p>Getter for the field <code>minScansBetweenAnchors</code>.</p>
     *
     * @return a int.
     */
    public int getMinScansBetweenAnchors() {
        return minScansBetweenAnchors;
    }

    /**
     * <p>Setter for the field <code>minScansBetweenAnchors</code>.</p>
     *
     * @param minScansBetweenAnchors a int.
     */
    public void setMinScansBetweenAnchors(int minScansBetweenAnchors) {
        this.minScansBetweenAnchors = minScansBetweenAnchors;
    }

    /**
     * <p>Constructor for DefaultPairSet.</p>
     *
     * @param a1 a {@link java.util.List} object.
     * @param a2 a {@link java.util.List} object.
     */
    public DefaultPairSet(final List<T> a1, final List<T> a2) {
        this.minScansBetweenAnchors = Factory.getInstance().getConfiguration()
                .getInt(this.getClass().getName() + ".minScansBetweenAnchors",
                        1);
        // EvalTools.eqI(a1.size(),a2.size());
        this.al = prepareWithSet(a1, a2);
    }

    /**
     * <p>getCorrespondingScans.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Tuple2D<Integer, Integer>> getCorrespondingScans() {
        final ArrayList<Tuple2D<Integer, Integer>> al1 = new ArrayList<>(
                getSize());
        for (final Tuple2D<T, T> t : this) {
            al1.add(new Tuple2D<>(t.getFirst().getScanIndex(),
                    t.getSecond().getScanIndex()));
        }
        return al1;
    }

    /**
     * <p>getSize.</p>
     *
     * @return a int.
     */
    public int getSize() {
        return this.al.size();
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Tuple2D<T, T>> iterator() {
        return this.al.iterator();
    }

    /**
     * <p>prepareWithSet.</p>
     *
     * @param a1 a {@link java.util.List} object.
     * @param a2 a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    protected List<Tuple2D<T, T>> prepareWithSet(final List<T> a1,
            final List<T> a2) {
        final List<Tuple2D<T, T>> pairedAnchors = new ArrayList<>();
        final HashMap<String, T> s1 = new HashMap<>();
        this.log.debug("Number of anchors: " + pairedAnchors.size());
        for (final T a : a1) {
            this.log.info("Adding lhs anchor with retention index "
                    + a.toString() + " scan index: " + a.getScanIndex());
            s1.put(a.toString(), a);
        }
        for (final T b : a2) {
            this.log.info("Adding rhs anchor with retention index "
                    + b.toString() + " scan index: " + b.getScanIndex());
            if (s1.containsKey(b.toString())) {
                this.log.info("IAnchor matches for ("
                        + s1.get(b.toString()).getScanIndex() + "<->"
                        + b.getScanIndex() + ")");
                pairedAnchors.add(new Tuple2D<>(s1.get(b.toString()), b));
            }
        }

        final List<Tuple2D<T, T>> validAnchors = pairedAnchors;// checkConsistency(pairedAnchors);
        this.log.info("Retaining {} anchors.", validAnchors.size());
        for (final Tuple2D<T, T> t : validAnchors) {
            final T a = t.getFirst();
            final T b = t.getSecond();
            // log.info(a.getName()+"
            // "+a.getScanIndex()+"<->"+b.getName()+" "+b.getScanIndex());
            this.log.info(a.getName() + " " + a.getScanIndex() + ":"
                    + b.getName() + " " + b.getScanIndex());
        }
        return validAnchors;
    }

    /**
     * <p>checkConsistency.</p>
     *
     * @param pairedAnchors a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    public List<Tuple2D<T, T>> checkConsistency(
            List<Tuple2D<T, T>> pairedAnchors) {
        int prevx = -1;
        int prevy = -1;
        final ArrayList<Tuple2D<T, T>> validAnchors = new ArrayList<>();
        for (int i = 0; i < pairedAnchors.size(); i++) {
            final Tuple2D<T, T> tpl = pairedAnchors.remove(i);
            // if current indices are lower, we must remove the last anchor
            // allowed configurations:
            // Let u be the current anchor's row and let u' be the previous
            // anchor's
            // row
            // and let v be the current anchor's col and let v' be the previous
            // anchor's col
            // then (u-u'>1 && v-v'>1) is a valid configuration. If either
            // fails, u,v is not a valid
            // anchor and must be removed.
            // the first anchor is always valid

            if (i == 0) {
                prevx = tpl.getFirst().getScanIndex();
                prevy = tpl.getSecond().getScanIndex();
                // add first tuple to valid anchors
                validAnchors.add(tpl);
            }
            // next cases, check, whether current anchor is valid w.r.t.
            // previous anchor
            if ((i > 0) && i < pairedAnchors.size() - 1) {
                if ((tpl.getFirst().getScanIndex() - prevx) > 1
                        && (tpl.getSecond().getScanIndex() - prevy) > 1) {
                    validAnchors.add(tpl);
                    this.log.info("Keeping valid anchor at {},{}", tpl
                            .getFirst().getScanIndex(), tpl.getSecond()
                            .getScanIndex());
                    prevx = tpl.getFirst().getScanIndex();
                    prevy = tpl.getSecond().getScanIndex();
                } else {
                    this.log.info("Removing invalid anchor at {},{}", tpl
                            .getFirst().getScanIndex(), tpl.getSecond()
                            .getScanIndex());
                    prevx = validAnchors.get(validAnchors.size() - 1)
                            .getFirst().getScanIndex();
                    prevy = validAnchors.get(validAnchors.size() - 1)
                            .getSecond().getScanIndex();
                }
            }
            // end case, remove preceding anchor, if it violates
            // our monotonicity assumption
            if ((i == pairedAnchors.size() - 1)) {
                if ((tpl.getFirst().getScanIndex() - prevx) > 1
                        && (tpl.getSecond().getScanIndex() - prevy) > 1) {
                    validAnchors.add(tpl);
                    this.log.info("Keeping valid anchor at {},{}", tpl
                            .getFirst().getScanIndex(), tpl.getSecond()
                            .getScanIndex());
                    prevx = tpl.getFirst().getScanIndex();
                    prevy = tpl.getSecond().getScanIndex();
                } else {
                    validAnchors.remove(validAnchors.size() - 1);
                    validAnchors.add(tpl);
                    this.log.info("Removing anchor before final anchor");
                    prevx = tpl.getFirst().getScanIndex();
                    prevy = tpl.getSecond().getScanIndex();
                }
            }
        }
        return validAnchors;
    }
}
