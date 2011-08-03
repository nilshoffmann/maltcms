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
 * $Id: RHClust.java 105 2010-03-10 11:15:53Z nilshoffmann $
 */
package maltcms.commands.fragments.cluster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import maltcms.datastructures.cluster.BinaryCluster;

import org.apache.commons.configuration.Configuration;

import cross.IConfigurable;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.exception.NotImplementedException;

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
    @Configurable(
    name = "maltcms.commands.fragments.cluster.RHClust.clusterMethod")
    private final String clusterMethod = ClusterMethod.COMPLETE.toString();

    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        throw new NotImplementedException();
    }

    @Override
    public void configure(final Configuration cfg) {
        cfg.getString(this.getClass().getName() + "." + this.clusterMethod,
                ClusterMethod.COMPLETE.toString());

    }

    @Override
    public BinaryCluster getCluster(final int i) {
        throw new NotImplementedException();
    }

    @Override
    public Set<Entry<Integer, BinaryCluster>> getClusters() {
        throw new NotImplementedException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * maltcms.commands.fragments.cluster.IClusteringAlgorithm#getConsensus()
     */
    @Override
    public IFileFragment getConsensus() {
        throw new NotImplementedException();
    }

    @Override
    public HashMap<Integer, IFileFragment> getFragments() {
        throw new NotImplementedException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * maltcms.commands.fragments.cluster.IClusteringAlgorithm#getInputFiles()
     */
    @Override
    public TupleND<IFileFragment> getInputFiles() {
        throw new NotImplementedException();
    }

    @Override
    public String[] getNames() {
        throw new NotImplementedException();
    }

    @Override
    public void init(final IFileFragment pwd, final TupleND<IFileFragment> t) {
        throw new NotImplementedException();
    }

    @Override
    public Iterator<IFileFragment> iterator() {
        throw new NotImplementedException();
    }

    @Override
    public void merge() {
        throw new NotImplementedException();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * maltcms.commands.fragments.cluster.IClusteringAlgorithm#setConsensus(
     * cross.datastructures.fragments.IFileFragment)
     */
    @Override
    public void setConsensus(final IFileFragment f) {
        throw new NotImplementedException();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * maltcms.commands.fragments.cluster.IClusteringAlgorithm#setInputFiles
     * (cross.datastructures.tuple.TupleND)
     */
    @Override
    public void setInputFiles(final TupleND<IFileFragment> t) {
        throw new NotImplementedException();

    }
}
