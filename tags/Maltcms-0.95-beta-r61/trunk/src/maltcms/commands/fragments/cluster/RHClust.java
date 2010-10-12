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
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import maltcms.datastructures.cluster.BinaryCluster;

import org.apache.commons.configuration.Configuration;

import annotations.Configurable;
import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;

public class RHClust implements IClusteringAlgorithm, IConfigurable {

	public enum ClusterMethod {
		WARD("ward"), SINGLE("single"), COMPLETE("complete"), AVERAGE("average"), MCQUITTY(
		        "mcquitty"), MEDIAN("median"), CENTROID("centroid");

		public static ClusterMethod fromString(final String s) {
			if (s.equals("ward")) {
				return WARD;
			} else if (s.equals("single")) {

			} else if (s.equals("complete")) {

			} else if (s.equals("average")) {

			} else if (s.equals("mcquitty")) {

			} else if (s.equals("median")) {

			} else if (s.equals("centroid")) {

			} else {
				throw new IllegalArgumentException("Unknown cluster method: "
				        + s);
			}
			return COMPLETE;
		}

		private String name = "complete";

		ClusterMethod(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	@Configurable(name = "maltcms.commands.fragments.cluster.RHClust.clusterMethod")
	private final String clusterMethod = ClusterMethod.COMPLETE.toString();

	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void configure(final Configuration cfg) {
		cfg.getString(this.getClass().getName() + "." + this.clusterMethod,
		        ClusterMethod.COMPLETE.toString());

	}

	@Override
	public BinaryCluster getCluster(final int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Entry<Integer, BinaryCluster>> getClusters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<Integer, IFileFragment> getFragments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(final IFileFragment pwd, final TupleND<IFileFragment> t) {

	}

	@Override
	public Iterator<IFileFragment> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void merge() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.commands.fragments.cluster.IClusteringAlgorithm#getConsensus()
	 */
	@Override
	public IFileFragment getConsensus() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.commands.fragments.cluster.IClusteringAlgorithm#getInputFiles()
	 */
	@Override
	public TupleND<IFileFragment> getInputFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.commands.fragments.cluster.IClusteringAlgorithm#setConsensus(
	 * cross.datastructures.fragments.IFileFragment)
	 */
	@Override
	public void setConsensus(IFileFragment f) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.commands.fragments.cluster.IClusteringAlgorithm#setInputFiles
	 * (cross.datastructures.tuple.TupleND)
	 */
	@Override
	public void setInputFiles(TupleND<IFileFragment> t) {
		// TODO Auto-generated method stub

	}

}
