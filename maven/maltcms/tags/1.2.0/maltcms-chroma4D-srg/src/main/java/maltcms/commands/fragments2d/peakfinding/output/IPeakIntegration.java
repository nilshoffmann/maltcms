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
package maltcms.commands.fragments2d.peakfinding.output;

import java.util.List;

import ucar.ma2.Array;
import maltcms.datastructures.peak.Peak2D;
import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.workflow.IWorkflow;

/**
 * Provides some methods to do a peak integration.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public interface IPeakIntegration extends IConfigurable {

	/**
	 * Integrate the peak area and adds the sum to the PeakArea.
	 * 
	 * @param peak
	 *            peak
	 * @param ff
	 *            file fragment
	 * @param otic
	 *            tic for integration
	 * @param workflow
	 *            workflow
	 */
	void integrate(final Peak2D peak, final IFileFragment ff,
			final List<Array> otic, final IWorkflow workflow);

}
