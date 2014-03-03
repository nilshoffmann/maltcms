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
package maltcms.commands.fragments2d.peakfinding;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.Peak2D;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;

@Slf4j
@Data
public class QualityControl {

    public List<Reliability> calc(List<Peak2D> pl1, List<Peak2D> pl2,
        List<List<Point>> bidibesthits) {
        Map<Integer, Integer> m = new HashMap<Integer, Integer>();
        for (List<Point> l : bidibesthits) {
            m.put(l.get(0).y, l.get(1).y);
        }
        List<Entry<Integer, Integer>> map = new ArrayList<Entry<Integer, Integer>>(
            m.entrySet());
        return calcV(pl1, pl2, map);
    }

    private List<Reliability> calcV(List<Peak2D> pl1, List<Peak2D> pl2,
        List<Entry<Integer, Integer>> map) {
        List<Double> values = new ArrayList<Double>();

        Peak2D p1, p2;
        int rt1dsi, rt2dsi;
        int rt1dsiSum = 0, rt2dsiSum = 0;
        int c = 0;
        for (Entry<Integer, Integer> m : map) {
            if (m.getKey() != -1 && m.getValue() != -1) {
                p1 = pl1.get(m.getKey());
                p2 = pl2.get(m.getValue());
                rt1dsi = Math.abs(p1.getFirstScanIndex()
                    - p2.getFirstScanIndex());
                rt2dsi = Math.abs(p1.getSecondScanIndex()
                    - p2.getSecondScanIndex());
                rt1dsiSum += rt1dsi;
                rt2dsiSum += rt2dsi;
                c++;
            }
        }
        double meanRT1dsi = (double) rt1dsiSum / (double) c;
        double meanRT2dsi = (double) rt2dsiSum / (double) c;
        double rt1dsivarSum = 0.0d, rt2dsivarSum = 0.0d;
        for (Entry<Integer, Integer> m : map) {
            if (m.getKey() != -1 && m.getValue() != -1) {
                p1 = pl1.get(m.getKey());
                p2 = pl2.get(m.getValue());
                rt1dsi = Math.abs(p1.getFirstScanIndex()
                    - p2.getFirstScanIndex());
                rt2dsi = Math.abs(p1.getSecondScanIndex()
                    - p2.getSecondScanIndex());

                rt1dsivarSum += Math.pow(meanRT1dsi - rt1dsi, 2.0d);
                rt2dsivarSum += Math.pow(meanRT2dsi - rt2dsi, 2.0d);
            }
        }

        double varRT1dsi = (double) rt1dsivarSum / (double) (c - 1);
        double varRT2dsi = (double) rt2dsivarSum / (double) (c - 1);
        if (varRT1dsi == 0.0d) {
            varRT1dsi += 1;
        }
        if (varRT2dsi == 0.0d) {
            varRT2dsi += 1;
        }

        // System.out.println("RT1: mean= " + meanRT1dsi + ", std= "
        // + Math.sqrt(varRT1dsi));
        NormalDistribution ndrt1 = new NormalDistributionImpl(0, Math.sqrt(
            varRT1dsi));
        // System.out.println("RT2: mean= " + meanRT2dsi + ", std= "
        // + Math.sqrt(varRT2dsi));
        NormalDistribution ndrt2 = new NormalDistributionImpl(0, Math.sqrt(
            varRT2dsi));

        double rt1p, rt2p;
        double drt1, drt2;
        double max = 0.0d;
        for (Entry<Integer, Integer> m : map) {
            if (m.getKey() != -1 && m.getValue() != -1) {
                p1 = pl1.get(m.getKey());
                p2 = pl2.get(m.getValue());
                drt1 = Math.abs(meanRT1dsi
                    - Math.abs(p1.getFirstScanIndex()
                        - p2.getFirstScanIndex()));
                drt2 = Math.abs(meanRT2dsi
                    - Math.abs(p1.getSecondScanIndex()
                        - p2.getSecondScanIndex()));
                try {
                    rt1p = 1 - ndrt1.cumulativeProbability(-drt1, drt1);
                    rt2p = 1 - ndrt2.cumulativeProbability(-drt2, drt2);
                    // System.out.println(drt1 + " - " + drt2);
                    // System.out.println(rt1p + " * " + rt2p + " = " + rt1p
                    // * rt2p);
                    if (rt1p * rt2p > max) {
                        max = rt1p * rt2p;
                    }
                    values.add(rt1p * rt2p);
                } catch (MathException e) {
                    e.printStackTrace();
                }
            } else {
                values.add(0.0d);
            }
        }

        List<Reliability> ret = new ArrayList<Reliability>();
        for (Double d : values) {
            ret.add(new Reliability(d / max));
        }

        return ret;
    }
    // public static void main(String[] args) {
    // Peak2D p;
    // List<Peak2D> pl1 = new ArrayList<Peak2D>();
    // p = new Peak2D();
    // p.setFirstScanIndex(10);
    // p.setSecondScanIndex(10);
    // pl1.add(p);
    // p = new Peak2D();
    // p.setFirstScanIndex(20);
    // p.setSecondScanIndex(20);
    // pl1.add(p);
    // p = new Peak2D();
    // p.setFirstScanIndex(30);
    // p.setSecondScanIndex(30);
    // pl1.add(p);
    // p = new Peak2D();
    // p.setFirstScanIndex(40);
    // p.setSecondScanIndex(40);
    // pl1.add(p);
    // p = new Peak2D();
    // p.setFirstScanIndex(50);
    // p.setSecondScanIndex(50);
    // pl1.add(p);
    // List<Peak2D> pl2 = new ArrayList<Peak2D>();
    // p = new Peak2D();
    // p.setFirstScanIndex(11);
    // p.setSecondScanIndex(12);
    // pl2.add(p);
    // p = new Peak2D();
    // p.setFirstScanIndex(21);
    // p.setSecondScanIndex(22);
    // pl2.add(p);
    // p = new Peak2D();
    // p.setFirstScanIndex(31);
    // p.setSecondScanIndex(32);
    // pl2.add(p);
    // p = new Peak2D();
    // p.setFirstScanIndex(41);
    // p.setSecondScanIndex(42);
    // pl2.add(p);
    // p = new Peak2D();
    // p.setFirstScanIndex(51);
    // p.setSecondScanIndex(52);
    // pl2.add(p);
    // Map<Integer, Integer> m = new HashMap<Integer, Integer>();
    // m.put(0, 0);
    // m.put(1, 1);
    // m.put(2, 2);
    // m.put(3, 3);
    // m.put(4, 4);
    // List<Entry<Integer, Integer>> map = new ArrayList<Entry<Integer,
    // Integer>>(
    // m.entrySet());
    //
    // for (Reliability d : new QualityControl().calcV(pl1, pl2, map)) {
    // System.out.println("" + d.getReliability());
    // }
    // }
}
