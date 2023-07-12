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
package maltcms.commands.fragments.alignment.peakCliqueAlignment;

import com.carrotsearch.hppc.LongObjectMap;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.media.jai.JAI;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.MinMaxNormalizationFilter;
import maltcms.io.csv.CSVWriter;
import maltcms.tools.ImageTools;
import org.jdom2.Element;
import ucar.ma2.ArrayDouble;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;

/**
 * <p>PeakSimilarityVisualizer class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
@Data
public class PeakSimilarityVisualizer implements IWorkflowElement {

    private IWorkflow workflow;
    private final WorkflowSlot workflowSlot = WorkflowSlot.VISUALIZATION;

    /**
     * <p>visualizePairwisePeakSimilarities.</p>
     *
     * @param outputDir a {@link java.io.File} object.
     * @param edgeMap a {@link com.carrotsearch.hppc.LongObjectMap} object.
     * @param lhsName a {@link java.lang.String} object.
     * @param lhsPeaks a {@link java.util.List} object.
     * @param rhsName a {@link java.lang.String} object.
     * @param rhsPeaks a {@link java.util.List} object.
     * @param samples a int.
     * @param prefix a {@link java.lang.String} object.
     * @param minimize a boolean.
     * @return a {@link java.io.File} object.
     * @since 1.3.2
     */
    public File visualizePairwisePeakSimilarities(File outputDir, final LongObjectMap<PeakEdge> edgeMap,
            final String lhsName, final List<? extends IBipacePeak> lhsPeaks,
            final String rhsName, final List<? extends IBipacePeak> rhsPeaks,
            final int samples, final String prefix, boolean minimize) {
        final List<? extends IBipacePeak> l = lhsPeaks;
        if (!lhsName.equals(rhsName)) {
            final List<? extends IBipacePeak> r = rhsPeaks;
            final ArrayDouble.D2 psims = new ArrayDouble.D2(l.size(),
                    r.size());
            int i = 0;
            for (final IBipacePeak pl : l) {
                int j = 0;
                for (final IBipacePeak pr : r) {
                    double sim = pl.getSimilarity(edgeMap, pr);
                    if (Double.isNaN(sim)) {
                        if (minimize) {
                            sim = Double.POSITIVE_INFINITY;
                        } else {
                            sim = Double.NEGATIVE_INFINITY;
                        }
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
            final RenderedImage bi = ImageTools.makeImage2D(img,
                    samples, Double.NEGATIVE_INFINITY);
            outputDir.mkdirs();
            JAI.create("filestore", bi, new File(outputDir, prefix + "_" + lhsName
                    + "-" + rhsName + "_peak_similarities.png").
                    getAbsolutePath(), "PNG");
            final CSVWriter csvw = new CSVWriter();
            File f = csvw.writeArray2DWithHeader(outputDir.
                    getAbsolutePath(), prefix + "_" + lhsName + "-"
                    + rhsName + "_peak_similarities.csv", psims, new String[]{lhsName, rhsName, "value"});
            return f;
        }
        return null;
    }

    /**
     * <p>visualizePeakSimilarities.</p>
     *
     * @param edgeMap a {@link com.carrotsearch.hppc.LongObjectMap} object.
     * @param hm a {@link java.util.Map} object.
     * @param samples a int.
     * @param prefix a {@link java.lang.String} object.
     */
    public void visualizePeakSimilarities(final LongObjectMap<PeakEdge> edgeMap,
            final Map<String, List<? extends IBipacePeak>> hm, final int samples,
            final String prefix) {

        int npeaks = 0;
        for (final String key : hm.keySet()) {
            npeaks += hm.get(key).size();
        }
        if (npeaks == 0) {
            log.warn("No peak similarities to visualize!");
            return;
        }
        log.info("Saving pairwise peak similarity images.");
        final List<String> keys = new ArrayList<>(hm.keySet());
        Collections.sort(keys);
        boolean minimize = false;
        File outputDir = workflow.getOutputDirectory(this);
        for (final String keyl : keys) {
            final List<? extends IBipacePeak> l = hm.get(keyl);
            for (final String keyr : keys) {
                File f = visualizePairwisePeakSimilarities(outputDir, edgeMap, keyl, l, keyr, hm.get(keyr), samples, prefix, minimize);
                workflow.append(new DefaultWorkflowResult(f, this, workflowSlot));
            }
        }

    }

    /** {@inheritDoc} */
    @Override
    public void appendXML(Element e) {
    }
}
