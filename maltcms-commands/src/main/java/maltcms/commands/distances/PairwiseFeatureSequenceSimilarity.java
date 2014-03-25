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
package maltcms.commands.distances;

import cross.commands.ICommand;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.IWorkflowElement;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 * Interface to allow the values of comparison of multiple arrays to also be
 * used as a cost/score measure.
 *
 * @author Nils Hoffmann
 *
 */
public interface PairwiseFeatureSequenceSimilarity extends
        ICommand<Tuple2D<Array[], Array[]>, Array[]>, IWorkflowElement {

    /**
     *
     * @param a
     * @param b
     * @return
     */
    public abstract IFileFragment apply(IFileFragment a, IFileFragment b);

    /**
     *
     * @return
     */
    public abstract ArrayDouble.D0 getResult();

    /**
     *
     * @return
     */
    public abstract IFileFragment getResultFileFragment();

    /**
     *
     * @return
     */
    public abstract ArrayDouble.D1 getResultVector();

    /**
     *
     * @return
     */
    public abstract StatsMap getStatsMap();

    /**
     * Returns true, if this LDF is a distance between Arrays, false if LDF is a
     * similarity.
     *
     * @return
     */
    public abstract boolean minimize();

    /**
     *
     * @param sm
     */
    public abstract void setStatsMap(StatsMap sm);
}
