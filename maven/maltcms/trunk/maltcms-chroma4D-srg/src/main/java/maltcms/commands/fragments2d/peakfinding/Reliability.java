/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.commands.fragments2d.peakfinding;

public class Reliability {

    double min = 0, max = 0, rel = 0;

    public Reliability(double rel) {
        this.min = rel;
        this.max = rel;
        this.rel = rel;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getReliability() {
        return rel;
    }

    public void addRel(double newRel) {
        if (newRel < this.min) {
            this.min = newRel;
        }
        if (newRel > this.max) {
            this.max = newRel;
        }
        this.rel *= newRel;
    }
}
