/*
 * $license$
 *
 * $Id$
 */
package maltcms.commands.fragments.alignment2;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.TupleND;
import cross.exception.ResourceNotAvailableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.Peak;
import maltcms.experimental.bipace.peakCliqueAlignment.PeakComparator;
import maltcms.tools.ArrayTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.Index;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
public class Peak1DProvider implements IPeakProvider<Peak> {

    private boolean useUserSuppliedAnchors = false;
    private boolean keepBestSimilaritiesOnly = true;
    private String anchorNames;
    private String anchorTimes;
    private String anchorRetentionIndex;
    private String anchorScanIndex;
    private String ticPeaks;
    private String binnedIntensities;
    private String binnedScanIndex;
    private String scanIndex;
    private String massValues;
    private String intensityValues;
    private String scanAcquisitionTime;

    @Override
    public List<Peak> getPeaks(IFileFragment t) {
        // if we have a valid variable defined,
        // use those peak indices
        Array peakCandidates1;
        try {
            IVariableFragment peakCandidates = t.getChild(this.ticPeaks);
            peakCandidates1 = peakCandidates.getArray();
            log.info("Using defined tic peaks for file {}", t.getAbsolutePath());
        } catch (ResourceNotAvailableException rnae) {
            // otherwise, create an index array for all scans!!!
            Array sidx = t.getChild(this.binnedScanIndex).getArray();
            peakCandidates1 = ArrayTools.indexArray(sidx.getShape()[0], 0);
            log.info("Using all mass spectra for file {}", t.getAbsolutePath());
        }

        // maxPeaks = Math.max(maxPeaks, peakCandidates1.getShape()[0]);
        t.getChild(this.binnedIntensities).setIndex(
                t.getChild(this.binnedScanIndex));
        final List<Array> bintens = t.getChild(
                this.binnedIntensities).getIndexedArray();
        final Array scan_acquisition_time = t.getChild(
                this.scanAcquisitionTime).getArray();
        EvalTools.notNull(peakCandidates1, this);
        List<Peak> peaks = new ArrayList<Peak>();

        log.debug("Adding peaks for {}", t.getName());
        final Index pc1 = peakCandidates1.getIndex();
        final Index sat1 = scan_acquisition_time.getIndex();
        for (int i = 0; i < peakCandidates1.getShape()[0]; i++) {
            final int pc1i = peakCandidates1.getInt(pc1.set(i));
            final Peak p = new Peak("", t, pc1i, bintens.get(pc1i),
                    scan_acquisition_time.getDouble(sat1.set(pc1i)));
            p.setStoreOnlyBestSimilarities(keepBestSimilaritiesOnly);
            peaks.add(p);
        }
        return peaks;
    }

    /**
     * FIXME add support for 1D only chromatograms -> see TICDynamicTimeWarp for
     * that
     *
     * @param al
     * @return
     */
    public HashMap<String, List<Peak>> checkUserSuppliedAnchors(
            final TupleND<IFileFragment> al) {
        // Check for already defined peaks
        final HashMap<String, List<Peak>> definedAnchors = new HashMap<String, List<Peak>>();
        for (final IFileFragment t : al) {
            try {
                List<Peak> peaks = getAnchors(t);
                definedAnchors.put(t.getName(), peaks);
            } catch (final ResourceNotAvailableException rne) {
                log.debug("Could not find any user-defined anchors!");
                definedAnchors.put(t.getName(), new ArrayList<Peak>(0));
            }
        }
        return definedAnchors;
    }

    private List<Peak> getAnchors(final IFileFragment t) throws ResourceNotAvailableException {
        IVariableFragment anames = t.getChild(this.anchorNames);
        IVariableFragment ascans = t.getChild(this.anchorScanIndex);
        final ArrayChar.D2 peakNames = (ArrayChar.D2) anames.getArray();
        final Array peakScans = ascans.getArray();
        final Index peakScansI = peakScans.getIndex();
        t.getChild(this.binnedIntensities).setIndex(
                t.getChild(this.binnedScanIndex));
        log.info("Checking user supplied anchors for: {}", t);
        final Array scan_acquisition_time = t.getChild(
                this.scanAcquisitionTime).getArray();
        List<Peak> peaks = new ArrayList<Peak>();
        final Index sat1 = scan_acquisition_time.getIndex();
        final List<Array> bintens = t.getChild(
                this.binnedIntensities).getIndexedArray();
        for (int i = 0; i < peakScans.getShape()[0]; i++) {
            final String name = peakNames.getString(i);
            final int scan = peakScans.getInt(peakScansI.set(i));
            final double sat = scan_acquisition_time.getDouble(sat1.set(
                    scan));
            log.debug("{}", t.getName());
            final Peak p = new Peak(name, t, scan, bintens.get(scan),
                    sat);
            p.setStoreOnlyBestSimilarities(keepBestSimilaritiesOnly);
            log.debug(
                    "Adding user supplied anchor {} with name {}", p,
                    p.getName());
            peaks.add(p);
        }
        return peaks;
    }

    /**
     *
     * @param al
     * @param fragmentToPeaks
     * @param peakFinderFragments
     * @param columnMap
     */
    public void initializePeaks(
            final TupleND<IFileFragment> originalFileFragments,
            final HashMap<String, List<Peak>> fragmentToPeaks,
            final HashMap<String, Integer> columnMap) {
        int column = 0;
        // int maxPeaks = Integer.MIN_VALUE;
        int npeaks = 0;
        HashMap<String, List<Peak>> definedAnchors = new HashMap<String, List<Peak>>();
        if (this.useUserSuppliedAnchors) {
            definedAnchors = checkUserSuppliedAnchors(originalFileFragments);
            for (final IFileFragment iff : originalFileFragments) {
                definedAnchors.put(iff.getName(), new ArrayList<Peak>(0));
            }
        }
        log.debug("{}", definedAnchors.toString());
        // Insert Peaks into HashMap
        for (final IFileFragment t : originalFileFragments) {

            List<Peak> peaks = null;
            if (fragmentToPeaks.containsKey(t.getName())) {
                peaks = fragmentToPeaks.get(t.getName());
            } else {
                peaks = getPeaks(t);
            }
            npeaks += peaks.size();
            //check for present anchors
            final List<Peak> userDefinedAnchors = definedAnchors.get(t.getName());
            if ((userDefinedAnchors != null) && this.useUserSuppliedAnchors) {
                for (final Peak p : userDefinedAnchors) {

                    final int n = Collections.binarySearch(peaks, p,
                            new PeakComparator());
                    if (n >= 0) {// if found in list, remove and add to anchors
                        final Peak q = peaks.get(n);
                        q.setName(p.getName());
                        log.debug("{} with name {} annotated by user!", p,
                                p.getName());
                    } else {// else add at proposed insert position
                        log.debug("Adding peak at position {}", n);
                        peaks.add(((-1) * n) - 1, p);
                    }
                }
            }

            //map fragment name to peaks
            fragmentToPeaks.put(t.getName(), peaks);
            //map fragment name to a column index
            columnMap.put(t.getName(), column++);
            // clearing space
            t.clearArrays();
        }
        log.debug("{} peaks present", npeaks);
    }
}
