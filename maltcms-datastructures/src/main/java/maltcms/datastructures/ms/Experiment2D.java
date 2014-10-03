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
package maltcms.datastructures.ms;

import cross.datastructures.fragments.IFileFragment;

/**
 * Concrete Implementation containing a 2-dimensional chromatogram, e.g. from
 * GCxGC-MS.
 *
 * @author Nils Hoffmann
 * 
 */
public class Experiment2D extends Experiment1D implements IExperiment2D {

    /**
     * <p>Constructor for Experiment2D.</p>
     */
    public Experiment2D() {
        super();
    }

    /**
     * <p>Constructor for Experiment2D.</p>
     *
     * @param ff1 a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public Experiment2D(IFileFragment ff1) {
        super(ff1);
    }
    private IChromatogram2D ic2d = null;

    /** {@inheritDoc} */
    @Override
    public IChromatogram2D getChromatogram2D() {
        return this.ic2d;
    }

    /** {@inheritDoc} */
    @Override
    public void setChromatogram2D(final IChromatogram2D ic) {
        this.ic2d = ic;
        setChromatogram(new Chromatogram1D(ic.getParent()));
    }
}
