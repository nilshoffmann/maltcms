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
package maltcms.commands.filters.array;

import cross.tools.MathTools;
import java.util.Arrays;
import lombok.Data;

import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;

/**
 * Estimate a local baseline using a windowed median and perform baseline
 * subtraction.
 *
 * @author Nils Hoffmann
 * 
 */
@Data

@ServiceProvider(service = AArrayFilter.class)
public class MedianBaselineFilter extends AArrayFilter {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MedianBaselineFilter.class);

    private int scans = 0;
    private int channels = 0;
    private int medianWindow = 0;
    private double snrMinimum = 1.0d;

    /**
     * <p>Constructor for MedianBaselineFilter.</p>
     */
    public MedianBaselineFilter() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public Array apply(final Array a) {
        return apply(new Array[]{a})[0];
    }

    /** {@inheritDoc} */
    @Override
    public Array[] apply(final Array[] a) {
        Array[] ret = null;
        int i = 0;
        for (final Array arr : a) {
            if (arr instanceof ArrayDouble.D2) {
                if (ret == null) {
                    ret = new Array[a.length];
                }
                final ArrayDouble.D2 c = new ArrayDouble.D2(this.scans,
                        this.channels);
                ret[i] = filterChromatogram(this.scans, this.channels,
                        ((ArrayDouble.D2) a[i]), c, this.medianWindow);
                i++;
            } else {
                throw new IllegalArgumentException(
                        "Only arrays of type ArrayDouble.D2 are supported!");
            }
        }
        if (ret == null) {
            return a;
        }
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        this.channels = cfg.getInt(this.getClass().getName() + ".numChannels",
                500);
        this.scans = cfg.getInt(this.getClass().getName() + ".numScans", 5500);
        this.medianWindow = cfg.getInt(this.getClass().getName()
                + ".medianWindow", 20);
        this.snrMinimum = cfg.getDouble(this.getClass().getName()
                + ".snrMinimum", 6.0d);
    }

    /**
     * <p>filterChromatogram.</p>
     *
     * @param scans1 a int.
     * @param channels1 a int.
     * @param a a {@link ucar.ma2.ArrayDouble.D2} object.
     * @param channels1
     * @param c a {@link ucar.ma2.ArrayDouble.D2} object.
     * @param median_window1 a int.
     * @return filtered Chromatogram as ArrayDouble.D2
     */
    protected ArrayDouble.D2 filterChromatogram(final int scans1,
            final int channels1, final ArrayDouble.D2 a,
            final ArrayDouble.D2 c, final int median_window1) {
        double lmedian = 0.0d;
        double lstddev = 0.0d;
        // mz channels
        for (int j = 0; j < channels1; j++) {
            Array slice;
            try {
                // Extract EIC (all scans, one mass channel)
                slice = a.section(new int[]{0, j}, new int[]{scans1, 1});
                // Array corrected =
                // Array.factory(slice.getElementType(),slice.getShape());
                log.debug("Shape of slice: {} = {}", j, Arrays.toString(slice.
                        getShape()));
                final Index ind = slice.getIndex();
                final Index cind = c.getIndex();
                double current;
                // scans
                for (int i = 0; i < slice.getShape()[0]; i++) {
                    log.debug("i=" + i);
                    // prev = slice.getDouble(ind.set(i - 1));
                    current = slice.getDouble(ind.set(i));
                    // next = slice.getDouble(ind.set(i + 1));
                    // a-1 < a < a+1 -> median = a
                    log.debug("Checking for extremum!");
                    // EvalTools.notNull(current,prev,next);
                    // medianWindow+= (0.5-medianWindow/lstddev);
                    // log.info("Median window size: {}",medianWindow);
                    final int lmedian_low = Math.max(0, i - median_window1);
                    final int lmedian_high = Math.min(slice.getShape()[0] - 1,
                            i + median_window1);
                    log.debug("Median low: " + lmedian_low + " high: "
                            + lmedian_high);
                    double[] vals;// = new int[lmedian_high-lmedian_low];
                    try {
                        vals = (double[]) slice.section(new int[]{lmedian_low},
                                new int[]{lmedian_high - lmedian_low},
                                new int[]{1}).get1DJavaArray(double.class);// ,
                        // 0,
                        // vals,
                        // 0,
                        // vals.length);
                        double mean = MathTools.average(vals, 0, vals.length - 1);
                        lmedian = MathTools.median(vals);
                        lstddev = Math.abs(vals[vals.length - 1] - vals[0]);
                        log.debug("local rel dev={}", lstddev);
                        cind.set(i, j);
                        final double corrected_value = Math.max(current
                                - lmedian, 0);// Math
                        // .
                        // max
                        // (
                        // current
                        // -
                        // lmedian
                        // ,
                        // 0
                        // )
                        // ;
                        final double lvar = vals[vals.length - 1] - lmedian;
                        final double snr = (mean / lstddev);
                        //log.info(snr);
                        final double snrdb = 10.0d * Math.log10(snr);// lmedian_high
                        //log.info(snrdb);
                        // -
                        // lmedian;
                        if (snrdb > 0.0d) {
                            log.debug(
                                    "Signal : {}, noise: {}, ratio: {}, log(ratio): {}",
                                    new Object[]{current, lmedian,
                                        snr, snrdb});
                            log.debug("{}\t{}\t{}\t{}\t{}\t{} ",
                                    new Object[]{current, vals[0], lmedian,
                                        vals[vals.length - 1], lstddev,
                                        lvar, snr});
                        }
                        c.setDouble(cind,
                                snrdb > this.snrMinimum ? corrected_value
                                : 0.0d);// corrected_value);//snrdb>3.0d?
                        // corrected_value:0.0d);
                    } catch (final InvalidRangeException e) {
                        log.error(e.getLocalizedMessage());
                    }
                }
            } catch (final InvalidRangeException e1) {
                log.error(e1.getLocalizedMessage());
            }
        }
        return c;
    }

    /** {@inheritDoc} */
    @Override
    public MedianBaselineFilter copy() {
        MedianBaselineFilter mbf = new MedianBaselineFilter();
        mbf.setChannels(channels);
        mbf.setMedianWindow(medianWindow);
        mbf.setScans(scans);
        mbf.setSnrMinimum(snrMinimum);
        return mbf;
    }
}
