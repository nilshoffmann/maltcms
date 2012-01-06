/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.evaluation.spi.tasks.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import net.sf.maltcms.execution.api.ICompletionService;
import net.sf.maltcms.execution.spi.MaltcmsCompletionService;
import net.sf.maltcms.execution.spi.MaltcmsResubmissionCompletionService;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public abstract class APipeline<T extends Serializable> implements IPipeline<T> {

    private List<Callable<T>> pipelines = new ArrayList<Callable<T>>();
    private final String name = UUID.randomUUID().toString();
    protected ICompletionService<T> ics = new MaltcmsResubmissionCompletionService<T>(
            new MaltcmsCompletionService<T>());

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
