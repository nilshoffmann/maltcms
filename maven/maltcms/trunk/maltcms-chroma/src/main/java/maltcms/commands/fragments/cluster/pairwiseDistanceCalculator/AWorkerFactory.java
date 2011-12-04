/*
 * $license$
 *
 * $Id$
 */
package maltcms.commands.fragments.cluster.pairwiseDistanceCalculator;

import java.io.Serializable;

/**
 * Extend this Factory in order to create custom worker instances.
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public abstract class AWorkerFactory implements Serializable {

    public abstract PairwiseDistanceWorker create();
}
