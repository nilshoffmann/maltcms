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
package maltcms.commands.fragments.alignment.peakCliqueAlignment;

import cross.datastructures.workflow.IWorkflow;
import java.awt.Color;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.media.jai.JAI;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.MinMaxNormalizationFilter;
import maltcms.datastructures.peak.Peak;
import maltcms.io.csv.CSVWriter;
import maltcms.io.csv.ColorRampReader;
import maltcms.tools.ImageTools;
import ucar.ma2.ArrayDouble;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;

/**
 *
 * @author nils
 */
@Slf4j
public class PeakSimilarityVisualizer {

    public void visualizePeakSimilarities(
            final Map<String, List<Peak>> hm, final int samples,
            final String prefix,
            final IWorkflow workflow) {

        int npeaks = 0;
        for (final String key : hm.keySet()) {
            npeaks += hm.get(key).size();
        }
        if (npeaks == 0) {
            log.warn("No peak similarities to visualize!");
            return;
        }
        log.info("Saving pairwise peak similarity images.");
        final List<String> keys = new ArrayList<String>(hm.keySet());
        Collections.sort(keys);
        boolean minimize = false;//costFunction.minimize();
        for (final String keyl : keys) {
            final List<Peak> l = hm.get(keyl);
            for (final String keyr : keys) {
                if (!keyl.equals(keyr)) {
                    final List<Peak> r = hm.get(keyr);
                    final ArrayDouble.D2 psims = new ArrayDouble.D2(l.size(),
                            r.size());
                    int i = 0;
                    for (final Peak pl : l) {
                        int j = 0;
                        for (final Peak pr : r) {
                            double sim = pl.getSimilarity(pr);
                            if (Double.isNaN(sim)) {
                                // log.warn("NaN occurred!");
                                if (minimize) {
                                    sim = Double.POSITIVE_INFINITY;
                                } else {
                                    sim = Double.NEGATIVE_INFINITY;
                                }
                                // throw new IllegalArgumentException();
                            }
                            log.debug("Setting index {}/{},{}/{}",
                                    new Object[]{i, l.size() - 1, j,
                                        r.size() - 1});
                            psims.set(
                                    i,
                                    j,
                                    sim == Double.NEGATIVE_INFINITY ? 0
                                    : sim == Double.POSITIVE_INFINITY ? Double.MAX_VALUE
                                    : sim);
                            j++;
                        }
                        i++;
                    }

                    MinMax mm = MAMath.getMinMax(psims);

                    MinMaxNormalizationFilter mmnf = new MinMaxNormalizationFilter(
                            mm.min, mm.max);
                    ArrayDouble.D2 img = (ArrayDouble.D2) mmnf.apply(psims);

                    int nsamples = 256;
                    double[] sampleTable = ImageTools.createSampleTable(nsamples);
                    final ColorRampReader crr = new ColorRampReader();
                    final int[][] colorRamp = crr.getDefaultRamp();
                    Color[] cRamp = ImageTools.rampToColorArray(colorRamp);
                    BufferedImage crampImg = ImageTools.createColorRampImage(
                            sampleTable, Transparency.TRANSLUCENT, cRamp);
                    BufferedImage sourceImg = ImageTools.makeImage2D(img,
                            nsamples);
                    BufferedImage destImg = ImageTools.applyLut(sourceImg,
                            ImageTools.createLookupTable(crampImg, 1.0f,
                            nsamples));

                    // final RenderedImage bi = ImageTools.makeImage2D(psims,
                    // samples, Double.NEGATIVE_INFINITY);
                    JAI.create("filestore", destImg, new File(workflow.
                            getOutputDirectory(this), prefix + "_" + keyl
                            + "-" + keyr + "_peak_similarities.png").
                            getAbsolutePath(), "PNG");
                    final CSVWriter csvw = new CSVWriter();
                    csvw.setWorkflow(workflow);
                    csvw.writeArray2D(workflow.getOutputDirectory(this).
                            getAbsolutePath(), prefix + "_" + keyl + "-"
                            + keyr + "_peak_similarities.csv", psims);
                }
            }

        }

    }
}
