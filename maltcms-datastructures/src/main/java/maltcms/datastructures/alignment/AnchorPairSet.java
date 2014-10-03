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

import cross.datastructures.tuple.Tuple2D;
import cross.exception.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.IAnchor;
import maltcms.datastructures.ms.RetentionInfo;

/**
 * Represents a set of matched pairs of anchors.
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
public class AnchorPairSet extends DefaultPairSet<IAnchor> {

    /**
     * <p>Constructor for AnchorPairSet.</p>
     *
     * @param a1 a {@link java.util.List} object.
     * @param a2 a {@link java.util.List} object.
     * @param rows a int.
     * @param cols a int.
     * @param minScansBetweenAnchors a int.
     */
    public AnchorPairSet(final List<IAnchor> a1, final List<IAnchor> a2,
            final int rows, final int cols, final int minScansBetweenAnchors) {
        super(a1, a2);
        // System.out.println(a1.size()+" "+a2.size());
        // EvalTools.eqI(a1.size(),a2.size());
        // prepareWithSet(a1, a2, width, height, this.al);
        super.al = checkAnchors(rows, cols, super.al, minScansBetweenAnchors);
        // checkZeroAndEndPresent(rows, cols, super.al);
    }

    /**
     * <p>Constructor for AnchorPairSet.</p>
     *
     * @param a1 a {@link java.util.List} object.
     * @param a2 a {@link java.util.List} object.
     * @param rows a int.
     * @param cols a int.
     */
    public AnchorPairSet(final List<IAnchor> a1, final List<IAnchor> a2,
            final int rows, final int cols) {
        super(a1, a2);
        // System.out.println(a1.size()+" "+a2.size());
        // EvalTools.eqI(a1.size(),a2.size());
        // prepareWithSet(a1, a2, width, height, this.al);
        super.al = checkAnchors(rows, cols, super.al,
                getMinScansBetweenAnchors());
        // checkZeroAndEndPresent(rows, cols, super.al);
    }

    /**
     * Ensure, that (0,0) and (m-1,n-1) are included as virtual anchors.
     *
     * @param virtual_width a int.
     * @param virtual_height a int.
     * @param al1 a {@link java.util.List} object.
     */
    protected void checkZeroAndEndPresent(final int virtual_width,
            final int virtual_height, final List<Tuple2D<IAnchor, IAnchor>> al1) {
        final IAnchor a1 = new RetentionInfo();
        a1.setName("START");
        a1.setScanIndex(0);
        final IAnchor b1 = new RetentionInfo();
        b1.setName("START");
        b1.setScanIndex(0);
        final IAnchor a2 = new RetentionInfo();
        a2.setName("END");
        a2.setScanIndex(virtual_width - 1);
        final IAnchor b2 = new RetentionInfo();
        b2.setName("END");
        b2.setScanIndex(virtual_height - 1);
        if (al1.isEmpty()) {
            this.log.debug("Only adding start and end anchors!");
            // System.out.println("Only adding start and end anchors!");
            al1.add(new Tuple2D<>(a1, b1));
            al1.add(new Tuple2D<>(a2, b2));
            return;
        }
        // int k = 0;
        Tuple2D<IAnchor, IAnchor> t = al1.get(0);
        if ((t.getFirst().getScanIndex() >= 0)
                && (t.getSecond().getScanIndex() >= 0)) {// if
            // there
            // is
            // no
            // tuple
            // 0,0
            this.log.debug("Adding start anchor!");
            // check for monotonicity:
            if (t.getFirst().getScanIndex() <= 1
                    || t.getSecond().getScanIndex() <= 1) {
                al1.remove(0);
            }
            al1.add(0, new Tuple2D<>(a1, b1));
        } else {
            throw new IllegalArgumentException(
                    "Can not process anchors with scan index < 0!: "
                    + t.getFirst().getScanIndex() + " "
                    + t.getSecond().getScanIndex());
            // Integer a = t.getFirst().getScanIndex();
            // Integer b = t.getSecond().getScanIndex();
            // if (a < 0) {
            // a = 0;
            // }
            // if (b < 0) {
            // b = 0;
            // }
            // this.log1.debug("Adding other anchor at " + a + "," + b);
            // final IAnchor a3 = new RetentionInfo();
            // a3.setName("ANCHOR");
            // a3.setScanIndex(a);
            // final IAnchor b3 = new RetentionInfo();
            // b3.setName("ANCHOR");
            // b3.setScanIndex(b);
            // al1.set(0, new Tuple2D<IAnchor, IAnchor>(a3, b3));
        }

        // process last anchor
        t = al1.get(al1.size() - 1);
        if ((t.getFirst().getScanIndex() < virtual_width - 1)
                && (t.getSecond().getScanIndex() < virtual_height - 1)) {
            this.log.debug("Adding end anchor");
            // al1.add(new Tuple2D<IAnchor, IAnchor>(a2, b2));
        } else {
            t = al1.remove(al1.size() - 1);
        }
        al1.add(new Tuple2D<>(a2, b2));
        // Integer a = t.getFirst().getScanIndex();
        // Integer b = t.getSecond().getScanIndex();
        // if (a > virtual_width) {
        // a = virtual_width - 1;
        // }
        // if (b > virtual_height) {
        // b = virtual_height - 1;
        // }
        // this.log1.debug("Adding other anchor at {},{}", a, b);
        // final IAnchor a4 = new RetentionInfo();
        // a4.setName("ANCHOR");
        // a4.setScanIndex(a);
        // final IAnchor b4 = new RetentionInfo();
        // b4.setName("ANCHOR");
        // b4.setScanIndex(b);
        // al1.set(al1.size(), new Tuple2D<IAnchor, IAnchor>(a4, b4));
        // }
    }

    /**
     * <p>checkAnchors.</p>
     *
     * @param rows a int.
     * @param cols a int.
     * @param l a {@link java.util.List} object.
     * @param minScansBetweenAnchors a int.
     * @return a {@link java.util.List} object.
     */
    public List<Tuple2D<IAnchor, IAnchor>> checkAnchors(int rows, int cols,
            List<Tuple2D<IAnchor, IAnchor>> l, int minScansBetweenAnchors) {
        ArrayList<Tuple2D<IAnchor, IAnchor>> valid = new ArrayList<>();

        // first element needs to be (0,0)
        if (l == null || l.isEmpty()) {
            RetentionInfo s1 = new RetentionInfo();
            RetentionInfo s2 = new RetentionInfo();
            s1.setName("START");
            s2.setName("START");
            s1.setScanIndex(0);
            s2.setScanIndex(0);
            valid.add(0, new Tuple2D<IAnchor, IAnchor>(s1, s2));
            s1 = new RetentionInfo();
            s2 = new RetentionInfo();
            s1.setName("END");
            s2.setName("END");
            s1.setScanIndex(rows - 1);
            s2.setScanIndex(cols - 1);
            valid.add(new Tuple2D<IAnchor, IAnchor>(s1, s2));
            return valid;
        }
        Tuple2D<IAnchor, IAnchor> first = l.get(0);
        // if not present, prepend
        if (first.getFirst().getScanIndex() == 0
                && first.getSecond().getScanIndex() == 0) {
            l.remove(0);
        }
        RetentionInfo s1 = new RetentionInfo();
        RetentionInfo s2 = new RetentionInfo();
        s1.setName("START");
        s2.setName("START");
        s1.setScanIndex(0);
        s2.setScanIndex(0);
        l.add(0, new Tuple2D<IAnchor, IAnchor>(s1, s2));

        Tuple2D<IAnchor, IAnchor> prev = l.remove(0);
        valid.add(prev);
        for (Tuple2D<IAnchor, IAnchor> t : l) {
            Tuple2D<IAnchor, IAnchor> current = t;
            int deltaRow = t.getFirst().getScanIndex()
                    - prev.getFirst().getScanIndex();
            int deltaCol = t.getSecond().getScanIndex()
                    - prev.getSecond().getScanIndex();
            if (deltaRow >= getMinScansBetweenAnchors()
                    && deltaCol >= getMinScansBetweenAnchors()) {
                valid.add(current);
                prev = current;
            }
        }

        if (l.isEmpty()) {
            this.log.debug("List is empty, adding default end anchor");
            s1 = new RetentionInfo();
            s2 = new RetentionInfo();
            s1.setName("END");
            s2.setName("END");
            s1.setScanIndex(rows - 1);
            s2.setScanIndex(cols - 1);
            valid.add(new Tuple2D<IAnchor, IAnchor>(s1, s2));
        } else {
            // last element needs to be (rows-1,cols-1)
            Tuple2D<IAnchor, IAnchor> last = valid.get(valid.size() - 1);
            this.log.debug("last: {}, {}", last.getFirst().getScanIndex(), last
                    .getSecond().getScanIndex());
            int deltaRow = rows - 1 - last.getFirst().getScanIndex();
            int deltaCol = cols - 1 - last.getSecond().getScanIndex();
            // if this holds, than anchor has already been found to be valid
            // by loop above
            if (deltaRow == 0 && deltaCol == 0) {
                // if present, set names
                last.getFirst().setName("END");
                last.getSecond().setName("END");
                valid.add(last);
                this.log.debug("Renaming last anchor to END");

            } else if (deltaRow >= getMinScansBetweenAnchors()
                    && deltaCol >= getMinScansBetweenAnchors()) {
                // if last is smaller than rows-1 and columns-1, retain
                // and add last anchor
                s1 = new RetentionInfo();
                s2 = new RetentionInfo();
                s1.setName("END");
                s2.setName("END");
                s1.setScanIndex(rows - 1);
                s2.setScanIndex(cols - 1);
                valid.add(new Tuple2D<IAnchor, IAnchor>(s1, s2));
                this.log.debug("Retaining last anchor and adding END anchor");

            } else if (deltaRow < getMinScansBetweenAnchors()
                    || deltaCol < getMinScansBetweenAnchors()) {
                // one of last's components does not obey monotonicity
                Tuple2D<IAnchor, IAnchor> rm = valid.remove(valid.size() - 1);
                this.log.debug("Removed: {} {} {}", new Object[]{rm,
                    rm.getFirst().getScanIndex(),
                    rm.getSecond().getScanIndex()});
                s1 = new RetentionInfo();
                s2 = new RetentionInfo();
                s1.setName("END");
                s2.setName("END");
                s1.setScanIndex(rows - 1);
                s2.setScanIndex(cols - 1);
                valid.add(new Tuple2D<IAnchor, IAnchor>(s1, s2));
            } else {
                throw new ConstraintViolationException("Unknown case: "
                        + deltaRow + " " + deltaCol);
            }
        }
        this.log.debug("rows: {}, cols: {}", rows, cols);

        for (Tuple2D<IAnchor, IAnchor> tuple2d : valid) {
            this.log.debug("{}: {} <-> {}", new Object[]{
                tuple2d.getFirst().getName(),
                tuple2d.getFirst().getScanIndex(),
                tuple2d.getSecond().getScanIndex()});
        }
        return valid;
    }
}
