/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
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
