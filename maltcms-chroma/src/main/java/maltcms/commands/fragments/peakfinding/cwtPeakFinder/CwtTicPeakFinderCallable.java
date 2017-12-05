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
import cross.datastructures.workflow.WorkflowSlot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.MultiplicationFilter;
import maltcms.commands.filters.array.wavelet.MexicanHatWaveletFilter;
import maltcms.commands.fragments.peakfinding.io.Peak1DUtilities;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.PeakFinderWorkerResult;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.WorkflowResult;
import maltcms.commands.fragments2d.peakfinding.CwtChartFactory;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.datastructures.peak.Peak1D;
import maltcms.datastructures.peak.PeakType;
import maltcms.datastructures.rank.Rank;
import maltcms.datastructures.ridge.Ridge;
import maltcms.tools.ImageTools;
import org.apache.commons.math.stat.descriptive.rank.Percentile;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;

/**
 * <p>
 * CwtTicPeakFinderCallable class.</p>
 *
 * @author Nils Hoffmann
 *
 */
@Data
@Slf4j
public class CwtTicPeakFinderCallable extends AbstractCwtPeakFinderCallable {

    @Configurable
    private boolean integrateRawTic = true;
    @Configurable
    private String totalIntensityVar = "total_intensity";

    private final Peak1DUtilities peakUtilities = new Peak1DUtilities();

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
        Array values = ff.getChild(totalIntensityVar).getArray();
        ArrayStatsScanner ass = new ArrayStatsScanner();
        StatsMap sm = ass.apply(new Array[]{values})[0];
        //normalize to 0..1
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
        for (int i = 1; i <= getMaxScale(); i++) {
            double scale = ((double) i);
            // log.info("Scale: " + scale);
            cwt.setScale(scale);
            Array res = cwt.apply(values);
            Index resI = res.getIndex();
            for (int j = 0; j < res.getShape()[0]; j++) {
                scaleogram.set(j, i - 1, res.getDouble(resI.set(j)));
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

        List<Peak1D> peaks = createPeaksForRidges(ff, values, ridges, cwt, PeakType.TIC_RAW);
        FileFragment outf = new FileFragment(getOutput());
        File outputDirectory = new File(outf.getUri()).getParentFile();
        if (isStoreScaleogram()) {
            VariableFragment scaleogramVar = new VariableFragment(outf, "cwt_scaleogram");
            scaleogramVar.setArray(scaleogram);
        }
        outf.addSourceFile(ff);
        getPeakUtilities().addTicResults(ff, peaks, getPeakNormalizers(), values, "tic_filtered");
        outf.save();
        List<WorkflowResult> results = new LinkedList<>();
        if (isStoreScaleogram()) {
            BufferedImage bi = CwtChartFactory.createColorHeatmap((ArrayDouble.D2) outf.getChild("cwt_scaleogram").getArray());
            File imageFile = ImageTools.saveImage(bi, "scaleogram-" + outf.getName(), "png", outputDirectory, null, outf);
            results.add(new WorkflowResult(imageFile.toURI(), CwtChartFactory.class.getName(), WorkflowSlot.VISUALIZATION, new URI[]{ff.getUri()}));
        }
        results.add(peakUtilities.saveXMLPeakAnnotations(outputDirectory, peaks, ff));
        results.add(peakUtilities.saveCSVPeakAnnotations(outputDirectory, peaks, ff));
        PeakFinderWorkerResult result = new PeakFinderWorkerResult(getOutput(), results);
        return result;
    }
}
