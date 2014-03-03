/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.datastructures.constraint;

import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.alignment.AnchorPairSet;

/**
 *
 * Factory for pairwise DTW alignment layout constraints.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class ConstraintFactory {

    private static final ConstraintFactory cf = new ConstraintFactory();

    public static ConstraintFactory getInstance() {
        return ConstraintFactory.cf;
    }

    public Area calculateLayout(final int rows, final int cols,
        final int neighborhood1, final AnchorPairSet aps,
        final double band, final int rowoverlap, final int coloverlap) {
        final List<Tuple2D<Integer, Integer>> ris = aps.getCorrespondingScans();
        // by convention, the first contained pair should be (0,0)
        // the last contained pair should be rows-1, cols-1
        final Tuple2D<Integer, Integer> tbegin = ris.remove(0);
        int x = tbegin.getFirst();
        int y = tbegin.getSecond();
        EvalTools.eqI(x, 0, ConstraintFactory.class);// check our assertion
        EvalTools.eqI(y, 0, ConstraintFactory.class);// check our assertion

        int cnt = 0;// no anchors processed so far
        // int row = 0;//row counter
        final int roverlap = rowoverlap;
        final int coverlap = coloverlap;
        int j0 = 0, i0 = 0, j1 = 0, i1 = 0;// bounds of partitions, i0<=i1,
        // j0<=j1
        final int npartitions = ris.size();
        final Area bounds = new Area();
        final ArrayList<Rectangle> partitions = new ArrayList<Rectangle>();
        final ArrayList<Rectangle> distinctRects = new ArrayList<Rectangle>();
        for (final Tuple2D<Integer, Integer> t : ris) {// iterate over anchors
            j1 = t.getSecond();// begin of next ri
            i1 = t.getFirst();
            j0 = x;// init to starting indices
            i0 = y;
            if (cnt == npartitions - 1) {// ensure that last partition ends at
                // bounds
                j1 = cols;
                i1 = rows;
            }
            // add partition rectangle
            final Rectangle r = new Rectangle(j0, i0, j1 - j0 + coverlap, i1
                - i0 + roverlap);
            // add local band constraint
            if ((band > 0.0d) && (band < 1.0d)) {
                final Area bandArea = createBandConstraint(j0, i0, r.height,
                    r.width, band);
                final Area b = new Area(r);
                b.intersect(bandArea);
                bounds.add(b);
            } else {
                bounds.add(new Area(r));
            }
            final Area dr = new Area(r);
            dr.intersect(bounds);
            distinctRects.add(dr.getBounds());
            x = j1;
            y = i1;
            final int ax = x - neighborhood1;
            final int ay = y - neighborhood1;
            // add anchor rectangle from x to x+1, y to y+1
            if (cnt < npartitions - 1) {
                final Rectangle anchor = new Rectangle(ax, ay,
                    (neighborhood1 * 2) + 1, (neighborhood1 * 2) + 1);
                bounds.add(new Area(anchor));
                // final Area dr2 = new Area(r);
                final Area dr2 = new Area(anchor);
                dr2.intersect(bounds);
                distinctRects.add(dr2.getBounds());
                // System.out.println("Adding anchor region: "+anchor.toString())
                // ;
                log.debug(
                    "Anchor # " + cnt + " at " + x + "," + y);
                // partitions.add(anchor);
            }
            x = j1 + 1;
            y = i1 + 1;
            cnt++;

            partitions.add(r);

        }
        for (final Rectangle r : distinctRects) {
            log.debug(r.toString());
        }
        return bounds;
    }

    public Area createBandConstraint(final int x, final int y, final int rows,
        final int cols, final double r) {
        final double maxdev = Math.max(1, Math.ceil(Math.max(rows, cols) * r));
        final double ascent = (double) cols / (double) rows;
        log.info(
            "Using band constraint with width {}, |REF| = {}, |QUERY| = {}",
            new Object[]{maxdev, rows, cols});
        log.info(
            "Ascent of diagonal is {}", ascent);
        final GeneralPath band = new GeneralPath();
        // create the band
        band.moveTo(x, y);
        band.lineTo(x + (int) maxdev, y);
        band.lineTo(x + cols, y + rows - (int) maxdev);
        band.lineTo(x + cols, y + rows);
        band.lineTo(x + cols - (int) maxdev, y + rows);
        band.lineTo(x, y + (int) maxdev);
        band.closePath();
        return new Area(band);
    }
}
