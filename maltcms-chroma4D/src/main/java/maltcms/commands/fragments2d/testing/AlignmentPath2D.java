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
package maltcms.commands.fragments2d.testing;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 * @author Mathias Wilhelm
 */
@Slf4j
@Data
public class AlignmentPath2D {

    private Map<Integer, List<Integer>> LHStoRHShorizontalMap = new HashMap<Integer, List<Integer>>(),
        LHStoRHSverticalMap = new HashMap<Integer, List<Integer>>();
    private Map<Integer, List<Integer>> RHStoLHShorizontalMap = new HashMap<Integer, List<Integer>>(),
        RHStoLHSverticalMap = new HashMap<Integer, List<Integer>>();
    private List<Point> horizontalList, verticalList;

    private AlignmentPath2D(final List<Point> horizontal,
        final List<Point> vertical) {
        this.horizontalList = horizontal;
        this.verticalList = vertical;

        for (final Point p : horizontal) {
            if (!this.LHStoRHShorizontalMap.containsKey(p.x)) {
                this.LHStoRHShorizontalMap.put(p.x, new ArrayList<Integer>());
            }
            this.LHStoRHShorizontalMap.get(p.x).add(p.y);

            if (!this.RHStoLHShorizontalMap.containsKey(p.y)) {
                this.RHStoLHShorizontalMap.put(p.y, new ArrayList<Integer>());
            }
            this.RHStoLHShorizontalMap.get(p.y).add(p.x);
        }

        for (final Point p : vertical) {
            if (!this.LHStoRHSverticalMap.containsKey(p.x)) {
                this.LHStoRHSverticalMap.put(p.x, new ArrayList<Integer>());
            }
            this.LHStoRHSverticalMap.get(p.x).add(p.y);

            if (!this.RHStoLHSverticalMap.containsKey(p.y)) {
                this.RHStoLHSverticalMap.put(p.y, new ArrayList<Integer>());
            }
            this.RHStoLHSverticalMap.get(p.y).add(p.x);
        }

    }

    /**
     *
     * @param lhsHorizontal
     * @return
     */
    public List<Integer> getRHSPointHorizontal(final Integer lhsHorizontal) {
        return this.LHStoRHShorizontalMap.get(lhsHorizontal);
    }

    /**
     *
     * @param rhsHorizontal
     * @return
     */
    public List<Integer> getLHSPointHorizontal(final Integer rhsHorizontal) {
        return this.RHStoLHShorizontalMap.get(rhsHorizontal);
    }

    /**
     *
     * @param lhsVertical
     * @return
     */
    public List<Integer> getRHSPointVerticals(final Integer lhsVertical) {
        return this.LHStoRHSverticalMap.get(lhsVertical);
    }

    /**
     *
     * @param rhsVertical
     * @return
     */
    public List<Integer> getLHSPointVertical(final Integer rhsVertical) {
        return this.RHStoLHSverticalMap.get(rhsVertical);
    }

    /**
     *
     * @return
     */
    public List<Point> getHorizontalList() {
        return this.horizontalList;
    }
}
