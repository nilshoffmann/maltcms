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
package maltcms.commands.scanners;

import cross.annotations.Configurable;
import cross.commands.ICommand;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * Scans a number of arrays for statistics, such as mean and variance, storing
 * results for each array in a HashMap indexed by Elements of
 *
 * @link{cross.Vars} .
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class ArrayStatsScanner implements ICommand<Array[], StatsMap[]> {

    private StatsMap gsm = null;
    private IFileFragment ff = new FileFragment(new File("default-stats.cdf"));
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
