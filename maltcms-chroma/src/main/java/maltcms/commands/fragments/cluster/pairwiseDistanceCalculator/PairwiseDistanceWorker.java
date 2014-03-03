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
package maltcms.commands.fragments.cluster.pairwiseDistanceCalculator;

import cross.Factory;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.IWorkflow;
import cross.tools.StringTools;
import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.distances.PairwiseFeatureSequenceSimilarity;
import maltcms.io.misc.StatsWriter;
import maltcms.tools.MaltcmsTools;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
public class PairwiseDistanceWorker implements
    Callable<PairwiseDistanceResult>, Serializable {

    private static final long serialVersionUID = 4556712389798130L;
    private PairwiseFeatureSequenceSimilarity similarity;
    private IWorkflow workflow;
    private Tuple2D<URI, URI> input;
    private File outputDirectory;
    private int jobNumber;
    private int nJobs;

    /**
     *
     * @return @throws Exception
     */
    @Override
    public PairwiseDistanceResult call() throws Exception {
        similarity.setWorkflow(getWorkflow());
        final String input1 = FileTools.getFilename(input.getFirst());
        final String input2 = FileTools.getFilename(input.getSecond());
        final String filename = "PW_DISTANCE_"
            + StringTools.removeFileExt(input1)
            + "_"
            + StringTools.removeFileExt(input2) + ".csv";
        final IFileFragment iff = new FileFragment(new File(outputDirectory, filename).toURI());
        final StatsMap sm = new StatsMap(iff);
        similarity.setStatsMap(sm);
        final long t_start = System.currentTimeMillis();
        log.info(
            "Running job {}/{}: {} with {}",
            new Object[]{jobNumber + 1, nJobs,
                input1,
                input2});
        IFileFragment ff;
        ff = similarity.apply(new FileFragment(input.getFirst()), new FileFragment(input.getSecond()));
        final long t_end = System.currentTimeMillis() - t_start;
        log.info("Finished dtw in {} seconds", t_end / 1000.0d);
        final double d = similarity.getResult().get();
        final int maplength = MaltcmsTools.getWarpPath(ff).size();
        log.info("Map length: {}", maplength);
        sm.setLabel(input1 + "-"
            + input2);
        sm.put("time", new Double(t_end));
        sm.put("value", new Double(d));
        sm.put("maplength", new Double(maplength));
        final StatsWriter sw = Factory.getInstance().
            getObjectFactory().instantiate(StatsWriter.class);
        sw.setWorkflow(getWorkflow());
        sw.write(sm);
        log.info("Distance: " + d);
        EvalTools.notNull(new Object[]{input, ff, d, sm}, this);
        PairwiseDistanceResult pdr = new PairwiseDistanceResult(input, ff.getUri(), d, sm);
        return pdr;
    }
}
