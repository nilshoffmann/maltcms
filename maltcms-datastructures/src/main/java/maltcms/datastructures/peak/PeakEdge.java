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
package maltcms.datastructures.peak;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Nils Hoffmann
 */
public final class PeakEdge implements Serializable {
	
	final UUID sourcePeakId, targetPeakId;//, edgeId;
	final double similarity;
	
	public PeakEdge(IPeak sourcePeak, IPeak targetPeak, double similarity) {
		this.sourcePeakId = sourcePeak.getUniqueId();
		this.targetPeakId = targetPeak.getUniqueId();
		this.similarity = similarity;
//		System.out.println("Peak Edge from "+sourcePeak.getAssociation()+" at "+sourcePeak.getPeakIndex()+ " to "+targetPeak.getAssociation()+" at "+targetPeak.getPeakIndex()+ " = "+similarity);
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 47 * hash + Objects.hashCode(this.sourcePeakId);
		hash = 47 * hash + Objects.hashCode(this.targetPeakId);
		return hash;
	}

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

	public UUID getSourcePeakId() {
		return sourcePeakId;
	}

	public UUID getTargetPeakId() {
		return targetPeakId;
	}

	public double getSimilarity() {
		return similarity;
	}
}