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
 * @author Nils Hoffmann
 */
@Data
public class BiPaceBidirectionalBestHit implements IBidirectionalBestHit {

    private IScalarArraySimilarity similarity;
    private boolean useMeanMS = false;

    @Override
    public List<List<Point>> getBidiBestHitList(List<List<Peak2D>> peaklists) {
        List<List<Point>> bbhs = new ArrayList<>();
        findBBHs(peaklists, bbhs);
        return bbhs;
    }

    /**
     *
     * @param peaklists
     * @param bbhs
     */
    protected void findBBHs(List<List<Peak2D>> peaklists, List<List<Point>> bbhs) {
        throw new NotImplementedException();
    }

    /**
     *
     * @param p1
     * @param p2
     * @return
     */
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

    /**
     *
     */
    @Override
    public void clear() {
    }

    /**
     *
     * @param c
     */
    @Override
    public void configure(Configuration c) {
    }
}
