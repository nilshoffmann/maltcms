/*
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
package maltcms.commands.fragments.peakfinding.ticPeakFinder;

import cross.annotations.Configurable;
import cross.datastructures.tools.EvalTools;
import cross.tools.MathTools;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.BatchFilter;
import maltcms.commands.filters.array.MultiplicationFilter;
import maltcms.commands.filters.array.TopHatFilter;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import ucar.ma2.Array;

/**
 *
 * @author nilshoffmann
 */
@Slf4j
@Data
public class QuantileSnrPeakFinder implements IPeakFinder {

    @Configurable
    private List<AArrayFilter> filter = Arrays.asList(
            (AArrayFilter) new MultiplicationFilter());
    @Configurable
    private TopHatFilter baselineFilter = new TopHatFilter();
    
    @Configurable
    private double peakSnrThreshold = 75.0;
    @Configurable
    private int peakSeparationWindow = 10;
    @Configurable
    private double lowerPercentile = 5.0;
    @Configurable
    private double upperPercentile = 95.0;
    @Configurable
    private int baselineFilterWindow = 500;
    @Configurable
    private int baselineEstimationMinimaWindow = 1000;
    @Configurable
    private int meanEstimationWindow = 100;
    
    protected Array applyFilters(final Array correctedtic) {
        final Array filteredtic = BatchFilter.applyFilters(correctedtic,
                this.filter);
        return filteredtic;
    }
    
    @Override
    public PeakPositionsResultSet findPeakPositions(Array tic) {
        EvalTools.notNull(tic, this);
        Array correctedtic = null;
        final ArrayList<Integer> ts = new ArrayList<Integer>();
        log.debug("Value\tLow\tMedian\tHigh\tDev\tGTMedian\tSNR");
        double[] ticValues = (double[]) tic.get1DJavaArray(double.class);
        correctedtic = applyFilters(tic.copy());
        // ticValues = getMinimumBaselineEstimate((double[]) correctedtic
        // .get1DJavaArray(double.class));
        // correctedtic = Array.factory(ticValues);
        baselineFilter.setWindow(baselineFilterWindow);
        double[] snrValues = new double[ticValues.length];
        double[] baselineEstimate = (double[])baselineFilter.apply(correctedtic).get1DJavaArray(double.class);
        double[] cticValues = (double[]) correctedtic.get1DJavaArray(
                double.class);
        DescriptiveStatistics stats = new DescriptiveStatistics();
        stats.setWindowSize(meanEstimationWindow);

        StandardDeviation sd = new StandardDeviation();
        for (int i = 0; i < snrValues.length; i++) {
            stats.addValue(cticValues[i]);
            double normalized = ((stats.getPercentile(upperPercentile)-stats.getPercentile(lowerPercentile)))/stats.getPercentile(lowerPercentile);
            double snr = 20.0d * Math.log10(normalized);
            snrValues[i] = Double.isInfinite(snr) ? 0 : snr;
            
        }
        log.debug("SNR: {}", Arrays.toString(snrValues));
        for (int i = 0; i < ticValues.length; i++) {
            log.debug("i=" + i);
            PeakFinderUtils.checkExtremum(cticValues, snrValues, ts, peakSnrThreshold, i,
                    peakSeparationWindow);
        }
        PeakPositionsResultSet pprs = new PeakPositionsResultSet(correctedtic,
                PeakFinderUtils.createPeakCandidatesArray(tic, ts), snrValues, ts,PeakFinderUtils.findBaseline(cticValues,baselineEstimationMinimaWindow));
        return pprs;
    }
    
}
