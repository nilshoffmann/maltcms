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
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.Peak;

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
    public List<Peak> findBiDiBestHits(final TupleND<IFileFragment> al,
            final HashMap<String, List<Peak>> fragmentToPeaks) {
        // For each pair of FileFragments
        final Set<Peak> matchedPeaks = new HashSet<Peak>();
        for (final Tuple2D<IFileFragment, IFileFragment> t : al.getPairs()) {

            final List<Peak> lhsPeaks = fragmentToPeaks.get(
                    t.getFirst().getName());
            final List<Peak> rhsPeaks = fragmentToPeaks.get(t.getSecond().
                    getName());
            log.debug("lhsPeaks: {}", lhsPeaks.size());
            log.debug("rhsPeaks: {}", rhsPeaks.size());
            for (final Peak plhs : lhsPeaks) {
                for (final Peak prhs : rhsPeaks) {
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
        List<Peak> unmatchedPeaks = new ArrayList<Peak>();
        for (final IFileFragment t : al) {
            final List<Peak> lhsPeaks = fragmentToPeaks.get(t.getName());
            log.debug("lhsPeaks: {}", lhsPeaks.size());
            ListIterator<Peak> liter = lhsPeaks.listIterator();
            while (liter.hasNext()) {
                final Peak plhs = liter.next();
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

    public boolean isBidiBestHitForAll(final List<Peak> peaks,
            final int numberOfFiles) {
        return isBidiBestHitForK(peaks, numberOfFiles, numberOfFiles);
    }

    public boolean isBidiBestHitForK(final List<Peak> peaks,
            final int numberOfFiles, final int minCliqueSize) {
        int i = 0;
        int j = 0;
        for (final Peak p : peaks) {
            for (final Peak q : peaks) {
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

    public boolean isFirstBidiBestHitForRest(final List<Peak> peaks,
            final int expectedHits) {
        int i = 0;
        final Peak p0 = peaks.get(0);
        for (final Peak p : peaks) {
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
            final HashMap<String, List<Peak>> fragmentToPeaks) {
        // no best hits means, that the corresponding list of sorted peaks has
        // length greater than one
        for (final String s : fragmentToPeaks.keySet()) {
            for (final Peak p : fragmentToPeaks.get(s)) {
                for (final IFileFragment iff : t) {
                    final List<Peak> l = p.getPeaksSortedBySimilarity(iff);
                    // clear similarities, if a best hit hasn't been assigned
                    if (l.size() > 1) {
                        log.debug("Clearing similarities for {} and {}",
                                iff.getName(), p);
                        p.clearSimilarities();
                    }
                }
            }
        }
    }
}
