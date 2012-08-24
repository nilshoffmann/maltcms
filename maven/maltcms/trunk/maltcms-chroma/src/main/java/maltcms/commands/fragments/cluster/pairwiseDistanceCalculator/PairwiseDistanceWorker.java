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
package maltcms.commands.fragments.cluster.pairwiseDistanceCalculator;

import cross.Factory;
import cross.annotations.Configurable;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.IWorkflow;
import cross.tools.StringTools;
import java.io.File;
import java.io.Serializable;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.io.misc.StatsWriter;
import maltcms.tools.MaltcmsTools;
import maltcms.commands.distances.PairwiseFeatureSequenceSimilarity;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
public class PairwiseDistanceWorker implements
        Callable<PairwiseDistanceResult>, Serializable {
    private static final long serialVersionUID = 4556712389798130L;

    private PairwiseFeatureSequenceSimilarity similarity;
    private IWorkflow workflow;
    private Tuple2D<File, File> input;
    private File outputDirectory;
    private int jobNumber;
    private int nJobs;
    
    @Override
    public PairwiseDistanceResult call() throws Exception {
        similarity.setWorkflow(getWorkflow());
        final String filename = "PW_DISTANCE_"
                + StringTools.removeFileExt(
                input.getFirst().getName())
                + "_"
                + StringTools.removeFileExt(input.getSecond().
                getName()) + ".csv";
        final IFileFragment iff = new FileFragment(outputDirectory,filename);
//        Factory.getInstance().
//                getFileFragmentFactory().create(
//                new File(getWorkflow().getOutputDirectory(
//                workflowElement), filename));
        final StatsMap sm = new StatsMap(iff);
        similarity.setStatsMap(sm);
        final long t_start = System.currentTimeMillis();
        log.info(
                "Running job {}/{}: {} with {}",
                new Object[]{jobNumber+1, nJobs,
                    input.getFirst().getName(),
                    input.getSecond().getName()});
        IFileFragment ff;
        ff = similarity.apply(new FileFragment(input.getFirst()), new FileFragment(input.getSecond()));
        final long t_end = System.currentTimeMillis() - t_start;
        log.info("Finished dtw in {} seconds",t_end/1000.0d);
        final double d = similarity.getResult().get();
        final int maplength = MaltcmsTools.getWarpPath(ff).size();
        log.info("Map length: {}",maplength);
        sm.setLabel(input.getFirst().getName() + "-"
                + input.getSecond().getName());
        sm.put("time", new Double(t_end));
        sm.put("value", new Double(d));
        sm.put("maplength", new Double(maplength));
        final StatsWriter sw = Factory.getInstance().
                getObjectFactory().instantiate(StatsWriter.class);
        sw.setWorkflow(getWorkflow());
        sw.write(sm);
        log.info("Distance: " + d);
        EvalTools.notNull(new Object[]{input,ff,d,sm}, this);
        return new PairwiseDistanceResult(input, new File(ff.getAbsolutePath()), d,sm);
    }
}
