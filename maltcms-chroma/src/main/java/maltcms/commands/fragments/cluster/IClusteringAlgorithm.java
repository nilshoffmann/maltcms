/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
