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
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.IBipacePeak;
import maltcms.datastructures.peak.IPeak;

/**
 *
 * @author nils
 */
@Slf4j
public class BBHFinder {

    /**
     * @param al
     * @param fragmentToPeaks
     */
    public List<IBipacePeak> findBiDiBestHits(final TupleND<IFileFragment> al,
            final HashMap<String, List<IBipacePeak>> fragmentToPeaks) {
        // For each pair of FileFragments
        final Set<IPeak> matchedPeaks = new HashSet<IPeak>();
        for (final Tuple2D<IFileFragment, IFileFragment> t : al.getPairs()) {

            final List<IBipacePeak> lhsPeaks = fragmentToPeaks.get(
                    t.getFirst().getName());
            final List<IBipacePeak> rhsPeaks = fragmentToPeaks.get(t.getSecond().
                    getName());
            log.debug("lhsPeaks: {}", lhsPeaks.size());
            log.debug("rhsPeaks: {}", rhsPeaks.size());
            for (final IBipacePeak plhs : lhsPeaks) {
                for (final IBipacePeak prhs : rhsPeaks) {
                    log.debug("Checking peaks {} and {}", plhs, prhs);
                    if (plhs.isBidiBestHitFor(prhs)) {
                        log.debug(
                                "Found a bidirectional best hit: {} and {}",
                                plhs, prhs);
                        matchedPeaks.add(plhs);
                        matchedPeaks.add(prhs);
                        prhs.retainSimilarityRemoveRest(plhs);
                        plhs.retainSimilarityRemoveRest(prhs);
                    }

                }
            }
        }

        log.info("Retained {} matched peaks!", matchedPeaks.size());
        log.debug("Counting and removing unmatched peaks!");
        int peaks = 0;
        List<IBipacePeak> unmatchedPeaks = new ArrayList<IBipacePeak>();
        for (final IFileFragment t : al) {
            final List<IBipacePeak> lhsPeaks = fragmentToPeaks.get(t.getName());
            log.debug("lhsPeaks: {}", lhsPeaks.size());
            ListIterator<IBipacePeak> liter = lhsPeaks.listIterator();
            while (liter.hasNext()) {
                final IBipacePeak plhs = liter.next();
                if (!matchedPeaks.contains(plhs)) {
                    unmatchedPeaks.add(plhs);
                    peaks++;
                    liter.remove();
                }
            }
        }
        log.info("Removed {} unmatched peaks!", peaks);
        return unmatchedPeaks;
    }

    public boolean isBidiBestHitForAll(final List<IBipacePeak> peaks,
            final int numberOfFiles) {
        return isBidiBestHitForK(peaks, numberOfFiles, numberOfFiles);
    }

    public boolean isBidiBestHitForK(final List<IBipacePeak> peaks,
            final int numberOfFiles, final int minCliqueSize) {
        int i = 0;
        int j = 0;
        for (final IBipacePeak p : peaks) {
            for (final IBipacePeak q : peaks) {
                if (!p.equals(q)) {
                    if (q.isBidiBestHitFor(p)) {
                        i++;
                    } else {
                    }
                    j++;
                }
            }
        }

        if ((minCliqueSize < 2) && (minCliqueSize >= -1)) {
            log.info(
                    "Illegal value for minCliqueSize = {}, allowed values are -1, >=2 <= number of chromatograms",
                    minCliqueSize);
        }
        if (i >= minCliqueSize) {
            log.debug(
                    "{} are BidiBestHits of each other: {}", i, peaks);
            return true;
        }
        return false;
    }

    public boolean isFirstBidiBestHitForRest(final List<IBipacePeak> peaks,
            final int expectedHits) {
        int i = 0;
        final IBipacePeak p0 = peaks.get(0);
        for (final IBipacePeak p : peaks) {
            // for(Peak q:peaks) {
            if (!p.equals(p0)) {
                if (p0.isBidiBestHitFor(p)) {
                    i++;
                } else {
                }
            }
            // }
        }
        if (i == expectedHits) {
            log.debug(
                    "All elements are BidiBestHits to first Peak: {}", peaks);
            return true;
        }
        return false;
    }

    public void removePeakSimilaritiesWhichHaveNoBestHits(
            final TupleND<IFileFragment> t,
            final HashMap<String, List<IBipacePeak>> fragmentToPeaks) {
        // no best hits means, that the corresponding list of sorted peaks has
        // length greater than one
        for (final String s : fragmentToPeaks.keySet()) {
            for (final IBipacePeak p : fragmentToPeaks.get(s)) {
                for (final IFileFragment iff : t) {
                    final List<UUID> l = p.getPeaksSortedBySimilarity(iff.getName());
                    // clear similarities, if a best hit hasn't been assigned
                    if (l.size() > 1) {
                        log.debug("Clearing similarities for {} and {}",
                                iff.getName(), p);
                        p.clearSimilarities(iff.getName());
                    }
                }
            }
        }
    }
}
