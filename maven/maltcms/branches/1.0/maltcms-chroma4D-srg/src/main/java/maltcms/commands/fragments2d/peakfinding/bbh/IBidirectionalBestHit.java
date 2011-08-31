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
 * $Id: IBidirectionalBestHit.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */
package maltcms.commands.fragments2d.peakfinding.bbh;

import java.awt.Point;
import java.util.List;

import maltcms.commands.fragments2d.peakfinding.SeededRegionGrowing;
import maltcms.datastructures.peak.Peak2D;
import cross.IConfigurable;

/**
 * Interface for an BBH algorithm used by {@link SeededRegionGrowing}.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public interface IBidirectionalBestHit extends IConfigurable {

	/**
	 * Getter.
	 * 
	 * 
	 * @param peaklists
	 *            list of peak list for all chromatograms
	 * @return a list of all bidirectional best hits. List contains the indices
	 *         of peak in the peaklist.
	 */
	List<List<Point>> getBidiBestHitList(List<List<Peak2D>> peaklists);

	double sim(Peak2D p1, Peak2D p2);

	void clear();

}
