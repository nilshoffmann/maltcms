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
package maltcms.commands.fragments.alignment.peakCliqueAlignment2;

import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ResourceNotAvailableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.PeakComparator;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.peakFactory.IPeakFactory;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.peakFactory.IPeakFactoryImpl;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.peakFactory.Peak1DMSFactory;
import maltcms.datastructures.peak.Peak;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.Index;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
public class PeakLoader implements IPeakLoader {

    @Configurable(name = "var.tic_peaks")
    private String ticPeaks = "tic_peaks";
    @Configurable(name = "var.retention_index_names")
    private String anchorNames = "retention_index_names";
    @Configurable(name = "var.retention_times")
    private String anchorTimes = "retention_times";
    @Configurable(name = "var.retention_indices")
    private String anchorRetentionIndex = "retention_indices";
    @Configurable(name = "var.retention_scans")
    private String anchorScanIndex = "retention_scans";
    @Configurable(name = "var.binned_intensity_values")
    private String binnedIntensities = "binned_intensity_values";
    @Configurable(name = "var.binned_scan_index")
    private String binnedScanIndex = "binned_scan_index";
    @Configurable(name = "var.scan_acquisition_time")
    private String scanAcquisitionTime = "scan_acquisition_time";
    @Configurable
    private boolean useUserSuppliedAnchors = false;
    @Configurable
    private boolean savePeakSimilarities = false;
    @Configurable
    private boolean useSparseArrays = false;
    @Configurable
    private IPeakFactory peakFactory = new Peak1DMSFactory();
    @Configurable
    private double massBinResolution = 1.0d;

    /**
     *
     * @param t
     */
    @Override
    public List<Peak> loadPeaks(IFileFragment t) {
        int npeaks = 0;
        List<Peak> definedAnchors = Collections.emptyList();
        if (this.useUserSuppliedAnchors) {
            log.debug("Checking for user-supplied anchors!");
            definedAnchors = checkUserSuppliedAnchors(t);
        }
        log.debug("{}", definedAnchors.toString());
        Tuple2D<Double, Double> minMaxMassRange = MaltcmsTools.getMinMaxMassRange(t);
        final int size = MaltcmsTools.getNumberOfIntegerMassBins(minMaxMassRange.getFirst(),
                minMaxMassRange.getSecond(), massBinResolution);
        if (useSparseArrays) {
            log.info("Using sparse arrays!");
        }
        // Insert Peaks into HashMap
        // if we have a valid variable defined,
        // use those peak indices
        Array peakCandidates1;
        try {
            IVariableFragment peakCandidates = t.getChild(this.ticPeaks);
            peakCandidates1 = peakCandidates.getArray();
            log.debug("Peaks for file {}: {}", t.getUri(),
                    peakCandidates1);
        } catch (ResourceNotAvailableException rnae) {
            // otherwise, create an index array for all scans!!!
            Array sidx = t.getChild(this.binnedScanIndex).getArray();
            peakCandidates1 = ArrayTools.indexArray(sidx.getShape()[0], 0);
        }
        EvalTools.notNull(peakCandidates1, this);
        List<Peak> peaks = new ArrayList<Peak>();
        log.debug("Adding peaks for {}", t.getName());
        final List<Peak> userDefinedAnchors = definedAnchors;
        IPeakFactoryImpl pfi = peakFactory.createInstance(t, savePeakSimilarities, minMaxMassRange, size, massBinResolution, useSparseArrays, savePeakSimilarities);

        for (int i = 0; i < peakCandidates1.getShape()[0]; i++) {
            final int pc1i = peakCandidates1.getInt(i);
            Peak p = pfi.create(i, pc1i);
            peaks.add(p);
            npeaks++;
        }

        if ((!userDefinedAnchors.isEmpty()) && this.useUserSuppliedAnchors) {
            log.info("Using user-defined anchors!");
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
        log.debug("Loaded {} peaks for file {}",npeaks,t.getName());
        return peaks;
    }

    /**
     *
     * @param t
     * @return
     */
    protected List<Peak> checkUserSuppliedAnchors(
            final IFileFragment t) {
        // Check for already defined peaks
        List<Peak> peaks = Collections.emptyList();
        IVariableFragment anames = null;
        IVariableFragment ascans = null;
        try {
            anames = t.getChild(this.anchorNames);
            ascans = t.getChild(this.anchorScanIndex);
            final ArrayChar.D2 peakNames = (ArrayChar.D2) anames.getArray();
            final Array peakScans = ascans.getArray();
            final Index peakScansI = peakScans.getIndex();

            t.getChild(this.binnedIntensities).setIndex(
                    t.getChild(this.binnedScanIndex));
            log.info("Checking user supplied anchors for: {}", t);
            final Array scan_acquisition_time = t.getChild(
                    this.scanAcquisitionTime).getArray();
            peaks = new ArrayList<Peak>();
            final Index sat1 = scan_acquisition_time.getIndex();
            final List<Array> bintens = t.getChild(
                    this.binnedIntensities).getIndexedArray();
            for (int i = 0; i < peakScans.getShape()[0]; i++) {
                final String name = peakNames.getString(i);
                final int scan = peakScans.getInt(peakScansI.set(i));
                final double sat = scan_acquisition_time.getDouble(sat1.set(
                        scan));
                log.debug("{}", t.getName());
                final Peak p = new Peak(scan, bintens.get(scan),
                        sat, t.getName(), this.savePeakSimilarities);
                p.setName(name);
                log.debug(
                        "Adding user supplied anchor {} with name {}", p,
                        p.getName());
                peaks.add(p);
            }
        } catch (final ResourceNotAvailableException rne) {
            log.debug("Could not find any user-defined anchors!");
        }
        return peaks;
    }
}
