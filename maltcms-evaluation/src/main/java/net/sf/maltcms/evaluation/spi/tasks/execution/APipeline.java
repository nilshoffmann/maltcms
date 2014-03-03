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
package net.sf.maltcms.evaluation.spi.tasks.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import net.sf.mpaxs.api.ICompletionService;
import net.sf.mpaxs.spi.concurrent.MpaxsCompletionService;
import net.sf.mpaxs.spi.concurrent.MpaxsResubmissionCompletionService;

/**
 *
 * @author Nils Hoffmann
 */
public abstract class APipeline<T extends Serializable> implements IPipeline<T> {

    private List<Callable<T>> pipelines = new ArrayList<Callable<T>>();
    private final String name = UUID.randomUUID().toString();
    protected ICompletionService<T> ics = new MpaxsResubmissionCompletionService<T>(
        new MpaxsCompletionService<T>());

    public String getName() {
        return name;
    }

    public List<Callable<T>> getPipelines() {
        return pipelines;
    }

    public void setPipelines(List<Callable<T>> pipelines) {
        this.pipelines = pipelines;
    }
}
