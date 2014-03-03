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

import cross.datastructures.tuple.Tuple2D;
import java.awt.Point;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ContinuityArgmaxNodeBuilder extends NodeBuilder {

    // private final double[] penaltyLookupTable;
    private final double scale = 1.0;
    private final int maxlevel;

    public ContinuityArgmaxNodeBuilder(int levels) {
        this.maxlevel = levels;
        // // this.scale = scale;
        // // penaltyLookupTable = new double[(int) (1.0 * scale) + 1];
        // // for (int i = 0; i < penaltyLookupTable.length; i++) {
        // // penaltyLookupTable[i] = Math.exp(-Math.pow(
        // // (((double) i) / scale), 2.0d) / 2.0d);
        // // System.out.println("p(" + (((double) i) / scale) + ")="
        // // + penaltyLookupTable[i]);
        // // }
        //
    }

    private int map(double v, double min, double max, double scale) {
        return (int) ((v / (max - min)) * scale);
    }

    @Override
    public List<Point> eval(List<double[]> points, int polyOrder) {
        Tuple2D<double[], double[]> minMax = getMinMax(points);
        List<double[]> anchors = calcArgmax(points, minMax.getFirst(), minMax.getSecond());
        System.out.println("Found " + anchors.size() + " anchors!");
        // System.out.println("Anchors: ");
        // for (double[] d : anchors) {
        // System.out.println(Arrays.toString(d));
        // }
        return getPointList(anchors);
        // the following rules apply:
        // -the first element and the last are always used as anchors
        //
        // -let a be the previous valid anchor and a'' the next valid
        // anchor,
        // then, either x(a) < x(b) < x(c) or y(a) < y(b) < y(c) must hold
        // to guarantee monotonicity. a < b < c define an open interval
        // (a,c):={b\inNXN| a<b<c}
        // -Additionally, (b_{1},...,b_{k}) = argmax
        // (z(a),...,z(b),...,z(c))
        // must hold, such
        // that all b have the maximum value within the open interval
        // defined by a and c, (a, c), such that
        // a and c are not members of the interval.
        // how many b_{i} will we usually find? how is this connected to the
        // interval size?
        //
        // Using a polynomial of order n requires n+1 points within a
        // partition to
        // uniquely determine the polynomial.
        // level 1: select start and end
        // find arg max between start and end -> (start,end), preferrably
        // close to the midpoint -> penalty (1/exp((x1-x2)^{2}/2))*z(a)
        // then subdivide into (start,s1), and (s1,end)
    }

    private Tuple2D<double[], double[]> getMinMax(List<double[]> points) {
        double[] min = new double[points.get(0).length];
        double[] max = new double[points.get(0).length];
        Arrays.fill(min, Double.POSITIVE_INFINITY);
        Arrays.fill(max, Double.NEGATIVE_INFINITY);
        for (double[] d : points) {
            for (int i = 0; i < min.length; i++) {
                min[i] = Math.min(d[i], min[i]);
                max[i] = Math.max(d[i], max[i]);
            }
        }
        return new Tuple2D<double[], double[]>(min, max);
    }

    private List<Integer> getArgmax(final List<double[]> l, final double[] a, final double[] c, final double[] min, final double[] max) {
        List<Integer> maxima = new LinkedList<Integer>();
        int maxIdx = 0;
        double maxscore = Double.NEGATIVE_INFINITY;
        int i = 0;
        for (double[] d : l) {
            if (i > 0) {
                double amscore = getScore(d, a, c, min, max, this.scale);
                if (amscore > maxscore) {
                    maxIdx = i;
                    maxscore = amscore;
                }
            } else {
                maxIdx = i;
                maxscore = getScore(d, a, c, min, max, this.scale);
            }
            i++;
        }
        maxima.add(Integer.valueOf(maxIdx));
        System.out.println("Found " + maxima.size() + " maxima between " + Arrays.toString(a) + " and " + Arrays.toString(c));
        return maxima;
    }

    private List<double[]> calcArgmax(List<double[]> points, final double[] min, final double[] max) {
        final List<double[]> rl = new LinkedList<double[]>();
        int level = 0;
        argmaxRecursion(points, min, max, rl, level);
        rl.add(0, points.get(0));
        rl.add(points.get(points.size() - 1));
        return rl;
    }

    private void argmaxRecursion(List<double[]> lpart, double[] min, double[] max, List<double[]> res, int level) {
        if (lpart.size() <= 2 || level == maxlevel) {
            return;
        }
        final double[] a = lpart.remove(0);
        // get end
        final double[] c = lpart.remove(lpart.size() - 1);
        // get dividing element/pivot index
        final List<Integer> amax = getArgmax(lpart, a, c, min, max);
        // retrieve pivot element
        final int nodeIndex = amax.get(0);
        final double[] b = lpart.get(amax.get(0));
        final LinkedList<double[]> rl = new LinkedList<double[]>();
        // add left boundary
        // rl.add(a);
        // recurse into left branch
        System.out.println("Left recursion");
        if (nodeIndex > 0) {
            argmaxRecursion(lpart.subList(0, nodeIndex), min, max, rl, level + 1);
        }
        rl.add(b);
        // recurse into right branch
        System.out.println("Right recursion");
        if (nodeIndex < lpart.size() - 1) {
            argmaxRecursion(lpart.subList(nodeIndex, lpart.size() - 1), min, max, rl, level + 1);
        } // rl.add(c);
        res.addAll(rl);
    }

    private double getScore(final double[] v, final double[] a, final double[] c, final double[] min, final double[] max, final double scale) {
        // double d = 0;
        // for (int i = 0; i < v.length; i++) {
        // d += ((penaltyLookupTable[map(Math.abs(v[i] - a[i]), min[i],
        // max[i], 1.0)] * (penaltyLookupTable[map(Math.abs(v[i]
        // - c[i]), min[i], max[i], scale)])));
        // }
        // return d;
        return v[v.length - 1];
    }
}
