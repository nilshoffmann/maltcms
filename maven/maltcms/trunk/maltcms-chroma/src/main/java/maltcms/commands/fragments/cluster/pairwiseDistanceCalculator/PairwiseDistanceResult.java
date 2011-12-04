/*
 * $license$
 *
 * $Id$
 */
package maltcms.commands.fragments.cluster.pairwiseDistanceCalculator;

import cross.datastructures.StatsMap;
import cross.datastructures.tuple.Tuple2D;
import java.io.File;
import java.io.Serializable;
import lombok.Data;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public class PairwiseDistanceResult implements Serializable {
    private static final long serialVersionUID = 60123098123486L;
    private final Tuple2D<File,File> input;
    private final File alignment;
    private final double value;
    private final StatsMap statsMap;
}
