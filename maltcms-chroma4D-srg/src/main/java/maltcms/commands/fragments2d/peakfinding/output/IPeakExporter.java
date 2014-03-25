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
package maltcms.commands.fragments2d.peakfinding.output;

import cross.IConfigurable;
import cross.datastructures.workflow.IWorkflowElement;
import java.awt.Point;
import java.util.List;
import maltcms.commands.fragments2d.peakfinding.Reliability;
import maltcms.commands.fragments2d.peakfinding.bbh.IBidirectionalBestHit;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.peak.Peak2D;

/**
 * Provides some methods to export relevant peak information.
 *
 * @author Mathias Wilhelm
 */
public interface IPeakExporter extends IConfigurable, IWorkflowElement {

    /**
     * Will export the information about the bidirectional best hits.
     *
     * @param bidiBestHitList bidirational best hits table
     * @param bbh bidirection best hits intance
     * @param names names of the chromtatograms
     * @param reliabilites reliability of peak groups
     */
    void exportBBHInformation(final List<List<Point>> bidiBestHitList, List<List<Peak2D>> peaklist,
            final IBidirectionalBestHit bbh, final List<String> names,
            final List<Reliability> reliabilities);

    /**
     * Will export the alignment of retention times about the bidirectional best
     * hits.
     *
     * @param bidiBestHitList bidirational best hits table
     * @param bbh bidirection best hits intance
     * @param names names of the chromtatograms
     * @param reliabilites reliability of peak groups
     */
    void exportBBHMultipleAlignmentRT(final List<List<Point>> bidiBestHitList, List<List<Peak2D>> peaklist,
            final IBidirectionalBestHit bbh, final List<String> names,
            final List<Reliability> reliabilities);

    /**
     * Will export a detailed list about the bidirectional best hits.
     *
     * @param bidiBestHitList bidirectional best hit list
     * @param peaklists peaklist
     * @param bbh bbh class - needed to run the sim() method
     * @param chromatogramNames names of the chromatograms
     * @param reliabilities reliabilities of peak groups
     * @param oFilename output filename
     */
    void exportDetailedBBHInformation(final List<List<Point>> bidiBestHitList,
            final List<List<Peak2D>> peaklists,
            final IBidirectionalBestHit bbh,
            final List<String> chromatogramNames,
            final List<Reliability> reliabilities, final String oFilename);

    /**
     * Will export some detailed peak information like unique mass integration,
     * ...
     *
     * @param filename filename
     * @param ps peaklist
     */
    void exportDetailedPeakInformation(final String filename,
            final List<Peak2D> ps);

    /**
     * Exports all given peaks as msp compatible format
     *
     * @param name peak name
     * @param ps peak list
     * @param isl instance of an scan line cache
     */
    void exportPeaksToMSP(final String name, final List<Peak2D> ps,
            final IScanLine isl);

    /**
     * Will export some peakinformation like retentiontime, name, ...
     *
     * @param filename filename
     * @param ps peaklist
     */
    void exportPeakInformation(final String filename, final List<Peak2D> ps);

    /**
     * Will export an peak occurrence map which will allow an easy plotting by
     * R. All Peaks will be exported in one list, but which additional
     * information about the occurrence, group id and some other.
     *
     * @param bidiBestHitList bidi best hit list from bbh
     * @param peaklists peaklist
     * @param bbh bbh instance
     * @param chromatogramNames chromatogram names
     * @param oFilename filename
     */
    void exportPeakOccurrenceMap(final List<List<Point>> bidiBestHitList,
            final List<List<Peak2D>> peaklists,
            final IBidirectionalBestHit bbh,
            final List<String> chromatogramNames, final String oFilename);

    /**
     * Exports all identifications available for a peak.
     *
     * @param peaklist peaklist
     * @param chomatogramName chromatogram name
     */
    void exportPeakNames(final List<Peak2D> peaklist, final String chomatogramName);

    /**
     * Setter.
     *
     * @param nCaller Creator class
     */
    @SuppressWarnings("unchecked")
    void setCaller(final Class nCaller);
}
