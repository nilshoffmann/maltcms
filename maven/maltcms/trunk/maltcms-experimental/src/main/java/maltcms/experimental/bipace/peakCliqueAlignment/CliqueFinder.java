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
package maltcms.experimental.bipace.peakCliqueAlignment;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.IPeak;
import maltcms.experimental.bipace.datastructures.api.Clique;
import org.jdom.Element;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
public class CliqueFinder<T extends IPeak> implements IWorkflowElement {

    private int minCliqueSize = -1;
    private IWorkflow workflow;
    private int maxBBHErrors = 0;
    private boolean savePlots = false;

    public List<Clique<T>> findCliques(final TupleND<IFileFragment> t,
            final HashMap<String, List<T>> fragmentToPeaks,
            final List<List<T>> ll, final HashMap<T, Clique<T>> peakToClique) {
        List<Clique<T>> cliques;
        if (this.minCliqueSize == -1 || this.minCliqueSize == t.size()) {
            log.info("Combining bidirectional best hits if present in all files");
            cliques = combineBiDiBestHits(t, fragmentToPeaks, ll, t.size(), peakToClique);
        } else {
            if (this.minCliqueSize > t.size()) {
                log.info("Resetting minimum group size to: {}, was: {}",
                        t.size(), this.minCliqueSize);
                this.minCliqueSize = t.size();
            }
            log.info("Combining bidirectional best hits, minimum group size: {}",
                    this.minCliqueSize);
            cliques = combineBiDiBestHits(t, fragmentToPeaks, ll,
                    this.minCliqueSize, peakToClique);
        }
        return cliques;
    }

    /**
     * @param al
     * @param fragmentToPeaks
     * @param ll
     * @param minCliqueSize
     * @return a list of clique objects
     */
    public List<Clique<T>> combineBiDiBestHits(final TupleND<IFileFragment> al,
            final HashMap<String, List<T>> fragmentToPeaks,
            final List<List<T>> ll, final int minCliqueSize,
            final HashMap<T, Clique<T>> peakToClique) {

        // given: a hashmap of name<->peak list
        // an empty list of peaks belonging to a clique
        // a minimum size for a clique from when on it is considered valid
        HashSet<T> incompatiblePeaks = new LinkedHashSet<T>();
        HashSet<T> unassignedPeaks = new LinkedHashSet<T>();
        // every peak is assigned to at most one clique!!!
        // reassignment is invalid and should not occur
        // for all files
        // file comparisons: k*(k-1)
        // per peak comparison: 2*l
        // check for clique membership: (k*l)
        for (IFileFragment iff : al) {
            final List<T> peaks = fragmentToPeaks.get(iff.getName());
            log.info("Checking {} peaks for file {}", peaks.size(),
                    iff.getName());
            // for all peaks in file

            // final List<T> bidiHits = new ArrayList<T>();
            // bidiHits.add(p);
            // for all other files
            for (final IFileFragment jff : al) {
                // only compare between partition matches, i!=j
                if (!iff.getName().equals(jff.getName())) {
                    for (final T p : peaks) {
                        // retrieve list of most similar peaks
                        final T q = (T) p.getPeakWithHighestSimilarity(jff.getName());
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
                        if (p.getSimilarity(q) == Double.NEGATIVE_INFINITY
                                || p.getSimilarity(q) == Double.POSITIVE_INFINITY) {
                            throw new IllegalArgumentException(
                                    "Infinite similarity value for associated peaks!");
                        }
                        // bidirectional hit
                        if (q != null && q.isBidiBestHitFor(p)) {
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
                            Clique c = null, d = null;
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
                                incompatiblePeaks.addAll(mergeCliques(c, d, peakToClique));
                            } else if (c != null && d == null) {
                                if (c.addPeak(q)) {
                                    peakToClique.put(q, c);
                                }
                            } else if (d != null && c == null) {
                                if (d.addPeak(p)) {
                                    peakToClique.put(p, d);
                                }
                            } else if (c == null && d == null) {
                                createNewClique(p, q, peakToClique);
                            } else if (c == d) {
                                if (c.addPeak(p)) {
                                    peakToClique.put(p, c);
                                }
                            } else {
                                log.error(
                                        "Unhandled case in if else! Missed a state?: c={} d={}, p={}, q={}",
                                        new Object[]{c, d, p, q});
                            }
                        } else {
                            log.debug(
                                    "T q:{} and p:{} are no bidirectional best hits!",
                                    p, q);
                        }
                    }
                }
            }
        }

//        if (incompatiblePeaks.size() > 0) {
        log.info("Found {} incompatible peaks.",
                incompatiblePeaks.size());
//        }
//        if (unassignedPeaks.size() > 0) {
        log.info("Found {} unassigned peaks.", unassignedPeaks.size());
//        }

        for (T p : incompatiblePeaks) {
            log.debug("Incompatible peak: " + p.toString());
        }

        for (T p : incompatiblePeaks) {
            p.clearSimilarities();
        }

        // retain all cliques, which exceed minimum size
        HashSet<Clique<T>> cliques = new HashSet<Clique<T>>();
        for (Clique<T> c : peakToClique.values()) {
            if (!cliques.contains(c)) {
                log.debug("Size of clique: {}\n{}",
                        c.getPeakList().size(), c);
                cliques.add(c);
            }
        }

        // sort cliques by clique rt mean
        List<Clique<T>> l = new ArrayList<Clique<T>>(cliques);
        Collections.sort(l, new Comparator<Clique<T>>() {
            @Override
            public int compare(Clique<T> o1, Clique<T> o2) {
                return o1.getCliqueStatistics().compareCliques(o1, o2);
            }
        });

        // add all remaining cliques to ll
        log.info("Minimum clique size: {}", minCliqueSize);
        ListIterator<Clique<T>> li = l.listIterator();
        while (li.hasNext()) {
            Clique<T> c = li.next();
            try {
                log.debug("Clique {}", c);
            } catch (NullPointerException npe) {
                log.debug("Clique empty?: {}", c.getPeakList());
            }
            if (c.getPeakList().size() >= minCliqueSize) {
                ll.add(c.getPeakList());
            } else {
                li.remove();
            }
        }

        log.info("Found {} cliques", l.size());
        return l;
    }

    /**
     * @param p
     * @param q
     */
    public void createNewClique(final T p, final T q, final HashMap<T, Clique<T>> peakToClique) {
        Clique<T> c;
        // assigned yet
        c = new Clique<T>();
        c.setMaxBBHErrors(this.maxBBHErrors);
        if (c.addPeak(p)) {
            peakToClique.put(p, c);
        }
        if (c.addPeak(q)) {
            peakToClique.put(q, c);
        }
    }

    /**
     * @param c
     * @param d
     * @return
     */
    public List<T> mergeCliques(Clique<T> c, Clique<T> d, final HashMap<T, Clique<T>> peakToClique) {
        int ds = d.getPeakList().size();
        int cs = c.getPeakList().size();
        //if either clique is empty, we can not merge,
        //so we can not have any incompatible peaks,
        //so we return an empty list
        if (ds == 0 || cs == 0) {
            return Collections.emptyList();
        }

        //start merging if both cliques have at least one peak in them
        log.debug("Merging cliques: c={}, d={}", c.toString(),
                d.toString());
        // ds has more peaks than cs -> join cs into
        // ds
        List<T> incompatiblePeaks = new LinkedList<T>();
        if (ds > cs) {
            for (T pk : c.getPeakList()) {
                if (d.addPeak(pk)) {
                    // c.removePeak(pk);
                    peakToClique.put(pk, d);
                } else {
                    incompatiblePeaks.add(pk);
                    log.debug("Adding of peak {} into clique {} failed", pk, d);
                }

            }
            log.debug("Clique {} has {} peaks left!", c, c.getPeakList().size());
            c.clear();
        } else {// ds has less peaks than cs -> join
            // ds into cs
            for (T pk : d.getPeakList()) {
                if (c.addPeak(pk)) {
                    // d.removePeak(pk);
                    peakToClique.put(pk, c);
                } else {
                    incompatiblePeaks.add(pk);
                    log.debug("Adding of peak {} into clique {} failed", pk, c);
                }

            }
            log.debug("Clique {} has {} peaks left!", d, d.getPeakList().size());
            d.clear();
        }
        return incompatiblePeaks;
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.STATISTICS;
    }

    @Override
    public void appendXML(Element e) {
    }
}
