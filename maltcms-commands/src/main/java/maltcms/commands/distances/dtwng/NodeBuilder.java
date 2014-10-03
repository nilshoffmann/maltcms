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
package maltcms.commands.distances.dtwng;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Abstract NodeBuilder class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public abstract class NodeBuilder {

    private List<double[]> nodes = Collections.emptyList();

    /**
     * <p>Setter for the field <code>nodes</code>.</p>
     *
     * @param nodes a {@link java.util.List} object.
     */
    public void setNodes(List<double[]> nodes) {
        this.nodes = nodes;
    }

    /**
     * <p>Getter for the field <code>nodes</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<double[]> getNodes() {
        return nodes;
    }

    /**
     * <p>eval.</p>
     *
     * @param points a {@link java.util.List} object.
     * @param polyOrder a int.
     * @return a {@link java.util.List} object.
     */
    public abstract List<Point> eval(List<double[]> points, int polyOrder);

    /**
     * <p>getNodesByDimension.</p>
     *
     * @param i a int.
     * @param dim an array of double.
     * @return an array of double.
     */
    public double[] getNodesByDimension(int i, double[] dim) {
        double[] dimt = dim == null ? new double[nodes.size()] : dim;
        for (double[] d : nodes) {
            dimt[i] = d[i];
        }
        return dimt;
    }

    /**
     * <p>getPointList.</p>
     *
     * @param points a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    public List<Point> getPointList(List<double[]> points) {
        List<Point> l = new ArrayList<>();
        for (double[] d : points) {
            l.add(new Point((int) d[0], (int) d[1]));
        }
        return l;
    }
}
