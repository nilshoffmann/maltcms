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

import cross.IConfigurable;

/**
 * Interface giving access to specific scans within an experiment.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 * @param <T extends IScan> provides concrete implementations of an IScan
 */
public interface IScanProvider<T extends IScan> extends IConfigurable, Iterable<T> {

    /**
     *
     * @param i the scan index to retrieve
     * @return the IScan
     */
    public T getScan(int i);

    /**
     *
     * @return the number of scans
     */
    public int getNumberOfScans();
}