/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.evaluation.spi.tasks.execution;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class FilePipeline extends APipeline<File> {

    @Override
    public List<File> call() throws Exception {
        for(Callable<File> pipe:getPipelines()) {
            ics.submit(pipe);
        }
        return ics.call();
    }
    
}
