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
package net.sf.maltcms.evaluation.spi.tasks.maltcms;

import net.sf.maltcms.evaluation.api.tasks.ITaskResult;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: nilshoffmann Date: 10.12.11 Time: 11:00 To
 * change this template use File | Settings | File Templates.
 *
 * @author hoffmann
 * 
 */
public class DefaultTaskResult implements ITaskResult {

    private final List<File> taskInputs;
    private final List<File> taskOutputs;

    /**
     * <p>Constructor for DefaultTaskResult.</p>
     */
    public DefaultTaskResult() {
        taskInputs = new LinkedList<>();
        taskOutputs = new LinkedList<>();
    }
    /** Constant <code>EMPTY</code> */
    public static final ITaskResult EMPTY = new EmptyTaskResult();

    /** {@inheritDoc} */
    @Override
    public List<File> getTaskInputs() {
        return taskInputs;
    }

    /** {@inheritDoc} */
    @Override
    public List<File> getTaskOutputs() {
        return taskOutputs;
    }

    private static class EmptyTaskResult implements ITaskResult {

        @Override
        public List<File> getTaskInputs() {
            return Collections.emptyList();
        }

        @Override
        public List<File> getTaskOutputs() {
            return Collections.emptyList();
        }
    }
}
