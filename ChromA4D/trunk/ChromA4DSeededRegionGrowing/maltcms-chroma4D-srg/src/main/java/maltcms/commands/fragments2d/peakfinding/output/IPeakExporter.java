/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: IPeakExporter.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */
package maltcms.commands.fragments2d.peakfinding.output;

import java.awt.Point;
import java.util.List;

import maltcms.commands.fragments2d.peakfinding.Reliability;
import maltcms.commands.fragments2d.peakfinding.bbh.IBidirectionalBestHit;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.peak.Peak2D;
import cross.IConfigurable;
import cross.datastructures.workflow.IWorkflow;

/**
 * Provides some methods to export relevant peak information.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public interface IPeakExporter extends IConfigurable {

	/**
	 * Will export the information about the bidirectional best hits.
	 * 
	 * @param bidiBestHitList
	 *            bidirational best hits table
	 * @param bbh
	 *            bidirection best hits intance
	 * @param names
	 *            names of the chromtatograms
	 * @param reliabilites
	 *            reliability of peak groups
	 */
	void exportBBHInformation(final List<List<Point>> bidiBestHitList, List<List<Peak2D>> peaklist,
			final IBidirectionalBestHit bbh, final List<String> names,
			final List<Reliability> reliabilities);

        /**
	 * Will export the alignment of retention times about the bidirectional best hits.
	 *
	 * @param bidiBestHitList
	 *            bidirational best hits table
	 * @param bbh
	 *            bidirection best hits intance
	 * @param names
	 *            names of the chromtatograms
	 * @param reliabilites
	 *            reliability of peak groups
	 */
	void exportBBHMultipleAlignmentRT(final List<List<Point>> bidiBestHitList, List<List<Peak2D>> peaklist,
			final IBidirectionalBestHit bbh, final List<String> names,
			final List<Reliability> reliabilities);

	/**
	 * Will export a detailed list about the bidirectional best hits.
	 * 
	 * @param bidiBestHitList
	 *            bidirectional best hit list
	 * @param peaklists
	 *            peaklist
	 * @param bbh
	 *            bbh class - needed to run the sim() method
	 * @param chromatogramNames
	 *            names of the chromatograms
	 * @param reliabilities
	 *            reliabilities of peak groups
	 * @param oFilename
	 *            output filename
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
	 * @param filename
	 *            filename
	 * @param ps
	 *            peaklist
	 */
	void exportDetailedPeakInformation(final String filename,
			final List<Peak2D> ps);

	/**
	 * Exports all given peaks as msp compatible format
	 * 
	 * @param name
	 *            peak name
	 * @param ps
	 *            peak list
	 * @param isl
	 *            instance of an scan line cache
	 */
	void exportPeaksToMSP(final String name, final List<Peak2D> ps,
			final IScanLine isl);

	/**
	 * Will export some peakinformation like retentiontime, name, ...
	 * 
	 * @param filename
	 *            filename
	 * @param ps
	 *            peaklist
	 */
	void exportPeakInformation(final String filename, final List<Peak2D> ps);

	/**
	 * Will export an peak occurrence map which will allow an easy plotting by
	 * R. All Peaks will be exported in one list, but which additional
	 * information about the occurrence, group id and some other.
	 * 
	 * @param bidiBestHitList
	 *            bidi best hit list from bbh
	 * @param peaklists
	 *            peaklist
	 * @param bbh
	 *            bbh instance
	 * @param chromatogramNames
	 *            chromatogram names
	 * @param oFilename
	 *            filename
	 */
	void exportPeakOccurrenceMap(final List<List<Point>> bidiBestHitList,
			final List<List<Peak2D>> peaklists,
			final IBidirectionalBestHit bbh,
			final List<String> chromatogramNames, final String oFilename);

        /**
         * Exports all identifications available for a peak.
         * 
         * @param peaklist
         *          peaklist
         * @param chomatogramName
         *          chromatogram name
         */
        void exportPeakNames(final List<Peak2D> peaklist, final String chomatogramName);

	/**
	 * Setter.
	 * 
	 * @param nCaller
	 *            Creator class
	 */
	@SuppressWarnings("unchecked")
	void setCaller(final Class nCaller);

	/**
	 * Setter.
	 * 
	 * @param workflow
	 *            {@link IWorkflow}
	 */
	void setIWorkflow(final IWorkflow workflow);

}
