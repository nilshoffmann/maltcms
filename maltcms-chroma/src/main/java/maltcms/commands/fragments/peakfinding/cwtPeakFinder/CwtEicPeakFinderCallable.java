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
package maltcms.commands.fragments.peakfinding.cwtPeakFinder;

import cross.annotations.Configurable;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.WorkflowSlot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;

import maltcms.commands.filters.array.MultiplicationFilter;
import maltcms.commands.filters.array.wavelet.MexicanHatWaveletFilter;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.PeakFinderWorkerResult;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.WorkflowResult;
import maltcms.commands.fragments2d.peakfinding.CwtChartFactory;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.datastructures.peak.Peak1D;
import maltcms.datastructures.peak.PeakType;
import maltcms.datastructures.rank.Rank;
import maltcms.datastructures.ridge.Ridge;
import maltcms.tools.ImageTools;
import maltcms.tools.MaltcmsTools;
import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;

/**
 * <p>
 * CwtEicPeakFinderCallable class.</p>
 *
 * @author Nils Hoffmann
 *
 */
@Data
public class CwtEicPeakFinderCallable extends AbstractCwtPeakFinderCallable {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CwtEicPeakFinderCallable.class);

    @Configurable
    private double massResolution;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PeakFinderWorkerResult call() {
        FileFragment ff = new FileFragment(getInput());
        FileFragment outf = new FileFragment(getOutput());
        File outputDirectory = new File(outf.getUri()).getParentFile();
        outf.addSourceFile(ff);
        log.info("Retrieving min/max mass range!");
        Tuple2D<Double, Double> mm = MaltcmsTools.getMinMaxMassRange(ff);
        double minMass = mm.getFirst(), maxMass = mm.getSecond();
        int massBins = MaltcmsTools.getNumberOfIntegerMassBins(minMass, maxMass, massResolution);
        int totalScans = MaltcmsTools.getNumberOfScans(ff);
        log.info("Using " + massBins + " mass bins at resolution " + massResolution + " on " + totalScans + " scans");
        int offset = 0;
        log.info("Loading eics from scan " + offset + " to scan " + (totalScans - 1));
        Tuple2D<Array, List<Array>> eicPairs = MaltcmsTools.getEICs(ff, massResolution, offset, totalScans);
        List<Array> eics = eicPairs.getSecond();
        List<WorkflowResult> results = new LinkedList<>();
        List<Peak1D> allEicPeaks = new ArrayList<Peak1D>();
        for (int i = 0; i < eics.size(); i++) {
            Array values = eics.get(i);
            ArrayStatsScanner ass = new ArrayStatsScanner();
            StatsMap sm = ass.apply(new Array[]{values})[0];
            MultiplicationFilter mf = new MultiplicationFilter(
                    1.0 / (sm.get(Vars.Max.name()) - sm.get(Vars.Min.name())));
            values = mf.apply(values);
            Percentile p = new Percentile(getMinPercentile());
            double fivePercent = p.evaluate((double[]) values.get1DJavaArray(
                    double.class));
            MexicanHatWaveletFilter cwt = new MexicanHatWaveletFilter();

            List<Double> scales = new LinkedList<>();

            final ArrayDouble.D2 scaleogram = new ArrayDouble.D2(
                    values.getShape()[0],
                    getMaxScale());
            for (int k = 1; k <= getMaxScale(); k++) {
                double scale = ((double) k);
                // log.info("Scale: " + scale);
                cwt.setScale(scale);
                Array res = cwt.apply(values);
                Index resI = res.getIndex();
                for (int j = 0; j < res.getShape()[0]; j++) {
                    scaleogram.set(j, k - 1, res.getDouble(resI.set(j)));
                }
                scales.add(scale);
            }
            List<Ridge> ridges = followRidgesBottomUp(fivePercent,
                    scaleogram, scales, getMinScale(), getMaxScale());

            List<Rank<Ridge>> ranks = new LinkedList<>();
            for (Ridge r : ridges) {
                ranks.add(new Rank<>(r));
            }

            filterRidgesByResponse(ranks, values);
            Collections.sort(ranks);

            log.info("Found " + ridges.size() + " ridges at maxScale="
                    + getMaxScale());

            List<Peak1D> peaks = createPeaksForRidges(ff, values, ridges, cwt, PeakType.EIC_RAW);
            allEicPeaks.addAll(peaks);
            if (isStoreScaleogram()) {
                VariableFragment scaleogramVar = new VariableFragment(outf, "cwt_scaleogram_"+i);
                scaleogramVar.setArray(scaleogram);
                BufferedImage bi = CwtChartFactory.createColorHeatmap((ArrayDouble.D2) outf.getChild("cwt_scaleogram_"+i).getArray());
                File imageFile = ImageTools.saveImage(bi, "scaleogram-"+i+"_" + outf.getName(), "png", outputDirectory, null, outf);
                results.add(new WorkflowResult(imageFile.toURI(), CwtChartFactory.class.getName(), WorkflowSlot.VISUALIZATION, new URI[]{ff.getUri()}));
            }
        }
        getPeakUtilities().addEicResults(ff, allEicPeaks, getPeakNormalizers());
        outf.save();

        results.add(getPeakUtilities().saveXMLPeakAnnotations(outputDirectory, allEicPeaks, ff));
        results.add(getPeakUtilities().saveCSVPeakAnnotations(outputDirectory, allEicPeaks, ff));
        PeakFinderWorkerResult result = new PeakFinderWorkerResult(getOutput(), results);

        return result;
    }
}
