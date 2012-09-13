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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
public class TreatmentGroup1D implements ITreatmentGroup<IChromatogram1D> {

    List<IChromatogram1D> l = new ArrayList<IChromatogram1D>();
    private String name = "";

    /*
     * (non-Javadoc)
     * 
     * @seemaltcms.datastructures.ms.ITreatmentGroup#addChromatogram(maltcms.
     * datastructures.ms.IChromatogram)
     */
    @Override
    public void addChromatogram(IChromatogram1D t) {
        this.l.add(t);
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.datastructures.ms.ITreatmentGroup#getChromatograms()
     */
    @Override
    public List<IChromatogram1D> getChromatograms() {
        return this.l;
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.datastructures.ms.ITreatmentGroup#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.datastructures.ms.ITreatmentGroup#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }
}
