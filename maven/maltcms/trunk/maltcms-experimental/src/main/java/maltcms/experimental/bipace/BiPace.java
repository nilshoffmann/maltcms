/*
 * $license$
 *
 * $Id$
 */
package maltcms.experimental.bipace;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.Peak;
import maltcms.experimental.bipace.datastructures.api.Clique;
import maltcms.experimental.bipace.datastructures.spi.PairwisePeakListSimilarities;
import maltcms.experimental.bipace.api.IPeakListProvider;
import maltcms.experimental.bipace.api.PeakSimilarityCalculator;
import maltcms.experimental.bipace.peakCliqueAlignment.CliqueFinder;
import net.sf.maltcms.execution.spi.MaltcmsCompletionService;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
public class BiPace<T extends Peak> implements Callable<BiPaceResult<T>> {

    private final List<File> inputFiles;
    private final IPeakListProvider<T> peakListProvider;
    private final PeakSimilarityCalculator<T> peakSimilarityCalculator;
    private final CliqueFinder<T> cliqueFinder;
    private final int numberOfThreads = 1;

    @Override
    public BiPaceResult<T> call() {
        //prepare and shuffle input
        TupleND<IFileFragment> fragments = prepareInput();
        //create a completion service for pairwise similarity calculation
        MaltcmsCompletionService<PairwisePeakListSimilarities> completionService = new MaltcmsCompletionService<PairwisePeakListSimilarities>(
                Executors.newFixedThreadPool(numberOfThreads), numberOfThreads,
                TimeUnit.MINUTES, false);
        TupleND<PeakList<T>> peakLists = preparePeakLists(fragments);
        //create jobs and wait for completion, also sets pairwise similarities for peak objects
        calculatePairwiseSimilarities(
                peakLists, completionService);

        HashMap<String, List<T>> fragmentToPeaks = prepareFragmentToPeakMap(
                peakLists);
        List<List<T>> ll = new ArrayList<List<T>>();
        HashMap<T, Clique<T>> peakToClique = new HashMap<T, Clique<T>>();
        List<Clique<T>> cliques = cliqueFinder.findCliques(fragments,
                fragmentToPeaks, ll, peakToClique);

        BiPaceResult<T> bpr = new BiPaceResult<T>();
        bpr.setCliques(cliques);
        return bpr;
    }

    private HashMap<String, List<T>> prepareFragmentToPeakMap(
            TupleND<PeakList<T>> peakLists) {
        HashMap<String, List<T>> fragmentToPeaks = new HashMap<String, List<T>>();
        for (PeakList<T> pl : peakLists) {
            fragmentToPeaks.put(pl.getFragment().getName(), pl.getPeaks());
        }
        return fragmentToPeaks;
    }

    private void calculatePairwiseSimilarities(
            TupleND<PeakList<T>> peakLists,
            MaltcmsCompletionService<PairwisePeakListSimilarities> completionService) throws RejectedExecutionException, NullPointerException {
        HashMap<UUID, Tuple2D<PeakList<T>, PeakList<T>>> taskMap = new HashMap<UUID, Tuple2D<PeakList<T>, PeakList<T>>>();
        //calculate pairwise similarities between peak lists for all unique pairs of chromatograms
        for (Tuple2D<PeakList<T>, PeakList<T>> t : peakLists.getPairs()) {
            PeakSimilarityCalculator<T> psc = peakSimilarityCalculator.copy();
            psc.setPeakListA(t.getFirst().getPeaks());
            psc.setPeakListB(t.getSecond().getPeaks());
            psc.setUniqueId(UUID.randomUUID());
            taskMap.put(psc.getUniqueId(), t);
            completionService.submit(psc);
        }
        List<PairwisePeakListSimilarities> results = Collections.emptyList();
        try {
            results = completionService.call();
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        if (!completionService.getFailedTasks().isEmpty()) {
            log.warn("{} jobs failed to terminate!", completionService.
                    getFailedTasks().size());
        }

        for (PairwisePeakListSimilarities ppls : results) {
            DoubleMatrix2D result = ppls.getResult();
            Tuple2D<PeakList<T>, PeakList<T>> t = taskMap.get(
                    ppls.getProducerId());
            List<T> peakListA = t.getFirst().getPeaks();
            List<T> peakListB = t.getSecond().getPeaks();
            // all-against-all peak list comparison
            // l^{2} for l=max(|lhsPeaks|,|rhsPeaks|)
            int minElements = Math.max(peakListA.size(),peakListB.size());
            IntArrayList alist = new IntArrayList(minElements);
            IntArrayList blist = new IntArrayList(minElements);
            DoubleArrayList reslist = new DoubleArrayList(minElements);
            result.getNonZeros(alist, blist, reslist);
            for (int idx = 0; idx < alist.size(); idx++) {
                int i = alist.getQuick(idx);
                int j = blist.getQuick(idx);
                double value = reslist.getQuick(idx);
                T a = peakListA.get(i);
                T b = peakListB.get(j);
                a.addSimilarity(b, value);
                b.addSimilarity(a, value);
            }
        }

    }

    private TupleND<IFileFragment> prepareInput() {
        TupleND<IFileFragment> fragments = new TupleND<IFileFragment>();
        for (File f : inputFiles) {
            fragments.add(new FileFragment(f));
        }
        return fragments;
    }

    private TupleND<PeakList<T>> preparePeakLists(
            TupleND<IFileFragment> fragments) {
        TupleND<PeakList<T>> peakLists = new TupleND<PeakList<T>>();
        for (IFileFragment fragment : fragments) {
            peakLists.add(peakListProvider.getPeaks(fragment));
        }
        return peakLists;
    }
}
