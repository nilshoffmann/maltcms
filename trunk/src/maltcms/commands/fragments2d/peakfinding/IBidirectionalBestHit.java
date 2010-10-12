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
 * $Id$
 */
package maltcms.commands.fragments2d.peakfinding;

import java.awt.Point;
import java.util.List;

import maltcms.datastructures.peak.Peak2D;
import cross.IConfigurable;

/**
 * Interface for an BBH algorithm used by {@link SeededRegionGrowing}.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public interface IBidirectionalBestHit extends IConfigurable {

	/**
	 * Adds a peak list to a internal peak list.
	 * 
	 * @param peakList
	 *            peak list
	 */
	void addPeakLists(final List<Peak2D> peakList);

	/**
	 * Getter.
	 * 
	 * @return a list of all bidirectional best hits. List contains the indices
	 *         of peak in the peaklist.
	 */
	List<List<Point>> getBidiBestHitList();

	/**
	 * Getter.
	 * 
	 * @return peak list
	 */
	List<List<Peak2D>> getPeakLists();

	double sim(Peak2D p1, Peak2D p2);

}