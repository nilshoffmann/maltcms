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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>TreatmentGroup2D class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class TreatmentGroup2D implements ITreatmentGroup<IChromatogram2D> {

    List<IChromatogram2D> l = new ArrayList<>();
    private String name = "";

    /*
     * (non-Javadoc)
     * 
     * @seemaltcms.datastructures.ms.ITreatmentGroup#addChromatogram(maltcms.
     * datastructures.ms.IChromatogram)
     */
    /** {@inheritDoc} */
    @Override
    public void addChromatogram(IChromatogram2D t) {
        this.l.add(t);
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.datastructures.ms.ITreatmentGroup#getChromatograms()
     */
    /** {@inheritDoc} */
    @Override
    public List<IChromatogram2D> getChromatograms() {
        return this.l;
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.datastructures.ms.ITreatmentGroup#getName()
     */
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.datastructures.ms.ITreatmentGroup#setName(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        this.name = name;
    }
}
