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

import java.util.List;
import java.util.UUID;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
public interface IBipacePeak extends IPeak{

	/**
	 * Add a similarity to Peak p. Resets the sortedPeaks list for the
	 * associated FileFragment of Peak p, so that a subsequent call to
	 * getPeakWithHighestSimilarity or getPeaksSortedBySimilarity will rebuild
	 * the list of peaks sorted ascending according to their similarity to this
	 * peak.
	 *
	 * @param p
	 * @param similarity
	 */
	void addSimilarity(final IBipacePeak p, final double similarity);

	void clearSimilarities(String association);

	Array getMsIntensities();
	
	void setMsIntensities(Array a);

	UUID getPeakWithHighestSimilarity(final String key);

	/**
	 * Only call this method, after having added all similarities!
	 *
	 * @param key
	 * @return
	 */
	List<UUID> getPeaksSortedBySimilarity(final String key);

	double getSimilarity(final IBipacePeak p);

	boolean isBidiBestHitFor(final IBipacePeak p);

	void retainSimilarityRemoveRest(final IBipacePeak p);
	
}
