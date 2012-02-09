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
package maltcms.commands.distances.dtwng;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class NodeBuilder {

    private List<double[]> nodes = Collections.emptyList();

    public void setNodes(List<double[]> nodes) {
        this.nodes = nodes;
    }

    public List<double[]> getNodes() {
        return nodes;
    }

    public abstract List<Point> eval(List<double[]> points, int polyOrder);

    public double[] getNodesByDimension(int i, double[] dim) {
        double[] dimt = dim == null ? new double[nodes.size()] : dim;
        for (double[] d : nodes) {
            dimt[i] = d[i];
        }
        return dimt;
    }

    public List<Point> getPointList(List<double[]> points) {
        List<Point> l = new ArrayList<Point>();
        for (double[] d : points) {
            l.add(new Point((int) d[0], (int) d[1]));
        }
        return l;
    }
}
