/*
 * $license$
 *
 * $Id$
 */
package net.sf.maltcms.evaluation.api.tasks;

import java.io.Serializable;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public interface IPostProcessor extends Serializable {
    public void process(ITask task);
}
