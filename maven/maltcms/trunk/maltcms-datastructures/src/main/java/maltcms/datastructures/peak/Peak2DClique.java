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
package maltcms.datastructures.peak;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cross.datastructures.fragments.IFileFragment;

public class Peak2DClique {

    private Map<IFileFragment, Peak2D> peaks;
    private Map<String, Double> ratios;
    private String id;

    public Peak2DClique(String id) {
        this.id = id;
        this.peaks = new HashMap<IFileFragment, Peak2D>();
        this.ratios = new HashMap<String, Double>();
    }

    public Peak2DClique(String id, Collection<IFileFragment> f,
            List<Peak2D> peaks) {
        this(id);
        Iterator<IFileFragment> i1 = f.iterator();
        Iterator<Peak2D> i2 = peaks.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            this.peaks.put(i1.next(), i2.next());
        }
    }

    public void add(IFileFragment ff, Peak2D peak) {
        this.peaks.put(ff, peak);
    }

    public Peak2D get(IFileFragment ff) {
        return peaks.get(ff);
    }

    public List<Peak2D> getAll() {
        return new ArrayList<Peak2D>(peaks.values());
        // final List<Peak2D> ret = new ArrayList<Peak2D>();
        // for (IFileFragment ff : f) {
        // ret.add(peaks.get(ff));
        // }
        // return ret;
    }

    public void addRatio(String class1, String class2, Double ratio) {
        this.ratios.put(class1 + "-" + class2, ratio);
    }

    public double getRatio(String class1, String class2) {
        if (this.ratios.containsKey(class1 + "-" + class2)) {
            return this.ratios.get(class1 + "-" + class2);
        }
        return Double.NEGATIVE_INFINITY;
    }

    public String getID() {
        return this.id;
    }
}