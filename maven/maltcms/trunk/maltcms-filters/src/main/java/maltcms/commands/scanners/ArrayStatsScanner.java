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
package maltcms.commands.scanners;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import cross.Factory;
import cross.annotations.Configurable;
import cross.commands.ICommand;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import lombok.extern.slf4j.Slf4j;

/**
 * Scans a number of arrays for statistics, such as mean and variance, storing
 * results for each array in a HashMap indexed by Elements of
 *
 * @link{cross.Vars} .
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Slf4j
public class ArrayStatsScanner implements ICommand<Array[], StatsMap[]> {

    private StatsMap gsm = null;
    private IFileFragment ff = Factory.getInstance().getFileFragmentFactory().
            create("default_stats.cdf");
    private IVariableFragment[] vfs = null;
    @Configurable(name = "ignorePositiveInfinity")
    private boolean ignorePositiveInfinity = true;
    @Configurable(name = "ignoreNegativeInfinity")
    private boolean ignoreNegativeInfinity = true;

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.ucar.ma2.Scanner#scan(java.lang.Object)
     */
    @Override
    public StatsMap[] apply(final Array[] t) {
        log.debug("Running ArrayStatsScanner on {} arrays", t.length);
        int i = 0;
        final StatsMap[] hashes = new StatsMap[t.length];
        this.gsm = new StatsMap(this.ff);

        long globalcnt = 0;
        double globalmean = 0.0d;
        double globalmin = Double.POSITIVE_INFINITY;
        double globalmax = Double.NEGATIVE_INFINITY;
        double globalvar = 0.0d;
        double globalskew = 0.0d;
        for (final Array arr : t) {
            long cnt = 0;
            double min = 0.0d, max = 0.0d;
            double mean = 0.0d;
            IndexIterator iter = arr.getIndexIteratorFast();

            min = Double.POSITIVE_INFINITY;
            max = Double.NEGATIVE_INFINITY;
            while (iter.hasNext()) {// first loop for min, max, mean
                final double d = iter.getDoubleNext();
                if (this.ignorePositiveInfinity || this.ignoreNegativeInfinity) {
                    if ((d < Double.POSITIVE_INFINITY)
                            && (d > Double.NEGATIVE_INFINITY)) {
                        max = Math.max(max, d);
                        min = Math.min(min, d);
                        globalmin = Math.min(globalmin, min);
                        globalmax = Math.max(globalmax, max);
                        globalmean += d;
                        globalcnt++;
                        mean += d;
                        cnt++;
                    }
                } else {
                    max = Math.max(max, d);
                    min = Math.min(min, d);
                    globalmin = Math.min(globalmin, min);
                    globalmax = Math.max(globalmax, max);
                    globalmean += d;
                    globalcnt++;
                    mean += d;
                    cnt++;
                }
            }
            mean = mean / (cnt);
            double variance = 0.0d;
            double skew = 0.0d;
            iter = arr.getIndexIteratorFast();
            while (iter.hasNext()) {// second loop for variance
                final double d = (iter.getDoubleNext());
                if (this.ignorePositiveInfinity || this.ignoreNegativeInfinity) {// FIXME
                    // this
                    // is
                    // weird
                    // !
                    // !
                    // !
                    if ((d < Double.POSITIVE_INFINITY)
                            && (d > Double.NEGATIVE_INFINITY)) {
                        variance += Math.pow((d - mean), 2.0d);
                        skew += Math.pow((d - mean), 3.0d);
                        // globalvar += Math.pow((d - globalmean), 2.0d);
                    }
                } else {
                    variance += Math.pow((d - mean), 2.0d);
                    skew += Math.pow((d - mean), 3.0d);
                    // globalvar += Math.pow((d - globalmean), 2.0d);
                }
            }
            variance = variance / (cnt - 1);
            skew = skew / (cnt - 1);

            final StatsMap map = new StatsMap(this.ff);
            map.put(Vars.Max.toString(), max);
            map.put(Vars.Min.toString(), min);
            map.put(Vars.Mean.toString(), mean);
            map.put(Vars.Variance.toString(), variance);
            map.put(Vars.Skewness.toString(), skew);
            // Iterator<String> it = map.keySet().iterator();
            // while (it.hasNext()) {
            // String s = it.next();
            // //Logging.getInstance().logger.debug(v + ": " + map.get(v));
            // }
            hashes[i] = map;
            i++;
            // Logger.getAnonymousLogger().log(Level.WARNING,"Max: "+max+" Min:
            // "+min+" Mean: "+mean);
        }
        globalmean = globalmean / globalcnt;
        for (final Array arr : t) {
            IndexIterator iter = arr.getIndexIteratorFast();
            iter = arr.getIndexIteratorFast();
            while (iter.hasNext()) {// second loop for variance
                final double d = (iter.getDoubleNext());
                if (this.ignorePositiveInfinity || this.ignoreNegativeInfinity) {// FIXME
                    // this
                    // is
                    // weird
                    // !
                    // !
                    // !
                    if ((d < Double.POSITIVE_INFINITY)
                            && (d > Double.NEGATIVE_INFINITY)) {
                        globalvar += Math.pow((d - globalmean), 2.0d);
                        globalskew += Math.pow((d - globalmean), 3.0d);
                        // globalvar += Math.pow((d - globalmean), 2.0d);
                    }
                } else {
                    globalvar += Math.pow((d - globalmean), 2.0d);
                    globalskew += Math.pow((d - globalmean), 3.0d);
                    // globalvar += Math.pow((d - globalmean), 2.0d);
                }
            }
        }
        globalvar = globalvar / (globalcnt - 1);
        globalskew = globalskew / (globalcnt - 1);
        this.gsm.put(Vars.Max.toString(), globalmax);
        this.gsm.put(Vars.Min.toString(), globalmin);
        this.gsm.put(Vars.Mean.toString(), globalmean);
        this.gsm.put(Vars.Variance.toString(), globalvar);
        this.gsm.put(Vars.Skewness.toString(), globalskew);
        // System.out.println("Global stats:");
        // System.out.println("Min: "+globalmin+" Max: "+globalmax+" Mean:
        // "+globalmean+" Variance: "+globalvar);
        return hashes;
    }

    @Override
    public void configure(final Configuration cfg) {
        this.ignorePositiveInfinity = cfg.getBoolean(this.getClass().getName()
                + ".ignorePositiveInfinity", true);
        this.ignoreNegativeInfinity = cfg.getBoolean(this.getClass().getName()
                + ".ignoreNegativeInfinity", true);
    }

    public StatsMap getGlobalStatsMap() {
        return this.gsm;
    }

    /**
     * @return the ignoreNegativeInfinity
     */
    public boolean isIgnoreNegativeInfinity() {
        return this.ignoreNegativeInfinity;
    }

    /**
     * @return the ignorePositiveInfinity
     */
    public boolean isIgnorePositiveInfinity() {
        return this.ignorePositiveInfinity;
    }

    public void setFileFragment(final IFileFragment ff1) {
        this.ff = ff1;
    }

    /**
     * @param ignoreNegativeInfinity the ignoreNegativeInfinity to set
     */
    public void setIgnoreNegativeInfinity(final boolean ignoreNegativeInfinity) {
        this.ignoreNegativeInfinity = ignoreNegativeInfinity;
    }

    /**
     * @param ignorePositiveInfinity the ignorePositiveInfinity to set
     */
    public void setIgnorePositiveInfinity(final boolean ignorePositiveInfinity) {
        this.ignorePositiveInfinity = ignorePositiveInfinity;
    }

    public void setVariableFragments(final IVariableFragment... vfs1) {
        this.vfs = vfs1;
    }
}
