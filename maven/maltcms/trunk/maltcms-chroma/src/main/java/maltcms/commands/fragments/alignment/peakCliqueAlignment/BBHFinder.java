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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.feature.PairwiseValueMap;
import maltcms.datastructures.peak.IPeak;
import maltcms.datastructures.peak.PeakEdge;

/**
 *
 * @author nils
 */
@Slf4j
public class BBHFinder {
	
	public BBHPeakEdgeList findBiDiBestHits(List<? extends IPeak> a, List<? extends IPeak> b) {
		final Set<Tuple2D<PeakEdge, PeakEdge>> matchedPeaks = new LinkedHashSet<Tuple2D<PeakEdge, PeakEdge>>();
		for (IPeak lapeak : a) {
			for (IPeak lbpeak : b) {
				if (lapeak != lbpeak && lapeak.isBidiBestHitFor(lbpeak)) {
					Tuple2D<PeakEdge, PeakEdge> t = new Tuple2D<>(new PeakEdge(lapeak, lbpeak, lapeak.getSimilarity(lbpeak)),
							new PeakEdge(lbpeak, lapeak, lapeak.getSimilarity(lapeak)));
					matchedPeaks.add(t);
				}
			}
		}
		log.debug("Found {} BBH peak pairs!", matchedPeaks.size());
		return new BBHPeakEdgeList(matchedPeaks);
	}
}
