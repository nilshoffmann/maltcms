/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
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

package maltcms.commands.distances;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.commands.ICommand;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.IWorkflowElement;

/**
 * Interface to allow the values of comparison of multiple arrays to also be
 * used as a cost/score measure.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface ListDistanceFunction extends
        ICommand<Tuple2D<Array[], Array[]>, Array[]>, IWorkflowElement {

	public abstract IFileFragment apply(IFileFragment a, IFileFragment b);

	public abstract ArrayDouble.D0 getResult();

	public abstract IFileFragment getResultFileFragment();

	public abstract ArrayDouble.D1 getResultV();

	public abstract StatsMap getStatsMap();

	/**
	 * Returns true, if this LDF is a distance between Arrays, false if LDF is a
	 * similarity.
	 * 
	 * @return
	 */
	public abstract boolean minimize();

	public abstract void setStatsMap(StatsMap sm);

	// public abstract Tuple2D<Array[],Array[]>
	// prepareInput(Tuple2D<List<VariableFragment>,List<VariableFragment>> t);

}
