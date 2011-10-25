/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: AlignmentPath2D.java 129 2010-06-25 11:57:02Z nilshoffmann $
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
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
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

    public List<Integer> getRHSPointHorizontal(final Integer lhsHorizontal) {
        return this.LHStoRHShorizontalMap.get(lhsHorizontal);
    }

    public List<Integer> getLHSPointHorizontal(final Integer rhsHorizontal) {
        return this.RHStoLHShorizontalMap.get(rhsHorizontal);
    }

    public List<Integer> getRHSPointVerticals(final Integer lhsVertical) {
        return this.LHStoRHSverticalMap.get(lhsVertical);
    }

    public List<Integer> getLHSPointVertical(final Integer rhsVertical) {
        return this.RHStoLHSverticalMap.get(rhsVertical);
    }

    public List<Point> getHorizontalList() {
        return this.horizontalList;
    }
}
