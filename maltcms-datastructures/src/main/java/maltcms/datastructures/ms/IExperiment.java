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

import java.util.HashMap;

import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import cross.exception.ResourceNotAvailableException;

/**
 * Interface representing an experiment. Delegate/Facade to cover a real
 * IFileFragment.
 *
 * @author Nils Hoffmann
 * 
 */
public interface IExperiment extends IConfigurable, IFileFragment {

    /**
     * <p>getFileFragment.</p>
     *
     * @return a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public IFileFragment getFileFragment();

    /**
     * <p>getMetadata.</p>
     *
     * @return a {@link java.util.HashMap} object.
     */
    public HashMap<String, String> getMetadata();

    /**
     * <p>setFileFragment.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public void setFileFragment(IFileFragment ff);

    /**
     * <p>setMetadata.</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setMetadata(String key, String value);

    /**
     * <p>getMetadata.</p>
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws cross.exception.ResourceNotAvailableException if any.
     */
    public String getMetadata(String key) throws ResourceNotAvailableException;
}
