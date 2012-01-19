/*
 * $license$
 *
 * $Id$
 */
package maltcms.commands.fragments2d.peakfinding.bbh;

import cross.exception.NotImplementedException;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import maltcms.datastructures.peak.Peak2D;
import maltcms.math.functions.IScalarArraySimilarity;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public class BiPaceBidirectionalBestHit implements IBidirectionalBestHit {

    private IScalarArraySimilarity similarity;
    private boolean useMeanMS = false;

    @Override
    public List<List<Point>> getBidiBestHitList(List<List<Peak2D>> peaklists) {
        List<List<Point>> bbhs = new ArrayList<List<Point>>();
        findBBHs(peaklists,bbhs);
        return bbhs;
    }
    
    protected void findBBHs(List<List<Peak2D>> peaklists, List<List<Point>> bbhs) {
        throw new NotImplementedException();
    }

    @Override
    public double sim(Peak2D p1, Peak2D p2) {
        double sim = Double.NEGATIVE_INFINITY;
        if (this.useMeanMS) {
            sim = this.similarity.apply(new double[]{p1.getFirstRetTime(), p1.getSecondRetTime()}, new double[]{p2.getFirstRetTime(),
                        p2.getSecondRetTime()}, p1.getPeakArea().
                    getMeanMS(), p2.getPeakArea().getMeanMS());
        } else {
            sim = this.similarity.apply(new double[]{p1.getFirstRetTime(), p1.getSecondRetTime()}, new double[]{p2.getFirstRetTime(),
                        p2.getSecondRetTime()}, p1.getPeakArea().
                    getSeedMS(), p1.getPeakArea().getSeedMS());
        }
        return sim;
    }

    @Override
    public void clear() {
    }

    @Override
    public void configure(Configuration c) {
    }
}
