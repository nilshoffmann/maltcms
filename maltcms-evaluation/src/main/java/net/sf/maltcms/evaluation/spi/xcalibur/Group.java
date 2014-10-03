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
package net.sf.maltcms.evaluation.spi.xcalibur;

import java.util.List;

/**
 * <p>Group class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class Group {

    private final String name;
    private List<Chromatogram> chromatograms;

    /**
     * <p>Constructor for Group.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param chromatograms a {@link java.util.List} object.
     */
    public Group(String name, List<Chromatogram> chromatograms) {
        this.name = name;
        this.chromatograms = chromatograms;
    }

    /**
     * <p>Getter for the field <code>name</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return name;
    }

    /**
     * <p>Getter for the field <code>chromatograms</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Chromatogram> getChromatograms() {
        return chromatograms;
    }

    /**
     * <p>Setter for the field <code>chromatograms</code>.</p>
     *
     * @param chromatograms a {@link java.util.List} object.
     */
    public void setChromatograms(List<Chromatogram> chromatograms) {
        this.chromatograms = chromatograms;
    }
}
