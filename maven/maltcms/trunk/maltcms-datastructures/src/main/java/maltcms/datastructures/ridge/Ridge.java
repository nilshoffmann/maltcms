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
package maltcms.datastructures.ridge;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import cross.datastructures.tuple.Tuple2D;

/**
 * @author Nils Hoffmann
 *
 *
 */
public class Ridge implements Comparable<Ridge> {

    private List<Tuple2D<Point2D, Double>> ridgePoints;

    public List<Tuple2D<Point2D, Double>> getRidgePoints() {
        return ridgePoints;
    }
    private GeneralPath gp = new GeneralPath();
    private double ridgePenalty = 0.0d;
    private final int globalScanIndex;
    private int maximumIndex = 0;
    private String classLabel = "NN";

    public String getClassLabel() {
        return classLabel;
    }

    public void setClassLabel(String classLabel) {
        this.classLabel = classLabel;
    }

    public Ridge(Point2D seed, double seedVal) {
        this.globalScanIndex = (int) seed.getX();
        this.ridgePoints = new LinkedList<Tuple2D<Point2D, Double>>();
        this.ridgePoints.add(new Tuple2D<Point2D, Double>(seed, seedVal));
        gp.moveTo(seed.getX(), seed.getY());
    }

    public int getGlobalScanIndex() {
        return this.globalScanIndex;
    }

    public int getIndexOfMaximum() {
        return this.maximumIndex;
    }

    public int getSize() {
        return this.ridgePoints.size();
    }

    @Override
    public String toString() {
        return "first: " + ridgePoints.get(0) + " last: "
                + ridgePoints.get(ridgePoints.size() - 1) + ", length: "
                + ridgePoints.size() + " Maximum value at scaleIndex: "
                + getIndexOfMaximum() + " with value "
                + ridgePoints.get(getIndexOfMaximum()).getSecond();
    }

    public void draw(Graphics2D g) {
        Color c = g.getColor();
        g.setColor(Color.RED);
        g.draw(this.gp);
        g.setColor(c);
    }
    private static List<IRidgeCost> ridgeCosts = null;

    public static List<IRidgeCost> getAvailableRidgeCostClasses() {
        if (ridgeCosts == null) {
            ServiceLoader<IRidgeCost> sl = ServiceLoader.load(IRidgeCost.class);
            List<IRidgeCost> l = new LinkedList<IRidgeCost>();
            Iterator<IRidgeCost> iter = sl.iterator();
            while (iter.hasNext()) {
                l.add(iter.next());
            }
            Collections.sort(l, new Comparator<IRidgeCost>() {
                @Override
                public int compare(IRidgeCost arg0, IRidgeCost arg1) {
                    String s1 = arg0.getClass().getName();
                    String s2 = arg1.getClass().getName();
                    return s1.compareTo(s2);
                }
            });
            System.out.println("Using the following ridge cost functions: " + l.toString());
            ridgeCosts = l;
        }
        return ridgeCosts;
    }

    public double getRidgeCost() {
        return getRidgeCosts().get(0).getSecond();
    }

    public List<Tuple2D<String, Double>> getRidgeCosts() {
        List<IRidgeCost> l = getAvailableRidgeCostClasses();
        List<Tuple2D<String, Double>> d = new ArrayList<Tuple2D<String, Double>>(l.size());
        for (IRidgeCost irc : l) {
            d.add(new Tuple2D<String, Double>(irc.getClass().getName(), irc.getCost(this)));
        }
        return d;
    }

    public boolean addPoint(int scaleDiff, int scaleIdx, double[] nextScale) {
        if (scaleIdx > this.ridgePoints.size()) {
            return false;
        }
        Tuple2D<Point2D, Double> prev = this.ridgePoints
                .get(this.ridgePoints.size() - 1);
        int x = (int) prev.getFirst().getX();
        double xval = prev.getSecond().doubleValue();
        int minX = Math.max(0, x - scaleDiff);
        int maxX = Math.min(nextScale.length - 1, x + scaleDiff);
        int maximumR = -1;
        int maximumL = -1;
        for (int i = x; i <= maxX; i++) {
            if (nextScale[i] >= xval) {
                maximumR = i;
            }
        }
        for (int j = x; j >= minX; j--) {
            if (nextScale[j] >= xval) {
                maximumL = j;
            }
        }
        if (maximumR == -1 && maximumL == -1) {
            // System.out.println("Could not extend ridge beyond: "
            // + prev.getFirst());
            return false;
            // now come the single candidates
        } else if (maximumR == -1 && maximumL > -1) {

            addPoint(scaleIdx, nextScale, maximumL);
            return true;
        } else if (maximumR > -1 && maximumL == -1) {
            addPoint(scaleIdx, nextScale, maximumR);
            return true;
        } else {
            // maximum directly above
            if (maximumR == maximumL) {
                addPoint(scaleIdx, nextScale, maximumR);
                return true;
            } else {
                if (Math.abs(x - maximumR) < Math.abs(x - maximumL)) {
                    addPoint(scaleIdx, nextScale, maximumR);
                    return true;
                } else if (Math.abs(x - maximumR) > Math.abs(x - maximumL)) {
                    addPoint(scaleIdx, nextScale, maximumL);
                    return true;
                } else {
                    // two candidates at same distance, choose larger one
                    if (nextScale[maximumR] < nextScale[maximumL]) {
                        addPoint(scaleIdx, nextScale, maximumL);
                        return true;
                    } else if (nextScale[maximumR] > nextScale[maximumL]) {
                        addPoint(scaleIdx, nextScale, maximumR);
                        return true;
                    } else {// draw

                        System.out
                                .println("Potential problem, detected a draw in value and positions!");
                        // Random r = new Random(System.nanoTime());
                        // float f = r.nextFloat();
                        // if(f>0.5) {
                        // addPoint(scaleIdx, nextScale, maximumR);
                        // return false;
                        // }else{
                        // addPoint(scaleIdx, nextScale, maximumL);
                        // return false;
                        // }
                        return false;
                    }
                }

                //					
            }

        }
    }

    /**
     * @param scaleIdx
     * @param nextScale
     * @param maximum
     */
    private void addPoint(int scaleIdx, double[] nextScale, int maximum) {
        // ridgePenalty += Math.pow(ridgePoints.get(scaleIdx - 1).getFirst()
        // .getX()
        // - maximum, 2)
        // / ((double) scaleIdx * scaleIdx);

        // ridgePenalty +=
        // Math.pow(ridgePoints.get(ridgePoints.size()-1).getFirst().getX()
        // - maximum, 2)
        // / ((double) scaleIdx) +
        // 0.5*(Math.pow(ridgePoints.get(ridgePoints.size()-1).getFirst().getX()
        // - maximum, 2));

        ridgePoints.add(new Tuple2D<Point2D, Double>(new Point2D.Double(
                maximum, scaleIdx), nextScale[maximum]));
        gp.lineTo(maximum, scaleIdx);
        double oldMax = ridgePoints.get(getIndexOfMaximum()).getSecond();
        if (nextScale[maximum] > oldMax) {
            this.maximumIndex = scaleIdx;
        }
    }

    @Override
    public int compareTo(Ridge o) {
        double response = getRidgePoints().get(0).getSecond();
        double oresponse = o.getRidgePoints().get(0).getSecond();
        return Double.compare(response, oresponse);
    }
}
