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
package maltcms.commands.fragments.cluster;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import maltcms.datastructures.cluster.BinaryCluster;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;

/**
 * <p>IClusteringAlgorithm interface.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public interface IClusteringAlgorithm extends Iterable<IFileFragment> {

    /**
     * <p>apply.</p>
     *
     * @param t a {@link cross.datastructures.tuple.TupleND} object.
     * @return a {@link cross.datastructures.tuple.TupleND} object.
     */
    public abstract TupleND<IFileFragment> apply(TupleND<IFileFragment> t);

    /**
     * <p>getCluster.</p>
     *
     * @param i a int.
     * @return a {@link maltcms.datastructures.cluster.BinaryCluster} object.
     */
    public abstract BinaryCluster getCluster(int i);

    /**
     * <p>getClusters.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public abstract Set<Entry<Integer, BinaryCluster>> getClusters();

    /**
     * <p>getConsensus.</p>
     *
     * @return a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public abstract IFileFragment getConsensus();

    /**
     * <p>getFragments.</p>
     *
     * @return a {@link java.util.HashMap} object.
     */
    public abstract HashMap<Integer, IFileFragment> getFragments();

    /**
     * <p>getInputFiles.</p>
     *
     * @return a {@link cross.datastructures.tuple.TupleND} object.
     */
    public abstract TupleND<IFileFragment> getInputFiles();

    /**
     * <p>getNames.</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public abstract String[] getNames();

    /**
     * <p>init.</p>
     *
     * @param pwd a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param t a {@link cross.datastructures.tuple.TupleND} object.
     */
    public abstract void init(IFileFragment pwd, TupleND<IFileFragment> t);

    /*
     * (non-Javadoc)
     * 
     * @see
     * maltcms.commands.fragments.multiplealignment.ClusteringAlgorithm#merge()
     */
    /**
     * <p>merge.</p>
     */
    public abstract void merge();

    /**
     * <p>setConsensus.</p>
     *
     * @param f a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public abstract void setConsensus(IFileFragment f);

    /**
     * <p>setInputFiles.</p>
     *
     * @param t a {@link cross.datastructures.tuple.TupleND} object.
     */
    public abstract void setInputFiles(TupleND<IFileFragment> t);
}
