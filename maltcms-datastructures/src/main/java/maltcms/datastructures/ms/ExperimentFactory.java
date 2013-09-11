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
package maltcms.datastructures.ms;

import cross.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;

/**
 * Factory to create Experiments.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
public class ExperimentFactory {

    /**
     * Create an instance of Experiment1D.
     *
     * @param ff
     * @return
     */
    public static IExperiment createExperiment(final IFileFragment ff) {
        return ExperimentFactory.createExperiment1D(ff);
    }

    /**
     * Create an instance of IExperiment1D and initialize with IFileFragment.
     *
     * @param ff
     * @param c
     * @return null if an Exception was caught, else new instance of IExperiment
     */
    public static IExperiment1D createExperiment1D(final IFileFragment ff) {
        final Class<Experiment1D> c = Experiment1D.class;
        final Experiment1D ie = Factory.getInstance().getObjectFactory()
                .instantiate(c);
        EvalTools.notNull(ie, ExperimentFactory.class);
        ie.setFileFragment(ff);
        return ie;
    }

    /**
     * Create an instance of IExperiment2D and initialize with IFileFragment.
     *
     * @param ff
     * @param c
     * @return null if an Exception was caught, else new instance of IExperiment
     */
    public static IExperiment2D createExperiment2D(final IFileFragment ff) {
        final Class<Experiment2D> c = Experiment2D.class;
        final Experiment2D ie = Factory.getInstance().getObjectFactory()
                .instantiate(c);
        EvalTools.notNull(ie, ExperimentFactory.class);
        ie.setFileFragment(ff);
        return ie;
    }
}
