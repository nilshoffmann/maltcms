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

package maltcms.commands.fragments.cluster;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import maltcms.datastructures.cluster.BinaryCluster;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;

public interface IClusteringAlgorithm extends Iterable<IFileFragment> {

	public abstract TupleND<IFileFragment> apply(TupleND<IFileFragment> t);

	public abstract BinaryCluster getCluster(int i);

	public abstract Set<Entry<Integer, BinaryCluster>> getClusters();

	public abstract IFileFragment getConsensus();

	public abstract HashMap<Integer, IFileFragment> getFragments();

	public abstract TupleND<IFileFragment> getInputFiles();

	public abstract String[] getNames();

	public abstract void init(IFileFragment pwd, TupleND<IFileFragment> t);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.commands.fragments.multiplealignment.ClusteringAlgorithm#merge()
	 */
	public abstract void merge();

	public abstract void setConsensus(IFileFragment f);

	public abstract void setInputFiles(TupleND<IFileFragment> t);

}
