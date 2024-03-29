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
package net.sf.maltcms.evaluation.api.tasks;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * <p>ITask interface.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public interface ITask<T extends Serializable> extends Callable<T>, Serializable {

    /**
     * <p>getPostProcessors.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<IPostProcessor> getPostProcessors();

    /**
     * <p>getAdditionalEnvironment.</p>
     *
     * @return a {@link java.util.HashMap} object.
     */
    public HashMap<String, String> getAdditionalEnvironment();

    /**
     * <p>getCommandLine.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getCommandLine();

    /**
     * <p>getWorkingDirectory.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getWorkingDirectory();

    /**
     * <p>getOutputDirectory.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getOutputDirectory();

    /**
     * <p>getTaskId.</p>
     *
     * @return a {@link java.util.UUID} object.
     */
    public UUID getTaskId();
}
