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
package net.sf.maltcms.evaluation.spi.xcalibur;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
public class Creator {

    private String name;
    private String creatorVersion;

    public Creator(String name, String creatorVersion) {
        this.name = name;
        this.creatorVersion = creatorVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatorVersion() {
        return creatorVersion;
    }

    public void setCreatorVersion(String creatorVersion) {
        this.creatorVersion = creatorVersion;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName() + " " + getCreatorVersion());
        return sb.toString();
    }
}
