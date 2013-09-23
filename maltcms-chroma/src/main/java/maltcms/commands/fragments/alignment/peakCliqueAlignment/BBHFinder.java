/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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

import cross.datastructures.tuple.Tuple2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.peak.IBipacePeak;

/**
 *
 * @author nils
 */
@Slf4j
public class BBHFinder {

	public BBHPeakEdgeSet findBiDiBestHits(List<? extends IBipacePeak> a, List<? extends IBipacePeak> b) {
		final BBHPeakEdgeSet matchedPeaks = new BBHPeakEdgeSet();
		String laassociation = a.get(0).getAssociation();
		String lbassociation = b.get(0).getAssociation();
		if(laassociation.equals(lbassociation)) {
			return matchedPeaks;
		}
		Map<UUID, IBipacePeak> peaks = new HashMap<UUID, IBipacePeak>();
		for (IBipacePeak p : b) {
			peaks.put(p.getUniqueId(), p);
		}
		for (IBipacePeak lapeak : a) {
			UUID bestPeak = lapeak.getPeakWithHighestSimilarity(lbassociation);
			IBipacePeak other = peaks.get(bestPeak);
			if (other != null) {
				UUID otherBestPeak = other.getPeakWithHighestSimilarity(laassociation);
				if (otherBestPeak != null && lapeak.getUniqueId().equals(otherBestPeak)) {
					Tuple2D<IBipacePeak, IBipacePeak> t = new Tuple2D<>(lapeak, other);
					matchedPeaks.add(t);
				}
			}
		}
		return matchedPeaks;
	}
}
