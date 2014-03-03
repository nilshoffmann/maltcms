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

import cross.annotations.RequiresVariables;
import cross.datastructures.fragments.IFileFragment;
import cross.exception.ResourceNotAvailableException;
import java.util.List;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;

@RequiresVariables(names = {"var.binned_mass_values", "var.binned_intensity_values", "var.binned_scan_index"})
public class ProfileChromatogram1D extends Chromatogram1D {

    /**
     * Create a profile chromatogram from a given file fragment.
     * Requires var.binned_scan_index, var.binned_intensity_values,
     * and var.binned_mass_values to be available.
     *
     * @param f
     */
    public ProfileChromatogram1D(IFileFragment f) {
        this(f, false);
    }

    /**
     * Create a profile chromatogram from a given file fragment.
     * Requires var.binned_scan_index, var.binned_intensity_values,
     * and var.binned_mass_values to be available.
     *
     * @param f
     * @throws ResourceNotAvailableException if any of the required variables is not available
     */
    public ProfileChromatogram1D(IFileFragment f, boolean checkVariableAvailability) {
        super(f);
        if (checkVariableAvailability) {
            //FIXME use CVResolver
            f.getChild("binned_scan_index", true);
            f.getChild("binned_mass_values", true);
            f.getChild("binned_intensity_values", true);
        }
    }

    /**
     *
     * @return
     * @throws ResourceNotAvailableException if any of the required variables is not available
     */
    public List<Array> getBinnedIntensities() {
        return MaltcmsTools.getBinnedMZIs(getParent()).getSecond();
    }

    /**
     *
     * @return
     * @throws ResourceNotAvailableException if any of the required variables is not available
     */
    public List<Array> getBinnedMasses() {
        return MaltcmsTools.getBinnedMZIs(getParent()).getFirst();
    }
}
