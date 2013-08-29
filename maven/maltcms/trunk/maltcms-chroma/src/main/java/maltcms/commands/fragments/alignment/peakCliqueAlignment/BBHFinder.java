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
import maltcms.datastructures.peak.IPeak;

/**
 *
 * @author nils
 */
@Slf4j
public class BBHFinder {

	public BBHPeaksList findBiDiBestHits(List<? extends IPeak> a, List<? extends IPeak> b) {
		final Set<Tuple2D<UUID,UUID>> matchedPeaks = new LinkedHashSet<Tuple2D<UUID,UUID>>();
		for (final IPeak plhs : a) {
			for (final IPeak prhs : b) {
				log.debug("Checking peaks {} and {}", plhs, prhs);
				if (plhs.isBidiBestHitFor(prhs)) {
					log.debug(
							"Found a bidirectional best hit: {} and {}",
							plhs, prhs);
					matchedPeaks.add(new Tuple2D<UUID,UUID>(plhs.getUniqueId(),prhs.getUniqueId()));
//					matchedPeaks.add(prhs);
					plhs.retainSimilarityRemoveRest(prhs);
					prhs.retainSimilarityRemoveRest(plhs);
				}
			}
		}
		log.info("Matched peak pairs: {}",matchedPeaks.size());
		return new BBHPeaksList(matchedPeaks);
	}
}
