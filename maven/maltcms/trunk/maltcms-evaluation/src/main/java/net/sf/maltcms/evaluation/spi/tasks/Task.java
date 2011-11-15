/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.evaluation.spi.tasks;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.Data;
import net.sf.maltcms.evaluation.api.IPostProcessor;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public abstract class Task implements Callable<List<File>>{

    private List<String> commandLine = Collections.emptyList();
    private List<IPostProcessor> postProcessor = Collections.emptyList();
    
    
    
}
