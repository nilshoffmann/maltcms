/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2013, The authors of Maltcms. All rights reserved.
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
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.experimental.Value;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Value
public class CliqueFinder {

    private final boolean saveIncompatiblePeaks;
    private final boolean saveUnassignedPeaks;
    private final double minBbhFraction;
    private final IWorkflowElement parent;
    private final boolean postProcessPeaks;

    /**
     * @param al
     * @param nameToFragment
     * @param fragmentToPeaks
     * @param minCliqueSize
     * @param npeaks
     * @return a PairwiseSimilarityResult
     */
    public BBHResult combineBiDiBestHits(final TupleND<IFileFragment> al, final Map<String, IFileFragment> nameToFragment, final Map<String, Integer> nameToIndex,
            final Map<String, List<IBipacePeak>> fragmentToPeaks, final int minCliqueSize, int npeaks, final LongObjectMap<PeakEdge> edgeMap) {

        // given: a hashmap of name<->peak list
        // an empty list of peaks belonging to a clique
        // a minimum size for a clique from when on it is considered valid
        HashMap<IBipacePeak, Clique<IBipacePeak>> peakToClique = new HashMap<>();
        Set<IBipacePeak> incompatiblePeaks = new LinkedHashSet<>();
        Set<IBipacePeak> unassignedPeaks = new LinkedHashSet<>();
        ObjectObjectOpenHashMap<UUID, IBipacePeak> peakRepository = new ObjectObjectOpenHashMap<>();
        for (String key : fragmentToPeaks.keySet()) {
            for (IBipacePeak p : fragmentToPeaks.get(key)) {
                peakRepository.put(p.getUniqueId(), p);
            }
        }
        // every peak is assigned to at most one clique!!!
        // reassignment is invalid and should not occur
        // for all files
        // file comparisons: k*(k-1)
        // per peak comparison: 2*l
        // check for clique membership: (k*l)
        for (IFileFragment iff : al) {
            final List<IBipacePeak> peaks = fragmentToPeaks.get(iff.getName());
            log.info("Checking {} peaks for file {}", peaks.size(),
                    iff.getName());
			// for all peaks in file

            // final List<IPeak> bidiHits = new ArrayList<IPeak>();
            // bidiHits.add(p);
            // for all other files
            for (final IFileFragment jff : al) {
                // only compare between partition matches, i!=j
                if (!iff.getName().equals(jff.getName())) {
                    for (final IBipacePeak p : peaks) {
                        // retrieve list of most similar peaks
                        final IBipacePeak q = peakRepository.get(p.getPeakWithHighestSimilarity(edgeMap, nameToIndex.get(jff.getName())));
                        if (q == null) {
                            // null peaks have no bidi best hit, so they are
                            // removed
                            // beforehand
                            log.debug("Skipping null peak");
                            unassignedPeaks.add(p);
                            continue;
                        }
                        // security check, this should never happen, but if
                        // the similarity function is wrongly parameterized,
                        // this may
                        // lead to false assignments, so inform the user that
                        // something
                        // is not right!
                        if (p.getSimilarity(edgeMap, q) == Double.NEGATIVE_INFINITY
                                || p.getSimilarity(edgeMap, q) == Double.POSITIVE_INFINITY) {
                            throw new IllegalArgumentException(
                                    "Infinite similarity value for associated peaks!");
                        }
                        // bidirectional hit
                        if (q != null && q.isBidiBestHitFor(edgeMap, p)) {
                            log.debug(
                                    "Found bidirectional best hit for peak {}: {}",
                                    p, q);
							// Possible cases, if we found a bidirectional hit
                            // for p
                            // 1: p is already in a clique
                            // 3: p and q are already in a clique
                            // 3: a: p and q are already in the same clique???
                            // 3: b: p and q are in different cliques !!!
                            // conflict!!!
                            // 4: p and q are not in a clique, create a new
                            // clique and add both

                            // initialization of cliques, if present
                            Clique<IBipacePeak> c = null, d = null;
                            if (peakToClique.containsKey(q)) {
                                d = peakToClique.get(q);
                                if (d != null) {
                                    log.debug("Found clique for peak q");
                                }
                            }
                            if (peakToClique.containsKey(p)) {// p has a clique
                                c = peakToClique.get(p);
                                if (c != null) {
                                    log.debug("Found clique for peak p");
                                }
                            }

                            //
                            if (d != null && c != null && c != d) {
                                log.debug(
                                        "Found different cliques for peak p and q!");
                                log.debug("Clique for p: {}", c);
                                log.debug("Clique for q: {}", d);
                                // try to merge cliques
                                incompatiblePeaks.addAll(mergeCliques(peakToClique, c, d, edgeMap));
                            } else if (c != null && d == null) {
                                if (c.addPeak(edgeMap, q)) {
                                    peakToClique.put(q, c);
                                }
                            } else if (d != null && c == null) {
                                if (d.addPeak(edgeMap, p)) {
                                    peakToClique.put(p, d);
                                }
                            } else if (c == null && d == null) {
                                createNewClique(peakToClique, p, q, edgeMap);
                            } else if (c == d && c != null) {
                                if (c.addPeak(edgeMap, p)) {
                                    peakToClique.put(p, c);
                                }
                            } else {
                                log.error(
                                        "Unhandled case in if else! Missed a state?: c={} d={}, p={}, q={}",
                                        new Object[]{c, d, p, q});
                            }
                        } else {
                            log.debug(
                                    "Peak q:{} and p:{} are no bidirectional best hits!",
                                    p, q);
                        }
                    }
                }
            }
        }

        log.info("Found {}/{} incompatible peaks.",
                incompatiblePeaks.size(), npeaks);
        log.info("Found {}/{} unassigned peaks.", unassignedPeaks.size(), npeaks);
        File workflowOutputDir = parent.getWorkflow().getOutputDirectory(parent);
        if (saveIncompatiblePeaks) {
            PeakListWriter writer = new PeakListWriter();
            File incompatiblePeaksFile = writer.savePeakList(workflowOutputDir, nameToFragment, incompatiblePeaks, "incompatiblePeaks.msp", "INCOMPATIBLE");
            parent.getWorkflow().append(new DefaultWorkflowResult(incompatiblePeaksFile, parent, WorkflowSlot.FILEIO, nameToFragment.values().toArray(new IFileFragment[nameToFragment.size()])));
        }
        
        if (saveUnassignedPeaks) {
            PeakListWriter writer = new PeakListWriter();
            File unassignedPeaksFile = writer.savePeakList(workflowOutputDir, nameToFragment, unassignedPeaks, "unassignedPeaks.msp", "UNASSIGNED");
            parent.getWorkflow().append(new DefaultWorkflowResult(unassignedPeaksFile, parent, WorkflowSlot.FILEIO, nameToFragment.values().toArray(new IFileFragment[nameToFragment.size()])));
        }
        
        if (postProcessPeaks) {
            postProcessCliques(edgeMap, peakToClique.values(), incompatiblePeaks, unassignedPeaks);
        }

        for (IBipacePeak p : incompatiblePeaks) {
            log.debug("Incompatible peak: " + p);
            for (String partition : nameToFragment.keySet()) {
                p.clearSimilarities(edgeMap, nameToIndex.get(partition));
                p.setMsIntensities(null);
            }
        }
        
        for (IBipacePeak p : unassignedPeaks) {
            for (String partition : nameToFragment.keySet()) {
                p.clearSimilarities(edgeMap, nameToIndex.get(partition));
                p.setMsIntensities(null);
            }
        }

        // retain all cliques, which exceed minimum size
        HashSet<Clique<IBipacePeak>> cliques = new HashSet<>();
        for (Clique<IBipacePeak> c : peakToClique.values()) {
            if (!cliques.contains(c)) {
                log.debug("Size of clique: {}\n{}",
                        c.size(), c);
                cliques.add(c);
            }
        }

        // sort cliques by clique rt mean
        List<Clique<IBipacePeak>> l = new ArrayList<>(cliques);
        Collections.sort(l, new Comparator<Clique<IBipacePeak>>() {
            @Override
            public int compare(Clique<IBipacePeak> o1, Clique<IBipacePeak> o2) {
                double rt1 = o1.getCliqueRTMean();
                double rt2 = o2.getCliqueRTMean();
                if (rt1 > rt2) {
                    return 1;
                } else if (rt1 < rt2) {
                    return -1;
                }
                return 0;
            }
        });
        return new BBHResult(l, peakToClique);
    }

    /**
     * @param p
     * @param q
     */
    private void createNewClique(HashMap<IBipacePeak, Clique<IBipacePeak>> peakToClique, final IBipacePeak p, final IBipacePeak q, final LongObjectMap<PeakEdge> edgeMap) {
        Clique<IBipacePeak> c;
        // assigned yet
        c = new Clique<>();
//		c.setMaxBBHErrors(this.maxBBHErrors);
        c.setMinBbhFraction(this.minBbhFraction);
        if (c.addPeak(edgeMap, p)) {
            peakToClique.put(p, c);
        }
        if (c.addPeak(edgeMap, q)) {
            peakToClique.put(q, c);
        }
    }

    /**
     * @param c
     * @param d
     * @return
     */
    private List<IBipacePeak> mergeCliques(HashMap<IBipacePeak, Clique<IBipacePeak>> peakToClique, Clique<IBipacePeak> c, Clique<IBipacePeak> d, final LongObjectMap<PeakEdge> edgeMap) {
        int ds = d.size();
        int cs = c.size();
        //if either clique is empty, we can not merge,
        //so we can not have any incompatible peaks,
        //so we return an empty list
        if (ds == 0 || cs == 0) {
            return Collections.emptyList();
        }

        //start merging if both cliques have at least one peak in them
        log.debug("Merging cliques: c={}, d={}", c.getID(),
                d.getID());
        // ds has more peaks than cs -> join cs into
        // ds
        List<IBipacePeak> incompatiblePeaks = new LinkedList<>();
        if (ds > cs) {
            for (IBipacePeak pk : c.getPeakList()) {
                if (d.addPeak(edgeMap, pk)) {
                    // c.removePeak(pk);
                    peakToClique.put(pk, d);
                } else {
                    incompatiblePeaks.add(pk);
                    log.debug("Adding of peak {} into clique {} failed", pk, d);
                }

            }
            log.debug("Clique {} has {} peaks left!", c, c.size());
            c.clear();
        } else {// ds has less peaks than cs -> join
            // ds into cs
            for (IBipacePeak pk : d.getPeakList()) {
                if (c.addPeak(edgeMap, pk)) {
                    // d.removePeak(pk);
                    peakToClique.put(pk, c);
                } else {
                    incompatiblePeaks.add(pk);
                    log.debug("Adding of peak {} into clique {} failed", pk, c);
                }

            }
            log.debug("Clique {} has {} peaks left!", d, d.size());
            d.clear();
        }
        return incompatiblePeaks;
    }

    private void postProcessCliques(LongObjectMap<PeakEdge> edgeMap, Collection<Clique<IBipacePeak>> cliques, Collection<IBipacePeak> incompatiblePeaks, Collection<IBipacePeak> unassignedPeaks) {
        log.error("Post processing cliques and unmatched peaks");
//			SparseDoubleMatrix2D sdm = new SparseDoubleMatrix2D(cliques.size(), result.getIncompatiblePeaks().size() + result.getUnassignedPeaks().size() + unmatchedPeaks.size());
        //identify all peaks that are not contained in the final cliques
//			int clique = 0;
        int peakNumberCorrection = 0;
        int incompatibleAdded = 0;
        int unassignedAdded = 0;
//        int unmatchedAdded = 0;
        for (Clique<IBipacePeak> c : cliques) {
            if (!c.getPeakList().isEmpty()) {
                log.debug("Checking clique at {} with size {} and rtvariance {}", new Object[]{c.getCliqueRTMean(), c.getPeakList().size(), c.getCliqueRTVariance()});
                //log.info("RT variance within clique {} containing {} peaks ={}; Stdev: {}", new Object[]{c.getCliqueRTMean(), c.getPeakList().size(), c.getCliqueRTVariance(), Math.sqrt(c.getCliqueRTVariance())});
                for (Iterator<IBipacePeak> incompatiblePeaksIter = incompatiblePeaks.iterator(); incompatiblePeaksIter.hasNext();) {
                    IBipacePeak q = incompatiblePeaksIter.next();
                    double ratio = c.getRatioOfRTDistanceToCentroidAndCliqueVariance(q);
                    if (ratio <= 1) {
                        if (c.addPeak(edgeMap, q)) {
                            log.debug("Adding formerly incompatible peak {} to clique", q);
                            peakNumberCorrection++;
                            incompatibleAdded++;
                            incompatiblePeaksIter.remove();
                        }
                    }
                }
                for (Iterator<IBipacePeak> unassignedPeaksIter = unassignedPeaks.iterator(); unassignedPeaksIter.hasNext();) {
                    IBipacePeak q = unassignedPeaksIter.next();
                    double ratio = c.getRatioOfRTDistanceToCentroidAndCliqueVariance(q);
                    if (ratio <= 1) {
                        if (c.addPeak(edgeMap, q)) {
                            log.debug("Adding formerly unassigned peak {} to clique", q);
                            peakNumberCorrection++;
                            unassignedAdded++;
                            unassignedPeaksIter.remove();
                        }
                    }
                }
//                for (Iterator<IBipacePeak> unmatchedPeaksIter = unmatchedPeaks.iterator(); unmatchedPeaksIter.hasNext();) {
//                    IBipacePeak q = unmatchedPeaksIter.next();
//                    double ratio = c.getRatioOfRTDistanceToCentroidAndCliqueVariance(q);
//                    if (ratio <= 1) {
//                        if (c.addPeak(edgeMap, q)) {
//                            log.debug("Adding formerly unmatched peak {} to clique", q);
//                            peakNumberCorrection++;
//                            unmatchedAdded++;
//                            unmatchedPeaksIter.remove();
//                        }
//                    }
////                        peak++;
//                }
            } else {
                log.debug("Skipping empty clique at {}", c.getCliqueRTMean());
            }
        }
        log.error("Post processing assigned {} incompatible and {} previously unassigned peaks to cliques!", new Object[]{incompatibleAdded, unassignedAdded});
    }
}
