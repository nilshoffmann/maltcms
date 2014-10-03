/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2013, The authors of Maltcms. All rights reserved.
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
package maltcms.commands.fragments.alignment.peakCliqueAlignment;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import maltcms.datastructures.peak.IPeak;

/**
 * <p>PeakEdge class.</p>
 *
 * @author Nils Hoffmann
 * 
 * @since 1.3.2
 */
public final class PeakEdge implements Serializable {

    final UUID sourcePeakId, targetPeakId;
    final double similarity;

    /**
     * <p>Constructor for PeakEdge.</p>
     *
     * @param sourcePeak a {@link maltcms.datastructures.peak.IPeak} object.
     * @param targetPeak a {@link maltcms.datastructures.peak.IPeak} object.
     * @param similarity a double.
     */
    public PeakEdge(IPeak sourcePeak, IPeak targetPeak, double similarity) {
        this.sourcePeakId = sourcePeak.getUniqueId();
        this.targetPeakId = targetPeak.getUniqueId();
        this.similarity = similarity;
//		System.out.println("Peak Edge from "+sourcePeak.getAssociation()+" at "+sourcePeak.getPeakIndex()+ " to "+targetPeak.getAssociation()+" at "+targetPeak.getPeakIndex()+ " = "+similarity);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.sourcePeakId);
        hash = 47 * hash + Objects.hashCode(this.targetPeakId);
        return hash;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PeakEdge other = (PeakEdge) obj;
        if (!Objects.equals(this.sourcePeakId, other.sourcePeakId)) {
            return false;
        }
        if (!Objects.equals(this.targetPeakId, other.targetPeakId)) {
            return false;
        }
        return true;
    }

    /**
     * <p>Getter for the field <code>sourcePeakId</code>.</p>
     *
     * @return a {@link java.util.UUID} object.
     */
    public UUID getSourcePeakId() {
        return sourcePeakId;
    }

    /**
     * <p>Getter for the field <code>targetPeakId</code>.</p>
     *
     * @return a {@link java.util.UUID} object.
     */
    public UUID getTargetPeakId() {
        return targetPeakId;
    }

    /**
     * <p>Getter for the field <code>similarity</code>.</p>
     *
     * @return a double.
     */
    public double getSimilarity() {
        return similarity;
    }

}
